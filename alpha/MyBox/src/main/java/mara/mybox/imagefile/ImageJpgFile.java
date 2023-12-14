package mara.mybox.imagefile;

import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.bufferedimage.ImageAttributes;
import mara.mybox.bufferedimage.ImageColorSpace;
import mara.mybox.bufferedimage.ImageConvertTools;
import mara.mybox.bufferedimage.ImageFileInformation;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.Languages;
import org.w3c.dom.NodeList;

/**
 * @Author Mara
 * @CreateDate 2018-6-19
 *
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageJpgFile {

    public static String[] getJpegCompressionTypes() {
        return new JPEGImageWriteParam(null).getCompressionTypes();
    }

    public static BufferedImage readBrokenJpgFile(FxTask task, ImageInformation imageInfo) {
        if (imageInfo == null) {
            return null;
        }
        File file = imageInfo.getFile();
        try (ImageInputStream iis = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            ImageReader reader = null;
            while (readers.hasNext()) {
                reader = readers.next();
                if (reader.canReadRaster()) {
                    break;
                }
            }
            if (reader == null) {
                iis.close();
                return null;
            }
            reader.setInput(iis);

            ImageFileInformation fileInfo = new ImageFileInformation(file);
            ImageFileReaders.readImageFileMetaData(task, reader, fileInfo);
            if (task != null && !task.isWorking()) {
                return null;
            }
            ImageInformation fileImageInfo = fileInfo.getImagesInformation().get(imageInfo.getIndex());

            imageInfo.setWidth(fileImageInfo.getWidth());
            imageInfo.setHeight(fileImageInfo.getHeight());
            imageInfo.setColorSpace(fileImageInfo.getColorSpace());
            imageInfo.setColorChannels(fileImageInfo.getColorChannels());
            imageInfo.setNativeAttribute("Adobe", fileImageInfo.getNativeAttribute("Adobe"));

            BufferedImage bufferedImage = readBrokenJpgFile(task, reader, imageInfo);
            if (task != null && !task.isWorking()) {
                return null;
            }
            int requiredWidth = (int) imageInfo.getRequiredWidth();
            if (requiredWidth > 0 && bufferedImage.getWidth() != requiredWidth) {
                bufferedImage = ScaleTools.scaleImageWidthKeep(bufferedImage, requiredWidth);
            }
            return bufferedImage;
        } catch (Exception ex) {
            imageInfo.setError(ex.toString());
            return null;
        }
    }

    private static BufferedImage readBrokenJpgFile(FxTask task, ImageReader reader, ImageInformation imageInfo) {
        if (reader == null || imageInfo == null || imageInfo.getColorChannels() != 4) {
            return null;
        }
        BufferedImage bufferedImage = null;
        try {
            ImageReadParam param = reader.getDefaultReadParam();
            Rectangle region = imageInfo.getIntRegion();
            if (region != null) {
                param.setSourceRegion(region);
            }
            int xscale = imageInfo.getXscale();
            int yscale = imageInfo.getYscale();
            if (xscale != 1 || yscale != 1) {
                param.setSourceSubsampling(xscale, yscale, 0, 0);
            } else {
                ImageInformation.checkMem(task, imageInfo);
                int sampleScale = imageInfo.getSampleScale();
                if (sampleScale > 1) {
                    param.setSourceSubsampling(sampleScale, sampleScale, 0, 0);
                }
            }
            if (task != null && !task.isWorking()) {
                return null;
            }
            WritableRaster srcRaster = (WritableRaster) reader.readRaster(imageInfo.getIndex(), param);
            if (task != null && !task.isWorking()) {
                return null;
            }
            boolean isAdobe = (boolean) imageInfo.getNativeAttribute("Adobe");
            if ("YCCK".equals(imageInfo.getColorSpace())) {
                ImageConvertTools.ycck2cmyk(task, srcRaster, isAdobe);
            } else if (isAdobe) {
                ImageConvertTools.invertPixelValue(task, srcRaster);
            }
            if (task != null && !task.isWorking()) {
                return null;
            }
            bufferedImage = new BufferedImage(srcRaster.getWidth(), srcRaster.getHeight(), BufferedImage.TYPE_INT_RGB);
            WritableRaster rgbRaster = bufferedImage.getRaster();
            ColorSpace cmykCS;
            if (isAdobe) {
                cmykCS = ImageColorSpace.adobeCmykColorSpace();
            } else {
                cmykCS = ImageColorSpace.eciCmykColorSpace();
            }
            ColorSpace rgbCS = bufferedImage.getColorModel().getColorSpace();
            ColorConvertOp cmykToRgb = new ColorConvertOp(cmykCS, rgbCS, null);
            if (task != null && !task.isWorking()) {
                return null;
            }
            cmykToRgb.filter(srcRaster, rgbRaster);

        } catch (Exception ex) {
            imageInfo.setError(ex.toString());
        }
        return bufferedImage;
    }

    public static ImageWriter getWriter() {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
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

    // https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/jpeg_metadata.html#image
    public static IIOMetadata getWriterMeta2(ImageAttributes attributes, BufferedImage image,
            ImageWriter writer, ImageWriteParam param) {
        try {

            IIOMetadata metaData = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
            if (metaData == null || metaData.isReadOnly() || attributes == null) {
                return metaData;
            }
            String nativeFormat = metaData.getNativeMetadataFormatName();// "javax_imageio_jpeg_image_1.0"

            IIOMetadataNode root = new IIOMetadataNode(nativeFormat);
            IIOMetadataNode jpegVariety = new IIOMetadataNode("JPEGvariety");
            IIOMetadataNode markerSequence = new IIOMetadataNode("markerSequence");
            IIOMetadataNode app0JFIF = new IIOMetadataNode("app0JFIF");
            root.appendChild(jpegVariety);
            root.appendChild(markerSequence);
            jpegVariety.appendChild(app0JFIF);

            app0JFIF.setAttribute("majorVersion", "1");
            app0JFIF.setAttribute("minorVersion", "2");
            app0JFIF.setAttribute("thumbWidth", "0");
            app0JFIF.setAttribute("thumbHeight", "0");

            if (attributes.getDensity() > 0) {
                app0JFIF.setAttribute("Xdensity", attributes.getDensity() + "");
                app0JFIF.setAttribute("Ydensity", attributes.getDensity() + "");
                app0JFIF.setAttribute("resUnits", "1"); // density is dots per inch
            }

            if (attributes.isEmbedProfile() && attributes.getProfile() != null) {
                IIOMetadataNode app2ICC = new IIOMetadataNode("app2ICC");
                app0JFIF.appendChild(app2ICC);
                app2ICC.setUserObject(attributes.getProfile());
            }

            metaData.mergeTree(nativeFormat, root);
            return metaData;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static IIOMetadata getWriterMeta(ImageAttributes attributes, BufferedImage image,
            ImageWriter writer, ImageWriteParam param) {
        try {
            IIOMetadata metaData = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
            if (metaData == null || metaData.isReadOnly() || attributes == null) {
                return metaData;
            }
            String nativeFormat = metaData.getNativeMetadataFormatName();// "javax_imageio_jpeg_image_1.0"
            IIOMetadataNode nativeTree = (IIOMetadataNode) metaData.getAsTree(nativeFormat);
            if (nativeTree == null) {
                return metaData;
            }

            IIOMetadataNode JPEGvariety, markerSequence;
            NodeList JPEGvarietyNode = nativeTree.getElementsByTagName("JPEGvariety");
            if (JPEGvarietyNode != null && JPEGvarietyNode.getLength() > 0) {
                JPEGvariety = (IIOMetadataNode) JPEGvarietyNode.item(0);
            } else {
                JPEGvariety = new IIOMetadataNode("JPEGvariety");
                nativeTree.appendChild(JPEGvariety);
            }
            NodeList markerSequenceNode = nativeTree.getElementsByTagName("markerSequence");
            if (markerSequenceNode == null) {
                markerSequence = new IIOMetadataNode("markerSequence");
                nativeTree.appendChild(markerSequence);
            }
            IIOMetadataNode app0JFIF;
            NodeList app0JFIFNode = nativeTree.getElementsByTagName("app0JFIF");
            if (app0JFIFNode != null && app0JFIFNode.getLength() > 0) {
                app0JFIF = (IIOMetadataNode) app0JFIFNode.item(0);
            } else {
                app0JFIF = new IIOMetadataNode("app0JFIF");
                JPEGvariety.appendChild(app0JFIF);
            }

            if (attributes.getDensity() > 0) {
                app0JFIF.setAttribute("Xdensity", attributes.getDensity() + "");
                app0JFIF.setAttribute("Ydensity", attributes.getDensity() + "");
                app0JFIF.setAttribute("resUnits", "1"); // density is dots per inch
            }

            if (attributes.isEmbedProfile() && attributes.getProfile() != null) {
                IIOMetadataNode app2ICC;
                NodeList app2ICCNode = nativeTree.getElementsByTagName("app2ICC");
                if (app2ICCNode != null && app2ICCNode.getLength() > 0) {
                    app2ICC = (IIOMetadataNode) app2ICCNode.item(0);
                } else {
                    app2ICC = new IIOMetadataNode("app2ICC");
                    app0JFIF.appendChild(app2ICC);
                }
                app2ICC.setUserObject(attributes.getProfile());
            }

            metaData.mergeTree(nativeFormat, nativeTree);
            return metaData;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static boolean writeJPEGImageFile(BufferedImage image,
            ImageAttributes attributes, File file) {
        try {
            ImageWriter writer = getWriter();
            ImageWriteParam param = getPara(attributes, writer);
            IIOMetadata metaData = getWriterMeta(attributes, image, writer, param);
            File tmpFile = FileTmpTools.getTempFile();
            try (ImageOutputStream out = ImageIO.createImageOutputStream(tmpFile)) {
                writer.setOutput(out);
                writer.write(metaData, new IIOImage(image, null, metaData), param);
                out.flush();
            }
            writer.dispose();
            return FileTools.override(tmpFile, file);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public static void explainJpegMetaData(Map<String, Map<String, List<Map<String, Object>>>> metaData,
            ImageInformation info) {
        try {
            if (!metaData.containsKey("javax_imageio_jpeg_image_1.0")) {
                return;
            }
            Map<String, List<Map<String, Object>>> javax_imageio_jpeg = metaData.get("javax_imageio_jpeg_image_1.0");
            if (javax_imageio_jpeg.containsKey("app0JFIF")) {
                Map<String, Object> app0JFIF = javax_imageio_jpeg.get("app0JFIF").get(0);
                if (app0JFIF.containsKey("majorVersion")) {
                    info.setNativeAttribute("majorVersion", Integer.parseInt((String) app0JFIF.get("majorVersion")));
                }
                if (app0JFIF.containsKey("minorVersion")) {
                    info.setNativeAttribute("minorVersion", Integer.parseInt((String) app0JFIF.get("minorVersion")));
                }
                if (app0JFIF.containsKey("resUnits")) {
                    int u = Integer.parseInt((String) app0JFIF.get("resUnits"));
                    info.setNativeAttribute("resUnits", u);
                    switch (u) {
                        case 1:
                            info.setNativeAttribute("resUnits", Languages.message("DotsPerInch"));
                            break;
                        case 2:
                            info.setNativeAttribute("resUnits", Languages.message("DotsPerCentimeter"));
                            break;
                        case 0:
                            info.setNativeAttribute("resUnits", Languages.message("None"));
                    }
                    if (app0JFIF.containsKey("Xdensity")) {
                        int v = Integer.parseInt((String) app0JFIF.get("Xdensity"));
                        info.setNativeAttribute("Xdensity", v);
                        if (u == 2) {
                            info.setXDpi(ImageConvertTools.dpi2dpcm(v));  // density value should be dpi
                        } else if (u == 1) {
                            info.setXDpi(v);
                        }
                    }
                    if (app0JFIF.containsKey("Ydensity")) {
                        int v = Integer.parseInt((String) app0JFIF.get("Ydensity"));
                        info.setNativeAttribute("Ydensity", v);
                        if (u == 2) {
                            info.setYDpi(ImageConvertTools.dpi2dpcm(v));  // density value should be dpi
                        } else if (u == 1) {
                            info.setYDpi(v);
                        }
                    }
                }
                if (app0JFIF.containsKey("thumbWidth")) {
                    info.setNativeAttribute("thumbWidth", Integer.parseInt((String) app0JFIF.get("thumbWidth")));
                }
                if (app0JFIF.containsKey("thumbHeight")) {
                    info.setNativeAttribute("thumbHeight", Integer.parseInt((String) app0JFIF.get("thumbHeight")));
                }
            }
            if (javax_imageio_jpeg.containsKey("app0JFXX")) {
                Map<String, Object> app0JFXX = javax_imageio_jpeg.get("app0JFXX").get(0);
                if (app0JFXX.containsKey("extensionCode")) {
                    info.setNativeAttribute("extensionCode", app0JFXX.get("extensionCode"));
                }
            }

            if (javax_imageio_jpeg.containsKey("JFIFthumbPalette")) {
                Map<String, Object> JFIFthumbPalette = javax_imageio_jpeg.get("JFIFthumbPalette").get(0);
                if (JFIFthumbPalette.containsKey("thumbWidth")) {
                    info.setNativeAttribute("thumbWidth", JFIFthumbPalette.get("thumbWidth"));
                }
                if (JFIFthumbPalette.containsKey("thumbHeight")) {
                    info.setNativeAttribute("thumbHeight", JFIFthumbPalette.get("thumbHeight"));
                }
            }

            if (javax_imageio_jpeg.containsKey("JFIFthumbRGB")) {
                Map<String, Object> JFIFthumbRGB = javax_imageio_jpeg.get("JFIFthumbRGB").get(0);
                if (JFIFthumbRGB.containsKey("thumbWidth")) {
                    info.setNativeAttribute("thumbWidth", JFIFthumbRGB.get("thumbWidth"));
                }
                if (JFIFthumbRGB.containsKey("thumbHeight")) {
                    info.setNativeAttribute("thumbHeight", JFIFthumbRGB.get("thumbHeight"));
                }
            }

            if (javax_imageio_jpeg.containsKey("app2ICC")) {
                Map<String, Object> app2ICC = javax_imageio_jpeg.get("app2ICC").get(0);
                ICC_Profile p = (ICC_Profile) app2ICC.get("UserObject");
                info.setIccProfile(p.getData());
            }

            if (javax_imageio_jpeg.containsKey("dqtable")) {
                Map<String, Object> dqtable = javax_imageio_jpeg.get("dqtable").get(0);
                if (dqtable.containsKey("elementPrecision")) {
                    info.setNativeAttribute("dqtableElementPrecision", dqtable.get("elementPrecision"));
                }
                if (dqtable.containsKey("qtableId")) {
                    info.setNativeAttribute("qtableId", dqtable.get("qtableId"));
                }
            }

            if (javax_imageio_jpeg.containsKey("dhtable")) {
                Map<String, Object> dhtable = javax_imageio_jpeg.get("dhtable").get(0);
                if (dhtable.containsKey("class")) {
                    info.setNativeAttribute("dhtableClass", dhtable.get("class"));
                }
                if (dhtable.containsKey("htableId")) {
                    info.setNativeAttribute("htableId", dhtable.get("htableId"));
                }
            }

            if (javax_imageio_jpeg.containsKey("dri")) {
                Map<String, Object> dri = javax_imageio_jpeg.get("dri").get(0);
                if (dri.containsKey("interval")) {
                    info.setNativeAttribute("driInterval", dri.get("interval"));
                }
            }

            if (javax_imageio_jpeg.containsKey("com")) {
                Map<String, Object> com = javax_imageio_jpeg.get("com").get(0);
                if (com.containsKey("comment")) {
                    info.setNativeAttribute("comment", com.get("comment"));
                }
            }

            if (javax_imageio_jpeg.containsKey("unknown")) {
                Map<String, Object> unknown = javax_imageio_jpeg.get("unknown").get(0);
                if (unknown.containsKey("MarkerTag")) {
                    info.setNativeAttribute("unknownMarkerTag", unknown.get("MarkerTag"));
                }
            }

            if (javax_imageio_jpeg.containsKey("app14Adobe")) {
                info.setNativeAttribute("Adobe", true);
                Map<String, Object> app14Adobe = javax_imageio_jpeg.get("app14Adobe").get(0);
                if (app14Adobe.containsKey("version")) {
                    info.setNativeAttribute("AdobeVersion", app14Adobe.get("version"));
                }
                if (app14Adobe.containsKey("flags0")) {
                    info.setNativeAttribute("AdobeFlags0", app14Adobe.get("flags0"));
                }
                if (app14Adobe.containsKey("flags1")) {
                    info.setNativeAttribute("AdobeFlags1", app14Adobe.get("flags1"));
                }
                /*
                    https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/jpeg_metadata.html#image
                    2 - The image is encoded as YCCK (implicitly converted from CMYK on encoding).
                    1 - The image is encoded as YCbCr (implicitly converted from RGB on encoding).
                    0 - Unknown. 3-channel images are assumed to be RGB, 4-channel images are assumed to be CMYK.
                 */
                if (app14Adobe.containsKey("transform")) {
                    info.setNativeAttribute("AdobeTransform", app14Adobe.get("transform"));
                }
            }

            if (javax_imageio_jpeg.containsKey("sof")) {
                Map<String, Object> sof = javax_imageio_jpeg.get("sof").get(0);
                if (sof.containsKey("process")) {
                    info.setNativeAttribute("FrameProcess", sof.get("process"));
                }
                if (sof.containsKey("samplePrecision")) {
                    info.setNativeAttribute("FrameSamplePrecision", sof.get("samplePrecision"));
                }
                if (sof.containsKey("numLines")) {
                    info.setNativeAttribute("FrameNumLines", sof.get("numLines"));
                }
                if (sof.containsKey("samplesPerLine")) {
                    info.setNativeAttribute("FrameSamplesPerLine", sof.get("samplesPerLine"));
                }
                if (sof.containsKey("numFrameComponents")) {
                    info.setNativeAttribute("FrameNumFrameComponents", sof.get("numFrameComponents"));
                }
            }

            if (javax_imageio_jpeg.containsKey("componentSpec")) {
                Map<String, Object> componentSpec = javax_imageio_jpeg.get("componentSpec").get(0);
                if (componentSpec.containsKey("componentId")) {
                    info.setNativeAttribute("FrameComponentId", componentSpec.get("componentId"));
                }
                if (componentSpec.containsKey("HsamplingFactor")) {
                    info.setNativeAttribute("FrameHsamplingFactor", componentSpec.get("HsamplingFactor"));
                }
                if (componentSpec.containsKey("VsamplingFactor")) {
                    info.setNativeAttribute("FrameVsamplingFactor", componentSpec.get("VsamplingFactor"));
                }
                if (componentSpec.containsKey("QtableSelector")) {
                    info.setNativeAttribute("FrameQtableSelector", componentSpec.get("QtableSelector"));
                }
            }

            if (javax_imageio_jpeg.containsKey("sos")) {
                Map<String, Object> sos = javax_imageio_jpeg.get("sos").get(0);
                if (sos.containsKey("numScanComponents")) {
                    info.setNativeAttribute("NumScanComponents", sos.get("numScanComponents"));
                }
                if (sos.containsKey("startSpectralSelection")) {
                    info.setNativeAttribute("ScanStartSpectralSelection", sos.get("startSpectralSelection"));
                }
                if (sos.containsKey("endSpectralSelection")) {
                    info.setNativeAttribute("ScanEndSpectralSelection", sos.get("endSpectralSelection"));
                }
                if (sos.containsKey("approxHigh")) {
                    info.setNativeAttribute("ScanApproxHigh", sos.get("approxHigh"));
                }
                if (sos.containsKey("approxLow")) {
                    info.setNativeAttribute("ScanApproxLow", sos.get("approxLow"));
                }
            }

            if (javax_imageio_jpeg.containsKey("scanComponentSpec")) {
                Map<String, Object> scanComponentSpec = javax_imageio_jpeg.get("scanComponentSpec").get(0);
                if (scanComponentSpec.containsKey("componentSelector")) {
                    info.setNativeAttribute("ScanComponentSelector", scanComponentSpec.get("componentSelector"));
                }
                if (scanComponentSpec.containsKey("dcHuffTable")) {
                    info.setNativeAttribute("ScanDcHuffTable", scanComponentSpec.get("dcHuffTable"));
                }
                if (scanComponentSpec.containsKey("acHuffTable")) {
                    info.setNativeAttribute("ScanAcHuffTable", scanComponentSpec.get("acHuffTable"));
                }

            }
        } catch (Exception e) {

        }
    }

}
