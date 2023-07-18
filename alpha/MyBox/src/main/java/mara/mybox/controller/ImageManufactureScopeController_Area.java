package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.bufferedimage.ImageScope.ScopeType;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Circle;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Ellipse;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Matting;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Polygon;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Rectangle;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.tools.DoubleTools.scale;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class ImageManufactureScopeController_Area extends ImageManufactureScopeController_Base {

    @FXML
    public void goScope() {
        try {
            if (isSettingValues || imageView == null || imageView.getImage() == null
                    || scope == null || scope.getScopeType() == null || !scopeView.isVisible()) {
                return;
            }
            switch (scope.getScopeType()) {
                case Matting:
                    pickMatting();
                    break;
                case Rectangle:
                    pickRectangle();
                    break;
                case Ellipse:
                    pickEllipse();
                    break;
                case Circle:
                    pickCircle();
                    break;
                case Polygon:
                    pickPolygon();
                    break;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void pickRectangle() {
        try {
            if (scope == null || scope.getScopeType() != ScopeType.Rectangle) {
                return;
            }
            DoubleRectangle rect = pickRectValues();
            if (rect == null) {
                return;
            }
            maskRectangleData = rect;
            scope.setRectangle(maskRectangleData.cloneValues());
            drawMaskRectangle();
            indicateScope();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void pickEllipse() {
        try {
            if (scope == null || scope.getScopeType() != ScopeType.Ellipse) {
                return;
            }
            DoubleRectangle rect = pickRectValues();
            if (rect == null) {
                return;
            }
            maskEllipseData = new DoubleEllipse(rect);
            scope.setEllipse(maskEllipseData.cloneValues());
            drawMaskEllipse();
            indicateScope();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public DoubleRectangle pickRectValues() {
        try {
            double x1, y1, x2, y2;
            try {
                x1 = Double.parseDouble(rectLeftTopXInput.getText());
                rectLeftTopXInput.setStyle(null);
            } catch (Exception e) {
                rectLeftTopXInput.setStyle(UserConfig.badStyle());
                return null;
            }
            try {
                y1 = Double.parseDouble(rectLeftTopYInput.getText());
                rectLeftTopYInput.setStyle(null);
            } catch (Exception e) {
                rectLeftTopYInput.setStyle(UserConfig.badStyle());
                return null;
            }
            try {
                x2 = Double.parseDouble(rightBottomXInput.getText());
                rightBottomXInput.setStyle(null);
            } catch (Exception e) {
                rightBottomXInput.setStyle(UserConfig.badStyle());
                return null;
            }
            try {
                y2 = Double.parseDouble(rightBottomYInput.getText());
                rightBottomYInput.setStyle(null);
            } catch (Exception e) {
                rightBottomYInput.setStyle(UserConfig.badStyle());
                return null;
            }
            DoubleRectangle rect = new DoubleRectangle(x1, y1, x2, y2);
            if (!rect.isValid()) {
                popError(Languages.message("InvalidData"));
                return null;
            }

            return rect;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @FXML
    public void pickCircle() {
        try {
            if (scope == null || scope.getScopeType() != ScopeType.Circle) {
                return;
            }
            double x, y, r;
            try {
                x = Double.parseDouble(circleCenterXInput.getText());
                circleCenterXInput.setStyle(null);
            } catch (Exception e) {
                circleCenterXInput.setStyle(UserConfig.badStyle());
                return;
            }
            try {
                y = Double.parseDouble(circleCenterYInput.getText());
                circleCenterYInput.setStyle(null);
            } catch (Exception e) {
                circleCenterYInput.setStyle(UserConfig.badStyle());
                return;
            }
            try {
                r = Double.parseDouble(circleRadiusInput.getText());
                circleRadiusInput.setStyle(null);
            } catch (Exception e) {
                circleRadiusInput.setStyle(UserConfig.badStyle());
                return;
            }
            DoubleCircle circle = new DoubleCircle(x, y, r);
            if (!circle.isValid()) {
                popError(Languages.message("InvalidData"));
                return;
            }
            maskCircleData = circle;
            scope.setCircle(maskCircleData.cloneValues());
            drawMaskCircle();
            indicateScope();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void pickMatting() {
        try {
            if (scope == null || scope.getScopeType() != ScopeType.Matting) {
                return;
            }
            scope.clearPoints();
            for (DoublePoint p : pointsController.tableData) {
                scope.addPoint((int) Math.round(p.getX()), (int) Math.round(p.getY()));
            }
            indicateScope();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void pickPolygon() {
        try {
            if (scope == null || scope.getScopeType() != ScopeType.Polygon) {
                return;
            }
            maskPolygonData = new DoublePolygon();
            maskPolygonData.setAll(pointsController.tableData);
            drawMaskPolygon();
            scope.setPolygon(maskPolygonData.cloneValues());
            indicateScope();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean drawMaskRectangle() {
        if (!super.drawMaskRectangle()) {
            return false;
        }
        rectLeftTopXInput.setText(scale(maskRectangleData.getSmallX(), 2) + "");
        rectLeftTopYInput.setText(scale(maskRectangleData.getSmallY(), 2) + "");
        rightBottomXInput.setText(scale(maskRectangleData.getBigX(), 2) + "");
        rightBottomYInput.setText(scale(maskRectangleData.getBigY(), 2) + "");
        return true;
    }

    @Override
    public boolean drawMaskCircle() {
        if (!super.drawMaskCircle()) {
            return false;
        }
        circleCenterXInput.setText(scale(maskCircleData.getCenterX(), 2) + "");
        circleCenterYInput.setText(scale(maskCircleData.getCenterY(), 2) + "");
        circleRadiusInput.setText(scale(maskCircleData.getRadius(), 2) + "");
        return true;
    }

    @Override
    public boolean drawMaskEllipse() {
        if (!super.drawMaskEllipse()) {
            return false;
        }
        DoubleRectangle rect = maskEllipseData.getRectangle();
        rectLeftTopXInput.setText(scale(rect.getSmallX(), 2) + "");
        rectLeftTopYInput.setText(scale(rect.getSmallY(), 2) + "");
        rightBottomXInput.setText(scale(rect.getBigX(), 2) + "");
        rightBottomYInput.setText(scale(rect.getBigY(), 2) + "");
        return true;
    }

}
