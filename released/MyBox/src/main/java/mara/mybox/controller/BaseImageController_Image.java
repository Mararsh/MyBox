package mara.mybox.controller;

import java.io.File;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ScaleTools;
import mara.mybox.fxml.NodeStyleTools;
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
            loadTask = new SingletonTask<Void>() {
                private ImageInformation targetInfo;

                @Override
                protected boolean handle() {
                    targetInfo = null;
                    Object ret = ImageFileReaders.readFrame(file, onlyInformation, index, width, imageInformation);
                    if (ret == null) {
                        return false;
                    } else if (ret instanceof ImageInformation) {
                        targetInfo = (ImageInformation) ret;
                        return targetInfo != null;
                    } else if (ret instanceof Exception) {
                        error = ((Exception) ret).toString();
                        return false;
                    } else {
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    recordFileOpened(file);
                    if (targetInfo.isNeedSample()) {
                        askSample(targetInfo);
                    } else {
                        sourceFile = file;
                        imageInformation = targetInfo;
                        image = targetInfo.getThumbnail();
                        afterInfoLoaded();
                        afterImageLoaded();
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

    public void loadImage(File sourceFile, ImageInformation imageInformation) {
        if (imageInformation == null) {
            loadImageFile(sourceFile);
            return;
        }
        boolean exist = this.sourceFile != null || image != null;
        this.sourceFile = sourceFile;
        this.imageInformation = imageInformation;
        synchronized (this) {
            if (loadTask != null && !loadTask.isQuit()) {
                return;
            }
            loadTask = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    image = imageInformation.loadThumbnail(loadWidth);
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

    public void loadImageInfo(ImageInformation imageInformation) {
        if (imageInformation == null) {
            loadImageFile(sourceFile);
            return;
        }
        loadImage(imageInformation.getFile(), imageInformation);
    }

    public void loadImage(Image inImage) {
        sourceFile = null;
        imageInformation = null;
        image = inImage;
        afterImageLoaded();
        setImageChanged(true);
    }

    public void loadImage(File sourceFile, ImageInformation imageInformation, Image image, boolean changed) {
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
        setImageChanged(true);
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
//            fitSize();
            drawMaskControls();
            setImageChanged(true);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void setLoadWidth() {
        if (isSettingValues) {
            return;
        }
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
