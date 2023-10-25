package mara.mybox.bufferedimage;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import mara.mybox.bufferedimage.ImageScope.ColorScopeType;
import mara.mybox.bufferedimage.ImageScope.ScopeType;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.data.IntPoint;
import mara.mybox.data.StringTable;
import mara.mybox.db.table.TableImageScope;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.JsonTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.XmlTools;
import static mara.mybox.tools.XmlTools.cdata;
import mara.mybox.value.AppPaths;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @Author Mara
 * @CreateDate 2018-8-1 16:22:41
 * @License Apache License Version 2.0
 */
public class ImageScopeTools {

    public static ImageScope.ScopeType scopeType(String type) {
        if (type == null) {
            return ImageScope.ScopeType.Invalid;
        }
        if ("All".equalsIgnoreCase(type)) {
            return ImageScope.ScopeType.All;
        }
        if ("Matting".equalsIgnoreCase(type)) {
            return ImageScope.ScopeType.Matting;
        }
        if ("Rectangle".equalsIgnoreCase(type) || "RectangleColor".equalsIgnoreCase(type)) {
            return ImageScope.ScopeType.Rectangle;
        }
        if ("Circle".equalsIgnoreCase(type) || "CircleColor".equalsIgnoreCase(type)) {
            return ImageScope.ScopeType.Circle;
        }
        if ("Ellipse".equalsIgnoreCase(type) || "EllipseColor".equalsIgnoreCase(type)) {
            return ImageScope.ScopeType.Ellipse;
        }
        if ("Polygon".equalsIgnoreCase(type) || "PolygonColor".equalsIgnoreCase(type)) {
            return ImageScope.ScopeType.Polygon;
        }
        if ("Color".equalsIgnoreCase(type)) {
            return ImageScope.ScopeType.Color;
        }
        if ("Outline".equalsIgnoreCase(type)) {
            return ImageScope.ScopeType.Outline;
        }
        if ("Operate".equalsIgnoreCase(type)) {
            return ImageScope.ScopeType.Operate;
        }
        return ImageScope.ScopeType.Invalid;
    }

    public static void cloneValues(ImageScope targetScope, ImageScope sourceScope) {
        try {
            List<IntPoint> npoints = new ArrayList<>();
            if (sourceScope.getPoints() != null) {
                npoints.addAll(sourceScope.getPoints());
            }
            targetScope.setPoints(npoints);
            List<Color> ncolors = new ArrayList<>();
            if (sourceScope.getColors() != null) {
                ncolors.addAll(sourceScope.getColors());
            }
            targetScope.setColors(ncolors);
            targetScope.setRectangle(sourceScope.getRectangle().copy());
            targetScope.setCircle(sourceScope.getCircle().copy());
            targetScope.setEllipse(sourceScope.getEllipse().copy());
            targetScope.setPolygon(sourceScope.getPolygon().copy());
            targetScope.setColorDistance(sourceScope.getColorDistance());
            targetScope.setColorDistanceSquare(sourceScope.getColorDistanceSquare());
            targetScope.setHsbDistance(sourceScope.getHsbDistance());
            targetScope.setColorExcluded(sourceScope.isColorExcluded());
            targetScope.setDistanceSquareRoot(sourceScope.isDistanceSquareRoot());
            targetScope.setAreaExcluded(sourceScope.isAreaExcluded());
            targetScope.setMaskOpacity(sourceScope.getMaskOpacity());
            targetScope.setCreateTime(sourceScope.getCreateTime());
            targetScope.setOutline(sourceScope.getOutline());
            targetScope.setEightNeighbor(sourceScope.isEightNeighbor());
        } catch (Exception e) {
            //            MyBoxLog.debug(e);
        }
    }

    public static ImageScope cloneAll(ImageScope sourceScope) {
        ImageScope targetScope = new ImageScope();
        ImageScopeTools.cloneAll(targetScope, sourceScope);
        return targetScope;
    }

