package mara.mybox.controller;

import java.text.MessageFormat;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import mara.mybox.data.FileEditInformation;
import mara.mybox.data.FileEditInformation.Edit_Type;
import mara.mybox.data.FileEditInformation.Line_Break;
import mara.mybox.data.FindReplaceFile;
import mara.mybox.data.FindReplaceString;
import mara.mybox.data.FindReplaceString.Operation;
import mara.mybox.data.LongIndex;
import mara.mybox.data.TextEditInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.NodeTools;
import static mara.mybox.fxml.NodeStyleTools.badStyle;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.ByteTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-11-8
 * @License Apache License Version 2.0
 */
public class ControlFindReplace extends BaseController {

    protected BaseFileEditorController editerController;
    protected FileEditInformation sourceInformation;
    protected LongIndex lastFileRange;   // whole file
    protected IndexRange lastStringRange;  // currentPage
    protected TextInputControl textInput;
    protected FindReplaceFile findReplace;
    protected double initX, initY;

    @FXML
    protected CheckBox caseInsensitiveCheck, wrapCheck, regexCheck, multilineCheck, dotallCheck;
    @FXML
    protected TextArea findArea, replaceArea;
    @FXML
    protected Button findPreviousButton, findNextButton, countButton,
            replaceButton, replaceAllButton, exampleFindButton;
    @FXML
    protected Label findLabel;
    @FXML
    protected VBox controlsBox, replaceBox;

    public ControlFindReplace() {
        baseTitle = Languages.message("FindReplace");
        TipsLabelKey = Languages.message("FindReplaceTips");
    }

    public void setEditor(BaseFileEditorController parent) {
        editerController = parent;
        parentController = parent;
        textInput = parent.mainArea;
        sourceInformation = editerController.sourceInformation;
        baseName = editerController.baseName;
        setControls();
    }

