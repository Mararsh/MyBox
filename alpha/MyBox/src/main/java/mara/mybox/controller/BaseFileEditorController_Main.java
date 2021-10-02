package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.AnchorPane;
import mara.mybox.data.FileEditInformation.Edit_Type;
import mara.mybox.data.FindReplaceString;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-12-09
 * @License Apache License Version 2.0
 */
public abstract class BaseFileEditorController_Main extends BaseFileEditorController_Pair {

    protected void initMainBox() {
        try {
            if (mainArea == null) {
                return;
            }
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
            MyBoxLog.error(e.toString());
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
        String info = message("SelectionInPage") + ": "
                + StringTools.format(pageStart + 1) + " - " + StringTools.format(pageEnd)
                + " (" + StringTools.format(len == 0 ? 0 : pageEnd - pageStart) + ")";
        if (sourceInformation != null && sourceInformation.getPagesNumber() > 1 && sourceInformation.getCurrentPage() > 0) {
            long fileStart = sourceInformation.getCurrentPageObjectStart() + pageStart;
            long fileEnd = len == 0 ? fileStart + 1 : sourceInformation.getCurrentPageObjectStart() + pageEnd;
            info += "  " + message("SelectionInFile") + ": "
                    + StringTools.format(fileStart + 1) + " - " + StringTools.format(fileEnd);
        }
        selectionLabel.setText(info);
    }

    protected boolean formatMainArea() {
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
        } else {
            StringBuilder lines = new StringBuilder();
            for (long i = from + 1; i < to; i++) {
                lines.append(i).append("\n");
            }
            lines.append(to);
            lineArea.setText(lines.toString());
        }
        isSettingValues = false;
        adjustLinesArea();
    }

