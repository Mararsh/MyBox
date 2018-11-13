package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.image.ImageConvertTools;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.fxml.FxmlImageTools;
import mara.mybox.fxml.FxmlTools;
import static mara.mybox.fxml.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-9-25
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchShadowController extends ImageManufactureBatchController {

    private int shadow, percent;
    private boolean isPercent;

    @FXML
    private ColorPicker shadowColorPicker;
    @FXML
    private Button transShadowButton;
    @FXML
    private ToggleGroup shadowGroup;
    @FXML
    private ComboBox<String> perBox, shadowBox;

    public ImageManufactureBatchShadowController() {

    }

    @Override
    protected void initializeNext2() {
        try {

            operationBarController.startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(sourceFilesInformation))
                    .or(shadowBox.getEditor().styleProperty().isEqualTo(badStyle))
                    .or(perBox.getEditor().styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    protected void initOptionsSection() {
        try {
            super.initOptionsSection();

            shadowBox.getItems().addAll(Arrays.asList("8", "5", "15", "3", "6", "1", "20", "30"));
            shadowBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkShadow();
                }
            });
            shadowBox.getSelectionModel().select(0);

            Tooltip tips = new Tooltip("1~100");
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(perBox, tips);

            perBox.getItems().addAll(Arrays.asList("2", "1", "3", "5", "4", "6", "8", "7", "10", "9"));
            perBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkPercent();
                }
            });
            perBox.getSelectionModel().select(0);

            shadowGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkType();
                }
            });
            checkType();

            shadowColorPicker.setValue(Color.BLACK);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkType() {
        shadowBox.setDisable(true);
        shadowBox.getEditor().setStyle(null);
        perBox.setDisable(true);
        perBox.getEditor().setStyle(null);

        RadioButton selected = (RadioButton) shadowGroup.getSelectedToggle();
        if (getMessage("WidthPercentage").equals(selected.getText())) {
            isPercent = true;
            perBox.setDisable(false);
            checkPercent();

        } else if (getMessage("Custom").equals(selected.getText())) {
            isPercent = false;
            shadowBox.setDisable(false);
            checkShadow();

        }
    }

    private void checkPercent() {
        try {
            percent = Integer.valueOf(perBox.getValue());
            if (percent > 0 && percent <= 100) {
                perBox.getEditor().setStyle(null);
            } else {
                percent = 15;
                perBox.getEditor().setStyle(badStyle);
            }
        } catch (Exception e) {
            logger.debug(e.toString());
            percent = 15;
            perBox.getEditor().setStyle(badStyle);
        }
    }

    private void checkShadow() {
        try {
            shadow = Integer.valueOf(shadowBox.getValue());
            if (shadow >= 0) {
                shadowBox.getEditor().setStyle(null);
            } else {
                shadow = 0;
                shadowBox.getEditor().setStyle(badStyle);
            }
        } catch (Exception e) {
            logger.debug(e.toString());
            shadow = 0;
            shadowBox.getEditor().setStyle(badStyle);
        }
    }

    @FXML
    public void shadowTransparentAction() {
        shadowColorPicker.setValue(Color.TRANSPARENT);
    }

    @FXML
    public void shadowWhiteAction() {
        shadowColorPicker.setValue(Color.WHITE);
    }

    @FXML
    public void shadowBlackAction() {
        shadowColorPicker.setValue(Color.BLACK);
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            int value = shadow;
            if (isPercent) {
                value = source.getWidth() * percent / 100;
            }
            Color color = shadowColorPicker.getValue();
            BufferedImage target = ImageConvertTools.addShadow(source, value, FxmlImageTools.colorConvert(color));

            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

}
