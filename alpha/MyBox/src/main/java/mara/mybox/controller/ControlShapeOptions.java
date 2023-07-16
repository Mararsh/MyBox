package mara.mybox.controller;

import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import mara.mybox.controller.BaseImageController_Shapes.ShapeType;
import static mara.mybox.controller.BaseImageController_Shapes.ShapeType.Circle;
import static mara.mybox.controller.BaseImageController_Shapes.ShapeType.Ellipse;
import static mara.mybox.controller.BaseImageController_Shapes.ShapeType.Line;
import static mara.mybox.controller.BaseImageController_Shapes.ShapeType.Lines;
import static mara.mybox.controller.BaseImageController_Shapes.ShapeType.Path;
import static mara.mybox.controller.BaseImageController_Shapes.ShapeType.Polygon;
import static mara.mybox.controller.BaseImageController_Shapes.ShapeType.Polyline;
import static mara.mybox.controller.BaseImageController_Shapes.ShapeType.Rectangle;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoubleLine;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.data.ShapeStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

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

    @FXML
    protected RadioButton rectangleRadio, circleRadio, ellipseRadio,
            lineRadio, polylineRadio, polygonRadio, linesRadio, pathRadio,
            linecapSquareRadio, linecapRoundRadio, linecapButtRadio;
    @FXML
    protected VBox shapeBox, shapeOutBox, pointsBox, linesBox,
            rectangleBox, circleBox, ellipseBox, lineBox, pathBox;
    @FXML
    protected TabPane shapesPane;
    @FXML
    protected ToggleGroup typeGroup;
    @FXML
    protected TextField circleXInput, circleYInput, circleRadiusInput,
            rectXInput, rectYInput, rectWidthInput, rectHeightInput,
            ellipseXInput, ellipseYInput, ellipseXRadiusInput, ellipseYRadiusInput,
            lineX1Input, lineY1Input, lineX2Input, lineY2Input,
            dashInput;
    @FXML
    protected TextArea pathArea;
    @FXML
    protected ControlPoints pointsController;
    @FXML
    protected ControlLines linesController;
    @FXML
    protected ComboBox<String> strokeWidthSelector, strokeOpacitySelector, fillOpacitySelector,
            anchorSizeSelector, arcSizeSelector;
    @FXML
    protected CheckBox fillCheck, dashCheck;
    @FXML
    protected ControlColorSet strokeColorController, anchorColorController, fillColorController;

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

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initStyleControls() {
        try {
            style = new ShapeStyle(interfaceName);

            if (strokeWidthSelector != null) {
                strokeWidthSelector.getItems().addAll(Arrays.asList(
                        "3", "0", "1", "2", "5", "8", "10", "15", "25", "30", "50", "80"));
                strokeWidthSelector.setValue("2");
                strokeWidthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        try {
                            float v = Float.parseFloat(newValue);
                            if (v >= 0) {
                                ValidationTools.setEditorNormal(strokeWidthSelector);
                            } else {
                                ValidationTools.setEditorBadStyle(strokeWidthSelector);
                            }
                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(strokeWidthSelector);
                        }
                    }
                });
            }

            if (strokeColorController != null) {
                strokeColorController.init(this, baseName + "StrokeColor", Color.BLACK);
            }

            if (fillColorController != null) {
                fillColorController.init(this, baseName + "FillColor", Color.TRANSPARENT);
            }
            if (fillCheck != null) {
                fillCheck.setSelected(false);
            }

            if (fillOpacitySelector != null) {
                fillOpacitySelector.getItems().addAll(
                        "0.3", "0.5", "0", "1.0", "0.05", "0.02", "0.1", "0.2", "0.8", "0.6", "0.4", "0.7", "0.9"
                );
                fillOpacitySelector.setValue("0.3");
                fillOpacitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        try {
                            float v = Float.parseFloat(newValue);
                            if (v >= 0) {
                                fillOpacitySelector.getEditor().setStyle(null);
                            } else {
                                fillOpacitySelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            fillOpacitySelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    }
                });
            }
            if (strokeOpacitySelector != null) {
                strokeOpacitySelector.getItems().addAll(
                        "1.0", "0.3", "0.5", "0", "0.05", "0.02", "0.1", "0.2", "0.8", "0.6", "0.4", "0.7", "0.9"
                );
                strokeOpacitySelector.setValue("0.3");
                strokeOpacitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        try {
                            float v = Float.parseFloat(newValue);
                            if (v >= 0) {
                                strokeOpacitySelector.getEditor().setStyle(null);
                            } else {
                                strokeOpacitySelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            strokeOpacitySelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    }
                });
            }

            if (anchorSizeSelector != null) {
                anchorSizeSelector.getItems().setAll(Arrays.asList("10", "15", "20", "25", "30", "40", "50"));
                anchorSizeSelector.setValue("10");
                anchorSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        try {
                            float v = Float.parseFloat(newValue);
                            if (v >= 0) {
                                ValidationTools.setEditorNormal(anchorSizeSelector);
                            } else {
                                ValidationTools.setEditorBadStyle(anchorSizeSelector);
                            }
                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(anchorSizeSelector);
                        }
                    }
                });
            }

            if (arcSizeSelector != null) {
                arcSizeSelector.getItems().setAll(Arrays.asList("0", "2", "5", "10", "15", "30", "40", "50"));
                arcSizeSelector.setValue("0");
                arcSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                ValidationTools.setEditorNormal(arcSizeSelector);
                            } else {
                                ValidationTools.setEditorBadStyle(arcSizeSelector);
                            }
                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(arcSizeSelector);
                        }
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void switchShape() {
        if (isSettingValues) {
            return;
        }
        imageController.clearMask();
        if (!showShape()) {
            return;
        }
        setShapeControls();
        setStyleControls();
    }


    /*
        values
     */
    public Image currentImage() {
        return imageController != null ? imageController.image : null;
    }

    public Shape currentShape() {
        if (imageController == null || imageController.shapeType == null) {
            return null;
        }
        switch (imageController.shapeType) {
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
            imageController.shapeType = null;
            if (rectangleRadio != null && rectangleRadio.isSelected()) {
                imageController.showMaskRectangle();

            } else if (circleRadio != null && circleRadio.isSelected()) {
                imageController.showMaskCircle();

            } else if (ellipseRadio != null && ellipseRadio.isSelected()) {
                imageController.showMaskEllipse();

            } else if (lineRadio != null && lineRadio.isSelected()) {
                imageController.showMaskLine();

            } else if (polylineRadio != null && polylineRadio.isSelected()) {
                imageController.showMaskPolyline();

            } else if (polygonRadio != null && polygonRadio.isSelected()) {
                imageController.showMaskPolygon();

            } else if (linesRadio != null && linesRadio.isSelected()) {
                imageController.showMaskLines();

            } else if (pathRadio != null && pathRadio.isSelected()) {
                imageController.drawPath();

            } else {
                popError(message("InvalidData"));
                return false;
            }
            return imageController.shapeType != null;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void setShapeControls() {
        try {
            shapeBox.getChildren().clear();
            if (imageController == null || imageController.shapeType == null) {
                return;
            }
            switch (imageController.shapeType) {
                case Rectangle:
                    shapeBox.getChildren().add(rectangleBox);
                    rectXInput.setText(scale(imageController.maskRectangleData.getSmallX()) + "");
                    rectYInput.setText(scale(imageController.maskRectangleData.getSmallY()) + "");
                    rectWidthInput.setText(scale(imageController.maskRectangleData.getWidth()) + "");
                    rectHeightInput.setText(scale(imageController.maskRectangleData.getHeight()) + "");
                    break;
                case Circle:
                    shapeBox.getChildren().add(circleBox);
                    circleXInput.setText(scale(imageController.maskCircleData.getCenterX()) + "");
                    circleYInput.setText(scale(imageController.maskCircleData.getCenterY()) + "");
                    circleRadiusInput.setText(scale(imageController.maskCircleData.getRadius()) + "");
                    break;
                case Ellipse:
                    shapeBox.getChildren().add(ellipseBox);
                    ellipseXInput.setText(scale(imageController.maskEllipseData.getCenterX()) + "");
                    ellipseYInput.setText(scale(imageController.maskEllipseData.getCenterY()) + "");
                    ellipseXRadiusInput.setText(scale(imageController.maskEllipseData.getRadiusX()) + "");
                    ellipseYRadiusInput.setText(scale(imageController.maskEllipseData.getRadiusY()) + "");
                    break;
                case Line:
                    shapeBox.getChildren().add(lineBox);
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
                    pathArea.setText(imageController.svgPath.getContent());
                    break;
                default:
                    popError(message("InvalidData"));
                    return;
            }

            refreshStyle(shapeBox);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setStyleControls() {
        try {
            if (imageController == null || style == null) {
                return;
            }
            if (strokeColorController != null) {
                strokeColorController.setColor(style.getStrokeColor());
            }
            if (strokeWidthSelector != null) {
                strokeWidthSelector.setValue(style.getStrokeWidth() + "");
            }
            if (strokeOpacitySelector != null) {
                strokeOpacitySelector.setValue(style.getStrokeOpacity() + "");
            }

            if (fillCheck != null) {
                fillCheck.setSelected(style.isIsFillColor());
            }
            if (fillColorController != null) {
                fillColorController.setColor(style.getFillColor());
            }
            if (fillOpacitySelector != null) {
                fillOpacitySelector.setValue(style.getFillOpacity() + "");
            }

            if (anchorColorController != null) {
                anchorColorController.setColor(style.getAnchorColor());
            }
            if (anchorSizeSelector != null) {
                anchorSizeSelector.setValue(style.getAnchorSize() + "");
            }

            if (arcSizeSelector != null) {
                arcSizeSelector.setValue(style.getRoundArc() + "");
            }

            if (dashCheck != null) {
                dashCheck.setSelected(style.isIsStrokeDash());
            }

            if (dashInput != null) {
                dashInput.setText(style.getStrokeDashText());
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
                            linecapSquareRadio.setSelected(true);
                            break;
                        default:
                            linecapButtRadio.setSelected(true);
                            break;
                    }
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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
            if (imageController == null || imageController.shapeType == null) {
                popError(message("InvalidData"));
                return false;
            }
            switch (imageController.shapeType) {
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
            String d = pathArea.getText();
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

    public boolean pickStyle() {
        try {
            if (imageController == null || style == null) {
                popError(message("InvalidParameter"));
                return false;
            }
            if (strokeColorController != null) {
                style.setStrokeColor(strokeColorController.color());
            }
            if (strokeWidthSelector != null) {
                try {
                    float v = Float.parseFloat(strokeWidthSelector.getValue());
                    if (v >= 0) {
                        strokeWidthSelector.getEditor().setStyle(null);
                        style.setStrokeWidth(v);
                    } else {
                        popError(message("InvalidParameter") + ": " + message("StrokeWidth"));
                        return false;
                    }
                } catch (Exception e) {
                    popError(message("InvalidParameter") + ": " + message("StrokeWidth"));
                    return false;
                }
            }
            if (strokeOpacitySelector != null) {
                try {
                    float v = Float.parseFloat(strokeOpacitySelector.getValue());
                    if (v >= 0) {
                        style.setStrokeOpacity(v);
                    } else {
                        popError(message("InvalidParameter") + ": " + message("Opacity"));
                        return false;
                    }
                } catch (Exception e) {
                    popError(message("InvalidParameter") + ": " + message("Opacity"));
                    return false;
                }
            }

            if (fillCheck != null) {
                style.setIsFillColor(fillCheck.isSelected());
            }
            if (fillColorController != null) {
                style.setFillColor(fillColorController.color());
            }
            if (fillOpacitySelector != null) {
                try {
                    float v = Float.parseFloat(fillOpacitySelector.getValue());
                    if (v >= 0) {
                        style.setFillOpacity(v);
                    } else {
                        popError(message("InvalidParameter") + ": " + message("Opacity"));
                        return false;
                    }
                } catch (Exception e) {
                    popError(message("InvalidParameter") + ": " + message("Opacity"));
                    return false;
                }
            }

            if (anchorColorController != null) {
                style.setAnchorColor(anchorColorController.color());
            }
            if (anchorSizeSelector != null) {
                try {
                    float v = Float.parseFloat(anchorSizeSelector.getValue());
                    if (v >= 0) {
                        style.setAnchorSize(v);
                    } else {
                        popError(message("InvalidParameter") + ": " + message("AnchorSize"));
                        return false;
                    }
                } catch (Exception e) {
                    popError(message("InvalidParameter") + ": " + message("AnchorSize"));
                    return false;
                }
            }

            if (arcSizeSelector != null) {
                try {
                    int v = Integer.parseInt(arcSizeSelector.getValue());
                    if (v >= 0) {
                        style.setRoundArc(v);
                    } else {
                        popError(message("InvalidParameter") + ": " + message("Arc"));
                        return false;
                    }
                } catch (Exception e) {
                    popError(message("InvalidParameter") + ": " + message("Arc"));
                    return false;
                }
            }

            if (dashCheck != null) {
                style.setIsStrokeDash(dashCheck.isSelected());
            }

            if (dashInput != null) {
                style.setStrokeDashText(dashInput.getText());
            }

            if (linecapSquareRadio != null) {
                if (linecapSquareRadio.isSelected()) {
                    style.setLineCap(StrokeLineCap.SQUARE);
                } else if (linecapRoundRadio.isSelected()) {
                    style.setLineCap(StrokeLineCap.ROUND);
                } else {
                    style.setLineCap(StrokeLineCap.BUTT);
                }
            }
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
        if (imageController == null || imageController.shapeType == null) {
            return;
        }
        switch (imageController.shapeType) {
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
        applyStyle();
    }

    public void drawRectangle() {
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

    public void drawPath() {
        imageController.drawPath();
    }

    public void applyStyle() {
        if (imageController == null || imageController.shapeType == null || style == null) {
            return;
        }
        if (imageController.shapeType == ShapeType.Lines) {
            if (imageController.maskLines != null && !imageController.maskLines.isEmpty()) {
                for (List<Line> lines : imageController.maskLines) {
                    for (Line line : lines) {
                        setStyle(line);
                    }
                }
            }
        } else {
            setStyle(currentShape());
        }
        imageController.setMaskAnchorsStyle(style.getAnchorColor(), style.getAnchorSize());
    }

    public void setStyle(Shape shape) {
        if (imageController == null || shape == null || style == null) {
            return;
        }
        shape.setStroke(style.getStrokeColor());
        shape.setStrokeWidth(style.getStrokeWidth());

        if (style.isIsFillColor()) {
            shape.setFill(style.getFillColor());
            shape.setOpacity(style.getFillOpacity());
        } else {
            shape.setFill(Color.TRANSPARENT);
            shape.setOpacity(1);
        }
        shape.setStrokeLineCap(style.getLineCap());
        shape.getStrokeDashArray().clear();
        if (style.isIsStrokeDash() && style.getStrokeDash() != null) {
            shape.getStrokeDashArray().addAll(style.getStrokeDash());
        }
    }

    /*
        action
     */
    @FXML
    @Override
    public void goAction() {
        if (pickShape() && pickStyle()) {
            redrawShape();
        }
    }

    /*
        helps
     */
    @FXML
    public void popExamplesPathMenu(Event event) {
        if (UserConfig.getBoolean("SvgPathExamplesPopWhenMouseHovering", false)) {
            showExamplesPathMenu(event);
        }
    }

    @FXML
    public void showExamplesPathMenu(Event event) {
        PopTools.popValues(this, pathArea, "SvgPathExamples", HelpTools.svgPathExamples(), event);
    }

}
