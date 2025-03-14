package mara.mybox.db.migration;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import mara.mybox.controller.BaseController;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.data.IntPoint;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.migration.OldImageScope.ColorScopeType;
import mara.mybox.db.migration.OldImageScope.ScopeType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.JsonTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.XmlTools;
import static mara.mybox.tools.XmlTools.cdata;
import mara.mybox.value.AppPaths;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @Author Mara
 * @CreateDate 2018-8-1 16:22:41
 * @License Apache License Version 2.0
 */
public class OldImageScopeTools {

    public static OldImageScope.ScopeType scopeType(String type) {
        if (type == null) {
            return null;
        }
        if ("Matting".equalsIgnoreCase(type)) {
            return OldImageScope.ScopeType.Matting;
        }
        if ("Rectangle".equalsIgnoreCase(type) || "RectangleColor".equalsIgnoreCase(type)) {
            return OldImageScope.ScopeType.Rectangle;
        }
        if ("Circle".equalsIgnoreCase(type) || "CircleColor".equalsIgnoreCase(type)) {
            return OldImageScope.ScopeType.Circle;
        }
        if ("Ellipse".equalsIgnoreCase(type) || "EllipseColor".equalsIgnoreCase(type)) {
            return OldImageScope.ScopeType.Ellipse;
        }
        if ("Polygon".equalsIgnoreCase(type) || "PolygonColor".equalsIgnoreCase(type)) {
            return OldImageScope.ScopeType.Polygon;
        }
        if ("Color".equalsIgnoreCase(type)) {
            return OldImageScope.ScopeType.Colors;
        }
        if ("Outline".equalsIgnoreCase(type)) {
            return OldImageScope.ScopeType.Outline;
        }
        return null;
    }

    public static void cloneValues(OldImageScope targetScope, OldImageScope sourceScope) {
        try {
            targetScope.setFile(sourceScope.getFile());
            targetScope.setName(sourceScope.getName());
            targetScope.setAreaData(sourceScope.getAreaData());
            targetScope.setColorData(sourceScope.getColorData());
            targetScope.setOutlineName(sourceScope.getOutlineName());
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
            targetScope.setRectangle(sourceScope.getRectangle() != null ? sourceScope.getRectangle().copy() : null);
            targetScope.setCircle(sourceScope.getCircle() != null ? sourceScope.getCircle().copy() : null);
            targetScope.setEllipse(sourceScope.getEllipse() != null ? sourceScope.getEllipse().copy() : null);
            targetScope.setPolygon(sourceScope.getPolygon() != null ? sourceScope.getPolygon().copy() : null);
            targetScope.setColorDistance(sourceScope.getColorDistance());
            targetScope.setColorDistanceSquare(sourceScope.getColorDistanceSquare());
            targetScope.setHsbDistance(sourceScope.getHsbDistance());
            targetScope.setColorExcluded(sourceScope.isColorExcluded());
            targetScope.setDistanceSquareRoot(sourceScope.isDistanceSquareRoot());
            targetScope.setAreaExcluded(sourceScope.isAreaExcluded());
            targetScope.setMaskOpacity(sourceScope.getMaskOpacity());
            targetScope.setCreateTime(sourceScope.getCreateTime());
            targetScope.setModifyTime(sourceScope.getModifyTime());
            targetScope.setOutlineSource(sourceScope.getOutlineSource());
            targetScope.setOutline(sourceScope.getOutline());
            targetScope.setEightNeighbor(sourceScope.isEightNeighbor());
            targetScope.setClip(sourceScope.getClip());
            targetScope.setMaskColor(sourceScope.getMaskColor());
            targetScope.setMaskOpacity(sourceScope.getMaskOpacity());
        } catch (Exception e) {
            //            MyBoxLog.debug(e);
        }
    }

    public static OldImageScope cloneAll(OldImageScope sourceScope) {
        OldImageScope targetScope = new OldImageScope();
        OldImageScopeTools.cloneAll(targetScope, sourceScope);
        return targetScope;
    }

