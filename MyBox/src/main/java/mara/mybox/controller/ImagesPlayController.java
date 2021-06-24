package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import mara.mybox.data.BaseTask;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageInformation;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;

/**
 * @Author Mara
 * @CreateDate 2021-5-29
 * @License Apache License Version 2.0
 */
public class ImagesPlayController extends ImageViewerController {

    protected final List<ImageInformation> imageInfos;
    protected int queueSize, interval, fileFramesSize, fromFrame, toFrame;
    protected double speed;
    protected String fileFormat;
    protected boolean isTransparent;
    protected LoadingController loading;
    protected long memoryThreadhold, currentDelay, reloadDelay;
    protected Thread loadThread;

    @FXML
    protected ToggleGroup typeGroup;
    @FXML
    protected RadioButton imagesRadio, pdfRadio, pptRadio;
    @FXML
    protected ComboBox<String> speedSelector, intervalSelector;
    @FXML
    protected CheckBox transparentBackgroundCheck;
    @FXML
    protected Button pauseButton, editButton, goFramesButton, thumbsListButton;
    @FXML
    protected VBox fileVBox, imageBox, pdfBox;
    @FXML
    protected Label promptLabel;
    @FXML
    protected TextField fromInput, toInput;
    @FXML
    protected FlowPane framesPane;

    public ImagesPlayController() {
        baseTitle = AppVariables.message("ImagesPlay");
        TipsLabelKey = "ImagesPlayTips";

        imageInfos = new ArrayList<>();
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Image);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            memoryThreadhold = 200 * 1024 * 1024;

            typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (pdfRadio.isSelected()) {
                        setFileType(VisitHistory.FileType.PDF);
                        if (!fileVBox.getChildren().contains(pdfBox)) {
                            fileVBox.getChildren().add(3, pdfBox);
                        }
                    } else if (pptRadio.isSelected()) {
                        setFileType(VisitHistory.FileType.PPT);
                        if (fileVBox.getChildren().contains(pdfBox)) {
                            fileVBox.getChildren().remove(pdfBox);
                        }
                    } else {
                        setFileType(VisitHistory.FileType.Image);
                        if (fileVBox.getChildren().contains(pdfBox)) {
                            fileVBox.getChildren().remove(pdfBox);
                        }
                    }
                }
            });

            fromFrame = 1;
            fromInput.setText("1");
            toFrame = -1;
            toInput.setText("-1");

            isTransparent = AppVariables.getUserConfigBoolean(baseName + "Transparent", false);
            transparentBackgroundCheck.setSelected(isTransparent);
            transparentBackgroundCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    isTransparent = transparentBackgroundCheck.isSelected();
                    AppVariables.setUserConfigValue(baseName + "Transparent", transparentBackgroundCheck.isSelected());
                    if (fileFormat != null && fileFormat.equalsIgnoreCase("pdf")) {
                        reloadImages();
                    }
                }
            });

            imageBox.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            viewPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));

            frameSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    try {
                        int v = Integer.valueOf(newValue);
                        pauseFrame(v - 1);
                    } catch (Exception e) {
                    }
                }
            });

            speed = 1.0;
            speedSelector.getItems().addAll(Arrays.asList(
                    "1", "1.5", "2", "0.5", "0.8", "1.2", "0.3", "3", "0.1", "5", "0.2", "8"
            ));
            speedSelector.setValue("1");
            speedSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        double v = Double.valueOf(newValue);
                        if (v <= 0) {
                            speedSelector.getEditor().setStyle(badStyle);
                        } else {
                            speed = v;
                            speedSelector.getEditor().setStyle(null);
                        }
                    } catch (Exception e) {
                        speedSelector.getEditor().setStyle(badStyle);
                    }
                }
            });

            intervalSelector.getItems().addAll(Arrays.asList(
                    "500", "200", "100", "1000", "50", "2000", "300", "3000", "20", "10", "6000", "30000", "12000", "60000"
            ));
            interval = AppVariables.getUserConfigInt(baseName + "Interval", 500);
            if (interval <= 0) {
                interval = 500;
            }
            intervalSelector.setValue(interval + "");
            intervalSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v <= 0) {
                            intervalSelector.getEditor().setStyle(badStyle);
                        } else {
                            interval = v;
                            intervalSelector.getEditor().setStyle(null);
                            AppVariables.setUserConfigInt(baseName + "Interval", v);
                            if (imageInfos != null) {
                                for (ImageInformation info : imageInfos) {
                                    info.setDuration(interval);
                                }
                            }
                        }
                    } catch (Exception e) {
                        speedSelector.getEditor().setStyle(badStyle);
                    }
                }
            });

            setPauseButton(false);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            fileVBox.getChildren().remove(pdfBox);
            FxmlControl.setTooltip(toInput, new Tooltip(message("ToPageComments")));
            FxmlControl.setTooltip(thumbsListButton, new Tooltip(message("ImagesEditor")));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    protected void setLoadWidth() {
        if (isSettingValues) {
            return;
        }
        reloadImages();
    }

    @Override
    public void checkDPI() {
        super.checkDPI();
        if (fileFormat != null && fileFormat.equalsIgnoreCase("pdf")) {
            reloadImages();
        }
    }

    public boolean checkMemory() {
        Runtime r = Runtime.getRuntime();
        long availableMem = r.maxMemory() - (r.totalMemory() - r.freeMemory());
        return availableMem >= memoryThreadhold;
    }

    public void clearList() {
        if (timer != null) {
            timer.cancel();
        }
        if (task != null && !task.isQuit()) {
            task.cancel();
        }
        fileFramesSize = 0;
        framesNumber = 0;
        frameIndex = 0;
        sourceFile = null;
        fileFormat = null;
        image = null;
        if (imageInfos != null) {
            imageInfos.clear();
        }
    }

    @Override
    public void sourceFileChanged(File file) {
        synchronized (this) {
            clearList();
            sourceFile = file;
            framesPane.setDisable(false);
            if (sourceFile == null) {
                return;
            }
            fileFormat = FileTools.getFileSuffix(sourceFile);
            if (fileFormat == null || fileFormat.isBlank()) {
                popError(message("NotSupport"));
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    if (fileFormat.equalsIgnoreCase("pdf")) {
                        return loadPDF();
                    } else if (fileFormat.equalsIgnoreCase("ppt") || fileFormat.equalsIgnoreCase("pptx")) {
                        return loadPPT();
                    } else {
                        return loadImageFile();
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (error != null && !error.isBlank()) {
                        alertError(error);
                    }
                    framesPane.setDisable(false);
                    playImages();
                }
            };
            loading = openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }

    }

    // Read images as more as possible
    protected boolean loadImageFile() {
        imageInfos.clear();
        Platform.runLater(() -> {
            imagesRadio.fire();
            thumbsListButton.setDisable(false);
        });
        if (sourceFile == null) {
            return false;
        }
        try ( ImageInputStream iis = ImageIO.createImageInputStream(sourceFile)) {
            ImageReader reader = ImageFileReaders.getReader(iis);
            if (reader == null) {
                return false;
            }
            reader.setInput(iis, false, false);
            ImageFileInformation fileInfo = new ImageFileInformation(sourceFile);
            if (loading != null) {
                loading.setInfo(message("Loading") + " " + message("MetaData"));
            }
            ImageFileReaders.readImageFileMetaData(reader, fileInfo);
            imageInfos.addAll(fileInfo.getImagesInformation());
            if (imageInfos == null) {
                reader.dispose();
                return false;
            }
            fileFramesSize = imageInfos.size();
            boolean notGif = !fileFormat.equalsIgnoreCase("gif");
            int start = fromFrame - 1;
            if (start < 0 || start >= fileFramesSize) {
                start = 0;
            }
            int end = toFrame - 1;
            if (end <= 0 || end >= fileFramesSize) {
                end = fileFramesSize - 1;
            }
            for (int i = start; i <= end; i++) {
                ImageInformation imageInfo = imageInfos.get(i);
                if (task == null || task.isCancelled()) {
                    reader.dispose();
                    return false;
                }
                if (!checkMemory()) {
                    if (task != null) {
                        task.setError(message("OutOfMeomey"));
                    }
                    loading.setInfo(message("OutOfMeomey"));
                    break;
                }
                int index = imageInfo.getIndex();
                if (loading != null) {
                    loading.setInfo(message("Loading") + " " + index + " / " + fileFramesSize);
                }
                if (notGif) {
                    imageInfo.setDuration(interval);
                }
                int imageWidth = imageInfo.getWidth();
                int targetWidth = loadWidth <= 0 ? imageInfo.getWidth() : loadWidth;
                int maxWidth = ImageInformation.countMaxWidth(imageInfo);
                if (targetWidth > maxWidth) {
                    System.gc();
                    maxWidth = ImageInformation.countMaxWidth(imageInfo);
                }
                if (targetWidth > maxWidth) {
                    if (task != null) {
                        task.setError(message("OutOfMeomey"));
                    }
                    loading.setInfo(message("OutOfMeomey"));
                    break;
                }
                ImageReadParam param = reader.getDefaultReadParam();
                int scale = targetWidth / imageWidth;
                if (scale > 1) {
                    param.setSourceSubsampling(scale, scale, 0, 0);
                }
                BufferedImage frame;
                try {
                    frame = reader.read(imageInfo.getIndex(), param);
                } catch (Exception e) {
                    frame = ImageFileReaders.readBrokenImage((Exception) e,
                            sourceFile.getAbsolutePath(), index, null, loadWidth);
                }
                if (frame == null) {
                    break;
                }
                if (task == null || task.isCancelled()) {
                    reader.dispose();
                    return false;
                }
                frame = ImageManufacture.scaleImageWidthKeep(frame, targetWidth);
                imageInfo.setImageType(frame.getType());
                imageInfo.setThumbnail(SwingFXUtils.toFXImage(frame, null));
            }
            reader.dispose();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return false;
        }
        return task != null && !task.isCancelled();
    }

    public boolean loadPPT() {
        imageInfos.clear();
        Platform.runLater(() -> {
            pptRadio.fire();
            thumbsListButton.setDisable(true);
        });
        if (sourceFile == null) {
            return false;
        }
        try ( SlideShow ppt = SlideShowFactory.create(sourceFile)) {
            List<Slide> slides = ppt.getSlides();
            int width = ppt.getPageSize().width;
            int height = ppt.getPageSize().height;
            fileFramesSize = slides.size();
            for (int i = 0; i < fileFramesSize; i++) {
                ImageInformation imageInfo = new ImageInformation(sourceFile);
                imageInfo.setIndex(i);
                imageInfo.setWidth(width);
                imageInfo.setHeight(height);
                imageInfo.setDuration(interval);
                imageInfos.add(imageInfo);
            }
            int start = fromFrame - 1;
            if (start < 0 || start >= fileFramesSize) {
                start = 0;
            }
            int end = toFrame - 1;
            if (end <= 0 || end >= fileFramesSize) {
                end = fileFramesSize - 1;
            }
            for (int i = start; i <= end; i++) {
                ImageInformation imageInfo = imageInfos.get(i);
                if (task == null || task.isCancelled()) {
                    return false;
                }
                if (!checkMemory()) {
                    if (task != null) {
                        task.setError(message("OutOfMeomey"));
                    }
                    loading.setInfo(message("OutOfMeomey"));
                    break;
                }
                if (loading != null) {
                    loading.setInfo(message("Loading") + " " + i + " / " + fileFramesSize);
                }
                try {
                    Slide slide = slides.get(i);
                    BufferedImage slideImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    slide.draw(slideImage.createGraphics());
                    if (task == null || task.isCancelled()) {
                        return false;
                    }
                    int targetWidth = loadWidth <= 0 ? width : loadWidth;
                    if (slideImage.getWidth() != targetWidth) {
                        slideImage = ImageManufacture.scaleImageWidthKeep(slideImage, targetWidth);
                    }
                    if (task == null || task.isCancelled()) {
                        return false;
                    }
                    imageInfo.setThumbnail(SwingFXUtils.toFXImage(slideImage, null));
                    imageInfo.setImageType(slideImage.getType());
                } catch (Exception e) {
                    MyBoxLog.debug(e.toString());
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
        return true;
    }

    public boolean loadPDF() {
        imageInfos.clear();
        Platform.runLater(() -> {
            pdfRadio.fire();
            thumbsListButton.setDisable(true);
        });
        if (sourceFile == null) {
            return false;
        }
        try ( PDDocument doc = PDDocument.load(sourceFile, AppVariables.pdfMemUsage)) {
            fileFramesSize = doc.getNumberOfPages();
            for (int i = 0; i < fileFramesSize; i++) {
                ImageInformation imageInfo = new ImageInformation(sourceFile);
                imageInfo.setIndex(i);
                imageInfo.setDuration(interval);
                imageInfos.add(imageInfo);
            }
            PDFRenderer renderer = new PDFRenderer(doc);
            ImageType type = ImageType.RGB;
            if (isTransparent) {
                type = ImageType.ARGB;
            }
            int start = fromFrame - 1;
            if (start < 0 || start >= fileFramesSize) {
                start = 0;
            }
            int end = toFrame - 1;
            if (end <= 0 || end >= fileFramesSize) {
                end = fileFramesSize - 1;
            }
            for (int i = start; i <= end; i++) {
                ImageInformation imageInfo = imageInfos.get(i);
                if (task == null || task.isCancelled()) {
                    return false;
                }
                if (!checkMemory()) {
                    if (task != null) {
                        task.setError(message("OutOfMeomey"));
                    }
                    loading.setInfo(message("OutOfMeomey"));
                    break;
                }
                if (loading != null) {
                    loading.setInfo(message("Loading") + " " + i + " / " + fileFramesSize);
                }
                try {
                    BufferedImage bufferedImage = renderer.renderImageWithDPI(i, dpi, type);
                    if (task == null || task.isCancelled()) {
                        return false;
                    }
                    int imageWidth = bufferedImage.getWidth();
                    imageInfo.setWidth(imageWidth);
                    imageInfo.setHeight(bufferedImage.getHeight());
                    imageInfo.setImageType(bufferedImage.getType());
                    int targetWidth = loadWidth <= 0 ? imageWidth : loadWidth;
                    if (imageWidth != targetWidth) {
                        bufferedImage = ImageManufacture.scaleImageWidthKeep(bufferedImage, targetWidth);
                    }
                    if (task == null || task.isCancelled()) {
                        return false;
                    }
                    imageInfo.setThumbnail(SwingFXUtils.toFXImage(bufferedImage, null));
                } catch (Exception e) {
                    MyBoxLog.debug(e.toString());
                }
            }
            doc.close();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
        return true;
    }

    public void loadImages(List<ImageInformation> infos) {
        synchronized (this) {
            clearList();
            if (infos == null || infos.isEmpty()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
                        for (ImageInformation info : infos) {
                            imageInfos.add(info.base());
                        }
                        framesNumber = imageInfos.size();
                        for (int i = 0; i < framesNumber; i++) {
                            if (task == null || task.isCancelled()) {
                                return false;
                            }
                            ImageInformation info = imageInfos.get(i);
                            if (info.getDuration() < 0) {
                                info.setDuration(interval);
                            }
                            if (!checkMemory()) {
                                if (task != null) {
                                    task.setError(message("OutOfMeomey"));
                                }
                                loading.setInfo(message("OutOfMeomey"));
                                break;
                            }
                            if (loading != null) {
                                loading.setInfo(message("Loading") + " " + i + " / " + framesNumber);
                            }
                            int targetWidth = loadWidth <= 0 ? info.getWidth() : loadWidth;
                            Image thumb = info.getThumbnail();
                            if (thumb != null && thumb.getWidth() == targetWidth) {
                                continue;
                            }
                            int maxWidth = ImageInformation.countMaxWidth(info);
                            if (targetWidth > maxWidth) {
                                System.gc();
                                maxWidth = ImageInformation.countMaxWidth(info);
                            }
                            if (targetWidth > maxWidth) {
                                if (task != null) {
                                    task.setError(message("OutOfMeomey"));
                                }
                                loading.setInfo(message("OutOfMeomey"));
                                break;
                            }
                            info.loadThumbnail(targetWidth);
                        }
                        return true;
                    } catch (Exception e) {
                        if (task != null) {
                            task.setError(e.toString());
                        }
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (error != null && !error.isBlank()) {
                        alertError(error);
                    }
                    thumbsListButton.setDisable(false);
                    framesPane.setDisable(true);
                    playImages();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }

    }

    @FXML
    public void goFrames() {
        if (fileFormat == null) {
            return;
        }
        String value = fromInput.getText();
        int f = CommonValues.InvalidInteger;
        if (value == null || value.isBlank()) {
            f = 1;
            fromInput.setStyle(null);
        } else {
            try {
                int v = Integer.valueOf(value);
                if (v < 0) {
                    fromInput.setStyle(badStyle);
                } else {
                    f = v;
                    fromInput.setStyle(null);
                }
            } catch (Exception e) {
                fromInput.setStyle(badStyle);
            }
        }
        int t = CommonValues.InvalidInteger;
        value = toInput.getText();
        if (value == null || value.isBlank()) {
            t = -1;
            toInput.setStyle(null);
        } else {
            try {
                int v = Integer.valueOf(value);
                if (v < 0) {
                    t = -1;
                    toInput.setStyle(null);
                } else {
                    t = v;
                    toInput.setStyle(null);
                }
            } catch (Exception e) {
                toInput.setStyle(badStyle);
            }
        }
        if (f == CommonValues.InvalidInteger || t == CommonValues.InvalidInteger
                || (t > 0 && f > t)) {
            popError(message("InvalidParametes"));
            return;
        }
        fromFrame = f;
        toFrame = t;
        reloadImages();
    }

    public void reloadImages() {
        if (sourceFile != null) {
            sourceFileChanged(sourceFile);
        } else if (imageInfos != null && !imageInfos.isEmpty()) {
            List<ImageInformation> infos = new ArrayList<>();
            infos.addAll(imageInfos);
            loadImages(infos);
        }
    }

    public synchronized boolean playImages() {
        try {
            if (timer != null) {
                timer.cancel();
            }
            frameIndex = 0;
            frameSelector.getItems().clear();
            if (imageInfos == null) {
                return false;
            }
            framesNumber = imageInfos.size();
            if (framesNumber == 0) {
                return false;
            }
            List<String> frames = new ArrayList<>();
            if (fromFrame <= 0 || fromFrame > framesNumber) {
                fromFrame = 1;
            }
            if (toFrame <= 0 || toFrame > framesNumber) {
                toFrame = framesNumber;
            }
            if (fromFrame > toFrame) {
                return false;
            }
            isSettingValues = true;
            for (int i = fromFrame; i <= toFrame; ++i) {
                frames.add(i + "");
            }
            frameSelector.getItems().addAll(frames);
            frameSelector.setValue(fromFrame + "");
            isSettingValues = false;
//            if (loadThread != null) {
//                loadThread = new LoadThread();
//            }
            goFrame(fromFrame - 1);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public synchronized void goFrame(int start) {
        try {
            if (timer != null) {
                timer.cancel();
            }
            setPauseButton(false);
            if (imageInfos == null || framesNumber < 1) {
                return;
            }
            displayFrame(start);
            int index;
            long delay;
            index = frameIndex + 1;
            delay = currentDelay;
//            if (image != null) {
//                index = frameIndex + 1;
//                delay = currentDelay;
//            } else {
//                index = frameIndex;
//                delay = reloadDelay;
//            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        goFrame(index);
                    });
                }
            }, delay);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void pauseFrame(int frame) {
        try {
            if (framesNumber == 0) {
                return;
            }
            if (timer != null) {
                timer.cancel();
            }
            setPauseButton(true);
            displayFrame(frame);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    // index is 0-based
    protected void displayFrame(int index) {
        try {
            if (imageInfos == null || framesNumber < 1) {
                return;
            }
            int end = toFrame - 1;
            if (end <= 0) {
                end = framesNumber - 1;
            }
            frameIndex = index;
            if (frameIndex > end) {
                frameIndex = fromFrame - 1;
            }
            if (frameIndex < 0) {
                frameIndex = end;
            }
            imageInformation = imageInfos.get(frameIndex);
            if (imageInformation != null) {
                speed = speed <= 0 ? 1 : speed;
                currentDelay = (int) (imageInformation.getDuration() / speed);
            } else {
                currentDelay = reloadDelay;
            }
            image = thumb(imageInformation);
            if (image == null) {  // Not implement reading image in loop in this version
//                if (loadThread != null) {
//                    loadThread = new LoadThread();
//                }
//                if (!loadThread.isAlive()) {
//                    loadThread.start();
//                }
//                synchronized (imageInfos) {
//                    imageInfos.notify();
//                }
            }
            imageView.setImage(image);
            refinePane();
            promptLabel.setText(AppVariables.message("TotalFrames") + ": " + framesNumber + "  "
                    + AppVariables.message("CurrentFrame") + ": " + (frameIndex + 1) + "  "
                    + AppVariables.message("DurationMilliseconds") + ": " + currentDelay + "  "
                    + AppVariables.message("Size") + ": "
                    + (image == null ? "" : (int) image.getWidth()
                            + "*" + (int) image.getHeight()));
            isSettingValues = true;
            frameSelector.getSelectionModel().select((frameIndex + 1) + "");
            isSettingValues = false;

            updateLabelsTitle();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected Image thumb(ImageInformation info) {
        try {
            if (info == null) {
                return null;
            }
            int imageWidth = info.getWidth();
            int targetWidth = loadWidth <= 0 ? imageWidth : loadWidth;
            Image thumb = info.getThumbnail();
            if (thumb != null && thumb.getWidth() == targetWidth) {
                return thumb;
            }
            return info.loadThumbnail(targetWidth);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @Override
    public File imageFile() {
        if (fileFormat == null || fileFormat.equalsIgnoreCase("pdf")
                || fileFormat.equalsIgnoreCase("ppt") || fileFormat.equalsIgnoreCase("pptx")) {
            return null;
        } else {
            return sourceFile;
        }
    }

    protected void setPauseButton(boolean setAsPaused) {
        if (setAsPaused) {
            ControlStyle.setNameIcon(pauseButton, message("Continue"), "iconPlay.png");
            previousButton.setDisable(false);
            nextButton.setDisable(false);
            pauseButton.setUserData("Paused");
        } else {
            ControlStyle.setNameIcon(pauseButton, message("Pause"), "iconPause.png");
            previousButton.setDisable(true);
            nextButton.setDisable(true);
            pauseButton.setUserData("Playing");
        }
        pauseButton.applyCss();
    }

    @FXML
    public void pauseAction() {
        try {
            if (pauseButton.getUserData().equals("Playing")) {
                pauseFrame(frameIndex);

            } else if (pauseButton.getUserData().equals("Paused")) {
                goFrame(frameIndex);

            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void previousAction() {
        try {
            pauseFrame(--frameIndex);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void nextAction() {
        try {
            pauseFrame(++frameIndex);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void viewAction() {
        try {
            if (fileFormat != null) {
                if (fileFormat.equalsIgnoreCase("pdf")) {
                    PdfViewController controller
                            = (PdfViewController) openStage(CommonValues.PdfViewFxml);
                    controller.sourceFileChanged(sourceFile);
                    return;
                } else if (fileFormat.equalsIgnoreCase("ppt") || fileFormat.equalsIgnoreCase("pptx")) {
                    PptViewController controller
                            = (PptViewController) openStage(CommonValues.PptViewFxml);
                    controller.sourceFileChanged(sourceFile);
                    return;
                }
            }
            ImageViewerController controller
                    = (ImageViewerController) openStage(CommonValues.ImageViewerFxml);
            controller.loadImage(sourceFile, imageInformation, imageView.getImage());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void editFrames() {
        if (fileFormat != null
                && (fileFormat.equalsIgnoreCase("pdf") || fileFormat.equalsIgnoreCase("ppt") || fileFormat.equalsIgnoreCase("pptx"))) {
            return;
        }
        BaseTask editTask = new SingletonTask<Void>() {

            private List<ImageInformation> infos;

            @Override
            protected boolean handle() {
                try {
                    infos = new ArrayList<>();
                    if (imageInfos == null) {
                        return true;
                    }
                    for (ImageInformation info : imageInfos) {
                        Image thumb = info.getThumbnail();
                        if (thumb == null) {
                            continue;
                        }
                        ImageInformation newInfo = info.base();
                        newInfo.setThumbnail(FxmlImageManufacture.scaleImage(thumb, AppVariables.thumbnailWidth));
                        infos.add(newInfo);
                    }
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                ImagesEditorController controller = (ImagesEditorController) openStage(CommonValues.ImagesEditorFxml);
                controller.loadImages(infos);
            }
        };
        editTask.setSelf(editTask);
        Thread thread = new Thread(editTask);
        thread.setDaemon(false);
        thread.start();

    }

    /*
        Methods to read image in loop to avoid out of memory.
        Not useful in this version
     */
    protected synchronized Image readImageFile2(int index) {
        try {
            if (imageInfos == null || index < 0 || index >= framesNumber) {
                return null;
            }
            ImageInformation imageInfo = imageInfos.get(index);
            int imageWidth = imageInfo.getWidth();
            int targetWidth = loadWidth <= 0 ? imageWidth : loadWidth;
            File file = imageInfo.getFile();
            try ( ImageInputStream iis = ImageIO.createImageInputStream(file)) {
                ImageReader reader = ImageFileReaders.getReader(iis);
                if (reader == null) {
                    return null;
                }
                reader.setInput(iis, true, true);
                int maxWidth = ImageInformation.countMaxWidth(imageInfo);
                if (targetWidth > maxWidth) {
                    imageView.setImage(null);
                    System.gc();
                    maxWidth = ImageInformation.countMaxWidth(imageInfo);
                }
                if (targetWidth > maxWidth) {
                    for (int i = index; i >= 0; i++) {
                        ImageInformation infoi = imageInfos.get(i);
                        infoi.setThumbnail(null);
                        System.gc();
                        maxWidth = ImageInformation.countMaxWidth(imageInfo);
                        if (targetWidth <= maxWidth) {
                            break;
                        }
                    }
                }
                if (targetWidth > maxWidth) {
                    for (int i = imageInfos.size() - 1; i > index; i--) {
                        ImageInformation infoi = imageInfos.get(i);
                        infoi.setThumbnail(null);
                        System.gc();
                        maxWidth = ImageInformation.countMaxWidth(imageInfo);
                        if (targetWidth <= maxWidth) {
                            break;
                        }
                    }
                }
                if (targetWidth > maxWidth) {
                    ImageReadParam param = reader.getDefaultReadParam();
                    int scale = targetWidth / imageWidth;
                    if (scale > 1) {
                        param.setSourceSubsampling(scale, scale, 0, 0);
                    }
                    BufferedImage frame;
                    try {
                        frame = reader.read(imageInfo.getIndex(), param);
                    } catch (Exception e) {
                        frame = ImageFileReaders.readBrokenImage((Exception) e, imageInfo.getFileName(), imageInfo.getIndex(), null, loadWidth);
                    }
                    if (frame != null) {
                        frame = ImageManufacture.scaleImageWidthKeep(frame, targetWidth);
                        imageInfo.setImageType(frame.getType());
                        imageInfo.setThumbnail(SwingFXUtils.toFXImage(frame, null));
                    }
                }
                reader.dispose();
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
            }
            return imageInfo.getThumbnail();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    protected class LoadThread2 extends Thread {

        @Override
        public void run() {
            synchronized (imageInfos) {

                imageInfos.notify();
            }
        }

        public void checkThumbs() {
            synchronized (imageInfos) {
                imageInformation = imageInfos.get(frameIndex);
                image = thumb(imageInformation);
            }
        }
    }

    protected synchronized Image readImage2(int index, boolean retry) {
        try {
            if (imageInfos == null || index < 0 || index >= framesNumber) {
                return null;
            }
            if (loading != null) {
                loading.setInfo(message("Loading") + " " + index);
            }
            ImageInformation info = imageInfos.get(index);
            int targetWidth = loadWidth <= 0 ? info.getWidth() : loadWidth;
            Image thumb = info.getThumbnail();
            if (thumb != null && thumb.getWidth() == targetWidth) {
                return thumb;
            }
            int maxWidth = ImageInformation.countMaxWidth(info);
            if (maxWidth >= targetWidth) {
                return info.loadThumbnail(targetWidth);
            } else if (retry) {
                for (int i = 0; i < index; ++i) {
                    ImageInformation infoi = imageInfos.get(i);
                    infoi.setThumbnail(null);
                    infoi.setImage(null);
                    System.gc();
                    maxWidth = ImageInformation.countMaxWidth(info);
                    if (maxWidth >= targetWidth) {
                        return info.loadThumbnail(targetWidth);
                    }
                }
                for (int i = framesNumber - 1; i > index; --i) {
                    ImageInformation infoi = imageInfos.get(i);
                    infoi.setThumbnail(null);
                    infoi.setImage(null);
                    System.gc();
                    maxWidth = ImageInformation.countMaxWidth(info);
                    if (maxWidth >= targetWidth) {
                        return info.loadThumbnail(targetWidth);
                    }
                }
            }
            return null;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            return null;
        }
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (loadThread != null) {
            loadThread.interrupt();
            loadThread = null;
        }
        if (loading != null) {
            loading.closeStage();
            loading = null;
        }
        return super.checkBeforeNextAction();
    }

}
