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
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.image.ImageManufacture;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-9-26
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchMarginsController extends BaseImageManufactureBatchController {

    protected int width, distance;
    private ImageManufactureMarginsController.OperationType opType;

    @FXML
    protected ToggleGroup opGroup;
    @FXML
    protected ComboBox<String> marginWidthBox;
    @FXML
    protected CheckBox marginsTopCheck, marginsBottomCheck, marginsLeftCheck, marginsRightCheck,
            preAlphaCheck;
    @FXML
    protected FlowPane setPane;
    @FXML
    protected HBox colorBox, distanceBox, widthBox;
    @FXML
    protected ColorSet colorSetController;
    @FXML
    protected TextField distanceInput;
    @FXML
    protected RadioButton blurMarginsRadio;
    @FXML
    protected ImageView preAlphaTipsView;

    public ImageManufactureBatchMarginsController() {
        baseTitle = AppVariables.message("ImageManufactureBatchMargins");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(tableView.getItems()))
                    .or(marginWidthBox.getEditor().styleProperty().isEqualTo(badStyle))
                    .or(marginsTopCheck.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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

            distance = AppVariables.getUserConfigInt(baseName + "Distance", 20);
            distanceInput.setText(distance + "");
            distanceInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkColor();
                }
            });

            width = AppVariables.getUserConfigInt(baseName + "Width", 20);
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
            MyBoxLog.error(e.toString());
        }
    }

    private void checkOperationType() {
        setPane.getChildren().clear();

        RadioButton selected = (RadioButton) opGroup.getSelectedToggle();
        if (message("AddMargins").equals(selected.getText())) {
            opType = ImageManufactureMarginsController.OperationType.AddMargins;
            setPane.getChildren().addAll(colorBox, widthBox);
            checkMarginWidth();
            distanceInput.setStyle(null);

        } else if (message("CutMarginsByWidth").equals(selected.getText())) {
            opType = ImageManufactureMarginsController.OperationType.CutMarginsByWidth;
            setPane.getChildren().addAll(widthBox);
            checkMarginWidth();
            distanceInput.setStyle(null);

        } else if (message("CutMarginsByColor").equals(selected.getText())) {
            opType = ImageManufactureMarginsController.OperationType.CutMarginsByColor;
            setPane.getChildren().addAll(colorBox, distanceBox);
            marginWidthBox.getEditor().setStyle(null);
            checkColor();

        } else if (message("BlurMargins").equals(selected.getText())) {
            opType = ImageManufactureMarginsController.OperationType.BlurMargins;
            setPane.getChildren().addAll(widthBox, preAlphaTipsView, preAlphaCheck);
            checkMarginWidth();
            distanceInput.setStyle(null);

        }
        FxmlControl.refreshStyle(setPane);

    }

    private void checkMarginWidth() {
        try {
            int v = Integer.valueOf(marginWidthBox.getValue());
            if (v > 0) {
                width = v;
                AppVariables.setUserConfigInt(baseName + "Width", width);
                FxmlControl.setEditorNormal(marginWidthBox);
            } else {
                FxmlControl.setEditorBadStyle(marginWidthBox);
            }
        } catch (Exception e) {
            FxmlControl.setEditorBadStyle(marginWidthBox);
        }
    }

    protected void checkColor() {
        try {
            int v = Integer.valueOf(distanceInput.getText());
            if (v >= 0 && v <= 255) {
                distance = v;
                AppVariables.setUserConfigInt(baseName + "Distance", distance);
                distanceInput.setStyle(null);
            } else {
                distanceInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            distanceInput.setStyle(badStyle);
        }
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            BufferedImage target;
            switch (opType) {
                case CutMarginsByWidth:
                    target = ImageManufacture.cutMargins(source,
                            FxmlImageManufacture.toAwtColor((Color) colorSetController.rect.getFill()),
                            marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                            marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());
                    break;
                case CutMarginsByColor:
                    target = ImageManufacture.cutMargins(source,
                            width,
                            marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                            marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());
                    break;
                case AddMargins:
                    target = ImageManufacture.addMargins(source,
                            FxmlImageManufacture.toAwtColor((Color) colorSetController.rect.getFill()), width,
                            marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                            marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());
                    break;
                case BlurMargins:
                    if (preAlphaCheck.isSelected()) {
                        target = ImageManufacture.blurMarginsNoAlpha(source,
                                width,
                                marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                                marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());

                    } else {
                        target = ImageManufacture.blurMarginsAlpha(source,
                                width,
                                marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                                marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());

                    }
                    break;
                default:
                    return null;
            }

            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }

    }
}
