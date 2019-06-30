package mara.mybox.image.file;

import java.awt.color.ColorSpace;
import mara.mybox.image.ImageValue;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.image.ImageAttributes;
import mara.mybox.image.ImageConvert;
import mara.mybox.image.ImageInformation;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.value.AppVaribles.logger;

import org.w3c.dom.Element;

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

    public static BufferedImage readBrokenJpgFile(File file, ImageInformation imageInfo,
            int index, int xscale, int yscale) {
        BufferedImage bufferedImage = null;
        try {
            if (imageInfo.getColorChannels() != 4) {
                return null;
            }
            try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
                ImageReader reader = null;
                while (readers.hasNext()) {
                    reader = (ImageReader) readers.next();
                    if (reader.canReadRaster()) {
                        break;
                    }
                }
                if (reader != null) {
                    reader.setInput(iis);
                    ImageReadParam param = reader.getDefaultReadParam();
                    param.setSourceSubsampling(xscale, yscale, 0, 0);
                    WritableRaster srcRaster = (WritableRaster) reader.readRaster(index, param);
                    boolean isAdobe = imageInfo.getBooleanAttribute("Adobe");
                    if ("YCCK".equals(imageInfo.getColorSpace())) {
                        ImageConvert.ycck2cmyk(srcRaster, isAdobe);
                    } else if (isAdobe) {
                        ImageConvert.invert(srcRaster);
                    }
                    bufferedImage = new BufferedImage(srcRaster.getWidth(), srcRaster.getHeight(), BufferedImage.TYPE_INT_RGB);
                    WritableRaster rgbRaster = bufferedImage.getRaster();
                    ColorSpace cmykCS;
                    if (isAdobe) {
                        cmykCS = ImageConvert.adobeCmykColorSpace();
                    } else {
                        cmykCS = ImageConvert.eciCmykColorSpace();
                    }
                    ColorSpace rgbCS = bufferedImage.getColorModel().getColorSpace();
                    ColorConvertOp cmykToRgb = new ColorConvertOp(cmykCS, rgbCS, null);
                    cmykToRgb.filter(srcRaster, rgbRaster);
                }
            }
        } catch (Exception ex) {
            logger.error(ex.toString());
        }
        return bufferedImage;
    }

    // https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/jpeg_metadata.html#image
    public static boolean writeJPEGImageFile(BufferedImage image,
            ImageAttributes attributes, File file) {
        try {
            try {
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                return false;
            }
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
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
//            logger.debug(param.getCompressionQuality());

            IIOMetadata metaData;
            try {
                metaData = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
                if (attributes != null && attributes.getDensity() > 0) {
                    String format = metaData.getNativeMetadataFormatName(); // "javax_imageio_jpeg_image_1.0"
                    Element tree = (Element) metaData.getAsTree(format);
                    Element jfif = (Element) tree.getElementsByTagName("app0JFIF").item(0);
                    jfif.setAttribute("Xdensity", attributes.getDensity() + "");
                    jfif.setAttribute("Ydensity", attributes.getDensity() + "");
                    jfif.setAttribute("resUnits", "1"); // density is dots per inch
                    metaData.mergeTree(format, tree);
                }
            } catch (Exception e) {
                logger.error(e.toString());
                metaData = null;
            }

            try (ImageOutputStream out = ImageIO.createImageOutputStream(file)) {
                writer.setOutput(out);
                writer.write(metaData, new IIOImage(image, null, metaData), param);
                out.flush();
            }
            writer.dispose();
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    public static void explainJpegMetaData(Map<String, Map<String, List<Map<String, String>>>> metaData,
            ImageInformation info) {
        try {
            if (!metaData.containsKey("javax_imageio_jpeg_image_1.0")) {
                return;
            }
//            logger.debug("explainPngMetaData");
            Map<String, List<Map<String, String>>> javax_imageio_jpeg = metaData.get("javax_imageio_jpeg_image_1.0");
            if (javax_imageio_jpeg.containsKey("app0JFIF")) {
                Map<String, String> app0JFIF = javax_imageio_jpeg.get("app0JFIF").get(0);
                if (app0JFIF.containsKey("majorVersion")) {
                    info.setAttribute("majorVersion", Integer.valueOf(app0JFIF.get("majorVersion")));
                }
                if (app0JFIF.containsKey("minorVersion")) {
                    info.setAttribute("minorVersion", Integer.valueOf(app0JFIF.get("minorVersion")));
                }
                if (app0JFIF.containsKey("resUnits")) {
                    int u = Integer.valueOf(app0JFIF.get("resUnits"));
                    switch (u) {
                        case 1:
                            info.setAttribute("resUnits", getMessage("DotsPerInch"));
                            break;
                        case 2:
                            info.setAttribute("resUnits", getMessage("DotsPerCentimeter"));
                            break;
//                        case 0:
//                        default:
//                            info.setAttribute("resUnits", getMessage("None"));
                    }
                    boolean isDpi = u == 1;
                    if (app0JFIF.containsKey("Xdensity")) {
                        int v = Integer.valueOf(app0JFIF.get("Xdensity"));
                        info.setAttribute("Xdensity", v);
                        if (!isDpi) {
                            info.setXDpi(ImageValue.dpi2dpcm(v));  // density value should be dpi
                        } else {
                            info.setXDpi(v);
                        }
                    }
                    if (app0JFIF.containsKey("Ydensity")) {
                        int v = Integer.valueOf(app0JFIF.get("Ydensity"));
                        info.setAttribute("Ydensity", v);
                        if (!isDpi) {
                            info.setYDpi(ImageValue.dpi2dpcm(v));  // density value should be dpi
                        } else {
                            info.setYDpi(v);
                        }
                    }
                }
                if (app0JFIF.containsKey("thumbWidth")) {
                    info.setAttribute("thumbWidth", Integer.valueOf(app0JFIF.get("thumbWidth")));
                }
                if (app0JFIF.containsKey("thumbHeight")) {
                    info.setAttribute("thumbHeight", Integer.valueOf(app0JFIF.get("thumbHeight")));
                }
            }
            if (javax_imageio_jpeg.containsKey("app0JFXX")) {
                Map<String, String> app0JFXX = javax_imageio_jpeg.get("app0JFXX").get(0);
                if (app0JFXX.containsKey("extensionCode")) {
                    info.setAttribute("extensionCode", app0JFXX.get("extensionCode"));
                }
            }

            if (javax_imageio_jpeg.containsKey("JFIFthumbPalette")) {
                Map<String, String> JFIFthumbPalette = javax_imageio_jpeg.get("JFIFthumbPalette").get(0);
                if (JFIFthumbPalette.containsKey("thumbWidth")) {
                    info.setAttribute("thumbWidth", JFIFthumbPalette.get("thumbWidth"));
                }
                if (JFIFthumbPalette.containsKey("thumbHeight")) {
                    info.setAttribute("thumbHeight", JFIFthumbPalette.get("thumbHeight"));
                }
            }

            if (javax_imageio_jpeg.containsKey("JFIFthumbRGB")) {
                Map<String, String> JFIFthumbRGB = javax_imageio_jpeg.get("JFIFthumbRGB").get(0);
                if (JFIFthumbRGB.containsKey("thumbWidth")) {
                    info.setAttribute("thumbWidth", JFIFthumbRGB.get("thumbWidth"));
                }
                if (JFIFthumbRGB.containsKey("thumbHeight")) {
                    info.setAttribute("thumbHeight", JFIFthumbRGB.get("thumbHeight"));
                }
            }

            if (javax_imageio_jpeg.containsKey("app14Adobe")) {
                info.setAttribute("Adobe", true);
                Map<String, String> app14Adobe = javax_imageio_jpeg.get("app14Adobe").get(0);
                if (app14Adobe.containsKey("version")) {
                    info.setAttribute("AdobeVersion", app14Adobe.get("version"));
                }
                if (app14Adobe.containsKey("flags0")) {
                    info.setAttribute("AdobeFlags0", app14Adobe.get("flags0"));
                }
                if (app14Adobe.containsKey("flags1")) {
                    info.setAttribute("AdobeFlags1", app14Adobe.get("flags1"));
                }
                /*
                    https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/jpeg_metadata.html#image
                    2 - The image is encoded as YCCK (implicitly converted from CMYK on encoding).
                    1 - The image is encoded as YCbCr (implicitly converted from RGB on encoding).
                    0 - Unknown. 3-channel images are assumed to be RGB, 4-channel images are assumed to be CMYK.
                 */
                if (app14Adobe.containsKey("transform")) {
                    info.setAttribute("AdobeTransform", app14Adobe.get("transform"));
                }

            }

//            if (javax_imageio_jpeg.containsKey("sof")) {
//                Map<String, String> sof = javax_imageio_jpeg.get("sof").get(0);
//                if (sof.containsKey("numLines")) {
//                    info.setHeight(Integer.valueOf(sof.get("numLines")));
//                }
//                if (sof.containsKey("samplesPerLine")) {
//                    info.setWidth(Integer.valueOf(sof.get("samplesPerLine")));
//                }
//                if (sof.containsKey("samplePrecision")) {
//                    info.setBitDepth(sof.get("samplePrecision"));
//                }
//            }
        } catch (Exception e) {

        }
    }

    public static void rgbToCmyk(InputStream inputStream, String fileName, String... newFileName) throws IOException {
//        BufferedImage rgbImage = ImageIO.read(inputStream);
//        BufferedImage cmykImage = null;
//        ColorSpace cpace = new ICC_ColorSpace(ICC_Profile.getInstance(TestImageBinary.class.getClassLoader().getResourceAsStream("ISOcoated_v2_300_eci.icc")));
//        ColorConvertOp op = new ColorConvertOp(rgbImage.getColorModel().getColorSpace(), cpace, null);
//        cmykImage = op.filter(rgbImage, null);
//        if (newFileName.length > 0 && newFileName[0] != null) {
//            String targetFileName = fileName.substring(0, fileName.lastIndexOf(".") + 1) + "tif";
//            JAI.create("filestore", cmykImage, targetFileName, "TIFF");
//            cmykImage.flush();
//            base64StringToImage(targetFileName, newFileName[0]);//转成对应格式
//            File file = new File(fileName);
//            if (file.exists()) {
//                file.delete();
//            }
//        } else {
//            String targetFileName = fileName.substring(0, fileName.lastIndexOf(".") + 1) + "tif";
//            JAI.create("filestore", cmykImage, targetFileName, "TIFF");
//        }
    }

    public static void rgbToCmyk(String fileName, String... format) throws IOException {
//        BufferedImage rgbImage = ImageIO.read(new File(fileName));
//        BufferedImage cmykImage = null;
//        ColorSpace cpace = new ICC_ColorSpace(ICC_Profile.getInstance(TestImageBinary.class.getClassLoader().getResourceAsStream("common/ISOcoated_v2_300_eci.icc")));
//        ColorConvertOp op = new ColorConvertOp(rgbImage.getColorModel().getColorSpace(), cpace, null);
//        cmykImage = op.filter(rgbImage, null);
//        String newFileName = null;
//        newFileName = fileName.substring(0, fileName.lastIndexOf("."));
//        if (format.length > 0) {
//            JAI.create("filestore", cmykImage, newFileName + format[0], format[0]);
//        } else {
//            JAI.create("filestore", cmykImage, newFileName + "tif", "TIFF");
//        }
    }

    static void base64StringToImage(String sourceFileName, String newFileName) {
//        try {
//            String base64String = getImageBinary(sourceFileName);
//            byte[] bytes1 = decoder.decodeBuffer(base64String);
//            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes1);
//            BufferedImage bi1 = ImageIO.read(inputStream);
//            File w2 = new File(newFileName);//可以是jpg,png,gif格式
//            ImageIO.write(bi1, getFileType(sourceFileName), w2);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

}
