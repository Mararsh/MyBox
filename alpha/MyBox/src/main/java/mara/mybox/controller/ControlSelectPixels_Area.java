package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.bufferedimage.ImageScope.ScopeType;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Circle;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Ellipse;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Rectangle;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class ControlSelectPixels_Area extends ControlSelectPixels_Base {

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
            showScope();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void selectAllAction() {
        if (!isValidScope() || isSettingValues
                || scope.getScopeType() != ScopeType.Rectangle) {
            return;
        }
        rectLeftTopXInput.setText("0");
        rectLeftTopYInput.setText("0");
        rightBottomXInput.setText(image.getWidth() + "");
        rightBottomYInput.setText(image.getHeight() + "");
        goScope();
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
            showScope();
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
            showScope();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public boolean withdrawAction() {
        if (!isValidScope() || isSettingValues
                || scope.getScopeType() != ScopeType.Polygon) {
            return false;
        }
        if (scope.getScopeType() == ScopeType.Polygon || scope.getScopeType() == ScopeType.Matting) {
            pointsController.removeLastItem();
        }
        return true;
    }

    @Override
    protected List<MenuItem> shapeDataMenu(Event event, DoublePoint p) {
        if (event == null || image == null) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;

        if (!canDeleteAnchor()) {
            return null;
        }

        CheckMenuItem pointMenuItem = new CheckMenuItem(message("AddPointWhenLeftClick"), StyleTools.getIconImageView("iconNewItem.png"));
        pointMenuItem.setSelected(UserConfig.getBoolean(baseName + "AddPointWhenLeftClick", true));
        pointMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent cevent) {
                UserConfig.setBoolean(baseName + "AddPointWhenLeftClick", pointMenuItem.isSelected());
                addPointWhenClick = pointMenuItem.isSelected();
            }
        });
        items.add(pointMenuItem);

        if (p != null) {
            menu = new MenuItem(message("AddPointInShape"), StyleTools.getIconImageView("iconAdd.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    pointsController.addPoint(p);
                }
            });
            items.add(menu);
        }

        menu = new MenuItem(message("RemoveLastPoint"), StyleTools.getIconImageView("iconDelete.png"));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent mevent) {
                pointsController.removeLastItem();
            }
        });
        items.add(menu);

        menu = new MenuItem(message("Clear"), StyleTools.getIconImageView("iconClear.png"));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent mevent) {
                pointsController.clear();
            }
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());
        return items;

    }

    @Override
    public boolean canDeleteAnchor() {
        if (scope == null || scope.getScopeType() == null || image == null) {
            return false;
        }
        ScopeType type = scope.getScopeType();
        return type == ScopeType.Polygon || type == ScopeType.Matting;
    }

    @Override
    public void moveMaskAnchor(int index, String name, DoublePoint p) {
        if (scope == null || scope.getScopeType() == null || image == null) {
            return;
        }
        ScopeType type = scope.getScopeType();
        if (type == ScopeType.Polygon || type == ScopeType.Matting) {
            pointsController.setPoint(index, p.getX(), p.getY());
        } else {
            super.moveMaskAnchor(index, name, p);
        }
    }

    @Override
    public void deleteMaskAnchor(int index, String name) {
        if (scope == null || scope.getScopeType() == null || image == null) {
            return;
        }
        ScopeType type = scope.getScopeType();
        if (type == ScopeType.Polygon || type == ScopeType.Matting) {
            pointsController.deletePoint(index);
        }
    }

}