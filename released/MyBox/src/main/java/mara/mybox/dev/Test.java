package mara.mybox.dev;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import javafx.geometry.Bounds;

/**
 * @Author Mara
 * @CreateDate 2023-8-7
 * @License Apache License Version 2.0
 */
public class Test {

    /*  https://github.com/Mararsh/MyBox/issues/1816
    
        Following are results of checking these values in Java20:
    
        javafx.scene.shape.Rectangle: Rectangle[x=0.0, y=0.0, width=5.0, height=6.0, fill=0x000000ff]
        default isPickOnBounds: false
        rect.contains (rect.getX(), rect.getY()): true   rect.contains (rect.getWidth(), rect.getHeight()):false
        rect.getBoundsInLocal(): 
        bind.getMinX: 0.0   bind.getMinY: 0.0   bind.getMaxX: 5.0   bind.getMaxY: 6.0   bind.getMaxX: 5.0   bind.getWidth: 5.0   bind.getHeight: 6.0
        bind.contains (bind.getMinX(), bind.getMinY()): true   bind.contains (bind.getMaxX(), bind.getMaxY()):true   bind.contains (bind.getWidth(), bind.getHeight()):true
        ---------------------
        javafx.scene.shape.Rectangle: Rectangle[x=0.0, y=0.0, width=5.0, height=6.0, fill=0x000000ff]
        isPickOnBounds: true
        rect.contains (rect.getX(), rect.getY()): true   rect.contains (rect.getWidth(), rect.getHeight()):true
        rect.getBoundsInLocal(): 
        bind.getMinX: 0.0   bind.getMinY: 0.0   bind.getMaxX: 5.0   bind.getMaxY: 6.0   bind.getMaxX: 5.0   bind.getWidth: 5.0   bind.getHeight: 6.0
        bind.contains (bind.getMinX(), bind.getMinY()): true   bind.contains (bind.getMaxX(), bind.getMaxY()):true   bind.contains (bind.getWidth(), bind.getHeight()):true
        ---------------------
        java.awt.geom.Rectangle2D.Double: java.awt.geom.Rectangle2D$Double[x=0.0,y=0.0,w=5.0,h=6.0]
        rect2d.getMinX: 0.0   rect2d.getMinY: 0.0   rect2d.getMaxX: 5.0   rect2d.getMaxY: 6.0   rect2d.getMaxX: 5.0   rect2d.getWidth: 5.0   rect2d.getHeight: 6.0
        rect2d.contains (rect.getX(), rect2d.getY()): true   rect2d.contains (rect.getWidth(), rect2d.getHeight()):false
        rect2d.getBounds(): 
        bind2d.getMinX: 0.0   bind2d.getMinY: 0.0   bind2d.getMaxX: 5.0   bind2d.getMaxY: 6.0   bind2d.getMaxX: 5.0   bind2d.getWidth: 5.0   bind2d.getHeight: 6.0
        bind2d.contains (bind2d.getMinX(), bind2d.getMinY()): true   bind2d.contains (bind2d.getMaxX(), bind2d.getMaxY()):false   bind2d.contains (bind2d.getWidth(), bind2d.getHeight()):false
     */
    public static void rectangleBounds() {
        javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(0, 0, 5, 6);
        MyBoxLog.console("javafx.scene.shape.Rectangle: " + rect.toString());
        MyBoxLog.console("default isPickOnBounds: " + rect.isPickOnBounds());
        MyBoxLog.console("rect.contains (rect.getX(), rect.getY()): " + rect.contains(rect.getX(), rect.getY())
                + "   rect.contains (rect.getWidth(), rect.getHeight()):" + rect.contains(rect.getWidth(), rect.getHeight()));

        Bounds bind = rect.getBoundsInLocal();
        MyBoxLog.console("rect.getBoundsInLocal(): ");
        MyBoxLog.console("bind.getMinX: " + bind.getMinX()
                + "   bind.getMinY: " + bind.getMinY()
                + "   bind.getMaxX: " + bind.getMaxX()
                + "   bind.getMaxY: " + bind.getMaxY()
                + "   bind.getMaxX: " + bind.getMaxX()
                + "   bind.getWidth: " + bind.getWidth()
                + "   bind.getHeight: " + bind.getHeight());
        MyBoxLog.console("bind.contains (bind.getMinX(), bind.getMinY()): " + bind.contains(bind.getMinX(), bind.getMinY())
                + "   bind.contains (bind.getMaxX(), bind.getMaxY()):" + bind.contains(bind.getMaxX(), bind.getMaxY())
                + "   bind.contains (bind.getWidth(), bind.getHeight()):" + bind.contains(bind.getWidth(), bind.getHeight()));

        MyBoxLog.console("---------------------");
        rect.setPickOnBounds(!rect.isPickOnBounds());
        MyBoxLog.console("javafx.scene.shape.Rectangle: " + rect.toString());
        MyBoxLog.console("isPickOnBounds: " + rect.isPickOnBounds());
        MyBoxLog.console("rect.contains (rect.getX(), rect.getY()): " + rect.contains(rect.getX(), rect.getY())
                + "   rect.contains (rect.getWidth(), rect.getHeight()):" + rect.contains(rect.getWidth(), rect.getHeight()));

        bind = rect.getBoundsInLocal();
        MyBoxLog.console("rect.getBoundsInLocal(): ");
        MyBoxLog.console("bind.getMinX: " + bind.getMinX()
                + "   bind.getMinY: " + bind.getMinY()
                + "   bind.getMaxX: " + bind.getMaxX()
                + "   bind.getMaxY: " + bind.getMaxY()
                + "   bind.getMaxX: " + bind.getMaxX()
                + "   bind.getWidth: " + bind.getWidth()
                + "   bind.getHeight: " + bind.getHeight());
        MyBoxLog.console("bind.contains (bind.getMinX(), bind.getMinY()): " + bind.contains(bind.getMinX(), bind.getMinY())
                + "   bind.contains (bind.getMaxX(), bind.getMaxY()):" + bind.contains(bind.getMaxX(), bind.getMaxY())
                + "   bind.contains (bind.getWidth(), bind.getHeight()):" + bind.contains(bind.getWidth(), bind.getHeight()));

        MyBoxLog.console("---------------------");
        Rectangle2D.Double rect2d = new Rectangle2D.Double(0, 0, 5, 6);
        MyBoxLog.console("java.awt.geom.Rectangle2D.Double: " + rect2d.toString());
        MyBoxLog.console("rect2d.getMinX: " + rect2d.getMinX()
                + "   rect2d.getMinY: " + rect2d.getMinY()
                + "   rect2d.getMaxX: " + rect2d.getMaxX()
                + "   rect2d.getMaxY: " + rect2d.getMaxY()
                + "   rect2d.getMaxX: " + rect2d.getMaxX()
                + "   rect2d.getWidth: " + rect2d.getWidth()
                + "   rect2d.getHeight: " + rect2d.getHeight());
        MyBoxLog.console("rect2d.contains (rect.getX(), rect2d.getY()): " + rect2d.contains(rect2d.getX(), rect2d.getY())
                + "   rect2d.contains (rect.getWidth(), rect2d.getHeight()):" + rect2d.contains(rect2d.getWidth(), rect2d.getHeight()));

        Rectangle bind2d = rect2d.getBounds();
        MyBoxLog.console("rect2d.getBounds(): ");
        MyBoxLog.console("bind2d.getMinX: " + bind2d.getMinX()
                + "   bind2d.getMinY: " + bind2d.getMinY()
                + "   bind2d.getMaxX: " + bind2d.getMaxX()
                + "   bind2d.getMaxY: " + bind2d.getMaxY()
                + "   bind2d.getMaxX: " + bind2d.getMaxX()
                + "   bind2d.getWidth: " + bind2d.getWidth()
                + "   bind2d.getHeight: " + bind2d.getHeight());
        MyBoxLog.console("bind2d.contains (bind2d.getMinX(), bind2d.getMinY()): " + bind2d.contains(bind2d.getMinX(), bind2d.getMinY())
                + "   bind2d.contains (bind2d.getMaxX(), bind2d.getMaxY()):" + bind2d.contains(bind2d.getMaxX(), bind2d.getMaxY())
                + "   bind2d.contains (bind2d.getWidth(), bind2d.getHeight()):" + bind2d.contains(bind2d.getWidth(), bind2d.getHeight()));
    }

}
