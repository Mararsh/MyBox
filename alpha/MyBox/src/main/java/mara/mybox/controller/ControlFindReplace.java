package mara.mybox.controller;

import java.text.MessageFormat;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import mara.mybox.data.BytesEditInformation;
import mara.mybox.data.FileEditInformation;
import mara.mybox.data.FileEditInformation.Edit_Type;
import mara.mybox.data.FindReplaceFile;
import mara.mybox.data.FindReplaceString;
import mara.mybox.data.FindReplaceString.Operation;
import mara.mybox.data.LongRange;
import mara.mybox.data.TextEditInformation;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-11-8
 * @License Apache License Version 2.0
 */
public class ControlFindReplace extends BaseController {

    protected BaseFileEditorController editerController;
    protected FileEditInformation sourceInformation;
    protected TextInputControl textInput;
    protected FindReplaceFile findReplace;
    protected double initX, initY;

    @FXML
    protected CheckBox caseInsensitiveCheck, wrapCheck, regexCheck, multilineCheck, dotallCheck;
    @FXML
    protected TextArea findArea, replaceArea;
    @FXML
    protected Button findPreviousButton, findNextButton, countButton, historyFindButton,
            replaceButton, replaceAllButton, exampleFindButton, historyStringButton;
    @FXML
    protected Label findLabel;
    @FXML
    protected VBox controlsBox, replaceBox;

    public ControlFindReplace() {
        baseTitle = message("FindReplace");
        TipsLabelKey = message("FindReplaceTips");
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            NodeStyleTools.removeTooltip(historyFindButton);
            if (historyStringButton != null) {
                NodeStyleTools.removeTooltip(historyStringButton);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
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

    public void setParent(BaseController parent) {
        try {
            this.parentController = parent;
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

            findArea.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                @Override
                public void handle(ContextMenuEvent event) {
                    if (editerController != null && editerController.sourceInformation instanceof BytesEditInformation) {
                        MenuBytesEditController.open(editerController, findArea, event);
                    } else {
                        MenuTextEditController.open(parentController, findArea, event);
                    }
                }
            });

            if (replaceArea != null) {
                replaceArea.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        checkReplaceInput(newValue);
                    }
                });

