package mara.mybox.controller;

import java.awt.geom.Arc2D;
import java.util.Arrays;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mara.mybox.data.DoubleArc;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleCubic;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoubleLine;
import mara.mybox.data.DoubleQuadratic;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape.ShapeType;
import static mara.mybox.data.DoubleShape.ShapeType.Cubic;
import static mara.mybox.data.DoubleShape.ShapeType.Polylines;
import static mara.mybox.data.DoubleShape.ShapeType.Quadratic;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-6-29
 * @License Apache License Version 2.0
 */
public class ControlShapeParameters extends BaseController {

    protected ControlShapeOptions optionsController;
    protected BaseShapeController imageController;

    @FXML
    protected VBox shapeBox, pointsBox, linesBox, rectangleBox, circleBox, ellipseBox,
            lineBox, quadraticBox, cubicBox, arcBox, pathBox;
    @FXML
    protected TabPane shapesPane;
    @FXML
    protected TextField circleXInput, circleYInput, circleRadiusInput,
            rectXInput, rectYInput, rectWidthInput, rectHeightInput,
            ellipseXInput, ellipseYInput, ellipseXRadiusInput, ellipseYRadiusInput,
            lineX1Input, lineY1Input, lineX2Input, lineY2Input,
            quadStartXInput, quadStartYInput, quadControlXInput, quadControlYInput, quadEndXInput, quadEndYInput,
            cubicStartXInput, cubicStartYInput, cubicControlX1Input, cubicControlY1Input,
            cubicControlX2Input, cubicControlY2Input, cubicEndXInput, cubicEndYInput,
            arcCenterXInput, arcCenterYInput, arcRadiusXInput, arcRadiusYInput, arcStartAngleInput, arcExtentAngleInput,
            dashInput;
    @FXML
    protected ComboBox<String> roundSizeSelector;
    @FXML
    protected RadioButton arcOpenRadio, arcChordRadio, arcPieRadio;
    @FXML
    protected ControlPoints pointsController;
    @FXML
    protected ControlLines linesController;
    @FXML
    protected ControlPath2D pathController;

