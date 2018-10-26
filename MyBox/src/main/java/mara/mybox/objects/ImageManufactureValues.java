package mara.mybox.objects;

import java.io.File;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-10-12
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureValues {

    private static final Logger logger = LogManager.getLogger();

    private File sourceFile;
    private ImageScope currentScope;
    private File refFile;
    private Image image, refImage, undoImage, redoImage, cropImage, currentImage, scopeImage;
    private ImageFileInformation refInfo, scopeInfo, imageInfo;
    private boolean refSync, isConfirmBeforeSave, imageChanged, showRef;
    private int stageWidth, stageHeight, saveAsType, imageViewWidth, imageViewHeight;

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

    public ImageFileInformation getImageInfo() {
        return imageInfo;
    }

    public void setImageInfo(ImageFileInformation imageInfo) {
        this.imageInfo = imageInfo;
    }

    public ImageScope getCurrentScope() {
        return currentScope;
    }

    public void setCurrentScope(ImageScope currentScope) {
        this.currentScope = currentScope;
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

    public Image getScopeImage() {
        return scopeImage;
    }

    public void setScopeImage(Image scopeImage) {
        this.scopeImage = scopeImage;
    }

    public ImageFileInformation getRefInfo() {
        return refInfo;
    }

    public void setRefInfo(ImageFileInformation refInfo) {
        this.refInfo = refInfo;
    }

    public ImageFileInformation getScopeInfo() {
        return scopeInfo;
    }

    public void setScopeInfo(ImageFileInformation scopeInfo) {
        this.scopeInfo = scopeInfo;
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

    public int getSaveAsType() {
        return saveAsType;
    }

    public void setSaveAsType(int saveAsType) {
        this.saveAsType = saveAsType;
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

}
