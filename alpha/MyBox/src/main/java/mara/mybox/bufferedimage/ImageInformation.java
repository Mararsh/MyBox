package mara.mybox.bufferedimage;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.CropTools;
import mara.mybox.fxml.FxTask;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.ImageHints;
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
public class ImageInformation extends ImageFileInformation {

    protected ImageFileInformation imageFileInformation;
    protected ImageInformation self;
    protected int index, imageType, sampleScale, dpi, xscale, yscale;
    protected double width, height, requiredWidth, maxWidth, thumbnailRotation;
    protected String colorSpace, pixelsString, loadSizeString, fileSizeString,
            profileName, profileCompressionMethod, metaDataXml, error;
    protected boolean isMultipleFrames, isSampled, isScaled, needSample;
    protected List<ImageTypeSpecifier> imageTypes;
    protected ImageTypeSpecifier rawImageType;
    protected LinkedHashMap<String, Object> standardAttributes, nativeAttributes;
    protected Map<String, Map<String, List<Map<String, Object>>>> metaData;
    protected Image image, thumbnail, regionImage;
    protected long availableMem, bytesSize, requiredMem, totalRequiredMem;
    protected byte[] iccProfile;
    protected DoubleRectangle region;

    public ImageInformation() {
        initImage();
    }

    public ImageInformation(File file) {
        super(file);
        initImage();
        if (file != null) {
            imageFormat = FileNameTools.ext(file.getName());
        }
        if (imageFormat != null) {
            imageFormat = imageFormat.toLowerCase();
        }
    }

    public ImageInformation(Image image) {
        this.image = image;
        if (image != null) {
            width = (int) image.getWidth();
            height = (int) image.getHeight();
        }
    }

    public final void initImage() {
        standardAttributes = new LinkedHashMap();
        nativeAttributes = new LinkedHashMap();
        imageType = -1;
        index = 0;
        duration = 500;
        dpi = 72;
        requiredWidth = -1;
        sampleScale = xscale = yscale = 1;
        image = null;
        thumbnail = null;
        regionImage = null;
        self = this;
    }

    public ImageInformation cloneAttributes() {
        ImageInformation info = new ImageInformation();
        return clone(this, info);
    }

    public static ImageInformation clone(ImageInformation sourceInfo, ImageInformation targetInfo) {
        if (sourceInfo == null || targetInfo == null) {
            return null;
        }
        ImageFileInformation.clone(sourceInfo, targetInfo);
        if (sourceInfo.imageFileInformation != null) {
            targetInfo.imageFileInformation = new ImageFileInformation();
            ImageFileInformation.clone(sourceInfo.imageFileInformation, targetInfo.imageFileInformation);
        }
        targetInfo.self = targetInfo;
        targetInfo.index = sourceInfo.index;
        targetInfo.imageType = sourceInfo.imageType;
        targetInfo.sampleScale = sourceInfo.sampleScale;
        targetInfo.dpi = sourceInfo.dpi;
        targetInfo.xscale = sourceInfo.xscale;
        targetInfo.yscale = sourceInfo.yscale;
        targetInfo.width = sourceInfo.width;
        targetInfo.height = sourceInfo.height;
        targetInfo.requiredWidth = sourceInfo.requiredWidth;
        targetInfo.maxWidth = sourceInfo.maxWidth;
        targetInfo.thumbnailRotation = sourceInfo.thumbnailRotation;
        targetInfo.colorSpace = sourceInfo.colorSpace;
        targetInfo.pixelsString = sourceInfo.pixelsString;
        targetInfo.loadSizeString = sourceInfo.loadSizeString;
        targetInfo.pixelsString = sourceInfo.pixelsString;
        targetInfo.fileSizeString = sourceInfo.fileSizeString;
        targetInfo.profileName = sourceInfo.profileName;
        targetInfo.profileCompressionMethod = sourceInfo.profileCompressionMethod;
        targetInfo.metaDataXml = sourceInfo.metaDataXml;
        targetInfo.error = sourceInfo.error;
        targetInfo.isMultipleFrames = sourceInfo.isMultipleFrames;
        targetInfo.isSampled = sourceInfo.isSampled;
        targetInfo.isScaled = sourceInfo.isScaled;
        targetInfo.needSample = sourceInfo.needSample;
        targetInfo.region = sourceInfo.region;
        targetInfo.availableMem = sourceInfo.availableMem;
        targetInfo.bytesSize = sourceInfo.bytesSize;
        targetInfo.requiredMem = sourceInfo.requiredMem;
        targetInfo.totalRequiredMem = sourceInfo.totalRequiredMem;
        targetInfo.image = sourceInfo.image;
        targetInfo.thumbnail = sourceInfo.thumbnail;
        targetInfo.regionImage = sourceInfo.regionImage;
        targetInfo.metaData = sourceInfo.metaData;
        targetInfo.standardAttributes = sourceInfo.standardAttributes;
        targetInfo.nativeAttributes = sourceInfo.nativeAttributes;
        targetInfo.imageTypes = sourceInfo.imageTypes;
        targetInfo.rawImageType = sourceInfo.rawImageType;
        return targetInfo;
    }

