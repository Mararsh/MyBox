package mara.mybox.data;

import org.apache.pdfbox.rendering.ImageType;

/**
 * @Author Mara
 * @CreateDate 2018-6-18 6:53:00
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageAttributes {

    public static class BinaryConversion {

        public static int DEFAULT = 0;
        public static int BINARY_OTSU = 1;
        public static int BINARY_THRESHOLD = 9;
    }

    private String imageFormat, compressionType;
    private int density, threshold, quality, binaryConversion, ratioAdjustion;
    private ImageType colorSpace;
    private boolean keepRatio, isDithering;
    private int sourceWidth, sourceHeight, targetWidth, targetHeight;

    public ImageAttributes() {
    }

    public ImageAttributes(String imageFormat, ImageType colorSpace, int density) {

        this.imageFormat = imageFormat;
        this.colorSpace = colorSpace;
        this.density = density;
        this.quality = 100;
    }

    public String getImageFormat() {
        return imageFormat;
    }

    public void setImageFormat(String imageFormat) {
        this.imageFormat = imageFormat;
    }

    public int getDensity() {
        return density;
    }

    public void setDensity(int density) {
        this.density = density;
    }

    public String getCompressionType() {
        return compressionType;
    }

    public void setCompressionType(String compressionType) {
        this.compressionType = compressionType;
    }

    public int getTargetWidth() {
        return targetWidth;
    }

    public void setTargetWidth(int targetWidth) {
        this.targetWidth = targetWidth;
    }

    public int getTargetHeight() {
        return targetHeight;
    }

    public void setTargetHeight(int targetHeight) {
        this.targetHeight = targetHeight;
    }

    public ImageType getColorSpace() {
        return colorSpace;
    }

    public void setColorSpace(ImageType colorSpace) {
        this.colorSpace = colorSpace;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getBinaryConversion() {
        return binaryConversion;
    }

    public void setBinaryConversion(int binaryConversion) {
        this.binaryConversion = binaryConversion;
    }

    public int getRatioAdjustion() {
        return ratioAdjustion;
    }

    public void setRatioAdjustion(int ratioAdjustion) {
        this.ratioAdjustion = ratioAdjustion;
    }

    public boolean isKeepRatio() {
        return keepRatio;
    }

    public void setKeepRatio(boolean keepRatio) {
        this.keepRatio = keepRatio;
    }

    public int getSourceWidth() {
        return sourceWidth;
    }

    public void setSourceWidth(int sourceWidth) {
        this.sourceWidth = sourceWidth;
    }

    public int getSourceHeight() {
        return sourceHeight;
    }

    public void setSourceHeight(int sourceHeight) {
        this.sourceHeight = sourceHeight;
    }

    public boolean isIsDithering() {
        return isDithering;
    }

    public void setIsDithering(boolean isDithering) {
        this.isDithering = isDithering;
    }

}
