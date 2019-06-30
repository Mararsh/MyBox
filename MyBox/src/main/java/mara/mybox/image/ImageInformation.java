package mara.mybox.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageTypeSpecifier;
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
    protected int index = -1, width, height;
    protected String colorSpace, pixelsString, loadSizeString, fileSizeString;
    protected boolean isMultipleFrames, isSampled, isScaled;
    protected List<ImageTypeSpecifier> imageTypes;
    protected ImageTypeSpecifier rawImageType;
    protected LinkedHashMap<String, Object> attributes;
    protected Map<String, Map<String, List<Map<String, String>>>> metaData;
    protected String metaDataXml;
    protected Image image;
    protected BufferedImage bufferedImage;
    protected List<BufferedImage> thumbnails;
    protected Map<String, Long> sizes;

    public ImageInformation() {
        init();
    }

    public ImageInformation(File file) {
        super(file);
        filename = file.getAbsolutePath();
        imageFormat = FileTools.getFileSuffix(filename);
        if (imageFormat != null) {
            imageFormat = imageFormat.toLowerCase();
        }
        init();
    }

    public ImageInformation(Image image) {
        this.image = image;
        init();
        if (image == null) {
            return;
        }
        width = (int) image.getWidth();
        height = (int) image.getHeight();
    }

    private void init() {
        attributes = new LinkedHashMap();
        index = 0;
    }

    public ImageInformation as() {
        switch (imageFormat.toLowerCase()) {
            case "png":
                return (ImageInformationPng) this;
            default:
                return this;
        }
    }

    public static ImageInformation create(String format, File file) {
        switch (format.toLowerCase()) {
            case "png":
                return new ImageInformationPng(file);
            default:
                return new ImageInformation(file);
        }
    }

    public static ImageFileInformation loadImageFileInformation(File file) {
        String fileName = file.getAbsolutePath();
        String format = FileTools.getFileSuffix(fileName).toLowerCase();
        if ("raw".equals(format) || !file.exists()) {
            return null;
        }
        return ImageFileReaders.readImageFileMetaData(fileName);
    }

    public static ImageInformation loadImage(File file) {
        ImageFileInformation imageFileInformation = ImageInformation.loadImageFileInformation(file);
        return ImageInformation.loadImage(file, imageFileInformation);
    }

    public static ImageInformation loadImage(File file, ImageFileInformation imageFileInformation) {
        return ImageInformation.loadImage(file, imageFileInformation.imageInformation.width, 0, imageFileInformation);
    }

    public static ImageInformation loadImage(File file, int loadWidth, int frameIndex) {
        ImageFileInformation imageFileInformation = ImageInformation.loadImageFileInformation(file);
        if (imageFileInformation == null) {
            return null;
        }
        return ImageInformation.loadImage(file, loadWidth, frameIndex, imageFileInformation);
    }

    public static ImageInformation loadImage(File file, int loadWidth, int frameIndex,
            ImageFileInformation imageFileInformation) {
        boolean needSampled = ImageFileReaders.needSampled(imageFileInformation.getImageInformation(), 1);
        return ImageInformation.loadImage(file, loadWidth, frameIndex, imageFileInformation, needSampled);
    }

    public static ImageInformation loadImage(File file, int loadWidth, int frameIndex,
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
            bufferImage = ImageManufacture.scaleImageWidthKeep(bufferImage, loadWidth);
        }
        Image theImage = SwingFXUtils.toFXImage(bufferImage, null);
        imageInfo.setImage(theImage);
        imageInfo.setIsSampled(needSampled);
        imageInfo.setIsScaled(needScale);