    protected void adjustLinesArea() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    lineArea.setScrollTop(mainArea.getScrollTop());
                    AnchorPane.setLeftAnchor(mainArea, (sourceInformation.getLinesNumber() + "").length() * AppVariables.sceneFontSize + 20d);

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
                });
            }
        }, 500);   // Wait for text loaded
    }

    protected boolean validMainArea() {
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

        if (changed && !validMainArea()) {
            if (editLabel != null) {
                editLabel.setText(message("InvalidData"));
            }
            mainArea.setStyle(NodeStyleTools.badStyle);
//            popError(message("InvalidData"));
        } else if (editLabel != null) {
            editLabel.setText("");
            mainArea.setStyle(null);
        }

        if (okButton != null) {
            okButton.setDisable(changed);
        }
        if (autoSaveCheck != null) {
            autoSaveCheck.setDisable(sourceFile == null);
        }
    }

    protected void updateNumbers(boolean changed) {
        saveButton.setDisable(false);
        if (saveAsButton != null) {
            saveAsButton.setDisable(false);
        }
        if (locatePane != null) {
            locatePane.setDisable(false);
        }
        String pageText = mainArea.getText();
        if (pageText == null) {
            pageText = "";
        }
        int pageLinesNumber = FindReplaceString.count(pageText, "\n") + 1;
        int pageObjectsNumber;
        if (editType == Edit_Type.Bytes) {
            pageObjectsNumber = pageText.replaceAll("\\s+|\n", "").length() / 2;
        } else {
            pageObjectsNumber = pageText.length();
        }
        long pageObjectStart = 0, pageObjectEnd = pageObjectsNumber;
        long pageLineStart = 0, pageLineEnd = pageLinesNumber, pagesNumber = 1;
        long fileObjectNumber = pageObjectsNumber;
        long fileLinesNumber = pageLinesNumber;
        int pageSize = sourceInformation.getPageSize();
        long currentPage = sourceInformation.getCurrentPage();
        String fileInfo = "";
        if (sourceFile == null) {
            if (pageLabel != null) {
                pageLabel.setText("");
            }
            sourceInformation.setObjectsNumber(pageObjectsNumber);
            sourceInformation.setLinesNumber(pageLinesNumber);
            writeLineNumbers(0, pageLinesNumber);
        } else {
            pageLineStart = sourceInformation.getCurrentPageLineStart();
            pageLineEnd = pageLineStart + pageLinesNumber;
            writeLineNumbers(pageLineStart, pageLineEnd);

            pageObjectStart = sourceInformation.getCurrentPageObjectStart();
            pageObjectEnd = pageObjectStart + pageObjectsNumber;

            if (!sourceInformation.isTotalNumberRead()) {
                saveButton.setDisable(true);
                if (saveAsButton != null) {
                    saveAsButton.setDisable(true);
                }
                if (locatePane != null) {
                    locatePane.setDisable(true);
                }
                fileInfo = message("CountingTotalNumber") + "\n\n";
            } else {
                fileObjectNumber = sourceInformation.getObjectsNumber();
                fileLinesNumber = sourceInformation.getLinesNumber();

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
            fileInfo += message("FileSize") + ": " + FileTools.showFileSize(sourceFile.length()) + "\n"
                    + message("FileModifyTime") + ": " + DateTools.datetimeToString(sourceFile.lastModified()) + "\n"
                    + (editType == Edit_Type.Bytes ? message("BytesNumberInFile") : message("CharactersNumberInFile"))
                    + ": " + StringTools.format(fileObjectNumber) + "\n"
                    + message("LinesNumberInFile") + ": " + StringTools.format(fileLinesNumber) + "\n"
                    + (editType == Edit_Type.Bytes ? message("BytesPerPage") : message("LinesPerPage"))
                    + ": " + StringTools.format(pageSize) + "\n"
                    + message("CurrentPage") + ": " + StringTools.format(currentPage + 1)
                    + " / " + StringTools.format(pagesNumber) + "\n";
        }

        String objectInfo, lineInfo;
        if (pagesNumber > 1) {
            objectInfo = editType == Edit_Type.Bytes ? message("BytesRangeInPage") : message("CharactersRangeInPage");
            objectInfo += ": " + StringTools.format(pageObjectStart + 1) + " - " + StringTools.format(pageObjectEnd)
                    + " ( " + StringTools.format(pageObjectsNumber) + " )\n";
            lineInfo = message("LinesRangeInPage")
                    + ": " + StringTools.format(pageLineStart + 1) + " - " + StringTools.format(pageLineEnd)
                    + " ( " + StringTools.format(pageLinesNumber) + " )\n";
        } else {
            objectInfo = editType == Edit_Type.Bytes ? message("BytesNumberInPage") : message("CharactersNumberInPage");
            objectInfo += ": " + StringTools.format(pageObjectsNumber) + "\n";
            lineInfo = message("LinesNumberInPage") + ": " + StringTools.format(pageLinesNumber) + "\n";
        }
        fileInfo += message("LineBreak") + ": " + sourceInformation.lineBreakName() + "\n"
                + message("Charset") + ": " + sourceInformation.getCharset().name() + "\n"
                + objectInfo + lineInfo
                + message("PageModifyTime") + ": " + DateTools.nowString();
        fileLabel.setText(fileInfo);

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

        if (pagesNumber > 1) {
            locatePane.getContent().setDisable(changed);
            filterPane.getContent().setDisable(changed);
        }
        if (filterController != null) {
            filterController.isBytes = editType == Edit_Type.Bytes;
            filterController.maxLen = pageSize;
            filterController.sourceLen = pageObjectsNumber;
            filterController.checkFilterStrings();
        }
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
                    mainArea.requestFocus();
                    mainArea.deselect();
                    mainArea.selectRange(0, 0);
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
                    mainArea.selectRange(Math.min(text.length(), startIndex + number), startIndex);
                });
            }
        }, 500);  // wait for text loaded
    }

    // 0-based
    protected void selectLines(long from, int number) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    mainArea.requestFocus();
                    mainArea.deselect();
                    mainArea.selectRange(0, 0);
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
                    mainArea.selectRange(charIndex, startIndex);
                });
            }
        }, 500); // wait for text loaded
    }

}
