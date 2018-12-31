package mara.mybox.controller;

import java.util.Arrays;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import static mara.mybox.objects.AppVaribles.logger;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.fxml.FxmlMarginsTools;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.fxml.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureMarginsController extends ImageManufactureController {

    protected int width, distance;
    private OperationType opType;

    @FXML
    protected ToggleGroup opGroup;
    @FXML
    protected ComboBox marginWidthBox;
    @FXML
    protected ColorPicker marginsColorPicker;
    @FXML
    protected Button marginsWhiteButton, marginsBlackButton, marginsOkButton, marginsTrButton;
    @FXML
    protected CheckBox marginsTopCheck, marginsBottomCheck, marginsLeftCheck, marginsRightCheck;
    @FXML
    private HBox colorBox, distanceBox, widthBox;
    @FXML
    private Label promptLabel;
    @FXML
    private TextField distanceInput;

    public ImageManufactureMarginsController() {
    }

    public enum OperationType {
        CutMarginsByColor,
        CutMarginsByWidth,
        AddMargins
    }

    @Override
    protected void initializeNext2() {
        try {
            initCommon();
            initMarginsTab();
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
            if (CommonValues.NoAlphaImages.contains(values.getImageInfo().getImageFormat())) {
                marginsTrButton.setDisable(true);
            } else {
                marginsTrButton.setDisable(false);
            }

            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void initMarginsTab() {
        try {
            opGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkOperationType();
                }
            });
            checkOperationType();

            distanceInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkColor();
                }
            });
            distanceInput.setText("20");

            marginWidthBox.getItems().addAll(Arrays.asList("5", "10", "2", "15", "20", "30", "1"));
            marginWidthBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkMarginWidth();
                }
            });
            marginWidthBox.getSelectionModel().select(0);

            marginsOkButton.disableProperty().bind(
                    marginWidthBox.getEditor().styleProperty().isEqualTo(badStyle)
                            .or(distanceInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkOperationType() {
        RadioButton selected = (RadioButton) opGroup.getSelectedToggle();
        if (getMessage("AddMargins").equals(selected.getText())) {
            opType = OperationType.AddMargins;
            colorBox.setDisable(false);
            distanceBox.setDisable(true);
            widthBox.setDisable(false);
            checkMarginWidth();
            promptLabel.setVisible(true);
            distanceInput.setStyle(null);

        } else if (getMessage("CutMarginsByWidth").equals(selected.getText())) {
            opType = OperationType.CutMarginsByWidth;
            colorBox.setDisable(true);
            distanceBox.setDisable(true);
            widthBox.setDisable(false);
            checkMarginWidth();
            promptLabel.setVisible(false);
            distanceInput.setStyle(null);

        } else if (getMessage("CutMarginsByColor").equals(selected.getText())) {
            opType = OperationType.CutMarginsByColor;
            colorBox.setDisable(false);
            distanceBox.setDisable(false);
            widthBox.setDisable(true);
            marginWidthBox.getEditor().setStyle(null);
            promptLabel.setVisible(true);
            checkColor();
        }
    }

    private void checkMarginWidth() {
        try {
            width = Integer.valueOf((String) marginWidthBox.getSelectionModel().getSelectedItem());
            if (width > 0) {
                marginWidthBox.getEditor().setStyle(null);
            } else {
                width = 0;
                marginWidthBox.getEditor().setStyle(badStyle);
            }

        } catch (Exception e) {
            width = 0;
            marginWidthBox.getEditor().setStyle(badStyle);
        }
    }

    protected void checkColor() {
        try {
            distance = Integer.valueOf(distanceInput.getText());
            distanceInput.setStyle(null);
            if (distance >= 0 && distance <= 255) {
                distanceInput.setStyle(null);
            } else {
                distanceInput.setStyle(badStyle);
                distance = 0;
            }
        } catch (Exception e) {
            distanceInput.setStyle(badStyle);
            distance = 0;
        }
    }

    @FXML
    public void setTransparentAction() {
        marginsColorPicker.setValue(Color.TRANSPARENT);
    }

    @FXML
    public void setBlackAction() {
        marginsColorPicker.setValue(Color.BLACK);
    }

    @FXML
    public void setWhiteAction() {
        marginsColorPicker.setValue(Color.WHITE);
    }

    @FXML
    @Override
    public void clickImage(MouseEvent event) {
        if (values.getCurrentImage() == null || opType == OperationType.CutMarginsByWidth) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        imageView.setCursor(Cursor.HAND);

        int x = (int) Math.round(event.getX() * values.getCurrentImage().getWidth() / imageView.getBoundsInLocal().getWidth());
        int y = (int) Math.round(event.getY() * values.getCurrentImage().getHeight() / imageView.getBoundsInLocal().getHeight());

        PixelReader pixelReader = values.getCurrentImage().getPixelReader();
        Color color = pixelReader.getColor(x, y);
        marginsColorPicker.setValue(color);

    }

    @FXML
    public void marginsAction() {
        if (!marginsTopCheck.isSelected()
                && !marginsBottomCheck.isSelected()
                && !marginsLeftCheck.isSelected()
                && !marginsRightCheck.isSelected()) {
            popError(AppVaribles.getMessage("NothingHandled"));
            return;
        }
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    final Image newImage;
                    if (null == opType) {
                        return null;
                    } else {
                        switch (opType) {
                            case CutMarginsByWidth:
                                newImage = FxmlMarginsTools.cutMarginsByWidth(values.getCurrentImage(), width,
                                        marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                                        marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());
                                recordImageHistory(ImageOperationType.Cut_Margins, newImage);
                                break;
                            case CutMarginsByColor:
                                newImage = FxmlMarginsTools.cutMarginsByColor(values.getCurrentImage(),
                                        marginsColorPicker.getValue(), distance,
                                        marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                                        marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());
                                recordImageHistory(ImageOperationType.Cut_Margins, newImage);
                                break;
                            case AddMargins:
                                newImage = FxmlMarginsTools.addMarginsFx(values.getCurrentImage(),
                                        marginsColorPicker.getValue(), width,
                                        marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                                        marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());
                                recordImageHistory(ImageOperationType.Add_Margins, newImage);
                                break;
                            default:
                                return null;
                        }
                    }
                    if (task.isCancelled()) {
                        return null;
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            values.setUndoImage(values.getCurrentImage());
                            values.setCurrentImage(newImage);
                            imageView.setImage(newImage);
                            paneSize();
                            setImageChanged(true);
                            setBottomLabel();
                        }
                    });
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

}
