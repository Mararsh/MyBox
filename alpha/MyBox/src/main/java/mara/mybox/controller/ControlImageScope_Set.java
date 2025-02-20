package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.IntPoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.image.data.ImageScope;
import mara.mybox.image.data.PixelsOperation;
import mara.mybox.image.data.PixelsOperationFactory;
import mara.mybox.image.tools.ImageScopeTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class ControlImageScope_Set extends ControlImageScope_Base {

    protected boolean needFixSize;

    public void indicateScope() {
        if (scope.getShapeType() == ImageScope.ShapeType.Outline) {
            indicateOutline(false);
            return;
        }
        if (pickScopeValues() == null) {
            return;
        }
        popInformation(message("Loading..."), scrollPane);
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private Image maskImage;

            @Override
            protected boolean handle() {
                try {
                    maskImage = maskImage(this);
                    if (task == null || isCancelled()) {
                        return false;
                    }
                    return maskImage != null;
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                closePopup();
                image = maskImage;
                imageView.setImage(maskImage);
                if (scope.getShapeType() == ImageScope.ShapeType.Matting4
                        || scope.getShapeType() == ImageScope.ShapeType.Matting8) {
                    drawMattingPoints();
                } else {
                    drawMaskShape();
                }
                if (needFixSize) {
                    paneSize();
                    needFixSize = false;
                }
                showNotify.set(!showNotify.get());
            }

        };
        start(task, false);
    }

    @FXML
    public void goShape() {
        try {
            if (!isValidScope()) {
                return;
            }
            switch (scope.getShapeType()) {
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
            if (!isValidScope() || scope.getShapeType() != ImageScope.ShapeType.Rectangle) {
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

    @FXML
    @Override
    public void selectAllAction() {
        if (!isValidScope() || isSettingValues
                || scope.getShapeType() != ImageScope.ShapeType.Rectangle) {
            return;
        }
        rectLeftTopXInput.setText("0");
        rectLeftTopYInput.setText("0");
        rightBottomXInput.setText(image.getWidth() + "");
        rightBottomYInput.setText(image.getHeight() + "");
        goShape();
    }

    public void pickEllipse() {
        try {
            if (!isValidScope() || scope.getShapeType() != ImageScope.ShapeType.Ellipse) {
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
            if (!isValidScope() || scope.getShapeType() != ImageScope.ShapeType.Circle) {
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

    @Override
    protected List<MenuItem> shapeDataMenu(Event event, DoublePoint p) {
        if (event == null || image == null) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;

        items.add(moveShapeMenu());

        CheckMenuItem clearMenuItem = new CheckMenuItem(message("ClearDataWhenLoadImage"), StyleTools.getIconImageView("iconClear.png"));
        clearMenuItem.setSelected(UserConfig.getBoolean(baseName + "ClearDataWhenLoadImage", true));
        clearMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent cevent) {
                if (clearDataWhenLoadImageCheck != null) {
                    clearDataWhenLoadImageCheck.setSelected(clearMenuItem.isSelected());
                } else {
                    UserConfig.setBoolean(baseName + "ClearDataWhenLoadImage", clearMenuItem.isSelected());
                }
            }
        });
        items.add(clearMenuItem);

        if (!canDeleteAnchor()) {
            return null;
        }

        items.add(addPointMenu());

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
        if (scope == null || scope.getShapeType() == null || image == null) {
            return false;
        }
        ImageScope.ShapeType type = scope.getShapeType();
        return type == ImageScope.ShapeType.Polygon
                || type == ImageScope.ShapeType.Matting4
                || type == ImageScope.ShapeType.Matting8;
    }

    @Override
    public void moveMaskAnchor(int index, String name, DoublePoint p) {
        if (scope == null || scope.getShapeType() == null || image == null) {
            return;
        }
        ImageScope.ShapeType type = scope.getShapeType();
        if (type == ImageScope.ShapeType.Polygon
                || type == ImageScope.ShapeType.Matting4
                || type == ImageScope.ShapeType.Matting8) {
            pointsController.setPoint(index, p.getX(), p.getY());
        } else {
            super.moveMaskAnchor(index, name, p);
        }
    }

    @Override
    public void deleteMaskAnchor(int index, String name) {
        if (scope == null || scope.getShapeType() == null || image == null) {
            return;
        }
        ImageScope.ShapeType type = scope.getShapeType();
        if (type == ImageScope.ShapeType.Polygon
                || type == ImageScope.ShapeType.Matting4
                || type == ImageScope.ShapeType.Matting8) {
            pointsController.deletePoint(index);
        }
    }

    public void drawMattingPoints() {
        try {
            clearMaskAnchors();
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
            for (int i = 0; i < scope.getPoints().size(); i++) {
                IntPoint p = scope.getPoints().get(i);
                double x = p.getX() * xRatio;
                double y = p.getY() * yRatio;
                addMaskAnchor(i, new DoublePoint(p.getX(), p.getY()), x, y);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        colors
     */
    @Override
    protected void startPickingColor() {
        imageView.setCursor(Cursor.HAND);
        setShapesCursor(Cursor.HAND);
        popInformation(pickingColorTips());
    }

    @Override
    public String pickingColorTips() {
        return message("PickingColorsForScope");
    }

    @Override
    protected void stopPickingColor() {
        imageView.setCursor(Cursor.DEFAULT);
        setShapesCursor(defaultShapeCursor());
    }

    public boolean addColor(Color color) {
        if (isSettingValues || color == null
                || scope == null || scope.getShapeType() == null
                || colorsList.getItems().contains(color)) {
            return false;
        }
        isSettingValues = true;
        colorsList.getItems().add(color);
        isSettingValues = false;
        indicateScope();
        return true;
    }

    @FXML
    public void deleteColors() {
        if (isSettingValues) {
            return;
        }
        List<Color> colors = colorsList.getSelectionModel().getSelectedItems();
        if (colors == null || colors.isEmpty()) {
            return;
        }
        isSettingValues = true;
        colorsList.getItems().removeAll(colors);
        isSettingValues = false;
        indicateScope();
    }

    @FXML
    public void clearColors() {
        if (isSettingValues) {
            return;
        }
        isSettingValues = true;
        colorsList.getItems().clear();
        isSettingValues = false;
        indicateScope();
    }

    @FXML
    public void saveColors() {
        List<Color> colors = colorsList.getSelectionModel().getSelectedItems();
        if (colors == null || colors.isEmpty()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                return tableColor.writeColors(colors, false) != null;
            }

        };
        start(task);
    }

    @FXML
    public void goMatch() {
        try {
            if (matchController.pickValuesTo(scope)) {
                indicateScope();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }


    /*
        outline
     */
    public boolean validOutline() {
        return srcImage() != null
                && scope != null
                && scope.getShapeType() == ImageScope.ShapeType.Outline
                && maskRectangleData != null;
    }

    public void indicateOutline(boolean pickOutline) {
        if (isSettingValues || !validOutline() || !pickEnvValues()) {
            return;
        }
        popInformation(message("Loading..."), scrollPane);
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private Image handledImage;

            @Override
            protected boolean handle() {
                try {
                    if (pickOutline || scope.getOutlineSource() == null) {
                        BufferedImage outline
                                = SwingFXUtils.fromFXImage(outlineController.getImage(), null);
                        scope.setOutlineSource(outline);
                        maskRectangleData = DoubleRectangle.image(outline);
                    }
                    if (scope.getOutlineSource() == null) {
                        return false;
                    }

                    Image bgImage = srcImage();
                    DoubleRectangle outlineReck = ImageScopeTools.makeOutline(this,
                            scope, bgImage, maskRectangleData,
                            outlineController.keepRatioCheck.isSelected());
                    if (outlineReck == null || task == null || isCancelled()) {
                        return false;
                    }
                    maskRectangleData = outlineReck;
                    PixelsOperation pixelsOperation = PixelsOperationFactory.createFX(
                            bgImage, scope, PixelsOperation.OperationType.ShowScope);
                    handledImage = pixelsOperation.startFx();
                    if (task == null || isCancelled()) {
                        return false;
                    }
                    return handledImage != null;
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                closePopup();
                image = handledImage;
                imageView.setImage(handledImage);
                showMaskRectangle();
                if (needFixSize) {
                    paneSize();
                    needFixSize = false;
                }
                showNotify.set(!showNotify.get());
            }

        };
        start(task, false);
    }

}
