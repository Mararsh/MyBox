package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import mara.mybox.fxml.SingletonCurrentTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class ControlSelectPixels_Colors extends ControlSelectPixels_Area {

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
        setShapesCursor(Cursor.MOVE);
    }

    public boolean addColor(Color color) {
        if (isSettingValues || color == null
                || scope == null || scope.getScopeType() == null
                || colorsList.getItems().contains(color)) {
            return false;
        }
        switch (scope.getScopeType()) {
            case Color:
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
        showScope();
    }

    @FXML
    public void clearColors() {
        if (isSettingValues) {
            return;
        }
        isSettingValues = true;
        colorsList.getItems().clear();
        isSettingValues = false;
        showScope();
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
        task = new SingletonCurrentTask<Void>(this) {
            @Override
            protected boolean handle() {
                return tableColor.writeColors(colors, false) != null;
            }

        };
        start(task);
    }

}
