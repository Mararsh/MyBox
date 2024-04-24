package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.PdfTools;
import static mara.mybox.value.Languages.message;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;

/**
 * @Author Mara
 * @CreateDate 2024-4-23
 * @License Apache License Version 2.0
 *
 * Refer: https://www.jb51.net/article/267446.htm
 * https://blog.csdn.net/cl939974883/article/details/136128194
 */
public class PdfAddWatermarkBatchController extends BaseBatchPdfController {

    protected PDExtendedGraphicsState waterTextState, waterImageState;
    protected PDImageXObject waterImage;
    protected PDFont waterTextFont, headerFont, footerFont, numberFont;
    protected int totalPages;
    protected File tmpFile;
    protected PDDocument targetDoc;
    protected PDPage pdPage;
    protected float pageWidth, pageHeight;
    protected int pageRotation;

    @FXML
    protected ControlPdfPageAttributes attributesController;

    public PdfAddWatermarkBatchController() {
        baseTitle = message("PdfAddWatermark");
        browseTargets = true;
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.PDF, VisitHistory.FileType.PDF);
    }

    @FXML
    @Override
    public boolean makeMoreParameters() {
        try {
            if (!super.makeMoreParameters() || !attributesController.pickValues()) {
                return false;
            }
            waterTextState = new PDExtendedGraphicsState();
            waterTextState.setNonStrokingAlphaConstant(attributesController.waterTextOpacity / 100f);
            waterTextState.setAlphaSourceFlag(true);
            waterTextState.setBlendMode(attributesController.waterTextBlend);

            waterImageState = new PDExtendedGraphicsState();
            waterImageState.setNonStrokingAlphaConstant(attributesController.waterImageOpacity / 100f);
            waterImageState.setAlphaSourceFlag(true);
            waterImageState.setBlendMode(attributesController.waterImageBlend);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean preHandlePages(FxTask currentTask) {
        try {
            if (doc == null) {
                return false;
            }
            File tFile = makeTargetFile(
                    FileNameTools.prefix(currentParameters.currentSourceFile.getName()),
                    ".pdf", currentParameters.currentTargetPath);
            currentTargetFile = tFile.getAbsolutePath();
            tmpFile = FileTmpTools.getTempFile();
            targetDoc = doc;

            totalPages = doc.getNumberOfPages();
            waterImage = null;
            if (attributesController.setWaterImage) {
                waterImage = PDImageXObject.createFromFile(attributesController.waterImageFile, doc);
            }
            waterTextFont = null;
            if (attributesController.setWaterText) {
                waterTextFont = PdfTools.getFont(doc, attributesController.waterTextFontFile);
            }
            headerFont = null;
            if (attributesController.setHeader) {
                headerFont = PdfTools.getFont(doc, attributesController.headerFontFile);
            }
            footerFont = null;
            if (attributesController.setFooter) {
                footerFont = PdfTools.getFont(doc, attributesController.footerFontFile);
            }
            numberFont = null;
            if (attributesController.setNumber) {
                numberFont = PdfTools.getFont(doc, attributesController.numberFontFile);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public int handleCurrentPage(FxTask currentTask) {
        pdPage = doc.getPage(currentParameters.currentPage - 1);  // 0-based
        try (PDPageContentStream cs = new PDPageContentStream(doc, pdPage,
                PDPageContentStream.AppendMode.APPEND, true, true)) {

            pageWidth = pdPage.getMediaBox().getWidth();
            pageHeight = pdPage.getMediaBox().getHeight();
            pageRotation = pdPage.getRotation();
            switch (pageRotation) {
                case 90:
                    pageWidth = pdPage.getMediaBox().getHeight();
                    pageHeight = pdPage.getMediaBox().getWidth();
                    cs.transform(Matrix.getRotateInstance(Math.toRadians(90), pageHeight, 0));
                    break;
                case 180:
                    cs.transform(Matrix.getRotateInstance(Math.toRadians(180), pageWidth, pageHeight));
                    break;
                case 270:
                    pageWidth = pdPage.getMediaBox().getHeight();
                    pageHeight = pdPage.getMediaBox().getWidth();
                    cs.transform(Matrix.getRotateInstance(Math.toRadians(270), 0, pageWidth));
                    break;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return 0;
        }

        if (waterImage != null) {
            drawWaterImage();
        }
        if (waterTextFont != null) {
            drawWaterText();
        }
        if (headerFont != null) {
            drawHeader();
        }
        if (footerFont != null) {
            drawFooter();
        }
        if (numberFont != null) {
            drawNumber();
        }
        return 1;

    }

    // https://www.jb51.net/article/267446.htm
    public void drawWaterImage() {
        if (pdPage == null || waterImage == null) {
            return;
        }
        try (PDPageContentStream cs = new PDPageContentStream(doc, pdPage,
                PDPageContentStream.AppendMode.APPEND, true, true)) {

            float cellWidth = (pageWidth - attributesController.waterImageMargin * 2)
                    / attributesController.waterImageColumns;
            float cellHeight = (pageHeight - attributesController.waterImageMargin * 2)
                    / attributesController.waterImageRows;

            float xOffset = attributesController.waterImageMargin
                    + (cellWidth - attributesController.waterImageWidth) / 2;
            float yOffset = attributesController.waterImageMargin
                    + (cellHeight - attributesController.waterImageHeight) / 2;

            float x;
            float y;
            for (int i = 0; i < attributesController.waterImageRows; i++) {
                y = i * cellHeight + yOffset;

                for (int j = 0; j < attributesController.waterImageColumns; j++) {
                    cs.setGraphicsStateParameters(waterImageState);

                    x = j * cellWidth + xOffset;
                    x += Math.sin(Math.toRadians(attributesController.waterImageRotate))
                            * attributesController.waterImageHeight;
                    Matrix matrix = new Matrix();
                    matrix.translate(x, y);
                    matrix.rotate(Math.toRadians(attributesController.waterImageRotate));
                    matrix.scale(attributesController.waterImageWidth, attributesController.waterImageHeight);

                    cs.drawImage(waterImage, matrix);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    // https://blog.csdn.net/cl939974883/article/details/136128194
    public void drawWaterText() {
        if (pdPage == null || waterTextFont == null) {
            return;
        }
        try (PDPageContentStream cs = new PDPageContentStream(doc, pdPage,
                PDPageContentStream.AppendMode.APPEND, true, true)) {

            float cellWidth = (pageWidth - attributesController.waterTextMargin * 2)
                    / attributesController.waterTextColumns;
            float cellHeight = (pageHeight - attributesController.waterTextMargin * 2)
                    / attributesController.waterTextRows;

            float xOffset = attributesController.waterTextMargin;
            float yOffset = attributesController.waterTextMargin;

            float x;
            float y;
            for (int i = 0; i < attributesController.waterTextRows; i++) {
                y = i * cellHeight + yOffset;

                for (int j = 0; j < attributesController.waterTextColumns; j++) {
                    x = j * cellWidth + xOffset;

                    cs.beginText();
                    cs.setFont(waterTextFont, attributesController.waterTextSize);
                    cs.setNonStrokingColor(attributesController.waterTextBgColor);
                    cs.setStrokingColor(attributesController.waterTextColor);
                    cs.setGraphicsStateParameters(waterTextState);
                    Matrix matrix = new Matrix();
                    matrix.translate(x, y);
                    matrix.rotate(Math.toRadians(attributesController.waterImageRotate));
                    cs.setTextMatrix(matrix);
                    cs.newLineAtOffset(x, y);
                    cs.showText(attributesController.waterText);
                    cs.endText();
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void drawHeader() {
        if (pdPage == null || headerFont == null) {
            return;
        }
        try (PDPageContentStream cs = new PDPageContentStream(doc, pdPage,
                PDPageContentStream.AppendMode.APPEND, true, true)) {

            cs.beginText();
            cs.setFont(headerFont, attributesController.headerSize);
            cs.setNonStrokingColor(attributesController.headerBgColor);
            cs.setStrokingColor(attributesController.headerColor);
            cs.newLineAtOffset(20, pageHeight - 20 + 2);
            cs.showText(attributesController.header);
            cs.endText();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void drawFooter() {
        if (pdPage == null || footerFont == null) {
            return;
        }
        try (PDPageContentStream cs = new PDPageContentStream(doc, pdPage,
                PDPageContentStream.AppendMode.APPEND, true, true)) {

            cs.beginText();
            cs.setFont(headerFont, attributesController.footerSize);
            cs.setNonStrokingColor(attributesController.footerBgColor);
            cs.setStrokingColor(attributesController.footerColor);
            cs.newLineAtOffset(pageWidth - 20 + 2, 20);
            cs.showText(attributesController.footer);
            cs.endText();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void drawNumber() {
        if (pdPage == null || numberFont == null) {
            return;
        }
        try (PDPageContentStream cs = new PDPageContentStream(doc, pdPage,
                PDPageContentStream.AppendMode.APPEND, true, true)) {

            cs.beginText();
            cs.setFont(headerFont, attributesController.numberSize);
            cs.setNonStrokingColor(attributesController.numberBgColor);
            cs.setStrokingColor(attributesController.numberColor);
            cs.newLineAtOffset(pageWidth + 20 - 80, 5);
            cs.showText(currentParameters.currentPage + " / " + totalPages);
            cs.endText();
        } catch (Exception e) {
            MyBoxLog.error(e);
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
