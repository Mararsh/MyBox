package mara.mybox.controller.base;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import mara.mybox.controller.ImageViewerController;
import mara.mybox.controller.ImagesBrowserController;
import mara.mybox.controller.LoadingController;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.image.ImageAttributes;
import mara.mybox.image.ImageInformation;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.image.file.ImageFileReaders;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-24
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class ImageBaseController extends BaseController {

    public ImageInformation imageInformation;
    public Image image;
    public ImageAttributes attributes;
    public Map<String, Object> imageData;
    public boolean careFrames;
    public int loadWidth, defaultLoadWidth, frameIndex;
    public LoadingController loadingController;
    public Task loadTask;

    public boolean imageChanged, isCroppped;
    public double mouseX, mouseY;

    @FXML
    public ScrollPane scrollPane;
    @FXML
    public ImageView imageView;
    @FXML
    public Label sampledTips;
    @FXML
    public Button imageSizeButton, paneSizeButton, zoomInButton, zoomOutButton,
            rotateLeftButton, rotateRightButton, turnOverButton;

    public ImageBaseController() {
        SourceFileType = VisitHistory.FileType.Image;
        SourcePathType = VisitHistory.FileType.Image;
        TargetPathType = VisitHistory.FileType.Image;
        TargetFileType = VisitHistory.FileType.Image;
        AddFileType = VisitHistory.FileType.Image;
        AddPathType = VisitHistory.FileType.Image;

        targetPathKey = "ImageTargetPath";
        sourcePathKey = "ImageSourcePath";

        SaveAsOptionsKey = "ImageSaveAsKey";

        fileExtensionFilter = CommonValues.ImageExtensionFilter;
        careFrames = true;
        loadWidth = -1;
        defaultLoadWidth = -1;
        frameIndex = 0;
    }

    @Override
    public void initializeNext() {
        try {
            initializeNext2();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void initializeNext2() {

    }

    @Override
    public void keyEventsHandler(KeyEvent event) {
        super.keyEventsHandler(event);
        if (event.isControlDown()) {
            String key = event.getText();
            if (key == null || key.isEmpty()) {
                return;
            }
            switch (key) {
                case "1":
                    if (imageSizeButton != null && !imageSizeButton.isDisabled()) {
                        loadedSize();
                    }
                    break;
                case "2":
                    if (paneSizeButton != null && !paneSizeButton.isDisabled()) {
                        paneSize();
                    }
                    break;
                case "3":
                    if (zoomInButton != null && !zoomInButton.isDisabled()) {
                        zoomIn();
                    }
                    break;
                case "4":
                    if (zoomOutButton != null && !zoomOutButton.isDisabled()) {
                        zoomOut();
                    }
                    break;
            }

        }
    }

    @FXML
    public void loadedSize() {

    }

    @FXML
    public void paneSize() {

    }

    @FXML
    public void zoomIn() {

    }

    @FXML
    public void zoomOut() {

    }

    public void loadImage(final File file) {
        loadImage(file, false, loadWidth, frameIndex, careFrames);
    }

    public void loadImage(final File file, int maxWidth) {
        loadImage(file, false, maxWidth, frameIndex, careFrames);
    }

    public void loadImageInformation(final File file) {
        loadImage(file, true);
    }

    public void loadImage(final File file, final boolean onlyInformation) {
        loadImage(file, onlyInformation, loadWidth, frameIndex, careFrames);
    }

    public void loadImage(final File file, final boolean onlyInformation,
            final int inLoadWidth, final int inFrameIndex, final boolean inCareFrames) {

        final String fileName = file.getPath();
        loadTask = new Task<Void>() {
            private boolean ok, multiplied;
            private ImageInformation imageInfo;

            @Override
            public Void call() {

                final ImageFileInformation imageFileInformation
                        = ImageInformation.loadImageFileInformation(file);
                if (imageFileInformation == null
                        || imageFileInformation.getImagesInformation() == null
                        || imageFileInformation.getImagesInformation().isEmpty()) {
                    return null;
                }

                String format = FileTools.getFileSuffix(fileName).toLowerCase();
                if (loadTask == null || loadTask.isCancelled() || "raw".equals(format)) {
                    return null;
                }

                if (!onlyInformation) {
                    if (imageFileInformation.getImagesInformation().size() > 1
                            && careFrames) {
                        multiplied = true;
                        return null;
                    }
                    boolean needSampled = ImageFileReaders.needSampled(imageFileInformation.getImageInformation(), 1);
                    if (needSampled) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                if (loadTask == null || !loadTask.isRunning() || loadingController == null) {
                                    return;
                                }
                                imageInfo = imageFileInformation.getImageInformation();
                                loadingController.setInfo(MessageFormat.format(AppVaribles.getMessage("ImageLargeSampling"),
                                        imageInfo.getWidth() + "x" + imageInfo.getHeight()));
                            }
                        });
                    }

                    imageInfo = ImageInformation.loadImage(file,
                            inLoadWidth, inFrameIndex, imageFileInformation, needSampled);

                }

                ok = true;
                return null;
            }

            @Override
            public void succeeded() {
                super.succeeded();

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (ok) {
                            sourceFile = file;
                            imageInformation = imageInfo;
                            image = imageInformation.getImage();;
                            imageData = null;
                            loadWidth = inLoadWidth;
                            frameIndex = inFrameIndex;
                            careFrames = inCareFrames;
                            getMyStage().setTitle(getBaseTitle() + " " + fileName);
                            afterInfoLoaded();
                            afterImageLoaded();
                        } else if (multiplied) {
                            loadMultipleFramesImage(file);
                        }
                    }
                });
            }

            @Override
            public void cancelled() {
                super.cancelled();
            }

            @Override
            public void failed() {
                super.failed();
            }
        };
        loadingController = openHandlingStage(loadTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(loadTask);
        thread.setDaemon(true);
        thread.start();
    }

    public void loadImage(final String fileName) {
        try {
            sourceFile = new File(fileName).getAbsoluteFile(); // Must convert to AbsoluteFile!
//            infoAction(fileName + "\n" + sourceFile.getAbsolutePath());
            if (sourceFileInput != null) {
                sourceFileInput.setText(sourceFile.getAbsolutePath());
            } else {
                loadImage(sourceFile);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void loadImage(File sourceFile, Image image, ImageInformation imageInformation,
            Map<String, Object> imageData) {
        this.sourceFile = sourceFile;
        this.imageInformation = imageInformation;
        this.image = image;
        this.imageData = imageData;
        afterImageLoaded();
        setImageChanged(true);
    }

    public void loadImage(File sourceFile, Image image, ImageInformation imageInformation) {
        this.sourceFile = sourceFile;
        this.imageInformation = imageInformation;
        this.image = image;
        imageData = null;
        afterImageLoaded();
        setImageChanged(true);
    }

    public void loadImage(ImageInformation imageInformation) {
        this.sourceFile = new File(imageInformation.getFilename());
        this.imageInformation = imageInformation;
        this.image = imageInformation.getImage();
        imageData = null;
        afterImageLoaded();
        setImageChanged(true);
    }

    public void loadImage(final Image inImage) {
        sourceFile = null;
        imageInformation = null;
        imageData = null;
        image = inImage;
        afterImageLoaded();
        setImageChanged(true);
    }

    public void loadImage(final Image inImage, int maxWidth) {
        sourceFile = null;
        imageInformation = null;
        imageData = null;
        image = FxmlImageManufacture.scaleImage(inImage, maxWidth);
        loadWidth = maxWidth;
        afterImageLoaded();
        setImageChanged(true);
    }

    public void setImageChanged(boolean imageChanged) {

    }

    public void afterInfoLoaded() {

    }

    public void afterImageLoaded() {
    }

    public void loadMultipleFramesImage(File file) {

    }

    public void updateLabelTitle() {

    }

    public void openImageManufacture(String filename) {
        FxmlStage.openImageManufacture(null, new File(filename));
    }

    public void openImageViewer(Image image) {
        try {
            final ImageViewerController controller = FxmlStage.openImageViewer(null);
            if (controller != null) {
                controller.loadImage(image);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void openImageViewer(ImageInformation info) {
        try {
            final ImageViewerController controller = FxmlStage.openImageViewer(null);
            if (controller != null) {
                controller.loadImage(info);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void openImageViewer(String file) {
        FxmlStage.openImageViewer(null, new File(file));
    }

    public void showImageInformation(ImageInformation info) {
        if (info == null) {
            return;
        }
        FxmlStage.openImageInformation(null, info);
    }

    public void showImageMetaData(ImageInformation info) {
        if (info == null) {
            return;
        }
        FxmlStage.openImageMetaData(null, info);
    }

    public void showImageStatistic(ImageInformation info) {
        if (info == null) {
            return;
        }
        FxmlStage.openImageStatistic(null, info);
    }

    public void showImageStatistic(Image image) {
        if (image == null) {
            return;
        }
        FxmlStage.openImageStatistic(null, image);
    }

    public void multipleFilesGenerated(final List<String> fileNames) {
        try {
            if (fileNames == null || fileNames.isEmpty()) {
                return;
            }
            String path = new File(fileNames.get(0)).getParent();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(getMyStage().getTitle());
            String info = MessageFormat.format(AppVaribles.getMessage("GeneratedFilesResult"),
                    fileNames.size(), "\"" + path + "\"");
            int num = fileNames.size();
            if (num > 10) {
                num = 10;
            }
            for (int i = 0; i < num; i++) {
                info += "\n    " + fileNames.get(i);
            }
            if (fileNames.size() > num) {
                info += "\n    ......";
            }
            alert.setContentText(info);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonOpen = new ButtonType(AppVaribles.getMessage("OpenTargetPath"));
            ButtonType buttonBrowse = new ButtonType(AppVaribles.getMessage("Browse"));
            ButtonType buttonBrowseNew = new ButtonType(AppVaribles.getMessage("BrowseInNew"));
            ButtonType buttonClose = new ButtonType(AppVaribles.getMessage("Close"));
            alert.getButtonTypes().setAll(buttonBrowseNew, buttonBrowse, buttonOpen, buttonClose);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonOpen) {
                browseURI(new File(path).toURI());
                recordFileOpened(path);
            } else if (result.get() == buttonBrowse) {
                final ImagesBrowserController controller = FxmlStage.openImagesBrowser(getMyStage());
                if (controller != null && sourceFile != null) {
                    controller.loadFiles(fileNames);
                }
            } else if (result.get() == buttonBrowseNew) {
                final ImagesBrowserController controller = FxmlStage.openImagesBrowser(null);
                if (controller != null && sourceFile != null) {
                    controller.loadFiles(fileNames);
                }
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    @FXML
    public void imageClicked(MouseEvent event) {
//        logger.debug("imageClicked");

    }

    @Override
    public ImageBaseController refresh() {
        File oldfile = sourceFile;
        ImageInformation oldInfo = imageInformation;
        Map<String, Object> oldMeta = imageData;
        Image oldImage = image;

        ImageBaseController c = (ImageBaseController) refreshBase();
        if (c == null) {
            return null;
        }
        if (oldfile != null && oldImage != null && oldInfo != null) {
            if (oldMeta != null) {
                c.loadImage(oldfile, oldImage, oldInfo, oldMeta);
            } else {
                c.loadImage(oldfile, oldImage, oldInfo);
            }
        } else if (oldInfo != null) {
            c.loadImage(oldInfo);
        } else if (oldfile != null) {
            c.loadImage(oldfile);
        } else if (oldImage != null) {
            c.loadImage(oldImage);
        }

        return c;
    }

}
