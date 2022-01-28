package mara.mybox.fxml;

import java.awt.Robot;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;

/**
 * @Author Mara
 * @CreateDate 2021-8-5
 * @License Apache License Version 2.0
 */
public class LocateTools {

    public static void locateRight(Stage stage) {
        Rectangle2D screen = NodeTools.getScreen();
        stage.setX(screen.getWidth() - stage.getWidth());
    }

    public static void locateUp(Region region, PopupWindow window) {
        Bounds bounds = region.localToScreen(region.getBoundsInLocal());
        window.show(region, bounds.getMinX() + 2, bounds.getMinY() - 50);
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

    public static void locateRightTop(Region region, PopupWindow window) {
        Bounds bounds = region.localToScreen(region.getBoundsInLocal());
        window.show(region, bounds.getMaxX() - window.getWidth() - 20, bounds.getMinY() + 50);
    }

    public static void aboveCenter(Node node, Node refer) {
        if (node == null || refer == null) {
            return;
        }
        Bounds regionBounds = node.getBoundsInParent();
        Bounds referBounds = refer.getBoundsInParent();
        double xOffset = referBounds.getWidth() - regionBounds.getWidth();
        node.setLayoutX(referBounds.getMinX() + xOffset / 2);
        node.setLayoutY(referBounds.getMinY() - 10);
    }

    public static void belowCenter(Node node, Node refer) {
        if (node == null || refer == null) {
            return;
        }
        Bounds regionBounds = node.getBoundsInParent();
        Bounds referBounds = refer.getBoundsInParent();
        double xOffset = referBounds.getWidth() - regionBounds.getWidth();
        node.setLayoutX(referBounds.getMinX() + xOffset / 2);
        node.setLayoutY(referBounds.getMaxY() + 10);
    }

    public static void center(Node node, Node refer) {
        if (node == null || refer == null) {
            return;
        }
        Bounds regionBounds = node.getBoundsInParent();
        Bounds referBounds = refer.getBoundsInParent();
        double xOffset = referBounds.getWidth() - regionBounds.getWidth();
        double yOffset = referBounds.getHeight() - regionBounds.getHeight();
        node.setLayoutX(referBounds.getMinX() + xOffset / 2);
        node.setLayoutY(referBounds.getMinY() + yOffset / 2);
    }

    public static void locateBelow(Node node, PopupWindow window) {
        Bounds bounds = node.localToScreen(node.getBoundsInLocal());
        window.show(node, bounds.getMinX() + 2, bounds.getMinY() + bounds.getHeight());
    }

    public static void locateBelow(Region region, PopupWindow window) {
        Bounds bounds = region.localToScreen(region.getBoundsInLocal());
        window.show(region, bounds.getMinX() + 2, bounds.getMinY() + bounds.getHeight());
    }

    public static void locateMouse(MouseEvent event, PopupWindow window) {
        window.show((Node) event.getSource(), event.getScreenX(), event.getScreenY());
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

    public static void moveCenter(Region pNnode, Node node) {
        moveXCenter(pNnode, node);
        moveYCenter(pNnode, node);
    }

    public static void moveCenter(Node node) {
        moveXCenter(node);
        moveYCenter(node);
    }

    public static double getScreenX(Node node) {
        Point2D localToScreen = node.localToScreen(0, 0);
        return localToScreen.getX();
    }

    public static double getScreenY(Node node) {
        Point2D localToScreen = node.localToScreen(0, 0);
        return localToScreen.getY();
    }

    public static Point2D getScreenCoordinate(ActionEvent event) {
        Button button = (Button) (event.getTarget());
        return button.localToScreen(0, 0);
    }

}
