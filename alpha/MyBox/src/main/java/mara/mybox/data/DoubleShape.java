package mara.mybox.data;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.BaseImageController;
import mara.mybox.controller.ControlSvgNodeEdit;
import mara.mybox.controller.TextEditorController;
import mara.mybox.controller.TextPopController;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.tools.DoubleTools.imageScale;
import static mara.mybox.value.Languages.message;
import org.w3c.dom.Element;

/**
 * @Author Mara
 * @CreateDate 2019-04-02
 * @License Apache License Version 2.0
 */
public interface DoubleShape {

    DoubleShape copy();

    boolean isValid();

    boolean isEmpty();

    Shape getShape();

    boolean translateRel(double offsetX, double offsetY);

    boolean scale(double scaleX, double scaleY);

    String name();

    String pathRel();

    String pathAbs();

    String elementAbs();

    String elementRel();

    /*
        static
     */
    public static enum ShapeType {
        Line, Rectangle, Circle, Ellipse, Polygon, Polyline, Polylines,
        Cubic, Quadratic, Arc, Path, Text;
    }

    public static final double ChangeThreshold = 0.01;

    public static boolean changed(double offsetX, double offsetY) {
        return Math.abs(offsetX) > ChangeThreshold || Math.abs(offsetY) > ChangeThreshold;
    }

    public static boolean changed(DoublePoint p1, DoublePoint p2) {
        if (p1 == null || p2 == null) {
            return false;
        }
        return changed(p1.getX() - p2.getX(), p1.getY() - p2.getY());
    }

    public static boolean translateCenterAbs(DoubleShape shapeData, double x, double y) {
        DoublePoint center = getCenter(shapeData);
        double offsetX = x - center.getX();
        double offsetY = y - center.getY();
        if (DoubleShape.changed(offsetX, offsetY)) {
            return shapeData.translateRel(offsetX, offsetY);
        }
        return false;
    }

    public static boolean translateRel(DoubleShape shapeData, double offsetX, double offsetY) {
        if (DoubleShape.changed(offsetX, offsetY)) {
            return shapeData.translateRel(offsetX, offsetY);
        }
        return false;
    }

