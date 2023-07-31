package mara.mybox.controller;

import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import static javafx.scene.shape.StrokeLineCap.ROUND;
import static javafx.scene.shape.StrokeLineCap.SQUARE;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoubleLine;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.data.ShapeStyle;
import static mara.mybox.data.ShapeStyle.DefaultControlColor;
import static mara.mybox.data.ShapeStyle.DefaultStrokeColor;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-6-29
 * @License Apache License Version 2.0
 */
public abstract class ControlShapeOptions extends BaseController {

    protected BaseImageController imageController;
    protected DoubleShape currentShape;
    protected ShapeStyle style;
    protected ChangeListener<Boolean> shapeDataChangeListener;
    public ShapeType shapeType = null;

    public enum ShapeType {
        Line, Rectangle, Circle, Ellipse, Polygon, Polyline, Lines,
        Cubic, Quadratic, Arc, Path, Text;
    }

    @FXML
    protected RadioButton lineRadio, rectangleRadio, circleRadio, ellipseRadio,
            polylineRadio, polygonRadio, linesRadio,
            arcRadio, quadraticRadio, cubicRadio, pathRadio,
            linecapSquareRadio, linecapRoundRadio, linecapButtRadio;
    @FXML
    protected VBox shapeBox, shapeOutBox, pointsBox, linesBox,
            rectangleBox, circleBox, ellipseBox, lineBox, pathBox;
    @FXML
    protected TabPane shapesPane;
    @FXML
    protected ToggleGroup typeGroup, linecap;
    @FXML
    protected TextField circleXInput, circleYInput, circleRadiusInput,
            rectXInput, rectYInput, rectWidthInput, rectHeightInput,
            ellipseXInput, ellipseYInput, ellipseXRadiusInput, ellipseYRadiusInput,
            lineX1Input, lineY1Input, lineX2Input, lineY2Input,
            dashInput;
    @FXML
    protected ControlPoints pointsController;
    @FXML
    protected ControlLines linesController;
    @FXML
    protected ControlPath2D pathController;
    @FXML
    protected ComboBox<String> strokeWidthSelector, strokeOpacitySelector, fillOpacitySelector,
            controlSizeSelector, arcSizeSelector;
    @FXML
    protected CheckBox fillCheck, dashCheck;
    @FXML
    protected ControlColorSet strokeColorController, ControlColorController, fillColorController;

    public void setParameters(BaseImageController imageController) {
        try {
            this.imageController = imageController;

            shapeDataChangeListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                    shapeDataChanged();
                }
            };

