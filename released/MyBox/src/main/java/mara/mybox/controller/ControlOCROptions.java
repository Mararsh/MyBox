package mara.mybox.controller;

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
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.OCRTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import net.sourceforge.tess4j.ITessAPI;

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

    protected String selectedLanguages;
    protected int psm, regionLevel, wordLevel, tesseractVersion;
    protected boolean setFormats, setLevels, isVersion3;

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
    protected ControlCheckBoxList languageListController;
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
            tesseractPathController.type(VisitHistory.FileType.Bytes)
                    .isDirectory(false).mustExist(true).permitNull(true)
                    .defaultFile("win".equals(os) ? new File("D:\\Programs\\Tesseract-OCR\\tesseract.exe") : new File("/bin/tesseract"))
                    .baseName(baseName).savedName("TesseractPath").init();

            dataPathController.isDirectory(true).mustExist(true).permitNull(false)
                    .defaultFile("win".equals(os) ? new File("D:\\Programs\\Tesseract-OCR\\tessdata") : new File("/usr/local/share/tessdata/"))
                    .baseName(baseName).savedName(OCRTools.TessDataPath).init();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
                tesseractRadio.fire();
            }
            engineGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle oldVal, Toggle newVal) {
                    checkEngine();
                    UserConfig.setBoolean("ImageOCREmbed", embedRadio.isSelected());
                }
            });

            languageListController.setParent(this);
            languageListController.checkedNotify.addListener(new ChangeListener<Boolean>() {
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
                    UserConfig.setBoolean("ImageOCRhtml", htmlCheck.isSelected());
                }
            });

            pdfCheck.setSelected(UserConfig.getBoolean("ImageOCRpdf", false));
            pdfCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("ImageOCRpdf", pdfCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void initWin() {
        embedRadio.setDisable(false);
        if (UserConfig.getBoolean("ImageOCREmbed", true)) {
            embedRadio.fire();
        } else {
            tesseractRadio.fire();
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
            languageListController.setValues(langs);

            selectedLanguages = UserConfig.getString("ImageOCRLanguages", null);
            if (selectedLanguages != null && !selectedLanguages.isEmpty()) {
                currentOCRFilesLabel.setText(MessageFormat.format(message("CurrentDataFiles"), selectedLanguages));
                currentOCRFilesLabel.setStyle(null);
                isSettingValues = true;
                String[] selected = selectedLanguages.split("\\+");
                Map<String, String> codes = OCRTools.codeName();
                List<String> selectedNames = new ArrayList<>();
                List<Integer> selectedIndices = new ArrayList<>();
                for (String code : selected) {
                    String name = codes.get(code);
                    if (name == null) {
                        name = code;
                    }
                    selectedNames.add(name);
                }
                for (int i = 0; i < langs.size(); i++) {
                    if (selectedNames.contains(langs.get(i))) {
                        selectedIndices.add(i);
                    }
                }
                languageListController.setCheckIndices(selectedIndices);
                isSettingValues = false;
            } else {
                currentOCRFilesLabel.setText(MessageFormat.format(message("CurrentDataFiles"), message("NoData")));
                currentOCRFilesLabel.setStyle(UserConfig.badStyle());
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
            MyBoxLog.debug(e.toString());
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
                    if (ocrController.ocrTabPane.getTabs().contains(ocrController.regionsTab)) {
                        ocrController.ocrTabPane.getTabs().removeAll(ocrController.regionsTab, ocrController.wordsTab);
                    }
                }
                tesseractPathController.thisPane.setDisable(false);
                tesseractPathController.checkFileInput();
                tesseractVersion = 4;
            } else {
                if (parentController != null && parentController instanceof ImageOCRController) {
                    ImageOCRController ocrController = (ImageOCRController) parentController;
                    if (!ocrController.ocrTabPane.getTabs().contains(ocrController.regionsTab)) {
                        ocrController.ocrTabPane.getTabs().addAll(ocrController.regionsTab, ocrController.wordsTab);
                    }
                }
                tesseractPathController.thisPane.setDisable(true);
                tesseractPathController.fileInput.setStyle(null);
                tesseractVersion = tesseractVersion();
            }
            dataPathController.checkFileInput();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public int tesseractVersion() {
        try {
            if (tesseractPathController.file == null || !tesseractPathController.file.exists()) {
                return -1;
            }
            List<String> parameters = new ArrayList<>();
            parameters.addAll(Arrays.asList(
                    tesseractPathController.file.getAbsolutePath(),
                    "--version"
            ));
            ProcessBuilder pb = new ProcessBuilder(parameters).redirectErrorStream(true);
            Process process = pb.start();
            try ( BufferedReader inReader = process.inputReader(Charset.forName("UTF-8"))) {
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
                MyBoxLog.debug(e.toString());
            }
            process.waitFor();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return -1;
    }

    public void checkLanguages() {
        if (isSettingValues) {
            return;
        }
        try {
            selectedLanguages = null;
            List<String> langsList = languageListController.checkedOrderedValues();
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
                currentOCRFilesLabel.setText(MessageFormat.format(message("CurrentDataFiles"), message("NoData")));
                currentOCRFilesLabel.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public Map<String, String> checkParameters() {
        String texts = optionsArea.getText();
        if (texts.isBlank()) {
            return null;
        }
        Map<String, String> p = new HashMap<>();
        String[] lines = texts.split("\n");
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
    public void refreshAction() {
        setLanguages();
    }

    @FXML
    public void aboutTesseract() {
        try {
            StringTable table = new StringTable(null, message("AboutTesseract"));
            table.newLinkRow("Home", "https://github.com/tesseract-ocr/tesseract");
            table.newLinkRow("Installation", "https://tesseract-ocr.github.io/tessdoc/Home.html");
            table.newLinkRow("InstallationOnWindows", "https://github.com/UB-Mannheim/tesseract/wiki");
            table.newLinkRow("DataFiles", "https://tesseract-ocr.github.io/tessdoc/Data-Files.html");
            table.newLinkRow("Documents", "https://github.com/tesseract-ocr/tessdoc");
            table.newLinkRow("ImproveQuality", "https://tesseract-ocr.github.io/tessdoc/ImproveQuality.html");

            File htmFile = HtmlWriteTools.writeHtml(table.html());
            openLink(htmFile);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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

}
