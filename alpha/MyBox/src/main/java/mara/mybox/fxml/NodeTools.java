package mara.mybox.fxml;

import java.awt.Toolkit;
import java.net.URL;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import mara.mybox.controller.BaseController;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 11:19:42
 * @License Apache License Version 2.0
 */
public class NodeTools {

    public static String getFxmlName(URL url) {
        if (url == null) {
            return null;
        }
        try {
            String fullPath = url.getPath();
            if (!fullPath.endsWith(".fxml")) {
                return null;
            }
            String fname;
            int pos = fullPath.lastIndexOf('/');
            if (pos < 0) {
                fname = fullPath;
            } else {
                fname = fullPath.substring(pos + 1);
            }
            return fname.substring(0, fname.length() - 5);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getFxmlFile(URL url) {
        return "/fxml/" + getFxmlName(url) + ".fxml";
    }

    public static Node findNode(Pane pane, String nodeId) {
        try {
            Node node = pane.lookup("#" + nodeId);
            return node;
        } catch (Exception e) {
            return null;
        }
    }

    public static Window getWindow(Node node) {
        try {
            return node.getScene().getWindow();
        } catch (Exception e) {
            return null;
        }
    }

    public static BaseController getController(Node node) {
        try {
            Window window = getWindow(node);
            return (BaseController) (window.getUserData());
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean setRadioFirstSelected(ToggleGroup group) {
        if (group == null) {
            return false;
        }
        ObservableList<Toggle> buttons = group.getToggles();
        for (Toggle button : buttons) {
            RadioButton radioButton = (RadioButton) button;
            radioButton.setSelected(true);
            return true;
        }
        return false;
    }

    public static boolean setRadioSelected(ToggleGroup group, String text) {
        if (group == null || text == null) {
            return false;
        }
        ObservableList<Toggle> buttons = group.getToggles();
        for (Toggle button : buttons) {
            RadioButton radioButton = (RadioButton) button;
            if (text.equals(radioButton.getText())) {
                button.setSelected(true);
                return true;
            }
        }
        return false;
    }

    public static boolean setItemSelected(ComboBox<String> box, String text) {
        if (box == null || text == null) {
            return false;
        }
        ObservableList<String> items = box.getItems();
        for (String item : items) {
            if (text.equals(item)) {
                box.getSelectionModel().select(item);
                return true;
            }
        }
        return false;
    }

    public static int getInputInt(TextField input) {
        try {
            return Integer.parseInt(input.getText());
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "InvalidData");
        }
    }

    public static Object textInputFocus(Scene scene) {
        if (scene == null) {
            return null;
        }
        return isTextInput(scene.getFocusOwner());
    }

    public static Object isTextInput(Object node) {
        if (node == null) {
            return null;
        }
        if (node instanceof TextInputControl) {
            return node;
        }
        if (node instanceof ComboBox) {
            ComboBox cb = (ComboBox) node;
            return cb.isEditable() ? node : null;
        }
        if (node instanceof WebView) {
            return WebViewTools.editor((WebView) node);
        }
        return null;
    }

    public static Rectangle2D getScreen() {
        return Screen.getPrimary().getVisualBounds();
    }

    // https://stackoverflow.com/questions/38599588/javafx-stage-setmaximized-only-works-once-on-mac-osx-10-9-5
    public static void setMaximized(Stage stage, boolean max) {
        stage.setMaximized(max);
        if (max) {
            Rectangle2D primaryScreenBounds = getScreen();
            stage.setX(primaryScreenBounds.getMinX());
            stage.setY(primaryScreenBounds.getMinY());
            stage.setWidth(primaryScreenBounds.getWidth());
            stage.setHeight(primaryScreenBounds.getHeight());
        }
    }

    public static double getX(Node node) {
        return node.getScene().getWindow().getX() + node.getScene().getX()
                + node.localToScene(0, 0).getX();
    }

    public static double getY(Node node) {
        return node.getScene().getWindow().getY() + node.getScene().getY()
                + node.localToScene(0, 0).getY();
    }

    public static double getWidth(Control control) {
        return control.getBoundsInParent().getWidth();
    }

    public static double getHeight(Control control) {
        return control.getBoundsInParent().getHeight();
    }

    public static void setScrollPane(ScrollPane scrollPane, double xOffset, double yOffset) {
        final Bounds visibleBounds = scrollPane.getViewportBounds();
        double scrollWidth = scrollPane.getContent().getBoundsInParent().getWidth() - visibleBounds.getWidth();
        double scrollHeight = scrollPane.getContent().getBoundsInParent().getHeight() - visibleBounds.getHeight();

        scrollPane.setHvalue(scrollPane.getHvalue() + xOffset / scrollWidth);
        scrollPane.setVvalue(scrollPane.getVvalue() + yOffset / scrollHeight);
    }

    // https://stackoverflow.com/questions/11552176/generating-a-mouseevent-in-javafx/11567122?r=SearchResults#11567122
    public static void fireMouseClicked(Node node) {
        try {
            Event.fireEvent(node, new MouseEvent(MouseEvent.MOUSE_CLICKED,
                    node.getLayoutX(), node.getLayoutY(), node.getLayoutX(), node.getLayoutY(),
                    MouseButton.PRIMARY, 1,
                    true, true, true, true, true, true, true, true, true, true, null));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    // Not work~
    public static void fireMouseRightClicked(Node node) {
        try {
            MyBoxLog.console(node.getScene().getWindow().getX() + " " + node.getScene().getWindow().getY());
            MyBoxLog.console(node.getScene().getX() + " " + node.getScene().getY());
            MyBoxLog.console(getX(node) + " " + getY(node));
            node.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED,
                    node.getScene().getX() + 20, node.getScene().getY() + 20, getX(node) + 20, getY(node) + 20,
                    MouseButton.SECONDARY, 1,
                    false, false, false, false, false, false, true, false, false, false, null));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public static double dpiScale() {
        try {
            double scale = Toolkit.getDefaultToolkit().getScreenResolution() / Screen.getPrimary().getDpi();
            return scale > 1 ? scale : 1;
        } catch (Exception e) {
            return 1;
        }
    }

    public static Image snap(Node node) {
        try {
            final Bounds bounds = node.getLayoutBounds();
            double scale = dpiScale();
            int imageWidth = (int) Math.round(bounds.getWidth() * scale);
            int imageHeight = (int) Math.round(bounds.getHeight() * scale);
            final SnapshotParameters snapPara = new SnapshotParameters();
            snapPara.setFill(Color.TRANSPARENT);
            snapPara.setTransform(javafx.scene.transform.Transform.scale(scale, scale));
            WritableImage snapshot = new WritableImage(imageWidth, imageHeight);
            snapshot = node.snapshot(snapPara, snapshot);
            return snapshot;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }

    }

}
