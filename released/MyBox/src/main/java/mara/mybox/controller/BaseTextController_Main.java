package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.AnchorPane;
import mara.mybox.data.FileEditInformation.Edit_Type;
import mara.mybox.data.FindReplaceString;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-12-09
 * @License Apache License Version 2.0
 */
public abstract class BaseTextController_Main extends BaseTextController_Pair {

    protected void initMainBox() {
        try {
            if (mainArea == null) {
                return;
            }
            lastPageFrom = lastPageTo = -1;

            mainArea.setStyle("-fx-highlight-fill: dodgerblue; -fx-highlight-text-fill: white;");

            mainArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    updateInterface(true);
                }

            });

            mainArea.scrollTopProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    if (UserConfig.getBoolean(baseName + "ScrollSynchronously", false)) {
                        scrollTopPairArea(newValue.doubleValue());
                    }
                    isSettingValues = true;
                    lineArea.setScrollTop(newValue.doubleValue());
                    isSettingValues = false;
                }
            });

            mainArea.scrollLeftProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    if (UserConfig.getBoolean(baseName + "ScrollSynchronously", false)) {
                        scrollLeftPairArea(newValue.doubleValue());
                    }
                }
            });
            mainArea.selectionProperty().addListener(new ChangeListener<IndexRange>() {
                @Override
                public void changed(ObservableValue ov, IndexRange oldValue, IndexRange newValue) {
                    checkMainAreaSelection();
                }
            });

            mainArea.widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    adjustLinesArea();
                }
            });

            lineArea.scrollTopProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    if (UserConfig.getBoolean(baseName + "ScrollSynchronously", false)) {
                        scrollTopPairArea(newValue.doubleValue());
                    }
                    isSettingValues = true;
                    mainArea.setScrollTop(newValue.doubleValue());
                    isSettingValues = false;
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void checkMainAreaSelection() {
        if (isSettingValues) {
            return;
        }
        setPairAreaSelection();
        IndexRange currentSelection = mainArea.getSelection();
        int len = currentSelection.getLength();
        long pageStart = 0, pageEnd;
        if (editType == Edit_Type.Bytes) {
            pageStart = currentSelection.getStart() / 3;
            pageEnd = len == 0 ? pageStart + 1 : currentSelection.getEnd() / 3;
        } else {
            pageStart = currentSelection.getStart();
            pageEnd = len == 0 ? pageStart + 1 : currentSelection.getEnd();
        }
        String info = message("SelectionInPage") + ": ["
                + StringTools.format(pageStart + 1) + " - " + StringTools.format(pageEnd)
                + "] " + StringTools.format(len == 0 ? 0 : pageEnd - pageStart);
        if (sourceInformation != null && sourceInformation.getPagesNumber() > 1 && sourceInformation.getCurrentPage() > 0) {
            long fileStart = sourceInformation.getCurrentPageObjectStart() + pageStart;
            long fileEnd = len == 0 ? fileStart + 1 : sourceInformation.getCurrentPageObjectStart() + pageEnd;
            info += "  " + message("SelectionInFile") + ": "
                    + StringTools.format(fileStart + 1) + " - " + StringTools.format(fileEnd);
        }
        selectionLabel.setText(info);
    }

    protected boolean validateMainArea() {
        return true;
    }

    protected void setMainArea(String text) {
        if (isSettingValues) {
            return;
        }
        mainArea.setText(text);
    }

    // 0-based, excluded
    protected void writeLineNumbers(long from, long to) {
        if (isSettingValues) {
            return;
        }
        isSettingValues = true;
        if (from < 0 || from > to) {
            lineArea.clear();
        } else if (from != lastPageFrom || to != lastPageTo) {
            StringBuilder lines = new StringBuilder();
            for (long i = from + 1; i < to; i++) {
                lines.append(i).append("\n");
            }
            lines.append(to);
            lineArea.setText(lines.toString());
        }
        lastPageFrom = from;
        lastPageTo = to;
        isSettingValues = false;
        adjustLinesArea();
    }

    protected void adjustLinesArea() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    isSettingValues = true;
                    lineArea.setScrollTop(mainArea.getScrollTop());
                    AnchorPane.setLeftAnchor(mainArea, (sourceInformation.getCurrentPageLineEnd() + "").length() * AppVariables.sceneFontSize + 5d);

                    // https://stackoverflow.com/questions/51075499/javafx-tableview-how-to-tell-if-scrollbar-is-visible
                    double barHeight = 0;
                    for (Node n : mainArea.lookupAll(".scroll-bar")) {
                        ScrollBar bar = (ScrollBar) n;
//                        MyBoxLog.console(bar.getWidth() + " " + bar.getHeight() + " " + bar.getOrientation() + " " + bar.isVisible());
                        if (bar.getOrientation().equals(Orientation.HORIZONTAL) && bar.isVisible()) {
                            barHeight = bar.getHeight();
                            break;
                        }
                    }
                    AnchorPane.setBottomAnchor(lineArea, barHeight);
                    isSettingValues = false;
                });
                Platform.requestNextPulse();
            }
        }, 300);   // Wait for text loaded
    }

    @FXML
    protected boolean formatMainArea() {
        return true;
    }

    @Override
    protected void updateInterface(boolean changed) {
        updateControls(changed);
        updateNumbers(changed);
        updatePairArea();
    }

    protected void updateControls(boolean changed) {
        fileChanged.set(changed);
        bottomLabel.setText("");
        selectionLabel.setText("");
        if (getMyStage() == null || sourceInformation == null) {
            return;
        }
        String t = getBaseTitle();
        if (sourceFile != null) {
            t += "  " + sourceFile.getAbsolutePath();
        }
        if (fileChanged.getValue()) {
            getMyStage().setTitle(t + "*");
        } else {
            getMyStage().setTitle(t);
        }

        if (changed && !validateMainArea()) {
            mainArea.setStyle(UserConfig.badStyle());
            popError(message("InvalidData"));
        } else {
            mainArea.setStyle(null);
        }

        if (okButton != null) {
            okButton.setDisable(changed);
        }
    }

    protected int pageLinesNumber(String pageText) {
        if (pageText == null) {
            return 0;
        }
        return FindReplaceString.count(null, pageText, "\n") + 1;
    }

    protected int pageObjectsNumber(String pageText) {
        if (pageText == null) {
            return 0;
        }
        if (editType == Edit_Type.Bytes) {
            return pageText.replaceAll("\\s+|\n", "").length() / 2;
        } else {
            return pageText.length();
        }
    }

    protected void updateNumbers(boolean changed) {
        buttonsPane.setDisable(false);
        String pageText = mainArea.getText();
        if (pageText == null) {
            pageText = "";
        }
        int pageLinesNumber = pageLinesNumber(pageText);
        int pageObjectsNumber = pageObjectsNumber(pageText);
        long pagesNumber = 1;
        int pageSize = sourceInformation.getPageSize();
        long currentPage = sourceInformation.getCurrentPage();
        if (sourceFile == null) {
            if (pageLabel != null) {
                pageLabel.setText("");
            }
            sourceInformation.setObjectsNumber(pageObjectsNumber);
            sourceInformation.setLinesNumber(pageLinesNumber);
            writeLineNumbers(0, pageLinesNumber);
        } else {
            long pageLineStart = sourceInformation.getCurrentPageLineStart();
            long pageLineEnd = pageLineStart + pageLinesNumber;
            writeLineNumbers(pageLineStart, pageLineEnd);

            if (!sourceInformation.isTotalNumberRead()) {
                buttonsPane.setDisable(true);
            } else {
                long fileObjectNumber = sourceInformation.getObjectsNumber();
                long fileLinesNumber = sourceInformation.getLinesNumber();

                if (editType == Edit_Type.Bytes) {
                    pagesNumber = fileObjectNumber / pageSize;
                    if (fileObjectNumber % pageSize > 0) {
                        pagesNumber++;
                    }
                } else {
                    pagesNumber = fileLinesNumber / pageSize;
                    if (fileLinesNumber % pageSize > 0) {
                        pagesNumber++;
                    }
                }

                sourceInformation.setPagesNumber(pagesNumber);
            }
        }

        pageBox.setDisable(changed);
        pagePreviousButton.setDisable(currentPage <= 0 || pagesNumber < 2);
        pageNextButton.setDisable(currentPage >= pagesNumber - 1 || pagesNumber < 2);
        List<String> pages = new ArrayList<>();
        for (int i = 1; i <= pagesNumber; i++) {
            pages.add(i + "");
        }
        isSettingValues = true;
        pageSelector.getItems().clear();
        pageSelector.getItems().setAll(pages);
        pageLabel.setText("/" + pagesNumber);
        pageSelector.setValue((currentPage + 1) + "");
        pageSelector.getEditor().setStyle(null);
        pageSizeSelector.setValue(StringTools.format(pageSize));
        isSettingValues = false;
    }

    protected void loadText(String text, boolean changed) {
        isSettingValues = true;
        mainArea.setText(text);
        isSettingValues = false;
        formatMainArea();
        updateInterface(changed);
    }

    protected boolean locateLine(long line) {
        if (line < 0 || line >= sourceInformation.getLinesNumber()) {
            popError(message("InvalidParameter") + ": " + message("LineNumber"));
            return false;
        }
        if (sourceFile == null || sourceInformation.getPagesNumber() <= 1) {
            selectLine(line);
        } else {
            if (line >= sourceInformation.getCurrentPageLineStart()
                    && line < sourceInformation.getCurrentPageLineEnd()) {
                selectLine(line - sourceInformation.getCurrentPageLineStart());
            } else {
                if (!checkBeforeNextAction()) {
                    return false;
                }
                if (task != null) {
                    task.cancel();
                }
                task = new FxSingletonTask<Void>(this) {

                    String text;

                    @Override
                    protected boolean handle() {
                        text = sourceInformation.readLine(this, line);
                        return text != null;
                    }

                    @Override
                    protected void whenSucceeded() {
                        loadText(text, false);
                        selectLine(line - sourceInformation.getCurrentPageLineStart());
                    }

                };
                start(task);
            }
        }
        return true;
    }

    protected boolean locateObject(long locate) {
        if (locate < 0 || locate >= sourceInformation.getObjectsNumber()) {
            popError(message("InvalidParameters"));
            return false;
        }
        int unit = sourceInformation.getObjectUnit();
        if (sourceFile == null || sourceInformation.getPagesNumber() <= 1) {
            selectObjects(locate * unit, unit);

        } else {
            if (locate >= sourceInformation.getCurrentPageObjectStart()
                    && locate < sourceInformation.getCurrentPageObjectEnd()) {
                selectObjects((locate - sourceInformation.getCurrentPageObjectStart()) * unit, unit);
            } else {
                if (!checkBeforeNextAction()) {
                    return false;
                }
                if (task != null) {
                    task.cancel();
                }
                task = new FxSingletonTask<Void>(this) {

                    String text;

                    @Override
                    protected boolean handle() {
                        text = sourceInformation.readObject(this, locate);
                        return text != null;
                    }

                    @Override
                    protected void whenSucceeded() {
                        loadText(text, false);
                        selectObjects((locate - sourceInformation.getCurrentPageObjectStart()) * unit, unit);
                    }

                };
                start(task);
            }
        }
        return true;
    }

    protected boolean locateLinesRange(long from, long to) {
        if (from < 0 || from >= sourceInformation.getLinesNumber()) {
            popError(message("InvalidParameters") + ": " + message("LinesRange"));
            return false;
        }
        if (to < 0 || to > sourceInformation.getLinesNumber() || from > to) {
            popError(message("InvalidParameters") + ": " + message("LinesRange"));
            return false;
        }
        int number = (int) (to - from);
        if (sourceFile == null || sourceInformation.getPagesNumber() <= 1) {
            selectLines(from, number);
        } else {
            if (from >= sourceInformation.getCurrentPageLineStart() && to <= sourceInformation.getCurrentPageLineEnd()) {
                selectLines(from - sourceInformation.getCurrentPageLineStart(), number);
            } else {
                if (!checkBeforeNextAction()) {
                    return false;
                }
                if (task != null) {
                    task.cancel();
                }
                task = new FxSingletonTask<Void>(this) {

                    String text;

                    @Override
                    protected boolean handle() {
                        text = sourceInformation.readLines(this, from, number);
                        return text != null;
                    }

                    @Override
                    protected void whenSucceeded() {
                        loadText(text, false);
                        selectLines(from - sourceInformation.getCurrentPageLineStart(), number);
                    }

                };
                start(task);
            }
        }
        return true;
    }

    protected boolean locateObjectsRange(long from, long to) {
        if (from < 0 || from >= sourceInformation.getObjectsNumber()) {
            popError(message("InvalidParameters") + ": " + message("From"));
            return false;
        }
        if (to < 0 || to > sourceInformation.getObjectsNumber() || from > to) {
            popError(message("InvalidParameters") + ": " + message("To"));
            return false;
        }
        int len = (int) (to - from), unit = sourceInformation.getObjectUnit();
        if (sourceFile == null || sourceInformation.getPagesNumber() <= 1) {
            selectObjects(from * unit, len * unit);

        } else {
            if (from >= sourceInformation.getCurrentPageObjectStart() && to <= sourceInformation.getCurrentPageObjectEnd()) {
                selectObjects((from - sourceInformation.getCurrentPageObjectStart()) * unit, len * unit);

            } else {
                if (!checkBeforeNextAction()) {
                    return false;
                }
                if (task != null) {
                    task.cancel();
                }
                task = new FxSingletonTask<Void>(this) {

                    String text;

                    @Override
                    protected boolean handle() {
                        text = sourceInformation.readObjects(this, from, len);
                        return text != null;
                    }

                    @Override
                    protected void whenSucceeded() {
                        loadText(text, false);
                        selectObjects((from - sourceInformation.getCurrentPageObjectStart()) * unit, len * unit);
                    }

                };
                start(task);
            }
        }
        return true;
    }

    // 0-based
    protected void selectLine(long line) {
        selectLines(line, 1);
    }

    // 0-based
    protected void selectObject(long index) {
        selectObjects(index, 1);
    }

    // 0-based
    protected void selectObjects(long from, int number) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    int startIndex;
                    if (from < 0) {
                        startIndex = 0;
                    } else {
                        startIndex = (int) from;
                    }
                    String text = mainArea.getText();
                    if (text == null || text.isEmpty()) {
                        return;
                    }
                    mainArea.requestFocus();
                    mainArea.selectRange(Math.min(text.length(), startIndex + number), startIndex);
                });
                Platform.requestNextPulse();
            }
        }, 300);  // wait for text loaded
    }

    // 0-based
    protected void selectLines(long from, int number) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    int startLine;
                    if (from < 0) {
                        startLine = 0;
                    } else {
                        startLine = (int) from;
                    }
                    String text = mainArea.getText();
                    if (text == null || text.isEmpty()) {
                        return;
                    }
                    String[] lines = text.split("\n", -1);
                    int charIndex = 0, startIndex = 0;
                    int endLine = Math.min(lines.length, startLine + number);
                    for (int i = 0; i < endLine; ++i) {
                        if (i == startLine) {
                            startIndex = charIndex;
                        }
                        charIndex += lines[i].length() + 1;
                    }
                    mainArea.requestFocus();
                    mainArea.selectRange(charIndex, startIndex);
                });
                Platform.requestNextPulse();
            }
        }, 300); // wait for text loaded
    }

}
