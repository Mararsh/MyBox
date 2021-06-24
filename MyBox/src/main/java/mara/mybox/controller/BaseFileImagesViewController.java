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
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

/**
 * @Author Mara
 * @CreateDate 2021-5-22
 * @License Apache License Version 2.0
 */
public abstract class BaseFileImagesViewController extends ImageViewerController {

    protected final int ThumbWidth = 200;
    protected int percent;
    protected Task thumbTask;
    protected int orcPage;
    protected LoadingController loading;
    protected Process process;

    @FXML
    protected ComboBox<String> percentBox, dpiBox, pageSelector;
    @FXML
    protected CheckBox thumbCheck;
    @FXML
    protected SplitPane mainPane;
    @FXML
    protected ScrollPane thumbScrollPane;
    @FXML
    protected VBox thumbBox;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab imageTab, ocrTab, ocrOptionsTab;
    @FXML
    protected TextArea ocrArea;
    @FXML
    protected Label pageLabel, resultLabel;
    @FXML
    protected ControlOCROptions ocrOptionsController;

    public BaseFileImagesViewController() {
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.All, VisitHistory.FileType.Image);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initImageView();
            if (ocrOptionsController != null) {
                ocrOptionsController.setParameters(this, false, false);
            }

            if (pageSelector != null) {
                pageSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        checkCurrentPage();
                    }
                });
            }

            if (thumbCheck != null) {
                thumbCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Thumbnails", false));
                thumbCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        checkThumbs();
                        AppVariables.setUserConfigValue(baseName + "Thumbnails", thumbCheck.isSelected());
                    }
                });

                thumbScrollPane.vvalueProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                        loadThumbs();
                    }
                });
            }

            if (percentBox != null) {
                percentBox.getItems().addAll(Arrays.asList("100", "75", "50", "125", "150", "200", "80", "25", "30", "15"));
                percent = AppVariables.getUserConfigInt(baseName + "Percent", 100);
                if (percent < 0) {
                    percent = 100;
                }
                percentBox.getSelectionModel().select(percent + "");
                percentBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        try {
                            int v = Integer.valueOf(newValue);
                            if (v > 0) {
                                percent = v;
                                AppVariables.setUserConfigInt(baseName + "Percent", percent);
                                setPercent(percent);
                                FxmlControl.setEditorNormal(percentBox);
                            } else {
                                FxmlControl.setEditorBadStyle(percentBox);
                            }

                        } catch (Exception e) {
                            FxmlControl.setEditorBadStyle(percentBox);
                        }
                    }
                });
            }

            if (dpiBox != null) {
                dpiBox.getItems().addAll(Arrays.asList("96", "72", "120", "160", "240", "300", "400", "600"));
                dpi = AppVariables.getUserConfigInt(baseName + "DPI", 96);
                if (dpi < 0) {
                    dpi = 96;
                }
                dpiBox.getSelectionModel().select(dpi + "");
                dpiBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        try {
                            int v = Integer.valueOf(newValue);
                            if (v > 0) {
                                dpi = v;
                                AppVariables.setUserConfigInt(baseName + "DPI", dpi);
                                loadPage();
                                FxmlControl.setEditorNormal(dpiBox);
                            } else {
                                FxmlControl.setEditorBadStyle(dpiBox);
                            }
                        } catch (Exception e) {
                            FxmlControl.setEditorBadStyle(dpiBox);
                        }
                    }
                });
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void initViewPane() {
        try {
            super.initViewPane();

            if (tabPane != null) {
                tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                    @Override
                    public void changed(ObservableValue ov, Tab oldValue, Tab newValue) {
                        if (!ocrTab.equals(newValue) || orcPage == frameIndex
                                || imageView.getImage() == null) {
                            return;
                        }
                        startOCR();
                    }
                });
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void viewSizeChanged(double change) {
        if (isSettingValues || imageView.getImage() == null) {
            return;
        }
        super.viewSizeChanged(change);
        percent = (int) (imageView.getFitHeight() * 100 / imageView.getImage().getHeight());
        isSettingValues = true;
        percentBox.getSelectionModel().select(percent + "");
        isSettingValues = false;
    }

    @Override
    public void initImageView() {
        try {
            if (imageView == null) {
                return;
            }
            mainPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));

            scrollPane.addEventHandler(ScrollEvent.SCROLL, new EventHandler<ScrollEvent>() {
                @Override
                public void handle(ScrollEvent event) {
                    double deltaY = event.getDeltaY();
                    if (event.isControlDown()) {
                    } else {
                        if (deltaY > 0) {
                            if (scrollPane.getVvalue() == scrollPane.getVmin()) {
                                event.consume();
                                pagePreviousAction();
                            }
                        } else {

                            if (scrollPane.getHeight() >= imageView.getFitHeight()
                                    || scrollPane.getVvalue() == scrollPane.getVmax()) {
                                event.consume();
                                pageNextAction();
                            }
                        }
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected boolean checkCurrentPage() {
        if (isSettingValues || pageSelector == null) {
            return false;
        }
        String value = pageSelector.getEditor().getText();
        try {
            int v = Integer.valueOf(value) - 1;
            if (v >= 0 && v < framesNumber) {
                setCurrentPage(v);
                loadPage();
                pageSelector.getEditor().setStyle(null);
                return true;
            } else {
                pageSelector.getEditor().setStyle(badStyle);
                return false;
            }
        } catch (Exception e) {
            pageSelector.getEditor().setStyle(badStyle);
            return false;
        }
    }

    protected void checkThumbs() {
        if (thumbCheck.isSelected()) {
            if (!mainPane.getItems().contains(thumbScrollPane)) {
                mainPane.getItems().add(0, thumbScrollPane);
            }
            loadThumbs();

        } else {
            if (mainPane.getItems().contains(thumbScrollPane)) {
                mainPane.getItems().remove(thumbScrollPane);
            }
        }
        adjustSplitPane();

    }

    protected void adjustSplitPane() {
        try {
            int size = mainPane.getItems().size();
            switch (size) {
                case 1:
                    mainPane.setDividerPositions(1);
                    break;
                case 2:
                    mainPane.setDividerPosition(0, 0.3);
                    break;
                case 3:
                    mainPane.setDividerPosition(0, 0.2);
                    mainPane.setDividerPosition(1, 0.5);
                    break;
            }
            mainPane.layout();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setSourceFile(File file) {
        sourceFile = file;
        orcPage = -1;
        if (imageView != null) {
            imageView.setImage(null);
            imageView.setTranslateX(0);
        }
    }

    protected void setTotalPages(int total) {
        framesNumber = total;
    }

    protected void setCurrentPage(int page) {
        frameIndex = page;
    }

    public void setImage(Image image, int percent) {
        if (imageView == null) {
            return;
        }
        imageView.setPreserveRatio(true);
        imageView.setImage(image);
        this.image = image;
        getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath() + " - " + message("Page") + " " + frameIndex);
        if (percent == 0) {
            paneSize();
        } else {
            setPercent(percent);
        }
        refinePane();
        setMaskStroke();
        checkSelect();
        setImageChanged(false);
        updateLabelsTitle();
        imageView.requestFocus();
        if (tabPane != null && tabPane.getSelectionModel().getSelectedItem() == ocrTab) {
            startOCR();
        }
    }

    protected void setPercent(int percent) {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        double w = imageView.getImage().getWidth();
        imageView.setFitWidth(w * percent / 100);
        double h = imageView.getImage().getHeight();
        imageView.setFitHeight(h * percent / 100);
    }

    public void initPage(File file, int page) {
        try {
            sourceFile = file;
            setTotalPages(0);
            setCurrentPage(page);
            percent = 0;
            isSettingValues = true;
            pageSelector.getItems().clear();
            pageSelector.setValue(null);
            thumbBox.getChildren().clear();
            isSettingValues = false;
            bottomLabel.setText("");
            pageLabel.setText("");
            setSourceFile(file);

            if (file == null) {
                getMyStage().setTitle(getBaseTitle());
                return;
            }
            getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath());
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void loadPage(int pageNumber) {
        setCurrentPage(pageNumber);
        loadPage();
    }

    public void initCurrentPage() {
        if (frameIndex < 0) {
            setCurrentPage(0);
        } else if (frameIndex >= framesNumber) {
            setCurrentPage(framesNumber - 1);
        }
        isSettingValues = true;
        pageSelector.setValue((frameIndex + 1) + "");
        isSettingValues = false;
        pagePreviousButton.setDisable(frameIndex <= 0);
        pageNextButton.setDisable(frameIndex >= (framesNumber - 1));
        bottomLabel.setText("");
    }

    // frameIndex is 0-based
    protected void loadPage() {
        initCurrentPage();
        if (sourceFile == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                task.cancel();
            }
            task = new SingletonTask<Void>() {

                private Image image;

                @Override
                protected boolean handle() {
                    image = readPageImage();
                    return image != null;
                }

                @Override
                protected void whenSucceeded() {
                    setImage(image, percent);
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL, MessageFormat.format(message("LoadingPageNumber"), (frameIndex + 1) + ""));
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    // return error
    protected Image readPageImage() {
        return null;
    }

    protected Map<Integer, Image> readThumbs(int pos, int end) {
        return null;
    }

    protected void loadThumbs() {
        synchronized (this) {
            if (thumbTask != null) {
                thumbTask.cancel();
            }
            if (thumbBox.getChildren().isEmpty()) {
                for (int i = 0; i < framesNumber; ++i) {
                    ImageView view = new ImageView();
                    view.setFitHeight(50);
                    view.setPreserveRatio(true);
                    thumbBox.getChildren().add(view);
                    thumbBox.getChildren().add(new Label((i + 1) + ""));
                }
            }
            int pos = Math.max(0, (int) (framesNumber * thumbScrollPane.getVvalue() / thumbScrollPane.getVmax()) - 1);
            ImageView view = (ImageView) thumbBox.getChildren().get(pos * 2);
            if (view.getImage() != null) {
                return;
            }
            int end = Math.min(pos + 20, framesNumber);
            thumbTask = new SingletonTask<Void>() {

                protected Map<Integer, Image> images;

                @Override
                protected boolean handle() {
                    images = readThumbs(pos, end);
                    return images != null;
                }

                @Override
                protected void whenSucceeded() {
                    for (int i = pos; i < end; ++i) {
                        if (i >= images.size()) {
                            break;
                        }
                        ImageView view = (ImageView) thumbBox.getChildren().get(2 * i);
                        if (view.getImage() != null) {
                            continue;
                        }
                        view.setImage(images.get(i));
                        view.setFitHeight(view.getImage().getHeight());
                        final int p = i;
                        view.setOnMouseClicked(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent event) {
                                loadPage(p);
                            }
                        });
                    }
                    thumbBox.layout();
                    adjustSplitPane();
                }

            };
            openHandlingStage(thumbTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(thumbTask);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    public void goPage() {
        checkCurrentPage();
    }

    @FXML
    @Override
    public void pageNextAction() {
        if (frameIndex >= framesNumber - 1) {
            return;
        }
        setCurrentPage(++frameIndex);
        loadPage();
    }

    @FXML
    @Override
    public void pagePreviousAction() {
        if (frameIndex <= 0) {
            return;
        }
        setCurrentPage(--frameIndex);
        loadPage();
    }

    @FXML
    @Override
    public void pageFirstAction() {
        setCurrentPage(0);
        loadPage();
    }

    @FXML
    @Override
    public void pageLastAction() {
        setCurrentPage(framesNumber - 1);
        loadPage();

    }

    @Override
    public void setDafultMaskRectangleValues() {
        if (imageView == null || maskPane == null || maskRectangleLine == null) {
            return;
        }
        if (maskRectangleData == null
                || maskRectangleData.getWidth() > imageView.getImage().getWidth()
                || maskRectangleData.getHeight() > imageView.getImage().getHeight()) {
            maskRectangleData = new DoubleRectangle(0, 0,
                    imageView.getImage().getWidth() - 1, imageView.getImage().getHeight() - 1);
        }
    }

    @Override
    public File imageFile() {
        return null;
    }

    /*
        OCR
     */
    @FXML
    public void startOCR() {
        if (imageView.getImage() == null) {
            return;
        }
        ocrOptionsController.setLanguages();
        File dataPath = ocrOptionsController.dataPathController.file;
        if (!dataPath.exists()) {
            popError(message("InvalidParameters"));
            ocrOptionsController.dataPathController.fileInput.setStyle(badStyle);
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
            popError(message("InvalidParameters"));
            ocrOptionsController.tesseractPathController.fileInput.setStyle(badStyle);
            return;
        }
        if (parentController != null) {
            loading = parentController.openHandlingStage(Modality.WINDOW_MODAL);
        } else {
            loading = openHandlingStage(Modality.WINDOW_MODAL);
        }

        new Thread() {
            private String outputs = "";

            @Override
            public void run() {
                try {
                    Image selected = cropImage();
                    if (selected == null) {
                        selected = imageView.getImage();
                    }
                    String imageFile = FileTools.getTempFile(".png").getAbsolutePath();
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(selected, null);
                    bufferedImage = ImageManufacture.removeAlpha(bufferedImage);
                    ImageFileWriters.writeImageFile(bufferedImage, "png", imageFile);

                    int version = ocrOptionsController.tesseractVersion();
                    String fileBase = FileTools.getTempFile().getAbsolutePath();
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
                    File configFile = FileTools.getTempFile();
                    String s = "tessedit_create_txt 1\n";
                    Map<String, String> p = ocrOptionsController.checkParameters();
                    if (p != null) {
                        for (String key : p.keySet()) {
                            s += key + "\t" + p.get(key) + "\n";
                        }
                    }
                    FileTools.writeFile(configFile, s, Charset.forName("utf-8"));
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
                        texts = FileTools.readTexts(txtFile);
                        FileTools.delete(txtFile);
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
                                resultLabel.setText(MessageFormat.format(message("OCRresults"),
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
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

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
                        Image selected = cropImage();
                        if (selected == null) {
                            selected = imageView.getImage();
                        }
                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(selected, null);
                        bufferedImage = ImageManufacture.removeAlpha(bufferedImage);
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
                    if (texts != null) {
                        ocrArea.setText(texts);
                        resultLabel.setText(MessageFormat.format(message("OCRresults"),
                                texts.length(), DateTools.datetimeMsDuration(new Date().getTime() - startTime.getTime())));
                        orcPage = frameIndex;
                        tabPane.getSelectionModel().select(ocrTab);
                    } else {
                        popWarn(message("OCRMissComments"));
                    }
                }
            };
            task.setSelf(task);
            if (parentController != null) {
                parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            } else {
                openHandlingStage(task, Modality.WINDOW_MODAL);
            }
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }

    }

    @FXML
    public void editOCR() {
        if (ocrArea.getText().isEmpty()) {
            return;
        }
        TextEditerController controller = (TextEditerController) FxmlStage.openStage(CommonValues.TextEditerFxml);
        controller.hideRightPane();
        controller.mainArea.setText(ocrArea.getText());
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (thumbTask != null && !thumbTask.isDone()) {
            thumbTask.cancel();
            thumbTask = null;
        }
        if (process != null) {
            process.destroy();
            process = null;
        }
        if (loading != null) {
            loading.closeStage();
            loading = null;
        }
        return super.checkBeforeNextAction();
    }

}
