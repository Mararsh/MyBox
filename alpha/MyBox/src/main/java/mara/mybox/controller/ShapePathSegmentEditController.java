package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.data.DoublePathSegment;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.Arc;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.Close;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.Cubic;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.CubicSmooth;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.Line;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.LineHorizontal;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.LineVertical;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.Move;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.Quadratic;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.QuadraticSmooth;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-8-16
 * @License Apache License Version 2.0
 */
public class ShapePathSegmentEditController extends BaseInputController {

    protected ControlPath2D pathController;
    protected double x, y, startX, startY;
    protected int index;
    protected DoublePathSegment segment;
    protected float angle;

    @FXML
    protected Label infoLabel;
    @FXML
    protected ToggleGroup typeGroup, coordGroup;
    @FXML
    protected RadioButton moveRadio, lineRadio, hlineRadio, vlineRadio, quadRadio, quadSmoothRadio,
            cubicRadio, cubicSmoothRadio, arcRadio, closeRadio, absRadio, relRadio;
    @FXML
    protected VBox setBox;
    @FXML
    protected FlowPane typePane, coodPane, control1Pane, control2Pane, radiusPane, endPane, rotatePane;
    @FXML
    protected TextField startXInput, startYInput, control1XInput, control1YInput, control2XInput, control2YInput,
            radiusXInput, radiusYInput, endXInput, endYInput;
    @FXML
    protected ComboBox<String> angleSelector;
    @FXML
    protected CheckBox largeCheck, closewiseCheck;

