package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import mara.mybox.bufferedimage.ImageFileInformation;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.data.BaseTask;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;
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
    protected int queueSize, fromFrame, toFrame;
    protected String fileFormat;
    protected boolean isTransparent;
    protected LoadingController loading;
    protected long memoryThreadhold;
    protected Thread loadThread;

    @FXML
    protected ToggleGroup typeGroup;
    @FXML
    protected RadioButton imagesRadio, pdfRadio, pptRadio;
    @FXML
    protected CheckBox transparentBackgroundCheck;
    @FXML
    protected Button goFramesButton, thumbsListButton;
    @FXML
    protected VBox fileVBox, imageBox, pdfBox;
    @FXML
    protected TextField fromInput, toInput;
    @FXML
    protected FlowPane framesPane;
    @FXML
    protected ControlPlay playController;

    public ImagesPlayController() {
        baseTitle = Languages.message("ImagesPlay");
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
                        setFileType(VisitHistory.FileType.PPTS);
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

            // Displayed values are 1-based while internal values are 0-based
            fromFrame = 0;
            toFrame = -1;
            fromInput.setText("1");
            toInput.setText("-1");

            isTransparent = UserConfig.getBoolean(baseName + "Transparent", false);
            transparentBackgroundCheck.setSelected(isTransparent);
            transparentBackgroundCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    isTransparent = transparentBackgroundCheck.isSelected();
                    UserConfig.setBoolean(baseName + "Transparent", transparentBackgroundCheck.isSelected());
                    if (fileFormat != null && fileFormat.equalsIgnoreCase("pdf")) {
                        reloadImages();
                    }
                }
            });

            imageBox.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            viewPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));

            playController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            fileVBox.getChildren().remove(pdfBox);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(toInput, new Tooltip(Languages.message("ToPageComments")));
            NodeStyleTools.setTooltip(thumbsListButton, new Tooltip(Languages.message("ImagesEditor")));
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
        framesNumber = 0;
        frameIndex = 0;
        sourceFile = null;
        fileFormat = null;
        image = null;
        imageInformation = null;
        if (imageInfos != null) {
            imageInfos.clear();
        }
        playController.clear();
        fileStatus();
    }

    public void fileStatus() {
        thumbsListButton.setDisable(sourceFile == null);
        viewButton.setDisable(sourceFile == null);
        framesPane.setDisable(sourceFile == null);
    }

    @Override
    public void sourceFileChanged(File file) {
        synchronized (this) {
            clearList();
            if (file == null) {
                return;
            }
            String format = FileNameTools.getFileSuffix(file);
            if (format == null || format.isBlank()) {
                popError(Languages.message("NotSupport"));
                return;
            }
            sourceFile = file;
            fileFormat = format;
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
                    fileStatus();
                    playImages();
                }
            };
            loading = start(task);
        }

    }

    // Read images as more as possible
    protected boolean loadImageFile() {
        imageInfos.clear();
        Platform.runLater(() -> {
            imagesRadio.fire();
            fileStatus();
        });
        if (sourceFile == null) {
            return false;
        }
        try ( ImageInputStream iis = ImageIO.createImageInputStream(sourceFile)) {
            ImageReader reader = ImageFileReaders.getReader(iis, FileNameTools.getFileSuffix(sourceFile));
            if (reader == null) {
                return false;
            }
            reader.setInput(iis, false, false);
            ImageFileInformation fileInfo = new ImageFileInformation(sourceFile);
            if (loading != null) {
                loading.setInfo(Languages.message("Loading") + " " + Languages.message("MetaData"));
            }
            ImageFileReaders.readImageFileMetaData(reader, fileInfo);
            imageInfos.addAll(fileInfo.getImagesInformation());
            if (imageInfos == null) {
                reader.dispose();
                return false;
            }
            framesNumber = imageInfos.size();
            int start = fromFrame;
            if (start < 0 || start >= framesNumber) {
                start = 0;
            }
            int end = toFrame;
            if (end <= 0 || end >= framesNumber) {
                end = framesNumber - 1;
            }
            boolean notGif = !fileFormat.equalsIgnoreCase("gif");
            for (int i = start; i <= end; i++) {
                ImageInformation imageInfo = imageInfos.get(i);
                if (task == null || task.isCancelled()) {
                    reader.dispose();
                    return false;
                }
                if (!checkMemory()) {
                    if (task != null) {
                        task.setError(Languages.message("OutOfMeomey"));
                    }
                    loading.setInfo(Languages.message("OutOfMeomey"));
                    break;
                }
                int index = imageInfo.getIndex();
                if (loading != null) {
                    loading.setInfo(Languages.message("Loading") + " " + index + " / " + framesNumber);
                }
                if (notGif) {
                    imageInfo.setDuration(playController.interval);
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
                        task.setError(Languages.message("OutOfMeomey"));
                    }
                    loading.setInfo(Languages.message("OutOfMeomey"));
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
                frame = ScaleTools.scaleImageWidthKeep(frame, targetWidth);
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
            fileStatus();
        });
        if (sourceFile == null) {
            return false;
        }
        try ( SlideShow ppt = SlideShowFactory.create(sourceFile)) {
            List<Slide> slides = ppt.getSlides();
            int width = ppt.getPageSize().width;
            int height = ppt.getPageSize().height;
            framesNumber = slides.size();
            for (int i = 0; i < framesNumber; i++) {
                ImageInformation imageInfo = new ImageInformation(sourceFile);
                imageInfo.setIndex(i);
                imageInfo.setWidth(width);
                imageInfo.setHeight(height);
                imageInfo.setDuration(playController.interval);
                imageInfos.add(imageInfo);
            }
            int start = fromFrame;
            if (start < 0 || start >= framesNumber) {
                start = 0;
            }
            int end = toFrame;
            if (end <= 0 || end >= framesNumber) {
                end = framesNumber - 1;
            }
            for (int i = start; i <= end; i++) {
                ImageInformation imageInfo = imageInfos.get(i);
                if (task == null || task.isCancelled()) {
                    return false;
                }
                if (!checkMemory()) {
                    if (task != null) {
                        task.setError(Languages.message("OutOfMeomey"));
                    }
                    loading.setInfo(Languages.message("OutOfMeomey"));
                    break;
                }
                if (loading != null) {
                    loading.setInfo(Languages.message("Loading") + " " + i + " / " + framesNumber);
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
                        slideImage = ScaleTools.scaleImageWidthKeep(slideImage, targetWidth);
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
            fileStatus();
        });
        if (sourceFile == null) {
            return false;
        }
        try ( PDDocument doc = PDDocument.load(sourceFile, AppVariables.pdfMemUsage)) {
            framesNumber = doc.getNumberOfPages();
            for (int i = 0; i < framesNumber; i++) {
                ImageInformation imageInfo = new ImageInformation(sourceFile);
                imageInfo.setIndex(i);
                imageInfo.setDuration(playController.interval);
                imageInfos.add(imageInfo);
            }
            PDFRenderer renderer = new PDFRenderer(doc);
            ImageType type = ImageType.RGB;
            if (isTransparent) {
                type = ImageType.ARGB;
            }
            int start = fromFrame;
            if (start < 0 || start >= framesNumber) {
                start = 0;
            }
            int end = toFrame;
            if (end <= 0 || end >= framesNumber) {
                end = framesNumber - 1;
            }
            for (int i = start; i <= end; i++) {
                ImageInformation imageInfo = imageInfos.get(i);
                if (task == null || task.isCancelled()) {
                    return false;
                }
                if (!checkMemory()) {
                    if (task != null) {
                        task.setError(Languages.message("OutOfMeomey"));
                    }
                    loading.setInfo(Languages.message("OutOfMeomey"));
                    break;
                }
                if (loading != null) {
                    loading.setInfo(Languages.message("Loading") + " " + i + " / " + framesNumber);
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
                        bufferedImage = ScaleTools.scaleImageWidthKeep(bufferedImage, targetWidth);
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
                                info.setDuration(playController.interval);
                            }
                            if (!checkMemory()) {
                                if (task != null) {
                                    task.setError(Languages.message("OutOfMeomey"));
                                }
                                loading.setInfo(Languages.message("OutOfMeomey"));
                                break;
                            }
                            if (loading != null) {
                                loading.setInfo(Languages.message("Loading") + " " + i + " / " + framesNumber);
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
                                    task.setError(Languages.message("OutOfMeomey"));
                                }
                                loading.setInfo(Languages.message("OutOfMeomey"));
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
                    playImages();
                }
            };
            start(task);
        }

    }

    @FXML
    public void goFrames() {
        if (fileFormat == null) {
            return;
        }
        String value = fromInput.getText();
        int f = AppValues.InvalidInteger;
        if (value == null || value.isBlank()) {
            f = 0;
            fromInput.setStyle(null);
        } else {
            try {
                int v = Integer.valueOf(value);
                if (v < 0) {
                    fromInput.setStyle(NodeStyleTools.badStyle);
                } else {
                    f = v - 1;
                    fromInput.setStyle(null);
                }
            } catch (Exception e) {
                fromInput.setStyle(NodeStyleTools.badStyle);
            }
        }
        int t = AppValues.InvalidInteger;
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
                    t = v - 1;
                    toInput.setStyle(null);
                }
            } catch (Exception e) {
                toInput.setStyle(NodeStyleTools.badStyle);
            }
        }
        if (f == AppValues.InvalidInteger || t == AppValues.InvalidInteger
                || (t >= 0 && f > t)) {
            popError(Languages.message("InvalidParametes"));
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
            fileStatus();
            if (imageInfos == null || framesNumber < 1) {
                return false;
            }
            int start = fromFrame, end = toFrame;
            if (start < 0 || start >= framesNumber) {
                start = 0;
            }
            if (end < 0 || end >= framesNumber) {
                end = framesNumber - 1;
            }
            if (start > end) {
                return false;
            }
            playController.play(framesNumber, start, end);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
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

    @FXML
    public void viewFile() {
        try {
            fileStatus();
            if (fileFormat == null) {
                return;
            }
            if (fileFormat.equalsIgnoreCase("pdf")) {
                PdfViewController controller
                        = (PdfViewController) openStage(Fxmls.PdfViewFxml);
                controller.loadFile(sourceFile, null, frameIndex);
            } else if (fileFormat.equalsIgnoreCase("ppt") || fileFormat.equalsIgnoreCase("pptx")) {
                PptViewController controller
                        = (PptViewController) openStage(Fxmls.PptViewFxml);
                controller.loadFile(sourceFile, frameIndex);
            } else {
                viewAction();
            }

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
                        newInfo.setThumbnail(mara.mybox.fximage.ScaleTools.scaleImage(thumb, AppVariables.thumbnailWidth));
                        infos.add(newInfo);
                    }
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                ImagesEditorController controller = (ImagesEditorController) openStage(Fxmls.ImagesEditorFxml);
                controller.loadImages(infos);
            }
        };
        start(editTask, false);
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
                ImageReader reader = ImageFileReaders.getReader(iis, FileNameTools.getFileSuffix(file));
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
                        frame = ScaleTools.scaleImageWidthKeep(frame, targetWidth);
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
                loading.setInfo(Languages.message("Loading") + " " + index);
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
    public void cleanPane() {
        try {
            if (loadThread != null) {
                loadThread.interrupt();
                loadThread = null;
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
