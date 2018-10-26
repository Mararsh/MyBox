package mara.mybox.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.image.ImageGrayTools;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.ImageScope;
import mara.mybox.objects.ImageScope.AreaScopeType;
import mara.mybox.objects.ImageScope.OperationType;
import mara.mybox.tools.FxmlImageTools;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureFiltersController extends ImageManufactureController {

    protected ImageScope filtersScope;
    protected int filtersOperationType, threshold;

    @FXML
    protected ToggleGroup filtersGroup;
    @FXML
    protected Slider binarySlider;
    @FXML
    protected Button binaryCalculateButton, binaryOkButton, filtersScopeButton;
    @FXML
    protected RadioButton grayRadio, bwRadio;
    @FXML
    protected HBox bwBox;
    @FXML
    protected TextField thresholdInput;

    public static class FiltersOperationType {

        public static int Gray = 0;
        public static int Invert = 1;
        public static int BlackOrWhite = 2;
        public static int Red = 3;
        public static int Green = 4;
        public static int Blue = 5;
        public static int RedInvert = 6;
        public static int GreenInvert = 7;
        public static int BlueInvert = 8;

    }

    public ImageManufactureFiltersController() {
    }

    @Override
    protected void initializeNext2() {
        try {
            initCommon();
            initFiltersTab();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void initInterface() {
        try {
            if (values == null || values.getImage() == null) {
                return;
            }
            super.initInterface();

            isSettingValues = true;

            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void initFiltersTab() {
        try {
            filtersScope = new ImageScope();
            filtersScope.setOperationType(OperationType.Filters);
            filtersScope.setAllColors(true);
            filtersScope.setAreaScopeType(AreaScopeType.AllArea);

            filtersGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkFiltersOperationType();
                }
            });
            checkFiltersOperationType();

            binarySlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    threshold = newValue.intValue();
                    thresholdInput.setText(threshold + "");
                }
            });
            binarySlider.setValue(50);

            thresholdInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkThresholdInput();
                }
            });

            Tooltip stips = new Tooltip(getMessage("ScopeComments"));
            stips.setFont(new Font(16));
            FxmlTools.setComments(filtersScopeButton, stips);

            stips = new Tooltip(getMessage("GrayBinaryComments"));
            stips.setFont(new Font(16));
            FxmlTools.setComments(grayRadio, stips);
            FxmlTools.setComments(bwRadio, stips);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkFiltersOperationType() {
        RadioButton selected = (RadioButton) filtersGroup.getSelectedToggle();
        if (getMessage("BlackOrWhite").equals(selected.getText())) {
            filtersOperationType = FiltersOperationType.BlackOrWhite;
            bwBox.setDisable(false);
        } else {
            if (getMessage("Gray").equals(selected.getText())) {
                filtersOperationType = FiltersOperationType.Gray;
            } else if (getMessage("Invert").equals(selected.getText())) {
                filtersOperationType = FiltersOperationType.Invert;
            } else if (getMessage("Red").equals(selected.getText())) {
                filtersOperationType = FiltersOperationType.Red;
            } else if (getMessage("Green").equals(selected.getText())) {
                filtersOperationType = FiltersOperationType.Green;
            } else if (getMessage("Blue").equals(selected.getText())) {
                filtersOperationType = FiltersOperationType.Blue;
            } else if (getMessage("RedInvert").equals(selected.getText())) {
                filtersOperationType = FiltersOperationType.RedInvert;
            } else if (getMessage("GreenInvert").equals(selected.getText())) {
                filtersOperationType = FiltersOperationType.GreenInvert;
            } else if (getMessage("BlueInvert").equals(selected.getText())) {
                filtersOperationType = FiltersOperationType.BlueInvert;
            }
            bwBox.setDisable(true);
        }
    }

    private void checkThresholdInput() {
        try {
            threshold = Integer.valueOf(thresholdInput.getText());
            if (threshold >= 0 && threshold <= binarySlider.getMax()) {
                thresholdInput.setStyle(null);
                binarySlider.setValue(threshold);
            } else {
                thresholdInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            thresholdInput.setStyle(badStyle);
        }
    }

    @FXML
    public void setFiltersScope() {
        setScope(filtersScope);
    }

    @Override
    protected void setScopePane() {
        try {
            showScopeCheck.setDisable(false);
            values.setCurrentScope(filtersScope);
            scopePaneValid = true;
            super.setScopePane();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void scopeDetermined(ImageScope imageScope) {
        values.setCurrentScope(imageScope);
        filtersScope = imageScope;
        setScopePane();
    }

    @FXML
    public void wholeFiltersScope() {
        filtersScope = new ImageScope();
        filtersScope.setOperationType(OperationType.Filters);
        filtersScope.setAllColors(true);
        filtersScope.setAreaScopeType(AreaScopeType.AllArea);
        setScopePane();
    }

    @FXML
    public void calculateThreshold() {
        threshold = ImageGrayTools.calculateThreshold(values.getSourceFile());
        threshold = threshold * 100 / 256;
        binarySlider.setValue(threshold);
    }

    @FXML
    public void filtersAction() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage;
                if (filtersOperationType == FiltersOperationType.Gray) {
                    newImage = FxmlImageTools.makeGray(values.getCurrentImage(), filtersScope);
                } else if (filtersOperationType == FiltersOperationType.Invert) {
                    newImage = FxmlImageTools.makeInvert(values.getCurrentImage(), filtersScope);
                } else if (filtersOperationType == FiltersOperationType.BlackOrWhite) {
                    newImage = FxmlImageTools.makeBinaryFx(values.getCurrentImage(), threshold, filtersScope);
                } else if (filtersOperationType == FiltersOperationType.Red) {
                    newImage = FxmlImageTools.keepRed(values.getCurrentImage(), filtersScope);
                } else if (filtersOperationType == FiltersOperationType.Green) {
                    newImage = FxmlImageTools.keepGreen(values.getCurrentImage(), filtersScope);
                } else if (filtersOperationType == FiltersOperationType.Blue) {
                    newImage = FxmlImageTools.keepBlue(values.getCurrentImage(), filtersScope);
                } else if (filtersOperationType == FiltersOperationType.RedInvert) {
                    newImage = FxmlImageTools.makeRedInvert(values.getCurrentImage(), filtersScope);
                } else if (filtersOperationType == FiltersOperationType.GreenInvert) {
                    newImage = FxmlImageTools.makeGreenInvert(values.getCurrentImage(), filtersScope);
                } else if (filtersOperationType == FiltersOperationType.BlueInvert) {
                    newImage = FxmlImageTools.makeBlueInvert(values.getCurrentImage(), filtersScope);
                } else {
                    return null;
                }
                recordImageHistory(ImageOperationType.Filters, newImage);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

}
