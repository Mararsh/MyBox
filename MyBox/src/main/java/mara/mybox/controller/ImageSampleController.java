package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.AppVaribles.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-12-03
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageSampleController extends ImageViewerController {

    private double scale;
    private int sampleWidth, sampleHeight;
    @FXML
    private ToolBar opBar;
    @FXML
    private HBox sampleBox;
    @FXML
    private VBox showBox;
    @FXML
    private CheckBox viewCheck;
    @FXML
    private ComboBox<String> widthBox, heightBox;
    @FXML
    private Label sampleLabel;

    public ImageSampleController() {
        baseTitle = AppVaribles.message("ImageSample");
        handleLoadedSize = false;

    }

    @Override
    public void initializeNext2() {
        try {
            scrollPane.setDisable(true);
            sampleBox.setDisable(true);
            showBox.setDisable(true);
            opBar.setDisable(true);

            List<String> values = Arrays.asList("1", "2", "3", "4", "5", "6", "8", "9", "10", "15", "20",
                    "25", "30", "50", "80", "100", "200", "500", "800", "1000");
            widthBox.getItems().addAll(values);
            widthBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkSample();
                }
            });
            widthBox.getSelectionModel().select("1");

            heightBox.getItems().addAll(values);
            heightBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkSample();
                }
            });
            heightBox.getSelectionModel().select("1");

            initMaskControls(false);

            saveButton.disableProperty().bind(
                    widthBox.getEditor().styleProperty().isEqualTo(badStyle)
                            .or(heightBox.getEditor().styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(widthBox.getEditor().textProperty()))
                            .or(Bindings.isEmpty(heightBox.getEditor().textProperty()))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkSample() {
        if (image == null) {
            return;
        }
        try {
            int v = Integer.valueOf(widthBox.getSelectionModel().getSelectedItem());
            if (v > 0) {
                sampleWidth = v;
                FxmlControl.setEditorNormal(widthBox);
            } else {
                FxmlControl.setEditorBadStyle(widthBox);
            }
        } catch (Exception e) {
            FxmlControl.setEditorBadStyle(widthBox);

        }

        try {
            int v = Integer.valueOf(heightBox.getSelectionModel().getSelectedItem());
            if (v > 0) {
                sampleHeight = v;
                FxmlControl.setEditorNormal(heightBox);
            } else {
                FxmlControl.setEditorBadStyle(heightBox);
            }
        } catch (Exception e) {
            FxmlControl.setEditorBadStyle(heightBox);
        }

        updateLabel();

    }

    private void updateLabel() {
        if (sampleWidth < 1 || sampleHeight < 1) {
            sampleLabel.setText(message("InvalidParameters"));
        } else {
            sampleLabel.setText(message("ImageSize") + ": "
                    + imageInformation.getWidth() + "x" + imageInformation.getHeight()
                    + "  " + message("SamplingSize") + ": "
                    + (int) Math.round(maskRectangleData.getWidth() / sampleWidth)
                    + "x" + (int) Math.round(maskRectangleData.getHeight() / sampleHeight));
        }
    }

    @Override
    public boolean drawMaskRectangleLine() {
        if (super.drawMaskRectangleLine()) {
            updateLabel();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void afterImageLoaded() {
        try {
            super.afterImageLoaded();
            if (image == null || imageInformation == null) {
                return;
            }

            scrollPane.setDisable(false);
            sampleBox.setDisable(false);
            showBox.setDisable(false);
            opBar.setDisable(false);

            if (imageInformation.isIsSampled()) {
                scale = imageInformation.getWidth() / image.getWidth();
            } else {
                scale = 1;
            }

            fitSize();
            initMaskRectangleLine(true);
            checkSample();

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    @Override
    protected void loadSampledImage() {

        if (sampledTips != null) {
            final String msg = getSmapledInfo() + "\n\n" + AppVaribles.message("ImagePartComments");
            sampledTips.setOnMouseMoved(null);
            sampledTips.setOnMouseMoved(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    popSampleInformation(msg);
                }
            });
        }

    }

    @FXML
    @Override
    public void saveAction() {
        if (image == null || sampleWidth < 1 || sampleHeight < 1) {
            return;
        }
        final File file = chooseSaveFile(AppVaribles.getUserConfigPath(targetPathKey),
                null, targetExtensionFilter, true);
        if (file == null) {
            return;
        }
        AppVaribles.setUserConfigValue(targetPathKey, file.getParent());

        task = new Task<Void>() {
            private boolean ok;
            private String filename;

            @Override
            protected Void call() throws Exception {
                filename = file.getAbsolutePath();
                String format = FileTools.getFileSuffix(filename);
                BufferedImage bufferedImage;
                bufferedImage = ImageFileReaders.readFileBySample(imageInformation.getImageFormat(),
                        sourceFile.getAbsolutePath(),
                        maskRectangleData, sampleWidth, sampleHeight);
                if (task == null || task.isCancelled()) {
                    return null;
                }
                ok = ImageFileWriters.writeImageFile(bufferedImage, format, filename);

                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (ok && file.exists()) {
                            popInformation(AppVaribles.message("Successful"));
                            if (viewCheck.isSelected()) {
                                try {
                                    final ImageViewerController controller
                                            = (ImageViewerController) openStage(CommonValues.ImageViewerFxml);
                                    controller.loadImage(filename);
                                } catch (Exception e) {
                                    logger.error(e.toString());
                                }
                            }
                        } else {
                            popError(AppVaribles.message("Failed"));
                        }
                    }
                });
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}