                replaceArea.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                    @Override
                    public void handle(ContextMenuEvent event) {
                        if (editerController != null && editerController.sourceInformation instanceof BytesEditInformation) {
                            MenuBytesEditController.open(editerController, replaceArea, event);
                        } else {
                            MenuTextEditController.open(parentController, replaceArea, event);
                        }
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
        }
        sourceInformation = editerController.sourceInformation;
        if (editerController.editType == Edit_Type.Bytes) {
            return validateFindBytes(string);
        } else {
            return validateFindText(string);
        }
    }

    protected boolean validateFindText(String string) {
        if (sourceInformation != null && sourceInformation.getFile() != null
                && string.length() >= sourceInformation.getFile().length()) {
            popError(message("FindStringLimitation"));
            return false;
        } else {
            return true;
        }
    }

    protected boolean validateFindBytes(String string) {
        if (isSettingValues || regexCheck.isSelected()) {
            findArea.setStyle(null);
            return true;
        }
        final String v = ByteTools.formatTextHex(string);
        if (v == null) {
            findArea.setStyle(UserConfig.badStyle());
            return false;
        } else {
            if (sourceInformation != null && v.length() >= sourceInformation.getFile().length() * 3) {
                popError(message("FindStringLimitation"));
                findArea.setStyle(UserConfig.badStyle());
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
        }
        sourceInformation = editerController.sourceInformation;
        if (editerController.editType == Edit_Type.Bytes) {
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
            replaceArea.setStyle(null);
            return true;
        }
        final String v = ByteTools.formatTextHex(string);
        if (v == null) {
            replaceArea.setStyle(UserConfig.badStyle());
            return false;
        } else {
            replaceArea.setStyle(null);
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

    protected boolean makeParamters(Operation operation) {
        if (null == operation || textInput == null || sourceInformation == null) {
            popError(message("InvalidParameters"));
            return false;
        }

        boolean multiplePages = sourceInformation != null && sourceInformation.getPagesNumber() > 1;
        if (editerController != null && multiplePages
                && (operation == Operation.Count || operation == Operation.ReplaceAll)
                && !editerController.checkBeforeNextAction()) {
            return false;
        }
        String findString = findArea.getText();
        if (findString == null || findString.isEmpty()) {
            popError(message("EmptyValue"));
            return false;
        }
        TableStringValues.add(baseName + "FindString", findString);
        String pageText = textInput.getText();
        if (pageText == null || pageText.isEmpty()) {
            popError(message("EmptyValue"));
            return false;
        }
        String replaceString = replaceArea == null ? null : replaceArea.getText();
        if (replaceString != null && replaceString.equals(findString)) {
            if (operation == Operation.ReplaceAll || operation == Operation.ReplaceFirst) {
                popError(message("Same"));
                return false;
            }
        }
        if (replaceString != null && !replaceString.isEmpty()) {
            TableStringValues.add(baseName + "ReplaceString", replaceString);
        }
        if (operation == Operation.ReplaceAll && multiplePages) {
            if (!PopTools.askSure(getMyStage().getTitle(), message("SureReplaceAll"))) {
                return false;
            }
        }
        String selectedText = textInput.getSelectedText();
        if (sourceInformation.getEditType() == Edit_Type.Bytes) {
            pageText = pageText.replaceAll("\n", " ");
            findString = findString.replaceAll("\n", " ");
            replaceString = replaceString.replaceAll("\n", " ");
            selectedText = selectedText.replaceAll("\n", " ");
        }
        int anchor = textInput.getAnchor(), unit = sourceInformation.getObjectUnit();
        long pageStart = (int) sourceInformation.getCurrentPageObjectStart() * unit;
        if (StringTools.match(selectedText, findString, regexCheck.isSelected(), dotallCheck.isSelected(),
                multilineCheck.isSelected(), caseInsensitiveCheck.isSelected())) {
            IndexRange selectIndex = textInput.getSelection();
            switch (operation) {
                case FindNext:
                    anchor = selectIndex.getStart() + unit;
                    break;
                case FindPrevious:
                    anchor = selectIndex.getEnd() - unit;
                    break;
                case ReplaceFirst:
                    textInput.replaceText(selectIndex, findReplace.getReplaceString());
                    textInput.selectRange(selectIndex.getStart() + findReplace.getReplaceString().length(), selectIndex.getStart());
                    String info = message("Replaced") + ": " + (pageStart + selectIndex.getStart() + 1) + "-" + (pageStart + selectIndex.getEnd());
                    findLabel.setText(info);
                    popInformation(info, textInput);
                    return false;
            }
        }
        findReplace = new FindReplaceFile()
                .setFileInfo(sourceInformation)
                .setPosition(anchor + pageStart);
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

    protected void editorFindReplace(Operation operation) {
        if (editerController == null || !makeParamters(operation)) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            textInput.deselect();
            task = new SingletonTask<Void>() {

                protected IndexRange lastStringRange;
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
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    String info = "";
                    switch (operation) {
                        case Count: {
                            int count = findReplace.getCount();
                            if (count > 0) {
                                info = MessageFormat.format(message("CountNumber"), count);
                            } else {
                                info = message("NotFound");
                            }
                            break;
                        }
                        case ReplaceAll: {
                            int count = findReplace.getCount();
                            if (count > 0) {
                                textInput.deselect();
                                if (findReplace.isMultiplePages()) {
                                    editerController.sourceInformation.setTotalNumberRead(false);
                                    editerController.loadPage();
                                } else {
                                    editerController.loadText(findReplace.getOutputString(), true);
                                }
                                info = MessageFormat.format(message("ReplaceAllOk"), count);
                            } else {
                                info = message("NotFound");
                            }
                            break;
                        }
                        default:
                            if (lastStringRange != null) {
                                int unit = findReplace.getUnit();
                                if (operation == Operation.FindNext || operation == Operation.FindPrevious) {
                                    editerController.loadText(findReplace.getOutputString(), editerController.fileChanged.getValue());
                                    editerController.selectObjects(lastStringRange.getStart(), (int) lastStringRange.getLength());
                                } else if (operation == Operation.ReplaceFirst) {
                                    editerController.loadText(findReplace.getOutputString(), true);
                                    editerController.selectObjects(lastStringRange.getStart(), findReplace.getReplaceString().length());
                                }
                                LongRange fileRange = findReplace.getFileRange();
                                if (fileRange != null && findReplace.isMultiplePages()) {
                                    info = message("RangeInFile") + ":" + (fileRange.getStart() / unit + 1) + "-" + (fileRange.getEnd() / unit) + "\n";
                                }
                                info += message("RangeInPage") + ":" + (lastStringRange.getStart() / unit + 1) + "-" + (lastStringRange.getEnd() / unit);
                            } else {
                                info = message("NotFound");
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
                        popError(message(error));
                    } else {
                        popFailed();
                    }
                }

            };
            editerController.start(task);
        }
    }

    protected void inputFindReplace(Operation operation) {
        if (textInput == null || !makeParamters(operation)) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                protected IndexRange lastStringRange;

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
                                info = MessageFormat.format(message("CountNumber"), count);
                            } else {
                                info = message("NotFound");
                            }
                            break;
                        }
                        case ReplaceAll: {
                            int count = findReplace.getCount();
                            if (count > 0) {
                                textInput.deselect();
                                textInput.setText(findReplace.getOutputString());
                                info = MessageFormat.format(message("ReplaceAllOk"), count);
                            } else {
                                info = message("NotFound");
                            }
                            break;
                        }
                        default:
                            if (lastStringRange != null) {
                                int start = lastStringRange.getStart();
                                int end = lastStringRange.getEnd();
                                textInput.deselect();
                                if (operation == Operation.FindNext || operation == Operation.FindPrevious) {
                                    textInput.selectRange(end, start);
                                    info = message("Found") + ": " + (start + 1) + "-" + end;

                                } else if (operation == Operation.ReplaceFirst) {
                                    textInput.replaceText(lastStringRange, findReplace.getReplaceString());
                                    textInput.selectRange(start + findReplace.getReplaceString().length(), start);
                                    info = message("Replaced") + ": " + (start + 1) + "-" + end;
                                }
                            } else {
                                info = message("NotFound");
                            }
                    }
                    if (!info.isBlank()) {
                        findLabel.setText(info);
                        popInformation(info, textInput);
                    }
                }

            };
            if (editerController != null) {
                editerController.start(task);
            } else {
                start(task, false);
            }
        }
    }

    @FXML
    public void popFindExample(MouseEvent mouseEvent) {
        PopTools.popRegexExample(this, findArea, mouseEvent);
    }

    @FXML
    public void popFindHistories(MouseEvent mouseEvent) {
        PopTools.popStringValues(this, findArea, mouseEvent, baseName + "FindString");
    }

    @FXML
    public void popReplaceHistories(MouseEvent mouseEvent) {
        PopTools.popStringValues(this, replaceArea, mouseEvent, baseName + "ReplaceString");
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
