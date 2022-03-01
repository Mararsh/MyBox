package mara.mybox.controller;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import mara.mybox.bufferedimage.AlphaTools;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.FileFilters;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;

/**
 * @Author Mara
 * @CreateDate 2019-9-17
 * @License Apache License Version 2.0
 */
/*
https://github.com/nguyenq/tess4j
https://github.com/tesseract-ocr/tesseract/wiki/Data-Files
Images intended for OCR should have at least 200 DPI in resolution, typically 300 DPI, 1 bpp (bit per pixel) monochome
or 8 bpp grayscale uncompressed TIFF or PNG format.
PNG is usually smaller in size than other image formats and still keeps high quality due to its employing lossless data compression algorithms;
TIFF has the advantage of the ability to contain multiple images (pages) in a file.
 */
public class ImageOCRController extends ImageViewerController {

    protected float scale;
    protected int threshold, rotate;
    protected LoadingController loading;
    protected Process process;

    @FXML
    protected VBox originalImageBox, imagesBox;
    @FXML
    protected ScrollPane imagePane, processPane;
    @FXML
    protected TextArea textArea;
    @FXML
    protected Label resultLabel;
    @FXML
    protected CheckBox startCheck, LoadCheck;
    @FXML
    protected HtmlTableController regionsTableController, wordsTableController, htmlController;
    @FXML
    protected ControlOCROptions ocrOptionsController;
    @FXML
    protected ImageOCRProcessController preprocessController;
    @FXML
    protected TabPane ocrTabPane;
    @FXML
    protected Tab txtTab, htmlTab, regionsTab, wordsTab;
    @FXML
    protected Tab ocrOptionsTab;

