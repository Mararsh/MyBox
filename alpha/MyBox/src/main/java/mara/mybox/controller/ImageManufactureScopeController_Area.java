package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.bufferedimage.ImageScope.ScopeType;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Circle;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Ellipse;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Rectangle;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
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
            if (!isValidScope()) {
                return;
            }
            switch (scope.getScopeType()) {
                case Rectangle:
                    pickRectangle();
                    break;
                case Ellipse:
                    pickEllipse();
                    break;
                case Circle:
                    pickCircle();
                    break;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void pickRectangle() {
        try {
            if (!isValidScope() || scope.getScopeType() != ScopeType.Rectangle) {
                return;
            }
            DoubleRectangle rect = pickRectValues();
            if (rect == null) {
                return;
            }
            maskRectangleData = rect;
            scope.setRectangle(maskRectangleData.copy());
            indicateScope();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void pickEllipse() {
        try {
            if (!isValidScope() || scope.getScopeType() != ScopeType.Ellipse) {
                return;
            }
            DoubleRectangle rect = pickRectValues();
            if (rect == null) {
                return;
            }
            maskEllipseData = DoubleEllipse.rect(rect);
            scope.setEllipse(maskEllipseData.copy());
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
            DoubleRectangle rect = DoubleRectangle.xy12(x1, y1, x2, y2);
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

    public void pickCircle() {
        try {
            if (!isValidScope() || scope.getScopeType() != ScopeType.Circle) {
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
            scope.setCircle(maskCircleData.copy());
            indicateScope();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void pickPoints() {
        try {
            if (!isValidScope() || isSettingValues
                    || pointsController.isSettingValues
                    || pointsController.isSettingTable) {
                return;
            }
            if (scope.getScopeType() == ScopeType.Matting) {
                scope.clearPoints();
                for (int i = 0; i < pointsController.tableData.size(); i++) {
                    DoublePoint p = pointsController.tableData.get(i);
                    scope.addPoint((int) Math.round(p.getX()), (int) Math.round(p.getY()));
                }
                indicateScope();

            } else if (scope.getScopeType() == ScopeType.Polygon) {
                maskPolygonData = new DoublePolygon();
                maskPolygonData.setAll(pointsController.tableData);
                scope.setPolygon(maskPolygonData.copy());
                indicateScope();
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void withdrawAction() {
        if (!isValidScope() || isSettingValues
                || scope.getScopeType() != ScopeType.Polygon) {
            return;
        }
        pointsController.removeLastItem();
    }

}
