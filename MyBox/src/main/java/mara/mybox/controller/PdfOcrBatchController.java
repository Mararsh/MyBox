package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * @Author Mara
 * @CreateDate 2018-7-4
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfOcrBatchController extends PdfBatchController {

    protected String separator;
    protected String ocrTexts;
    protected File tmpFile;
    protected String ocrPath;
    protected String language;
    protected BufferedImage lastImage;
    protected ITesseract OCRinstance;
    protected PDFRenderer renderer;
    protected int dpi;
    protected ImageType colorType;

    @FXML
    protected CheckBox separatorCheck;
    @FXML
    protected TextField separatorInput;
    @FXML
    protected Label setOCRLabel;
    @FXML
    protected ComboBox<String> dpiSelector, langSelector, colorSpaceSelector;

    public PdfOcrBatchController() {
        baseTitle = AppVariables.message("PdfOCRBatch");
        browseTargets = false;
    }

    @Override
    public void initOptionsSection() {
        try {
            ocrPathDefined();

            colorType = ImageType.RGB;
            colorSpaceSelector.getItems().addAll(Arrays.asList(
                    "RGB", message("Gray"), message("BlackOrWhite")
            ));
            colorSpaceSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    if (message("BlackOrWhite").equals(newV)) {
                        colorType = ImageType.BINARY;
                    } else if (message("Gray").equals(newV)) {
                        colorType = ImageType.GRAY;
                    } else if ("RGB".equals(newV)) {
                        colorType = ImageType.RGB;
                    }
                }
            });
            colorSpaceSelector.getSelectionModel().select(0);

            dpi = 96;
            dpiSelector.getItems().addAll(Arrays.asList(
                    "96", "72", "300", "160", "240", "120", "600", "400"
            ));
            dpiSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    dpi = Integer.valueOf(dpiSelector.getValue());
                    AppVariables.setUserConfigInt("PdfOcrDpi", dpi);
                }
            });
            dpiSelector.getSelectionModel().select(AppVariables.getUserConfigValue("PdfOcrDpi", "96"));

            FxmlControl.setTooltip(separatorInput, message("InsertPageSeparatorComments"));

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected boolean ocrPathDefined() {
        setOCRLabel.setText("");
        if (ocrPath != null) {
            return true;
        }
        langSelector.getItems().clear();
        String p = AppVariables.getUserConfigValue("OCRDataPath", null);
        if (p != null && !p.isEmpty()) {
            File ocrP = new File(p);
            if (!ocrP.exists() || !ocrP.isDirectory()) {
                ocrPath = null;
            } else {
                ocrPath = ocrP.getAbsolutePath();
            }
        }
        if (ocrPath == null) {
            setOCRLabel.setText(message("SetOCRPath"));
            settingsAction();
            return false;

        }
        File[] files = new File(ocrPath).listFiles();
        for (File f : files) {
            String name = f.getName();
            if (!f.isFile() || !name.endsWith(".traineddata")) {
                continue;
            }
            langSelector.getItems().add(name.substring(0, name.length() - ".traineddata".length()));
        }
        if (AppVariables.currentBundle == CommonValues.BundleZhCN) {
            if (!langSelector.getItems().contains("chi_sim")) {
                setOCRLabel.setText(message("MissChineseOCRData"));
                langSelector.getItems().add(0, message("English"));
            } else {
                langSelector.getItems().add(0, message("SimplifiedChinese"));
                if (langSelector.getItems().contains("chi_tra")) {
                    langSelector.getItems().add(1, message("TraditionalChinese"));
                }
                langSelector.getItems().add(2, message("English"));
            }
            langSelector.getSelectionModel().select(0);
        } else {
            langSelector.getSelectionModel().select("eng");
        }
        return true;
    }

    @FXML
    public void settingsAction() {
        SettingsController controller = (SettingsController) openStage(CommonValues.SettingsFxml);
        controller.setParentController(this);
        controller.setParentFxml(myFxml);
        controller.tabPane.getSelectionModel().select(controller.dataTab);
    }

    @Override
    public boolean makeActualParameters() {
        if (!ocrPathDefined() || !super.makeActualParameters()) {
            return false;
        }
        String lang = langSelector.getValue();
        if (lang != null) {
            if (message("SimplifiedChinese").equals(lang)) {
                language = "chi_sim+chi_sim_vert+eng+osd+equ";
            } else if (message("TraditionalChinese").equals(lang)) {
                language = "chi_tra+chi_tra_vert+eng+osd+equ";
            } else if (message("English").equals(lang)) {
                language = "eng+osd+equ";
            } else {
                language = lang + "+eng+osd+equ";
            }
        } else {
            language = "eng+osd+equ";
        }
        separator = separatorInput.getText();
        if (!separatorCheck.isSelected() || separator == null || separator.isEmpty()) {
            separator = null;
        }
        try {
            OCRinstance = new Tesseract();
            OCRinstance.setDatapath(ocrPath);
            OCRinstance.setLanguage(language);
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    @Override
    public boolean preHandlePages() {
        try {
            ocrTexts = "";
            renderer = new PDFRenderer(doc);
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    @Override
    public int handleCurrentPage() {
        try {
            // ImageType.BINARY work bad while ImageType.RGB works best
            lastImage
                    = renderer.renderImageWithDPI(currentParameters.currentPage - 1, dpi, colorType);    // 0-based

            String result = OCRinstance.doOCR(lastImage);
            if (result != null) {
                ocrTexts += result;
                String s = separator.replace("<Page Number>", currentParameters.currentPage + " ");
                s = s.replace("<Total Number>", doc.getNumberOfPages() + "");
                ocrTexts += s + System.getProperty("line.separator");
                return 1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return 0;
        }
    }

    @Override
    public void postHandlePages() {
        try {
            File tFile = makeTargetFile(FileTools.getFilePrefix(currentParameters.currentSourceFile.getName()),
                    ".txt", currentParameters.currentTargetPath);
            currentTargetFile = tFile.getAbsolutePath();
            if (FileTools.writeFile(tFile, ocrTexts) != null) {
                currentParameters.finalTargetName = tFile.getAbsolutePath();
                targetFiles.add(tFile);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void view(File file) {
        String s = FileTools.readTexts(file);
        ImageOCRController controller = (ImageOCRController) openStage(CommonValues.ImageOCRFxml);
        controller.displayResult(lastImage, s);
    }

    @Override
    public void quitProcess() {
        OCRinstance = null;
    }

}
