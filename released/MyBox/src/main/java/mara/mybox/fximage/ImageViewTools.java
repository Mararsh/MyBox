package mara.mybox.fximage;

import javafx.geometry.Bounds;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mara.mybox.data.DoublePoint;
import mara.mybox.fxml.LocateTools;

/**
 * @Author Mara
 * @CreateDate 2018-6-11
 * @License Apache License Version 2.0
 */
public class ImageViewTools {

    public static void imageSize(ScrollPane sPane, ImageView iView) {
        try {
            if (iView == null || iView.getImage() == null || sPane == null) {
                return;
            }
            iView.setFitWidth(iView.getImage().getWidth());
            iView.setFitHeight(iView.getImage().getHeight());
            LocateTools.moveCenter(sPane, iView);
            //            MyBoxLog.console(iView.getImage().getWidth() + " " + iView.getImage().getHeight());
            //            iView.setLayoutY(10);
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
        }
    }

    public static void paneSize(ScrollPane sPane, ImageView iView) {
        try {
            if (iView == null || iView.getImage() == null || sPane == null) {
                return;
            }
            Bounds bounds = sPane.getBoundsInParent();
            double ratioW = bounds.getWidth() / iView.getImage().getWidth();
            double ratioH = bounds.getHeight() / iView.getImage().getHeight();
            if (ratioW < ratioH) {
                double w = bounds.getWidth() - 10;
                iView.setFitHeight(iView.getImage().getHeight() * w / iView.getImage().getWidth());
                iView.setFitWidth(w);
            } else {
                double h = bounds.getHeight() - 10;
                iView.setFitWidth(iView.getImage().getWidth() * h / iView.getImage().getHeight());
                iView.setFitHeight(h);
            }
            LocateTools.moveCenter(sPane, iView);
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
        }
    }

    public static void zoomIn(ScrollPane sPane, ImageView iView, int xZoomStep, int yZoomStep) {
        double currentWidth = iView.getFitWidth();
        if (currentWidth == -1) {
            currentWidth = iView.getImage().getWidth();
        }
        iView.setFitWidth(currentWidth + xZoomStep);
        double currentHeight = iView.getFitHeight();
        if (currentHeight == -1) {
            currentHeight = iView.getImage().getHeight();
        }
        iView.setFitHeight(currentHeight + yZoomStep);
        LocateTools.moveCenter(sPane, iView);
    }

    public static void zoomOut(ScrollPane sPane, ImageView iView, int xZoomStep, int yZoomStep) {
        double currentWidth = iView.getFitWidth();
        if (currentWidth == -1) {
            currentWidth = iView.getImage().getWidth();
        }
        if (currentWidth <= xZoomStep) {
            return;
        }
        iView.setFitWidth(currentWidth - xZoomStep);
        double currentHeight = iView.getFitHeight();
        if (currentHeight == -1) {
            currentHeight = iView.getImage().getHeight();
        }
        if (currentHeight <= yZoomStep) {
            return;
        }
        iView.setFitHeight(currentHeight - yZoomStep);
        LocateTools.moveCenter(sPane, iView);
    }

    public static DoublePoint getImageXY(MouseEvent event, ImageView view) {
        if (event == null || view.getImage() == null) {
            return null;
        }
        double offsetX = event.getX() - view.getLayoutX() - view.getX();
        double offsetY = event.getY() - view.getLayoutY() - view.getY();
//        if (offsetX < 0 || offsetX >= view.getBoundsInParent().getWidth()
//                || offsetY < 0 || offsetY >= view.getBoundsInParent().getHeight()) {
//            return null;
//        }
        double x = offsetX * view.getImage().getWidth() / view.getBoundsInParent().getWidth();
        double y = offsetY * view.getImage().getHeight() / view.getBoundsInParent().getHeight();
        return new DoublePoint(x, y);
    }

    public static Color imagePixel(MouseEvent event, ImageView view) {
        DoublePoint p = ImageViewTools.getImageXY(event, view);
        if (p == null) {
            return null;
        }
        return imagePixel(p, view);
    }

    public static Color imagePixel(DoublePoint p, ImageView view) {
        if (p == null || view == null || view.getImage() == null) {
            return null;
        }
        int x = (int) p.getX();
        int y = (int) p.getY();
        if (x >= 0 && x < view.getImage().getWidth()
                && y >= 0 && y < view.getImage().getHeight()) {
            PixelReader pixelReader = view.getImage().getPixelReader();
            return pixelReader.getColor(x, y);
        } else {
            return null;
        }
    }

}
