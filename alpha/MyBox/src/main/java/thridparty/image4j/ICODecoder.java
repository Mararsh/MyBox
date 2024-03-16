/*
 * ICODecoder.java
 *
 * Created on May 9, 2006, 9:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package thridparty.image4j;

import java.awt.image.*;
import java.io.*;
import java.util.*;
import mara.mybox.dev.MyBoxLog;

/**
 * Decodes images in ICO format.
 *
 * @author Ian McDonagh
 */
// ##### Updated by mara. To avoid crash when the file is invalid
public class ICODecoder {

    private static final int PNG_MAGIC = 0x89504E47;
    private static final int PNG_MAGIC_LE = 0x474E5089;
    private static final int PNG_MAGIC2 = 0x0D0A1A0A;
    private static final int PNG_MAGIC2_LE = 0x0A1A0A0D;

    // private List<BufferedImage> img;
    private ICODecoder() {
    }

    /**
     * Reads and decodes the given ICO file. Convenience method equivalent to
     * null null null null null     {@link #read(java.io.InputStream) read(new
	 * java.io.FileInputStream(file))}.
     *
     * @param file the source file to read
     * @return the list of images decoded from the ICO data
     *
     */
    public static List<BufferedImage> read(File file) {
        try {
            List<ICOImage> icoList = readExt(file);
            if (icoList == null) {
                return null;
            }
            List<BufferedImage> bgList = new ArrayList<>(icoList.size());
            for (int i = 0; i < icoList.size(); i++) {
                ICOImage icoImage = icoList.get(i);
                BufferedImage image = icoImage.getImage();
                bgList.add(image);
            }
            return bgList;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
        }
    }

    /**
     * Reads and decodes the given ICO file, together with all metadata.
     * Convenience method equivalent to {@link #readExt(java.io.InputStream)
     * readExt(new java.io.FileInputStream(file))}.
     *
     * @param file the source file to read
     * @return the list of images decoded from the ICO data
     * @since 0.7
     */
    public static List<ICOImage> readExt(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        try (FileInputStream fin = new FileInputStream(file)) {
            return readExt(new BufferedInputStream(fin));
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
        }
    }

