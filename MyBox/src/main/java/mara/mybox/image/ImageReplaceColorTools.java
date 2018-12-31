package mara.mybox.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import static mara.mybox.objects.CommonValues.AlphaColor;
import static mara.mybox.objects.AppVaribles.logger;


/**
 * @Author Mara
 * @CreateDate 2018-11-10 20:15:43
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageReplaceColorTools {

    

    public static BufferedImage replaceColor(BufferedImage source,
            Color oldColor, Color newColor, int distance,
            boolean isColor, boolean excluded) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int newValue = newColor.getRGB();
            int imageType = source.getType();
            if (newColor.getRGB() == AlphaColor.getRGB()) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    int color = source.getRGB(i, j);
                    if (ImageColorTools.matchColor(new Color(color), oldColor, distance, isColor, excluded)) {
                        target.setRGB(i, j, newValue);
                    } else {
                        target.setRGB(i, j, color);
                    }
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

//        public static Image replaceColorsRectangle(Image image, Color newColor, int x1, int y1, int x2, int y2) {
//        PixelReader pixelReader = image.getPixelReader();
//        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
//        PixelWriter pixelWriter = newImage.getPixelWriter();
//
//        for (int y = 0; y < image.getHeight(); y++) {
//            for (int x = 0; x < image.getWidth(); x++) {
//                if (x >= x1 && x <= x2 && y >= y1 && y <= y2) {
//                    pixelWriter.setColor(x, y, newColor);
//                } else {
//                    pixelWriter.setColor(x, y, pixelReader.getColor(x, y));
//                }
//            }
//        }
//        return newImage;
//    }
//
//    public static Image replaceColorsCircle(Image image, Color newColor, int cx, int cy, int r) {
//        PixelReader pixelReader = image.getPixelReader();
//        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
//        PixelWriter pixelWriter = newImage.getPixelWriter();
//
//        int r2 = r * r;
//        for (int y = 0; y < image.getHeight(); y++) {
//            for (int x = 0; x < image.getWidth(); x++) {
//                Color color = pixelReader.getColor(x, y);
//                int distanceX = cx - x;
//                int distaneY = cy - y;
//                if (distanceX * distanceX + distaneY * distaneY <= r2) {
//                    pixelWriter.setColor(x, y, newColor);
//                } else {
//                    pixelWriter.setColor(x, y, color);
//                }
//            }
//        }
//        return newImage;
//    }
//
//    public static Image replaceColorsMatting(Image source, Color newColor,
//            int startx, int starty, int distance) {
//        try {
//            int width = (int) source.getWidth();
//            int height = (int) source.getHeight();
//            PixelReader pixelReader = source.getPixelReader();
//            WritableImage newImage = new WritableImage(width, height);
//            PixelWriter pixelWriter = newImage.getPixelWriter();
//            pixelWriter.setPixels(0, 0, width, height, pixelReader, 0, 0);
//
//            Color startColor = pixelReader.getColor(startx, starty);
//            boolean[][] visited = new boolean[height][width];
//            Queue<Point> queue = new LinkedList<>();
//            queue.add(new Point(startx, starty));
//
//            while (!queue.isEmpty()) {
//                Point p = queue.remove();
//                if (p.x < 0 || p.x >= width || p.y < 0 || p.y >= height || visited[p.y][p.x]) {
//                    continue;
//                }
//                visited[p.y][p.x] = true;
//                Color pixelColor = pixelReader.getColor(p.x, p.y);
//                if (isColorMatch(pixelColor, startColor, distance)) {
//                    pixelWriter.setColor(p.x, p.y, newColor);
//                    queue.add(new Point(p.x + 1, p.y));
//                    queue.add(new Point(p.x - 1, p.y));
//                    queue.add(new Point(p.x, p.y + 1));
//                    queue.add(new Point(p.x, p.y - 1));
//                }
//            }
//            return newImage;
//        } catch (Exception e) {
//            logger.error(e.toString());
//            return null;
//        }
//
//    }
}
