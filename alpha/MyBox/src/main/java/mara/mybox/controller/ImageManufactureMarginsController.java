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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mara.mybox.controller.ImageManufactureController_Image.ImageOperation;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.MarginTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

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
    protected CheckBox marginsTopCheck, marginsBottomCheck, marginsLeftCheck, marginsRightCheck;
    @FXML
    protected FlowPane colorBox, distanceBox, marginsBox;
    @FXML
    protected HBox widthBox;
    @FXML
    protected TextField distanceInput;
    @FXML
    protected RadioButton dragRadio, addRadio, blurRadio, cutColorRadio, cutWidthRadio;
    @FXML
    protected VBox setBox;
    @FXML
    protected ControlColorSet colorSetController;
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
            super.initPane();

            opGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkOperationType();
                }
            });

            colorSetController.init(this, baseName + "Color");

            marginWidthBox.getItems().clear();
            int width = (int) imageView.getImage().getWidth();
            marginWidthBox.getItems().addAll(Arrays.asList(
                    width / 6 + "", width / 8 + "", width / 4 + "", width / 10 + "",
                    "20", "10", "5", "100", "200", "300", "50", "150", "500"));
            marginWidthBox.getSelectionModel().select(UserConfig.getInt("ImageMarginsWidth", 20) + "");

            distanceInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkColorDistance();
                }
            });
            distanceInput.setText("20");
            distanceInput.setText(UserConfig.getInt("ImageMarginsColorDistance", 20) + "");

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
                    marginWidthBox.getEditor().styleProperty().isEqualTo(UserConfig.badStyle())
                            .or(distanceInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @Override
    protected void paneExpanded() {
        editor.showRightPane();
        checkOperationType();
    }

    private void checkOperationType() {
        editor.imageTab();
        editor.resetImagePane();
        setBox.getChildren().clear();
        ValidationTools.setEditorNormal(marginWidthBox);
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
            setBox.getChildren().addAll(widthBox, marginsBox);
            checkMarginWidth();

        }

        refreshStyle(setBox);
    }

    private void initDragging() {
        try {
            commentsLabel.setText(Languages.message("DragMarginsComments"));
            editor.maskRectangleData = new DoubleRectangle(0, 0,
                    imageView.getImage().getWidth() - 1,
                    imageView.getImage().getHeight() - 1);
            editor.showMaskRectangle();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    private void checkMarginWidth() {
        try {
            int v = Integer.parseInt(marginWidthBox.getValue());
            if (v > 0) {
                addedWidth = v;
                UserConfig.setInt("ImageMarginsWidth", v);
                ValidationTools.setEditorNormal(marginWidthBox);
            } else {
                ValidationTools.setEditorBadStyle(marginWidthBox);
            }
        } catch (Exception e) {
            ValidationTools.setEditorBadStyle(marginWidthBox);
        }
    }

    protected void checkColorDistance() {
        try {
            int v = Integer.parseInt(distanceInput.getText());
            if (distance >= 0 && distance <= 255) {
                distance = v;
                distanceInput.setStyle(null);
                UserConfig.setInt("ImageMarginsColorDistance", v);
            } else {
                distanceInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            distanceInput.setStyle(UserConfig.badStyle());
        }
    }

    @Override
    public void paneClicked(MouseEvent event, DoublePoint p) {
        if (opType != OperationType.SetMarginsByDragging
                || editor.maskRectangleData == null) {
            return;
        }
        String info = Languages.message("OriginalSize") + ": " + (int) Math.round(editor.image.getWidth())
                + "x" + (int) Math.round(editor.image.getHeight()) + "\n"
                + Languages.message("CurrentSize") + ": " + (int) Math.round(imageView.getImage().getWidth())
                + "x" + (int) Math.round(imageView.getImage().getHeight()) + "\n"
                + Languages.message("AfterChange") + ": " + (int) Math.round(editor.maskRectangleData.getWidth())
                + "x" + (int) Math.round(editor.maskRectangleData.getHeight());
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
                popError(Languages.message("NothingHandled"));
                return;
            }
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private Image newImage;
            private String value = null;

            @Override
            protected boolean handle() {
                switch (opType) {
                    case SetMarginsByDragging:
                        newImage = MarginTools.dragMarginsFx(imageView.getImage(),
                                (Color) colorSetController.rect.getFill(), editor.maskRectangleData);
                        break;
                    case CutMarginsByWidth:
                        newImage = MarginTools.cutMarginsByWidth(imageView.getImage(), addedWidth,
                                marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                                marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());
                        value = addedWidth + "";
                        break;
                    case CutMarginsByColor:
                        newImage = MarginTools.cutMarginsByColor(imageView.getImage(),
                                (Color) colorSetController.rect.getFill(), distance,
                                marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                                marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());
                        value = distance + "";
                        break;
                    case AddMargins:
                        newImage = MarginTools.addMarginsFx(imageView.getImage(),
                                (Color) colorSetController.rect.getFill(), addedWidth,
                                marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                                marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());
                        value = addedWidth + "";
                        break;
                    case BlurMargins:
                        newImage = MarginTools.blurMarginsAlpha(imageView.getImage(), addedWidth,
                                marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                                marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());
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
                editor.popSuccessful();
                editor.updateImage(ImageOperation.Margins, opType.name(), value, newImage, cost);
                String info = Languages.message("OriginalSize") + ": " + (int) Math.round(editor.image.getWidth())
                        + "x" + (int) Math.round(editor.image.getHeight()) + "\n"
                        + Languages.message("CurrentSize") + ": " + Math.round(newImage.getWidth())
                        + "x" + Math.round(newImage.getHeight());
                commentsLabel.setText(info);

                if (opType == OperationType.SetMarginsByDragging) {
                    initDragging();
                }

            }
        };
        start(task);
    }

    @Override
    protected void resetOperationPane() {
        checkOperationType();
    }

}
