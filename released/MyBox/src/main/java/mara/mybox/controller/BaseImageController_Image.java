package mara.mybox.controller;

import java.io.File;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ScaleTools;
import mara.mybox.fxml.SingletonTask;
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
        if (loadTask != null && !loadTask.isQuit()) {
            return;
        }
        loadTask = new SingletonTask<Void>(this) {
            private ImageInformation loadedInfo;

            @Override
            protected boolean handle() {
                if (framesNumber <= 0) {
                    framesNumber = 1;
                }
                int frame = index;
                if (frame < 0) {
                    frame = framesNumber - 1;
                }
                if (frame >= framesNumber) {
                    frame = 0;
                }
                loadedInfo = new ImageInformation(file);
                loadedInfo.setIndex(frame);
                loadedInfo.setRequiredWidth(width);
                loadedInfo.setTask(this);
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

            @Override
            protected void finalAction() {
                super.finalAction();
                loadTask = null;
            }

        };
        start(loadTask);
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
        if (loadTask != null && !loadTask.isQuit()) {
            return;
        }
        boolean exist = (info.getRegion() == null) && (sourceFile != null || image != null);
        loadTask = new SingletonTask<Void>(this) {

            private Image thumbLoaded;

            @Override
            protected boolean handle() {
                thumbLoaded = info.loadThumbnail(loadWidth);
                return thumbLoaded != null;
            }

            @Override
            protected void whenSucceeded() {
                image = thumbLoaded;
                sourceFile = file;
                imageInformation = info;
                afterImageLoaded();
                setImageChanged(exist);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                loadTask = null;
            }
        };
        loadingController = start(loadTask);
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

            @Override
            protected void finalAction() {
                super.finalAction();
                loadTask = null;
            }
        };
        loadingController = start(loadTask);
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
        if (index == frameIndex) {
            return;
        }
        loadImage(sourceFile, false, loadWidth, index);
    }

    public void afterInfoLoaded() {
        if (infoButton != null) {
            infoButton.setDisable(imageInformation == null);
        }
        if (metaButton != null) {
            metaButton.setDisable(imageInformation == null);
        }
        File file = imageFile();
        if (deleteButton != null) {
            deleteButton.setDisable(file == null);
        }
        if (renameButton != null) {
            renameButton.setDisable(file == null);
        }
        if (previousButton != null) {
            previousButton.setDisable(file == null);
        }
        if (nextButton != null) {
            nextButton.setDisable(file == null);
        }
        if (openSourceButton != null) {
            openSourceButton.setDisable(file == null || !file.exists());
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

            refinePane();

            if (imageInformation == null) {
                setImageChanged(true);
            } else {
                setImageChanged(imageInformation.isIsScaled());
            }

            isPickingColor = false;
            checkPickingColor();
            finalRefineView();

            notifyLoad();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (imageView != null) {
                imageView.setImage(null);
            }
            alertInformation(message("NotSupported"));
            return false;
        }
    }

    public void updateImage(Image image) {
        try {
            imageView.setImage(image);
            refinePane();
//            fitSize();
            setImageChanged(true);
        } catch (Exception e) {
            MyBoxLog.debug(e);
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
        File file = imageFile();
        if (file != null && file.exists()) {
            loadImageFile(file, loadWidth);
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

    protected boolean canPickColor() {
        return imageView != null && imageView.getImage() != null
                && !(this instanceof ImageSplitController)
                && !(this instanceof ImageSampleController);
    }

    protected boolean canSelect() {
        return imageView != null && imageView.getImage() != null
                && maskRectangle != null && maskCircle == null
                && !(this instanceof ImageSplitController)
                && !(this instanceof ImageSampleController)
                && !(this instanceof ImageManufactureController);
    }

    protected void finalRefineView() {
        if (isSettingValues) {
            return;
        }
        if (isPop) {
            paneSize();
        } else {
            fitSize();
        }
        clearMask();
        if (canSelect()) {
            boolean selected = UserConfig.getBoolean(baseName + "SelectArea", false);
            if (cropButton != null) {
                cropButton.setDisable(!selected);
            }
            if (selectAllButton != null) {
                selectAllButton.setDisable(!selected);
            }
            if (selected) {
                showMaskRectangle();
            } else {
                maskShapeChanged();
            }
        }
        refinePane();
    }

}
