package mara.mybox.fxml;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.ControlWebView;
import mara.mybox.controller.MenuController;
import mara.mybox.controller.TextInputController;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.HtmlStyles;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.TimeFormats;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class PopTools {

    public static void browseURI(BaseController controller, URI uri) {
        if (uri == null) {
            return;
        }
        if (SystemTools.isLinux()) {
            // On my CentOS 7, system hangs when both Desktop.isDesktopSupported() and
            // desktop.isSupported(Desktop.Action.BROWSE) are true.
            // https://stackoverflow.com/questions/27879854/desktop-getdesktop-browse-hangs
            // Below workaround for Linux because "Desktop.getDesktop().browse()" doesn't work on some Linux implementations
            try {
                if (Runtime.getRuntime().exec(new String[]{"which", "xdg-open"}).getInputStream().read() > 0) {
                    Runtime.getRuntime().exec(new String[]{"xdg-open",
                        uri.toString()});
                    return;
                } else {
                }
            } catch (Exception e) {
            }
        } else if (SystemTools.isMac()) {
            // https://stackoverflow.com/questions/5226212/how-to-open-the-default-webbrowser-using-java/28807079#28807079
            try {
                Runtime rt = Runtime.getRuntime();
                rt.exec("open " + uri.toString());
                return;
            } catch (Exception e) {
            }
        } else if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(uri);
                    // https://stackoverflow.com/questions/23176624/javafx-freeze-on-desktop-openfile-desktop-browseuri?r=SearchResults
                    // Menus are blocked after system explorer is opened
                    //                    if (myStage != null) {
                    //                        new Timer().schedule(new TimerTask() {
                    //                            @Override
                    //                            public void run() {
                    //                                Platform.runLater(() -> {
                    //                                    myStage.requestFocus();
                    //                                });
                    //                            }
                    //                        }, 1000);
                    //                    }
                    return;
                } catch (Exception e) {
                    MyBoxLog.error(e.toString());
                }
            }
        }
        if (!uri.getScheme().equals("file") || new File(uri.getPath()).isFile()) {
            ControllerTools.openTarget(null, uri.toString());
        } else {
            alertError(controller, message("DesktopNotSupportBrowse"));
        }
    }

    public static Alert alert(BaseController controller, Alert.AlertType type, String information) {
        try {
            Alert alert = new Alert(type);
            if (controller != null) {
                if (controller.getAlert() != null) {
                    controller.getAlert().close();
                }
                alert.setTitle(controller.getTitle());
                controller.setAlert(alert);
            }
            alert.setHeaderText(null);
            alert.setContentText(information);
            alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.getDialogPane().applyCss();
            // https://stackoverflow.com/questions/38799220/javafx-how-to-bring-dialog-alert-to-the-front-of-the-screen?r=SearchResults
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();
            stage.sizeToScene();
            alert.showAndWait();
            return alert;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static Alert alertInformation(BaseController controller, String information) {
        return alert(controller, Alert.AlertType.INFORMATION, information);
    }

    public static Alert alertWarning(BaseController controller, String information) {
        return alert(controller, Alert.AlertType.WARNING, information);
    }

    public static Alert alertError(BaseController controller, String information) {
        return alert(controller, Alert.AlertType.ERROR, information);
    }

    public static String askValue(String title, String header, String name, String initValue) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(name);
        dialog.getEditor().setText(initValue);
        dialog.getEditor().setPrefWidth(initValue == null ? 200 : Math.min(600, initValue.length() * AppVariables.sceneFontSize));
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();
        Optional<String> result = dialog.showAndWait();
        if (result == null || !result.isPresent()) {
            return null;
        }
        String value = result.get();
        return value;
    }

    public static boolean askSure(BaseController controller, String title, String sureString) {
        return askSure(controller, title, null, sureString);
    }

    public static boolean askSure(BaseController controller, String title, String header, String sureString) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        if (controller != null) {
            if (controller.getAlert() != null) {
                controller.getAlert().close();
            }
            controller.setAlert(alert);
        }
        alert.setTitle(title);
        if (header != null) {
            alert.setHeaderText(header);
        }
        alert.setContentText(sureString);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ButtonType buttonSure = new ButtonType(message("Sure"));
        ButtonType buttonCancel = new ButtonType(message("Cancel"));
        alert.getButtonTypes().setAll(buttonSure, buttonCancel);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();
        Optional<ButtonType> result = alert.showAndWait();
        return result != null && result.isPresent() && result.get() == buttonSure;
    }

    public static Popup makePopWindow(BaseController parent, String fxml) {
        try {
            BaseController controller = WindowTools.loadFxml(fxml);
            if (controller == null) {
                return null;
            }
            Popup popup = new Popup();
            popup.setAutoHide(true);
            popup.getContent().add(controller.getMyScene().getRoot());
            popup.setUserData(controller);
            popup.setOnHiding((WindowEvent event) -> {
                WindowTools.closeWindow(popup);
            });
            controller.setParentController(parent);
            controller.setMyWindow(popup);
            if (parent != null) {
                parent.closePopup();
                parent.setPopup(popup);
            }
            return popup;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static Popup popWindow(BaseController parent, String fxml, Node owner, double x, double y) {
        try {
            Popup popup = makePopWindow(parent, fxml);
            if (popup == null) {
                return null;
            }
            popup.show(owner, x, y);
            return popup;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ContextMenu popHtmlStyle(MouseEvent mouseEvent, ControlWebView controller) {
        try {
            if (mouseEvent == null || controller == null) {
                return null;
            }
            ContextMenu cMenu = controller.getPopMenu();
            if (cMenu != null && cMenu.isShowing()) {
                cMenu.hide();
            }
            final ContextMenu popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            String baseName = controller.getBaseName();

            MenuItem menu = new MenuItem(message("HtmlStyle"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

            ToggleGroup sgroup = new ToggleGroup();
            String prefix = UserConfig.getBoolean(baseName + "ShareHtmlStyle", true) ? "AllInterface" : baseName;
            String currentStyle = UserConfig.getString(prefix + "HtmlStyle", null);

            RadioMenuItem rmenu = new RadioMenuItem(message("None"));
            rmenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    controller.setStyle(null);
                }
            });
            rmenu.setSelected(currentStyle == null);
            rmenu.setToggleGroup(sgroup);
            popMenu.getItems().add(rmenu);

            boolean predefinedValue = false;
            for (HtmlStyles.HtmlStyle style : HtmlStyles.HtmlStyle.values()) {
                rmenu = new RadioMenuItem(message(style.name()));
                String styleValue = HtmlStyles.styleValue(style);
                rmenu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        controller.setStyle(styleValue);
                    }
                });
                boolean isCurrent = currentStyle != null && currentStyle.equals(styleValue);
                rmenu.setSelected(isCurrent);
                rmenu.setToggleGroup(sgroup);
                popMenu.getItems().add(rmenu);
                if (isCurrent) {
                    predefinedValue = true;
                }
            }

            rmenu = new RadioMenuItem(message("Input") + "...");
            rmenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    TextInputController inputController = TextInputController.open(controller,
                            message("Style"), UserConfig.getString(prefix + "HtmlStyle", null));
                    inputController.getNotify().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                            String value = inputController.getText();
                            if (value == null || value.isBlank()) {
                                value = null;
                            }
                            controller.setStyle(value);
                            inputController.closeStage();
                        }
                    });
                }
            });
            rmenu.setSelected(currentStyle != null && !predefinedValue);
            rmenu.setToggleGroup(sgroup);
            popMenu.getItems().add(rmenu);

            popMenu.getItems().add(new SeparatorMenuItem());

            CheckMenuItem checkMenu = new CheckMenuItem(message("ShareAllInterface"));
            checkMenu.setSelected(UserConfig.getBoolean(baseName + "ShareHtmlStyle", true));
            checkMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "ShareHtmlStyle", checkMenu.isSelected());
                }
            });
            popMenu.getItems().add(checkMenu);

            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);
            controller.setPopMenu(popMenu);
            LocateTools.locateCenter((Region) mouseEvent.getSource(), popMenu);
            return popMenu;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ContextMenu popEraExample(ContextMenu inPopMenu, TextField input, MouseEvent mouseEvent) {
        try {
            if (inPopMenu != null && inPopMenu.isShowing()) {
                inPopMenu.hide();
            }
            final ContextMenu popMenu = new ContextMenu();
            popMenu.setAutoHide(true);
            List<String> values = new ArrayList<>();
            values.add(DateTools.nowString());
            values.add(DateTools.datetimeToString(new Date(), TimeFormats.DatetimeMs, TimeZone.getDefault()));
            values.add(DateTools.datetimeToString(new Date(), TimeFormats.TimeMs, TimeZone.getDefault()));
            values.add(DateTools.datetimeToString(new Date(), TimeFormats.DatetimeMs + " Z", TimeZone.getDefault()));
            values.addAll(Arrays.asList("2020-07-15T36:55:09", "960-01-23", "581", "-2020-07-10 10:10:10.532 +0800", "-960-01-23", "-581"));
            if (Languages.isChinese()) {
                values.addAll(Arrays.asList("\u516c\u5143960", "\u516c\u5143960-01-23", "\u516c\u51432020-07-10 10:10:10",
                        "\u516c\u5143\u524d202", "\u516c\u5143\u524d770-12-11", "\u516c\u5143\u524d1046-03-10 10:10:10"));
            }
            values.addAll(Arrays.asList("202 BC", "770-12-11 BC", "1046-03-10 10:10:10 BC", "581 AD", "960-01-23 AD", "2020-07-10 10:10:10 AD"));
            MenuItem menu;
            for (String value : values) {
                menu = new MenuItem(value);
                menu.setOnAction((ActionEvent event) -> {
                    input.setText(value);
                });
                popMenu.getItems().add(menu);
            }
            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);
            LocateTools.locateMouse(mouseEvent, popMenu);
            return popMenu;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void popRegexExample(BaseController parent, TextInputControl input, MouseEvent mouseEvent) {
        try {
            MenuController controller = MenuController.open(parent, input, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            Button clearButton = new Button(message("Clear"));
            clearButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    input.clear();
                }
            });
            controller.addNode(clearButton);
            List<String> values = Arrays.asList("^      "
                    + message("StartLocation"), "$      "
                    + message("EndLocation"), "*      "
                    + message("ZeroOrNTimes"), "+      "
                    + message("OneOrNTimes"), "?      "
                    + message("ZeroOrOneTimes"), "{n}      "
                    + message("NTimes"), "{n,}      "
                    + message("N+Times"), "{n,m}      "
                    + message("NMTimes"), "|      "
                    + message("Or"), "[abc]      "
                    + message("MatchOneCharacters"), "[A-Z]      "
                    + message("A-Z"), "\\x20      "
                    + message("Blank"), "\\s      "
                    + message("NonprintableCharacter"), "\\S      "
                    + message("PrintableCharacter"), "\\n      "
                    + message("LineBreak"), "\\r      "
                    + message("CarriageReturn"), "\\t      "
                    + message("Tab"), "[0-9]{n}      "
                    + message("NNumber"), "[A-Z]{n}      "
                    + message("NUppercase"), "[a-z]{n}      "
                    + message("NLowercase"), "[\\u4e00-\\u9fa5]      "
                    + message("Chinese"), "[^\\x00-\\xff]      "
                    + message("DoubleByteCharacter"), "[A-Za-z0-9]+      "
                    + message("EnglishAndNumber"), "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*      "
                    + message("Email"), "(13[0-9]|14[5|7]|15[0|1|2|3|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}      "
                    + message("PhoneNumber"), "[a-zA-z]+://[^\\s]*       "
                    + message("URL"), "^(\\s*)\\n       "
                    + message("BlankLine"), "\\d+\\.\\d+\\.\\d+\\.\\d+      "
                    + message("IP"));
            List<Node> nodes = new ArrayList<>();
            for (String value : values) {
                String[] vv = value.split("      ");
                Button button = new Button(vv[1].trim());
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        input.appendText(vv[0]);
                    }
                });
                NodeStyleTools.setTooltip(button, new Tooltip(vv[0]));
                nodes.add(button);
            }
            controller.addFlowPane(nodes);

            Hyperlink link = new Hyperlink(message("AboutRegularExpression"));
            link.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (Languages.isChinese()) {
                        parent.openLink("https://baike.baidu.com/item/%E6%AD%A3%E5%88%99%E8%A1%A8%E8%BE%BE%E5%BC%8F/1700215");
                    } else {
                        parent.openLink("https://en.wikipedia.org/wiki/Regular_expression");
                    }
                }
            });
            controller.addNode(link);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void popColorExamples(BaseController parent, TextInputControl input, MouseEvent mouseEvent) {
        try {
            MenuController controller = MenuController.open(parent, input, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            Button clearButton = new Button(message("Clear"));
            clearButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    input.clear();
                }
            });

            List<String> values = new ArrayList<>();
            values.addAll(Arrays.asList(
                    "orange", "pink", "lightblue", "wheat",
                    "0xff668840", "0x5f86df", "#226688", "#68f",
                    "rgb(255,102,136)", "rgb(100%,60%,50%)",
                    "rgba(102,166,136,0.25)", "rgba(155,20%,70%,0.25)",
                    "hsl(240,70%,80%)", "hsla(60,50%,60%,0.25)"
            ));

            boolean isTextArea = input instanceof TextArea;
            List<Node> nodes = new ArrayList<>();
            for (String value : values) {
                Button button = new Button(value);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        if (isTextArea) {
                            input.appendText(value + "\n");
                        } else {
                            input.setText(value);
                        }
                        input.requestFocus();
                    }
                });
                nodes.add(button);
            }
            controller.addFlowPane(nodes);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void popStringValues(BaseController parent, TextInputControl input, MouseEvent mouseEvent, String name) {
        try {
            int max = UserConfig.getInt(name + "MaxSaved", 20);

            MenuController controller = MenuController.open(parent, input, mouseEvent.getScreenX(), mouseEvent.getScreenY());

            List<Node> setButtons = new ArrayList<>();
            Button clearButton = new Button(message("Clear"));
            clearButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    input.clear();
                }
            });
            setButtons.add(clearButton);

            Button maxButton = new Button(message("MaxSaved"));
            maxButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String value = PopTools.askValue(parent.getTitle(), null, message("MaxSaved"), max + "");
                    if (value == null) {
                        return;
                    }
                    try {
                        int v = Integer.parseInt(value);
                        UserConfig.setInt(name + "MaxSaved", v);
                    } catch (Exception e) {
                        MyBoxLog.error(e);
                    }
                }
            });
            setButtons.add(maxButton);
            controller.addFlowPane(setButtons);

            List<String> values = TableStringValues.max(name, max);
            List<Node> valueButtons = new ArrayList<>();
            for (String value : values) {
                Button button = new Button(value);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        input.setText(value);
                    }
                });
                valueButtons.add(button);
            }
            controller.addFlowPane(valueButtons);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static ContextMenu popWindowStyles(BaseController parent, String baseStyle, MouseEvent mouseEvent) {
        try {
            ContextMenu popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            String baseName = parent.getBaseName();
            MenuItem menu = new MenuItem(message("WindowStyle"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

            Map<String, String> styles = new LinkedHashMap<>();
            styles.put("None", "");
            styles.put("Transparent", "; -fx-text-fill: black; -fx-background-color: transparent;");
            styles.put("Console", "; -fx-text-fill: #CCFF99; -fx-background-color: black;");
            styles.put("Blackboard", "; -fx-text-fill: white; -fx-background-color: #336633;");
            styles.put("Ago", "; -fx-text-fill: white; -fx-background-color: darkblue;");
            styles.put("Book", "; -fx-text-fill: black; -fx-background-color: #F6F1EB;");
            ToggleGroup sgroup = new ToggleGroup();
            String prefix = UserConfig.getBoolean(baseName + "ShareWindowStyle", true) ? "AllInterface" : baseName;
            String currentStyle = UserConfig.getString(prefix + "WindowStyle", "");

            for (String name : styles.keySet()) {
                RadioMenuItem rmenu = new RadioMenuItem(message(name));
                String style = styles.get(name);
                rmenu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        UserConfig.setString(prefix + "WindowStyle", style);
                        parent.getThisPane().setStyle(baseStyle + style);
                        setMenuLabelsStyle(parent.getThisPane(), baseStyle + style);
                    }
                });
                rmenu.setSelected(currentStyle != null && currentStyle.equals(style));
                rmenu.setToggleGroup(sgroup);
                popMenu.getItems().add(rmenu);
            }
            popMenu.getItems().add(new SeparatorMenuItem());

            CheckMenuItem checkMenu = new CheckMenuItem(message("ShareAllInterface"));
            checkMenu.setSelected(UserConfig.getBoolean(baseName + "ShareWindowStyle", true));
            checkMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "ShareWindowStyle", checkMenu.isSelected());
                }
            });
            popMenu.getItems().add(checkMenu);

            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            parent.closePopup();
            parent.setPopMenu(popMenu);

            LocateTools.locateMouse(mouseEvent, popMenu);
            return popMenu;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void setMenuLabelsStyle(Node node, String style) {
        if (node instanceof Label) {
            node.setStyle(style);
        } else if (node instanceof Parent && !(node instanceof TableView)) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                setMenuLabelsStyle(child, style);
            }
        }
    }

    public static void setWindowStyle(Pane pane, String baseName, String baseStyle) {
        String prefix = UserConfig.getBoolean(baseName + "ShareWindowStyle", true) ? "AllInterface" : baseName;
        String style = UserConfig.getString(prefix + "WindowStyle", "");
        pane.setStyle(baseStyle + style);
        setMenuLabelsStyle(pane, baseStyle + style);
    }

    public static void closeAllPopup() {
        try {
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                if (window instanceof Popup) {
                    window.hide();
                }
            }
        } catch (Exception e) {
        }
    }

}
