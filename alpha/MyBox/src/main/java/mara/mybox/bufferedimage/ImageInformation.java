package mara.mybox.bufferedimage;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageTypeSpecifier;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.CropTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;

/**
 * @Author Mara
 * @CreateDate 2018-6-19
 * @License Apache License Version 2.0
 */
public class ImageInformation extends ImageFileInformation implements Cloneable {

    protected ImageFileInformation imageFileInformation;
    protected ImageInformation self;
    protected int index = 0, width, height, regionWidth, regionHeight, imageType, sampleScale, dpi;
    protected String colorSpace, pixelsString, loadSizeString, fileSizeString, profileName, profileCompressionMethod;
    protected boolean isMultipleFrames, isSampled, isScaled, needSample;
    protected List<ImageTypeSpecifier> imageTypes;
    protected ImageTypeSpecifier rawImageType;
    protected LinkedHashMap<String, Object> standardAttributes, nativeAttributes;
    protected Map<String, Map<String, List<Map<String, Object>>>> metaData;
    protected String metaDataXml;
    protected Image image, thumbnail;
    protected double thumbnailRotation;
    protected long availableMem, bytesSize, requiredMem, totalRequiredMem;
    protected byte[] iccProfile;
    protected Rectangle region;

    public ImageInformation() {
        init();
    }

    public ImageInformation(File file) {
        super(file);
        imageFormat = FileNameTools.getFileSuffix(fileName);
        if (imageFormat != null) {
            imageFormat = imageFormat.toLowerCase();
        }
        init();
    }

    public ImageInformation(Image image) {
        this.image = image;
        if (image != null) {
            width = (int) image.getWidth();
            height = (int) image.getHeight();
        }
        init();
    }

