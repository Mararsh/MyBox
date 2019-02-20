package mara.mybox.image;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.IntCircle;
import mara.mybox.data.IntRectangle;
import static mara.mybox.value.AppVaribles.logger;


/**
 * @Author Mara
 * @CreateDate 2018-11-11 13:02:11
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageScopeTools {

    

    public static BufferedImage indicateRectangle(BufferedImage source,
            Color color, int lineWidth, IntRectangle rect) {
        try {

            int width = source.getWidth();
            int height = source.getHeight();
            if (!rect.isValid(width, height)) {
                return source;
            }
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.drawImage(source, 0, 0, width, height, null);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
            g.setComposite(ac);
            g.setColor(color);
            BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    1f, new float[]{lineWidth, lineWidth}, 0f);
            g.setStroke(stroke);
            g.drawRect((int) rect.getLeftX(), (int) rect.getLeftY(), (int) rect.getWidth(), (int) rect.getHeight());
            g.dispose();
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return source;
        }
    }

    public static BufferedImage indicateCircle(BufferedImage source,
            Color color, int lineWidth, IntCircle circle) {
        try {
            if (!circle.isValid()) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.drawImage(source, 0, 0, width, height, null);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
            g.setComposite(ac);
            g.setColor(color);
            BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    1f, new float[]{lineWidth, lineWidth}, 0f);
            g.setStroke(stroke);
            g.drawOval((int) circle.getCenterX() - (int) circle.getRadius(), (int) circle.getCenterY() - (int) circle.getRadius(),
                    2 * (int) circle.getRadius(), 2 * (int) circle.getRadius());
            g.dispose();
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return source;
        }
    }

    public static BufferedImage indicateSplit(BufferedImage source,
            List<Integer> rows, List<Integer> cols,
            Color lineColor, int lineWidth, boolean showSize, double scale) {
        try {
            if (rows == null || cols == null) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();

            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.drawImage(source, 0, 0, width, height, null);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
            g.setComposite(ac);
            g.setColor(lineColor);
            BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    1f, new float[]{lineWidth, lineWidth}, 0f);
//            BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
            g.setStroke(stroke);

            for (int i = 0; i < rows.size(); i++) {
                int row = (int) (rows.get(i) / scale);
                if (row <= 0 || row >= height - 1) {
                    continue;
                }
                g.drawLine(0, row, width, row);
            }
            for (int i = 0; i < cols.size(); i++) {
                int col = (int) (cols.get(i) / scale);
                if (col <= 0 || col >= width - 1) {
                    continue;
                }
                g.drawLine(col, 0, col, height);
            }

            if (showSize) {
                List<String> texts = new ArrayList<>();
                List<Integer> xs = new ArrayList<>();
                List<Integer> ys = new ArrayList<>();
                for (int i = 0; i < rows.size() - 1; i++) {
                    int h = rows.get(i + 1) - rows.get(i) + 1;
                    for (int j = 0; j < cols.size() - 1; j++) {
                        int w = cols.get(j + 1) - cols.get(j) + 1;
                        texts.add(w + "x" + h);
                        xs.add(cols.get(j) + w / 3);
                        ys.add(rows.get(i) + h / 3);
//                    logger.debug(w / 2 + ", " + h / 2 + "  " + w + "x" + h);
                    }
                }

                int fontSize = width / (cols.size() * 10);
                Font font = new Font(Font.MONOSPACED, Font.BOLD, fontSize);
                g.setFont(font);
                for (int i = 0; i < texts.size(); i++) {
                    g.drawString(texts.get(i), xs.get(i), ys.get(i));
                }
            }

            g.dispose();
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return source;
        }
    }

}