    public static void cloneAll(ImageScope targetScope, ImageScope sourceScope) {
        try {
            targetScope.setImage(sourceScope.getImage());
            targetScope.setScopeType(sourceScope.getScopeType());
            targetScope.setColorScopeType(sourceScope.getColorScopeType());
            cloneValues(targetScope, sourceScope);
        } catch (Exception e) {
        }
    }

    public static boolean inShape(DoubleShape shape, boolean areaExcluded, int x, int y) {
        if (areaExcluded) {
            return !DoubleShape.contains(shape, x, y);
        } else {
            return DoubleShape.contains(shape, x, y);
        }
    }

    public static boolean isColorMatchSquare(List<Color> colors, boolean colorExcluded, int colorDistanceSqure, Color color) {
        if (colors == null || colors.isEmpty()) {
            return true;
        }
        if (colorExcluded) {
            for (Color oColor : colors) {
                if (ColorMatchTools.isColorMatchSquare(color, oColor, colorDistanceSqure)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (ColorMatchTools.isColorMatchSquare(color, oColor, colorDistanceSqure)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isRedMatch(List<Color> colors, boolean colorExcluded, int colorDistance, Color color) {
        if (colors == null || colors.isEmpty()) {
            return true;
        }
        if (colorExcluded) {
            for (Color oColor : colors) {
                if (ColorMatchTools.isRedMatch(color, oColor, colorDistance)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (ColorMatchTools.isRedMatch(color, oColor, colorDistance)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isGreenMatch(List<Color> colors, boolean colorExcluded, int colorDistance, Color color) {
        if (colors == null || colors.isEmpty()) {
            return true;
        }
        if (colorExcluded) {
            for (Color oColor : colors) {
                if (ColorMatchTools.isGreenMatch(color, oColor, colorDistance)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (ColorMatchTools.isGreenMatch(color, oColor, colorDistance)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isBlueMatch(List<Color> colors, boolean colorExcluded, int colorDistance, Color color) {
        if (colors == null || colors.isEmpty()) {
            return true;
        }
        if (colorExcluded) {
            for (Color oColor : colors) {
                if (ColorMatchTools.isBlueMatch(color, oColor, colorDistance)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (ColorMatchTools.isBlueMatch(color, oColor, colorDistance)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isHueMatch(List<Color> colors, boolean colorExcluded, float hsbDistance, Color color) {
        if (colors == null || colors.isEmpty()) {
            return true;
        }
        if (colorExcluded) {
            for (Color oColor : colors) {
                if (ColorMatchTools.isHueMatch(color, oColor, hsbDistance)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (ColorMatchTools.isHueMatch(color, oColor, hsbDistance)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isSaturationMatch(List<Color> colors, boolean colorExcluded, float hsbDistance, Color color) {
        if (colors == null || colors.isEmpty()) {
            return true;
        }
        if (colorExcluded) {
            for (Color oColor : colors) {
                if (ColorMatchTools.isSaturationMatch(color, oColor, hsbDistance)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (ColorMatchTools.isSaturationMatch(color, oColor, hsbDistance)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isBrightnessMatch(List<Color> colors, boolean colorExcluded, float hsbDistance, Color color) {
        if (colors == null || colors.isEmpty()) {
            return true;
        }
        if (colorExcluded) {
            for (Color oColor : colors) {
                if (ColorMatchTools.isBrightnessMatch(color, oColor, hsbDistance)) {
                    return false;
                }
            }
            return true;
        } else {

            for (Color oColor : colors) {
                if (ColorMatchTools.isBrightnessMatch(color, oColor, hsbDistance)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static BufferedImage indicateEllipse(BufferedImage source, Color color, int lineWidth, DoubleEllipse ellipse) {
        try {
            if (!ellipse.isValid()) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            if (AppVariables.imageRenderHints != null) {
                g.addRenderingHints(AppVariables.imageRenderHints);
            }
            g.drawImage(source, 0, 0, width, height, null);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0F);
            g.setComposite(ac);
            g.setColor(color);
            BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0F, new float[]{
                lineWidth, lineWidth}, 0.0F);
            g.setStroke(stroke);
            g.drawOval((int) Math.round(ellipse.getX()), (int) Math.round(ellipse.getY()),
                    (int) Math.round(ellipse.getWidth()), (int) Math.round(ellipse.getHeight()));
            g.dispose();
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return source;
        }
    }

    public static BufferedImage indicateRectangle(BufferedImage source, Color color, int lineWidth, DoubleRectangle rect) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            if (AppVariables.imageRenderHints != null) {
                g.addRenderingHints(AppVariables.imageRenderHints);
            }
            g.drawImage(source, 0, 0, width, height, null);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0F);
            g.setComposite(ac);
            g.setColor(color);
            BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0F, new float[]{
                lineWidth, lineWidth}, 0.0F);
            g.setStroke(stroke);
            g.drawRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
            g.dispose();
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return source;
        }
    }

    public static BufferedImage indicateCircle(BufferedImage source, Color color, int lineWidth, DoubleCircle circle) {
        try {
            if (!circle.isValid()) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            if (AppVariables.imageRenderHints != null) {
                g.addRenderingHints(AppVariables.imageRenderHints);
            }
            g.drawImage(source, 0, 0, width, height, null);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0F);
            g.setComposite(ac);
            g.setColor(color);
            BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0F, new float[]{
                lineWidth, lineWidth}, 0.0F);
            g.setStroke(stroke);
            g.drawOval((int) circle.getCenterX() - (int) circle.getRadius(), (int) circle.getCenterY() - (int) circle.getRadius(), 2 * (int) circle.getRadius(), 2 * (int) circle.getRadius());
            g.dispose();
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return source;
        }
    }

    public static BufferedImage indicateSplit(BufferedImage source,
            List<Integer> rows, List<Integer> cols, Color lineColor, int lineWidth, boolean showSize, double scale) {
        try {
            if (rows == null || cols == null) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            if (AppVariables.imageRenderHints != null) {
                g.addRenderingHints(AppVariables.imageRenderHints);
            }
            g.drawImage(source, 0, 0, width, height, null);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0F);
            g.setComposite(ac);
            g.setColor(lineColor);
            BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0F, new float[]{
                lineWidth, lineWidth}, 0.0F);
            //            BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
            g.setStroke(stroke);
            for (int i = 0; i < rows.size(); ++i) {
                int row = (int) (rows.get(i) / scale);
                if (row <= 0 || row >= height - 1) {
                    continue;
                }
                g.drawLine(0, row, width, row);
            }
            for (int i = 0; i < cols.size(); ++i) {
                int col = (int) (cols.get(i) / scale);
                if (col <= 0 || col >= width - 1) {
                    continue;
                }
                g.drawLine(col, 0, col, height);
            }
            if (showSize) {
                int fontSize = width / (cols.size() * 10);
                Font font = new Font(Font.MONOSPACED, Font.BOLD, fontSize);
                g.setFont(font);
                FontMetrics metrics = g.getFontMetrics(font);
                int yOffset = metrics.getAscent();
                for (int i = 0; i < rows.size() - 1; ++i) {
                    int h = rows.get(i + 1) - rows.get(i);
                    for (int j = 0; j < cols.size() - 1; ++j) {
                        int w = cols.get(j + 1) - cols.get(j);
                        int x = cols.get(j) + w / 3;
                        int y = rows.get(i) + h / 3 + yOffset;
                        g.drawString(w + "x" + h, (int) (x / scale), (int) (y / scale) + yOffset);
                    }
                }
            }
            g.dispose();
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return source;
        }
    }

    /*
       make scope from
     */
    public static ImageScope fromXML(String s) {
        try {
            if (s == null || s.isBlank()) {
                return null;
            }
            Element e = XmlTools.toElement(null, s);
            if (e == null) {
                return null;
            }
            String tag = e.getTagName();
            if (!XmlTools.matchXmlTag("ImageScope", tag)) {
                return null;
            }
            NodeList children = e.getChildNodes();
            if (children == null) {
                return null;
            }
            ImageScope scope = new ImageScope();
            for (int dIndex = 0; dIndex < children.getLength(); dIndex++) {
                Node child = children.item(dIndex);
                if (!(child instanceof Element)) {
                    continue;
                }
                tag = child.getNodeName();

                if (tag == null || tag.isBlank()) {
                    continue;
                }
                if (XmlTools.matchXmlTag("Background", tag)) {
                    scope.setFile(cdata(child));

                } else if (XmlTools.matchXmlTag("Name", tag)) {
                    scope.setName(cdata(child));

                } else if (XmlTools.matchXmlTag("Outline", tag)) {
                    scope.setOutlineName(cdata(child));

                } else {
                    String value = child.getTextContent();
                    if (value == null || value.isBlank()) {
                        continue;
                    }
                    if (XmlTools.matchXmlTag("ScopeType", tag)) {
                        scope.setScopeType(ImageScopeTools.scopeType(value));

                    } else if (XmlTools.matchXmlTag("ScopeColorType", tag)) {
                        scope.setColorScopeType(ImageScope.ColorScopeType.valueOf(value));

                    } else if (XmlTools.matchXmlTag("Area", tag)) {
                        scope.setAreaData(value);

                    } else if (XmlTools.matchXmlTag("Colors", tag)) {
                        scope.setColorData(value);

                    } else if (XmlTools.matchXmlTag("ColorDistance", tag)) {
                        try {
                            scope.setColorDistance(Integer.parseInt(value));
                        } catch (Exception ex) {
                        }

                    } else if (XmlTools.matchXmlTag("AreaExcluded", tag)) {
                        scope.setAreaExcluded(StringTools.isTrue(value));

                    } else if (XmlTools.matchXmlTag("ColorExcluded", tag)) {
                        scope.setColorExcluded(StringTools.isTrue(value));

                    } else if (XmlTools.matchXmlTag("CreateTime", tag)) {
                        scope.setCreateTime(DateTools.encodeDate(value));

                    } else if (XmlTools.matchXmlTag("ModifyTime", tag)) {
                        scope.setModifyTime(DateTools.encodeDate(value));

                    }
                }
            }
            return scope;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
       convert scope to
     */
    public static String toXML(ImageScope scope, String inPrefix) {
        try {
            if (scope == null) {
                return null;
            }
            ScopeType type = scope.getScopeType();
            if (type == null) {
                return null;
            }
            String prefix = inPrefix + AppValues.Indent;
            StringBuilder s = new StringBuilder();
            s.append(prefix).append("<").append(XmlTools.xmlTag("ImageScope")).append(">\n");
            s.append(prefix).append("<").append(XmlTools.xmlTag("ScopeType")).append(">")
                    .append(type)
                    .append("</").append(XmlTools.xmlTag("ScopeType")).append(">\n");
            String v = scope.getFile();
            if (v != null && !v.isBlank()) {
                s.append(prefix).append("<").append(XmlTools.xmlTag("Background")).append(">")
                        .append("<![CDATA[").append(v).append("]]>")
                        .append("</").append(XmlTools.xmlTag("Background")).append(">\n");
            }
            v = scope.getName();
            if (v != null && !v.isBlank()) {
                s.append(prefix).append("<").append(XmlTools.xmlTag("Name")).append(">")
                        .append("<![CDATA[").append(v).append("]]>")
                        .append("</").append(XmlTools.xmlTag("Name")).append(">\n");
            }
            v = scope.getOutlineName();
            if (v != null && !v.isBlank()) {
                s.append(prefix).append("<").append(XmlTools.xmlTag("Outline")).append(">")
                        .append("<![CDATA[").append(v).append("]]>")
                        .append("</").append(XmlTools.xmlTag("Outline")).append(">\n");
            }
            ColorScopeType ctype = scope.getColorScopeType();
            if (ctype != null) {
                s.append(prefix).append("<").append(XmlTools.xmlTag("ScopeColorType")).append(">")
                        .append(ctype)
                        .append("</").append(XmlTools.xmlTag("ScopeColorType")).append(">\n");
            }
            v = scope.getAreaData();
            if (v != null && !v.isBlank()) {
                s.append(prefix).append("<").append(XmlTools.xmlTag("Area")).append(">")
                        .append("<![CDATA[").append(v).append("]]>")
                        .append("</").append(XmlTools.xmlTag("Area")).append(">\n");
            }
            v = scope.getColorData();
            if (v != null && !v.isBlank()) {
                s.append(prefix).append("<").append(XmlTools.xmlTag("Colors")).append(">")
                        .append("<![CDATA[").append(v).append("]]>")
                        .append("</").append(XmlTools.xmlTag("Colors")).append(">\n");
            }
            if (scope.getColorDistance() > 0) {
                s.append(prefix).append("<").append(XmlTools.xmlTag("ColorDistance")).append(">")
                        .append(scope.getColorDistance())
                        .append("</").append(XmlTools.xmlTag("ColorDistance")).append(">\n");
            }
            s.append(prefix).append("<").append(XmlTools.xmlTag("AreaExcluded")).append(">")
                    .append(scope.isAreaExcluded() ? "true" : "false")
                    .append("</").append(XmlTools.xmlTag("AreaExcluded")).append(">\n");
            s.append(prefix).append("<").append(XmlTools.xmlTag("ColorExcluded")).append(">")
                    .append(scope.isColorExcluded() ? "true" : "false")
                    .append("</").append(XmlTools.xmlTag("ColorExcluded")).append(">\n");
            s.append(prefix).append("<").append(XmlTools.xmlTag("CreateTime")).append(">")
                    .append(DateTools.dateToString(scope.getCreateTime()))
                    .append("</").append(XmlTools.xmlTag("CreateTime")).append(">\n");
            s.append(prefix).append("<").append(XmlTools.xmlTag("ModifyTime")).append(">")
                    .append(DateTools.dateToString(scope.getModifyTime()))
                    .append("</").append(XmlTools.xmlTag("ModifyTime")).append(">\n");
            s.append(prefix).append("</").append(XmlTools.xmlTag("ImageScope")).append(">\n");
            return s.toString();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String toJSON(ImageScope scope, String inPrefix) {
        try {
            if (scope == null) {
                return null;
            }
            ScopeType type = scope.getScopeType();
            if (type == null) {
                return null;
            }
            String prefix = inPrefix + AppValues.Indent;
            StringBuilder s = new StringBuilder();
            s.append(inPrefix).append("\"").append(message("ImageScope")).append("\": {\n");
            s.append(prefix).append("\"").append(message("ScopeType")).append("\": ")
                    .append(JsonTools.encode(type.name())).append(",\n");
            String v = scope.getFile();
            if (v != null && !v.isBlank()) {
                s.append(prefix).append("\"").append(message("Background")).append("\": ")
                        .append(JsonTools.encode(v)).append(",\n");
            }
            v = scope.getName();
            if (v != null && !v.isBlank()) {
                s.append(prefix).append("\"").append(message("Name")).append("\": ")
                        .append(JsonTools.encode(v)).append(",\n");
            }
            v = scope.getOutlineName();
            if (v != null && !v.isBlank()) {
                s.append(prefix).append("\"").append(message("Outline")).append("\": ")
                        .append(JsonTools.encode(v)).append(",\n");
            }
            ColorScopeType ctype = scope.getColorScopeType();
            if (ctype != null) {
                s.append(prefix).append("\"").append(message("ScopeColorType")).append("\": ")
                        .append(JsonTools.encode(ctype.name())).append(",\n");
            }
            v = scope.getAreaData();
            if (v != null && !v.isBlank()) {
                s.append(prefix).append("\"").append(message("Area")).append("\": ")
                        .append(JsonTools.encode(v)).append(",\n");
            }
            v = scope.getColorData();
            if (v != null && !v.isBlank()) {
                s.append(prefix).append("\"").append(message("Colors")).append("\": ")
                        .append(JsonTools.encode(v)).append(",\n");
            }
            if (scope.getColorDistance() > 0) {
                s.append(prefix).append("\"").append(message("ColorDistance")).append("\": ")
                        .append(scope.getColorDistance()).append(",\n");
            }
            s.append(prefix).append("\"").append(message("AreaExcluded")).append("\": ")
                    .append(scope.isAreaExcluded() ? "true" : "false").append(",\n");
            s.append(prefix).append("\"").append(message("ColorExcluded")).append("\": ")
                    .append(scope.isColorExcluded() ? "true" : "false").append(",\n");
            s.append(prefix).append("\"").append(message("CreateTime")).append("\": ")
                    .append(JsonTools.encode(DateTools.dateToString(scope.getCreateTime()))).append(",\n");
            s.append(prefix).append("\"").append(message("ModifyTime")).append("\": ")
                    .append(JsonTools.encode(DateTools.dateToString(scope.getModifyTime()))).append("\n");
            s.append(inPrefix).append("}\n");
            return s.toString();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static String toHtml(ImageScope scope) {
        try {
            if (scope == null) {
                return null;
            }
            ScopeType type = scope.getScopeType();
            if (type == null) {
                return null;
            }
            StringTable htmlTable = new StringTable();
            List<String> row = new ArrayList<>();
            row.addAll(Arrays.asList(message("ScopeType"), type.name()));
            htmlTable.add(row);
            String v = scope.getFile();
            if (v != null && !v.isBlank()) {
                row = new ArrayList<>();
                row.addAll(Arrays.asList(message("Background"), "<PRE><CODE>" + v + "</CODE></PRE>"));
                htmlTable.add(row);
            }
            v = scope.getName();
            if (v != null && !v.isBlank()) {
                row = new ArrayList<>();
                row.addAll(Arrays.asList(message("Name"), "<PRE><CODE>" + v + "</CODE></PRE>"));
                htmlTable.add(row);
            }
            v = scope.getOutlineName();
            if (v != null && !v.isBlank()) {
                row = new ArrayList<>();
                row.addAll(Arrays.asList(message("Outline"), "<PRE><CODE>" + v + "</CODE></PRE>"));
                htmlTable.add(row);
            }
            ColorScopeType ctype = scope.getColorScopeType();
            if (ctype != null) {
                row = new ArrayList<>();
                row.addAll(Arrays.asList(message("ScopeColorType"), ctype.name()));
                htmlTable.add(row);
            }
            v = scope.getAreaData();
            if (v != null && !v.isBlank()) {
                row = new ArrayList<>();
                row.addAll(Arrays.asList(message("Area"), v));
                htmlTable.add(row);
            }
            v = scope.getColorData();
            if (v != null && !v.isBlank()) {
                row = new ArrayList<>();
                row.addAll(Arrays.asList(message("Colors"), v));
                htmlTable.add(row);
            }
            if (scope.getColorDistance() > 0) {
                row = new ArrayList<>();
                row.addAll(Arrays.asList(message("ColorDistance"), scope.getColorDistance() + ""));
                htmlTable.add(row);
            }
            row = new ArrayList<>();
            row.addAll(Arrays.asList(message("AreaExcluded"), scope.isAreaExcluded() ? message("Yes") : ""));
            htmlTable.add(row);
            row = new ArrayList<>();
            row.addAll(Arrays.asList(message("ColorExcluded"), scope.isColorExcluded() ? message("Yes") : ""));
            htmlTable.add(row);
            row = new ArrayList<>();
            row.addAll(Arrays.asList(message("CreateTime"), DateTools.dateToString(scope.getCreateTime())));
            htmlTable.add(row);
            row = new ArrayList<>();
            row.addAll(Arrays.asList(message("ModifyTime"), DateTools.dateToString(scope.getModifyTime())));
            htmlTable.add(row);
            return htmlTable.div();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
       extract value from scope
     */
    public static boolean decodeColorData(ImageScope scope) {
        if (scope == null) {
            return false;
        }
        return decodeColorData(scope.getScopeType(), scope.getColorData(), scope);
    }

    public static boolean decodeColorData(ImageScope.ScopeType type, String colorData, ImageScope scope) {
        if (type == null || colorData == null || scope == null) {
            return false;
        }
        try {
            switch (type) {
                case Color:
                case Rectangle:
                case Circle:
                case Ellipse:
                case Polygon: {
                    List<Color> colors = new ArrayList<>();
                    if (!colorData.isBlank()) {
                        String[] items = colorData.split(TableImageScope.DataSeparator);
                        for (String item : items) {
                            try {
                                colors.add(new Color(Integer.parseInt(item), true));
                            } catch (Exception e) {
                                MyBoxLog.error(e);
                            }
                        }
                    }
                    scope.setColors(colors);
                    scope.setColorData(colorData);
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean decodeOutline(ImageScope scope) {
        if (scope == null) {
            return false;
        }
        return decodeOutline(scope.getScopeType(), scope.getOutlineName(), scope);
    }

    public static boolean decodeOutline(ImageScope.ScopeType type, String outline, ImageScope scope) {
        if (type == null || outline == null || scope == null) {
            return false;
        }
        if (type != ImageScope.ScopeType.Outline) {
            return true;
        }
        try {
            scope.setOutlineName(outline);
            BufferedImage image = ImageFileReaders.readImage(new File(outline));
            scope.setOutlineSource(image);
            return image != null;
        } catch (Exception e) {
            MyBoxLog.error(e);
            //            MyBoxLog.debug(e);
            return false;
        }
    }

    public static String encodeColorData(ImageScope scope) {
        if (scope == null || scope.getScopeType() == null) {
            return "";
        }
        String s = "";
        try {
            switch (scope.getScopeType()) {
                case Color:
                case Rectangle:
                case Circle:
                case Ellipse:
                case Polygon:
                    List<Color> colors = scope.getColors();
                    if (colors != null) {
                        for (Color color : colors) {
                            s += color.getRGB() + TableImageScope.DataSeparator;
                        }
                        if (s.endsWith(TableImageScope.DataSeparator)) {
                            s = s.substring(0, s.length() - TableImageScope.DataSeparator.length());
                        }
                    }
            }
            scope.setColorData(s);
        } catch (Exception e) {
            MyBoxLog.error(e);
            s = "";
        }
        return s;
    }

    public static String encodeAreaData(ImageScope scope) {
        if (scope == null || scope.getScopeType() == null) {
            return "";
        }
        String s = "";
        try {
            switch (scope.getScopeType()) {
                case Matting: {
                    List<IntPoint> points = scope.getPoints();
                    if (points != null) {
                        for (IntPoint p : points) {
                            s += p.getX() + TableImageScope.DataSeparator + p.getY() + TableImageScope.DataSeparator;
                        }
                        if (s.endsWith(TableImageScope.DataSeparator)) {
                            s = s.substring(0, s.length() - TableImageScope.DataSeparator.length());
                        }
                    }
                }
                break;
                case Rectangle:
                case Outline:
                    DoubleRectangle rect = scope.getRectangle();
                    if (rect != null) {
                        s = (int) rect.getX() + TableImageScope.DataSeparator + (int) rect.getY() + TableImageScope.DataSeparator + (int) rect.getMaxX() + TableImageScope.DataSeparator + (int) rect.getMaxY();
                    }
                    break;
                case Circle:
                    DoubleCircle circle = scope.getCircle();
                    if (circle != null) {
                        s = (int) circle.getCenterX() + TableImageScope.DataSeparator + (int) circle.getCenterY() + TableImageScope.DataSeparator + (int) circle.getRadius();
                    }
                    break;
                case Ellipse:
                    DoubleEllipse ellipse = scope.getEllipse();
                    if (ellipse != null) {
                        s = (int) ellipse.getX() + TableImageScope.DataSeparator + (int) ellipse.getY() + TableImageScope.DataSeparator + (int) ellipse.getMaxX() + TableImageScope.DataSeparator + (int) ellipse.getMaxY();
                    }
                    break;
                case Polygon:
                    DoublePolygon polygon = scope.getPolygon();
                    if (polygon != null) {
                        for (Double d : polygon.getData()) {
                            s += Math.round(d) + TableImageScope.DataSeparator;
                        }
                        if (s.endsWith(TableImageScope.DataSeparator)) {
                            s = s.substring(0, s.length() - TableImageScope.DataSeparator.length());
                        }
                    }
                    break;
            }
            scope.setAreaData(s);
        } catch (Exception e) {
            MyBoxLog.error(e);
            s = "";
        }
        return s;
    }

    public static boolean decodeAreaData(ImageScope scope) {
        if (scope == null) {
            return false;
        }
        return decodeAreaData(scope.getScopeType(), scope.getAreaData(), scope);
    }

    public static boolean decodeAreaData(ImageScope.ScopeType type, String areaData, ImageScope scope) {
        if (type == null || areaData == null || scope == null) {
            return false;
        }
        try {
            switch (type) {
                case Matting: {
                    String[] items = areaData.split(TableImageScope.DataSeparator);
                    for (int i = 0; i < items.length / 2; ++i) {
                        int x = (int) Double.parseDouble(items[i * 2]);
                        int y = (int) Double.parseDouble(items[i * 2 + 1]);
                        scope.addPoint(x, y);
                    }
                }
                break;
                case Rectangle:
                case Outline: {
                    String[] items = areaData.split(TableImageScope.DataSeparator);
                    if (items.length == 4) {
                        DoubleRectangle rect = DoubleRectangle.xy12(Double.parseDouble(items[0]),
                                Double.parseDouble(items[1]),
                                Double.parseDouble(items[2]),
                                Double.parseDouble(items[3]));
                        scope.setRectangle(rect);
                    } else {
                        return false;
                    }
                }
                break;
                case Circle: {
                    String[] items = areaData.split(TableImageScope.DataSeparator);
                    if (items.length == 3) {
                        DoubleCircle circle = new DoubleCircle(Double.parseDouble(items[0]),
                                Double.parseDouble(items[1]),
                                Double.parseDouble(items[2]));
                        scope.setCircle(circle);
                    } else {
                        return false;
                    }
                }
                break;
                case Ellipse: {
                    String[] items = areaData.split(TableImageScope.DataSeparator);
                    if (items.length == 4) {
                        DoubleEllipse ellipse = DoubleEllipse.xy12(Double.parseDouble(items[0]),
                                Double.parseDouble(items[1]),
                                Double.parseDouble(items[2]),
                                Double.parseDouble(items[3]));
                        scope.setEllipse(ellipse);
                    } else {
                        return false;
                    }
                }
                break;
                case Polygon: {
                    String[] items = areaData.split(TableImageScope.DataSeparator);
                    DoublePolygon polygon = new DoublePolygon();
                    for (int i = 0; i < items.length / 2; ++i) {
                        int x = (int) Double.parseDouble(items[i * 2]);
                        int y = (int) Double.parseDouble(items[i * 2 + 1]);
                        polygon.add(x, y);
                    }
                    scope.setPolygon(polygon);
                }
                break;
            }
            scope.setAreaData(areaData);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static String encodeOutline(ImageScope scope) {
        if (scope == null || scope.getScopeType() != ImageScope.ScopeType.Outline || scope.getOutline() == null) {
            return "";
        }
        String s = "";
        try {
            String prefix = AppPaths.getImageScopePath() + File.separator + scope.getScopeType() + "_";
            String filename = prefix + (new Date().getTime()) + "_" + new Random().nextInt(1000) + ".png";
            while (new File(filename).exists()) {
                filename = prefix + (new Date().getTime()) + "_" + new Random().nextInt(1000) + ".png";
            }
            if (ImageFileWriters.writeImageFile(scope.getOutlineSource(), "png", filename)) {
                s = filename;
            }
            scope.setOutlineName(filename);
        } catch (Exception e) {
            MyBoxLog.error(e);
            s = "";
        }
        return s;
    }

}
