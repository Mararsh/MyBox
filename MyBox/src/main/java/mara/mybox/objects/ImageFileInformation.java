package mara.mybox.objects;

import java.io.File;
import javafx.scene.image.Image;
import mara.mybox.tools.FileTools;

/**
 * @Author Mara
 * @CreateDate 2018-6-19
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageFileInformation extends FileInformation {

    private String imageFormat, colorSpace, compressionType, imageRotation, bitDepth, extraFormat = "";
    private int xPixels, yPixels, colorChannels;
    private int yDensity, xDensity;  // dpi
    private boolean hasAlpha, isLossless, isScope;
    private String metaData, filename, pixels;
    private Image image;
    private ImageFileInformation self;

    public ImageFileInformation() {
        hasAlpha = false;
        isLossless = true;
        isScope = false;
    }

    public ImageFileInformation(File file) {
        super(file);
        hasAlpha = false;
        isLossless = true;
        isScope = false;
        filename = fileName.get();
        imageFormat = FileTools.getFileSuffix(filename);
    }

    public ImageFileInformation(Image image) {
        hasAlpha = false;
        isLossless = true;
        isScope = false;
        this.image = image;
        if (image == null) {
            return;
        }
        xPixels = (int) image.getWidth();
        yPixels = (int) image.getHeight();
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

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public ImageFileInformation getSelf() {
        return self;
    }

    public void setSelf(ImageFileInformation self) {
        this.self = self;
    }

    public String getFilename() {
        filename = file.getAbsolutePath();
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPixels() {
        if (xPixels == 0 && yPixels == 0) {
            return "";
        }
        pixels = xPixels + "x" + yPixels;
        return pixels;
    }

    public void setPixels(String pixels) {
        this.pixels = pixels;
    }

}