    public static List<ICOImage> readExt(java.io.InputStream is) {
        if (is == null) {
            return null;
        }
        try (LittleEndianInputStream in
                = new LittleEndianInputStream(new CountingInputStream(is))) {
            // Count 2 byte Number of Icons in this file
            // Reserved 2 byte =0
            short sReserved = in.readShortLE();
            // Type 2 byte =1
            short sType = in.readShortLE();
            // Count 2 byte Number of Icons in this file
            short sCount = in.readShortLE();

            // Entries Count * 16 list of icons
            IconEntry[] entries = new IconEntry[sCount];
            for (short s = 0; s < sCount; s++) {
                entries[s] = new IconEntry(in);
            }
            // Seems like we don't need this, but you never know!
            // entries = sortByFileOffset(entries);

            // images list of bitmap structures in BMP/PNG format
            List<ICOImage> ret = new ArrayList<>(sCount);

            for (int i = 0; i < sCount; i++) {
                // Make sure we're at the right file offset!
                int fileOffset = in.getCount();
                if (fileOffset != entries[i].iFileOffset) {
                    MyBoxLog.error("Cannot read image #" + i
                            + " starting at unexpected file offset.");
                    return null;
                }
                int info = in.readIntLE();
//                MyBoxLog.console("Image #" + i + " @ " + in.getCount()
//                        + " info = " + EndianUtils.toInfoString(info));
                if (info == 40) {

                    // read XOR bitmap
                    // BMPDecoder bmp = new BMPDecoder(is);
                    InfoHeader infoHeader = BMPDecoder.readInfoHeader(in, info);
                    InfoHeader andHeader = new InfoHeader(infoHeader);
                    andHeader.iHeight = (int) (infoHeader.iHeight / 2);
                    InfoHeader xorHeader = new InfoHeader(infoHeader);
                    xorHeader.iHeight = andHeader.iHeight;

                    andHeader.sBitCount = 1;
                    andHeader.iNumColors = 2;

                    // for now, just read all the raster data (xor + and)
                    // and store as separate images
                    BufferedImage xor = BMPDecoder.read(xorHeader, in);
                    // If we want to be sure we've decoded the XOR mask
                    // correctly,
                    // we can write it out as a PNG to a temp file here.
                    // try {
                    // File temp = File.createTempFile("image4j", ".png");
                    // ImageIO.write(xor, "png", temp);
                    // log.info("Wrote xor mask for image #" + i + " to "
                    // + temp.getAbsolutePath());
                    // } catch (Throwable ex) {
                    // }
                    // Or just add it to the output list:
                    // img.add(xor);

                    BufferedImage img = new BufferedImage(xorHeader.iWidth,
                            xorHeader.iHeight, BufferedImage.TYPE_INT_ARGB);

                    ColorEntry[] andColorTable = new ColorEntry[]{
                        new ColorEntry(255, 255, 255, 255),
                        new ColorEntry(0, 0, 0, 0)};

                    if (infoHeader.sBitCount == 32) {
                        // transparency from alpha
                        // ignore bytes after XOR bitmap
                        int size = entries[i].iSizeInBytes;
                        int infoHeaderSize = infoHeader.iSize;
                        // data size = w * h * 4
                        int dataSize = xorHeader.iWidth * xorHeader.iHeight * 4;
                        int skip = size - infoHeaderSize - dataSize;
                        int skip2 = entries[i].iFileOffset + size
                                - in.getCount();

                        // ignore AND bitmap since alpha channel stores
                        // transparency
                        if (in.skip(skip, false) < skip && i < sCount - 1) {
                            throw new EOFException("Unexpected end of input");
                        }
                        // If we skipped less bytes than expected, the AND mask
                        // is probably badly formatted.
                        // If we're at the last/only entry in the file, silently
                        // ignore and continue processing...

                        // //read AND bitmap
                        // BufferedImage and = BMPDecoder.read(andHeader, in,
                        // andColorTable);
                        // this.img.add(and);
                        WritableRaster srgb = xor.getRaster();
                        WritableRaster salpha = xor.getAlphaRaster();
                        WritableRaster rgb = img.getRaster();
                        WritableRaster alpha = img.getAlphaRaster();

                        for (int y = xorHeader.iHeight - 1; y >= 0; y--) {
                            for (int x = 0; x < xorHeader.iWidth; x++) {
                                int r = srgb.getSample(x, y, 0);
                                int g = srgb.getSample(x, y, 1);
                                int b = srgb.getSample(x, y, 2);
                                int a = salpha.getSample(x, y, 0);
                                rgb.setSample(x, y, 0, r);
                                rgb.setSample(x, y, 1, g);
                                rgb.setSample(x, y, 2, b);
                                alpha.setSample(x, y, 0, a);
                            }
                        }

                    } else {
                        BufferedImage and = BMPDecoder.read(andHeader, in,
                                andColorTable);
                        // img.add(and);

                        // copy rgb
                        WritableRaster srgb = xor.getRaster();
                        WritableRaster rgb = img.getRaster();
                        // copy alpha
                        WritableRaster alpha = img.getAlphaRaster();
                        WritableRaster salpha = and.getRaster();

                        for (int y = 0; y < xorHeader.iHeight; y++) {
                            for (int x = 0; x < xorHeader.iWidth; x++) {
                                int r, g, b;
                                int c = xor.getRGB(x, y);
                                r = (c >> 16) & 0xFF;
                                g = (c >> 8) & 0xFF;
                                b = (c) & 0xFF;
                                // red
                                rgb.setSample(x, y, 0, r);
                                // green
                                rgb.setSample(x, y, 1, g);
                                // blue
                                rgb.setSample(x, y, 2, b);
                                // System.out.println(x+","+y+"="+Integer.toHexString(c));
                                // img.setRGB(x, y, c);

                                // alpha
                                int a = and.getRGB(x, y);
                                alpha.setSample(x, y, 0, a);
                            }
                        }
                    }
                    // create ICOImage
                    IconEntry iconEntry = entries[i];
                    ICOImage icoImage = new ICOImage(img, infoHeader, iconEntry);
                    icoImage.setPngCompressed(false);
                    icoImage.setIconIndex(i);
                    ret.add(icoImage);
                } // check for PNG magic header and that image height and width =
                // 0 = 256 -> Vista format
                else if (info == PNG_MAGIC_LE) {

                    int info2 = in.readIntLE();

                    if (info2 != PNG_MAGIC2_LE) {
                        MyBoxLog.error("Unrecognized icon format for image #" + i);
                        return null;
                    }

                    IconEntry e = entries[i];
                    int size = e.iSizeInBytes - 8;
                    byte[] pngData = new byte[size];
                    /* int count = */
                    in.readFully(pngData);
                    // if (count != pngData.length) {
                    // throw new
                    // IOException("Unable to read image #"+i+" - incomplete PNG compressed data");
                    // }
                    java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream();
                    java.io.DataOutputStream dout = new java.io.DataOutputStream(
                            bout);
                    dout.writeInt(PNG_MAGIC);
                    dout.writeInt(PNG_MAGIC2);
                    dout.write(pngData);
                    byte[] pngData2 = bout.toByteArray();
                    java.io.ByteArrayInputStream bin = new java.io.ByteArrayInputStream(
                            pngData2);
                    javax.imageio.stream.ImageInputStream input = javax.imageio.ImageIO
                            .createImageInputStream(bin);
                    javax.imageio.ImageReader reader = getPNGImageReader();
                    reader.setInput(input);
                    java.awt.image.BufferedImage img = reader.read(0);

                    // create ICOImage
                    IconEntry iconEntry = entries[i];
                    ICOImage icoImage = new ICOImage(img, null, iconEntry);
                    icoImage.setPngCompressed(true);
                    icoImage.setIconIndex(i);
                    ret.add(icoImage);
                } else {
                    MyBoxLog.error("Unrecognized icon format for image #" + i);
                    return null;
                }

                /*
				 * InfoHeader andInfoHeader = new InfoHeader();
				 * andInfoHeader.iColorsImportant = 0; andInfoHeader.iColorsUsed
				 * = 0; andInfoHeader.iCompression = BMPConstants.BI_RGB;
				 * andInfoHeader.iHeight = xorInfoHeader.iHeight / 2;
				 * andInfoHeader.iWidth = xorInfoHeader.
                 */
            }

            // long t2 = System.currentTimeMillis();
            // System.out.println("Loaded ICO file in "+(t2 - t)+"ms");
            return ret;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
        }
    }

    private static IconEntry[] sortByFileOffset(IconEntry[] entries) {
        List<IconEntry> list = Arrays.asList(entries);
        Collections.sort(list, new Comparator<IconEntry>() {

            @Override
            public int compare(IconEntry o1, IconEntry o2) {
                return o1.iFileOffset - o2.iFileOffset;
            }
        });
        return list.toArray(new IconEntry[list.size()]);
    }

    /**
     * Reads and decodes ICO data from the given source, together with all
     * metadata. The returned list of images is in the order in which they
     * appear in the source ICO data.
     *
     * @param is the source <tt>InputStream</tt> to read
     * @return the list of images decoded from the ICO data
     * @throws java.io.IOException if an error occurs
     * @since 0.7
     */
    private static javax.imageio.ImageReader getPNGImageReader() {
        javax.imageio.ImageReader ret = null;
        java.util.Iterator<javax.imageio.ImageReader> itr = javax.imageio.ImageIO
                .getImageReadersByFormatName("png");
        if (itr.hasNext()) {
            ret = itr.next();
        }
        return ret;
    }
}