    public ImageInformation as() {
        switch (imageFormat.toLowerCase()) {
            case "png":
                return (ImageInformationPng) this;
            default:
                return this;
        }
    }

    public Image loadImage(FxTask task) {
        if (region != null) {
            return readRegion(task, this, -1);
        }
        if (image == null || image.getWidth() != width) {
            image = readImage(task, this);
        }
        return image;
    }

    public Image loadThumbnail(FxTask task, int thumbWidth) {
        if (region != null) {
            thumbnail = readRegion(task, this, thumbWidth);
        }
        if (thumbnail == null || (int) (thumbnail.getWidth()) != thumbWidth) {
            thumbnail = readImage(task, this, thumbWidth);
        }
        if (image == null && thumbnail != null && (int) (thumbnail.getWidth()) == width) {
            image = thumbnail;
        }
        return thumbnail;
    }

    public Image loadThumbnail(FxTask task) {
        return loadThumbnail(task, AppVariables.thumbnailWidth);
    }

    public void loadBufferedImage(BufferedImage bufferedImage) {
        if (bufferedImage != null) {
            thumbnail = SwingFXUtils.toFXImage(bufferedImage, null);
            isScaled = width > 0 && bufferedImage.getWidth() != (int) width;
            if (imageType < 0) {
                imageType = bufferedImage.getType();
            }
            if (width <= 0) {
                width = bufferedImage.getWidth();
                height = bufferedImage.getHeight();
                image = thumbnail;
            } else if (!isScaled) {
                image = thumbnail;
            }
        } else {
            thumbnail = null;
            isScaled = false;
        }
    }

    public String sampleInformation(FxTask task, Image image) {
        int sampledWidth, sampledHeight, sampledSize;
        if (image != null) {
            sampledWidth = (int) image.getWidth();
            sampledHeight = (int) image.getHeight();
        } else {
            if (width <= 0) {
                width = 512;
            }
            checkMem(task, this);
            sampledWidth = (int) maxWidth;
            sampledHeight = (int) (maxWidth * height / width);
        }
        int channels = getColorChannels();
        int mb = 1024 * 1024;
        sampledSize = (int) (sampledWidth * sampledHeight * channels / mb);
        String msg = MessageFormat.format(message("ImageTooLarge"),
                width, height, channels,
                bytesSize / mb, requiredMem / mb, availableMem / mb,
                sampleScale, sampledWidth, sampledHeight, sampledSize);
        return msg;
    }

