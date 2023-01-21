package mara.mybox.controller;

import java.io.File;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ScaleTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-10
 * @License Apache License Version 2.0
 */
public abstract class BaseImageController_Image extends BaseImageController_MouseEvents {

    public File imageFile() {
        return sourceFile;
    }

    @Override
    public void sourceFileChanged(File file) {
        if (file == null) {
            return;
        }
        frameIndex = 0;
        framesNumber = 0;
        loadImageFile(file, loadWidth);
    }

    public void loadImageFile(File file) {
        loadImageFile(file, loadWidth);
    }

    public void loadImageFile(File file, int width) {
        loadImage(file, false, width, frameIndex);
    }

    public void loadImageFile(File file, boolean onlyInformation) {
        loadImage(file, onlyInformation, loadWidth, frameIndex);
    }

    public void loadImageFile(File file, int width, int index) {
        loadImage(file, false, width, index);
    }

    // index is 0-based
    public void loadImage(File file, boolean onlyInformation, int width, int index) {
        if (file == null || !file.exists() || !file.isFile()) {
            return;
        }
        synchronized (this) {
            if (loadTask != null && !loadTask.isQuit()) {
                return;
            }
            loadTask = new SingletonTask<Void>(this) {
                private ImageInformation loadedInfo;

                @Override
                protected boolean handle() {
                    loadedInfo = new ImageInformation(file);
                    loadedInfo.setIndex(index);
                    loadedInfo.setRequiredWidth(width);
                    loadedInfo.setTask(loadTask);
                    loadedInfo = ImageFileReaders.makeInfo(loadedInfo, onlyInformation);
                    if (loadedInfo == null) {
                        return false;
                    }
                    error = loadedInfo.getError();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    recordFileOpened(file);
                    if (loadedInfo.isNeedSample()) {
                        askSample(loadedInfo);
                    } else {
                        sourceFile = file;
                        imageInformation = loadedInfo;
                        image = loadedInfo.getThumbnail();
                        afterInfoLoaded();
                        afterImageLoaded();
                    }
                    if (error != null && !error.isBlank()) {
                        popError(error);
                    }
                }

            };
            start(loadTask);
        }
    }

    public void askSample(ImageInformation imageInfo) {
        ImageTooLargeController controller = (ImageTooLargeController) openChildStage(Fxmls.ImageTooLargeFxml, true);
        controller.setParameters((BaseImageController) this, imageInfo);
    }

