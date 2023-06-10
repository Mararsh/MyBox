package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Date;
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
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

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
        File dataPath = ocrOptionsController.dataPathController.file();
        if (!dataPath.exists()) {
            popError(message("InvalidParameters"));
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
        if (!ocrOptionsController.checkCommandPamameters(false, false)) {
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
                    File imageFile = FileTmpTools.getTempFile(".png");
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(selected, null);
                    bufferedImage = AlphaTools.removeAlpha(bufferedImage);
                    ImageFileWriters.writeImageFile(bufferedImage, "png", imageFile.getAbsolutePath());

                    String fileBase = FileTmpTools.getTempFile().getAbsolutePath();
                    process = process = ocrOptionsController.process(imageFile, fileBase);
                    if (process == null) {
                        return;
                    }
                    long start = new Date().getTime();
                    try (BufferedReader inReader = process.inputReader(Charset.defaultCharset())) {
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
                                ocrLabel.setText(MessageFormat.format(message("OCRresults"),
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
        if (ocrTask != null) {
            ocrTask.cancel();
        }
        ocrTask = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    Image selected = imageToHandle();
                    if (selected == null) {
                        selected = imageView.getImage();
                    }
                    return ocrOptionsController.imageOCR(this, selected, false);
                } catch (Exception e) {
                    error = e.toString();
                    MyBoxLog.debug(e.toString());
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                ocrArea.setText(ocrOptionsController.texts);
                ocrLabel.setText(MessageFormat.format(message("OCRresults"),
                        ocrOptionsController.texts.length(),
                        DateTools.datetimeMsDuration(new Date().getTime() - startTime.getTime())));
                orcPage = frameIndex;
                tabPane.getSelectionModel().select(ocrTab);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                ocrTask = null;
                ocrOptionsController.clearResults();
            }

        };
        start(ocrTask, MessageFormat.format(message("LoadingPageNumber"), (frameIndex + 1) + ""));
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
