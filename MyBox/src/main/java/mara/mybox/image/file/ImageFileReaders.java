package mara.mybox.image.file;

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
import mara.mybox.color.ColorBase;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.image.ImageColor;
import static mara.mybox.image.ImageConvert.pixelSizeMm2dpi;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageInformation;
import mara.mybox.image.ImageManufacture;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;
import net.sf.image4j.codec.ico.ICODecoder;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:27
 *
 * @Description
 * @License Apache License Version 2.0
 */
// https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/standard_metadata.html
public class ImageFileReaders {

    public static ImageReader getReader(String format) {
        return ImageIO.getImageReadersByFormatName(format).next();
    }

    public static ImageReader getReader(File file) {
        return getReader(FileTools.getFileSuffix(file));
    }

    public static BufferedImage readImage(File file) {
        if (file == null) {
            return null;
        }
        BufferedImage image;
        try {
            String format = FileTools.getFileSuffix(file);
            if ("ico".equals(format) || "icon".equals(format)) {
                return readIcon(file);
            }
//            if (ImageJpeg2000File.isJpeg2000(file)) {
//                return ImageJpeg2000File.readImage(file);
//            }
            image = ImageIO.read(file);
        } catch (Exception e) {
            image = readBrokenImage(e, file);
        }
        return image;
    }

    public static BufferedImage readImage(File file, ImageInformation imageInfo) {
        if (file == null) {
            return null;
        }
        BufferedImage image;
        try {
            image = readImage(file);
        } catch (Exception e) {
            image = readBrokenImage(e, file, imageInfo);
        }
        return image;
    }

    public static BufferedImage readFileByHeight(String format, String filename,
            int height) {
        BufferedImage bufferedImage;
        int scale = 1;
        try {
            if ("ico".equals(format) || "icon".equals(format)) {
                bufferedImage = readIcon(new File(filename));
                return ImageManufacture.scaleImageHeightKeep(bufferedImage, height);
            }
            ImageReader reader = getReader(format);
            try ( ImageInputStream in = ImageIO.createImageInputStream(
                    new BufferedInputStream(new BufferedInputStream(new FileInputStream(filename))))) {
                reader.setInput(in, false);
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
            bufferedImage = readBrokenImage(e, new File(filename), scale);
        }
        return bufferedImage;
    }

    public static BufferedImage readFileByWidth(String format, String filename,
            int width) {
        BufferedImage bufferedImage;
        int scale = 1;
        try {
            if ("ico".equals(format) || "icon".equals(format)) {
                bufferedImage = readIcon(new File(filename));
                return ImageManufacture.scaleImageWidthKeep(bufferedImage, width);
            }
            ImageReader reader = getReader(format);
            try ( ImageInputStream in = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
                reader.setInput(in, false);
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
            bufferedImage = readBrokenImage(e, new File(filename), scale);
        }
        return bufferedImage;
    }

    public static BufferedImage readFileByScale(String format, String filename,
            int scale) {
        BufferedImage bufferedImage;
        try {
            if ("ico".equals(format) || "icon".equals(format)) {
                bufferedImage = readIcon(new File(filename));
                return ImageManufacture.scaleImage(bufferedImage, scale);
            }
            ImageReader reader = getReader(format);
            try ( ImageInputStream in = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(filename)))) {

                reader.setInput(in, false);

                ImageReadParam param = reader.getDefaultReadParam();

                param.setSourceSubsampling(scale, scale, 0, 0);

                bufferedImage = reader.read(0);
//                bufferedImage = reader.read(0, param);

                reader.dispose();
            }
        } catch (Exception e) {
            bufferedImage = readBrokenImage(e, new File(filename), scale);
        }
        return bufferedImage;
    }

    public static BufferedImage readFileBySample(String format, String filename,
            DoubleRectangle rect, int sampleWidth, int sampleHeight) {
        return readFileBySample(format, filename,
                (int) Math.round(rect.getSmallX()), (int) Math.round(rect.getSmallY()),
                (int) Math.round(rect.getBigX()), (int) Math.round(rect.getBigY()),
                sampleWidth, sampleHeight);
    }

