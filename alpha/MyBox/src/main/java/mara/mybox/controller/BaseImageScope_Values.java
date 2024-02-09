package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
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
import mara.mybox.bufferedimage.AlphaTools;
import mara.mybox.bufferedimage.ImageScope;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Circle;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Ellipse;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Rectangle;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.ImageItem;
import mara.mybox.data.IntPoint;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.value.AppVariables;
import mara.mybox.value.FileFilters;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class BaseImageScope_Values extends BaseImageScope_Base {

    protected boolean needFixSize;

    public void indicateScope() {
        if (scope.getScopeType() == ImageScope.ScopeType.Outline) {
            indicateOutline();
            return;
        }
        if (pickScopeValues() == null) {
            return;
        }
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
                image = maskImage;
                imageView.setImage(maskImage);
                if (scope.getScopeType() == ImageScope.ScopeType.Matting) {
                    drawMattingPoints();
                } else {
                    drawMaskShape();
                }
                fitView();
                showNotify.set(!showNotify.get());
            }

            @Override
            protected void whenCanceled() {
            }

            @Override
            protected void whenFailed() {
            }

        };
        start(task, viewBox);
    }

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
            if (!isValidScope() || scope.getScopeType() != ImageScope.ScopeType.Rectangle) {
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
                || scope.getScopeType() != ImageScope.ScopeType.Rectangle) {
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
            if (!isValidScope() || scope.getScopeType() != ImageScope.ScopeType.Ellipse) {
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
            if (!isValidScope() || scope.getScopeType() != ImageScope.ScopeType.Circle) {
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
        if (scope == null || scope.getScopeType() == null || image == null) {
            return false;
        }
        ImageScope.ScopeType type = scope.getScopeType();
        return type == ImageScope.ScopeType.Polygon || type == ImageScope.ScopeType.Matting;
    }

    @Override
    public void moveMaskAnchor(int index, String name, DoublePoint p) {
        if (scope == null || scope.getScopeType() == null || image == null) {
            return;
        }
        ImageScope.ScopeType type = scope.getScopeType();
        if (type == ImageScope.ScopeType.Polygon || type == ImageScope.ScopeType.Matting) {
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
        ImageScope.ScopeType type = scope.getScopeType();
        if (type == ImageScope.ScopeType.Polygon || type == ImageScope.ScopeType.Matting) {
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
                || scope == null || scope.getScopeType() == null
                || colorsList.getItems().contains(color)) {
            return false;
        }
        switch (scope.getScopeType()) {
            case Colors:
            case Rectangle:
            case Circle:
            case Ellipse:
            case Polygon:
                colorsList.getItems().add(color);
                return true;
            default:
                return false;
        }
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

    /*
        outline
     */
    public void outlineExamples() {
        FxSingletonTask outlinesTask = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                for (ImageItem item : ImageItem.predefined()) {
                    if (isCancelled()) {
                        return true;
                    }
                    Image image = item.readImage();
                    if (image != null) {
                        Platform.runLater(() -> {
                            isSettingValues = true;
                            outlinesList.getItems().add(image);
                            isSettingValues = false;
                        });
                        setInfo(item.getName());
                    }
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                outlinesList.getSelectionModel().select(0);
            }

        };
        start(outlinesTask);
    }

    @FXML
    public void selectOutlineFile() {
        try {
            File file = FxFileTools.selectFile(this,
                    UserConfig.getPath(baseName + "SourcePath"),
                    FileFilters.AlphaImageExtensionFilter);
            if (file == null) {
                return;
            }
            loadOutlineSource(file);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadOutlineSource(File file) {
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private Image outlineImage;

            @Override
            protected boolean handle() {
                try {
                    BufferedImage bufferedImage = ImageFileReaders.readImage(this, file);
                    outlineImage = SwingFXUtils.toFXImage(bufferedImage, null);
                    return outlineImage != null;
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                isSettingValues = true;
                outlinesList.getItems().add(0, outlineImage);
                isSettingValues = false;
                outlinesList.getSelectionModel().select(0);
            }

        };
        start(task);
    }

    public void loadOutlineSource(BufferedImage bufferedImage) {
        if (isSettingValues || bufferedImage == null) {
            return;
        }
        scope.setOutlineSource(bufferedImage);
        maskRectangleData = DoubleRectangle.image(bufferedImage);
        indicateOutline();
    }

    public void loadOutlineSource(Image image) {
        if (isSettingValues || image == null) {
            return;
        }
        loadOutlineSource(SwingFXUtils.fromFXImage(image, null));
    }

    public boolean validOutline() {
        return srcImage() != null
                && scope != null
                && scope.getScopeType() == ImageScope.ScopeType.Outline
                && scope.getOutlineSource() != null
                && maskRectangleData != null;
    }

    public void indicateOutline() {
        if (isSettingValues || !validOutline() || !pickBaseValues()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private BufferedImage[] outline;
            private Image outlineImage;

            @Override
            protected boolean handle() {
                try {
                    Image bgImage = srcImage();
                    outline = AlphaTools.outline(this,
                            scope.getOutlineSource(),
                            maskRectangleData,
                            (int) bgImage.getWidth(),
                            (int) bgImage.getHeight(),
                            scopeOutlineKeepRatioCheck.isSelected());
                    if (outline == null || task == null || isCancelled()
                            || !validOutline()) {
                        return false;
                    }
                    maskRectangleData = DoubleRectangle.xywh(
                            maskRectangleData.getX(), maskRectangleData.getY(),
                            outline[0].getWidth(), outline[0].getHeight());
                    scope.setOutline(outline[1]);
                    scope.setRectangle(maskRectangleData.copy());

                    PixelsOperation pixelsOperation = PixelsOperationFactory.createFX(
                            bgImage, scope, PixelsOperation.OperationType.ShowScope);
                    outlineImage = pixelsOperation.startFx();
                    if (task == null || isCancelled()) {
                        return false;
                    }
                    return outlineImage != null;
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                image = outlineImage;
                imageView.setImage(outlineImage);
                showMaskRectangle();
                showNotify.set(!showNotify.get());
            }

        };
        start(task, viewBox);
    }

    @FXML
    public void showOutlineFileMenu(Event event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event, false) {
            @Override
            public List<VisitHistory> recentFiles() {
                int fileNumber = AppVariables.fileRecentNumber;
                return VisitHistoryTools.getRecentAlphaImages(fileNumber);
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return recentSourcePathsBesidesFiles();
            }

            @Override
            public void handleSelect() {
                selectOutlineFile();
            }

            @Override
            public void handleFile(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    handleSelect();
                    return;
                }
                loadOutlineSource(file);
            }

            @Override
            public void handlePath(String fname) {
                handleSourcePath(fname);
            }

        }.pop();
    }

    @FXML
    public void pickOutlineFile(Event event) {
        if (UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true)
                || AppVariables.fileRecentNumber <= 0) {
            selectOutlineFile();
        } else {
            showOutlineFileMenu(event);
        }
    }

    @FXML
    public void popOutlineFile(Event event) {
        if (UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true)) {
            showOutlineFileMenu(event);
        }
    }

}
