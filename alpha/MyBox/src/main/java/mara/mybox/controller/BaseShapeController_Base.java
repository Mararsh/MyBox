package mara.mybox.controller;

import java.awt.geom.Arc2D;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import mara.mybox.data.DoubleArc;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleCubic;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoubleLine;
import mara.mybox.data.DoublePath;
import mara.mybox.data.DoublePathSegment;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoublePolyline;
import mara.mybox.data.DoublePolylines;
import mara.mybox.data.DoubleQuadratic;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.data.ShapeStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-10
 * @License Apache License Version 2.0
 */
public abstract class BaseShapeController_Base extends BaseImageController {

    // Only one shape is shown at one time
    protected DoubleRectangle maskRectangleData;
    protected DoubleCircle maskCircleData;
    protected DoubleEllipse maskEllipseData;
    protected DoubleLine maskLineData;
    protected DoublePolygon maskPolygonData;
    protected DoublePolyline maskPolylineData;
    protected DoublePolylines maskPolylinesData;
    protected DoubleQuadratic maskQuadraticData;
    protected DoubleCubic maskCubicData;
    protected DoubleArc maskArcData;
    protected DoublePath maskPathData;
    protected boolean maskControlDragged, showAnchors, popItemMenu, popShapeMenu,
            addPointWhenClick, supportPath;
    protected AnchorShape anchorShape;
    protected Polyline currentLine;
    protected List<DoublePoint> currentLineData;
    protected List<Polyline> maskPolylines;
    protected DoublePoint lastPoint;

    protected ShapeStyle shapeStyle = null;
    public SimpleBooleanProperty maskShapeChanged = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty maskShapeDataChanged = new SimpleBooleanProperty(false);

    public enum AnchorShape {
        Rectangle, Circle, Name
    }
    @FXML
    protected Rectangle maskRectangle;
    @FXML
    protected Circle maskCircle;
    @FXML
    protected Ellipse maskEllipse;
    @FXML
    protected Line maskLine;
    @FXML
    protected Polygon maskPolygon;
    @FXML
    protected Polyline maskPolyline;
    @FXML
    protected Arc maskArc;
    @FXML
    protected CubicCurve maskCubic;
    @FXML
    protected QuadCurve maskQuadratic;
    @FXML
    protected SVGPath maskSVGPath;
    @FXML
    protected CheckBox fillCheck, dashCheck, anchorCheck,
            popAnchorMenuCheck, popLineMenuCheck, addPointCheck;

    /*
        values
     */
    public Color strokeColor() {
        try {
            return shapeStyle == null
                    ? Color.web(UserConfig.getString(baseName + "StrokeColor", ShapeStyle.DefaultStrokeColor))
                    : shapeStyle.getStrokeColor();
        } catch (Exception e) {
            return Color.web(ShapeStyle.DefaultStrokeColor);
        }
    }

    public float strokeWidth() {
        float v = shapeStyle == null ? UserConfig.getFloat(baseName + "StrokeWidth", 2)
                : shapeStyle.getStrokeWidth() * (float) viewXRatio();
        if (v < 0) {
            v = 2;
        }
        return v;
    }

    public Color anchorColor() {
        try {
            return shapeStyle == null
                    ? Color.web(UserConfig.getString(baseName + "AnchorColor", ShapeStyle.DefaultAnchorColor))
                    : shapeStyle.getAnchorColor();
        } catch (Exception e) {
            return Color.web(ShapeStyle.DefaultAnchorColor);
        }
    }

    public float anchorSize() {
        float v = shapeStyle == null ? UserConfig.getFloat(baseName + "AnchorSize", 10)
                : shapeStyle.getAnchorSize();
        if (v < 0) {
            v = 10;
        }
        return v;
    }

    /*
        shapes
     */
    public boolean drawMaskShape() {
        if (isMaskRectangleShown()) {
            return drawMaskRectangle();
        } else if (isMaskCircleShown()) {
            return drawMaskCircle();
        } else if (isMaskEllipseShown()) {
            return drawMaskEllipse();
        } else if (isMaskLineShown()) {
            return drawMaskLine();
        } else if (isMaskPolygonShown()) {
            return drawMaskPolygon();
        } else if (isMaskPolylineShown()) {
            return drawMaskPolyline();
        } else if (isMaskPolylinesShown()) {
            return drawMaskPolylines();
        } else if (isMaskQuadraticShown()) {
            return drawMaskQuadratic();
        } else if (isMaskCubicShown()) {
            return drawMaskCubic();
        } else if (isMaskArcShown()) {
            return drawMaskArc();
        } else if (isMaskPathShown()) {
            return drawMaskPath();
        }
        return false;
    }

    // Any mask operations when pane size is changed
    public boolean redrawMaskShape() {
        return drawMaskShape();
    }

    public void setMaskShapesStyle() {
        if (isMaskRectangleShown()) {
            setShapeStyle(maskRectangle);
        } else if (isMaskCircleShown()) {
            setShapeStyle(maskCircle);
        } else if (isMaskEllipseShown()) {
            setShapeStyle(maskEllipse);
        } else if (isMaskLineShown()) {
            setShapeStyle(maskLine);
        } else if (isMaskPolygonShown()) {
            setShapeStyle(maskPolygon);
        } else if (isMaskPolylineShown()) {
            setShapeStyle(maskPolyline);
        } else if (isMaskPolylinesShown()) {
            for (Polyline line : maskPolylines) {
                setShapeStyle(line);
            }
        } else if (isMaskQuadraticShown()) {
            setShapeStyle(maskQuadratic);
        } else if (isMaskCubicShown()) {
            setShapeStyle(maskCubic);
        } else if (isMaskArcShown()) {
            setShapeStyle(maskArc);
        } else if (isMaskPathShown()) {
            setShapeStyle(maskSVGPath);
        }
        setMaskAnchorsStyle();
    }

