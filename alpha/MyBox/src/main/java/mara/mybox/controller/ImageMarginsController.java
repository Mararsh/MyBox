package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.MarginTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-4
 * @License Apache License Version 2.0
 */
public class ImageMarginsController extends BaseImageEditController {

    protected int margin, distance;

    @FXML
    protected ToggleGroup opGroup;
    @FXML
    protected ComboBox<String> widthSelector;
    @FXML
    protected CheckBox marginsTopCheck, marginsBottomCheck, marginsLeftCheck, marginsRightCheck;
    @FXML
    protected FlowPane colorPane, distancePane, marginsPane, widthPane;
    @FXML
    protected TextField distanceInput;
    @FXML
    protected RadioButton dragRadio, addRadio, blurRadio, cutColorRadio, cutWidthRadio;
    @FXML
    protected VBox setBox;
    @FXML
    protected ControlColorSet colorSetController;
    @FXML
    protected Button selectAllRectButton;
    @FXML
    protected Label commentsLabel;

    public ImageMarginsController() {
        baseTitle = message("Margins");
    }

    @Override
    protected void initMore() {
        try {
            operation = message("Margins");

            opGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkOperationType();
                }
            });

            colorSetController.init(this, baseName + "Color");

            margin = UserConfig.getInt(baseName + "MarginsWidth", 20);
            if (margin <= 0) {
                margin = 20;
            }
            widthSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkMarginWidth();
                }
            });

            distanceInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkColorDistance();
                }
            });
            distanceInput.setText("20");
            distanceInput.setText(UserConfig.getInt("ImageMarginsColorDistance", 20) + "");

            okButton.disableProperty().bind(widthSelector.getEditor().styleProperty().isEqualTo(UserConfig.badStyle())
                    .or(distanceInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded()) {
                return false;
            }
            image = currentImage();
            if (image == null) {
                return false;
            }
            int width = (int) image.getWidth();
            int height = (int) image.getWidth();
            String info = message("CurrentSize") + ": " + width + "x" + height;
            commentsLabel.setText(info);

            List<String> ms = new ArrayList<>();
            ms.addAll(Arrays.asList(
                    width / 6 + "", width / 8 + "", width / 4 + "", width / 10 + "",
                    "20", "10", "5", "100", "200", "300", "50", "150", "500"));
            String m = margin + "";
            if (!ms.contains(m)) {
                ms.add(0, m);
            }
            isSettingValues = true;
            widthSelector.getItems().setAll(ms);
            widthSelector.setValue(m);
            isSettingValues = false;

            checkOperationType();

            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    protected void checkOperationType() {
        try {
            clearMask();
            setBox.getChildren().clear();
            ValidationTools.setEditorNormal(widthSelector);
            distanceInput.setStyle(null);
            commentsLabel.setText("");

            if (opGroup.getSelectedToggle() == null) {
                return;
            }

            if (dragRadio.isSelected()) {
                setBox.getChildren().addAll(colorPane, selectAllRectButton);
                initDragging();

            } else if (addRadio.isSelected()) {
                setBox.getChildren().addAll(colorPane, widthPane, marginsPane);
                checkMarginWidth();

            } else if (cutWidthRadio.isSelected()) {
                setBox.getChildren().addAll(widthPane, marginsPane);
                checkMarginWidth();

            } else if (cutColorRadio.isSelected()) {
                setBox.getChildren().addAll(colorPane, distancePane, marginsPane);
                widthSelector.getEditor().setStyle(null);
                checkColorDistance();

            } else if (blurRadio.isSelected()) {
                setBox.getChildren().addAll(widthPane, marginsPane);
                checkMarginWidth();

            }

            refreshStyle(setBox);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initDragging() {
        try {
            commentsLabel.setText(message("ImageDragMarginsComments"));
            maskRectangleData = DoubleRectangle.image(imageView.getImage());
            showMaskRectangle();
            popItemMenu = false;
            showAnchors = true;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    protected void checkMarginWidth() {
        try {
            if (isSettingValues) {
                return;
            }
            int v = Integer.parseInt(widthSelector.getValue());
            if (v > 0) {
                margin = v;
                UserConfig.setInt(baseName + "MarginsWidth", v);
                ValidationTools.setEditorNormal(widthSelector);
            } else {
                ValidationTools.setEditorBadStyle(widthSelector);
            }
        } catch (Exception e) {
            ValidationTools.setEditorBadStyle(widthSelector);
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
        if (!dragRadio.isSelected()
                || maskRectangleData == null || image == null) {
            return;
        }
        String info = message("CurrentSize") + ": " + (int) Math.round(image.getWidth())
                + "x" + (int) Math.round(image.getHeight()) + "  "
                + message("AfterChange") + ": " + (int) Math.round(maskRectangleData.getWidth())
                + "x" + (int) Math.round(maskRectangleData.getHeight());
        commentsLabel.setText(info);
    }

    @FXML
    public void selectAllRect() {
        if (!dragRadio.isSelected() || imageView.getImage() == null) {
            return;
        }
        maskRectangleData = DoubleRectangle.xywh(0, 0,
                imageView.getImage().getWidth(), imageView.getImage().getHeight());
        drawMaskRectangle();
    }

    @Override
    protected boolean checkOptions() {
        if (!super.checkOptions()) {
            return false;
        }
        if (!dragRadio.isSelected()) {
            if (!marginsTopCheck.isSelected()
                    && !marginsBottomCheck.isSelected()
                    && !marginsLeftCheck.isSelected()
                    && !marginsRightCheck.isSelected()) {
                popError(message("NothingHandled"));
                return false;
            }
        }
        return true;
    }

    @Override
    protected void handleImage() {
        if (dragRadio.isSelected()) {
            handledImage = MarginTools.dragMarginsFx(currentImage(),
                    (Color) colorSetController.rect.getFill(), maskRectangleData);

        } else if (addRadio.isSelected()) {
            handledImage = MarginTools.addMarginsFx(currentImage(),
                    (Color) colorSetController.rect.getFill(), margin,
                    marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                    marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());
            opInfo = margin + "";

        } else if (blurRadio.isSelected()) {
            handledImage = MarginTools.blurMarginsAlpha(currentImage(), margin,
                    marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                    marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());
            opInfo = margin + "";

        } else if (cutColorRadio.isSelected()) {
            handledImage = MarginTools.cutMarginsByColor(currentImage(),
                    (Color) colorSetController.rect.getFill(), distance,
                    marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                    marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());
            opInfo = distance + "";

        } else if (cutWidthRadio.isSelected()) {
            handledImage = MarginTools.cutMarginsByWidth(currentImage(), margin,
                    marginsTopCheck.isSelected(), marginsBottomCheck.isSelected(),
                    marginsLeftCheck.isSelected(), marginsRightCheck.isSelected());
            opInfo = margin + "";
        }
    }

    /*
        static methods
     */
    public static ImageMarginsController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageMarginsController controller = (ImageMarginsController) WindowTools.branch(
                    parent.getMyWindow(), Fxmls.ImageMarginsFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
