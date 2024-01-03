package mara.mybox.imagefile;

import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import mara.mybox.bufferedimage.BufferedImageTools;
import mara.mybox.bufferedimage.ImageColor;
import static mara.mybox.bufferedimage.ImageConvertTools.pixelSizeMm2dpi;
import mara.mybox.bufferedimage.ImageFileInformation;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.color.ColorBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.AppVariables.ImageHints;
import static mara.mybox.value.Languages.message;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import thridparty.image4j.ICODecoder;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:27
 * @License Apache License Version 2.0
 */
// https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/standard_metadata.html
public class ImageFileReaders {

    public static ImageReader getReader(ImageInputStream iis, String format) {
        try {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers == null || !readers.hasNext()) {
                return getReader(format);
            }
            return getReader(readers);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public static ImageReader getReader(String format) {
        try {
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(format.toLowerCase());
            return getReader(readers);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public static ImageReader getReader(Iterator<ImageReader> readers) {
        try {
            if (readers == null || !readers.hasNext()) {
                return null;
            }
            ImageReader reader = null;
            while (readers.hasNext()) {
                reader = readers.next();
                if (!reader.getClass().toString().contains("TIFFImageReader")
                        || reader instanceof com.github.jaiimageio.impl.plugins.tiff.TIFFImageReader) {
                    return reader;
                }
            }
            return reader;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public static BufferedImage readImage(FxTask task, File file) {
        ImageInformation readInfo = new ImageInformation(file);
        return readFrame(task, readInfo);
    }

    public static BufferedImage readImage(FxTask task, File file, int width) {
        ImageInformation imageInfo = new ImageInformation(file);
        imageInfo.setRequiredWidth(width);
        return readFrame(task, imageInfo);
    }

    public static BufferedImage readFrame(FxTask task, File file, int index) {
        ImageInformation imageInfo = new ImageInformation(file);
        imageInfo.setIndex(index);
        return readFrame(task, imageInfo);
    }

    public static BufferedImage readFrame(FxTask task, ImageInformation imageInfo) {
        if (imageInfo == null) {
            return null;
        }
        File file = imageInfo.getFile();
        if (file == null) {
            return null;
        }
        String format = imageInfo.getImageFormat();
        if (task != null) {
            task.setInfo(message("File") + ": " + file);
            task.setInfo(message("FileSize") + ": " + FileTools.showFileSize(file.length()));
            task.setInfo(message("Format") + ": " + format);
        }
        if ("ico".equals(format) || "icon".equals(format)) {
            return readIcon(task, imageInfo);
        }
        BufferedImage bufferedImage = null;
        try (ImageInputStream iis = ImageIO.createImageInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            ImageReader reader = getReader(iis, format);
            if (reader == null) {
                return null;
            }
            reader.setInput(iis, true, true);
            if (task != null && !task.isWorking()) {
                return null;
            }
            bufferedImage = readFrame(task, reader, imageInfo);
            reader.dispose();
        } catch (Exception e) {
            imageInfo.setError(e.toString());
        }
        return bufferedImage;
    }

    public static BufferedImage readFrame(FxTask task, ImageReader reader, ImageInformation imageInfo) {
        if (reader == null || imageInfo == null) {
            return null;
        }
        try {
            ImageReadParam param = reader.getDefaultReadParam();
            Rectangle region = imageInfo.getIntRegion();
            if (region != null) {
                param.setSourceRegion(region);
                if (task != null) {
                    task.setInfo(message("Region") + ": " + region.toString());
                }
            }
            int xscale = imageInfo.getXscale();
            int yscale = imageInfo.getYscale();
            if (xscale != 1 || yscale != 1) {
                param.setSourceSubsampling(xscale, yscale, 0, 0);
                if (task != null) {
                    task.setInfo(message("Scale") + ": " + " xscale=" + xscale + " yscale= " + yscale);
                }
            } else {
                ImageInformation.checkMem(task, imageInfo);
                int sampleScale = imageInfo.getSampleScale();
                if (sampleScale > 1) {
                    if (task != null) {
                        task.setInfo("sampleScale: " + sampleScale);
                    }
//                    param.setSourceSubsampling(sampleScale, sampleScale, 0, 0);
                    return null;
                }
            }
            BufferedImage bufferedImage;
            try {
                if (task != null) {
                    task.setInfo(message("Reading") + ": " + message("Frame") + " " + imageInfo.getIndex());
                }
                bufferedImage = reader.read(imageInfo.getIndex(), param);
                if (bufferedImage == null) {
                    return null;
                }
                if (task != null && !task.isWorking()) {
                    return null;
                }
                imageInfo.setImageType(bufferedImage.getType());
                int requiredWidth = (int) imageInfo.getRequiredWidth();
                if (requiredWidth > 0 && bufferedImage.getWidth() != requiredWidth) {
                    if (task != null) {
                        task.setInfo(message("Scale") + ": " + message("Width") + " " + requiredWidth);
                    }
                    bufferedImage = ScaleTools.scaleImageWidthKeep(bufferedImage, requiredWidth);
                } else if (ImageHints != null) {
                    bufferedImage = BufferedImageTools.applyRenderHints(bufferedImage, ImageHints);
                }
                return bufferedImage;
            } catch (Exception e) {
                if (task != null) {
                    task.setInfo(message("Error") + ": " + e.toString());
                }
                return readBrokenImage(task, e, imageInfo);
            }
        } catch (Exception e) {
            imageInfo.setError(e.toString());
            return null;
        }
    }

    public static ImageInformation makeInfo(FxTask task, File file, int width) {
        ImageInformation readInfo = new ImageInformation(file);
        readInfo.setRequiredWidth(width);
        return makeInfo(task, readInfo, false);
    }

    public static ImageInformation makeInfo(FxTask task, ImageInformation readInfo, boolean onlyInformation) {
        try {
            if (readInfo == null) {
                return null;
            }
            ImageFileInformation fileInfo = null;
            ImageInformation imageInfo = null;
            File file = readInfo.getFile();
            int index = readInfo.getIndex();
            int requiredWidth = (int) readInfo.getRequiredWidth();
            if (task != null) {
                task.setInfo(message("File") + ": " + file);
                task.setInfo(message("FileSize") + ": " + FileTools.showFileSize(file.length()));
                task.setInfo(message("Frame") + ": " + index);
                if (requiredWidth > 0) {
                    task.setInfo(message("LoadWidth") + ": " + requiredWidth);
                }
            }
            String format = readInfo.getImageFormat();
            if ("ico".equals(format) || "icon".equals(format)) {
                if (fileInfo == null) {
                    fileInfo = new ImageFileInformation(file);
                    ImageFileReaders.readImageFileMetaData(task, null, fileInfo);
                }
                if (task != null && !task.isWorking()) {
                    return null;
                }
                if (fileInfo.getImagesInformation() == null) {
                    return null;
                }
                int framesNumber = fileInfo.getImagesInformation().size();
                if (task != null) {
                    task.setInfo(message("FramesNumber") + ": " + framesNumber);
                }
                if (framesNumber > 0 && index < framesNumber) {
                    imageInfo = fileInfo.getImagesInformation().get(index);
                    if (task != null) {
                        task.setInfo(message("Pixels") + ": " + (int) imageInfo.getWidth() + "x" + (int) imageInfo.getHeight());
                    }
                    if (!onlyInformation) {
                        if (task != null) {
                            task.setInfo(message("Reading") + ": " + message("Frame")
                                    + " " + index + "/" + framesNumber);
                        }
                        imageInfo.setRequiredWidth(requiredWidth);
                        BufferedImage bufferedImage = readIcon(task, imageInfo);
                        if (task != null && !task.isWorking()) {
                            return null;
                        }
                        imageInfo.loadBufferedImage(bufferedImage);
                    }
                }
            } else {
                try (ImageInputStream iis = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                    ImageReader reader = getReader(iis, format);
                    if (reader != null) {
                        reader.setInput(iis, false, false);
                        fileInfo = new ImageFileInformation(file);
                        if (task != null) {
                            task.setInfo(message("Reading") + ": " + message("MetaData"));
                        }
                        ImageFileReaders.readImageFileMetaData(task, reader, fileInfo);
                        if (task != null && !task.isWorking()) {
                            return null;
                        }
                        if (fileInfo.getImagesInformation() == null) {
                            return null;
                        }
                        int framesNumber = fileInfo.getImagesInformation().size();
                        if (task != null) {
                            task.setInfo(message("FramesNumber") + ": " + framesNumber);
                        }
                        if (framesNumber > 0 && index < framesNumber) {
                            imageInfo = fileInfo.getImagesInformation().get(index);
                            if (task != null) {
                                task.setInfo(message("Pixels") + ": " + (int) imageInfo.getWidth() + "x" + (int) imageInfo.getHeight());
                            }
                            if (!onlyInformation) {
                                if (task != null) {
                                    task.setInfo(message("Reading") + ": " + message("Frame")
                                            + " " + index + "/" + framesNumber);
                                }
                                imageInfo.setRequiredWidth(requiredWidth);
                                BufferedImage bufferedImage = readFrame(task, reader, imageInfo);
                                if (task != null && !task.isWorking()) {
                                    return null;
                                }
                                imageInfo.loadBufferedImage(bufferedImage);
                            }
                        }
                        reader.dispose();
                    } else {
                        if (task != null) {
                            task.setError("Fail to get reader");
                        }
                    }
                } catch (Exception e) {
                    if (task != null) {
                        task.setError(e.toString());
                    } else {
                        MyBoxLog.error(e.toString());
                    }
                    return null;
                }
            }
            return imageInfo;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }
    }

    public static BufferedImage readIcon(FxTask task, File srcFile) {
        return readIcon(task, srcFile, 0);
    }

    public static BufferedImage readIcon(FxTask task, File srcFile, int index) {
        try {
            if (srcFile == null) {
                return null;
            }
            List<BufferedImage> frames = ICODecoder.read(srcFile);
            if (frames == null || frames.isEmpty()) {
                return null;
            }
            return frames.get(index >= 0 && index < frames.size() ? index : 0);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }
    }

    public static BufferedImage readIcon(FxTask task, ImageInformation imageInfo) {
        try {
            BufferedImage bufferedImage = readIcon(task, imageInfo.getFile(), imageInfo.getIndex());
            if (task != null && !task.isWorking()) {
                return null;
            }
            return adjust(task, imageInfo, bufferedImage);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }
    }

    // call this only when region and scale is not handled when create bufferedImage
    public static BufferedImage adjust(FxTask task, ImageInformation imageInfo, BufferedImage bufferedImage) {
        try {
            int requiredWidth = (int) imageInfo.getRequiredWidth();
            int bmWidth = bufferedImage.getWidth();
            int xscale = imageInfo.getXscale();
            int yscale = imageInfo.getYscale();
            Rectangle region = imageInfo.getIntRegion();
            if (region == null) {
                if (xscale != 1 || yscale != 1) {
                    bufferedImage = ScaleTools.scaleImageByScale(bufferedImage, xscale, yscale);
                } else if (requiredWidth > 0 && bmWidth != requiredWidth) {
                    bufferedImage = ScaleTools.scaleImageWidthKeep(bufferedImage, requiredWidth);
                }
            } else {
                if (xscale != 1 || yscale != 1) {
                    bufferedImage = mara.mybox.bufferedimage.CropTools.sample(task, bufferedImage,
                            imageInfo.getRegion(), xscale, yscale);
                } else {
                    bufferedImage = mara.mybox.bufferedimage.CropTools.sample(task, bufferedImage,
                            imageInfo.getRegion(), requiredWidth);
                }
            }
            return bufferedImage;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }

    }

    /*
        Broken image
     */
    public static BufferedImage readBrokenImage(FxTask task, Exception e, ImageInformation imageInfo) {
        BufferedImage image = null;
        try {
            File file = imageInfo.getFile();
            if (e == null || file == null) {
                return null;
            }
            String format = FileNameTools.suffix(file.getName()).toLowerCase();
            switch (format) {
                case "gif":
                    // Read Gif with JDK api normally. When broken, use DhyanB's API.
                    // if (e.toString().contains("java.lang.ArrayIndexOutOfBoundsException: 4096")) {
                    image = ImageGifFile.readBrokenGifFile(task, imageInfo);
//                    if (e.toString().contains("java.lang.ArrayIndexOutOfBoundsException")) {
//                        image = ImageGifFile.readBrokenGifFile(imageInfo);
//                    }
                    break;
                case "jpg":
                case "jpeg":
                    image = ImageJpgFile.readBrokenJpgFile(task, imageInfo);
//                    if (e.toString().contains("Unsupported Image Type")) {
//                        image = ImageJpgFile.readBrokenJpgFile(imageInfo);
//                    }
                    break;
                default:
                    if (task != null) {
                        task.setError(e.toString());
                    } else {
                        MyBoxLog.error(e.toString());
                    }
            }
        } catch (Exception ex) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
        }
        return image;
    }

    /*
        Meta data
     */
    public static ImageFileInformation readImageFileMetaData(FxTask task, String fileName) {
        return readImageFileMetaData(task, new File(fileName));
    }

    public static ImageFileInformation readImageFileMetaData(FxTask task, File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }
        if (task != null) {
            task.setInfo(message("ReadingMedia...") + ": " + file);
        }
        ImageFileInformation fileInfo = new ImageFileInformation(file);
        String format = fileInfo.getImageFormat();
        if ("ico".equals(format) || "icon".equals(format)) {
            fileInfo = ImageFileInformation.readIconFile(task, file);
        } else {
            try (ImageInputStream iis = ImageIO.createImageInputStream(
                    new BufferedInputStream(new FileInputStream(file)))) {
                ImageReader reader = getReader(iis, format);
                if (reader == null) {
                    return null;
                }
                reader.setInput(iis, false, false);
                readImageFileMetaData(task, reader, fileInfo);
                reader.dispose();
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                } else {
                    MyBoxLog.error(e.toString());
                }
            }
        }
        return fileInfo;
    }

    public static boolean readImageFileMetaData(FxTask task,
            ImageReader reader, ImageFileInformation fileInfo) {
        try {
            if (fileInfo == null) {
                return false;
            }
            String targetFormat = fileInfo.getImageFormat();
            File file = fileInfo.getFile();
            List<ImageInformation> imagesInfo = new ArrayList<>();
            if (reader == null) {
                if (task != null) {
                    task.setInfo("fail to get reader");
                }
                fileInfo.setNumberOfImages(1);
                ImageInformation imageInfo = ImageInformation.create(targetFormat, file);
                imageInfo.setImageFileInformation(fileInfo);
                imageInfo.setImageFormat(targetFormat);
                imageInfo.setFile(file);
                imageInfo.setCreateTime(fileInfo.getCreateTime());
                imageInfo.setModifyTime(fileInfo.getModifyTime());
                imageInfo.setFileSize(fileInfo.getFileSize());
                imageInfo.setIndex(0);
                ImageInformation.checkMem(task, imageInfo);
                imagesInfo.add(imageInfo);
                fileInfo.setImagesInformation(imagesInfo);
                fileInfo.setImageInformation(imageInfo);
                return true;
            }
            String format = reader.getFormatName().toLowerCase();
            fileInfo.setImageFormat(format);
            int num = reader.getNumImages(true);
            fileInfo.setNumberOfImages(num);
            if (task != null) {
                task.setInfo("Number Of Images: " + num);
            }

            ImageInformation imageInfo;
            for (int i = 0; i < num; ++i) {
                if (task != null && !task.isWorking()) {
                    return false;
                }
                if (task != null) {
                    task.setInfo(message("Handle") + ": " + i + "/" + num);
                }
                imageInfo = ImageInformation.create(format, file);
                imageInfo.setImageFileInformation(fileInfo);
                imageInfo.setImageFormat(format);
                imageInfo.setFile(file);
                imageInfo.setCreateTime(fileInfo.getCreateTime());
                imageInfo.setModifyTime(fileInfo.getModifyTime());
                imageInfo.setFileSize(fileInfo.getFileSize());
                imageInfo.setWidth(reader.getWidth(i));
                imageInfo.setHeight(reader.getHeight(i));
                imageInfo.setPixelAspectRatio(reader.getAspectRatio(i));
                imageInfo.setIsMultipleFrames(num > 1);

                imageInfo.setIsTiled(reader.isImageTiled(i));
                imageInfo.setIndex(i);

                Iterator<ImageTypeSpecifier> types = reader.getImageTypes(i);
                List<ImageTypeSpecifier> typesValue = new ArrayList<>();
                if (types != null) {
                    while (types.hasNext()) {
                        if (task != null && !task.isWorking()) {
                            return false;
                        }
                        ImageTypeSpecifier t = types.next();
                        typesValue.add(t);
                        if (task != null) {
                            task.setInfo("ImageTypeSpecifier : " + t.getClass());
                        }
                    }
                    ImageTypeSpecifier imageType = reader.getRawImageType(i);
                    ColorModel colorModel = null;
                    if (imageType != null) {
                        imageInfo.setRawImageType(imageType);
                        colorModel = imageType.getColorModel();
                    }
                    if (colorModel == null) {
                        if (!typesValue.isEmpty()) {
                            colorModel = typesValue.get(0).getColorModel();
                        }
                    }
                    if (colorModel != null) {
                        ColorSpace colorSpace = colorModel.getColorSpace();
                        imageInfo.setColorSpace(ColorBase.colorSpaceType(colorSpace.getType()));
                        imageInfo.setColorChannels(colorModel.getNumComponents());
                        imageInfo.setBitDepth(colorModel.getPixelSize());
                    }
                }
                imageInfo.setImageTypeSpecifiers(typesValue);
                try {
                    imageInfo.setPixelAspectRatio(reader.getAspectRatio(i));
                } catch (Exception e) {
                    if (task != null) {
                        task.setError(e.toString());
                    } else {
                        MyBoxLog.error(e.toString());
                    }
                }
                try {
                    imageInfo.setHasThumbnails(reader.hasThumbnails(i));
                    imageInfo.setNumberOfThumbnails(reader.getNumThumbnails(i));
                } catch (Exception e) {
                    if (task != null) {
                        task.setError(e.toString());
                    } else {
                        MyBoxLog.error(e.toString());
                    }
                }
                try {
                    readImageMetaData(task, format, imageInfo, reader.getImageMetadata(i));
                } catch (Exception e) {
                    if (task != null) {
                        task.setError(e.toString());
                    } else {
                        MyBoxLog.error(e.toString());
                    }
                }
                ImageInformation.checkMem(task, imageInfo);
                imagesInfo.add(imageInfo);
            }
            fileInfo.setImagesInformation(imagesInfo);
            if (!imagesInfo.isEmpty()) {
                fileInfo.setImageInformation(imagesInfo.get(0));
            }
            return task == null || task.isWorking();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return false;
        }
    }

    public static boolean readImageMetaData(FxTask task, String format,
            ImageInformation imageInfo, IIOMetadata iioMetaData) {
        try {
            if (imageInfo == null || iioMetaData == null) {
                return false;
            }
            if (task != null) {
                task.setInfo("read Image Meta Data : " + format);
            }
            StringBuilder metaDataXml = new StringBuilder();
            String[] formatNames = iioMetaData.getMetadataFormatNames();
            Map<String, Map<String, List<Map<String, Object>>>> metaData = new HashMap<>();
            for (String formatName : formatNames) {
                if (task != null && !task.isWorking()) {
                    return false;
                }
                Map<String, List<Map<String, Object>>> formatMetaData = new HashMap<>();
                IIOMetadataNode tree = (IIOMetadataNode) iioMetaData.getAsTree(formatName);
                readImageMetaData(task, formatMetaData, metaDataXml, tree, 2);
                metaData.put(formatName, formatMetaData);
            }
            imageInfo.setMetaData(metaData);
            imageInfo.setMetaDataXml(metaDataXml.toString());

            explainCommonMetaData(metaData, imageInfo);
            switch (format.toLowerCase()) {
                case "png":
                    ImagePngFile.explainPngMetaData(metaData, imageInfo);
                    break;
                case "jpg":
                case "jpeg":
                    ImageJpgFile.explainJpegMetaData(metaData, imageInfo);
                    break;
                case "gif":
                    ImageGifFile.explainGifMetaData(metaData, imageInfo);
                    break;
                case "bmp":
                    ImageBmpFile.explainBmpMetaData(metaData, imageInfo);
                    break;
                case "tif":
                case "tiff":
                    ImageTiffFile.explainTiffMetaData(iioMetaData, imageInfo);
                    break;
                default:

            }
//            MyBoxLog.debug(metaData);
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return false;
        }
    }

    public static boolean readImageMetaData(FxTask task, Map<String, List<Map<String, Object>>> formatMetaData,
            StringBuilder metaDataXml, IIOMetadataNode node, int level) {
        try {
            String lineSeparator = System.getProperty("line.separator");
            for (int i = 0; i < level; ++i) {
                metaDataXml.append("    ");
            }
            metaDataXml.append("<").append(node.getNodeName());
            Map<String, Object> nodeAttrs = new HashMap<>();
            NamedNodeMap map = node.getAttributes();
            boolean isTiff = "TIFFField".equals(node.getNodeName());
            if (map != null && map.getLength() > 0) {
                int length = map.getLength();
                for (int i = 0; i < length; ++i) {
                    if (task != null && !task.isWorking()) {
                        return false;
                    }
                    Node attr = map.item(i);
                    String name = attr.getNodeName();
                    String value = attr.getNodeValue();
                    if (!isTiff) {
                        nodeAttrs.put(name, value);
                    }
                    metaDataXml.append(" ").append(name).append("=\"").append(value).append("\"");
                    if (isTiff && "ICC Profile".equals(value)) {
                        metaDataXml.append(" value=\"skip...\"/>").append(lineSeparator);
                        return true;
                    }
                }
            }
            Object userObject = node.getUserObject();
            if (userObject != null) {
                if (!isTiff) {
                    nodeAttrs.put("UserObject", userObject);
                }
                metaDataXml.append(" ").append("UserObject=\"skip...\"");
            }
            if (!isTiff && !nodeAttrs.isEmpty()) {
                List<Map<String, Object>> nodeAttrsList = formatMetaData.get(node.getNodeName());
                if (nodeAttrsList == null) {
                    nodeAttrsList = new ArrayList<>();
                }
                nodeAttrsList.add(nodeAttrs);
                formatMetaData.put(node.getNodeName(), nodeAttrsList);
            }
            IIOMetadataNode child = (IIOMetadataNode) node.getFirstChild();
            if (child == null) {
                metaDataXml.append("/>").append(lineSeparator);
                return true;
            }
            metaDataXml.append(">").append(lineSeparator);
            while (child != null) {
                if (task != null && !task.isWorking()) {
                    return false;
                }
                readImageMetaData(task, formatMetaData, metaDataXml, child, level + 1);
                child = (IIOMetadataNode) child.getNextSibling();
            }
            for (int i = 0; i < level; ++i) {
                metaDataXml.append("    ");
            }
            metaDataXml.append("</").append(node.getNodeName()).append(">").append(lineSeparator);
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return false;
        }
    }

    // https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/standard_metadata.html
    public static void explainCommonMetaData(Map<String, Map<String, List<Map<String, Object>>>> metaData,
            ImageInformation imageInfo) {
        try {
            if (!metaData.containsKey("javax_imageio_1.0")) {
                return;
            }
            Map<String, List<Map<String, Object>>> javax_imageio = metaData.get("javax_imageio_1.0");
//            MyBoxLog.debug("explainCommonMetaData");
            if (javax_imageio.containsKey("ColorSpaceType")) {
                Map<String, Object> ColorSpaceType = javax_imageio.get("ColorSpaceType").get(0);
                if (ColorSpaceType.containsKey("name")) {
                    imageInfo.setColorSpace((String) ColorSpaceType.get("name"));
//                    MyBoxLog.debug(" colorSpaceType:" + ColorSpaceType.get("name"));
                }
            }
            if (javax_imageio.containsKey("NumChannels")) {
                Map<String, Object> NumChannels = javax_imageio.get("NumChannels").get(0);
                if (NumChannels.containsKey("value")) {
                    imageInfo.setColorChannels(Integer.parseInt((String) NumChannels.get("value")));
//                    MyBoxLog.debug(" NumChannels:" + NumChannels.get("value"));
                }
            }
            if (javax_imageio.containsKey("Gamma")) {
                Map<String, Object> Gamma = javax_imageio.get("Gamma").get(0);
                if (Gamma.containsKey("value")) {
                    imageInfo.setGamma(Float.parseFloat((String) Gamma.get("value")));
                }
            }
            if (javax_imageio.containsKey("BlackIsZero")) {
                Map<String, Object> BlackIsZero = javax_imageio.get("BlackIsZero").get(0);
                if (BlackIsZero.containsKey("value")) {
                    imageInfo.setBlackIsZero(BlackIsZero.get("value").equals("TRUE"));
                }
            }
            if (javax_imageio.containsKey("PaletteEntry")) {
                List<Map<String, Object>> PaletteEntryList = javax_imageio.get("PaletteEntry");
                imageInfo.setStandardAttribute("PaletteSize", PaletteEntryList.size());
//                List<ImageColor> Palette = new ArrayList<>();
//                for (Map<String, Object> PaletteEntry : PaletteEntryList) {
//                    int index = Integer.parseInt(PaletteEntry.get("index"));
//                    int red = Integer.parseInt(PaletteEntry.get("red"));
//                    int green = Integer.parseInt(PaletteEntry.get("green"));
//                    int blue = Integer.parseInt(PaletteEntry.get("blue"));
//                    int alpha = 255;
//                    if (PaletteEntry.containsKey("alpha")) {
//                        alpha = Integer.parseInt(PaletteEntry.get("alpha"));
//                    }
//                    Palette.add(new ImageColor(index, red, green, blue, alpha));
//                }
//                imageInfo.setPalette(Palette);
            }
            if (javax_imageio.containsKey("BackgroundIndex")) {
                Map<String, Object> BackgroundIndex = javax_imageio.get("BackgroundIndex").get(0);
                if (BackgroundIndex.containsKey("value")) {
                    imageInfo.setBackgroundIndex(Integer.parseInt((String) BackgroundIndex.get("value")));
                }
            }
            if (javax_imageio.containsKey("BackgroundColor")) {
                Map<String, Object> BackgroundColor = javax_imageio.get("BackgroundColor").get(0);
                int red = Integer.parseInt((String) BackgroundColor.get("red"));
                int green = Integer.parseInt((String) BackgroundColor.get("green"));
                int blue = Integer.parseInt((String) BackgroundColor.get("blue"));
                imageInfo.setBackgroundColor(new ImageColor(red, green, blue));
            }
            if (javax_imageio.containsKey("CompressionTypeName")) {
                Map<String, Object> CompressionTypeName = javax_imageio.get("CompressionTypeName").get(0);
                if (CompressionTypeName.containsKey("value")) {
                    imageInfo.setCompressionType((String) CompressionTypeName.get("value"));
                }
            }
            if (javax_imageio.containsKey("Lossless")) {
                Map<String, Object> Lossless = javax_imageio.get("Lossless").get(0);
                if (Lossless.containsKey("value")) {
                    imageInfo.setIsLossless(Lossless.get("value").equals("TRUE"));
                }
            }
            if (javax_imageio.containsKey("NumProgressiveScans")) {
                Map<String, Object> NumProgressiveScans = javax_imageio.get("NumProgressiveScans").get(0);
                if (NumProgressiveScans.containsKey("value")) {
                    imageInfo.setNumProgressiveScans(Integer.parseInt((String) NumProgressiveScans.get("value")));
                }
            }
            if (javax_imageio.containsKey("BitRate")) {
                Map<String, Object> BitRate = javax_imageio.get("BitRate").get(0);
                if (BitRate.containsKey("value")) {
                    imageInfo.setBitRate(Float.parseFloat((String) BitRate.get("value")));
                }
            }
            if (javax_imageio.containsKey("PlanarConfiguration")) {
                Map<String, Object> PlanarConfiguration = javax_imageio.get("PlanarConfiguration").get(0);
                if (PlanarConfiguration.containsKey("value")) {
                    imageInfo.setPlanarConfiguration((String) PlanarConfiguration.get("value"));
                }
            }
            if (javax_imageio.containsKey("SampleFormat")) {
                Map<String, Object> SampleFormat = javax_imageio.get("SampleFormat").get(0);
                if (SampleFormat.containsKey("value")) {
                    imageInfo.setSampleFormat((String) SampleFormat.get("value"));
                }
            }
            if (javax_imageio.containsKey("BitsPerSample")) {
                Map<String, Object> BitsPerSample = javax_imageio.get("BitsPerSample").get(0);
                if (BitsPerSample.containsKey("value")) {
                    imageInfo.setBitsPerSample((String) BitsPerSample.get("value"));
                }
            }
            if (javax_imageio.containsKey("SignificantBitsPerSample")) {
                Map<String, Object> SignificantBitsPerSample = javax_imageio.get("SignificantBitsPerSample").get(0);
                if (SignificantBitsPerSample.containsKey("value")) {
                    imageInfo.setSignificantBitsPerSample((String) SignificantBitsPerSample.get("value"));
                }
            }
            if (javax_imageio.containsKey("SampleMSB")) {
                Map<String, Object> SampleMSB = javax_imageio.get("SampleMSB").get(0);
                if (SampleMSB.containsKey("value")) {
                    imageInfo.setSampleMSB((String) SampleMSB.get("value"));
                }
            }
            if (javax_imageio.containsKey("PixelAspectRatio")) {
                Map<String, Object> PixelAspectRatio = javax_imageio.get("PixelAspectRatio").get(0);
                if (PixelAspectRatio.containsKey("value")) {
                    imageInfo.setPixelAspectRatio(Float.parseFloat((String) PixelAspectRatio.get("value")));
                }
            }
            if (javax_imageio.containsKey("ImageOrientation")) {
                Map<String, Object> ImageOrientation = javax_imageio.get("ImageOrientation").get(0);
                if (ImageOrientation.containsKey("value")) {
                    imageInfo.setImageRotation((String) ImageOrientation.get("value"));
                }
            }
            if (javax_imageio.containsKey("HorizontalPixelSize")) { // The width of a pixel, in millimeters
                Map<String, Object> HorizontalPixelSize = javax_imageio.get("HorizontalPixelSize").get(0);
                if (HorizontalPixelSize.containsKey("value")) {
                    float v = Float.parseFloat((String) HorizontalPixelSize.get("value"));
                    imageInfo.setHorizontalPixelSize(v);
                    imageInfo.setXDpi(pixelSizeMm2dpi(v));
                }
            }
            if (javax_imageio.containsKey("VerticalPixelSize")) { // The height of a pixel, in millimeters
                Map<String, Object> VerticalPixelSize = javax_imageio.get("VerticalPixelSize").get(0);
                if (VerticalPixelSize.containsKey("value")) {
                    float v = Float.parseFloat((String) VerticalPixelSize.get("value"));
                    imageInfo.setVerticalPixelSize(v);
                    imageInfo.setYDpi(pixelSizeMm2dpi(v));
                }
            }
            if (javax_imageio.containsKey("HorizontalPhysicalPixelSpacing")) {
                Map<String, Object> HorizontalPhysicalPixelSpacing = javax_imageio.get("HorizontalPhysicalPixelSpacing").get(0);
                if (HorizontalPhysicalPixelSpacing.containsKey("value")) {
                    float v = Float.parseFloat((String) HorizontalPhysicalPixelSpacing.get("value"));
                    imageInfo.setHorizontalPhysicalPixelSpacing(v);
                }
            }
            if (javax_imageio.containsKey("VerticalPhysicalPixelSpacing")) {
                Map<String, Object> VerticalPhysicalPixelSpacing = javax_imageio.get("VerticalPhysicalPixelSpacing").get(0);
                if (VerticalPhysicalPixelSpacing.containsKey("value")) {
                    float v = Float.parseFloat((String) VerticalPhysicalPixelSpacing.get("value"));
                    imageInfo.setVerticalPhysicalPixelSpacing(v);
                }
            }
            if (javax_imageio.containsKey("HorizontalPosition")) {
                Map<String, Object> HorizontalPosition = javax_imageio.get("HorizontalPosition").get(0);
                if (HorizontalPosition.containsKey("value")) {
                    float v = Float.parseFloat((String) HorizontalPosition.get("value"));
                    imageInfo.setHorizontalPosition(v);
                }
            }
            if (javax_imageio.containsKey("VerticalPosition")) {
                Map<String, Object> VerticalPosition = javax_imageio.get("VerticalPosition").get(0);
                if (VerticalPosition.containsKey("value")) {
                    float v = Float.parseFloat((String) VerticalPosition.get("value"));
                    imageInfo.setVerticalPosition(v);
                }
            }
            if (javax_imageio.containsKey("HorizontalPixelOffset")) {
                Map<String, Object> HorizontalPixelOffset = javax_imageio.get("HorizontalPixelOffset").get(0);
                if (HorizontalPixelOffset.containsKey("value")) {
                    float v = Float.parseFloat((String) HorizontalPixelOffset.get("value"));
                    imageInfo.setHorizontalPixelOffset(v);
                }
            }
            if (javax_imageio.containsKey("VerticalPixelOffset")) {
                Map<String, Object> VerticalPixelOffset = javax_imageio.get("VerticalPixelOffset").get(0);
                if (VerticalPixelOffset.containsKey("value")) {
                    float v = Float.parseFloat((String) VerticalPixelOffset.get("value"));
                    imageInfo.setVerticalPixelOffset(v);
                }
            }
            if (javax_imageio.containsKey("HorizontalScreenSize")) {
                Map<String, Object> HorizontalScreenSize = javax_imageio.get("HorizontalScreenSize").get(0);
                if (HorizontalScreenSize.containsKey("value")) {
                    float v = Float.parseFloat((String) HorizontalScreenSize.get("value"));
                    imageInfo.setHorizontalScreenSize(v);
                }
            }
            if (javax_imageio.containsKey("VerticalScreenSize")) {
                Map<String, Object> VerticalScreenSize = javax_imageio.get("VerticalScreenSize").get(0);
                if (VerticalScreenSize.containsKey("value")) {
                    float v = Float.parseFloat((String) VerticalScreenSize.get("value"));
                    imageInfo.setVerticalScreenSize(v);
                }
            }
            if (javax_imageio.containsKey("FormatVersion")) {
                Map<String, Object> FormatVersion = javax_imageio.get("FormatVersion").get(0);
                if (FormatVersion.containsKey("value")) {
                    imageInfo.setFormatVersion((String) FormatVersion.get("value"));
                }
            }
            if (javax_imageio.containsKey("SubimageInterpretation")) {
                Map<String, Object> SubimageInterpretation = javax_imageio.get("SubimageInterpretation").get(0);
                if (SubimageInterpretation.containsKey("value")) {
                    imageInfo.setSubimageInterpretation((String) SubimageInterpretation.get("value"));
                }
            }
            if (javax_imageio.containsKey("ImageCreationTime")) {
                Map<String, Object> ImageCreationTime = javax_imageio.get("ImageCreationTime").get(0);
                String t = ImageCreationTime.get("year") + "-" + ImageCreationTime.get("month") + "-"
                        + ImageCreationTime.get("day");
                if (ImageCreationTime.containsKey("hour")) {
                    t += " " + ImageCreationTime.get("hour");
                } else {
                    t += " 00";
                }
                if (ImageCreationTime.containsKey("minute")) {
                    t += ":" + ImageCreationTime.get("minute");
                } else {
                    t += ":00";
                }
                if (ImageCreationTime.containsKey("second")) {
                    t += ":" + ImageCreationTime.get("second");
                } else {
                    t += ":00";
                }
                imageInfo.setImageCreationTime(t);
            }
            if (javax_imageio.containsKey("ImageModificationTime")) {
                Map<String, Object> ImageModificationTime = javax_imageio.get("ImageModificationTime").get(0);
                String t = ImageModificationTime.get("year") + "-" + ImageModificationTime.get("month") + "-"
                        + ImageModificationTime.get("day");
                if (ImageModificationTime.containsKey("hour")) {
                    t += " " + ImageModificationTime.get("hour");
                } else {
                    t += " 00";
                }
                if (ImageModificationTime.containsKey("minute")) {
                    t += ":" + ImageModificationTime.get("minute");
                } else {
                    t += ":00";
                }
                if (ImageModificationTime.containsKey("second")) {
                    t += ":" + ImageModificationTime.get("second");
                } else {
                    t += ":00";
                }
                imageInfo.setImageModificationTime(t);
            }
            if (javax_imageio.containsKey("Alpha")) {
                Map<String, Object> Alpha = javax_imageio.get("Alpha").get(0);
                if (Alpha.containsKey("value")) {
                    imageInfo.setAlpha((String) Alpha.get("value"));
                }
            }
            if (javax_imageio.containsKey("TransparentIndex")) {
                Map<String, Object> TransparentIndex = javax_imageio.get("TransparentIndex").get(0);
                if (TransparentIndex.containsKey("value")) {
                    imageInfo.setTransparentIndex(Integer.parseInt((String) TransparentIndex.get("value")));
                }
            }
            if (javax_imageio.containsKey("TransparentColor")) {
                Map<String, Object> TransparentColor = javax_imageio.get("TransparentColor").get(0);
                if (TransparentColor.containsKey("value")) {
                    imageInfo.setTransparentColor((String) TransparentColor.get("value"));
                }
            }

        } catch (Exception e) {

        }
    }

    public static IIOMetadata getIIOMetadata(FxTask task, String format, File file) {
        switch (format) {
            case "bmp":
                IIOMetadata bm = ImageBmpFile.getBmpIIOMetadata(file);
                if (bm != null) {
                    return bm;
                }
                break;
//            case "gif":
            //                return ImageGifTools.getGifMetadata(file);
            case "tif":
            case "tiff":
                IIOMetadata tm = ImageTiffFile.getTiffIIOMetadata(file);
                if (tm != null) {
                    return tm;
                }
                break;
            case "pcx":
                IIOMetadata pm = ImagePcxFile.getPcxMetadata(file);
                if (pm != null) {
                    return pm;
                }
                break;
            case "pnm":
                IIOMetadata pnm = ImagePnmFile.getPnmMetadata(file);
                if (pnm != null) {
                    return pnm;
                }
                break;
            case "wbmp":
                IIOMetadata wm = ImageWbmpFile.getWbmpMetadata(file);
                if (wm != null) {
                    return wm;
                }
        }
        return getIIOMetadata(task, file);
    }

    public static IIOMetadata getIIOMetadata(FxTask task, File file) {
        IIOMetadata iioMetaData = null;
        try (ImageInputStream iis = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            ImageReader reader = getReader(iis, FileNameTools.suffix(file.getName()).toLowerCase());
            if (reader == null) {
                return null;
            }
            reader.setInput(iis, true, false);
            iioMetaData = reader.getImageMetadata(0);
            reader.dispose();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
        }
        return iioMetaData;
    }

}
