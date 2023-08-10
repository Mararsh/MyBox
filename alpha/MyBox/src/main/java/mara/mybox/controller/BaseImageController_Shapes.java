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
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.PixelReader;
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
import javafx.stage.Window;
import mara.mybox.data.DoubleArc;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleCubic;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoubleLine;
import mara.mybox.data.DoublePath;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoublePolyline;
import mara.mybox.data.DoublePolylines;
import mara.mybox.data.DoubleQuadratic;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.data.ShapeStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fximage.ImageViewTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-10
 * @License Apache License Version 2.0
 */
public abstract class BaseImageController_Shapes extends BaseImageController_ImageView {

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
    public boolean maskControlDragged, showAnchors;
    protected Polyline currentPolyline;
    protected List<Polyline> maskPolylines;
    protected DoublePoint lastPoint;

    protected ShapeStyle shapeStyle = null;
    public SimpleBooleanProperty maskShapeChanged = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty maskShapeDataChanged = new SimpleBooleanProperty(false);

    @FXML
    protected Rectangle maskRectangle, maskHandlerLeftCenter, maskHandlerTopLeft, maskHandlerTopCenter, maskHandlerTopRight,
            maskHandlerBottomLeft, maskHandlerBottomCenter, maskHandlerBottomRight, maskHandlerRightCenter;
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