    public static BufferedImage readFileBySample(String format, String filename,
            int x1, int y1, int x2, int y2,
            int sampleWidth, int sampleHeight) {
        BufferedImage bufferedImage;
        try {
            if (x1 >= x2 || y1 >= y2
                    || x1 < 0 || x2 < 0 || y1 < 0 || y2 < 0
                    || sampleWidth <= 0 || sampleHeight <= 0) {
                return null;
            }
            ImageReader reader = getReader(format);
            try ( ImageInputStream in = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
                reader.setInput(in, false);
                ImageReadParam param = reader.getDefaultReadParam();
                param.setSourceRegion(new Rectangle(x1, y1, x2 - x1 + 1, y2 - y1 + 1));
                param.setSourceSubsampling(sampleWidth, sampleHeight, 0, 0);
                bufferedImage = reader.read(0, param);
//                logger.debug(bufferedImage.getWidth() + " " + bufferedImage.getHeight());
                reader.dispose();
            }
        } catch (Exception e) {
            bufferedImage = readBrokenImage(e, new File(filename), 0, sampleWidth, sampleHeight);
        }
        return bufferedImage;
    }

    public static List<BufferedImage> readFrames(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        return readFrames(FileTools.getFileSuffix(file), file.getAbsolutePath());
    }

    public static List<BufferedImage> readFrames(String format, String filename) {
        try {
            if ("ico".equals(format) || "icon".equals(format)) {
                return ICODecoder.read(new File(filename));
            }
            List<BufferedImage> images = new ArrayList<>();
            ImageReader reader = getReader(format);
            try ( ImageInputStream in = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
                reader.setInput(in, false);
                int count = 0;
                while (true) {
                    BufferedImage m;
                    try {
                        m = reader.read(count);
                    } catch (Exception e) {
                        m = readBrokenImage(e, new File(filename), count, 1, 1);
                    }
                    if (m != null) {
                        images.add(m);
                        count++;
                    } else {
                        break;
                    }
                }
                reader.dispose();
                return images;
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static List<BufferedImage> readFramesWithWidth(String format,
            String filename, int width) {
        try {
            List<BufferedImage> images = new ArrayList<>();
            if ("ico".equals(format) || "icon".equals(format)) {
                List<BufferedImage> imageSrc = ICODecoder.read(new File(filename));
                for (BufferedImage image : imageSrc) {
                    images.add(ImageManufacture.scaleImageWidthKeep(image, width));
                }
                return images;
            }
            ImageReader reader = getReader(format);
            boolean broken = false;
            try ( ImageInputStream in = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
                reader.setInput(in, false);
                int count = 0, scale;
                ImageReadParam param = reader.getDefaultReadParam();
                while (true) {
                    if (reader.getWidth(count) <= width) {
                        scale = 1;
                    } else {
                        scale = reader.getWidth(count) / width + 1;
                        if (scale < 2) {
                            scale = 2;
                        }
                    }
                    BufferedImage m;
                    try {
                        param.setSourceSubsampling(scale, scale, 0, 0);
                        m = reader.read(count, param);
                    } catch (Exception e) {
                        m = readBrokenImage(e, new File(filename), scale);
                    }
                    if (m != null) {
                        images.add(m);
                        count++;
                    } else {
                        break;
                    }
                }
                reader.dispose();
                return images;
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static boolean needSampled(ImageInformation imageInfo,
            int framesNumber) {
        if (imageInfo == null || framesNumber < 1) {
            return false;
        }
        long availableMem, pixelsSize, requiredMem, totalRequiredMem;
        pixelsSize = imageInfo.getHeight() * imageInfo.getWidth() * 1L;
        if (imageInfo.getColorChannels() > 0) {
            pixelsSize = pixelsSize * imageInfo.getColorChannels();
        } else {
            pixelsSize = pixelsSize * 4;
        }
        pixelsSize = pixelsSize / (1024 * 1024);
        Runtime r = Runtime.getRuntime();
        long used = (r.totalMemory() - r.freeMemory()) / (1024 * 1024L);
        availableMem = r.maxMemory() / (1024 * 1024L) - used;
        requiredMem = pixelsSize * 5L;
        totalRequiredMem = requiredMem * framesNumber + 200;

        Map<String, Long> sizes = new HashMap<>();
        sizes.put("availableMem", availableMem);
        sizes.put("pixelsSize", pixelsSize);
        sizes.put("requiredMem", requiredMem);
        sizes.put("totalRequiredMem", totalRequiredMem);

        long maxSize = availableMem * 1014 * 1024L / (6 * framesNumber);
        double ratio = imageInfo.getHeight() * 1.0 / imageInfo.getWidth();
        long sampledWidth = (long) Math.sqrt(maxSize / (ratio * imageInfo.getColorChannels()));
        int max = AppVariables.getUserConfigInt("MaxImageSampleWidth", 4096);
        if (sampledWidth > max) {
            sampledWidth = max;
        }
        sizes.put("sampledWidth", sampledWidth);

        imageInfo.setSizes(sizes);
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
                    bufferedImage = FxmlImageManufacture.getBufferedImage(imageInfo.getImage());
                }
            }
            if (bufferedImage == null) {
                String fname = imageInfo.getFileName();
                if (fname != null) {
                    if (imageInfo.getIndex() <= 0) {
                        bufferedImage = ImageFileReaders.readImage(new File(fname), imageInfo);
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
            if ("ico".equals(format) || "icon".equals(format)) {
                return readFrames(format, filename);
            }
            List<BufferedImage> images = new ArrayList<>();
            if (imagesInfo == null || imagesInfo.isEmpty()) {
                return images;
            }
            ImageReader reader = getReader(format);
            int total = imagesInfo.size();

            try ( ImageInputStream in = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
                reader.setInput(in, false);
                int scale;
                ImageReadParam param = reader.getDefaultReadParam();
                for (int i = 0; i < imagesInfo.size(); ++i) {
                    ImageInformation info = imagesInfo.get(i);
                    scale = 1;
                    boolean needSampled = needSampled(info, total);
                    if (needSampled) {
                        Map<String, Long> sizes = info.getSizes();
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
                    try {
                        BufferedImage m;
                        try {
                            param.setSourceSubsampling(scale, scale, 0, 0);
                            m = reader.read(i, param);
                        } catch (Exception e) {
                            m = readBrokenImage(e, new File(filename), scale);
                        }
                        if (scale == 1) {
                            info.setIsSampled(false);
                            info.setBufferedImage(m);
                        } else {
                            info.setIsSampled(true);
//                            info.setSampledBufferedImage(m);
                        }
                        info.setIndex(i + 1);
                        images.add(m);
                    } catch (Exception e) {
                        logger.error(e.toString());
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
            ImageInformation imageInfo) {
        try {
            if ("ico".equals(format) || "icon".equals(format)) {
                return readIcon(new File(filename));
            }
            BufferedImage bufferedImage;
            ImageReader reader = getReader(format);
            try ( ImageInputStream in = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
                reader.setInput(in, false);
                int scale = 1;
                ImageReadParam param = reader.getDefaultReadParam();
                if (imageInfo != null) {
                    boolean needSampled = needSampled(imageInfo, 1);
                    Map<String, Long> sizes = imageInfo.getSizes();
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
                try {
                    try {
                        param.setSourceSubsampling(scale, scale, 0, 0);
                        bufferedImage = reader.read(0, param);
                    } catch (Exception e) {
                        bufferedImage = readBrokenImage(e, new File(filename), scale);
                    }

                    if (imageInfo != null) {
                        if (scale == 1) {
                            imageInfo.setIsSampled(false);
                            imageInfo.setBufferedImage(bufferedImage);
                        } else {
                            imageInfo.setIsSampled(true);
//                            imageInfo.setSampledBufferedImage(m);
                        }
                    }

                } catch (Exception e) {
                    bufferedImage = readBrokenImage(e, new File(filename), scale);
                }

                reader.dispose();
            }
            return bufferedImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage readFrame(String format, String filename,
            int index) {
        BufferedImage bufferedImage = null;
        try {
            if ("ico".equals(format) || "icon".equals(format)) {
                return ICODecoder.read(new File(filename)).get(index);
            }
            ImageReader reader = getReader(format);
            try ( ImageInputStream in = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
                reader.setInput(in, false);
                try {
                    bufferedImage = reader.read(index);
                } catch (Exception e) {
                    bufferedImage = readBrokenImage(e, new File(filename), index, 1, 1);
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
            BufferedImage bufferedImage;
            if ("ico".equals(format) || "icon".equals(format)) {
                bufferedImage = readIcon(new File(filename));
                bufferedImage = bufferedImage.getSubimage(bounds.x, bounds.y, bounds.width, bounds.height);
                return bufferedImage;
            }
            ImageReader reader = getReader(format);
            try ( ImageInputStream in = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
                reader.setInput(in, false);
                ImageReadParam param = reader.getDefaultReadParam();
                param.setSourceRegion(bounds);
                try {
                    bufferedImage = reader.read(0, param);
                } catch (Exception e) {
                    bufferedImage = readBrokenImage(e, new File(filename));
                    bufferedImage = bufferedImage.getSubimage(bounds.x, bounds.y, bounds.width, bounds.height);
                }
                reader.dispose();
            }
            return bufferedImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage readIcon(File srcFile) {
        try {
            if (srcFile == null) {
                return null;
            }
            List<BufferedImage> images = ICODecoder.read(srcFile);
            if (images == null || images.isEmpty()) {
                return null;
            }
            return images.get(0);
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    /*
        Broken image
     */
    public static BufferedImage readBrokenImage(Exception e, File file) {
        return readBrokenImage(e, file, 1);
    }

    public static BufferedImage readBrokenImage(Exception e, File file,
            int scale) {
        return readBrokenImage(e, file, 0, 1, 1);

    }

    public static BufferedImage readBrokenImage(Exception e, File file,
            int index, int xscale, int yscale) {
        BufferedImage image = null;
        try {
            ImageFileInformation finfo = readImageFileMetaData(file.getAbsolutePath());
            if (finfo == null) {
                return null;
            }
            image = readBrokenImage(e, file, finfo.getImageInformation(), index, xscale, yscale);
        } catch (Exception ex) {
//            logger.error(ex.toString());
        }
        return image;
    }

    public static BufferedImage readBrokenImage(Exception e, File file,
            ImageInformation imageInfo) {
        return readBrokenImage(e, file, imageInfo, 0, 1, 1);
    }

    public static BufferedImage readBrokenImage(Exception e, File file,
            ImageInformation imageInfo,
            int index, int xscale, int yscale) {
        if (imageInfo == null) {
            return null;
        }
        BufferedImage image = null;
        String format = imageInfo.getImageFormat();
        switch (format) {
            case "gif":
                // Read Gif with JDK api normally. When broken, use DhyanB's API.
                // if (e.toString().contains("java.lang.ArrayIndexOutOfBoundsException: 4096")) {
                if (e.toString().contains("java.lang.ArrayIndexOutOfBoundsException")) {
                    image = ImageGifFile.readBrokenGifFile(file.getAbsolutePath(), index, xscale, yscale);
                }
                break;
            case "jpg":
            case "jpeg":
                if (e.toString().contains("Unsupported Image Type") && imageInfo.getColorChannels() == 4) {
                    image = ImageJpgFile.readBrokenJpgFile(file, imageInfo, index, xscale, yscale);
                }
                break;
            default:
//                logger.error(e.toString());
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
        String targetFormat = FileTools.getFileSuffix(file).toLowerCase();
        if ("ico".equals(targetFormat) || "icon".equals(targetFormat)) {
            fileInfo.setNumberOfImages(1);
            fileInfo.setImageFormat(targetFormat);
            ImageInformation imageInfo = ImageInformation.create(targetFormat, file);
            imageInfo.setImageFileInformation(fileInfo);
            imageInfo.setImageFormat(targetFormat);
            imageInfo.setFileName(fileInfo.getFileName());
            imageInfo.setCreateTime(fileInfo.getCreateTime());
            imageInfo.setModifyTime(fileInfo.getModifyTime());
            imageInfo.setFileSize(fileInfo.getFileSize());
            List<ImageInformation> imagesInfo = new ArrayList<>();
            imagesInfo.add(imageInfo);
            fileInfo.setImagesInformation(imagesInfo);
            fileInfo.setImageInformation(imageInfo);
            return fileInfo;
        }
        try {
            try ( ImageInputStream iis = ImageIO.createImageInputStream(file)) {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
                if (readers.hasNext()) {
                    ImageReader reader = readers.next();
                    reader.setInput(iis, false);
                    int num = reader.getNumImages(true);
                    fileInfo.setNumberOfImages(num);
                    String format = reader.getFormatName().toLowerCase();
                    fileInfo.setImageFormat(format);

                    List<ImageInformation> imagesInfo = new ArrayList<>();
                    for (int i = 0; i < num; ++i) {
                        ImageInformation imageInfo = ImageInformation.create(format, file);
                        imageInfo.setImageFileInformation(fileInfo);
                        imageInfo.setImageFormat(format);
                        imageInfo.setFileName(fileInfo.getFileName());
                        imageInfo.setCreateTime(fileInfo.getCreateTime());
                        imageInfo.setModifyTime(fileInfo.getModifyTime());
                        imageInfo.setFileSize(fileInfo.getFileSize());
                        imageInfo.setWidth(reader.getWidth(i));
                        imageInfo.setHeight(reader.getHeight(i));
                        imageInfo.setIsMultipleFrames(num > 1);
                        imageInfo.setIsTiled(reader.isImageTiled(i));
                        imageInfo.setIndex(i + 1);
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
                            logger.error(e.toString());
                        }
                        try {
                            imageInfo.setHasThumbnails(reader.hasThumbnails(i));
                            imageInfo.setNumberOfThumbnails(reader.getNumThumbnails(i));
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
//            logger.debug(metaData);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void readImageMetaData(
            Map<String, List<Map<String, Object>>> formatMetaData,
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
            logger.error(e.toString());
        }
    }

    // https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/standard_metadata.html
    public static void explainCommonMetaData(
            Map<String, Map<String, List<Map<String, Object>>>> metaData,
            ImageInformation imageInfo) {
        try {
            if (!metaData.containsKey("javax_imageio_1.0")) {
                return;
            }
            Map<String, List<Map<String, Object>>> javax_imageio = metaData.get("javax_imageio_1.0");
//            logger.debug("explainCommonMetaData");
            if (javax_imageio.containsKey("ColorSpaceType")) {
                Map<String, Object> ColorSpaceType = javax_imageio.get("ColorSpaceType").get(0);
                if (ColorSpaceType.containsKey("name")) {
                    imageInfo.setColorSpace((String) ColorSpaceType.get("name"));
//                    logger.debug(" colorSpaceType:" + ColorSpaceType.get("name"));
                }
            }
            if (javax_imageio.containsKey("NumChannels")) {
                Map<String, Object> NumChannels = javax_imageio.get("NumChannels").get(0);
                if (NumChannels.containsKey("value")) {
                    imageInfo.setColorChannels(Integer.valueOf((String) NumChannels.get("value")));
//                    logger.debug(" NumChannels:" + NumChannels.get("value"));
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
        try {
            try ( ImageInputStream iis = ImageIO.createImageInputStream(file)) {
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

}