    @Override
    public void setFile(File file) {
        if (imageFileInformation != null) {
            imageFileInformation.setFile(file);
        }
        super.setFile(file);
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

    public static Image readImage(FxTask task, ImageInformation imageInfo) {
        if (imageInfo == null) {
            return null;
        }
        return readImage(task, imageInfo, -1);
    }

    public static Image readImage(FxTask task, ImageInformation imageInfo, int requiredWidth) {
        if (imageInfo == null) {
            return null;
        }
        if (imageInfo.getRegion() != null) {
            return readRegion(task, imageInfo, requiredWidth);
        }
        imageInfo.setIsSampled(false);
        imageInfo.setIsScaled(false);
        Image targetImage = null;
        try {
            int infoWidth = (int) imageInfo.getWidth();
            int targetWidth = requiredWidth <= 0 ? infoWidth : requiredWidth;
            Image image = imageInfo.getImage();
            File file = imageInfo.getFile();
            if (image != null) {
                int imageWidth = (int) image.getWidth();
                if (imageWidth == targetWidth) {
                    targetImage = image;
                } else if (file == null || (requiredWidth > 0 && imageWidth > targetWidth)) {
                    targetImage = mara.mybox.fximage.ScaleTools.scaleImage(image, targetWidth);
                }
            }
            if (targetImage == null) {
                Image thumb = imageInfo.getThumbnail();
                if (thumb != null) {
                    int thumbWidth = (int) thumb.getWidth();
                    if (image == null && thumbWidth == infoWidth) {
                        imageInfo.setImage(image);
                    }
                    if (thumbWidth == targetWidth) {
                        targetImage = thumb;
                    } else if (file == null || (requiredWidth > 0 && thumbWidth > targetWidth)) {
                        targetImage = mara.mybox.fximage.ScaleTools.scaleImage(thumb, targetWidth);
                    }
                }
            }
            if (targetImage == null && file != null) {
                BufferedImage bufferedImage;
                String suffix = FileNameTools.ext(file.getName());
                if (suffix != null && suffix.equalsIgnoreCase("pdf")) {
                    bufferedImage = readPDF(imageInfo, targetWidth);
                } else if (suffix != null && (suffix.equalsIgnoreCase("ppt") || suffix.equalsIgnoreCase("pptx"))) {
                    bufferedImage = readPPT(imageInfo, targetWidth);
                } else {
                    imageInfo.setRequiredWidth(requiredWidth);
                    bufferedImage = ImageFileReaders.readFrame(task, imageInfo);
                }
                if (bufferedImage != null) {
                    targetImage = SwingFXUtils.toFXImage(bufferedImage, null);
                    if (imageInfo.getImageType() < 0) {
                        imageInfo.setImageType(bufferedImage.getType());
                    }
                }
            }
            if (targetImage != null && infoWidth != (int) targetImage.getWidth()) {
                imageInfo.setIsScaled(true);
                if (imageInfo.isNeedSample()) {
                    imageInfo.setIsSampled(true);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return targetImage;
    }

    public static BufferedImage readPDF(ImageInformation imageInfo, int width) {
        BufferedImage bufferedImage = null;
        try (PDDocument pdfDoc = PDDocument.load(imageInfo.getFile(), imageInfo.getPassword(), AppVariables.PdfMemUsage)) {
            bufferedImage = readPDF(null, new PDFRenderer(pdfDoc), ImageType.RGB, imageInfo, width);
            pdfDoc.close();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return bufferedImage;
    }

    public static BufferedImage readPDF(FxTask task, PDFRenderer renderer, ImageType imageType,
            ImageInformation imageInfo, int targetWidth) {
        if (renderer == null || imageInfo == null) {
            return null;
        }
        BufferedImage bufferedImage = null;
        try {
            int dpi = imageInfo.getDpi();
            if (dpi <= 0) {
                dpi = 72;
            }
            if (imageType == null) {
                imageType = ImageType.RGB;
            }
            bufferedImage = renderer.renderImageWithDPI(imageInfo.getIndex(), dpi, imageType);
            if (task != null && task.isCancelled()) {
                return null;
            }
            bufferedImage = scaleImage(task, bufferedImage, imageInfo, targetWidth);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
        return bufferedImage;
    }

    public static BufferedImage scaleImage(FxTask task, BufferedImage inImage,
            ImageInformation imageInfo, int targetWidth) {
        if (imageInfo == null) {
            return null;
        }
        try {
            imageInfo.setThumbnail(null);
            BufferedImage bufferedImage = inImage;
            int imageWidth = bufferedImage.getWidth();
            imageInfo.setWidth(imageWidth);
            imageInfo.setHeight(bufferedImage.getHeight());
            imageInfo.setImageType(bufferedImage.getType());
            DoubleRectangle region = imageInfo.getRegion();
            if (region != null) {
                bufferedImage = mara.mybox.bufferedimage.CropTools.cropOutside(task, inImage, region);
                if (task != null && task.isCancelled()) {
                    return null;
                }
            }
            if (targetWidth > 0 && imageWidth != targetWidth) {
                bufferedImage = ScaleTools.scaleImageWidthKeep(bufferedImage, targetWidth);
                if (task != null && task.isCancelled()) {
                    return null;
                }
            }
            if (ImageHints != null) {
                bufferedImage = BufferedImageTools.applyRenderHints(bufferedImage, ImageHints);
                if (task != null && task.isCancelled()) {
                    return null;
                }
            }
            imageInfo.setThumbnail(SwingFXUtils.toFXImage(bufferedImage, null));
            return bufferedImage;
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return inImage;
        }
    }

    public static BufferedImage readPPT(ImageInformation imageInfo, int width) {
        BufferedImage bufferedImage = null;
        try (SlideShow ppt = SlideShowFactory.create(imageInfo.getFile())) {
            bufferedImage = readPPT(null, ppt, imageInfo, width);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return bufferedImage;
    }

    public static BufferedImage readPPT(FxTask task, SlideShow ppt, ImageInformation imageInfo, int targetWidth) {
        if (ppt == null || imageInfo == null) {
            return null;
        }
        BufferedImage bufferedImage = null;
        try {
            List<Slide> slides = ppt.getSlides();
            int pptWidth = ppt.getPageSize().width;
            int pptHeight = ppt.getPageSize().height;
            Slide slide = slides.get(imageInfo.getIndex());
            bufferedImage = new BufferedImage(pptWidth, pptHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bufferedImage.createGraphics();
            if (AppVariables.ImageHints != null) {
                g.addRenderingHints(AppVariables.ImageHints);
            }
            if (task != null && task.isCancelled()) {
                return null;
            }
            slide.draw(g);
            if (task != null && task.isCancelled()) {
                return null;
            }
            bufferedImage = scaleImage(task, bufferedImage, imageInfo, targetWidth);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
        return bufferedImage;
    }

    public static BufferedImage readImage(FxTask task,
            ImageReader reader, ImageInformation imageInfo, int targetWidth) {
        if (reader == null || imageInfo == null) {
            return null;
        }
        BufferedImage bufferedImage = null;
        try {
            imageInfo.setThumbnail(null);
            imageInfo.setRequiredWidth(targetWidth);
            bufferedImage = ImageFileReaders.readFrame(task, reader, imageInfo);
            if (bufferedImage != null) {
                imageInfo.setThumbnail(SwingFXUtils.toFXImage(bufferedImage, null));
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
        return bufferedImage;
    }

    public static Image readRegion(FxTask task, ImageInformation imageInfo, int requireWidth) {
        if (imageInfo == null) {
            return null;
        }
        DoubleRectangle region = imageInfo.getRegion();
        if (region == null) {
            return readImage(task, imageInfo, requireWidth);
        }
        Image regionImage = imageInfo.getRegionImage();
        try {
            int infoWidth = (int) imageInfo.getWidth();
            int targetWidth = requireWidth <= 0 ? (int) region.getWidth() : requireWidth;
            if (regionImage != null && (int) regionImage.getWidth() == targetWidth) {
                return regionImage;
            }
            regionImage = null;
            imageInfo.setRequiredWidth(targetWidth);
            Image image = imageInfo.getImage();
            if (image != null && (int) image.getWidth() == infoWidth) {
                regionImage = CropTools.cropOutsideFx(task, image, region);
                regionImage = mara.mybox.fximage.ScaleTools.scaleImage(regionImage, targetWidth);
            }
            if (regionImage == null) {
                Image thumb = imageInfo.getThumbnail();
                if (thumb != null && (int) thumb.getWidth() == infoWidth) {
                    regionImage = CropTools.cropOutsideFx(task, thumb, region);
                    regionImage = mara.mybox.fximage.ScaleTools.scaleImage(regionImage, targetWidth);
                }
            }
            if (regionImage == null) {
                File file = imageInfo.getFile();
                if (file != null) {
                    BufferedImage bufferedImage;
                    String suffix = FileNameTools.ext(file.getName());
                    if (suffix != null && suffix.equalsIgnoreCase("pdf")) {
                        bufferedImage = readPDF(imageInfo, targetWidth);
                    } else if (suffix != null && (suffix.equalsIgnoreCase("ppt") || suffix.equalsIgnoreCase("pptx"))) {
                        bufferedImage = readPPT(imageInfo, targetWidth);
                    } else {
                        bufferedImage = ImageFileReaders.readFrame(task, imageInfo);
                    }
                    if (bufferedImage != null) {
                        regionImage = SwingFXUtils.toFXImage(bufferedImage, null);
                    }
                }
            }
            imageInfo.setRegionImage(regionImage);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return regionImage;
    }

    public static BufferedImage readBufferedImage(FxTask task, ImageInformation info) {
        Image image = readImage(task, info);
        if (image != null) {
            return SwingFXUtils.fromFXImage(image, null);
        } else {
            return null;
        }
    }

    public static boolean checkMem(FxTask task, ImageInformation imageInfo) {
        if (imageInfo == null) {
            return false;
        }
        try {
            if (imageInfo.getWidth() > 0 && imageInfo.getHeight() > 0) {
                long channels = imageInfo.getColorChannels() > 0 ? imageInfo.getColorChannels() : 4;
                long bytesSize = (long) (channels * imageInfo.getHeight() * imageInfo.getWidth());
                long requiredMem = bytesSize * 6L;

                Runtime r = Runtime.getRuntime();
                long availableMem = r.maxMemory() - (r.totalMemory() - r.freeMemory());
                if (availableMem < requiredMem) {
                    System.gc();
                    availableMem = r.maxMemory() - (r.totalMemory() - r.freeMemory());
                }

                imageInfo.setAvailableMem(availableMem);
                imageInfo.setBytesSize(bytesSize);
                imageInfo.setRequiredMem(requiredMem);

                if (availableMem < requiredMem) {
                    int scale = (int) Math.ceil(1d * requiredMem / availableMem);
//                    int scale = (int) Math.sqrt(1d * requiredMem / availableMem);
                    imageInfo.setNeedSample(true);
                    imageInfo.setSampleScale(scale);
                    imageInfo.setMaxWidth(imageInfo.getWidth() / scale);

                    if (task != null) {
                        int sampledWidth = (int) (imageInfo.getWidth() / scale);
                        int sampledHeight = (int) (imageInfo.getHeight() / scale);
                        int sampledSize = (int) (sampledWidth * sampledHeight * imageInfo.getColorChannels() / (1024 * 1024));
                        String msg = MessageFormat.format(message("ImageTooLarge"),
                                imageInfo.getWidth(), imageInfo.getHeight(), imageInfo.getColorChannels(),
                                bytesSize / (1024 * 1024), requiredMem / (1024 * 1024), availableMem / (1024 * 1024),
                                sampledWidth, sampledHeight, sampledSize);
                        task.setInfo(msg);
                    }

                } else {
                    double ratio = Math.sqrt(1d * availableMem / requiredMem);
                    imageInfo.setSampleScale(1);
                    imageInfo.setNeedSample(false);
                    imageInfo.setMaxWidth(imageInfo.getWidth() * ratio);

                    if (task != null) {
                        String msg = message("AvaliableMemory") + ": " + availableMem / (1024 * 1024) + "MB" + "\n"
                                + message("RequireMemory") + ": " + requiredMem / (1024 * 1024) + "MB";
                        task.setInfo(msg);
                    }
                }
            }

            return true;
        } catch (Exception e) {
            imageInfo.setError(e.toString());
            MyBoxLog.debug(e);
            return false;
        }
    }

    /*
        customized get/set
     */
    public double getWidth() {
        if (width <= 0 && image != null) {
            width = (int) image.getWidth();
        }
        return width;
    }

    public double getPickedWidth() {
        return region != null ? region.getWidth() : getWidth();
    }

    public double getHeight() {
        if (height <= 0 && image != null) {
            height = image.getHeight();
        }
        return height;
    }

    public double getPickedHeight() {
        return region != null ? region.getHeight() : getHeight();
    }

    public String getPixelsString() {
        if (region == null) {
            pixelsString = (int) width + "x" + (int) height;
        } else {
            pixelsString = message("Region") + " " + (int) region.getWidth() + "x" + (int) region.getHeight();
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

    public ImageInformation setRegion(double x1, double y1, double x2, double y2) {
        region = DoubleRectangle.xy12(x1, y1, x2, y2);
        regionImage = null;
        return this;
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

    public ImageInformation setIndex(int index) {
        this.index = index;
        return this;
    }

    public int getImageType() {
        return imageType;
    }

    public void setImageType(int imageType) {
        this.imageType = imageType;
    }

    public ImageInformation setWidth(double width) {
        this.width = width;
        return this;
    }

    public void setHeight(double height) {
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

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getXscale() {
        return xscale;
    }

    public ImageInformation setXscale(int xscale) {
        this.xscale = xscale;
        return this;
    }

    public int getYscale() {
        return yscale;
    }

    public ImageInformation setYscale(int yscale) {
        this.yscale = yscale;
        return this;
    }

    public double getRequiredWidth() {
        return requiredWidth;
    }

    public ImageInformation setRequiredWidth(double requiredWidth) {
        this.requiredWidth = requiredWidth;
        return this;
    }

    public double getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(double maxWidth) {
        this.maxWidth = maxWidth;
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

    public DoubleRectangle getRegion() {
        return region;
    }

    public Rectangle getIntRegion() {
        if (region == null) {
            return null;
        }
        return new Rectangle((int) region.getX(), (int) region.getY(),
                (int) region.getWidth(), (int) region.getHeight());
    }

    public Image getRegionImage() {
        return regionImage;
    }

    public ImageInformation setRegionImage(Image regionImage) {
        this.regionImage = regionImage;
        return this;
    }

    public int getDpi() {
        return dpi;
    }

    public void setDpi(int dpi) {
        this.dpi = dpi;
    }

}
