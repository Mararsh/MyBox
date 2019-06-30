package mara.mybox.image.file;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.color.CIEData;
import mara.mybox.image.ImageAttributes;
import mara.mybox.image.ImageColor;
import static mara.mybox.image.ImageValue.dpi2dpm;
import static mara.mybox.image.ImageValue.dpm2dpi;
import mara.mybox.image.ImageInformation;
import mara.mybox.image.ImageInformationPng;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-19
 *
 * @Description
 * @License Apache License Version 2.0
 */
public class ImagePngFile {

    // https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/png_metadata.html#image
    public static boolean writePNGImageFile(BufferedImage image,
            ImageAttributes attributes, File file) {
        try {
            try {
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                return false;
            }
            ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
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

            IIOMetadata metaData;
            try {
                metaData = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
                if (metaData != null && !metaData.isReadOnly() && attributes != null && attributes.getDensity() > 0) {
                    String format = metaData.getNativeMetadataFormatName(); // "javax_imageio_png_1.0"
                    IIOMetadataNode tree = (IIOMetadataNode) metaData.getAsTree(format);
                    IIOMetadataNode pHYs = new IIOMetadataNode("pHYs");
                    String dpm = dpi2dpm(attributes.getDensity()) + "";
                    pHYs.setAttribute("pixelsPerUnitXAxis", dpm);
                    pHYs.setAttribute("pixelsPerUnitYAxis", dpm);
                    pHYs.setAttribute("unitSpecifier", "meter");  // density is dots per !Meter!
                    tree.appendChild(pHYs);
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

    // https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/png_metadata.html#image
    // http://www.libpng.org/pub/png/spec/iso/index-object.html
    // http://www.libpng.org/pub/png/spec/1.2/PNG-ColorAppendix.html
    // http://www.libpng.org/pub/png/spec/1.2/PNG-GammaAppendix.html
    public static void explainPngMetaData(Map<String, Map<String, List<Map<String, String>>>> metaData,
            ImageInformation info) {
        try {
            if (!metaData.containsKey("javax_imageio_png_1.0")) {
                return;
            }
            ImageInformationPng pngInfo = (ImageInformationPng) info;
//            logger.debug("explainPngMetaData");
            Map<String, List<Map<String, String>>> javax_imageio_png = metaData.get("javax_imageio_png_1.0");
            if (javax_imageio_png.containsKey("IHDR")) {
                Map<String, String> IHDR = javax_imageio_png.get("IHDR").get(0);
                if (IHDR.containsKey("width")) {
                    pngInfo.setWidth(Integer.valueOf(IHDR.get("width")));
                }
                if (IHDR.containsKey("height")) {
                    pngInfo.setHeight(Integer.valueOf(IHDR.get("height")));
                }
                if (IHDR.containsKey("bitDepth")) {
                    pngInfo.setBitDepth(Integer.valueOf(IHDR.get("bitDepth")));
                }
                if (IHDR.containsKey("colorType")) {
                    pngInfo.setColorType(IHDR.get("colorType"));
                }
                if (IHDR.containsKey("compressionMethod")) {
                    pngInfo.setCompressionMethod(IHDR.get("compressionMethod"));
                }
                if (IHDR.containsKey("filterMethod")) {
                    pngInfo.setFilterMethod(IHDR.get("filterMethod"));
                }
                if (IHDR.containsKey("interlaceMethod")) {
                    pngInfo.setInterlaceMethod(IHDR.get("interlaceMethod"));
                }
            }
            if (javax_imageio_png.containsKey("PLTEEntry")) {
                List<Map<String, String>> PaletteEntryList = javax_imageio_png.get("PLTEEntry");
                pngInfo.setPngPaletteSize(PaletteEntryList.size());
//                List<ImageColor> Palette = new ArrayList();
//                for (Map<String, String> PaletteEntry : PaletteEntryList) {
//                    int index = Integer.valueOf(PaletteEntry.get("index"));
//                    int red = Integer.valueOf(PaletteEntry.get("red"));
//                    int green = Integer.valueOf(PaletteEntry.get("green"));
//                    int blue = Integer.valueOf(PaletteEntry.get("blue"));
//                    int alpha = 255;
//                    Palette.add(new ImageColor(index, red, green, blue, alpha));
//                }
//                pngInfo.setPngPalette(Palette);
            }
            if (javax_imageio_png.containsKey("bKGD_Grayscale")) {
                Map<String, String> bKGD_Grayscale = javax_imageio_png.get("bKGD_Grayscale").get(0);
                pngInfo.setbKGD_Grayscale(Integer.valueOf(bKGD_Grayscale.get("gray")));
            }
            if (javax_imageio_png.containsKey("bKGD_RGB")) {
                Map<String, String> bKGD_RGB = javax_imageio_png.get("bKGD_RGB").get(0);
                int red = Integer.valueOf(bKGD_RGB.get("red"));
                int green = Integer.valueOf(bKGD_RGB.get("green"));
                int blue = Integer.valueOf(bKGD_RGB.get("blue"));
                int alpha = 255;
                pngInfo.setbKGD_RGB(new ImageColor(red, green, blue, alpha));
            }
            if (javax_imageio_png.containsKey("bKGD_Palette")) {
                Map<String, String> bKGD_Palette = javax_imageio_png.get("bKGD_Palette").get(0);
                pngInfo.setbKGD_Palette(Integer.valueOf(bKGD_Palette.get("index")));
            }
            if (javax_imageio_png.containsKey("cHRM")) {
                Map<String, String> cHRM = javax_imageio_png.get("cHRM").get(0);
                double x = 0.00001d * Integer.valueOf(cHRM.get("whitePointX"));
                double y = 0.00001d * Integer.valueOf(cHRM.get("whitePointY"));
                pngInfo.setWhite(new CIEData(x, y));
                x = 0.00001d * Integer.valueOf(cHRM.get("redX"));
                y = 0.00001d * Integer.valueOf(cHRM.get("redY"));
                pngInfo.setRed(new CIEData(x, y));
                x = 0.00001d * Integer.valueOf(cHRM.get("greenX"));
                y = 0.00001d * Integer.valueOf(cHRM.get("greenY"));
                pngInfo.setGreen(new CIEData(x, y));
                x = 0.00001d * Integer.valueOf(cHRM.get("blueX"));
                y = 0.00001d * Integer.valueOf(cHRM.get("blueY"));
                pngInfo.setBlue(new CIEData(x, y));
            }
            if (javax_imageio_png.containsKey("gAMA")) {
                Map<String, String> gAMA = javax_imageio_png.get("gAMA").get(0);
                float g = 0.00001f * Integer.valueOf(gAMA.get("value"));
                pngInfo.setGamma(g);
            }
            if (javax_imageio_png.containsKey("iCCP")) {
                Map<String, String> iCCP = javax_imageio_png.get("iCCP").get(0);
                pngInfo.setProfileName(iCCP.get("profileName"));
                pngInfo.setProfileCompressionMethod(iCCP.get("compressionMethod"));
            }
            if (javax_imageio_png.containsKey("pHYs")) {
                Map<String, String> pHYs = javax_imageio_png.get("pHYs").get(0);
                if (pHYs.containsKey("unitSpecifier")) {
                    pngInfo.setUnitSpecifier(pHYs.get("unitSpecifier"));
                    boolean isMeter = "meter".equals(pHYs.get("unitSpecifier"));
                    if (pHYs.containsKey("pixelsPerUnitXAxis")) {
                        int v = Integer.valueOf(pHYs.get("pixelsPerUnitXAxis"));
                        pngInfo.setPixelsPerUnitXAxis(v);
                        if (isMeter) {
                            pngInfo.setXDpi(dpm2dpi(v));  // resolution value should be dpi
                        } else {
                            pngInfo.setXDpi(v);
                        }
//                        logger.debug("pixelsPerUnitXAxis:" + pngInfo.gethResolution());
                    }
                    if (pHYs.containsKey("pixelsPerUnitYAxis")) {
                        int v = Integer.valueOf(pHYs.get("pixelsPerUnitYAxis"));
                        pngInfo.setPixelsPerUnitYAxis(v);
                        if (isMeter) {
                            pngInfo.setYDpi(dpm2dpi(v));   // resolution value should be dpi
                        } else {
                            pngInfo.setYDpi(v);
                        }
//                        logger.debug("pixelsPerUnitYAxis:" + pngInfo.getvResolution());
                    }
                }
            }
            if (javax_imageio_png.containsKey("sBIT_Grayscale")) {
                Map<String, String> sBIT_Grayscale = javax_imageio_png.get("sBIT_Grayscale").get(0);
                pngInfo.setsBIT_Grayscale(Integer.valueOf(sBIT_Grayscale.get("gray")));
            }
            if (javax_imageio_png.containsKey("sBIT_GrayAlpha")) {
                Map<String, String> sBIT_GrayAlpha = javax_imageio_png.get("sBIT_GrayAlpha").get(0);
                pngInfo.setsBIT_GrayAlpha_gray(Integer.valueOf(sBIT_GrayAlpha.get("gray")));
                pngInfo.setsBIT_GrayAlpha_alpha(Integer.valueOf(sBIT_GrayAlpha.get("alpha")));
            }
            if (javax_imageio_png.containsKey("sBIT_RGB")) {
                Map<String, String> sBIT_RGB = javax_imageio_png.get("sBIT_RGB").get(0);
                pngInfo.setsBIT_RGB_red(Integer.valueOf(sBIT_RGB.get("red")));
                pngInfo.setsBIT_RGB_green(Integer.valueOf(sBIT_RGB.get("green")));
                pngInfo.setsBIT_RGB_blue(Integer.valueOf(sBIT_RGB.get("blue")));
            }
            if (javax_imageio_png.containsKey("sBIT_RGBAlpha")) {
                Map<String, String> sBIT_RGBAlpha = javax_imageio_png.get("sBIT_RGBAlpha").get(0);
                pngInfo.setsBIT_RGBAlpha_red(Integer.valueOf(sBIT_RGBAlpha.get("red")));
                pngInfo.setsBIT_RGBAlpha_green(Integer.valueOf(sBIT_RGBAlpha.get("green")));
                pngInfo.setsBIT_RGBAlpha_blue(Integer.valueOf(sBIT_RGBAlpha.get("blue")));
                pngInfo.setsBIT_RGBAlpha_alpha(Integer.valueOf(sBIT_RGBAlpha.get("alpha")));
            }
            if (javax_imageio_png.containsKey("sBIT_Palette")) {
                Map<String, String> sBIT_Palette = javax_imageio_png.get("sBIT_Palette").get(0);
                pngInfo.setsBIT_Palette_red(Integer.valueOf(sBIT_Palette.get("red")));
                pngInfo.setsBIT_Palette_green(Integer.valueOf(sBIT_Palette.get("green")));
                pngInfo.setsBIT_Palette_blue(Integer.valueOf(sBIT_Palette.get("blue")));
            }
            if (javax_imageio_png.containsKey("sPLTEntry")) {
                List<Map<String, String>> sPLTEntryList = javax_imageio_png.get("sPLTEntry");
                pngInfo.setSuggestedPaletteSize(sPLTEntryList.size());
//                List<ImageColor> Palette = new ArrayList();
//                for (Map<String, String> PaletteEntry : sPLTEntryList) {
//                    int index = Integer.valueOf(PaletteEntry.get("index"));
//                    int red = Integer.valueOf(PaletteEntry.get("red"));
//                    int green = Integer.valueOf(PaletteEntry.get("green"));
//                    int blue = Integer.valueOf(PaletteEntry.get("blue"));
//                    int alpha = 255;
//                    Palette.add(new ImageColor(index, red, green, blue, alpha));
//                }
//                pngInfo.setSuggestedPalette(Palette);
            }
            if (javax_imageio_png.containsKey("sRGB")) {
                Map<String, String> sRGB = javax_imageio_png.get("sRGB").get(0);
                pngInfo.setRenderingIntent(sRGB.get("renderingIntent"));
            }
            if (javax_imageio_png.containsKey("tIME")) {
                Map<String, String> ImageModificationTime = javax_imageio_png.get("tIME").get(0);
                String t = ImageModificationTime.get("year") + "-" + ImageModificationTime.get("month") + "-"
                        + ImageModificationTime.get("day") + " " + ImageModificationTime.get("hour")
                        + ":" + ImageModificationTime.get("minute") + ":" + ImageModificationTime.get("second");
                pngInfo.setImageModificationTime(t);
            }
            if (javax_imageio_png.containsKey("tRNS_Grayscale")) {
                Map<String, String> tRNS_Grayscale = javax_imageio_png.get("tRNS_Grayscale").get(0);
                pngInfo.settRNS_Grayscale(Integer.valueOf(tRNS_Grayscale.get("gray")));
            }
            if (javax_imageio_png.containsKey("tRNS_RGB")) {
                Map<String, String> tRNS_RGB = javax_imageio_png.get("tRNS_RGB").get(0);
                int red = Integer.valueOf(tRNS_RGB.get("red"));
                int green = Integer.valueOf(tRNS_RGB.get("green"));
                int blue = Integer.valueOf(tRNS_RGB.get("blue"));
                int alpha = 255;
                pngInfo.settRNS_RGB(new ImageColor(red, green, blue, alpha));
            }
            if (javax_imageio_png.containsKey("tRNS_Palette")) {
                Map<String, String> tRNS_Palette = javax_imageio_png.get("tRNS_Palette").get(0);
                pngInfo.settRNS_Palette_index(Integer.valueOf(tRNS_Palette.get("index")));
                pngInfo.settRNS_Palette_alpha(Integer.valueOf(tRNS_Palette.get("alpha")));
            }

        } catch (Exception e) {

        }
    }

}
