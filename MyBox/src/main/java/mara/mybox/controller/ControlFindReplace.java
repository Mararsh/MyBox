package mara.mybox.controller;

import java.text.MessageFormat;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.data.FileEditInformation;
import mara.mybox.data.FileEditInformation.Edit_Type;
import mara.mybox.data.FileEditInformation.Line_Break;
import mara.mybox.data.FindReplaceFile;
import mara.mybox.data.FindReplaceString;
import mara.mybox.data.FindReplaceString.Operation;
import mara.mybox.data.LongIndex;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.ByteTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-11-8
 * @License Apache License Version 2.0
 */
public class ControlFindReplace extends BaseController {

    protected FileEditerController editerController;
    protected FileEditInformation sourceInformation;
    protected LongIndex lastFileRange;   // whole file
    protected IndexRange lastStringRange;  // currentPage
    protected TextArea mainArea;
    protected FindReplaceFile findReplace;

    @FXML
    protected CheckBox caseInsensitiveCheck, wrapCheck, regexCheck, multilineCheck, dotallCheck;
    @FXML
    protected TextArea findArea, replaceArea;
    @FXML
    protected Button findPreviousButton, findNextButton, countButton,
            replaceButton, replaceAllButton, exampleFindButton;
    @FXML
    protected Label findLabel, replaceLabel;

