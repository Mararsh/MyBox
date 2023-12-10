package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.MarginTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-9-26
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchMarginsController extends BaseImageEditBatchController {

    protected int width, distance;

    @FXML
    protected ToggleGroup opGroup;
    @FXML
    protected ComboBox<String> marginWidthBox;
    @FXML
    protected CheckBox marginsTopCheck, marginsBottomCheck, marginsLeftCheck, marginsRightCheck;
    @FXML
    protected FlowPane setPane;
    @FXML
    protected HBox colorBox, distanceBox, widthBox;
    @FXML
    protected ControlColorSet colorSetController;
    @FXML
    protected TextField distanceInput;
    @FXML
    protected RadioButton dragRadio, addRadio, blurRadio, cutColorRadio, cutWidthRadio;

    public ImageManufactureBatchMarginsController() {
        baseTitle = Languages.message("ImageManufactureBatchMargins");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(targetPathController.valid.not()
                    .or(Bindings.isEmpty(tableView.getItems()))
                    .or(marginWidthBox.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(marginsTopCheck.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();

        checkOperationType();
        distanceInput.setText("20");
        marginWidthBox.getSelectionModel().select(0);

    }

    @Override
    public void initOptionsSection() {
        try {
            opGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkOperationType();
                }
            });

            distance = UserConfig.getInt(baseName + "Distance", 20);
            distanceInput.setText(distance + "");
            distanceInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkColor();
                }
            });

            width = UserConfig.getInt(baseName + "Width", 20);
            marginWidthBox.getItems().addAll(Arrays.asList(
                    "50", "20", "10", "5", "100", "200", "300", "150", "500"));
            marginWidthBox.setValue(width + "");
            marginWidthBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    checkMarginWidth();
                }
            });

            colorSetController.init(this, baseName + "Color");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void checkOperationType() {
        setPane.getChildren().clear();

        if (addRadio.isSelected()) {
            setPane.getChildren().addAll(colorBox, widthBox);
            checkMarginWidth();
            distanceInput.setStyle(null);

        } else if (cutWidthRadio.isSelected()) {
            setPane.getChildren().addAll(widthBox);
            checkMarginWidth();
            distanceInput.setStyle(null);

        } else if (cutColorRadio.isSelected()) {
            setPane.getChildren().addAll(colorBox, distanceBox);
            marginWidthBox.getEditor().setStyle(null);
            checkColor();

        } else if (blurRadio.isSelected()) {
            setPane.getChildren().addAll(widthBox);
            checkMarginWidth();
            distanceInput.setStyle(null);

        }
        refreshStyle(setPane);

    }

    private void checkMarginWidth() {
        try {
            int v = Integer.parseInt(marginWidthBox.getValue());
            if (v > 0) {
                width = v;
                UserConfig.setInt(baseName + "Width", width);
                ValidationTools.setEditorNormal(marginWidthBox);
            } else {
                ValidationTools.setEditorBadStyle(marginWidthBox);
            }
        } catch (Exception e) {
            ValidationTools.setEditorBadStyle(marginWidthBox);
        }
    }

    protected void checkColor() {
        try {
            int v = Integer.parseInt(distanceInput.getText());
            if (v >= 0 && v <= 255) {
                distance = v;
                UserConfig.setInt(baseName + "Distance", distance);
                distanceInput.setStyle(null);
            } else {
                distanceInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            distanceInput.setStyle(UserConfig.badStyle());
        }
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            BufferedImage target = null;
            if (addRadio.isSelected()) {
                target = MarginTools.addMargins(task, source,
                        FxColorTools.toAwtColor((Color) colorSetController.rect.getFill()), width,
                        marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                        marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());

            } else if (cutWidthRadio.isSelected()) {
                target = MarginTools.cutMargins(task, source,
                        FxColorTools.toAwtColor((Color) colorSetController.rect.getFill()),
                        marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                        marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());

            } else if (cutColorRadio.isSelected()) {
                target = MarginTools.cutMargins(task, source,
                        width,
                        marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                        marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());

            } else if (blurRadio.isSelected()) {
                target = MarginTools.blurMarginsAlpha(task, source,
                        width, marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                        marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());

            }

            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }

    }
}
