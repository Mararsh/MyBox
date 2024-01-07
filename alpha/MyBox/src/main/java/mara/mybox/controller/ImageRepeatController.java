package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import mara.mybox.bufferedimage.RepeatTools;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.ValidationTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-9-24
 * @License Apache License Version 2.0
 */
public class ImageRepeatController extends BaseController {

    protected int hValue, vValue, interval, margins;

    @FXML
    protected BaseImageController sourceController;
    @FXML
    protected ControlImageView targetController;
    @FXML
    protected Tab sourceTab, repeatTab;
    @FXML
    protected VBox sourceBox, targetBox;
    @FXML
    protected ToggleGroup repeatGroup;
    @FXML
    protected RadioButton repeatRadio, tileRadio;
    @FXML
    protected TextField horizontalInput, veriticalInput;
    @FXML
    protected ComboBox<String> intervalSelector, marginSelector;
    @FXML
    protected ControlColorSet colorController;
    @FXML
    protected Label repeatLabel;

    public ImageRepeatController() {
        baseTitle = message("ImageRepeatTile");
        TipsLabelKey = "ImageRepeatTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            repeatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    checkRepeatType();
                }
            });

            interval = UserConfig.getInt(baseName + "Interval", 0);
            intervalSelector.getItems().addAll(
                    Arrays.asList("0", "5", "-5", "1", "-1", "10", "-10", "15", "-15", "20", "-20", "30", "-30"));
            intervalSelector.setValue(interval + "");

            margins = UserConfig.getInt(baseName + "Margins", 0);
            marginSelector.getItems().addAll(Arrays.asList("0", "5", "-5", "10", "-10", "20", "-20", "30", "-30"));
            marginSelector.setValue(margins + "");

            colorController.init(this, baseName + "Color");
            checkRepeatType();

            goButton.disableProperty().bind(sourceController.imageView.imageProperty().isNull());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public Image sourceImage() {
        return sourceController.imageView.getImage();
    }

    public void checkRepeatType() {
        if (repeatRadio.isSelected()) {
            repeatLabel.setText(message("RepeatNumber"));

            hValue = UserConfig.getInt(baseName + "RepeatHorizontal", 3);
            if (hValue <= 0) {
                hValue = 3;
            }
            vValue = UserConfig.getInt(baseName + "RepeatVertivcal", 3);
            if (vValue <= 0) {
                vValue = 3;
            }

        } else {
            repeatLabel.setText(message("CanvasSize"));
            Image srcImage = sourceImage();
            if (srcImage != null) {
                hValue = (int) srcImage.getWidth() * 3;
                vValue = (int) srcImage.getHeight() * 3;
            } else {
                hValue = UserConfig.getInt(baseName + "CanvasHorizontal", 500);
                if (hValue <= 0) {
                    hValue = 500;
                }
                vValue = UserConfig.getInt(baseName + "CanvasVertical", 500);
                if (vValue <= 0) {
                    vValue = 500;
                }
            }

        }
        horizontalInput.setText(hValue + "");
        veriticalInput.setText(vValue + "");
    }

    public boolean checkHorizontalInput() {
        int v;
        try {
            v = Integer.parseInt(horizontalInput.getText());
        } catch (Exception e) {
            v = -1;
        }
        if (v > 0) {
            hValue = v;
            horizontalInput.setStyle(null);
            return true;
        } else {
            horizontalInput.setStyle(UserConfig.badStyle());
            popError(message("InvalidParameter") + ": " + message("Horizontal"));
            return false;
        }
    }

    public boolean checkVeriticalInput() {
        int v;
        try {
            v = Integer.parseInt(veriticalInput.getText());
        } catch (Exception e) {
            v = -1;
        }
        if (v > 0) {
            vValue = v;
            veriticalInput.setStyle(null);
            return true;
        } else {
            veriticalInput.setStyle(UserConfig.badStyle());
            popError(message("InvalidParameter") + ": " + message("Vertical"));
            return false;
        }
    }

    public boolean checkInterval() {
        try {
            interval = Integer.parseInt(intervalSelector.getValue());
            ValidationTools.setEditorNormal(intervalSelector);
            return true;
        } catch (Exception e) {
            ValidationTools.setEditorBadStyle(intervalSelector);
            popError(message("InvalidParameter") + ": " + message("Interval"));
            return false;
        }
    }

    public boolean checkMargins() {
        try {
            margins = Integer.parseInt(marginSelector.getValue());
            ValidationTools.setEditorNormal(marginSelector);
            return true;
        } catch (Exception e) {
            ValidationTools.setEditorBadStyle(marginSelector);
            popError(message("InvalidParameter") + ": " + message("Margins"));
            return false;
        }
    }

    public boolean checkOptionss() {
        if (!checkHorizontalInput() || !checkVeriticalInput()
                || !checkInterval() || !checkMargins()) {
            return false;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            if (repeatRadio.isSelected()) {
                UserConfig.setInt(conn, baseName + "RepeatHorizontal", hValue);
                UserConfig.setInt(conn, baseName + "RepeatVertivcal", vValue);
            } else {
                UserConfig.setInt(conn, baseName + "CanvasHorizontal", hValue);
                UserConfig.setInt(conn, baseName + "CanvasVertical", vValue);
            }
            UserConfig.setInt(conn, baseName + "Interval", interval);
            UserConfig.setInt(conn, baseName + "Margins", margins);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
        return true;
    }

    @FXML
    @Override
    public void goAction() {
        if (!checkOptionss()) {
            return;
        }
        drawRepeat();
    }

    public void drawRepeat() {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            Image repeatImage;

            @Override
            protected boolean handle() {
                Image srcImage = sourceImage();
                if (srcImage == null) {
                    error = message("NoData") + ": " + message("Image");
                    return false;
                }
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(srcImage, null);
                if (repeatRadio.isSelected()) {
                    bufferedImage = RepeatTools.repeat(this, bufferedImage,
                            hValue, vValue, interval, margins, colorController.awtColor());
                } else {
                    bufferedImage = RepeatTools.tile(this, bufferedImage,
                            hValue, vValue, interval, margins, colorController.awtColor());
                }
                if (bufferedImage == null) {
                    return false;
                }
                repeatImage = SwingFXUtils.toFXImage(bufferedImage, null);
                return repeatImage != null;
            }

            @Override
            protected void whenSucceeded() {
                targetController.loadImage(repeatImage);
            }

        };
        start(task);
    }

    @FXML
    @Override
    public boolean menuAction() {
        if (targetBox.isFocused() || targetBox.isFocusWithin()) {
            targetController.menuAction();
            return true;
        } else if (sourceBox.isFocused() || sourceBox.isFocusWithin()) {
            sourceController.menuAction();
            return true;
        }
        return super.menuAction();
    }

    @FXML
    @Override
    public boolean popAction() {
        if (targetBox.isFocused() || targetBox.isFocusWithin()) {
            targetController.popAction();
            return true;
        } else if (sourceBox.isFocused() || sourceBox.isFocusWithin()) {
            sourceController.popAction();
            return true;
        }
        return super.popAction();
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (targetBox.isFocused() || targetBox.isFocusWithin()) {
            if (targetController.keyEventsFilter(event)) {
                return true;
            }
        } else if (sourceBox.isFocused() || sourceBox.isFocusWithin()) {
            if (sourceController.keyEventsFilter(event)) {
                return true;
            }
        }
        return super.keyEventsFilter(event);
    }

    @FXML
    @Override
    public void saveAction() {
        targetController.saveAsAction();
    }

}
