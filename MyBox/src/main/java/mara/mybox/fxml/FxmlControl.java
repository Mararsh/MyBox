package mara.mybox.fxml;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import javafx.stage.Popup;
import javafx.stage.PopupWindow;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import mara.mybox.tools.FileTools;
import static mara.mybox.tools.FileTools.getFileSuffix;
import mara.mybox.tools.SoundTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.MyboxDataPath;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 11:19:42
 * @Description
 * @License Apache License Version 2.0
 */
public class FxmlControl {

    public static String blueText = "-fx-text-fill: #2e598a;";
    public static String darkBlueText = "-fx-text-fill: #2e598a;  -fx-font-weight: bolder;";
    public static String redText = "-fx-text-fill: #961c1c;";
    public static String darkRedText = "-fx-text-fill: #961c1c;  -fx-font-weight: bolder;";
    public static String badStyle = "-fx-text-box-border: red;   -fx-text-fill: red;";
    public static String warnStyle = "-fx-text-box-border: orange;   -fx-text-fill: orange;";
    public static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    public static void miao3() {
        playSound("/sound/miao3.mp3", "miao3.mp3");
    }

    public static void miao8() {
        playSound("/sound/miao8.mp3", "miao8.mp3");
    }

    public static Node findNode(Pane pane, String nodeId) {
        try {
            Node node = pane.lookup("#" + nodeId);
            return node;
        } catch (Exception e) {
            return null;
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

    public static void refreshStyle(Parent node) {
        node.applyCss();
        node.layout();
        applyStyle(node);
    }

    public static void applyStyle(Node node) {
        if (node == null) {
            return;
        }
        ControlStyle.setStyle(node);
        if (node instanceof Parent) {
            for (Node c : ((Parent) node).getChildrenUnmodifiable()) {
                applyStyle(c);
            }
        }
    }

    public static void playSound(final String file, final String userFile) {

        Task miaoTask = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    File miao = FxmlControl.getInternalFile(file, "sound", userFile);
                    FloatControl control = SoundTools.getControl(miao);
                    Clip player = SoundTools.playback(miao, control.getMaximum() * 0.6f);
                    player.start();
                } catch (Exception e) {
                }
                return null;
            }
        };
        Thread thread = new Thread(miaoTask);
        thread.setDaemon(true);
        thread.start();

    }

    public static void setScrollPane(ScrollPane scrollPane, double xOffset, double yOffset) {
        final Bounds visibleBounds = scrollPane.getViewportBounds();
        double scrollWidth = scrollPane.getContent().getBoundsInParent().getWidth() - visibleBounds.getWidth();
        double scrollHeight = scrollPane.getContent().getBoundsInParent().getHeight() - visibleBounds.getHeight();

        scrollPane.setHvalue(scrollPane.getHvalue() + xOffset / scrollWidth);
        scrollPane.setVvalue(scrollPane.getVvalue() + yOffset / scrollHeight);
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

    public static String getFxmlName(String fullPath) {
        if (fullPath == null) {
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
    }

    public static int getInputInt(TextField input) {
        try {
            return Integer.parseInt(input.getText());
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "InvalidData");
        }
    }

    public static void setNonnegativeValidation(final TextField input) {
        setNonnegativeValidation(input, Integer.MAX_VALUE);
    }

    public static void setNonnegativeValidation(final TextField input, final int max) {
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    int v = Integer.parseInt(newValue);
                    if (v >= 0 && v <= max) {
                        input.setStyle(null);
                    } else {
                        input.setStyle(badStyle);
                    }
                } catch (Exception e) {
                    input.setStyle(badStyle);
                }
            }
        });
    }

    public static void setPositiveValidation(final TextField input) {
        setPositiveValidation(input, Integer.MAX_VALUE);
    }

    public static void setPositiveValidation(final TextField input, final int max) {
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    int v = Integer.parseInt(newValue);
                    if (v > 0 && v <= max) {
                        input.setStyle(null);
                    } else {
                        input.setStyle(badStyle);
                    }
                } catch (Exception e) {
                    input.setStyle(badStyle);
                }
            }
        });
    }

    public static int positiveValue(final TextField input) {
        return positiveValue(input, Integer.MAX_VALUE);
    }

    public static int positiveValue(final TextField input, final int max) {
        try {
            int v = Integer.parseInt(input.getText());
            if (v > 0 && v <= max) {
                input.setStyle(null);
                return v;
            } else {
                input.setStyle(badStyle);
                return -1;
            }
        } catch (Exception e) {
            input.setStyle(badStyle);
            return -1;
        }
    }

    public static void setFloatValidation(final TextField input) {
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    float v = Float.valueOf(newValue);
                    input.setStyle(null);
                } catch (Exception e) {
                    input.setStyle(badStyle);
                }
            }
        });
    }

    public static void setFileValidation(final TextField input) {
        if (input == null) {
            return;
        }
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                final File file = new File(newValue);
                if (!file.exists() || !file.isFile()) {
                    input.setStyle(badStyle);
                    return;
                }
                input.setStyle(null);
            }
        });
    }

    public static void setPathValidation(final TextField input) {
        if (input == null) {
            return;
        }
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                final File file = new File(newValue);
                if (!file.isDirectory()) {
                    input.setStyle(badStyle);
                    return;
                }
                input.setStyle(null);
            }
        });
    }

    public static void setPathExistedValidation(final TextField input) {
        if (input == null) {
            return;
        }
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                final File file = new File(newValue);
                if (!file.exists() || !file.isDirectory()) {
                    input.setStyle(badStyle);
                    return;
                }
                input.setStyle(null);
            }
        });
    }

    public static File getInternalFile(String resourceFile, String subPath, String userFile) {
        return getInternalFile(resourceFile, subPath, userFile, false);
    }

    // Solution from https://stackoverflow.com/questions/941754/how-to-get-a-path-to-a-resource-in-a-java-jar-file
    public static File getInternalFile(String resourceFile, String subPath, String userFile,
            boolean deleteExisted) {
        if (resourceFile == null || userFile == null) {
            return null;
        }
        try {
            File path = new File(MyboxDataPath + File.separator + subPath);
            if (!path.exists()) {
                path.mkdirs();
            }
            File file = new File(MyboxDataPath + File.separator + subPath + File.separator + userFile);
            if (file.exists() && !deleteExisted) {
                return file;
            }
            File tmpFile = getInternalFile(resourceFile);
            if (tmpFile == null) {
                return null;
            }
            if (file.exists()) {
                file.delete();
            }
            FileTools.copyFile(tmpFile, file);
            return file;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static File getInternalFile(String resourceFile) {
        if (resourceFile == null) {
            return null;
        }
        try {
            InputStream input = FxmlControl.class.getResourceAsStream(resourceFile);
            File file = File.createTempFile("MyBox", "." + getFileSuffix(resourceFile));
            OutputStream out = new FileOutputStream(file);
            int read;
            byte[] bytes = new byte[1024];
            while ((read = input.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            file.deleteOnExit();
            return file;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static void setEditorStyle(final ComboBox box, final String style) {
        box.getEditor().setStyle(style);
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                box.getEditor().setStyle(style);
//            }
//        });
    }

    public static void setEditorBadStyle(final ComboBox box) {
        setEditorStyle(box, badStyle);
    }

    public static void setEditorWarnStyle(final ComboBox box) {
        setEditorStyle(box, warnStyle);
    }

    public static void setEditorNormal(final ComboBox box) {
        setEditorStyle(box, null);
    }

    public static double getX(Control control) {
        return control.getScene().getWindow().getX() + control.getScene().getX()
                + control.localToScene(0, 0).getX();
    }

    public static double getY(Control control) {
        return control.getScene().getWindow().getY() + control.getScene().getY()
                + control.localToScene(0, 0).getY();
    }

    public static double getWidth(Control control) {
        return control.getBoundsInParent().getWidth();
    }

    public static double getHeight(Control control) {
        return control.getBoundsInParent().getHeight();
    }

    public static void moveXCenter(Region pNnode, Node node) {
        try {
            if (node == null || pNnode == null) {
                return;
            }
            double xOffset = pNnode.getWidth() - node.getBoundsInLocal().getWidth();

            if (xOffset > 0) {
//                logger.debug(pNnode.getWidth() + " " + node.getBoundsInLocal().getWidth());
                node.setLayoutX(pNnode.getLayoutX() + xOffset / 2);
//                logger.debug(pNnode.getLayoutX() + " " + xOffset / 2 + " " + node.getLayoutX());
            } else {
                node.setLayoutX(0);
            }
        } catch (Exception e) {
        }
    }

    public static void moveYCenter(Region pNnode, Node node) {
        if (node == null || pNnode == null) {
            return;
        }
        double yOffset = pNnode.getHeight() - node.getBoundsInLocal().getHeight();
        if (yOffset > 0) {
            node.setLayoutY(pNnode.getLayoutY() + yOffset / 2);
        }
    }

    public static void moveCenter(Region pNnode, Node node) {
        moveXCenter(pNnode, node);
        moveYCenter(pNnode, node);
    }

    public static void moveXCenter(Node node) {
        if (node == null || node.getParent() == null) {
            return;
        }
        double xOffset = node.getParent().getBoundsInLocal().getWidth() - node.getBoundsInLocal().getWidth();
        if (xOffset > 0) {
            node.setLayoutX(xOffset / 2);
        } else {
            node.setLayoutX(0);
        }
    }

    public static void moveYCenter(Node node) {
        if (node == null || node.getParent() == null) {
            return;
        }
        double yOffset = node.getParent().getBoundsInLocal().getHeight() - node.getBoundsInLocal().getHeight();
        if (yOffset > 0) {
            node.setLayoutY(yOffset / 2);
        }
    }

    public static void moveCenter(Node node) {
        moveXCenter(node);
        moveYCenter(node);
    }

    public static void paneSize(ScrollPane sPane, ImageView iView) {
        try {
            if (iView == null || iView.getImage() == null
                    || sPane == null) {
                return;
            }
            iView.setFitWidth(sPane.getWidth() - 20);
            iView.setFitHeight(sPane.getHeight() - 20);
            FxmlControl.moveXCenter(sPane, iView);
            iView.setLayoutY(10);
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public static void imageSize(ScrollPane sPane, ImageView iView) {
        try {
            if (iView == null || iView.getImage() == null
                    || sPane == null) {
                return;
            }
            iView.setFitWidth(iView.getImage().getWidth());
            iView.setFitHeight(iView.getImage().getHeight());
            FxmlControl.moveXCenter(sPane, iView);
            iView.setLayoutY(10);
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public static void zoomIn(ScrollPane sPane, ImageView iView, int xZoomStep, int yZoomStep) {
        double currentWidth = iView.getFitWidth();
        if (currentWidth == -1) {
            currentWidth = iView.getImage().getWidth();
        }
        iView.setFitWidth(currentWidth + xZoomStep);
        double currentHeight = iView.getFitHeight();
        if (currentHeight == -1) {
            currentHeight = iView.getImage().getHeight();
        }
        iView.setFitHeight(currentHeight + yZoomStep);
        FxmlControl.moveXCenter(sPane, iView);
    }

    public static void zoomOut(ScrollPane sPane, ImageView iView, int xZoomStep, int yZoomStep) {
        double currentWidth = iView.getFitWidth();
        if (currentWidth == -1) {
            currentWidth = iView.getImage().getWidth();
        }
        if (currentWidth <= xZoomStep) {
            return;
        }
        iView.setFitWidth(currentWidth - xZoomStep);
        double currentHeight = iView.getFitHeight();
        if (currentHeight == -1) {
            currentHeight = iView.getImage().getHeight();
        }
        if (currentHeight <= yZoomStep) {
            return;
        }
        iView.setFitHeight(currentHeight - yZoomStep);
        FxmlControl.moveXCenter(sPane, iView);
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

    public static Rectangle2D getScreen() {
        return Screen.getPrimary().getVisualBounds();
    }

    public static void locateCenter(Region region, PopupWindow window) {
        Bounds bounds = region.localToScreen(region.getBoundsInLocal());
        window.show(region, bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY() + bounds.getHeight() / 2);
    }

    public static void locateBelow(Node node, PopupWindow window) {
        Bounds bounds = node.localToScreen(node.getBoundsInLocal());
        window.show(node, bounds.getMinX() + 2, bounds.getMinY() + bounds.getHeight() + 5);
    }

    public static void locateBelow(Region region, PopupWindow window) {
        Bounds bounds = region.localToScreen(region.getBoundsInLocal());
        window.show(region, bounds.getMinX() + 2, bounds.getMinY() + bounds.getHeight() + 5);
    }

    public static void locateRightTop(Region region, PopupWindow window) {
        Bounds bounds = region.localToScreen(region.getBoundsInLocal());
        window.show(region, bounds.getMaxX() - window.getWidth() - 20, bounds.getMinY() + 50);
    }

    public static void locateUp(Region region, PopupWindow window) {
        Bounds bounds = region.localToScreen(region.getBoundsInLocal());
        window.show(region, bounds.getMinX() + 2, bounds.getMinY() - 50);
    }

    public static void locateRight(Stage stage) {
        Rectangle2D screen = getScreen();
        stage.setX(screen.getWidth() - stage.getWidth());
    }

    public static List<Node> traverseNode(Node node, List<Node> children) {
        if (node == null) {
            return children;
        }
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(node);
        if (node instanceof Parent) {
            for (Node c : ((Parent) node).getChildrenUnmodifiable()) {
                traverseNode(c, children);
            }
        }
        return children;
    }

    // https://stackoverflow.com/questions/11552176/generating-a-mouseevent-in-javafx/11567122?r=SearchResults#11567122
    public static void fireMouseClicked(Node node) {
        try {
            Event.fireEvent(node, new MouseEvent(MouseEvent.MOUSE_CLICKED, 0,
                    0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true,
                    true, true, true, true, true, true, null));
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public static void popText(String text, String color, String size, Stage stage) {
        try {
            Popup popup = new Popup();
            popup.setAutoHide(true);
            popup.setAutoFix(true);
            Label popupLabel = new Label(text);
            popupLabel.setStyle("-fx-background-color:black;"
                    + " -fx-text-fill: " + color + ";"
                    + " -fx-font-size: " + size + ";"
                    + " -fx-padding: 10px;"
                    + " -fx-background-radius: 6;");
            popup.getContent().add(popupLabel);

            popup.show(stage);

        } catch (Exception e) {

        }
    }

    // https://stackoverflow.com/questions/31264847/how-to-set-remember-scrollbar-thumb-position-in-javafx-8-webview?r=SearchResults
    public static ScrollBar getVScrollBar(WebView webView) {
        try {
            Set<Node> scrolls = webView.lookupAll(".scroll-bar");
            for (Node scrollNode : scrolls) {
                if (ScrollBar.class.isInstance(scrollNode)) {
                    ScrollBar scroll = (ScrollBar) scrollNode;
                    if (scroll.getOrientation() == Orientation.VERTICAL) {
                        return scroll;
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
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
            logger.debug(e.toString());
            return null;
        }

    }

}
