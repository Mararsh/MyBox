package mara.mybox.controller;

import com.recognition.software.jdeskew.ImageDeskew;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.VBox;
import mara.mybox.data.ConvolutionKernel;
import mara.mybox.data.StringTable;
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
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
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

    protected String selectedLanguages;
    protected float scale;
    protected int threshold, rotate, regionLevel, wordLevel;
    protected ImageContrast.ContrastAlgorithm contrastAlgorithm;
    protected BufferedImage lastImage;
    protected ITesseract OCRinstance;
    protected List<File> textFiles;

    @FXML
    protected VBox preprocessVBox, ocrOptionsVBox;
    @FXML
    protected ListView languageList;
    @FXML
    protected ComboBox<String> enhancementSelector, rotateSelector,
            binarySelector, scaleSelector, regionSelector, wordSelector;
    @FXML
    protected CheckBox deskewCheck, invertCheck, htmlCheck, pdfCheck, mergeCheck;
    @FXML
    protected Label currentOCRFilesLabel;

    public ImageOCRBatchController() {
        baseTitle = AppVariables.message("ImageOCRBatch");
        browseTargets = false;
    }

    @Override
    public void initOptionsSection() {
        try {
            initPreprocessBox();
            initOCROptionsBox();

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void initPreprocessBox() {
        try {
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

            enhancementSelector.getItems().addAll(Arrays.asList(
                    message("HSBHistogramEqualization"), message("GrayHistogramEqualization"),
                    message("GrayHistogramStretching"), message("GrayHistogramShifting"),
                    message("UnsharpMasking"),
                    message("FourNeighborLaplace"), message("EightNeighborLaplace"),
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

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void initOCROptionsBox() {
        try {
            languageList.getItems().clear();
            languageList.getItems().addAll(OCRTools.namesList());
            languageList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            languageList.setPrefHeight(200);
            languageList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                    checkLanguages();
                }
            });
            selectedLanguages = AppVariables.getUserConfigValue("ImageOCRLanguages", null);
            if (selectedLanguages != null && !selectedLanguages.isEmpty()) {
                currentOCRFilesLabel.setText(MessageFormat.format(message("CurrentDataFiles"), selectedLanguages));
                currentOCRFilesLabel.setStyle(null);
                isSettingValues = true;
                String[] langs = selectedLanguages.split("\\+");
                Map<String, String> codes = OCRTools.codeName();
                for (String code : langs) {
                    String name = codes.get(code);
                    if (name == null) {
                        name = code;
                    }
                    languageList.getSelectionModel().select(name);
                }
                isSettingValues = false;
            } else {
                currentOCRFilesLabel.setText(MessageFormat.format(message("CurrentDataFiles"), message("NoData")));
                currentOCRFilesLabel.setStyle(badStyle);
            }

            regionLevel = -1;
            regionSelector.getItems().addAll(Arrays.asList(message("None"),
                    message("Block"), message("Paragraph"), message("Textline"),
                    message("Word"), message("Symbol")
            ));
            regionSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (message("Block").equals(newValue)) {
                        regionLevel = TessPageIteratorLevel.RIL_BLOCK;

                    } else if (message("Paragraph").equals(newValue)) {
                        regionLevel = TessPageIteratorLevel.RIL_PARA;

                    } else if (message("Textline").equals(newValue)) {
                        regionLevel = TessPageIteratorLevel.RIL_TEXTLINE;

                    } else if (message("Word").equals(newValue)) {
                        regionLevel = TessPageIteratorLevel.RIL_WORD;

                    } else if (message("Symbol").equals(newValue)) {
                        regionLevel = TessPageIteratorLevel.RIL_SYMBOL;

                    } else {
                        regionLevel = -1;
                    }
                    AppVariables.setUserConfigValue("ImageOCRRegionLevel", newValue);
                }
            });
            regionSelector.getSelectionModel().select(
                    AppVariables.getUserConfigValue("ImageOCRRegionLevel", message("Symbol")));

            wordLevel = -1;
            wordSelector.getItems().addAll(Arrays.asList(message("None"),
                    message("Block"), message("Paragraph"), message("Textline"),
                    message("Word"), message("Symbol")
            ));
            wordSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (message("Block").equals(newValue)) {
                        wordLevel = TessPageIteratorLevel.RIL_BLOCK;

                    } else if (message("Paragraph").equals(newValue)) {
                        wordLevel = TessPageIteratorLevel.RIL_PARA;

                    } else if (message("Textline").equals(newValue)) {
                        wordLevel = TessPageIteratorLevel.RIL_TEXTLINE;

                    } else if (message("Word").equals(newValue)) {
                        wordLevel = TessPageIteratorLevel.RIL_WORD;

                    } else if (message("Symbol").equals(newValue)) {
                        wordLevel = TessPageIteratorLevel.RIL_SYMBOL;

                    } else {
                        wordLevel = -1;
                    }
                    AppVariables.setUserConfigValue("ImageOCRWordLevel", newValue);
                }
            });
            wordSelector.getSelectionModel().select(
                    AppVariables.getUserConfigValue("ImageOCRWordLevel", message("Symbol")));

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void checkLanguages() {
        if (isSettingValues) {
            return;
        }
        List<String> langsList = languageList.getSelectionModel().getSelectedItems();
        selectedLanguages = null;
        Map<String, String> names = OCRTools.nameCode();
        for (String name : langsList) {
            String code = names.get(name);
            if (code == null) {
                code = name;
            }
            if (selectedLanguages == null) {
                selectedLanguages = code;
            } else {
                selectedLanguages += "+" + code;
            }
        }
        if (selectedLanguages != null) {
            AppVariables.setUserConfigValue("ImageOCRLanguages", selectedLanguages);
            currentOCRFilesLabel.setText(MessageFormat.format(message("CurrentDataFiles"), selectedLanguages));
            currentOCRFilesLabel.setStyle(null);
        } else {
            currentOCRFilesLabel.setText(MessageFormat.format(message("CurrentDataFiles"), message("NoData")));
            currentOCRFilesLabel.setStyle(badStyle);
        }
    }

    @FXML
    public void upAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(languageList.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        isSettingValues = true;
        List<Integer> newselected = new ArrayList<>();
        for (Integer index : selected) {
            if (index == 0 || newselected.contains(index - 1)) {
                newselected.add(index);
                continue;
            }
            String lang = (String) languageList.getItems().get(index);
            languageList.getItems().set(index, languageList.getItems().get(index - 1));
            languageList.getItems().set(index - 1, lang);
            newselected.add(index - 1);
        }
        languageList.getSelectionModel().clearSelection();
        for (int index : newselected) {
            languageList.getSelectionModel().select(index);
        }
        languageList.refresh();
        isSettingValues = false;
        checkLanguages();
    }

    @FXML
    public void topAction() {
        List<Integer> selectedIndices = new ArrayList<>();
        selectedIndices.addAll(languageList.getSelectionModel().getSelectedIndices());
        if (selectedIndices.isEmpty()) {
            return;
        }
        List<String> selected = new ArrayList<>();
        selected.addAll(languageList.getSelectionModel().getSelectedItems());
        isSettingValues = true;
        int size = selectedIndices.size();
        for (int i = size - 1; i >= 0; --i) {
            int index = selectedIndices.get(i);
            languageList.getItems().remove(index);
        }
        languageList.getSelectionModel().clearSelection();
        languageList.getItems().addAll(0, selected);
        languageList.getSelectionModel().selectRange(0, size);
        languageList.refresh();
        isSettingValues = false;
        checkLanguages();
    }

    @FXML
    public void downAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(languageList.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        isSettingValues = true;
        List<Integer> newselected = new ArrayList<>();
        for (int i = selected.size() - 1; i >= 0; --i) {
            int index = selected.get(i);
            if (index == languageList.getItems().size() - 1
                    || newselected.contains(index + 1)) {
                newselected.add(index);
                continue;
            }
            String lang = (String) languageList.getItems().get(index);
            languageList.getItems().set(index, languageList.getItems().get(index + 1));
            languageList.getItems().set(index + 1, lang);
            newselected.add(index + 1);
        }
        languageList.getSelectionModel().clearSelection();
        for (int index : newselected) {
            languageList.getSelectionModel().select(index);
        }
        languageList.refresh();
        isSettingValues = false;
        checkLanguages();
    }

    @FXML
    public void settingsAction() {
        SettingsController controller = (SettingsController) openStage(CommonValues.SettingsFxml);
        controller.setParentController(this);
        controller.setParentFxml(myFxml);
        controller.tabPane.getSelectionModel().select(controller.ocrTab);
    }

    /*
        Batch
     */
    @Override
    public boolean makeActualParameters() {
        if (!super.makeActualParameters()) {
            return false;
        }

        try {
            OCRinstance = new Tesseract();
            // https://stackoverflow.com/questions/58286373/tess4j-pdf-to-tiff-to-tesseract-warning-invalid-resolution-0-dpi-using-70/58296472#58296472
            OCRinstance.setTessVariable("user_defined_dpi", "96");
            OCRinstance.setTessVariable("debug_file", "/dev/null");
            String path = AppVariables.getUserConfigValue("TessDataPath", null);
            if (path != null) {
                OCRinstance.setDatapath(path);
            }
            if (selectedLanguages != null) {
                OCRinstance.setLanguage(selectedLanguages);
            }
            textFiles = new ArrayList<>();
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
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
            countHandling(srcFile);
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return AppVariables.message("Skip");
            }

            lastImage = ImageFileReaders.readImage(srcFile);
            if (lastImage == null) {
                return AppVariables.message("Failed");
            }

            if (threshold > 0) {
                ImageBinary bin = new ImageBinary(lastImage, threshold);
                lastImage = bin.operateImage();
            }

            if (rotate != 0) {
                lastImage = ImageManufacture.rotateImage(lastImage, rotate);
            }
            if (scale > 0 && scale != 1) {
                lastImage = ImageManufacture.scaleImage(lastImage, scale);
            }

            String enhance = enhancementSelector.getValue();
            if (enhance == null || enhance.trim().isEmpty()) {
            } else if (message("GrayHistogramEqualization").equals(enhance)) {
                ImageContrast imageContrast = new ImageContrast(lastImage,
                        ImageContrast.ContrastAlgorithm.Gray_Histogram_Equalization);
                lastImage = imageContrast.operateImage();

            } else if (message("GrayHistogramStretching").equals(enhance)) {
                ImageContrast imageContrast = new ImageContrast(lastImage,
                        ImageContrast.ContrastAlgorithm.Gray_Histogram_Stretching);
                imageContrast.setIntPara1(100);
                imageContrast.setIntPara2(100);
                lastImage = imageContrast.operateImage();

            } else if (message("GrayHistogramShifting").equals(enhance)) {
                ImageContrast imageContrast = new ImageContrast(lastImage,
                        ImageContrast.ContrastAlgorithm.Gray_Histogram_Shifting);
                imageContrast.setIntPara1(80);
                lastImage = imageContrast.operateImage();

            } else if (message("HSBHistogramEqualization").equals(enhance)) {
                ImageContrast imageContrast = new ImageContrast(lastImage,
                        ImageContrast.ContrastAlgorithm.HSB_Histogram_Equalization);
                lastImage = imageContrast.operateImage();

            } else if (message("UnsharpMasking").equals(enhance)) {
                ConvolutionKernel kernel = ConvolutionKernel.makeUnsharpMasking(3);
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.operateImage();

            } else if (message("FourNeighborLaplace").equals(enhance)) {
                ConvolutionKernel kernel = ConvolutionKernel.MakeSharpenFourNeighborLaplace();
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.operateImage();

            } else if (message("EightNeighborLaplace").equals(enhance)) {
                ConvolutionKernel kernel = ConvolutionKernel.MakeSharpenEightNeighborLaplace();
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.operateImage();

            } else if (message("GaussianBlur").equals(enhance)) {
                ConvolutionKernel kernel = ConvolutionKernel.makeGaussBlur(3);
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.operateImage();

            } else if (message("AverageBlur").equals(enhance)) {
                ConvolutionKernel kernel = ConvolutionKernel.makeAverageBlur(1);
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

            List<ITesseract.RenderedFormat> formats = new ArrayList<>();
            formats.add(ITesseract.RenderedFormat.TEXT);
            if (htmlCheck.isSelected()) {
                formats.add(ITesseract.RenderedFormat.HOCR);
            }
            if (pdfCheck.isSelected()) {
                formats.add(ITesseract.RenderedFormat.PDF);
            }
            String prefix = FileTools.getFilePrefix(target.getAbsolutePath());

            OCRinstance.createDocumentsWithResults​(lastImage, null,
                    prefix, formats, TessPageIteratorLevel.RIL_SYMBOL);
            File textFile = new File(prefix + ".txt");
            textFiles.add(textFile);
            targetFileGenerated(textFile);

            if (htmlCheck.isSelected()) {
                File hocrFile = new File(prefix + ".hocr");
                File htmlFile = new File(prefix + ".html");
                if (htmlFile.exists()) {
                    htmlFile.delete();
                }
                hocrFile.renameTo(htmlFile);
                targetFileGenerated(htmlFile);
            }

            if (pdfCheck.isSelected()) {
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
            return AppVariables.message("Successful");
        } catch (Exception e) {
            logger.error(e.toString());
            return AppVariables.message("Failed");
        }
    }

    @Override
    public File makeTargetFile(File srcFile, File targetPath) {
        try {
            String namePrefix = FileTools.getFilePrefix(srcFile.getName());
            namePrefix = namePrefix.replace(" ", "_");
            return makeTargetFile(namePrefix, ".txt", targetPath);
        } catch (Exception e) {
            logger.error(e.toString());
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
