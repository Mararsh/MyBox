package mara.mybox.controller;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
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
import javafx.scene.layout.VBox;
import mara.mybox.data.BytesEditInformation;
import mara.mybox.data.FileEditInformation.Edit_Type;
import mara.mybox.data.FindReplaceFile;
import mara.mybox.data.FindReplaceMatch;
import mara.mybox.data.FindReplaceString;
import mara.mybox.data.FindReplaceString.Operation;
import static mara.mybox.data.FindReplaceString.Operation.Count;
import mara.mybox.data.LongRange;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.style.NodeStyleTools;
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

    protected BaseTextController editerController;
    protected TextInputControl textInput;
    protected FindReplaceFile findReplace;
    protected double initX, initY;
    protected Edit_Type editType;

    @FXML
    protected CheckBox caseInsensitiveCheck, wrapCheck, regexCheck, multilineCheck, dotallCheck, shareCheck;
    @FXML
    protected TextArea findArea, replaceArea;
    @FXML
    protected Button findPreviousButton, findNextButton, countButton, listButton, historyFindButton,
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
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            if (shareCheck != null) {
                shareCheck.setSelected(UserConfig.getBoolean("ShareFindReplaceOptions", true));
                shareCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean("ShareFindReplaceOptions", shareCheck.isSelected());
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setEditor(BaseTextController parent) {
        editerController = parent;
        parentController = parent;
        textInput = parent.mainArea;
        editType = parent.editType;
        setControls();
    }

    public void setEditInput(BaseController parent, TextInputControl textInput) {
        try {
            this.parentController = parent;
            this.textInput = textInput;
            editType = Edit_Type.Text;
            setControls();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void setParent(BaseController parent, Edit_Type editType) {
        try {
            this.parentController = parent;
            this.editType = editType;
            setControls();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void setControls() {
        try {
            baseName = "FindReplace";
            if (shareCheck == null || !shareCheck.isSelected()) {
                baseName = parentController.baseName + baseName;
            }

            findArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkFindInput(newValue);
                }
            });

            caseInsensitiveCheck.setSelected(UserConfig.getBoolean(baseName + "FindReplaceCaseInsensitive", false));
            caseInsensitiveCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "FindReplaceCaseInsensitive", caseInsensitiveCheck.isSelected());
                }
            });

            if (wrapCheck != null) {
                wrapCheck.setSelected(UserConfig.getBoolean(baseName + "FindReplaceWrap", true));
                wrapCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "FindReplaceWrap", wrapCheck.isSelected());
                    }
                });
            }

            multilineCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "FindMultiline", multilineCheck.isSelected());
                }
            });
            multilineCheck.setSelected(UserConfig.getBoolean(baseName + "FindMultiline", true));

            dotallCheck.setSelected(UserConfig.getBoolean(baseName + "FindDotAll", true));
            dotallCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "FindDotAll", dotallCheck.isSelected());
                }
            });

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
                        MenuBytesEditController.openBytes(editerController, findArea, event);
                    } else {
                        MenuTextEditController.textMenu(parentController, findArea, event);
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
                            MenuBytesEditController.openBytes(editerController, replaceArea, event);
                        } else {
                            MenuTextEditController.textMenu(parentController, replaceArea, event);
                        }
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
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
        listButton.setDisable(invalid);
        findPreviousButton.setDisable(invalid);
        findNextButton.setDisable(invalid);
        if (replaceButton != null) {
            replaceButton.setDisable(invalid);
            replaceAllButton.setDisable(invalid);
        }
    }

    protected boolean validateFind(String string) {
        if (editType == Edit_Type.Bytes) {
            return validateFindBytes(string);
        } else {
            return validateFindText(string);
        }
    }

    protected boolean validateFindText(String string) {
        if (editerController != null
                && editerController.sourceInformation != null
                && editerController.sourceInformation.getFile() != null
                && string.length() >= editerController.sourceInformation.getFile().length()) {
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
            if (editerController != null
                    && editerController.sourceInformation != null
                    && v.length() >= editerController.sourceInformation.getFile().length() * 3) {
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
        if (editType == Edit_Type.Bytes) {
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
    protected void listAction() {
        findReplace(Operation.FindAll);
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
            editorFindReplace(operation);
        } else {
            inputFindReplace(operation);
        }
    }

    protected String findString() {
        String findString = findArea.getText();
        if (findString == null || findString.isEmpty()) {
            popError(message("EmptyValue") + ": " + message("Find"));
            return null;
        }
        if (!findString.isBlank()) {
            TableStringValues.add(baseName + "FindString", findString);
        }
        return findString;
    }

    protected String replaceString() {
        String replaceString = replaceArea == null ? "" : replaceArea.getText();
        if (replaceString == null) {
            replaceString = "";
        }
        if (!replaceString.isBlank()) {
            TableStringValues.add(baseName + "ReplaceString", replaceString);
        }
        return replaceString;
    }

    protected boolean makeParamters(Operation operation) {
        if (null == operation || textInput == null) {
            popError(message("InvalidParameters"));
            return false;
        }
        boolean multiplePages = editerController != null
                && editerController.sourceInformation != null
                && editerController.sourceInformation.getPagesNumber() > 1;

        if (multiplePages
                && (operation == Operation.Count || operation == Operation.ReplaceAll)
                && !editerController.checkBeforeNextAction()) {
            return false;
        }
        String findString = findString();
        if (findString == null || findString.isEmpty()) {
            return false;
        }
        String pageText = textInput.getText();
        if (pageText == null || pageText.isEmpty()) {
            popError(message("EmptyValue"));
            return false;
        }
        String replaceString = replaceString();
        if (replaceString.equals(findString)) {
            if (operation == Operation.ReplaceAll || operation == Operation.ReplaceFirst) {
                popError(message("Unchanged"));
                return false;
            }
        }
        if (operation == Operation.ReplaceAll && multiplePages) {
            if (!PopTools.askSure(getTitle(), message("SureReplaceAll"))) {
                return false;
            }
        }
        String selectedText = textInput.getSelectedText();
        if (editerController != null && editerController.sourceInformation.getEditType() == Edit_Type.Bytes) {
            pageText = StringTools.replaceLineBreak(pageText);
            findString = StringTools.replaceLineBreak(findString);
            replaceString = StringTools.replaceLineBreak(replaceString);
            selectedText = StringTools.replaceLineBreak(selectedText);
        }
        int anchor = textInput.getAnchor();
        int unit = editerController != null ? editerController.sourceInformation.getObjectUnit() : 1;
        long pageStart = editerController != null
                ? (int) editerController.sourceInformation.getCurrentPageObjectStart() * unit : 0;
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
                    textInput.replaceText(selectIndex, replaceString);
                    textInput.selectRange(selectIndex.getStart() + replaceString.length(), selectIndex.getStart());
                    String info = message("Replaced") + ": " + (pageStart + selectIndex.getStart() + 1) + "-" + (pageStart + selectIndex.getEnd());
                    findLabel.setText(info);
                    popInformation(info, textInput);
                    return false;
            }
        }
        findReplace = new FindReplaceFile()
                .setPosition(anchor + pageStart);
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

        if (editerController != null) {
            findReplace.setFileInfo(editerController.sourceInformation)
                    .setController(editerController);
            editerController.sourceInformation.setFindReplace(findReplace);
        }
        return true;
    }

    protected void editorFindReplace(Operation operation) {
        if (editerController == null || !makeParamters(operation)) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        textInput.deselect();
        task = new FxSingletonTask<Void>(this) {

            protected IndexRange lastStringRange;
            private boolean askSave = false;

            @Override
            protected boolean handle() {
                if (!findReplace.isMultiplePages()) {
                    if (!findReplace.handleString(this)) {
                        if (!isWorking()) {
                            return false;
                        }
                        error = findReplace.getError();
                    }
                } else if (!findReplace.handlePage(this)) {
                    if (!isWorking()) {
                        return false;
                    }
                    if (editerController.fileChanged.getValue()) {
                        askSave = true;
                        return false;
                    }
                    if (!findReplace.handleFile(this)) {
                        if (!isWorking()) {
                            return false;
                        }
                        error = findReplace.getError();
                    }
                }
                if (error != null) {
                    return false;
                }
                lastStringRange = findReplace.getStringRange();
                return isWorking();
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
                    case FindAll: {
                        info = reportMatches();
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
        start(task);
    }

    protected void inputFindReplace(Operation operation) {
        if (textInput == null || !makeParamters(operation)) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            protected IndexRange lastStringRange;

            @Override
            protected boolean handle() {
                if (!findReplace.handleString(this)) {
                    if (!isWorking()) {
                        return false;
                    }
                    error = findReplace.getError();
                }
                if (error != null) {
                    return false;
                }
                lastStringRange = findReplace.getStringRange();
                return isWorking();
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
                    case FindAll: {
                        reportMatches();
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
        start(task, false);
    }

    public String reportMatches() {
        try {
            int count = findReplace.getCount();
            if (count <= 0) {
                return message("NotFound");
            }
            DataFileCSV matchesData = findReplace.getMatchesData();
            if (matchesData != null) {
                DataFileCSVController.loadCSV(matchesData);
            } else {
                List<FindReplaceMatch> matches = findReplace.getMatches();
                if (matches == null || matches.isEmpty()) {
                    return message("NotFound");
                }
                List<String> names = new ArrayList<>();
                names.addAll(Arrays.asList(message("Index"), message("Start"), message("End"), message("String")));
                StringTable t = new StringTable(names, message("Find") + "\n" + findReplace.getFindString());
                int index = 0;
                int unit = findReplace.getUnit();
                for (FindReplaceMatch m : matches) {
                    List<String> row = new ArrayList<>();
                    row.add(++index + "");
                    row.add((m.getStart() / unit + 1) + "");
                    row.add(m.getEnd() / unit + "");
                    row.add(m.getMatchedPrefix());
                    t.add(row);
                }
                t.htmlTable();
            }
            return MessageFormat.format(message("CountNumber"), count);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return message("Invalid");
        }
    }

    @FXML
    protected void showFindExample(Event event) {
        PopTools.popRegexExamples(this, findArea, event);
    }

    @FXML
    protected void popFindExample(Event event) {
        if (UserConfig.getBoolean("RegexExamplesPopWhenMouseHovering", false)) {
            showFindExample(event);
        }
    }

    @FXML
    protected void showFindHistories(Event event) {
        PopTools.popStringValues(this, findArea, event, baseName + "FindString", false, true);
    }

    @FXML
    public void popFindHistories(Event event) {
        if (UserConfig.getBoolean(baseName + "FindStringPopWhenMouseHovering", false)) {
            showFindHistories(event);
        }
    }

    @FXML
    protected void showReplaceHistories(Event event) {
        PopTools.popStringValues(this, replaceArea, event, baseName + "ReplaceString", false, true);
    }

    @FXML
    public void popReplaceHistories(Event event) {
        if (UserConfig.getBoolean(baseName + "ReplaceStringPopWhenMouseHovering", false)) {
            showReplaceHistories(event);
        }
    }

}
