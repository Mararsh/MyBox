package mara.mybox.controller;

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
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.AlphaTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @License Apache License Version 2.0
 */
public abstract class PdfViewController_OCR extends BaseFileImagesViewController {

    protected int orcPage;
    protected Task ocrTask;
    protected Thread ocrThread;

    @FXML
    protected Tab imageTab, ocrTab, ocrOptionsTab;
    @FXML
    protected TextArea ocrArea;
    @FXML
    protected Label ocrLabel;
    @FXML
    protected ControlOCROptions ocrOptionsController;

    @FXML
    public void startOCR() {
        if (imageView.getImage() == null) {
            return;
        }
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
        if (imageView.getImage() == null || timer != null || process != null) {
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
                    Image selected = imageToHandle();
                    if (selected == null) {
                        selected = imageView.getImage();
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
                    String s = "tessedit_create_txt 1\n";
                    Map<String, String> p = ocrOptionsController.checkParameters();
                    if (p != null) {
                        for (String key : p.keySet()) {
                            s += key + "\t" + p.get(key) + "\n";
                        }
                    }
                    TextFileTools.writeFile(configFile, s, Charset.forName("utf-8"));
                    parameters.add(configFile.getAbsolutePath());

                    ProcessBuilder pb = new ProcessBuilder(parameters).redirectErrorStream(true);
                    long start = new Date().getTime();
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
                                ocrArea.setText(texts);
                                ocrLabel.setText(MessageFormat.format(Languages.message("OCRresults"),
                                        texts.length(), DateTools.datetimeMsDuration(new Date().getTime() - start)));
                                orcPage = frameIndex;
                                tabPane.getSelectionModel().select(ocrTab);
                            } else {
                                if (outputs != null && !outputs.isBlank()) {
                                    alertError(outputs);
                                } else {
                                    popFailed();
                                }
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
        if (imageView.getImage() == null) {
            return;
        }
        synchronized (this) {
            if (ocrTask != null) {
                ocrTask.cancel();
            }
            ocrTask = new SingletonTask<Void>(this) {

                private String texts;

                @Override
                protected boolean handle() {
                    try {
                        ITesseract instance = new Tesseract();
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
                        Image selected = imageToHandle();
                        if (selected == null) {
                            selected = imageView.getImage();
                        }
                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(selected, null);
                        bufferedImage = AlphaTools.removeAlpha(bufferedImage);
                        if (task == null || isCancelled() || bufferedImage == null) {
                            return false;
                        }
                        texts = instance.doOCR(bufferedImage);
                        return texts != null;
                    } catch (Exception e) {
                        error = e.toString();
                        MyBoxLog.debug(e.toString());
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    ocrArea.setText(texts);
                    ocrLabel.setText(MessageFormat.format(Languages.message("OCRresults"),
                            texts.length(), DateTools.datetimeMsDuration(new Date().getTime() - startTime.getTime())));
                    orcPage = frameIndex;
                    tabPane.getSelectionModel().select(ocrTab);
                }
            };
            start(ocrTask, MessageFormat.format(message("LoadingPageNumber"), (frameIndex + 1) + ""));
        }

    }

    @FXML
    public void editOCR() {
        if (ocrArea.getText().isEmpty()) {
            popError(message("NoData"));
            return;
        }
        TextEditorController controller = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
        controller.loadContents(ocrArea.getText());
    }

}
