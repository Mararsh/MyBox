package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputControl;
import javafx.stage.Window;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-6-27
 * @License Apache License Version 2.0
 */
public class MenuTextBaseController extends MenuController {

    protected TextInputControl textInput;

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
            super.setParameters(parent, node, x, y);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
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
    public void menuAction() {
        if (parentController == null || node == null) {
            return;
        }
        MenuTextEditController.open(parentController, node, initX, initY);
    }

    @FXML
    @Override
    public void findAction() {
        if (textInput == null) {
            return;
        }
        Window window = thisPane.getScene().getWindow();
        FindPopController.open(parentController, node, window.getX(), window.getY());
        window.hide();
    }

    @FXML
    @Override
    public void replaceAction() {
        if (textInput == null) {
            return;
        }
        Window window = thisPane.getScene().getWindow();
        FindReplacePopController.open(parentController, node, window.getX(), window.getY());
        window.hide();
    }

    @FXML
    public void editAction() {
        if (textInput == null) {
            return;
        }
        TextEditorController controller = (TextEditorController) openStage(Fxmls.TextEditorFxml);
        controller.loadContents(textInput.getText());
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
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            final File file = chooseSaveFile(TargetFileType);
            if (file == null) {
                return;
            }
            task = new SingletonTask<Void>() {

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
            if (parentController != null) {
                parentController.handling(task);
            } else {
                handling(task);
            }
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

}
