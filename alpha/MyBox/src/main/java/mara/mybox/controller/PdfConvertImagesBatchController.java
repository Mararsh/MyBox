package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Toggle;
import mara.mybox.bufferedimage.ImageAttributes;
import mara.mybox.bufferedimage.ImageConvertTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * @Author Mara
 * @CreateDate 2018-6-16
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfConvertImagesBatchController extends BaseBatchPdfController {

    protected ImageAttributes attributes;
    protected PDFRenderer renderer;

    @FXML
    protected ControlImageFormat formatController;
    @FXML
    protected CheckBox transparentBackgroundCheck,
            appendColorCheck, appendCompressionCheck, appendQualityCheck, appendDensityCheck;

    public PdfConvertImagesBatchController() {
        baseTitle = Languages.message("PdfConvertImagesBatch");
        browseTargets = true;

    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            formatController.setParameters(this, true);

            formatController.formatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue v, Toggle oldV, Toggle newV) {
                    transparentBackgroundCheck.setVisible(formatController.pngRadio.isSelected() || formatController.tifRadio.isSelected());
                }
            });
            transparentBackgroundCheck.setVisible(formatController.pngRadio.isSelected() || formatController.tifRadio.isSelected());

            transparentBackgroundCheck.setSelected(UserConfig.getBoolean(baseName + "TransparentBackground", false));
            transparentBackgroundCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "TransparentBackground", transparentBackgroundCheck.isSelected());
                }
            });

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(tableView.getItems())
                    .or(targetPathController.valid.not())
                    .or(formatController.qualitySelector.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(formatController.dpiSelector.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(formatController.profileInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(formatController.thresholdInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
                    UserConfig.setBoolean("PdfConverterAppendColor", appendColorCheck.isSelected());
                }
            });
            appendColorCheck.setSelected(UserConfig.getBoolean("PdfConverterAppendColor"));

            appendCompressionCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v,
                        Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean("PdfConverterAppendCompression", appendCompressionCheck.isSelected());
                }
            });
            appendCompressionCheck.setSelected(UserConfig.getBoolean("PdfConverterAppendCompression"));

            appendQualityCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v,
                        Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean("PdfConverterAppendQuality", appendQualityCheck.isSelected());
                }
            });
            appendQualityCheck.setSelected(UserConfig.getBoolean("PdfConverterAppendQuality"));

            appendDensityCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v,
                        Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean("PdfConverterAppendDensity", appendDensityCheck.isSelected());
                }
            });
            appendDensityCheck.setSelected(UserConfig.getBoolean("PdfConverterAppendDensity"));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public boolean makeActualParameters() {
        if (!super.makeActualParameters()) {
            return false;
        }

        attributes = formatController.attributes;

        return true;
    }

    @Override
    public boolean preHandlePages() {
        try {
            renderer = new PDFRenderer(doc);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            renderer = null;
        }
        return renderer != null;
    }

    @Override
    public int handleCurrentPage() {
        try {
            File tFile = makeTargetFile();
            ImageType imageType = ImageType.RGB;
            if (formatController.pngRadio.isSelected() || formatController.tifRadio.isSelected()) {
                if (transparentBackgroundCheck.isSelected()) {
                    imageType = ImageType.ARGB;
                }
            }
            BufferedImage pageImage = renderer.renderImageWithDPI(currentParameters.currentPage - 1, // 0-based
                    attributes.getDensity(), imageType);
            String targetFormat = attributes.getImageFormat();
            if ("ico".equals(targetFormat) || "icon".equals(targetFormat)) {
                if (ImageConvertTools.convertToIcon(pageImage, attributes, tFile)) {
                    targetFileGenerated(tFile);
                    return 1;
                } else {
                    return 0;
                }
            } else {
                BufferedImage targetImage = ImageConvertTools.convertColorSpace(pageImage, attributes);
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
            MyBoxLog.error(e.toString());
            return 0;
        }
    }

    public File makeTargetFile() {
        try {
            String namePrefix = FileNameTools.getFilePrefix(currentParameters.currentSourceFile.getName())
                    + "_page" + currentParameters.currentPage;
            if (!"ico".equals(attributes.getImageFormat())) {
                if (appendColorCheck.isSelected()) {
                    if (Languages.message("IccProfile").equals(attributes.getColorSpaceName())) {
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
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @Override
    public void postHandlePages() {
        renderer = null;
    }

}
