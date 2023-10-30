package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.style.NodeStyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class ControlImageScopeInput_Colors extends ControlImageScopeInput_Area {

    @Override
    protected void startPickingColor() {
        imageView.setCursor(Cursor.HAND);
        setShapesCursor(Cursor.HAND);;
        imageLabelOriginal = new Label(scopeTips.getText());
        imageLabelOriginal.setStyle(scopeTips.getStyle());
        String tips = pickingColorTips();
        scopeTips.setText(tips);
        scopeTips.setStyle(NodeStyleTools.darkRedTextStyle());
        popInformation(tips);
    }

    @Override
    public String pickingColorTips() {
        return message("PickingColorsForScope");
    }

    @Override
    protected void stopPickingColor() {
        imageView.setCursor(Cursor.DEFAULT);
        setShapesCursor(Cursor.MOVE);
        if (imageLabelOriginal != null) {
            String tips = imageLabelOriginal.getText();
            scopeTips.setText(tips);
            scopeTips.setStyle(imageLabelOriginal.getStyle());
            imageLabelOriginal = null;
        } else {
            scopeTips.setText("");
        }

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
                scope.addColor(ColorConvertTools.converColor(color));
                colorsList.getItems().add(color);
                indicateScope();
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
        for (Color color : colors) {
            scope.getColors().remove(ColorConvertTools.converColor(color));
        }
        colorsList.getItems().removeAll(colors);
        indicateScope();
    }

    @FXML
    public void clearColors() {
        if (isSettingValues) {
            return;
        }
        scope.getColors().clear();
        colorsList.getItems().clear();
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
        task = new SingletonCurrentTask<Void>(this) {
            @Override
            protected boolean handle() {
                return tableColor.writeColors(colors, false) != null;
            }

        };
        start(task);
    }

    public void pickColors() {
        List<Color> colors = colorsList.getItems();
        if (colors == null || colors.isEmpty()) {
            scope.getColors().clear();
            return;
        }
        for (Color color : colors) {
            scope.addColor(ColorConvertTools.converColor(color));
        }
    }

}
