package mara.mybox.controller;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Modality;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.image.file.ImageGifFile;
import static mara.mybox.value.AppVaribles.getMessage;

/**
 * @Author Mara
 * @CreateDate 2018-11-16
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageGifEditerController extends ImageSourcesController {

    protected int currentIndex, interval, width, height;
    private boolean keepSize;

    @FXML
    protected ComboBox<String> intervalCBox;
    @FXML
    protected ToggleGroup sizeGroup;
    @FXML
    protected TextField widthInput, heightInput;
    @FXML
    private CheckBox loopCheck;

    public ImageGifEditerController() {
        fileExtensionFilter = CommonValues.GifExtensionFilter;
    }

    @Override
    protected void initOptionsSection() {
        try {

            optionsBox.setDisable(true);
            sourcesBox.setDisable(true);

            interval = 500;
            List<String> values = Arrays.asList("500", "300", "1000", "2000", "3000", "5000", "10000");
            intervalCBox.getItems().addAll(values);
            intervalCBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            interval = v;
                            intervalCBox.getEditor().setStyle(null);
                            if (!isSettingValues && targetFile != null) {
                                setImageChanged(true);
                            }
                        } else {

                            intervalCBox.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        intervalCBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            intervalCBox.getSelectionModel().select(0);

            sizeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkSizeType();
                }
            });
            checkSizeType();

            widthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkSize();
                }
            });

            heightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkSize();
                }
            });

            saveButton.disableProperty().bind(
                    Bindings.isEmpty(sourceImages)
                            .or(widthInput.styleProperty().isEqualTo(badStyle))
                            .or(heightInput.styleProperty().isEqualTo(badStyle))
            );

            saveAsButton.disableProperty().bind(
                    saveButton.disableProperty()
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkSizeType() {
        RadioButton selected = (RadioButton) sizeGroup.getSelectedToggle();
        if (getMessage("KeepImagesSize").equals(selected.getText())) {
            keepSize = true;
            widthInput.setStyle(null);
            heightInput.setStyle(null);
        } else if (getMessage("AllSetAs").equals(selected.getText())) {
            keepSize = false;
            checkSize();
        }
    }

    private void checkSize() {
        try {
            int v = Integer.valueOf(widthInput.getText());
            if (v > 0) {
                width = v;
                widthInput.setStyle(null);
            } else {
                widthInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            widthInput.setStyle(badStyle);
        }

        try {
            int v = Integer.valueOf(heightInput.getText());
            if (v > 0) {
                height = v;
                heightInput.setStyle(null);
            } else {
                heightInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            heightInput.setStyle(badStyle);
        }

    }

    @Override
    protected void saveFileDo(final File outFile) {
        try {
            task = new Task<Void>() {
                private String ret;

                @Override
                protected Void call() throws Exception {
                    try {
                        ret = ImageGifFile.writeImages(sourceImages, outFile,
                                interval, loopCheck.isSelected(), keepSize, width, height);
                        if (task.isCancelled()) {
                            return null;
                        }
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                if (ret.isEmpty()) {
                                    popInformation(AppVaribles.getMessage("Successful"));
                                    if (viewCheck.isSelected()) {
                                        try {
                                            final ImageGifViewerController controller
                                                    = (ImageGifViewerController) openStage(CommonValues.ImageGifViewerFxml, false, true);
                                            controller.setBaseTitle(AppVaribles.getMessage("ImageGifViewer"));
                                            controller.loadImage(outFile.getAbsolutePath());
                                        } catch (Exception e) {
                                            logger.error(e.toString());
                                        }
                                    }
                                } else {
                                    popError(AppVaribles.getMessage(ret));
                                }
                            }
                        });
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                    return null;
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