    public static boolean scale(DoubleShape shapeData, double scaleX, double scaleY) {
        try {
            if (shapeData == null) {
                return true;
            }
            DoublePoint c = getCenter(shapeData);
            if (shapeData.scale(scaleX, scaleY)) {
                DoubleShape.translateCenterAbs(shapeData, c.getX(), c.getY());
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static DoublePath rorate(DoubleShape shapeData, double angle, double x, double y) {
        try {
            if (shapeData == null) {
                return null;
            }
            AffineTransform t = AffineTransform.getRotateInstance(Math.toRadians(angle), x, y);
            Shape shape = t.createTransformedShape(shapeData.getShape());
            return DoublePath.shapeToPathData(shape);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DoublePath shear(DoubleShape shapeData, double x, double y) {
        try {
            if (shapeData == null) {
                return null;
            }
            AffineTransform t = AffineTransform.getShearInstance(x, y);
            Shape shape = t.createTransformedShape(shapeData.getShape());
            return DoublePath.shapeToPathData(shape);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DoublePath pathData(DoubleShape shapeData) {
        try {
            if (shapeData == null) {
                return null;
            }
            return DoublePath.shapeToPathData(shapeData.getShape());
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    // notice bound may truncate values
    public static Rectangle2D getBound(DoubleShape shapeData) {
        return shapeData.getShape().getBounds2D();
    }

    public static boolean contains(DoubleShape shapeData, double x, double y) {
        return shapeData.isValid() && shapeData.getShape().contains(x, y);
    }

    public static DoublePoint getCenter(DoubleShape shapeData) {
        Rectangle2D bound = getBound(shapeData);
        return new DoublePoint(bound.getCenterX(), bound.getCenterY());
    }

    public static String values(DoubleShape shapeData) {
        Rectangle2D bounds = getBound(shapeData);
        return shapeData.name() + "\n"
                + message("LeftTop") + ": " + imageScale(bounds.getMinX()) + ", " + imageScale(bounds.getMinY()) + "\n"
                + message("RightBottom") + ": " + imageScale(bounds.getMaxX()) + ", " + imageScale(bounds.getMaxY()) + "\n"
                + message("Center") + ": " + imageScale(bounds.getCenterX()) + ", " + imageScale(bounds.getCenterY()) + "\n"
                + message("Width") + ": " + imageScale(bounds.getWidth()) + "  " + message("Height") + ": " + imageScale(bounds.getHeight());
    }

    public static DoubleShape toShape(BaseController controller, Element node) {
        try {
            if (node == null) {
                return null;
            }
            switch (node.getNodeName().toLowerCase()) {
                case "rect":
                    return toRect(node);
                case "circle":
                    return toCircle(node);
                case "ellipse":
                    return toEllipse(node);
                case "line":
                    return toLine(node);
                case "polyline":
                    return toPolyline(node);
                case "polygon":
                    return toPolygon(node);
                case "path":
                    return toPath(controller, node);
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static DoubleRectangle toRect(Element node) {
        try {
            float x, y, w, h;
            try {
                x = Float.parseFloat(node.getAttribute("x"));
            } catch (Exception e) {
                return null;
            }
            try {
                y = Float.parseFloat(node.getAttribute("y"));
            } catch (Exception e) {
                return null;
            }
            try {
                w = Float.parseFloat(node.getAttribute("width"));
            } catch (Exception e) {
                w = -1f;
            }
            if (w <= 0) {
                return null;
            }
            try {
                h = Float.parseFloat(node.getAttribute("height"));
            } catch (Exception e) {
                h = -1f;
            }
            if (h <= 0) {
                return null;
            }
            return DoubleRectangle.xywh(x, y, w, h);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DoubleCircle toCircle(Element node) {
        try {
            float x, y, r;
            try {
                x = Float.parseFloat(node.getAttribute("cx"));
            } catch (Exception e) {
                return null;
            }
            try {
                y = Float.parseFloat(node.getAttribute("cy"));
            } catch (Exception e) {
                return null;
            }
            try {
                r = Float.parseFloat(node.getAttribute("r"));
            } catch (Exception e) {
                r = -1f;
            }
            if (r <= 0) {
                return null;
            }
            return new DoubleCircle(x, y, r);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DoubleEllipse toEllipse(Element node) {
        try {
            float cx, cy, rx, ry;
            try {
                cx = Float.parseFloat(node.getAttribute("cx"));
            } catch (Exception e) {
                return null;
            }
            try {
                cy = Float.parseFloat(node.getAttribute("cy"));
            } catch (Exception e) {
                return null;
            }
            try {
                rx = Float.parseFloat(node.getAttribute("rx"));
            } catch (Exception e) {
                rx = -1f;
            }
            if (rx <= 0) {
                return null;
            }
            try {
                ry = Float.parseFloat(node.getAttribute("ry"));
            } catch (Exception e) {
                ry = -1f;
            }
            if (ry <= 0) {
                return null;
            }
            return DoubleEllipse.ellipse(cx, cy, rx, ry);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DoubleLine toLine(Element node) {
        try {
            float x1, y1, x2, y2;
            try {
                x1 = Float.parseFloat(node.getAttribute("x1"));
            } catch (Exception e) {
                return null;
            }
            try {
                y1 = Float.parseFloat(node.getAttribute("y1"));
            } catch (Exception e) {
                return null;
            }
            try {
                x2 = Float.parseFloat(node.getAttribute("x2"));
            } catch (Exception e) {
                return null;
            }
            try {
                y2 = Float.parseFloat(node.getAttribute("y2"));
            } catch (Exception e) {
                return null;
            }
            return new DoubleLine(x1, y1, x2, y2);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DoublePolyline toPolyline(Element node) {
        try {
            DoublePolyline polyline = new DoublePolyline();
            polyline.setAll(DoublePoint.parseImageCoordinates(node.getAttribute("points")));
            return polyline;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DoublePolygon toPolygon(Element node) {
        try {
            DoublePolygon polygon = new DoublePolygon();
            polygon.setAll(DoublePoint.parseImageCoordinates(node.getAttribute("points")));
            return polygon;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DoublePath toPath(BaseController controller, Element node) {
        try {
            String d = node.getAttribute("d");
            return new DoublePath(controller, d);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<MenuItem> elementMenu(BaseController controller, Element node) {
        return svgMenu(controller, toShape(controller, node));
    }

    public static List<MenuItem> svgMenu(BaseController controller, DoubleShape shapeData) {
        if (shapeData == null) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;

        if (shapeData instanceof DoublePath) {
            DoublePath pathData = (DoublePath) shapeData;
            menu = new MenuItem(message("ConvertToAbsoluteCoordinates"), StyleTools.getIconImageView("iconDelimiter.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    if (pathData.toAbs(controller)) {
                        if (controller instanceof BaseImageController) {
                            ((BaseImageController) controller).maskShapeDataChanged();
                        } else if (controller instanceof ControlSvgNodeEdit) {
                            ((ControlSvgNodeEdit) controller).loadPath(pathData.getContent());
                        }
                    }
                }
            });
            items.add(menu);

            menu = new MenuItem(message("ConvertToRelativeCoordinates"), StyleTools.getIconImageView("iconDelimiter.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    if (pathData.toRel(controller)) {
                        if (controller instanceof BaseImageController) {
                            ((BaseImageController) controller).maskShapeDataChanged();
                        } else if (controller instanceof ControlSvgNodeEdit) {
                            ((ControlSvgNodeEdit) controller).loadPath(pathData.getContent());
                        }
                    }
                }
            });
            items.add(menu);

            menu = new MenuItem(message("Pop"), StyleTools.getIconImageView("iconPop.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    TextPopController.loadText(controller, pathData.getContent());
                }
            });
            items.add(menu);

            menu = new MenuItem(message("TextEditer"), StyleTools.getIconImageView("iconEdit.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    TextEditorController.edit(pathData.getContent());
                }
            });
            items.add(menu);
        }

        if (shapeData instanceof DoublePath || shapeData instanceof DoubleQuadratic
                || shapeData instanceof DoubleCubic || shapeData instanceof DoubleArc) {
            menu = new MenuItem(message("DisplaySVGElement") + " - " + message("AbsoluteCoordinate"), StyleTools.getIconImageView("iconView.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    TextPopController.loadText(controller, shapeData.elementAbs());
                }
            });
            items.add(menu);

            menu = new MenuItem(message("DisplaySVGElement") + " - " + message("RelativeCoordinate"), StyleTools.getIconImageView("iconView.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    TextPopController.loadText(controller, shapeData.elementRel());
                }
            });
            items.add(menu);
        } else {
            menu = new MenuItem(message("DisplaySVGElement"), StyleTools.getIconImageView("iconView.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    TextPopController.loadText(controller, shapeData.elementAbs());
                }
            });
            items.add(menu);

            menu = new MenuItem(message("DisplaySVGPath") + " - " + message("AbsoluteCoordinate"), StyleTools.getIconImageView("iconView.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    TextPopController.loadText(controller, "<path d=\"\n" + shapeData.pathAbs() + "\n\">");
                }
            });
            items.add(menu);

            menu = new MenuItem(message("DisplaySVGPath") + " - " + message("RelativeCoordinate"), StyleTools.getIconImageView("iconView.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    TextPopController.loadText(controller, "<path d=\"\n" + shapeData.pathRel() + "\n\">");
                }
            });
            items.add(menu);

        }

        items.add(new SeparatorMenuItem());

        return items;

    }

}