    public ImageOCRController() {
        baseTitle = Languages.message("ImageOCR");
        TipsLabelKey = "OCRPreprocessComment";

        TargetPathType = VisitHistory.FileType.Text;
        TargetFileType = VisitHistory.FileType.Text;
        targetExtensionFilter = FileFilters.TextExtensionFilter;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            preprocessController.OCRController = this;
            ocrOptionsController.setParameters(this, false, true);

            originalImageBox.disableProperty().bind(imageView.imageProperty().isNull());
            processPane.disableProperty().bind(imageView.imageProperty().isNull());
            rightPane.disableProperty().bind(imageView.imageProperty().isNull());

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public boolean menuAction() {
        if (processPane.isFocused() || preprocessController.scrollPane.isFocused() || preprocessController.imageView.isFocused()) {
            preprocessController.menuAction();
            return true;

        } else if (imagePane.isFocused() || originalImageBox.isFocused() || scrollPane.isFocused() || imageView.isFocused()) {
            return super.menuAction();

        } else if (rightPane.isFocused() || ocrTabPane.isFocused()) {

            if (txtTab.isSelected()) {
                Point2D localToScreen = textArea.localToScreen(textArea.getWidth() - 80, 80);
                MenuTextEditController.open(myController, textArea, localToScreen.getX(), localToScreen.getY());
                return true;

            } else if (htmlTab.isSelected()) {
                htmlController.menuAction();
                return true;

            } else if (regionsTab.isSelected()) {
                regionsTableController.menuAction();
                return true;

            } else if (wordsTab.isSelected()) {
                wordsTableController.menuAction();
                return true;
            }
        }
        return super.menuAction();
    }

    @FXML
    @Override
    public boolean popAction() {
        if (processPane.isFocused() || preprocessController.scrollPane.isFocused() || preprocessController.imageView.isFocused()) {
            preprocessController.popAction();
            return true;

        } else if (imagePane.isFocused() || originalImageBox.isFocused() || scrollPane.isFocused() || imageView.isFocused()) {
            return super.popAction();

        } else if (rightPane.isFocused() || ocrTabPane.isFocused()) {

            if (txtTab.isSelected()) {
                TextPopController.openInput(this, textArea);
                return true;

            } else if (htmlTab.isSelected()) {
                htmlController.popAction();
                return true;

            } else if (regionsTab.isSelected()) {
                regionsTableController.popAction();
                return true;

            } else if (wordsTab.isSelected()) {
                wordsTableController.popAction();
                return true;
            }
        }
        return super.popAction();
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded()) {
                return false;
            }

            String name = sourceFile != null ? FileNameTools.prefix(sourceFile.getName()) : "";
            regionsTableController.baseTitle = name + "_regions";
            wordsTableController.baseTitle = name + "_words";
            htmlController.baseTitle = name + "_texts";

            preprocessController.recoverAction();

            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    /*
        OCR
     */
    @FXML
    @Override
    public void startAction() {
        ocrOptionsController.setLanguages();
        File dataPath = ocrOptionsController.dataPathController.file;
        if (!dataPath.exists()) {
            popError(Languages.message("InvalidParameters"));
            ocrOptionsController.dataPathController.fileInput.setStyle(UserConfig.badStyle());
            return;
        }
        if (ocrOptionsController.embedRadio.isSelected()) {
            embedded();
        } else {
            command();
        }
    }

    protected void command() {
        if (preprocessController.imageView.getImage() == null || timer != null || process != null
                || ocrOptionsController.dataPathController.file == null) {
            return;
        }
        File tesseract = ocrOptionsController.tesseractPathController.file;
        if (!tesseract.exists()) {
            popError(Languages.message("InvalidParameters"));
            ocrOptionsController.tesseractPathController.fileInput.setStyle(UserConfig.badStyle());
            return;
        }
        loading = handling();
        new Thread() {
            private String outputs = "";

            @Override
            public void run() {
                try {
                    Image selected = preprocessController.scopeImage();
                    if (selected == null) {
                        selected = preprocessController.imageView.getImage();
                    }
                    String imageFile = TmpFileTools.getTempFile(".png").getAbsolutePath();
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(selected, null);
                    bufferedImage = AlphaTools.removeAlpha(bufferedImage);
                    ImageFileWriters.writeImageFile(bufferedImage, "png", imageFile);

                    int version = ocrOptionsController.tesseractVersion();
                    String fileBase = TmpFileTools.getTempFile().getAbsolutePath();
                    List<String> parameters = new ArrayList<>();
                    parameters.addAll(Arrays.asList(
                            tesseract.getAbsolutePath(),
                            imageFile, fileBase,
                            "--tessdata-dir", ocrOptionsController.dataPathController.file.getAbsolutePath(),
                            version > 3 ? "--psm" : "-psm", ocrOptionsController.psm + ""
                    ));
                    if (ocrOptionsController.selectedLanguages != null) {
                        parameters.addAll(Arrays.asList("-l", ocrOptionsController.selectedLanguages));
                    }
                    File configFile = TmpFileTools.getTempFile();
                    String s = "tessedit_create_txt 1\n"
                            + "tessedit_create_hocr 1\n";
                    Map<String, String> p = ocrOptionsController.checkParameters();
                    if (p != null) {
                        for (String key : p.keySet()) {
                            s += key + "\t" + p.get(key) + "\n";
                        }
                    }
                    TextFileTools.writeFile(configFile, s, Charset.forName("utf-8"));
                    parameters.add(configFile.getAbsolutePath());

                    ProcessBuilder pb = new ProcessBuilder(parameters).redirectErrorStream(true);
                    long startTime = new Date().getTime();
                    process = pb.start();
                    try ( BufferedReader inReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = inReader.readLine()) != null) {
                            outputs += line + "\n";
                        }
                    } catch (Exception e) {
                        outputs += e.toString() + "\n";
                    }
                    process.waitFor();

                    String texts;
                    File txtFile = new File(fileBase + ".txt");
                    if (txtFile.exists()) {
                        texts = TextFileTools.readTexts(txtFile);
                        FileDeleteTools.delete(txtFile);
                    } else {
                        texts = null;
                    }
                    String html;
                    File htmlFile = new File(fileBase + ".hocr");
                    if (htmlFile.exists()) {
                        html = TextFileTools.readTexts(htmlFile);
                        FileDeleteTools.delete(htmlFile);
                    } else {
                        html = null;
                    }
                    if (process != null) {
                        process.destroy();
                        process = null;
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (loading != null) {
                                loading.closeStage();
                                loading = null;
                            }
                            if (texts != null) {
                                textArea.setText(texts);
                                resultLabel.setText(MessageFormat.format(Languages.message("OCRresults"),
                                        texts.length(), DateTools.datetimeMsDuration(new Date().getTime() - startTime)));
                                ocrTabPane.getSelectionModel().select(txtTab);
                            } else {
                                if (outputs != null && !outputs.isBlank()) {
                                    alertError(outputs);
                                } else {
                                    popFailed();
                                }
                            }
                            if (html != null) {
                                htmlController.loadHtml(html);
                            }
                        }
                    });

                } catch (Exception e) {
                    MyBoxLog.debug(e.toString());
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (loading != null) {
                                loading.closeStage();
                                loading = null;
                            }
                        }
                    });
                }
            }
        }.start();
    }

    protected void embedded() {
        if (preprocessController.imageView.getImage() == null
                || ocrOptionsController.dataPathController.file == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                private String texts, html;
                private List<Rectangle> rectangles;
                private List<Word> words;

                @Override
                protected boolean handle() {
                    try {
                        ITesseract instance = new Tesseract();
                        // https://stackoverflow.com/questions/58286373/tess4j-pdf-to-tiff-to-tesseract-warning-invalid-resolution-0-dpi-using-70/58296472#58296472
                        instance.setTessVariable("user_defined_dpi", "96");
                        instance.setTessVariable("debug_file", "/dev/null");
                        instance.setPageSegMode(ocrOptionsController.psm);
                        Map<String, String> p = ocrOptionsController.checkParameters();
                        if (p != null && !p.isEmpty()) {
                            for (String key : p.keySet()) {
                                instance.setTessVariable(key, p.get(key));
                            }
                        }
                        instance.setDatapath(ocrOptionsController.dataPathController.file.getAbsolutePath());
                        if (ocrOptionsController.selectedLanguages != null) {
                            instance.setLanguage(ocrOptionsController.selectedLanguages);
                        }
                        Image selected = preprocessController.scopeImage();
                        if (selected == null) {
                            selected = preprocessController.imageView.getImage();
                        }

                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(selected, null);
                        bufferedImage = AlphaTools.removeAlpha(bufferedImage);
                        if (task == null || isCancelled() || bufferedImage == null) {
                            return false;
                        }

                        List<ITesseract.RenderedFormat> formats = new ArrayList<>();
                        formats.add(ITesseract.RenderedFormat.TEXT);
                        formats.add(ITesseract.RenderedFormat.HOCR);

                        File tmpFile = File.createTempFile("MyboxOCR", "");
                        String tmp = File.createTempFile("MyboxOCR", "").getAbsolutePath();
                        FileDeleteTools.delete(tmpFile);
                        instance.createDocumentsWithResults​(bufferedImage, null,
                                tmp, formats, ITessAPI.TessPageIteratorLevel.RIL_SYMBOL);
                        File txtFile = new File(tmp + ".txt");
                        texts = TextFileTools.readTexts(txtFile);
                        FileDeleteTools.delete(txtFile);

                        File htmlFile = new File(tmp + ".hocr");
                        html = TextFileTools.readTexts(htmlFile);
                        FileDeleteTools.delete(htmlFile);

                        if (ocrOptionsController.wordLevel >= 0) {
                            words = instance.getWords(bufferedImage, ocrOptionsController.wordLevel);
                        }

                        if (ocrOptionsController.regionLevel >= 0) {
                            rectangles = instance.getSegmentedRegions(bufferedImage, ocrOptionsController.regionLevel);
                        }

                        return texts != null;
                    } catch (Exception e) {
                        error = e.toString();
                        MyBoxLog.debug(e.toString());
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (texts.length() == 0) {
                        popWarn(Languages.message("OCRMissComments"));
                    }
                    textArea.setText(texts);
                    resultLabel.setText(MessageFormat.format(Languages.message("OCRresults"),
                            texts.length(), DateTools.datetimeMsDuration(cost)));
                    ocrTabPane.getSelectionModel().select(txtTab);

                    htmlController.loadHtml(html);

                    if (rectangles != null) {
                        List<String> names = new ArrayList<>();
                        names.addAll(Arrays.asList(Languages.message("Index"),
                                Languages.message("CoordinateX"), Languages.message("CoordinateY"),
                                Languages.message("Width"), Languages.message("Height")
                        ));
                        regionsTableController.initTable(Languages.message(""), names);
                        for (int i = 0; i < rectangles.size(); ++i) {
                            Rectangle rect = rectangles.get(i);
                            List<String> data = new ArrayList<>();
                            data.addAll(Arrays.asList(
                                    i + "", rect.x + "", rect.y + "", rect.width + "", rect.height + ""
                            ));
                            regionsTableController.addData(data);
                        }
                        regionsTableController.displayHtml();
                    } else {
                        regionsTableController.clear();
                    }

                    if (words != null) {
                        List<String> names = new ArrayList<>();
                        names.addAll(Arrays.asList(Languages.message("Index"),
                                Languages.message("Contents"), Languages.message("Confidence"),
                                Languages.message("CoordinateX"), Languages.message("CoordinateY"),
                                Languages.message("Width"), Languages.message("Height")
                        ));
                        wordsTableController.initTable(Languages.message(""), names);
                        for (int i = 0; i < words.size(); ++i) {
                            Word word = words.get(i);
                            Rectangle rect = word.getBoundingBox();
                            List<String> data = new ArrayList<>();
                            data.addAll(Arrays.asList(
                                    i + "", word.getText(), word.getConfidence() + "",
                                    rect.x + "", rect.y + "", rect.width + "", rect.height + ""
                            ));
                            wordsTableController.addData(data);
                            wordsTableController.displayHtml();
                        }
                    } else {
                        wordsTableController.clear();
                    }

                }

            };
            start(task);
        }

    }

    @Override
    public void cleanPane() {
        try {
            if (process != null) {
                process.destroy();
                process = null;
            }
            if (loading != null) {
                loading.closeStage();
                loading = null;
            }
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
