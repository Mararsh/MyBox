package mara.mybox.controller;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.AlphaTools;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;
import net.sourceforge.tess4j.Word;

/**
 * @Author Mara
 * @CreateDate 2019-9-17
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
public class ImageOCRController extends ImageViewerController {

    protected float scale;
    protected int threshold, rotate;
    protected LoadingController loading;
    protected Process process;

    @FXML
    protected Tab imageTab, processTab, optionsTab, resultsTab;
    @FXML
    protected TextArea textArea;
    @FXML
    protected Label resultLabel;
    @FXML
    protected CheckBox startCheck;
    @FXML
    protected HtmlTableController regionsTableController, wordsTableController, htmlController;
    @FXML
    protected ControlOCROptions ocrOptionsController;
    @FXML
    protected ImageOCRProcessController preprocessController;
    @FXML
    protected TabPane ocrTabPane;
    @FXML
    protected Tab txtTab, htmlTab, regionsTab, wordsTab;
    @FXML
    protected Tab ocrOptionsTab;

    public ImageOCRController() {
        baseTitle = Languages.message("ImageOCR");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Image, VisitHistory.FileType.Text);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            preprocessController.OCRController = this;
            ocrOptionsController.setParameters(this, false, true);
            ocrOptionsController.isSettingValues = true;
            ocrOptionsController.htmlCheck.setSelected(true);
            ocrOptionsController.isSettingValues = false;

            startCheck.setSelected(UserConfig.getBoolean(baseName + "Start", false));
            startCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Start", startCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    @Override
    public boolean menuAction() {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if (tab == processTab) {
            preprocessController.menuAction();
            return true;

        } else if (tab == imageTab) {
            return super.menuAction();

        } else if (tab == resultsTab) {

            if (txtTab.isSelected()) {
                Point2D localToScreen = textArea.localToScreen(textArea.getWidth() - 80, 80);
                MenuTextEditController.open(myController, textArea, localToScreen.getX(), localToScreen.getY());
                return true;

            } else if (htmlTab.isSelected()) {
                htmlController.menuAction();
                return true;

            } else if (regionsTab.isSelected()) {
                regionsTableController.menuAction();
                return true;

            } else if (wordsTab.isSelected()) {
                wordsTableController.menuAction();
                return true;
            }
        }
        return super.menuAction();
    }

    @FXML
    @Override
    public boolean popAction() {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if (tab == processTab) {
            preprocessController.popAction();
            return true;

        } else if (tab == imageTab) {
            return super.popAction();

        } else if (tab == resultsTab) {

            if (txtTab.isSelected()) {
                TextPopController.openInput(this, textArea);
                return true;

            } else if (htmlTab.isSelected()) {
                htmlController.popAction();
                return true;

            } else if (regionsTab.isSelected()) {
                regionsTableController.popAction();
                return true;

            } else if (wordsTab.isSelected()) {
                wordsTableController.popAction();
                return true;
            }
        }
        return super.popAction();
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded()) {
                return false;
            }

            String name = sourceFile != null ? FileNameTools.prefix(sourceFile.getName()) : "";
            regionsTableController.baseTitle = name + "_regions";
            wordsTableController.baseTitle = name + "_words";
            htmlController.baseTitle = name + "_texts";

            preprocessController.recoverAction();

            if (startCheck.isSelected()) {
                startAction();
            }

            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    /*
        OCR
     */
    @FXML
    @Override
    public void startAction() {
        ocrOptionsController.setLanguages();
        File dataPath = ocrOptionsController.dataPathController.file();
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
        if (preprocessController.imageView.getImage() == null || timer != null || process != null) {
            return;
        }
        if (!ocrOptionsController.checkCommandPamameters(true, false)) {
            return;
        }
        loading = handling();
        new Thread() {
            private String outputs = "";

            @Override
            public void run() {
                try {
                    Image selected = preprocessController.imageToHandle();
                    if (selected == null) {
                        selected = preprocessController.imageView.getImage();
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
                    long startTime = new Date().getTime();
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
                    String html;
                    File htmlFile = new File(fileBase + ".hocr");
                    if (htmlFile.exists()) {
                        html = TextFileTools.readTexts(htmlFile);
                        FileDeleteTools.delete(htmlFile);
                    } else {
                        html = null;
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
                                textArea.setText(texts);
                                resultLabel.setText(MessageFormat.format(Languages.message("OCRresults"),
                                        texts.length(), DateTools.datetimeMsDuration(new Date().getTime() - startTime)));
                                tabPane.getSelectionModel().select(resultsTab);
                                ocrTabPane.getSelectionModel().select(txtTab);
                            } else {
                                if (outputs != null && !outputs.isBlank()) {
                                    alertError(outputs);
                                } else {
                                    popFailed();
                                }
                            }
                            if (html != null) {
                                htmlController.loadHtml(html);
                            }
                        }
                    });

                } catch (Exception e) {
                    MyBoxLog.debug(e);
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
        if (preprocessController.imageView.getImage() == null
                || ocrOptionsController.dataPathController.file() == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    Image selected = preprocessController.imageToHandle();
                    if (selected == null) {
                        selected = preprocessController.imageView.getImage();
                    }
                    return ocrOptionsController.imageOCR(this, selected, true);
                } catch (Exception e) {
                    error = e.toString();
                    MyBoxLog.debug(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (ocrOptionsController.texts.length() == 0) {
                    popWarn(Languages.message("OCRMissComments"));
                }
                textArea.setText(ocrOptionsController.texts);
                resultLabel.setText(MessageFormat.format(Languages.message("OCRresults"),
                        ocrOptionsController.texts.length(), DateTools.datetimeMsDuration(cost)));
                tabPane.getSelectionModel().select(resultsTab);
                ocrTabPane.getSelectionModel().select(txtTab);

                htmlController.loadHtml(ocrOptionsController.html);

                if (ocrOptionsController.rectangles != null) {
                    List<String> names = new ArrayList<>();
                    names.addAll(Arrays.asList(Languages.message("Index"),
                            Languages.message("CoordinateX"), Languages.message("CoordinateY"),
                            Languages.message("Width"), Languages.message("Height")
                    ));
                    regionsTableController.initTable(Languages.message(""), names);
                    for (int i = 0; i < ocrOptionsController.rectangles.size(); ++i) {
                        Rectangle rect = ocrOptionsController.rectangles.get(i);
                        List<String> data = new ArrayList<>();
                        data.addAll(Arrays.asList(
                                i + "", rect.x + "", rect.y + "", rect.width + "", rect.height + ""
                        ));
                        regionsTableController.addData(data);
                    }
                    regionsTableController.displayHtml();
                } else {
                    regionsTableController.clear();
                }

                if (ocrOptionsController.words != null) {
                    List<String> names = new ArrayList<>();
                    names.addAll(Arrays.asList(Languages.message("Index"),
                            Languages.message("Contents"), Languages.message("Confidence"),
                            Languages.message("CoordinateX"), Languages.message("CoordinateY"),
                            Languages.message("Width"), Languages.message("Height")
                    ));
                    wordsTableController.initTable(Languages.message(""), names);
                    for (int i = 0; i < ocrOptionsController.words.size(); ++i) {
                        Word word = ocrOptionsController.words.get(i);
                        Rectangle rect = word.getBoundingBox();
                        List<String> data = new ArrayList<>();
                        data.addAll(Arrays.asList(
                                i + "", word.getText(), word.getConfidence() + "",
                                rect.x + "", rect.y + "", rect.width + "", rect.height + ""
                        ));
                        wordsTableController.addData(data);
                        wordsTableController.displayHtml();
                    }
                } else {
                    wordsTableController.clear();
                }

            }

            @Override
            protected void finalAction() {
                super.finalAction();
                ocrOptionsController.clearResults();
            }

        };
        start(task);
    }

    @Override
    public void cleanPane() {
        try {
            if (process != null) {
                process.destroy();
                process = null;
            }
            if (loading != null) {
                loading.closeStage();
                loading = null;
            }
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
