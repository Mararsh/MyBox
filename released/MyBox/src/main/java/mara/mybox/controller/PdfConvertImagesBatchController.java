package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.NodeStyleTools.badStyle;
import mara.mybox.bufferedimage.ImageAttributes;
import mara.mybox.bufferedimage.ImageConvertTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import static mara.mybox.value.UserConfig.setString;
import static mara.mybox.value.UserConfig.setString;
import static mara.mybox.value.UserConfig.setBoolean;

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
    protected CheckBox appendColorCheck, appendCompressionCheck, appendQualityCheck, appendDensityCheck;

    public PdfConvertImagesBatchController() {
        baseTitle = Languages.message("PdfConvertImagesBatch");
        browseTargets = true;

    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            formatController.setParameters(this, true);

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(tableView.getItems())
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                            .or(formatController.qualitySelector.getEditor().styleProperty().isEqualTo(NodeStyleTools.badStyle))
                            .or(formatController.dpiSelector.getEditor().styleProperty().isEqualTo(NodeStyleTools.badStyle))
                            .or(formatController.profileInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                            .or(formatController.thresholdInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
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
            BufferedImage pageImage = renderer.renderImageWithDPI(currentParameters.currentPage - 1,
                    attributes.getDensity(), ImageType.ARGB);                              // 0-based
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
