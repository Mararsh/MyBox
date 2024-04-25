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
    protected float pageWidth, pageHeight, waterTextFontWidth, waterTextFontHeight,
            waterTextAngle, waterImageAngle;
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
            if (attributesController.setWaterText) {
                waterTextState = new PDExtendedGraphicsState();
                waterTextState.setNonStrokingAlphaConstant(attributesController.waterTextOpacity / 100f);
                waterTextState.setAlphaSourceFlag(true);
                waterTextState.setBlendMode(attributesController.waterTextBlend);
                waterTextAngle = (float) Math.toRadians(attributesController.waterTextRotate);
            }

            if (attributesController.setWaterImage) {
                waterImageState = new PDExtendedGraphicsState();
                waterImageState.setNonStrokingAlphaConstant(attributesController.waterImageOpacity / 100f);
                waterImageState.setAlphaSourceFlag(true);
                waterImageState.setBlendMode(attributesController.waterImageBlend);
                waterImageAngle = (float) Math.toRadians(attributesController.waterImageRotate);
            }
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
                waterTextFontWidth = attributesController.waterTextWidth(waterTextFont);
                waterTextFontHeight = attributesController.waterTextHeight(waterTextFont);
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

            if (headerFont != null) {
                drawHeader(cs);
            }
            if (footerFont != null) {
                drawFooter(cs);
            }
            if (numberFont != null) {
                drawNumber(cs);
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
        return 1;
    }

    // (0,0) is left-bottom of the page
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

            float scaleWidth = attributesController.waterImageWidth;
            float scaleHeight = attributesController.waterImageHeight;

            float xOffset = attributesController.waterImageMargin
                    + (cellWidth - scaleWidth) / 2;
            float yOffset = attributesController.waterImageMargin
                    + (cellHeight - scaleHeight) / 2;

            float x;
            float y;
            cs.setGraphicsStateParameters(waterImageState);
            for (int i = 0; i < attributesController.waterImageRows; i++) {
                y = i * cellHeight + yOffset;

                for (int j = 0; j < attributesController.waterImageColumns; j++) {
                    x = j * cellWidth + xOffset;
                    x += Math.sin(waterImageAngle) * scaleWidth;
                    Matrix matrix = new Matrix();
                    matrix.translate(x, y);
                    matrix.rotate(waterImageAngle);
                    matrix.scale(scaleWidth, scaleHeight);
                    cs.drawImage(waterImage, matrix);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

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

            float xOffset = attributesController.waterTextMargin
                    + (cellWidth - waterTextFontWidth) / 2;
            float yOffset = attributesController.waterTextMargin
                    + (cellHeight - waterTextFontHeight) / 2;

            float x;
            float y;
            cs.setGraphicsStateParameters(waterTextState);
            for (int i = 0; i < attributesController.waterTextRows; i++) {
                y = i * cellHeight + yOffset;

                for (int j = 0; j < attributesController.waterTextColumns; j++) {
                    x = j * cellWidth + xOffset;
                    x += Math.sin(waterTextAngle) * waterTextFontHeight;

                    Matrix matrix = new Matrix();
                    matrix.translate(x, y);
                    matrix.rotate(waterTextAngle);

                    cs.beginText();
                    cs.setFont(waterTextFont, attributesController.waterTextSize);
                    cs.setStrokingColor(attributesController.waterTextColor);
                    cs.setNonStrokingColor(attributesController.waterTextColor);
                    cs.setTextMatrix(matrix);
                    cs.showText(attributesController.waterText);
                    cs.endText();
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void drawHeader(PDPageContentStream cs) {
        if (pdPage == null || headerFont == null) {
            return;
        }
        try {
            cs.beginText();
            cs.setFont(headerFont, attributesController.headerSize);
            cs.setStrokingColor(attributesController.headerColor);
            cs.setNonStrokingColor(attributesController.headerColor);
            cs.newLineAtOffset(PdfTools.DefaultMargin,
                    pageHeight - PdfTools.DefaultMargin
                    - PdfTools.fontHeight(headerFont, attributesController.headerSize));
            cs.showText(attributesController.header);
            cs.endText();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void drawFooter(PDPageContentStream cs) {
        if (pdPage == null || footerFont == null) {
            return;
        }
        try {
            cs.beginText();
            cs.setFont(footerFont, attributesController.footerSize);
            cs.setStrokingColor(attributesController.footerColor);
            cs.setNonStrokingColor(attributesController.footerColor);
            cs.newLineAtOffset(pageWidth - PdfTools.DefaultMargin
                    - PdfTools.fontWidth(footerFont, attributesController.footer, attributesController.footerSize),
                    PdfTools.DefaultMargin
                    + PdfTools.fontHeight(footerFont, attributesController.footerSize));
            cs.showText(attributesController.footer);
            cs.endText();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void drawNumber(PDPageContentStream cs) {
        if (pdPage == null || numberFont == null) {
            return;
        }
        try {
            cs.beginText();
            cs.setFont(numberFont, attributesController.numberSize);
            cs.setStrokingColor(attributesController.numberColor);
            cs.setNonStrokingColor(attributesController.numberColor);
            String num = currentParameters.currentPage + " / " + totalPages;
            cs.newLineAtOffset(pageWidth - PdfTools.DefaultMargin
                    - PdfTools.fontWidth(numberFont, num, attributesController.numberSize),
                    PdfTools.fontHeight(numberFont, attributesController.numberSize));
            cs.showText(num);
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
