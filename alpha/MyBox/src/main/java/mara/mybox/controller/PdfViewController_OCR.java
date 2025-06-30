package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Date;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import mara.mybox.data.TesseractOptions;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.image.tools.AlphaTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @License Apache License Version 2.0
 */
public abstract class PdfViewController_OCR extends BaseFileImagesController {

    protected TesseractOptions tesseractOptions;
    protected int orcPage;
    protected FxTask ocrTask;
    protected Thread ocrThread;

    @FXML
    protected Tab imageTab, ocrTab, ocrOptionsTab;
    @FXML
    protected TextArea ocrArea;
    @FXML
    protected Label ocrLabel;

    @Override
    public void initValues() {
        try {
            super.initValues();
            tesseractOptions = new TesseractOptions();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void startOCR() {
        if (imageView.getImage() == null) {
            return;
        }
        File dataPath = tesseractOptions.getDataPath();
        if (!dataPath.exists()) {
            popError(message("InvalidParameters"));
            return;
        }
        if (tesseractOptions.isEmbed()) {
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
        if (ocrTask != null) {
            ocrTask.cancel();
        }
        ocrTask = new FxTask<Void>(this) {
            private String outputs = "", texts;

            @Override
            protected boolean handle() {
                try {
                    Image selected = imageView.getImage();
                    File imageFile = FileTmpTools.getTempFile(".png");
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(selected, null);
                    bufferedImage = AlphaTools.removeAlpha(this, bufferedImage);
                    if (bufferedImage == null || !isWorking()) {
                        return false;
                    }
                    if (!ImageFileWriters.writeImageFile(this, bufferedImage, "png", imageFile.getAbsolutePath())) {
                        return false;
                    }
                    String fileBase = FileTmpTools.getTempFile().getAbsolutePath();
                    process = process = ocrOptionsController.process(imageFile, fileBase);
                    if (process == null) {
                        return false;
                    }
                    startTime = new Date();
                    try (BufferedReader inReader = process.inputReader(Charset.defaultCharset())) {
                        String line;
                        while ((line = inReader.readLine()) != null) {
                            if (!isWorking()) {
                                process.destroyForcibly();
                                return false;
                            }
                            outputs += line + "\n";
                        }
                    } catch (Exception e) {
                        outputs += e.toString() + "\n";
                    }
                    process.waitFor();

                    File txtFile = new File(fileBase + ".txt");
                    if (txtFile.exists()) {
                        texts = TextFileTools.readTexts(this, txtFile);
                        FileDeleteTools.delete(txtFile);
                    } else {
                        texts = null;
                    }

                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    MyBoxLog.debug(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (texts != null) {
                    ocrArea.setText(texts);
                    ocrLabel.setText(MessageFormat.format(message("OCRresults"),
                            texts.length(), DateTools.datetimeMsDuration(new Date().getTime() - startTime.getTime())));
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

            @Override
            protected void finalAction() {
                super.finalAction();
                if (process != null) {
                    process.destroy();
                    process = null;
                }
                ocrTask = null;
                ocrOptionsController.clearResults();
            }

        };
        start(ocrTask, MessageFormat.format(message("LoadingPageNumber"), (frameIndex + 1) + ""));
    }

    protected void embedded() {
        if (imageView.getImage() == null) {
            return;
        }
        if (ocrTask != null) {
            ocrTask.cancel();
        }
        ocrTask = new FxTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    Image selected = imageView.getImage();
                    return ocrOptionsController.imageOCR(this, selected, false);
                } catch (Exception e) {
                    error = e.toString();
                    MyBoxLog.debug(e);
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
    public void ocrOptions() {
        TesseractOptionsController.open(this, tesseractOptions);
    }

    @FXML
    public void editOCR() {
        if (ocrArea.getText().isEmpty()) {
            popError(message("NoData"));
            return;
        }
        TextEditorController.edit(ocrArea.getText());
    }

}
