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
    protected int index = -1, width, height, imageType;
    protected String colorSpace, pixelsString, loadSizeString, fileSizeString, profileName, profileCompressionMethod;
    protected boolean isMultipleFrames, isSampled, isScaled;
    protected List<ImageTypeSpecifier> imageTypes;
    protected ImageTypeSpecifier rawImageType;
    protected LinkedHashMap<String, Object> standardAttributes, nativeAttributes;
    protected Map<String, Map<String, List<Map<String, Object>>>> metaData;
    protected String metaDataXml;
    protected Image image;
    protected BufferedImage bufferedImage;
    protected List<BufferedImage> thumbnails;
    protected Map<String, Long> sizes;
    protected byte[] iccProfile;

    public ImageInformation() {
        init();
    }

    public ImageInformation(File file) {
        super(file);
        imageFormat = FileTools.getFileSuffix(fileName);
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
        standardAttributes = new LinkedHashMap();
        nativeAttributes = new LinkedHashMap();
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
        if (file == null) {
            return null;
        }
        String fileName = file.getAbsolutePath();
        String format = FileTools.getFileSuffix(fileName).toLowerCase();
        if ("raw".equals(format) || !file.exists()) {
            return null;
        }
        return ImageFileReaders.readImageFileMetaData(fileName);
    }

    public static ImageInformation loadImage(File file) {
        if (file == null) {
            return null;
        }
        ImageFileInformation imageFileInformation = ImageInformation.loadImageFileInformation(file);
        return ImageInformation.loadImage(file, imageFileInformation);
    }

    public static ImageInformation loadImage(File file,
            ImageFileInformation imageFileInformation) {
        if (file == null || imageFileInformation == null || imageFileInformation.imageInformation == null) {
            return null;
        }
        return ImageInformation.loadImage(file, imageFileInformation.imageInformation.width, 0, imageFileInformation);
    }

    public static ImageInformation loadImage(File file, int loadWidth,
            int frameIndex) {
        ImageFileInformation imageFileInformation = ImageInformation.loadImageFileInformation(file);
        if (imageFileInformation == null) {
            return null;
        }
        return ImageInformation.loadImage(file, loadWidth, frameIndex, imageFileInformation);
    }

    public static ImageInformation loadImage(File file, int loadWidth,
            int frameIndex,
            ImageFileInformation imageFileInformation) {
        if (imageFileInformation == null || imageFileInformation.getImageInformation() == null) {
            return null;
        }
        boolean needSampled = ImageFileReaders.needSampled(imageFileInformation.getImageInformation(), 1);
        return ImageInformation.loadImage(file, loadWidth, frameIndex, imageFileInformation, needSampled);
    }

    public static ImageInformation loadImage(File file, int loadWidth,
            int frameIndex,
            ImageFileInformation imageFileInformation, boolean needSampled) {
        if (imageFileInformation == null || imageFileInformation.getImageInformation() == null) {
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
        boolean needScale = false;
        if (!"ico".equals(format) && !"icon".equals(format)) {
            needScale = (loadWidth > 0 && loadWidth != bufferImage.getWidth());
            if (needScale && !needSampled) {
                bufferImage = ImageManufacture.scaleImageWidthKeep(bufferImage, loadWidth);
            }
        }
        Image theImage = SwingFXUtils.toFXImage(bufferImage, null);
        imageInfo.setImage(theImage);
        imageInfo.setImageType(bufferImage.getType());
        imageInfo.setIsSampled(needSampled);
        imageInfo.setIsScaled(needScale);
        imageInfo.setWidth(bufferImage.getWidth());
        imageInfo.setHeight(bufferImage.getHeight());
//        imageInfo.setIsMultipleFrames(imageFileInformation.getNumberOfImages() > 1);
        return imageInfo;
    }

    /*
        get/set
     */
    public ImageFileInformation getImageFileInformation() {
        return imageFileInformation;
    }

    public void setImageFileInformation(
            ImageFileInformation imageFileInformation) {
        this.imageFileInformation = imageFileInformation;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getImageType() {
        return imageType;
    }

    public void setImageType(int imageType) {
        this.imageType = imageType;
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

    public Map<String, Map<String, List<Map<String, Object>>>> getMetaData() {
        return metaData;
    }

    public void setMetaData(
            Map<String, Map<String, List<Map<String, Object>>>> metaData) {
        this.metaData = metaData;
    }

    public String getPixelsString() {
        if (imageFileInformation != null && imageFileInformation.getImageInformation() != null) {
            pixelsString = imageFileInformation.getImageInformation().getWidth() + "x"
                    + imageFileInformation.getImageInformation().getHeight();
        } else if (image != null) {
            pixelsString = (int) image.getWidth() + "x"
                    + (int) image.getHeight();
        } else {
            pixelsString = "";
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

    /*
        attributes
     */
    public LinkedHashMap<String, Object> getStandardAttributes() {
        return standardAttributes;
    }

    public void setStandardAttributes(LinkedHashMap<String, Object> attributes) {
        this.standardAttributes = attributes;
    }

    public Object getStandardAttribute(String key) {
        try {
            return standardAttributes.get(key);
        } catch (Exception e) {
            return null;
        }
    }

    public String getStandardStringAttribute(String key) {
        try {
            return (String) standardAttributes.get(key);
        } catch (Exception e) {
            return null;
        }
    }

    public int getStandardIntAttribute(String key) {
        try {
            return (int) standardAttributes.get(key);
        } catch (Exception e) {
            return -1;
        }
    }

    public float getStandardFloatAttribute(String key) {
        try {
            return (float) standardAttributes.get(key);
        } catch (Exception e) {
            return -1;
        }
    }

    public boolean getStandardBooleanAttribute(String key) {
        try {
            return (boolean) standardAttributes.get(key);
        } catch (Exception e) {
            return false;
        }
    }

    public void setStandardAttribute(String key, Object value) {
        standardAttributes.put(key, value);
    }

    public LinkedHashMap<String, Object> getNativeAttributes() {
        return nativeAttributes;
    }

    public void setNativeAttributes(
            LinkedHashMap<String, Object> nativeAttributes) {
        this.nativeAttributes = nativeAttributes;
    }

    public void setNativeAttribute(String key, Object value) {
        nativeAttributes.put(key, value);
    }

    public Object getNativeAttribute(String key) {
        try {
            return nativeAttributes.get(key);
        } catch (Exception e) {
            return null;
        }
    }

    public String getColorSpace() {
        try {
            return (String) standardAttributes.get("ColorSpace");
        } catch (Exception e) {
            return null;
        }
    }

    public void setColorSpace(String colorSpace) {
        standardAttributes.put("ColorSpace", colorSpace);
    }

    public String getImageRotation() {
        try {
            return (String) standardAttributes.get("ImageRotation");
        } catch (Exception e) {
            return null;
        }
    }

    public void setImageRotation(String imageRotation) {
        standardAttributes.put("ImageRotation", imageRotation);
    }

    public int getXDpi() {
        try {
            return (int) standardAttributes.get("xDpi");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setXDpi(int xDpi) {
        standardAttributes.put("xDpi", xDpi);
    }

    public int getYDpi() {
        try {
            return (int) standardAttributes.get("yDpi");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setYDpi(int yDpi) {
        standardAttributes.put("yDpi", yDpi);
    }

    public int getColorChannels() {
        try {
            return (int) standardAttributes.get("ColorChannels");
        } catch (Exception e) {
            return 0;
        }
    }

    public void setColorChannels(int colorChannels) {
        standardAttributes.put("ColorChannels", colorChannels);
    }

    public int getBitDepth() {
        try {
            return (int) standardAttributes.get("BitDepth");
        } catch (Exception e) {
            return 0;
        }
    }

    public void setBitDepth(int bitDepth) {
        standardAttributes.put("BitDepth", bitDepth);
    }

    public String getAlpha() {
        try {
            return (String) standardAttributes.get("Alpha");
        } catch (Exception e) {
            return null;
        }
    }

    public void setAlpha(String alpha) {
        standardAttributes.put("Alpha", alpha);
    }

    public boolean isIsLossless() {
        try {
            return (Boolean) standardAttributes.get("IsLossless");
        } catch (Exception e) {
            return false;
        }
    }

    public void setIsLossless(boolean isLossless) {
        standardAttributes.put("IsLossless", isLossless);
    }

    public float getPixelAspectRatio() {
        try {
            return (float) standardAttributes.get("PixelAspectRatio");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setPixelAspectRatio(float pixelAspectRatio) {
        standardAttributes.put("PixelAspectRatio", pixelAspectRatio);
    }

    public boolean isHasThumbnails() {
        try {
            return (Boolean) standardAttributes.get("HasThumbnails");
        } catch (Exception e) {
            return false;
        }
    }

    public void setHasThumbnails(boolean hasThumbnails) {
        standardAttributes.put("HasThumbnails", hasThumbnails);
    }

    public String getCompressionType() {
        try {
            return (String) standardAttributes.get("CompressionType");
        } catch (Exception e) {
            return null;
        }
    }

    public void setCompressionType(String compressionType) {
        standardAttributes.put("CompressionType", compressionType);
    }

    public int getTileWidth() {
        try {
            return (int) standardAttributes.get("TileWidth");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setTileWidth(int TileWidth) {
        standardAttributes.put("TileWidth", TileWidth);
    }

    public int getTileHeight() {
        try {
            return (int) standardAttributes.get("TileHeight");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setTileHeight(int TileHeight) {
        standardAttributes.put("TileHeight", TileHeight);
    }

    public int getTileOffetX() {
        try {
            return (int) standardAttributes.get("TileOffetX");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setTileOffetX(int TileOffetX) {
        standardAttributes.put("TileOffetX", TileOffetX);
    }

    public int getTileOffsetY() {
        try {
            return (int) standardAttributes.get("TileOffsetY");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setTileOffsetY(int TileOffsetY) {
        standardAttributes.put("TileOffsetY", TileOffsetY);
    }

    public List<BufferedImage> getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(List<BufferedImage> Thumbnails) {
        standardAttributes.put("Thumbnails", Thumbnails);
    }

    public boolean isIsTiled() {
        try {
            return (Boolean) standardAttributes.get("IsTiled");
        } catch (Exception e) {
            return false;
        }
    }

    public void setIsTiled(boolean IsTiled) {
        standardAttributes.put("IsTiled", IsTiled);
    }

    public int getNumberOfThumbnails() {
        try {
            return (int) standardAttributes.get("NumberOfThumbnails");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setNumberOfThumbnails(int NumberOfThumbnails) {
        standardAttributes.put("NumberOfThumbnails", NumberOfThumbnails);
    }

    public float getGamma() {
        try {
            return (float) standardAttributes.get("Gamma");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setGamma(float Gamma) {
        standardAttributes.put("Gamma", Gamma);
    }

    public boolean isBlackIsZero() {
        try {
            return (Boolean) standardAttributes.get("BlackIsZero");
        } catch (Exception e) {
            return true;
        }
    }

    public void setBlackIsZero(boolean BlackIsZero) {
        standardAttributes.put("BlackIsZero", BlackIsZero);
    }

    public List<ImageColor> getPalette() {
        try {
            return (List<ImageColor>) standardAttributes.get("Palette");
        } catch (Exception e) {
            return null;
        }
    }

    public void setPalette(List<ImageColor> Palette) {
        standardAttributes.put("Palette", Palette);
    }

    public int getBackgroundIndex() {
        try {
            return (int) standardAttributes.get("BackgroundIndex");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setBackgroundIndex(int BackgroundIndex) {
        standardAttributes.put("BackgroundIndex", BackgroundIndex);
    }

    public ImageColor getBackgroundColor() {
        try {
            return (ImageColor) standardAttributes.get("BackgroundColor");
        } catch (Exception e) {
            return null;
        }
    }

    public void setBackgroundColor(ImageColor BackgroundColor) {
        standardAttributes.put("BackgroundColor", BackgroundColor);
    }

    public int getNumProgressiveScans() {
        try {
            return (int) standardAttributes.get("NumProgressiveScans");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setNumProgressiveScans(int NumProgressiveScans) {
        standardAttributes.put("NumProgressiveScans", NumProgressiveScans);
    }

    public float getBitRate() {
        try {
            return (float) standardAttributes.get("BitRate");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setBitRate(float BitRate) {
        standardAttributes.put("BitRate", BitRate);
    }

    public String getPlanarConfiguration() {
        try {
            return (String) standardAttributes.get("PlanarConfiguration");
        } catch (Exception e) {
            return null;
        }
    }

    public void setPlanarConfiguration(String PlanarConfiguration) {
        standardAttributes.put("PlanarConfiguration", PlanarConfiguration);
    }

    public String getSampleFormat() {
        try {
            return (String) standardAttributes.get("SampleFormat");
        } catch (Exception e) {
            return null;
        }
    }

    public void setSampleFormat(String SampleFormat) {
        standardAttributes.put("SampleFormat", SampleFormat);
    }

    public String getBitsPerSample() {
        try {
            return (String) standardAttributes.get("BitsPerSample");
        } catch (Exception e) {
            return null;
        }
    }

    public void setBitsPerSample(String BitsPerSample) {
        standardAttributes.put("BitsPerSample", BitsPerSample);
    }

    public String getSignificantBitsPerSample() {
        try {
            return (String) standardAttributes.get("SignificantBitsPerSample");
        } catch (Exception e) {
            return null;
        }
    }

    public void setSignificantBitsPerSample(String SignificantBitsPerSample) {
        standardAttributes.put("SignificantBitsPerSample", SignificantBitsPerSample);
    }

    public String getSampleMSB() {
        try {
            return (String) standardAttributes.get("SampleMSB");
        } catch (Exception e) {
            return null;
        }
    }

    public void setSampleMSB(String SampleMSB) {
        standardAttributes.put("SampleMSB", SampleMSB);
    }

    public float getHorizontalPixelSize() {
        try {
            return (float) standardAttributes.get("HorizontalPixelSize");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setHorizontalPixelSize(float HorizontalPixelSize) {
        standardAttributes.put("HorizontalPixelSize", HorizontalPixelSize);
    }

    public float getVerticalPixelSize() {
        try {
            return (float) standardAttributes.get("VerticalPixelSize");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setVerticalPixelSize(float VerticalPixelSize) {
        standardAttributes.put("VerticalPixelSize", VerticalPixelSize);
    }

    public float getHorizontalPhysicalPixelSpacing() {
        try {
            return (float) standardAttributes.get("HorizontalPhysicalPixelSpacing");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setHorizontalPhysicalPixelSpacing(
            float HorizontalPhysicalPixelSpacing) {
        standardAttributes.put("HorizontalPhysicalPixelSpacing", HorizontalPhysicalPixelSpacing);
    }

    public float getVerticalPhysicalPixelSpacing() {
        try {
            return (float) standardAttributes.get("VerticalPhysicalPixelSpacing");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setVerticalPhysicalPixelSpacing(
            float VerticalPhysicalPixelSpacing) {
        standardAttributes.put("VerticalPhysicalPixelSpacing", VerticalPhysicalPixelSpacing);
    }

    public float getHorizontalPosition() {
        try {
            return (float) standardAttributes.get("HorizontalPosition");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setHorizontalPosition(float HorizontalPosition) {
        standardAttributes.put("HorizontalPosition", HorizontalPosition);
    }

    public float getVerticalPosition() {
        try {
            return (float) standardAttributes.get("VerticalPosition");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setVerticalPosition(float VerticalPosition) {
        standardAttributes.put("VerticalPosition", VerticalPosition);
    }

    public float getHorizontalPixelOffset() {
        try {
            return (float) standardAttributes.get("HorizontalPixelOffset");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setHorizontalPixelOffset(float HorizontalPixelOffset) {
        standardAttributes.put("HorizontalPixelOffset", HorizontalPixelOffset);
    }

    public float getVerticalPixelOffset() {
        try {
            return (float) standardAttributes.get("VerticalPixelOffset");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setVerticalPixelOffset(float verticalPixelOffset) {
        standardAttributes.put("VerticalPixelOffset", verticalPixelOffset);
    }

    public float getHorizontalScreenSize() {
        try {
            return (float) standardAttributes.get("HorizontalScreenSize");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setHorizontalScreenSize(float horizontalScreenSize) {
        standardAttributes.put("HorizontalScreenSize", horizontalScreenSize);
    }

    public float getVerticalScreenSize() {
        try {
            return (float) standardAttributes.get("VerticalScreenSize");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setVerticalScreenSize(float verticalScreenSize) {
        standardAttributes.put("VerticalScreenSize", verticalScreenSize);
    }

    public String getFormatVersion() {
        try {
            return (String) standardAttributes.get("FormatVersion");
        } catch (Exception e) {
            return null;
        }
    }

    public void setFormatVersion(String formatVersion) {
        standardAttributes.put("FormatVersion", formatVersion);
    }

    public String getSubimageInterpretation() {
        try {
            return (String) standardAttributes.get("SubimageInterpretation");
        } catch (Exception e) {
            return null;
        }
    }

    public void setSubimageInterpretation(String subimageInterpretation) {
        standardAttributes.put("SubimageInterpretation", subimageInterpretation);
    }

    public String getImageCreationTime() {
        try {
            return (String) standardAttributes.get("ImageCreationTime");
        } catch (Exception e) {
            return null;
        }
    }

    public void setImageCreationTime(String imageCreationTime) {
        standardAttributes.put("ImageCreationTime", imageCreationTime);
    }

    public String getImageModificationTime() {
        try {
            return (String) standardAttributes.get("ImageModificationTime");
        } catch (Exception e) {
            return null;
        }
    }

    public void setImageModificationTime(String imageModificationTime) {
        standardAttributes.put("ImageModificationTime", imageModificationTime);
    }

    public int getTransparentIndex() {
        try {
            return (int) standardAttributes.get("TransparentIndex");
        } catch (Exception e) {
            return -1;
        }
    }

    public void setTransparentIndex(int transparentIndex) {
        standardAttributes.put("TransparentIndex", transparentIndex);
    }

    public String getTransparentColor() {
        try {
            return (String) standardAttributes.get("TransparentColor");
        } catch (Exception e) {
            return null;
        }
    }

    public void setTransparentColor(String transparentColor) {
        standardAttributes.put("TransparentColor", transparentColor);
    }

    public List<ImageTypeSpecifier> getImageTypes() {
        return imageTypes;
    }

    public void setImageTypes(List<ImageTypeSpecifier> imageTypes) {
        this.imageTypes = imageTypes;
    }

    public byte[] getIccProfile() {
        return iccProfile;
    }

    public void setIccProfile(byte[] iccProfile) {
        this.iccProfile = iccProfile;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileCompressionMethod() {
        return profileCompressionMethod;
    }

    public void setProfileCompressionMethod(String profileCompressionMethod) {
        this.profileCompressionMethod = profileCompressionMethod;
    }

}
