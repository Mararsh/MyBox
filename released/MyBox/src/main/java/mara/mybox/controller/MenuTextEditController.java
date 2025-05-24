package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.PdfTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-7-22
 * @License Apache License Version 2.0
 */
public class MenuTextEditController extends MenuTextBaseController {

    protected ChangeListener<String> textListener;
    protected ChangeListener<IndexRange> selectionListener;

    @FXML
    protected HBox fileBox;
    @FXML
    protected Button findButton;

    public MenuTextEditController() {
        baseTitle = message("Texts");
    }

    @Override
    public void setParameters(BaseController parent, Node node, double x, double y) {
        try {
            super.setParameters(parent, node, x, y);

            if (parent instanceof BaseTextController) {
                BaseTextController e = (BaseTextController) parent;
                if (textInput == null || textInput != e.mainArea) {
                    fileBox.getChildren().removeAll(saveButton, recoverButton);
                }
            } else {
                fileBox.getChildren().removeAll(saveButton, recoverButton);
            }
            if (textInput != null) {
                textListener = new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        checkEditPane();
                    }
                };
                textInput.textProperty().addListener(textListener);

                selectionListener = new ChangeListener<IndexRange>() {
                    @Override
                    public void changed(ObservableValue ov, IndexRange oldValue, IndexRange newValue) {
                        checkEditPane();
                    }
                };
                textInput.selectionProperty().addListener(selectionListener);
                checkEditPane();
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void checkEditPane() {
        if (textInput == null) {
            return;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                IndexRange range = textInput.getSelection();
                int selection = range != null ? range.getLength() : 0;
                String info = message("CharactersNumber") + ": " + StringTools.format(textInput.getLength()) + "  ";
                if (selection > 0) {
                    info += message("Selection") + ": [" + StringTools.format(range.getStart() + 1)
                            + "-" + StringTools.format(range.getEnd()) + "]" + StringTools.format(selection);
                } else {
                    info += message("Cursor") + ": " + StringTools.format(textInput.getAnchor() + 1) + " " + message("Selection") + ": 0";
                }
                bottomLabel.setText(info);
                if (undoButton != null) {
                    undoButton.setDisable(!textInput.isEditable() || textInput.isDisable() || !textInput.isUndoable());
                }
                if (redoButton != null) {
                    redoButton.setDisable(!textInput.isEditable() || textInput.isDisable() || !textInput.isRedoable());
                }
                boolean selectNone = selection < 1;
                if (cropButton != null) {
                    cropButton.setDisable(!textInput.isEditable() || textInput.isDisable() || selectNone);
                }
                if (deleteButton != null) {
                    deleteButton.setDisable(!textInput.isEditable() || textInput.isDisable() || selectNone);
                }
                if (clearButton != null) {
                    clearButton.setDisable(!textInput.isEditable() || textInput.isDisable());
                }
                if (pasteContentInSystemClipboardButton != null) {
                    pasteContentInSystemClipboardButton.setDisable(!textInput.isEditable() || textInput.isDisable()
                            || !TextClipboardTools.systemClipboardHasString());
                }
                boolean empty = textInput.getLength() < 1;
                if (selectAllButton != null) {
                    selectAllButton.setDisable(empty);
                }
                if (selectNoneButton != null) {
                    selectNoneButton.setDisable(empty);
                }
                if (editButton != null) {
                    editButton.setDisable(empty);
                }
                if (copyToSystemClipboardButton != null) {
                    copyToSystemClipboardButton.setDisable(empty);
                }
                if (copyToMyBoxClipboardButton != null) {
                    copyToMyBoxClipboardButton.setDisable(empty);
                }
                if (saveAsButton != null) {
                    saveAsButton.setDisable(empty);
                }
                if (findButton != null) {
                    findButton.setDisable(empty);
                }
                if (replaceButton != null) {
                    replaceButton.setDisable(empty || !textInput.isEditable() || textInput.isDisable());
                }
                if (TextClipboardTools.isMonitoringCopy()) {
                    NodeStyleTools.setTooltip(copyToSystemClipboardButton, new Tooltip(message("CopyToClipboards") + "\nCTRL+c / ALT+c"));
                } else {
                    NodeStyleTools.setTooltip(copyToSystemClipboardButton, new Tooltip(message("CopyToSystemClipboard") + "\nCTRL+c / ALT+c"));
                }
            }
        });
    }

    @FXML
    @Override
    public void copyToSystemClipboard() {
        if (textInput == null) {
            return;
        }
        TextClipboardTools.copyToSystemClipboard(myController, textInput);
        checkEditPane();
    }

    @FXML
    @Override
    public void copyToMyBoxClipboard() {
        if (textInput == null) {
            return;
        }
        TextClipboardTools.copyToMyBoxClipboard(myController, textInput);
        checkEditPane();
    }

    @FXML
    @Override
    public void pasteContentInSystemClipboard() {
        if (textInput == null) {
            return;
        }
        textInput.paste();
    }

    @FXML
    @Override
    public void cropAction() {
        if (textInput == null) {
            return;
        }
        textInput.cut();
    }

    @FXML
    @Override
    public void deleteAction() {
        if (textInput == null) {
            return;
        }
        textInput.deleteText(textInput.getSelection());
    }

    @FXML
    @Override
    public void clearAction() {
        if (textInput == null) {
            return;
        }
        textInput.clear();
    }

    @FXML
    @Override
    public void selectAllAction() {
        if (textInput == null) {
            return;
        }
        textInput.selectAll();
    }

    @FXML
    @Override
    public void selectNoneAction() {
        if (textInput == null) {
            return;
        }
        textInput.deselect();
    }

    @FXML
    @Override
    public void undoAction() {
        if (textInput == null) {
            return;
        }
        textInput.undo();
    }

    @FXML
    @Override
    public void redoAction() {
        if (textInput == null) {
            return;
        }
        textInput.redo();
    }

    @FXML
    @Override
    public void saveAction() {
        if (textInput == null || !(parentController instanceof BaseTextController)) {
            return;
        }
        parentController.saveAction();
    }

    @FXML
    @Override
    public void recoverAction() {
        if (textInput == null || !(parentController instanceof BaseTextController)) {
            return;
        }
        parentController.recoverAction();
    }

    @FXML
    @Override
    public boolean popAction() {
        if (textInput == null) {
            return false;
        }
        TextPopController.openInput(parentController, textInput);
        return true;
    }

    @FXML
    public void htmlAction() {
        if (textInput == null) {
            return;
        }
        String text = textInput.getText();
        if (text == null || text.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        popInformation(message("WaitAndHandling"));
        FxTask htmltask = new FxTask<Void>(this) {

            private String html;

            @Override
            protected boolean handle() {
                html = HtmlWriteTools.textToHtml(text);
                return html != null;
            }

            @Override
            protected void whenSucceeded() {
                WebBrowserController.openHtml(html, true);
            }

        };
        start(htmltask, false);
    }

    @FXML
    public void pdfAction() {
        if (textInput == null) {
            return;
        }
        String text = textInput.getText();
        if (text == null || text.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        popInformation(message("WaitAndHandling"));
        FxTask pdftask = new FxTask<Void>(this) {

            private File pdf;

            @Override
            protected boolean handle() {
                pdf = PdfTools.text2pdf(this, text);
                return pdf != null && pdf.exists();
            }

            @Override
            protected void whenSucceeded() {
                PdfViewController.open(pdf);
            }

        };
        start(pdftask, false);
    }

    @FXML
    public void snapAction() {
        if (textInput == null) {
            return;
        }
        String text = textInput.getText();
        if (text == null || text.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        ImageEditorController.openImage(NodeTools.snap(textInput));
    }

    @Override
    public void cleanPane() {
        try {
            if (textInput != null) {
                textInput.textProperty().removeListener(textListener);
                textInput.selectionProperty().removeListener(selectionListener);
            }
            textListener = null;
            selectionListener = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        static methods
     */
    public static void closeAll() {

    }

    public static MenuTextEditController textMenu(BaseController parent, Node node, double x, double y) {
        try {
            if (parent == null || node == null) {
                return null;
            }
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                Object object = window.getUserData();
                if (object != null && object instanceof MenuTextEditController) {
                    try {
                        MenuTextEditController controller = (MenuTextEditController) object;
                        if (controller.textInput != null && controller.textInput.equals(node)) {
                            controller.close();
                        }
                    } catch (Exception e) {
                    }
                }
            }
            MenuTextEditController controller = (MenuTextEditController) WindowTools.referredTopStage(
                    parent, Fxmls.MenuTextEditFxml);
            controller.setParameters(parent, node, x, y);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static MenuTextEditController textMenu(BaseController parent, Region node) {
        try {
            if (parent == null || node == null) {
                return null;
            }
            Point2D localToScreen = node.localToScreen(node.getWidth() - 80, 80);
            return textMenu(parent, node, localToScreen.getX(), localToScreen.getY());
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static MenuTextEditController textMenu(BaseController parent, Node node, MouseEvent event) {
        return textMenu(parent, node, event.getScreenX() + 40, event.getScreenY() + 40);
    }

    public static MenuTextEditController textMenu(BaseController parent, Node node, ContextMenuEvent event) {
        return textMenu(parent, node, event.getScreenX() + 40, event.getScreenY() + 40);
    }
}
