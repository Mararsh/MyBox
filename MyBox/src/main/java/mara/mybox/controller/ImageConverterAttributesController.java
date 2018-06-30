package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.tools.FxmlTools;

/**
 * @Author Mara
 * @CreateDate 2018-6-26
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageConverterAttributesController extends ImageAttributesBaseController {

    @FXML
    private HBox imageConvertAttributesPane;
    @FXML
    protected ToggleGroup ratioGroup;
    @FXML
    protected TextField xInput, yInput;
    @FXML
    protected Button originalButton;
    @FXML
    protected CheckBox keepCheck;
    @FXML
    protected HBox ratioBox, ratioBaseBox;

    public static class RatioAdjustion {

        public static int BaseOnWidth = 0;
        public static int BaseOnHeight = 1;
        public static int BaseOnLarger = 2;
        public static int BaseOnSmaller = 3;
        public static int None = 9;
    }

    @Override
    protected void initializeNext2() {

        FxmlTools.setNonnegativeValidation(xInput);
        FxmlTools.setNonnegativeValidation(yInput);

        keepCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                attributes.setKeepRatio(newValue);
                ratioBaseBox.setDisable(!newValue);
                if (newValue) {
                    checkRatio();
                }
            }
        });
        attributes.setKeepRatio(keepCheck.isSelected());

        ratioGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkRatioAdjustion();
            }
        });
        FxmlTools.setRadioSelected(ratioGroup, AppVaribles.getConfigValue("ic_ratioAdjustion", getMessage("BaseOnWidth")));
        checkRatioAdjustion();

        attributes.setSourceWidth(0);
        attributes.setSourceHeight(0);
    }

    @FXML
    protected void setOriginalSize() {
        if (attributes.getSourceWidth() > 0) {
            xInput.setText(attributes.getSourceWidth() + "");
        }
        if (attributes.getSourceHeight() > 0) {
            yInput.setText(attributes.getSourceHeight() + "");
        }
    }

    @FXML
    protected void openPixelsCalculator(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.PixelsCalculator), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            PixelsCalculationController controller = fxmlLoader.getController();
            Stage stage = new Stage();
            controller.setMyStage(stage);
            controller.setSource(attributes, xInput, yInput);

            Scene scene = new Scene(pane);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(getMyStage());
            stage.setTitle(AppVaribles.getMessage("PixelsCalculator"));
            stage.getIcons().add(CommonValues.AppIcon);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void checkRatioAdjustion() {
        try {
            RadioButton selected = (RadioButton) ratioGroup.getSelectedToggle();
            String s = selected.getText();
            AppVaribles.setConfigValue("ic_ratioAdjustion", s);
            if (getMessage("BaseOnWidth").equals(s)) {
                attributes.setRatioAdjustion(RatioAdjustion.BaseOnWidth);
            } else if (getMessage("BaseOnHeight").equals(s)) {
                attributes.setRatioAdjustion(RatioAdjustion.BaseOnHeight);
            } else if (getMessage("BaseOnLarger").equals(s)) {
                attributes.setRatioAdjustion(RatioAdjustion.BaseOnLarger);
            } else if (getMessage("BaseOnSamller").equals(s)) {
                attributes.setRatioAdjustion(RatioAdjustion.BaseOnSmaller);
            } else {
                attributes.setRatioAdjustion(RatioAdjustion.None);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkRatio() {
        try {
            long x = Long.valueOf(xInput.getText());
            long y = Long.valueOf(yInput.getText());
            attributes.setTargetWidth((int) x);
            attributes.setTargetHeight((int) y);
            int sourceX = attributes.getSourceWidth();
            int sourceY = attributes.getSourceHeight();
            if (!attributes.isKeepRatio() || sourceX <= 0 || sourceY <= 0) {
                return;
            }
            long ratioX = Math.round(x * 1000 / sourceX);
            long ratioY = Math.round(y * 1000 / sourceY);
            if (ratioX == ratioY) {
                return;
            }
            if (attributes.getRatioAdjustion() == RatioAdjustion.BaseOnWidth) {
                yInput.setText(Math.round(x * sourceY / sourceX) + "");
            } else if (attributes.getRatioAdjustion() == RatioAdjustion.BaseOnHeight) {
                xInput.setText(Math.round(y * sourceX / sourceY) + "");
            } else if (attributes.getRatioAdjustion() == RatioAdjustion.BaseOnLarger) {
                if (ratioX > ratioY) {
                    yInput.setText(Math.round(x * sourceY / sourceX) + "");
                } else {
                    xInput.setText(Math.round(y * sourceX / sourceY) + "");
                }
            } else if (attributes.getRatioAdjustion() == RatioAdjustion.BaseOnSmaller) {
                if (ratioX > ratioY) {
                    xInput.setText(Math.round(y * sourceX / sourceY) + "");
                } else {
                    yInput.setText(Math.round(x * sourceY / sourceX) + "");
                }
            }
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public void getFinalXY() {
        try {
            long x = Long.valueOf(xInput.getText());
            long y = Long.valueOf(yInput.getText());
            attributes.setTargetWidth((int) x);
            attributes.setTargetHeight((int) y);
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public HBox getImageConvertAttributesPane() {
        return imageConvertAttributesPane;
    }

    public void setImageConvertAttributesPane(HBox imageConvertAttributesPane) {
        this.imageConvertAttributesPane = imageConvertAttributesPane;
    }

    public RadioButton getPcxSelect() {
        return pcxSelect;
    }

    public void setPcxSelect(RadioButton pcxSelect) {
        this.pcxSelect = pcxSelect;
    }

    public TextField getxInput() {
        return xInput;
    }

    public void setxInput(TextField xInput) {
        this.xInput = xInput;
    }

    public TextField getyInput() {
        return yInput;
    }

    public void setyInput(TextField yInput) {
        this.yInput = yInput;
    }

    public Button getOriginalButton() {
        return originalButton;
    }

    public void setOriginalButton(Button originalButton) {
        this.originalButton = originalButton;
    }

    public CheckBox getKeepCheck() {
        return keepCheck;
    }

    public void setKeepCheck(CheckBox keepCheck) {
        this.keepCheck = keepCheck;
    }

    public ToggleGroup getRatioGroup() {
        return ratioGroup;
    }

    public void setRatioGroup(ToggleGroup ratioGroup) {
        this.ratioGroup = ratioGroup;
    }

    public HBox getRatioBox() {
        return ratioBox;
    }

    public void setRatioBox(HBox ratioBox) {
        this.ratioBox = ratioBox;
    }

}
