package mara.mybox.image;

import java.io.File;
import java.util.Map;
import javafx.scene.image.Image;

/**
 * @Author Mara
 * @CreateDate 2018-10-12
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureValues {

    private File sourceFile;
    private File refFile;
    private Image image, refImage, undoImage, redoImage, cropImage, currentImage;
    private ImageInformation refInfo, imageInfo;
    private boolean refSync, isConfirmBeforeSave, imageChanged, showRef, isPaste;
    private int stageWidth, stageHeight, imageViewWidth, imageViewHeight;
    protected Map<String, Object> imageData;

    public ImageManufactureValues() {
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public File getRefFile() {
        return refFile;
    }

    public void setRefFile(File refFile) {
        this.refFile = refFile;
    }

    public Image getRefImage() {
        return refImage;
    }

    public void setRefImage(Image refImage) {
        this.refImage = refImage;
    }

    public Image getUndoImage() {
        return undoImage;
    }

    public void setUndoImage(Image undoImage) {
        this.undoImage = undoImage;
    }

    public Image getRedoImage() {
        return redoImage;
    }

    public void setRedoImage(Image redoImage) {
        this.redoImage = redoImage;
    }

    public Image getCropImage() {
        return cropImage;
    }

    public void setCropImage(Image cropImage) {
        this.cropImage = cropImage;
    }

    public Image getCurrentImage() {
        return currentImage;
    }

    public void setCurrentImage(Image currentImage) {
        this.currentImage = currentImage;
    }

    public ImageInformation getRefInfo() {
        return refInfo;
    }

    public void setRefInfo(ImageInformation refInfo) {
        this.refInfo = refInfo;
    }

    public ImageInformation getImageInfo() {
        return imageInfo;
    }

    public void setImageInfo(ImageInformation imageInfo) {
        this.imageInfo = imageInfo;
    }

    public boolean isRefSync() {
        return refSync;
    }

    public void setRefSync(boolean refSync) {
        this.refSync = refSync;
    }

    public boolean isIsConfirmBeforeSave() {
        return isConfirmBeforeSave;
    }

    public void setIsConfirmBeforeSave(boolean isConfirmBeforeSave) {
        this.isConfirmBeforeSave = isConfirmBeforeSave;
    }

    public int getStageWidth() {
        return stageWidth;
    }

    public void setStageWidth(int stageWidth) {
        this.stageWidth = stageWidth;
    }

    public int getStageHeight() {
        return stageHeight;
    }

    public void setStageHeight(int stageHeight) {
        this.stageHeight = stageHeight;
    }

    public boolean isImageChanged() {
        return imageChanged;
    }

    public void setImageChanged(boolean imageChanged) {
        this.imageChanged = imageChanged;
    }

    public boolean isShowRef() {
        return showRef;
    }

    public void setShowRef(boolean showRef) {
        this.showRef = showRef;
    }

    public int getImageViewWidth() {
        return imageViewWidth;
    }

    public void setImageViewWidth(int imageViewWidth) {
        this.imageViewWidth = imageViewWidth;
    }

    public int getImageViewHeight() {
        return imageViewHeight;
    }

    public void setImageViewHeight(int imageViewHeight) {
        this.imageViewHeight = imageViewHeight;
    }

    public Map<String, Object> getImageData() {
        return imageData;
    }

    public void setImageData(Map<String, Object> imageData) {
        this.imageData = imageData;
    }

    public boolean isIsPaste() {
        return isPaste;
    }

    public void setIsPaste(boolean isPaste) {
        this.isPaste = isPaste;
    }

}
