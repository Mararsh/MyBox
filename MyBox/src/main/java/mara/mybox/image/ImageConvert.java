package mara.mybox.image;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import static mara.mybox.value.AppVaribles.env;

/**
 * @Author Mara
 * @CreateDate 2019-6-22 9:52:02
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageConvert {

    public ImageConvert() {

    }

    public static void convertInvertedColors(WritableRaster raster) {
        int height = raster.getHeight();
        int width = raster.getWidth();
        int stride = width * 4;
        int[] pixelRow = new int[stride];
        for (int h = 0; h < height; h++) {
            raster.getPixels(0, h, width, 1, pixelRow);
            for (int x = 0; x < stride; x++) {
                pixelRow[x] = 255 - pixelRow[x];
            }
            raster.setPixels(0, h, width, 1, pixelRow);
        }
    }

    // https://stackoverflow.com/questions/8118712/java-cmyk-to-rgb-with-profile-output-is-too-dark/12132556#12132556
    public static ICC_Profile cmykProfile(String file) {
        try {
            ICC_Profile cmykProfile = ICC_Profile.getInstance(env.getResourceAsStream(file));
            if (cmykProfile.getProfileClass() != ICC_Profile.CLASS_DISPLAY) {
                byte[] profileData = cmykProfile.getData(); // Need to clone entire profile, due to a JDK 7 bug
                if (profileData[ICC_Profile.icHdrRenderingIntent] == ICC_Profile.icPerceptual) {
                    intToBigEndian(ICC_Profile.icSigDisplayClass, profileData, ICC_Profile.icHdrDeviceClass); // Header is first
                    cmykProfile = ICC_Profile.getInstance(profileData);
                }
            }
            return cmykProfile;
        } catch (Exception ex) {
            return null;
        }
    }

    public static ICC_Profile eciCmykProfile() {
        return cmykProfile("/data/ICC/ECI_CMYK.icc");
    }

    static void intToBigEndian(int value, byte[] array, int index) {
        array[index] = (byte) (value >> 24);
        array[index + 1] = (byte) (value >> 16);
        array[index + 2] = (byte) (value >> 8);
        array[index + 3] = (byte) (value);
    }

    public static ICC_ColorSpace eciCmykColorSpace() {
        try {
            return new ICC_ColorSpace(eciCmykProfile());
        } catch (Exception ex) {
            return null;
        }
    }

    public static ICC_Profile adobeCmykProfile() {
        return cmykProfile("/data/ICC/AdobeCMYK_UncoatedFOGRA29.icc");
    }

    public static ICC_ColorSpace adobeCmykColorSpace() {
        try {
            return new ICC_ColorSpace(adobeCmykProfile());
        } catch (Exception ex) {
            return null;
        }
    }

    // https://stackoverflow.com/questions/2408613/unable-to-read-jpeg-image-using-imageio-readfile-file/12132805#12132805
    // https://stackoverflow.com/questions/50798014/determining-color-space-for-jpeg/50861048?r=SearchResults#50861048
    // https://blog.idrsolutions.com/2011/10/ycck-color-conversion-in-pdf-files/
    // https://community.oracle.com/message/10003648?tstart=0
    public static void ycck2cmyk(WritableRaster rast, boolean invertedColors) {
        int w = rast.getWidth(), h = rast.getHeight();
        double c, m, y, k;
        double Y, Cb, Cr, K;
        int[] pixels = null;
        for (int row = 0; row < h; row++) {
            pixels = rast.getPixels(0, row, w, 1, pixels);
            for (int i = 0; i < pixels.length; i += 4) {
                Y = pixels[i];
                Cb = pixels[i + 1];
                Cr = pixels[i + 2];
                K = pixels[i + 3];

                c = Math.min(255, Math.max(0, 255 - (Y + 1.402 * Cr - 179.456)));;
                m = Math.min(255, Math.max(0, 255 - (Y - 0.34414 * Cb - 0.71414 * Cr + 135.45984)));
                y = Math.min(255, Math.max(0, 255 - (Y + 1.7718d * Cb - 226.816)));
                k = K;

                if (invertedColors) {
                    pixels[i] = (byte) (255 - c);
                    pixels[i + 1] = (byte) (255 - m);
                    pixels[i + 2] = (byte) (255 - y);
                    pixels[i + 3] = (byte) (255 - k);
                } else {
                    pixels[i] = (byte) c;
                    pixels[i + 1] = (byte) m;
                    pixels[i + 2] = (byte) y;
                    pixels[i + 3] = (byte) k;
                }
            }
            rast.setPixels(0, row, w, 1, pixels);
        }
    }

    public static void invert(WritableRaster raster) {
        int height = raster.getHeight();
        int width = raster.getWidth();
        int stride = width * 4;
        int[] pixelRow = new int[stride];
        for (int h = 0; h < height; h++) {
            raster.getPixels(0, h, width, 1, pixelRow);
            for (int x = 0; x < stride; x++) {
                pixelRow[x] = 255 - pixelRow[x];
            }
            raster.setPixels(0, h, width, 1, pixelRow);
        }
    }

    public static Raster ycck2cmyk(final byte[] buffer, final int w, final int h) throws IOException {
        final int pixelCount = w * h * 4;
        for (int i = 0; i < pixelCount; i = i + 4) {
            int y = (buffer[i] & 255);
            int cb = (buffer[i + 1] & 255);
            int cr = (buffer[i + 2] & 255);
//            int k = (buffer[i + 3] & 255);

            int r = Math.max(0, Math.min(255, (int) (y + 1.402 * cr - 179.456)));
            int g = Math.max(0, Math.min(255, (int) (y - 0.34414 * cb - 0.71414 * cr + 135.95984)));
            int b = Math.max(0, Math.min(255, (int) (y + 1.772 * cb - 226.316)));

            buffer[i] = (byte) (255 - r);
            buffer[i + 1] = (byte) (255 - g);
            buffer[i + 2] = (byte) (255 - b);
        }

        return Raster.createInterleavedRaster(new DataBufferByte(buffer, pixelCount), w, h, w * 4, 4, new int[]{0, 1, 2, 3}, null);
    }

    public static BufferedImage rgb2cmyk(ICC_Profile cmykProfile, BufferedImage inImage) throws IOException {
        if (cmykProfile == null) {
            cmykProfile = ICC_Profile.getInstance(env.getResourceAsStream("/data/ICC/ECI_CMYK.icc"));
        }
        ICC_ColorSpace cmykCS = new ICC_ColorSpace(cmykProfile);
        ColorConvertOp rgb2cmyk = new ColorConvertOp(cmykCS, null);
        return rgb2cmyk.filter(inImage, null);
    }

    public static BufferedImage cmyk2rgb(Raster cmykRaster, ICC_Profile cmykProfile) throws IOException {
        if (cmykProfile == null) {
            cmykProfile = ICC_Profile.getInstance(env.getResourceAsStream("/data/ICC/ECI_CMYK.icc"));
        }
        ICC_ColorSpace cmykCS = new ICC_ColorSpace(cmykProfile);
        BufferedImage rgbImage = new BufferedImage(cmykRaster.getWidth(), cmykRaster.getHeight(), BufferedImage.TYPE_INT_RGB);
        WritableRaster rgbRaster = rgbImage.getRaster();
        ColorSpace rgbCS = rgbImage.getColorModel().getColorSpace();
        ColorConvertOp cmykToRgb = new ColorConvertOp(cmykCS, rgbCS, null);
        cmykToRgb.filter(cmykRaster, rgbRaster);
        return rgbImage;
    }

    // https://bugs.openjdk.java.net/browse/JDK-8041125
    public static BufferedImage cmyk2rgb(final byte[] buffer, final int w, final int h) throws IOException {

        final ColorSpace CMYK = ImageConvert.adobeCmykColorSpace();

        final int pixelCount = w * h * 4;
        int C, M, Y, K, lastC = -1, lastM = -1, lastY = -1, lastK = -1;

        int j = 0;
        float[] RGB = new float[]{0f, 0f, 0f};
        //turn YCC in Buffer to CYM using profile
        for (int i = 0; i < pixelCount; i = i + 4) {

            C = (buffer[i] & 255);
            M = (buffer[i + 1] & 255);
            Y = (buffer[i + 2] & 255);
            K = (buffer[i + 3] & 255);

            // System.out.println(C+" "+M+" "+Y+" "+K);
            if (C == lastC && M == lastM && Y == lastY && K == lastK) {
                //no change so use last value
            } else { //new value

                RGB = CMYK.toRGB(new float[]{C / 255f, M / 255f, Y / 255f, K / 255f});

                //flag so we can just reuse if next value the same
                lastC = C;
                lastM = M;
                lastY = Y;
                lastK = K;
            }

            //put back as CMY
            buffer[j] = (byte) (RGB[0] * 255f);
            buffer[j + 1] = (byte) (RGB[1] * 255f);
            buffer[j + 2] = (byte) (RGB[2] * 255f);

            j = j + 3;

        }

        /**
         * create CMYK raster from buffer
         */
        final Raster raster = Raster.createInterleavedRaster(new DataBufferByte(buffer, j), w, h, w * 3, 3, new int[]{0, 1, 2}, null);

        //data now sRGB so create image
        final BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        image.setData(raster);

        return image;
    }

}