    @Override
    public void initControls() {
        try {
            super.initControls();

            typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue v, Toggle ov, Toggle nv) {
                    checkType();
                }
            });
            checkType();

            coordGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue v, Toggle ov, Toggle nv) {
                    checkCoord();
                }
            });

            startXInput.setDisable(true);
            startYInput.setDisable(true);

            angle = UserConfig.getFloat("ShapeRotateAngle", 45);
            angleSelector.getItems().addAll(Arrays.asList(
                    "45", "30", "60", "90", "180", "270", "15", "20", "300", "330"));
            angleSelector.setValue(angle + "");
            angleSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue v, String ov, String nv) {
                    try {
                        angle = Float.parseFloat(angleSelector.getValue());
                        UserConfig.setFloat("ShapeRotateAngle", angle);
                    } catch (Exception e) {
                        popError(message("InvalidParameter"));
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void checkType() {
        try {
            if (isSettingValues) {
                return;
            }
            setBox.getChildren().clear();
            endXInput.setDisable(false);
            endYInput.setDisable(false);
            if (moveRadio.isSelected()) {
                setBox.getChildren().addAll(coodPane, endPane);

            } else if (lineRadio.isSelected()) {
                setBox.getChildren().addAll(coodPane, endPane);

            } else if (hlineRadio.isSelected()) {
                setBox.getChildren().addAll(coodPane, endPane);
                endYInput.setDisable(true);

            } else if (vlineRadio.isSelected()) {
                setBox.getChildren().addAll(coodPane, endPane);
                endXInput.setDisable(true);

            } else if (quadRadio.isSelected()) {
                setBox.getChildren().addAll(coodPane, control1Pane, endPane);

            } else if (quadSmoothRadio.isSelected()) {
                setBox.getChildren().addAll(coodPane, endPane);

            } else if (cubicRadio.isSelected()) {
                setBox.getChildren().addAll(coodPane, control1Pane, control2Pane, endPane);

            } else if (cubicSmoothRadio.isSelected()) {
                setBox.getChildren().addAll(coodPane, control1Pane, endPane);

            } else if (arcRadio.isSelected()) {
                setBox.getChildren().addAll(coodPane, radiusPane, rotatePane, endPane);

            } else if (closeRadio.isSelected()) {

            }

            checkCoord();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void checkCoord() {
        if (isSettingValues || segment == null) {
            return;
        }
        DoublePoint p;
        if (arcRadio.isSelected()) {
            p = segment.getArcRadius();
            radiusXInput.setText(p.getX() + "");
            radiusYInput.setText(p.getY() + "");

            p = absRadio.isSelected() ? segment.getEndPoint() : segment.getEndPointRel();
            endXInput.setText(p.getX() + "");
            endYInput.setText(p.getY() + "");

            angleSelector.setValue(segment.getValue() + "");
            largeCheck.setSelected(segment.isFlag1());
            closewiseCheck.setSelected(segment.isFlag2());
            return;
        }

        p = absRadio.isSelected() ? segment.getControlPoint1() : segment.getControlPoint1Rel();
        if (p != null) {
            control1XInput.setText(p.getX() + "");
            control1YInput.setText(p.getY() + "");
        }

        p = absRadio.isSelected() ? segment.getControlPoint2() : segment.getControlPoint2Rel();
        if (p != null) {
            control2XInput.setText(p.getX() + "");
            control2YInput.setText(p.getY() + "");
        }

        p = absRadio.isSelected() ? segment.getEndPoint() : segment.getEndPointRel();
        if (p != null) {
            endXInput.setText(p.getX() + "");
            endYInput.setText(p.getY() + "");
        }

    }

    public void setParameters(ControlPath2D pathController, int pos, DoublePathSegment segment) {
        try {
            super.setParameters(pathController, null);
            this.pathController = pathController;
            this.index = pos;
            this.segment = segment;
            setTitle(pathController.getTitle());

            int segIndex;
            int size = pathController.tableData.size();
            if (index < 0) {
                segIndex = size + 1;
            } else {
                segIndex = index + 1;
            }
            String info;
            if (segment != null) {
                typePane.setDisable(true);
                load();

                DoublePoint p = segment.getStartPoint();
                startX = p.getX();
                startY = p.getY();
                info = message("Index") + ": " + segIndex + "\n" + segment.text();

            } else {
                absRadio.setSelected(true);
                typePane.setDisable(false);

                DoublePoint p = pathController.tableData.get(size - 1).getEndPoint();
                startX = p.getX();
                startY = p.getY();
                info = message("Add") + " " + message("Index") + ": " + segIndex;
            }

            startXInput.setText(startX + "");
            startYInput.setText(startY + "");
            infoLabel.setText(info);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void load() {
        try {
            isSettingValues = true;
            switch (segment.getType()) {
                case Move:
                    moveRadio.setSelected(true);
                    break;
                case Line:
                    lineRadio.setSelected(true);
                    break;
                case LineHorizontal:
                    hlineRadio.setSelected(true);
                    break;
                case LineVertical:
                    vlineRadio.setSelected(true);
                    break;
                case Quadratic:
                    quadRadio.setSelected(true);
                    break;
                case QuadraticSmooth:
                    quadSmoothRadio.setSelected(true);
                    break;
                case Cubic:
                    cubicRadio.setSelected(true);
                    break;
                case CubicSmooth:
                    cubicSmoothRadio.setSelected(true);
                    break;
                case Arc:
                    arcRadio.setSelected(true);
                    break;
                case Close:
                    closeRadio.setSelected(true);
                    break;
            }
            if (segment.isIsAbsolute()) {
                absRadio.setSelected(true);
            } else {
                relRadio.setSelected(true);
            }

            isSettingValues = false;

            checkType();
            checkCoord();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public DoublePoint checkEndPoint() {
        double px, py;
        try {
            px = Double.parseDouble(endXInput.getText());
        } catch (Exception e) {
            popError(message("InvalidParameter") + ": " + message("EndPoint"));
            return null;
        }
        try {
            py = Double.parseDouble(endYInput.getText());
        } catch (Exception e) {
            popError(message("InvalidParameter") + ": " + message("EndPoint"));
            return null;
        }
        return new DoublePoint(px, py);
    }

    public DoublePoint checkControl1() {
        double px, py;
        try {
            px = Double.parseDouble(control1XInput.getText());
        } catch (Exception e) {
            popError(message("InvalidParameter") + ": " + message("ControlPoint1"));
            return null;
        }
        try {
            py = Double.parseDouble(control1YInput.getText());
        } catch (Exception e) {
            popError(message("InvalidParameter") + ": " + message("ControlPoint1"));
            return null;
        }
        return new DoublePoint(px, py);
    }

    public DoublePoint checkControl2() {
        double px, py;
        try {
            px = Double.parseDouble(control2XInput.getText());
        } catch (Exception e) {
            popError(message("InvalidParameter") + ": " + message("ControlPoint2"));
            return null;
        }
        try {
            py = Double.parseDouble(control2YInput.getText());
        } catch (Exception e) {
            popError(message("InvalidParameter") + ": " + message("ControlPoint2"));
            return null;
        }
        return new DoublePoint(px, py);
    }

    public boolean checkArc(DoublePathSegment seg) {
        double irx, iry, irotate;
        try {
            irx = Double.parseDouble(radiusXInput.getText());
        } catch (Exception e) {
            popError(message("InvalidParameter") + ": " + message("Radius"));
            return false;
        }
        try {
            iry = Double.parseDouble(radiusYInput.getText());
        } catch (Exception e) {
            popError(message("InvalidParameter") + ": " + message("Radius"));
            return false;
        }
        try {
            irotate = Double.parseDouble(angleSelector.getValue());
        } catch (Exception e) {
            popError(message("InvalidParameter") + ": " + message("RotateAngle"));
            return false;
        }
        seg.setArcRadius(new DoublePoint(irx, iry));
        seg.setValue(irotate)
                .setFlag1(largeCheck.isSelected())
                .setFlag2(closewiseCheck.isSelected());
        return true;
    }

    @FXML
    @Override
    public void okAction() {
        try {
            boolean abs = absRadio.isSelected();
            DoublePathSegment seg = new DoublePathSegment()
                    .setIsAbsolute(abs)
                    .setScale(segment != null ? segment.getScale() : UserConfig.imageScale())
                    .setStartPoint(new DoublePoint(startX, startY))
                    .setIndex(index);

            if (setBox.getChildren().contains(endPane)) {
                DoublePoint p = checkEndPoint();
                if (p == null) {
                    return;
                }
                if (abs) {
                    seg.setEndPoint(p)
                            .setEndPointRel(new DoublePoint(p.getX() - startX, p.getY() - startY));
                } else {
                    seg.setEndPointRel(p)
                            .setEndPoint(new DoublePoint(p.getX() + startX, p.getY() + startY));
                }
            }
            if (setBox.getChildren().contains(control1Pane)) {
                DoublePoint p = checkControl1();
                if (p == null) {
                    return;
                }
                if (abs) {
                    seg.setControlPoint1(p)
                            .setControlPoint1Rel(new DoublePoint(p.getX() - startX, p.getY() - startY));
                } else {
                    seg.setControlPoint1Rel(p)
                            .setControlPoint1(new DoublePoint(p.getX() + startX, p.getY() + startY));
                }
            }
            if (setBox.getChildren().contains(control2Pane)) {
                DoublePoint p = checkControl2();
                if (p == null) {
                    return;
                }
                if (abs) {
                    seg.setControlPoint2(p)
                            .setControlPoint2Rel(new DoublePoint(p.getX() - startX, p.getY() - startY));
                } else {
                    seg.setControlPoint2Rel(p)
                            .setControlPoint2(new DoublePoint(p.getX() + startX, p.getY() + startY));
                }
            }
            if (setBox.getChildren().contains(radiusPane)) {
                if (!checkArc(seg)) {
                    return;
                }
            }
            if (moveRadio.isSelected()) {
                seg.setType(DoublePathSegment.PathSegmentType.Move);
            } else if (lineRadio.isSelected()) {
                seg.setType(DoublePathSegment.PathSegmentType.Line);
            } else if (hlineRadio.isSelected()) {
                seg.setType(DoublePathSegment.PathSegmentType.LineHorizontal)
                        .setValue(seg.getEndPoint().getX())
                        .setValueRel(seg.getEndPointRel().getX());
            } else if (vlineRadio.isSelected()) {
                seg.setType(DoublePathSegment.PathSegmentType.LineVertical)
                        .setValue(seg.getEndPoint().getY())
                        .setValueRel(seg.getEndPointRel().getY());
            } else if (quadRadio.isSelected()) {
                seg.setType(DoublePathSegment.PathSegmentType.Quadratic);
            } else if (quadSmoothRadio.isSelected()) {
                seg.setType(DoublePathSegment.PathSegmentType.QuadraticSmooth);
            } else if (cubicRadio.isSelected()) {
                seg.setType(DoublePathSegment.PathSegmentType.Cubic);
            } else if (cubicSmoothRadio.isSelected()) {
                seg.setType(DoublePathSegment.PathSegmentType.CubicSmooth);
            } else if (arcRadio.isSelected()) {
                seg.setType(DoublePathSegment.PathSegmentType.Arc);
            } else if (closeRadio.isSelected()) {
                seg.setType(DoublePathSegment.PathSegmentType.Close)
                        .setEndPoint(seg.getStartPoint().copy());
            } else {
                return;
            }

            pathController.isSettingValues = true;
            if (segment != null) {
                pathController.tableData.set(index, seg);
            } else if (index >= 0 && index < pathController.tableData.size()) {
                pathController.tableData.add(index, seg);
            } else {
                pathController.tableData.add(seg);
            }
            pathController.isSettingValues = false;
            pathController.tableChanged(true);

            close();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public static ShapePathSegmentEditController open(ControlPath2D pathController,
            int index, DoublePathSegment segment) {
        try {
            ShapePathSegmentEditController controller = (ShapePathSegmentEditController) WindowTools.openChildStage(
                    pathController.getMyWindow(), Fxmls.ShapePathSegmentEditFxml, true);
            controller.setParameters(pathController, index, segment);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