    public static void cloneAll(OldImageScope targetScope, OldImageScope sourceScope) {
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
                if (OldColorMatchTools.isColorMatchSquare(color, oColor, colorDistanceSqure)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (OldColorMatchTools.isColorMatchSquare(color, oColor, colorDistanceSqure)) {
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
                if (OldColorMatchTools.isRedMatch(color, oColor, colorDistance)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (OldColorMatchTools.isRedMatch(color, oColor, colorDistance)) {
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
                if (OldColorMatchTools.isGreenMatch(color, oColor, colorDistance)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (OldColorMatchTools.isGreenMatch(color, oColor, colorDistance)) {
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
                if (OldColorMatchTools.isBlueMatch(color, oColor, colorDistance)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (OldColorMatchTools.isBlueMatch(color, oColor, colorDistance)) {
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
                if (OldColorMatchTools.isHueMatch(color, oColor, hsbDistance)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (OldColorMatchTools.isHueMatch(color, oColor, hsbDistance)) {
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
                if (OldColorMatchTools.isSaturationMatch(color, oColor, hsbDistance)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (OldColorMatchTools.isSaturationMatch(color, oColor, hsbDistance)) {
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
                if (OldColorMatchTools.isBrightnessMatch(color, oColor, hsbDistance)) {
                    return false;
                }
            }
            return true;
        } else {

            for (Color oColor : colors) {
                if (OldColorMatchTools.isBrightnessMatch(color, oColor, hsbDistance)) {
                    return true;
                }
            }
            return false;
        }
    }

    /*
       make scope from
     */
    public static OldImageScope fromDataNode(FxTask task, BaseController controller, DataNode node) {
        try {
            if (node == null) {
                return null;
            }
            OldImageScope scope = new OldImageScope();
            scope.setName(node.getTitle());
            scope.setScopeType(OldImageScopeTools.scopeType(node.getStringValue("scope_type")));
            scope.setColorScopeType(OldImageScope.ColorScopeType.valueOf(node.getStringValue("color_type")));
            scope.setAreaData(node.getStringValue("area_data"));
            scope.setColorData(node.getStringValue("color_data"));
            scope.setColorDistance(node.getIntValue("color_data"));
            scope.setAreaExcluded(node.getBooleanValue("area_excluded"));
            scope.setColorExcluded(node.getBooleanValue("color_excluded"));
            scope.setFile(node.getStringValue("background_file"));
            scope.setOutlineName(node.getStringValue("outline_file"));
            if (task != null && !task.isWorking()) {
                return null;
            }
            decodeColorData(scope);
            decodeAreaData(scope);
            decodeOutline(task, scope);
            return scope;
        } catch (Exception e) {
//            MyBoxLog.error(e);
            return null;
        }
    }

    public static OldImageScope fromXML(FxTask task, BaseController controller, String s) {
        try {
            if (s == null || s.isBlank()) {
                return null;
            }
            Element e = XmlTools.toElement(task, controller, s);
            if (e == null || (task != null && !task.isWorking())) {
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
            OldImageScope scope = new OldImageScope();
            for (int dIndex = 0; dIndex < children.getLength(); dIndex++) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
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
                        scope.setScopeType(OldImageScopeTools.scopeType(value));

                    } else if (XmlTools.matchXmlTag("ScopeColorType", tag)) {
                        scope.setColorScopeType(OldImageScope.ColorScopeType.valueOf(value));

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
            decodeColorData(scope);
            decodeAreaData(scope);
            decodeOutline(task, scope);
            return scope;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
       convert scope to
     */
    public static DataNode toDataNode(DataNode inNode, OldImageScope scope) {
        try {
            if (scope == null) {
                return null;
            }
            ScopeType type = scope.getScopeType();
            if (type == null) {
                return null;
            }
            DataNode node = inNode;
            if (node == null) {
                node = DataNode.create();
            }
            node.setValue("scope_type", type.name());
            node.setValue("color_type", scope.getColorScopeType().name());
            node.setValue("area_data", OldImageScopeTools.encodeAreaData(scope));
            node.setValue("area_excluded", scope.isAreaExcluded());
            node.setValue("color_data", OldImageScopeTools.encodeColorData(scope));
            node.setValue("color_excluded", scope.isColorExcluded());
            node.setValue("color_distance", scope.getColorDistance());
            node.setValue("background_file", scope.getFile());
            node.setValue("outline_file", OldImageScopeTools.encodeOutline(null, scope));
            return node;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String toXML(OldImageScope scope, String inPrefix) {
        try {
            if (scope == null || scope.getScopeType() == null) {
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
            v = OldImageScopeTools.encodeOutline(null, scope);
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
            v = OldImageScopeTools.encodeAreaData(scope);
            if (v != null && !v.isBlank()) {
                s.append(prefix).append("<").append(XmlTools.xmlTag("Area")).append(">")
                        .append("<![CDATA[").append(v).append("]]>")
                        .append("</").append(XmlTools.xmlTag("Area")).append(">\n");
            }
            v = OldImageScopeTools.encodeColorData(scope);
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

    public static String toJSON(OldImageScope scope, String inPrefix) {
        try {
            if (scope == null || scope.getScopeType() == null) {
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
            v = OldImageScopeTools.encodeOutline(null, scope);
            if (v != null && !v.isBlank()) {
                s.append(prefix).append("\"").append(message("Outline")).append("\": ")
                        .append(JsonTools.encode(v)).append(",\n");
            }
            ColorScopeType ctype = scope.getColorScopeType();
            if (ctype != null) {
                s.append(prefix).append("\"").append(message("ScopeColorType")).append("\": ")
                        .append(JsonTools.encode(ctype.name())).append(",\n");
            }
            v = OldImageScopeTools.encodeAreaData(scope);
            if (v != null && !v.isBlank()) {
                s.append(prefix).append("\"").append(message("Area")).append("\": ")
                        .append(JsonTools.encode(v)).append(",\n");
            }
            v = OldImageScopeTools.encodeColorData(scope);
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

    /*
       extract value from scope
     */
    public static boolean decodeColorData(OldImageScope scope) {
        if (scope == null || scope.getScopeType() == null) {
            return false;
        }
        return decodeColorData(scope.getScopeType(), scope.getColorData(), scope);
    }

    public static boolean decodeColorData(OldImageScope.ScopeType type, String colorData, OldImageScope scope) {
        if (type == null || colorData == null || scope == null) {
            return false;
        }
        try {
            switch (type) {
                case Colors:
                case Rectangle:
                case Circle:
                case Ellipse:
                case Polygon: {
                    List<Color> colors = new ArrayList<>();
                    if (!colorData.isBlank()) {
                        String[] items = colorData.split(OldImageScope.ValueSeparator);
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

    public static boolean decodeOutline(FxTask task, OldImageScope scope) {
        if (scope == null || scope.getScopeType() == null) {
            return false;
        }
        return decodeOutline(task, scope.getScopeType(), scope.getOutlineName(), scope);
    }

    public static boolean decodeOutline(FxTask task, OldImageScope.ScopeType type,
            String outline, OldImageScope scope) {
        if (type == null || outline == null || scope == null) {
            return false;
        }
        if (type != OldImageScope.ScopeType.Outline) {
            return true;
        }
        try {
            scope.setOutlineName(outline);
            BufferedImage image = ImageFileReaders.readImage(task, new File(outline));
            if (task != null && !task.isWorking()) {
                return false;
            }
            scope.setOutlineSource(image);
            return image != null;
        } catch (Exception e) {
            MyBoxLog.error(e);
            //            MyBoxLog.debug(e);
            return false;
        }
    }

    public static String encodeColorData(OldImageScope scope) {
        if (scope == null || scope.getScopeType() == null) {
            return "";
        }
        String s = "";
        try {
            switch (scope.getScopeType()) {
                case Colors:
                case Rectangle:
                case Circle:
                case Ellipse:
                case Polygon:
                    List<Color> colors = scope.getColors();
                    if (colors != null) {
                        for (Color color : colors) {
                            s += color.getRGB() + OldImageScope.ValueSeparator;
                        }
                        if (s.endsWith(OldImageScope.ValueSeparator)) {
                            s = s.substring(0, s.length() - OldImageScope.ValueSeparator.length());
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

    public static String encodeAreaData(OldImageScope scope) {
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
                            s += p.getX() + OldImageScope.ValueSeparator + p.getY() + OldImageScope.ValueSeparator;
                        }
                        if (s.endsWith(OldImageScope.ValueSeparator)) {
                            s = s.substring(0, s.length() - OldImageScope.ValueSeparator.length());
                        }
                    }
                }
                break;
                case Rectangle:
                case Outline:
                    DoubleRectangle rect = scope.getRectangle();
                    if (rect != null) {
                        s = (int) rect.getX() + OldImageScope.ValueSeparator + (int) rect.getY() + OldImageScope.ValueSeparator + (int) rect.getMaxX() + OldImageScope.ValueSeparator + (int) rect.getMaxY();
                    }
                    break;
                case Circle:
                    DoubleCircle circle = scope.getCircle();
                    if (circle != null) {
                        s = (int) circle.getCenterX() + OldImageScope.ValueSeparator + (int) circle.getCenterY() + OldImageScope.ValueSeparator + (int) circle.getRadius();
                    }
                    break;
                case Ellipse:
                    DoubleEllipse ellipse = scope.getEllipse();
                    if (ellipse != null) {
                        s = (int) ellipse.getX() + OldImageScope.ValueSeparator + (int) ellipse.getY() + OldImageScope.ValueSeparator + (int) ellipse.getMaxX() + OldImageScope.ValueSeparator + (int) ellipse.getMaxY();
                    }
                    break;
                case Polygon:
                    DoublePolygon polygon = scope.getPolygon();
                    if (polygon != null) {
                        for (Double d : polygon.getData()) {
                            s += Math.round(d) + OldImageScope.ValueSeparator;
                        }
                        if (s.endsWith(OldImageScope.ValueSeparator)) {
                            s = s.substring(0, s.length() - OldImageScope.ValueSeparator.length());
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

    public static boolean decodeAreaData(OldImageScope scope) {
        if (scope == null || scope.getScopeType() == null) {
            return false;
        }
        return decodeAreaData(scope.getScopeType(), scope.getAreaData(), scope);
    }

    public static boolean decodeAreaData(OldImageScope.ScopeType type, String areaData, OldImageScope scope) {
        if (type == null || areaData == null || scope == null) {
            return false;
        }
        try {
            switch (type) {
                case Matting: {
                    String[] items = areaData.split(OldImageScope.ValueSeparator);
                    for (int i = 0; i < items.length / 2; ++i) {
                        int x = (int) Double.parseDouble(items[i * 2]);
                        int y = (int) Double.parseDouble(items[i * 2 + 1]);
                        scope.addPoint(x, y);
                    }
                }
                break;
                case Rectangle:
                case Outline: {
                    String[] items = areaData.split(OldImageScope.ValueSeparator);
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
                    String[] items = areaData.split(OldImageScope.ValueSeparator);
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
                    String[] items = areaData.split(OldImageScope.ValueSeparator);
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
                    String[] items = areaData.split(OldImageScope.ValueSeparator);
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

    public static String encodeOutline(FxTask task, OldImageScope scope) {
        if (scope == null || scope.getScopeType() == null
                || scope.getScopeType() != OldImageScope.ScopeType.Outline
                || scope.getOutline() == null) {
            return "";
        }
        String s = "";
        try {
            String prefix = AppPaths.getImageScopePath() + File.separator + scope.getScopeType() + "_";
            String filename = prefix + (new Date().getTime()) + "_" + new Random().nextInt(1000) + ".png";
            while (new File(filename).exists()) {
                filename = prefix + (new Date().getTime()) + "_" + new Random().nextInt(1000) + ".png";
            }
            if (ImageFileWriters.writeImageFile(task, scope.getOutlineSource(), "png", filename)) {
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
