/*
 *  Copyright (c) Matthew Abboud 2016
 *
 *  Pdf2Dom is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Pdf2Dom is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with CSSBox. If not, see <http://www.gnu.org/licenses/>.
 */
package thridparty.pdfdom;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

public class PathDrawer {

    private static final Logger log = getLogger(PathDrawer.class);
    private final PDGraphicsState state;

    public PathDrawer(PDGraphicsState state) {
        this.state = state;
    }

    public ImageResource drawPath(List<PathSegment> path) throws IOException {
        if (path.size() == 0) {
            return new ImageResource("PathImage", new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
        }

        Rectangle2D.Double bounds = getPathBounds(path);
        if (bounds.getHeight() <= 0 || bounds.getWidth() <= 0) {
            bounds.width = bounds.height = 1;
            log.info("Filled curved paths are not yet supported by Pdf2Dom.");
        }

        BufferedImage image = new BufferedImage((int) bounds.getWidth(), (int) bounds.getHeight(),
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D gfx = image.createGraphics();
        try {
            clearPathGraphics(bounds, gfx);
            drawPathSegments(path, gfx);
        } catch (UnsupportedOperationException e) {
            log.info("Discarding unsupported path");
            image = null;
        }
        gfx.dispose();

        if (image != null) {
            // keep track of whitespace cropped off for html element positioning
            ImageResource drawnPath = new ImageResource("PathImage", image);
            drawnPath.setX(bounds.getX());
            drawnPath.setY(bounds.getY());
            return drawnPath;
        } else {
            return null;
        }
    }

    private void clearPathGraphics(Rectangle2D.Double bounds, Graphics2D gfx) throws IOException {
        Color transparent = new Color(255, 255, 255, 0);
        gfx.setColor(transparent);
        gfx.fillRect(0, 0, (int) bounds.getWidth() * 2, (int) bounds.getHeight() * 2);

        Color fill = pdfColorToColor(state.getNonStrokingColor());
        Color stroke = pdfColorToColor(state.getStrokingColor());

        // crop off rendered path whitespace
        gfx.translate(-bounds.getX(), -bounds.getY());

        gfx.setPaint(stroke);
        gfx.setColor(fill);
    }

    private void drawPathSegments(List<PathSegment> path, Graphics2D gfx) {
        int[] xPts = new int[path.size()];
        int[] yPts = new int[path.size()];

        for (int i = 0; i < path.size(); i++) {
            PathSegment segmentOn = path.get(i);
            xPts[i] = (int) segmentOn.getX1();
            yPts[i] = (int) segmentOn.getY1();
        }

        gfx.fillPolygon(xPts, yPts, path.size());
    }

    private Rectangle2D.Double getPathBounds(List<PathSegment> path) {
        PathSegment first = path.get(0);
        int minX = (int) first.getX1(), maxX = (int) first.getX1();
        int minY = (int) first.getY2(), maxY = (int) first.getY1();

        for (PathSegment segmentOn : path) {
            maxX = Math.max((int) segmentOn.getX1(), maxX);
            maxX = Math.max((int) segmentOn.getX2(), maxX);
            maxY = Math.max((int) segmentOn.getY1(), maxY);
            maxY = Math.max((int) segmentOn.getY2(), maxY);

            minX = Math.min((int) segmentOn.getX1(), minX);
            minX = Math.min((int) segmentOn.getX2(), minX);
            minY = Math.min((int) segmentOn.getY1(), minY);
            minY = Math.min((int) segmentOn.getY2(), minY);
        }

        int width = maxX - minX;
        int height = maxY - minY;
        int x = minX;
        int y = minY;

        return new Rectangle2D.Double(x, y, width, height);
    }

    private Color pdfColorToColor(PDColor color) throws IOException {
        float[] rgb = color.getColorSpace().toRGB(color.getComponents());

        return new Color(rgb[0], rgb[1], rgb[2]);
    }
}
