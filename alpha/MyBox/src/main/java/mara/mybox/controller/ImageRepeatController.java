package mara.mybox.controller;

import java.awt.image.BufferedImage;
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
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.ValidationTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-9-24
 * @License Apache License Version 2.0
 */
public class ImageRepeatController extends ImageViewerController {

    protected int canvasWidth, canvasHeight, repeatH, repeatV, interval, margin;

    @FXML
    protected ControlImageSize sizeController;
    @FXML
    protected ImageViewerController scaleController, repeatController;
    @FXML
    protected Tab imageTab, scaleTab, repeatTab, saveTab;
    @FXML
    protected ToggleGroup repeatGroup;
    @FXML
    protected RadioButton repeatRadio, tileRadio;
    @FXML
    protected TextField horizontalInput, veriticalInput;
    @FXML
    protected ComboBox<String> intervalSelector, marginSelector;
    @FXML
    protected ControlColorSet colorSetController;
    @FXML
    protected VBox mainBox, optionsBox;
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

            repeatController.formatController = formatController;

            sizeController.setParameters(scaleController);

            saveTab.disableProperty().bind(repeatController.imageView.imageProperty().isNull());
            repeatTab.disableProperty().bind(scaleController.imageView.imageProperty().isNull());

            checkRepeatType();
            repeatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    checkRepeatType();
                }
            });

            intervalSelector.getItems().addAll(Arrays.asList("0", "1", "2", "3", "5", "-1", "-3", "-5", "10", "15", "20", "30"));
            intervalSelector.getSelectionModel().select(UserConfig.getString(baseName + "Interval", "5"));

            marginSelector.getItems().addAll(Arrays.asList("5", "10", "15", "20", "1", "3", "30", "0"));
            marginSelector.getSelectionModel().select(UserConfig.getString(baseName + "Margins", "5"));

            colorSetController.init(this, baseName + "Color");

            loadNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    recoverSize();
                    repeatController.loadImage(null);
                    tabPane.getSelectionModel().select(scaleTab);
                }
            });

            rectDrawnNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    recoverSize();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public Image sourceImage() {
        return scaleController.scopeImage();
    }

    public void checkRepeatType() {
        if (repeatRadio.isSelected()) {
            repeatLabel.setText(message("RepeatNumber"));
            horizontalInput.setText(UserConfig.getInt(baseName + "RepeatHorizontal", 3) + "");
            veriticalInput.setText(UserConfig.getInt(baseName + "RepeatVertivcal", 3) + "");
        } else {
            repeatLabel.setText(message("CanvasSize"));
            Image srcImage = sourceImage();
            horizontalInput.setText(UserConfig.getInt(baseName + "CanvasHorizontal",
                    srcImage == null ? 500 : (int) srcImage.getWidth() * 3) + "");
            veriticalInput.setText(UserConfig.getInt(baseName + "CanvasVertical",
                    srcImage == null ? 500 : (int) srcImage.getHeight() * 3) + "");
        }
    }

    @FXML
    public void okSize() {
        sizeController.scale();
    }

    @FXML
    public void recoverSize() {
        scaleController.loadImage(scopeImage());
    }

    @FXML
    @Override
    public void okAction() {
        if (repeatRadio.isSelected()) {
            try {
                int v = Integer.parseInt(horizontalInput.getText());
                if (v > 0) {
                    repeatH = v;
                    horizontalInput.setStyle(null);
                    UserConfig.setInt(baseName + "RepeatHorizontal", repeatH);
                } else {
                    horizontalInput.setStyle(UserConfig.badStyle());
                    popError(message("InvalidParameter") + ": " + message("Horizontal"));
                    return;
                }
            } catch (Exception e) {
                horizontalInput.setStyle(UserConfig.badStyle());
                popError(message("InvalidParameter") + ": " + message("Horizontal"));
                return;
            }
            try {
                int v = Integer.parseInt(veriticalInput.getText());
                if (v > 0) {
                    repeatV = v;
                    veriticalInput.setStyle(null);
                    UserConfig.setInt(baseName + "RepeatVertical", repeatV);
                } else {
                    veriticalInput.setStyle(UserConfig.badStyle());
                    popError(message("InvalidParameter") + ": " + message("Vertical"));
                    return;
                }
            } catch (Exception e) {
                veriticalInput.setStyle(UserConfig.badStyle());
                popError(message("InvalidParameter") + ": " + message("Vertical"));
                return;
            }
        } else {
            try {
                int v = Integer.parseInt(horizontalInput.getText());
                if (v > 0) {
                    canvasWidth = v;
                    horizontalInput.setStyle(null);
                    UserConfig.setInt(baseName + "CanvasHorizontal", canvasWidth);
                } else {
                    horizontalInput.setStyle(UserConfig.badStyle());
                    popError(message("InvalidParameter") + ": " + message("Horizontal"));
                    return;
                }
            } catch (Exception e) {
                horizontalInput.setStyle(UserConfig.badStyle());
                popError(message("InvalidParameter") + ": " + message("Horizontal"));
                return;
            }
            try {
                int v = Integer.parseInt(veriticalInput.getText());
                if (v > 0) {
                    canvasHeight = v;
                    veriticalInput.setStyle(null);
                    UserConfig.setInt(baseName + "CanvasVertical", canvasHeight);
                } else {
                    veriticalInput.setStyle(UserConfig.badStyle());
                    popError(message("InvalidParameter") + ": " + message("Vertical"));
                    return;
                }
            } catch (Exception e) {
                veriticalInput.setStyle(UserConfig.badStyle());
                popError(message("InvalidParameter") + ": " + message("Vertical"));
                return;
            }
        }

        try {
            int v = Integer.parseInt(intervalSelector.getValue());
            interval = v;
            UserConfig.setString(baseName + "Interval", v + "");
            ValidationTools.setEditorNormal(intervalSelector);

        } catch (Exception e) {
            ValidationTools.setEditorBadStyle(intervalSelector);
            popError(message("InvalidParameter") + ": " + message("Interval"));
            return;
        }

        try {
            int v = Integer.parseInt(marginSelector.getValue());
            if (v >= 0) {
                margin = v;
                UserConfig.setString(baseName + "Margins", v + "");
                ValidationTools.setEditorNormal(marginSelector);
            } else {
                ValidationTools.setEditorBadStyle(marginSelector);
                popError(message("InvalidParameter") + ": " + message("Margins"));
                return;
            }
        } catch (Exception e) {
            ValidationTools.setEditorBadStyle(marginSelector);
            popError(message("InvalidParameter") + ": " + message("Margins"));
            return;
        }

        drawRepeat();
    }

    public void drawRepeat() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
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
                    bufferedImage = RepeatTools.repeat(bufferedImage,
                            repeatH, repeatV, interval, margin, colorSetController.awtColor());
                } else {
                    bufferedImage = RepeatTools.tile(bufferedImage,
                            canvasWidth, canvasHeight, interval, margin, colorSetController.awtColor());
                }
                if (bufferedImage == null) {
                    return false;
                }
                repeatImage = SwingFXUtils.toFXImage(bufferedImage, null);
                return repeatImage != null;
            }

            @Override
            protected void whenSucceeded() {
                repeatController.loadImage(repeatImage);
                tabPane.getSelectionModel().select(repeatTab);
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void saveAsAction() {
        repeatController.saveAsAction();
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (tabPane.getSelectionModel().getSelectedItem() == repeatTab) {
            if (repeatController.keyEventsFilter(event)) {
                return true;
            }
        }
        return super.keyEventsFilter(event);
    }

}
