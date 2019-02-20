package mara.mybox.image.file;

import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
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
import javax.imageio.stream.ImageInputStream;
import mara.mybox.fxml.image.ImageTools;
import mara.mybox.data.ImageFileInformation;
import mara.mybox.image.ImageColor;

import static mara.mybox.value.AppVaribles.logger;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import static mara.mybox.image.ImageValue.pixelSizeMm2dpi;
import static mara.mybox.image.file.ImageGifFile.readBrokenGifFile;
import static mara.mybox.image.file.ImageGifFile.readBrokenGifFileWithWidth;
import mara.mybox.value.AppVaribles;
import mara.mybox.data.ImageInformation;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:27
 *
 * @Description
 * @License Apache License Version 2.0
 */
// https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/standard_metadata.html
public class ImageFileReaders {

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
        try {
            try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
                if (readers.hasNext()) {
                    ImageReader reader = readers.next();
                    reader.setInput(iis, false);
                    IIOMetadata iioMetaData = reader.getImageMetadata(0);
                    reader.dispose();
                    return iioMetaData;
                }
            }
            return null;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage readFileByHeight(String format, String filename, int height) {
        BufferedImage bufferedImage = null;
        try {

            ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName(format).next();
            try (ImageInputStream in = ImageIO.createImageInputStream(new FileInputStream(filename))) {
                reader.setInput(in, false);
                int scale;
                if (reader.getHeight(0) <= height) {
                    scale = 1;
                } else {
                    scale = reader.getHeight(0) / height + 1;
                    if (scale < 2) {
                        scale = 2;
                    }
                }
//                logger.debug(height + " " + scale);
                ImageReadParam param = reader.getDefaultReadParam();
                param.setSourceSubsampling(scale, scale, 0, 0);
                bufferedImage = reader.read(0, param);
//                logger.debug(bufferedImage.getWidth() + " " + bufferedImage.getHeight());
                reader.dispose();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return bufferedImage;
    }

    public static BufferedImage readFileByWidth(String format, String filename, int width) {
        BufferedImage bufferedImage = null;
        try {

            ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName(format).next();
            try (ImageInputStream in = ImageIO.createImageInputStream(new FileInputStream(filename))) {
                reader.setInput(in, false);
                int scale;
                if (reader.getWidth(0) <= width) {
                    scale = 1;
                } else {
                    scale = reader.getWidth(0) / width + 1;
                    if (scale < 2) {
                        scale = 2;
                    }
                }
                ImageReadParam param = reader.getDefaultReadParam();
                param.setSourceSubsampling(scale, scale, 0, 0);
                bufferedImage = reader.read(0, param);
//                logger.debug(bufferedImage.getWidth() + " " + bufferedImage.getHeight());
                reader.dispose();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return bufferedImage;
    }

    public static BufferedImage readFileByScale(String format, String filename, int scale) {
        BufferedImage bufferedImage = null;
        try {

            ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName(format).next();
            try (ImageInputStream in = ImageIO.createImageInputStream(new FileInputStream(filename))) {
                reader.setInput(in, false);
                ImageReadParam param = reader.getDefaultReadParam();
                param.setSourceSubsampling(scale, scale, 0, 0);
                bufferedImage = reader.read(0, param);
//                logger.debug(bufferedImage.getWidth() + " " + bufferedImage.getHeight());
                reader.dispose();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return bufferedImage;
    }

    public static BufferedImage readFileBySample(String format, String filename,
            int x1, int y1, int x2, int y2,
            int sampleWidth, int sampleHeight) {
        BufferedImage bufferedImage = null;
        try {
            if (x1 >= x2 || y1 >= y2
                    || x1 < 0 || x2 < 0 || y1 < 0 || y2 < 0
                    || sampleWidth <= 0 || sampleHeight <= 0) {
                return null;
            }
            ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName(format).next();
            try (ImageInputStream in = ImageIO.createImageInputStream(new FileInputStream(filename))) {
                reader.setInput(in, false);
                ImageReadParam param = reader.getDefaultReadParam();
                param.setSourceRegion(new Rectangle(x1, y1, x2 - x1 + 1, y2 - y1 + 1));
                param.setSourceSubsampling(sampleWidth, sampleHeight, 0, 0);
                bufferedImage = reader.read(0, param);
//                logger.debug(bufferedImage.getWidth() + " " + bufferedImage.getHeight());
                reader.dispose();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return bufferedImage;
    }

    public static List<BufferedImage> readFrames(String format, String filename) {
        try {
            List<BufferedImage> images = new ArrayList<>();
            ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName(format).next();
            boolean broken = false;
            try (ImageInputStream in = ImageIO.createImageInputStream(new FileInputStream(filename))) {
                reader.setInput(in, false);
                int count = 0;
                while (true) {
                    try {
                        BufferedImage m = reader.read(count);
                        if (m != null) {
                            images.add(m);
                            count++;
                        } else {
                            break;
                        }
                    } catch (Exception e) {   // Read Gif with JDK api normally. When broken, use DhyanB's API.
//                        logger.error(e.toString());
                        if (e.toString().contains("java.lang.ArrayIndexOutOfBoundsException: 4096")
                                && format.toLowerCase().equals("gif")) {
                            broken = true;
                        }
                        break;
                    }
                }
                reader.dispose();
                if (broken) {
                    return readBrokenGifFile(filename);
                } else {
                    return images;
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static List<BufferedImage> readFramesWithWidth(String format, String filename, int width) {
        try {
            List<BufferedImage> images = new ArrayList<>();
            ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName(format).next();
            boolean broken = false;
            try (ImageInputStream in = ImageIO.createImageInputStream(new FileInputStream(filename))) {
                reader.setInput(in, false);
                int count = 0, scale;
                ImageReadParam param = reader.getDefaultReadParam();
                while (true) {
                    try {
                        if (reader.getWidth(count) <= width) {
                            scale = 1;
                        } else {
                            scale = reader.getWidth(count) / width + 1;
                            if (scale < 2) {
                                scale = 2;
                            }
                        }
                        param.setSourceSubsampling(scale, scale, 0, 0);
                        BufferedImage m = reader.read(count, param);
                        if (m != null) {
                            images.add(m);
                            count++;
                        } else {
                            break;
                        }
                    } catch (Exception e) {   // Read Gif with JDK api normally. When broken, use DhyanB's API.
//                        logger.error(e.toString());
                        if (e.toString().contains("java.lang.ArrayIndexOutOfBoundsException: 4096")
                                && format.toLowerCase().equals("gif")) {
                            broken = true;
                        }
                        break;
                    }
                }
                reader.dispose();
                if (broken) {
                    return readBrokenGifFileWithWidth(filename, width);
                } else {
                    return images;
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static boolean needSampled(ImageInformation imageInfo, int total, Map<String, Long> sizes) {
        if (total < 1) {
            return false;
        }
        long availableMem, pixelsSize, requiredMem, totalRequiredMem;
        pixelsSize = imageInfo.getHeight() * imageInfo.getWidth();
        if (imageInfo.getColorChannels() > 0) {
            pixelsSize = pixelsSize * imageInfo.getColorChannels();
        } else {
            pixelsSize = pixelsSize * 4;
        }
        pixelsSize = pixelsSize / (1024 * 1024);
        Runtime r = Runtime.getRuntime();
        long used = (r.totalMemory() - r.freeMemory()) / (1024 * 1024);
        availableMem = r.maxMemory() / (1024 * 1024) - used;
        requiredMem = pixelsSize * 5;
        totalRequiredMem = requiredMem * total + 200;

        if (sizes != null) {
            sizes.put("availableMem", availableMem);
            sizes.put("pixelsSize", pixelsSize);
            sizes.put("requiredMem", requiredMem);
            sizes.put("totalRequiredMem", totalRequiredMem);

            long maxSize = availableMem * 1014 * 1024 / (6 * total);
            double ratio = imageInfo.getHeight() * 1.0 / imageInfo.getWidth();
            long sampledWidth = (long) Math.sqrt(maxSize / (ratio * imageInfo.getColorChannels()));
            int max = AppVaribles.getUserConfigInt("MaxImageSampleWidth", 4096);
            if (sampledWidth > max) {
                sampledWidth = max;
            }
            sizes.put("sampledWidth", sampledWidth);
        }
//        logger.debug(availableMem + "  " + pixelsSize + "  " + requiredMem);
        return totalRequiredMem > availableMem;
    }

    public static BufferedImage getBufferedImage(ImageInformation imageInfo) {
        try {
            if (imageInfo == null) {
                return null;
            }
            BufferedImage bufferedImage = null;
            if (!imageInfo.isIsSampled()) {
                if (imageInfo.getBufferedImage() != null) {
                    bufferedImage = imageInfo.getBufferedImage();
                } else if (imageInfo.getImage() != null) {
                    bufferedImage = ImageTools.getBufferedImage(imageInfo.getImage());
                }
            }
            if (bufferedImage == null) {
                String fname = imageInfo.getFilename();
                if (fname != null) {
                    if (imageInfo.getIndex() <= 0) {
                        bufferedImage = ImageIO.read(new File(fname));
                    } else {
                        bufferedImage = ImageFileReaders.readFrame(imageInfo.getImageFormat(), fname, imageInfo.getIndex());
                    }
                }
            }
            return bufferedImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static List<BufferedImage> readFrames(String format, String filename,
            List<ImageInformation> imagesInfo) {
        try {
            List<BufferedImage> images = new ArrayList<>();
            if (imagesInfo == null || imagesInfo.isEmpty()) {
                return images;
            }
            ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName(format).next();
            int total = imagesInfo.size();
            Map<String, Long> sizes = new HashMap<>();
            try (ImageInputStream in = ImageIO.createImageInputStream(new FileInputStream(filename))) {
                reader.setInput(in, false);
                int scale;
                ImageReadParam param = reader.getDefaultReadParam();
                for (int i = 0; i < imagesInfo.size(); i++) {
                    ImageInformation info = imagesInfo.get(i);
                    scale = 1;
                    boolean needSampled = needSampled(info, total, sizes);
                    if (needSampled) {
                        int sampledWidth = sizes.get("sampledWidth").intValue();
                        if (reader.getWidth(i) <= sampledWidth) {
                            scale = 1;
                        } else {
                            scale = reader.getWidth(i) / sampledWidth + 1;
                            if (scale < 2) {
                                scale = 2;
                            }
                        }
                    }
//                    logger.debug(needSampled + " " + scale);
                    param.setSourceSubsampling(scale, scale, 0, 0);
                    try {
                        BufferedImage m = reader.read(i, param);
                        if (scale == 1) {
                            info.setIsSampled(false);
                            info.setBufferedImage(m);
                            info.setSizeString(info.getWidth() + "x" + info.getHeight());
                        } else {
                            info.setIsSampled(true);
//                            info.setSampledBufferedImage(m);
                            info.setSizeString(info.getWidth() + "x" + info.getHeight() + " *");
                        }
                        info.setIndex(i);
                        images.add(m);
                    } catch (Exception e) {   // Read Gif with JDK api normally. When broken, use DhyanB's API.
                        logger.error(format + "  " + e.toString());
                        if (e.toString().contains("java.lang.ArrayIndexOutOfBoundsException: 4096")
                                && format.toLowerCase().equals("gif")) {
                            images = readBrokenGifFile(filename, imagesInfo);
                            break;
                        }
                    }
                }
                reader.dispose();
            }
            return images;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage readImage(String format, String filename,
            ImageInformation imageInfo, Map<String, Long> sizes) {
        try {
            BufferedImage bufferedImage = null;
            ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName(format).next();
            try (ImageInputStream in = ImageIO.createImageInputStream(new FileInputStream(filename))) {
                reader.setInput(in, false);
                int scale = 1;
                ImageReadParam param = reader.getDefaultReadParam();
                if (imageInfo != null) {
                    boolean needSampled = needSampled(imageInfo, 1, sizes);
                    if (needSampled) {
                        int sampledWidth = sizes.get("sampledWidth").intValue();
                        if (reader.getWidth(0) <= sampledWidth) {
                            scale = 1;
                        } else {
                            scale = reader.getWidth(0) / sampledWidth + 1;
                            if (scale < 2) {
                                scale = 2;
                            }
                        }
                    }
                }
                param.setSourceSubsampling(scale, scale, 0, 0);
                try {
                    bufferedImage = reader.read(0, param);
                    if (imageInfo != null) {
                        if (scale == 1) {
                            imageInfo.setIsSampled(false);
                            imageInfo.setBufferedImage(bufferedImage);
                        } else {
                            imageInfo.setIsSampled(true);
//                            imageInfo.setSampledBufferedImage(m);
                        }
                    }

                } catch (Exception e) {   // Read Gif with JDK api normally. When broken, use DhyanB's API.
//                        logger.error(e.toString());
                    if (e.toString().contains("java.lang.ArrayIndexOutOfBoundsException: 4096")
                            && format.toLowerCase().equals("gif")) {
                        return readBrokenGifFile(filename, imageInfo, sizes);
                    }
                }

                reader.dispose();
            }
            return bufferedImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage readFrame(String format, String filename, int index) {
        BufferedImage bufferedImage = null;
        try {

            ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName(format).next();
            try (ImageInputStream in = ImageIO.createImageInputStream(new FileInputStream(filename))) {
                reader.setInput(in, false);
                try {
                    bufferedImage = reader.read(index);
                } catch (Exception e) {   // Read Gif with JDK api normally. When broken, use DhyanB's API.
                    logger.error(e.toString());
                    if (format.toLowerCase().equals("gif")) {
                        bufferedImage = ImageGifFile.readBrokenGifFile(filename, index);
                    }
                }
                reader.dispose();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return bufferedImage;
    }

    public static BufferedImage readRectangle(String format, String filename,
            int x1, int y1, int x2, int y2) {
        try {
            if (x1 >= x2 || y1 >= y2
                    || x1 < 0 || x2 < 0 || y1 < 0 || y2 < 0) {
                return null;
            }
            return readRectangle(format, filename, new Rectangle(x1, y1, x2 - x1 + 1, y2 - y1 + 1));
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage readRectangle(String format, String filename,
            Rectangle bounds) {
        try {
            if (bounds == null || filename == null || format == null) {
                return null;
            }
            BufferedImage bufferedImage = null;
            ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName(format).next();
            try (ImageInputStream in = ImageIO.createImageInputStream(new FileInputStream(filename))) {
                reader.setInput(in, false);
                ImageReadParam param = reader.getDefaultReadParam();
                param.setSourceRegion(bounds);
                bufferedImage = reader.read(0, param);
                reader.dispose();
            }
            return bufferedImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageFileInformation readImageFileMetaData(String fileName) {
        File file = new File(fileName);
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        ImageFileInformation fileInfo = new ImageFileInformation(file);

        try {
            try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
                if (readers.hasNext()) {
                    ImageReader reader = readers.next();
                    reader.setInput(iis, false);
                    int num = reader.getNumImages(true);
                    fileInfo.setNumberOfImages(num);
                    String format = reader.getFormatName();
                    if (!format.equals(fileInfo.getImageFormat())) {
                        fileInfo.setImageFormat(format);
                    }

                    List<ImageInformation> imagesInfo = new ArrayList<>();
                    for (int i = 0; i < num; i++) {
                        ImageInformation imageInfo = new ImageInformation(file);
                        imageInfo.setImageFileInformation(fileInfo);
                        imageInfo.setImageFormat(format);
                        imageInfo.setWidth(reader.getWidth(i));
                        imageInfo.setHeight(reader.getHeight(i));
                        imageInfo.setIsTiled(reader.isImageTiled(i));
                        Iterator<ImageTypeSpecifier> types = reader.getImageTypes(i);
                        List<ImageTypeSpecifier> typesValue = new ArrayList<>();
                        if (types != null) {
                            while (types.hasNext()) {
                                typesValue.add(types.next());
                            }
                            if (!typesValue.isEmpty()) {
                                try {
                                    ColorModel cm = typesValue.get(0).getColorModel();
                                    ColorSpace cs = cm.getColorSpace();
                                    imageInfo.setColorSpace(ImageColor.getColorSpaceName(cs.getType()));
                                    imageInfo.setColorChannels(cm.getNumComponents());
                                    imageInfo.setBitDepth(cm.getPixelSize() + "");
                                } catch (Exception e) {
                                    logger.error(e.toString());
                                }
                            }
                        }
                        imageInfo.setImageTypes(typesValue);
                        try {
                            imageInfo.setAspectRatio(reader.getAspectRatio(i));
                            imageInfo.setRawImageType(reader.getRawImageType(i));
                        } catch (Exception e) {
                            logger.error(e.toString());
                        }
                        try {
                            imageInfo.setHasThumbnails(reader.hasThumbnails(i));
                        } catch (Exception e) {
                            logger.error(e.toString());
                        }
                        try {
                            readImageMetaData(format, imageInfo, reader.getImageMetadata(i));
                        } catch (Exception e) {
                            logger.error(e.toString());
                        }

                        imagesInfo.add(imageInfo);
                    }
                    fileInfo.setImagesInformation(imagesInfo);
                    if (!imagesInfo.isEmpty()) {
                        fileInfo.setImageInformation(imagesInfo.get(0));
                    }
                    reader.dispose();
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());

        }
        return fileInfo;
    }

    public static void readImageMetaData(String format,
            ImageInformation imageInfo, IIOMetadata iioMetaData) {
        try {
            if (imageInfo == null || iioMetaData == null) {
                return;
            }
            Map<String, Map<String, Map<String, String>>> metaData = new HashMap();
            StringBuilder metaDataXml = new StringBuilder();
            readImageMetaData(iioMetaData, metaData, metaDataXml);
            imageInfo.setMetaData(metaDataXml.toString());

            explainCommonMetaData(metaData, imageInfo);
            switch (format.toLowerCase()) {
                case "png":
                    ImagePngFile.explainPngMetaData(metaData, imageInfo);
                    break;
                case "jpg":
                case "jpeg":
                    ImageJpegFile.explainJpegMetaData(metaData, imageInfo);
                    break;
                case "bmp":
                    ImageBmpFile.explainBmpMetaData(metaData, imageInfo);
                    break;
                case "tif":
                case "tiff":
                    ImageTiffFile.explainTiffMetaData(imageInfo.getMetaData(), imageInfo);
                    break;
                default:

            }
//            logger.debug(metaData);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void readImageMetaData(IIOMetadata iioMetaData,
            Map<String, Map<String, Map<String, String>>> meteData, StringBuilder metaDataXml) {
        try {
            if (iioMetaData == null) {
                return;
            }
            String[] formatNames = iioMetaData.getMetadataFormatNames();
            int length = formatNames.length;
            for (int i = 0; i < length; i++) {
                Map<String, Map<String, String>> formatMetaData = new HashMap();
                readImageMetaData(formatMetaData, metaDataXml, iioMetaData.getAsTree(formatNames[i]), 0);
                meteData.put(formatNames[i], formatMetaData);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void readImageMetaData(Map<String, Map<String, String>> formatMetaData,
            StringBuilder metaDataXml, Node node, int level) {
        String lineSeparator = System.getProperty("line.separator");
        for (int i = 0; i < level; i++) {
            metaDataXml.append("    ");
        }
        metaDataXml.append("<").append(node.getNodeName());

        NamedNodeMap map = node.getAttributes();
        if (map != null && map.getLength() > 0) {
            Map<String, String> nodeAttrs = new HashMap();
            int length = map.getLength();
            for (int i = 0; i < length; i++) {
                Node attr = map.item(i);
                nodeAttrs.put(attr.getNodeName(), attr.getNodeValue());
                metaDataXml.append(" ").append(attr.getNodeName()).append("=\"")
                        .append(attr.getNodeValue()).append("\"");
            }
            formatMetaData.put(node.getNodeName(), nodeAttrs);
        }

        Node child = node.getFirstChild();
        if (child == null) {
            metaDataXml.append("/>").append(lineSeparator);
            return;
        }
        metaDataXml.append(">").append(lineSeparator);
        while (child != null) {
            readImageMetaData(formatMetaData, metaDataXml, child, level + 1);
            child = child.getNextSibling();
        }
        for (int i = 0; i < level; i++) {
            metaDataXml.append("    ");
        }
        metaDataXml.append("</").append(node.getNodeName()).append(">").append(lineSeparator);
    }

    // https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/standard_metadata.html
    public static void explainCommonMetaData(Map<String, Map<String, Map<String, String>>> metaData,
            ImageInformation imageInfo) {
        try {
            if (!metaData.containsKey("javax_imageio_1.0")) {
                return;
            }
            Map<String, Map<String, String>> javax_imageio = metaData.get("javax_imageio_1.0");
//            logger.debug("explainCommonMetaData");
            if (javax_imageio.containsKey("ColorSpaceType")) {
                Map<String, String> ColorSpaceType = javax_imageio.get("ColorSpaceType");
                if (ColorSpaceType.containsKey("name")) {
                    imageInfo.setColorSpace(ColorSpaceType.get("name"));
                }
            }
            if (javax_imageio.containsKey("NumChannels")) {
                Map<String, String> NumChannels = javax_imageio.get("NumChannels");
                if (NumChannels.containsKey("value")) {
                    imageInfo.setColorChannels(Integer.valueOf(NumChannels.get("value")));
                }
            }
            if (javax_imageio.containsKey("CompressionTypeName")) {
                Map<String, String> CompressionTypeName = javax_imageio.get("CompressionTypeName");
                if (CompressionTypeName.containsKey("value")) {
                    imageInfo.setCompressionType(CompressionTypeName.get("value"));
                }
            }
            if (javax_imageio.containsKey("Lossless")) {
                Map<String, String> Lossless = javax_imageio.get("Lossless");
                if (Lossless.containsKey("value")) {
                    imageInfo.setIsLossless(Lossless.get("value").equals("TRUE"));
                }
            }
            if (javax_imageio.containsKey("ImageOrientation")) {
                Map<String, String> ImageOrientation = javax_imageio.get("ImageOrientation");
                if (ImageOrientation.containsKey("value")) {
                    imageInfo.setImageRotation(ImageOrientation.get("value"));
                }
            }
            if (javax_imageio.containsKey("Alpha")) {
                Map<String, String> Alpha = javax_imageio.get("Alpha");
                if (Alpha.containsKey("value")) {
                    imageInfo.setHasAlpha(!Alpha.get("value").equals("none"));
                }
            }
            if (javax_imageio.containsKey("HorizontalPixelSize")) { // The width of a pixel, in millimeters
                Map<String, String> HorizontalPixelSize = javax_imageio.get("HorizontalPixelSize");
                if (HorizontalPixelSize.containsKey("value")) {
                    float v = Float.valueOf(HorizontalPixelSize.get("value"));
                    imageInfo.setwDensity(pixelSizeMm2dpi(v));
//                    logger.debug("HorizontalPixelSize:" + imageInfo.gethResolution());
                }
            }
            if (javax_imageio.containsKey("VerticalPixelSize")) { // The height of a pixel, in millimeters
                Map<String, String> VerticalPixelSize = javax_imageio.get("VerticalPixelSize");
                if (VerticalPixelSize.containsKey("value")) {
                    float v = Float.valueOf(VerticalPixelSize.get("value"));
                    imageInfo.sethDensity(pixelSizeMm2dpi(v));
//                    logger.debug("VerticalPixelSize:" + imageInfo.getvResolution());
                }
            }
            if (javax_imageio.containsKey("FormatVersion")) {
                Map<String, String> FormatVersion = javax_imageio.get("FormatVersion");
                if (FormatVersion.containsKey("value")) {
                    if (!imageInfo.getImageFormat().equals(FormatVersion.get("value"))) {
                        String extra = imageInfo.getExtraFormat();
                        if (extra == null) {
                            imageInfo.setExtraFormat(FormatVersion.get("value"));
                        } else if (!FormatVersion.get("value").equals(extra)) {
                            imageInfo.setExtraFormat(extra + " " + FormatVersion.get("value"));
                        }
                    }
                }
            }
            if (javax_imageio.containsKey("BitsPerSample")) {
                Map<String, String> BitsPerSample = javax_imageio.get("BitsPerSample");
                if (BitsPerSample.containsKey("value")) {
                    imageInfo.setBitDepth(BitsPerSample.get("value"));
                }
            }

        } catch (Exception e) {

        }
    }

    // http://johnbokma.com/java/obtaining-image-metadata.html
    public static void displayMetadata(String fileName) {
        try {
            System.out.println("\n\n" + fileName);
            File file = new File(fileName);
            try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
                if (readers.hasNext()) {
                    ImageReader reader = readers.next();
                    reader.setInput(iis, false);
                    IIOMetadata metadata = reader.getImageMetadata(0);
                    String[] names = metadata.getMetadataFormatNames();
                    int length = names.length;
                    for (int i = 0; i < length; i++) {
                        System.out.println("Format name: " + names[i]);
                        displayMetadata(metadata.getAsTree(names[i]));
                    }
                    reader.dispose();
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void displayMetadata(Node root) {
        displayMetadata(root, 0);
    }

    public static void indent(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
    }

    public static void displayMetadata(Node node, int level) {
        // print open tag of element
        indent(level);
        System.out.print("<" + node.getNodeName());
        NamedNodeMap map = node.getAttributes();
        if (map != null) {

            // print attribute values
            int length = map.getLength();
            for (int i = 0; i < length; i++) {
                Node attr = map.item(i);
                System.out.print(" " + attr.getNodeName()
                        + "=\"" + attr.getNodeValue() + "\"");
            }
        }

        Node child = node.getFirstChild();
        if (child == null) {
            // no children, so close element and return
            System.out.println("/>");
            return;
        }

        // children, so close current tag
        System.out.println(">");
        while (child != null) {
            // print children recursively
            displayMetadata(child, level + 1);
            child = child.getNextSibling();
        }

        // print close tag of element
        indent(level);
        System.out.println("</" + node.getNodeName() + ">");
    }

}
