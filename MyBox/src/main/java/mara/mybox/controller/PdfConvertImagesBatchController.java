package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageAttributes;
import mara.mybox.image.ImageConvert;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import static mara.mybox.value.AppVariables.setUserConfigValue;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * @Author Mara
 * @CreateDate 2018-6-16
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfConvertImagesBatchController extends PdfBatchController {

    protected ImageAttributes attributes;
    protected PDFRenderer renderer;

    @FXML
    protected ImageConverterOptionsController optionsController;
    @FXML
    protected CheckBox appendColorCheck, appendCompressionCheck, appendQualityCheck, appendDensityCheck;

    public PdfConvertImagesBatchController() {
        baseTitle = AppVariables.message("PdfConvertImagesBatch");
        browseTargets = true;

    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            optionsController.initDpiBox(true);

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    Bindings.isEmpty(tableView.getItems())
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(optionsController.qualitySelector.getEditor().styleProperty().isEqualTo(badStyle))
                            .or(optionsController.dpiSelector.getEditor().styleProperty().isEqualTo(badStyle))
                            .or(optionsController.profileInput.styleProperty().isEqualTo(badStyle))
                            .or(optionsController.thresholdInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void initTargetSection() {
        try {
            super.initTargetSection();

            appendColorCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v,
                        Boolean oldV, Boolean newV) {
                    setUserConfigValue("PdfConverterAppendColor", appendColorCheck.isSelected());
                }
            });
            appendColorCheck.setSelected(AppVariables.getUserConfigBoolean("PdfConverterAppendColor"));

            appendCompressionCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v,
                        Boolean oldV, Boolean newV) {
                    setUserConfigValue("PdfConverterAppendCompression", appendCompressionCheck.isSelected());
                }
            });
            appendCompressionCheck.setSelected(AppVariables.getUserConfigBoolean("PdfConverterAppendCompression"));

            appendQualityCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v,
                        Boolean oldV, Boolean newV) {
                    setUserConfigValue("PdfConverterAppendQuality", appendQualityCheck.isSelected());
                }
            });
            appendQualityCheck.setSelected(AppVariables.getUserConfigBoolean("PdfConverterAppendQuality"));

            appendDensityCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v,
                        Boolean oldV, Boolean newV) {
                    setUserConfigValue("PdfConverterAppendDensity", appendDensityCheck.isSelected());
                }
            });
            appendDensityCheck.setSelected(AppVariables.getUserConfigBoolean("PdfConverterAppendDensity"));

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public boolean makeActualParameters() {
        if (!super.makeActualParameters()) {
            return false;
        }

        attributes = optionsController.attributes;

        return true;
    }

    @Override
    public boolean preHandlePages() {
        try {
            renderer = new PDFRenderer(doc);
        } catch (Exception e) {
            logger.error(e.toString());
            renderer = null;
        }
        return renderer != null;
    }

    @Override
    public int handleCurrentPage() {
        try {
            File tFile = makeTargetFile();
            BufferedImage pageImage = renderer.renderImageWithDPI(currentParameters.currentPage - 1,
                    attributes.getDensity(), ImageType.ARGB);                              // 0-based
            String targetFormat = attributes.getImageFormat();
            if ("ico".equals(targetFormat) || "icon".equals(targetFormat)) {
                if (ImageConvert.convertToIcon(pageImage, attributes, tFile)) {
                    targetFileGenerated(tFile);
                    return 1;
                } else {
                    return 0;
                }
            } else {
                BufferedImage targetImage = ImageConvert.convertColorSpace(pageImage, attributes);
                if (targetImage == null) {
                    return 0;
                }

                if (!ImageFileWriters.writeImageFile(targetImage, attributes, tFile.getAbsolutePath())) {
                    return 0;
                }
                targetFileGenerated(tFile);
                return 1;
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return 0;
        }
    }

    public File makeTargetFile() {
        try {
            String namePrefix = FileTools.getFilePrefix(currentParameters.currentSourceFile.getName())
                    + "_page" + currentParameters.currentPage;
            if (!"ico".equals(attributes.getImageFormat())) {
                if (appendColorCheck.isSelected()) {
                    if (message("IccProfile").equals(attributes.getColorSpaceName())) {
                        namePrefix += "_" + attributes.getProfileName();
                    } else {
                        namePrefix += "_" + attributes.getColorSpaceName();
                    }
                }
                if (attributes.getCompressionType() != null) {
                    if (appendCompressionCheck.isSelected()) {
                        namePrefix += "_" + attributes.getCompressionType();
                    }
                    if (appendQualityCheck.isSelected()) {
                        namePrefix += "_quality-" + attributes.getQuality() + "%";
                    }
                }
                if (appendDensityCheck.isSelected()) {
                    namePrefix += "_" + attributes.getDensity();
                }
            }
            namePrefix = namePrefix.replace(" ", "_");
            String nameSuffix = "." + attributes.getImageFormat();

            return makeTargetFile(namePrefix, nameSuffix, currentParameters.currentTargetPath);
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    @Override
    public void postHandlePages() {
        renderer = null;
    }

}
