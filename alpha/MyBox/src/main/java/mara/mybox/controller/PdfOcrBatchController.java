package mara.mybox.controller;

import com.recognition.software.jdeskew.ImageDeskew;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.bufferedimage.AlphaTools;
import mara.mybox.bufferedimage.ImageBinary;
import mara.mybox.bufferedimage.ImageContrast;
import mara.mybox.bufferedimage.ImageConvolution;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.bufferedimage.TransformTools;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.OCRTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.util.ImageHelper;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * @Author Mara
 * @CreateDate 2019-9-18
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfOcrBatchController extends BaseBatchPdfController {

    protected String separator;
    protected String ocrTexts;
    protected File tmpFile;
    protected String ocrPath;
    protected BufferedImage lastImage;
    protected ITesseract OCRinstance;
    protected PDFRenderer renderer;
    protected int threshold, rotate, tesseractVersion;
    protected float scale;
    protected long pageStart;
    protected File configFile;
    protected Process process;

    @FXML
    protected ToggleGroup getImageType;
    @FXML
    protected CheckBox separatorCheck, deskewCheck, invertCheck;
    @FXML
    protected TextField separatorInput;
    @FXML
    protected ComboBox<String> algorithmSelector, rotateSelector,
            binarySelector, scaleSelector;
    @FXML
    protected RadioButton convertRadio, extractRadio;
    @FXML
    protected HBox scaleBox, dpiBox;
    @FXML
    protected FlowPane imageOptionsPane;
    @FXML
    protected ControlOCROptions ocrOptionsController;
    @FXML
    protected VBox preprocessVBox, ocrOptionsVBox;

    public PdfOcrBatchController() {
        baseTitle = Languages.message("PdfOCRBatch");
        browseTargets = false;
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.PDF, VisitHistory.FileType.Text);
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(separatorInput, Languages.message("InsertPageSeparatorComments"));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initOptionsSection() {
        try {
            initPreprocessBox();
            ocrOptionsController.setParameters(this, false, false);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void initPreprocessBox() {
        try {
            getImageType.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle oldV, Toggle newV) {
                    checkGetImageType();
                }
            });
            checkGetImageType();

            scale = 1.0f;
            scaleSelector.getItems().addAll(Arrays.asList(
                    "1.0", "1.5", "2.0", "2.5", "3.0", "5.0", "10.0"
            ));
            scaleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    try {
                        if (newV == null || newV.isEmpty()) {
                            scale = 1;
                            return;
                        }
                        float f = Float.valueOf(newV);
                        if (f > 0) {
                            scale = f;
                            scaleSelector.getEditor().setStyle(null);
                        } else {
                            scaleSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        scaleSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });
            scaleSelector.getSelectionModel().select(0);

            threshold = 0;
            binarySelector.getItems().addAll(Arrays.asList(
                    "65", "50", "75", "45", "30", "80", "85", "15"
            ));
            binarySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    try {
                        if (newV == null || newV.isEmpty()) {
                            threshold = 0;
                            return;
                        }
                        int i = Integer.valueOf(newV);
                        if (i > 0) {
                            threshold = i;
                            binarySelector.getEditor().setStyle(null);
                        } else {
                            binarySelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        binarySelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            algorithmSelector.getItems().addAll(Arrays.asList(Languages.message("EdgeDetection") + "-" + Languages.message("EightNeighborLaplaceInvert"),
                    Languages.message("EdgeDetection") + "-" + Languages.message("EightNeighborLaplace"),
                    Languages.message("HSBHistogramEqualization"), Languages.message("GrayHistogramEqualization"),
                    Languages.message("GrayHistogramStretching"), Languages.message("GrayHistogramShifting"),
                    Languages.message("UnsharpMasking"),
                    Languages.message("Enhancement") + "-" + Languages.message("EightNeighborLaplace"),
                    Languages.message("Enhancement") + "-" + Languages.message("FourNeighborLaplace"),
                    Languages.message("GaussianBlur"), Languages.message("AverageBlur")
            ));

            rotate = 0;
            rotateSelector.getItems().addAll(Arrays.asList(
                    "0", "90", "45", "15", "30", "60", "75", "180", "105", "135", "120", "150", "165", "270", "300", "315"
            ));
            rotateSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    try {
                        if (newV == null || newV.isEmpty()) {
                            rotate = 0;
                            return;
                        }
                        rotate = Integer.valueOf(newV);
                    } catch (Exception e) {

                    }
                }
            });

            deskewCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("ImageOCRDeskew", newValue);
                }
            });
            deskewCheck.setSelected(UserConfig.getBoolean("ImageOCRDeskew", false));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void checkGetImageType() {
        if (convertRadio.isSelected()) {
            if (imageOptionsPane.getChildren().contains(scaleBox)) {
                imageOptionsPane.getChildren().remove(scaleBox);
            }
            if (!imageOptionsPane.getChildren().contains(dpiBox)) {
                imageOptionsPane.getChildren().add(dpiBox);
            }
            scale = 1.0f;
        } else if (extractRadio.isSelected()) {

            if (imageOptionsPane.getChildren().contains(dpiBox)) {
                imageOptionsPane.getChildren().remove(dpiBox);
            }
            if (!imageOptionsPane.getChildren().contains(scaleBox)) {
                imageOptionsPane.getChildren().add(scaleBox);
            }
            scale = Float.valueOf(scaleSelector.getValue());
        }
    }

    @FXML
    public void clearAlgorithm() {
        algorithmSelector.setValue(null);
    }

    @FXML
    public void clearThreadhold() {
        binarySelector.setValue(null);
    }

    @FXML
    public void clearRotate() {
        rotateSelector.setValue(null);
    }

    @Override
    public boolean makeActualParameters() {
        if (!super.makeActualParameters()) {
            return false;
        }
        separator = separatorInput.getText();
        if (!separatorCheck.isSelected() || separator == null || separator.isEmpty()) {
            separator = null;
        }
        try {
            if (ocrOptionsController.embedRadio.isSelected()) {
                OCRinstance = new Tesseract();
                // https://stackoverflow.com/questions/58286373/tess4j-pdf-to-tiff-to-tesseract-warning-invalid-resolution-0-dpi-using-70/58296472#58296472
                OCRinstance.setTessVariable("user_defined_dpi", "96");
                OCRinstance.setTessVariable("debug_file", "/dev/null");
                OCRinstance.setPageSegMode(ocrOptionsController.psm);
                Map<String, String> p = ocrOptionsController.checkParameters();
                if (p != null && !p.isEmpty()) {
                    for (String key : p.keySet()) {
                        OCRinstance.setTessVariable(key, p.get(key));
                    }
                }
                String path = UserConfig.getString(OCRTools.TessDataPath, null);
                if (path != null) {
                    OCRinstance.setDatapath(path);
                }
                if (ocrOptionsController.selectedLanguages != null) {
                    OCRinstance.setLanguage(ocrOptionsController.selectedLanguages);
                }
            } else {
                tesseractVersion = ocrOptionsController.tesseractVersion();
                File tesseract = ocrOptionsController.tesseractPathController.file;
                if (!tesseract.exists()) {
                    popError(Languages.message("InvalidParameters"));
                    ocrOptionsController.tesseractPathController.fileInput.setStyle(UserConfig.badStyle());
                    return false;
                }
                File dataPath = ocrOptionsController.dataPathController.file;
                if (!dataPath.exists()) {
                    popError(Languages.message("InvalidParameters"));
                    ocrOptionsController.dataPathController.fileInput.setStyle(UserConfig.badStyle());
                    return false;
                }
                configFile = TmpFileTools.getTempFile();
                String s = "tessedit_create_txt 1\n";
                Map<String, String> p = ocrOptionsController.checkParameters();
                if (p != null) {
                    for (String key : p.keySet()) {
                        s += key + "\t" + p.get(key) + "\n";
                    }
                }
                TextFileTools.writeFile(configFile, s, Charset.forName("utf-8"));
                if (!configFile.exists()) {
                    popError(Languages.message("NotFound") + ":" + configFile);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public void disableControls(boolean disable) {
        super.disableControls(disable);
        preprocessVBox.setDisable(disable);
        ocrOptionsVBox.setDisable(disable);
    }

    @Override
    public boolean preHandlePages() {
        try {
            ocrTexts = "";
            renderer = new PDFRenderer(doc);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public int handleCurrentPage() {
        int num;
        updateLogs(Languages.message("HandlingPage") + ":" + currentParameters.currentPage, true, true);
        pageStart = new Date().getTime();
        if (convertRadio.isSelected()) {
            num = convertPage();
        } else {
            num = extractPage();
        }
        if (num > 0 && separatorCheck.isSelected()) {
            String s = separator.replace("<Page Number>", currentParameters.currentPage + " ");
            s = s.replace("<Total Number>", doc.getNumberOfPages() + "");
            ocrTexts += s + System.getProperty("line.separator");
        }
        return num;
    }

    protected int convertPage() {
        String text = null;
        try {
            // ImageType.BINARY work bad while ImageType.RGB works best
            BufferedImage bufferedImage
                    = renderer.renderImageWithDPI(currentParameters.currentPage - 1, dpi, ImageType.RGB);    // 0-based
            text = ocr(bufferedImage);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        if (text != null) {
            String s = Languages.message("Page") + ":" + currentParameters.currentPage + "   "
                    + MessageFormat.format(Languages.message("OCRresults"),
                            text.length(), DateTools.datetimeMsDuration(new Date().getTime() - pageStart));
            updateLogs(s, true, true);
            ocrTexts += text + System.getProperty("line.separator");
            return 1;
        } else {
            String s = Languages.message("Failed") + ":" + currentParameters.currentPage;
            updateLogs(s, true, true);
            return 0;
        }
    }

    protected int extractPage() {
        int count = 0;
        String text = "";
        try {
            PDPage page = doc.getPage(currentParameters.currentPage - 1);  // 0-based
            PDResources pdResources = page.getResources();
            Iterable<COSName> iterable = pdResources.getXObjectNames();
            if (iterable != null) {
                Iterator<COSName> pageIterator = iterable.iterator();
                int index = 0;
                while (pageIterator.hasNext()) {
                    if (task == null || task.isCancelled()) {
                        break;
                    }
                    COSName cosName = pageIterator.next();
                    if (!pdResources.isImageXObject(cosName)) {
                        continue;
                    }
                    index++;
                    PDImageXObject pdxObject = (PDImageXObject) pdResources.getXObject(cosName);
                    BufferedImage bufferedImage = pdxObject.getImage();
                    String results = ocr(bufferedImage);
                    if (results != null) {
                        text += results + System.getProperty("line.separator");
                        count++;
                    }
                    if (isPreview) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        if (!text.isBlank()) {
            String s = Languages.message("Page") + ":" + currentParameters.currentPage + "   "
                    + MessageFormat.format(Languages.message("OCRresults"),
                            text.length(), DateTools.datetimeMsDuration(new Date().getTime() - pageStart));
            updateLogs(s, true, true);
            ocrTexts += text + System.getProperty("line.separator");
        }
        return count;
    }

    protected BufferedImage preprocess(BufferedImage bufferedImage) {
        try {
            lastImage = bufferedImage;

            if (threshold > 0) {
                ImageBinary bin = new ImageBinary(lastImage, threshold);
                lastImage = bin.operateImage();
            }

            if (rotate != 0) {
                lastImage = TransformTools.rotateImage(lastImage, rotate);
            }
            if (scale > 0 && scale != 1) {
                lastImage = ScaleTools.scaleImageByScale(lastImage, scale);
            }

            String algorithm = algorithmSelector.getValue();
            if (algorithm == null || algorithm.trim().isEmpty()) {
            } else if (Languages.message("GrayHistogramEqualization").equals(algorithm)) {
                ImageContrast imageContrast = new ImageContrast(lastImage,
                        ImageContrast.ContrastAlgorithm.Gray_Histogram_Equalization);
                lastImage = imageContrast.operateImage();

            } else if (Languages.message("GrayHistogramStretching").equals(algorithm)) {
                ImageContrast imageContrast = new ImageContrast(lastImage,
                        ImageContrast.ContrastAlgorithm.Gray_Histogram_Stretching);
                imageContrast.setIntPara1(100);
                imageContrast.setIntPara2(100);
                lastImage = imageContrast.operateImage();

            } else if (Languages.message("GrayHistogramShifting").equals(algorithm)) {
                ImageContrast imageContrast = new ImageContrast(lastImage,
                        ImageContrast.ContrastAlgorithm.Gray_Histogram_Shifting);
                imageContrast.setIntPara1(80);
                lastImage = imageContrast.operateImage();

            } else if (Languages.message("HSBHistogramEqualization").equals(algorithm)) {
                ImageContrast imageContrast = new ImageContrast(lastImage,
                        ImageContrast.ContrastAlgorithm.HSB_Histogram_Equalization);
                lastImage = imageContrast.operateImage();

            } else if (Languages.message("UnsharpMasking").equals(algorithm)) {
                ConvolutionKernel kernel = ConvolutionKernel.makeUnsharpMasking(3);
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.operateImage();

            } else if ((Languages.message("Enhancement") + "-" + "FourNeighborLaplace").equals(algorithm)) {
                ConvolutionKernel kernel = ConvolutionKernel.MakeSharpenFourNeighborLaplace();
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.operateImage();

            } else if ((Languages.message("Enhancement") + "-" + "EightNeighborLaplace").equals(algorithm)) {
                ConvolutionKernel kernel = ConvolutionKernel.MakeSharpenEightNeighborLaplace();
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.operateImage();

            } else if (Languages.message("GaussianBlur").equals(algorithm)) {
                ConvolutionKernel kernel = ConvolutionKernel.makeGaussBlur(3);
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.operateImage();

            } else if (Languages.message("AverageBlur").equals(algorithm)) {
                ConvolutionKernel kernel = ConvolutionKernel.makeAverageBlur(1);
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.operateImage();

            } else if ((Languages.message("EdgeDetection") + "-" + Languages.message("EightNeighborLaplaceInvert")).equals(algorithm)) {
                ConvolutionKernel kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplaceInvert().setGray(true);
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.operateImage();

            } else if ((Languages.message("EdgeDetection") + "-" + Languages.message("EightNeighborLaplace")).equals(algorithm)) {
                ConvolutionKernel kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplace().setGray(true);
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.operateImage();
            }

            if (deskewCheck.isSelected()) {
                ImageDeskew id = new ImageDeskew(lastImage);
                double imageSkewAngle = id.getSkewAngle();
                if ((imageSkewAngle > OCRTools.MINIMUM_DESKEW_THRESHOLD
                        || imageSkewAngle < -(OCRTools.MINIMUM_DESKEW_THRESHOLD))) {
                    lastImage = ImageHelper.rotateImage(lastImage, -imageSkewAngle);
                }
            }

            if (invertCheck.isSelected()) {
                PixelsOperation pixelsOperation = PixelsOperationFactory.create(lastImage,
                        null, PixelsOperation.OperationType.RGB, PixelsOperation.ColorActionType.Invert);
                lastImage = pixelsOperation.operateImage();
            }

            return lastImage;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    protected String ocr(BufferedImage image) {
        if (image == null) {
            return null;
        }
        try {
            lastImage = preprocess(image);
            if (ocrOptionsController.embedRadio.isSelected()) {
                return OCRinstance.doOCR(lastImage);
            }
            if (configFile == null || !configFile.exists()) {
                return null;
            }
            if (process != null) {
                process.destroy();
                process = null;
            }
            String imageFile = TmpFileTools.getTempFile(".png").getAbsolutePath();
            BufferedImage bufferedImage = AlphaTools.removeAlpha(lastImage);
            ImageFileWriters.writeImageFile(bufferedImage, "png", imageFile);

            String fileBase = TmpFileTools.getTempFile().getAbsolutePath();
            List<String> parameters = new ArrayList<>();
            parameters.addAll(Arrays.asList(
                    ocrOptionsController.tesseractPathController.file.getAbsolutePath(),
                    imageFile, fileBase,
                    "--tessdata-dir", ocrOptionsController.dataPathController.file.getAbsolutePath(),
                    tesseractVersion > 3 ? "--psm" : "-psm", ocrOptionsController.psm + ""
            ));
            if (ocrOptionsController.selectedLanguages != null) {
                parameters.addAll(Arrays.asList("-l", ocrOptionsController.selectedLanguages));
            }
            parameters.add(configFile.getAbsolutePath());
            ProcessBuilder pb = new ProcessBuilder(parameters).redirectErrorStream(true);
            process = pb.start();
            String outputs = "", line;
            try ( BufferedReader inReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                while ((line = inReader.readLine()) != null) {
                    outputs += line + "\n";
                }
            } catch (Exception e) {
                outputs += e.toString() + "\n";
            }
            process.waitFor();
            File textFile = new File(fileBase + ".txt");
            if (textFile.exists()) {
                String texts = TextFileTools.readTexts(textFile);
                FileDeleteTools.delete(textFile);
                return texts;
            } else {
                updateLogs(Languages.message("Failed" + ":" + outputs), true, true);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return null;
    }

    @Override
    public void postHandlePages() {
        try {
            File tFile = makeTargetFile(FileNameTools.getFilePrefix(currentParameters.currentSourceFile.getName()),
                    ".txt", currentParameters.currentTargetPath);
            currentTargetFile = tFile.getAbsolutePath();
            if (TextFileTools.writeFile(tFile, ocrTexts) != null) {
                targetFileGenerated(tFile);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void quitProcess() {
        OCRinstance = null;
    }

}
