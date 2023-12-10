package mara.mybox.controller;

import com.recognition.software.jdeskew.ImageDeskew;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import mara.mybox.bufferedimage.ImageBinary;
import mara.mybox.bufferedimage.ImageContrast;
import mara.mybox.bufferedimage.ImageConvolution;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.bufferedimage.TransformTools;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.OCRTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
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
public class ImageOCRBatchController extends BaseBatchImageController {

    protected float scale;
    protected int threshold, rotate;
    protected ImageContrast.ContrastAlgorithm contrastAlgorithm;
    protected BufferedImage lastImage;
    protected Tesseract OCRinstance;
    protected List<File> textFiles;
    protected Process process;

    @FXML
    protected VBox preprocessVBox, ocrOptionsVBox;
    @FXML
    protected ComboBox<String> algorithmSelector, rotateSelector, binarySelector, scaleSelector;
    @FXML
    protected CheckBox deskewCheck, invertCheck, mergeCheck;
    @FXML
    protected ControlOCROptions ocrOptionsController;

    public ImageOCRBatchController() {
        baseTitle = message("ImageOCRBatch");
        browseTargets = false;
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Image, VisitHistory.FileType.Text);
    }

    @Override
    public void initOptionsSection() {
        try {
            ocrOptionsController.setParameters(this, true, true);

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
                        float f = Float.parseFloat(newV);
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
                        int i = Integer.parseInt(newV);
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

            algorithmSelector.getItems().addAll(Arrays.asList(message("EdgeDetection") + "-" + message("EightNeighborLaplaceInvert"),
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
                        rotate = Integer.parseInt(newV);
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

            mergeCheck.setSelected(UserConfig.getBoolean("ImageOCRmerge", false));
            mergeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("ImageOCRmerge", mergeCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e);
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
        try {
            if (ocrOptionsController.embedRadio.isSelected()) {
                OCRinstance = ocrOptionsController.tesseract();

            } else {

                if (!ocrOptionsController.checkCommandPamameters(true, true)) {
                    return false;
                }
            }
            textFiles = new ArrayList<>();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
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
    public String handleFile(File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return message("Skip");
            }
            lastImage = preprocess(srcFile);
            if (lastImage == null) {
                return message("Failed");
            }

            boolean ret;
            if (ocrOptionsController.embedRadio.isSelected()) {
                ret = embedded(srcFile, target);
            } else {
                ret = command(srcFile, target);
            }
            if (ret) {
                return message("Successful");
            } else {
                return message("Failed");
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return message("Failed");
        }
    }

    public BufferedImage preprocess(File srcFile) {
        try {
            lastImage = ImageFileReaders.readImage(task, srcFile);
            if (lastImage == null) {
                return null;
            }
//            lastImage = ImageManufacture.removeAlpha(lastImage);
            if (threshold > 0) {
                ImageBinary imageBinary = new ImageBinary();
                imageBinary.setImage(lastImage)
                        .setIntPara1(threshold);
                lastImage = imageBinary.operateImage();
            }

            if (rotate != 0) {
                lastImage = TransformTools.rotateImage(task, lastImage, rotate);
            }
            if (scale > 0 && scale != 1) {
                lastImage = ScaleTools.scaleImageByScale(lastImage, scale);
            }

            String algorithm = algorithmSelector.getValue();
            if (algorithm == null || algorithm.trim().isEmpty()) {
            } else if (message("GrayHistogramEqualization").equals(algorithm)) {
                ImageContrast imageContrast = new ImageContrast()
                        .setAlgorithm(ImageContrast.ContrastAlgorithm.Gray_Histogram_Equalization);
                imageContrast.setImage(lastImage).setTask(task);
                lastImage = imageContrast.operateImage();

            } else if (message("GrayHistogramStretching").equals(algorithm)) {
                ImageContrast imageContrast = new ImageContrast()
                        .setAlgorithm(ImageContrast.ContrastAlgorithm.Gray_Histogram_Stretching);
                imageContrast.setImage(lastImage).setTask(task).
                        setIntPara1(100).setIntPara2(100);
                lastImage = imageContrast.operateImage();

            } else if (message("GrayHistogramShifting").equals(algorithm)) {
                ImageContrast imageContrast = new ImageContrast()
                        .setAlgorithm(ImageContrast.ContrastAlgorithm.Gray_Histogram_Shifting);
                imageContrast.setImage(lastImage).setIntPara1(80).setTask(task);
                lastImage = imageContrast.operateImage();

            } else if (message("HSBHistogramEqualization").equals(algorithm)) {
                ImageContrast imageContrast = new ImageContrast()
                        .setAlgorithm(ImageContrast.ContrastAlgorithm.HSB_Histogram_Equalization);
                imageContrast.setImage(lastImage).setTask(task);
                lastImage = imageContrast.operateImage();

            } else if (message("UnsharpMasking").equals(algorithm)) {
                ConvolutionKernel kernel = ConvolutionKernel.makeUnsharpMasking(3);
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.setTask(task).operateImage();

            } else if ((message("Enhancement") + "-" + "FourNeighborLaplace").equals(algorithm)) {
                ConvolutionKernel kernel = ConvolutionKernel.MakeSharpenFourNeighborLaplace();
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.setTask(task).operateImage();

            } else if ((message("Enhancement") + "-" + "EightNeighborLaplace").equals(algorithm)) {
                ConvolutionKernel kernel = ConvolutionKernel.MakeSharpenEightNeighborLaplace();
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.setTask(task).operateImage();

            } else if (message("GaussianBlur").equals(algorithm)) {
                ConvolutionKernel kernel = ConvolutionKernel.makeGaussBlur(3);
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.setTask(task).operateImage();

            } else if (message("AverageBlur").equals(algorithm)) {
                ConvolutionKernel kernel = ConvolutionKernel.makeAverageBlur(1);
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.setTask(task).operateImage();

            } else if ((message("EdgeDetection") + "-" + message("EightNeighborLaplaceInvert")).equals(algorithm)) {
                ConvolutionKernel kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplaceInvert().setGray(true);
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.setTask(task).operateImage();

            } else if ((message("EdgeDetection") + "-" + message("EightNeighborLaplace")).equals(algorithm)) {
                ConvolutionKernel kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplace().setGray(true);
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.setTask(task).operateImage();
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
                lastImage = pixelsOperation.setTask(task).operateImage();
            }
            return lastImage;
        } catch (Exception e) {
            MyBoxLog.error(e);
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
            // Looks OCR engine does not support non-English file name
            String actualPrefix = targetFile.getParent() + File.separator
                    + FileNameTools.prefix(targetFile.getName());
            String tmpPrefix = FileTmpTools.getTempFile().getAbsolutePath();

            OCRinstance.createDocumentsWithResults​(lastImage, tmpPrefix,
                    tmpPrefix, formats, TessPageIteratorLevel.RIL_SYMBOL);
            File tmpTextFile = new File(tmpPrefix + ".txt");
            if (!tmpTextFile.exists()) {
                updateLogs(message("Failed" + ":" + tmpTextFile), true, true);
                return false;
            }
            File textFile = new File(actualPrefix + ".txt");
            FileTools.rename(tmpTextFile, textFile);
            textFiles.add(textFile);
            targetFileGenerated(textFile);

            if (ocrOptionsController.htmlCheck.isSelected()) {
                File hocrFile = new File(tmpPrefix + ".hocr");
                File htmlFile = new File(actualPrefix + ".html");
                if (FileTools.rename(hocrFile, htmlFile)) {
                    targetFileGenerated(htmlFile);
                }
            }

            if (ocrOptionsController.pdfCheck.isSelected()) {
                File tmpPdfFile = new File(tmpPrefix + ".pdf");
                File pdfFile = new File(actualPrefix + ".pdf");
                if (FileTools.rename(tmpPdfFile, pdfFile)) {
                    targetFileGenerated(pdfFile);
                }
            }

            if (ocrOptionsController.wordLevel >= 0) {
                List<Word> words = OCRinstance.getWords(lastImage, ocrOptionsController.wordLevel);
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
                File wordsFile = new File(actualPrefix + "_words.html");
                if (TextFileTools.writeFile(wordsFile, html) != null) {
                    targetFileGenerated(wordsFile);
                } else {
                }
            }

            if (ocrOptionsController.regionLevel >= 0) {
                List<Rectangle> rectangles = OCRinstance.getSegmentedRegions(lastImage, ocrOptionsController.regionLevel);
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
                File regionsFile = new File(actualPrefix + "_regions.html");
                if (TextFileTools.writeFile(regionsFile, html) != null) {
                    targetFileGenerated(regionsFile);
                } else {
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    protected boolean command(File srcFile, File targetFile) {
        if (lastImage == null) {
            return false;
        }
        if (process != null) {
            process.destroy();
            process = null;
        }
        try {
            // Looks OCR engine does not support non-English file name
            String actualPrefix = targetFile.getParent() + File.separator
                    + FileNameTools.prefix(targetFile.getName());
            String tmpPrefix = FileTmpTools.getTempFile().getAbsolutePath();

            process = ocrOptionsController.process(srcFile, tmpPrefix);
            if (process == null) {
                return false;
            }
            String outputs = "", line;
            try (BufferedReader inReader = process.inputReader(Charset.defaultCharset())) {
                while ((line = inReader.readLine()) != null) {
                    outputs += line + "\n";
                }
            } catch (Exception e) {
                outputs += e.toString() + "\n";
            }
            process.waitFor();

            File tmpTextFile = new File(tmpPrefix + ".txt");
            if (!tmpTextFile.exists()) {
                updateLogs(message("Failed" + ":" + outputs), true, true);
                return false;
            }
            File textFile = new File(actualPrefix + ".txt");
            FileTools.rename(tmpTextFile, textFile);
            textFiles.add(textFile);
            targetFileGenerated(textFile);

            if (ocrOptionsController.htmlCheck.isSelected()) {
                File hocrFile = new File(tmpPrefix + ".hocr");
                File htmlFile = new File(actualPrefix + ".html");
                if (FileTools.rename(hocrFile, htmlFile)) {
                    targetFileGenerated(htmlFile);
                }
            }

            if (ocrOptionsController.pdfCheck.isSelected()) {
                File tmpPdfFile = new File(tmpPrefix + ".pdf");
                File pdfFile = new File(actualPrefix + ".pdf");
                if (FileTools.rename(tmpPdfFile, pdfFile)) {
                    targetFileGenerated(pdfFile);
                }
            }
            if (process != null) {
                process.destroy();
                process = null;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    @Override
    public File makeTargetFile(File srcFile, File targetPath) {
        try {
            String namePrefix = FileNameTools.prefix(srcFile.getName());
            namePrefix = namePrefix.replace(" ", "_");
            return makeTargetFile(namePrefix, ".txt", targetPath);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public void afterHandleFiles() {
        if (textFiles != null && textFiles.size() > 1 && mergeCheck.isSelected()) {
            File mFile = new File(FileNameTools.append(textFiles.get(0).getAbsolutePath(), "_OCR_merged"));
            if (TextFileTools.mergeTextFiles(task, textFiles, mFile)) {
                targetFileGenerated(mFile);
            }
        }
        OCRinstance = null;
    }

}
