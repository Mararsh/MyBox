package mara.mybox.objects;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javafx.scene.image.Image;
import javax.imageio.ImageTypeSpecifier;
import mara.mybox.tools.FileTools;

/**
 * @Author Mara
 * @CreateDate 2018-6-19
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageInformation {

    private ImageFileInformation imageFileInformation;
    private String imageFormat, filename, sizeString;
    private String colorSpace, imageRotation, compressionType, bitDepth, extraFormat = "";
    private int index, width, height, colorChannels, tileWidth, tileHeight, tileOffetX, tileOffsetY;
    private List<ImageTypeSpecifier> imageTypes;
    private ImageTypeSpecifier rawImageType;
    private float aspectRatio;
    private int wDensity, hDensity;  // dpi
    private String metaData;
    private boolean hasAlpha, isLossless, isTiled, hasThumbnails, isSampled;
    private Image image;
    private BufferedImage bufferedImage;
    private List<BufferedImage> thumbnails;

    public ImageInformation() {
        hasAlpha = isTiled = hasThumbnails = isSampled = isLossless = false;
        index = 0;
    }

    public ImageInformation(File file) {
        hasAlpha = isTiled = hasThumbnails = isSampled = isLossless = false;
        index = 0;
        filename = file.getAbsolutePath();
        imageFormat = FileTools.getFileSuffix(filename).toLowerCase();
    }

    public ImageInformation(Image image) {
        hasAlpha = isTiled = hasThumbnails = isSampled = isLossless = false;
        index = 0;
        this.image = image;
        if (image == null) {
            return;
        }
        width = (int) image.getWidth();
        height = (int) image.getHeight();
    }

    public ImageFileInformation getImageFileInformation() {
        return imageFileInformation;
    }

    public void setImageFileInformation(ImageFileInformation imageFileInformation) {
        this.imageFileInformation = imageFileInformation;
    }

    public String getColorSpace() {
        return colorSpace;
    }

    public void setColorSpace(String colorSpace) {
        this.colorSpace = colorSpace;
    }

    public String getImageRotation() {
        return imageRotation;
    }

    public void setImageRotation(String imageRotation) {
        this.imageRotation = imageRotation;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getwDensity() {
        return wDensity;
    }

    public void setwDensity(int wDensity) {
        this.wDensity = wDensity;
    }

    public int gethDensity() {
        return hDensity;
    }

    public void sethDensity(int hDensity) {
        this.hDensity = hDensity;
    }

    public int getColorChannels() {
        return colorChannels;
    }

    public void setColorChannels(int colorChannels) {
        this.colorChannels = colorChannels;
    }

    public String getBitDepth() {
        return bitDepth;
    }

    public void setBitDepth(String bitDepth) {
        this.bitDepth = bitDepth;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public String getExtraFormat() {
        return extraFormat;
    }

    public void setExtraFormat(String extraFormat) {
        this.extraFormat = extraFormat;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    public void setBufferedImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }

    public boolean isHasAlpha() {
        return hasAlpha;
    }

    public void setHasAlpha(boolean hasAlpha) {
        this.hasAlpha = hasAlpha;
    }

    public boolean isIsLossless() {
        return isLossless;
    }

    public void setIsLossless(boolean isLossless) {
        this.isLossless = isLossless;
    }

    public boolean isIsTiled() {
        return isTiled;
    }

    public void setIsTiled(boolean isTiled) {
        this.isTiled = isTiled;
    }

    public List<ImageTypeSpecifier> getImageTypes() {
        return imageTypes;
    }

    public void setImageTypes(List<ImageTypeSpecifier> imageTypes) {
        this.imageTypes = imageTypes;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public ImageTypeSpecifier getRawImageType() {
        return rawImageType;
    }

    public void setRawImageType(ImageTypeSpecifier rawImageType) {
        this.rawImageType = rawImageType;
    }

    public boolean isHasThumbnails() {
        return hasThumbnails;
    }

    public void setHasThumbnails(boolean hasThumbnails) {
        this.hasThumbnails = hasThumbnails;
    }

    public String getCompressionType() {
        return compressionType;
    }

    public void setCompressionType(String compressionType) {
        this.compressionType = compressionType;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public void setTileWidth(int tileWidth) {
        this.tileWidth = tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public void setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
    }

    public int getTileOffetX() {
        return tileOffetX;
    }

    public void setTileOffetX(int tileOffetX) {
        this.tileOffetX = tileOffetX;
    }

    public int getTileOffsetY() {
        return tileOffsetY;
    }

    public void setTileOffsetY(int tileOffsetY) {
        this.tileOffsetY = tileOffsetY;
    }

    public List<BufferedImage> getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(List<BufferedImage> thumbnails) {
        this.thumbnails = thumbnails;
    }

    public String getImageFormat() {
        return imageFormat;
    }

    public void setImageFormat(String imageFormat) {
        this.imageFormat = imageFormat;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getSizeString() {
        return sizeString;
    }

    public void setSizeString(String sizeString) {
        this.sizeString = sizeString;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isIsSampled() {
        return isSampled;
    }

    public void setIsSampled(boolean isSampled) {
        this.isSampled = isSampled;
    }

}