    private void init() {
        standardAttributes = new LinkedHashMap();
        nativeAttributes = new LinkedHashMap();
        index = 0;
        duration = 500;
        dpi = 72;
        self = this;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {   // just need shallow copy
        try {
            ImageInformation newInfo = (ImageInformation) super.clone();
            newInfo.self = newInfo;
            return newInfo;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public ImageInformation cloneAttributes() {
        try {
            return (ImageInformation) clone();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public ImageInformation as() {
        switch (imageFormat.toLowerCase()) {
            case "png":
                return (ImageInformationPng) this;
            default:
                return this;
        }
    }

    public Image loadImage() {
        if (image == null || region != null || image.getWidth() != width) {
            image = readImage(this);
        }
        return image;
    }

    public Image loadThumbnail(int thumbWidth) {
        if (thumbnail == null || region != null || (int) (thumbnail.getWidth()) != thumbWidth) {
            thumbnail = readImage(this, thumbWidth);
        }
        if (image == null && thumbnail != null && (int) (thumbnail.getWidth()) == width) {
            image = thumbnail;
        }
        return thumbnail;
    }

    public Image loadThumbnail() {
        return loadThumbnail(AppVariables.thumbnailWidth);
    }

    public Image loadRegion() {
        return loadRegion(-1);
    }

    public Image loadRegion(int width) {
        return readRegion(this, width);
    }

    public void setBufferedImage(BufferedImage bufferedImage) {
        if (bufferedImage != null) {
            image = SwingFXUtils.toFXImage(bufferedImage, null);
        }
    }

    public String sampleInformation(Image image) {
        int sampledWidth, sampledHeight, sampledSize;
        if (image != null) {
            sampledWidth = (int) image.getWidth();
            sampledHeight = (int) image.getHeight();
        } else {
            if (width <= 0) {
                width = 512;
            }
            sampledWidth = ImageInformation.countMaxWidth(this);
            sampledHeight = sampledWidth * height / width;
        }
        sampledSize = (int) (sampledWidth * sampledHeight * getColorChannels() / (1014 * 1024));
        String msg = MessageFormat.format(Languages.message("ImageTooLarge"),
                width, height, getColorChannels(),
                bytesSize / (1014 * 1024), requiredMem / (1014 * 1024), availableMem / (1014 * 1024),
                sampledWidth, sampledHeight, sampledSize);
        return msg;
    }

    /*
        static methods
     */
    public static ImageInformation create(String format, File file) {
        switch (format.toLowerCase()) {
            case "png":
                return new ImageInformationPng(file);
            default:
                return new ImageInformation(file);
        }
    }

    public static Image readImage(ImageInformation imageInfo) {
        if (imageInfo == null) {
            return null;
        }
        return readImage(imageInfo, -1);
    }

    public static Image readImage(ImageInformation imageInfo, int width) {
        return readImage(imageInfo, width, true);
    }

    public static Image readImage(ImageInformation imageInfo, int width, boolean isReadFile) {
        if (imageInfo == null) {
            return null;
        }
        if (imageInfo.getRegion() != null) {
            return readRegion(imageInfo, width);
        }
        imageInfo.setIsSampled(false);
        imageInfo.setIsScaled(false);
        Image targetImage = null;
        try {
            int infoWidth = imageInfo.getWidth();
            int targetWidth = width <= 0 ? infoWidth : width;
            Image image = imageInfo.getImage();
            String fileName = imageInfo.getFileName();
            if (image != null) {
                int imageWidth = (int) (image.getWidth());
                if (imageWidth == targetWidth) {
                    targetImage = image;
                } else if (fileName == null || (width > 0 && imageWidth > targetWidth)) {
                    targetImage = mara.mybox.fximage.ScaleTools.scaleImage(image, targetWidth);
                }
            }
            if (targetImage == null) {
                Image thumb = imageInfo.getThumbnail();
                if (thumb != null) {
                    int thumbWidth = (int) (thumb.getWidth());
                    if (image == null && thumbWidth == infoWidth) {
                        imageInfo.setImage(image);
                    }
                    if (thumbWidth == targetWidth) {
                        targetImage = thumb;
                    } else if (fileName == null || (width > 0 && thumbWidth > targetWidth)) {
                        targetImage = mara.mybox.fximage.ScaleTools.scaleImage(thumb, targetWidth);
                    }
                }
            }
            if (targetImage == null && isReadFile && fileName != null) {
                BufferedImage bufferedImage;
                String suffix = FileNameTools.getFileSuffix(fileName);
                if (suffix != null && suffix.equalsIgnoreCase("pdf")) {
                    bufferedImage = readPDF(imageInfo, targetWidth);
                } else if (suffix != null && (suffix.equalsIgnoreCase("ppt") || suffix.equalsIgnoreCase("pptx"))) {
                    bufferedImage = readPPT(imageInfo, targetWidth);
                } else {
                    bufferedImage = readImageFile(imageInfo, targetWidth);
                }
                if (bufferedImage != null) {
                    targetImage = SwingFXUtils.toFXImage(bufferedImage, null);
                    imageInfo.setImageType(bufferedImage.getType());
                }
            }
            if (targetImage != null && infoWidth != (int) targetImage.getWidth()) {
                imageInfo.setIsScaled(true);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return targetImage;
    }

    public static BufferedImage readImageFile(ImageInformation imageInfo, int width) {
        try {
            String fileName = imageInfo.getFileName();
            String format = imageInfo.getImageFormat();
            BufferedImage bufferedImage;
            int maxWidth = ImageInformation.countMaxWidth(imageInfo);
            if (width > maxWidth) {
                System.gc();
                maxWidth = ImageInformation.countMaxWidth(imageInfo);
            }
            if (width > maxWidth) {
                imageInfo.setIsSampled(true);
                bufferedImage = ImageFileReaders.readFrameByScale(format,
                        fileName, imageInfo.getIndex(), imageInfo.getRegion(), imageInfo.getSampleScale());
            } else {
                bufferedImage = ImageFileReaders.readFrameByWidth(format,
                        fileName, imageInfo.getIndex(), imageInfo.getRegion(), width);
            }
            return bufferedImage;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage readPDF(ImageInformation imageInfo, int width) {
        BufferedImage bufferedImage = null;
        try (PDDocument doc = PDDocument.load(imageInfo.getFile(), AppVariables.pdfMemUsage)) {
            PDFRenderer renderer = new PDFRenderer(doc);
            int dpi = imageInfo.getDpi();
            if (dpi <= 0) {
                dpi = 72;
            }
            bufferedImage = renderer.renderImageWithDPI(imageInfo.getIndex(), dpi, ImageType.RGB);
            doc.close();
            Rectangle region = imageInfo.getRegion();
            if (region != null) {
                bufferedImage = mara.mybox.bufferedimage.CropTools.cropOutside(bufferedImage, new DoubleRectangle(region));
            }
            if (width > 0 && bufferedImage.getWidth() != width) {
                bufferedImage = ScaleTools.scaleImageWidthKeep(bufferedImage, width);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return bufferedImage;
    }

    public static BufferedImage readPPT(ImageInformation imageInfo, int width) {
        BufferedImage bufferedImage = null;
        try (SlideShow ppt = SlideShowFactory.create(imageInfo.getFile())) {
            List<Slide> slides = ppt.getSlides();
            int pptWidth = ppt.getPageSize().width;
            int pptHeight = ppt.getPageSize().height;
            Slide slide = slides.get(imageInfo.getIndex());
            bufferedImage = new BufferedImage(pptWidth, pptHeight, BufferedImage.TYPE_INT_ARGB);
            slide.draw(bufferedImage.createGraphics());
            Rectangle region = imageInfo.getRegion();
            if (region != null) {
                bufferedImage = mara.mybox.bufferedimage.CropTools.cropOutside(bufferedImage, new DoubleRectangle(region));
            }
            if (width > 0 && bufferedImage.getWidth() != width) {
                bufferedImage = ScaleTools.scaleImageWidthKeep(bufferedImage, width);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return bufferedImage;
    }

    public static Image readRegion(ImageInformation imageInfo, int width) {
        if (imageInfo == null) {
            return null;
        }

        Rectangle region = imageInfo.getRegion();
        if (region == null) {
            return readImage(imageInfo, width);
        }
        Image regionImage = null;
        try {
            int infoWidth = imageInfo.getWidth();
            int regionWidth = width <= 0 ? (int) (region.getWidth()) : width;
            Image image = imageInfo.getImage();
            if (image != null && (int) (image.getWidth()) == infoWidth) {
                regionImage = CropTools.cropOutsideFx(image, new DoubleRectangle(region));
                regionImage = mara.mybox.fximage.ScaleTools.scaleImage(regionImage, regionWidth);
                return regionImage;
            }
            Image thumb = imageInfo.getThumbnail();
            if (thumb != null && (int) (thumb.getWidth()) == infoWidth) {
                regionImage = CropTools.cropOutsideFx(thumb, new DoubleRectangle(region));
                regionImage = mara.mybox.fximage.ScaleTools.scaleImage(regionImage, regionWidth);
                return regionImage;
            }
            String fileName = imageInfo.getFileName();
            if (fileName == null) {
                return null;
            }
            BufferedImage bufferedImage;
            String suffix = FileNameTools.getFileSuffix(fileName);
            if (suffix != null && suffix.equalsIgnoreCase("pdf")) {
                bufferedImage = readPDF(imageInfo, regionWidth);
            } else if (suffix != null && (suffix.equalsIgnoreCase("ppt") || suffix.equalsIgnoreCase("pptx"))) {
                bufferedImage = readPPT(imageInfo, regionWidth);
            } else {
                bufferedImage = readImageFile(imageInfo, regionWidth);
            }
            if (bufferedImage != null) {
                regionImage = SwingFXUtils.toFXImage(bufferedImage, null);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return regionImage;
    }

    public static BufferedImage readBufferedImage(ImageInformation info) {
        Image image = readImage(info);
        if (image != null) {
            return SwingFXUtils.fromFXImage(image, null);
        } else {
            return null;
        }
    }

    public static int countMaxWidth(ImageInformation imageInfo) {
        if (imageInfo == null) {
            return -1;
        }
        try {
            Runtime r = Runtime.getRuntime();
            long availableMem = r.maxMemory() - (r.totalMemory() - r.freeMemory());
            imageInfo.setAvailableMem(availableMem);

            long channels = imageInfo.getColorChannels() > 0 ? imageInfo.getColorChannels() : 4;
            long bytesSize = channels * imageInfo.getHeight() * imageInfo.getWidth();
            long requiredMem = bytesSize * 6L;
            imageInfo.setBytesSize(bytesSize);
            imageInfo.setRequiredMem(requiredMem);

            if (availableMem < requiredMem) {
                int scale = (int) Math.ceil(1d * requiredMem / availableMem);
                imageInfo.setSampleScale(scale);
                imageInfo.setNeedSample(true);
                return imageInfo.getWidth() / scale;
            } else {
                double ratio = Math.sqrt(1d * availableMem / requiredMem);
                imageInfo.setSampleScale(1);
                imageInfo.setNeedSample(false);
                return (int) (imageInfo.getWidth() * ratio);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return -1;
        }
    }

    /*
        customized get/set
     */
    public int getWidth() {
        if (width <= 0 && image != null) {
            width = (int) image.getWidth();
        }
        return width;
    }

    public int getHeight() {
        if (height <= 0 && image != null) {
            height = (int) image.getHeight();
        }
        return height;
    }

    public String getPixelsString() {
        if (region == null) {
            pixelsString = width + "x" + height;
        } else {
            pixelsString = message("Region") + " " + region.width + "x" + region.height;
        }
        return pixelsString;
    }

    public String getLoadSizeString() {
        if (thumbnail != null) {
            loadSizeString = (int) thumbnail.getWidth() + "x" + (int) thumbnail.getHeight();
        } else {
            loadSizeString = "";
        }
        return loadSizeString;
    }

    public String getFileSizeString() {
        if (imageFileInformation != null) {
            fileSizeString = FileTools.showFileSize(imageFileInformation.getFileSize());
        } else {
            fileSizeString = "";
        }
        return fileSizeString;
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

    public int getImageType() {
        return imageType;
    }

    public void setImageType(int imageType) {
        this.imageType = imageType;
    }

    public void setWidth(int width) {
        this.width = width;
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

    public void setImage(Image image) {
        this.image = image;
    }

    public Image getImage() {
        return image;
    }

    public Image getThumbnail() {
        return thumbnail;
    }

    public Map<String, Map<String, List<Map<String, Object>>>> getMetaData() {
        return metaData;
    }

    public void setMetaData(
            Map<String, Map<String, List<Map<String, Object>>>> metaData) {
        this.metaData = metaData;
    }

    public void setPixelsString(String pixelsString) {
        this.pixelsString = pixelsString;
    }

    public void setLoadSizeString(String loadSizeString) {
        this.loadSizeString = loadSizeString;
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

    public boolean isNeedSample() {
        return needSample;
    }

    public void setNeedSample(boolean needSample) {
        this.needSample = needSample;
    }

    public void setThumbnail(Image thumbnail) {
        this.thumbnail = thumbnail;
    }

    public ImageInformation getSelf() {
        return self;
    }

    public void setSelf(ImageInformation self) {
        this.self = self;
    }

    public long getAvailableMem() {
        return availableMem;
    }

    public void setAvailableMem(long availableMem) {
        this.availableMem = availableMem;
    }

    public long getBytesSize() {
        return bytesSize;
    }

    public void setBytesSize(long bytesSize) {
        this.bytesSize = bytesSize;
    }

    public long getRequiredMem() {
        return requiredMem;
    }

    public void setRequiredMem(long requiredMem) {
        this.requiredMem = requiredMem;
    }

    public long getTotalRequiredMem() {
        return totalRequiredMem;
    }

    public void setTotalRequiredMem(long totalRequiredMem) {
        this.totalRequiredMem = totalRequiredMem;
    }

    public int getSampleScale() {
        return sampleScale;
    }

    public void setSampleScale(int sampleScale) {
        this.sampleScale = sampleScale;
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

    public double getThumbnailRotation() {
        return thumbnailRotation;
    }

    public void setThumbnailRotation(double thumbnailRotation) {
        this.thumbnailRotation = thumbnailRotation;
    }

    public Rectangle getRegion() {
        return region;
    }

    public void setRegion(Rectangle region) {
        this.region = region;
    }

    public int getDpi() {
        return dpi;
    }

    public void setDpi(int dpi) {
        this.dpi = dpi;
    }

    public void setRegionWidth(int regionWidth) {
        this.regionWidth = regionWidth;
    }

    public void setRegionHeight(int regionHeight) {
        this.regionHeight = regionHeight;
    }

}
