package mara.mybox.bufferedimage;

import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import mara.mybox.bufferedimage.ImageBinary.BinaryAlgorithm;
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

    protected String imageFormat, compressionType, colorSpaceName;
    protected ImageType colorType;
    protected int density, threshold, quality, ratioAdjustion, width;
    protected Alpha alpha;
    protected BinaryAlgorithm binaryConversion;
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
                alpha = Alpha.Remove;
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
                alpha = Alpha.Remove;
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

    public ImageAttributes setImageFormat(String imageFormat) {
        this.imageFormat = imageFormat;
        return this;
    }

    public int getDensity() {
        return density;
    }

    public ImageAttributes setDensity(int density) {
        this.density = density;
        return this;
    }

    public String getCompressionType() {
        return compressionType;
    }

    public ImageAttributes setCompressionType(String compressionType) {
        this.compressionType = compressionType;
        return this;
    }

    public int getTargetWidth() {
        return targetWidth;
    }

    public ImageAttributes setTargetWidth(int targetWidth) {
        this.targetWidth = targetWidth;
        return this;
    }

    public int getTargetHeight() {
        return targetHeight;
    }

    public ImageAttributes setTargetHeight(int targetHeight) {
        this.targetHeight = targetHeight;
        return this;
    }

    public ImageType getColorType() {
        return colorType;
    }

    public ImageAttributes setColorType(ImageType colorType) {
        this.colorType = colorType;
        return this;
    }

    public int getThreshold() {
        return threshold;
    }

    public ImageAttributes setThreshold(int threshold) {
        this.threshold = threshold;
        return this;
    }

    public int getQuality() {
        return quality;
    }

    public ImageAttributes setQuality(int quality) {
        this.quality = quality;
        return this;
    }

    public BinaryAlgorithm getBinaryConversion() {
        return binaryConversion;
    }

    public ImageAttributes setBinaryConversion(BinaryAlgorithm binaryConversion) {
        this.binaryConversion = binaryConversion;
        return this;
    }

    public int getRatioAdjustion() {
        return ratioAdjustion;
    }

    public ImageAttributes setRatioAdjustion(int ratioAdjustion) {
        this.ratioAdjustion = ratioAdjustion;
        return this;
    }

    public boolean isKeepRatio() {
        return keepRatio;
    }

    public ImageAttributes setKeepRatio(boolean keepRatio) {
        this.keepRatio = keepRatio;
        return this;
    }

    public int getSourceWidth() {
        return sourceWidth;
    }

    public ImageAttributes setSourceWidth(int sourceWidth) {
        this.sourceWidth = sourceWidth;
        return this;
    }

    public int getSourceHeight() {
        return sourceHeight;
    }

    public ImageAttributes setSourceHeight(int sourceHeight) {
        this.sourceHeight = sourceHeight;
        return this;
    }

    public boolean isIsDithering() {
        return isDithering;
    }

    public ImageAttributes setIsDithering(boolean isDithering) {
        this.isDithering = isDithering;
        return this;
    }

    public ICC_Profile getProfile() {
        return profile;
    }

    public ImageAttributes setProfile(ICC_Profile profile) {
        this.profile = profile;
        return this;
    }

    public String getProfileName() {
        return profileName;
    }

    public ImageAttributes setProfileName(String profileName) {
        this.profileName = profileName;
        return this;
    }

    public String getColorSpaceName() {
        return colorSpaceName;
    }

    public ImageAttributes setColorSpaceName(String colorSpaceName) {
        this.colorSpaceName = colorSpaceName;
        return this;
    }

    public Alpha getAlpha() {
        return alpha;
    }

    public ImageAttributes setAlpha(Alpha alpha) {
        this.alpha = alpha;
        return this;
    }

    public boolean isEmbedProfile() {
        return embedProfile;
    }

    public ImageAttributes setEmbedProfile(boolean embedProfile) {
        this.embedProfile = embedProfile;
        return this;
    }

    public int getWidth() {
        return width;
    }

    public ImageAttributes setWidth(int width) {
        this.width = width;
        return this;
    }

}
