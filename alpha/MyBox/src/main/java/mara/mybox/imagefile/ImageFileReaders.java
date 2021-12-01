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
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import mara.mybox.bufferedimage.CropTools;
import mara.mybox.bufferedimage.ImageColor;
import static mara.mybox.bufferedimage.ImageConvertTools.pixelSizeMm2dpi;
import mara.mybox.bufferedimage.ImageFileInformation;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.color.ColorBase;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileNameTools;
import net.sf.image4j.codec.ico.ICODecoder;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:27
 * @License Apache License Version 2.0
 */
// https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/standard_metadata.html
public class ImageFileReaders {

    public static ImageReader getReader(ImageInputStream iis, String format) {
        if (iis == null) {
            return null;
        }
        try {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers == null || !readers.hasNext()) {
                return getReader(format);
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
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static ImageReader getReader(String format) {
        if (format == null) {
            return null;
        }
        try {
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(format.toLowerCase());
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
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static ImageInformation readInfo(File file, int width) {
        Object ret = readFrame(file, false, 0, width, null);
        if (ret != null && ret instanceof ImageInformation) {
            return (ImageInformation) ret;
        } else {
            return null;
        }
    }

    public static Object readFrame(File file, boolean onlyInformation, int index, int width, ImageInformation existImageInfo) {
        try {
            if (file == null) {
                return null;
            }
            ImageFileInformation fileInfo = null;
            ImageInformation imageInfo = null;
            if (existImageInfo != null) {
                ImageFileInformation referFileInfo = existImageInfo.getImageFileInformation();
                if (referFileInfo != null && referFileInfo.getFile().equals(file)) {
                    int size = referFileInfo.getNumberOfImages();
                    if (size > 0) {
                        fileInfo = referFileInfo;
                        int targetIndex = index;
                        if (targetIndex < 0) {
                            targetIndex = size > 0 ? size - 1 : 0;
                        }
                        if (targetIndex >= size) {
                            targetIndex = 0;
                        }
                        if (targetIndex == existImageInfo.getIndex()) {
                            Image referThumb = existImageInfo.getThumbnail();
                            if (referThumb != null) {
                                int targetWidth = width <= 0 ? existImageInfo.getWidth() : width;
                                if (referThumb.getWidth() == targetWidth) {
                                    return existImageInfo;
                                }
                            }
                        }
                    }
                }
            }

            int targetIndex = index, targetWidth = width;
            String format = FileNameTools.getFileSuffix(file);
            BufferedImage bufferedImage = null;
            if ("ico".equals(format) || "icon".equals(format)) {
                if (fileInfo == null) {
                    fileInfo = new ImageFileInformation(file);
                    ImageFileReaders.readImageFileMetaData(null, fileInfo);
                }
                int size = fileInfo.getNumberOfImages();
                if (size > 0) {
                    if (targetIndex < 0) {
                        targetIndex = size > 0 ? size - 1 : 0;
                    }
                    if (targetIndex >= size) {
                        targetIndex = 0;
                    }
                    imageInfo = fileInfo.getImagesInformation().get(targetIndex);
                    if (!onlyInformation) {
                        bufferedImage = readIcon(file, targetIndex);
                        bufferedImage = ScaleTools.scaleImageWidthKeep(bufferedImage, targetWidth);
                    }
                }
            } else {
                try ( ImageInputStream iis = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                    ImageReader reader = getReader(iis, format);
                    if (reader != null) {
                        if (fileInfo == null) {
                            reader.setInput(iis, false, false);
                            fileInfo = new ImageFileInformation(file);
                            ImageFileReaders.readImageFileMetaData(reader, fileInfo);
                        } else {
                            reader.setInput(iis, false, true);
                        }
                        int size = fileInfo.getNumberOfImages();
                        if (size > 0) {
                            if (targetIndex < 0) {
                                targetIndex = size > 0 ? size - 1 : 0;
                            }
                            if (targetIndex >= size) {
                                targetIndex = 0;
                            }
                            imageInfo = fileInfo.getImagesInformation().get(targetIndex);
                            imageInfo.setThumbnail(null);
                            if (!onlyInformation) {
                                targetWidth = targetWidth <= 0 ? imageInfo.getWidth() : targetWidth;
                                int maxWidth = ImageInformation.countMaxWidth(imageInfo);
                                if (targetWidth > maxWidth) {
                                    System.gc();
                                    maxWidth = ImageInformation.countMaxWidth(imageInfo);
                                }
                                if (targetWidth <= maxWidth) {
                                    Object ret = ImageFileReaders.readFrameByWidth(reader, targetIndex, null, targetWidth);
                                    if (ret != null) {
                                        if (ret instanceof BufferedImage) {
                                            bufferedImage = (BufferedImage) ret;
                                        } else if (ret instanceof Exception) {
                                            bufferedImage = ImageFileReaders.readBrokenImage((Exception) ret, file.getAbsolutePath(), targetIndex, null, targetWidth);
                                            if (bufferedImage == null) {
                                                return ret;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        reader.dispose();
                    }
                } catch (Exception e) {
//                    MyBoxLog.console(e);
                    return e;
                }
            }
            if (imageInfo != null && bufferedImage != null) {
                Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                imageInfo.setThumbnail(image);
                imageInfo.setIsScaled(image.getWidth() != imageInfo.getWidth());
                imageInfo.setImageType(bufferedImage.getType());
            }
            return imageInfo;
        } catch (Exception e) {
            return e;
        }
    }

    public static boolean checkInfo(List<File> files, Map<File, List<ImageInformation>> map, ImageInformation imageInfo, int width) {
        if (map == null || imageInfo == null || files == null) {
            return false;
        }
        Image image = ImageInformation.readImage(imageInfo, width, false);
        if (image != null) {
            imageInfo.setThumbnail(image);
            return true;
        } else {
            File file = imageInfo.getFile();
            if (file == null) {
                return false;
            }
            List<ImageInformation> fileInfos = null;
            if (files.contains(file)) {
                fileInfos = map.get(file);
            } else {
                files.add(file);
            }
            if (fileInfos == null) {
                fileInfos = new ArrayList<>();
                map.put(file, fileInfos);
            }
            fileInfos.add(imageInfo);
            return false;
        }
    }

    public static void readFrames2(List<ImageInformation> imageInfos, int index, int width) {
        try {
            if (imageInfos == null || imageInfos.isEmpty()) {
                return;
            }
            Map<File, List<ImageInformation>> map = new HashMap<>();
            List<File> files = new ArrayList<>();
            int pos = -1;
            if (index >= 0 && index < imageInfos.size()) {
                ImageInformation imageInfo = imageInfos.get(index);
                Image image = ImageInformation.readImage(imageInfo, width, false);
                if (image != null) {
                    imageInfo.setThumbnail(image);
                } else {
                    File file = imageInfo.getFile();
                    if (file != null) {
//                        List<ImageInformation> fileInfos = null;
//                        fileInfos.add(imageInfo);
                    }
                }
                checkInfo(files, map, imageInfo, width);
                pos = index;
            }
            for (int i = pos + 1; i < imageInfos.size(); i++) {
                ImageInformation imageInfo = imageInfos.get(i);
                checkInfo(files, map, imageInfo, width);
            }
            for (int i = 0; i < pos; i++) {
                ImageInformation imageInfo = imageInfos.get(i);
                checkInfo(files, map, imageInfo, width);
            }
//            readFrames(files, map, width);
        } catch (Exception e) {

        }
    }

    public static BufferedImage readImage(File file) {
        return readFrame(file, 0);
    }

    public static BufferedImage readFrame(File file, int index) {
        if (file == null || !file.exists()) {
            return null;
        }
        return readFrameByWidth(null, file.getAbsolutePath(), index, -1);
    }

    public static BufferedImage readFrame(String format, String filename, int index) {
        return readFrameByWidth(format, filename, index, -1);
    }

    public static BufferedImage readFrameByWidth(String format, String filename, int index, int width) {
        return readFrameByWidth(format, filename, index, null, width);
    }

    public static BufferedImage readFrameByWidth(String format, String filename, int index, Rectangle region, int width) {
        if (filename == null) {
            return null;
        }
        if (format == null) {
            format = FileNameTools.getFileSuffix(filename).toLowerCase();
        }
        BufferedImage frame = null;
        if ("ico".equals(format) || "icon".equals(format)) {
            frame = readIcon(new File(filename), index);
            if (region != null) {
                frame = CropTools.cropOutside(frame, new DoubleRectangle(region));
            }
            frame = ScaleTools.scaleImageWidthKeep(frame, width);
            return frame;
        }
        try ( ImageInputStream iis = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
            ImageReader reader = getReader(iis, format);
            if (reader == null) {
                return null;
            }
            reader.setInput(iis, true, true);
            Object ret = readFrameByWidth(reader, index, region, width);
            if (ret != null) {
                if (ret instanceof BufferedImage) {
                    frame = (BufferedImage) ret;
                } else if (ret instanceof Exception) {
                    frame = readBrokenImage((Exception) ret, filename, index, region, width);
                }
            }
            reader.dispose();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return frame;
    }

    public static Object readFrameByWidth(ImageReader reader, int index, Rectangle region, int width) {
        if (reader == null || index < 0) {
            return null;
        }
        try {
            ImageReadParam param = reader.getDefaultReadParam();
            int imageWidth = reader.getWidth(index);
            int requriedWidth, scale;
            if (region != null) {
                param.setSourceRegion(region);
                if (width <= 0) {
                    requriedWidth = (int) Math.ceil(region.getWidth());
                    scale = 1;
                } else {
                    requriedWidth = width;
                    scale = (int) Math.ceil(region.getWidth() / width);
                }
            } else {
                if (width <= 0) {
                    requriedWidth = imageWidth;
                    scale = 1;
                } else {
                    requriedWidth = width;
                    scale = (int) Math.ceil(imageWidth / width);
                }
            }
            if (scale > 1) {
                param.setSourceSubsampling(scale, scale, 0, 0);
            } else {
                scale = 1;
            }
            BufferedImage frame;
            try {
                frame = reader.read(index, param);
            } catch (Exception e) {
                return e;
            }
            frame = ScaleTools.scaleImageWidthKeep(frame, requriedWidth);
            return frame;
        } catch (Exception e) {
            return e;
        }
    }

    public static BufferedImage readFrameByHeight(String format, String filename, int index, int height) {
        return readFrameByHeight(format, filename, index, null, height);
    }

    public static BufferedImage readFrameByHeight(String format, String filename, int index, Rectangle region, int height) {
        if (filename == null) {
            return null;
        }
        if (format == null) {
            format = FileNameTools.getFileSuffix(filename).toLowerCase();
        }
        BufferedImage frame = null;
        if ("ico".equals(format) || "icon".equals(format)) {
            frame = readIcon(new File(filename), index);
            if (region != null) {
                frame = CropTools.cropOutside(frame, new DoubleRectangle(region));
            }
            frame = ScaleTools.scaleImageHeightKeep(frame, height);
            return frame;
        }
        try ( ImageInputStream iis = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
            ImageReader reader = getReader(iis, format);
            if (reader == null) {
                return null;
            }
            reader.setInput(iis, true, true);
            ImageReadParam param = reader.getDefaultReadParam();
            int imageHeight = reader.getHeight(index);
            int requriedHeight, scale;
            if (region != null) {
                param.setSourceRegion(region);
                if (height <= 0) {
                    requriedHeight = (int) Math.ceil(region.getWidth());
                    scale = 1;
                } else {
                    requriedHeight = height;
                    scale = (int) Math.ceil(region.getWidth() / height);
                }
            } else {
                if (height <= 0) {
                    requriedHeight = imageHeight;
                    scale = 1;
                } else {
                    requriedHeight = height;
                    scale = (int) Math.ceil(imageHeight / height);
                }
            }
            if (scale > 1) {
                param.setSourceSubsampling(scale, scale, 0, 0);
            } else {
                scale = 1;
            }
            try {
                frame = reader.read(index, param);
            } catch (Exception e) {
                frame = readBrokenImage(e, filename, index, region, scale, scale);
            }
            if (frame != null) {
                frame = ScaleTools.scaleImageHeightKeep(frame, requriedHeight);
            }
            reader.dispose();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return frame;
    }

    public static BufferedImage readImageByWidth(String format, String filename, int width) {
        return readFrameByWidth(format, filename, 0, width);
    }

    public static BufferedImage readImageByWidth(String filename, int width) {
        return readFrameByWidth(null, filename, 0, width);
    }

    public static BufferedImage readFrameByScale(String format, String filename, int index, int scale) {
        return readFrameByScale(format, filename, index, scale, scale);
    }

    public static BufferedImage readFrameByScale(String format, String filename, int index, int xscale, int yscale) {
        return readFrame(format, filename, index, null, xscale, yscale);
    }

    public static BufferedImage readFrameByScale(String format, String filename, int index, Rectangle rectangle, int scale) {
        return readFrame(format, filename, index, rectangle, scale, scale);
    }

    public static BufferedImage readFrame(String format, String filename, int index, Rectangle region, int xscale, int yscale) {
        if (filename == null) {
            return null;
        }
        if (format == null) {
            format = FileNameTools.getFileSuffix(filename).toLowerCase();
        }
        int realXScale = xscale > 0 ? xscale : 1;
        int realYScale = yscale > 0 ? yscale : 1;
        BufferedImage frame = null;
        if ("ico".equals(format) || "icon".equals(format)) {
            frame = readIcon(new File(filename), index);
            if (region == null) {
                frame = ScaleTools.scaleImageByScale(frame, xscale, yscale);
            } else {
                frame = CropTools.sample(frame, new DoubleRectangle(region), xscale, yscale);
            }
            return frame;
        }
        try ( ImageInputStream iis = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
            ImageReader reader = getReader(iis, format);
            if (reader == null) {
                return null;
            }
            reader.setInput(iis, true, true);
            ImageReadParam param = reader.getDefaultReadParam();

            try {
                param.setSourceSubsampling(realXScale, realYScale, 0, 0);
                if (region != null) {
                    param.setSourceRegion(region);
                }
                frame = reader.read(index, param);
            } catch (Exception e) {
                frame = readBrokenImage(e, filename, index, region, realXScale, realYScale);
            }
            reader.dispose();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return frame;
    }

    public static BufferedImage readFrame(String format, String filename, int x1, int y1, int x2, int y2) {
        if (x1 >= x2 || y1 >= y2 || x1 < 0 || x2 < 0 || y1 < 0 || y2 < 0) {
            return null;
        }
//        MyBoxLog.console(x1 + " " + y1 + " " + x2 + " " + y2);
        return readFrame(format, filename, 0, new Rectangle(x1, y1, x2 - x1 + 1, y2 - y1 + 1), 1, 1);
    }

    public static BufferedImage readFrame(String format, String filename,
            int index, int x1, int y1, int x2, int y2, int xScale, int yScale) {
        if (x1 >= x2 || y1 >= y2 || x1 < 0 || x2 < 0 || y1 < 0 || y2 < 0) {
            return null;
        }
        return readFrame(format, filename, index, new Rectangle(x1, y1, x2 - x1 + 1, y2 - y1 + 1), xScale, yScale);
    }

    public static BufferedImage readIcon(File srcFile) {
        return readIcon(srcFile, 0);
    }

    public static BufferedImage readIcon(File srcFile, int index) {
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
//            MyBoxLog.error(e.toString());
            return null;
        }
    }

    /*
        Broken image
     */
    public static BufferedImage readBrokenImage(Exception e, String filename, int index, Rectangle region, int width) {
        BufferedImage image = null;
        try {
            if (e == null || filename == null) {
                return null;
            }
            String format = FileNameTools.getFileSuffix(filename).toLowerCase();
            switch (format) {
                case "gif":
                    // Read Gif with JDK api normally. When broken, use DhyanB's API.
                    // if (e.toString().contains("java.lang.ArrayIndexOutOfBoundsException: 4096")) {
                    if (e.toString().contains("java.lang.ArrayIndexOutOfBoundsException")) {
                        image = ImageGifFile.readBrokenGifFile(filename, index, region, width);
                    }
                    break;
                case "jpg":
                case "jpeg":
                    if (e.toString().contains("Unsupported Image Type")) {
                        image = ImageJpgFile.readBrokenJpgFile(filename, index, region, width);
                    }
                    break;
                default:
//                MyBoxLog.error(e.toString());
            }
        } catch (Exception ex) {
            MyBoxLog.error(ex.toString());
        }
        return image;
    }

    public static BufferedImage readBrokenImage(Exception e, String filename, int index, Rectangle region, int xscale, int yscale) {
        BufferedImage image = null;
        try {
            if (e == null || filename == null) {
                return null;
            }
            String format = FileNameTools.getFileSuffix(filename).toLowerCase();
            switch (format) {
                case "gif":
                    // Read Gif with JDK api normally. When broken, use DhyanB's API.
                    // if (e.toString().contains("java.lang.ArrayIndexOutOfBoundsException: 4096")) {
                    if (e.toString().contains("java.lang.ArrayIndexOutOfBoundsException")) {
                        image = ImageGifFile.readBrokenGifFile(filename, index, region, xscale, yscale);
                    }
                    break;
                case "jpg":
                case "jpeg":
                    if (e.toString().contains("Unsupported Image Type")) {
                        image = ImageJpgFile.readBrokenJpgFile(filename, index, region, xscale, yscale);
                    }
                    break;
                default:
//                MyBoxLog.error(e.toString());
            }
        } catch (Exception ex) {
            MyBoxLog.error(ex.toString());
        }
        return image;
    }

    /*
        Meta data
     */
    public static ImageFileInformation readImageFileMetaData(String fileName) {
        return readImageFileMetaData(new File(fileName));
    }

    public static ImageFileInformation readImageFileMetaData(File file) {
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        ImageFileInformation fileInfo = new ImageFileInformation(file);
        String format = fileInfo.getImageFormat();
        if ("ico".equals(format) || "icon".equals(format)) {
            readImageFileMetaData(null, fileInfo);
        } else {
            try ( ImageInputStream iis = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                ImageReader reader = getReader(iis, format);
                if (reader == null) {
                    return null;
                }
                reader.setInput(iis, false, false);
                readImageFileMetaData(reader, fileInfo);
                reader.dispose();
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
            }
        }
        return fileInfo;
    }

    public static void readImageFileMetaData(ImageReader reader, ImageFileInformation fileInfo) {
        try {
            if (fileInfo == null) {
                return;
            }
            String targetFormat = fileInfo.getImageFormat();
            File file = fileInfo.getFile();
            if (reader == null) {
                fileInfo.setNumberOfImages(1);
                ImageInformation imageInfo = ImageInformation.create(targetFormat, file);
                imageInfo.setImageFileInformation(fileInfo);
                imageInfo.setImageFormat(targetFormat);
                imageInfo.setFileName(fileInfo.getFileName());
                imageInfo.setCreateTime(fileInfo.getCreateTime());
                imageInfo.setModifyTime(fileInfo.getModifyTime());
                imageInfo.setFileSize(fileInfo.getFileSize());
                imageInfo.setIndex(0);
                ImageInformation.countMaxWidth(imageInfo);
                List<ImageInformation> imagesInfo = new ArrayList<>();
                imagesInfo.add(imageInfo);
                fileInfo.setImagesInformation(imagesInfo);
                fileInfo.setImageInformation(imageInfo);
                return;
            }
            String format = reader.getFormatName().toLowerCase();
            fileInfo.setImageFormat(format);
            int num = reader.getNumImages(true);
            fileInfo.setNumberOfImages(num);
            List<ImageInformation> imagesInfo = new ArrayList<>();
            ImageInformation imageInfo;
            for (int i = 0; i < num; ++i) {
                imageInfo = ImageInformation.create(format, file);
                imageInfo.setImageFileInformation(fileInfo);
                imageInfo.setImageFormat(format);
                imageInfo.setFileName(fileInfo.getFileName());
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
                        typesValue.add(types.next());
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
                    MyBoxLog.console(e.toString());
                }
                try {
                    imageInfo.setHasThumbnails(reader.hasThumbnails(i));
                    imageInfo.setNumberOfThumbnails(reader.getNumThumbnails(i));
                } catch (Exception e) {
                    MyBoxLog.console(e.toString());
                }
                try {
                    readImageMetaData(format, imageInfo, reader.getImageMetadata(i));
                } catch (Exception e) {
                    MyBoxLog.console(e.toString());
                }
                ImageInformation.countMaxWidth(imageInfo);
                imagesInfo.add(imageInfo);
            }
            fileInfo.setImagesInformation(imagesInfo);
            if (!imagesInfo.isEmpty()) {
                fileInfo.setImageInformation(imagesInfo.get(0));
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void readImageMetaData(String format, ImageInformation imageInfo, IIOMetadata iioMetaData) {
        try {
            if (imageInfo == null || iioMetaData == null) {
                return;
            }
            StringBuilder metaDataXml = new StringBuilder();
            String[] formatNames = iioMetaData.getMetadataFormatNames();
            Map<String, Map<String, List<Map<String, Object>>>> metaData = new HashMap<>();
            for (String formatName : formatNames) {
                Map<String, List<Map<String, Object>>> formatMetaData = new HashMap<>();
                IIOMetadataNode tree = (IIOMetadataNode) iioMetaData.getAsTree(formatName);
                readImageMetaData(formatMetaData, metaDataXml, tree, 2);
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

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void readImageMetaData(Map<String, List<Map<String, Object>>> formatMetaData,
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
                    Node attr = map.item(i);
                    String name = attr.getNodeName();
                    String value = attr.getNodeValue();
                    if (!isTiff) {
                        nodeAttrs.put(name, value);
                    }
                    metaDataXml.append(" ").append(name).append("=\"").append(value).append("\"");
                    if (isTiff && "ICC Profile".equals(value)) {
                        metaDataXml.append(" value=\"skip...\"/>").append(lineSeparator);
                        return;
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
                return;
            }
            metaDataXml.append(">").append(lineSeparator);
            while (child != null) {
                readImageMetaData(formatMetaData, metaDataXml, child, level + 1);
                child = (IIOMetadataNode) child.getNextSibling();
            }
            for (int i = 0; i < level; ++i) {
                metaDataXml.append("    ");
            }
            metaDataXml.append("</").append(node.getNodeName()).append(">").append(lineSeparator);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
                    imageInfo.setColorChannels(Integer.valueOf((String) NumChannels.get("value")));
//                    MyBoxLog.debug(" NumChannels:" + NumChannels.get("value"));
                }
            }
            if (javax_imageio.containsKey("Gamma")) {
                Map<String, Object> Gamma = javax_imageio.get("Gamma").get(0);
                if (Gamma.containsKey("value")) {
                    imageInfo.setGamma(Float.valueOf((String) Gamma.get("value")));
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
//                    int index = Integer.valueOf(PaletteEntry.get("index"));
//                    int red = Integer.valueOf(PaletteEntry.get("red"));
//                    int green = Integer.valueOf(PaletteEntry.get("green"));
//                    int blue = Integer.valueOf(PaletteEntry.get("blue"));
//                    int alpha = 255;
//                    if (PaletteEntry.containsKey("alpha")) {
//                        alpha = Integer.valueOf(PaletteEntry.get("alpha"));
//                    }
//                    Palette.add(new ImageColor(index, red, green, blue, alpha));
//                }
//                imageInfo.setPalette(Palette);
            }
            if (javax_imageio.containsKey("BackgroundIndex")) {
                Map<String, Object> BackgroundIndex = javax_imageio.get("BackgroundIndex").get(0);
                if (BackgroundIndex.containsKey("value")) {
                    imageInfo.setBackgroundIndex(Integer.valueOf((String) BackgroundIndex.get("value")));
                }
            }
            if (javax_imageio.containsKey("BackgroundColor")) {
                Map<String, Object> BackgroundColor = javax_imageio.get("BackgroundColor").get(0);
                int red = Integer.valueOf((String) BackgroundColor.get("red"));
                int green = Integer.valueOf((String) BackgroundColor.get("green"));
                int blue = Integer.valueOf((String) BackgroundColor.get("blue"));
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
                    imageInfo.setNumProgressiveScans(Integer.valueOf((String) NumProgressiveScans.get("value")));
                }
            }
            if (javax_imageio.containsKey("BitRate")) {
                Map<String, Object> BitRate = javax_imageio.get("BitRate").get(0);
                if (BitRate.containsKey("value")) {
                    imageInfo.setBitRate(Float.valueOf((String) BitRate.get("value")));
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
                    imageInfo.setPixelAspectRatio(Float.valueOf((String) PixelAspectRatio.get("value")));
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
                    float v = Float.valueOf((String) HorizontalPixelSize.get("value"));
                    imageInfo.setHorizontalPixelSize(v);
                    imageInfo.setXDpi(pixelSizeMm2dpi(v));
                }
            }
            if (javax_imageio.containsKey("VerticalPixelSize")) { // The height of a pixel, in millimeters
                Map<String, Object> VerticalPixelSize = javax_imageio.get("VerticalPixelSize").get(0);
                if (VerticalPixelSize.containsKey("value")) {
                    float v = Float.valueOf((String) VerticalPixelSize.get("value"));
                    imageInfo.setVerticalPixelSize(v);
                    imageInfo.setYDpi(pixelSizeMm2dpi(v));
                }
            }
            if (javax_imageio.containsKey("HorizontalPhysicalPixelSpacing")) {
                Map<String, Object> HorizontalPhysicalPixelSpacing = javax_imageio.get("HorizontalPhysicalPixelSpacing").get(0);
                if (HorizontalPhysicalPixelSpacing.containsKey("value")) {
                    float v = Float.valueOf((String) HorizontalPhysicalPixelSpacing.get("value"));
                    imageInfo.setHorizontalPhysicalPixelSpacing(v);
                }
            }
            if (javax_imageio.containsKey("VerticalPhysicalPixelSpacing")) {
                Map<String, Object> VerticalPhysicalPixelSpacing = javax_imageio.get("VerticalPhysicalPixelSpacing").get(0);
                if (VerticalPhysicalPixelSpacing.containsKey("value")) {
                    float v = Float.valueOf((String) VerticalPhysicalPixelSpacing.get("value"));
                    imageInfo.setVerticalPhysicalPixelSpacing(v);
                }
            }
            if (javax_imageio.containsKey("HorizontalPosition")) {
                Map<String, Object> HorizontalPosition = javax_imageio.get("HorizontalPosition").get(0);
                if (HorizontalPosition.containsKey("value")) {
                    float v = Float.valueOf((String) HorizontalPosition.get("value"));
                    imageInfo.setHorizontalPosition(v);
                }
            }
            if (javax_imageio.containsKey("VerticalPosition")) {
                Map<String, Object> VerticalPosition = javax_imageio.get("VerticalPosition").get(0);
                if (VerticalPosition.containsKey("value")) {
                    float v = Float.valueOf((String) VerticalPosition.get("value"));
                    imageInfo.setVerticalPosition(v);
                }
            }
            if (javax_imageio.containsKey("HorizontalPixelOffset")) {
                Map<String, Object> HorizontalPixelOffset = javax_imageio.get("HorizontalPixelOffset").get(0);
                if (HorizontalPixelOffset.containsKey("value")) {
                    float v = Float.valueOf((String) HorizontalPixelOffset.get("value"));
                    imageInfo.setHorizontalPixelOffset(v);
                }
            }
            if (javax_imageio.containsKey("VerticalPixelOffset")) {
                Map<String, Object> VerticalPixelOffset = javax_imageio.get("VerticalPixelOffset").get(0);
                if (VerticalPixelOffset.containsKey("value")) {
                    float v = Float.valueOf((String) VerticalPixelOffset.get("value"));
                    imageInfo.setVerticalPixelOffset(v);
                }
            }
            if (javax_imageio.containsKey("HorizontalScreenSize")) {
                Map<String, Object> HorizontalScreenSize = javax_imageio.get("HorizontalScreenSize").get(0);
                if (HorizontalScreenSize.containsKey("value")) {
                    float v = Float.valueOf((String) HorizontalScreenSize.get("value"));
                    imageInfo.setHorizontalScreenSize(v);
                }
            }
            if (javax_imageio.containsKey("VerticalScreenSize")) {
                Map<String, Object> VerticalScreenSize = javax_imageio.get("VerticalScreenSize").get(0);
                if (VerticalScreenSize.containsKey("value")) {
                    float v = Float.valueOf((String) VerticalScreenSize.get("value"));
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
                    imageInfo.setTransparentIndex(Integer.valueOf((String) TransparentIndex.get("value")));
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

    public static IIOMetadata getIIOMetadata(String format, File file) {
        switch (format) {
            case "png":
                return getIIOMetadata(file);
            case "jpg":
            case "jpeg":
                return getIIOMetadata(file);
            case "bmp":
                IIOMetadata bm = ImageBmpFile.getBmpIIOMetadata(file);
                if (bm != null) {
                    return bm;
                }
                return getIIOMetadata(file);
            case "gif":
                return getIIOMetadata(file);
            //                return ImageGifTools.getGifMetadata(file);
            case "tif":
            case "tiff":
                IIOMetadata tm = ImageTiffFile.getTiffIIOMetadata(file);
                if (tm != null) {
                    return tm;
                }
                return getIIOMetadata(file);
            case "pcx":
                IIOMetadata pm = ImagePcxFile.getPcxMetadata(file);
                if (pm != null) {
                    return pm;
                }
                return getIIOMetadata(file);
            case "pnm":
                IIOMetadata pnm = ImagePnmFile.getPnmMetadata(file);
                if (pnm != null) {
                    return pnm;
                }
                return getIIOMetadata(file);
            case "wbmp":
                IIOMetadata wm = ImageWbmpFile.getWbmpMetadata(file);
                if (wm != null) {
                    return wm;
                }
                return getIIOMetadata(file);
            default:
                return getIIOMetadata(file);
        }
    }

    public static IIOMetadata getIIOMetadata(File file) {
        IIOMetadata iioMetaData = null;
        try ( ImageInputStream iis = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            ImageReader reader = getReader(iis, FileNameTools.getFileSuffix(file).toLowerCase());
            if (reader == null) {
                return null;
            }
            reader.setInput(iis, true, false);
            iioMetaData = reader.getImageMetadata(0);
            reader.dispose();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return iioMetaData;
    }

}
