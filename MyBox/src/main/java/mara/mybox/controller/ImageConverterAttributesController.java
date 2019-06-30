package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import mara.mybox.controller.base.ImageAttributesBaseController;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.image.ImageManufacture.KeepRatioType;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;
import mara.mybox.value.CommonValues;
import mara.mybox.image.ImageAttributes;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;

/**
 * @Author Mara
 * @CreateDate 2018-6-26
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageConverterAttributesController extends ImageAttributesBaseController {

    public boolean noRatio;

    @FXML
    public HBox imageConvertAttributesPane;
    @FXML
    public ToggleGroup ratioGroup;
    @FXML
    public TextField xInput, yInput;
    @FXML
    public Button originalButton;
    @FXML
    public CheckBox keepCheck, alphaCheck;
    @FXML
    public HBox ratioBox, ratioBaseBox, sizeBox;

    @Override
    public void initializeNext2() {

        xInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    long v = Long.valueOf(xInput.getText());
                    if (v >= 0) {
                        xInput.setStyle(null);
                        checkRatio();
                    } else {
                        xInput.setStyle(badStyle);
                    }
                } catch (Exception e) {
                    xInput.setStyle(badStyle);
                }
            }
        });

        yInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    long v = Long.valueOf(yInput.getText());
                    if (v >= 0) {
                        yInput.setStyle(null);
                        checkRatio();
                    } else {
                        yInput.setStyle(badStyle);
                    }
                } catch (Exception e) {
                    yInput.setStyle(badStyle);
                }
            }
        });

        keepCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                imageAttributes.setKeepRatio(newValue);
                ratioBaseBox.setDisable(!newValue);
                if (newValue) {
                    checkRatio();
                }
            }
        });
        imageAttributes.setKeepRatio(keepCheck.isSelected());

        ratioGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkRatioAdjustion();
            }
        });
        FxmlControl.setRadioSelected(ratioGroup, AppVaribles.getUserConfigValue("ic_ratioAdjustion", getMessage("BaseOnWidth")));
        checkRatioAdjustion();

        imageAttributes.setSourceWidth(0);
        imageAttributes.setSourceHeight(0);
    }

    @FXML
    public void setOriginalSize() {
        noRatio = true;
        if (imageAttributes.getSourceWidth() > 0) {
            xInput.setText(imageAttributes.getSourceWidth() + "");
        }
        if (imageAttributes.getSourceHeight() > 0) {
            yInput.setText(imageAttributes.getSourceHeight() + "");
        }
        noRatio = false;
    }

    @FXML
    public void openPixelsCalculator(ActionEvent event) {
        try {
            final PixelsCalculationController controller
                    = (PixelsCalculationController) FxmlStage.openStage(myStage,
                            CommonValues.PixelsCalculatorFxml,
                            true, Modality.WINDOW_MODAL, null);

            controller.setSource(imageAttributes, xInput, yInput);

            noRatio = true;
            controller.myStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    noRatio = false;
                    if (!controller.leavingScene()) {
                        event.consume();
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void checkRatioAdjustion() {
        try {
            RadioButton selected = (RadioButton) ratioGroup.getSelectedToggle();
            String s = selected.getText();
            AppVaribles.setUserConfigValue("ic_ratioAdjustion", s);
            if (getMessage("BaseOnWidth").equals(s)) {
                imageAttributes.setRatioAdjustion(KeepRatioType.BaseOnWidth);
            } else if (getMessage("BaseOnHeight").equals(s)) {
                imageAttributes.setRatioAdjustion(KeepRatioType.BaseOnHeight);
            } else if (getMessage("BaseOnLarger").equals(s)) {
                imageAttributes.setRatioAdjustion(KeepRatioType.BaseOnLarger);
            } else if (getMessage("BaseOnSmaller").equals(s)) {
                imageAttributes.setRatioAdjustion(KeepRatioType.BaseOnSmaller);
            } else {
                imageAttributes.setRatioAdjustion(KeepRatioType.None);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void checkRatio() {
        try {
            long x = Long.valueOf(xInput.getText());
            long y = Long.valueOf(yInput.getText());
            imageAttributes.setTargetWidth((int) x);
            imageAttributes.setTargetHeight((int) y);
            int sourceX = imageAttributes.getSourceWidth();
            int sourceY = imageAttributes.getSourceHeight();
            if (noRatio || !imageAttributes.isKeepRatio() || sourceX <= 0 || sourceY <= 0) {
                return;
            }
            long ratioX = Math.round(x * 1000 / sourceX);
            long ratioY = Math.round(y * 1000 / sourceY);
            if (ratioX == ratioY) {
                return;
            }
            switch (imageAttributes.getRatioAdjustion()) {
                case KeepRatioType.BaseOnWidth:
                    yInput.setText(Math.round(x * sourceY / sourceX) + "");
                    break;
                case KeepRatioType.BaseOnHeight:
                    xInput.setText(Math.round(y * sourceX / sourceY) + "");
                    break;
                case KeepRatioType.BaseOnLarger:
                    if (ratioX > ratioY) {
                        yInput.setText(Math.round(x * sourceY / sourceX) + "");
                    } else {
                        xInput.setText(Math.round(y * sourceX / sourceY) + "");
                    }
                    break;
                case KeepRatioType.BaseOnSmaller:
                    if (ratioX > ratioY) {
                        xInput.setText(Math.round(y * sourceX / sourceY) + "");
                    } else {
                        yInput.setText(Math.round(x * sourceY / sourceX) + "");
                    }
                    break;
                default:
                    break;
            }
            x = Long.valueOf(xInput.getText());
            y = Long.valueOf(yInput.getText());
            imageAttributes.setTargetWidth((int) x);
            imageAttributes.setTargetHeight((int) y);
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public void getFinalXY() {
        try {
            long x = Long.valueOf(xInput.getText());
            long y = Long.valueOf(yInput.getText());
            imageAttributes.setTargetWidth((int) x);
            imageAttributes.setTargetHeight((int) y);
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public ImageAttributes getAttributes() {
        getFinalXY();
        return imageAttributes;
    }

}