    @Override
    public void initControls() {
        try {
            super.initControls();

            thisPane.getChildren().remove(tabPane);

            roundSizeSelector.getItems().setAll(Arrays.asList("0", "2", "5", "10", "15", "30", "40", "50"));
            roundSizeSelector.setValue("0");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        load
     */
    public void setShapeControls(ShapeType shapeType) {
        try {
            shapeBox.getChildren().clear();
            if (imageController == null || shapeType == null) {
                return;
            }
            switch (shapeType) {
                case Rectangle:
                    shapeBox.getChildren().setAll(rectangleBox, goButton);
                    rectXInput.setText(scale(imageController.maskRectangleData.getX()) + "");
                    rectYInput.setText(scale(imageController.maskRectangleData.getY()) + "");
                    rectWidthInput.setText(scale(imageController.maskRectangleData.getWidth()) + "");
                    rectHeightInput.setText(scale(imageController.maskRectangleData.getHeight()) + "");
                    break;
                case Circle:
                    shapeBox.getChildren().setAll(circleBox, goButton);
                    circleXInput.setText(scale(imageController.maskCircleData.getCenterX()) + "");
                    circleYInput.setText(scale(imageController.maskCircleData.getCenterY()) + "");
                    circleRadiusInput.setText(scale(imageController.maskCircleData.getRadius()) + "");
                    break;
                case Ellipse:
                    shapeBox.getChildren().setAll(ellipseBox, goButton);
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
                case Polylines:
                    shapeBox.getChildren().add(linesBox);
                    VBox.setVgrow(linesBox, Priority.ALWAYS);
                    linesController.loadList(imageController.maskPolylinesData.getLines());
                    break;
                case Quadratic:
                    shapeBox.getChildren().addAll(quadraticBox, goButton);
                    quadStartXInput.setText(scale(imageController.maskQuadraticData.getStartX()) + "");
                    quadStartYInput.setText(scale(imageController.maskQuadraticData.getStartY()) + "");
                    quadControlXInput.setText(scale(imageController.maskQuadraticData.getControlX()) + "");
                    quadControlYInput.setText(scale(imageController.maskQuadraticData.getControlY()) + "");
                    quadEndXInput.setText(scale(imageController.maskQuadraticData.getEndX()) + "");
                    quadEndYInput.setText(scale(imageController.maskQuadraticData.getEndY()) + "");
                    break;
                case Cubic:
                    shapeBox.getChildren().addAll(cubicBox, goButton);
                    cubicStartXInput.setText(scale(imageController.maskCubicData.getStartX()) + "");
                    cubicStartYInput.setText(scale(imageController.maskCubicData.getStartY()) + "");
                    cubicControlX1Input.setText(scale(imageController.maskCubicData.getControlX1()) + "");
                    cubicControlY1Input.setText(scale(imageController.maskCubicData.getControlY1()) + "");
                    cubicControlX2Input.setText(scale(imageController.maskCubicData.getControlX2()) + "");
                    cubicControlY2Input.setText(scale(imageController.maskCubicData.getControlY2()) + "");
                    cubicEndXInput.setText(scale(imageController.maskCubicData.getEndX()) + "");
                    cubicEndYInput.setText(scale(imageController.maskCubicData.getEndY()) + "");
                    break;
                case Arc:
                    shapeBox.getChildren().addAll(arcBox, goButton);
                    arcCenterXInput.setText(scale(imageController.maskArcData.getCenterX()) + "");
                    arcCenterYInput.setText(scale(imageController.maskArcData.getCenterY()) + "");
                    arcRadiusXInput.setText(scale(imageController.maskArcData.getRadiusX()) + "");
                    arcRadiusYInput.setText(scale(imageController.maskArcData.getRadiusY()) + "");
                    arcStartAngleInput.setText(scale(imageController.maskArcData.getStartAngle()) + "");
                    arcExtentAngleInput.setText(scale(imageController.maskArcData.getExtentAngle()) + "");
                    switch (imageController.maskArcData.getType()) {
                        case Arc2D.CHORD:
                            arcChordRadio.setSelected(true);
                            break;
                        case Arc2D.PIE:
                            arcPieRadio.setSelected(true);
                            break;
                        default:
                            arcOpenRadio.setSelected(true);
                            break;
                    }
                    break;
                case Path:
                    shapeBox.getChildren().add(pathBox);
                    VBox.setVgrow(pathBox, Priority.ALWAYS);
                    pathController.loadPath(imageController.maskPathData.getContent());

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

    /*
        pick
     */
    public double scale(double v) {
        return DoubleTools.scale(v, 2);
    }

    public boolean pickShape(ShapeType shapeType) {
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
                case Polylines:
                    return pickLines();
                case Quadratic:
                    return pickQuadratic();
                case Cubic:
                    return pickCubic();
                case Arc:
                    return pickArc();
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
            float x, y, w, h, round;
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
            try {
                round = Float.parseFloat(roundSizeSelector.getValue());
            } catch (Exception e) {
                round = 0;
            }
            if (round < 0) {
                round = 0;
            }
            imageController.maskRectangleData = DoubleRectangle.xywh(x, y, w, h);
            imageController.maskRectangleData.setRound(round);
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
            float cx, cy, rx, ry;
            try {
                cx = Float.parseFloat(ellipseXInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x");
                return false;
            }
            try {
                cy = Float.parseFloat(ellipseYInput.getText());
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
            imageController.maskEllipseData = DoubleEllipse.ellipse(cx, cy, rx, ry);
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
            imageController.maskPolylineData.setAll(pointsController.getPoints());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean pickPolygon() {
        try {
            imageController.maskPolygonData.setAll(pointsController.getPoints());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean pickLines() {
        try {
            imageController.maskPolylinesData.setLines(linesController.getLines());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean pickQuadratic() {
        try {
            float sx, sy, cx, cy, ex, ey;
            try {
                sx = Float.parseFloat(quadStartXInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("StartPoint") + " x");
                return false;
            }
            try {
                sy = Float.parseFloat(quadStartYInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("StartPoint") + " y");
                return false;
            }
            try {
                cx = Float.parseFloat(quadControlXInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("ControlPoint") + " x");
                return false;
            }
            try {
                cy = Float.parseFloat(quadControlYInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("ControlPoint") + " y");
                return false;
            }
            try {
                ex = Float.parseFloat(quadEndXInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("EndPoint") + " x");
                return false;
            }
            try {
                ey = Float.parseFloat(quadEndYInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("EndPoint") + " y");
                return false;
            }
            imageController.maskQuadraticData = new DoubleQuadratic(sx, sy, cx, cy, ex, ey);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean pickCubic() {
        try {
            float sx, sy, cx1, cy1, cx2, cy2, ex, ey;
            try {
                sx = Float.parseFloat(cubicStartXInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("StartPoint") + " x");
                return false;
            }
            try {
                sy = Float.parseFloat(quadStartYInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("StartPoint") + " y");
                return false;
            }
            try {
                cx1 = Float.parseFloat(cubicControlX1Input.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("ControlPoint1") + " x");
                return false;
            }
            try {
                cy1 = Float.parseFloat(cubicControlY1Input.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("ControlPoint1") + " y");
                return false;
            }
            try {
                cx2 = Float.parseFloat(cubicControlX2Input.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("ControlPoint2") + " x");
                return false;
            }
            try {
                cy2 = Float.parseFloat(cubicControlY2Input.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("ControlPoint2") + " y");
                return false;
            }
            try {
                ex = Float.parseFloat(cubicEndXInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("EndPoint") + " x");
                return false;
            }
            try {
                ey = Float.parseFloat(cubicEndYInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("End") + " y");
                return false;
            }
            imageController.maskCubicData = new DoubleCubic(sx, sy, cx1, cy1, cx2, cy2, ex, ey);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean pickArc() {
        try {
            float cx, cy, rx, ry, sa, ea;
            int type;
            try {
                cx = Float.parseFloat(arcCenterXInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("Center") + " x");
                return false;
            }
            try {
                cy = Float.parseFloat(arcCenterYInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("Center") + " y");
                return false;
            }
            try {
                rx = Float.parseFloat(arcRadiusXInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("RadiusX"));
                return false;
            }
            try {
                ry = Float.parseFloat(arcRadiusYInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("RadiusY"));
                return false;
            }
            try {
                sa = Float.parseFloat(arcStartAngleInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("StartAngle"));
                return false;
            }
            try {
                ea = Float.parseFloat(arcExtentAngleInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("ExtentAngle"));
                return false;
            }
            if (arcChordRadio.isSelected()) {
                type = Arc2D.CHORD;
            } else if (arcPieRadio.isSelected()) {
                type = Arc2D.PIE;
            } else {
                type = Arc2D.OPEN;
            }
            imageController.maskArcData = DoubleArc.arc(cx, cy, rx, ry, sa, ea, type);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean pickPath() {
        try {
            if (!pathController.pickValue()) {
                return false;
            }
            imageController.maskPathData.setContent(pathController.getText());
            imageController.maskPathData.setSegments(pathController.getSegments());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @FXML
    @Override
    public void goAction() {
        optionsController.goShape();
    }

}