            initStyleControls();
            initShapeControls();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initShapeControls() {
        try {
            if (shapeOutBox != null) {
                shapeOutBox.getChildren().remove(shapesPane);
                refreshStyle(shapeOutBox);
            }

            if (typeGroup != null) {
                typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue v, Toggle ov, Toggle nv) {
                        switchShape();
                    }
                });
            }

            if (pointsController != null) {
                pointsController.tableData.addListener(new ListChangeListener<DoublePoint>() {
                    @Override
                    public void onChanged(ListChangeListener.Change<? extends DoublePoint> c) {
                        goShape();
                    }
                });
            }

            if (linesController != null) {
                linesController.tableData.addListener(new ListChangeListener<List<DoublePoint>>() {
                    @Override
                    public void onChanged(ListChangeListener.Change<? extends List<DoublePoint>> c) {
                        goShape();
                    }
                });
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initStyleControls() {
        try {
            style = new ShapeStyle(interfaceName);

            isSettingValues = true;
            if (strokeWidthSelector != null) {
                strokeWidthSelector.getItems().addAll(Arrays.asList(
                        "3", "0", "1", "2", "5", "8", "10", "15", "25", "30", "50", "80"));
                strokeWidthSelector.setValue(style.getStrokeWidth() + "");
                strokeWidthSelector.valueProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue v, String ov, String nv) {
                        if (isSettingValues) {
                            return;
                        }
                        float f;
                        try {
                            f = Float.parseFloat(strokeWidthSelector.getValue());
                        } catch (Exception e) {
                            f = -1;
                        }
                        style.setStrokeWidth(f);
                        goStyle();
                    }
                });
            }

            if (strokeColorController != null) {
                strokeColorController.init(this, interfaceName + "StrokeColor", Color.web(DefaultStrokeColor));
                strokeColorController.initColor(style.getStrokeColor());
                strokeColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                    @Override
                    public void changed(ObservableValue v, Paint ov, Paint nv) {
                        if (isSettingValues) {
                            return;
                        }
                        style.setStrokeColor(strokeColorController.color());
                        goStyle();
                    }
                });
            }

            if (fillColorController != null) {
                fillColorController.init(this, interfaceName + "FillColor", Color.TRANSPARENT);
                fillColorController.initColor(style.getFillColor());
                fillColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                    @Override
                    public void changed(ObservableValue v, Paint ov, Paint nv) {
                        if (isSettingValues) {
                            return;
                        }
                        style.setStrokeColor(fillColorController.color());
                        goStyle();
                    }
                });
            }
            if (fillCheck != null) {
                fillCheck.setSelected(style.isIsFillColor());
                fillCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                        if (isSettingValues) {
                            return;
                        }
                        style.setIsFillColor(fillCheck.isSelected());
                        goStyle();
                    }
                });
            }

            if (fillOpacitySelector != null) {
                fillOpacitySelector.getItems().addAll(
                        "0.3", "0.5", "0", "1.0", "0.05", "0.02", "0.1", "0.2", "0.8", "0.6", "0.4", "0.7", "0.9"
                );
                fillOpacitySelector.setValue(style.getFillOpacity() + "");
                fillOpacitySelector.valueProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue v, String ov, String nv) {
                        if (isSettingValues) {
                            return;
                        }
                        float f;
                        try {
                            f = Float.parseFloat(fillOpacitySelector.getValue());
                        } catch (Exception e) {
                            f = -1;
                        }
                        style.setFillOpacity(f);
                        goStyle();
                    }
                });
            }
            if (strokeOpacitySelector != null) {
                strokeOpacitySelector.getItems().addAll(
                        "1.0", "0.3", "0.5", "0", "0.05", "0.02", "0.1", "0.2", "0.8", "0.6", "0.4", "0.7", "0.9"
                );
                strokeOpacitySelector.setValue(style.getStrokeOpacity() + "");
                strokeOpacitySelector.valueProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue v, String ov, String nv) {
                        if (isSettingValues) {
                            return;
                        }
                        float f;
                        try {
                            f = Float.parseFloat(strokeOpacitySelector.getValue());
                        } catch (Exception e) {
                            f = -1;
                        }
                        style.setStrokeOpacity(f);
                        goStyle();
                    }
                });
            }

            if (arcSizeSelector != null) {
                arcSizeSelector.getItems().setAll(Arrays.asList("0", "2", "5", "10", "15", "30", "40", "50"));
                arcSizeSelector.setValue(style.getRoundArc() + "");
                arcSizeSelector.valueProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue v, String ov, String nv) {
                        if (isSettingValues) {
                            return;
                        }
                        int a;
                        try {
                            a = Integer.parseInt(arcSizeSelector.getValue());
                        } catch (Exception e) {
                            a = -1;
                        }
                        style.setRoundArc(a);
                        goStyle();
                    }
                });
            }

            if (dashCheck != null) {
                dashCheck.setSelected(style.isIsStrokeDash());
                dashCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                        if (isSettingValues) {
                            return;
                        }
                        style.setIsStrokeDash(dashCheck.isSelected());
                        goStyle();
                    }
                });
            }

            if (dashInput != null) {
                dashInput.setText(style.getStrokeDashText());
                dashInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                        if (isSettingValues || nv) {
                            return;
                        }
                        style.setStrokeDashText(dashInput.getText());
                        goStyle();
                    }
                });
            }

            if (linecapSquareRadio != null) {
                StrokeLineCap lineCap = style.getLineCap();
                if (null == lineCap) {
                    linecapButtRadio.setSelected(true);
                } else {
                    switch (lineCap) {
                        case SQUARE:
                            linecapSquareRadio.setSelected(true);
                            break;
                        case ROUND:
                            linecapRoundRadio.setSelected(true);
                            break;
                        default:
                            linecapButtRadio.setSelected(true);
                            break;
                    }
                }
                linecap.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue v, Toggle ov, Toggle nv) {
                        if (isSettingValues) {
                            return;
                        }
                        if (linecapSquareRadio.isSelected()) {
                            style.setLineCap(StrokeLineCap.SQUARE);
                        } else if (linecapRoundRadio.isSelected()) {
                            style.setLineCap(StrokeLineCap.ROUND);
                        } else if (linecapButtRadio.isSelected()) {
                            style.setLineCap(StrokeLineCap.BUTT);
                        } else {
                            style.setLineCap(null);
                        }
                        goStyle();
                    }
                });
            }

            if (ControlColorController != null) {
                ControlColorController.init(this, interfaceName + "ControlColor", Color.web(DefaultControlColor));
                ControlColorController.initColor(style.getControlColor());
                ControlColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                    @Override
                    public void changed(ObservableValue v, Paint ov, Paint nv) {
                        if (isSettingValues) {
                            return;
                        }
                        style.setControlColor(ControlColorController.color());
                        goStyle();
                    }
                });
            }

            if (controlSizeSelector != null) {
                controlSizeSelector.getItems().setAll(Arrays.asList("10", "15", "20", "25", "30", "40", "50"));
                controlSizeSelector.setValue(style.getControlSize() + "");
                controlSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        float f;
                        try {
                            f = Float.parseFloat(controlSizeSelector.getValue());
                        } catch (Exception e) {
                            f = -1;
                        }
                        style.setControlSize(f);
                        goStyle();
                    }
                });
            }

            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void switchShape() {
        if (isSettingValues) {
            return;
        }
        imageController.clearMaskShapes();
        if (showShape()) {
            setShapeControls();
        }
    }


    /*
        values
     */
    public Image currentImage() {
        return imageController != null ? imageController.image : null;
    }

    public Shape currentShape() {
        if (imageController == null || shapeType == null) {
            return null;
        }
        switch (shapeType) {
            case Rectangle:
                return imageController.maskRectangle;
            case Circle:
                return imageController.maskCircle;
            case Ellipse:
                return imageController.maskEllipse;
            case Line:
                return imageController.maskLine;
            case Polyline:
                return imageController.maskPolyline;
            case Polygon:
                return imageController.maskPolygon;
            case Lines:
                return null;
            case Path:
                return imageController.svgPath;
            default:
                return null;
        }
    }

    public double scale(double v) {
        return DoubleTools.scale(v, 2);
    }

    /*
        load
     */
    public boolean showShape() {
        try {
            if (imageController == null) {
                return false;
            }
            shapeType = null;
            imageController.shapeStyle = style;

            if (rectangleRadio != null && rectangleRadio.isSelected()) {
                imageController.showMaskRectangle();
                shapeType = ShapeType.Rectangle;

            } else if (circleRadio != null && circleRadio.isSelected()) {
                imageController.showMaskCircle();
                shapeType = ShapeType.Circle;

            } else if (ellipseRadio != null && ellipseRadio.isSelected()) {
                imageController.showMaskEllipse();
                shapeType = ShapeType.Ellipse;

            } else if (lineRadio != null && lineRadio.isSelected()) {
                imageController.showMaskLine();
                shapeType = ShapeType.Line;

            } else if (polylineRadio != null && polylineRadio.isSelected()) {
                imageController.showMaskPolyline();
                shapeType = ShapeType.Polyline;

            } else if (polygonRadio != null && polygonRadio.isSelected()) {
                imageController.showMaskPolygon();
                shapeType = ShapeType.Polygon;

            } else if (linesRadio != null && linesRadio.isSelected()) {
                imageController.showMaskLines();
                shapeType = ShapeType.Lines;

            } else if (pathRadio != null && pathRadio.isSelected()) {
                imageController.showPath();
                shapeType = ShapeType.Path;

            } else {
                popError(message("InvalidData"));
            }
            return shapeType != null;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void setShapeControls() {
        try {
            shapeBox.getChildren().clear();
            if (imageController == null || shapeType == null) {
                return;
            }
            isSettingValues = true;
            switch (shapeType) {
                case Rectangle:
                    shapeBox.getChildren().addAll(rectangleBox, goButton);
                    rectXInput.setText(scale(imageController.maskRectangleData.getSmallX()) + "");
                    rectYInput.setText(scale(imageController.maskRectangleData.getSmallY()) + "");
                    rectWidthInput.setText(scale(imageController.maskRectangleData.getWidth()) + "");
                    rectHeightInput.setText(scale(imageController.maskRectangleData.getHeight()) + "");
                    break;
                case Circle:
                    shapeBox.getChildren().addAll(circleBox, goButton);
                    circleXInput.setText(scale(imageController.maskCircleData.getCenterX()) + "");
                    circleYInput.setText(scale(imageController.maskCircleData.getCenterY()) + "");
                    circleRadiusInput.setText(scale(imageController.maskCircleData.getRadius()) + "");
                    break;
                case Ellipse:
                    shapeBox.getChildren().addAll(ellipseBox, goButton);
                    ellipseXInput.setText(scale(imageController.maskEllipseData.getCenterX()) + "");
                    ellipseYInput.setText(scale(imageController.maskEllipseData.getCenterY()) + "");
                    ellipseXRadiusInput.setText(scale(imageController.maskEllipseData.getRadiusX()) + "");
                    ellipseYRadiusInput.setText(scale(imageController.maskEllipseData.getRadiusY()) + "");
                    break;
                case Line:
                    shapeBox.getChildren().addAll(lineBox, goButton);
                    lineX1Input.setText(scale(imageController.maskLineData.getStartX()) + "");
                    lineY1Input.setText(scale(imageController.maskLineData.getStartY()) + "");
                    lineX2Input.setText(scale(imageController.maskLineData.getEndX()) + "");
                    lineY2Input.setText(scale(imageController.maskLineData.getEndY()) + "");
                    break;
                case Polyline:
                    shapeBox.getChildren().add(pointsBox);
                    VBox.setVgrow(pointsBox, Priority.ALWAYS);
                    pointsController.loadList(imageController.maskPolylineData.getPoints());
                    break;
                case Polygon:
                    shapeBox.getChildren().add(pointsBox);
                    VBox.setVgrow(pointsBox, Priority.ALWAYS);
                    pointsController.loadList(imageController.maskPolygonData.getPoints());
                    break;
                case Lines:
                    shapeBox.getChildren().add(linesBox);
                    VBox.setVgrow(linesBox, Priority.ALWAYS);
                    linesController.loadList(imageController.maskLinesData.getLinePoints());
                    break;
                case Path:
                    shapeBox.getChildren().add(pathBox);
                    VBox.setVgrow(pathBox, Priority.ALWAYS);
                    pathController.loadPath(imageController.pathData.getContent());
                    break;
                default:
                    popError(message("InvalidData"));
            }
            refreshStyle(shapeBox);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        isSettingValues = false;
    }

    public void shapeDataChanged() {
        setShapeControls();
    }

    public void addListener() {
        imageController.maskShapeDataChanged.addListener(shapeDataChangeListener);
    }

    public void removeListener() {
        imageController.maskShapeDataChanged.removeListener(shapeDataChangeListener);
    }

    /*
        pick
     */
    public boolean pickShape() {
        try {
            if (imageController == null || shapeType == null) {
                popError(message("InvalidData"));
                return false;
            }
            switch (shapeType) {
                case Rectangle:
                    return pickRect();
                case Circle:
                    return pickCircle();
                case Ellipse:
                    return pickEllipse();
                case Line:
                    return pickLine();
                case Polyline:
                    return pickPolyline();
                case Polygon:
                    return pickPolygon();
                case Lines:
                    return pickLines();
                case Path:
                    return pickPath();
                default:
                    break;
            }
            popError(message("InvalidData"));
            return false;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean pickRect() {
        try {
            float x, y, w, h;
            try {
                x = Float.parseFloat(rectXInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x");
                return false;
            }
            try {
                y = Float.parseFloat(rectYInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y");
                return false;
            }
            try {
                w = Float.parseFloat(rectWidthInput.getText());
            } catch (Exception e) {
                w = -1f;
            }
            if (w <= 0) {
                popError(message("InvalidParameter") + ": " + message("Width"));
                return false;
            }
            try {
                h = Float.parseFloat(rectHeightInput.getText());
            } catch (Exception e) {
                h = -1f;
            }
            if (h <= 0) {
                popError(message("InvalidParameter") + ": " + message("Height"));
                return false;
            }
            imageController.maskRectangleData = new DoubleRectangle(x, y, x + w - 1, y + h - 1);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean pickCircle() {
        try {
            float x, y, r;
            try {
                x = Float.parseFloat(circleXInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x");
                return false;
            }
            try {
                y = Float.parseFloat(circleYInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y");
                return false;
            }
            try {
                r = Float.parseFloat(circleRadiusInput.getText());
            } catch (Exception e) {
                r = -1f;
            }
            if (r <= 0) {
                popError(message("InvalidParameter") + ": " + message("Radius"));
                return false;
            }
            imageController.maskCircleData = new DoubleCircle(x, y, r);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean pickEllipse() {
        try {
            float x, y, rx, ry;
            try {
                x = Float.parseFloat(ellipseXInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x");
                return false;
            }
            try {
                y = Float.parseFloat(ellipseYInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y");
                return false;
            }
            try {
                rx = Float.parseFloat(ellipseXRadiusInput.getText());
            } catch (Exception e) {
                rx = -1f;
            }
            if (rx <= 0) {
                popError(message("InvalidParameter") + ": " + message("Radius"));
                return false;
            }
            try {
                ry = Float.parseFloat(ellipseYRadiusInput.getText());
            } catch (Exception e) {
                ry = -1f;
            }
            if (ry <= 0) {
                popError(message("InvalidParameter") + ": " + message("Radius"));
                return false;
            }
            imageController.maskEllipseData = new DoubleEllipse(x, y, x + rx * 2 - 1, y + ry * 2 - 1);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean pickLine() {
        try {
            float x1, y1, x2, y2;
            try {
                x1 = Float.parseFloat(lineX1Input.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x1");
                return false;
            }
            try {
                y1 = Float.parseFloat(lineY1Input.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y1");
                return false;
            }
            try {
                x2 = Float.parseFloat(lineX2Input.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x2");
                return false;
            }
            try {
                y2 = Float.parseFloat(lineY2Input.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y2");
                return false;
            }
            imageController.maskLineData = new DoubleLine(x1, y1, x2, y2);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean pickPolyline() {
        try {
            imageController.maskPolylineData.setAll(pointsController.tableData);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean pickPolygon() {
        try {
            imageController.maskPolygonData.setAll(pointsController.tableData);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean pickLines() {
        try {
            imageController.maskLinesData.setLinePoints(linesController.tableData);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean pickPath() {
        try {
            String d = pathController.pickPath(" ");
            if (d == null || d.isBlank()) {
                popError(message("NoData"));
                return false;
            }
            imageController.svgPath.setContent(d);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    /*
        draw
     */
    public void redrawShape() {
        if (imageController == null || shapeType == null) {
            return;
        }
        switch (shapeType) {
            case Rectangle:
                drawRectangle();
                break;
            case Circle:
                drawCircle();
                break;
            case Ellipse:
                drawEllipse();
                break;
            case Line:
                drawLine();
                break;
            case Polyline:
                drawPolyline();
                break;
            case Polygon:
                drawPolygon();
                break;
            case Lines:
                drawLines();
                break;
            case Path:
                drawPath();
                break;
            default:
                return;
        }
    }

    public void drawRectangle() {
        MyBoxLog.debug("drawRectangle");
        imageController.showMaskRectangle();
    }

    public void drawCircle() {
        imageController.showMaskCircle();
    }

    public void drawEllipse() {
        imageController.showMaskEllipse();
    }

    public void drawPolygon() {
        imageController.showMaskPolygon();
    }

    public void drawPolyline() {
        imageController.showMaskPolyline();
    }

    public void drawLine() {
        imageController.showMaskLine();
    }

    public void drawLines() {
        imageController.showMaskLines();
    }

    /*
        action
     */
    @FXML
    @Override
    public void goAction() {
        goShape();
    }

    public void goShape() {
        if (pickShape()) {
            redrawShape();
        }
    }

    public void goStyle() {
        redrawShape();
    }

    /*
        path
     */
    public void drawPath() {
        imageController.drawPath();
    }

}
