package mara.mybox.fxml;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.MenuController;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.HtmlStyles;

import mara.mybox.value.Languages;
import mara.mybox.value.TimeFormats;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class PopTools {

    public static void browseURI(URI uri) {
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
                    Runtime.getRuntime().exec(new String[]{"xdg-open", uri.toString()});
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
            alertError(Languages.message("DesktopNotSupportBrowse"));
        }
    }

    public static Alert alert(BaseController controller, Alert.AlertType type, String information) {
        try {
            Alert alert = new Alert(type);
            alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            if (controller != null) {
                alert.setTitle(controller.getTitle());
            }
            alert.setHeaderText(null);
            alert.setContentText(information);
            //            alert.getDialogPane().applyCss();
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

    public static Alert alertError(String information) {
        return alertError(null, information);
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
        if (!result.isPresent()) {
            return null;
        }
        String value = result.get();
        return value;
    }

    public static boolean askSure(String title, String sureString) {
        return askSure(title, null, sureString);
    }

    public static boolean askSure(String title, String header, String sureString) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        if (header != null) {
            alert.setHeaderText(header);
        }
        alert.setContentText(sureString);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ButtonType buttonSure = new ButtonType(Languages.message("Sure"));
        ButtonType buttonCancel = new ButtonType(Languages.message("Cancel"));
        alert.getButtonTypes().setAll(buttonSure, buttonCancel);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();
        Optional<ButtonType> result = alert.showAndWait();
        return result.get() == buttonSure;
    }

    public static Popup makePopWindow(BaseController parent, String fxml) {
        try {
            BaseController controller = WindowTools.setScene(fxml);
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

    public static ContextMenu popHtmlStyle(MouseEvent mouseEvent, BaseController controller, ContextMenu inPopMenu, WebEngine webEngine) {
        try {
            if (inPopMenu != null && inPopMenu.isShowing()) {
                inPopMenu.hide();
            }
            final ContextMenu popMenu = new ContextMenu();
            popMenu.setAutoHide(true);
            String baseName = controller == null ? "" : controller.getBaseName();
            MenuItem menu;
            for (HtmlStyles.HtmlStyle style : HtmlStyles.HtmlStyle.values()) {
                menu = new MenuItem(Languages.message(style.name()));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        try {
                            UserConfig.setString(baseName + "HtmlStyle", style.name());
                            if (webEngine == null) {
                                return;
                            }
                            Object c = webEngine.executeScript("document.documentElement.outerHTML");
                            if (c == null) {
                                return;
                            }
                            String html = (String) c;
                            html = HtmlWriteTools.setStyle(html, style);
                            webEngine.loadContent(html);
                        } catch (Exception e) {
                            MyBoxLog.error(e.toString());
                        }
                    }
                });
                popMenu.getItems().add(menu);
            }
            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(Languages.message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);
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
                values.addAll(Arrays.asList("\u516c\u5143960", "\u516c\u5143960-01-23", "\u516c\u51432020-07-10 10:10:10", "\u516c\u5143\u524d202", "\u516c\u5143\u524d770-12-11", "\u516c\u5143\u524d1046-03-10 10:10:10"));
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
            menu = new MenuItem(Languages.message("PopupClose"));
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
            List<String> values = Arrays.asList("^      " + Languages.message("StartLocation"), "$      " + Languages.message("EndLocation"), "*      " + Languages.message("ZeroOrNTimes"), "+      " + Languages.message("OneOrNTimes"), "?      " + Languages.message("ZeroOrOneTimes"), "{n}      " + Languages.message("NTimes"), "{n,}      " + Languages.message("N+Times"), "{n,m}      " + Languages.message("NMTimes"), "|      " + Languages.message("Or"), "[abc]      " + Languages.message("MatchOneCharacters"), "[A-Z]      " + Languages.message("A-Z"), "\\x20      " + Languages.message("Blank"), "\\s      " + Languages.message("NonprintableCharacter"), "\\S      " + Languages.message("PrintableCharacter"), "\\n      " + Languages.message("LineBreak"), "\\r      " + Languages.message("CarriageReturn"), "\\t      " + Languages.message("Tab"), "[0-9]{n}      " + Languages.message("NNumber"), "[A-Z]{n}      " + Languages.message("NUppercase"), "[a-z]{n}      " + Languages.message("NLowercase"), "[\\u4e00-\\u9fa5]      " + Languages.message("Chinese"), "[^\\x00-\\xff]      " + Languages.message("DoubleByteCharacter"), "[A-Za-z0-9]+      " + Languages.message("EnglishAndNumber"), "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*      " + Languages.message("Email"), "(13[0-9]|14[5|7]|15[0|1|2|3|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}      " + Languages.message("PhoneNumber"), "[a-zA-z]+://[^\\s]*       " + Languages.message("URL"), "^(\\s*)\\n       " + Languages.message("BlankLine"), "\\d+\\.\\d+\\.\\d+\\.\\d+      " + Languages.message("IP"));
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
            Hyperlink link = new Hyperlink(Languages.message("AboutRegularExpression"));
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

}
