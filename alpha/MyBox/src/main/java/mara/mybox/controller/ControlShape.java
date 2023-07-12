package mara.mybox.controller;

import java.util.Arrays;
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
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoubleLine;
import mara.mybox.data.DoubleLines;
import mara.mybox.data.DoublePath;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoublePolyline;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.data.ShapeStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.ValidationTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-6-29
 * @License Apache License Version 2.0
 */
public abstract class ControlShape extends BaseController {

    protected BaseImageController imageController;
    protected float strokeWidth, strokeOpacity, fillOpacity, anchorSize, arcSize;
    protected DoubleShape currentShape;

    @FXML
    protected RadioButton rectangleRadio, circleRadio, ellipseRadio,
            lineRadio, polylineRadio, polygonRadio, linesRadio, pathRadio,
            linecapSquareRadio, linecapRoundRadio, linecapButtRadio;
    @FXML
    protected VBox shapeBox, shapeOutBox, pointsBox,
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
    protected ComboBox<String> strokeWidthSelector, strokeOpacitySelector, fillOpacitySelector,
            anchorSizeSelector, arcSizeSelector;
    @FXML
    protected CheckBox fillCheck;
    @FXML
    protected ControlColorSet strokeColorController, anchorColorController, fillColorController;

    public void setParameters(BaseImageController imageController) {
        try {
            this.imageController = imageController;
            this.baseName = imageController.baseName;

            imageController.maskShapeChangedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (!isSettingValues) {
                        loadShape();
                        drawShape();
                    }
                }
            });

            initStyle();
            initShapes();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initShapes() {
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

    public void initStyle() {
        try {

            strokeWidth = 2;
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
                                strokeWidth = v;
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

            fillOpacity = 0.3f;
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
                                fillOpacity = v;
                            }
                            fillOpacitySelector.getEditor().setStyle(null);
                        } catch (Exception e) {
                            fillOpacitySelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    }
                });
            }
            strokeOpacity = 1f;
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
                                strokeOpacity = v;
                            }
                            strokeOpacitySelector.getEditor().setStyle(null);
                        } catch (Exception e) {
                            strokeOpacitySelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    }
                });
            }

            anchorSize = 10;
            if (anchorSizeSelector != null) {
                anchorSizeSelector.getItems().setAll(Arrays.asList("10", "15", "20", "25", "30", "40", "50"));
                anchorSizeSelector.setValue("10");
                anchorSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                anchorSize = v;
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

            arcSize = 0;
            if (arcSizeSelector != null) {
                arcSizeSelector.getItems().setAll(Arrays.asList("0", "2", "5", "10", "15", "30", "40", "50"));
                arcSizeSelector.setValue("0");
                arcSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                arcSize = v;
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


    /*
        values
     */
    public Image currentImage() {
        return imageController != null ? imageController.image : null;
    }

    /*
        loadText
     */
    public void switchShape() {
        if (!createShape()) {
            return;
        }
        loadShape();
        loadStyle();
        drawShape();
    }

    public boolean createShape() {
        try {
            if (imageController == null) {
                return false;
            }
            imageController.clearMaskShapes();

            double width, height;
            Image image = currentImage();
            if (image != null) {
                width = image.getWidth();
                height = image.getHeight();
            } else {
                width = 500;
                height = 500;
            }
            double min = Math.min(width, height);

            if (rectangleRadio != null && rectangleRadio.isSelected()) {
                if (imageController.maskRectangleData == null) {
                    imageController.maskRectangleData = new DoubleRectangle(
                            (int) (width / 4), (int) (height / 4), (int) (width * 3 / 4), (int) (height * 3 / 4));
                }
                imageController.maskShape = imageController.maskRectangleData;

            } else if (circleRadio != null && circleRadio.isSelected()) {
                if (imageController.maskCircleData == null) {
                    imageController.maskCircleData = new DoubleCircle(
                            (int) (width / 2), (int) (height / 2), (int) (min / 4));
                }
                imageController.maskShape = imageController.maskCircleData;

            } else if (ellipseRadio != null && ellipseRadio.isSelected()) {
                if (imageController.maskEllipseData == null) {
                    imageController.maskEllipseData = new DoubleEllipse(
                            (int) (width / 4), (int) (height / 4), (int) (width * 3 / 4), (int) (height * 3 / 4));
                }
                imageController.maskShape = imageController.maskEllipseData;

            } else if (lineRadio != null && lineRadio.isSelected()) {
                if (imageController.maskLineData == null) {
                    imageController.maskLineData = new DoubleLine(
                            (int) (width / 4), (int) (height / 4), (int) (width * 3 / 4), (int) (height * 3 / 4));
                }
                imageController.maskShape = imageController.maskLineData;

            } else if (polylineRadio != null && polylineRadio.isSelected()) {
                if (imageController.maskPolylineData == null) {
                    imageController.maskPolylineData = new DoublePolyline();
                    imageController.maskPolylineData.add(10, 10);
                    imageController.maskPolylineData.add(width / 2, 10);
                    imageController.maskPolylineData.add(width / 4, height / 2);
                    imageController.maskPolylineData.add(width - 30, height / 2);
                }
                imageController.maskShape = imageController.maskPolylineData;

            } else if (polygonRadio != null && polygonRadio.isSelected()) {
                if (imageController.maskPolygonData == null) {
                    imageController.maskPolygonData = new DoublePolygon();
                    imageController.maskPolygonData.add(10, 10);
                    imageController.maskPolygonData.add(width / 2, 10);
                    imageController.maskPolygonData.add(width / 4, height / 2);
                    imageController.maskPolygonData.add(width - 30, height / 2);
                }
                imageController.maskShape = imageController.maskPolygonData;

            } else if (linesRadio != null && linesRadio.isSelected()) {
                if (imageController.maskPenData == null) {
                    imageController.maskPenData = new DoubleLines();
                }
                imageController.maskShape = imageController.maskPenData;

            } else if (pathRadio != null && pathRadio.isSelected()) {
                if (imageController.svgPath == null) {
                    imageController.svgPath = new SVGPath();
                    imageController.svgPath.setContent("M 10,30\n"
                            + "           A 20,20 0,0,1 50,30\n"
                            + "           A 20,20 0,0,1 90,30\n"
                            + "           Q 90,60 50,90\n"
                            + "           Q 10,60 10,30 z");
                }
                if (imageController.pathData == null) {
                    imageController.pathData = new DoublePath();
                }
                imageController.maskShape = imageController.pathData;

            } else {
                popError(message("InvalidData"));
                return false;
            }
            return imageController.maskShape != null;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void loadShape() {
        try {
            shapeBox.getChildren().clear();

            if (imageController == null || imageController.maskShape == null) {
                return;
            }

            if (imageController.maskShape instanceof DoubleRectangle) {
                shapeBox.getChildren().add(rectangleBox);
                rectXInput.setText(imageController.maskRectangleData.getSmallX() + "");
                rectYInput.setText(imageController.maskRectangleData.getSmallY() + "");
                rectWidthInput.setText(imageController.maskRectangleData.getWidth() + "");
                rectHeightInput.setText(imageController.maskRectangleData.getHeight() + "");

            } else if (imageController.maskShape instanceof DoubleCircle) {
                shapeBox.getChildren().add(circleBox);
                circleXInput.setText(imageController.maskCircleData.getCenterX() + "");
                circleYInput.setText(imageController.maskCircleData.getCenterY() + "");
                circleRadiusInput.setText(imageController.maskCircleData.getRadius() + "");

            } else if (imageController.maskShape instanceof DoubleEllipse) {
                shapeBox.getChildren().add(ellipseBox);
                ellipseXInput.setText(imageController.maskEllipseData.getCenterX() + "");
                ellipseYInput.setText(imageController.maskEllipseData.getCenterY() + "");
                ellipseXRadiusInput.setText(imageController.maskEllipseData.getRadiusX() + "");
                ellipseYRadiusInput.setText(imageController.maskEllipseData.getRadiusY() + "");

            } else if (imageController.maskShape instanceof DoubleLine) {
                shapeBox.getChildren().add(lineBox);
                lineX1Input.setText(imageController.maskLineData.getStartX() + "");
                lineY1Input.setText(imageController.maskLineData.getStartY() + "");
                lineX2Input.setText(imageController.maskLineData.getEndX() + "");
                lineY2Input.setText(imageController.maskLineData.getEndY() + "");

            } else if (imageController.maskShape instanceof DoublePolyline) {
                shapeBox.getChildren().add(pointsBox);
                VBox.setVgrow(pointsBox, Priority.ALWAYS);
                pointsController.loadList(imageController.maskPolylineData.getPoints());

            } else if (imageController.maskShape instanceof DoublePolygon) {
                shapeBox.getChildren().add(pointsBox);
                VBox.setVgrow(pointsBox, Priority.ALWAYS);
                pointsController.loadList(imageController.maskPolygonData.getPoints());

            } else if (imageController.maskShape instanceof DoubleLines) {

            } else if (imageController.maskShape instanceof DoublePath) {
                shapeBox.getChildren().add(pathBox);
                pathArea.setText(imageController.svgPath.getContent());

            } else {
                popError(message("InvalidData"));
                return;
            }

            refreshStyle(shapeBox);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadStyle() {
        try {
            if (imageController == null || imageController.maskShape == null) {
                return;
            }
            if (strokeColorController != null) {
                strokeColorController.setColor(ShapeStyle.strokeColor(imageController.maskShape));
            }
            if (strokeWidthSelector != null) {
                strokeWidthSelector.setValue(ShapeStyle.strokeWidth(imageController.maskShape) + "");
            }
            if (strokeOpacitySelector != null) {
                strokeOpacitySelector.setValue(ShapeStyle.strokeOpacity(imageController.maskShape) + "");
            }

            if (fillCheck != null) {
                fillCheck.setSelected(ShapeStyle.isFillColor(imageController.maskShape));
            }
            if (fillColorController != null) {
                fillColorController.setColor(ShapeStyle.fillColor(imageController.maskShape));
            }
            if (fillOpacitySelector != null) {
                fillOpacitySelector.setValue(ShapeStyle.fillOpacity(imageController.maskShape) + "");
            }

            if (anchorColorController != null) {
                anchorColorController.setColor(ShapeStyle.anchorColor(imageController.maskShape));
            }
            if (anchorSizeSelector != null) {
                anchorSizeSelector.setValue(ShapeStyle.anchorSize(imageController.maskShape) + "");
            }

            if (arcSizeSelector != null) {
                arcSizeSelector.setValue(ShapeStyle.roundArc(imageController.maskShape) + "");
            }

            if (dashInput != null) {
                dashInput.setText(ShapeStyle.strokeDashString(imageController.maskShape));
            }

            if (linecapSquareRadio != null) {
                StrokeLineCap lineCap = ShapeStyle.lineCap(imageController.maskShape);
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

    /*
        pick
     */
    public boolean pickShape() {
        try {
            if (imageController == null) {
                return false;
            }
            if (imageController.maskShape instanceof DoubleRectangle) {
                return pickRect();

            } else if (imageController.maskShape instanceof DoubleCircle) {
                return pickCircle();

            } else if (imageController.maskShape instanceof DoubleEllipse) {
                return pickEllipse();

            } else if (imageController.maskShape instanceof DoubleLine) {
                return pickLine();

            } else if (imageController.maskShape instanceof DoublePolyline) {
                return pickPolyline();

            } else if (imageController.maskShape instanceof DoublePolygon) {
                return pickPolygon();

            } else if (imageController.maskShape instanceof DoubleLines) {
                return pickLines();

            } else if (imageController.maskShape instanceof DoublePath) {
                return pickPath();

            } else {
                imageController.maskShape = null;
                popError(message("InvalidData"));
                return false;
            }
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
            imageController.maskShape = imageController.maskRectangleData;
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
            imageController.maskShape = imageController.maskCircleData;
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
            imageController.maskShape = imageController.maskEllipseData;
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
            imageController.maskShape = imageController.maskLineData;
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean pickPolyline() {
        try {
            imageController.maskPolylineData.setAll(pointsController.tableData);
            imageController.maskShape = imageController.maskPolylineData;
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean pickPolygon() {
        try {
            imageController.maskPolygonData.setAll(pointsController.tableData);
            imageController.maskShape = imageController.maskPolygonData;
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean pickLines() {
        try {
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
            imageController.maskShape = imageController.pathData;
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void pickStyle() {
        try {
            if (imageController == null || imageController.maskShape == null) {
                return;
            }
            if (strokeColorController != null) {
                ShapeStyle.setStrokeColor(imageController.maskShape, strokeColorController.color());
            }
            if (strokeWidthSelector != null) {
                ShapeStyle.setStrokeWidth(imageController.maskShape, strokeWidth);
            }
            if (strokeOpacitySelector != null) {
                ShapeStyle.setStrokeOpacity(imageController.maskShape, strokeOpacity);
            }

            if (fillCheck != null) {
                ShapeStyle.setIsFillColor(imageController.maskShape, fillCheck.isSelected());
            }
            if (fillColorController != null) {
                ShapeStyle.setFillColor(imageController.maskShape, fillColorController.color());
            }
            if (fillOpacitySelector != null) {
                ShapeStyle.setFillOpacity(imageController.maskShape, fillOpacity);
            }

            if (anchorColorController != null) {
                ShapeStyle.setAnchorColor(imageController.maskShape, anchorColorController.color());
            }
            if (anchorSizeSelector != null) {
                ShapeStyle.setAnchorSize(imageController.maskShape, anchorSize);
            }

            if (arcSizeSelector != null) {
                ShapeStyle.setRoundArc(imageController.maskShape, (int) arcSize);
            }

            if (dashInput != null) {
                ShapeStyle.setStrokeDashString(imageController.maskShape, dashInput.getText());
            }

            if (linecapSquareRadio != null) {
                if (linecapSquareRadio.isSelected()) {
                    ShapeStyle.setLineCap(imageController.maskShape, StrokeLineCap.SQUARE);
                } else if (linecapRoundRadio.isSelected()) {
                    ShapeStyle.setLineCap(imageController.maskShape, StrokeLineCap.ROUND);
                } else {
                    ShapeStyle.setLineCap(imageController.maskShape, StrokeLineCap.BUTT);
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }


    /*
        draw
     */
    public void drawShape() {
        if (imageController == null || imageController.maskShape == null) {
            return;
        }
        if (imageController.maskShape instanceof DoubleRectangle) {
            drawRectangle();

        } else if (imageController.maskShape instanceof DoubleCircle) {
            drawCircle();

        } else if (imageController.maskShape instanceof DoubleEllipse) {
            drawEllipse();

        } else if (imageController.maskShape instanceof DoubleLine) {
            drawLine();

        } else if (imageController.maskShape instanceof DoublePolyline) {
            drawPolyline();

        } else if (imageController.maskShape instanceof DoublePolygon) {
            drawPolygon();

        } else if (imageController.maskShape instanceof DoubleLines) {
            drawLines();

        } else if (imageController.maskShape instanceof DoublePath) {
            drawPath();
        }
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
        imageController.showMaskPenlines();
    }

    public void drawPath() {
        imageController.drawPath();
    }

    /*
        action
     */
    @FXML
    public void goShape() {
        if (pickShape()) {
            pickStyle();
            drawShape();
        }
    }

    @FXML
    public void goStyle() {
        goShape();
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
