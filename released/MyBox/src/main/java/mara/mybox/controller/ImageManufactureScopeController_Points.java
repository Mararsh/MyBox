package mara.mybox.controller;

import java.util.List;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class ImageManufactureScopeController_Points extends ImageManufactureScopeController_Area {

    public void initPointsTab() {
        try {
            pointsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            pointsList.getItems().addListener(new ListChangeListener<String>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends String> c) {
                    int size = pointsList.getItems().size();
                    pointsSizeLabel.setText(Languages.message("Count") + ": " + size);
                    if (size > 100) {
                        pointsSizeLabel.setStyle(NodeStyleTools.redTextStyle());
                    } else {
                        pointsSizeLabel.setStyle(NodeStyleTools.blueTextStyle());
                    }
                    clearPointsButton.setDisable(size == 0);
                }
            });

            clearPointsButton.setDisable(true);

            deletePointsButton.disableProperty().bind(pointsList.getSelectionModel().selectedItemProperty().isNull());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void deletePoints() {
        if (isSettingValues) {
            return;
        }
        if (scope.getScopeType() == ImageScope.ScopeType.Matting) {
            List<Integer> indices = pointsList.getSelectionModel().getSelectedIndices();
            for (int i = indices.size() - 1; i >= 0; i--) {
                int index = indices.get(i);
                if (index < scope.getPoints().size()) {
                    scope.getPoints().remove(index);
                }
            }
        } else if (scope.getScopeType() == ImageScope.ScopeType.Polygon
                || scope.getScopeType() == ImageScope.ScopeType.PolygonColor) {
            List<Integer> indices = pointsList.getSelectionModel().getSelectedIndices();
            for (int i = indices.size() - 1; i >= 0; i--) {
                maskPolygonData.remove(indices.get(i));
            }
            drawMaskPolygonLine();
            scope.setPolygon(maskPolygonData.cloneValues());
        }
        pointsList.getItems().removeAll(pointsList.getSelectionModel().getSelectedItems());
        indicateScope();
    }

    @FXML
    public void clearPoints() {
        if (isSettingValues) {
            return;
        }
        scope.clearPoints();
        pointsList.getItems().clear();
        if (scope.getScopeType() == ImageScope.ScopeType.Polygon
                || scope.getScopeType() == ImageScope.ScopeType.PolygonColor) {
            maskPolygonData.clear();
            drawMaskPolygonLine();
            scope.setPolygon(maskPolygonData.cloneValues());
        }
        indicateScope();
    }
}