    public void loadImage(File file, ImageInformation info) {
        if (info == null) {
            loadImageFile(file);
            return;
        }
        boolean exist = (info.getRegion() == null) && (sourceFile != null || image != null);
        sourceFile = file;
        imageInformation = info;
        synchronized (this) {
            if (loadTask != null && !loadTask.isQuit()) {
                return;
            }
            loadTask = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    image = info.loadThumbnail(loadWidth);
                    return image != null;
                }

                @Override
                protected void whenSucceeded() {
                    afterImageLoaded();
                    setImageChanged(exist);
                }

            };
            loadingController = start(loadTask);
        }
    }

    public void loadImageInfo(ImageInformation info) {
        if (info == null) {
            loadImageFile(sourceFile);
            return;
        }
        if (info.getRegion() != null) {
            loadRegion(info);
            return;
        }
        loadImage(info.getFile(), info);
    }

    public void loadRegion(ImageInformation info) {
        if (info == null) {
            loadImageFile(sourceFile);
            return;
        }
        if (info.getRegion() == null) {
            loadImageInfo(info);
            return;
        }
        synchronized (this) {
            if (loadTask != null && !loadTask.isQuit()) {
                return;
            }
            loadTask = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    image = info.loadThumbnail(loadWidth);
                    return image != null;
                }

                @Override
                protected void whenSucceeded() {
                    loadImage(image);
                }

            };
            loadingController = start(loadTask);
        }
    }

    public void loadImage(Image inImage) {
        sourceFile = null;
        imageInformation = null;
        image = inImage;
        afterImageLoaded();
    }

    public void loadImage(File sourceFile, ImageInformation imageInformation,
            Image image, boolean changed) {
        this.sourceFile = sourceFile;
        this.imageInformation = imageInformation;
        this.image = image;
        afterImageLoaded();
        setImageChanged(changed);
    }

    public void loadImage(Image inImage, int maxWidth) {
        sourceFile = null;
        imageInformation = null;
        image = ScaleTools.scaleImage(inImage, maxWidth);
        loadWidth = maxWidth;
        afterImageLoaded();
    }

    public void loadFrame(int index) {
        loadImage(sourceFile, false, loadWidth, index);
    }

    public void afterInfoLoaded() {
        if (infoButton != null) {
            infoButton.setDisable(imageInformation == null);
        }
        if (metaButton != null) {
            metaButton.setDisable(imageInformation == null);
        }
        if (deleteButton != null) {
            deleteButton.setDisable(imageFile() == null);
        }
        if (renameButton != null) {
            renameButton.setDisable(imageFile() == null);
        }
        if (previousButton != null) {
            previousButton.setDisable(imageFile() == null);
        }
        if (nextButton != null) {
            nextButton.setDisable(imageFile() == null);
        }
    }

    public boolean afterImageLoaded() {
        try {
            afterInfoLoaded();

            if (imageInformation != null) {
                frameIndex = imageInformation.getIndex();
                if (imageInformation.getImageFileInformation() != null) {
                    framesNumber = imageInformation.getImageFileInformation().getNumberOfImages();
                }
            } else if (image != null) {
                frameIndex = 0;
                framesNumber = 1;
            } else {
                frameIndex = 0;
                framesNumber = 0;
            }

            if (image != null) {
                setZoomStep(image);
            }
            if (imageView == null) {
                return true;
            }
            imageView.setPreserveRatio(true);
            imageView.setImage(image);
            if (image == null) {
                return true;
            }

            if (sampledView != null) {
                if (imageInformation != null && imageInformation.isIsSampled()) {
                    NodeStyleTools.setTooltip(sampledView, imageInformation.sampleInformation(image));
                    sampledView.setVisible(true);
                } else {
                    sampledView.setVisible(false);
                }
            }

            if (isPop) {
                paneSize();
            } else {
                fitSize();
            }
            refinePane();

            if (imageInformation == null) {
                setImageChanged(true);
            } else {
                setImageChanged(imageInformation.isIsScaled());
            }
            setMaskStroke();

            isPickingColor = false;
            checkPickingColor();
            checkSelect();

            notifyLoad();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            if (imageView != null) {
                imageView.setImage(null);
            }
            alertInformation(message("NotSupported"));
            return false;
        }
    }

    public void loadMultipleFramesImage(File file) {
        if (file == null || !file.exists() || file.length() == 0) {
            return;
        }
        ImagesEditorController controller = (ImagesEditorController) openStage(Fxmls.ImagesEditorFxml);
        controller.open(file);
    }

    public void updateImage(Image image) {
        try {
            imageView.setImage(image);
            refinePane();
//            fitSize();
            drawMaskControls();
            setImageChanged(true);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void setLoadWidth(int width) {
        loadWidth = width;
        setLoadWidth();
    }

    protected void setLoadWidth() {
        if (isSettingValues) {
            return;
        }
        UserConfig.setInt(baseName + "LoadWidth", loadWidth);
        if (imageFile() != null) {
            loadImageFile(imageFile(), loadWidth);
        } else if (imageView.getImage() != null) {
            loadImage(imageView.getImage(), loadWidth);
        } else if (image != null) {
            loadImage(image, loadWidth);
        }
        if (imageInformation != null) {
            setImageChanged(imageInformation.isIsScaled());
        } else {
            setImageChanged(false);
        }
    }

    protected void checkSelect() {
        if (isSettingValues) {
            return;
        }
        boolean selected = UserConfig.getBoolean(baseName + "SelectArea", false);
        if (cropButton != null) {
            cropButton.setDisable(!selected);
        }
        if (selectAllButton != null) {
            selectAllButton.setDisable(!selected);
        }
        initMaskRectangleLine(selected);
        updateLabelsTitle();
    }

}
