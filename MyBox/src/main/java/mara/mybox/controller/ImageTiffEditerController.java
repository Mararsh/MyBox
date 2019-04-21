package mara.mybox.controller;

import mara.mybox.controller.base.ImageSourcesController;
import java.io.File;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageValue;
import mara.mybox.image.file.ImageTiffFile;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.data.ImageAttributes;
import mara.mybox.data.VisitHistory;
import static mara.mybox.value.AppVaribles.getMessage;
import org.apache.pdfbox.rendering.ImageType;

/**
 * @Author Mara
 * @CreateDate 2018-11-16
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageTiffEditerController extends ImageSourcesController {

    @FXML
    private HBox compressionBox, binaryBox;
    @FXML
    protected ToggleGroup colorGroup, compressionGroup, binaryGroup;
    @FXML
    private TextField thresholdInput;

    public ImageTiffEditerController() {
        baseTitle = AppVaribles.getMessage("ImageTiffEditer");

        SourceFileType = VisitHistory.FileType.Tif;
        SourcePathType = VisitHistory.FileType.Tif;
        TargetFileType = VisitHistory.FileType.Tif;
        TargetPathType = VisitHistory.FileType.Tif;
        AddFileType = VisitHistory.FileType.Image;
        AddPathType = VisitHistory.FileType.Image;

        fileExtensionFilter = CommonValues.TiffExtensionFilter;
    }

    @Override
    public void initOptionsSection() {
        try {
            attributes = new ImageAttributes();
            attributes.setImageFormat("tif");
            optionsBox.setDisable(true);
            sourcesBox.setDisable(true);

            colorGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkColorType();
                }
            });
            checkColorType();

            compressionGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkCompressionType();
                }
            });

            binaryGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkBinary();
                }
            });

            thresholdInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkThreshold();
                }
            });

            saveButton.disableProperty().bind(
                    Bindings.isEmpty(sourceImages)
                            .or(thresholdInput.styleProperty().isEqualTo(badStyle))
            );

            saveAsButton.disableProperty().bind(
                    saveButton.disableProperty()
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkColorType() {
        try {
            binaryBox.setDisable(true);
            thresholdInput.setStyle(null);
            RadioButton selected = (RadioButton) colorGroup.getSelectedToggle();
            String s = selected.getText();
            if (getMessage("Colorful").equals(s)) {
                attributes.setColorSpace(ImageType.RGB);
            } else if (getMessage("ColorAlpha").equals(s)) {
                attributes.setColorSpace(ImageType.ARGB);
            } else if (getMessage("ShadesOfGray").equals(s)) {
                attributes.setColorSpace(ImageType.GRAY);
            } else if (getMessage("BlackOrWhite").equals(s)) {
                attributes.setColorSpace(ImageType.BINARY);
                checkBinary();
            } else {
                attributes.setColorSpace(ImageType.RGB);
            }
            setCompressionTypes();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void setCompressionTypes() {
        try {
            compressionBox.getChildren().clear();
            compressionGroup = new ToggleGroup();
            String[] compressionTypes
                    = ImageValue.getCompressionTypes("tif", attributes.getColorSpace());
            for (String ctype : compressionTypes) {
                if (ctype.equals("ZLib")) { // This type looks not work for mutiple frames tiff file
                    continue;
                }
                RadioButton newv = new RadioButton(ctype);
                newv.setToggleGroup(compressionGroup);
                compressionBox.getChildren().add(newv);
            }

            compressionGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkCompressionType();
                }
            });
            compressionGroup.selectToggle((RadioButton) compressionBox.getChildren().get(0));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkCompressionType() {
        try {
            RadioButton selected = (RadioButton) compressionGroup.getSelectedToggle();
            attributes.setCompressionType(selected.getText());
        } catch (Exception e) {
            attributes.setCompressionType(null);
        }
    }

    protected void checkBinary() {
        try {
            binaryBox.setDisable(false);
            thresholdInput.setStyle(null);
            RadioButton selected = (RadioButton) binaryGroup.getSelectedToggle();
            String s = selected.getText();
            if (getMessage("Threshold").equals(s)) {
                attributes.setBinaryConversion(ImageAttributes.BinaryConversion.BINARY_THRESHOLD);
                checkThreshold();
            } else if (getMessage("OTSU").equals(s)) {
                attributes.setBinaryConversion(ImageAttributes.BinaryConversion.BINARY_OTSU);
            } else {
                attributes.setBinaryConversion(ImageAttributes.BinaryConversion.DEFAULT);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkThreshold() {
        try {
            if (attributes.getBinaryConversion() != ImageAttributes.BinaryConversion.BINARY_THRESHOLD) {
                thresholdInput.setStyle(null);
                return;
            }
            int inputValue = Integer.parseInt(thresholdInput.getText());
            if (inputValue >= 0 && inputValue <= 255) {
                attributes.setThreshold(inputValue);
                thresholdInput.setStyle(null);
            } else {
                thresholdInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            thresholdInput.setStyle(badStyle);
        }
    }

    @Override
    public void saveFileDo(final File outFile) {
        try {
            task = new Task<Void>() {
                private String ret;

                @Override
                protected Void call() throws Exception {

                    ret = ImageTiffFile.writeTiffImagesWithInfo(sourceImages, attributes, outFile);

                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (ret.isEmpty()) {
                                popInformation(AppVaribles.getMessage("Successful"));
                                if (viewCheck.isSelected()) {
                                    try {
                                        final ImageFramesViewerController controller
                                                = (ImageFramesViewerController) openStage(CommonValues.ImageFramesViewerFxml);
                                        controller.selectSourceFile(outFile);
                                    } catch (Exception e) {
                                        logger.error(e.toString());
                                    }
                                }
                                setImageChanged(false);
                            } else {
                                popError(AppVaribles.getMessage(ret));
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