    public void setEditInput(BaseController parent, TextInputControl textInput) {
        try {
            this.parentController = parent;
            this.textInput = textInput;
            this.baseName = parent.baseName;
            setControls();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void setControls() {
        try {
            findArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkFindInput(newValue);
                }
            });

            caseInsensitiveCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "FindCaseInsensitive", caseInsensitiveCheck.isSelected());
                }
            });
            caseInsensitiveCheck.setSelected(UserConfig.getBoolean(baseName + "FindCaseInsensitive", false));

            if (wrapCheck != null) {
                wrapCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "Wrap", wrapCheck.isSelected());
                    }
                });
                wrapCheck.setSelected(UserConfig.getBoolean(baseName + "Wrap", true));
            }

            multilineCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "FindMultiline", multilineCheck.isSelected());
                }
            });
            multilineCheck.setSelected(UserConfig.getBoolean(baseName + "FindMultiline", true));

            dotallCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "DotAll", dotallCheck.isSelected());
                }
            });
            dotallCheck.setSelected(UserConfig.getBoolean(baseName + "DotAll", true));

            regexCheck.setSelected(UserConfig.getBoolean(baseName + "FindRegex", false));
            regexCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "FindRegex", regexCheck.isSelected());
                }
            });

            multilineCheck.disableProperty().bind(regexCheck.selectedProperty().not());
            dotallCheck.disableProperty().bind(regexCheck.selectedProperty().not());
            exampleFindButton.disableProperty().bind(regexCheck.selectedProperty().not());

            if (replaceArea != null) {
                replaceArea.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        checkReplaceInput(newValue);
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    public boolean controlAltF() {
        findNextAction();
        return true;
    }

    @Override
    public boolean controlAltW() {
        replaceAllAction();
        return true;
    }

    @Override
    public boolean controlAlt1() {
        findPreviousAction();
        return true;
    }

    @Override
    public boolean controlAlt2() {
        findNextAction();
        return true;
    }

    protected void checkFindInput(String string) {
        boolean invalid = string.isEmpty() || !validateFind(string);
        countButton.setDisable(invalid);
        findPreviousButton.setDisable(invalid);
        findNextButton.setDisable(invalid);
        if (replaceButton != null) {
            replaceButton.setDisable(invalid);
            replaceAllButton.setDisable(invalid);
        }
    }

    protected boolean validateFind(String string) {
        if (editerController == null) {
            return true;
        } else if (editerController.editType == Edit_Type.Bytes) {
            return validateFindBytes(string);
        } else {
            return validateFindText(string);
        }
    }

    protected boolean validateFindText(String string) {
        if (sourceInformation != null && sourceInformation.getFile() != null
                && string.length() >= sourceInformation.getFile().length()) {
            popError(Languages.message("FindStringLimitation"));
            return false;
        } else {
            return true;
        }
    }

    protected boolean validateFindBytes(String string) {
        if (isSettingValues || regexCheck.isSelected()) {
            return true;
        }
        final String v = ByteTools.validateTextHex(string);
        if (v == null) {
            findArea.setStyle(NodeStyleTools.badStyle);
            return false;
        } else {
            if (sourceInformation != null && v.length() >= sourceInformation.getFile().length() * 3) {
                popError(Languages.message("FindStringLimitation"));
                findArea.setStyle(NodeStyleTools.badStyle);
                return false;
            }
            findArea.setStyle(null);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    isSettingValues = true;
                    findArea.setText(v);
                    findArea.end();
                    isSettingValues = false;
                }
            });
            return true;
        }
    }

    protected boolean checkReplaceInput(String string) {
        if (replaceButton == null) {
            return true;
        }
        boolean invalid = !validateReplace(string);
        replaceButton.setDisable(invalid);
        replaceAllButton.setDisable(invalid);
        return true;
    }

    protected boolean validateReplace(String string) {
        if (editerController == null) {
            return true;
        } else if (editerController.editType == Edit_Type.Bytes) {
            return validateReplaceBytes(string);
        } else {
            return validateReplaceText(string);
        }
    }

    protected boolean validateReplaceText(String string) {
        return true;
    }

    protected boolean validateReplaceBytes(String string) {
        if (isSettingValues || string.trim().isEmpty() || replaceArea == null) {
            return true;
        }
        final String v = ByteTools.validateTextHex(string);
        if (v == null) {
            replaceArea.setStyle(NodeStyleTools.badStyle);
            return false;
        } else {
            return true;
        }
    }

    @FXML
    protected void countAction() {
        findReplace(Operation.Count);
    }

    @FXML
    protected void findNextAction() {
        findReplace(Operation.FindNext);
    }

    @FXML
    protected void findPreviousAction() {
        findReplace(Operation.FindPrevious);
    }

    @FXML
    @Override
    public void findAction() {
        findNextAction();
    }

    @FXML
    @Override
    public void replaceAction() {
        if (replaceArea == null) {
            return;
        }
        findReplace(Operation.ReplaceFirst);
    }

    @FXML
    protected void replaceAllAction() {
        if (replaceArea == null) {
            return;
        }
        findReplace(Operation.ReplaceAll);
    }

    protected void findReplace(FindReplaceString.Operation operation) {
        if (null == operation || textInput == null) {
            closeStage();
            return;
        }
        findLabel.setText("");
        if (textInput != null) {
            textInput.requestFocus();
        }
        if (editerController != null) {
            sourceInformation = editerController.sourceInformation;
            editorFindReplace(operation);
        } else {
            sourceInformation = new TextEditInformation();
            inputFindReplace(operation);
        }
    }

    protected void editorFindReplace(Operation operation) {
        if (editerController == null || !makeData(operation)) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private boolean askSave = false;

                @Override
                protected boolean handle() {
                    if (!findReplace.isMultiplePages()) {
                        if (!findReplace.run()) {
                            error = findReplace.getError();
                        }
                    } else if (!findReplace.page()) {
                        if (editerController.fileChanged.getValue()) {
                            askSave = true;
                            return false;
                        }
                        if (!findReplace.file()) {
                            error = findReplace.getError();
                        }
                    }
                    if (error != null) {
                        return false;
                    }
                    lastStringRange = findReplace.getStringRange();
                    lastFileRange = findReplace.getFileRange();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
//                    MyBoxLog.debug("(lastFileRange != null)：" + (lastFileRange != null) + " (lastStringRange != null)：" + (lastStringRange != null));
                    String info = "";
                    switch (operation) {
                        case Count: {
                            int count = findReplace.getCount();
                            if (count > 0) {
                                info = MessageFormat.format(Languages.message("CountNumber"), count);
                            } else {
                                info = Languages.message("NotFound");
                            }
                            break;
                        }
                        case ReplaceAll: {
                            int count = findReplace.getCount();
                            if (count > 0) {
                                textInput.deselect();
                                editerController.lastCursor = -1;
                                sourceInformation.setCurrentLine(-1);
                                if (findReplace.isMultiplePages()) {
                                    editerController.loadPage();
                                } else {
                                    textInput.setText(findReplace.getOutputString());
                                    editerController.updateInterface(true);
                                }
                                info = MessageFormat.format(Languages.message("ReplaceAllOk"), count);
                            } else {
                                info = Languages.message("NotFound");
                            }
                            break;
                        }
                        default:
//                            if (lastFileRange != null) {
//                                info = message("RangeInFile") + ":"
//                                        + (lastFileRange.getStart() + 1) + "-" + (lastFileRange.getEnd());
//                            }
//                            if (lastStringRange != null) {
//                                info = (info.isEmpty() ? "" : info + "\n")
//                                        + message("RangeInPage") + ":"
//                                        + (lastStringRange.getStart() + 1) + "-" + (lastStringRange.getEnd());
//                            }
                            if (lastStringRange != null) {
                                textInput.deselect();
                                editerController.lastCursor = -1;
                                sourceInformation.setCurrentLine(-1);
                                if (operation == Operation.FindNext || operation == Operation.FindPrevious) {
                                    if (findReplace.isPageReloaded()) {
                                        editerController.isSettingValues = true;
                                        textInput.setText(findReplace.getOutputString());
                                        editerController.isSettingValues = false;
                                        editerController.updateInterface(false);
                                    }
                                    textInput.selectRange(lastStringRange.getStart(), lastStringRange.getEnd());
                                } else if (operation == Operation.ReplaceFirst) {
                                    editerController.isSettingValues = true;
                                    if (findReplace.isPageReloaded()) {
                                        textInput.setText(findReplace.getOutputString());
                                    } else {
                                        textInput.replaceText(lastStringRange, findReplace.getReplaceString());
                                    }
                                    textInput.selectRange(lastStringRange.getStart(), lastStringRange.getStart() + findReplace.getReplaceString().length());
                                    editerController.isSettingValues = false;
                                    editerController.updateInterface(true);
                                }
//                                MyBoxLog.debug("lastStringRange:" + lastStringRange.getStart() + "," + lastStringRange.getEnd() + " anchor" + textInput.getAnchor());
                            } else {
                                info = Languages.message("NotFound");
                            }
                    }
                    if (!info.isBlank()) {
                        findLabel.setText(info);
                        editerController.popInformation(info);
                    }
                }

                @Override
                protected void whenFailed() {
                    if (askSave) {
                        editerController.checkBeforeNextAction();
                    } else if (error != null) {
                        popError(Languages.message(error));
                    } else {
                        popFailed();
                    }
                }

            };
            editerController.handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    protected boolean makeData(Operation operation) {
        if (null == operation || textInput == null || sourceInformation == null) {
            popError(Languages.message("InvalidParameters"));
            return false;
        }

        boolean multiplePages = sourceInformation.getPagesNumber() > 1;
        if (editerController != null && multiplePages
                && (operation == Operation.Count || operation == Operation.ReplaceAll)
                && !editerController.checkBeforeNextAction()) {
            return false;
        }
        String findString = findArea.getText();
        if (findString.isEmpty()) {
            popError(Languages.message("EmptyValue"));
            return false;
        }
        if (findReplace == null
                || !findString.equals(findReplace.getFindString())
                || regexCheck.isSelected() != findReplace.isIsRegex()
                || caseInsensitiveCheck.isSelected() != findReplace.isCaseInsensitive()
                || multilineCheck.isSelected() != findReplace.isMultiline()
                || dotallCheck.isSelected() != findReplace.isDotAll()
                || wrapCheck.isSelected() != findReplace.isWrap()) {
            lastFileRange = null;
            lastStringRange = null;
//            MyBoxLog.debug("reset");
        }
        String pageText = textInput.getText();
        if (pageText.isEmpty()) {
            popError(Languages.message("EmptyValue"));
            return false;
        }
        String replaceString = replaceArea == null ? null : replaceArea.getText();
        if (operation == Operation.ReplaceAll) {
            if (!PopTools.askSure(getMyStage().getTitle(), Languages.message("SureReplaceAll"))) {
                return false;
            }
        }
        int anchor = 0, unit = sourceInformation.getObjectUnit();
        long position = 0;
        if (operation != Operation.Count && operation != Operation.ReplaceAll) {
            anchor = textInput.getAnchor();
//            MyBoxLog.debug("anchor：" + anchor);

//            MyBoxLog.debug("operation：" + operation + " (lastStringRange != null)：" + (lastStringRange != null));
            if (lastStringRange != null && lastStringRange.getStart() == anchor) {
                if (operation == Operation.FindPrevious) {
//                    MyBoxLog.debug(lastStringRange != null);
                    anchor = anchor + textInput.getSelectedText().length() - unit;
                } else if (operation == Operation.FindNext) {
//                    MyBoxLog.debug(lastStringRange != null);
                    anchor += unit;
                }
//                MyBoxLog.debug("operation：" + operation + " anchor：" + anchor);
            }
            position = anchor;

            if (multiplePages && sourceInformation.getEditType() != Edit_Type.Bytes
                    && sourceInformation.getLineBreak().equals(Line_Break.CRLF)) {
                String sub = pageText.substring(0, anchor);
                int linesNumber = FindReplaceString.count(sub, "\n");
                position += linesNumber;
//                    MyBoxLog.debug("linesNumber：" + linesNumber + " position：" + position);
            }
            position += sourceInformation.getCurrentPageObjectStart() * unit;
        }
        if (sourceInformation.getEditType() == Edit_Type.Bytes) {
            pageText = pageText.replaceAll("\n", " ");
            findString = findString.replaceAll("\n", " ");
            replaceString = replaceString.replaceAll("\n", " ");
//            MyBoxLog.debug("replaced");
        }
//        MyBoxLog.debug("anchor：" + anchor + " position：" + position);
//        MyBoxLog.debug("\n------\n" + pageText + "\n-----");
        findReplace = new FindReplaceFile()
                .setFileInfo(sourceInformation)
                .setMultiplePages(multiplePages)
                .setPosition(position);
        if (editerController != null) {
            findReplace.setBackupController(editerController.backupController);
        }
        findReplace.setOperation(operation)
                .setInputString(pageText)
                .setFindString(findString)
                .setAnchor(anchor)
                .setReplaceString(replaceString)
                .setUnit(unit)
                .setIsRegex(regexCheck.isSelected())
                .setCaseInsensitive(caseInsensitiveCheck.isSelected())
                .setMultiline(multilineCheck.isSelected())
                .setDotAll(dotallCheck.isSelected())
                .setWrap(wrapCheck.isSelected());
        sourceInformation.setFindReplace(findReplace);
        return true;
    }

    protected void inputFindReplace(Operation operation) {
        if (textInput == null || !makeData(operation)) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
                    if (!findReplace.run()) {
                        error = findReplace.getError();
                    }
                    if (error != null) {
                        return false;
                    }
                    lastStringRange = findReplace.getStringRange();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    String info = "";
                    if (textInput instanceof TextField) {
                        textInput.getScene().getWindow().requestFocus();
                        textInput.requestFocus();
                    }
                    switch (operation) {
                        case Count: {
                            int count = findReplace.getCount();
                            if (count > 0) {
                                info = MessageFormat.format(Languages.message("CountNumber"), count);
                            } else {
                                info = Languages.message("NotFound");
                            }
                            break;
                        }
                        case ReplaceAll: {
                            int count = findReplace.getCount();
                            if (count > 0) {
                                textInput.deselect();
                                textInput.setText(findReplace.getOutputString());
                                info = MessageFormat.format(Languages.message("ReplaceAllOk"), count);
                            } else {
                                info = Languages.message("NotFound");
                            }
                            break;
                        }
                        default:
                            if (lastStringRange != null) {
                                textInput.deselect();
                                if (operation == Operation.FindNext || operation == Operation.FindPrevious) {
                                    textInput.selectRange(lastStringRange.getStart(), lastStringRange.getEnd());
                                    info = Languages.message("Found") + ": " + (lastStringRange.getStart() + 1) + "-" + (lastStringRange.getEnd());
                                } else if (operation == Operation.ReplaceFirst) {
                                    textInput.replaceText(lastStringRange, findReplace.getReplaceString());
                                    textInput.selectRange(lastStringRange.getStart(), lastStringRange.getStart() + findReplace.getReplaceString().length());
                                    info = Languages.message("Replaced") + ": " + (lastStringRange.getStart() + 1) + "-" + (lastStringRange.getEnd());
                                }
                            } else {
                                info = Languages.message("NotFound");
                            }
                    }
                    if (!info.isBlank()) {
                        findLabel.setText(info);
                        popInformation(info, textInput);
                    }
                }

            };
            if (editerController != null) {
                editerController.handling(task);
            }
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    public void clearFind() {
        findArea.clear();
    }

    @FXML
    public void clearReplace() {
        replaceArea.clear();
    }

    @FXML
    public void popFindExample(MouseEvent mouseEvent) {
        PopTools.popRegexExample(this, findArea, mouseEvent);
    }


    /*
        get/set
     */
    public FileEditInformation getSourceInformation() {
        return sourceInformation;
    }

    public void setSourceInformation(FileEditInformation sourceInformation) {
        this.sourceInformation = sourceInformation;
    }

}
