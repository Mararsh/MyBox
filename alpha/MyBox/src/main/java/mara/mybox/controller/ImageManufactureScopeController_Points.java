package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class ImageManufactureScopeController_Points extends ImageManufactureScopeController_Area {

    public void initPointsTab() {
        try {
            pointsController.tableDataChangedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    pointsChanged();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void pointsChanged() {
        if (isSettingValues) {
            return;
        }
        if (scope.getScopeType() == ImageScope.ScopeType.Matting) {
            scope.clearPoints();
            for (DoublePoint p : pointsController.tableData) {
                scope.addPoint((int) Math.round(p.getX()), (int) Math.round(p.getY()));
            }
        } else if (scope.getScopeType() == ImageScope.ScopeType.Polygon) {
            maskPolygonData.setAll(pointsController.tableData);
            drawMaskPolygon();
            scope.setPolygon(maskPolygonData.cloneValues());
        }
        indicateScope();
    }

}
