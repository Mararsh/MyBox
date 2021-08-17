package mara.mybox.imagefile;

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
import mara.mybox.dev.MyBoxLog;
import mara.mybox.bufferedimage.ImageAttributes;
import mara.mybox.bufferedimage.ImageColor;
import mara.mybox.bufferedimage.ImageConvertTools;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.bufferedimage.ImageInformationPng;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TmpFileTools;
import org.w3c.dom.NodeList;

/**
 * @Author Mara
 * @CreateDate 2018-6-19
 *
 * @Description
 * @License Apache License Version 2.0
 */
public class ImagePngFile {

    // https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/png_metadata.html#image
    public static ImageWriter getWriter() {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
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

    public static IIOMetadata getWriterMeta(ImageAttributes attributes, BufferedImage image,
            ImageWriter writer, ImageWriteParam param) {
        try {
            IIOMetadata metaData = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
            if (metaData == null || metaData.isReadOnly() || attributes == null) {
                return metaData;
            }
//                    if (attributes.getAlpha() == ImageAttributes.Alpha.PremultipliedAndKeep
//                            && metaData.isStandardMetadataFormatSupported()) {
//                        try {
//                            String standardFormat = IIOMetadataFormatImpl.standardMetadataFormatName; // "javax_imageio_1.0"
//                            MyBoxLog.error(standardFormat);
//                            IIOMetadataNode standardTree = (IIOMetadataNode) metaData.getAsTree(standardFormat);
//                            NodeList TransparencyNode = standardTree.getElementsByTagName("Transparency");
//                            IIOMetadataNode Transparency, Alpha;
//                            if (TransparencyNode != null && TransparencyNode.getLength() > 0) {
//                                Transparency = (IIOMetadataNode) TransparencyNode.item(0);
//                            } else {
//                                Transparency = new IIOMetadataNode("Transparency");
//                                standardTree.appendChild(Transparency);
//                            }
//                            Alpha = new IIOMetadataNode("Alpha");
//                            Alpha.setAttribute("value", "premultiplied");
//                            Transparency.appendChild(Alpha);
//                            metaData.mergeTree(standardFormat, standardTree);
//                        } catch (Exception e) {
//                            MyBoxLog.error(e.toString());
//                        }
//                    }
            String nativeFormat = metaData.getNativeMetadataFormatName(); // "javax_imageio_png_1.0"
            IIOMetadataNode nativeTree = (IIOMetadataNode) metaData.getAsTree(nativeFormat);
            if (attributes.getDensity() > 0) {
                NodeList pHYsNode = nativeTree.getElementsByTagName("pHYs");
                IIOMetadataNode pHYs;
                if (pHYsNode != null && pHYsNode.getLength() > 0) {
                    pHYs = (IIOMetadataNode) pHYsNode.item(0);
                } else {
                    pHYs = new IIOMetadataNode("pHYs");
                    nativeTree.appendChild(pHYs);
                }
                String dpm = ImageConvertTools.dpi2dpm(attributes.getDensity()) + "";
                pHYs.setAttribute("pixelsPerUnitXAxis", dpm);
                pHYs.setAttribute("pixelsPerUnitYAxis", dpm);
                pHYs.setAttribute("unitSpecifier", "meter");  // density is dots per !Meter!
            }
            if (attributes.isEmbedProfile()
                    && attributes.getProfile() != null && attributes.getProfileName() != null) {
                NodeList iCCPsNode = nativeTree.getElementsByTagName("iCCP");
                IIOMetadataNode iCCP;
                if (iCCPsNode != null && iCCPsNode.getLength() > 0) {
                    iCCP = (IIOMetadataNode) iCCPsNode.item(0);
                } else {
                    iCCP = new IIOMetadataNode("iCCP");
                    nativeTree.appendChild(iCCP);
                }
                iCCP.setUserObject(ByteTools.deflate(attributes.getProfile().getData()));
                iCCP.setAttribute("profileName", attributes.getProfileName());
                iCCP.setAttribute("compressionMethod", "deflate");
            }
            metaData.mergeTree(nativeFormat, nativeTree);
            return metaData;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static boolean writePNGImageFile(BufferedImage image,
            ImageAttributes attributes, File file) {
        try {
            ImageWriter writer = getWriter();
            ImageWriteParam param = getPara(attributes, writer);
            IIOMetadata metaData = getWriterMeta(attributes, image, writer, param);
            File tmpFile = TmpFileTools.getTempFile();
            try ( ImageOutputStream out = ImageIO.createImageOutputStream(tmpFile)) {
                writer.setOutput(out);
                writer.write(metaData, new IIOImage(image, null, metaData), param);
                out.flush();
            }
            writer.dispose();
            return FileTools.rename(tmpFile, file);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    // https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/png_metadata.html#image
    // http://www.libpng.org/pub/png/spec/iso/index-object.html
    // http://www.libpng.org/pub/png/spec/1.2/PNG-ColorAppendix.html
    // http://www.libpng.org/pub/png/spec/1.2/PNG-GammaAppendix.html
    public static void explainPngMetaData(Map<String, Map<String, List<Map<String, Object>>>> metaData,
            ImageInformation info) {
        try {
            if (!metaData.containsKey("javax_imageio_png_1.0")) {
                return;
            }
            ImageInformationPng pngInfo = (ImageInformationPng) info;
//            MyBoxLog.debug("explainPngMetaData");
            Map<String, List<Map<String, Object>>> javax_imageio_png = metaData.get("javax_imageio_png_1.0");
            if (javax_imageio_png.containsKey("IHDR")) {
                Map<String, Object> IHDR = javax_imageio_png.get("IHDR").get(0);
                if (IHDR.containsKey("width")) {
                    pngInfo.setWidth(Integer.valueOf((String) IHDR.get("width")));
                }
                if (IHDR.containsKey("height")) {
                    pngInfo.setHeight(Integer.valueOf((String) IHDR.get("height")));
                }
                if (IHDR.containsKey("bitDepth")) {
                    pngInfo.setBitDepth(Integer.valueOf((String) IHDR.get("bitDepth")));
                }
                if (IHDR.containsKey("colorType")) {
                    pngInfo.setColorType((String) IHDR.get("colorType"));
                }
                if (IHDR.containsKey("compressionMethod")) {
                    pngInfo.setCompressionMethod((String) IHDR.get("compressionMethod"));
                }
                if (IHDR.containsKey("filterMethod")) {
                    pngInfo.setFilterMethod((String) IHDR.get("filterMethod"));
                }
                if (IHDR.containsKey("interlaceMethod")) {
                    pngInfo.setInterlaceMethod((String) IHDR.get("interlaceMethod"));
                }
            }
            if (javax_imageio_png.containsKey("PLTEEntry")) {
                List<Map<String, Object>> PaletteEntryList = javax_imageio_png.get("PLTEEntry");
                pngInfo.setPngPaletteSize(PaletteEntryList.size());
//                List<ImageColor> Palette = new ArrayList<>();
//                for (Map<String, Object> PaletteEntry : PaletteEntryList) {
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
                Map<String, Object> bKGD_Grayscale = javax_imageio_png.get("bKGD_Grayscale").get(0);
                pngInfo.setbKGD_Grayscale(Integer.valueOf((String) bKGD_Grayscale.get("gray")));
            }
            if (javax_imageio_png.containsKey("bKGD_RGB")) {
                Map<String, Object> bKGD_RGB = javax_imageio_png.get("bKGD_RGB").get(0);
                int red = Integer.valueOf((String) bKGD_RGB.get("red"));
                int green = Integer.valueOf((String) bKGD_RGB.get("green"));
                int blue = Integer.valueOf((String) bKGD_RGB.get("blue"));
                int alpha = 255;
                pngInfo.setbKGD_RGB(new ImageColor(red, green, blue, alpha));
            }
            if (javax_imageio_png.containsKey("bKGD_Palette")) {
                Map<String, Object> bKGD_Palette = javax_imageio_png.get("bKGD_Palette").get(0);
                pngInfo.setbKGD_Palette(Integer.valueOf((String) bKGD_Palette.get("index")));
            }
            if (javax_imageio_png.containsKey("cHRM")) {
                Map<String, Object> cHRM = javax_imageio_png.get("cHRM").get(0);
                double x = 0.00001d * Integer.valueOf((String) cHRM.get("whitePointX"));
                double y = 0.00001d * Integer.valueOf((String) cHRM.get("whitePointY"));
                pngInfo.setWhite(new CIEData(x, y));
                x = 0.00001d * Integer.valueOf((String) cHRM.get("redX"));
                y = 0.00001d * Integer.valueOf((String) cHRM.get("redY"));
                pngInfo.setRed(new CIEData(x, y));
                x = 0.00001d * Integer.valueOf((String) cHRM.get("greenX"));
                y = 0.00001d * Integer.valueOf((String) cHRM.get("greenY"));
                pngInfo.setGreen(new CIEData(x, y));
                x = 0.00001d * Integer.valueOf((String) cHRM.get("blueX"));
                y = 0.00001d * Integer.valueOf((String) cHRM.get("blueY"));
                pngInfo.setBlue(new CIEData(x, y));
            }
            if (javax_imageio_png.containsKey("gAMA")) {
                Map<String, Object> gAMA = javax_imageio_png.get("gAMA").get(0);
                float g = 0.00001f * Integer.valueOf((String) gAMA.get("value"));
                pngInfo.setGamma(g);
            }
            if (javax_imageio_png.containsKey("iCCP")) {
                Map<String, Object> iCCP = javax_imageio_png.get("iCCP").get(0);
                pngInfo.setProfileName((String) iCCP.get("profileName"));
                pngInfo.setProfileCompressionMethod((String) iCCP.get("compressionMethod"));
                pngInfo.setIccProfile(ByteTools.inflate((byte[]) iCCP.get("UserObject")));
            }
            if (javax_imageio_png.containsKey("pHYs")) {
                Map<String, Object> pHYs = javax_imageio_png.get("pHYs").get(0);
                if (pHYs.containsKey("unitSpecifier")) {
                    pngInfo.setUnitSpecifier((String) pHYs.get("unitSpecifier"));
                    boolean isMeter = "meter".equals(pHYs.get("unitSpecifier"));
                    if (pHYs.containsKey("pixelsPerUnitXAxis")) {
                        int v = Integer.valueOf((String) pHYs.get("pixelsPerUnitXAxis"));
                        pngInfo.setPixelsPerUnitXAxis(v);
                        if (isMeter) {
                            pngInfo.setXDpi(ImageConvertTools.dpm2dpi(v));  // resolution value should be dpi
                        } else {
                            pngInfo.setXDpi(v);
                        }
//                        MyBoxLog.debug("pixelsPerUnitXAxis:" + pngInfo.gethResolution());
                    }
                    if (pHYs.containsKey("pixelsPerUnitYAxis")) {
                        int v = Integer.valueOf((String) pHYs.get("pixelsPerUnitYAxis"));
                        pngInfo.setPixelsPerUnitYAxis(v);
                        if (isMeter) {
                            pngInfo.setYDpi(ImageConvertTools.dpm2dpi(v));   // resolution value should be dpi
                        } else {
                            pngInfo.setYDpi(v);
                        }
//                        MyBoxLog.debug("pixelsPerUnitYAxis:" + pngInfo.getvResolution());
                    }
                }
            }
            if (javax_imageio_png.containsKey("sBIT_Grayscale")) {
                Map<String, Object> sBIT_Grayscale = javax_imageio_png.get("sBIT_Grayscale").get(0);
                pngInfo.setsBIT_Grayscale(Integer.valueOf((String) sBIT_Grayscale.get("gray")));
            }
            if (javax_imageio_png.containsKey("sBIT_GrayAlpha")) {
                Map<String, Object> sBIT_GrayAlpha = javax_imageio_png.get("sBIT_GrayAlpha").get(0);
                pngInfo.setsBIT_GrayAlpha_gray(Integer.valueOf((String) sBIT_GrayAlpha.get("gray")));
                pngInfo.setsBIT_GrayAlpha_alpha(Integer.valueOf((String) sBIT_GrayAlpha.get("alpha")));
            }
            if (javax_imageio_png.containsKey("sBIT_RGB")) {
                Map<String, Object> sBIT_RGB = javax_imageio_png.get("sBIT_RGB").get(0);
                pngInfo.setsBIT_RGB_red(Integer.valueOf((String) sBIT_RGB.get("red")));
                pngInfo.setsBIT_RGB_green(Integer.valueOf((String) sBIT_RGB.get("green")));
                pngInfo.setsBIT_RGB_blue(Integer.valueOf((String) sBIT_RGB.get("blue")));
            }
            if (javax_imageio_png.containsKey("sBIT_RGBAlpha")) {
                Map<String, Object> sBIT_RGBAlpha = javax_imageio_png.get("sBIT_RGBAlpha").get(0);
                pngInfo.setsBIT_RGBAlpha_red(Integer.valueOf((String) sBIT_RGBAlpha.get("red")));
                pngInfo.setsBIT_RGBAlpha_green(Integer.valueOf((String) sBIT_RGBAlpha.get("green")));
                pngInfo.setsBIT_RGBAlpha_blue(Integer.valueOf((String) sBIT_RGBAlpha.get("blue")));
                pngInfo.setsBIT_RGBAlpha_alpha(Integer.valueOf((String) sBIT_RGBAlpha.get("alpha")));
            }
            if (javax_imageio_png.containsKey("sBIT_Palette")) {
                Map<String, Object> sBIT_Palette = javax_imageio_png.get("sBIT_Palette").get(0);
                pngInfo.setsBIT_Palette_red(Integer.valueOf((String) sBIT_Palette.get("red")));
                pngInfo.setsBIT_Palette_green(Integer.valueOf((String) sBIT_Palette.get("green")));
                pngInfo.setsBIT_Palette_blue(Integer.valueOf((String) sBIT_Palette.get("blue")));
            }
            if (javax_imageio_png.containsKey("sPLTEntry")) {
                List<Map<String, Object>> sPLTEntryList = javax_imageio_png.get("sPLTEntry");
                pngInfo.setSuggestedPaletteSize(sPLTEntryList.size());
//                List<ImageColor> Palette = new ArrayList<>();
//                for (Map<String, Object> PaletteEntry : sPLTEntryList) {
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
                Map<String, Object> sRGB = javax_imageio_png.get("sRGB").get(0);
                pngInfo.setRenderingIntent((String) sRGB.get("renderingIntent"));
            }
            if (javax_imageio_png.containsKey("tIME")) {
                Map<String, Object> ImageModificationTime = javax_imageio_png.get("tIME").get(0);
                String t = ImageModificationTime.get("year") + "-" + ImageModificationTime.get("month") + "-"
                        + ImageModificationTime.get("day") + " " + ImageModificationTime.get("hour")
                        + ":" + ImageModificationTime.get("minute") + ":" + ImageModificationTime.get("second");
                pngInfo.setImageModificationTime(t);
            }
            if (javax_imageio_png.containsKey("tRNS_Grayscale")) {
                Map<String, Object> tRNS_Grayscale = javax_imageio_png.get("tRNS_Grayscale").get(0);
                pngInfo.settRNS_Grayscale(Integer.valueOf((String) tRNS_Grayscale.get("gray")));
            }
            if (javax_imageio_png.containsKey("tRNS_RGB")) {
                Map<String, Object> tRNS_RGB = javax_imageio_png.get("tRNS_RGB").get(0);
                int red = Integer.valueOf((String) tRNS_RGB.get("red"));
                int green = Integer.valueOf((String) tRNS_RGB.get("green"));
                int blue = Integer.valueOf((String) tRNS_RGB.get("blue"));
                int alpha = 255;
                pngInfo.settRNS_RGB(new ImageColor(red, green, blue, alpha));
            }
            if (javax_imageio_png.containsKey("tRNS_Palette")) {
                Map<String, Object> tRNS_Palette = javax_imageio_png.get("tRNS_Palette").get(0);
                pngInfo.settRNS_Palette_index(Integer.valueOf((String) tRNS_Palette.get("index")));
                pngInfo.settRNS_Palette_alpha(Integer.valueOf((String) tRNS_Palette.get("alpha")));
            }

        } catch (Exception e) {

        }
    }

}
