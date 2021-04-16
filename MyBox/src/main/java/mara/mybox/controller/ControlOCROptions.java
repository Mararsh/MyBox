package mara.mybox.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
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
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.HtmlTools;
import mara.mybox.tools.OCRTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
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
    protected ListView<String> languageList;
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
        baseTitle = AppVariables.message("ImageOCR");
        TipsLabelKey = "ImageOCRComments";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            String os = SystemTools.os();
            tesseractPathController.type(VisitHistory.FileType.Bytes)
                    .isDirectory(false).mustExist(true).permitNull(true)
                    .defaultValue("win".equals(os) ? "D:\\Programs\\Tesseract-OCR\\tesseract.exe" : "/bin/tesseract")
                    .name("TesseractPath", true);

            dataPathController.label(message("OCRDataPath"))
                    .isDirectory(true).mustExist(true).permitNull(false)
                    .defaultValue("win".equals(os) ? "D:\\Programs\\Tesseract-OCR\\tessdata" : "/usr/local/share/tessdata/")
                    .name(OCRTools.TessDataPath, true);

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
                    AppVariables.setUserConfigValue("ImageOCREmbed", embedRadio.isSelected());
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
                    AppVariables.setUserConfigInt("ImageOCRPSM", psm);
                }
            });
            psmSelector.getSelectionModel().select(AppVariables.getUserConfigInt("ImageOCRPSM", 6));

            htmlCheck.setSelected(AppVariables.getUserConfigBoolean("ImageOCRhtml", false));
            htmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue("ImageOCRhtml", htmlCheck.isSelected());
                }
            });

            pdfCheck.setSelected(AppVariables.getUserConfigBoolean("ImageOCRpdf", false));
            pdfCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue("ImageOCRpdf", pdfCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void initWin() {
        embedRadio.setDisable(false);
        if (AppVariables.getUserConfigBoolean("ImageOCREmbed", true)) {
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
                AppVariables.setUserConfigValue("ImageOCRWordLevel", newValue);
            }
        });
        wordSelector.getSelectionModel().select(
                AppVariables.getUserConfigValue("ImageOCRWordLevel", message("Symbol")));
    }

    public void setLanguages() {
        try {
            languageList.getItems().clear();
            languageList.getItems().addAll(OCRTools.namesList(tesseractVersion > 3));
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
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void setValues(BaseController parent, boolean needFormats, boolean needLevels) {
        try {
            parentController = parent;
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
            try ( BufferedReader inReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
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
            table.newLinkRow("HomeBrew on Mac for Chinese", "https://www.jianshu.com/p/6a0fff005f20");
            table.newLinkRow("DataFiles", "https://tesseract-ocr.github.io/tessdoc/Data-Files.html");
            table.newLinkRow("Documents", "https://github.com/tesseract-ocr/tessdoc");
            table.newLinkRow("Parameters", "https://github.com/tesseract-ocr/tessdoc/blob/master/ControlParams.md");
            table.newLinkRow("ImproveQuality", "https://tesseract-ocr.github.io/tessdoc/ImproveQuality.html");

            File htmFile = HtmlTools.writeHtml(table.html());
            openLink(htmFile.toURI().toString());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void helpMe() {
        File help = FxmlControl.getInternalFile(
                "/data/tessdata/tesseract-parameters.txt", "doc", "tesseract-parameters.txt", false);
        TextEditerController controller = (TextEditerController) openStage(CommonValues.TextEditerFxml);
        controller.hideLeftPane();
        controller.hideRightPane();
        controller.openTextFile(help);
    }

}
