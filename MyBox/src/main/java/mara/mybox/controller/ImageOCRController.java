package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import mara.mybox.data.VisitHistory;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonImageValues;
import mara.mybox.value.CommonValues;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

/**
 * @Author Mara
 * @CreateDate 2019-9-17
 * @Description
 * @License Apache License Version 2.0
 */
/*
http://tess4j.sourceforge.net/usage.html
https://github.com/tesseract-ocr/tesseract/wiki/Data-Files
Images intended for OCR should have at least 200 DPI in resolution, typically 300 DPI, 1 bpp (bit per pixel) monochome
or 8 bpp grayscale uncompressed TIFF or PNG format.
PNG is usually smaller in size than other image formats and still keeps high quality due to its employing lossless data compression algorithms;
TIFF has the advantage of the ability to contain multiple images (pages) in a file.
 */
public class ImageOCRController extends ImageViewerController {

    protected File ocrPath;

    @FXML
    protected TextArea ocrArea;
    @FXML
    protected Label setOCRLabel, resultLabel;
    @FXML
    protected ComboBox<String> langSelector, engineSelector;
    @FXML
    protected CheckBox synchronizeCheck;

    public ImageOCRController() {
        baseTitle = AppVariables.message("ImageOCR");

        TargetPathType = VisitHistory.FileType.Text;
        TargetFileType = VisitHistory.FileType.Text;

        targetPathKey = "TextFilePath";
        targetExtensionFilter = CommonImageValues.TextExtensionFilter;

    }

    @Override
    public void initializeNext2() {
        langSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                startOCR();
            }
        });
    }

    @Override
    public void toFront() {
        super.toFront();

        ocrPath = null;
        ocrPathDefined();

    }

    protected boolean ocrPathDefined() {
        setOCRLabel.setText("");
        if (ocrPath != null) {
            return true;
        }
        langSelector.getItems().clear();
        String p = AppVariables.getUserConfigValue("OCRDataPath", null);
        if (p != null && !p.isEmpty()) {
            ocrPath = new File(p);
            if (!ocrPath.exists() || !ocrPath.isDirectory()) {
                ocrPath = null;
            }
        }
        if (ocrPath == null) {
            setOCRLabel.setText(message("SetOCRPath"));
            settingsAction();
            return false;

        }
        File[] files = ocrPath.listFiles();
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

    @Override
    public void afterImageLoaded() {
        super.afterImageLoaded();
        if (synchronizeCheck.isSelected()) {
            startOCR();
        }
    }

    public void startOCR() {
        if (imageView.getImage() == null || !ocrPathDefined()) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private String result;
                private long cost;

                @Override
                protected boolean handle() {
                    try {
                        cost = new Date().getTime();
                        ITesseract instance = new Tesseract();
                        instance.setDatapath(ocrPath.getAbsolutePath());
                        String lang = langSelector.getValue();
                        if (lang != null) {
                            if (message("SimplifiedChinese").equals(lang)) {
                                instance.setLanguage("chi_sim+chi_sim_vert+eng+osd+equ");
                            } else if (message("TraditionalChinese").equals(lang)) {
                                instance.setLanguage("chi_tra+chi_tra_vert+eng+osd+equ");
                            } else if (message("English").equals(lang)) {
                                instance.setLanguage("eng+osd+equ");
                            } else {
                                instance.setLanguage(lang + "+eng+osd+equ");
                            }
                        } else {
                            instance.setLanguage("eng+osd+equ");
                        }

                        Image selected = cropImage();
                        if (selected == null) {
                            selected = imageView.getImage();
                        }
                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(selected, null);
                        if (task == null || isCancelled() || bufferedImage == null) {
                            return false;
                        }
                        result = instance.doOCR(bufferedImage);
                        cost = new Date().getTime() - cost;
                        return result != null;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (result.length() == 0) {
                        popText(message("OCRMissComments"), 5000, "white", "1.1em", null);
                    }
                    ocrArea.setText(result);
                    resultLabel.setText(MessageFormat.format(message("OCRresults"), result.length(), cost));
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    @FXML
    public void settingsAction() {
        SettingsController controller = (SettingsController) openStage(CommonValues.SettingsFxml);
        controller.setParentController(this);
        controller.setParentFxml(myFxml);
        controller.tabPane.getSelectionModel().select(controller.dataTab);
    }

    @FXML
    @Override
    public void startAction() {
        startOCR();
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (ocrArea.getText().isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }

            String name = null;
            if (sourceFile != null) {
                name = FileTools.getFilePrefix(sourceFile.getName());
            }
            final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                    name, targetExtensionFilter, true);
            if (file == null) {
                return;
            }
            recordFileWritten(file);

            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    ok = FileTools.writeFile(file, ocrArea.getText()) != null;
                    return true;
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void displayResult(BufferedImage image, String text) {
        imageView.setImage(SwingFXUtils.toFXImage(image, null));
        ocrArea.setText(text);
    }

}
