package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoubleLines;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoublePolyline;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.IntPoint;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-24
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageMaskController extends ImageBaseController {

    public static final String DefaultStrokeColor = "#c94d58", DefaultAnchorColor = "#0066cc";

    protected boolean needNotRulers, needNotCoordinates, changed;
    protected DoubleRectangle maskRectangleData;
    protected DoubleCircle maskCircleData;
    protected DoubleEllipse maskEllipseData;
    protected DoublePolygon maskPolygonData;
    protected DoublePolyline maskPolylineData;
    protected DoublePolyline maskLineData;
    protected List<Line> maskLineLines;
    protected DoubleLines maskPenData;
    protected List<List<Line>> maskPenLines;

    @FXML
    protected AnchorPane maskPane;
    @FXML
    protected ImageView maskView;
    @FXML
    protected Rectangle maskRectangleLine, leftCenterHandler, topLeftHandler, topCenterHandler, topRightHandler,
            bottomLeftHandler, bottomCenterHandler, bottomRightHandler, rightCenterHandler,
            polygonP1, polygonP2;
    @FXML
    protected Circle maskCircleLine;
    @FXML
    protected Ellipse maskEllipseLine;
    @FXML
    protected Polygon maskPolygonLine;
    @FXML
    protected Polyline maskPolyline;
    @FXML
    protected Label imageLabel;
    @FXML
    protected Text xyText;
    @FXML
    protected CheckBox rulerXCheck, rulerYCheck, coordinateCheck;

    public ImageMaskController() {
        needNotRulers = false;
        needNotCoordinates = false;

    }

    @Override
    public void initializeNext() {
        try {
            initImageView();
            initMaskPane();
            initRulersCheck();
            initializeNext2();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void init(ImageViewerController parent) {
        this.parentController = parent;
        initMaskPane();
        initMaskControls(false);
    }

    public void init(File sourceFile, Image image) {
        try {
            this.sourceFile = sourceFile;
            this.image = image;
            imageView.setImage(image);
            fitSize();

            checkRulerX();
            checkRulerY();
            checkCoordinate();
            setMaskStroke();
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void initMaskPane() {
        if (maskPane == null) {
            return;
        }
        try {
            maskPane.prefWidthProperty().bind(imageView.fitWidthProperty());
            maskPane.prefHeightProperty().bind(imageView.fitHeightProperty());
            initMaskControls(false);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void initRulersCheck() {
        try {
            if (rulerXCheck != null) {
                rulerXCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue("ImageRulerXKey", rulerXCheck.isSelected());
                        checkRulerX();
                    }
                });
                rulerXCheck.setSelected(AppVariables.getUserConfigBoolean("ImageRulerXKey", false));
            }
            if (rulerYCheck != null) {
                rulerYCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue("ImageRulerYKey", rulerYCheck.isSelected());
                        checkRulerY();
                    }
                });
                rulerYCheck.setSelected(AppVariables.getUserConfigBoolean("ImageRulerYKey", false));
            }

            if (coordinateCheck != null) {
                coordinateCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue("ImagePopCooridnate", coordinateCheck.isSelected());
                        AppVariables.ImagePopCooridnate = coordinateCheck.isSelected();
                        checkCoordinate();
                    }
                });
                coordinateCheck.setSelected(AppVariables.ImagePopCooridnate);

            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkRulerX() {
        drawMaskRulerX();
    }

    protected void checkRulerY() {
        drawMaskRulerY();
    }

    protected void checkCoordinate() {
        if (xyText != null) {
            xyText.setText("");
        }
    }

    @Override
    public void viewSizeChanged(double change) {
        if (imageView.getImage() == null) {
            return;
        }
        super.viewSizeChanged(change);
        drawMaskControls();
    }

    public void clear() {

    }

    // Any mask operations when pane size is changed
    public void drawMaskControls() {
        drawMaskRectangleLine();
        drawMaskCircleLine();
        drawMaskEllipseLine();
        drawMaskPolygonLine();
        drawMaskPolyline();
        drawMaskRulerX();
        drawMaskRulerY();
    }

    @Override
    public void setMaskStroke() {
        try {
            if (isSettingValues || maskPane == null) {
                return;
            }
            double strokeWidth = AppVariables.getUserConfigInt("StrokeWidth", 2);
            Color strokeColor = Color.web(AppVariables.getUserConfigValue("StrokeColor", DefaultStrokeColor));
            setMaskLinesStroke(strokeColor, strokeWidth, true);

            Color anchorColor = Color.web(AppVariables.getUserConfigValue("AnchorColor", DefaultAnchorColor));
            int anchorWidth = AppVariables.getUserConfigInt("AnchorWidth", 10);
            setMaskAnchorsStroke(anchorColor, anchorWidth);

            if (xyText != null) {
                xyText.setFill(strokeColor);
                xyText.setText("");
            }

            drawMaskRulerX();
            drawMaskRulerY();
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    public void setShapeStroke(Shape shape,
            Color strokeColor, double width, boolean dotted) {

        if (shape == null) {
            return;
        }
        if (strokeColor == null) {
            strokeColor = Color.RED;
        }
        if (width <= 0) {
            width = 3.0d;
        }
        shape.setStroke(strokeColor);
        shape.setStrokeWidth(width);
        shape.getStrokeDashArray().clear();
        if (dotted) {
            shape.getStrokeDashArray().addAll(width, width * 3);
        }

    }

    public void setMaskLinesStroke(Color strokeColor, double width, boolean dotted) {
        setShapeStroke(maskRectangleLine, strokeColor, width, dotted);
        setShapeStroke(maskCircleLine, strokeColor, width, dotted);
        setShapeStroke(maskEllipseLine, strokeColor, width, dotted);
        setShapeStroke(maskPolygonLine, strokeColor, width, dotted);
        setShapeStroke(maskPolyline, strokeColor, width, dotted);

    }

    public void setMaskAnchorsStroke(Color anchorColor, double anchorWidth) {
        if (isSettingValues || maskPane == null) {
            return;
        }
        if (anchorColor == null) {
            anchorColor = Color.BLUE;
        }
        if (anchorWidth <= 0) {
            anchorWidth = 10;
        }
        if (topLeftHandler != null) {
            topLeftHandler.setStroke(anchorColor);
            topCenterHandler.setStroke(anchorColor);
            topRightHandler.setStroke(anchorColor);
            bottomLeftHandler.setStroke(anchorColor);
            bottomCenterHandler.setStroke(anchorColor);
            bottomRightHandler.setStroke(anchorColor);
            leftCenterHandler.setStroke(anchorColor);
            rightCenterHandler.setStroke(anchorColor);

            topLeftHandler.setWidth(anchorWidth);
            topLeftHandler.setHeight(anchorWidth);
            topCenterHandler.setWidth(anchorWidth);
            topCenterHandler.setHeight(anchorWidth);
            topRightHandler.setWidth(anchorWidth);
            topRightHandler.setHeight(anchorWidth);
            bottomLeftHandler.setWidth(anchorWidth);
            bottomLeftHandler.setHeight(anchorWidth);
            bottomCenterHandler.setWidth(anchorWidth);
            bottomCenterHandler.setHeight(anchorWidth);
            bottomRightHandler.setWidth(anchorWidth);
            bottomRightHandler.setHeight(anchorWidth);
            leftCenterHandler.setWidth(anchorWidth);
            leftCenterHandler.setHeight(anchorWidth);
            rightCenterHandler.setWidth(anchorWidth);
            rightCenterHandler.setHeight(anchorWidth);

            if (AppVariables.getUserConfigBoolean("AnchorSolid", true)) {
                topLeftHandler.setFill(anchorColor);
                topCenterHandler.setFill(anchorColor);
                topRightHandler.setFill(anchorColor);
                bottomLeftHandler.setFill(anchorColor);
                bottomCenterHandler.setFill(anchorColor);
                bottomRightHandler.setFill(anchorColor);
                leftCenterHandler.setFill(anchorColor);
                rightCenterHandler.setFill(anchorColor);
            } else {
                topLeftHandler.setFill(null);
                topCenterHandler.setFill(null);
                topRightHandler.setFill(null);
                bottomLeftHandler.setFill(null);
                bottomCenterHandler.setFill(null);
                bottomRightHandler.setFill(null);
                leftCenterHandler.setFill(null);
                rightCenterHandler.setFill(null);
            }

        }

        if (polygonP1 != null) {
            polygonP1.setStroke(anchorColor);
            polygonP2.setStroke(anchorColor);

            polygonP1.setWidth(anchorWidth);
            polygonP2.setWidth(anchorWidth);

            if (AppVariables.getUserConfigBoolean("AnchorSolid", true)) {
                polygonP1.setFill(anchorColor);
                polygonP2.setFill(anchorColor);
            } else {
                polygonP1.setFill(null);
                polygonP2.setFill(null);
            }
        }

    }

    private int getRulerStep(double width) {
        if (width <= 1000) {
            return 10;
        } else if (width <= 10000) {
            return (int) (width / 1000) * 10;
        } else {
            return (int) (width / 10000) * 10;
        }
    }

    @Override
    public void drawMaskRulerX() {
        if (needNotRulers || maskPane == null || imageView.getImage() == null) {
            return;
        }
        clearMaskRulerX();
        if (AppVariables.getUserConfigBoolean("ImageRulerXKey", false)) {
            Color strokeColor = Color.web(AppVariables.getUserConfigValue("StrokeColor", "#FF0000"));
            double imageWidth = getImageWidth();
            double ratio = imageView.getBoundsInParent().getWidth() / imageWidth;
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

    public void clearMaskRulerX() {
        if (needNotRulers || maskPane == null || imageView.getImage() == null) {
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

    @Override
    public void drawMaskRulerY() {
        if (needNotRulers || maskPane == null || imageView.getImage() == null) {
            return;
        }
        clearMaskRulerY();
        if (AppVariables.getUserConfigBoolean("ImageRulerYKey", false)) {
            Color strokeColor = Color.web(AppVariables.getUserConfigValue("StrokeColor", "#FF0000"));
            double imageHeight = getImageHeight();
            double ratio = imageView.getBoundsInParent().getHeight() / imageHeight;
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

    public void clearMaskRulerY() {
        if (needNotRulers || maskPane == null || imageView.getImage() == null) {
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

    public void initMaskControls(boolean show) {
        initMaskRectangleLine(show);
        initMaskCircleLine(show);
        initMaskEllipseLine(show);
        initMaskPolygonLine(show);
        initMaskPolyline(show);
        initMaskline(show);
        initMaskPenlines(show);
        drawMaskRulerX();
        drawMaskRulerY();
    }

    public void resetMaskControls() {
        try {
            if (maskRectangleLine != null && maskRectangleLine.isVisible()) {
                initMaskRectangleLine(true);
            }
            if (maskCircleLine != null && maskCircleLine.isVisible()) {
                initMaskCircleLine(true);
            }
            if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
                initMaskEllipseLine(true);
            }
            if (maskPolygonLine != null && maskPolygonLine.isVisible()) {
                initMaskPolygonLine(true);
            }
            if (maskPolyline != null && maskPolyline.isVisible()) {
                initMaskPolyline(true);
            }
            initMaskline(true);
            initMaskPenlines(true);
            drawMaskRulerX();
            drawMaskRulerY();
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void initMaskRectangleLine(boolean show) {
        if (imageView == null || maskPane == null || maskRectangleLine == null) {
            return;
        }

        setMaskRectangleLineVisible(show);
        if (show && imageView.getImage() != null) {
            setDafultMaskRectangleValues();
            drawMaskRectangleLine();
        }

    }

    public void setMaskRectangleLineVisible(boolean show) {
        if (imageView == null || maskPane == null || maskRectangleLine == null) {
            return;
        }
        if (show && imageView.getImage() != null) {
            maskRectangleLine.setVisible(true);
            if (!maskPane.getChildren().contains(maskRectangleLine)) {
                maskPane.getChildren().addAll(maskRectangleLine,
                        leftCenterHandler, rightCenterHandler,
                        topLeftHandler, topCenterHandler, topRightHandler,
                        bottomLeftHandler, bottomCenterHandler, bottomRightHandler);
            }
            setMaskStroke();
        } else {
            maskPane.getChildren().removeAll(maskRectangleLine,
                    leftCenterHandler, rightCenterHandler,
                    topLeftHandler, topCenterHandler, topRightHandler,
                    bottomLeftHandler, bottomCenterHandler, bottomRightHandler);
            maskRectangleLine.setVisible(false);
        }

    }

    public void setDafultMaskRectangleValues() {
        if (imageView == null || maskPane == null || maskRectangleLine == null) {
            return;
        }

        double w = getImageWidth();
        double h = getImageHeight();
        maskRectangleData = new DoubleRectangle(w / 4, h / 4, w * 3 / 4, h * 3 / 4);
    }

    public boolean drawMaskRectangleLine() {

        return drawMaskRectangleLineAsData();
    }

    public void countMaskRectangleData() {
        if (maskRectangleLine == null || !maskPane.getChildren().contains(maskRectangleLine)
                || maskRectangleData == null
                || imageView == null || imageView.getImage() == null) {
            return;
        }
        double xRatio = getImageWidth() / imageView.getBoundsInParent().getWidth();
        double yRatio = getImageHeight() / imageView.getBoundsInParent().getHeight();

        double smallX = maskRectangleLine.getLayoutX() - imageView.getLayoutX();
        smallX = smallX * xRatio;
        if (smallX >= getImageWidth() - 1) {
            smallX = getImageWidth() - 2;
        }
        maskRectangleData.setSmallX(smallX);

        double smallY = maskRectangleLine.getLayoutY() - imageView.getLayoutY();
        smallY = smallY * yRatio;
        if (smallY >= getImageHeight() - 1) {
            smallY = getImageHeight() - 2;
        }
        maskRectangleData.setSmallY(smallY);

        double bigX = maskRectangleLine.getLayoutX() + maskRectangleLine.getWidth() - imageView.getLayoutX();
        bigX = bigX * xRatio;
        if (bigX <= 0) {
            bigX = 1;
        }
        maskRectangleData.setBigX(bigX);

        double bigY = maskRectangleLine.getLayoutY() + maskRectangleLine.getHeight() - imageView.getLayoutY();
        bigY = bigY * yRatio;
        if (bigY <= 0) {
            bigY = 1;
        }
        maskRectangleData.setBigY(bigY);

    }

    public boolean drawMaskRectangleLineAsData() {
        try {
            if (maskRectangleLine == null || !maskPane.getChildren().contains(maskRectangleLine)
                    || maskRectangleData == null
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }
            int anchorHW = AppVariables.getUserConfigInt("AnchorWidth", 10) / 2;
            double xRatio = imageView.getBoundsInParent().getWidth() / getImageWidth();
            double yRatio = imageView.getBoundsInParent().getHeight() / getImageHeight();
            double x1 = maskRectangleData.getSmallX() * xRatio;
            double y1 = maskRectangleData.getSmallY() * yRatio;
            double x2 = maskRectangleData.getBigX() * xRatio;
            double y2 = maskRectangleData.getBigY() * yRatio;
            maskRectangleLine.setLayoutX(imageView.getLayoutX() + x1);
            maskRectangleLine.setLayoutY(imageView.getLayoutY() + y1);
            maskRectangleLine.setWidth(x2 - x1 + 1);
            maskRectangleLine.setHeight(y2 - y1 + 1);

            double lineX = maskRectangleLine.getLayoutX();
            double lineY = maskRectangleLine.getLayoutY();
            topLeftHandler.setLayoutX(lineX - anchorHW);
            topLeftHandler.setLayoutY(lineY - anchorHW);
            topCenterHandler.setLayoutX(lineX + maskRectangleLine.getWidth() / 2 - anchorHW);
            topCenterHandler.setLayoutY(lineY - anchorHW);
            topRightHandler.setLayoutX(lineX + maskRectangleLine.getWidth() - anchorHW);
            topRightHandler.setLayoutY(lineY - anchorHW);
            bottomLeftHandler.setLayoutX(lineX - anchorHW);
            bottomLeftHandler.setLayoutY(lineY + maskRectangleLine.getHeight() - anchorHW);
            bottomCenterHandler.setLayoutX(lineX + maskRectangleLine.getWidth() / 2 - anchorHW);
            bottomCenterHandler.setLayoutY(lineY + maskRectangleLine.getHeight() - anchorHW);
            bottomRightHandler.setLayoutX(lineX + maskRectangleLine.getWidth() - anchorHW);
            bottomRightHandler.setLayoutY(lineY + maskRectangleLine.getHeight() - anchorHW);
            leftCenterHandler.setLayoutX(lineX - anchorHW);
            leftCenterHandler.setLayoutY(lineY + maskRectangleLine.getHeight() / 2 - anchorHW);
            rightCenterHandler.setLayoutX(lineX + maskRectangleLine.getWidth() - anchorHW);
            rightCenterHandler.setLayoutY(lineY + maskRectangleLine.getHeight() / 2 - anchorHW);

            updateLabelTitle();
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }

    }

    public void drawMaskAroundLine(ImageView view) {
        maskRectangleData = new DoubleRectangle(0, 0,
                imageView.getImage().getWidth() - 1, imageView.getImage().getHeight() - 1);
        drawMaskRectangleLineAsData();
    }

    public void initMaskCircleLine(boolean show) {
        initMaskCircleLine(show, null);
    }

    public void initMaskCircleLine(boolean show, DoubleCircle circle) {
        if (imageView == null || maskPane == null || maskCircleLine == null) {
            return;
        }
        if (show && imageView.getImage() != null) {
            if (circle == null) {
                setDafultMaskCircleValues();
            } else {
                maskCircleData = circle;
            }
            maskCircleLine.setVisible(true);
            if (!maskPane.getChildren().contains(maskCircleLine)) {
                maskPane.getChildren().addAll(maskCircleLine,
                        leftCenterHandler, rightCenterHandler,
                        topCenterHandler, bottomCenterHandler);
            }
            setMaskStroke();
            drawMaskCircleLine();

        } else {
            maskPane.getChildren().removeAll(maskCircleLine,
                    leftCenterHandler, rightCenterHandler,
                    topCenterHandler, bottomCenterHandler);
            maskCircleLine.setVisible(false);
        }

    }

    public void setDafultMaskCircleValues() {
        if (imageView == null || maskPane == null || maskCircleLine == null) {
            return;
        }
        double w = getImageWidth();
        double h = getImageHeight();
        maskCircleData = new DoubleCircle(w / 2, h / 2, Math.min(w, h) / 4);
    }

    public boolean drawMaskCircleLine() {
        return drawMaskCircleLineAsData();
    }

    public boolean drawMaskCircleLineAsData() {
        try {
            if (maskCircleLine == null || !maskCircleLine.isVisible()
                    || maskCircleData == null
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }

            int anchorHW = AppVariables.getUserConfigInt("AnchorWidth", 10) / 2;
            double xRatio = imageView.getBoundsInParent().getWidth() / getImageWidth();
            double r = maskCircleData.getRadius() * xRatio;
            double x = maskCircleData.getCenterX() * xRatio;
            double y = maskCircleData.getCenterY() * xRatio;
            maskCircleLine.setLayoutX(imageView.getLayoutX() + x);  // Circle's layout is about its center
            maskCircleLine.setLayoutY(imageView.getLayoutY() + y);
            maskCircleLine.setRadius(r);

            topCenterHandler.setLayoutX(maskCircleLine.getLayoutX() - anchorHW);
            topCenterHandler.setLayoutY(maskCircleLine.getLayoutY() - maskCircleLine.getRadius() - anchorHW);
            bottomCenterHandler.setLayoutX(maskCircleLine.getLayoutX() - anchorHW);
            bottomCenterHandler.setLayoutY(maskCircleLine.getLayoutY() + maskCircleLine.getRadius() - anchorHW);
            leftCenterHandler.setLayoutX(maskCircleLine.getLayoutX() - maskCircleLine.getRadius() - anchorHW);
            leftCenterHandler.setLayoutY(maskCircleLine.getLayoutY() - anchorHW);
            rightCenterHandler.setLayoutX(maskCircleLine.getLayoutX() + maskCircleLine.getRadius() - anchorHW);
            rightCenterHandler.setLayoutY(maskCircleLine.getLayoutY() - anchorHW);

//            updateLabelTitle();
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public void initMaskEllipseLine(boolean show) {
        if (imageView == null || maskPane == null || maskEllipseLine == null) {
            return;
        }
        if (show && imageView.getImage() != null) {
            setDafultMaskEllipseValues();
            maskEllipseLine.setVisible(true);
            if (!maskPane.getChildren().contains(maskEllipseLine)) {
                maskPane.getChildren().addAll(maskEllipseLine,
                        leftCenterHandler, rightCenterHandler,
                        topCenterHandler, bottomCenterHandler);
            }
            setMaskStroke();
            drawMaskEllipseLine();

        } else {
            maskPane.getChildren().removeAll(maskEllipseLine,
                    leftCenterHandler, rightCenterHandler,
                    topCenterHandler, bottomCenterHandler);
            maskEllipseLine.setVisible(false);
        }

    }

    public void setDafultMaskEllipseValues() {
        if (imageView == null || maskPane == null || maskEllipseLine == null) {
            return;
        }
        double w = getImageWidth();
        double h = getImageHeight();
        maskEllipseData = new DoubleEllipse(w / 4, h / 4, w * 3 / 4, h * 3 / 4);
    }

    public boolean drawMaskEllipseLine() {
        return drawMaskEllipseLineAsData();
    }

    public boolean drawMaskEllipseLineAsData() {
        try {
            if (maskEllipseLine == null || !maskEllipseLine.isVisible()
                    || maskEllipseData == null
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }

            int anchorHW = AppVariables.getUserConfigInt("AnchorWidth", 10) / 2;
            double xRatio = imageView.getBoundsInParent().getWidth() / getImageWidth();
            double yRatio = imageView.getBoundsInParent().getHeight() / getImageHeight();
            double rx = maskEllipseData.getRadiusX() * xRatio;
            double ry = maskEllipseData.getRadiusY() * yRatio;
            double cx = maskEllipseData.getCenterX() * xRatio;
            double cy = maskEllipseData.getCenterY() * xRatio;
            maskEllipseLine.setLayoutX(imageView.getLayoutX() + cx);
            maskEllipseLine.setLayoutY(imageView.getLayoutY() + cy);
            maskEllipseLine.setRadiusX(rx);
            maskEllipseLine.setRadiusY(ry);

            topCenterHandler.setLayoutX(maskEllipseLine.getLayoutX() - anchorHW);
            topCenterHandler.setLayoutY(maskEllipseLine.getLayoutY() - maskEllipseLine.getRadiusY() - anchorHW);
            bottomCenterHandler.setLayoutX(maskEllipseLine.getLayoutX() - anchorHW);
            bottomCenterHandler.setLayoutY(maskEllipseLine.getLayoutY() + maskEllipseLine.getRadiusY() - anchorHW);
            leftCenterHandler.setLayoutX(maskEllipseLine.getLayoutX() - maskEllipseLine.getRadiusX() - anchorHW);
            leftCenterHandler.setLayoutY(maskEllipseLine.getLayoutY() - anchorHW);
            rightCenterHandler.setLayoutX(maskEllipseLine.getLayoutX() + maskEllipseLine.getRadiusX() - anchorHW);
            rightCenterHandler.setLayoutY(maskEllipseLine.getLayoutY() - anchorHW);

//            updateLabelTitle();
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }

    }

    public void initMaskPolygonLine(boolean show) {
        if (imageView == null || maskPane == null || maskPolygonLine == null) {
            return;
        }
        setDafultMaskPolygonValues();
        if (show && imageView.getImage() != null) {
            maskPolygonLine.setVisible(true);
            if (!maskPane.getChildren().contains(maskPolygonLine)) {
                maskPane.getChildren().addAll(maskPolygonLine, polygonP1, polygonP2);
            }
            setMaskStroke();
            drawMaskPolygonLine();

        } else {
            maskPane.getChildren().removeAll(maskPolygonLine, polygonP1, polygonP2);
            maskPolygonLine.setVisible(false);
        }

    }

    public void setDafultMaskPolygonValues() {
        if (imageView == null || maskPane == null || maskPolygonLine == null) {
            return;
        }
        maskPolygonData = new DoublePolygon();
        maskPolygonLine.getPoints().clear();

    }

    public boolean drawMaskPolygonLine() {
        return drawMaskPolygonLineAsData();
    }

    public boolean drawMaskPolygonLineAsData() {
        try {
            if (maskPolygonLine == null || !maskPolygonLine.isVisible()
                    || maskPolygonData == null
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }

            int anchorHW = AppVariables.getUserConfigInt("AnchorWidth", 10) / 2;
            double xRatio = imageView.getBoundsInParent().getWidth() / getImageWidth();
            double yRatio = imageView.getBoundsInParent().getHeight() / getImageHeight();

            List<Double> d = new ArrayList<>();
            for (int i = 0; i < maskPolygonData.getSize(); ++i) {
                d.add(maskPolygonData.get(i).getX() * xRatio);
                d.add(maskPolygonData.get(i).getY() * yRatio);
            }
            maskPolygonLine.getPoints().setAll(d);

            if (d.isEmpty()) {
                maskPolygonLine.setOpacity(0);
                polygonP1.setOpacity(0);
                polygonP2.setOpacity(0);

            } else if (maskPolygonData.getSize() > 2) {

                maskPolygonLine.setOpacity(1);
                polygonP1.setOpacity(0);
                polygonP2.setOpacity(0);

                maskPolygonLine.setLayoutX(imageView.getLayoutX());
                maskPolygonLine.setLayoutY(imageView.getLayoutY());

            } else if (maskPolygonData.getSize() == 2) {

                maskPolygonLine.setOpacity(0);
                polygonP1.setOpacity(1);
                polygonP2.setOpacity(1);

                DoublePoint p1 = maskPolygonData.get(0);
                polygonP1.setLayoutX(imageView.getLayoutX() + p1.getX() * xRatio - anchorHW);
                polygonP1.setLayoutY(imageView.getLayoutY() + p1.getY() * yRatio - anchorHW);

                DoublePoint p2 = maskPolygonData.get(1);
                polygonP2.setLayoutX(imageView.getLayoutX() + p2.getX() * xRatio - anchorHW);
                polygonP2.setLayoutY(imageView.getLayoutY() + p2.getY() * yRatio - anchorHW);

            } else if (maskPolygonData.getSize() == 1) {

                maskPolygonLine.setOpacity(0);
                polygonP1.setOpacity(1);
                polygonP2.setOpacity(0);

                DoublePoint p1 = maskPolygonData.get(0);
                polygonP1.setLayoutX(imageView.getLayoutX() + p1.getX() * xRatio - anchorHW);
                polygonP1.setLayoutY(imageView.getLayoutY() + p1.getY() * yRatio - anchorHW);
            }

//            updateLabelTitle();
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }

    }

    public void initMaskPolyline(boolean show) {
        if (imageView == null || maskPane == null || maskPolyline == null) {
            return;
        }
        setDafultMaskPolylineValues();
        if (show && imageView.getImage() != null) {
            maskPolyline.setVisible(true);
            if (!maskPane.getChildren().contains(maskPolyline)) {
                maskPane.getChildren().addAll(maskPolyline, polygonP1);
            }
            setMaskStroke();
            drawMaskPolyline();

        } else {
            maskPane.getChildren().removeAll(maskPolyline, polygonP1, polygonP2);
            maskPolyline.setVisible(false);
        }

    }

    public void setDafultMaskPolylineValues() {
        if (imageView == null || maskPane == null || maskPolyline == null) {
            return;
        }
        maskPolylineData = new DoublePolyline();
        maskPolyline.getPoints().clear();

    }

    public boolean drawMaskPolyline() {
        return drawMaskPolylineAsData();
    }

    public boolean drawMaskPolylineAsData() {
        try {
            if (maskPolyline == null || imageView == null
                    || imageView.getImage() == null || !maskPolyline.isVisible()) {
                return false;
            }
            maskPolyline.setOpacity(0);
            polygonP1.setOpacity(0);

            maskPolyline.getPoints().clear();

            int anchorHW = AppVariables.getUserConfigInt("AnchorWidth", 10) / 2;
            double xRatio = imageView.getBoundsInParent().getWidth() / getImageWidth();
            double yRatio = imageView.getBoundsInParent().getHeight() / getImageHeight();

            List<Double> d = new ArrayList<>();
            for (int i = 0; i < maskPolylineData.getSize(); ++i) {
                d.add(maskPolylineData.get(i).getX() * xRatio);
                d.add(maskPolylineData.get(i).getY() * yRatio);
            }
            maskPolyline.getPoints().addAll(d);

            if (d.isEmpty()) {

            } else if (maskPolylineData.getSize() > 1) {

                maskPolyline.setLayoutX(imageView.getLayoutX());
                maskPolyline.setLayoutY(imageView.getLayoutY());

                if (maskPolylineData.getSize() == 2) {  // Have to add one more to make it displayed.
                    maskPolyline.getPoints().addAll(maskPolylineData.get(1).getX() * xRatio + 0.5,
                            maskPolylineData.get(1).getY() * yRatio + 0.5);
                }
                maskPolyline.setOpacity(1);

            } else if (maskPolylineData.getSize() == 1) {

                polygonP1.setOpacity(1);

                DoublePoint p1 = maskPolylineData.get(0);
                polygonP1.setLayoutX(imageView.getLayoutX() + p1.getX() * xRatio - anchorHW);
                polygonP1.setLayoutY(imageView.getLayoutY() + p1.getY() * yRatio - anchorHW);

            }

//            updateLabelTitle();
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }

    }

    public void initMaskline(boolean show) {
        try {
            if (imageView == null || maskPane == null) {
                return;
            }
            if (show && imageView.getImage() != null) {
                maskLineLines = new ArrayList<>();
                maskLineData = new DoublePolyline();
            } else {
                if (maskLineLines != null) {
                    maskPane.getChildren().removeAll(maskLineLines);
                    maskLineLines.clear();
                }
                if (maskLineData != null) {
                    maskLineData.clear();
                }
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    // Polyline of Java shows weird results. So I just use lines directly.
    public boolean drawMaskLine(double strokeWidth, Color strokeColor, boolean dotted, float opacity) {
        maskPane.getChildren().removeAll(maskLineLines);
        maskLineLines.clear();
        polygonP1.setOpacity(0);
        int size = maskLineData.getSize();
        if (size == 0) {
            return true;
        }

        double xRatio = imageView.getBoundsInParent().getWidth() / getImageWidth();
        double yRatio = imageView.getBoundsInParent().getHeight() / getImageHeight();
        double drawStrokeWidth = strokeWidth * xRatio;
        if (size == 1) {
            polygonP1.setOpacity(1);
            DoublePoint p1 = maskLineData.get(0);
            int anchorHW = AppVariables.getUserConfigInt("AnchorWidth", 10) / 2;
            polygonP1.setLayoutX(imageView.getLayoutX() + p1.getX() * xRatio - anchorHW);
            polygonP1.setLayoutY(imageView.getLayoutY() + p1.getY() * yRatio - anchorHW);
        } else if (size > 1) {
            double lastx = -1, lasty = -1, thisx, thisy;
            for (DoublePoint p : maskLineData.getPoints()) {
                thisx = p.getX() * xRatio;
                thisy = p.getY() * yRatio;
                if (lastx >= 0) {
                    Line line = new Line(lastx, lasty, thisx, thisy);
                    if (strokeColor.equals(Color.TRANSPARENT)) {
                        // Have not found how to make line as transparent. For display only.
                        line.setStroke(Color.WHITE);
                    } else {
                        line.setStroke(strokeColor);
                    }
                    line.setStrokeWidth(drawStrokeWidth);
                    line.getStrokeDashArray().clear();
                    if (dotted) {
                        line.getStrokeDashArray().addAll(drawStrokeWidth * 1d, drawStrokeWidth * 3d);
                    }
                    line.setOpacity(opacity);
                    maskLineLines.add(line);
                    maskPane.getChildren().add(line);
                    line.setLayoutX(imageView.getLayoutX());
                    line.setLayoutY(imageView.getLayoutY());
                }
                lastx = thisx;
                lasty = thisy;
            }
        }
        return true;
    }

    public void initMaskPenlines(boolean show) {
        if (imageView == null || maskPane == null) {
            return;
        }
        if (maskPenLines != null) {
            for (List<Line> penline : maskPenLines) {
                maskPane.getChildren().removeAll(penline);
            }
            maskPenLines.clear();
        }
        if (maskPenData != null) {
            maskPenData.clear();
        }
        if (show) {
            maskPenLines = new ArrayList<>();
            maskPenData = new DoubleLines();
        }

    }

    // strokeWidth is value expected shown on image, so it needs apply ratio for view
    public boolean drawMaskPenLines(double strokeWidth, Color strokeColor, boolean dotted, float opacity) {
        for (List<Line> penline : maskPenLines) {
            maskPane.getChildren().removeAll(penline);
        }
        maskPenLines.clear();
        polygonP1.setOpacity(0);
        int size = maskPenData.getPointsSize();
        if (size == 0) {
            return true;
        }
        double xRatio = imageView.getBoundsInParent().getWidth() / getImageWidth();
        double yRatio = imageView.getBoundsInParent().getHeight() / getImageHeight();
        double drawStrokeWidth = strokeWidth * xRatio;
        if (size == 1) {
            polygonP1.setOpacity(1);
            DoublePoint p1 = maskPenData.getPoint(0);
            int anchorHW = AppVariables.getUserConfigInt("AnchorWidth", 10) / 2;
            polygonP1.setLayoutX(imageView.getLayoutX() + p1.getX() * xRatio - anchorHW);
            polygonP1.setLayoutY(imageView.getLayoutY() + p1.getY() * yRatio - anchorHW);
        } else if (size > 1) {
            double lastx, lasty = -1, thisx, thisy;
            for (List<DoublePoint> lineData : maskPenData.getLines()) {
                List<Line> penLine = new ArrayList<>();
                lastx = -1;
                for (DoublePoint p : lineData) {
                    thisx = p.getX() * xRatio;
                    thisy = p.getY() * yRatio;
                    if (lastx >= 0) {
                        Line line = new Line(lastx, lasty, thisx, thisy);
                        if (strokeColor.equals(Color.TRANSPARENT)) {
                            // Have not found how to make line as transparent. For display only.
                            line.setStroke(Color.WHITE);
                        } else {
                            line.setStroke(strokeColor);
                        }
                        line.setStrokeWidth(drawStrokeWidth);
                        line.getStrokeDashArray().clear();
                        if (dotted) {
                            line.getStrokeDashArray().addAll(drawStrokeWidth * 1d, drawStrokeWidth * 3d);
                        }
                        line.setOpacity(opacity);
                        penLine.add(line);
                        maskPane.getChildren().add(line);
                        line.setLayoutX(imageView.getLayoutX());
                        line.setLayoutY(imageView.getLayoutY());
                    }
                    lastx = thisx;
                    lasty = thisy;
                }
                maskPenLines.add(penLine);
            }

        }
        return true;
    }

    public boolean drawMaskMosaicLines(double strokeWidth) {
        for (List<Line> penline : maskPenLines) {
            maskPane.getChildren().removeAll(penline);
        }
        maskPenLines.clear();
        polygonP1.setOpacity(0);
        int size = maskPenData.getPointsSize();
        if (size == 0) {
            return true;
        }
        double xRatio = imageView.getBoundsInParent().getWidth() / getImageWidth();
        double yRatio = imageView.getBoundsInParent().getHeight() / getImageHeight();
        if (size == 1) {
            polygonP1.setOpacity(1);
            DoublePoint p1 = maskPenData.getPoint(0);
            int anchorHW = AppVariables.getUserConfigInt("AnchorWidth", 10) / 2;
            polygonP1.setLayoutX(imageView.getLayoutX() + p1.getX() * xRatio - anchorHW);
            polygonP1.setLayoutY(imageView.getLayoutY() + p1.getY() * yRatio - anchorHW);
        } else if (size > 1) {
            double lastx, lasty = -1, thisx, thisy;
            for (List<DoublePoint> lineData : maskPenData.getLines()) {
                List<Line> penLine = new ArrayList<>();
                lastx = -1;
                for (DoublePoint p : lineData) {
                    thisx = p.getX() * xRatio;
                    thisy = p.getY() * yRatio;
                    if (lastx >= 0) {
                        Line line = new Line(lastx, lasty, thisx, thisy);
                        line.setStrokeWidth(strokeWidth);
                        penLine.add(line);
                        maskPane.getChildren().add(line);
                        line.setLayoutX(imageView.getLayoutX());
                        line.setLayoutY(imageView.getLayoutY());
                    }
                    lastx = thisx;
                    lasty = thisy;
                }
                maskPenLines.add(penLine);
            }

        }
        return true;
    }

    @FXML
    public void handlerPressed(MouseEvent event) {

        scrollPane.setPannable(false);
        mouseX = event.getX();
        mouseY = event.getY();
//        logger.debug((int) mouseX + " " + (int) mouseY);
    }

    @FXML
    public void rectangleReleased(MouseEvent event) {
        if (isPickingColor.get() || maskRectangleLine == null || !maskRectangleLine.isVisible()
                || !maskPane.getChildren().contains(maskRectangleLine)
                || (mouseX == event.getX() && mouseY == event.getY())) {
            return;
        }
        scrollPane.setPannable(true);
        double offsetX = maskRectangleLine.getLayoutX() + event.getX() - mouseX - imageView.getLayoutX();
        double offsetY = maskRectangleLine.getLayoutY() + event.getY() - mouseY - imageView.getLayoutY();
        double x = offsetX * getImageWidth() / imageView.getBoundsInParent().getWidth();
        double y = offsetY * getImageHeight() / imageView.getBoundsInParent().getHeight();

        if (x <= 0 - maskRectangleData.getWidth()) {
            x = 0 - maskRectangleData.getWidth() + 1;
        }
        if (x >= getImageWidth()) {
            x = getImageWidth() - 1;
        }
        if (y <= 0 - maskRectangleData.getHeight()) {
            y = 0 - maskRectangleData.getHeight() + 1;
        }
        if (y >= getImageHeight()) {
            y = getImageHeight() - 1;
        }
        maskRectangleData = new DoubleRectangle(x, y,
                x + maskRectangleData.getWidth() - 1, y + maskRectangleData.getHeight() - 1);
        drawMaskRectangleLine();
    }

    @FXML
    public void circleReleased(MouseEvent event) {
        if (isPickingColor.get() || maskCircleLine == null || !maskCircleLine.isVisible()
                || !maskPane.getChildren().contains(maskCircleLine)
                || (mouseX == event.getX() && mouseY == event.getY())) {
            return;
        }
        scrollPane.setPannable(true);
        double offsetX = maskCircleLine.getLayoutX() + event.getX() - mouseX - imageView.getLayoutX();
        double offsetY = maskCircleLine.getLayoutY() + event.getY() - mouseY - imageView.getLayoutY();
        double x = offsetX * getImageWidth() / imageView.getBoundsInParent().getWidth();
        double y = offsetY * getImageHeight() / imageView.getBoundsInParent().getHeight();

        if (x <= 0 - maskCircleData.getRadius()) {
            x = 0 - maskCircleData.getRadius() + 1;
        }
        if (x >= getImageWidth() + maskCircleData.getRadius()) {
            x = getImageWidth() + maskCircleData.getRadius() - 1;
        }
        if (y <= 0 - maskCircleData.getRadius()) {
            y = 0 - maskCircleData.getRadius() + 1;
        }
        if (y >= getImageHeight() + maskCircleData.getRadius()) {
            y = getImageHeight() + maskCircleData.getRadius() - 1;
        }

        maskCircleData.setCenterX(x);
        maskCircleData.setCenterY(y);
        drawMaskCircleLine();
    }

    @FXML
    public void ellipseReleased(MouseEvent event) {
        if (isPickingColor.get() || maskEllipseLine == null || !maskEllipseLine.isVisible()
                || !maskPane.getChildren().contains(maskEllipseLine)
                || (mouseX == event.getX() && mouseY == event.getY())) {
            return;
        }
        scrollPane.setPannable(true);
        double offsetX = maskEllipseLine.getLayoutX() + event.getX() - mouseX - imageView.getLayoutX();
        double offsetY = maskEllipseLine.getLayoutY() + event.getY() - mouseY - imageView.getLayoutY();
        double x = offsetX * getImageWidth() / imageView.getBoundsInParent().getWidth();
        double y = offsetY * getImageHeight() / imageView.getBoundsInParent().getHeight();

        double rx = maskEllipseData.getRadiusX();
        double ry = maskEllipseData.getRadiusY();

        if (x <= 0 - rx) {
            x = 0 - rx + 1;
        }
        if (x >= getImageWidth() + rx) {
            x = getImageWidth() + rx - 1;
        }
        if (y <= 0 - ry) {
            y = 0 - ry + 1;
        }
        if (y >= getImageHeight() + ry) {
            y = getImageHeight() + ry - 1;
        }

        maskEllipseData = new DoubleEllipse(x - rx, y - ry, x + rx, y + ry);
        drawMaskEllipseLine();
    }

    @FXML
    public void polygonReleased(MouseEvent event) {
        if (isPickingColor.get() || maskPolygonLine == null || !maskPolygonLine.isVisible()
                || !maskPane.getChildren().contains(maskPolygonLine)
                || (mouseX == event.getX() && mouseY == event.getY())) {
            return;
        }
        scrollPane.setPannable(true);
        double offsetX = maskPolygonLine.getLayoutX() + event.getX() - mouseX - imageView.getLayoutX();
        double offsetY = maskPolygonLine.getLayoutY() + event.getY() - mouseY - imageView.getLayoutY();
        double x = offsetX * getImageWidth() / imageView.getBoundsInParent().getWidth();
        double y = offsetY * getImageHeight() / imageView.getBoundsInParent().getHeight();

        List<DoublePoint> maskPoints = maskPolygonData.getPoints();
        List<DoublePoint> points = new ArrayList<>();
        for (int i = 0; i < maskPoints.size(); ++i) {
            DoublePoint mp = maskPoints.get(i);
            points.add(new DoublePoint(mp.getX() + x, mp.getY() + y));
        }
        maskPolygonData.setAll(points);
        drawMaskPolygonLine();

    }

    @FXML
    public void topLeftHandlerReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor.get() || topLeftHandler == null || !topLeftHandler.isVisible()
                || !maskPane.getChildren().contains(topLeftHandler)) {
            return;
        }

        double offsetX = topLeftHandler.getLayoutX() + event.getX() - mouseX - imageView.getLayoutX();
        double offsetY = topLeftHandler.getLayoutY() + event.getY() - mouseY - imageView.getLayoutY();
        double x = offsetX * getImageWidth() / imageView.getBoundsInParent().getWidth();
        double y = offsetY * getImageHeight() / imageView.getBoundsInParent().getHeight();
        if (maskRectangleLine != null && maskRectangleLine.isVisible()) {

            if (x < maskRectangleData.getBigX() && y < maskRectangleData.getBigY()) {
                if (x >= getImageWidth() - 1) {
                    x = getImageWidth() - 2;
                }
                if (y >= getImageHeight() - 1) {
                    y = getImageHeight() - 2;
                }
                maskRectangleData.setSmallX(x);
                maskRectangleData.setSmallY(y);
                drawMaskRectangleLine();
            }
        }

    }

    @FXML
    public void topCenterHandlerReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor.get()) {
            return;
        }
        if (maskRectangleLine != null && maskRectangleLine.isVisible()) {
            double offsetY = topCenterHandler.getLayoutY() + event.getY() - mouseY - imageView.getLayoutY();
            double y = offsetY * getImageHeight() / imageView.getBoundsInParent().getHeight();
            if (y < maskRectangleData.getBigY()) {
                if (y >= getImageHeight() - 1) {
                    y = getImageHeight() - 2;
                }
                maskRectangleData.setSmallY(y);
                drawMaskRectangleLine();
            }
        }

        if (maskCircleLine != null && maskCircleLine.isVisible()) {
            double d = bottomCenterHandler.getLayoutY() - topCenterHandler.getLayoutY() - event.getY() + mouseY;
            if (d > 0) {
                d = d * getImageHeight() / imageView.getBoundsInParent().getHeight();
                maskCircleData.setRadius(d / 2);
                drawMaskCircleLine();
            }
        }

        if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
            double ry = (bottomCenterHandler.getLayoutY() - topCenterHandler.getLayoutY() - event.getY() + mouseY) / 2;
            if (ry > 0) {
                ry = ry * getImageHeight() / imageView.getBoundsInParent().getHeight();
                double rx = maskEllipseData.getRadiusX();
                double cx = maskEllipseData.getCenterX();
                double cy = maskEllipseData.getCenterY();

                maskEllipseData = new DoubleEllipse(cx - rx, cy - ry, cx + rx, cy + ry);
                drawMaskEllipseLine();
            }
        }

    }

    @FXML
    public void topRightHandlerReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor.get()) {
            return;
        }

        double offsetX = topRightHandler.getLayoutX() + event.getX() - mouseX - imageView.getLayoutX();
        double offsetY = topRightHandler.getLayoutY() + event.getY() - mouseY - imageView.getLayoutY();
        double x = offsetX * getImageWidth() / imageView.getBoundsInParent().getWidth();
        double y = offsetY * getImageHeight() / imageView.getBoundsInParent().getHeight();

        if (x > maskRectangleData.getSmallX() && y < maskRectangleData.getBigY()) {
            if (x <= 0) {
                x = 1;
            }
            if (y >= getImageHeight() - 1) {
                y = getImageHeight() - 2;
            }
            maskRectangleData.setBigX(x);
            maskRectangleData.setSmallY(y);
            drawMaskRectangleLine();
        }
    }

    @FXML
    public void bottomLeftHandlerReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor.get()) {
            return;
        }

        double offsetX = bottomLeftHandler.getLayoutX() + event.getX() - mouseX - imageView.getLayoutX();
        double offsetY = bottomLeftHandler.getLayoutY() + event.getY() - mouseY - imageView.getLayoutY();
        double x = offsetX * getImageWidth() / imageView.getBoundsInParent().getWidth();
        double y = offsetY * getImageHeight() / imageView.getBoundsInParent().getHeight();

        if (x < maskRectangleData.getBigX() && y > maskRectangleData.getSmallY()) {
            if (x >= getImageWidth() - 1) {
                x = getImageWidth() - 2;
            }
            if (y <= 0) {
                y = 1;
            }
            maskRectangleData.setSmallX(x);
            maskRectangleData.setBigY(y);
            drawMaskRectangleLine();
        }
    }

    @FXML
    public void bottomCenterHandlerReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor.get()) {
            return;
        }

        if (maskRectangleLine != null && maskRectangleLine.isVisible()) {
            double offsetY = bottomCenterHandler.getLayoutY() + event.getY() - mouseY - imageView.getLayoutY();
            double y = offsetY * getImageHeight() / imageView.getBoundsInParent().getHeight();

            if (y > maskRectangleData.getSmallY()) {
                if (y <= 0) {
                    y = 1;
                }
                maskRectangleData.setBigY(y);
                drawMaskRectangleLine();
            }
        }

        if (maskCircleLine != null && maskCircleLine.isVisible()) {
            double d = bottomCenterHandler.getLayoutY() + event.getY() - mouseY - topCenterHandler.getLayoutY();
            if (d > 0) {
                d = d * getImageHeight() / imageView.getBoundsInParent().getHeight();
                maskCircleData.setRadius(d / 2);
                drawMaskCircleLine();
            }
        }

        if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
            double ry = (bottomCenterHandler.getLayoutY() + event.getY() - mouseY - topCenterHandler.getLayoutY()) / 2;
            if (ry > 0) {
                ry = ry * getImageHeight() / imageView.getBoundsInParent().getHeight();
                double rx = maskEllipseData.getRadiusX();
                double cx = maskEllipseData.getCenterX();
                double cy = maskEllipseData.getCenterY();

                maskEllipseData = new DoubleEllipse(cx - rx, cy - ry, cx + rx, cy + ry);
                drawMaskEllipseLine();
            }
        }

    }

    @FXML
    public void bottomRightHandlerReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor.get()) {
            return;
        }

        double offsetX = bottomRightHandler.getLayoutX() + event.getX() - mouseX - imageView.getLayoutX();
        double offsetY = bottomRightHandler.getLayoutY() + event.getY() - mouseY - imageView.getLayoutY();
        double x = offsetX * getImageWidth() / imageView.getBoundsInParent().getWidth();
        double y = offsetY * getImageHeight() / imageView.getBoundsInParent().getHeight();

        if (x > maskRectangleData.getSmallX() && y > maskRectangleData.getSmallY()) {
            if (x <= 0) {
                x = 1;
            }
            if (y <= 0) {
                y = 1;
            }
            maskRectangleData.setBigX(x);
            maskRectangleData.setBigY(y);
            drawMaskRectangleLine();
        }
    }

    @FXML
    public void leftCenterHandlerReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor.get()) {
            return;
        }

        if (maskRectangleLine != null && maskRectangleLine.isVisible()) {
            double offsetX = leftCenterHandler.getLayoutX() + event.getX() - mouseX - imageView.getLayoutX();
            double x = offsetX * getImageWidth() / imageView.getBoundsInParent().getWidth();

            if (x < maskRectangleData.getBigX()) {
                if (x >= getImageWidth() - 1) {
                    x = getImageWidth() - 2;
                }
                maskRectangleData.setSmallX(x);
                drawMaskRectangleLine();
            }
        }

        if (maskCircleLine != null && maskCircleLine.isVisible()) {
            double d = rightCenterHandler.getLayoutX() - leftCenterHandler.getLayoutX() - event.getX() + mouseX;
            if (d > 0) {
                d = d * getImageWidth() / imageView.getBoundsInParent().getWidth();
                maskCircleData.setRadius(d / 2);
                drawMaskCircleLine();
            }
        }

        if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
            double rx = (rightCenterHandler.getLayoutX() - leftCenterHandler.getLayoutX() - event.getX() + mouseX) / 2;
            if (rx > 0) {
                rx = rx * getImageWidth() / imageView.getBoundsInParent().getWidth();
                double ry = maskEllipseData.getRadiusY();
                double cx = maskEllipseData.getCenterX();
                double cy = maskEllipseData.getCenterY();
                maskEllipseData = new DoubleEllipse(cx - rx, cy - ry, cx + rx, cy + ry);
                drawMaskEllipseLine();
            }
        }

    }

    @FXML
    public void rightCenterHandlerReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor.get()) {
            return;
        }

        if (maskRectangleLine != null && maskRectangleLine.isVisible()) {
            double offsetX = rightCenterHandler.getLayoutX() + event.getX() - mouseX - imageView.getLayoutX();
            double x = offsetX * getImageWidth() / imageView.getBoundsInParent().getWidth();

            if (x > maskRectangleData.getSmallX()) {
                if (x <= 0) {
                    x = 1;
                }
                maskRectangleData.setBigX(x);
                drawMaskRectangleLine();
            }
        }

        if (maskCircleLine != null && maskCircleLine.isVisible()) {
            double d = rightCenterHandler.getLayoutX() + event.getX() - mouseX - leftCenterHandler.getLayoutX();
            if (d > 0) {
                d = d * getImageWidth() / imageView.getBoundsInParent().getWidth();
                maskCircleData.setRadius(d / 2);
                drawMaskCircleLine();
            }
        }

        if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
            double rx = (rightCenterHandler.getLayoutX() + event.getX() - mouseX - leftCenterHandler.getLayoutX()) / 2;
            if (rx > 0) {
                rx = rx * getImageWidth() / imageView.getBoundsInParent().getWidth();
                double ry = maskEllipseData.getRadiusY();
                double cx = maskEllipseData.getCenterX();
                double cy = maskEllipseData.getCenterY();
                maskEllipseData = new DoubleEllipse(cx - rx, cy - ry, cx + rx, cy + ry);
                drawMaskEllipseLine();
            }
        }

    }

    @FXML
    public DoublePoint showXY(MouseEvent event) {
        if (needNotCoordinates
                || !AppVariables.ImagePopCooridnate
                || xyText == null || !xyText.isVisible()) {
            return null;
        }
        DoublePoint p = getImageXY(event, imageView);
        showXY(event, p);
        return p;
    }

    public DoublePoint showXY(MouseEvent event, DoublePoint p) {
        if (needNotCoordinates
                || !AppVariables.ImagePopCooridnate
                || xyText == null || !xyText.isVisible()) {
            return null;
        }
        if (p == null) {

            if (xyText != null) {
                xyText.setText("");
            }
            return null;
        }

        if (xyText != null && xyText.isVisible()) {
            PixelReader pixelReader = imageView.getImage().getPixelReader();
            Color color = pixelReader.getColor((int) p.getX(), (int) p.getY());
            String s = (int) Math.round(p.getX()) + "," + (int) Math.round(p.getY()) + "\n"
                    + FxmlColor.colorDisplaySimple(color);
            xyText.setText(s);
            xyText.setX(event.getX() + 10);
            xyText.setY(event.getY());
        }
        return p;
    }

    // Clicking events happen on maskPane but not on imageview itslef
    public DoublePoint getImageXY(MouseEvent event, ImageView view) {
        if (view.getImage() == null) {
            return null;
        }
        double offsetX = event.getX() - view.getLayoutX() - view.getX();
        double offsetY = event.getY() - view.getLayoutY() - view.getY();
        if (offsetX < 0 || offsetX >= view.getBoundsInParent().getWidth()
                || offsetY < 0 || offsetY >= view.getBoundsInParent().getHeight()) {
            return null;
        }
        double x = offsetX * getImageWidth() / view.getBoundsInParent().getWidth();
        double y = offsetY * getImageHeight() / view.getBoundsInParent().getHeight();
        return new DoublePoint(x, y);
    }

    public IntPoint getImageXYint(MouseEvent event, ImageView view) {
        DoublePoint p = getImageXY(event, view);
        if (p == null) {
            return null;
        }
        int ix = (int) Math.round(p.getX());
        int iy = (int) Math.round(p.getY());

        return new IntPoint(ix, iy);
    }

    @FXML
    public void paneClicked(MouseEvent event) {
        if (isPickingColor.get() || imageView.getImage() == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        if (event.getClickCount() > 1) {  // Notice: Double click always trigger single click at first
            doubleClickedRectangle(event);
            doubleClickedCircle(event);
            doubleClickedEllipse(event);

        } else if (event.getClickCount() == 1) {
            singleClickedPolygonLine(event);
        }
    }

    protected void singleClickedPolygonLine(MouseEvent event) {
        if (maskPolygonLine == null || !maskPolygonLine.isVisible()) {
            return;
        }

        DoublePoint p = getImageXY(event, imageView);
        if (p == null) {
            return;
        }
        if (event.getButton() == MouseButton.PRIMARY) {
            maskPolygonData.add(p.getX(), p.getY());
            drawMaskPolygonLine();

        } else if (event.getButton() == MouseButton.SECONDARY && maskPolygonData.getSize() > 2) {
            List<DoublePoint> maskPoints = maskPolygonData.getPoints();
            DoublePoint p0 = maskPoints.get(0);
            double offsetX = p.getX() - p0.getX();
            double offsetY = p.getY() - p0.getY();

            if (offsetX != 0 || offsetY != 0) {
                maskPolygonData = maskPolygonData.move(offsetX, offsetY);
                drawMaskPolygonLine();
            }
        }

    }

    protected void doubleClickedRectangle(MouseEvent event) {
        if (maskRectangleLine == null || !maskRectangleLine.isVisible()) {
            return;
        }
        DoublePoint p = getImageXY(event, imageView);
        if (p != null) {
            double x = p.getX();
            double y = p.getY();
            if (event.getButton() == MouseButton.PRIMARY) {

                if (x < maskRectangleData.getBigX() && y < maskRectangleData.getBigY()) {
                    maskRectangleData.setSmallX(x);
                    maskRectangleData.setSmallY(y);
                    drawMaskRectangleLine();
                }

            } else if (event.getButton() == MouseButton.SECONDARY) {

                if (x > maskRectangleData.getSmallX() && y > maskRectangleData.getSmallY()) {
                    maskRectangleData.setBigX(x);
                    maskRectangleData.setBigY(y);
                    drawMaskRectangleLine();
                }

            }
        }
    }

    protected void doubleClickedCircle(MouseEvent event) {
        if (maskCircleLine == null || !maskCircleLine.isVisible()) {
            return;
        }
        DoublePoint p = getImageXY(event, imageView);
        if (p == null) {
            return;
        }
        double x = p.getX();
        double y = p.getY();
        if (event.getButton() == MouseButton.PRIMARY) {

            maskCircleData.setCenterX(x);
            maskCircleData.setCenterY(y);
            drawMaskCircleLine();

        } else if (event.getButton() == MouseButton.SECONDARY) {

            if (x != maskCircleData.getCenterX() || y != maskCircleData.getCenterY()) {
                double dx = x - maskCircleData.getCenterX();
                double dy = y - maskCircleData.getCenterY();
                maskCircleData.setRadius(Math.sqrt(dx * dx + dy * dy));
                drawMaskCircleLine();
            }

        }
    }

    protected void doubleClickedEllipse(MouseEvent event) {
        if (maskEllipseLine == null || !maskEllipseLine.isVisible()) {
            return;
        }
        DoublePoint p = getImageXY(event, imageView);
        if (p == null) {
            return;
        }
        double x = p.getX();
        double y = p.getY();
        if (event.getButton() == MouseButton.PRIMARY) {
            if (x != maskEllipseData.getCenterX()) {
                double xr = Math.abs(x - maskEllipseData.getCenterX());
                maskEllipseData = new DoubleEllipse(
                        maskEllipseData.getCenterX() - xr,
                        maskEllipseData.getCenterY() - maskEllipseData.getRadiusY(),
                        maskEllipseData.getCenterX() + xr,
                        maskEllipseData.getCenterY() + maskEllipseData.getRadiusY()
                );
                drawMaskEllipseLine();
            }

        } else if (event.getButton() == MouseButton.SECONDARY) {
            if (y != maskEllipseData.getCenterY()) {
                double yr = Math.abs(y - maskEllipseData.getCenterY());
                maskEllipseData = new DoubleEllipse(
                        maskEllipseData.getCenterX() - maskEllipseData.getRadiusX(),
                        maskEllipseData.getCenterY() - yr,
                        maskEllipseData.getCenterX() + maskEllipseData.getRadiusX(),
                        maskEllipseData.getCenterY() + yr
                );
                drawMaskEllipseLine();
            }

        }
    }

}
