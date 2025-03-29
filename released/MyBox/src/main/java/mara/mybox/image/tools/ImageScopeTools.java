package mara.mybox.image.tools;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.controller.BaseController;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.data.ImageItem;
import mara.mybox.data.IntPoint;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.DataNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.image.FxImageTools;
import mara.mybox.fxml.image.ScopeTools;
import mara.mybox.image.data.ImageScope;
import mara.mybox.image.data.ImageScope.ShapeType;
import static mara.mybox.image.data.ImageScope.ShapeType.Matting4;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.value.AppPaths;
import mara.mybox.value.InternalImages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-8-1 16:22:41
 * @License Apache License Version 2.0
 */
public class ImageScopeTools {

    public static void cloneValues(ImageScope targetScope, ImageScope sourceScope) {
        try {
            targetScope.setBackground(sourceScope.getBackground());
            targetScope.setName(sourceScope.getName());
            targetScope.setShapeData(sourceScope.getShapeData());
            targetScope.setColorData(sourceScope.getColorData());
            targetScope.setOutlineName(sourceScope.getOutlineName());
            List<IntPoint> npoints = new ArrayList<>();
            if (sourceScope.getPoints() != null) {
                npoints.addAll(sourceScope.getPoints());
            }
            targetScope.setPoints(npoints);
            targetScope.setRectangle(sourceScope.getRectangle() != null ? sourceScope.getRectangle().copy() : null);
            targetScope.setCircle(sourceScope.getCircle() != null ? sourceScope.getCircle().copy() : null);
            targetScope.setEllipse(sourceScope.getEllipse() != null ? sourceScope.getEllipse().copy() : null);
            targetScope.setPolygon(sourceScope.getPolygon() != null ? sourceScope.getPolygon().copy() : null);
            targetScope.setShapeExcluded(sourceScope.isShapeExcluded());

            targetScope.setColors(sourceScope.getColors());
            targetScope.setColorExcluded(sourceScope.isColorExcluded());
            sourceScope.getColorMatch().copyTo(targetScope.getColorMatch());

            targetScope.setMaskOpacity(sourceScope.getMaskOpacity());
            targetScope.setOutlineSource(sourceScope.getOutlineSource());
            targetScope.setOutline(sourceScope.getOutline());
            targetScope.setOutlineName(sourceScope.getOutlineName());
            targetScope.setClip(sourceScope.getClip());
            targetScope.setMaskColor(sourceScope.getMaskColor());
            targetScope.setMaskOpacity(sourceScope.getMaskOpacity());
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
            targetScope.setShapeType(sourceScope.getShapeType());
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

    /*
       make scope from
     */
    public static ImageScope fromDataNode(FxTask task, BaseController controller, DataNode node) {
        try {
            if (node == null) {
                return null;
            }
            ImageScope scope = new ImageScope();
            scope.setName(node.getTitle());
            scope.setShapeType(node.getStringValue("shape_type"));
            scope.setShapeData(node.getStringValue("shape_data"));
            scope.setShapeExcluded(node.getBooleanValue("shape_excluded"));
            scope.setColorAlgorithm(node.getStringValue("color_algorithm"));
            scope.setColorData(node.getStringValue("color_data"));
            scope.setColorThreshold(node.getDoubleValue("color_threshold"));
            scope.setColorWeights(node.getStringValue("color_weights"));
            scope.setColorExcluded(node.getBooleanValue("color_excluded"));
            scope.setBackground(node.getStringValue("background_file"));
            scope.setOutlineName(node.getStringValue("outline_file"));
            scope.decode(task);
            return scope;
        } catch (Exception e) {
//            MyBoxLog.error(e);
            return null;
        }
    }

    /*
       convert scope to
     */
    public static DataNode toDataNode(DataNode inNode, ImageScope scope) {
        try {
            if (scope == null) {
                return null;
            }
            ShapeType type = scope.getShapeType();
            if (type == null) {
                type = ShapeType.Whole;
            }
            DataNode node = inNode;
            if (node == null) {
                node = DataNode.create();
            }
            node.setValue("shape_type", type.name());
            node.setValue("shape_data", encodeShapeData(scope));
            node.setValue("shape_excluded", scope.isShapeExcluded());
            node.setValue("color_algorithm", scope.getColorAlgorithm().name());
            node.setValue("color_data", encodeColorData(scope));
            node.setValue("color_excluded", scope.isColorExcluded());
            node.setValue("color_threshold", scope.getColorThreshold());
            node.setValue("color_weights", scope.getColorWeights());
            node.setValue("background_file", scope.getBackground());
            node.setValue("outline_file", encodeOutline(null, scope));
            return node;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    // scope should have been decoded
    public static String toHtml(FxTask task, ImageScope scope) {
        try {
            if (scope == null || scope.isWhole()) {
                return null;
            }
            String html = "";
            StringTable htmlTable = new StringTable();
            List<String> row;
            try {
                ImageItem item = new ImageItem(scope.getBackground());
                Image image = item.readImage();
                if (image == null) {
                    image = new Image(InternalImages.exampleImageName());
                }
                if (task != null && !task.isWorking()) {
                    return null;
                }
                if (scope.getShapeType() == ShapeType.Outline) {
                    makeOutline(task, scope, image, scope.getRectangle(), true);
                }
                image = ScopeTools.maskScope(task, image, scope, false, true);
                if (task != null && !task.isWorking()) {
                    return null;
                }
                if (image != null) {
                    File imgFile = FxImageTools.writeImage(task, image);
                    if (imgFile != null) {
                        html = "<P align=\"center\"><Img src='"
                                + imgFile.toURI().toString() + "' width=500></P><BR>";
                    }
                }
                String v = scope.getName();
                if (v != null && !v.isBlank()) {
                    row = new ArrayList<>();
                    row.addAll(Arrays.asList(message("Name"), "<PRE><CODE>" + v + "</CODE></PRE>"));
                    htmlTable.add(row);
                }
                row = new ArrayList<>();
                row.addAll(Arrays.asList(message("ShapeType"), message(scope.getShapeType().name())));
                htmlTable.add(row);
                v = scope.getBackground();
                if (v != null && !v.isBlank()) {
                    row = new ArrayList<>();
                    row.addAll(Arrays.asList(message("Background"), "<PRE><CODE>" + v + "</CODE></PRE>"));
                    htmlTable.add(row);
                }
                v = scope.getOutlineName();
                if (v != null && !v.isBlank()) {
                    row = new ArrayList<>();
                    row.addAll(Arrays.asList(message("Outline"), "<PRE><CODE>" + v + "</CODE></PRE>"));
                    htmlTable.add(row);
                }
                row = new ArrayList<>();
                row.addAll(Arrays.asList(message("ColorMatchAlgorithm"),
                        message(scope.getColorAlgorithm().name())));
                htmlTable.add(row);
                v = scope.getShapeData();
                if (v != null && !v.isBlank()) {
                    row = new ArrayList<>();
                    row.addAll(Arrays.asList(message("Shape"), v));
                    htmlTable.add(row);
                }
                v = scope.getColorData();
                if (v != null && !v.isBlank()) {
                    row = new ArrayList<>();
                    row.addAll(Arrays.asList(message("Colors"), v));
                    htmlTable.add(row);
                }
                row = new ArrayList<>();
                row.addAll(Arrays.asList(message("ColorMatchThreshold"), scope.getColorThreshold() + ""));
                htmlTable.add(row);
                row = new ArrayList<>();
                row.addAll(Arrays.asList(message("ShapeExcluded"), scope.isShapeExcluded() ? message("Yes") : ""));
                htmlTable.add(row);
                row = new ArrayList<>();
                row.addAll(Arrays.asList(message("ColorExcluded"), scope.isColorExcluded() ? message("Yes") : ""));
                htmlTable.add(row);
            } catch (Exception e) {
            }
            return html + htmlTable.div();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DoubleRectangle makeOutline(FxTask task, ImageScope scope, Image bgImage,
            DoubleRectangle reck, boolean keepRatio) {
        try {
            if (scope.getOutlineSource() == null) {
                if (scope.getOutlineName() != null) {
                    Image image = new ImageItem(scope.getOutlineName()).readImage();
                    if (image != null) {
                        scope.setOutlineSource(SwingFXUtils.fromFXImage(image, null));
                    }
                }
            }
            if (scope.getOutlineSource() == null) {
                return null;
            }
            BufferedImage[] outline = AlphaTools.outline(task,
                    scope.getOutlineSource(),
                    reck,
                    (int) bgImage.getWidth(),
                    (int) bgImage.getHeight(),
                    keepRatio);
            if (outline == null || (task != null && !task.isWorking())) {
                return null;
            }
            DoubleRectangle outlineReck = DoubleRectangle.xywh(
                    reck.getX(), reck.getY(),
                    outline[0].getWidth(), outline[0].getHeight());
            scope.setOutline(outline[1]);
            scope.setRectangle(outlineReck.copy());
            return outlineReck;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
       extract value from scope
     */
    public static Image encodeBackground(FxTask task, String address) {
        try {
            String background = address;
            ImageItem item = new ImageItem(background);
            Image image = item.readImage();
            if (image == null) {
                background = InternalImages.exampleImageName();
                image = new Image(background);
            }
            return image;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static boolean decodeColorData(ImageScope scope) {
        if (scope == null) {
            return false;
        }
        return decodeColorData(scope.getColorData(), scope);
    }

    public static boolean decodeColorData(String colorData, ImageScope scope) {
        if (colorData == null || scope == null) {
            return false;
        }
        try {
            List<Color> colors = new ArrayList<>();
            if (!colorData.isBlank()) {
                String[] items = colorData.split(ImageScope.ValueSeparator);
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
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static String encodeColorData(ImageScope scope) {
        if (scope == null) {
            return "";
        }
        String s = "";
        try {
            List<Color> colors = scope.getColors();
            if (colors != null) {
                for (Color color : colors) {
                    s += color.getRGB() + ImageScope.ValueSeparator;
                }
                if (s.endsWith(ImageScope.ValueSeparator)) {
                    s = s.substring(0, s.length() - ImageScope.ValueSeparator.length());
                }
            }
            scope.setColorData(s);
        } catch (Exception e) {
            MyBoxLog.error(e);
            s = "";
        }
        return s;
    }

    public static String encodeShapeData(ImageScope scope) {
        if (scope == null) {
            return "";
        }
        String s = "";
        try {
            switch (scope.getShapeType()) {
                case Matting4:
                case Matting8: {
                    List<IntPoint> points = scope.getPoints();
                    if (points != null) {
                        for (IntPoint p : points) {
                            s += p.getX() + ImageScope.ValueSeparator + p.getY() + ImageScope.ValueSeparator;
                        }
                        if (s.endsWith(ImageScope.ValueSeparator)) {
                            s = s.substring(0, s.length() - ImageScope.ValueSeparator.length());
                        }
                    }
                }
                break;
                case Rectangle:
                case Outline:
                    DoubleRectangle rect = scope.getRectangle();
                    if (rect != null) {
                        s = (int) rect.getX() + ImageScope.ValueSeparator
                                + (int) rect.getY() + ImageScope.ValueSeparator
                                + (int) rect.getMaxX() + ImageScope.ValueSeparator
                                + (int) rect.getMaxY();
                    }
                    break;
                case Circle:
                    DoubleCircle circle = scope.getCircle();
                    if (circle != null) {
                        s = (int) circle.getCenterX() + ImageScope.ValueSeparator
                                + (int) circle.getCenterY() + ImageScope.ValueSeparator
                                + (int) circle.getRadius();
                    }
                    break;
                case Ellipse:
                    DoubleEllipse ellipse = scope.getEllipse();
                    if (ellipse != null) {
                        s = (int) ellipse.getX() + ImageScope.ValueSeparator
                                + (int) ellipse.getY() + ImageScope.ValueSeparator
                                + (int) ellipse.getMaxX() + ImageScope.ValueSeparator
                                + (int) ellipse.getMaxY();
                    }
                    break;
                case Polygon:
                    DoublePolygon polygon = scope.getPolygon();
                    if (polygon != null) {
                        for (Double d : polygon.getData()) {
                            s += Math.round(d) + ImageScope.ValueSeparator;
                        }
                        if (s.endsWith(ImageScope.ValueSeparator)) {
                            s = s.substring(0, s.length() - ImageScope.ValueSeparator.length());
                        }
                    }
                    break;
            }
            scope.setShapeData(s);
        } catch (Exception e) {
            MyBoxLog.error(e);
            s = "";
        }
        return s;
    }

    public static boolean decodeShapeData(ImageScope scope) {
        try {
            if (scope == null) {
                return false;
            }
            ShapeType type = scope.getShapeType();
            String shapeData = scope.getShapeData();
            if (shapeData == null) {
                return false;
            }
            switch (type) {
                case Matting4:
                case Matting8: {
                    String[] items = shapeData.split(ImageScope.ValueSeparator);
                    for (int i = 0; i < items.length / 2; ++i) {
                        int x = (int) Double.parseDouble(items[i * 2]);
                        int y = (int) Double.parseDouble(items[i * 2 + 1]);
                        scope.addPoint(x, y);
                    }
                }
                break;
                case Rectangle:
                case Outline: {
                    String[] items = shapeData.split(ImageScope.ValueSeparator);
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
                    String[] items = shapeData.split(ImageScope.ValueSeparator);
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
                    String[] items = shapeData.split(ImageScope.ValueSeparator);
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
                    String[] items = shapeData.split(ImageScope.ValueSeparator);
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
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static String encodeOutline(FxTask task, ImageScope scope) {
        if (scope == null
                || scope.getShapeType() != ImageScope.ShapeType.Outline) {
            return "";
        }
        String outlineName = scope.getOutlineName();
        try {
            if (outlineName != null
                    && (outlineName.startsWith("img/") || outlineName.startsWith("buttons/"))) {
                return outlineName;
            }
            BufferedImage outlineSource = scope.getOutlineSource();
            if (outlineSource == null) {
                return "";
            }
            String prefix = AppPaths.getImageScopePath() + File.separator + scope.getShapeType() + "_";
            String name = prefix + (new Date().getTime()) + "_" + new Random().nextInt(1000) + ".png";
            while (new File(name).exists()) {
                name = prefix + (new Date().getTime()) + "_" + new Random().nextInt(1000) + ".png";
            }
            if (ImageFileWriters.writeImageFile(task, outlineSource, "png", name)) {
                outlineName = name;
            }
            scope.setOutlineName(outlineName);
        } catch (Exception e) {
//            MyBoxLog.error(e);
            outlineName = "";
        }
        return outlineName;
    }

}
