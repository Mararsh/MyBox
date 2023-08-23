package mara.mybox.fxml;

import java.awt.Point;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.robot.Robot;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.SystemTools;

/**
 * @Author Mara
 * @CreateDate 2021-8-5
 * @License Apache License Version 2.0
 */
public class LocateTools {

    public static final int PopOffsetY = 30;

    public static void locateRight(Stage stage) {
        Rectangle2D screen = NodeTools.getScreen();
        stage.setX(screen.getWidth() - stage.getWidth());
    }

    public static void locateUp(Node node, PopupWindow window) {
        Bounds bounds = node.localToScreen(node.getBoundsInLocal());
        window.show(node, bounds.getMinX() + 2, bounds.getMinY() - 50);
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

    public static void locateCenter(Node node, PopupWindow window) {
        Bounds bounds = node.localToScreen(node.getBoundsInLocal());
        window.show(node, bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY() + bounds.getHeight() / 2);
    }

    public static void locateRightTop(Node node, PopupWindow window) {
        Bounds bounds = node.localToScreen(node.getBoundsInLocal());
        window.show(node, bounds.getMaxX() - window.getWidth() - 20, bounds.getMinY() + 50);
    }

    public static void aboveCenter(Node node, Node refer) {
        if (node == null || refer == null) {
            return;
        }
        Bounds regionBounds = node.getBoundsInLocal();
        Bounds referBounds = refer.getBoundsInLocal();
        double xOffset = referBounds.getWidth() - regionBounds.getWidth();
        node.setLayoutX(referBounds.getMinX() + xOffset / 2);
        node.setLayoutY(referBounds.getMinY() - 10);
    }

    public static void belowCenter(Node node, Node refer) {
        if (node == null || refer == null) {
            return;
        }
        Bounds regionBounds = node.getBoundsInLocal();
        Bounds referBounds = refer.getBoundsInLocal();
        double xOffset = referBounds.getWidth() - regionBounds.getWidth();
        node.setLayoutX(referBounds.getMinX() + xOffset / 2);
        node.setLayoutY(referBounds.getMaxY() + 10);
    }

    public static void center(Node node, Node refer) {
        if (node == null || refer == null) {
            return;
        }
        Bounds regionBounds = node.getBoundsInLocal();
        Bounds referBounds = refer.getBoundsInLocal();
        double xOffset = referBounds.getWidth() - regionBounds.getWidth();
        double yOffset = referBounds.getHeight() - regionBounds.getHeight();
        node.setLayoutX(referBounds.getMinX() + xOffset / 2);
        node.setLayoutY(referBounds.getMinY() + yOffset / 2);
    }

    public static void locateBelow(Node node, PopupWindow window) {
        Bounds bounds = node.localToScreen(node.getBoundsInLocal());
        window.show(node, bounds.getMinX() + 2, bounds.getMinY() + bounds.getHeight());
    }

    public static void locateMouse(MouseEvent event, PopupWindow window) {
        window.show((Node) event.getSource(), event.getScreenX(), event.getScreenY());
    }

    public static void locateEvent(Event event, PopupWindow window) {
        if (window == null || event == null) {
            return;
        }
        double x, y;
        try {
            Point2D everntCoord = LocateTools.getScreenCoordinate(event);
            x = everntCoord.getX();
            y = everntCoord.getY() + LocateTools.PopOffsetY;
        } catch (Exception e) {
            javafx.scene.robot.Robot r = new javafx.scene.robot.Robot();
            x = r.getMouseX();
            y = r.getMouseY() + PopOffsetY;
        }
        window.show((Node) event.getSource(), x, y);
    }

    public static void locateMouse(Node owner, PopupWindow window) {
        Point point = SystemTools.getMousePoint();
        window.show(owner, point.getX(), point.getY());
    }

    public static void moveXCenter(Node pNnode, Node node) {
        if (node == null || pNnode == null) {
            return;
        }
        double xOffset = pNnode.getBoundsInLocal().getWidth() - node.getBoundsInLocal().getWidth();
        if (xOffset > 0) {
            node.setLayoutX(xOffset / 2);
        } else {
            node.setLayoutX(0);
        }
    }

    public static void moveXCenter(Node node) {
        if (node == null) {
            return;
        }
        double xOffset = node.getBoundsInLocal().getWidth() - node.getBoundsInLocal().getWidth();
        if (xOffset > 0) {
            node.setLayoutX(xOffset / 2);
        } else {
            node.setLayoutX(0);
        }
    }

    public static void moveYCenter(Node pNnode, Node node) {
        if (node == null || pNnode == null) {
            return;
        }
        double yOffset = pNnode.getBoundsInLocal().getHeight() - node.getBoundsInLocal().getHeight();
        if (yOffset > 0) {
            node.setLayoutY(yOffset / 2);
        } else {
            node.setLayoutY(0);
        }
    }

    public static void moveYCenter(Node node) {
        if (node == null) {
            return;
        }
        double yOffset = node.getBoundsInLocal().getHeight() - node.getBoundsInLocal().getHeight();
        if (yOffset > 0) {
            node.setLayoutY(yOffset / 2);
        } else {
            node.setLayoutY(0);
        }
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

    public static void moveCenter(Node pNnode, Node node) {
        moveXCenter(pNnode, node);
        moveYCenter(pNnode, node);
    }

    public static void moveCenter(Node node) {
        moveXCenter(node);
        moveYCenter(node);
    }

    public static void fitSize(Node pNnode, Region region, int margin) {
        try {
            if (pNnode == null || region == null) {
                return;
            }
            Bounds bounds = pNnode.getBoundsInLocal();
            region.setPrefSize(bounds.getWidth() - 2 * margin, bounds.getHeight() - 2 * margin);
            LocateTools.moveCenter(pNnode, region);
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
        }
    }

    public static double getScreenX(Node node) {
        Point2D localToScreen = node.localToScreen(0, 0);
        return localToScreen.getX();
    }

    public static double getScreenY(Node node) {
        Point2D localToScreen = node.localToScreen(0, 0);
        return localToScreen.getY();
    }

    public static Point2D getScreenCoordinate(Event event) {
        try {
            Node node = (Node) (event.getTarget());
            return node.localToScreen(0, 0);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
