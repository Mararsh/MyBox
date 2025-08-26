package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import javafx.scene.control.Tab;
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
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.OCRTools;
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
public class TesseractOptionsController extends BaseChildController {

    protected TesseractOptions options;
    protected String selectedLanguages;

    @FXML
    protected Tab langTab, engineTab;
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

    public void setParameters(BaseController parent, TesseractOptions inOptions) {
        try {
            parentController = parent;
            options = inOptions != null ? inOptions : new TesseractOptions();

            tesseractPathController.type(VisitHistory.FileType.All)
                    .isDirectory(false).mustExist(true).permitNull(true)
                    .defaultFile(options.getTesseract())
                    .parent(this, "TesseractOptionsExe");

            dataPathController.isDirectory(true).mustExist(true).permitNull(false)
                    .defaultFile(options.getDataPath())
                    .parent(this, "TesseractOptionsData");

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
            psmSelector.getSelectionModel().select(options.getPsm());

            htmlCheck.setSelected(options.isOutHtml());
            pdfCheck.setSelected(options.isOutPdf());

            if (options.isSetFormats()) {
                if (!optionsBox.getChildren().contains(outputsBox)) {
                    optionsBox.getChildren().add(outputsBox);
                }
            } else {
                if (optionsBox.getChildren().contains(outputsBox)) {
                    optionsBox.getChildren().remove(outputsBox);
                }
            }
            checkEngine();
            loadLanguages();

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

    public void loadLanguages() {
        try {
            languagesController.loadNames(options.namesList());

            selectedLanguages = options.getSelectedLanguages();
            if (selectedLanguages != null && !selectedLanguages.isEmpty()) {
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
            }
            checkLanguages();
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
            if (embedRadio.isSelected()) {
                tesseractPathController.thisPane.setDisable(true);
                if (parentController != null && parentController instanceof ImageOCRController) {
                    ImageOCRController ocrController = (ImageOCRController) parentController;
                    if (!ocrController.resultsTabPane.getTabs().contains(ocrController.regionsTab)) {
                        ocrController.resultsTabPane.getTabs().addAll(ocrController.regionsTab, ocrController.wordsTab);
                    }
                }
                options.setTesseractVersion(5);
            } else {
                tesseractPathController.thisPane.setDisable(false);
                options.setTesseractVersion(options.tesseractVersion());
            }
            
   
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void checkLanguages() {
        try {
            if (isSettingValues) {
                return;
            }
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
                currentOCRFilesLabel.setText(MessageFormat.format(message("CurrentDataFiles"), selectedLanguages));
                currentOCRFilesLabel.setStyle(null);
            } else {
                currentOCRFilesLabel.setText(MessageFormat.format(message("CurrentDataFiles"), message("SelectToHandle")));
                currentOCRFilesLabel.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        try {
            if (tesseractRadio.isSelected()) {
                File tesseract = tesseractPathController.pickFile();
                if (tesseract == null || !tesseract.exists()) {
                    popError(message("InvalidParameters") + ": " + message("tesseractInstallationPath"));
                    tabPane.getSelectionModel().select(engineTab);
                    return;
                }
                options.setTesseract(tesseract);
            }
            if (options.getTesseractVersion() < 0) {
                popError(message("InvalidParameters") + ": " + message("tesseractInstallationPath"));
                tabPane.getSelectionModel().select(engineTab);
                return;
            }

            File dataPath = dataPathController.pickFile();
            if (dataPath == null || !dataPath.exists()) {
                popError(message("InvalidParameters") + ". " + message("OCRDataPath")
                        + (dataPath != null ? " \"" + dataPath.getAbsolutePath() + "\"" : ""));
                tabPane.getSelectionModel().select(langTab);
                return;
            }
            options.setDataPath(dataPath);

            options.setEmbed(embedRadio.isSelected());
            options.setOutHtml(htmlCheck.isSelected());
            options.setOutPdf(pdfCheck.isSelected());
            options.setSelectedLanguages(selectedLanguages);
            options.setMore(optionsArea.getText());
            options.setTessInstance(null);

            String pss = psmSelector.getSelectionModel().getSelectedItem();
            if (pss == null || pss.isBlank()) {
                options.setPsm(-1);
            } else {
                String[] vs = pss.split("    ");
                options.setPsm(Integer.parseInt(vs[0]));
            }

            options.writeValues();

            options.makeConfigFile();

            parentController.popInformation(message("Done"));

            close();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void download() {
        openLink("https://tesseract-ocr.github.io/tessdoc/Home.html");
    }

    @FXML
    @Override
    public void refreshAction() {
        loadLanguages();
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

    /*
        static
     */
    public static TesseractOptionsController open(BaseController parent, TesseractOptions options) {
        try {
            TesseractOptionsController controller = (TesseractOptionsController) WindowTools.childStage(
                    parent, Fxmls.TesseractOptionsFxml);
            controller.setParameters(parent, options);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
