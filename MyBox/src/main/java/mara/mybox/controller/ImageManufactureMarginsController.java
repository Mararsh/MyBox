package mara.mybox.controller;

import java.text.MessageFormat;
import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import mara.mybox.controller.ImageManufactureController.ImageOperation;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-9-4
 * @License Apache License Version 2.0
 */
public class ImageManufactureMarginsController extends ImageManufactureOperationController {

    protected int addedWidth, distance;
    private OperationType opType;

    @FXML
    protected ToggleGroup opGroup;
    @FXML
    protected ComboBox<String> marginWidthBox;
    @FXML
    protected CheckBox marginsTopCheck, marginsBottomCheck, marginsLeftCheck, marginsRightCheck,
            preAlphaCheck;
    @FXML
    private FlowPane colorBox, distanceBox, marginsBox, alphaBox;
    @FXML
    private HBox widthBox;
    @FXML
    private TextField distanceInput;
    @FXML
    protected RadioButton blurMarginsRadio, dragRadio;
    @FXML
    protected ImageView preAlphaTipsView;
    @FXML
    protected VBox setBox;
    @FXML
    protected Button paletteButton;
    @FXML
    protected Rectangle bgRect;

    public enum OperationType {
        SetMarginsByDragging,
        CutMarginsByColor,
        CutMarginsByWidth,
        AddMargins,
        BlurMargins
    }

    public ImageManufactureMarginsController() {
        baseTitle = AppVariables.message("ImageManufactureMargins");
        operation = ImageOperation.Margins;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            myPane = marginsPane;

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    public void initPane(ImageManufactureController parent) {
        try {
            super.initPane(parent);
            if (parent == null) {
                return;
            }

            opGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkOperationType();
                }
            });

            if (parent.imageInformation != null
                    && CommonValues.NoAlphaImages.contains(parent.imageInformation.getImageFormat())) {
                preAlphaCheck.setSelected(true);
                preAlphaCheck.setDisable(true);
            } else {
                preAlphaCheck.setSelected(false);
                preAlphaCheck.setDisable(false);
            }
            String c = AppVariables.getUserConfigValue("ImageMarginsColor", Color.TRANSPARENT.toString());
            bgRect.setFill(Color.web(c));
            FxmlControl.setTooltip(bgRect, FxmlColor.colorNameDisplay((Color) bgRect.getFill()));

            marginWidthBox.getItems().clear();
            int width = (int) imageView.getImage().getWidth();
            marginWidthBox.getItems().addAll(Arrays.asList(
                    width / 6 + "", width / 8 + "", width / 4 + "", width / 10 + "",
                    "20", "10", "5", "100", "200", "300", "50", "150", "500"));
            marginWidthBox.getSelectionModel().select(AppVariables.getUserConfigInt("ImageMarginsWidth", 20) + "");

            distanceInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkColorDistance();
                }
            });
            distanceInput.setText("20");
            distanceInput.setText(AppVariables.getUserConfigInt("ImageMarginsColorDistance", 20) + "");

            marginWidthBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    checkMarginWidth();
                }
            });

            okButton.disableProperty().bind(
                    marginWidthBox.getEditor().styleProperty().isEqualTo(badStyle)
                            .or(distanceInput.styleProperty().isEqualTo(badStyle))
            );

            checkOperationType();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void checkOperationType() {
        setBox.getChildren().clear();
        FxmlControl.setEditorNormal(marginWidthBox);
        distanceInput.setStyle(null);
        imageController.imageLabel.setText("");

        if (opGroup.getSelectedToggle() == null) {
            imageController.clearOperating();
            return;
        }

        RadioButton selected = (RadioButton) opGroup.getSelectedToggle();
        if (message("Dragging").equals(selected.getText())) {
            opType = OperationType.SetMarginsByDragging;
            setBox.getChildren().addAll(colorBox);
            initDragging();

        } else {

            imageController.clearOperating();

            if (message("AddMargins").equals(selected.getText())) {
                opType = OperationType.AddMargins;
                setBox.getChildren().addAll(colorBox, widthBox, marginsBox);
                checkMarginWidth();

            } else if (message("CutMarginsByWidth").equals(selected.getText())) {
                opType = OperationType.CutMarginsByWidth;
                setBox.getChildren().addAll(widthBox, marginsBox);
                checkMarginWidth();

            } else if (message("CutMarginsByColor").equals(selected.getText())) {
                opType = OperationType.CutMarginsByColor;
                setBox.getChildren().addAll(colorBox, distanceBox, marginsBox);
                marginWidthBox.getEditor().setStyle(null);
                checkColorDistance();

            } else if (message("Blur").equals(selected.getText())) {
                opType = OperationType.BlurMargins;
                setBox.getChildren().addAll(alphaBox, widthBox, marginsBox);
                checkMarginWidth();

            }
        }
        FxmlControl.refreshStyle(setBox);
    }

    private void initDragging() {
        imageController.operating();
        imageController.maskRectangleData = new DoubleRectangle(0, 0,
                imageView.getImage().getWidth() - 1,
                imageView.getImage().getHeight() - 1);
        imageController.scope.setRectangle(imageController.maskRectangleData.cloneValues());
        imageController.setMaskRectangleLineVisible(true);
        imageController.drawMaskRectangleLineAsData();
        imageController.imageLabel.setText(message("DragMarginsComments"));
    }

    private void checkMarginWidth() {
        try {
            int v = Integer.valueOf(marginWidthBox.getValue());
            if (v > 0) {
                addedWidth = v;
                AppVariables.setUserConfigInt("ImageMarginsWidth", v);
                FxmlControl.setEditorNormal(marginWidthBox);
            } else {
                FxmlControl.setEditorBadStyle(marginWidthBox);
            }
        } catch (Exception e) {
            FxmlControl.setEditorBadStyle(marginWidthBox);
        }
    }

    protected void checkColorDistance() {
        try {
            int v = Integer.valueOf(distanceInput.getText());
            if (distance >= 0 && distance <= 255) {
                distance = v;
                distanceInput.setStyle(null);
                AppVariables.setUserConfigInt("ImageMarginsColorDistance", v);
            } else {
                distanceInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            distanceInput.setStyle(badStyle);
        }
    }

    @Override
    public boolean setColor(Control control, Color color) {
        if (control == null || color == null) {
            return false;
        }
        if (paletteButton.equals(control)) {
            bgRect.setFill(color);
            FxmlControl.setTooltip(bgRect, FxmlColor.colorNameDisplay(color));
            AppVariables.setUserConfigValue("ImageShadowBackground", color.toString());
        }
        return true;
    }

    @FXML
    @Override
    public void showPalette(ActionEvent event) {
        showPalette(paletteButton, message("Margins"), true);
    }

    @Override
    public void paneClicked(MouseEvent event) {
        if (opType != OperationType.SetMarginsByDragging) {
            return;
        }
        String info = MessageFormat.format(AppVariables.message("ImageSizeChanged"),
                (int) Math.round(imageController.image.getWidth()) + "x" + (int) Math.round(imageController.image.getHeight()),
                (int) Math.round(imageView.getImage().getWidth()) + "x" + (int) Math.round(imageView.getImage().getHeight()),
                (int) Math.round(imageController.maskRectangleData.getWidth())
                + "x" + (int) Math.round(imageController.maskRectangleData.getHeight()));
        imageController.imageLabel.setText(info);

    }

    @FXML
    @Override
    public void okAction() {
        if (opType != OperationType.SetMarginsByDragging) {
            if (!marginsTopCheck.isSelected()
                    && !marginsBottomCheck.isSelected()
                    && !marginsLeftCheck.isSelected()
                    && !marginsRightCheck.isSelected()) {
                popError(AppVariables.message("NothingHandled"));
                return;
            }
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image newImage;
                private String value = null;

                @Override
                protected boolean handle() {

                    switch (opType) {
                        case SetMarginsByDragging:
                            newImage = FxmlImageManufacture.dragMarginsFx(imageView.getImage(),
                                    (Color) bgRect.getFill(), imageController.maskRectangleData);
                            break;
                        case CutMarginsByWidth:
                            newImage = FxmlImageManufacture.cutMarginsByWidth(imageView.getImage(), addedWidth,
                                    marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                                    marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());
                            value = addedWidth + "";
                            break;
                        case CutMarginsByColor:
                            newImage = FxmlImageManufacture.cutMarginsByColor(imageView.getImage(),
                                    (Color) bgRect.getFill(), distance,
                                    marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                                    marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());
                            value = distance + "";
                            break;
                        case AddMargins:
                            newImage = FxmlImageManufacture.addMarginsFx(imageView.getImage(),
                                    (Color) bgRect.getFill(), addedWidth,
                                    marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                                    marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());
                            value = addedWidth + "";
                            break;
                        case BlurMargins:
                            if (preAlphaCheck.isSelected()) {
                                newImage = FxmlImageManufacture.blurMarginsNoAlpha(imageView.getImage(), addedWidth,
                                        marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                                        marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());
                            } else {
                                newImage = FxmlImageManufacture.blurMarginsAlpha(imageView.getImage(), addedWidth,
                                        marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                                        marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());
                            }
                            value = addedWidth + "";
                            break;
                        default:
                            return false;
                    }
                    if (task == null || isCancelled()) {
                        return false;
                    }
                    return newImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    parent.updateImage(ImageOperation.Margins, opType.name(), value, newImage);
                    if (opType == OperationType.SetMarginsByDragging) {
//                        opGroup.selectToggle(null);
                        initDragging();
                    }

                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void setDragMode() {
        dragRadio.fire();
    }

}
