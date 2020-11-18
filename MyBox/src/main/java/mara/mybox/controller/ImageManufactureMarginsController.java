package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
import javafx.stage.Modality;
import mara.mybox.controller.ImageManufactureController.ImageOperation;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleRectangle;
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
    protected RadioButton dragRadio, addRadio, blurRadio, cutColorRadio, cutWidthRadio;
    @FXML
    protected ImageView preAlphaTipsView;
    @FXML
    protected VBox setBox;
    @FXML
    protected ColorSetController colorSetController;
    @FXML
    protected Label commentsLabel;

    public enum OperationType {
        SetMarginsByDragging,
        CutMarginsByColor,
        CutMarginsByWidth,
        AddMargins,
        BlurMargins
    }

    @Override
    public void initPane() {
        try {
            opGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkOperationType();
                }
            });

            if (imageController.imageInformation != null
                    && CommonValues.NoAlphaImages.contains(imageController.imageInformation.getImageFormat())) {
                preAlphaCheck.setSelected(true);
                preAlphaCheck.setDisable(true);
            } else {
                preAlphaCheck.setSelected(false);
                preAlphaCheck.setDisable(false);
            }

            colorSetController.init(this, baseName + "Color");

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

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    protected void paneExpanded() {
        checkOperationType();
    }

    private void checkOperationType() {
        imageController.resetImagePane();
        imageController.showImagePane();
        imageController.hideScopePane();
        setBox.getChildren().clear();
        FxmlControl.setEditorNormal(marginWidthBox);
        distanceInput.setStyle(null);
        commentsLabel.setText("");

        if (opGroup.getSelectedToggle() == null) {
            return;
        }

        if (dragRadio.isSelected()) {
            opType = OperationType.SetMarginsByDragging;
            setBox.getChildren().addAll(colorBox);
            initDragging();

        } else if (addRadio.isSelected()) {
            opType = OperationType.AddMargins;
            setBox.getChildren().addAll(colorBox, widthBox, marginsBox);
            checkMarginWidth();

        } else if (cutWidthRadio.isSelected()) {
            opType = OperationType.CutMarginsByWidth;
            setBox.getChildren().addAll(widthBox, marginsBox);
            checkMarginWidth();

        } else if (cutColorRadio.isSelected()) {
            opType = OperationType.CutMarginsByColor;
            setBox.getChildren().addAll(colorBox, distanceBox, marginsBox);
            marginWidthBox.getEditor().setStyle(null);
            checkColorDistance();

        } else if (blurRadio.isSelected()) {
            opType = OperationType.BlurMargins;
            setBox.getChildren().addAll(alphaBox, widthBox, marginsBox);
            checkMarginWidth();

        }

        FxmlControl.refreshStyle(setBox);
    }

    private void initDragging() {
        try {
            imageController.setMaskRectangleLineVisible(true);
            imageController.maskRectangleData = new DoubleRectangle(0, 0,
                    imageView.getImage().getWidth(),
                    imageView.getImage().getHeight());
            imageController.drawMaskRectangleLineAsData();
            commentsLabel.setText(message("DragMarginsComments"));
        } catch (Exception e) {
            logger.debug(e.toString());
        }

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
    public void imageClicked(MouseEvent event, DoublePoint p) {
        if (opType != OperationType.SetMarginsByDragging
                || imageController.maskRectangleData == null) {
            return;
        }
        String info = message("OriginalSize") + ": " + (int) Math.round(imageController.image.getWidth())
                + "x" + (int) Math.round(imageController.image.getHeight()) + "\n"
                + message("CurrentSize") + ": " + (int) Math.round(imageView.getImage().getWidth())
                + "x" + (int) Math.round(imageView.getImage().getHeight()) + "\n"
                + message("AfterChange") + ": " + (int) Math.round(imageController.maskRectangleData.getWidth())
                + "x" + (int) Math.round(imageController.maskRectangleData.getHeight());
        commentsLabel.setText(info);

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
            if (task != null && !task.isQuit()) {
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
                                    (Color) colorSetController.rect.getFill(), imageController.maskRectangleData);
                            break;
                        case CutMarginsByWidth:
                            newImage = FxmlImageManufacture.cutMarginsByWidth(imageView.getImage(), addedWidth,
                                    marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                                    marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());
                            value = addedWidth + "";
                            break;
                        case CutMarginsByColor:
                            newImage = FxmlImageManufacture.cutMarginsByColor(imageView.getImage(),
                                    (Color) colorSetController.rect.getFill(), distance,
                                    marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                                    marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());
                            value = distance + "";
                            break;
                        case AddMargins:
                            newImage = FxmlImageManufacture.addMarginsFx(imageView.getImage(),
                                    (Color) colorSetController.rect.getFill(), addedWidth,
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
                    imageController.popSuccessful();
                    imageController.updateImage(ImageOperation.Margins, opType.name(), value, newImage, cost);
                    String info = message("OriginalSize") + ": " + (int) Math.round(imageController.image.getWidth())
                            + "x" + (int) Math.round(imageController.image.getHeight()) + "\n"
                            + message("CurrentSize") + ": " + Math.round(newImage.getWidth())
                            + "x" + Math.round(newImage.getHeight());
                    commentsLabel.setText(info);

                    if (opType == OperationType.SetMarginsByDragging) {
                        initDragging();
                    }

                }
            };
            imageController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    protected void resetOperationPane() {
        checkOperationType();
    }

}
