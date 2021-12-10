package mara.mybox.bufferedimage;

import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import mara.mybox.value.FileExtensions;
import org.apache.pdfbox.rendering.ImageType;

/**
 * @Author Mara
 * @CreateDate 2018-6-18 6:53:00
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageAttributes {

    public static enum Alpha {
        Keep, Remove, PremultipliedAndKeep, PremultipliedAndRemove
    }

    public static enum BinaryConversion {
        DEFAULT, BINARY_OTSU, BINARY_THRESHOLD
    }

    protected String imageFormat, compressionType, colorSpaceName;
    protected ImageType colorType;
    protected int density, threshold, quality, ratioAdjustion, width;
    protected Alpha alpha;
    protected BinaryConversion binaryConversion;
    protected boolean embedProfile, keepRatio, isDithering;
    protected int sourceWidth, sourceHeight, targetWidth, targetHeight;
    protected ICC_Profile profile;
    protected String profileName;

    public ImageAttributes() {
        init(null, null);
    }

    public ImageAttributes(String format, ImageType colorSpace, int density) {
        init(null, format);
        this.colorType = colorSpace;
        this.density = density;
        this.quality = 100;
    }

    public ImageAttributes(BufferedImage image, String format) {
        init(image, format);
    }

    public ImageAttributes(String format) {
        init(null, format);
    }

    private void init(BufferedImage image, String format) {
        if (format == null || !FileExtensions.SupportedImages.contains(format)) {
            format = "png";
        }
        imageFormat = format.toLowerCase();
        switch (imageFormat) {
            case "jpg":
            case "jpeg":
                compressionType = "JPEG";
                break;
            case "gif":
                compressionType = "LZW";
                break;
            case "tif":
            case "tiff":
                if (image != null && image.getType() == BufferedImage.TYPE_BYTE_BINARY) {
                    compressionType = "CCITT T.6";
                } else {
                    compressionType = "Deflate";
                }
                break;
            case "bmp":
                compressionType = "BI_RGB";
                break;
        }
        density = 96;
        quality = 100;
    }

    /*
        get/set
     */
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

    public ImageType getColorType() {
        return colorType;
    }

    public void setColorType(ImageType colorType) {
        this.colorType = colorType;
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

    public BinaryConversion getBinaryConversion() {
        return binaryConversion;
    }

    public void setBinaryConversion(BinaryConversion binaryConversion) {
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

    public ICC_Profile getProfile() {
        return profile;
    }

    public void setProfile(ICC_Profile profile) {
        this.profile = profile;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getColorSpaceName() {
        return colorSpaceName;
    }

    public void setColorSpaceName(String colorSpaceName) {
        this.colorSpaceName = colorSpaceName;
    }

    public Alpha getAlpha() {
        return alpha;
    }

    public void setAlpha(Alpha alpha) {
        this.alpha = alpha;
    }

    public boolean isEmbedProfile() {
        return embedProfile;
    }

    public void setEmbedProfile(boolean embedProfile) {
        this.embedProfile = embedProfile;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

}
