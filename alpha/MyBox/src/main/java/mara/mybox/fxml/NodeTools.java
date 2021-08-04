package mara.mybox.fxml;

import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.PopupWindow;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import mara.mybox.bufferedimage.ImageBlend;
import mara.mybox.bufferedimage.PixelsBlend;
import mara.mybox.bufferedimage.PixelsBlendFactory;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.ImagesBrowserController;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ImageViewTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 11:19:42
 * @License Apache License Version 2.0
 */
public class NodeTools {

    public static String blueText = "-fx-text-fill: #2e598a;";
    public static String darkBlueText = "-fx-text-fill: #2e598a;  -fx-font-weight: bolder;";
    public static String redText = "-fx-text-fill: #961c1c;";
    public static String darkRedText = "-fx-text-fill: #961c1c;  -fx-font-weight: bolder;";
    public static String badStyle = "-fx-text-box-border: blue;   -fx-text-fill: blue;";
    public static String warnStyle = "-fx-text-box-border: orange;   -fx-text-fill: orange;";
    public static String errorData = "-fx-background-color: #e5fbe5;";
    public static String selectedData = "-fx-background-color:  #0096C9; -fx-text-background-color: white;";

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
    }

    public static void setScrollPane(ScrollPane scrollPane, double xOffset,
            double yOffset) {
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
        input.textProperty().addListener(
                (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    try {
                        float v = Float.valueOf(newValue);
                        input.setStyle(null);
                    } catch (Exception e) {
                        input.setStyle(badStyle);
                    }
                });
    }

    public static void setFileValidation(final TextField input, String key) {
        if (input == null) {
            return;
        }
        input.setStyle(badStyle);
        input.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            String v = input.getText();
            if (v == null || v.isEmpty()) {
                input.setStyle(badStyle);
                return;
            }
            final File file = new File(newValue);
            if (!file.exists() || !file.isFile()) {
                input.setStyle(badStyle);
                return;
            }
            input.setStyle(null);
            UserConfig.setUserConfigString(key, file.getParent());
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

    public static HTMLEditor editor(WebView webView) {
        if (webView == null) {
            return null;
        }
        Parent p = webView.getParent();
        while (p != null) {
            if (p instanceof HTMLEditor) {
                return (HTMLEditor) p;
            }
            p = p.getParent();
        }
        return null;
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
            return editor((WebView) node);
        }
        return null;
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

    public static void moveXCenter(Region pNnode, Node node) {
        if (node == null || pNnode == null) {
            return;
        }
        double xOffset = pNnode.getBoundsInParent().getWidth() - node.getBoundsInParent().getWidth();
        if (xOffset > 0) {
            node.setLayoutX(xOffset / 2);
        } else {
            node.setLayoutX(0);
        }
    }

    public static void moveYCenter(Region pNnode, Node node) {
        if (node == null || pNnode == null) {
            return;
        }
        double yOffset = pNnode.getBoundsInParent().getHeight() - node.getBoundsInParent().getHeight();
        if (yOffset > 0) {
            node.setLayoutY(yOffset / 2);
        } else {
            node.setLayoutY(0);
        }
    }

    public static void moveCenter(Region pNnode, Node node) {
        moveXCenter(pNnode, node);
        moveYCenter(pNnode, node);
    }

    public static void moveXCenter(Node node) {
        if (node == null) {
            return;
        }
        double xOffset = node.getBoundsInParent().getWidth() - node.getBoundsInParent().getWidth();
        if (xOffset > 0) {
            node.setLayoutX(xOffset / 2);
        } else {
            node.setLayoutX(0);
        }
    }

    public static void moveYCenter(Node node) {
        if (node == null) {
            return;
        }
        double yOffset = node.getBoundsInParent().getHeight() - node.getBoundsInParent().getHeight();
        if (yOffset > 0) {
            node.setLayoutY(yOffset / 2);
        } else {
            node.setLayoutY(0);
        }
    }

    public static void moveCenter(Node node) {
        moveXCenter(node);
        moveYCenter(node);
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

    public static void mouseCenter() {
        Rectangle2D screen = NodeTools.getScreen();
        try {
            Robot robot = new Robot();
            robot.mouseMove((int) screen.getWidth() / 2, (int) screen.getHeight() / 2);
        } catch (Exception e) {
        }
    }

    public static void mouseCenter(Stage stage) {
        try {
            Robot robot = new Robot();
            robot.mouseMove((int) (stage.getX() + stage.getWidth() / 2), (int) (stage.getY() + stage.getHeight() / 2));
        } catch (Exception e) {
        }
    }

    public static void locateCenter(Stage stage, Node node) {
        if (stage == null || node == null) {
            return;
        }
        Rectangle2D screen = NodeTools.getScreen();
        Bounds bounds = node.localToScreen(node.getBoundsInLocal());
        double centerX = bounds.getMinX() - stage.getWidth() / 2;
        centerX = Math.min(screen.getWidth(), Math.max(0, centerX));
        stage.setX(centerX);

        double centerY = bounds.getMinY() - stage.getHeight() / 2;
        centerY = Math.min(screen.getHeight(), Math.max(0, centerY));
        stage.setY(centerY);
    }

    public static void locateCenter(Region region, PopupWindow window) {
        Bounds bounds = region.localToScreen(region.getBoundsInLocal());
        window.show(region, bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY() + bounds.getHeight() / 2);
    }

    public static void locateBelow(Node node, PopupWindow window) {
        Bounds bounds = node.localToScreen(node.getBoundsInLocal());
        window.show(node, bounds.getMinX() + 2, bounds.getMinY() + bounds.getHeight());
    }

    public static void locateBelow(Region region, PopupWindow window) {
        Bounds bounds = region.localToScreen(region.getBoundsInLocal());
        window.show(region, bounds.getMinX() + 2, bounds.getMinY() + bounds.getHeight());
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

    public static void locateMouse(MouseEvent event, PopupWindow window) {
        window.show((Node) event.getSource(), event.getScreenX(), event.getScreenY());
    }

    // https://stackoverflow.com/questions/11552176/generating-a-mouseevent-in-javafx/11567122?r=SearchResults#11567122
    public static void fireMouseClicked(Node node) {
        try {
            Event.fireEvent(node, new MouseEvent(MouseEvent.MOUSE_CLICKED, 0,
                    0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true,
                    true, true, true, true, true, true, null));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
            MyBoxLog.debug(e.toString());
            return null;
        }

    }

    public static Color imagePixel(MouseEvent event, ImageView view) {
        DoublePoint p = ImageViewTools.getImageXY(event, view);
        if (p == null) {
            return null;
        }
        return imagePixel(p, view);
    }

    public static Color imagePixel(DoublePoint p, ImageView view) {
        if (p == null || view == null) {
            return null;
        }
        PixelReader pixelReader = view.getImage().getPixelReader();
        return pixelReader.getColor((int) p.getX(), (int) p.getY());
    }

    public static void blendDemoFx(BaseController parent, Button demoButton,
            Image foreImage, Image backImage, int x, int y,
            float opacity, boolean orderReversed, boolean ignoreTransparent) {
        BufferedImage foreBI = null;
        if (foreImage != null) {
            foreBI = SwingFXUtils.fromFXImage(foreImage, null);
        }
        BufferedImage backBI = null;
        if (backImage != null) {
            backBI = SwingFXUtils.fromFXImage(backImage, null);
        }
        blendDemo(parent, demoButton, foreBI, backBI, x, y, opacity, orderReversed, ignoreTransparent);
    }

    public static void blendDemo(BaseController parent, Button demoButton,
            BufferedImage foreImage, BufferedImage backImage, int x, int y,
            float opacity, boolean orderReversed, boolean ignoreTransparent) {
        if (parent != null) {
            parent.popInformation(Languages.message("WaitAndHandling"), 6000);
        }
        if (demoButton != null) {
            demoButton.setVisible(false);
        }
        Task demoTask = new Task<Void>() {
            private List<File> files;

            @Override
            protected Void call() {
                try {
                    BufferedImage foreBI = foreImage;
                    if (foreBI == null) {
                        foreBI = SwingFXUtils.fromFXImage(new Image("img/About.png"), null);
                    }
                    BufferedImage backBI = backImage;
                    if (backBI == null) {
                        backBI = SwingFXUtils.fromFXImage(new Image("img/ww8.png"), null);
                    }
                    files = new ArrayList<>();
                    for (String name : PixelsBlendFactory.blendModes()) {
                        PixelsBlend.ImagesBlendMode mode = PixelsBlendFactory.blendMode(name);
                        if (mode == PixelsBlend.ImagesBlendMode.NORMAL) {
                            BufferedImage blended = ImageBlend.blend(foreBI, backBI, x, y, mode, 1f, orderReversed, ignoreTransparent);
                            File tmpFile = new File(AppVariables.MyBoxTempPath + File.separator + name + "-"
                                    + Languages.message("Opacity") + "-1.0f.png");
                            if (ImageFileWriters.writeImageFile(blended, tmpFile)) {
                                files.add(tmpFile);
                            }
                            if (opacity < 1f) {
                                blended = ImageBlend.blend(foreBI, backBI, x, y, mode, opacity, orderReversed, ignoreTransparent);
                                tmpFile = new File(AppVariables.MyBoxTempPath + File.separator + name + "-"
                                        + Languages.message("Opacity") + "-" + opacity + "f.png");
                                if (ImageFileWriters.writeImageFile(blended, tmpFile)) {
                                    files.add(tmpFile);
                                }
                            }
                        } else {
                            BufferedImage blended = ImageBlend.blend(foreBI, backBI, x, y, mode, opacity, orderReversed, ignoreTransparent);
                            File tmpFile = new File(AppVariables.MyBoxTempPath + File.separator + name + "-"
                                    + Languages.message("Opacity") + "-" + opacity + "f.png");
                            if (ImageFileWriters.writeImageFile(blended, tmpFile)) {
                                files.add(tmpFile);
                            }
                        }
                    }
                } catch (Exception e) {
                    MyBoxLog.debug(e.toString());
                }
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                if (demoButton != null) {
                    demoButton.setVisible(true);
                }
                if (files.isEmpty()) {
                    return;
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ImagesBrowserController controller
                                    = (ImagesBrowserController) WindowTools.openStage(Fxmls.ImagesBrowserFxml);
                            controller.loadImages(files);
                        } catch (Exception e) {
                            MyBoxLog.error(e.toString());
                        }
                    }
                });
            }

        };
        Thread thread = new Thread(demoTask);
        thread.setDaemon(false);
        thread.start();

    }

}
