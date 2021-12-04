package mara.mybox.imagefile;

import com.github.jaiimageio.impl.plugins.gif.GIFImageMetadata;
import com.github.jaiimageio.impl.plugins.gif.GIFImageWriter;
import com.github.jaiimageio.impl.plugins.gif.GIFImageWriterSpi;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.bufferedimage.ImageAttributes;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TmpFileTools;
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
            try ( ImageInputStream iis = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
                reader.setInput(iis, false);
                GIFImageMetadata metadata = (GIFImageMetadata) reader.getImageMetadata(0);
                reader.dispose();
                return metadata;
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static List<BufferedImage> readGifFile(String src) {
        try {
            List<BufferedImage> images = new ArrayList<>();

            boolean broken = false;
            try ( ImageInputStream iis = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(src)))) {
                ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
                reader.setInput(iis, false);
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
//                        MyBoxLog.error(e.toString());
                        if (!e.toString().contains("Attempt to read past end of image sequence!")) {
                            broken = true;
                        }
                        break;
                    }
                }
                reader.dispose();
            }
            if (broken) {
                return readBrokenGifFile(src);
            } else {
                return images;
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    // https://stackoverflow.com/questions/22259714/arrayindexoutofboundsexception-4096-while-reading-gif-file
    // https://github.com/DhyanB/Open-Imaging
    public static List<BufferedImage> readBrokenGifFile(String src) {
        try {
//            MyBoxLog.debug("readBrokenGifFile");
            List<BufferedImage> images = new ArrayList<>();
            try ( BufferedInputStream in = new BufferedInputStream(new FileInputStream(src))) {
                GifImage gif = GifDecoder.read(in);
                int frameCount = gif.getFrameCount();
                for (int i = 0; i < frameCount; ++i) {
                    BufferedImage img = gif.getFrame(i);
                    images.add(img);
                }
            }
            return images;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage readBrokenGifIndex(String src, int index) {
        BufferedImage bufferedImage = null;
        try ( BufferedInputStream in = new BufferedInputStream(new FileInputStream(src))) {
            GifImage gif = GifDecoder.read(in);
//                MyBoxLog.error(gif.getFrameCount());
            bufferedImage = gif.getFrame(index);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return bufferedImage;
    }

    public static BufferedImage readBrokenGifFile(ImageInformation imageInfo) {
        BufferedImage bufferedImage = null;
        try {
            ImageInformation.checkValues(imageInfo);
            bufferedImage = readBrokenGifIndex(imageInfo.getFileName(), imageInfo.getIndex());
            if (imageInfo.getWidth() <= 0) {
                imageInfo.setWidth(bufferedImage.getWidth());
                imageInfo.setHeight(bufferedImage.getHeight());
            }
            bufferedImage = imageInfo.loadBufferedImage(bufferedImage, false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            imageInfo.loadBufferedImage(null, false);
        }
        return bufferedImage;
    }

    // 0-based
    public static List<String> extractGifImages(File source, String filePrefix, String format, int from, int to) {
        try {
            if (source == null || filePrefix == null || format == null
                    || from < 0 || to < 0 || from > to) {
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

            String filename;
            int digit = (size + "").length();
            List<String> names = new ArrayList<>();
            for (int i = from; i <= to; ++i) {
                filename = filePrefix + "-" + StringTools.fillLeftZero(i, digit) + "." + format;
                ImageFileWriters.writeImageFile(images.get(i), format, filename);
                names.add(filename);
            }
            return names;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    // https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/gif_metadata.html#image
    public static ImageWriter getWriter() {
        GIFImageWriterSpi gifspi = new GIFImageWriterSpi();
        GIFImageWriter writer = new GIFImageWriter(gifspi);
        return writer;
    }

    public static ImageWriteParam getPara(ImageAttributes attributes, ImageWriter writer) {
        try {
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                if (attributes != null && attributes.getCompressionType() != null) {
                    param.setCompressionType(attributes.getCompressionType());
                }
                if (attributes != null && attributes.getQuality() > 0) {
                    param.setCompressionQuality(attributes.getQuality() / 100.0f);
                } else {
                    param.setCompressionQuality(1.0f);
                }
            }
            return param;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static IIOMetadata getWriterMeta(ImageAttributes attributes,
            BufferedImage image, ImageWriter writer, ImageWriteParam param) {
        try {
            GIFImageMetadata metaData;
            try {
                metaData = (GIFImageMetadata) writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
                metaData = null;
            }
            if (attributes.getDensity() > 0) {
                // Have not found the way to set density data in meta data of GIF format.
            }
            return metaData;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void writeGifImageFile(BufferedImage image, ImageAttributes attributes, String outFile) {
        try {
            ImageWriter writer = getWriter();
            ImageWriteParam param = getPara(attributes, writer);
            IIOMetadata metaData = getWriterMeta(attributes, image, writer, param);
            try ( ImageOutputStream out = ImageIO.createImageOutputStream(new File(outFile))) {
                writer.setOutput(out);
                writer.write(metaData, new IIOImage(image, null, metaData), param);
                out.flush();
            }
            writer.dispose();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static boolean getParaMeta(long duration, boolean loop, ImageWriteParam param, GIFImageMetadata metaData) {
        try {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType("LZW");
            param.setCompressionQuality(1);
            String durationV;
            if (duration > 0) {
                durationV = duration / 10 + "";
            } else {
                durationV = "100";
            }
            String format = metaData.getNativeMetadataFormatName();
            IIOMetadataNode tree = (IIOMetadataNode) metaData.getAsTree(format);
            IIOMetadataNode graphicsControlExtensionNode = new IIOMetadataNode("GraphicControlExtension");
            graphicsControlExtensionNode.setAttribute("delayTime", durationV);
            graphicsControlExtensionNode.setAttribute("disposalMethod", "restoreToBackgroundColor");
            graphicsControlExtensionNode.setAttribute("userInputFlag", "false");
            graphicsControlExtensionNode.setAttribute("transparentColorFlag", "false");
            graphicsControlExtensionNode.setAttribute("delayTime", durationV);
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
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    // https://www.programcreek.com/java-api-examples/javax.imageio.ImageWriter
    // http://www.java2s.com/Code/Java/2D-Graphics-GUI/GiffileEncoder.htm
    // https://programtalk.com/python-examples/com.sun.media.imageioimpl.plugins.gif.GIFImageWriterSpi/
    // https://www.jianshu.com/p/df52f1511cf8
    // http://giflib.sourceforge.net/whatsinagif/index.html
    public static String writeImages(List<ImageInformation> imagesInfo,
            File outFile, boolean loop, boolean keepSize, int width) {
        try {
            if (imagesInfo == null || imagesInfo.isEmpty() || outFile == null) {
                return "InvalidParameters";
            }
            System.gc();
            ImageWriter gifWriter = getWriter();
            ImageWriteParam param = gifWriter.getDefaultWriteParam();
            GIFImageMetadata metaData = (GIFImageMetadata) gifWriter.getDefaultImageMetadata(
                    ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB), param);
            File tmpFile = TmpFileTools.getTempFile();
            try ( ImageOutputStream out = ImageIO.createImageOutputStream(tmpFile)) {
                gifWriter.setOutput(out);
                gifWriter.prepareWriteSequence(null);
                for (ImageInformation info : imagesInfo) {
                    BufferedImage bufferedImage = ImageInformation.readBufferedImage(info);
//                    bufferedImage = ImageManufacture.removeAlpha(bufferedImage);
                    if (bufferedImage != null) {
                        if (!keepSize) {
                            bufferedImage = ScaleTools.scaleImageWidthKeep(bufferedImage, width);
                        }
                        getParaMeta(info.getDuration(), loop, param, metaData);
                        gifWriter.writeToSequence(new IIOImage(bufferedImage, null, metaData), param);
                    }
                }
                gifWriter.endWriteSequence();
                gifWriter.dispose();
                out.flush();
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
                return e.toString();
            }
            return FileTools.rename(tmpFile, outFile) ? "" : "Failed";
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return e.toString();
        }
    }

    public static String writeImageFiles(List<File> srcFiles, File outFile, int duration, boolean deleteSource) {
        try {
            if (srcFiles == null || srcFiles.isEmpty() || outFile == null) {
                return "InvalidParameters";
            }
            ImageWriter gifWriter = getWriter();
            ImageWriteParam param = gifWriter.getDefaultWriteParam();
            GIFImageMetadata metaData = (GIFImageMetadata) gifWriter.getDefaultImageMetadata(
                    ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB), param);
            File tmpFile = TmpFileTools.getTempFile();
            try ( ImageOutputStream out = ImageIO.createImageOutputStream(tmpFile)) {
                gifWriter.setOutput(out);

                gifWriter.prepareWriteSequence(null);
                for (File file : srcFiles) {
                    BufferedImage bufferedImage = ImageFileReaders.readImage(file);
                    if (bufferedImage != null) {
//                        bufferedImage = ImageManufacture.removeAlpha(bufferedImage);
                        getParaMeta(duration, true, param, metaData);
                        gifWriter.writeToSequence(new IIOImage(bufferedImage, null, metaData), param);
                    }
                }
                gifWriter.endWriteSequence();
                out.flush();
            }
            gifWriter.dispose();
            if (!FileTools.rename(tmpFile, outFile)) {
                return "Failed";
            }
            if (deleteSource) {
                for (File file : srcFiles) {
                    FileDeleteTools.delete(file);
                }
                srcFiles.clear();
            }
            return "";
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return e.toString();
        }
    }

    public static String writeImages(List<BufferedImage> images, File outFile, int duration) {
        try {
            if (images == null || images.isEmpty() || outFile == null) {
                return "InvalidParameters";
            }
            ImageWriter gifWriter = getWriter();
            ImageWriteParam param = gifWriter.getDefaultWriteParam();
            GIFImageMetadata metaData = (GIFImageMetadata) gifWriter.getDefaultImageMetadata(
                    ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB), param);
            File tmpFile = TmpFileTools.getTempFile();
            try ( ImageOutputStream out = ImageIO.createImageOutputStream(tmpFile)) {
                gifWriter.setOutput(out);
                gifWriter.prepareWriteSequence(null);
                for (BufferedImage bufferedImage : images) {
                    if (bufferedImage != null) {
//                        bufferedImage = ImageManufacture.removeAlpha(bufferedImage);
                        getParaMeta(duration, true, param, metaData);
                        gifWriter.writeToSequence(new IIOImage(bufferedImage, null, metaData), param);
                    }
                }
                gifWriter.endWriteSequence();
                out.flush();
            }
            gifWriter.dispose();
            return FileTools.rename(tmpFile, outFile) ? "" : "Failed";
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return e.toString();
        }
    }

    public static void explainGifMetaData(Map<String, Map<String, List<Map<String, Object>>>> metaData,
            ImageInformation info) {
        try {
            String format = "javax_imageio_gif_stream_1.0";
            if (metaData.containsKey(format)) {
                Map<String, List<Map<String, Object>>> javax_imageio_gif_stream = metaData.get(format);
                if (javax_imageio_gif_stream.containsKey("Version")) {
                    Map<String, Object> Version = javax_imageio_gif_stream.get("Version").get(0);
                    if (Version.containsKey("value")) {
                        info.setNativeAttribute("Version", (String) Version.get("value"));
                    }
                }
                if (javax_imageio_gif_stream.containsKey("LogicalScreenDescriptor")) {
                    Map<String, Object> LogicalScreenDescriptor = javax_imageio_gif_stream.get("LogicalScreenDescriptor").get(0);
                    if (LogicalScreenDescriptor.containsKey("logicalScreenWidth")) {
                        info.setNativeAttribute("logicalScreenWidth", Integer.valueOf((String) LogicalScreenDescriptor.get("logicalScreenWidth")));
                    }
                    if (LogicalScreenDescriptor.containsKey("logicalScreenHeight")) {
                        info.setNativeAttribute("logicalScreenHeight", Integer.valueOf((String) LogicalScreenDescriptor.get("logicalScreenHeight")));
                    }
                    if (LogicalScreenDescriptor.containsKey("colorResolution")) {
                        info.setNativeAttribute("colorResolution", Integer.valueOf((String) LogicalScreenDescriptor.get("colorResolution")));
                    }
                    if (LogicalScreenDescriptor.containsKey("pixelAspectRatio")) {
                        int v = Integer.valueOf((String) LogicalScreenDescriptor.get("pixelAspectRatio"));
                        if (v == 0) {
                            info.setNativeAttribute("pixelAspectRatio", 1);
                        } else {
                            info.setNativeAttribute("pixelAspectRatio", (v + 15.f) / 64);
                        }
                    }
                }
                if (javax_imageio_gif_stream.containsKey("GlobalColorTable")) {
                    Map<String, Object> GlobalColorTable = javax_imageio_gif_stream.get("GlobalColorTable").get(0);
                    if (GlobalColorTable.containsKey("sizeOfGlobalColorTable")) {
                        info.setNativeAttribute("sizeOfGlobalColorTable", Integer.valueOf((String) GlobalColorTable.get("sizeOfGlobalColorTable")));
                    }
                    if (GlobalColorTable.containsKey("backgroundColorIndex")) {
                        info.setNativeAttribute("backgroundColorIndex", Integer.valueOf((String) GlobalColorTable.get("backgroundColorIndex")));
                    }
                    if (GlobalColorTable.containsKey("sortFlag")) {
                        info.setNativeAttribute("sortFlag", (String) GlobalColorTable.get("sortFlag"));
                    }
                }
                if (javax_imageio_gif_stream.containsKey("stream_ColorTableEntry")) {
                    List<Map<String, Object>> ColorTableEntryList = javax_imageio_gif_stream.get("ColorTableEntry");
                    if (ColorTableEntryList != null) {
                        info.setNativeAttribute("stream_ColorTableEntryList", ColorTableEntryList.size());  // Extract data if need in future
                    }
                }
            }

            format = "javax_imageio_gif_image_1.0";
            if (metaData.containsKey(format)) {
                Map<String, List<Map<String, Object>>> javax_imageio_gif_image = metaData.get(format);
                if (javax_imageio_gif_image.containsKey("Version")) {
                    Map<String, Object> ImageDescriptor = javax_imageio_gif_image.get("ImageDescriptor").get(0);
                    if (ImageDescriptor.containsKey("imageLeftPosition")) {
                        info.setNativeAttribute("imageLeftPosition", (String) ImageDescriptor.get("imageLeftPosition"));
                    }
                    if (ImageDescriptor.containsKey("imageTopPosition")) {
                        info.setNativeAttribute("imageTopPosition", (String) ImageDescriptor.get("imageTopPosition"));
                    }
                    if (ImageDescriptor.containsKey("imageWidth")) {
                        info.setNativeAttribute("imageWidth", (String) ImageDescriptor.get("imageWidth"));
                    }
                    if (ImageDescriptor.containsKey("imageHeight")) {
                        info.setNativeAttribute("imageHeight", (String) ImageDescriptor.get("imageHeight"));
                    }
                    if (ImageDescriptor.containsKey("interlaceFlag")) {
                        info.setNativeAttribute("interlaceFlag", (String) ImageDescriptor.get("interlaceFlag"));
                    }
                }
                if (javax_imageio_gif_image.containsKey("ColorTableEntry")) {
                    List<Map<String, Object>> ColorTableEntryList = javax_imageio_gif_image.get("ColorTableEntry");
                    if (ColorTableEntryList != null) {
                        info.setNativeAttribute("ColorTableEntryList", ColorTableEntryList.size()); // Extract data if need in future
                    }
                }
                if (javax_imageio_gif_image.containsKey("GraphicControlExtension")) {
                    Map<String, Object> GraphicControlExtension = javax_imageio_gif_image.get("GraphicControlExtension").get(0);
                    if (GraphicControlExtension.containsKey("disposalMethod")) {
                        info.setNativeAttribute("disposalMethod", (String) GraphicControlExtension.get("disposalMethod"));
                    }
                    if (GraphicControlExtension.containsKey("userInputFlag")) {
                        info.setNativeAttribute("userInputFlag", (String) GraphicControlExtension.get("userInputFlag"));
                    }
                    if (GraphicControlExtension.containsKey("transparentColorFlag")) {
                        info.setNativeAttribute("transparentColorFlag", (String) GraphicControlExtension.get("transparentColorFlag"));
                    }
                    if (GraphicControlExtension.containsKey("delayTime")) {   // in hundredths of a second
                        info.setNativeAttribute("delayTime", GraphicControlExtension.get("delayTime"));
                        try {
                            int v = Integer.valueOf((String) GraphicControlExtension.get("delayTime"));
                            info.setDuration(v * 10);
                        } catch (Exception e) {
                        }
                    }
                    if (GraphicControlExtension.containsKey("transparentColorIndex")) {
                        info.setNativeAttribute("transparentColorIndex", (String) GraphicControlExtension.get("transparentColorIndex"));
                    }
                }
                if (javax_imageio_gif_image.containsKey("PlainTextExtension")) {
                    Map<String, Object> PlainTextExtension = javax_imageio_gif_image.get("PlainTextExtension").get(0);
                    if (PlainTextExtension.containsKey("textGridLeft")) {
                        info.setNativeAttribute("textGridLeft", PlainTextExtension.get("textGridLeft"));
                    }
                    if (PlainTextExtension.containsKey("textGridTop")) {
                        info.setNativeAttribute("textGridTop", PlainTextExtension.get("textGridTop"));
                    }
                    if (PlainTextExtension.containsKey("textGridWidth")) {
                        info.setNativeAttribute("textGridWidth", PlainTextExtension.get("textGridWidth"));
                    }
                    if (PlainTextExtension.containsKey("textGridHeight")) {
                        info.setNativeAttribute("textGridHeight", PlainTextExtension.get("textGridHeight"));
                    }
                    if (PlainTextExtension.containsKey("characterCellWidth")) {
                        info.setNativeAttribute("characterCellWidth", PlainTextExtension.get("characterCellWidth"));
                    }
                    if (PlainTextExtension.containsKey("characterCellHeight")) {
                        info.setNativeAttribute("characterCellHeight", PlainTextExtension.get("characterCellHeight"));
                    }
                    if (PlainTextExtension.containsKey("textForegroundColor")) {
                        info.setNativeAttribute("textForegroundColor", PlainTextExtension.get("textForegroundColor"));
                    }
                    if (PlainTextExtension.containsKey("textBackgroundColor")) {
                        info.setNativeAttribute("textBackgroundColor", PlainTextExtension.get("textBackgroundColor"));
                    }
                }
                if (javax_imageio_gif_image.containsKey("ApplicationExtensions")) {
                    Map<String, Object> ApplicationExtensions = javax_imageio_gif_image.get("ApplicationExtensions").get(0);
                    if (ApplicationExtensions.containsKey("applicationID")) {
                        info.setNativeAttribute("applicationID", ApplicationExtensions.get("applicationID"));
                    }
                    if (ApplicationExtensions.containsKey("authenticationCode")) {
                        info.setNativeAttribute("authenticationCode", ApplicationExtensions.get("authenticationCode"));
                    }
                }
                if (javax_imageio_gif_image.containsKey("CommentExtensions")) {
                    Map<String, Object> CommentExtensions = javax_imageio_gif_image.get("CommentExtensions").get(0);
                    if (CommentExtensions.containsKey("value")) {
                        info.setNativeAttribute("CommentExtensions", CommentExtensions.get("value"));
                    }

                }
            }

        } catch (Exception e) {

        }
    }

}
