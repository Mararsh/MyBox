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
import mara.mybox.controller.base.ImagesListController;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.file.ImageGifFile;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.AppVaribles.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-11-16
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageGifEditerController extends ImagesListController {

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
        baseTitle = AppVaribles.message("ImageGifEditer");

        SourceFileType = VisitHistory.FileType.Gif;
        SourcePathType = VisitHistory.FileType.Gif;
        TargetFileType = VisitHistory.FileType.Gif;
        TargetPathType = VisitHistory.FileType.Gif;
        AddFileType = VisitHistory.FileType.Image;
        AddPathType = VisitHistory.FileType.Image;

        sourceExtensionFilter = CommonValues.GifExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initOptionsSection() {
        try {

            optionsBox.setDisable(true);
            tableBox.setDisable(true);

            interval = 500;
            List<String> values = Arrays.asList("500", "300", "1000", "2000", "3000", "5000", "10000");
            intervalCBox.getItems().addAll(values);
            intervalCBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            interval = v;
                            FxmlControl.setEditorNormal(intervalCBox);
                        } else {
                            FxmlControl.setEditorBadStyle(intervalCBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(intervalCBox);
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
                    Bindings.isEmpty(tableData)
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
        if (message("KeepImagesSize").equals(selected.getText())) {
            keepSize = true;
            widthInput.setStyle(null);
            heightInput.setStyle(null);
        } else if (message("AllSetAs").equals(selected.getText())) {
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
    public void saveFileDo(final File outFile) {
        try {
            task = new Task<Void>() {
                private String ret;

                @Override
                protected Void call() throws Exception {
                    ret = ImageGifFile.writeImages(tableData, outFile,
                            interval, loopCheck.isSelected(), keepSize, width, height);

                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (ret.isEmpty()) {
                                popInformation(AppVaribles.message("Successful"));
                                if (outFile.equals(sourceFile)) {
                                    setImageChanged(false);
                                } else if (viewCheck.isSelected()) {
                                    try {
                                        final ImageGifViewerController controller
                                                = (ImageGifViewerController) openStage(CommonValues.ImageGifViewerFxml);
                                        controller.loadImage(outFile.getAbsolutePath());
                                    } catch (Exception e) {
                                        logger.error(e.toString());
                                    }
                                }
                            } else {
                                popError(AppVaribles.message(ret));
                            }
                        }
                    });
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
