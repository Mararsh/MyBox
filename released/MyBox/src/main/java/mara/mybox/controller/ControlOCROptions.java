package mara.mybox.controller;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.bufferedimage.AlphaTools;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.OCRTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;

/**
 * @Author Mara
 * @CreateDate 2020-10-6
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
public class ControlOCROptions extends BaseController {

    protected String selectedLanguages, texts, html;
    protected List<Rectangle> rectangles;
    protected List<Word> words;
    protected int psm, regionLevel, wordLevel, tesseractVersion;
    protected boolean setFormats, setLevels, isVersion3;
    protected File configFile;

    @FXML
    protected ControlFileSelecter tesseractPathController, dataPathController;
    @FXML
    protected VBox optionsBox, levelsBox;
    @FXML
    protected HBox outputsBox;
    @FXML
    protected TextArea textArea;
    @FXML
    protected Label resultLabel, originalViewLabel, currentOCRFilesLabel;
    @FXML
    protected ControlSelection languagesController;
    @FXML
    protected ComboBox<String> psmSelector, regionSelector, wordSelector;
    @FXML
    protected CheckBox htmlCheck, pdfCheck;
    @FXML
    protected ToggleGroup engineGroup;
    @FXML
    protected RadioButton embedRadio, tesseractRadio;
    @FXML
    protected TextArea optionsArea;
    @FXML
    protected Button helpMeButton;

    public ControlOCROptions() {
        baseTitle = message("ImageOCR");
        TipsLabelKey = "ImageOCRComments";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            String os = SystemTools.os();
            tesseractPathController.type(VisitHistory.FileType.All)
                    .isDirectory(false).mustExist(true).permitNull(true)
                    .defaultFile("win".equals(os) ? new File("D:\\Programs\\Tesseract-OCR\\tesseract.exe") : new File("/bin/tesseract"))
                    .parent(this, "TesseractPath");

            dataPathController.isDirectory(true).mustExist(true).permitNull(false)
                    .defaultFile("win".equals(os) ? new File("D:\\Programs\\Tesseract-OCR\\tessdata") : new File("/usr/local/share/tessdata/"))
                    .parent(this, OCRTools.TessDataPath);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            String os = SystemTools.os();

            if ("win".equals(os)) {
                initWin();
            } else {
                embedRadio.setDisable(true);
                tesseractRadio.setSelected(true);
            }
            engineGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle oldVal, Toggle newVal) {
                    checkEngine();
                    UserConfig.setBoolean("ImageOCREmbed", embedRadio.isSelected());
                }
            });

            languagesController.setParameters(this, message("Language"), "");
            languagesController.selectedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldV, Boolean newV) {
                    checkLanguages();
                }
            });

            psm = 6;
            psmSelector.getItems().addAll(Arrays.asList(
                    "0    " + message("PSM0"),
                    "1    " + message("PSM1"),
                    "2    " + message("PSM2"),
                    "3    " + message("PSM3"),
                    "4    " + message("PSM4"),
                    "5    " + message("PSM5"),
                    "6    " + message("PSM6"),
                    "7    " + message("PSM7"),
                    "8    " + message("PSM8"),
                    "9    " + message("PSM9"),
                    "10    " + message("PSM10"),
                    "11    " + message("PSM11"),
                    "12    " + message("PSM12"),
                    "13    " + message("PSM13")
            ));
            psmSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    String[] vs = newValue.split("    ");
                    psm = Integer.parseInt(vs[0]);
                    UserConfig.setInt("ImageOCRPSM", psm);
                }
            });
            psmSelector.getSelectionModel().select(UserConfig.getInt("ImageOCRPSM", 6));

            htmlCheck.setSelected(UserConfig.getBoolean("ImageOCRhtml", false));
            htmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean("ImageOCRhtml", htmlCheck.isSelected());
                }
            });

            pdfCheck.setSelected(UserConfig.getBoolean("ImageOCRpdf", false));
            pdfCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean("ImageOCRpdf", pdfCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void initWin() {
        embedRadio.setDisable(false);
        if (UserConfig.getBoolean("ImageOCREmbed", true)) {
            embedRadio.setSelected(true);
        } else {
            tesseractRadio.setSelected(true);
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
                    regionLevel = ITessAPI.TessPageIteratorLevel.RIL_BLOCK;

                } else if (message("Paragraph").equals(newValue)) {
                    regionLevel = ITessAPI.TessPageIteratorLevel.RIL_PARA;

                } else if (message("Textline").equals(newValue)) {
                    regionLevel = ITessAPI.TessPageIteratorLevel.RIL_TEXTLINE;

                } else if (message("Word").equals(newValue)) {
                    regionLevel = ITessAPI.TessPageIteratorLevel.RIL_WORD;

                } else if (message("Symbol").equals(newValue)) {
                    regionLevel = ITessAPI.TessPageIteratorLevel.RIL_SYMBOL;

                } else {
                    regionLevel = -1;
                }
                UserConfig.setString("ImageOCRRegionLevel", newValue);
            }
        });
        regionSelector.getSelectionModel().select(UserConfig.getString("ImageOCRRegionLevel", message("Symbol")));

        wordLevel = -1;
        wordSelector.getItems().addAll(Arrays.asList(message("None"),
                message("Block"), message("Paragraph"), message("Textline"),
                message("Word"), message("Symbol")
        ));
        wordSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                if (message("Block").equals(newValue)) {
                    wordLevel = ITessAPI.TessPageIteratorLevel.RIL_BLOCK;

                } else if (message("Paragraph").equals(newValue)) {
                    wordLevel = ITessAPI.TessPageIteratorLevel.RIL_PARA;

                } else if (message("Textline").equals(newValue)) {
                    wordLevel = ITessAPI.TessPageIteratorLevel.RIL_TEXTLINE;

                } else if (message("Word").equals(newValue)) {
                    wordLevel = ITessAPI.TessPageIteratorLevel.RIL_WORD;

                } else if (message("Symbol").equals(newValue)) {
                    wordLevel = ITessAPI.TessPageIteratorLevel.RIL_SYMBOL;

                } else {
                    wordLevel = -1;
                }
                UserConfig.setString("ImageOCRWordLevel", newValue);
            }
        });
        wordSelector.getSelectionModel().select(UserConfig.getString("ImageOCRWordLevel", message("Symbol")));
    }

    public void setLanguages() {
        try {
            List<String> langs = OCRTools.namesList(tesseractVersion > 3);
            languagesController.loadNames(langs);

            selectedLanguages = UserConfig.getString("ImageOCRLanguages", null);
            if (selectedLanguages != null && !selectedLanguages.isEmpty()) {
                currentOCRFilesLabel.setText(MessageFormat.format(message("CurrentDataFiles"), selectedLanguages));
                currentOCRFilesLabel.setStyle(null);
                isSettingValues = true;
                String[] selected = selectedLanguages.split("\\+");
                Map<String, String> codes = OCRTools.codeName();
                List<String> selectedNames = new ArrayList<>();
                for (String code : selected) {
                    String name = codes.get(code);
                    if (name == null) {
                        name = code;
                    }
                    selectedNames.add(name);
                }
                languagesController.selectNames(selectedNames);
                isSettingValues = false;
            } else {
                currentOCRFilesLabel.setText(MessageFormat.format(message("CurrentDataFiles"), message("SelectToHandle")));
                currentOCRFilesLabel.setStyle(UserConfig.badStyle());
            }

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void setParameters(BaseController parent, boolean needFormats, boolean needLevels) {
        try {
            parentController = parent;
            baseTitle = parent.baseTitle;
            baseName = parent.baseName;
            this.setFormats = needFormats;
            this.setLevels = needLevels;
            if (needFormats) {
                if (!optionsBox.getChildren().contains(outputsBox)) {
                    optionsBox.getChildren().add(outputsBox);
                }
            } else {
                if (optionsBox.getChildren().contains(outputsBox)) {
                    optionsBox.getChildren().remove(outputsBox);
                }
            }
            checkEngine();
            setLanguages();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void checkEngine() {
        try {
            if (setLevels && embedRadio.isSelected()) {
                if (!optionsBox.getChildren().contains(levelsBox)) {
                    optionsBox.getChildren().add(levelsBox);
                }
            } else {
                if (optionsBox.getChildren().contains(levelsBox)) {
                    optionsBox.getChildren().remove(levelsBox);
                }
            }
            if (tesseractRadio.isSelected()) {
                if (parentController != null && parentController instanceof ImageOCRController) {
                    ImageOCRController ocrController = (ImageOCRController) parentController;
                    if (ocrController.resultsTabPane.getTabs().contains(ocrController.regionsTab)) {
                        ocrController.resultsTabPane.getTabs().removeAll(ocrController.regionsTab, ocrController.wordsTab);
                    }
                }
                tesseractPathController.thisPane.setDisable(false);
                tesseractPathController.pickFile();
                tesseractVersion = 4;
            } else {
                if (parentController != null && parentController instanceof ImageOCRController) {
                    ImageOCRController ocrController = (ImageOCRController) parentController;
                    if (!ocrController.resultsTabPane.getTabs().contains(ocrController.regionsTab)) {
                        ocrController.resultsTabPane.getTabs().addAll(ocrController.regionsTab, ocrController.wordsTab);
                    }
                }
                tesseractPathController.thisPane.setDisable(true);
                tesseractVersion = tesseractVersion();
            }
            dataPathController.pickFile();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public int tesseractVersion() {
        try {
            File tesseractPath = tesseractPathController.pickFile();
            if (tesseractPath == null || !tesseractPath.exists()) {
                popError(message("InvalidParameters"));
                return -1;
            }
            List<String> parameters = new ArrayList<>();
            parameters.addAll(Arrays.asList(tesseractPath.getAbsolutePath(), "--version"));
            ProcessBuilder pb = new ProcessBuilder(parameters).redirectErrorStream(true);
            Process process = pb.start();
            try (BufferedReader inReader = process.inputReader(Charset.defaultCharset())) {
                String line;
                while ((line = inReader.readLine()) != null) {
                    if (line.contains("tesseract v4.") || line.contains("tesseract 4.")) {
                        return 4;
                    }
                    if (line.contains("tesseract v5.") || line.contains("tesseract 5.")) {
                        return 5;
                    }
                    if (line.contains("tesseract v3.") || line.contains("tesseract 3.")) {
                        return 3;
                    }
                    if (line.contains("tesseract v2.") || line.contains("tesseract 2.")) {
                        return 2;
                    }
                }
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            process.waitFor();
        } catch (Exception e) {
            popError(e.toString());
            MyBoxLog.debug(e);
        }
        return -1;
    }

    public void checkLanguages() {
        if (isSettingValues) {
            return;
        }
        try {
            selectedLanguages = null;
            List<String> langsList = languagesController.selectedNames();
            if (langsList != null) {
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
                UserConfig.setString("ImageOCRLanguages", selectedLanguages);
                currentOCRFilesLabel.setText(MessageFormat.format(message("CurrentDataFiles"), selectedLanguages));
                currentOCRFilesLabel.setStyle(null);
            } else {
                UserConfig.setString("ImageOCRLanguages", null);
                currentOCRFilesLabel.setText(MessageFormat.format(message("CurrentDataFiles"), message("SelectToHandle")));
                currentOCRFilesLabel.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public Map<String, String> checkParameters() {
        String options = optionsArea.getText();
        if (options.isBlank()) {
            return null;
        }
        Map<String, String> p = new HashMap<>();
        String[] lines = options.split("\n");
        for (String line : lines) {
            String[] fields = line.split("\t");
            if (fields.length < 2) {
                continue;
            }
            p.put(fields[0].trim(), fields[1].trim());
        }
        return p;
    }

    @FXML
    public void download() {
        openLink("https://tesseract-ocr.github.io/tessdoc/Home.html");
    }

    @FXML
    @Override
    public void refreshAction() {
        setLanguages();
    }

    @FXML
    public void aboutTesseract() {
        try {
            StringTable table = new StringTable(null, message("AboutTesseract"));
            table.newLinkRow(message("Home"), "https://github.com/tesseract-ocr/tesseract");
            table.newLinkRow(message("Installation"), "https://tesseract-ocr.github.io/tessdoc/Home.html");
            table.newLinkRow(message("InstallationOnWindows"), "https://github.com/UB-Mannheim/tesseract/wiki");
            table.newLinkRow(message("DataFiles"), "https://tesseract-ocr.github.io/tessdoc/Data-Files.html");
            table.newLinkRow(message("Documents"), "https://github.com/tesseract-ocr/tessdoc");
            table.newLinkRow(message("ImproveQuality"), "https://tesseract-ocr.github.io/tessdoc/ImproveQuality.html");

            File htmFile = HtmlWriteTools.writeHtml(table.html());
            openHtml(htmFile);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void helpMe() {
        File help = FxFileTools.getInternalFile(
                "/data/tessdata/tesseract-parameters.txt", "doc", "tesseract-parameters.txt");
        TextEditorController controller = (TextEditorController) openStage(Fxmls.TextEditorFxml);
        controller.hideLeftPane();
        controller.hideRightPane();
        controller.sourceFileChanged(help);
    }

    public void clearResults() {
        texts = null;
        html = null;
        rectangles = null;
        words = null;
    }

    public boolean imageOCR(FxTask currentTask, Image image, boolean allData) {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        bufferedImage = AlphaTools.removeAlpha(currentTask, bufferedImage);
        return bufferedImageOCR(currentTask, bufferedImage, allData);
    }

    public boolean bufferedImageOCR(FxTask currentTask, BufferedImage bufferedImage, boolean allData) {
        try {
            clearResults();
            if (bufferedImage == null || (currentTask != null && !currentTask.isWorking())) {
                return false;
            }
            Tesseract instance = tesseract();
            List<ITesseract.RenderedFormat> formats = new ArrayList<>();
            formats.add(ITesseract.RenderedFormat.TEXT);
            if (allData) {
                formats.add(ITesseract.RenderedFormat.HOCR);
            }

            File tmpFile = File.createTempFile("MyboxOCR", "");
            String tmp = tmpFile.getAbsolutePath();
            FileDeleteTools.delete(tmpFile);

            instance.createDocumentsWithResults​(bufferedImage, tmp,
                    tmp, formats, ITessAPI.TessPageIteratorLevel.RIL_SYMBOL);
            File txtFile = new File(tmp + ".txt");
            texts = TextFileTools.readTexts(currentTask, txtFile);
            FileDeleteTools.delete(txtFile);
            if (texts == null || (currentTask != null && !currentTask.isWorking())) {
                return false;
            }
            if (allData) {
                File htmlFile = new File(tmp + ".hocr");
                html = TextFileTools.readTexts(currentTask, htmlFile);
                FileDeleteTools.delete(htmlFile);
                if (html == null || (currentTask != null && !currentTask.isWorking())) {
                    return false;
                }

                if (wordLevel >= 0) {
                    words = instance.getWords(bufferedImage, wordLevel);
                }

                if (regionLevel >= 0) {
                    rectangles = instance.getSegmentedRegions(bufferedImage, regionLevel);
                }
            }

            return texts != null;
        } catch (Exception e) {
            error = e.toString();
            MyBoxLog.debug(e);
            return false;
        }
    }

    public Tesseract tesseract() {
        try {
            Tesseract instance = new Tesseract();
            // https://stackoverflow.com/questions/58286373/tess4j-pdf-to-tiff-to-tesseract-warning-invalid-resolution-0-dpi-using-70/58296472#58296472
            instance.setVariable("user_defined_dpi", "96");
            instance.setVariable("debug_file", "/dev/null");
            instance.setPageSegMode(psm);
            Map<String, String> p = checkParameters();
            if (p != null && !p.isEmpty()) {
                for (String key : p.keySet()) {
                    instance.setVariable(key, p.get(key));
                }
            }
            instance.setDatapath(dataPathController.pickFile().getAbsolutePath());
            if (selectedLanguages != null) {
                instance.setLanguage(selectedLanguages);
            }
            return instance;
        } catch (Exception e) {
            error = e.toString();
            MyBoxLog.debug(e);
            return null;
        }
    }

    public boolean checkCommandPamameters(boolean html, boolean pdf) {
        try {
            File tesseract = tesseractPathController.pickFile();
            if (!tesseract.exists()) {
                popError(message("InvalidParameters"));
                return false;
            }
            File dataPath = dataPathController.pickFile();
            if (!dataPath.exists()) {
                popError(message("InvalidParameters"));
                return false;
            }

            tesseractVersion = tesseractVersion();
            if (tesseractVersion < 0) {
                return false;
            }
            configFile = FileTmpTools.getTempFile();
            String s = "tessedit_create_txt 1\n";
            if (html && htmlCheck.isSelected()) {
                s += "tessedit_create_hocr 1\n";
            }
            if (pdf && pdfCheck.isSelected()) {
                s += "tessedit_create_pdf 1\n";
            }
            Map<String, String> p = checkParameters();
            if (p != null) {
                for (String key : p.keySet()) {
                    s += key + "\t" + p.get(key) + "\n";
                }
            }
            TextFileTools.writeFile(configFile, s, Charset.forName("utf-8"));
            return true;
        } catch (Exception e) {
            popError(e.toString());
            MyBoxLog.console(e.toString());
            return false;
        }
    }

    public Process process(File file, String prefix) {
        try {
            if (file == null || configFile == null) {
                return null;
            }
            List<String> parameters = new ArrayList<>();
            parameters.addAll(Arrays.asList(
                    tesseractPathController.pickFile().getAbsolutePath(),
                    file.getAbsolutePath(), prefix,
                    "--tessdata-dir", dataPathController.pickFile().getAbsolutePath(),
                    tesseractVersion > 3 ? "--psm" : "-psm", psm + ""
            ));
            if (selectedLanguages != null) {
                parameters.addAll(Arrays.asList("-l", selectedLanguages));
            }
            parameters.add(configFile.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(parameters).redirectErrorStream(true);
            return pb.start();
        } catch (Exception e) {
            popError(e.toString());
            MyBoxLog.error(e);
            return null;
        }
    }

}
