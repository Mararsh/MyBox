package mara.mybox.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import static mara.mybox.image.ImageConvert.cropImage;
import static mara.mybox.image.ImageConvert.hasAlpha;
import static mara.mybox.value.CommonValues.AlphaColor;
import static mara.mybox.value.AppVaribles.logger;


/**
 * @Author Mara
 * @CreateDate 2018-11-10 20:11:00
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageMargins {

    

    public static BufferedImage cutMargins(BufferedImage source, Color cutColor,
            boolean cutTop, boolean cutBottom, boolean cutLeft, boolean cutRight) {
        try {
            if (cutColor.getRGB() == AlphaColor.getRGB()
                    && !hasAlpha(source)) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int top = 0, bottom = height - 1, left = 0, right = width - 1;
            int cutValue = cutColor.getRGB();
            if (cutTop) {
                for (int j = 0; j < height; j++) {
                    boolean hasValue = false;
                    for (int i = 0; i < width; i++) {
                        if (source.getRGB(i, j) != cutValue) {
                            hasValue = true;
                            break;
                        }
                    }
                    if (hasValue) {
                        top = j;
                        break;
                    }
                }
            }
//            logger.debug("top: " + top);
            if (top < 0) {
                return null;
            }
            if (cutBottom) {
                for (int j = height - 1; j >= 0; j--) {
                    boolean hasValue = false;
                    for (int i = 0; i < width; i++) {
                        if (source.getRGB(i, j) != cutValue) {
                            hasValue = true;
                            break;
                        }
                    }
                    if (hasValue) {
                        bottom = j;
                        break;
                    }
                }
            }
//            logger.debug("bottom: " + bottom);
            if (bottom < 0) {
                return null;
            }
            if (cutLeft) {
                for (int i = 0; i < width; i++) {
                    boolean hasValue = false;
                    for (int j = 0; j < height; j++) {
                        if (source.getRGB(i, j) != cutValue) {
                            hasValue = true;
                            break;
                        }
                    }
                    if (hasValue) {
                        left = i;
                        break;
                    }
                }
            }
//            logger.debug("left: " + left);
            if (left < 0) {
                return null;
            }
            if (cutRight) {
                for (int i = width - 1; i >= 0; i--) {
                    boolean hasValue = false;
                    for (int j = 0; j < height; j++) {
                        if (source.getRGB(i, j) != cutValue) {
                            hasValue = true;
                            break;
                        }
                    }
                    if (hasValue) {
                        right = i;
                        break;
                    }
                }
            }
//            logger.debug("right: " + right);
            if (right < 0) {
                return null;
            }

            int w = right + 1 - left;
            int h = bottom + 1 - top;
            BufferedImage target = source.getSubimage(left, top, w, h);
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage cutMargins(BufferedImage source,
            int MarginWidth, boolean cutTop, boolean cutBottom, boolean cutLeft, boolean cutRight) {
        try {
            if (source == null || MarginWidth <= 0) {
                return source;
            }
            if (!cutTop && !cutBottom && !cutLeft && !cutRight) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int x1 = 0, y1 = 0, x2 = width - 1, y2 = height - 2;
            if (cutLeft) {
                x1 = MarginWidth;
            }
            if (cutRight) {
                x2 = width - 1 - MarginWidth;
            }
            if (cutTop) {
                y1 = MarginWidth;
            }
            if (cutBottom) {
                y2 = height - 1 - MarginWidth;
            }
            return cropImage(source, x1, y1, x2, y2);
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage addMargins(BufferedImage source, Color addColor,
            int MarginWidth, boolean addTop, boolean addBottom, boolean addLeft, boolean addRight) {
        try {
            if (source == null || MarginWidth <= 0) {
                return source;
            }
            if (!addTop && !addBottom && !addLeft && !addRight) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int totalWidth = width, totalHegiht = height;
            int x = 0, y = 0;
            if (addLeft) {
                totalWidth += MarginWidth;
                x = MarginWidth;
            }
            if (addRight) {
                totalWidth += MarginWidth;
            }
            if (addTop) {
                totalHegiht += MarginWidth;
                y = MarginWidth;
            }
            if (addBottom) {
                totalHegiht += MarginWidth;
            }
            int imageType = source.getType();
            if (addColor.getRGB() == AlphaColor.getRGB()) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(totalWidth, totalHegiht, imageType);
            Graphics2D g = target.createGraphics();
            g.setColor(addColor);
            g.fillRect(0, 0, totalWidth, totalHegiht);
            g.drawImage(source, x, y, width, height, null);
            g.dispose();
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

}
