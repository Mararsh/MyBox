package mara.mybox.image;

import java.awt.color.ColorSpace;
import java.io.File;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2019-7-4
 * @License Apache License Version 2.0
 */
public class ImageFileAttributes {

    public static enum BinaryConversion {
        DEFAULT, BINARY_OTSU, BINARY_THRESHOLD
    }
    private String fileFormat, compressionType;
    private ColorSpace colorSpace;
    private File IccProfile;
    private int density, threshold, quality;
    private BinaryConversion binaryConversion;
    private boolean isBinary, isDithering;

    public ImageFileAttributes() {
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public String getCompressionType() {
        return compressionType;
    }

    public void setCompressionType(String compressionType) {
        this.compressionType = compressionType;
    }

    public ColorSpace getColorSpace() {
        return colorSpace;
    }

    public void setColorSpace(ColorSpace colorSpace) {
        if (colorSpace != null) {
            logger.debug(colorSpace.getType());
        }
        this.colorSpace = colorSpace;
    }

    public File getIccProfile() {
        return IccProfile;
    }

    public void setIccProfile(File IccProfile) {
        this.IccProfile = IccProfile;
    }

    public int getDensity() {
        return density;
    }

    public void setDensity(int density) {
        this.density = density;
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

    public boolean isIsDithering() {
        return isDithering;
    }

    public void setIsDithering(boolean isDithering) {
        this.isDithering = isDithering;
    }

    public boolean isIsBinary() {
        return isBinary;
    }

    public void setIsBinary(boolean isBinary) {
        this.isBinary = isBinary;
    }

}
