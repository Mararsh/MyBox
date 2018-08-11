package mara.mybox.objects;

import java.io.File;

/**
 * @Author Mara
 * @CreateDate 2018-6-19
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageFileInformation {

    private File file;
    private String imageFormat, colorSpace, compressionType, imageRotation, bitDepth, extraFormat = "";
    private int xPixels, yPixels, colorChannels;
    private int yDensity, xDensity;  // dpi
    private long createTime;
    private boolean hasAlpha, isLossless, isScope;
    private String metaData;

    public ImageFileInformation() {
        hasAlpha = false;
        isLossless = true;
        isScope = false;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getImageFormat() {
        return imageFormat;
    }

    public void setImageFormat(String imageFormat) {
        this.imageFormat = imageFormat;
    }

    public String getColorSpace() {
        return colorSpace;
    }

    public void setColorSpace(String colorSpace) {
        this.colorSpace = colorSpace;
    }

    public String getCompressionType() {
        return compressionType;
    }

    public void setCompressionType(String compressionType) {
        this.compressionType = compressionType;
    }

    public String getImageRotation() {
        return imageRotation;
    }

    public void setImageRotation(String imageRotation) {
        this.imageRotation = imageRotation;
    }

    public int getxPixels() {
        return xPixels;
    }

    public void setxPixels(int xPixels) {
        this.xPixels = xPixels;
    }

    public int getyPixels() {
        return yPixels;
    }

    public void setyPixels(int yPixels) {
        this.yPixels = yPixels;
    }

    public int getyDensity() {
        return yDensity;
    }

    public void setyDensity(int yDensity) {
        this.yDensity = yDensity;
    }

    public int getxDensity() {
        return xDensity;
    }

    public void setxDensity(int xDensity) {
        this.xDensity = xDensity;
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

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
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

    public boolean isIsScope() {
        return isScope;
    }

    public void setIsScope(boolean isScope) {
        this.isScope = isScope;
    }

}