    public ControlFindReplace() {
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            findArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkFindInput(newValue);
                }
            });

            replaceArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkReplaceInput(newValue);
                }
            });

            caseInsensitiveCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "FindCaseInsensitive", caseInsensitiveCheck.isSelected());
                }
            });
            caseInsensitiveCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "FindCaseInsensitive", false));

            if (wrapCheck != null) {
                wrapCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue(baseName + "Wrap", wrapCheck.isSelected());
                    }
                });
                wrapCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Wrap", true));
            }

            multilineCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "FindMultiline", multilineCheck.isSelected());
                }
            });
            multilineCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "FindMultiline", true));

            dotallCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "DotAll", dotallCheck.isSelected());
                }
            });
            dotallCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "DotAll", true));

            regexCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "FindRegex", false));
            regexCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "FindRegex", regexCheck.isSelected());
                }
            });

            multilineCheck.disableProperty().bind(regexCheck.selectedProperty().not());
            dotallCheck.disableProperty().bind(regexCheck.selectedProperty().not());
            exampleFindButton.disableProperty().bind(regexCheck.selectedProperty().not());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void setValues(FileEditerController parent) {
        editerController = parent;
        mainArea = parent.mainArea;
        sourceInformation = editerController.sourceInformation;
    }

    @Override
    public void keyEventsHandler(KeyEvent event) {
        if (editerController == null) {
            return;
        }
        if (event.getCode() != null
                && (event.isControlDown() || event.isAltDown())) {
            switch (event.getCode()) {
                case DIGIT1:
                    findPreviousAction();
                    event.consume();
                    return;
                case DIGIT2:
                    findNextAction();
                    event.consume();
                    return;
                case Q:
                case H:
                    replaceAction();
                    event.consume();
                    return;
                case W:
                    replaceAllAction();
                    event.consume();
                    return;
            }
        }
    }

    protected void checkFindInput(String string) {
        boolean invalid = string.isEmpty() || !validateFind(string);
        countButton.setDisable(invalid);
        findPreviousButton.setDisable(invalid);
        findNextButton.setDisable(invalid);
        replaceButton.setDisable(invalid);
        replaceAllButton.setDisable(invalid);
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
        if (sourceInformation != null && string.length() >= sourceInformation.getFile().length()) {
            popError(AppVariables.message("FindStringLimitation"));
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
            findArea.setStyle(badStyle);
            return false;
        } else {
            if (sourceInformation != null && v.length() >= sourceInformation.getFile().length() * 3) {
                popError(AppVariables.message("FindStringLimitation"));
                findArea.setStyle(badStyle);
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
        if (isSettingValues || string.trim().isEmpty()) {
            return true;
        }
        final String v = ByteTools.validateTextHex(string);
        if (v == null) {
            replaceArea.setStyle(badStyle);
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
    protected void replaceAction() {
        findReplace(Operation.ReplaceFirst);
    }

    @FXML
    protected void replaceAllAction() {
        findReplace(Operation.ReplaceAll);
    }

    protected boolean makeFindReplace(Operation operation) {
        findLabel.setText("");
        replaceLabel.setText("");
        mainArea.requestFocus();
        if (null == operation || editerController == null) {
            popError(message("InvalidParameters"));
            return false;
        }
        sourceInformation = editerController.sourceInformation;
        boolean multiplePages = sourceInformation.getPagesNumber() > 1;
        if (multiplePages && (operation == Operation.Count || operation == Operation.ReplaceAll)
                && !editerController.checkBeforeNextAction()) {
            return false;
        }
        String findString = findArea.getText();
        if (findString.isEmpty()) {
            popError(message("EmptyValue"));
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
        String pageText = mainArea.getText();
        if (pageText.isEmpty()) {
            popError(message("EmptyValue"));
            return false;
        }

        if (multiplePages && operation == Operation.ReplaceAll) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVariables.message("SureReplaceAll"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSure = new ButtonType(AppVariables.message("Sure"));
            ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSure, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() != buttonSure) {
                return false;
            }
        }

        String replaceString = replaceArea.getText();
        int anchor = 0, unit = sourceInformation.getObjectUnit();
        long position = 0;

        if (operation != Operation.Count && operation != Operation.ReplaceAll) {
            anchor = mainArea.getAnchor();
//            MyBoxLog.debug("anchor：" + anchor);

//            MyBoxLog.debug("operation：" + operation + " (lastStringRange != null)：" + (lastStringRange != null));
            if (lastStringRange != null && lastStringRange.getStart() == anchor) {
                if (operation == Operation.FindPrevious) {
//                    MyBoxLog.debug(lastStringRange != null);
                    anchor = anchor + mainArea.getSelectedText().length() - unit;
                } else if (operation == Operation.FindNext || operation == Operation.ReplaceFirst) {
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

    protected void findReplace(FindReplaceString.Operation operation) {
        if (!makeFindReplace(operation)) {
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
                    return error == null;
                }

                @Override
                protected void whenSucceeded() {
                    lastStringRange = findReplace.getStringRange();
                    lastFileRange = findReplace.getFileRange();
//                    MyBoxLog.debug("(lastFileRange != null)：" + (lastFileRange != null) + " (lastStringRange != null)：" + (lastStringRange != null));
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
                                mainArea.deselect();
                                editerController.lastCursor = -1;
                                sourceInformation.setCurrentLine(-1);
                                if (findReplace.isMultiplePages()) {
                                    editerController.loadPage();
                                } else {
                                    mainArea.setText(findReplace.getOutputString());
                                    editerController.updateInterface(true);
                                }
                                info = MessageFormat.format(message("ReplaceAllOk"), count);
                            } else {
                                info = message("NotFound");
                            }
                            break;
                        }
                        default:
                            if (lastFileRange != null) {
                                info = message("RangeInFile") + ":"
                                        + (lastFileRange.getStart() + 1) + "-" + (lastFileRange.getEnd());
                            }
                            if (lastStringRange != null) {
                                info = (info.isEmpty() ? "" : info + "\n")
                                        + message("RangeInPage") + ":"
                                        + (lastStringRange.getStart() + 1) + "-" + (lastStringRange.getEnd());
                            }
                            if (lastStringRange != null) {
                                mainArea.deselect();
                                editerController.lastCursor = -1;
                                sourceInformation.setCurrentLine(-1);
                                if (operation == Operation.FindNext || operation == Operation.FindPrevious) {
                                    if (findReplace.isPageReloaded()) {
                                        editerController.isSettingValues = true;
                                        mainArea.setText(findReplace.getOutputString());
                                        editerController.isSettingValues = false;
                                        editerController.updateInterface(false);
                                    } else {
                                        mainArea.selectRange(lastStringRange.getStart(), lastStringRange.getEnd());
                                    }
                                } else if (operation == Operation.ReplaceFirst) {
                                    editerController.isSettingValues = true;
                                    if (findReplace.isPageReloaded()) {
                                        mainArea.setText(findReplace.getOutputString());
                                    } else {
                                        mainArea.replaceText(lastStringRange, findReplace.getReplaceString());
                                        mainArea.selectRange(lastStringRange.getStart(), lastStringRange.getStart() + findReplace.getReplaceString().length());
                                    }
                                    editerController.isSettingValues = false;
                                    editerController.updateInterface(true);
                                }
//                                MyBoxLog.debug("lastStringRange:" + lastStringRange.getStart() + "," + lastStringRange.getEnd() + " anchor" + mainArea.getAnchor());
                            } else {
                                info = message("NotFound");
                            }
                    }
                    findLabel.setText(info);
                    editerController.popInformation(info);
                }

                @Override
                protected void whenFailed() {
                    if (askSave) {
                        editerController.checkBeforeNextAction();
                    } else if (error != null) {
                        popError(AppVariables.message(error));
                    } else {
                        popFailed();
                    }
                }

            };
            editerController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
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
        popMenu = FxmlControl.popRegexExample(this, popMenu, findArea, mouseEvent);
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
