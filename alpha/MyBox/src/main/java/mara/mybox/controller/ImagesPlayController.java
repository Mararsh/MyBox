package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import mara.mybox.bufferedimage.ImageFileInformation;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
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
public class ImagesPlayController extends BaseFileController {

    protected List<ImageInformation> imageInfos;
    protected int loadWidth, framesNumber, frameIndex, queueSize, fromFrame, toFrame;
    protected String fileFormat, pdfPassword, inPassword;
    protected LoadingController loading;
    protected long memoryThreadhold;
    protected Thread loadThread;
    protected PDDocument pdfDoc;
    protected ImageType pdfImageType;
    protected PDFRenderer pdfRenderer;
    protected SlideShow ppt;
    protected ImageInputStream imageInputStream;
    protected ImageReader imageReader;
    protected Thread frameThread;
    protected FxSingletonTask frameTask;

    @FXML
    protected ToggleGroup typeGroup;
    @FXML
    protected RadioButton imagesRadio, pdfRadio, pptRadio;
    @FXML
    protected ComboBox<String> loadWidthSelector;
    @FXML
    protected CheckBox transparentBackgroundCheck;
    @FXML
    protected Button goFramesButton;
    @FXML
    protected VBox fileVBox, pdfBox, viewBox, playBox;
    @FXML
    protected TextField fromInput, toInput;
    @FXML
    protected FlowPane framesPane;
    @FXML
    protected ControlPlay playController;
    @FXML
    protected ControlImageView viewController;

    public ImagesPlayController() {
        baseTitle = message("ImagesPlay");
        TipsLabelKey = "ImagesPlayTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            memoryThreadhold = 200 * 1024 * 1024;
            imageInfos = new ArrayList<>();

            typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (pdfRadio.isSelected()) {
                        setFileType(VisitHistory.FileType.PDF);
                        if (!fileVBox.getChildren().contains(pdfBox)) {
                            fileVBox.getChildren().add(3, pdfBox);
                            NodeStyleTools.refreshStyle(pdfBox);
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

            List<String> values = Arrays.asList(message("OriginalSize"),
                    "512", "1024", "256", "128", "2048", "100", "80", "4096");
            loadWidthSelector.getItems().addAll(values);
            loadWidth = UserConfig.getInt(baseName + "LoadWidth", -1);
            if (loadWidth <= 0) {
                loadWidth = -1;
                loadWidthSelector.getSelectionModel().select(0);
            } else {
                loadWidthSelector.setValue(loadWidth + "");
            }
            loadWidthSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (message("OriginalSize").equals(newValue)) {
                        loadWidth = -1;
                    } else {
                        try {
                            loadWidth = Integer.parseInt(newValue);
                            if (loadWidth <= 0) {
                                loadWidth = -1;
                            }
                            ValidationTools.setEditorNormal(loadWidthSelector);
                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(loadWidthSelector);
                        }
                    }
                }
            });

