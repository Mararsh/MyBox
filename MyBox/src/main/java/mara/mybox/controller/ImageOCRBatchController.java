package mara.mybox.controller;

import com.recognition.software.jdeskew.ImageDeskew;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import mara.mybox.data.ConvolutionKernel;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageBinary;
import mara.mybox.image.ImageContrast;
import mara.mybox.image.ImageConvolution;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.PixelsOperation;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.OCRTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import net.sourceforge.tess4j.ITessAPI.TessPageIteratorLevel;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;
import net.sourceforge.tess4j.util.ImageHelper;

/**
 * @Author Mara
 * @CreateDate 2019-9-18
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageOCRBatchController extends ImagesBatchController {

    protected float scale;
    protected int threshold, rotate, regionLevel, wordLevel, tesseractVersion;
    protected ImageContrast.ContrastAlgorithm contrastAlgorithm;
    protected BufferedImage lastImage;
    protected ITesseract OCRinstance;
    protected List<File> textFiles;
    protected Process process;
    protected File configFile;

    @FXML
    protected VBox preprocessVBox;
    @FXML
    protected ComboBox<String> algorithmSelector, rotateSelector, binarySelector, scaleSelector;
    @FXML
    protected CheckBox deskewCheck, invertCheck, mergeCheck;
    @FXML
    protected ImageOCROptionsController ocrOptionsController;
    @FXML
    protected Tab ocrOptionsTab;

    public ImageOCRBatchController() {
        baseTitle = AppVariables.message("ImageOCRBatch");
        browseTargets = false;
    }

    @Override
    public void initOptionsSection() {
        try {
            ocrOptionsController.setValues(this, true, true);

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
                            scaleSelector.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        scaleSelector.getEditor().setStyle(badStyle);
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
                            binarySelector.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        binarySelector.getEditor().setStyle(badStyle);
                    }
                }
            });

            algorithmSelector.getItems().addAll(Arrays.asList(
                    message("EdgeDetection") + "-" + message("EightNeighborLaplaceInvert"),
                    message("EdgeDetection") + "-" + message("EightNeighborLaplace"),
                    message("HSBHistogramEqualization"), message("GrayHistogramEqualization"),
                    message("GrayHistogramStretching"), message("GrayHistogramShifting"),
                    message("UnsharpMasking"),
                    message("Enhancement") + "-" + message("EightNeighborLaplace"),
                    message("Enhancement") + "-" + message("FourNeighborLaplace"),
                    message("GaussianBlur"), message("AverageBlur")
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
                    AppVariables.setUserConfigValue("ImageOCRDeskew", newValue);
                }
            });
            deskewCheck.setSelected(AppVariables.getUserConfigBoolean("ImageOCRDeskew", false));

            mergeCheck.setSelected(AppVariables.getUserConfigBoolean("ImageOCRmerge", false));
            mergeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue("ImageOCRmerge", mergeCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public boolean makeActualParameters() {
        if (!super.makeActualParameters()) {
            return false;
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
                String path = AppVariables.getUserConfigValue(OCRTools.TessDataPath, null);
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
                    popError(message("InvalidParameters"));
                    ocrOptionsController.tesseractPathController.fileInput.setStyle(badStyle);
                    return false;
                }
                File dataPath = ocrOptionsController.dataPathController.file;
                if (!dataPath.exists()) {
                    popError(message("InvalidParameters"));
                    ocrOptionsController.dataPathController.fileInput.setStyle(badStyle);
                    return false;
                }
                configFile = FileTools.getTempFile();
                String s = "tessedit_create_txt 1\n";
                if (ocrOptionsController.htmlCheck.isSelected()) {
                    s += "tessedit_create_hocr 1\n";
                }
                if (ocrOptionsController.pdfCheck.isSelected()) {
                    s += "tessedit_create_pdf 1\n";
                }
                Map<String, String> p = ocrOptionsController.checkParameters();
                if (p != null) {
                    for (String key : p.keySet()) {
                        s += key + "\t" + p.get(key) + "\n";
                    }
                }
                FileTools.writeFile(configFile, s, Charset.forName("utf-8"));
                if (!configFile.exists()) {
                    popError(message("NotFound") + ":" + configFile);
                    return false;
                }
            }
            textFiles = new ArrayList<>();
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
        ocrOptionsTab.setDisable(disable);
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            countHandling(srcFile);
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return AppVariables.message("Skip");
            }
            lastImage = preprocess(srcFile);
            if (lastImage == null) {
                return AppVariables.message("Failed");
            }

            boolean ret;
            if (ocrOptionsController.embedRadio.isSelected()) {
                ret = embedded(srcFile, target);
            } else {
                ret = command(srcFile, target);
            }
            if (ret) {
                return AppVariables.message("Successful");
            } else {
                return AppVariables.message("Failed");
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return AppVariables.message("Failed");
        }
    }

    public BufferedImage preprocess(File srcFile) {
        try {
            lastImage = ImageFileReaders.readImage(srcFile);
            if (lastImage == null) {
                return null;
            }
//            lastImage = ImageManufacture.removeAlpha(lastImage);
            if (threshold > 0) {
                ImageBinary bin = new ImageBinary(lastImage, threshold);
                lastImage = bin.operateImage();
            }

            if (rotate != 0) {
                lastImage = ImageManufacture.rotateImage(lastImage, rotate);
            }
            if (scale > 0 && scale != 1) {
                lastImage = ImageManufacture.scaleImageByScale(lastImage, scale);
            }

            String algorithm = algorithmSelector.getValue();
            if (algorithm == null || algorithm.trim().isEmpty()) {
            } else if (message("GrayHistogramEqualization").equals(algorithm)) {
                ImageContrast imageContrast = new ImageContrast(lastImage,
                        ImageContrast.ContrastAlgorithm.Gray_Histogram_Equalization);
                lastImage = imageContrast.operateImage();

            } else if (message("GrayHistogramStretching").equals(algorithm)) {
                ImageContrast imageContrast = new ImageContrast(lastImage,
                        ImageContrast.ContrastAlgorithm.Gray_Histogram_Stretching);
                imageContrast.setIntPara1(100);
                imageContrast.setIntPara2(100);
                lastImage = imageContrast.operateImage();

            } else if (message("GrayHistogramShifting").equals(algorithm)) {
                ImageContrast imageContrast = new ImageContrast(lastImage,
                        ImageContrast.ContrastAlgorithm.Gray_Histogram_Shifting);
                imageContrast.setIntPara1(80);
                lastImage = imageContrast.operateImage();

            } else if (message("HSBHistogramEqualization").equals(algorithm)) {
                ImageContrast imageContrast = new ImageContrast(lastImage,
                        ImageContrast.ContrastAlgorithm.HSB_Histogram_Equalization);
                lastImage = imageContrast.operateImage();

            } else if (message("UnsharpMasking").equals(algorithm)) {
                ConvolutionKernel kernel = ConvolutionKernel.makeUnsharpMasking(3);
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.operateImage();

            } else if ((message("Enhancement") + "-" + "FourNeighborLaplace").equals(algorithm)) {
                ConvolutionKernel kernel = ConvolutionKernel.MakeSharpenFourNeighborLaplace();
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.operateImage();

            } else if ((message("Enhancement") + "-" + "EightNeighborLaplace").equals(algorithm)) {
                ConvolutionKernel kernel = ConvolutionKernel.MakeSharpenEightNeighborLaplace();
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.operateImage();

            } else if (message("GaussianBlur").equals(algorithm)) {
                ConvolutionKernel kernel = ConvolutionKernel.makeGaussBlur(3);
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.operateImage();

            } else if (message("AverageBlur").equals(algorithm)) {
                ConvolutionKernel kernel = ConvolutionKernel.makeAverageBlur(1);
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.operateImage();

            } else if ((message("EdgeDetection") + "-" + message("EightNeighborLaplaceInvert")).equals(algorithm)) {
                ConvolutionKernel kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplaceInvert().setGray(true);
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.operateImage();

            } else if ((message("EdgeDetection") + "-" + message("EightNeighborLaplace")).equals(algorithm)) {
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
                PixelsOperation pixelsOperation = PixelsOperation.create(lastImage,
                        null, PixelsOperation.OperationType.RGB, PixelsOperation.ColorActionType.Invert);
                lastImage = pixelsOperation.operateImage();
            }
            return lastImage;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    protected boolean embedded(File srcFile, File targetFile) {
        try {
            if (lastImage == null || OCRinstance == null) {
                return false;
            }
            List<ITesseract.RenderedFormat> formats = new ArrayList<>();
            formats.add(ITesseract.RenderedFormat.TEXT);
            if (ocrOptionsController.htmlCheck.isSelected()) {
                formats.add(ITesseract.RenderedFormat.HOCR);
            }
            if (ocrOptionsController.pdfCheck.isSelected()) {
                formats.add(ITesseract.RenderedFormat.PDF);
            }
            String prefix = FileTools.getFilePrefix(targetFile.getAbsolutePath());

            OCRinstance.createDocumentsWithResults​(lastImage, null,
                    prefix, formats, TessPageIteratorLevel.RIL_SYMBOL);
            File textFile = new File(prefix + ".txt");
            if (!textFile.exists()) {
                updateLogs(message("Failed" + ":" + textFile), true, true);
                return false;
            }
            textFiles.add(textFile);
            targetFileGenerated(textFile);

            if (ocrOptionsController.htmlCheck.isSelected()) {
                File hocrFile = new File(prefix + ".hocr");
                File htmlFile = new File(prefix + ".html");
                if (FileTools.rename(hocrFile, htmlFile)) {
                    targetFileGenerated(htmlFile);
                }
            }

            if (ocrOptionsController.pdfCheck.isSelected()) {
                File pdfFile = new File(prefix + ".pdf");
                targetFileGenerated(pdfFile);
            }

            if (wordLevel >= 0) {
                List<Word> words = OCRinstance.getWords(lastImage, wordLevel);
                List<String> names = new ArrayList<>();
                names.addAll(Arrays.asList(message("Index"),
                        message("Contents"), message("Confidence"),
                        message("CoordinateX"), message("CoordinateY"),
                        message("Width"), message("Height")
                ));
                StringTable table = new StringTable(names, srcFile.getAbsolutePath());
                for (int i = 0; i < words.size(); ++i) {
                    Word word = words.get(i);
                    Rectangle rect = word.getBoundingBox();
                    List<String> data = new ArrayList<>();
                    data.addAll(Arrays.asList(
                            i + "", word.getText(), word.getConfidence() + "",
                            rect.x + "", rect.y + "", rect.width + "", rect.height + ""
                    ));
                    table.add(data);
                }
                String html = StringTable.tableHtml(table);
                File wordsFile = new File(prefix + "_words.html");
                if (FileTools.writeFile(wordsFile, html) != null) {
                    targetFileGenerated(wordsFile);
                } else {
                }
            }

            if (regionLevel >= 0) {
                List<Rectangle> rectangles = OCRinstance.getSegmentedRegions(lastImage, regionLevel);
                List<String> names = new ArrayList<>();
                names.addAll(Arrays.asList(message("Index"),
                        message("CoordinateX"), message("CoordinateY"),
                        message("Width"), message("Height")
                ));
                StringTable table = new StringTable(names, srcFile.getAbsolutePath());
                for (int i = 0; i < rectangles.size(); ++i) {
                    Rectangle rect = rectangles.get(i);
                    List<String> data = new ArrayList<>();
                    data.addAll(Arrays.asList(
                            i + "", rect.x + "", rect.y + "", rect.width + "", rect.height + ""
                    ));
                    table.add(data);
                }
                String html = StringTable.tableHtml(table);
                File regionsFile = new File(prefix + "_regions.html");
                if (FileTools.writeFile(regionsFile, html) != null) {
                    targetFileGenerated(regionsFile);
                } else {
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    protected boolean command(File srcFile, File targetFile) {
        if (lastImage == null || configFile == null || !configFile.exists()) {
            return false;
        }
        if (process != null) {
            process.destroy();
            process = null;
        }
        try {
            String fileBase = FileTools.getFilePrefix(targetFile.getAbsolutePath());
            List<String> parameters = new ArrayList<>();
            parameters.addAll(Arrays.asList(
                    ocrOptionsController.tesseractPathController.file.getAbsolutePath(),
                    srcFile.getAbsolutePath(), fileBase,
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
            if (!textFile.exists()) {
                updateLogs(message("Failed" + ":" + outputs), true, true);
                return false;
            }
            textFiles.add(textFile);
            targetFileGenerated(textFile);

            if (ocrOptionsController.htmlCheck.isSelected()) {
                File hocrFile = new File(fileBase + ".hocr");
                File htmlFile = new File(fileBase + ".html");
                if (FileTools.rename(hocrFile, htmlFile)) {
                    targetFileGenerated(htmlFile);
                }
            }

            if (ocrOptionsController.pdfCheck.isSelected()) {
                File pdfFile = new File(fileBase + ".pdf");
                targetFileGenerated(pdfFile);
            }
            if (process != null) {
                process.destroy();
                process = null;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    @Override
    public File makeTargetFile(File srcFile, File targetPath) {
        try {
            String namePrefix = FileTools.getFilePrefix(srcFile.getName());
            namePrefix = namePrefix.replace(" ", "_");
            return makeTargetFile(namePrefix, ".txt", targetPath);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @Override
    public void donePost() {
        if (textFiles != null && textFiles.size() > 1 && mergeCheck.isSelected()) {
            File mFile = new File(FileTools.appendName(textFiles.get(0).getAbsolutePath(), "_OCR_merged"));
            if (FileTools.mergeFiles(textFiles, mFile)) {
                popInformation(MessageFormat.format(message("FilesGenerated"), mFile.getAbsolutePath()));
                targetFileGenerated(mFile);
            }
        }
        super.donePost();

    }

    @Override
    public void quitProcess() {
        OCRinstance = null;
    }

}
