package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-4
 * @License Apache License Version 2.0
 */
public class ControlImageMargins extends BaseController {

    protected int margin, distance;
    protected ImageMarginsController marginsController;

    @FXML
    protected ToggleGroup opGroup;
    @FXML
    protected ComboBox<String> widthSelector;
    @FXML
    protected CheckBox marginsTopCheck, marginsBottomCheck, marginsLeftCheck, marginsRightCheck;
    @FXML
    protected FlowPane colorPane, distancePane, marginsPane, widthPane;
    @FXML
    protected TextField distanceInput;
    @FXML
    protected RadioButton dragRadio, addRadio, blurRadio, cutColorRadio, cutWidthRadio;
    @FXML
    protected VBox setBox;
    @FXML
    protected ControlColorSet colorController;
    @FXML
    protected Button selectAllRectButton;

    public void setParameters(ImageMarginsController marginsController) {
        try {
            this.marginsController = marginsController;
            if (marginsController != null) {
                dragRadio.setVisible(true);
                dragRadio.setSelected(true);
            } else {
                dragRadio.setVisible(false);
                addRadio.setSelected(true);
            }

            opGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkOperationType();
                }
            });

            colorController.init(this, baseName + "Color");

            margin = UserConfig.getInt(baseName + "MarginsWidth", 20);
            if (margin <= 0) {
                margin = 20;
            }
            widthSelector.setValue(margin + "");
            widthSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkMarginWidth();
                }
            });

            distance = UserConfig.getInt(baseName + "ColorDistance", 20);
            if (distance <= 0) {
                distance = 20;
            }
            distanceInput.setText(distance + "");
            distanceInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkColorDistance();
                }
            });

            checkOperationType();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    public void imageWidth(int width) {
        try {
            List<String> ms = new ArrayList<>();
            ms.addAll(Arrays.asList(
                    width / 6 + "", width / 8 + "", width / 4 + "", width / 10 + "",
                    "20", "10", "5", "100", "200", "300", "50", "150", "500"));
            String m = margin + "";
            if (!ms.contains(m)) {
                ms.add(0, m);
            }
            isSettingValues = true;
            widthSelector.getItems().setAll(ms);
            widthSelector.setValue(m);
            isSettingValues = false;

            checkOperationType();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void checkOperationType() {
        try {
            setBox.getChildren().clear();
            ValidationTools.setEditorNormal(widthSelector);
            distanceInput.setStyle(null);
            if (marginsController != null) {
                marginsController.clearMask();
                marginsController.commentsLabel.setText("");
            }

            if (opGroup.getSelectedToggle() == null) {
                return;
            }

            if (marginsController != null && dragRadio.isSelected()) {
                setBox.getChildren().addAll(colorPane, selectAllRectButton);
                initDragging();

            } else if (addRadio.isSelected()) {
                setBox.getChildren().addAll(colorPane, widthPane, marginsPane);
                checkMarginWidth();

            } else if (cutWidthRadio.isSelected()) {
                setBox.getChildren().addAll(widthPane, marginsPane);
                checkMarginWidth();

            } else if (cutColorRadio.isSelected()) {
                setBox.getChildren().addAll(colorPane, distancePane, marginsPane);
                widthSelector.getEditor().setStyle(null);
                checkColorDistance();

            } else if (blurRadio.isSelected()) {
                setBox.getChildren().addAll(widthPane, marginsPane);
                checkMarginWidth();

            }

            refreshStyle(setBox);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initDragging() {
        try {
            if (marginsController == null) {
                return;
            }
            marginsController.commentsLabel.setText(message("ImageDragMarginsComments"));
            marginsController.maskRectangleData = DoubleRectangle.image(marginsController.imageView.getImage());
            marginsController.showMaskRectangle();
            marginsController.popItemMenu = false;
            marginsController.showAnchors = true;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    protected boolean checkMarginWidth() {
        if (isSettingValues) {
            return true;
        }
        int v;
        try {
            v = Integer.parseInt(widthSelector.getValue());
        } catch (Exception e) {
            v = -1;
        }
        if (v > 0) {
            margin = v;
            UserConfig.setInt(baseName + "MarginsWidth", v);
            ValidationTools.setEditorNormal(widthSelector);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("Width"));
            ValidationTools.setEditorBadStyle(widthSelector);
            return false;
        }
    }

    protected boolean checkColorDistance() {
        if (isSettingValues) {
            return true;
        }
        int v;
        try {
            v = Integer.parseInt(distanceInput.getText());
        } catch (Exception e) {
            v = -1;
        }
        if (v >= 0 && v <= 255) {
            distance = v;
            distanceInput.setStyle(null);
            UserConfig.setInt(baseName + "ColorDistance", v);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("ColorDistance"));
            distanceInput.setStyle(UserConfig.badStyle());
            return false;
        }
    }

    @FXML
    public void selectAllRect() {
        if (marginsController == null || !dragRadio.isSelected()
                || marginsController.imageView.getImage() == null) {
            return;
        }
        marginsController.selectAllRect();
    }

    protected boolean pickValues() {
        if (!dragRadio.isSelected()) {
            if (!marginsTopCheck.isSelected()
                    && !marginsBottomCheck.isSelected()
                    && !marginsLeftCheck.isSelected()
                    && !marginsRightCheck.isSelected()) {
                popError(message("NothingHandled"));
                return false;
            }
        }
        return checkMarginWidth() && checkColorDistance();
    }

}
