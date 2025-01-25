package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.image.data.ImageAttributes;
import mara.mybox.image.tools.ImageConvertTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.PdfTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * @Author Mara
 * @CreateDate 2020-11-1
 * @License Apache License Version 2.0
 */
public class PdfImagesConvertBatchController extends BaseBatchPdfController {

    protected String format;
    protected PDDocument targetDoc;
    protected File tmpFile;
    protected ImageAttributes attributes;

    @FXML
    protected CheckBox copyAllCheck;
    @FXML
    protected ControlImageFormat formatController;

    public PdfImagesConvertBatchController() {
        baseTitle = Languages.message("PdfImagesConvertBatch");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            copyAllCheck.setSelected(UserConfig.getBoolean(baseName + "CopyAll", false));
            copyAllCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "CopyAll", false);
                }
            });

            if (formatController != null) {
                formatController.setParameters(this, true);

                startButton.disableProperty().unbind();
                startButton.disableProperty().bind(Bindings.isEmpty(tableView.getItems())
                        .or(formatController.qualitySelector.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()))
                        .or(formatController.dpiSelector.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()))
                        .or(formatController.profileInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                        .or(formatController.binaryController.thresholdInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                );
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean makeActualParameters() {
        if (!super.makeActualParameters()) {
            return false;
        }
        if (formatController != null) {
            attributes = formatController.attributes;
            format = attributes.getImageFormat();
        }
        return true;
    }

    @Override
    public boolean preHandlePages(FxTask currentTask) {
        try {
            File tFile = makeTargetFile(FileNameTools.prefix(currentParameters.currentSourceFile.getName()),
                    ".pdf", currentParameters.currentTargetPath);
            currentTargetFile = tFile.getAbsolutePath();
            tmpFile = FileTmpTools.getTempFile();
            if (copyAllCheck.isSelected()) {
//                doc.save(tFile);
                targetDoc = doc;
            } else {
                targetDoc = PdfTools.createPDF(tmpFile, "MyBox v" + AppValues.AppVersion);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
            targetDoc = null;
        }
        return targetDoc != null;
    }

    @Override
    public int handleCurrentPage(FxTask currentTask) {
        int count = 0;
        try {
            PDPage sourcePage = doc.getPage(currentParameters.currentPage - 1);  // 0-based
            PDResources pdResources = sourcePage.getResources();
            pdResources.getXObjectNames();
            Iterable<COSName> iterable = pdResources.getXObjectNames();
            if (iterable == null) {
                return 0;
            }
            Iterator<COSName> pageIterator = iterable.iterator();

            while (pageIterator.hasNext()) {
                if (currentTask == null || currentTask.isCancelled()) {
                    break;
                }
                COSName cosName = pageIterator.next();
                if (!pdResources.isImageXObject(cosName)) {
                    continue;
                }
                PDImageXObject pdxObject = (PDImageXObject) pdResources.getXObject(cosName);
                BufferedImage sourceImage = pdxObject.getImage();
                PDImageXObject newObject = handleImage(currentTask, sourceImage);
                if (currentTask == null || currentTask.isCancelled()) {
                    break;
                }
                if (newObject != null) {
                    pdResources.put(cosName, newObject);
                    count++;
                    if (isPreview) {
                        break;
                    }
                }
            }
            if (currentTask == null || currentTask.isCancelled()) {
                return count;
            }
            if (copyAllCheck.isSelected()) {
                targetDoc.getPage(currentParameters.currentPage - 1).setResources(pdResources);
            } else {
                targetDoc.addPage(sourcePage);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return count;

    }

    public PDImageXObject handleImage(FxTask currentTask, BufferedImage sourceImage) {
        if (sourceImage == null) {
            return null;
        }
        try {
            PDImageXObject newObject = null;
            BufferedImage targetImage;
            if ("ico".equals(format) || "icon".equals(format)) {
                targetImage = ImageConvertTools.convertToIcon(currentTask, sourceImage, attributes);
            } else {
                targetImage = ImageConvertTools.convertColorSpace(currentTask, sourceImage, attributes);
            }
            if (currentTask == null || !currentTask.isWorking()) {
                return null;
            }
            if (targetImage != null) {
                newObject = PdfTools.imageObject(currentTask, doc, format, targetImage);
            }
            return newObject;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public void postHandlePages(FxTask currentTask) {
        try {
            if (targetDoc != null) {
                targetDoc.save(tmpFile);
                targetDoc.close();
                File tFile = new File(currentTargetFile);
                if (FileTools.override(tmpFile, tFile)) {
                    targetFileGenerated(tFile);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        targetDoc = null;
    }

}
