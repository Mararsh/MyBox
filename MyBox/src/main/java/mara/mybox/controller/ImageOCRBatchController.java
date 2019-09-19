package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

/**
 * @Author Mara
 * @CreateDate 2019-9-18
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageOCRBatchController extends ImagesBatchController {

    protected String ocrPath;
    protected String language;
    protected BufferedImage lastImage;
    protected ITesseract OCRinstance;

    @FXML
    protected Label setOCRLabel;
    @FXML
    protected ComboBox<String> langSelector;

    public ImageOCRBatchController() {
        baseTitle = AppVariables.message("ImageOCRBatch");
        browseTargets = false;
    }

    @Override
    public void initOptionsSection() {
        try {
            ocrPathDefined();
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
    public String handleFile(File srcFile, File targetPath) {
        try {
            if (ocrPath == null) {
                return message("Failed");
            }
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return AppVariables.message("Skip");
            }

            lastImage = ImageFileReaders.readImage(srcFile);
            if (lastImage == null) {
                return AppVariables.message("Failed");
            }

            String result = OCRinstance.doOCR(lastImage);
            if (FileTools.writeFile(target, result) != null) {
                actualParameters.finalTargetName = target.getAbsolutePath();
                targetFiles.add(target);
                return AppVariables.message("Successful");
            } else {
                return AppVariables.message("Failed");
            }
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
