package mara.mybox.image.file;

import com.github.jaiimageio.jpeg2000.J2KImageReadParam;
import com.github.jaiimageio.jpeg2000.J2KImageWriteParam;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReader;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import mara.mybox.image.ImageAttributes;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-19
 *
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageJpeg2000File {

    // https://sno.phy.queensu.ca/~phil/exiftool/TagNames/Jpeg2000.html
    public static boolean isJpeg2000(String format) {
        format = format.toLowerCase();
        return "jpeg2000".equals(format) || "jpeg 2000".equals(format)
                || "jpx".equals(format) || "jp2".equals(format) || "jpm".equals(format);
    }

    public static boolean isJpeg2000(File file) {
        return isJpeg2000(FileTools.getFileSuffix(file));
    }

    public static String[] getJ2KCompressionTypes() {
        return new J2KImageWriteParam(null).getCompressionTypes();
    }

    public static ImageWriter getWriter() {
        return ImageIO.getImageWritersByFormatName("JPEG 2000").next();
//        return new J2KImageWriter(new J2KImageWriterSpi());
    }

    public static ImageReader getReader() {
        return ImageIO.getImageReadersByFormatName("JPEG 2000").next();
//        return new J2KImageReader(new J2KImageReaderSpi());
    }

    public static BufferedImage readImage(File file) {
        try {
            J2KImageReader reader = (J2KImageReader) getReader();
            BufferedImage bufferedImage;
            try ( ImageInputStream in = ImageIO.createImageInputStream(
                    new BufferedInputStream(new FileInputStream(file)))) {
                J2KImageReadParam param = (J2KImageReadParam) reader.getDefaultReadParam();
                param.getResolution();
                reader.setInput(in, false, true);
                bufferedImage = reader.read(0);
                reader.dispose();
            }
            return bufferedImage;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }

    }

    public static ImageWriteParam getWriterParam(ImageAttributes attributes) {
        try {
            J2KImageWriteParam param = (J2KImageWriteParam) getWriter().getDefaultWriteParam();
//            param.setSOP(true);
//            param.setWriteCodeStreamOnly(true);
//            param.setProgressionType("layer");
//            param.setComponentTransformation(false);
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                if (attributes != null && attributes.getCompressionType() != null) {
                    param.setCompressionType(attributes.getCompressionType());
                }
                if (attributes != null && attributes.getQuality() > 0) {
                    if (attributes.getQuality() < 100) {
                        param.setCompressionQuality(attributes.getQuality() / 100.0f);
//                        param.setLossless(false);
//                        param.setFilter(J2KImageWriteParam.FILTER_97);
                    } else {
                        logger.debug(attributes.getQuality());
                        param.setCompressionQuality(1.0f);
//                        param.setLossless(true);
//                        param.setFilter(J2KImageWriteParam.FILTER_53);
                    }
                } else {
                    param.setCompressionQuality(1.0f);
//                    param.setLossless(true);
//                    param.setFilter(J2KImageWriteParam.FILTER_53);
                }
            }

            return param;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

}
