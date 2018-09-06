package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.stage.WindowEvent;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.image.ImageConvertionTools.KeepRatioType;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.ImageAttributes;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-6-26
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageConverterAttributesController extends ImageAttributesBaseController {

    private boolean noRatio;

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

    @Override
    protected void initializeNext2() {

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
        noRatio = true;
        if (attributes.getSourceWidth() > 0) {
            xInput.setText(attributes.getSourceWidth() + "");
        }
        if (attributes.getSourceHeight() > 0) {
            yInput.setText(attributes.getSourceHeight() + "");
        }
        noRatio = false;
    }

    @FXML
    protected void openPixelsCalculator(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.PixelsCalculatorFxml), AppVaribles.CurrentBundle);
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
            noRatio = true;
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    noRatio = false;
                }
            });
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
                attributes.setRatioAdjustion(KeepRatioType.BaseOnWidth);
            } else if (getMessage("BaseOnHeight").equals(s)) {
                attributes.setRatioAdjustion(KeepRatioType.BaseOnHeight);
            } else if (getMessage("BaseOnLarger").equals(s)) {
                attributes.setRatioAdjustion(KeepRatioType.BaseOnLarger);
            } else if (getMessage("BaseOnSmaller").equals(s)) {
                attributes.setRatioAdjustion(KeepRatioType.BaseOnSmaller);
            } else {
                attributes.setRatioAdjustion(KeepRatioType.None);
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
            if (noRatio || !attributes.isKeepRatio() || sourceX <= 0 || sourceY <= 0) {
                return;
            }
            long ratioX = Math.round(x * 1000 / sourceX);
            long ratioY = Math.round(y * 1000 / sourceY);
            if (ratioX == ratioY) {
                return;
            }
            switch (attributes.getRatioAdjustion()) {
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
            attributes.setTargetWidth((int) x);
            attributes.setTargetHeight((int) y);
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

    @Override
    public ImageAttributes getAttributes() {
        getFinalXY();
        return attributes;
    }

}
