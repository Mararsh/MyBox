package mara.mybox.tools;

import java.awt.image.BufferedImage;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * @Author Mara
 * @CreateDate 2018-6-17 9:13:00
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfTools {

    private static final Logger logger = LogManager.getLogger();

    public static BufferedImage page2Image(PDFRenderer renderer,
            int pageNumber,
            Map<String, Object> options) {
        try {

            BufferedImage image = null;
//            if (ImageType.BINARY == options.get("ImageColor")) {
//                if ((int) options.get("ColorSchema") == CommonValues.ColorScheme.BINARY_THRESHOLD
//                        && (int) options.get("ColorThreshold")  >= 0) {
//                    image = renderer.renderImageWithDPI(i, imageAttributesController.getDensity(), ImageType.RGB);
//                    image = ImageTools.color2BinaryWithPercentage(image, imageAttributesController.getThreshold());
//                } else if (imageAttributesController.getColorConversion() == CommonValues.ColorConversion.OTSU) {
//                    image = renderer.renderImageWithDPI(i, imageAttributesController.getDensity(), ImageType.RGB);
//                    image = ImageTools.color2Binary(image);
//                } else {
//                    logger.error(i + ":" + imageAttributesController.getDensity());
//                    image = renderer.renderImageWithDPI(i, imageAttributesController.getDensity(), imageAttributesController.getImageColor());
//                }
//            } else {
//                logger.error(i + ":" + imageAttributesController.getDensity());
//                image = renderer.renderImageWithDPI(i, imageAttributesController.getDensity(), imageAttributesController.getImageColor());
//            }

            return image;

        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }
}
