package mara.mybox.controller;

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
import mara.mybox.data.TesseractOptions;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.OCRTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

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
public class TesseractOptionsController extends BaseController {

    protected TesseractOptions options;

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

    public TesseractOptionsController() {
        baseTitle = message("ImageOCR");
        TipsLabelKey = "ImageOCRComments";
    }

    public void setParameters(TesseractOptions inOptions, boolean html, boolean pdf) {
        try {
            options = inOptions != null ? inOptions : new TesseractOptions();

            tesseractPathController.type(VisitHistory.FileType.All)
                    .isDirectory(false).mustExist(true).permitNull(true)
                    .defaultFile(options.getTesseract())
                    .parent(this, "TesseractPath");

            dataPathController.isDirectory(true).mustExist(true).permitNull(false)
                    .defaultFile(options.getDataPath())
                    .parent(this, OCRTools.TessDataPath);

            if (options.isWin()) {
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
                    if (isSettingValues) {
                        return;
                    }
                    String[] vs = newValue.split("    ");
                    options.setPsm(Integer.parseInt(vs[0]));
                }
            });
            psmSelector.getSelectionModel().select(UserConfig.getInt("ImageOCRPSM", 6));

            htmlCheck.setSelected(options.isOutHtml());
            htmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    options.setOutHtml(htmlCheck.isSelected());

                }
            });

            pdfCheck.setSelected(options.isOutPdf());
            pdfCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    options.setOutPdf(pdfCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void initWin() {
        embedRadio.setDisable(false);
        if (options.isEmbed()) {
            embedRadio.setSelected(true);
        } else {
            tesseractRadio.setSelected(true);
        }

        regionSelector.getItems().addAll(Arrays.asList(message("None"),
                message("Block"), message("Paragraph"), message("Textline"),
                message("Word"), message("Symbol")
        ));
        regionSelector.getSelectionModel().select(options.getRegionLevel());
        regionSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                options.setRegionLevel(newValue);
            }
        });

        wordSelector.getItems().addAll(Arrays.asList(message("None"),
                message("Block"), message("Paragraph"), message("Textline"),
                message("Word"), message("Symbol")
        ));
        wordSelector.getSelectionModel().select(options.getWordLevel());
        wordSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                options.setWordLevel(newValue);
            }
        });
    }

    public void setLanguages() {
        try {
            List<String> langs = options.namesList();
            languagesController.loadNames(langs);

            String selectedLanguages = options.getSelectedLanguages();
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
            options.setSetFormats(needFormats);
            options.setSetLevels(needLevels);
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
            if (options.isSetLevels() && embedRadio.isSelected()) {
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
                options.setTesseractVersion(4);
            } else {
                if (parentController != null && parentController instanceof ImageOCRController) {
                    ImageOCRController ocrController = (ImageOCRController) parentController;
                    if (!ocrController.resultsTabPane.getTabs().contains(ocrController.regionsTab)) {
                        ocrController.resultsTabPane.getTabs().addAll(ocrController.regionsTab, ocrController.wordsTab);
                    }
                }
                tesseractPathController.thisPane.setDisable(true);
                options.setTesseractVersion(options.tesseractVersion());
            }

            dataPathController.pickFile();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void checkLanguages() {
        if (isSettingValues) {
            return;
        }
        try {
            String selectedLanguages = null;
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
                options.setSelectedLanguages(selectedLanguages);
                currentOCRFilesLabel.setText(MessageFormat.format(message("CurrentDataFiles"), selectedLanguages));
                currentOCRFilesLabel.setStyle(null);
            } else {
                options.setSelectedLanguages(null);
                currentOCRFilesLabel.setText(MessageFormat.format(message("CurrentDataFiles"), message("SelectToHandle")));
                currentOCRFilesLabel.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public Map<String, String> checkMore() {
        String s = optionsArea.getText();
        if (s == null || s.isBlank()) {
            return null;
        }
        Map<String, String> p = new HashMap<>();
        String[] lines = s.split("\n");
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

    @Override
    public void cleanPane() {
        try {
            try {
                File tesseract = tesseractPathController.pickFile();
                if (!tesseract.exists()) {
                    popError(message("InvalidParameters"));
                    return false;
                }
                options.setTesseract(tesseract);
                File dataPath = dataPathController.pickFile();
                if (!dataPath.exists()) {
                    popError(message("InvalidParameters"));
                    return false;
                }
                options.setDataPath(dataPath);

                int tesseractVersion = options.tesseractVersion();
                if (tesseractVersion < 0) {
                    popError(message("InvalidParameters"));
                    return false;
                }
                File configFile = FileTmpTools.getTempFile();
                String s = "tessedit_create_txt 1\n";
                if (html && htmlCheck.isSelected()) {
                    s += "tessedit_create_hocr 1\n";
                }
                if (pdf && pdfCheck.isSelected()) {
                    s += "tessedit_create_pdf 1\n";
                }
                Map<String, String> more = checkMore();
                options.setMore(more);
                if (more != null) {
                    for (String key : more.keySet()) {
                        s += key + "\t" + more.get(key) + "\n";
                    }
                }
                TextFileTools.writeFile(configFile, s, Charset.forName("utf-8"));
                options.setConfigFile(configFile);
                this.cleanPane();
                return true;
            } catch (Exception e) {
                popError(e.toString());
                MyBoxLog.console(e.toString());
                return false;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public boolean checkCommandPamameters(boolean html, boolean pdf) {
        try {
            File tesseract = tesseractPathController.pickFile();
            if (!tesseract.exists()) {
                popError(message("InvalidParameters"));
                return false;
            }
            options.setTesseract(tesseract);
            File dataPath = dataPathController.pickFile();
            if (!dataPath.exists()) {
                popError(message("InvalidParameters"));
                return false;
            }
            options.setDataPath(dataPath);

            int tesseractVersion = options.tesseractVersion();
            if (tesseractVersion < 0) {
                popError(message("InvalidParameters"));
                return false;
            }
            File configFile = FileTmpTools.getTempFile();
            String s = "tessedit_create_txt 1\n";
            if (html && htmlCheck.isSelected()) {
                s += "tessedit_create_hocr 1\n";
            }
            if (pdf && pdfCheck.isSelected()) {
                s += "tessedit_create_pdf 1\n";
            }
            Map<String, String> more = checkMore();
            options.setMore(more);
            if (more != null) {
                for (String key : more.keySet()) {
                    s += key + "\t" + more.get(key) + "\n";
                }
            }
            TextFileTools.writeFile(configFile, s, Charset.forName("utf-8"));
            options.setConfigFile(configFile);
            this.cleanPane();
            return true;
        } catch (Exception e) {
            popError(e.toString());
            MyBoxLog.console(e.toString());
            return false;
        }
    }

    /*
        static
     */
    public static TesseractOptionsController open(BaseController parent,
            TesseractOptions options, boolean html, boolean pdf) {
        try {
            TesseractOptionsController controller = (TesseractOptionsController) WindowTools.childStage(
                    parent, Fxmls.TesseractOptionsFxml);
            controller.setParameters(options, html, pdf);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
