package mara.mybox.fxml;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jdk.jshell.JShell;
import jdk.jshell.SourceCodeAnalysis;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.BaseController_Attributes;
import mara.mybox.controller.BaseLogsController;
import mara.mybox.controller.ControlWebView;
import mara.mybox.controller.HtmlStyleInputController;
import mara.mybox.controller.MenuController;
import mara.mybox.data2d.Data2D;
import static mara.mybox.db.data.ColumnDefinition.ColumnType.Color;
import static mara.mybox.db.data.ColumnDefinition.ColumnType.Date;
import static mara.mybox.db.data.ColumnDefinition.ColumnType.Datetime;
import static mara.mybox.db.data.ColumnDefinition.ColumnType.Enumeration;
import static mara.mybox.db.data.ColumnDefinition.ColumnType.EnumerationEditable;
import static mara.mybox.db.data.ColumnDefinition.ColumnType.Era;
import static mara.mybox.db.data.ColumnDefinition.ColumnType.File;
import static mara.mybox.db.data.ColumnDefinition.ColumnType.Image;
import static mara.mybox.db.data.ColumnDefinition.ColumnType.Latitude;
import static mara.mybox.db.data.ColumnDefinition.ColumnType.Longitude;
import static mara.mybox.db.data.ColumnDefinition.ColumnType.Short;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
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

    /*
        common
     */
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
                    Runtime.getRuntime().exec(new String[]{"xdg-open", uri.toString()});
                    return;
                }
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }

        } else if (SystemTools.isMac()) {
            // https://stackoverflow.com/questions/5226212/how-to-open-the-default-webbrowser-using-java/28807079#28807079
            try {
                Runtime.getRuntime().exec(new String[]{"open", uri.toString()});
                return;
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
        } else if (Desktop.isDesktopSupported()) {
            // https://stackoverflow.com/questions/23176624/javafx-freeze-on-desktop-openfile-desktop-browseuri?r=SearchResults
            // interface are blocked after system explorer is opened. Happened again after javafx 17.0.2
//            new Thread(() -> {
//                try {
//                    Desktop.getDesktop().browse(uri);
//                } catch (Exception e) {
//                    MyBoxLog.debug(e);
//                }
//            }).start();
            try {
                Runtime.getRuntime().exec(new String[]{"explorer.exe", uri.toString()});
                return;
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
        }
        if (!uri.getScheme().equals("file") || new File(uri.getPath()).isFile()) {
            ControllerTools.openTarget(uri.toString());
        } else {
            alertError(controller, message("DesktopNotSupportBrowse"));
        }
    }

    public static Alert alert(BaseController controller, Alert.AlertType type, String information) {
        try {
            Alert alert = new Alert(type);
            if (controller != null) {
                alert.setTitle(controller.getTitle());
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
        return askValue(title, header, name, initValue, 400);
    }

    public static String askValue(String title, String header, String name, String initValue, int minWidth) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(name);
        dialog.getEditor().setText(initValue);
        dialog.getEditor().setPrefWidth(initValue == null ? minWidth : Math.min(minWidth, initValue.length() * AppVariables.sceneFontSize));
        dialog.getEditor().selectEnd();
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();
        stage.getScene().getRoot().requestFocus();
        Optional<String> result = dialog.showAndWait();
        if (result == null || !result.isPresent()) {
            return null;
        }
        String value = result.get();
        return value;
    }

    public static boolean askSure(String title, String sureString) {
        return askSure(title, null, sureString);
    }

    // https://openjfx.io/javadoc/17/javafx.controls/javafx/scene/control/Dialog.html
    public static boolean askSure(String title, String header, String sureString) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        if (header != null) {
            alert.setHeaderText(header);
        }
        alert.setContentText(sureString);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ButtonType buttonSure = new ButtonType(message("Sure"));
        ButtonType buttonCancel = new ButtonType(message("Cancel"), ButtonData.CANCEL_CLOSE);
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
            controller.setParent(parent, BaseController_Attributes.StageType.Popup);
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

    public static void showError(BaseController controller, String error) {
        if (controller == null) {
            MyBoxLog.error(error);
        } else if (controller instanceof BaseLogsController) {
            ((BaseLogsController) controller).updateLogs(error, true, true);
        } else if (controller.getTask() != null) {
            controller.getTask().setError(error);
        } else {
            Platform.runLater(() -> {
                controller.alertError(error);
//                MyBoxLog.debug(error);
            });
        }
    }

    /*
        buttons
     */
    public static void addButtonsPane(MenuController controller, TextInputControl input,
            List<String> values) {
        addButtonsPane(controller, input, values, -1);
    }

    public static void addButtonsPane(MenuController controller, TextInputControl input,
            List<String> values, int index) {
        try {
            String valueName = controller.getMenuName();
            List<Node> buttons = new ArrayList<>();
            for (String value : values) {
                if (value == null) {
                    continue;
                }
                Button button = new Button(value.length() > 200 ? value.substring(0, 200) + " ..." : value);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        if (UserConfig.getBoolean(valueName + "ValuesClearAndSet", true)) {
                            input.setText(value);
                        } else {
                            input.replaceText(input.getSelection(), value);
                        }
                        if (UserConfig.getBoolean(valueName + "ValuesCloseAfterPaste", true)) {
                            controller.close();
                        } else {
                            controller.getThisPane().requestFocus();
                        }
                        input.requestFocus();
                    }
                });
                buttons.add(button);
            }
            controller.addFlowPane(buttons);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        style
     */
    public static ContextMenu popHtmlStyle(Event event, ControlWebView controller) {
        try {
            if (event == null || controller == null) {
                return null;
            }
            List<MenuItem> items = new ArrayList<>();

            String baseName = controller.getBaseName();

            MenuItem menu = new MenuItem(message("HtmlStyle"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            items.add(menu);
            items.add(new SeparatorMenuItem());

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
            items.add(rmenu);

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
                items.add(rmenu);
                if (isCurrent) {
                    predefinedValue = true;
                }
            }

            rmenu = new RadioMenuItem(message("Input") + "...");
            rmenu.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    HtmlStyleInputController inputController = HtmlStyleInputController.open(controller,
                            message("Style"), UserConfig.getString(prefix + "HtmlStyle", null));
                    inputController.getNotify().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                            String value = inputController.getInputString();
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
            items.add(rmenu);

            items.add(new SeparatorMenuItem());

            CheckMenuItem checkMenu = new CheckMenuItem(message("ShareAllInterface"));
            checkMenu.setSelected(UserConfig.getBoolean(baseName + "ShareHtmlStyle", true));
            checkMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "ShareHtmlStyle", checkMenu.isSelected());
                }
            });
            items.add(checkMenu);

            CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            hoverMenu.setSelected(UserConfig.getBoolean("HtmlStylesPopWhenMouseHovering", false));
            hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("HtmlStylesPopWhenMouseHovering", hoverMenu.isSelected());
                }
            });
            items.add(hoverMenu);

            controller.popEventMenu(event, items);
            return controller.getPopMenu();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ContextMenu popWindowStyles(BaseController parent, String baseStyle, Event event) {
        try {
            List<MenuItem> items = new ArrayList<>();

            String baseName = parent.getBaseName();
            MenuItem menu = new MenuItem(message("WindowStyle"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            items.add(menu);
            items.add(new SeparatorMenuItem());

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
                items.add(rmenu);
            }
            items.add(new SeparatorMenuItem());

            CheckMenuItem checkMenu = new CheckMenuItem(message("ShareAllInterface"));
            checkMenu.setSelected(UserConfig.getBoolean(baseName + "ShareWindowStyle", true));
            checkMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "ShareWindowStyle", checkMenu.isSelected());
                }
            });
            items.add(checkMenu);

            CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            hoverMenu.setSelected(UserConfig.getBoolean("WindowStylesPopWhenMouseHovering", false));
            hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("WindowStylesPopWhenMouseHovering", hoverMenu.isSelected());
                }
            });
            items.add(hoverMenu);

            parent.popEventMenu(event, items);
            return parent.getPopMenu();
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

    /*
        saved values
     */
    public static void popStringValues(BaseController parent, TextInputControl input, Event event,
            String valueName, boolean alwaysClear) {
        try {
            int max = UserConfig.getInt(valueName + "MaxSaved", 20);
            MenuController controller = MenuController.open(parent, input, event, valueName, alwaysClear);

            List<Node> setButtons = new ArrayList<>();
            Button clearInputButton = new Button(message("ClearInputArea"));
            clearInputButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    input.clear();
                }
            });
            setButtons.add(clearInputButton);

            Button clearValuesButton = new Button(message("ClearValues"));
            clearValuesButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent aevent) {
                    TableStringValues.clear(valueName);
                    controller.close();
                    popStringValues(parent, input, aevent, valueName, alwaysClear);
                }
            });
            setButtons.add(clearValuesButton);

            Button maxButton = new Button(message("MaxSaved"));
            maxButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent aevent) {
                    String value = PopTools.askValue(parent.getTitle(), null, message("MaxSaved"), max + "");
                    if (value == null) {
                        return;
                    }
                    try {
                        int v = Integer.parseInt(value);
                        UserConfig.setInt(valueName + "MaxSaved", v);
                    } catch (Exception e) {
                        MyBoxLog.error(e);
                    }
                }
            });
            setButtons.add(maxButton);

            controller.addFlowPane(setButtons);
            controller.addNode(new Separator());
            controller.addNode(new Label(message("PopValuesComments")));

            List<String> values = TableStringValues.max(valueName, max);
            List<Node> buttons = new ArrayList<>();
            for (String value : values) {
                Button button = new Button(StringTools.start(value, 100));
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent aevent) {
                        if (UserConfig.getBoolean(valueName + "ValuesClearAndSet", true)) {
                            input.setText(value);
                        } else {
                            input.replaceText(input.getSelection(), value);
                        }
                        if (UserConfig.getBoolean(valueName + "ValuesCloseAfterPaste", true)) {
                            controller.close();
                        } else {
                            controller.getThisPane().requestFocus();
                        }
                        input.requestFocus();
                        input.deselect();
                    }
                });
                button.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent aevent) {
                        if (aevent.getButton() == MouseButton.SECONDARY) {
                            TableStringValues.delete(valueName, value);
                            controller.close();
                            popStringValues(parent, input, event, valueName, alwaysClear);
                        }
                    }
                });
                buttons.add(button);
            }
            controller.addFlowPane(buttons);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public static void popStringValues(BaseController parent, TextInputControl input, Event event, String valueName) {
        popStringValues(parent, input, event, valueName, false);
    }

    public static void popValues(BaseController parent, TextInputControl input,
            String valueName, LinkedHashMap<String, String> values, Event event) {
        try {
            MenuController controller = MenuController.open(parent, input, event, valueName, false);

            List<Node> topButtons = new ArrayList<>();
            Button newLineButton = new Button();
            newLineButton.setGraphic(StyleTools.getIconImageView("iconTurnOver.png"));
            NodeStyleTools.setTooltip(newLineButton, new Tooltip(message("Newline")));
            newLineButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    input.replaceText(input.getSelection(), "\n");
                    input.requestFocus();
                }
            });
            topButtons.add(newLineButton);

            Button clearButton = new Button(message("ClearInputArea"));
            clearButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    input.clear();
                }
            });
            topButtons.add(clearButton);

            controller.addFlowPane(topButtons);
            controller.addNode(new Separator());

            List<Node> nodes = new ArrayList<>();
            for (String value : values.keySet()) {
                String msg = values.get(value);
                Button button = new Button(value + (msg != null && !msg.isBlank() ? "    " + msg : ""));
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        if (UserConfig.getBoolean(valueName + "ValuesClearAndSet", true)) {
                            input.setText(value);
                        } else {
                            input.replaceText(input.getSelection(), value);
                        }
                        if (UserConfig.getBoolean(valueName + "ValuesCloseAfterPaste", true)) {
                            controller.close();
                        } else {
                            controller.getThisPane().requestFocus();
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

    /*
        examples
     */
    public static ContextMenu popDatetimeExamples(BaseController parent, ContextMenu inPopMenu,
            TextField input, MouseEvent mouseEvent) {
        try {
            List<String> values = new ArrayList<>();
            Date d = new Date();
            values.add(DateTools.datetimeToString(d, TimeFormats.Datetime));
            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeMs));
            values.add(DateTools.datetimeToString(d, TimeFormats.Date));
            values.add(DateTools.datetimeToString(d, TimeFormats.Month));
            values.add(DateTools.datetimeToString(d, TimeFormats.Year));
            values.add(DateTools.datetimeToString(d, TimeFormats.TimeMs));
            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeZone));
            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeC));
            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeMsC));
            values.add(DateTools.datetimeToString(d, TimeFormats.DateC));
            values.add(DateTools.datetimeToString(d, TimeFormats.MonthC));
            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeZoneC));
            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeE));
            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeMsE));
            values.add(DateTools.datetimeToString(d, TimeFormats.DateE));
            values.add(DateTools.datetimeToString(d, TimeFormats.MonthE));
            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeZoneE));
            values.addAll(Arrays.asList(
                    "2020-07-15T36:55:09", "2020-07-10T10:10:10.532 +0800"
            ));
            return popDateMenu(parent, inPopMenu, input, mouseEvent, values);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ContextMenu popDateExamples(BaseController parent, ContextMenu inPopMenu,
            TextField input, MouseEvent mouseEvent) {
        try {
            List<String> values = new ArrayList<>();
            Date d = new Date();
            values.add(DateTools.datetimeToString(d, TimeFormats.Date));
            values.add(DateTools.datetimeToString(d, TimeFormats.Month));
            values.add(DateTools.datetimeToString(d, TimeFormats.Year));
            values.add(DateTools.datetimeToString(d, TimeFormats.DateC));
            values.add(DateTools.datetimeToString(d, TimeFormats.MonthC));
            values.add(DateTools.datetimeToString(d, TimeFormats.DateE));
            values.add(DateTools.datetimeToString(d, TimeFormats.MonthE));
            return popDateMenu(parent, inPopMenu, input, mouseEvent, values);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void popEraExamples(BaseController parent, TextField input, MouseEvent mouseEvent) {
        try {
            List<String> values = new ArrayList<>();
            Date d = new Date();
            values.add(DateTools.datetimeToString(d, TimeFormats.Datetime + " G", Locale.ENGLISH, null));
            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeMs + " G", Locale.ENGLISH, null));
            values.add(DateTools.datetimeToString(d, TimeFormats.Date + " G", Locale.ENGLISH, null));
            values.add(DateTools.datetimeToString(d, TimeFormats.Month + " G", Locale.ENGLISH, null));
            values.add(DateTools.datetimeToString(d, "G" + TimeFormats.DateA, Locale.ENGLISH, null));

            Date bc = DateTools.encodeDate("770-3-9 12:56:33.498 BC");
            values.add(DateTools.datetimeToString(bc, TimeFormats.DatetimeA + " G", Locale.ENGLISH, null));
            values.add(DateTools.datetimeToString(bc, TimeFormats.Date + " G", Locale.ENGLISH, null));
            values.add(DateTools.datetimeToString(bc, "G" + TimeFormats.MonthA, Locale.ENGLISH, null));
            values.add(DateTools.datetimeToString(bc, "G" + TimeFormats.YearA, Locale.ENGLISH, null));

            if (Languages.isChinese()) {
                values.add(DateTools.datetimeToString(d, TimeFormats.Datetime + " G", Locale.CHINESE, null));
                values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeMs + " G", Locale.CHINESE, null));
                values.add(DateTools.datetimeToString(d, TimeFormats.Date + " G", Locale.CHINESE, null));
                values.add(DateTools.datetimeToString(d, TimeFormats.Month + " G", Locale.CHINESE, null));
                values.add(DateTools.datetimeToString(d, "G" + TimeFormats.DateA, Locale.CHINESE, null));

                values.add(DateTools.datetimeToString(bc, TimeFormats.DatetimeA + " G", Locale.CHINESE, null));
                values.add(DateTools.datetimeToString(bc, TimeFormats.DateA + " G", Locale.CHINESE, null));
                values.add(DateTools.datetimeToString(bc, "G" + TimeFormats.MonthA, Locale.CHINESE, null));
                values.add(DateTools.datetimeToString(bc, "G" + TimeFormats.YearA, Locale.CHINESE, null));
            }

            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeMsC + " G", Locale.ENGLISH, null));
            values.add(DateTools.datetimeToString(d, TimeFormats.DateC + " G", Locale.ENGLISH, null));
            values.add(DateTools.datetimeToString(d, "G" + TimeFormats.MonthC, Locale.ENGLISH, null));

            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeMsB + " G", Locale.ENGLISH, null));
            values.add(DateTools.datetimeToString(d, TimeFormats.DateB + " G", Locale.ENGLISH, null));
            values.add(DateTools.datetimeToString(d, "G" + TimeFormats.DatetimeB, Locale.ENGLISH, null));
            values.add(DateTools.datetimeToString(d, "G" + TimeFormats.MonthB, Locale.ENGLISH, null));

            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeZone));
            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeZoneE));
            values.addAll(Arrays.asList(
                    "2020-07-15T36:55:09", "2020-07-10T10:10:10.532 +0800"
            ));

            MenuController controller = MenuController.open(parent, input, mouseEvent, "EraExamples", true);

            List<Node> nodes = new ArrayList<>();
            for (String value : values) {
                Button button = new Button(value);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        input.setText(value);
                        controller.close();
                    }
                });
                nodes.add(button);
            }
            controller.addFlowPane(nodes);

            Hyperlink link = new Hyperlink("DateFormat");
            link.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    parent.openLink(HelpTools.simpleDateFormatLink());
                }
            });
            controller.addNode(link);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static ContextMenu popDateMenu(BaseController parent, ContextMenu inPopMenu,
            TextField input, MouseEvent mouseEvent, List<String> values) {
        try {
            if (inPopMenu != null && inPopMenu.isShowing()) {
                inPopMenu.hide();
            }
            if (values == null || values.isEmpty()) {
                return inPopMenu;
            }
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu;
            for (String value : values) {
                menu = new MenuItem(value);
                menu.setOnAction((ActionEvent event) -> {
                    input.setText(value);
                    input.requestFocus();
                });
                items.add(menu);
            }
            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("DateFormat"));
            menu.setStyle("-fx-text-fill: blue;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    parent.openLink(HelpTools.simpleDateFormatLink());
                }
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            parent.popEventMenu(mouseEvent, items);
            return parent.getPopMenu();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void popRegexExamples(BaseController parent, TextInputControl input, Event event) {
        try {
            String valueName = "RegexExamples";
            MenuController controller = MenuController.open(parent, input, event, valueName, false);

            List<Node> topButtons = new ArrayList<>();
            Button clearButton = new Button(message("Clear"));
            NodeStyleTools.setTooltip(clearButton, new Tooltip(message("ClearInputArea")));
            clearButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    input.clear();
                    controller.getThisPane().requestFocus();
                    input.requestFocus();
                }
            });
            topButtons.add(clearButton);

            controller.addFlowPane(topButtons);
            controller.addNode(new Separator());

            List<String> values = Arrays.asList("^      " + message("StartLocation"),
                    "$      " + message("EndLocation"),
                    "*      " + message("ZeroOrNTimes"),
                    "+      " + message("OneOrNTimes"),
                    "?      " + message("ZeroOrOneTimes"),
                    "{n}      " + message("NTimes"),
                    "{n,}      " + message("N+Times"),
                    "{n,m}      " + message("NMTimes"),
                    "|      " + message("Or"),
                    "[abc]      " + message("MatchOneCharacters"),
                    "[A-Z]      " + message("A-Z"),
                    "\\x20      " + message("Blank"),
                    "\\s      " + message("NonprintableCharacter"),
                    "\\S      " + message("PrintableCharacter"),
                    "\\n      " + message("LineBreak"),
                    "\\r      " + message("CarriageReturn"),
                    "\\t      " + message("Tab"),
                    "[0-9]{n}      " + message("NNumber"),
                    "[A-Z]{n}      " + message("NUppercase"),
                    "[a-z]{n}      " + message("NLowercase"),
                    "[\\u4e00-\\u9fa5]      " + message("Chinese"),
                    "[^\\x00-\\xff]      " + message("DoubleByteCharacter"),
                    "[A-Za-z0-9]+      " + message("EnglishAndNumber"),
                    "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*      " + message("Email"),
                    "(13[0-9]|14[5|7]|15[0|1|2|3|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}      " + message("PhoneNumber"),
                    "[a-zA-z]+://[^\\s]*       " + message("URL"),
                    "^(\\s*)\\n       " + message("BlankLine"),
                    "\\d+\\.\\d+\\.\\d+\\.\\d+      " + message("IP"),
                    "line1\\s*\\nline2      " + message("MultipleLines"));
            List<Node> nodes = new ArrayList<>();
            for (String value : values) {
                String[] vv = value.split("      ");
                Button button = new Button(vv[1].trim());
                String v = vv[0];
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        if (UserConfig.getBoolean(valueName + "ValuesClearAndSet", true)) {
                            input.setText(v);
                        } else {
                            input.replaceText(input.getSelection(), v);
                        }
                        if (UserConfig.getBoolean(valueName + "ValuesCloseAfterPaste", true)) {
                            controller.close();
                        } else {
                            controller.getThisPane().requestFocus();
                        }
                        input.requestFocus();
                    }
                });
                NodeStyleTools.setTooltip(button, new Tooltip(v));
                nodes.add(button);
            }
            controller.addFlowPane(nodes);

            Hyperlink link = new Hyperlink(message("AboutRegularExpression"));
            link.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (Languages.isChinese()) {
                        parent.openLink(HelpTools.expZhLink());
                    } else {
                        parent.openLink(HelpTools.expEnLink());
                    }
                }
            });
            controller.addNode(link);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void popColorExamples(BaseController parent, TextInputControl input, Event event) {
        try {
            String valueName = "ColorExamples";
            MenuController controller = MenuController.open(parent, input, event, valueName, true);

            List<Node> topButtons = new ArrayList<>();

            Button clearButton = new Button(message("ClearInputArea"));
            clearButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    input.clear();
                }
            });
            topButtons.add(clearButton);

            controller.addFlowPane(topButtons);
            controller.addNode(new Separator());

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
                        if (UserConfig.getBoolean(valueName + "ValuesCloseAfterPaste", true)) {
                            controller.close();
                        } else {
                            controller.getThisPane().requestFocus();
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

    public static void popSqlExamples(BaseController parent, TextInputControl input,
            String tableName, boolean onlyQuery, Event event) {
        try {
            String valueName = "SqlExamples";
            MenuController controller = MenuController.open(parent, input, event, valueName, false);

            boolean isTextArea = input instanceof TextArea;

            List<Node> topButtons = new ArrayList<>();
            if (isTextArea) {
                Button newLineButton = new Button();
                newLineButton.setGraphic(StyleTools.getIconImageView("iconTurnOver.png"));
                NodeStyleTools.setTooltip(newLineButton, new Tooltip(message("Newline")));
                newLineButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        input.replaceText(input.getSelection(), "\n");
                        controller.getThisPane().requestFocus();
                        input.requestFocus();
                    }
                });
                topButtons.add(newLineButton);
            }

            Button clearButton = new Button();
            clearButton.setGraphic(StyleTools.getIconImageView("iconClear.png"));
            NodeStyleTools.setTooltip(clearButton, new Tooltip(message("ClearInputArea")));
            clearButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    input.clear();
                    controller.getThisPane().requestFocus();
                    input.requestFocus();
                }
            });
            topButtons.add(clearButton);

            controller.addFlowPane(topButtons);
            controller.addNode(new Separator());

            String tname = tableName == null ? "<table>" : tableName;
            addButtonsPane(controller, input, Arrays.asList(
                    "SELECT * FROM " + tname,
                    " WHERE ", " ORDER BY ", " DESC ", " ASC ",
                    " FETCH FIRST ROW ONLY", " FETCH FIRST <size> ROWS ONLY",
                    " OFFSET <start> ROWS FETCH NEXT <size> ROWS ONLY"
            ));
            addButtonsPane(controller, input, Arrays.asList(
                    " , ", " (   ) ", " = ", " '' ", " >= ", " > ", " <= ", " < ", " != "
            ));
            addButtonsPane(controller, input, Arrays.asList(
                    " AND ", " OR ", " NOT ", " IS NULL ", " IS NOT NULL "
            ));
            addButtonsPane(controller, input, Arrays.asList(
                    " LIKE 'a%' ", " LIKE 'a_' ", " BETWEEN <value1> AND <value2>"
            ));
            addButtonsPane(controller, input, Arrays.asList(
                    " IN ( <value1>, <value2> ) ", " IN (SELECT FROM " + tname + " WHERE <condition>) "
            ));
            addButtonsPane(controller, input, Arrays.asList(
                    " EXISTS (SELECT FROM " + tname + " WHERE <condition>) "
            ));
            addButtonsPane(controller, input, Arrays.asList(
                    " DATE('1998-02-26') ", " TIMESTAMP('1962-09-23 03:23:34.234') "
            ));
            addButtonsPane(controller, input, Arrays.asList(
                    " COUNT(*) ", " AVG() ", " MAX() ", " MIN() ", " SUM() ", " GROUP BY ", " HAVING "
            ));
            addButtonsPane(controller, input, Arrays.asList(
                    " JOIN ", " INNER JOIN ", " LEFT OUTER JOIN ", " RIGHT OUTER JOIN ", " CROSS JOIN "
            ));
            if (!onlyQuery) {
                addButtonsPane(controller, input, Arrays.asList(
                        "INSERT INTO " + tname + " (column1, column2) VALUES (value1, value2)",
                        "UPDATE " + tname + " SET <column1>=<value1>, <column2>=<value2> WHERE <condition>",
                        "DELETE FROM " + tname + " WHERE <condition>", "TRUNCATE TABLE <table>",
                        "ALTER TABLE " + tname + " ALTER COLUMN id RESTART WITH 100"
                ));
            }

            Hyperlink link = new Hyperlink("Derby Reference Manual");
            link.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    parent.openLink(HelpTools.derbyLink());
                }
            });
            controller.addNode(link);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void popJShellSuggesions(BaseController parent, JShell jShell, TextInputControl scriptInput) {
        try {
            List<SourceCodeAnalysis.Suggestion> suggestions = jShell.sourceCodeAnalysis().completionSuggestions(
                    scriptInput.getText(), scriptInput.getCaretPosition(), new int[1]);
            if (suggestions == null || suggestions.isEmpty()) {
                return;
            }
            ContextMenu ePopMenu = parent.getPopMenu();
            if (ePopMenu != null && ePopMenu.isShowing()) {
                ePopMenu.hide();
            }
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu;

            for (SourceCodeAnalysis.Suggestion suggestion : suggestions) {
                String c = suggestion.continuation();
                menu = new MenuItem(StringTools.menuPrefix(c));
                menu.setOnAction((ActionEvent event) -> {
                    scriptInput.replaceText(scriptInput.getSelection(), c);
                });
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());
            parent.popNodeMenu(scriptInput, items);
        } catch (Exception e) {
        }
    }

    public static MenuController popJavaScriptExamples(BaseController parent, Event event,
            TextInputControl scriptInput, String valueName, List<List<String>> preValues) {
        try {

            MenuController controller = MenuController.open(parent, scriptInput, event, valueName, false);

            controller.setTitleLabel(message("Examples"));

            List<Node> topButtons = new ArrayList<>();
            Button newLineButton = new Button();
            newLineButton.setGraphic(StyleTools.getIconImageView("iconTurnOver.png"));
            NodeStyleTools.setTooltip(newLineButton, new Tooltip(message("Newline")));
            newLineButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    scriptInput.replaceText(scriptInput.getSelection(), "\n");
                    scriptInput.requestFocus();
                }
            });
            topButtons.add(newLineButton);

            Button clearInputButton = new Button();
            clearInputButton.setGraphic(StyleTools.getIconImageView("iconClear.png"));
            NodeStyleTools.setTooltip(clearInputButton, new Tooltip(message("ClearInputArea")));
            clearInputButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    scriptInput.clear();
                }
            });
            topButtons.add(clearInputButton);

            controller.addFlowPane(topButtons);
            controller.addNode(new Separator());

            List<List<String>> pvalues;
            if (preValues == null || preValues.isEmpty()) {
                pvalues = javaScriptExamples("numberV", "stringV", "dateV");
            } else {
                pvalues = preValues;
            }
            for (List<String> values : pvalues) {
                PopTools.addButtonsPane(controller, scriptInput, values);
            }

            PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                    " + ", " - ", " * ", " / ", " % ",
                    "''", "( )", ";", " = ", " += ", " -= ", " *= ", " /= ", " %= ",
                    "++ ", "-- ", " , ", " { } ", "[ ]", "\" \"", ".",
                    " var ", " this"
            ));

            PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                    " >= ", " > ", " <= ", " < ", " != ", " && ", " || ", " !",
                    " == ", " === ", " !== ",
                    " true ", " false ", " null ", " undefined "
            ));

            return controller;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static List<List<String>> javaScriptExamples(String numColumn,
            String stringColumn, String dateColumn) {
        List<List<String>> preValues = new ArrayList<>();
        List<String> values = new ArrayList<>();

        if (numColumn != null) {
            values.addAll(Arrays.asList(
                    numColumn + " == 0", numColumn + " >= 0", numColumn + " < 0",
                    "Math.abs(" + numColumn + ")",
                    "Math.trunc(" + numColumn + ")", "Math.round(" + numColumn + ")",
                    "Math.ceil(" + numColumn + ")", "Math.floor(" + numColumn + ")",
                    "Math.pow(" + numColumn + ", 2)", "Math.sqrt(" + numColumn + ")",
                    "Math.pow(" + numColumn + ", 1d/3)",
                    "Math.exp(" + numColumn + ")", "Math.log(" + numColumn + ")",
                    "Math.min(" + numColumn + ",2,-3)", "Math.max(" + numColumn + ",2,-3)",
                    "Math.sin(" + numColumn + ")", "Math.cos(" + numColumn + ")", "Math.tan(" + numColumn + ")"
            ));
        }
        values.addAll(Arrays.asList(
                "Math.PI", "Math.E", "Math.random()"
        ));
        preValues.add(values);

        if (stringColumn != null) {
            values = Arrays.asList(
                    stringColumn + " == null || " + stringColumn + " == ''",
                    stringColumn + " == 'Hello'",
                    "if ( " + stringColumn + " != null ) \n"
                    + "    " + stringColumn + ".length\n"
                    + "else\n"
                    + "    -1",
                    "if ( " + stringColumn + " != null ) \n"
                    + "    " + stringColumn + ".indexOf('Hello')\n"
                    + "else\n"
                    + "    -1",
                    "if ( " + stringColumn + " != null ) \n"
                    + "    " + stringColumn + ".search(/Hello/ig)\n"
                    + "else\n"
                    + "    -1",
                    "if ( " + stringColumn + " != null ) \n"
                    + "    " + stringColumn + ".startsWith('Hello')\n"
                    + "else\n"
                    + "    undefined",
                    "if ( " + stringColumn + " != null ) \n"
                    + "    " + stringColumn + ".endsWith('Hello')\n"
                    + "else\n"
                    + "    undefined",
                    "if ( " + stringColumn + " != null ) \n"
                    + "    " + stringColumn + ".replace(/h/g, \"H\")\n"
                    + "else\n"
                    + "    null",
                    "if ( " + stringColumn + " != null ) \n"
                    + "    " + stringColumn + ".toLowerCase()\n"
                    + "else\n"
                    + "    null",
                    "if ( " + stringColumn + " != null ) \n"
                    + "    " + stringColumn + ".toUpperCase()\n"
                    + "else\n"
                    + "    null"
            );
            preValues.add(values);
        }

        if (dateColumn != null) {
            values = Arrays.asList(
                    dateColumn + " == '2016-05-19 11:34:28'",
                    "if ( " + dateColumn + " != null ) \n"
                    + "    " + dateColumn + ".startsWith('2016-05-19 11')\n"
                    + "else\n"
                    + "    undefined",
                    "if ( " + dateColumn + " != null ) \n"
                    + "    " + "new Date(" + dateColumn + ").getTime()  > new Date('2016/05/19 09:23:12').getTime()\n"
                    + "else\n"
                    + "    undefined",
                    "function formatDate(date) {\n"
                    + "     var y = date.getFullYear();\n"
                    + "     var m = date.getMonth() + 1;\n"
                    + "     m = m < 10 ? ('0' + m) : m;\n"
                    + "     var d = date.getDate();\n"
                    + "     d = d < 10 ? ('0' + d) : d;\n"
                    + "     var h =date.getHours();\n"
                    + "     h = h < 10 ? ('0' + h) : h;\n"
                    + "     var M =date.getMinutes();\n"
                    + "     M = M < 10 ? ('0' + M) : M;\n"
                    + "     var s =date.getSeconds();\n"
                    + "     s = s < 10 ? ('0' + s) : s;\n"
                    + "     return y + '-' + m + '-' + d + ' ' + h + ':' + M + ':' + s;\n"
                    + "}\n"
                    + "if (" + dateColumn + " != null)\n"
                    + "   formatDate(new Date(" + dateColumn + "));\n"
                    + "else\n"
                    + "   null;"
            );
            preValues.add(values);
        }

        return preValues;
    }

    public static MenuController popRowExpressionExamples(BaseController parent, Event event,
            TextInputControl scriptInput, String valueName, Data2D data2D) {
        try {
            if (data2D == null) {
                return popJavaScriptExamples(parent, event, scriptInput, valueName, null);
            }
            List<List<String>> preValues = new ArrayList<>();

            List<String> values = Arrays.asList(
                    "#{" + message("DataRowNumber") + "} % 2 == 0",
                    "#{" + message("DataRowNumber") + "} % 2 == 1",
                    "#{" + message("DataRowNumber") + "} >= 9",
                    "#{" + message("TableRowNumber") + "} % 2 == 0",
                    "#{" + message("TableRowNumber") + "} % 2 == 1",
                    "#{" + message("TableRowNumber") + "} == 1"
            );
            preValues.add(values);

            String stringColumn = null, dateColumn = null, numColumn = null, cname;
            for (Data2DColumn c : data2D.getColumns()) {
                cname = "#{" + c.getColumnName() + "}";
                switch (c.getType()) {
                    case String:
                    case Enumeration:
                    case EnumerationEditable:
                    case File:
                    case Image:
                    case Color:
                        if (stringColumn == null) {
                            stringColumn = cname;
                        }
                        break;
                    case Double:
                    case Longitude:
                    case Latitude:
                    case Float:
                    case Long:
                    case Integer:
                    case Short:
                        if (numColumn == null) {
                            numColumn = cname;
                        }
                        break;
                    case Datetime:
                    case Date:
                    case Era:
                        if (dateColumn == null) {
                            dateColumn = cname;
                        }
                        break;
                }
            }

            preValues.addAll(javaScriptExamples(numColumn, stringColumn, dateColumn));

            return popJavaScriptExamples(parent, event, scriptInput, valueName, preValues);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static List<String> htmlTags() {
        return Arrays.asList(
                "p", "img", "a", "div", "li", "ul", "ol", "h1", "h2", "h3", "h4",
                "button", "input", "label", "form", "table", "tr", "th", "td",
                "font", "span", "b", "hr", "br", "frame", "pre",
                "meta", "script", "style"
        );
    }

    public static void popHtmlTagExamples(BaseController parent, TextInputControl input, Event event) {
        try {
            String valueName = "HtmlTagExamples";
            MenuController controller = MenuController.open(parent, input, event, valueName, true);

            Button clearButton = new Button(message("ClearInputArea"));
            clearButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    input.clear();
                    controller.getThisPane().requestFocus();
                    input.requestFocus();
                }
            });
            controller.addNode(clearButton);

            List< Node> nodes = new ArrayList<>();
            for (String value : htmlTags()) {
                Button button = new Button(value);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        input.setText(value);
                        if (UserConfig.getBoolean(valueName + "ValuesCloseAfterPaste", true)) {
                            controller.close();
                        } else {
                            controller.getThisPane().requestFocus();
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

}