            transparentBackgroundCheck.setSelected(UserConfig.getBoolean(baseName + "Transparent", false));
            transparentBackgroundCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Transparent", transparentBackgroundCheck.isSelected());
                    if (fileFormat != null && fileFormat.equalsIgnoreCase("pdf")) {
                        reloadImages();
                    }
                }
            });

            fileVBox.getChildren().remove(pdfBox);

            frameThread = new Thread() {
                @Override
                public void run() {
                    displayFrame(playController.currentIndex);
                }
            };
            playController.setParameters(this, frameThread, null);

            playController.stopped.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    closeFile();
                }
            });

            viewBox.disableProperty().bind(viewController.imageView.imageProperty().isNull());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(toInput, new Tooltip(message("ToPageComments")));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
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
        if (task != null) {
            task.cancel();
        }
        framesNumber = 0;
        frameIndex = 0;
        sourceFile = null;
        fileFormat = null;
        pdfPassword = null;
        imageInfos.clear();
        playController.clear();
        viewController.loadImage(null);
    }

    @Override
    public void sourceFileChanged(File file) {
        clearList();
        if (file == null) {
            return;
        }
        String format = FileNameTools.ext(file.getName());
        if (format == null || format.isBlank()) {
            popError(message("NotSupport"));
            return;
        }
        sourceFile = file;
        fileFormat = format;
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                if (fileFormat.equalsIgnoreCase("pdf")) {
                    return loadPDF();
                } else if (fileFormat.equalsIgnoreCase("ppt") || fileFormat.equalsIgnoreCase("pptx")) {
                    return loadPPT();
                } else if (fileFormat.equalsIgnoreCase("ico") || fileFormat.equalsIgnoreCase("icon")) {
                    return loadIconFile(this);
                } else {
                    return loadImageFile(this);
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
        loading = start(task);
    }

    protected boolean loadImageFile(FxTask currentTask) {
        imageReader = null;
        imageInfos.clear();
        Platform.runLater(() -> {
            imagesRadio.setSelected(true);
        });
        try {
            openImageFile();
            if (imageReader == null) {
                return false;
            }
            ImageFileInformation fileInfo = new ImageFileInformation(sourceFile);
            if (loading != null) {
                loading.setInfo(message("Loading") + " " + message("MetaData"));
            }
            ImageFileReaders.readImageFileMetaData(currentTask, imageReader, fileInfo);
            if (currentTask == null || !currentTask.isWorking()) {
                loading.setInfo(message("Canceled"));
                return false;
            }
            imageInfos.addAll(fileInfo.getImagesInformation());
            if (imageInfos == null) {
                imageReader.dispose();
                return false;
            }
            framesNumber = imageInfos.size();
            if (!fileFormat.equalsIgnoreCase("gif")) {
                for (int i = 0; i < framesNumber; i++) {
                    imageInfos.get(i).setDuration(playController.timeValue);
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
        return task != null && !task.isCancelled();
    }

    protected void openImageFile() {
        try {
            closeFile();
            if (sourceFile == null) {
                return;
            }
            imageInputStream = ImageIO.createImageInputStream(sourceFile);
            imageReader = ImageFileReaders.getReader(imageInputStream, FileNameTools.ext(sourceFile.getName()));
            if (imageReader != null) {
                imageReader.setInput(imageInputStream, false, false);
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
        }
    }

    protected boolean loadIconFile(FxTask currentTask) {
        imageInfos.clear();
        Platform.runLater(() -> {
            imagesRadio.setSelected(true);
        });
        if (sourceFile == null) {
            return false;
        }
        try {
            ImageFileInformation finfo = ImageFileInformation.readIconFile(currentTask, sourceFile);
            if (currentTask == null || !currentTask.isWorking()) {
                return false;
            }
            imageInfos.addAll(finfo.getImagesInformation());
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
        return true;
    }

    public boolean loadPPT() {
        imageInfos.clear();
        Platform.runLater(() -> {
            pptRadio.setSelected(true);
        });
        try {
            openPPT();
            if (ppt == null) {
                return false;
            }
            List<Slide> slides = ppt.getSlides();
            int width = ppt.getPageSize().width;
            int height = ppt.getPageSize().height;
            framesNumber = slides.size();
            for (int i = 0; i < framesNumber; i++) {
                ImageInformation imageInfo = new ImageInformation(sourceFile);
                imageInfo.setIndex(i);
                imageInfo.setWidth(width);
                imageInfo.setHeight(height);
                imageInfo.setDuration(playController.timeValue);
                imageInfos.add(imageInfo);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
        return true;
    }

    protected void openPPT() {
        try {
            closeFile();
            if (sourceFile == null) {
                return;
            }
            ppt = SlideShowFactory.create(sourceFile);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
        }
    }

    public boolean loadPDF() {
        imageInfos.clear();
        Platform.runLater(() -> {
            pdfRadio.setSelected(true);
        });
        try {
            openPDF(inPassword);
            inPassword = null;
            if (pdfDoc == null) {
                return false;
            }
            pdfImageType = ImageType.RGB;
            if (transparentBackgroundCheck.isSelected()) {
                pdfImageType = ImageType.ARGB;
            }
            framesNumber = pdfDoc.getNumberOfPages();
            for (int i = 0; i < framesNumber; i++) {
                ImageInformation imageInfo = new ImageInformation(sourceFile);
                imageInfo.setIndex(i);
                imageInfo.setDuration(playController.timeValue);
                imageInfo.setDpi(dpi);
                imageInfo.setPassword(pdfPassword);
                imageInfos.add(imageInfo);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
        return true;
    }

    protected void openPDF(String password) {
        closeFile();
        if (sourceFile == null) {
            return;
        }
        try {
            pdfDoc = Loader.loadPDF(sourceFile, password);
            pdfPassword = password;
        } catch (InvalidPasswordException e) {
            try {
                Platform.runLater(() -> {
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setContentText(message("UserPassword"));
                    Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                    stage.setAlwaysOnTop(true);
                    stage.toFront();
                    Optional<String> result = dialog.showAndWait();
                    if (result.isPresent()) {
                        pdfPassword = result.get();
                    }
                    synchronized (sourceFile) {
                        sourceFile.notifyAll();
                    }
                });
                synchronized (sourceFile) {
                    sourceFile.wait();
                }
                Platform.requestNextPulse();
                try {
                    pdfDoc = Loader.loadPDF(sourceFile, pdfPassword);
                } catch (Exception ee) {
                    error = ee.toString();
                    sourceFile = null;
                }
            } catch (Exception eee) {
                error = eee.toString();
            }
        } catch (Exception eeee) {
            error = eeee.toString();
        }
        if (pdfDoc != null) {
            pdfRenderer = new PDFRenderer(pdfDoc);
        }
    }

    public void loadImages(List<ImageInformation> infos) {
        try {
            clearList();
            if (infos == null || infos.isEmpty()) {
                return;
            }
            for (ImageInformation info : infos) {
                ImageInformation ninfo = info.cloneAttributes();
                if (ninfo.getDuration() < 0) {
                    ninfo.setDuration(playController.timeValue);
                }
                imageInfos.add(ninfo);
                if (ninfo.getRegion() != null) {
                }
            }
            framesNumber = imageInfos.size();
            playImages();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public synchronized boolean playImages() {
        try {
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
            viewController.reset();
            playController.play(framesNumber, start, end);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
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
                int v = Integer.parseInt(value);
                if (v < 0) {
                    fromInput.setStyle(UserConfig.badStyle());
                } else {
                    f = v - 1;
                    fromInput.setStyle(null);
                }
            } catch (Exception e) {
                fromInput.setStyle(UserConfig.badStyle());
            }
        }
        int t = AppValues.InvalidInteger;
        value = toInput.getText();
        if (value == null || value.isBlank()) {
            t = -1;
            toInput.setStyle(null);
        } else {
            try {
                int v = Integer.parseInt(value);
                if (v < 0) {
                    t = -1;
                    toInput.setStyle(null);
                } else {
                    t = v - 1;
                    toInput.setStyle(null);
                }
            } catch (Exception e) {
                toInput.setStyle(UserConfig.badStyle());
            }
        }
        if (f == AppValues.InvalidInteger || t == AppValues.InvalidInteger
                || (t >= 0 && f > t)) {
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

    @FXML
    public void viewFile() {
        try {
            if (fileFormat == null) {
                ImageEditorController.openFile(sourceFile);

            } else if (fileFormat.equalsIgnoreCase("pdf")) {
                PdfViewController controller = (PdfViewController) openStage(Fxmls.PdfViewFxml);
                controller.loadFile(sourceFile, null, frameIndex);

            } else if (fileFormat.equalsIgnoreCase("ppt") || fileFormat.equalsIgnoreCase("pptx")) {
                PptViewController controller = (PptViewController) openStage(Fxmls.PptViewFxml);
                controller.loadFile(sourceFile, frameIndex);

            } else {
                ImageEditorController.openFile(sourceFile);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void editFrames() {
        ImagesEditorController.openImages(imageInfos);
    }

    public void displayFrame(int index) {
        if (frameTask != null) {
            frameTask.cancel();
        }
        if (imageInfos == null) {
            playController.clear();
            viewController.loadImage(null);
            return;
        }
        frameTask = new FxSingletonTask<Void>(this) {
            ImageInformation info;
            Image image;

            @Override
            protected boolean handle() {
                try {
                    info = imageInfos.get(index);
                    frameIndex = index;
                    image = thumb(this, info);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (image == null) {
                    return;
                }
                viewController.loadImage(myController, image, framesNumber, frameIndex);
                playController.refreshList();
                info.setThumbnail(null); // release memory
            }

            @Override
            protected void whenFailed() {
            }

        };
        start(frameTask, false);
    }

    protected Image thumb(FxTask thumbTask, ImageInformation info) {
        try {
            if (info == null) {
                return null;
            }
            double imageWidth = info.getWidth();
            double targetWidth = loadWidth <= 0 ? imageWidth : loadWidth;
            if (info.getRegion() == null) {
                Image thumb = info.getThumbnail();
                if (thumb != null && (int) thumb.getWidth() == (int) targetWidth) {
                    return thumb;
                }
            }
            info.setThumbnail(null);
            if (fileFormat == null) {
                info.loadThumbnail(thumbTask, loadWidth);
            } else if (fileFormat.equalsIgnoreCase("pdf")) {
                if (pdfRenderer == null) {
                    openPDF(pdfPassword);
                }
                if (pdfRenderer == null) {
                    return null;
                }
                ImageInformation.readPDF(thumbTask, pdfRenderer, pdfImageType, info, loadWidth);

            } else if (fileFormat.equalsIgnoreCase("ppt") || fileFormat.equalsIgnoreCase("pptx")) {
                if (ppt == null) {
                    openPPT();
                }
                ImageInformation.readPPT(thumbTask, ppt, info, loadWidth);

            } else if (sourceFile != null) {
                if (imageReader == null) {
                    openImageFile();
                }
                ImageInformation.readImage(thumbTask, imageReader, info, loadWidth);

            } else {
                info.loadThumbnail(thumbTask, loadWidth);
            }
            return info.getThumbnail();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public void closeFile() {
        try {
            if (imageInputStream != null) {
                imageInputStream.close();
                imageInputStream = null;
            }
            imageReader = null;

            if (pdfDoc != null) {
                pdfDoc.close();
                pdfDoc = null;
            }
            pdfRenderer = null;

            if (ppt != null) {
                ppt.close();
                ppt = null;
            }

        } catch (Exception e) {
            playController.clear();
            MyBoxLog.debug(e);
        }
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (viewBox.isFocused() || viewBox.isFocusWithin()) {
            if (viewController.keyEventsFilter(event)) {
                return true;
            }
        } else if (playBox.isFocused() || playBox.isFocusWithin()) {
            if (playController.keyEventsFilter(event)) {
                return true;
            }
        }
        if (super.keyEventsFilter(event)) {
            return true;
        }
        if (viewController.keyEventsFilter(event)) {
            return true;
        }
        return playController.keyEventsFilter(event);
    }

    @Override
    public void cleanPane() {
        try {
            playController.clear();
            if (loading != null) {
                loading.cancelAction();
                loading = null;
            }
            closeFile();
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        static
     */
    public static ImagesPlayController open() {
        try {
            ImagesPlayController controller = (ImagesPlayController) WindowTools.openStage(Fxmls.ImagesPlayFxml);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static ImagesPlayController playImages(List<ImageInformation> infos) {
        try {
            ImagesPlayController controller = open();
            controller.loadImages(infos);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static ImagesPlayController playPDF(File file, String password) {
        try {
            ImagesPlayController controller = open();
            if (controller != null) {
                controller.requestMouse();
                controller.inPassword = password;
                controller.sourceFileChanged(file);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static ImagesPlayController playFile(File file) {
        try {
            ImagesPlayController controller = open();
            if (controller != null) {
                controller.requestMouse();
                controller.sourceFileChanged(file);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
