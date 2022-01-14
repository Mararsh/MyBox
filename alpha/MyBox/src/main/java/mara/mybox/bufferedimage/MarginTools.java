package mara.mybox.bufferedimage;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Colors;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @License Apache License Version 2.0
 */
public class MarginTools {

    public static BufferedImage blurMarginsAlpha(BufferedImage source, int blurWidth, boolean blurTop, boolean blurBottom, boolean blurLeft, boolean blurRight) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            float iOpocity;
            float jOpacity;
            float opocity;
            Color newColor;
            for (int j = 0; j < height; ++j) {
                for (int i = 0; i < width; ++i) {
                    int pixel = source.getRGB(i, j);
                    if (pixel == 0) {
                        target.setRGB(i, j, 0);
                        continue;
                    }
                    iOpocity = jOpacity = 1.0F;
                    if (i < blurWidth) {
                        if (blurLeft) {
                            iOpocity = 1.0F * i / blurWidth;
                        }
                    } else if (i > width - blurWidth) {
                        if (blurRight) {
                            iOpocity = 1.0F * (width - i) / blurWidth;
                        }
                    }
                    if (j < blurWidth) {
                        if (blurTop) {
                            jOpacity = 1.0F * j / blurWidth;
                        }
                    } else if (j > height - blurWidth) {
                        if (blurBottom) {
                            jOpacity = 1.0F * (height - j) / blurWidth;
                        }
                    }
                    opocity = iOpocity * jOpacity;
                    if (opocity == 1.0F) {
                        target.setRGB(i, j, pixel);
                    } else {
                        newColor = new Color(pixel);
                        opocity = newColor.getAlpha() * opocity;
                        newColor = new Color(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), (int) opocity);
                        target.setRGB(i, j, newColor.getRGB());
                    }
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage cutMargins(BufferedImage source, Color cutColor,
            boolean cutTop, boolean cutBottom, boolean cutLeft, boolean cutRight) {
        try {
            if (cutColor.getRGB() == Colors.TRANSPARENT.getRGB() && !AlphaTools.hasAlpha(source)) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int top;
            int bottom;
            int left;
            int right;
            int cutValue = cutColor.getRGB();
            if (cutTop) {
                top = -1;
                toploop:
                for (int j = 0; j < height; ++j) {
                    for (int i = 0; i < width; ++i) {
                        if (source.getRGB(i, j) != cutValue) {
                            top = j;
                            break toploop;
                        }
                    }
                }
                if (top < 0) {
                    return null;
                }
            } else {
                top = 0;
            }
            if (cutBottom) {
                bottom = -1;
                bottomploop:
                for (int j = height - 1; j >= 0; --j) {
                    for (int i = 0; i < width; ++i) {
                        if (source.getRGB(i, j) != cutValue) {
                            bottom = j;
                            break bottomploop;
                        }
                    }
                }
                if (bottom < 0) {
                    return null;
                }
            } else {
                bottom = height - 1;
            }
            if (cutLeft) {
                left = -1;
                leftloop:
                for (int i = 0; i < width; ++i) {
                    for (int j = 0; j < height; ++j) {
                        if (source.getRGB(i, j) != cutValue) {
                            left = i;
                            break leftloop;
                        }
                    }
                }
                if (left < 0) {
                    return null;
                }
            } else {
                left = 0;
            }
            if (cutRight) {
                right = -1;
                rightloop:
                for (int i = width - 1; i >= 0; --i) {
                    for (int j = 0; j < height; ++j) {
                        if (source.getRGB(i, j) != cutValue) {
                            right = i;
                            break rightloop;
                        }
                    }
                }
                if (right < 0) {
                    return null;
                }
            } else {
                right = width - 1;
            }
            //            int w = right + 1 - left;
            //            int h = bottom + 1 - top;
            //            BufferedImage target = source.getSubimage(left, top, w, h); // This way works on Java8 but messes on Java 12
            BufferedImage target = CropTools.cropOutside(source, left, top, right, bottom);
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage cutMargins(BufferedImage source, int MarginWidth, boolean cutTop, boolean cutBottom, boolean cutLeft, boolean cutRight) {
        try {
            if (source == null || MarginWidth <= 0) {
                return source;
            }
            if (!cutTop && !cutBottom && !cutLeft && !cutRight) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int x1 = 0;
            int y1 = 0;
            int x2 = width - 1;
            int y2 = height - 2;
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
            return CropTools.cropOutside(source, x1, y1, x2, y2);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage blurMarginsNoAlpha(BufferedImage source, int blurWidth, boolean blurTop, boolean blurBottom, boolean blurLeft, boolean blurRight) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_RGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            float iOpocity;
            float jOpacity;
            float opocity;
            Color alphaColor = ColorConvertTools.alphaColor();
            for (int j = 0; j < height; ++j) {
                for (int i = 0; i < width; ++i) {
                    int pixel = source.getRGB(i, j);
                    if (pixel == 0) {
                        target.setRGB(i, j, alphaColor.getRGB());
                        continue;
                    }
                    iOpocity = jOpacity = 1.0F;
                    if (i < blurWidth) {
                        if (blurLeft) {
                            iOpocity = 1.0F * i / blurWidth;
                        }
                    } else if (i > width - blurWidth) {
                        if (blurRight) {
                            iOpocity = 1.0F * (width - i) / blurWidth;
                        }
                    }
                    if (j < blurWidth) {
                        if (blurTop) {
                            jOpacity = 1.0F * j / blurWidth;
                        }
                    } else if (j > height - blurWidth) {
                        if (blurBottom) {
                            jOpacity = 1.0F * (height - j) / blurWidth;
                        }
                    }
                    opocity = iOpocity * jOpacity;
                    if (opocity == 1.0F) {
                        target.setRGB(i, j, pixel);
                    } else {
                        Color color = ColorBlendTools.blendColor(new Color(pixel), opocity, alphaColor);
                        target.setRGB(i, j, color.getRGB());
                    }
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage addMargins(BufferedImage source, Color addColor, int MarginWidth, boolean addTop, boolean addBottom, boolean addLeft, boolean addRight) {
        try {
            if (source == null || MarginWidth <= 0) {
                return source;
            }
            if (!addTop && !addBottom && !addLeft && !addRight) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int totalWidth = width;
            int totalHegiht = height;
            int x = 0;
            int y = 0;
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
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(totalWidth, totalHegiht, imageType);
            Graphics2D g = target.createGraphics();
            g.setColor(addColor);
            g.fillRect(0, 0, totalWidth, totalHegiht);
            g.drawImage(source, x, y, width, height, null);
            g.dispose();
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
