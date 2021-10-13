package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Separator;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import mara.mybox.data.FileEditInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.ByteTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-7-24
 * @License Apache License Version 2.0
 */
public class MenuBytesEditController extends MenuTextEditController {

    public MenuBytesEditController() {
        baseTitle = Languages.message("Bytes");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Bytes);
    }

    @Override
    public void setParameters(BaseController parent, Node node, double x, double y) {
        try {
            super.setParameters(parent, node, x, y);
            if (textInput != null && textInput.isEditable()) {
                addBytesButton();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void addBytesButton() {
        try {
            if (textInput == null) {
                return;
            }
            addNode(new Separator());

            List<Node> number = new ArrayList<>();
            for (int i = 0; i <= 9; ++i) {
                String s = i + "";
                Button button = new Button(s);
                String value = ByteTools.stringToHexFormat(s);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        textInput.insertText(textInput.getSelection().getStart(), value);
                    }
                });
                NodeStyleTools.setTooltip(button, value);
                number.add(button);
            }
            addFlowPane(number);
            addNode(new Separator());

            List<Node> AZ = new ArrayList<>();
            for (char i = 'A'; i <= 'Z'; ++i) {
                String s = i + "";
                String value = ByteTools.stringToHexFormat(s);
                Button button = new Button(s);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        textInput.insertText(textInput.getSelection().getStart(), value);
                    }
                });
                NodeStyleTools.setTooltip(button, value);
                AZ.add(button);
            }
            addFlowPane(AZ);
            addNode(new Separator());

            List<Node> az = new ArrayList<>();
            for (char i = 'a'; i <= 'z'; ++i) {
                String s = i + "";
                String value = ByteTools.stringToHexFormat(s);
                Button button = new Button(s);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        textInput.insertText(textInput.getSelection().getStart(), value);
                    }
                });
                NodeStyleTools.setTooltip(button, value);
                az.add(button);
            }
            addFlowPane(az);
            addNode(new Separator());

            List<String> names = Arrays.asList("LF", "CR", Languages.message("Space"),
                    "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+", "-",
                    ",", ".", "/", ":", ";", "<", "=", ">", "?", "@", "[", "]", "\\", "^", "_", "`",
                    "{", "}", "|", "~");
            List<Node> special = new ArrayList<>();
            for (int i = 0; i < names.size(); i++) {
                String name = names.get(i);
                Button button = new Button(name);
                if (name.equals(Languages.message("Space"))) {
                    name = " ";
                } else if (name.equals("LF")) {
                    name = "\n";
                } else if (name.equals("CR")) {
                    name = "\r";
                }
                String value = ByteTools.stringToHexFormat(name);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        textInput.insertText(textInput.getSelection().getStart(), value);
                    }
                });
                NodeStyleTools.setTooltip(button, value);
                special.add(button);
            }
            addFlowPane(special);

            Hyperlink link = new Hyperlink(Languages.message("AsciiTable"));
            link.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    openLink("https://www.ascii-code.com/");
                }
            });
            addNode(link);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void editAction() {
        BytesEditorController controller = (BytesEditorController) openStage(Fxmls.BytesEditorFxml);
        controller.loadContents(textInput.getText());
    }

    @FXML
    public void hexAction() {
        String text = textInput.getText();
        text = ByteTools.formatTextHex(text);
        if (text != null) {
            if (text.isEmpty()) {
                return;
            }
            String hex;
            if (parentController instanceof BytesEditorController) {
                BytesEditorController c = (BytesEditorController) parentController;
                FileEditInformation info = c.sourceInformation;
                hex = ByteTools.formatHex(text, info.getLineBreak(), info.getLineBreakWidth(), info.getLineBreakValue());
            } else {
                hex = ByteTools.formatHex(text, FileEditInformation.Line_Break.Width, 30, "0A");
            }
            isSettingValues = true;
            textInput.setText(hex);
            isSettingValues = false;
        } else {
            popError(message("InvalidData"));
        }
    }

    @FXML
    @Override
    public boolean popAction() {
        if (textInput == null) {
            return false;
        }
        BytesPopController.open(parentController, textInput);
        return true;
    }

    /*
        static methods
     */
    public static MenuBytesEditController open(BaseController parent, Node node, double x, double y) {
        try {
            if (parent == null || node == null) {
                return null;
            }
            Popup popup = PopTools.popWindow(parent, Fxmls.MenuBytesEditFxml, node, x, y);
            if (popup == null) {
                return null;
            }
            Object object = popup.getUserData();
            if (object == null && !(object instanceof MenuBytesEditController)) {
                return null;
            }
            MenuBytesEditController controller = (MenuBytesEditController) object;
            controller.setParameters(parent, node, x, y);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static MenuBytesEditController open(BaseController parent, Node node, MouseEvent event) {
        return open(parent, node, event.getScreenX() + 40, event.getScreenY() + 40);
    }

    public static MenuBytesEditController open(BaseController parent, Node node, ContextMenuEvent event) {
        return open(parent, node, event.getScreenX() + 40, event.getScreenY() + 40);
    }

}
