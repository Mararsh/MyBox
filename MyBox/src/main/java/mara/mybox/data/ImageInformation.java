package mara.mybox.data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageTypeSpecifier;
import mara.mybox.image.ImageConvert;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.tools.FileTools;

/**
 * @Author Mara
 * @CreateDate 2018-6-19
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageInformation extends ImageFileInformation {

    protected ImageFileInformation imageFileInformation;
    protected String pixelsString, loadSizeString, fileSizeString;
    protected String colorSpace, imageRotation, compressionType, bitDepth, extraFormat = "";
    protected int index, width, height, colorChannels, tileWidth, tileHeight, tileOffetX, tileOffsetY;
    protected List<ImageTypeSpecifier> imageTypes;
    protected ImageTypeSpecifier rawImageType;
    protected float aspectRatio;
    protected int wDensity, hDensity;  // dpi
    protected String metaData;
    protected boolean hasAlpha, isLossless, isMultipleFrames, hasThumbnails, isSampled, isScaled, isTiled;
    protected Image image;
    protected BufferedImage bufferedImage;
    protected List<BufferedImage> thumbnails;
    protected Map<String, Long> sizes;

    public ImageInformation() {
        hasAlpha = isMultipleFrames = hasThumbnails = isSampled = isLossless = false;
        index = 0;
    }

    public ImageInformation(File file) {
        super(file);
        hasAlpha = isMultipleFrames = hasThumbnails = isSampled = isLossless = false;
        index = 0;
        filename = file.getAbsolutePath();
        imageFormat = FileTools.getFileSuffix(filename);
        if (imageFormat != null) {
            imageFormat = imageFormat.toLowerCase();
        }
    }

    public ImageInformation(Image image) {
        hasAlpha = isMultipleFrames = hasThumbnails = isSampled = isLossless = false;
        index = 0;
        this.image = image;
        if (image == null) {
            return;
        }
        width = (int) image.getWidth();
        height = (int) image.getHeight();
    }

    public static ImageFileInformation loadImageFileInformation(File file) {
        String fileName = file.getAbsolutePath();
        String format = FileTools.getFileSuffix(fileName).toLowerCase();
        if ("raw".equals(format) || !file.exists()) {
            return null;
        }
        return ImageFileReaders.readImageFileMetaData(fileName);
    }

    public static ImageInformation loadImageInformation(File file, int loadWidth, int frameIndex) {
        ImageFileInformation imageFileInformation = ImageInformation.loadImageFileInformation(file);
        if (imageFileInformation == null) {
            return null;
        }
        return loadImageInformation(file, loadWidth, frameIndex, imageFileInformation);
    }

    public static ImageInformation loadImageInformation(File file, int loadWidth, int frameIndex,
            ImageFileInformation imageFileInformation) {
        boolean needSampled = ImageFileReaders.needSampled(imageFileInformation.getImageInformation(), 1);
        return loadImageInformation(file, loadWidth, frameIndex, imageFileInformation, needSampled);
    }

    public static ImageInformation loadImageInformation(File file, int loadWidth, int frameIndex,
            ImageFileInformation imageFileInformation, boolean needSampled) {
        if (imageFileInformation == null) {
            return null;
        }
        String fileName = file.getAbsolutePath();
        String format = FileTools.getFileSuffix(fileName).toLowerCase();
        ImageInformation imageInfo = imageFileInformation.getImageInformation();
        BufferedImage bufferImage;
        if (needSampled) {
            bufferImage = ImageFileReaders.readFileByWidth(format, fileName,
                    imageInfo.getSizes().get("sampledWidth").intValue());
        } else {
            bufferImage = ImageFileReaders.readImage(file);
        }
        boolean needScale = (loadWidth > 0 && loadWidth != bufferImage.getWidth());
        if (needScale && !needSampled) {
            bufferImage = ImageConvert.scaleImageWidthKeep(bufferImage, loadWidth);
        }
        Image theImage = SwingFXUtils.toFXImage(bufferImage, null);
        imageInfo.setImage(theImage);
        imageInfo.setIsSampled(needSampled);
        imageInfo.setIsScaled(needScale);
//        imageInfo.setIsMultipleFrames(imageFileInformation.getNumberOfImages() > 1);
        return imageInfo;
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

    public boolean isIsMultipleFrames() {
        return isMultipleFrames;
    }

    public void setIsMultipleFrames(boolean isMultipleFrames) {
        this.isMultipleFrames = isMultipleFrames;
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

    public boolean isIsScaled() {
        return isScaled;
    }

    public void setIsScaled(boolean isScaled) {
        this.isScaled = isScaled;
    }

    public boolean isIsTiled() {
        return isTiled;
    }

    public void setIsTiled(boolean isTiled) {
        this.isTiled = isTiled;
    }

    public Map<String, Long> getSizes() {
        return sizes;
    }

    public void setSizes(Map<String, Long> sizes) {
        this.sizes = sizes;
    }

    public String getPixelsString() {
        if (imageFileInformation != null && imageFileInformation.getImageInformation() != null) {
            pixelsString = (int) imageFileInformation.getImageInformation().getWidth() + "x"
                    + (int) imageFileInformation.getImageInformation().getHeight();
        } else if (image != null) {
            pixelsString = (int) image.getWidth() + "x"
                    + (int) image.getHeight();
        } else {
            pixelsString = "";
        }
        if (isSampled) {
            pixelsString += " *";
        }
        return pixelsString;
    }

    public void setPixelsString(String pixelsString) {
        this.pixelsString = pixelsString;
    }

    public String getLoadSizeString() {
        if (image != null) {
            loadSizeString = (int) image.getWidth() + "x"
                    + (int) image.getHeight();
        } else {
            loadSizeString = "";
        }
        return loadSizeString;
    }

    public void setLoadSizeString(String loadSizeString) {
        this.loadSizeString = loadSizeString;
    }

    public String getFileSizeString() {
        if (imageFileInformation != null) {
            fileSizeString = FileTools.showFileSize(imageFileInformation.getFileSize());
        } else {
            fileSizeString = "";
        }
        return fileSizeString;
    }

    public void setFileSizeString(String fileSizeString) {
        this.fileSizeString = fileSizeString;
    }

}
