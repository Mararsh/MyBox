package mara.mybox.controller;

import java.io.File;
import javafx.scene.image.Image;
import mara.mybox.image.data.ImageInformation;
import mara.mybox.image.data.ImageScope;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.image.ScaleTools;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.image.file.ImageFileReaders;
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
        loadImageFile(file);
    }

    public void loadImageFile(File file) {
        loadImage(file, false, loadWidth, frameIndex);
    }

    // index is 0-based
    public void loadImage(File file, boolean onlyInformation, int width, int index) {
        if (file == null || !file.exists() || !file.isFile()) {
            return;
        }
        if (loadTask != null) {
            loadTask.cancel();
        }
        loadTask = new FxTask<Void>(this) {
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
                loadedInfo = ImageFileReaders.makeInfo(this, loadedInfo, onlyInformation);
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
        if (backgroundLoad) {
            start(loadTask, thisPane);
        } else {
            start(loadTask);
        }
    }

    public void askSample(ImageInformation imageInfo) {
        ImageTooLargeController controller = (ImageTooLargeController) WindowTools.childStage(this, Fxmls.ImageTooLargeFxml);
        controller.setParameters((BaseImageController) this, imageInfo);
    }

    public void loadImage(File file, ImageInformation info) {
        if (info == null) {
            loadImageFile(file);
            return;
        }
        if (loadTask != null) {
            loadTask.cancel();
        }
        boolean exist = (info.getRegion() == null) && (sourceFile != null || image != null);
        loadTask = new FxTask<Void>(this) {

            private Image thumbLoaded;

            @Override
            protected boolean handle() {
                thumbLoaded = info.loadThumbnail(this, loadWidth);
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
        if (backgroundLoad) {
            start(loadTask, thisPane);
        } else {
            start(loadTask);
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
        if (loadTask != null) {
            loadTask.cancel();
        }
        loadTask = new FxTask<Void>(this) {

            @Override
            protected boolean handle() {
                image = info.loadThumbnail(this, loadWidth);
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
        if (backgroundLoad) {
            start(loadTask, thisPane);
        } else {
            start(loadTask);
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
        if (deleteButton != null) {
            deleteButton.setDisable(sourceFile == null);
        }
        if (renameButton != null) {
            renameButton.setDisable(sourceFile == null);
        }
        if (previousButton != null) {
            previousButton.setDisable(sourceFile == null);
        }
        if (nextButton != null) {
            nextButton.setDisable(sourceFile == null);
        }
        if (openSourceButton != null) {
            openSourceButton.setDisable(sourceFile == null || !sourceFile.exists());
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

            if (imageInformation == null) {
                setImageChanged(true);
            } else {
                setImageChanged(imageInformation.isIsScaled());
            }

            fitView();

            drawMaskRulers();
            if (xyText != null) {
                xyText.setText("");
            }

            if (pickColorCheck != null) {
                isPickingColor = pickColorCheck.isSelected();
            } else {
                isPickingColor = false;
            }
            checkPickingColor();

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

    public void fitView() {
        fitSize();
    }

    public void updateImage(Image newImage) {
        try {
            imageView.setImage(newImage);
            refinePane();
            setImageChanged(true);
            notifyLoad();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void updateImage(String operation, Image newImage) {
        updateImage(newImage);
    }

    public void updateImage(String operation, String value, ImageScope opScope, Image newImage) {
        updateImage(newImage);
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
        if (sourceFile != null && sourceFile.exists()) {
            loadImageFile(sourceFile);
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

}