//        imageInfo.setIsMultipleFrames(imageFileInformation.getNumberOfImages() > 1);
        return imageInfo;
    }

    /*
        get/set
     */
    public ImageFileInformation getImageFileInformation() {
        return imageFileInformation;
    }

    public void setImageFileInformation(ImageFileInformation imageFileInformation) {
        this.imageFileInformation = imageFileInformation;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
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

    public String getMetaDataXml() {
        return metaDataXml;
    }

    public void setMetaDataXml(String metaDataXml) {
        this.metaDataXml = metaDataXml;
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

    public Map<String, Map<String, List<Map<String, String>>>> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, Map<String, List<Map<String, String>>>> metaData) {
        this.metaData = metaData;
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
        if (isIsSampled()) {
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

    public boolean isIsMultipleFrames() {
        return isMultipleFrames;
    }

    public void setIsMultipleFrames(boolean isMultipleFrames) {
        this.isMultipleFrames = isMultipleFrames;
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

    public Map<String, Long> getSizes() {
        return sizes;
    }

    public void setSizes(Map<String, Long> sizes) {
        this.sizes = sizes;
    }

    public ImageTypeSpecifier getRawImageType() {
        return rawImageType;
    }

    public void setRawImageType(ImageTypeSpecifier rawImageType) {
        this.rawImageType = rawImageType;
    }

    public List<ImageTypeSpecifier> getImageTypeSpecifiers() {
        return imageTypes;
    }

    public void setImageTypeSpecifiers(List<ImageTypeSpecifier> imageTypes) {
        this.imageTypes = imageTypes;
    }

    public LinkedHashMap<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(LinkedHashMap<String, Object> attributes) {
        this.attributes = attributes;
    }

    /*
        attributes
     */
    public Object getAttribute(String key) {
        try {
            return attributes.get(key);
        } catch (Exception e) {
            return null;
        }
    }

    public String getStringAttribute(String key) {
        try {
            return (String) attributes.get(key);
        } catch (Exception e) {
            return null;
        }
    }

    public int getIntAttribute(String key) {
        try {
            return (int) attributes.get(key);
        } catch (Exception e) {
            return -1;
        }
    }

    public float getFloatAttribute(String key) {
        try {
            return (float) attributes.get(key);
        } catch (Exception e) {
            return -1;
        }
    }

    public boolean getBooleanAttribute(String key) {
        try {
            return (boolean) attributes.get(key);
        } catch (Exception e) {
            return false;
        }
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public String getColorSpace() {
        try {
            return (String) attributes.get("ColorSpace");
        } catch (Exception e) {
            return null;
        }
    }

    public void setColorSpace(String colorSpace) {
        attributes.put("ColorSpace", colorSpace);
    }

    public String getImageRotation() {
        try {
            return (String) attributes.get("ImageRotation");
        } catch (Exception e) {
            return null;
        }
    }

    public void setImageRotation(String imageRotation) {
        attributes.put("ImageRotation", imageRotation);
    }

    public int getXDpi() {
        try {
            return (int) attributes.get("xDpi");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setXDpi(int xDpi) {
        attributes.put("xDpi", xDpi);
    }

    public int getYDpi() {
        try {
            return (int) attributes.get("yDpi");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setYDpi(int yDpi) {
        attributes.put("yDpi", yDpi);
    }

    public int getColorChannels() {
        try {
            return (int) attributes.get("ColorChannels");
        } catch (Exception e) {
            return 0;
        }
    }

    public void setColorChannels(int colorChannels) {
        attributes.put("ColorChannels", colorChannels);
    }

    public int getBitDepth() {
        try {
            return (int) attributes.get("BitDepth");
        } catch (Exception e) {
            return 0;
        }
    }

    public void setBitDepth(int bitDepth) {
        attributes.put("BitDepth", bitDepth);
    }

    public String getAlpha() {
        try {
            return (String) attributes.get("Alpha");
        } catch (Exception e) {
            return null;
        }
    }

    public void setAlpha(String alpha) {
        attributes.put("Alpha", alpha);
    }

    public boolean isIsLossless() {
        try {
            return (Boolean) attributes.get("IsLossless");
        } catch (Exception e) {
            return false;
        }
    }

    public void setIsLossless(boolean isLossless) {
        attributes.put("IsLossless", isLossless);
    }

    public float getPixelAspectRatio() {
        try {
            return (float) attributes.get("PixelAspectRatio");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setPixelAspectRatio(float pixelAspectRatio) {
        attributes.put("PixelAspectRatio", pixelAspectRatio);
    }

    public boolean isHasThumbnails() {
        try {
            return (Boolean) attributes.get("HasThumbnails");
        } catch (Exception e) {
            return false;
        }
    }

    public void setHasThumbnails(boolean hasThumbnails) {
        attributes.put("HasThumbnails", hasThumbnails);
    }

    public String getCompressionType() {
        try {
            return (String) attributes.get("CompressionType");
        } catch (Exception e) {
            return null;
        }
    }

    public void setCompressionType(String compressionType) {
        attributes.put("CompressionType", compressionType);
    }

    public int getTileWidth() {
        try {
            return (int) attributes.get("TileWidth");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setTileWidth(int TileWidth) {
        attributes.put("TileWidth", TileWidth);
    }

    public int getTileHeight() {
        try {
            return (int) attributes.get("TileHeight");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setTileHeight(int TileHeight) {
        attributes.put("TileHeight", TileHeight);
    }

    public int getTileOffetX() {
        try {
            return (int) attributes.get("TileOffetX");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setTileOffetX(int TileOffetX) {
        attributes.put("TileOffetX", TileOffetX);
    }

    public int getTileOffsetY() {
        try {
            return (int) attributes.get("TileOffsetY");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setTileOffsetY(int TileOffsetY) {
        attributes.put("TileOffsetY", TileOffsetY);
    }

    public List<BufferedImage> getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(List<BufferedImage> Thumbnails) {
        attributes.put("Thumbnails", Thumbnails);
    }

    public boolean isIsTiled() {
        try {
            return (Boolean) attributes.get("IsTiled");
        } catch (Exception e) {
            return false;
        }
    }

    public void setIsTiled(boolean IsTiled) {
        attributes.put("IsTiled", IsTiled);
    }

    public int getNumberOfThumbnails() {
        try {
            return (int) attributes.get("NumberOfThumbnails");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setNumberOfThumbnails(int NumberOfThumbnails) {
        attributes.put("NumberOfThumbnails", NumberOfThumbnails);
    }

    public float getGamma() {
        try {
            return (float) attributes.get("Gamma");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setGamma(float Gamma) {
        attributes.put("Gamma", Gamma);
    }

    public boolean isBlackIsZero() {
        try {
            return (Boolean) attributes.get("BlackIsZero");
        } catch (Exception e) {
            return true;
        }
    }

    public void setBlackIsZero(boolean BlackIsZero) {
        attributes.put("BlackIsZero", BlackIsZero);
    }

    public List<ImageColor> getPalette() {
        try {
            return (List<ImageColor>) attributes.get("Palette");
        } catch (Exception e) {
            return null;
        }
    }

    public void setPalette(List<ImageColor> Palette) {
        attributes.put("Palette", Palette);
    }

    public int getBackgroundIndex() {
        try {
            return (int) attributes.get("BackgroundIndex");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setBackgroundIndex(int BackgroundIndex) {
        attributes.put("BackgroundIndex", BackgroundIndex);
    }

    public ImageColor getBackgroundColor() {
        try {
            return (ImageColor) attributes.get("BackgroundColor");
        } catch (Exception e) {
            return null;
        }
    }

    public void setBackgroundColor(ImageColor BackgroundColor) {
        attributes.put("BackgroundColor", BackgroundColor);
    }

    public int getNumProgressiveScans() {
        try {
            return (int) attributes.get("NumProgressiveScans");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setNumProgressiveScans(int NumProgressiveScans) {
        attributes.put("NumProgressiveScans", NumProgressiveScans);
    }

    public float getBitRate() {
        try {
            return (float) attributes.get("BitRate");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setBitRate(float BitRate) {
        attributes.put("BitRate", BitRate);
    }

    public String getPlanarConfiguration() {
        try {
            return (String) attributes.get("PlanarConfiguration");
        } catch (Exception e) {
            return null;
        }
    }

    public void setPlanarConfiguration(String PlanarConfiguration) {
        attributes.put("PlanarConfiguration", PlanarConfiguration);
    }

    public String getSampleFormat() {
        try {
            return (String) attributes.get("SampleFormat");
        } catch (Exception e) {
            return null;
        }
    }

    public void setSampleFormat(String SampleFormat) {
        attributes.put("SampleFormat", SampleFormat);
    }

    public String getBitsPerSample() {
        try {
            return (String) attributes.get("BitsPerSample");
        } catch (Exception e) {
            return null;
        }
    }

    public void setBitsPerSample(String BitsPerSample) {
        attributes.put("BitsPerSample", BitsPerSample);
    }

    public String getSignificantBitsPerSample() {
        try {
            return (String) attributes.get("SignificantBitsPerSample");
        } catch (Exception e) {
            return null;
        }
    }

    public void setSignificantBitsPerSample(String SignificantBitsPerSample) {
        attributes.put("SignificantBitsPerSample", SignificantBitsPerSample);
    }

    public String getSampleMSB() {
        try {
            return (String) attributes.get("SampleMSB");
        } catch (Exception e) {
            return null;
        }
    }

    public void setSampleMSB(String SampleMSB) {
        attributes.put("SampleMSB", SampleMSB);
    }

    public float getHorizontalPixelSize() {
        try {
            return (float) attributes.get("HorizontalPixelSize");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setHorizontalPixelSize(float HorizontalPixelSize) {
        attributes.put("HorizontalPixelSize", HorizontalPixelSize);
    }

    public float getVerticalPixelSize() {
        try {
            return (float) attributes.get("VerticalPixelSize");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setVerticalPixelSize(float VerticalPixelSize) {
        attributes.put("VerticalPixelSize", VerticalPixelSize);
    }

    public float getHorizontalPhysicalPixelSpacing() {
        try {
            return (float) attributes.get("HorizontalPhysicalPixelSpacing");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setHorizontalPhysicalPixelSpacing(float HorizontalPhysicalPixelSpacing) {
        attributes.put("HorizontalPhysicalPixelSpacing", HorizontalPhysicalPixelSpacing);
    }

    public float getVerticalPhysicalPixelSpacing() {
        try {
            return (float) attributes.get("VerticalPhysicalPixelSpacing");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setVerticalPhysicalPixelSpacing(float VerticalPhysicalPixelSpacing) {
        attributes.put("VerticalPhysicalPixelSpacing", VerticalPhysicalPixelSpacing);
    }

    public float getHorizontalPosition() {
        try {
            return (float) attributes.get("HorizontalPosition");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setHorizontalPosition(float HorizontalPosition) {
        attributes.put("HorizontalPosition", HorizontalPosition);
    }

    public float getVerticalPosition() {
        try {
            return (float) attributes.get("VerticalPosition");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setVerticalPosition(float VerticalPosition) {
        attributes.put("VerticalPosition", VerticalPosition);
    }

    public float getHorizontalPixelOffset() {
        try {
            return (float) attributes.get("HorizontalPixelOffset");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setHorizontalPixelOffset(float HorizontalPixelOffset) {
        attributes.put("HorizontalPixelOffset", HorizontalPixelOffset);
    }

    public float getVerticalPixelOffset() {
        try {
            return (float) attributes.get("VerticalPixelOffset");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setVerticalPixelOffset(float verticalPixelOffset) {
        attributes.put("VerticalPixelOffset", verticalPixelOffset);
    }

    public float getHorizontalScreenSize() {
        try {
            return (float) attributes.get("HorizontalScreenSize");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setHorizontalScreenSize(float horizontalScreenSize) {
        attributes.put("HorizontalScreenSize", horizontalScreenSize);
    }

    public float getVerticalScreenSize() {
        try {
            return (float) attributes.get("VerticalScreenSize");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setVerticalScreenSize(float verticalScreenSize) {
        attributes.put("VerticalScreenSize", verticalScreenSize);
    }

    public String getFormatVersion() {
        try {
            return (String) attributes.get("FormatVersion");
        } catch (Exception e) {
            return null;
        }
    }

    public void setFormatVersion(String formatVersion) {
        attributes.put("FormatVersion", formatVersion);
    }

    public String getSubimageInterpretation() {
        try {
            return (String) attributes.get("SubimageInterpretation");
        } catch (Exception e) {
            return null;
        }
    }

    public void setSubimageInterpretation(String subimageInterpretation) {
        attributes.put("SubimageInterpretation", subimageInterpretation);
    }

    public String getImageCreationTime() {
        try {
            return (String) attributes.get("ImageCreationTime");
        } catch (Exception e) {
            return null;
        }
    }

    public void setImageCreationTime(String imageCreationTime) {
        attributes.put("ImageCreationTime", imageCreationTime);
    }

    public String getImageModificationTime() {
        try {
            return (String) attributes.get("ImageModificationTime");
        } catch (Exception e) {
            return null;
        }
    }

    public void setImageModificationTime(String imageModificationTime) {
        attributes.put("ImageModificationTime", imageModificationTime);
    }

    public int getTransparentIndex() {
        try {
            return (int) attributes.get("TransparentIndex");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setTransparentIndex(int transparentIndex) {
        attributes.put("TransparentIndex", transparentIndex);
    }

    public String getTransparentColor() {
        try {
            return (String) attributes.get("TransparentColor");
        } catch (Exception e) {
            return null;
        }
    }

    public void setTransparentColor(String transparentColor) {
        attributes.put("TransparentColor", transparentColor);
    }

}
