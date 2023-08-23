package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.stage.Window;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-6-27
 * @License Apache License Version 2.0
 */
public class MenuTextBaseController extends MenuController {

    protected TextInputControl textInput;

    @FXML
    protected Button replaceButton;

    public MenuTextBaseController() {
        baseTitle = Languages.message("Value");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void setParameters(BaseController parent, Node node, double x, double y) {
        try {
            this.node = node;
            if (node != null) {
                if (node instanceof TextInputControl) {
                    textInput = (TextInputControl) node;
                } else if (node instanceof ComboBox) {
                    ComboBox cb = (ComboBox) node;
                    if (cb.isEditable()) {
                        textInput = cb.getEditor();
                    }
                }
            }
            if (textInput != null && !textInput.isEditable() && replaceButton != null) {
                replaceButton.setDisable(true);
            }
            super.setParameters(parent, node, x, y);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void addButtonsPane(List<String> names) {
        List<Node> buttons = new ArrayList<>();
        boolean isTextArea = textInput instanceof TextArea;
        for (String name : names) {
            Button button = new Button(name);
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (isTextArea) {
                        textInput.appendText(name);
                    } else {
                        textInput.setText(name);
                    }
                    textInput.requestFocus();
                }
            });
            buttons.add(button);
        }
        addFlowPane(buttons);
    }

    @FXML
    @Override
    public void myBoxClipBoard() {
        if (textInput == null) {
            super.myBoxClipBoard();
        } else {
            TextClipboardPopController.open(parentController, node);
        }
    }

    @FXML
    @Override
    public boolean menuAction() {
        if (parentController == null || node == null) {
            return false;
        }
        MenuTextEditController.textMenu(parentController, node, initX, initY);
        return true;
    }

    @FXML
    @Override
    public void findAction() {
        if (textInput == null) {
            return;
        }
        Window window = thisPane.getScene().getWindow();
        FindPopController.findMenu(parentController, node, window.getX(), window.getY());
        window.hide();
    }

    @FXML
    @Override
    public void replaceAction() {
        if (textInput == null) {
            return;
        }
        Window window = thisPane.getScene().getWindow();
        FindReplacePopController.replaceMenu(parentController, node, window.getX(), window.getY());
    }

    @FXML
    public void editAction() {
        if (textInput == null) {
            return;
        }
        TextEditorController.edit(textInput.getText());
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (textInput == null) {
            return;
        }
        String text = textInput.getText();
        if (text == null || text.isEmpty()) {
            popError(Languages.message("DoData"));
            return;
        }
        final File file = chooseSaveFile(TargetFileType);
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            @Override
            protected boolean handle() {
                return TextFileTools.writeFile(file, text) != null;
            }

            @Override
            protected void whenSucceeded() {
                popSaved();
                recordFileWritten(file);
            }

        };
        start(task);
    }

}
