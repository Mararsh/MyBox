package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.image.ImageAttributes;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageInformation;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-24
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class ImageBaseController extends BaseController {

    protected ImageInformation imageInformation;
    protected Image image;

    protected ImageAttributes attributes;

    protected boolean careFrames, handleLoadedSize, isPaneSize;
    protected int loadWidth, defaultLoadWidth, frameIndex, sizeChangeAware = 10;
    protected LoadingController loadingController;
    protected Task loadTask;

    protected boolean imageChanged, isCroppped;
    protected double mouseX, mouseY;

    protected int xZoomStep = 50, yZoomStep = 50;

    @FXML
    protected ScrollPane scrollPane;
    @FXML
    protected ImageView imageView;
    @FXML
    protected Label sampledTips;
    @FXML
    protected Button imageSizeButton, paneSizeButton, zoomInButton, zoomOutButton,
            rotateLeftButton, rotateRightButton, turnOverButton;

    public ImageBaseController() {
        SourceFileType = VisitHistory.FileType.Image;
        SourcePathType = VisitHistory.FileType.Image;
        TargetPathType = VisitHistory.FileType.Image;
        TargetFileType = VisitHistory.FileType.Image;
        AddFileType = VisitHistory.FileType.Image;
        AddPathType = VisitHistory.FileType.Image;

        targetPathKey = "ImageFilePath";
        sourcePathKey = "ImageFilePath";

        SaveAsOptionsKey = "ImageSaveAsKey";

        sourceExtensionFilter = CommonFxValues.ImageExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
        careFrames = true;
        handleLoadedSize = true;
        loadWidth = -1;
        defaultLoadWidth = -1;
        frameIndex = 0;
    }

    @Override
    public void initializeNext() {
        try {
            initImageView();
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

    protected void initImageView() {
        if (imageView == null) {
            return;
        }

        try {
            imageView.fitWidthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                    viewSizeChanged(Math.abs(new_val.intValue() - old_val.intValue()));
                }
            });
            imageView.fitHeightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                    viewSizeChanged(Math.abs(new_val.intValue() - old_val.intValue()));

                }
            });
            if (scrollPane != null) {
                scrollPane.widthProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                        viewSizeChanged(Math.abs(new_val.intValue() - old_val.intValue()));
                    }
                });
                scrollPane.heightProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                        viewSizeChanged(Math.abs(new_val.intValue() - old_val.intValue()));
                    }
                });
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void viewSizeChanged(double change) {
        refinePane();
//        if (change > sizeChangeAware) {
//            refinePane();
//        }
    }

    public void refinePane() {
        if (scrollPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        FxmlControl.moveXCenter(scrollPane, imageView);
        scrollPane.setVvalue(scrollPane.getVmin());
    }

    public double getImageWidth() {
        if (imageView == null || imageView.getImage() == null) {
            return 1;
        }
        try {
            if (handleLoadedSize || imageInformation == null) {
                return imageView.getImage().getWidth();
            } else {
                return imageInformation.getWidth();
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return 1;
        }
    }

    public double getImageHeight() {
        if (imageView == null || imageView.getImage() == null) {
            return 1;
        }
        try {
            if (handleLoadedSize || imageInformation == null) {
                return imageView.getImage().getHeight();
            } else {
                return imageInformation.getHeight();
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return 1;
        }
    }

    @FXML
    public void loadedSize() {
        if (imageView == null || imageView.getImage() == null || scrollPane == null) {
            return;
        }
        try {
            isPaneSize = false;
            FxmlControl.imageSize(scrollPane, imageView);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void paneSize() {
        if (imageView == null || imageView.getImage() == null || scrollPane == null) {
            return;
        }
        try {
            isPaneSize = true;
            FxmlControl.paneSize(scrollPane, imageView);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void fitSize() {
        if (scrollPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        try {
            if (scrollPane.getHeight() < getImageHeight()
                    || scrollPane.getWidth() < getImageWidth()) {
                paneSize();
            } else {
                loadedSize();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void zoomIn() {
        if (scrollPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        isPaneSize = false;
        FxmlControl.zoomIn(scrollPane, imageView, xZoomStep, yZoomStep);
    }

    @FXML
    public void zoomOut() {
        if (scrollPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        isPaneSize = false;
        FxmlControl.zoomOut(scrollPane, imageView, xZoomStep, yZoomStep);
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
        if (file == null) {
            return;
        }
        synchronized (this) {
            if (loadTask != null) {
                return;
            }
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
                    if (loadTask == null || isCancelled() || "raw".equals(format)) {
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
                                    loadingController.setInfo(MessageFormat.format(AppVariables.message("ImageLargeSampling"),
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
                    loadTask = null;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (ok) {
                                sourceFile = file;
                                imageInformation = imageInfo;
                                image = imageInformation.getImage();
                                loadWidth = inLoadWidth;
                                frameIndex = inFrameIndex;
                                careFrames = inCareFrames;
                                getMyStage().setTitle(getBaseTitle() + " " + fileName);
                                afterInfoLoaded();
                                afterImageLoaded();
                            } else if (multiplied) {
                                loadMultipleFramesImage(file);
                            } else {
                                popError(AppVariables.message("FailOpenImage"));
                            }
                        }
                    });
                }

                @Override
                public void cancelled() {
                    super.cancelled();
                    loadTask = null;
                }

                @Override
                public void failed() {
                    super.failed();
                    loadTask = null;
                }
            };
            loadingController = openHandlingStage(loadTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(loadTask);
            thread.setDaemon(true);
            thread.start();
        }
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

    public void loadImage(File sourceFile, Image image, ImageInformation imageInformation) {
        this.sourceFile = sourceFile;
        this.imageInformation = imageInformation;
        this.image = image;
        afterImageLoaded();
        setImageChanged(true);
    }

    public void loadImage(ImageInformation imageInformation) {
        this.sourceFile = new File(imageInformation.getFileName());
        this.imageInformation = imageInformation;
        this.image = imageInformation.getImage();
        afterImageLoaded();
        setImageChanged(true);
    }

    public void loadImage(final Image inImage) {
        sourceFile = null;
        imageInformation = null;
        image = inImage;
        afterImageLoaded();
        setImageChanged(true);
    }

    public void loadImage(final Image inImage, int maxWidth) {
        sourceFile = null;
        imageInformation = null;
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

    @FXML
    public void imageClicked(MouseEvent event) {
//        logger.debug("imageClicked");

    }

    @Override
    public ImageBaseController refresh() {
        File oldfile = sourceFile;
        ImageInformation oldInfo = imageInformation;
        Image oldImage = image;

        BaseController b = refreshBase();
        if (b == null) {
            return null;
        }
        ImageBaseController c = (ImageBaseController) b;
        if (oldfile != null && oldImage != null && oldInfo != null) {
            c.loadImage(oldfile, oldImage, oldInfo);
        } else if (oldInfo != null) {
            c.loadImage(oldInfo);
        } else if (oldfile != null) {
            c.loadImage(oldfile);
        } else if (oldImage != null) {
            c.loadImage(oldImage);
        }

        return c;
    }

    /*
        get/set
     */
    public ImageInformation getImageInformation() {
        return imageInformation;
    }

    public void setImageInformation(ImageInformation imageInformation) {
        this.imageInformation = imageInformation;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public ImageAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(ImageAttributes attributes) {
        this.attributes = attributes;
    }

    public boolean isCareFrames() {
        return careFrames;
    }

    public void setCareFrames(boolean careFrames) {
        this.careFrames = careFrames;
    }

    public int getLoadWidth() {
        return loadWidth;
    }

    public void setLoadWidth(int loadWidth) {
        this.loadWidth = loadWidth;
    }

    public int getDefaultLoadWidth() {
        return defaultLoadWidth;
    }

    public void setDefaultLoadWidth(int defaultLoadWidth) {
        this.defaultLoadWidth = defaultLoadWidth;
    }

    public int getFrameIndex() {
        return frameIndex;
    }

    public void setFrameIndex(int frameIndex) {
        this.frameIndex = frameIndex;
    }

    public LoadingController getLoadingController() {
        return loadingController;
    }

    public void setLoadingController(LoadingController loadingController) {
        this.loadingController = loadingController;
    }

    public Task getLoadTask() {
        return loadTask;
    }

    public void setLoadTask(Task loadTask) {
        this.loadTask = loadTask;
    }

    public boolean isIsCroppped() {
        return isCroppped;
    }

    public void setIsCroppped(boolean isCroppped) {
        this.isCroppped = isCroppped;
    }

    public double getMouseX() {
        return mouseX;
    }

    public void setMouseX(double mouseX) {
        this.mouseX = mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }

    public void setMouseY(double mouseY) {
        this.mouseY = mouseY;
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }

    public void setScrollPane(ScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public Label getSampledTips() {
        return sampledTips;
    }

    public void setSampledTips(Label sampledTips) {
        this.sampledTips = sampledTips;
    }

    public Button getImageSizeButton() {
        return imageSizeButton;
    }

    public void setImageSizeButton(Button imageSizeButton) {
        this.imageSizeButton = imageSizeButton;
    }

    public Button getPaneSizeButton() {
        return paneSizeButton;
    }

    public void setPaneSizeButton(Button paneSizeButton) {
        this.paneSizeButton = paneSizeButton;
    }

    public Button getZoomInButton() {
        return zoomInButton;
    }

    public void setZoomInButton(Button zoomInButton) {
        this.zoomInButton = zoomInButton;
    }

    public Button getZoomOutButton() {
        return zoomOutButton;
    }

    public void setZoomOutButton(Button zoomOutButton) {
        this.zoomOutButton = zoomOutButton;
    }

    public Button getRotateLeftButton() {
        return rotateLeftButton;
    }

    public void setRotateLeftButton(Button rotateLeftButton) {
        this.rotateLeftButton = rotateLeftButton;
    }

    public Button getRotateRightButton() {
        return rotateRightButton;
    }

    public void setRotateRightButton(Button rotateRightButton) {
        this.rotateRightButton = rotateRightButton;
    }

    public Button getTurnOverButton() {
        return turnOverButton;
    }

    public void setTurnOverButton(Button turnOverButton) {
        this.turnOverButton = turnOverButton;
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (loadTask != null && loadTask.isRunning()) {
            loadTask.cancel();
            loadTask = null;
        }
        return true;
    }

}
