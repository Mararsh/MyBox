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
import mara.mybox.image.FxmlImageTools;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-9-25
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchArcController extends ImageManufactureBatchController {

    private final String ImageArcKey, ImageArcPerKey;
    private int arc, percent;
    private boolean isPercent;

    @FXML
    private ColorPicker arcColorPicker;
    @FXML
    private Button transForArcButton;
    @FXML
    private ComboBox<String> arcBox, perBox;
    @FXML
    private ToggleGroup arcGroup;

    public ImageManufactureBatchArcController() {
        ImageArcKey = "ImageArcKey";
        ImageArcPerKey = "ImageArcPerKey";
    }

    @Override
    protected void initializeNext2() {
        try {

            operationBarController.startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(sourceFilesInformation))
                    .or(arcBox.getEditor().styleProperty().isEqualTo(badStyle))
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

            arcBox.getItems().addAll(Arrays.asList("15", "30", "50", "150", "300", "10", "3"));
            arcBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkArc();
                }
            });
            arcBox.getSelectionModel().select(0);

            Tooltip tips = new Tooltip("1~100");
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(perBox, tips);

            perBox.getItems().addAll(Arrays.asList("15", "25", "30", "10", "12", "8"));
            perBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkPercent();
                }
            });
            perBox.getSelectionModel().select(0);

            arcGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkType();
                }
            });
            checkType();

            arcColorPicker.setValue(Color.TRANSPARENT);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkType() {
        arcBox.setDisable(true);
        arcBox.getEditor().setStyle(null);
        perBox.setDisable(true);
        perBox.getEditor().setStyle(null);

        RadioButton selected = (RadioButton) arcGroup.getSelectedToggle();
        if (getMessage("WidthPercentage").equals(selected.getText())) {
            isPercent = true;
            perBox.setDisable(false);
            checkPercent();

        } else if (getMessage("Custom").equals(selected.getText())) {
            isPercent = false;
            arcBox.setDisable(false);
            checkArc();

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

    private void checkArc() {
        try {
            arc = Integer.valueOf(arcBox.getValue());
            if (arc >= 0) {
                arcBox.getEditor().setStyle(null);
            } else {
                arc = 0;
                arcBox.getEditor().setStyle(badStyle);
            }
        } catch (Exception e) {
            logger.debug(e.toString());
            arc = 0;
            arcBox.getEditor().setStyle(badStyle);
        }
    }

    @FXML
    public void arcTransparentAction() {
        arcColorPicker.setValue(Color.TRANSPARENT);
    }

    @FXML
    public void arcWhiteAction() {
        arcColorPicker.setValue(Color.WHITE);
    }

    @FXML
    public void arcBlackAction() {
        arcColorPicker.setValue(Color.BLACK);
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            int value = arc;
            if (isPercent) {
                value = source.getWidth() * percent / 100;
            }
            BufferedImage target = ImageConvertTools.addArc(source, value,
                    FxmlImageTools.colorConvert(arcColorPicker.getValue()));
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

}
