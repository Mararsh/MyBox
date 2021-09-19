package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.IndexRange;
import javafx.scene.layout.AnchorPane;
import mara.mybox.data.FileEditInformation.Edit_Type;
import mara.mybox.data.FileEditInformation.Line_Break;
import mara.mybox.data.FindReplaceFile;
import mara.mybox.data.FindReplaceString;
import mara.mybox.data.FindReplaceString.Operation;
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
        long pageStart = 0, pageEnd;
        // end of range is *excluded* when handled internally, while it is *included* when displayed
        if (editType == Edit_Type.Bytes) {
            pageStart = currentSelection.getStart() / 3 + 1;
            pageEnd = currentSelection.getLength() == 0 ? pageStart : currentSelection.getEnd() / 3;

        } else {
            pageStart = currentSelection.getStart() + 1;
            pageEnd = currentSelection.getLength() == 0 ? pageStart : currentSelection.getEnd();
            if (sourceInformation != null && sourceInformation.getLineBreak().equals(Line_Break.CRLF)) {
                String sub = mainArea.getText(0, currentSelection.getStart());
                int startLinesNumber = FindReplaceString.count(sub, "\n");
                pageStart += startLinesNumber;
                sub = mainArea.getText(currentSelection.getStart(), currentSelection.getEnd());
                int linesNumber = FindReplaceString.count(sub, "\n");
                pageEnd += startLinesNumber + linesNumber;
            }
        }
        String info = message("SelectionInPage") + ": "
                + StringTools.format(pageStart) + " - " + StringTools.format(pageEnd)
                + " (" + StringTools.format(currentSelection.getLength() == 0 ? 0 : pageEnd - pageStart + 1) + ")";
        if (sourceInformation != null
                && sourceInformation.getPagesNumber() > 1 && sourceInformation.getCurrentPage() > 1) {
            long fileStart = sourceInformation.getCurrentPageObjectStart() + pageStart;
            long fileEnd = sourceInformation.getCurrentPageObjectStart() + pageEnd;
            info += "  " + message("SelectionInFile") + ": "
                    + StringTools.format(fileStart) + " - " + StringTools.format(fileEnd);
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

    // include "to"
    protected void setLines(long from, long to) {
        if (isSettingValues) {
            return;
        }
        isSettingValues = true;
        if (from < 0 || to <= 0 || from > to) {
            lineArea.clear();
        } else {
            StringBuilder lines = new StringBuilder();
            for (long i = from; i <= to; ++i) {
                lines.append(i).append("\n");
            }
            lineArea.setText(lines.toString());
        }
        lineArea.setScrollTop(mainArea.getScrollTop());
        AnchorPane.setLeftAnchor(mainArea, (to + "").length() * AppVariables.sceneFontSize + 20d);
        isSettingValues = false;
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
        String pageText = mainArea.getText();
        int pageObjectsNumber;
        int pageLinesNumber = FindReplaceString.count(pageText, "\n") + 1;
        if (editType == Edit_Type.Bytes) {
            pageObjectsNumber = pageText.replaceAll("\\s+|\n", "").length() / 2;
        } else {
            pageObjectsNumber = pageText.length();
            if (Line_Break.CRLF.equals(sourceInformation.getLineBreak())) {
                pageObjectsNumber += pageLinesNumber - 1;
            }
        }
        long pageObjectStart = 1;
        long pageObjectEnd = pageObjectsNumber;
        long pageLineStart = 1, pageLineEnd = pageLinesNumber, pagesNumber = 1;
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
            setLines(1, pageLinesNumber);
        } else {
            pageLineStart = sourceInformation.getCurrentPageLineStart();
            pageLineEnd = pageLineStart + pageLinesNumber - 1;
            setLines(pageLineStart, pageLineEnd);
            pageObjectStart = sourceInformation.getCurrentPageObjectStart() + 1;
            pageObjectEnd = sourceInformation.getCurrentPageObjectStart() + pageObjectsNumber;
            if (!sourceInformation.isTotalNumberRead()) {
                saveButton.setDisable(true);
                if (saveAsButton != null) {
                    saveAsButton.setDisable(true);
                }

                if (locateObjectButton != null) {
                    locateObjectButton.setDisable(true);
                    locateLineButton.setDisable(true);
                }
                fileInfo = message("CountingTotalNumber") + "\n\n";
            } else {
                fileObjectNumber = sourceInformation.getObjectsNumber();
                fileLinesNumber = sourceInformation.getLinesNumber();

                pagesNumber = fileObjectNumber / pageSize;
                if (fileObjectNumber % pageSize > 0) {
                    pagesNumber++;
                }
                sourceInformation.setPagesNumber(pagesNumber);
            }
            fileInfo += message("FileSize") + ": " + FileTools.showFileSize(sourceFile.length()) + "\n"
                    + message("FileModifyTime") + ": " + DateTools.datetimeToString(sourceFile.lastModified()) + "\n"
                    + (editType == Edit_Type.Bytes ? message("BytesNumberInFile") : message("CharactersNumberInFile"))
                    + ": " + StringTools.format(fileObjectNumber) + "\n"
                    + message("LinesNumberInFile") + ": " + StringTools.format(fileLinesNumber) + "\n"
                    + (editType == Edit_Type.Bytes ? message("PageSizeBytes") : message("PageSizeCharacters"))
                    + ": " + StringTools.format(pageSize) + "\n"
                    + message("CurrentPage") + ": " + StringTools.format(currentPage)
                    + " / " + StringTools.format(pagesNumber) + "\n";
        }

        String objectInfo, lineInfo;
        if (pagesNumber > 1) {
            objectInfo = editType == Edit_Type.Bytes ? message("BytesRangeInPage") : message("CharactersRangeInPage");
            objectInfo += ": " + StringTools.format(pageObjectStart) + " - " + StringTools.format(pageObjectEnd)
                    + " ( " + StringTools.format(pageObjectsNumber) + " )\n";
            lineInfo = message("LinesRangeInPage")
                    + ": " + StringTools.format(pageLineStart) + " - " + StringTools.format(pageLineEnd)
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
        pagePreviousButton.setDisable(currentPage <= 1 || pagesNumber < 2);
        pageNextButton.setDisable(currentPage >= pagesNumber || pagesNumber < 2);
        List<String> pages = new ArrayList<>();
        for (int i = 1; i <= pagesNumber; i++) {
            pages.add(i + "");
        }
        isSettingValues = true;
        pageSelector.getItems().clear();
        pageSelector.getItems().setAll(pages);
        pageLabel.setText("/" + pagesNumber);
        pageSelector.setValue(currentPage + "");
        pageSelector.getEditor().setStyle(null);
        pageSizeSelector.setValue(StringTools.format(pageSize));
        isSettingValues = false;

        if (pagesNumber > 1) {
            locatePane.getContent().setDisable(changed);
            filterPane.getContent().setDisable(changed);
        }
        if (editType != Edit_Type.Bytes) {
            charsetSelector.setDisable(sourceInformation.isWithBom());
        }
        if (filterController != null) {
            filterController.isBytes = editType == Edit_Type.Bytes;
            filterController.maxLen = pageSize;
            filterController.sourceLen = pageObjectsNumber;
            filterController.checkFilterStrings();
        }
    }

    protected void recordCursor() {
        lastScrollLeft = mainArea.getScrollLeft();
        lastScrollTop = mainArea.getScrollTop();
        lastCursor = mainArea.getAnchor();
        lastCaret = mainArea.getCaretPosition();
    }

    protected void resetCursor() {
        lastScrollLeft = -1;
        lastScrollTop = -1;
        lastCursor = -1;
        lastCaret = -1;
    }

    protected void recoverCursor() {
        int delay = Math.min(2000, mainArea.getLength() / 10);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (lastScrollLeft >= 0) {
                        mainArea.setScrollLeft(lastScrollLeft);
                    }
                    if (lastScrollTop >= 0) {
                        mainArea.setScrollTop(lastScrollTop);
                    }
                    resetCursor();
                });
            }
        }, delay);
    }

    protected synchronized void updateCursor() {
        String pageText = mainArea.getText();
        if (lastCursor >= 0) {
            mainArea.requestFocus();
            mainArea.deselect();
            mainArea.selectRange(lastCursor, lastCaret > lastCursor ? lastCaret : lastCursor);

        } else if (sourceInformation.getCurrentLine() >= 1) {
            if (sourceInformation.getCurrentPageLineStart() <= sourceInformation.getCurrentLine()
                    && sourceInformation.getCurrentPageLineEnd() >= sourceInformation.getCurrentLine()) {
                String[] lines = pageText.split("\n");
                int index = 0, end = (int) (sourceInformation.getCurrentLine() - sourceInformation.getCurrentPageLineStart());
                for (int i = 0; i < end; ++i) {
                    index += lines[i].length() + 1;
                }
                mainArea.selectRange(index, index);
            }

        } else if (findReplaceController != null && sourceInformation.getFindReplace() != null
                && sourceInformation.getFindReplace().getFileRange() != null) {
            FindReplaceFile findReplaceFile = sourceInformation.getFindReplace();
            findReplaceController.lastFileRange = findReplaceFile.getFileRange();
            String info = message("RangeInFile") + ":"
                    + (findReplaceController.lastFileRange.getStart() / findReplaceFile.getUnit() + 1) + "-"
                    + (findReplaceController.lastFileRange.getEnd() / findReplaceFile.getUnit());

            if (findReplaceFile.getStringRange() == null) {
                if (sourceInformation.getEditType() != Edit_Type.Bytes) {
                    FindReplaceFile.stringRange(findReplaceFile, pageText);
                } else {
                    FindReplaceFile.bytesRange(findReplaceFile, pageText);
                }
            }
            IndexRange range = findReplaceFile.getStringRange();
            if (range != null) {
                mainArea.requestFocus();
                mainArea.deselect();
//                MyBoxLog.debug("pageText.length():" + pageText.length() + " range.getEnd():" + range.getEnd());
                int start = range.getStart(), end = pageText.length();
                if (findReplaceFile.getOperation() == Operation.ReplaceFirst) {
                    end = Math.min(end, start + findReplaceFile.getReplaceString().length());
                } else {
                    end = Math.min(end, range.getEnd());
                }
                mainArea.selectRange(start, end);
                findReplaceController.lastStringRange = range;
                info += "\n" + message("RangeInPage") + ":" + (range.getStart() + 1) + "-" + (range.getEnd());
            }
            findReplaceController.findLabel.setText(info);
        }
//        recoverCursor();
    }

}
