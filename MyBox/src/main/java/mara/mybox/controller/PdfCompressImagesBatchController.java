/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageBinary;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.PdfTools;
import mara.mybox.tools.PdfTools.PdfImageFormat;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.CCITTFactory;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * @Author Mara
 * @CreateDate 2018-9-10
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfCompressImagesBatchController extends PdfBatchController {

    protected String AuthorKey;
    protected int jpegQuality, threshold;
    protected PdfImageFormat format;
    protected PDDocument targetDoc;
    protected File tmpFile;

    @FXML
    protected ToggleGroup formatGroup;
    @FXML
    protected ComboBox<String> jpegBox;
    @FXML
    protected TextField thresholdInput, authorInput;
    @FXML
    protected CheckBox ditherCheck, copyAllCheck;

    public PdfCompressImagesBatchController() {
        baseTitle = AppVariables.message("PdfCompressImagesBatch");
        AuthorKey = "AuthorKey";
    }

    @Override
    public void initOptionsSection() {

        formatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkFormat();
            }
        });
        checkFormat();

        jpegBox.getItems().addAll(Arrays.asList(
                "100", "75", "90", "50", "60", "80", "30", "10"
        ));
        jpegBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                checkJpegQuality();
            }
        });
        jpegBox.getSelectionModel().select(0);
        checkJpegQuality();

        thresholdInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                checkThreshold();
            }
        });
        checkThreshold();

        authorInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                AppVariables.setUserConfigValue(AuthorKey, newValue);
            }
        });
        authorInput.setText(AppVariables.getUserConfigValue(AuthorKey, System.getProperty("user.name")));

    }

    protected void checkFormat() {
        jpegBox.setDisable(true);
        jpegBox.setStyle(null);
        thresholdInput.setDisable(true);

        RadioButton selected = (RadioButton) formatGroup.getSelectedToggle();
        if (AppVariables.message("CCITT4").equals(selected.getText())) {
            format = PdfImageFormat.Tiff;
            thresholdInput.setDisable(false);
        } else if (AppVariables.message("JpegQuailty").equals(selected.getText())) {
            format = PdfImageFormat.Jpeg;
            jpegBox.setDisable(false);
            checkJpegQuality();
        }
    }

    protected void checkJpegQuality() {
        jpegQuality = 100;
        try {
            jpegQuality = Integer.valueOf(jpegBox.getSelectionModel().getSelectedItem());
            if (jpegQuality >= 0 && jpegQuality <= 100) {
                jpegBox.setStyle(null);
            } else {
                jpegBox.setStyle(badStyle);
            }
        } catch (Exception e) {
            jpegBox.setStyle(badStyle);
        }
    }

    protected void checkThreshold() {
        try {
            if (thresholdInput.getText().isEmpty()) {
                threshold = -1;
                thresholdInput.setStyle(null);
                return;
            }
            threshold = Integer.valueOf(thresholdInput.getText());
            if (threshold >= 0 && threshold <= 255) {
                thresholdInput.setStyle(null);
            } else {
                threshold = -1;
                thresholdInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            threshold = -1;
            thresholdInput.setStyle(badStyle);
        }
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    Bindings.isEmpty(tableView.getItems())
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(jpegBox.styleProperty().isEqualTo(badStyle))
                            .or(thresholdInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(tableView.getItems()))
            );
        } catch (Exception e) {
            logger.debug(e.toString());
        }
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
                targetDoc = PdfTools.createPDF(tmpFile, authorInput.getText());
            }

        } catch (Exception e) {
            logger.error(e.toString());
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
                PDImageXObject newObject = null;
                if (format == PdfImageFormat.Tiff) {
                    ImageBinary imageBinary = new ImageBinary(sourceImage, threshold);
                    imageBinary.setIsDithering(ditherCheck.isSelected());
                    BufferedImage newImage = imageBinary.operate();
                    newImage = ImageBinary.byteBinary(newImage);
                    newObject = CCITTFactory.createFromImage(doc, newImage);

                } else if (format == PdfImageFormat.Jpeg) {
                    newObject = JPEGFactory.createFromImage(doc, sourceImage, jpegQuality / 100f);
                }
                if (newObject != null) {
                    pdResources.put(cosName, newObject);
                    count++;
                }
                if (isPreview) {
                    break;
                }
            }
            if (copyAllCheck.isSelected()) {
                targetDoc.getPage(currentParameters.currentPage - 1).setResources(pdResources);
            } else {
                targetDoc.addPage(sourcePage);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return count;

    }

    @Override
    public void postHandlePages() {
        try {
            if (targetDoc != null) {
                targetDoc.save(tmpFile);
                targetDoc.close();
                File tFile = new File(currentTargetFile);
                if (tFile.exists()) {
                    tFile.delete();
                }
                tmpFile.renameTo(tFile);
                currentParameters.finalTargetName = currentTargetFile;
                targetFiles.add(new File(currentTargetFile));
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
        targetDoc = null;
    }
}
