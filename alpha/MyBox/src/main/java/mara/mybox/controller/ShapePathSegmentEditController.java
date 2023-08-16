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
import mara.mybox.data.DoublePath;
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
    protected float x, y;
    protected DoublePath pathData;
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
            p = segment.getControlPoint1();
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

    public void setParameters(ControlPath2D pathController, DoublePath pathData, int index) {
        try {
            super.setParameters(pathController, null);
            this.pathController = pathController;
            this.pathData = pathData;
            this.index = index;
            setTitle(pathController.getTitle());

            if (index >= 0) {
                segment = pathData.getSegments().get(index);
                typePane.setDisable(true);
                load();

            } else {
                segment = null;
                infoLabel.setText(message("Add") + " " + message("Index") + ": " + pathData.getSegments().size());
                startXInput.setText(pathData.lastPoint().getX() + "");
                startYInput.setText(pathData.lastPoint().getY() + "");
                absRadio.setSelected(true);
                typePane.setDisable(false);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void load() {
        try {
            isSettingValues = true;
            String info = message("Index") + ": " + (index + 1) + "\n" + segment.text();
            infoLabel.setText(info);
            startXInput.setText(segment.getStartPoint().getX() + "");
            startYInput.setText(segment.getStartPoint().getY() + "");
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

    @FXML
    @Override
    public void okAction() {

        close();
    }

    public static ShapePathSegmentEditController open(ControlPath2D pathController, DoublePath pathData, int index) {
        try {
            ShapePathSegmentEditController controller = (ShapePathSegmentEditController) WindowTools.openChildStage(
                    pathController.getMyWindow(), Fxmls.ShapePathSegmentEditFxml, true);
            controller.setParameters(pathController, pathData, index);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
