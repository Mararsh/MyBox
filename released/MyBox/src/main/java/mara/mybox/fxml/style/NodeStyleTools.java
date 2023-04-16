package mara.mybox.fxml.style;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.util.Duration;
import mara.mybox.controller.BaseController;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2021-8-5
 * @License Apache License Version 2.0
 */
public class NodeStyleTools {

    public static String errorDataStyle() {
        return "-fx-background-color: #FFF0F5;";
    }

    public static String blueTextStyle() {
        return "-fx-text-fill: #2e598a;";
    }

    public static String redTextStyle() {
        return "-fx-text-fill: #961c1c;";
    }

    public static String darkRedTextStyle() {
        return "-fx-text-fill: #961c1c;  -fx-font-weight: bolder;";
    }

    public static String darkBlueTextStyle() {
        return "-fx-text-fill: #2e598a;  -fx-font-weight: bolder;";
    }

    public static String selectedDataStyle() {
        return "-fx-background-color:  #0096C9; -fx-text-background-color: white;";
    }

    public static String selectedRowStyle() {
        return "-fx-background-color:  lightgray; -fx-text-fill: -fx-selection-bar-text;";
    }

    public static void applyStyle(Node node) {
        try {
            if (node == null) {
                return;
            }
            // Base styles are width in first
            StyleTools.setStyle(node);
            if (node instanceof SplitPane) {
                for (Node child : ((SplitPane) node).getItems()) {
                    applyStyle(child);
                }
            } else if (node instanceof ScrollPane) {
                applyStyle(((ScrollPane) node).getContent());
            } else if (node instanceof TitledPane) {
                applyStyle(((TitledPane) node).getContent());
            } else if (node instanceof TabPane) {
                for (Tab tab : ((TabPane) node).getTabs()) {
                    applyStyle(tab.getContent());
                }
            } else if (node instanceof Parent) {
                for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                    applyStyle(child);
                }
            }
            // Special styles are deep in first
            Object o = node.getUserData();
            if (o != null && o instanceof BaseController) {
                BaseController c = (BaseController) o;
                c.setControlsStyle();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void refreshStyle(Parent node) {
        try {
            if (node == null) {
                return;
            }
            applyStyle(node);
            node.applyCss();
            node.layout();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static boolean setStyle(Pane pane, String nodeId, String style) {
        try {
            Node node = pane.lookup("#" + nodeId);
            return setStyle(node, style);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean setStyle(Node node, String style) {
        try {
            if (node == null) {
                return false;
            }
            if (node instanceof ComboBox) {
                ComboBox c = (ComboBox) node;
                c.getEditor().setStyle(style);
            } else {
                node.setStyle(style);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void setTooltip(final Node node, Node tip) {
        if (node instanceof Control) {
            removeTooltip((Control) node);
        }
        Tooltip tooltip = new Tooltip();
        tooltip.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        tooltip.setGraphic(tip);
        tooltip.setShowDelay(Duration.millis(10));
        tooltip.setShowDuration(Duration.millis(360000));
        tooltip.setHideDelay(Duration.millis(10));
        Tooltip.install(node, tooltip);
    }

    public static void setTooltip(final Node node, String tips) {
        setTooltip(node, new Tooltip(tips));
    }

    public static void setTooltip(final Node node, final Tooltip tooltip) {
        if (node instanceof Control) {
            removeTooltip((Control) node);
        }
        tooltip.setFont(new Font(AppVariables.sceneFontSize));
        tooltip.setShowDelay(Duration.millis(10));
        tooltip.setShowDuration(Duration.millis(360000));
        tooltip.setHideDelay(Duration.millis(10));
        Tooltip.install(node, tooltip);
    }

    public static void removeTooltip(final Control node) {
        Tooltip.uninstall(node, node.getTooltip());
    }

    public static void removeTooltip(final Node node, final Tooltip tooltip) {
        Tooltip.uninstall(node, tooltip);
    }

}