    public void initMaskPane() {
        if (maskPane == null) {
            return;
        }
        try {
            showAnchors = true;
            maskControlDragged = false;

            maskPane.prefWidthProperty().bind(imageView.fitWidthProperty());
            maskPane.prefHeightProperty().bind(imageView.fitHeightProperty());

            if (maskPane.getOnMouseClicked() == null) {
                maskPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        paneClicked(event);
                    }
                });
            }

            if (maskPane.getOnMouseMoved() == null) {
                maskPane.setOnMouseMoved(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        showXY(event);
                    }
                });
            }

            if (maskPane.getOnMouseDragged() == null) {
                maskPane.setOnMouseDragged(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        showXY(event);
                    }
                });
            }

            if (maskPane.getOnMousePressed() == null) {
                maskPane.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        showXY(event);
                    }
                });
            }

            if (maskPane.getOnMouseReleased() == null) {
                maskPane.setOnMouseReleased(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        showXY(event);
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }


    /*
        image
     */
    @Override
    public void viewSizeChanged(double change) {
        if (isSettingValues || change < sizeChangeAware
                || imageView == null || imageView.getImage() == null) {
            return;
        }
        refinePane();
        redrawMaskShapes();
    }

    public void setImageChanged(boolean imageChanged) {
        try {
            this.imageChanged = imageChanged;
            updateLabelsTitle();
            if (imageChanged) {
                redrawMaskShapes();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected String moreDisplayInfo() {
        if (maskRectangle != null && maskRectangle.isVisible() && maskRectangleData != null) {
            return Languages.message("SelectedSize") + ":"
                    + (int) (maskRectangleData.getWidth() / widthRatio()) + "x"
                    + (int) (maskRectangleData.getHeight() / heightRatio());
        } else {
            return "";
        }
    }

    /*
        values
     */
    public Color strokeColor() {
        try {
            return Color.web(UserConfig.getString("StrokeColor", ShapeStyle.DefaultStrokeColor));
        } catch (Exception e) {
            return Color.web(ShapeStyle.DefaultStrokeColor);
        }
    }

    public float strokeWidth() {
        float v = UserConfig.getFloat("StrokeWidth", 2);
        if (v < 0) {
            v = 2;
        }
        return v;
    }

    public Color anchorColor() {
        try {
            return shapeStyle == null
                    ? Color.web(UserConfig.getString("AnchorColor", ShapeStyle.DefaultAnchorColor))
                    : shapeStyle.getAnchorColor();
        } catch (Exception e) {
            return Color.web(ShapeStyle.DefaultAnchorColor);
        }
    }

    public float anchorSize() {
        float v = shapeStyle == null ? UserConfig.getFloat("AnchorSize", 10) : shapeStyle.getAnchorSize();
        if (v < 0) {
            v = 10;
        }
        return v;
    }

    public double viewXRatio() {
        return viewWidth() / imageWidth();
    }

    public double viewYRatio() {
        return viewHeight() / imageHeight();
    }

    public double imageXRatio() {
        return imageWidth() / viewWidth();
    }

    public double imageYRatio() {
        return imageHeight() / viewHeight();
    }

    public double maskEventX(MouseEvent event) {
        return event.getX() * imageXRatio();
    }

    public double maskEventY(MouseEvent event) {
        return event.getY() * imageYRatio();
    }

    public double imageOffsetX(MouseEvent event) {
        return (event.getX() - mouseX) * imageXRatio();
    }

    public double imageOffsetY(MouseEvent event) {
        return (event.getY() - mouseY) * imageYRatio();
    }

    /*
        event
     */
    @FXML
    public void paneClicked(MouseEvent event) {
        if (imageView.getImage() == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
        paneClicked(event, p);
        event.consume();
    }

    public void paneClicked(MouseEvent event, DoublePoint p) {
    }

    @FXML
    public void imageClicked(MouseEvent event) {
//        MyBoxLog.debug("imageClicked");
    }

    @FXML
    public DoublePoint showXY(MouseEvent event) {
        if (xyText == null || !xyText.isVisible()) {
            return null;
        }
        if (!isPickingColor && !UserConfig.getBoolean("ImagePopCooridnate", false)) {
            xyText.setText("");
            return null;
        }
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
        showXY(event, p);
        return p;
    }

    public DoublePoint showXY(MouseEvent event, DoublePoint p) {
        if (p == null) {
            xyText.setText("");
            return null;
        }
        PixelReader pixelReader = imageView.getImage().getPixelReader();
        Color color = pixelReader.getColor((int) p.getX(), (int) p.getY());
        String s = (int) Math.round(p.getX() / widthRatio()) + ","
                + (int) Math.round(p.getY() / heightRatio()) + "\n"
                + FxColorTools.colorDisplaySimple(color);
        if (isPickingColor) {
            if (this instanceof ImageManufactureScopeController_Base) {
                s = message("PickingColorsForScope") + "\n" + s;
            } else {
                s = message("PickingColorsNow") + "\n" + s;
            }
        }
        xyText.setText(s);
        xyText.setX(event.getX() + 10);
        xyText.setY(event.getY());
        return p;
    }

    public void drawMaskRulers() {
        drawMaskGrid();
        drawMaskRulerX();
        drawMaskRulerY();
    }

    private void drawMaskRulerX() {
        if (maskPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        clearMaskRulerX();
        if (UserConfig.getBoolean("ImageRulerXY", false)) {
            Color strokeColor = Color.web(UserConfig.getString("RulerColor", "#FF0000"));
            double imageWidth = imageWidth() / widthRatio();
            double ratio = viewWidth() / imageWidth;
            int step = getRulerStep(imageWidth);
            for (int i = step; i < imageWidth; i += step) {
                double x = i * ratio;
                Line line = new Line(x, 0, x, 8);
                line.setId("MaskRulerX" + i);
                line.setStroke(strokeColor);
                line.setStrokeWidth(1);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                maskPane.getChildren().add(line);
            }
            String style = " -fx-font-size: 0.8em; ";
            int step10 = step * 10;
            for (int i = step10; i < imageWidth; i += step10) {
                double x = i * ratio;
                Line line = new Line(x, 0, x, 15);
                line.setId("MaskRulerX" + i);
                line.setStroke(strokeColor);
                line.setStrokeWidth(2);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                maskPane.getChildren().add(line);
                Text text = new Text(i + " ");
                text.setStyle(style);
                text.setFill(strokeColor);
                text.setLayoutX(imageView.getLayoutX() + x - 10);
                text.setLayoutY(imageView.getLayoutY() + 30);
                text.setId("MaskRulerXtext" + i);
                maskPane.getChildren().add(text);
            }
        }
    }

    private void clearMaskRulerX() {
        if (maskPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(maskPane.getChildren());
        for (Node node : nodes) {
            if (node.getId() != null && node.getId().startsWith("MaskRulerX")) {
                maskPane.getChildren().remove(node);
                node = null;
            }
        }
    }

    private void drawMaskRulerY() {
        if (maskPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        clearMaskRulerY();
        if (UserConfig.getBoolean("ImageRulerXY", false)) {
            Color strokeColor = Color.web(UserConfig.getString("RulerColor", "#FF0000"));
            double imageHeight = imageHeight() / heightRatio();
            double ratio = viewHeight() / imageHeight;
            int step = getRulerStep(imageHeight);
            for (int j = step; j < imageHeight; j += step) {
                double y = j * ratio;
                Line line = new Line(0, y, 8, y);
                line.setId("MaskRulerY" + j);
                line.setStroke(strokeColor);
                line.setStrokeWidth(1);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                maskPane.getChildren().add(line);
            }
            String style = " -fx-font-size: 0.8em; ";
            int step10 = step * 10;
            for (int j = step10; j < imageHeight; j += step10) {
                double y = j * ratio;
                Line line = new Line(0, y, 15, y);
                line.setId("MaskRulerY" + j);
                line.setStroke(strokeColor);
                line.setStrokeWidth(2);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                Text text = new Text(j + " ");
                text.setStyle(style);
                text.setFill(strokeColor);
                text.setLayoutX(imageView.getLayoutX() + 25);
                text.setLayoutY(imageView.getLayoutY() + y + 8);
                text.setId("MaskRulerYtext" + j);
                maskPane.getChildren().addAll(line, text);
            }
        }
    }

    private void clearMaskRulerY() {
        if (maskPane == null || imageView.getImage() == null) {
            return;
        }
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(maskPane.getChildren());
        for (Node node : nodes) {
            if (node.getId() != null && node.getId().startsWith("MaskRulerY")) {
                maskPane.getChildren().remove(node);
                node = null;
            }
        }
    }

    public void drawMaskGrid() {
        drawMaskGridX();
        drawMaskGridY();
    }

    private void drawMaskGridX() {
        if (maskPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        clearMaskGridX();
        if (UserConfig.getBoolean("ImageGridLines", false)) {
            Color lineColor = Color.web(UserConfig.getString("GridLinesColor", Color.LIGHTGRAY.toString()));
            int lineWidth = UserConfig.getInt("GridLinesWidth", 1);
            lineWidth = lineWidth <= 0 ? 1 : lineWidth;
            double imageWidth = imageWidth() / widthRatio();
            double imageHeight = imageHeight() / heightRatio();
            double wratio = viewWidth() / imageWidth;
            double hratio = viewHeight() / imageHeight;
            int istep = getRulerStep(imageWidth);
            int interval = UserConfig.getInt("GridLinesInterval", -1);
            interval = interval <= 0 ? istep : interval;
            float opacity = 0.1f;
            try {
                opacity = Float.parseFloat(UserConfig.getString("GridLinesOpacity", "0.1"));
            } catch (Exception e) {
            }
            for (int i = interval; i < imageWidth; i += interval) {
                double x = i * wratio;
                Line line = new Line(x, 0, x, imageHeight * hratio);
                line.setId("GridLinesX" + i);
                line.setStroke(lineColor);
                line.setStrokeWidth(lineWidth);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                line.setOpacity(opacity);
                maskPane.getChildren().add(line);
            }
            int step10 = istep * 10;
            String style = " -fx-font-size: 0.8em; ";
            for (int i = step10; i < imageWidth; i += step10) {
                double x = i * wratio;
                Line line = new Line(x, 0, x, imageHeight * hratio);
                line.setId("GridLinesX" + i);
                line.setStroke(lineColor);
                line.setStrokeWidth(lineWidth + 1);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                line.setOpacity(opacity);
                maskPane.getChildren().add(line);
                if (!UserConfig.getBoolean("ImageRulerXY", false)) {
                    Text text = new Text(i + " ");
                    text.setStyle(style);
                    text.setFill(lineColor);
                    text.setLayoutX(imageView.getLayoutX() + x - 10);
                    text.setLayoutY(imageView.getLayoutY() + 15);
                    text.setId("GridLinesXtext" + i);
                    maskPane.getChildren().add(text);
                }
            }
        }
    }

    private void drawMaskGridY() {
        if (maskPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        clearMaskGridY();
        if (UserConfig.getBoolean("ImageGridLines", false)) {
            Color lineColor = Color.web(UserConfig.getString("GridLinesColor", Color.LIGHTGRAY.toString()));
            int lineWidth = UserConfig.getInt("GridLinesWidth", 1);
            double imageWidth = imageWidth() / widthRatio();
            double imageHeight = imageHeight() / heightRatio();
            double wratio = viewWidth() / imageWidth;
            double hratio = viewHeight() / imageHeight;
            int istep = getRulerStep(imageHeight);
            int interval = UserConfig.getInt("GridLinesInterval", -1);
            interval = interval <= 0 ? istep : interval;
            double w = imageWidth * wratio;
            float opacity = 0.1f;
            try {
                opacity = Float.parseFloat(UserConfig.getString("GridLinesOpacity", "0.1"));
            } catch (Exception e) {
            }
            for (int j = interval; j < imageHeight; j += interval) {
                double y = j * hratio;
                Line line = new Line(0, y, w, y);
                line.setId("GridLinesY" + j);
                line.setStroke(lineColor);
                line.setStrokeWidth(lineWidth);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                line.setOpacity(opacity);
                maskPane.getChildren().add(line);
            }
            String style = " -fx-font-size: 0.8em; ";
            int step10 = istep * 10;
            for (int j = step10; j < imageHeight; j += step10) {
                double y = j * hratio;
                Line line = new Line(0, y, w, y);
                line.setId("GridLinesY" + j);
                line.setStroke(lineColor);
                line.setStrokeWidth(lineWidth + 2);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                line.setOpacity(opacity);
                maskPane.getChildren().add(line);
                if (!UserConfig.getBoolean("ImageRulerXY", false)) {
                    Text text = new Text(j + " ");
                    text.setStyle(style);
                    text.setFill(lineColor);
                    text.setLayoutX(imageView.getLayoutX());
                    text.setLayoutY(imageView.getLayoutY() + y + 8);
                    text.setId("GridLinesYtext" + j);
                    maskPane.getChildren().add(text);
                }
            }
        }
    }

    private void clearMaskGridX() {
        if (maskPane == null || imageView.getImage() == null) {
            return;
        }
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(maskPane.getChildren());
        for (Node node : nodes) {
            if (node.getId() != null && node.getId().startsWith("GridLinesX")) {
                maskPane.getChildren().remove(node);
                node = null;
            }
        }
    }

    private void clearMaskGridY() {
        if (maskPane == null || imageView.getImage() == null) {
            return;
        }
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(maskPane.getChildren());
        for (Node node : nodes) {
            if (node.getId() != null && node.getId().startsWith("GridLinesY")) {
                maskPane.getChildren().remove(node);
                node = null;
            }
        }
    }

    protected void checkCoordinate() {
        if (xyText != null) {
            xyText.setText("");
            xyText.setFill(strokeColor());
        }
    }

    /*
        all shapes
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
    public boolean redrawMaskShapes() {
        try {
            drawMaskRulers();
            checkCoordinate();
            return drawMaskShape();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void setMaskShapesStyle() {
        drawMaskRulers();
        checkCoordinate();
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
        if (shape == null || !shape.isVisible()) {
            return;
        }
        Color strokeColor = strokeColor();
        if (strokeColor.equals(Color.TRANSPARENT)) {
            // Have not found how to make line as transparent. For display only.
            strokeColor = Color.WHITE;
        }
        shape.setStroke(strokeColor);
        double strokeWidth = strokeWidth();
        shape.setStrokeWidth(strokeWidth);

        shape.setFill(Color.TRANSPARENT);

        shape.getStrokeDashArray().clear();
        shape.getStrokeDashArray().addAll(strokeWidth, strokeWidth * 3);

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
            shapeStyle = null;
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
        if (!isSettingValues && maskShapeDataChanged != null) {
            maskShapeDataChanged.set(!maskShapeDataChanged.get());
        }
    }

    public void hideMaskShape() {
        try {
            if (isMaskPolylinesShown()) {
                for (Polyline line : maskPolylines) {
                    line.setOpacity(0);
                }
                if (currentPolyline != null) {
                    maskPane.getChildren().remove(currentPolyline);
                    currentPolyline = null;
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

    public void controlPressed(MouseEvent event) {
        scrollPane.setPannable(false);
        mouseX = event.getX();
        mouseY = event.getY();
    }

    /* 
        anchor
        index: 0-based
     */
    public void addMaskAnchor(int index, DoublePoint p, double x, double y) {
        addMaskAnchor(index, (index + 1) + "", message("Point") + " " + (index + 1), p, x, y);
    }

    public void addMaskAnchor(int index, String name, String title, DoublePoint p, double x, double y) {
        try {
            Text text = new Text(name);
            text.setLayoutX(imageView.getLayoutX());
            text.setLayoutY(imageView.getLayoutY());
            text.setX(x);
            text.setY(y);
            text.setId("MaskAnchor" + index);
            text.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    controlPressed(event);
                    maskControlDragged = true;
                }
            });
            text.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    maskControlDragged = true;
                }
            });
            text.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    scrollPane.setPannable(true);
                    if (isPickingColor) {
                        return;
                    }
                    double nx = maskEventX(event);
                    double ny = maskEventY(event);
                    if (DoubleShape.changed(nx - p.getX(), ny - p.getY())) {
                        moveMaskAnchor(index, new DoublePoint(nx, ny));
                    }
                }
            });
            text.hoverProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        if (UserConfig.getBoolean("ImageShapeControlPopMenu", true)) {
                            popNodeMenu(text, maskAnchorMenu(text, index, title, p));
                        }
                        if (isPickingColor) {
                            text.setCursor(Cursor.HAND);
                        } else {
                            text.setCursor(Cursor.MOVE);
                        }
                    }
                }
            });
            setTextStyle(text);
            maskPane.getChildren().add(text);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setTextStyle(Text text) {
        if (text == null) {
            return;
        }
        if (showAnchors) {
            text.setFill(anchorColor());
            text.setFont(new Font(anchorSize()));
        } else {
            text.setVisible(false);
        }
    }

    protected List<MenuItem> maskAnchorMenu(Text text, int index, String title, DoublePoint p) {
        try {
            if (text == null) {
                return null;
            }
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(title);
            menu.setStyle("-fx-text-fill: #2e598a;");
            items.add(menu);
            menu = new MenuItem(StringTools.menuPrefix(p.text(2)));
            menu.setStyle("-fx-text-fill: #2e598a;");
            items.add(menu);
            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("Edit"), StyleTools.getIconImageView("iconEdit.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                PointInputController inputController = PointInputController.open(this, title, p);
                inputController.getNotify().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                        moveMaskAnchor(index, inputController.picked);
                        inputController.close();
                    }
                });
            });
            items.add(menu);

            if (!isMaskQuadraticShown() && !isMaskCubicShown()) {
                menu = new MenuItem(message("Delete"), StyleTools.getIconImageView("iconDelete.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    deleteMaskAnchor(index);
                });
                items.add(menu);
            }

            DoubleShape shape = currentMaskShapeData();
            if (shape != null) {
                menu = new MenuItem(message("MoveShapeCenterTo"), StyleTools.getIconImageView("iconMove.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    PointInputController inputController = PointInputController.open(this,
                            message("MoveShapeCenterTo"), DoubleShape.getCenter(shape));
                    inputController.getNotify().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                            DoubleShape.translateAbs(shape, inputController.picked.getX(), inputController.picked.getY());
                            drawMaskShape();
                            maskShapeDataChanged();
                            inputController.close();
                        }
                    });
                });
                items.add(menu);
            }

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public void moveMaskAnchor(int index, DoublePoint p) {
        if (maskPolyline != null && maskPolyline.isVisible() && maskPolylineData != null) {
            maskPolylineData.set(index, p);
            drawMaskPolyline();
            maskShapeDataChanged();

        } else if (maskPolygon != null && maskPolygon.isVisible() && maskPolygonData != null) {
            maskPolygonData.set(index, p);
            drawMaskPolygon();
            maskShapeDataChanged();

        } else if (isMaskQuadraticShown()) {
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
                    return;
            }
            drawMaskQuadratic();
            maskShapeDataChanged();

        } else if (isMaskCubicShown()) {
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
                    return;
            }
            drawMaskCubic();
            maskShapeDataChanged();

        }

    }

    public void deleteMaskAnchor(int index) {
        if (maskPolyline != null && maskPolyline.isVisible() && maskPolylineData != null) {
            maskPolylineData.remove(index);
            drawMaskPolyline();
            maskShapeDataChanged();

        } else if (maskPolygon != null && maskPolygon.isVisible() && maskPolygonData != null) {
            maskPolygonData.remove(index);
            drawMaskPolygon();
            maskShapeDataChanged();

        }
    }

    public void setMaskAnchorsStyle() {
        try {
            if (maskPane == null) {
                return;
            }
            Color anchorColor = anchorColor();
            float anchorSize = anchorSize();
            for (Node node : maskPane.getChildren()) {
                if (node == null || node.getId() == null) {
                    continue;
                }
                if ((node instanceof Rectangle) && node.getId().startsWith("maskHandler")) {
                    if (showAnchors) {
                        Rectangle rect = (Rectangle) node;
                        rect.setStrokeWidth(0);
                        rect.setFill(anchorColor);
                        rect.setWidth(anchorSize);
                        rect.setHeight(anchorSize);
                        node.setVisible(true);
                    } else {
                        node.setVisible(false);
                    }

                } else if ((node instanceof Text) && node.getId().startsWith("MaskAnchor")) {
                    if (showAnchors) {
                        Text text = (Text) node;
                        text.setFill(anchorColor);
                        text.setFont(new Font(anchorSize));
                        node.setVisible(true);
                    } else {
                        node.setVisible(false);
                    }
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
            if (node == null || !(node instanceof Text) || node.getId() == null) {
                continue;
            }
            if (node.getId().startsWith("MaskAnchor")) {
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
                if (maskHandlerLeftCenter != null && !maskPane.getChildren().contains(maskHandlerLeftCenter)) {
                    maskPane.getChildren().addAll(maskHandlerLeftCenter, maskHandlerRightCenter,
                            maskHandlerTopLeft, maskHandlerTopCenter, maskHandlerTopRight,
                            maskHandlerBottomLeft, maskHandlerBottomCenter, maskHandlerBottomRight);
                }
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
            double x1 = maskRectangleData.getX() * xRatio;
            double y1 = maskRectangleData.getY() * yRatio;
            double x2 = maskRectangleData.getMaxX() * xRatio;
            double y2 = maskRectangleData.getMaxY() * yRatio;
            maskRectangle.setLayoutX(layoutX);
            maskRectangle.setLayoutY(layoutY);
            maskRectangle.setX(x1);
            maskRectangle.setY(y1);
            maskRectangle.setWidth(x2 - x1);
            maskRectangle.setHeight(y2 - y1);

            setShapeStyle(maskRectangle);

            double anchorHW = anchorSize() * 0.5;
            double cx = (x2 + x1) * 0.5;
            double cy = (y2 + y1) * 0.5;

            maskHandlerTopLeft.setLayoutX(layoutX);
            maskHandlerTopLeft.setLayoutY(layoutY);
            maskHandlerTopLeft.setX(x1 - anchorHW);
            maskHandlerTopLeft.setY(y1 - anchorHW);

            maskHandlerTopCenter.setLayoutX(layoutX);
            maskHandlerTopCenter.setLayoutY(layoutY);
            maskHandlerTopCenter.setX(cx - anchorHW);
            maskHandlerTopCenter.setY(y1 - anchorHW);

            maskHandlerTopRight.setLayoutX(layoutX);
            maskHandlerTopRight.setLayoutY(layoutY);
            maskHandlerTopRight.setX(x2 - anchorHW);
            maskHandlerTopRight.setY(y1 - anchorHW);

            maskHandlerBottomLeft.setLayoutX(layoutX);
            maskHandlerBottomLeft.setLayoutY(layoutY);
            maskHandlerBottomLeft.setX(x1 - anchorHW);
            maskHandlerBottomLeft.setY(y2 - anchorHW);

            maskHandlerBottomCenter.setLayoutX(layoutX);
            maskHandlerBottomCenter.setLayoutY(layoutY);
            maskHandlerBottomCenter.setX(cx - anchorHW);
            maskHandlerBottomCenter.setY(y2 - anchorHW);

            maskHandlerBottomRight.setLayoutX(layoutX);
            maskHandlerBottomRight.setLayoutY(layoutY);
            maskHandlerBottomRight.setX(x2 - anchorHW);
            maskHandlerBottomRight.setY(y2 - anchorHW);

            maskHandlerLeftCenter.setLayoutX(layoutX);
            maskHandlerLeftCenter.setLayoutY(layoutY);
            maskHandlerLeftCenter.setX(x1 - anchorHW);
            maskHandlerLeftCenter.setY(cy - anchorHW);

            maskHandlerRightCenter.setLayoutX(layoutX);
            maskHandlerRightCenter.setLayoutY(layoutY);
            maskHandlerRightCenter.setX(x2 - anchorHW);
            maskHandlerRightCenter.setY(cy - anchorHW);

            setMaskAnchorsStyle();

            maskShapeChanged();

            updateLabelsTitle();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void clearMaskRectangle() {
        try {
            if (imageView == null || maskPane == null || maskRectangle == null) {
                return;
            }
            maskPane.getChildren().removeAll(maskRectangle,
                    maskHandlerLeftCenter, maskHandlerRightCenter,
                    maskHandlerTopLeft, maskHandlerTopCenter, maskHandlerTopRight,
                    maskHandlerBottomLeft, maskHandlerBottomCenter, maskHandlerBottomRight);
            maskRectangle.setVisible(false);

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
            maskPane.getChildren().addAll(maskCircle,
                    maskHandlerLeftCenter, maskHandlerRightCenter,
                    maskHandlerTopCenter, maskHandlerBottomCenter);
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
            double r = maskCircleData.getRadius() * xRatio;
            double x = maskCircleData.getCenterX() * xRatio;
            double y = maskCircleData.getCenterY() * yRatio;
            maskCircle.setLayoutX(layoutX);
            maskCircle.setLayoutY(layoutY);
            maskCircle.setCenterX(x);
            maskCircle.setCenterY(y);
            maskCircle.setRadius(r);

            double anchorHW = anchorSize() * 0.5;
            maskHandlerTopCenter.setLayoutX(layoutX);
            maskHandlerTopCenter.setLayoutY(layoutY);
            maskHandlerTopCenter.setX(x - anchorHW);
            maskHandlerTopCenter.setY(y - r - anchorHW);

            maskHandlerBottomCenter.setLayoutX(layoutX);
            maskHandlerBottomCenter.setLayoutY(layoutY);
            maskHandlerBottomCenter.setX(x - anchorHW);
            maskHandlerBottomCenter.setY(y + r - anchorHW);

            maskHandlerLeftCenter.setLayoutX(layoutX);
            maskHandlerLeftCenter.setLayoutY(layoutY);
            maskHandlerLeftCenter.setX(x - r - anchorHW);
            maskHandlerLeftCenter.setY(y - anchorHW);

            maskHandlerRightCenter.setLayoutX(layoutX);
            maskHandlerRightCenter.setLayoutY(layoutY);
            maskHandlerRightCenter.setX(x + r - anchorHW);
            maskHandlerRightCenter.setY(y - anchorHW);

            setShapeStyle(maskCircle);
            setMaskAnchorsStyle();

            maskShapeChanged();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void clearMaskCircle() {
        if (imageView == null || maskPane == null || maskCircle == null) {
            return;
        }
        maskPane.getChildren().removeAll(maskCircle,
                maskHandlerLeftCenter, maskHandlerRightCenter,
                maskHandlerTopCenter, maskHandlerBottomCenter);
        maskCircle.setVisible(false);
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
            maskPane.getChildren().addAll(maskEllipse,
                    maskHandlerLeftCenter, maskHandlerRightCenter,
                    maskHandlerTopCenter, maskHandlerBottomCenter);
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
            double rx = maskEllipseData.getRadiusX() * xRatio;
            double ry = maskEllipseData.getRadiusY() * yRatio;
            double cx = maskEllipseData.getCenterX() * xRatio;
            double cy = maskEllipseData.getCenterY() * yRatio;
            maskEllipse.setLayoutX(layoutX);
            maskEllipse.setLayoutY(layoutY);
            maskEllipse.setCenterX(cx);
            maskEllipse.setCenterY(cy);
            maskEllipse.setRadiusX(rx);
            maskEllipse.setRadiusY(ry);

            double anchorHW = anchorSize() * 0.5;
            maskHandlerTopCenter.setLayoutX(layoutX);
            maskHandlerTopCenter.setLayoutY(layoutY);
            maskHandlerTopCenter.setX(cx - anchorHW);
            maskHandlerTopCenter.setY(cy - ry - anchorHW);

            maskHandlerBottomCenter.setLayoutX(layoutX);
            maskHandlerBottomCenter.setLayoutY(layoutY);
            maskHandlerBottomCenter.setX(cx - anchorHW);
            maskHandlerBottomCenter.setY(cy + ry - anchorHW);

            maskHandlerLeftCenter.setLayoutX(layoutX);
            maskHandlerLeftCenter.setLayoutY(layoutY);
            maskHandlerLeftCenter.setX(cx - rx - anchorHW);
            maskHandlerLeftCenter.setY(cy - anchorHW);

            maskHandlerRightCenter.setLayoutX(layoutX);
            maskHandlerRightCenter.setLayoutY(layoutY);
            maskHandlerRightCenter.setX(cx + rx - anchorHW);
            maskHandlerRightCenter.setY(cy - anchorHW);

            setShapeStyle(maskEllipse);
            setMaskAnchorsStyle();

            maskShapeChanged();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }

    }

    public void clearMaskEllipse() {
        if (imageView == null || maskPane == null || maskEllipse == null) {
            return;
        }
        maskPane.getChildren().removeAll(maskEllipse,
                maskHandlerLeftCenter, maskHandlerRightCenter,
                maskHandlerTopCenter, maskHandlerBottomCenter);
        maskEllipse.setVisible(false);
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
            maskPane.getChildren().addAll(maskLine,
                    maskHandlerTopLeft, maskHandlerBottomRight);
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

            double anchorHW = anchorSize() * 0.5;
            maskHandlerTopLeft.setLayoutX(layoutX);
            maskHandlerTopLeft.setLayoutY(layoutY);
            maskHandlerTopLeft.setX(startX - anchorHW);
            maskHandlerTopLeft.setY(startY - anchorHW);

            maskHandlerBottomRight.setLayoutX(layoutX);
            maskHandlerBottomRight.setLayoutY(layoutY);
            maskHandlerBottomRight.setX(endX - anchorHW);
            maskHandlerBottomRight.setY(endY - anchorHW);

            setShapeStyle(maskLine);
            setMaskAnchorsStyle();

            maskShapeChanged();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void clearMaskLine() {
        if (imageView == null || maskPane == null || maskLine == null) {
            return;
        }
        maskPane.getChildren().removeAll(maskLine,
                maskHandlerTopLeft, maskHandlerBottomRight);
        maskLine.setVisible(false);

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
        double w = imageWidth();
        double h = imageHeight();
        maskPolylineData = new DoublePolyline();
        maskPolylineData.add(30, 50);
        maskPolylineData.add(w / 2, 10);
        maskPolylineData.add(w / 4, h / 3);
        maskPolylineData.add(w - 30, h / 2);
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

    public boolean drawMaskPolylines() {
        try {
            if (imageView == null || imageView.getImage() == null || maskPolylines == null) {
                return false;
            }
            if (maskPolylinesData == null) {
                maskPolylinesData = new DoublePolylines();
            }
            clearMaskPolylines();
            maskPolylines = new ArrayList<>();
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
            for (int i = 0; i < maskPolylinesData.getLinesSize(); i++) {
                List<DoublePoint> points = maskPolylinesData.getLines().get(i);
                if (points.isEmpty()) {
                    continue;
                }
                Polyline pline = new Polyline();
                for (DoublePoint p : points) {
                    pline.getPoints().add(p.getX() * xRatio);
                    pline.getPoints().add(p.getY() * yRatio);
                }
                maskPolylines.add(pline);
                maskPane.getChildren().add(pline);
                pline.setLayoutX(imageView.getLayoutX());
                pline.setLayoutY(imageView.getLayoutY());
                setShapeStyle(pline);
                int index = i;
                pline.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        controlPressed(event);
                        maskControlDragged = true;
                    }
                });
                pline.setOnMouseDragged(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        maskControlDragged = true;
                    }
                });
                pline.setOnMouseReleased(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        scrollPane.setPannable(true);
                        maskControlDragged = true;
                        if (isPickingColor) {
                            return;
                        }
                        double offsetX = imageOffsetX(event);
                        double offsetY = imageOffsetY(event);
                        if (!DoubleShape.changed(offsetX, offsetY)) {
                            return;
                        }
                        maskPolylinesData.translateLineRel(index, offsetX, offsetY);
                        drawMaskPolylines();
                        maskShapeDataChanged();
                    }
                });
                pline.hoverProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        if (newValue) {
                            if (UserConfig.getBoolean("ImageShapeControlPopMenu", true)) {
                                popNodeMenu(pline, lineMenu(pline, points));
                            }
                            if (isPickingColor) {
                                pline.setCursor(Cursor.HAND);
                            } else {
                                pline.setCursor(Cursor.MOVE);
                            }
                        }
                    }
                });
            }

            maskShapeChanged();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
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
                        drawMaskPolylines();
                        maskShapeDataChanged();
                    }
                });
            });
            items.add(menu);

            menu = new MenuItem(message("Delete"), StyleTools.getIconImageView("iconDelete.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                maskPolylinesData.removeLine(index);
                drawMaskPolylines();
                maskShapeDataChanged();
            });
            items.add(menu);

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public boolean makeCurrentLine(DoublePoint p) {
        if (!DoubleShape.changed(lastPoint, p)) {
            return false;
        }
        double xRatio = viewXRatio();
        double yRatio = viewYRatio();
        if (currentPolyline == null) {
            currentPolyline = new Polyline();
            currentPolyline.setStroke(Color.RED);
            currentPolyline.setStrokeWidth((shapeStyle != null ? shapeStyle.getStrokeWidth() : strokeWidth()) * xRatio);
            currentPolyline.getStrokeDashArray().clear();
            maskPane.getChildren().add(currentPolyline);
            currentPolyline.setLayoutX(imageView.getLayoutX());
            currentPolyline.setLayoutY(imageView.getLayoutY());
        }
        currentPolyline.getPoints().add(p.getX() * xRatio);
        currentPolyline.getPoints().add(p.getY() * yRatio);
        return true;
    }

    public void addMaskLinesData() {
        if (maskPolylines == null || maskPolylinesData == null
                || currentPolyline == null) {
            return;
        }
        List<DoublePoint> newLine = new ArrayList<>();
        double xRatio = imageXRatio();
        double yRatio = imageYRatio();
        List<Double> values = currentPolyline.getPoints();
        for (int i = 0; i < values.size(); i++) {
            newLine.add(new DoublePoint(values.get(i) * xRatio, values.get(++i) * yRatio));
        }
        maskPolylinesData.addLine(newLine);
        maskShapeDataChanged();
    }

    public void hideMaskPolylines() {
        if (maskPolylines != null) {
            for (Polyline line : maskPolylines) {
                line.setOpacity(0);
            }
        }
        if (currentPolyline != null) {
            maskPane.getChildren().remove(currentPolyline);
            currentPolyline = null;
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
        if (currentPolyline != null) {
            maskPane.getChildren().remove(currentPolyline);
            currentPolyline = null;
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
        double w = imageWidth();
        double h = imageHeight();
        maskPolygonData = new DoublePolygon();
        maskPolygonData.add(50, 80);
        maskPolygonData.add(w / 2, 10);
        maskPolygonData.add(w / 4, h / 3);
        maskPolygonData.add(w - 30, h / 2);
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
            clearMaskAnchors();
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

            setShapeStyle(maskQuadratic);

            maskShapeChanged();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
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
            maskPane.getChildren().addAll(maskArc,
                    maskHandlerLeftCenter, maskHandlerRightCenter,
                    maskHandlerTopCenter, maskHandlerBottomCenter);
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

            double anchorHW = anchorSize() * 0.5;
            maskHandlerTopCenter.setLayoutX(layoutX);
            maskHandlerTopCenter.setLayoutY(layoutY);
            maskHandlerTopCenter.setX(cx - anchorHW);
            maskHandlerTopCenter.setY(cy - ry - anchorHW);

            maskHandlerBottomCenter.setLayoutX(layoutX);
            maskHandlerBottomCenter.setLayoutY(layoutY);
            maskHandlerBottomCenter.setX(cx - anchorHW);
            maskHandlerBottomCenter.setY(cy + ry - anchorHW);

            maskHandlerLeftCenter.setLayoutX(layoutX);
            maskHandlerLeftCenter.setLayoutY(layoutY);
            maskHandlerLeftCenter.setX(cx - rx - anchorHW);
            maskHandlerLeftCenter.setY(cy - anchorHW);

            maskHandlerRightCenter.setLayoutX(layoutX);
            maskHandlerRightCenter.setLayoutY(layoutY);
            maskHandlerRightCenter.setX(cx + rx - anchorHW);
            maskHandlerRightCenter.setY(cy - anchorHW);

            setShapeStyle(maskArc);
            setMaskAnchorsStyle();

            maskShapeChanged();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void clearMaskArc() {
        if (imageView == null || maskPane == null || maskArc == null) {
            return;
        }
        maskPane.getChildren().removeAll(maskArc,
                maskHandlerLeftCenter, maskHandlerRightCenter,
                maskHandlerTopCenter, maskHandlerBottomCenter);
        maskArc.setVisible(false);

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
        maskPathData = new DoublePath("M 10,30\n"
                + "           A 20,20 0,0,1 50,30\n"
                + "           A 20,20 0,0,1 90,30\n"
                + "           Q 90,60 50,90\n"
                + "           Q 10,60 10,30 z");
    }

    public boolean drawMaskPath() {
        try {
            if (imageView == null || maskPane == null || maskSVGPath == null) {
                return false;
            }
            if (maskPathData == null) {
                setMaskPathDefaultValues();
            }
            maskSVGPath.setContent(maskPathData.getContent());
            maskSVGPath.setLayoutX(imageView.getLayoutX());
            maskSVGPath.setLayoutY(imageView.getLayoutY());
            setShapeStyle(maskSVGPath);

            maskSVGPath.setVisible(true);
            maskShapeChanged();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
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

    /*
        static
     */
    public static void updateMaskStrokes() {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof BaseImageController) {
                try {
                    BaseImageController controller = (BaseImageController) object;
                    controller.setMaskShapesStyle();
                } catch (Exception e) {
                }
            }
        }
    }

    public static void updateMaskAnchors() {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof BaseImageController) {
                try {
                    BaseImageController controller = (BaseImageController) object;
                    controller.setMaskAnchorsStyle();
                } catch (Exception e) {
                }
            }
        }
    }

    public static void updateMaskRulerXY() {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof BaseImageController) {
                try {
                    BaseImageController controller = (BaseImageController) object;
                    controller.drawMaskRulers();
                } catch (Exception e) {
                }
            }
        }
    }

    public static void updateMaskGrid() {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof BaseImageController) {
                try {
                    BaseImageController controller = (BaseImageController) object;
                    controller.drawMaskGrid();
                } catch (Exception e) {
                }
            }
        }
    }

}
