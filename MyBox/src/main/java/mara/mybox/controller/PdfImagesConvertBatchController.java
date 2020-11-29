package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageAttributes;
import mara.mybox.image.ImageConvert;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.PdfTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.CommonValues;
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
public class PdfImagesConvertBatchController extends PdfBatchController {

    protected String format;
    protected PDDocument targetDoc;
    protected File tmpFile;
    protected ImageAttributes attributes;

    @FXML
    protected CheckBox copyAllCheck;
    @FXML
    protected ImageConverterOptionsController optionsController;

    public PdfImagesConvertBatchController() {
        baseTitle = AppVariables.message("PdfImagesConvertBatch");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            copyAllCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "CopyAll", false));
            copyAllCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "CopyAll", false);
                }
            });

            if (optionsController != null) {
                optionsController.setValues(true);

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
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean makeActualParameters() {
        if (!super.makeActualParameters()) {
            return false;
        }
        if (optionsController != null) {
            attributes = optionsController.attributes;
            format = attributes.getImageFormat();
        }
        return true;
    }

    @Override
    public boolean preHandlePages() {
        try {
            File tFile = makeTargetFile(FileTools.getFilePrefix(currentParameters.currentSourceFile.getName()),
                    ".pdf", currentParameters.currentTargetPath);
            currentTargetFile = tFile.getAbsolutePath();
            tmpFile = FileTools.getTempFile();
            if (copyAllCheck.isSelected()) {
//                doc.save(tFile);
                targetDoc = doc;
            } else {
                targetDoc = PdfTools.createPDF(tmpFile, "MyBox v" + CommonValues.AppVersion);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            targetDoc = null;
        }
        return targetDoc != null;
    }

    @Override
    public int handleCurrentPage() {
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
                if (task.isCancelled()) {
                    break;
                }
                COSName cosName = pageIterator.next();
                if (!pdResources.isImageXObject(cosName)) {
                    continue;
                }
                PDImageXObject pdxObject = (PDImageXObject) pdResources.getXObject(cosName);
                BufferedImage sourceImage = pdxObject.getImage();
                PDImageXObject newObject = handleImage(sourceImage);
                if (newObject != null) {
                    pdResources.put(cosName, newObject);
                    count++;
                    if (isPreview) {
                        break;
                    }
                }
            }
            if (copyAllCheck.isSelected()) {
                targetDoc.getPage(currentParameters.currentPage - 1).setResources(pdResources);
            } else {
                targetDoc.addPage(sourcePage);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return count;

    }

    public PDImageXObject handleImage(BufferedImage sourceImage) {
        if (sourceImage == null) {
            return null;
        }
        try {
            PDImageXObject newObject = null;
            BufferedImage targetImage;
            if ("ico".equals(format) || "icon".equals(format)) {
                targetImage = ImageConvert.convertToIcon(sourceImage, attributes);
            } else {
                targetImage = ImageConvert.convertColorSpace(sourceImage, attributes);
            }
            if (targetImage != null) {
                newObject = PdfTools.imageObject(doc, format, targetImage);
            }
            return newObject;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @Override
    public void postHandlePages() {
        try {
            if (targetDoc != null) {
                targetDoc.save(tmpFile);
                targetDoc.close();
                File tFile = new File(currentTargetFile);
                if (FileTools.rename(tmpFile, tFile)) {
                    targetFileGenerated(tFile);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        targetDoc = null;
    }
}
