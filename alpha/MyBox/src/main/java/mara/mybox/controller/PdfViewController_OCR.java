package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Date;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
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
import mara.mybox.value.UserConfig;

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
    protected TextArea ocrArea;
    @FXML
    protected Label ocrLabel;
    @FXML
    protected CheckBox wrapOCRCheck;

    @Override
    public void initControls() {
        try {
            super.initControls();

            tesseractOptions = new TesseractOptions()
                    .setOutHtml(false)
                    .setOutPdf(false);

            wrapOCRCheck.setSelected(UserConfig.getBoolean(baseName + "WrapOCR", true));
            wrapOCRCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "WrapOCR", newValue);
                    ocrArea.setWrapText(newValue);
                }
            });
            ocrArea.setWrapText(wrapOCRCheck.isSelected());

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
                    process = process = tesseractOptions.process(imageFile, fileBase);
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
                tesseractOptions.clearResults();
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
                    return tesseractOptions.imageOCR(this, selected, false);
                } catch (Exception e) {
                    error = e.toString();
                    MyBoxLog.debug(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                ocrArea.setText(tesseractOptions.getTexts());
                ocrLabel.setText(MessageFormat.format(message("OCRresults"),
                        tesseractOptions.getTexts().length(),
                        DateTools.datetimeMsDuration(new Date().getTime() - startTime.getTime())));
                orcPage = frameIndex;
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                ocrTask = null;
                tesseractOptions.clearResults();
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
