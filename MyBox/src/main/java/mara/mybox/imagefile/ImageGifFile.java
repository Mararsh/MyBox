package mara.mybox.imagefile;

import com.github.jaiimageio.impl.plugins.gif.GIFImageMetadata;
import com.github.jaiimageio.impl.plugins.gif.GIFImageWriter;
import com.github.jaiimageio.impl.plugins.gif.GIFImageWriterSpi;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.image.ImageConvertTools;
import static mara.mybox.imagefile.ImageFileReaders.needSampled;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.ImageAttributes;
import mara.mybox.objects.ImageInformation;
import mara.mybox.tools.FileTools;

import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.tools.ValueTools;

import thridparty.GifDecoder;
import thridparty.GifDecoder.GifImage;

/**
 * @Author Mara
 * @CreateDate 2018-6-24
 *
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageGifFile {

    public static GIFImageMetadata getGifMetadata(File file) {
        try {
//            ImageReaderSpi readerSpi = new GIFImageReaderSpi();
//            GIFImageReader gifReader = (GIFImageReader) readerSpi.createReaderInstance();

            ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName("gif").next();
            try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
                reader.setInput(iis, false);
                GIFImageMetadata metadata = (GIFImageMetadata) reader.getImageMetadata(0);
                reader.dispose();
                return metadata;
            }

        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static List<BufferedImage> readGifFile(String src) {
        try {
            List<BufferedImage> images = new ArrayList<>();
            ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName("gif").next();
            boolean broken = false;
            try (ImageInputStream in = ImageIO.createImageInputStream(new FileInputStream(src))) {
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
                        if (!e.toString().contains("Attempt to read past end of image sequence!")) {
                            broken = true;
                        }
                        break;
                    }
                }
                reader.dispose();
                if (broken) {
                    return readBrokenGifFile(src);
                } else {
                    return images;
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    // https://stackoverflow.com/questions/22259714/arrayindexoutofboundsexception-4096-while-reading-gif-file
    // https://github.com/DhyanB/Open-Imaging
    public static List<BufferedImage> readBrokenGifFile(String src) {
        try {
//            logger.debug("readBrokenGifFile");
            List<BufferedImage> images = new ArrayList<>();
            try (FileInputStream in = new FileInputStream(src)) {
                final GifImage gif = GifDecoder.read(in);
                final int frameCount = gif.getFrameCount();
                for (int i = 0; i < frameCount; i++) {
                    final BufferedImage img = gif.getFrame(i);
                    images.add(img);
                }
            }
            return images;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static List<BufferedImage> readBrokenGifFileWithWidth(String src, int width) {
        try {
//            logger.debug("readBrokenGifFile");
            List<BufferedImage> images = new ArrayList<>();
            try (FileInputStream in = new FileInputStream(src)) {
                final GifImage gif = GifDecoder.read(in);
                final int frameCount = gif.getFrameCount();
                for (int i = 0; i < frameCount; i++) {
                    final BufferedImage img = gif.getFrame(i);
                    images.add(ImageConvertTools.scaleImageWidthKeep(img, width));
                }
            }
            return images;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static List<BufferedImage> readBrokenGifFile(String src, List<ImageInformation> imagesInfo) {
        try {
//            logger.debug("readBrokenGifFile");
            List<BufferedImage> images = new ArrayList<>();
            try (FileInputStream in = new FileInputStream(src)) {
                final GifImage gif = GifDecoder.read(in);
                final int frameCount = gif.getFrameCount();
                Map<String, Long> sizes = new HashMap<>();
                for (int i = 0; i < frameCount; i++) {
                    ImageInformation info = imagesInfo.get(i);
                    boolean needSampled = needSampled(info, frameCount, sizes);
                    final BufferedImage img = gif.getFrame(i);
                    if (needSampled) {
                        images.add(ImageConvertTools.scaleImageWidthKeep(img, sizes.get("sampledWidth").intValue()));
                        info.setIsSampled(true);
                        info.setSizeString(info.getWidth() + "x" + info.getHeight() + " *");
                    } else {
                        images.add(img);
                        info.setIsSampled(false);
                        info.setBufferedImage(img);
                        info.setSizeString(info.getWidth() + "x" + info.getHeight());
                    }
                }
            }
            return images;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage readBrokenGifFile(String src,
            ImageInformation imagesInfo, Map<String, Long> sizes) {
        try {
            BufferedImage bufferedImage;
            try (FileInputStream in = new FileInputStream(src)) {
                final GifImage gif = GifDecoder.read(in);
                boolean needSampled = needSampled(imagesInfo, 1, sizes);
                bufferedImage = gif.getFrame(0);
                if (needSampled) {
                    bufferedImage = ImageConvertTools.scaleImageWidthKeep(bufferedImage, sizes.get("sampledWidth").intValue());
                    imagesInfo.setIsSampled(true);
                } else {
                    imagesInfo.setIsSampled(false);
                    imagesInfo.setBufferedImage(bufferedImage);
                }
            }
            return bufferedImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage readBrokenGifFile(String src, int index) {
        try {
            try (FileInputStream in = new FileInputStream(src)) {
                final GifImage gif = GifDecoder.read(in);
                final BufferedImage img = gif.getFrame(index);
                return img;
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static List<String> extractGifImages(File source, File target,
            int from, int to) {
        try {
            if (source == null || target == null || from < 0 || to < 0 || from > to) {
                return null;
            }
            List<BufferedImage> images = readGifFile(source.getAbsolutePath());
            if (images == null) {
                return null;
            }
            int size = images.size();
            if (images.isEmpty() || from >= size || to >= size) {
                return null;
            }
            String filePrefix = FileTools.getFilePrefix(target.getAbsolutePath());
            String format = FileTools.getFileSuffix(target.getAbsolutePath());
            if (!CommonValues.SupportedImages.contains(format)) {
                return null;
            }
            String filename;
            int digit = (size + "").length();
            List<String> names = new ArrayList<>();
            for (int i = from; i <= to; i++) {
                filename = filePrefix + "-" + ValueTools.fillNumber(i, digit) + "." + format;
                ImageFileWriters.writeImageFile(images.get(i), format, filename);
                names.add(filename);
            }
            return names;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    // https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/gif_metadata.html#image
    public static void writeGifImageFile(BufferedImage image,
            ImageAttributes attributes, String outFile) {
        try {

            GIFImageWriterSpi gifspi = new GIFImageWriterSpi();
            GIFImageWriter writer = new GIFImageWriter(gifspi);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (attributes.getCompressionType() != null) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                logger.error(attributes.getCompressionType());
                param.setCompressionType(attributes.getCompressionType());
                param.setCompressionQuality(1);
            }

            GIFImageMetadata metaData;
            try {
                metaData = (GIFImageMetadata) writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
            } catch (Exception e) {
                logger.error(e.toString());
                metaData = null;
            }

            if (attributes.getDensity() > 0) {
                // Have not found the way to set density data in meta data of GIF format.
            }

            try (ImageOutputStream out = ImageIO.createImageOutputStream(new File(outFile))) {
                writer.setOutput(out);
                writer.write(metaData, new IIOImage(image, null, metaData), param);
                out.flush();
            }
            writer.dispose();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static boolean getParaMeta(int interval, boolean loop,
            GIFImageWriter gifWriter, ImageWriteParam param, GIFImageMetadata metaData) {
        try {

            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType("LZW");
            param.setCompressionQuality(1);

            String delay;
            if (interval > 0) {
                delay = interval / 10 + "";
            } else {
                delay = "100";
            }
            String format = metaData.getNativeMetadataFormatName();
            IIOMetadataNode tree = (IIOMetadataNode) metaData.getAsTree(format);
            IIOMetadataNode graphicsControlExtensionNode = new IIOMetadataNode("GraphicControlExtension");
            graphicsControlExtensionNode.setAttribute("delayTime", delay);
            graphicsControlExtensionNode.setAttribute("disposalMethod", "restoreToBackgroundColor");
            graphicsControlExtensionNode.setAttribute("userInputFlag", "false");
            graphicsControlExtensionNode.setAttribute("transparentColorFlag", "false");
            graphicsControlExtensionNode.setAttribute("delayTime", delay);
            graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");
            tree.appendChild(graphicsControlExtensionNode);
            if (loop) {
                IIOMetadataNode applicationExtensionsNode = new IIOMetadataNode("ApplicationExtensions");
                IIOMetadataNode applicationExtensionNode = new IIOMetadataNode("ApplicationExtension");
                applicationExtensionNode.setAttribute("applicationID", "NETSCAPE");
                applicationExtensionNode.setAttribute("authenticationCode", "2.0");
                byte[] k = {1, 0, 0};
                applicationExtensionNode.setUserObject(k);
                applicationExtensionsNode.appendChild(applicationExtensionNode);
                tree.appendChild(applicationExtensionsNode);
            }
            metaData.mergeTree(format, tree);

            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    // https://www.programcreek.com/java-api-examples/javax.imageio.ImageWriter
    // http://www.java2s.com/Code/Java/2D-Graphics-GUI/GiffileEncoder.htm
    // https://programtalk.com/python-examples/com.sun.media.imageioimpl.plugins.gif.GIFImageWriterSpi/
    // https://www.jianshu.com/p/df52f1511cf8
    // http://giflib.sourceforge.net/whatsinagif/index.html
    public static boolean writeImages(List<BufferedImage> images,
            File outFile, int interval, boolean loop) {
        try {

            if (images == null || outFile == null || images.isEmpty()) {
                return false;
            }
            try {
                if (outFile.exists()) {
                    outFile.delete();
                }
            } catch (Exception e) {
                return false;
            }
            GIFImageWriterSpi gifspi = new GIFImageWriterSpi();
            GIFImageWriter gifWriter = new GIFImageWriter(gifspi);
            ImageWriteParam param = gifWriter.getDefaultWriteParam();
            GIFImageMetadata metaData = (GIFImageMetadata) gifWriter.getDefaultImageMetadata(
                    ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB), param);

            try (ImageOutputStream out = ImageIO.createImageOutputStream(outFile)) {
                gifWriter.setOutput(out);
                getParaMeta(interval, loop, gifWriter, param, metaData);
                gifWriter.prepareWriteSequence(null);
                for (BufferedImage image : images) {
                    gifWriter.writeToSequence(new IIOImage(image, null, metaData), param);
                }
                gifWriter.endWriteSequence();
                out.flush();
            }
            gifWriter.dispose();

            return true;

        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    public static String writeImages(List<ImageInformation> imagesInfo,
            File outFile, int interval, boolean loop, boolean keepSize, int width, int height) {
        try {
            if (imagesInfo == null || imagesInfo.isEmpty() || outFile == null) {
                return "InvalidParameters";
            }
            try {
                if (outFile.exists()) {
                    outFile.delete();
                }
            } catch (Exception e) {
                return e.toString();
            }
            GIFImageWriterSpi gifspi = new GIFImageWriterSpi();
            GIFImageWriter gifWriter = new GIFImageWriter(gifspi);
            ImageWriteParam param = gifWriter.getDefaultWriteParam();
            GIFImageMetadata metaData = (GIFImageMetadata) gifWriter.getDefaultImageMetadata(
                    ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB), param);

            try (ImageOutputStream out = ImageIO.createImageOutputStream(outFile)) {
                gifWriter.setOutput(out);
                getParaMeta(interval, loop, gifWriter, param, metaData);
                gifWriter.prepareWriteSequence(null);
                for (ImageInformation info : imagesInfo) {
                    BufferedImage bufferedImage = ImageFileReaders.getBufferedImage(info);
                    if (bufferedImage != null) {
                        if (!keepSize) {
                            bufferedImage = ImageConvertTools.scaleImage(bufferedImage, width, height);
                        }
                        gifWriter.writeToSequence(new IIOImage(bufferedImage, null, metaData), param);
                    }
                }
                gifWriter.endWriteSequence();
                out.flush();
            }
            gifWriter.dispose();
            return "";

        } catch (Exception e) {
            logger.error(e.toString());
            return e.toString();
        }
    }

}