    public void setShapeStyle(Shape shape) {
        try {
            if (shape == null || !shape.isVisible()) {
                return;
            }
            double strokeWidth = strokeWidth();
            shape.setStrokeWidth(strokeWidth);
            shape.setStroke(strokeColor());
            shape.getStrokeDashArray().clear();
            if (shapeStyle != null) {
                if (shapeStyle.isIsFillColor()) {
                    shape.setFill(shapeStyle.getFillColor());
                } else {
                    shape.setFill(Color.TRANSPARENT);
                }
                shape.setStrokeLineCap(shapeStyle.getStrokeLineCap());
                if (shapeStyle.isIsStrokeDash() && shapeStyle.getStrokeDash() != null) {
                    shape.getStrokeDashArray().addAll(shapeStyle.getStrokeDash());
                }
            } else {
                shape.setFill(Color.TRANSPARENT);
                shape.getStrokeDashArray().addAll(strokeWidth, strokeWidth * 3);
            }
            if (isPickingColor) {
                shape.setCursor(Cursor.HAND);
            } else {
                shape.setCursor(Cursor.MOVE);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearMask() {
        clearMaskShapes();
        clearMaskShapesData();
    }

    public void clearMaskShapes() {
        try {
            drawMaskRulers();
            clearMaskRectangle();
            clearMaskCircle();
            clearMaskEllipse();
            clearMaskLine();
            clearMaskPolyline();
            clearMaskPolygon();
            clearMaskPolylines();
            clearMaskQuadratic();
            clearMaskCubic();
            clearMaskArc();
            clearMaskPath();
            clearMaskAnchors();
            maskControlDragged = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearMaskShapesData() {
        try {
            clearMaskRectangleData();
            clearMaskCircleData();
            clearMaskEllipseData();
            clearMaskLineData();
            clearMaskPolylineData();
            clearMaskPolygonData();
            clearMaskPolylinesData();
            clearMaskQuadraticData();
            clearMaskCubicData();
            clearMaskArcData();
            clearMaskPathData();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void maskShapeChanged() {
        if (!isSettingValues && maskShapeChanged != null) {
            maskShapeChanged.set(!maskShapeChanged.get());
        }
    }

    public void maskShapeDataChanged() {
        drawMaskShape();
        notifyShapeDataChanged();
    }

    public void notifyShapeDataChanged() {
        if (!isSettingValues && maskShapeDataChanged != null) {
            maskShapeDataChanged.set(!maskShapeDataChanged.get());
        }
        maskControlDragged = false;
    }

    public void hideMaskShape() {
        try {
            if (isMaskPolylinesShown()) {
                for (Polyline line : maskPolylines) {
                    line.setOpacity(0);
                }
                if (currentLine != null) {
                    maskPane.getChildren().remove(currentLine);
                    currentLine = null;
                }
                lastPoint = null;
            } else {
                Shape shape = currentMaskShape();
                if (shape != null) {
                    shape.setOpacity(0);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public Shape currentMaskShape() {
        if (isMaskRectangleShown()) {
            return maskRectangle;
        } else if (isMaskCircleShown()) {
            return maskCircle;
        } else if (isMaskEllipseShown()) {
            return maskEllipse;
        } else if (isMaskLineShown()) {
            return maskLine;
        } else if (isMaskPolygonShown()) {
            return maskPolygon;
        } else if (isMaskPolylineShown()) {
            return maskPolyline;
        } else if (isMaskQuadraticShown()) {
            return maskQuadratic;
        } else if (isMaskCubicShown()) {
            return maskCubic;
        } else if (isMaskArcShown()) {
            return maskArc;
        } else if (isMaskPathShown()) {
            return maskSVGPath;
        }
        return null;
    }

    public DoubleShape currentMaskShapeData() {
        if (isMaskRectangleShown()) {
            return maskRectangleData;
        } else if (isMaskCircleShown()) {
            return maskCircleData;
        } else if (isMaskEllipseShown()) {
            return maskEllipseData;
        } else if (isMaskLineShown()) {
            return maskLineData;
        } else if (isMaskPolygonShown()) {
            return maskPolygonData;
        } else if (isMaskPolylineShown()) {
            return maskPolylineData;
        } else if (isMaskPolylinesShown()) {
            return maskPolylinesData;
        } else if (isMaskQuadraticShown()) {
            return maskQuadraticData;
        } else if (isMaskCubicShown()) {
            return maskCubicData;
        } else if (isMaskArcShown()) {
            return maskArcData;
        } else if (isMaskPathShown()) {
            return maskPathData;
        }
        return null;
    }

    public DoubleShape shapeData(Shape shape) {
        if (shape == null) {
            return null;
        }
        if (shape == maskRectangle) {
            return maskRectangleData;
        } else if (shape == maskCircle) {
            return maskCircleData;
        } else if (shape == maskEllipse) {
            return maskEllipseData;
        } else if (shape == maskLine) {
            return maskLineData;
        } else if (shape == maskPolygon) {
            return maskPolygonData;
        } else if (shape == maskPolyline) {
            return maskPolylineData;
        } else if (shape == maskQuadratic) {
            return maskQuadraticData;
        } else if (shape == maskCubic) {
            return maskCubicData;
        } else if (shape == maskArc) {
            return maskArcData;
        } else if (shape == maskSVGPath) {
            return maskPathData;
        }
        return null;
    }

    /* 
        anchor
        index: 0-based
     */
    public Node addMaskAnchor(int index, DoublePoint p, double x, double y) {
        return addMaskAnchor(index, "p" + (index + 1) + "", message("Point") + " " + (index + 1),
                p, x, y, Cursor.MOVE);
    }

    public Node addMaskAnchor(int index, String name, String title,
            DoublePoint p, double x, double y) {
        return addMaskAnchor(index, name, title, p, x, y, Cursor.HAND);
    }

    public Node addMaskAnchor(int index, String name, String title, DoublePoint p,
            double x, double y, Cursor cursor) {
        try {
            Node anchor;
            if (anchorShape == AnchorShape.Name) {
                Text text = new Text(name == null || name.isBlank() ? "p" + index : name);
                text.setX(x - anchorSize() * 0.5);
                text.setY(y);
                anchor = text;
            } else if (anchorShape == AnchorShape.Circle) {
                Circle circle = new Circle();
                circle.setCenterX(x);
                circle.setCenterY(y);
                anchor = circle;
            } else {
                double anchorHW = anchorSize() * 0.5;
                Rectangle rect = new Rectangle();
                rect.setX(x - anchorHW);
                rect.setY(y - anchorHW);
                anchor = rect;
            }
            anchor.setLayoutX(imageView.getLayoutX());
            anchor.setLayoutY(imageView.getLayoutY());
            setAnchorStyle(anchor);

            anchor.setId("MaskShapeAnchor" + index + (name != null ? "_" + name : ""));
            anchor.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    controlPressed(event);
                    if (isPickingColor) {
                        return;
                    }
                    maskControlDragged = true;
                }
            });
            anchor.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    scrollPane.setPannable(true);
                    if (isPickingColor) {
                        return;
                    }
                    maskControlDragged = true;
                    double nx = maskEventX(event);
                    double ny = maskEventY(event);
                    if (DoubleShape.changed(nx - p.getX(), ny - p.getY())) {
                        moveMaskAnchor(index, name, new DoublePoint(nx, ny));
                    }
                }
            });
            anchor.hoverProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        if (isPickingColor) {
                            anchor.setCursor(Cursor.HAND);
                        } else {
                            anchor.setCursor(cursor);
                            if (popItemMenu) {
                                popNodeMenu(anchor, maskAnchorMenu(index, name, title, p));
                            }
                        }
                    }
                }
            });

            maskPane.getChildren().add(anchor);

            return anchor;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public boolean canDeleteAnchor() {
        return isMaskPolylineShown() || isMaskPolygonShown();
    }

    protected List<MenuItem> maskAnchorMenu(int index, String name, String title, DoublePoint p) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(title + "\n" + StringTools.menuPrefix(p.text(2)));
            menu.setStyle("-fx-text-fill: #2e598a;");
            items.add(menu);
            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("EditAnchor"), StyleTools.getIconImageView("iconEdit.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                PointInputController inputController = PointInputController.open(this, title, p);
                inputController.getNotify().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                        moveMaskAnchor(index, name, inputController.picked);
                        inputController.close();
                    }
                });
            });
            items.add(menu);

            if (canDeleteAnchor()) {
                menu = new MenuItem(message("DeleteAnchor"), StyleTools.getIconImageView("iconDelete.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    deleteMaskAnchor(index, name);
                });
                items.add(menu);
            }

            items.add(anchorShowItem());

            items.add(anchorMenuItem());

            if (isMaskPolygonShown() || isMaskPolylineShown()) {
                items.add(addPointMenu());
            }

            items.add(optionsMenu());

            items.add(new SeparatorMenuItem());

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public MenuItem anchorShowItem() {
        CheckMenuItem anchorShowItem = new CheckMenuItem(message("ShowAnchors"), StyleTools.getIconImageView("iconAnchor.png"));
        anchorShowItem.setSelected(UserConfig.getBoolean(baseName + "ShowAnchor", true));
        anchorShowItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent cevent) {
                if (anchorCheck != null) {
                    anchorCheck.setSelected(anchorShowItem.isSelected());
                } else {
                    UserConfig.setBoolean(baseName + "ShowAnchor", anchorShowItem.isSelected());
                    showAnchors = anchorShowItem.isSelected();
                    setMaskAnchorsStyle();
                }
            }
        });
        return anchorShowItem;
    }

    public MenuItem anchorMenuItem() {
        CheckMenuItem anchorMenuItem = new CheckMenuItem(
                isMaskPolylinesShown() ? message("PopLineMenu") : message("PopAnchorMenu"),
                StyleTools.getIconImageView("iconShape.png"));
        anchorMenuItem.setSelected(UserConfig.getBoolean(baseName + "ItemPopMenu", true));
        anchorMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent cevent) {
                if (popAnchorMenuCheck != null) {
                    popAnchorMenuCheck.setSelected(anchorMenuItem.isSelected());
                } else if (popLineMenuCheck != null) {
                    popLineMenuCheck.setSelected(anchorMenuItem.isSelected());
                } else {
                    UserConfig.setBoolean(baseName + "ItemPopMenu", anchorMenuItem.isSelected());
                    popItemMenu = anchorMenuItem.isSelected();
                }
            }
        });
        return anchorMenuItem;
    }

    public MenuItem optionsMenu() {
        MenuItem menu = new MenuItem(message("Options"), StyleTools.getIconImageView("iconOptions.png"));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent mevent) {
                options();
            }
        });
        return menu;
    }

    public MenuItem addPointMenu() {
        CheckMenuItem pointMenuItem = new CheckMenuItem(message("AddPointWhenLeftClick"), StyleTools.getIconImageView("iconNewItem.png"));
        pointMenuItem.setSelected(UserConfig.getBoolean(baseName + "AddPointWhenLeftClick", true));
        pointMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent cevent) {
                if (addPointCheck != null) {
                    addPointCheck.setSelected(pointMenuItem.isSelected());
                } else {
                    UserConfig.setBoolean(baseName + "AddPointWhenLeftClick", pointMenuItem.isSelected());
                    addPointWhenClick = pointMenuItem.isSelected();
                }
            }
        });
        return pointMenuItem;
    }

    public void moveMaskAnchor(int index, String name, DoublePoint p) {
        if (isMaskRectangleShown()) {
            moveMaskRectangleAnchor(index, p);

        } else if (isMaskCircleShown()) {
            moveMaskCircleAnchor(index, p);

        } else if (isMaskEllipseShown()) {
            moveMaskEllipseAnchor(index, p);

        } else if (isMaskLineShown()) {
            moveMaskLineAnchor(index, p);

        } else if (isMaskPolylineShown()) {
            maskPolylineData.set(index, p);
            maskShapeDataChanged();

        } else if (isMaskPolygonShown()) {
            maskPolygonData.set(index, p);
            maskShapeDataChanged();

        } else if (isMaskQuadraticShown()) {
            moveMaskQuadraticAnchor(index, p);

        } else if (isMaskCubicShown()) {
            moveMaskCubicAnchor(index, p);

        } else if (isMaskArcShown()) {
            moveMaskArcAnchor(index, p);

        } else if (isMaskPathShown()) {
            moveMaskPathAnchor(index, name, p);

        }

    }

    public void deleteMaskAnchor(int index, String name) {
        if (maskPolyline != null && maskPolyline.isVisible() && maskPolylineData != null) {
            maskPolylineData.remove(index);
            maskShapeDataChanged();

        } else if (maskPolygon != null && maskPolygon.isVisible() && maskPolygonData != null) {
            maskPolygonData.remove(index);
            maskShapeDataChanged();

        }
    }

    public void setAnchorStyle(Node anchor) {
        if (anchor == null) {
            return;
        }
        if (showAnchors) {
            Color anchorColor = anchorColor();
            float anchorSize = anchorSize();
            setAnchorStyle(anchor, anchorColor, anchorSize, new Font(anchorSize));
        } else {
            anchor.setVisible(false);
        }
    }

    public void setAnchorStyle(Node anchor, Color anchorColor, float anchorSize, Font font) {
        if (anchor == null) {
            return;
        }
        if (showAnchors) {
            if (anchor instanceof Rectangle) {
                Rectangle rect = (Rectangle) anchor;
                rect.setStrokeWidth(0);
                rect.setFill(anchorColor);
                rect.setWidth(anchorSize);
                rect.setHeight(anchorSize);

            } else if (anchor instanceof Circle) {
                Circle circle = (Circle) anchor;
                circle.setStrokeWidth(0);
                circle.setFill(anchorColor);
                circle.setRadius(anchorSize * 0.5);

            } else if (anchor instanceof Text) {
                Text text = (Text) anchor;
                text.setFill(anchorColor);
                text.setFont(font);

            }
            anchor.setVisible(true);
            anchor.toFront();
        } else {
            anchor.setVisible(false);
        }
    }

    public void setMaskAnchorsStyle() {
        try {
            if (maskPane == null) {
                return;
            }
            Color anchorColor = anchorColor();
            float anchorSize = anchorSize();
            Font font = new Font(anchorSize);
            List<Node> nodes = new ArrayList<>();
            nodes.addAll(maskPane.getChildren());
            for (Node node : nodes) {
                if (node == null || node.getId() == null) {
                    continue;
                }
                if (node.getId().startsWith("MaskShapeAnchor")) {
                    setAnchorStyle(node, anchorColor, anchorSize, font);
                }

            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearMaskAnchors() {
        if (maskPane == null) {
            return;
        }
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(maskPane.getChildren());
        for (Node node : nodes) {
            if (node == null || node.getId() == null) {
                continue;
            }
            if (node.getId().startsWith("MaskShapeAnchor")) {
                maskPane.getChildren().remove(node);
            }
        }
    }

    /*
        rectangle
     */
    public boolean isMaskRectangleShown() {
        return imageView != null && maskPane != null
                && maskRectangle != null && maskRectangle.isVisible()
                && maskRectangle != null;
    }

    public boolean showMaskRectangle() {
        if (imageView == null || maskPane == null || maskRectangle == null) {
            return false;
        }
        try {
            if (!maskPane.getChildren().contains(maskRectangle)) {
                maskPane.getChildren().addAll(maskRectangle);
            }
            maskRectangle.setOpacity(1);
            maskRectangle.setVisible(true);
            return drawMaskRectangle();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void setMaskRectangleDefaultValues() {
        if (imageView == null || maskPane == null || maskRectangle == null) {
            return;
        }
        double w = imageWidth();
        double h = imageHeight();
        maskRectangleData = DoubleRectangle.xywh(w / 4, h / 4, w / 2, h / 2);
    }

    public boolean drawMaskRectangle() {
        try {
            if (maskRectangle == null
                    || !maskPane.getChildren().contains(maskRectangle)
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }
            if (maskRectangleData == null) {
                setMaskRectangleDefaultValues();
            }
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
            double layoutX = imageView.getLayoutX();
            double layoutY = imageView.getLayoutY();
            double px1 = maskRectangleData.getX();
            double py1 = maskRectangleData.getY();
            double px2 = maskRectangleData.getMaxX();
            double py2 = maskRectangleData.getMaxY();
            double pcx = (px1 + px2) * 0.5;
            double pcy = (py1 + py2) * 0.5;
            double x1 = px1 * xRatio;
            double y1 = py1 * yRatio;
            double x2 = px2 * xRatio;
            double y2 = py2 * yRatio;
            double cx = pcx * xRatio;
            double cy = pcy * yRatio;
            maskRectangle.setLayoutX(layoutX);
            maskRectangle.setLayoutY(layoutY);
            maskRectangle.setX(x1);
            maskRectangle.setY(y1);
            maskRectangle.setWidth(x2 - x1);
            maskRectangle.setHeight(y2 - y1);

            setShapeStyle(maskRectangle);

            clearMaskAnchors();
            addMaskAnchor(1, message("LeftTop"), message("LeftTop"), new DoublePoint(px1, py1),
                    x1, y1, Cursor.NW_RESIZE);
            addMaskAnchor(2, message("TopCenter"), message("TopCenter"), new DoublePoint(pcx, py1),
                    cx, y1, Cursor.N_RESIZE);
            addMaskAnchor(3, message("RightTop"), message("RightTop"), new DoublePoint(px2, py1),
                    x2, y1, Cursor.NE_RESIZE);
            addMaskAnchor(4, message("LeftCenter"), message("LeftCenter"), new DoublePoint(px1, pcy),
                    x1, cy, Cursor.W_RESIZE);
            addMaskAnchor(5, message("RightCenter"), message("RightCenter"), new DoublePoint(px2, pcy),
                    x2, cy, Cursor.E_RESIZE);
            addMaskAnchor(6, message("LeftBottom"), message("LeftBottom"), new DoublePoint(px1, py2),
                    x1, y2, Cursor.SW_RESIZE);
            addMaskAnchor(7, message("BottomCenter"), message("BottomCenter"), new DoublePoint(pcx, py2),
                    cx, y2, Cursor.S_RESIZE);
            addMaskAnchor(8, message("RightBottom"), message("RightBottom"), new DoublePoint(px2, py2),
                    x2, y2, Cursor.SE_RESIZE);

            maskShapeChanged();

            updateLabelsTitle();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean moveMaskRectangleAnchor(int index, DoublePoint p) {
        double x = p.getX();
        double y = p.getY();
        double x1 = maskRectangleData.getX();
        double y1 = maskRectangleData.getY();
        double x2 = maskRectangleData.getMaxX();
        double y2 = maskRectangleData.getMaxY();
        switch (index) {
            case 1:
                maskRectangleData = DoubleRectangle.xy12(x, y, x2, y2);
                break;
            case 2:
                maskRectangleData = DoubleRectangle.xy12(x1, y, x2, y2);
                break;
            case 3:
                maskRectangleData = DoubleRectangle.xy12(x1, y, x, y2);
                break;
            case 4:
                maskRectangleData = DoubleRectangle.xy12(x, y1, x2, y2);
                break;
            case 5:
                maskRectangleData = DoubleRectangle.xy12(x1, y1, x, y2);
                break;
            case 6:
                maskRectangleData = DoubleRectangle.xy12(x, y1, x2, y);
                break;
            case 7:
                maskRectangleData = DoubleRectangle.xy12(x1, y1, x2, y);
                break;
            case 8:
                maskRectangleData = DoubleRectangle.xy12(x1, y1, x, y);
                break;
            default:
                return false;
        }
        maskShapeDataChanged();
        return true;
    }

    public void clearMaskRectangle() {
        try {
            if (imageView == null || maskPane == null || maskRectangle == null) {
                return;
            }
            maskPane.getChildren().removeAll(maskRectangle);
            maskRectangle.setVisible(false);
            clearMaskAnchors();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearMaskRectangleData() {
        if (maskRectangleData != null) {
            maskRectangleData = null;
        }
    }

    /*
        circle
     */
    public boolean isMaskCircleShown() {
        return imageView != null && maskPane != null
                && maskCircle != null && maskCircle.isVisible()
                && maskCircleData != null;
    }

    public boolean showMaskCircle() {
        if (imageView == null || maskPane == null || maskCircle == null) {
            return false;
        }
        if (!maskPane.getChildren().contains(maskCircle)) {
            maskPane.getChildren().addAll(maskCircle);
        }
        maskCircle.setOpacity(1);
        maskCircle.setVisible(true);
        return drawMaskCircle();
    }

    public void setMaskCircleDefaultValues() {
        if (imageView == null || maskPane == null || maskCircle == null) {
            return;
        }
        double w = imageWidth();
        double h = imageHeight();
        maskCircleData = new DoubleCircle(w / 2, h / 2, Math.min(w, h) / 4);
    }

    public boolean drawMaskCircle() {
        try {
            if (maskCircle == null || !maskCircle.isVisible()
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }
            if (maskCircleData == null) {
                setMaskCircleDefaultValues();
            }
            double layoutX = imageView.getLayoutX();
            double layoutY = imageView.getLayoutY();
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
            double sr = maskCircleData.getRadius();
            double scx = maskCircleData.getCenterX();
            double scy = maskCircleData.getCenterY();
            double r = sr * xRatio;
            double x = scx * xRatio;
            double y = scy * yRatio;
            maskCircle.setLayoutX(layoutX);
            maskCircle.setLayoutY(layoutY);
            maskCircle.setCenterX(x);
            maskCircle.setCenterY(y);
            maskCircle.setRadius(r);
            setShapeStyle(maskCircle);

            clearMaskAnchors();
            addMaskAnchor(1, message("TopCenter"), message("TopCenter"), new DoublePoint(scx, scy - sr),
                    x, y - r, Cursor.N_RESIZE);
            addMaskAnchor(2, message("LeftCenter"), message("LeftCenter"), new DoublePoint(scx - sr, scy),
                    x - r, y, Cursor.W_RESIZE);
            addMaskAnchor(3, message("RightCenter"), message("RightCenter"), new DoublePoint(scx + sr, scy),
                    x + r, y, Cursor.E_RESIZE);
            addMaskAnchor(4, message("BottomCenter"), message("BottomCenter"), new DoublePoint(scx, scy + sr),
                    x, y + r, Cursor.S_RESIZE);

            maskShapeChanged();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean moveMaskCircleAnchor(int index, DoublePoint p) {
        double x = p.getX();
        double y = p.getY();
        double scx = maskCircleData.getCenterX();
        double scy = maskCircleData.getCenterY();
        double sr = maskCircleData.getRadius();
        switch (index) {
            case 1:
                maskCircleData = new DoubleCircle(scx, (scy + sr + y) * 0.5, Math.abs(scy + sr - y) * 0.5);
                break;
            case 2:
                maskCircleData = new DoubleCircle((x + scx + sr) * 0.5, scy, Math.abs(scx + sr - x) * 0.5);
                break;
            case 3:
                maskCircleData = new DoubleCircle((scx - sr + x) * 0.5, scy, Math.abs(x - scx + sr) * 0.5);
                break;
            case 4:
                maskCircleData = new DoubleCircle(scx, (y + scy - sr) * 0.5, Math.abs(y - scy + sr) * 0.5);
                break;
            default:
                return false;
        }
        maskShapeDataChanged();
        return true;
    }

    public void clearMaskCircle() {
        if (imageView == null || maskPane == null || maskCircle == null) {
            return;
        }
        maskPane.getChildren().removeAll(maskCircle);
        maskCircle.setVisible(false);
        clearMaskAnchors();
    }

    public void clearMaskCircleData() {
        if (maskCircleData != null) {
            maskCircleData = null;
        }
    }

    /*
        ellipse
     */
    public boolean isMaskEllipseShown() {
        return imageView != null && maskPane != null
                && maskEllipse != null && maskEllipse.isVisible()
                && maskEllipseData != null;
    }

    public boolean showMaskEllipse() {
        if (imageView == null || maskPane == null || maskEllipse == null) {
            return false;
        }
        if (!maskPane.getChildren().contains(maskEllipse)) {
            maskPane.getChildren().addAll(maskEllipse);
        }
        maskEllipse.setOpacity(1);
        maskEllipse.setVisible(true);
        return drawMaskEllipse();
    }

    public void setMaskEllipseDefaultValues() {
        if (imageView == null || maskPane == null || maskEllipse == null) {
            return;
        }
        double w = imageWidth();
        double h = imageHeight();
        maskEllipseData = DoubleEllipse.ellipse(w / 2, h / 2, w / 4, h / 4);
    }

    public boolean drawMaskEllipse() {
        try {
            if (maskEllipse == null || !maskEllipse.isVisible()
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }
            if (maskEllipseData == null) {
                setMaskEllipseDefaultValues();
            }
            double layoutX = imageView.getLayoutX();
            double layoutY = imageView.getLayoutY();
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
            double srx = maskEllipseData.getRadiusX();
            double sry = maskEllipseData.getRadiusY();
            double scx = maskEllipseData.getCenterX();
            double scy = maskEllipseData.getCenterY();
            double rx = srx * xRatio;
            double ry = sry * yRatio;
            double cx = scx * xRatio;
            double cy = scy * yRatio;
            maskEllipse.setLayoutX(layoutX);
            maskEllipse.setLayoutY(layoutY);
            maskEllipse.setCenterX(cx);
            maskEllipse.setCenterY(cy);
            maskEllipse.setRadiusX(rx);
            maskEllipse.setRadiusY(ry);
            setShapeStyle(maskEllipse);

            clearMaskAnchors();
            addMaskAnchor(1, message("TopCenter"), message("TopCenter"), new DoublePoint(scx, scy - sry),
                    cx, cy - ry, Cursor.N_RESIZE);
            addMaskAnchor(2, message("LeftCenter"), message("LeftCenter"), new DoublePoint(scx - srx, scy),
                    cx - rx, cy, Cursor.W_RESIZE);
            addMaskAnchor(3, message("RightCenter"), message("RightCenter"), new DoublePoint(scx + srx, scy),
                    cx + rx, cy, Cursor.E_RESIZE);
            addMaskAnchor(4, message("BottomCenter"), message("BottomCenter"), new DoublePoint(scx, scy + sry),
                    cx, cy + ry, Cursor.S_RESIZE);

            maskShapeChanged();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }

    }

    public boolean moveMaskEllipseAnchor(int index, DoublePoint p) {
        double x = p.getX();
        double y = p.getY();
        double x1 = maskEllipseData.getX();
        double y1 = maskEllipseData.getY();
        double x2 = maskEllipseData.getMaxX();
        double y2 = maskEllipseData.getMaxY();
        switch (index) {
            case 1:
                maskEllipseData = DoubleEllipse.xy12(x1, y, x2, y2);
                break;
            case 2:
                maskEllipseData = DoubleEllipse.xy12(x, y1, x2, y2);
                break;
            case 3:
                maskEllipseData = DoubleEllipse.xy12(x1, y1, x, y2);
                break;
            case 4:
                maskEllipseData = DoubleEllipse.xy12(x1, y1, x2, y);
                break;
            default:
                return false;
        }
        maskShapeDataChanged();
        return true;
    }

    public void clearMaskEllipse() {
        if (imageView == null || maskPane == null || maskEllipse == null) {
            return;
        }
        maskPane.getChildren().removeAll(maskEllipse);
        maskEllipse.setVisible(false);
        clearMaskAnchors();
    }

    public void clearMaskEllipseData() {
        if (maskEllipseData != null) {
            maskEllipseData = null;
        }
    }

    /*
        line
     */
    public boolean isMaskLineShown() {
        return imageView != null && maskPane != null
                && maskLine != null && maskLine.isVisible()
                && maskLineData != null;
    }

    public boolean showMaskLine() {
        if (imageView == null || maskPane == null || maskLine == null) {
            return false;
        }
        if (!maskPane.getChildren().contains(maskLine)) {
            maskPane.getChildren().addAll(maskLine);
        }
        maskLine.setOpacity(1);
        maskLine.setVisible(true);
        return drawMaskLine();
    }

    public void setMaskLineDefaultValues() {
        if (imageView == null || maskPane == null || maskLine == null) {
            return;
        }
        double w = imageWidth();
        double h = imageHeight();
        maskLineData = new DoubleLine(w / 4, h / 4, w * 3 / 4, h * 3 / 4);
    }

    public boolean drawMaskLine() {
        try {
            if (maskLine == null || !maskLine.isVisible()
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }
            if (maskLineData == null) {
                setMaskLineDefaultValues();
            }
            double layoutX = imageView.getLayoutX();
            double layoutY = imageView.getLayoutY();
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
            double startX = maskLineData.getStartX() * xRatio;
            double startY = maskLineData.getStartY() * yRatio;
            double endX = maskLineData.getEndX() * xRatio;
            double endY = maskLineData.getEndY() * yRatio;

            maskLine.setLayoutX(layoutX);
            maskLine.setLayoutY(layoutY);
            maskLine.setStartX(startX);
            maskLine.setStartY(startY);
            maskLine.setEndX(endX);
            maskLine.setEndY(endY);
            maskLine.setVisible(true);
            setShapeStyle(maskLine);

            clearMaskAnchors();
            addMaskAnchor(1, message("StartPoint"), message("StartPoint"),
                    new DoublePoint(maskLineData.getStartX(), maskLineData.getStartY()),
                    startX, startY);
            addMaskAnchor(2, message("EndPoint"), message("EndPoint"),
                    new DoublePoint(maskLineData.getEndX(), maskLineData.getEndY()),
                    endX, endY);

            maskShapeChanged();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean moveMaskLineAnchor(int index, DoublePoint p) {
        double x = p.getX();
        double y = p.getY();
        switch (index) {
            case 1:
                maskLineData.setStartX(x);
                maskLineData.setStartY(y);
                break;
            case 2:
                maskLineData.setEndX(x);
                maskLineData.setEndY(y);
                break;
            default:
                return false;
        }
        maskShapeDataChanged();
        return true;
    }

    public void clearMaskLine() {
        if (imageView == null || maskPane == null || maskLine == null) {
            return;
        }
        maskPane.getChildren().removeAll(maskLine);
        maskLine.setVisible(false);
        clearMaskAnchors();
    }

    public void clearMaskLineData() {
        if (maskLineData != null) {
            maskLineData = null;
        }
    }

    /*
        polyline
     */
    public boolean isMaskPolylineShown() {
        return imageView != null && maskPane != null
                && maskPolyline != null && maskPolyline.isVisible()
                && maskPolylineData != null;
    }

    public boolean showMaskPolyline() {
        if (imageView == null || maskPane == null || maskPolyline == null) {
            return false;
        }
        if (!maskPane.getChildren().contains(maskPolyline)) {
            maskPane.getChildren().addAll(maskPolyline);
        }
        maskPolyline.setOpacity(1);
        maskPolyline.setVisible(true);
        return drawMaskPolyline();
    }

    public void setMaskPolylineDefaultValues() {
        if (imageView == null || maskPane == null || maskPolyline == null) {
            return;
        }
        maskPolylineData = new DoublePolyline();
    }

    public void setMaskPolylineExample() {
        if (imageView == null || maskPane == null || maskPolyline == null) {
            return;
        }
        double w = imageWidth();
        double h = imageHeight();
        maskPolylineData = new DoublePolyline();
        maskPolylineData.add(w / 4, h / 2);
        maskPolylineData.add(w / 2, h / 4);
        maskPolylineData.add(w * 3 / 8, h * 3 / 4);
        maskPolylineData.add(w * 3 / 4, h / 2);
    }

    public boolean drawMaskPolyline() {
        try {
            if (maskPolyline == null || !maskPolyline.isVisible()
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }
            if (maskPolylineData == null) {
                setMaskPolylineDefaultValues();
            }
            double layoutX = imageView.getLayoutX();
            double layoutY = imageView.getLayoutY();
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();

            clearMaskAnchors();
            maskPolyline.getPoints().clear();
            maskPolyline.setLayoutX(layoutX);
            maskPolyline.setLayoutY(layoutY);
            for (int i = 0; i < maskPolylineData.getSize(); ++i) {
                DoublePoint p = maskPolylineData.get(i);
                double x = p.getX() * xRatio;
                double y = p.getY() * yRatio;
                addMaskPolylinePoint(i, p, x, y);
            }

            setShapeStyle(maskPolyline);

            maskShapeChanged();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void addMaskPolylinePoint(int index, DoublePoint p, double x, double y) {
        try {
            maskPolyline.getPoints().add(x);
            maskPolyline.getPoints().add(y);

            addMaskAnchor(index, p, x, y);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearMaskPolyline() {
        if (imageView == null || maskPane == null || maskPolyline == null) {
            return;
        }
        maskPane.getChildren().remove(maskPolyline);
        maskPolyline.setVisible(false);
        maskPolyline.getPoints().clear();

        clearMaskAnchors();
    }

    public void clearMaskPolylineData() {
        if (maskPolylineData != null) {
            maskPolylineData.clear();
            maskPolylineData = null;
        }
    }

    /*
        polylines
     */
    public boolean isMaskPolylinesShown() {
        return imageView != null && maskPane != null && maskPolylines != null;
    }

    public void showMaskPolylines() {
        if (imageView == null || maskPane == null) {
            return;
        }
        clearMaskPolylines();
        maskPolylines = new ArrayList<>();
        drawMaskPolylines();
    }

    public void setMaskPolylinesDefaultValues() {
        if (imageView == null || maskPane == null || maskPolylines == null) {
            return;
        }
        maskPolylinesData = new DoublePolylines();
    }

    public void setMaskPolylinesExample() {
        if (imageView == null || maskPane == null || maskPolylines == null) {
            return;
        }
        double w = imageWidth();
        double h = imageHeight();
        maskPolylinesData = new DoublePolylines();
        List<DoublePoint> line = new ArrayList<>();
        double y = h / 2 - w / 4;
        line.add(new DoublePoint(w / 4, y));
        line.add(new DoublePoint(w / 2 - w / 16, y));
        maskPolylinesData.addLine(line);
        line = new ArrayList<>();
        line.add(new DoublePoint(w / 2 + w / 16, y));
        line.add(new DoublePoint(w * 3 / 4, y));
        maskPolylinesData.addLine(line);
        line = new ArrayList<>();
        y = h / 2;
        line.add(new DoublePoint(w / 4, y));
        line.add(new DoublePoint(w * 3 / 4, y));
        maskPolylinesData.addLine(line);
        line = new ArrayList<>();
        y = h / 2 + w / 4;
        line.add(new DoublePoint(w / 4, y));
        line.add(new DoublePoint(w * 3 / 4, y));
        maskPolylinesData.addLine(line);

    }

    public boolean drawMaskPolylines() {
        try {
            if (imageView == null || imageView.getImage() == null || maskPolylines == null) {
                return false;
            }
            if (maskPolylinesData == null) {
                setMaskPolylinesDefaultValues();
            }
            clearMaskPolylines();
            maskPolylines = new ArrayList<>();
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
            for (int i = 0; i < maskPolylinesData.getLinesSize(); i++) {
                List<DoublePoint> points = maskPolylinesData.getLines().get(i);
                if (points.size() < 2) {
                    continue;
                }
                Polyline pline = new Polyline();
                for (DoublePoint p : points) {
                    pline.getPoints().add(p.getX() * xRatio);
                    pline.getPoints().add(p.getY() * yRatio);
                }
                addToPolylines(pline, points);
            }
            maskShapeChanged();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void addToPolylines(Polyline line, List<DoublePoint> points) {
        try {
            if (maskPolylines == null || line == null || points == null) {
                return;
            }
            maskPolylines.add(line);
            maskPane.getChildren().add(line);
            line.setLayoutX(imageView.getLayoutX());
            line.setLayoutY(imageView.getLayoutY());
            setShapeStyle(line);
            int index = maskPolylines.indexOf(line);
            line.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    controlPressed(event);
                    if (isPickingColor) {
                        return;
                    }
                    maskControlDragged = true;
                }
            });
            line.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (isPickingColor) {
                        return;
                    }
                    maskControlDragged = true;
                }
            });
            line.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    scrollPane.setPannable(true);
                    if (isPickingColor) {
                        return;
                    }
                    double offsetX = imageOffsetX(event);
                    double offsetY = imageOffsetY(event);
                    if (!DoubleShape.changed(offsetX, offsetY)) {
                        maskControlDragged = false;
                        return;
                    }
                    maskPolylinesData.translateLineRel(index, offsetX, offsetY);
                    maskShapeDataChanged();
                    maskControlDragged = true;
                }
            });
            line.hoverProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        if (isPickingColor) {
                            line.setCursor(Cursor.HAND);
                        } else {
                            line.setCursor(Cursor.MOVE);
                            if (popItemMenu) {
                                popNodeMenu(line, lineMenu(line, points));
                            }
                        }
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected List<MenuItem> lineMenu(Polyline line, List<DoublePoint> points) {
        try {
            if (line == null) {
                return null;
            }
            int index = maskPolylines.indexOf(line);
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            String title = message("Line") + " " + (index + 1);
            menu = new MenuItem(title);
            menu.setStyle("-fx-text-fill: #2e598a;");
            items.add(menu);
            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("Edit"), StyleTools.getIconImageView("iconEdit.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                LineInputController inputController = LineInputController.open(this, title, points);
                inputController.getNotify().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                        List<DoublePoint> line = inputController.picked;
                        if (line == null || line.isEmpty()) {
                            popError(message("InvalidValue"));
                            return;
                        }
                        inputController.close();
                        maskPolylinesData.setLine(index, line);
                        maskShapeDataChanged();
                    }
                });
            });
            items.add(menu);

            menu = new MenuItem(message("Delete"), StyleTools.getIconImageView("iconDelete.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                maskPolylinesData.removeLine(index);
                maskShapeDataChanged();
            });
            items.add(menu);

            items.add(anchorShowItem());

            items.add(anchorMenuItem());

            items.add(new SeparatorMenuItem());

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public boolean addPointToCurrentLine(DoublePoint p) {
        if (lastPoint != null && !DoubleShape.changed(lastPoint, p)) {
            return false;
        }
        if (currentLineData == null) {
            currentLineData = new ArrayList<>();
            currentLine = new Polyline();
        } else if (currentLineData.size() > 1
                && !maskPolylines.contains(currentLine)) {
            addToPolylines(currentLine, currentLineData);
            maskPolylinesData.addLine(currentLineData);
        }

        currentLineData.add(p);

        double xRatio = viewXRatio();
        double yRatio = viewYRatio();
        currentLine.getPoints().add(p.getX() * xRatio);
        currentLine.getPoints().add(p.getY() * yRatio);
        return true;
    }

    public void hideMaskPolylines() {
        if (maskPolylines != null) {
            for (Polyline line : maskPolylines) {
                line.setOpacity(0);
            }
        }
        if (currentLine != null) {
            maskPane.getChildren().remove(currentLine);
            currentLine = null;
        }
        lastPoint = null;
    }

    public void clearMaskPolylines() {
        if (imageView == null || maskPane == null) {
            return;
        }
        if (maskPolylines != null) {
            for (Polyline line : maskPolylines) {
                maskPane.getChildren().remove(line);
            }
            maskPolylines.clear();
            maskPolylines = null;
        }
        if (currentLine != null) {
            maskPane.getChildren().remove(currentLine);
            currentLine = null;
        }
        lastPoint = null;
    }

    public void clearMaskPolylinesData() {
        if (maskPolylinesData != null) {
            maskPolylinesData.clear();
            maskPolylinesData = null;
        }
        maskPolylines = null;
    }

    /*
        polygon
     */
    public boolean isMaskPolygonShown() {
        return imageView != null && maskPane != null
                && maskPolygon != null && maskPolygon.isVisible()
                && maskPolygonData != null;
    }

    public boolean showMaskPolygon() {
        if (imageView == null || maskPane == null || maskPolygon == null) {
            return false;
        }
        if (!maskPane.getChildren().contains(maskPolygon)) {
            maskPane.getChildren().addAll(maskPolygon);
        }
        maskPolygon.setOpacity(1);
        maskPolygon.setVisible(true);
        return drawMaskPolygon();
    }

    public void setMaskPolygonDefaultValues() {
        if (imageView == null || maskPane == null || maskPolygon == null) {
            return;
        }
        maskPolygonData = new DoublePolygon();
    }

    public void setMaskPolygonExample() {
        if (imageView == null || maskPane == null || maskPolygon == null) {
            return;
        }
        double w = imageWidth();
        double h = imageHeight();
        maskPolygonData = new DoublePolygon();
        maskPolygonData.add(w / 4, h / 2);
        maskPolygonData.add(w / 2, h / 4);
        maskPolygonData.add(w * 3 / 8, h * 3 / 4);
        maskPolygonData.add(w * 3 / 4, h / 2);
    }

    public boolean drawMaskPolygon() {
        try {
            if (maskPolygon == null || !maskPolygon.isVisible()
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }
            clearMaskAnchors();
            if (maskPolygonData == null) {
                setMaskPolygonDefaultValues();
            }
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
            maskPolygon.getPoints().clear();
            maskPolygon.setLayoutX(imageView.getLayoutX());
            maskPolygon.setLayoutY(imageView.getLayoutY());
            for (int i = 0; i < maskPolygonData.getSize(); ++i) {
                DoublePoint p = maskPolygonData.get(i);
                double x = p.getX() * xRatio;
                double y = p.getY() * yRatio;
                addMaskPolygonPoint(i, p, x, y);
            }

            setShapeStyle(maskPolygon);

            maskShapeChanged();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void addMaskPolygonPoint(int index, DoublePoint p, double x, double y) {
        try {
            maskPolygon.getPoints().add(x);
            maskPolygon.getPoints().add(y);

            addMaskAnchor(index, p, x, y);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearMaskPolygon() {
        if (imageView == null || maskPane == null || maskPolygon == null) {
            return;
        }
        maskPane.getChildren().remove(maskPolygon);
        maskPolygon.setVisible(false);
        maskPolygon.getPoints().clear();
        clearMaskAnchors();
    }

    public void clearMaskPolygonData() {
        if (maskPolygonData != null) {
            maskPolygonData.clear();
            maskPolygonData = null;
        }
    }

    /*
        quadratic curve
     */
    public boolean isMaskQuadraticShown() {
        return imageView != null && maskPane != null
                && maskQuadratic != null && maskQuadratic.isVisible()
                && maskQuadraticData != null;
    }

    public boolean showMaskQuadratic() {
        if (imageView == null || maskPane == null || maskQuadratic == null) {
            return false;
        }
        if (!maskPane.getChildren().contains(maskQuadratic)) {
            maskPane.getChildren().addAll(maskQuadratic);
        }
        maskQuadratic.setOpacity(1);
        maskQuadratic.setVisible(true);
        return drawMaskQuadratic();
    }

    public void setMaskQuadraticDefaultValues() {
        if (imageView == null || maskPane == null || maskQuadratic == null) {
            return;
        }
        double w = imageWidth();
        double h = imageHeight();
        maskQuadraticData = new DoubleQuadratic(w / 5, h * 3 / 5, w / 3, 20, w * 3 / 4, h * 4 / 5);
    }

    public boolean drawMaskQuadratic() {
        try {
            if (maskQuadratic == null || !maskQuadratic.isVisible()
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }
            clearMaskAnchors();
            if (maskQuadraticData == null) {
                setMaskQuadraticDefaultValues();
            }
            double layoutX = imageView.getLayoutX();
            double layoutY = imageView.getLayoutY();
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
            double sx = maskQuadraticData.getStartX() * xRatio;
            double sy = maskQuadraticData.getStartY() * yRatio;
            double cx = maskQuadraticData.getControlX() * xRatio;
            double cy = maskQuadraticData.getControlY() * yRatio;
            double ex = maskQuadraticData.getEndX() * xRatio;
            double ey = maskQuadraticData.getEndY() * yRatio;
            maskQuadratic.setLayoutX(layoutX);
            maskQuadratic.setLayoutY(layoutY);
            maskQuadratic.setStartX(sx);
            maskQuadratic.setStartY(sy);
            maskQuadratic.setControlX(cx);
            maskQuadratic.setControlY(cy);
            maskQuadratic.setEndX(ex);
            maskQuadratic.setEndY(ey);

            addMaskAnchor(1, message("StartPoint"), message("StartPoint"),
                    new DoublePoint(maskQuadraticData.getStartX(), maskQuadraticData.getStartY()),
                    sx, sy);
            addMaskAnchor(2, message("ControlPoint"), message("ControlPoint"),
                    new DoublePoint(maskQuadraticData.getControlX(), maskQuadraticData.getControlY()),
                    cx, cy);
            addMaskAnchor(3, message("EndPoint"), message("EndPoint"),
                    new DoublePoint(maskQuadraticData.getEndX(), maskQuadraticData.getEndY()),
                    ex, ey);

            setShapeStyle(maskQuadratic);

            maskShapeChanged();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean moveMaskQuadraticAnchor(int index, DoublePoint p) {
        switch (index) {
            case 1:
                maskQuadraticData.setStartX(p.getX());
                maskQuadraticData.setStartY(p.getY());
                break;
            case 2:
                maskQuadraticData.setControlX(p.getX());
                maskQuadraticData.setControlY(p.getY());
                break;
            case 3:
                maskQuadraticData.setEndX(p.getX());
                maskQuadraticData.setEndY(p.getY());
                break;
            default:
                return false;
        }
        maskShapeDataChanged();
        return true;
    }

    public void clearMaskQuadratic() {
        if (imageView == null || maskPane == null || maskQuadratic == null) {
            return;
        }
        maskPane.getChildren().removeAll(maskQuadratic);
        maskQuadratic.setVisible(false);
        clearMaskAnchors();
    }

    public void clearMaskQuadraticData() {
        maskQuadraticData = null;
    }

    /*
        cubic curve
     */
    public boolean isMaskCubicShown() {
        return imageView != null && maskPane != null
                && maskCubic != null && maskCubic.isVisible()
                && maskCubicData != null;
    }

    public boolean showMaskCubic() {
        if (imageView == null || maskPane == null || maskCubic == null) {
            return false;
        }
        if (!maskPane.getChildren().contains(maskCubic)) {
            maskPane.getChildren().addAll(maskCubic);
        }
        maskCubic.setOpacity(1);
        maskCubic.setVisible(true);
        return drawMaskCubic();
    }

    public void setMaskCubicDefaultValues() {
        if (imageView == null || maskPane == null || maskCubic == null) {
            return;
        }
        double w = imageWidth();
        double h = imageHeight();
        maskCubicData = new DoubleCubic(w / 5, h * 3 / 5, w / 2 - 10, 20, w / 2 + 30, h - 35, w * 3 / 4, h * 4 / 5);
    }

    public boolean drawMaskCubic() {
        try {
            if (maskCubic == null || !maskCubic.isVisible()
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }
            if (maskCubicData == null) {
                setMaskCubicDefaultValues();
            }
            double layoutX = imageView.getLayoutX();
            double layoutY = imageView.getLayoutY();
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
            double sx = maskCubicData.getStartX() * xRatio;
            double sy = maskCubicData.getStartY() * yRatio;
            double cx1 = maskCubicData.getControlX1() * xRatio;
            double cy1 = maskCubicData.getControlY1() * yRatio;
            double cx2 = maskCubicData.getControlX2() * xRatio;
            double cy2 = maskCubicData.getControlY2() * yRatio;
            double ex = maskCubicData.getEndX() * xRatio;
            double ey = maskCubicData.getEndY() * yRatio;
            maskCubic.setLayoutX(layoutX);
            maskCubic.setLayoutY(layoutY);
            maskCubic.setStartX(sx);
            maskCubic.setStartY(sy);
            maskCubic.setControlX1(cx1);
            maskCubic.setControlY1(cy1);
            maskCubic.setControlX2(cx2);
            maskCubic.setControlY2(cy2);
            maskCubic.setEndX(ex);
            maskCubic.setEndY(ey);

            clearMaskAnchors();
            addMaskAnchor(1, message("StartPoint"), message("StartPoint"),
                    new DoublePoint(maskCubicData.getStartX(), maskCubicData.getStartY()),
                    sx, sy);
            addMaskAnchor(2, message("ControlPoint1"), message("ControlPoint1"),
                    new DoublePoint(maskCubicData.getControlX1(), maskCubicData.getControlY1()),
                    cx1, cy1);
            addMaskAnchor(3, message("ControlPoint2"), message("ControlPoint2"),
                    new DoublePoint(maskCubicData.getControlX2(), maskCubicData.getControlY2()),
                    cx2, cy2);
            addMaskAnchor(4, message("EndPoint"), message("EndPoint"),
                    new DoublePoint(maskCubicData.getEndX(), maskCubicData.getEndY()),
                    ex, ey);

            setShapeStyle(maskCubic);

            maskShapeChanged();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean moveMaskCubicAnchor(int index, DoublePoint p) {
        switch (index) {
            case 1:
                maskCubicData.setStartX(p.getX());
                maskCubicData.setStartY(p.getY());
                break;
            case 2:
                maskCubicData.setControlX1(p.getX());
                maskCubicData.setControlY1(p.getY());
                break;
            case 3:
                maskCubicData.setControlX2(p.getX());
                maskCubicData.setControlY2(p.getY());
                break;
            case 4:
                maskCubicData.setEndX(p.getX());
                maskCubicData.setEndY(p.getY());
                break;
            default:
                return false;
        }
        maskShapeDataChanged();
        return true;
    }

    public void clearMaskCubic() {
        if (imageView == null || maskPane == null || maskCubic == null) {
            return;
        }
        maskPane.getChildren().removeAll(maskCubic);
        maskCubic.setVisible(false);
        clearMaskAnchors();
    }

    public void clearMaskCubicData() {
        maskCubicData = null;
    }

    /*
        arc
     */
    public boolean isMaskArcShown() {
        return imageView != null && maskPane != null
                && maskArc != null && maskArc.isVisible()
                && maskArcData != null;
    }

    public boolean showMaskArc() {
        if (imageView == null || maskPane == null || maskArc == null) {
            return false;
        }
        if (!maskPane.getChildren().contains(maskArc)) {
            maskPane.getChildren().addAll(maskArc);
        }
        maskArc.setOpacity(1);
        maskArc.setVisible(true);
        return drawMaskArc();
    }

    public void setMaskArcDefaultValues() {
        if (imageView == null || maskPane == null || maskArc == null) {
            return;
        }
        double w = imageWidth();
        double h = imageHeight();
        maskArcData = DoubleArc.rect(w / 5, h / 5, w / 2, h / 2, 45, 270, Arc2D.OPEN);
    }

    public boolean drawMaskArc() {
        try {
            if (maskArc == null || !maskArc.isVisible()
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }
            if (maskArcData == null) {
                setMaskArcDefaultValues();
            }
            double layoutX = imageView.getLayoutX();
            double layoutY = imageView.getLayoutY();
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
            double cx = maskArcData.getCenterX() * xRatio;
            double cy = maskArcData.getCenterY() * yRatio;
            double rx = maskArcData.getRadiusX() * xRatio;
            double ry = maskArcData.getRadiusY() * yRatio;
            double sa = maskArcData.getStartAngle() * xRatio;
            double ea = maskArcData.getExtentAngle() * yRatio;
            maskArc.setLayoutX(layoutX);
            maskArc.setLayoutY(layoutY);
            maskArc.setCenterX(cx);
            maskArc.setCenterY(cy);
            maskArc.setRadiusX(rx);
            maskArc.setRadiusY(ry);
            maskArc.setStartAngle(sa);
            maskArc.setLength(ea);
            setShapeStyle(maskArc);

            clearMaskAnchors();
            addMaskAnchor(1, message("TopCenter"), message("TopCenter"),
                    new DoublePoint(maskArcData.getCenterX(), maskArcData.getCenterY() - maskArcData.getRadiusY()),
                    cx, cy - ry, Cursor.N_RESIZE);
            addMaskAnchor(2, message("LeftCenter"), message("LeftCenter"),
                    new DoublePoint(maskArcData.getCenterX() - maskArcData.getRadiusX(), maskArcData.getCenterY()),
                    cx - rx, cy, Cursor.W_RESIZE);
            addMaskAnchor(3, message("RightCenter"), message("RightCenter"),
                    new DoublePoint(maskArcData.getCenterX() + maskArcData.getRadiusX(), maskArcData.getCenterY()),
                    cx + rx, cy, Cursor.E_RESIZE);
            addMaskAnchor(4, message("BottomCenter"), message("BottomCenter"),
                    new DoublePoint(maskArcData.getCenterX(), maskArcData.getCenterY() + maskArcData.getRadiusY()),
                    cx, cy + ry, Cursor.S_RESIZE);

            maskShapeChanged();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean moveMaskArcAnchor(int index, DoublePoint p) {
        double x = p.getX();
        double y = p.getY();
        double x1 = maskArcData.getX1();
        double y1 = maskArcData.getY1();
        double x2 = maskArcData.getX2();
        double y2 = maskArcData.getY2();
        double startAngle = maskArcData.getStartAngle();
        double extentAngle = maskArcData.getExtentAngle();
        int type = maskArcData.getType();
        switch (index) {
            case 1:
                maskArcData = DoubleArc.rect(x1, y, x2, y2, startAngle, extentAngle, type);
                break;
            case 2:
                maskArcData = DoubleArc.rect(x, y1, x2, y2, startAngle, extentAngle, type);
                break;
            case 3:
                maskArcData = DoubleArc.rect(x1, y1, x, y2, startAngle, extentAngle, type);
                break;
            case 4:
                maskArcData = DoubleArc.rect(x1, y1, x2, y, startAngle, extentAngle, type);
                break;
            default:
                return false;
        }
        maskShapeDataChanged();
        return true;
    }

    public void clearMaskArc() {
        if (imageView == null || maskPane == null || maskArc == null) {
            return;
        }
        maskPane.getChildren().removeAll(maskArc);
        maskArc.setVisible(false);
        clearMaskAnchors();
    }

    public void clearMaskArcData() {
        maskArcData = null;
    }

    /*
        path
     */
    public boolean isMaskPathShown() {
        return imageView != null && maskPane != null
                && maskSVGPath != null && maskSVGPath.isVisible()
                && maskPathData != null;
    }

    public boolean showMaskPath() {
        if (imageView == null || maskPane == null || maskSVGPath == null) {
            return false;
        }
        if (!maskPane.getChildren().contains(maskSVGPath)) {
            maskPane.getChildren().addAll(maskSVGPath);
        }
        maskSVGPath.setOpacity(1);
        maskSVGPath.setVisible(true);
        return drawMaskPath();
    }

    public void setMaskPathDefaultValues() {
        if (imageView == null || maskPane == null || maskSVGPath == null) {
            return;
        }
        maskPathData = new DoublePath();
    }

    public void setMaskPathExample() {
        if (imageView == null || maskPane == null || maskSVGPath == null) {
            return;
        }
        double w = imageWidth();
        double h = imageHeight();
        int r = (int) (Math.min(w, h) / 8);
        String s = "M " + (int) (w / 2 - r * 2) + "," + (int) (h / 2 - r) + "\n"
                + "A " + r + "," + r + " 0,1,1 " + (int) (w / 2) + "," + (int) (h / 2 - r) + "\n"
                + "A " + r + "," + r + " 0,1,1 " + (int) (w / 2 + r * 2) + "," + (int) (h / 2 - r) + "\n"
                + "Q " + (int) (w / 2 + 2 * r) + "," + (int) (h / 2 + r) + "  " + (int) (w / 2) + "," + (int) (h / 2 + 2 * r) + "\n"
                + "Q " + (int) (w / 2 - 2 * r) + "," + (int) (h / 2 + r) + "  " + (int) (w / 2 - r * 2) + "," + (int) (h / 2 - r);
        maskPathData = new DoublePath(this, s);
    }

    public boolean drawMaskPath() {
        try {
            if (imageView == null || maskPane == null || maskSVGPath == null) {
                return false;
            }
            if (maskPathData == null) {
                setMaskPathDefaultValues();
            }
            clearMaskAnchors();
            double layoutX = imageView.getLayoutX();
            double layoutY = imageView.getLayoutY();
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
            DoublePath path = DoublePath.scale(maskPathData, xRatio, yRatio);
            maskSVGPath.setContent(path.getContent());
            maskSVGPath.setLayoutX(layoutX);
            maskSVGPath.setLayoutY(layoutY);
            setShapeStyle(maskSVGPath);

            List<DoublePathSegment> segments = maskPathData.getSegments();
            if (segments != null) {
                DoublePoint p;
                for (int i = 0; i < segments.size(); i++) {
                    DoublePathSegment seg = segments.get(i);
                    p = seg.getControlPoint1();
                    String prefix = (i + 1) + seg.getCommand().trim();
                    if (p != null) {
                        String info = seg.text() + "\n" + message("ControlPoint1");
                        addMaskAnchor(i, prefix + ".c1", info, p, p.getX() * xRatio, p.getY() * yRatio);
                    }
                    p = seg.getControlPoint2();
                    if (p != null) {
                        String info = seg.text() + "\n" + message("ControlPoint2");
                        addMaskAnchor(i, prefix + ".c2", info, p, p.getX() * xRatio, p.getY() * yRatio);
                    }
                    p = seg.getEndPoint();
                    if (p != null) {
                        String info = seg.text() + "\n" + message("EndPoint");
                        addMaskAnchor(i, prefix + ".e", info, p, p.getX() * xRatio, p.getY() * yRatio);
                    }

                }
            }

            maskShapeChanged();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean moveMaskPathAnchor(int index, String name, DoublePoint p) {
        DoublePathSegment seg = maskPathData.getSegments().get(index);

        DoublePoint sp = seg.getStartPoint();
        if (name.endsWith(".c1")) {
            seg.setControlPoint1(p)
                    .setControlPoint1Rel(new DoublePoint(p.getX() - sp.getX(), p.getY() - sp.getY()));

        } else if (name.endsWith(".c2")) {
            seg.setControlPoint2(p)
                    .setControlPoint2Rel(new DoublePoint(p.getX() - sp.getX(), p.getY() - sp.getY()));

        } else if (name.endsWith(".e")) {
            seg.setEndPoint(p)
                    .setEndPointRel(new DoublePoint(p.getX() - sp.getX(), p.getY() - sp.getY()));

        } else {
            return false;
        }

        maskPathData.replace(index, seg);
        maskPathData.parseContent(this, maskPathData.getContent()); // to fix values
        maskShapeDataChanged();
        return true;
    }

    public void clearMaskPath() {
        if (maskPane == null || maskSVGPath == null) {
            return;
        }
        maskPane.getChildren().remove(maskSVGPath);
        maskSVGPath.setContent("");
        maskSVGPath.setVisible(false);
    }

    public void clearMaskPathData() {
        maskPathData = null;
    }

}
