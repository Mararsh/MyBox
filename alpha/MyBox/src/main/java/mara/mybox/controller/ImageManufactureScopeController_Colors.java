package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Callback;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.cell.ListColorCell;
import mara.mybox.fxml.style.NodeStyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class ImageManufactureScopeController_Colors extends ImageManufactureScopeController_Points {

    public void initColorsTab() {
        try {
            colorsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            colorsList.setCellFactory(new Callback<ListView<Color>, ListCell<Color>>() {
                @Override
                public ListCell<Color> call(ListView<Color> p) {
                    return new ListColorCell();
                }
            });
            colorsList.getItems().addListener(new ListChangeListener<Color>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends Color> c) {
                    int size = colorsList.getItems().size();
                    colorsSizeLabel.setText(message("Count") + ": " + size);
                    if (size > 100) {
                        colorsSizeLabel.setStyle(NodeStyleTools.redTextStyle());
                    } else {
                        colorsSizeLabel.setStyle(NodeStyleTools.blueTextStyle());
                    }
                    clearColorsButton.setDisable(size == 0);
                }
            });

            clearColorsButton.setDisable(true);
            deleteColorsButton.disableProperty().bind(colorsList.getSelectionModel().selectedItemProperty().isNull());
            saveColorsButton.disableProperty().bind(colorsList.getSelectionModel().selectedItemProperty().isNull());

            colorSetController.init(this, baseName + "Color", Color.THISTLE);
            colorSetController.hideRect();
            colorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    addColor((Color) newValue);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean controlAltK() {
        if (tabPane.getTabs().contains(colorsTab)) {
            pickColorCheck.setSelected(!pickColorCheck.isSelected());
        } else {
            isPickingColor = false;
        }
        return true;
    }

    @Override
    protected void startPickingColor() {
        if (!tabPane.getTabs().contains(colorsTab)) {
            isPickingColor = false;
            stopPickingColor();
            return;
        }
        imageLabelOriginal = new Label(scopeTips.getText());
        imageLabelOriginal.setStyle(scopeTips.getStyle());
        String tips = message("PickingColorsForScope");
        scopeTips.setText(tips);
        scopeTips.setStyle(NodeStyleTools.darkRedTextStyle());
        NodeStyleTools.setTooltip(scopeTips, tips);
        popInformation(tips);
    }

    @Override
    protected void stopPickingColor() {
        if (imageLabelOriginal != null) {
            String tips = imageLabelOriginal.getText();
            scopeTips.setText(tips);
            scopeTips.setStyle(imageLabelOriginal.getStyle());
            NodeStyleTools.setTooltip(scopeTips, tips);
            imageLabelOriginal = null;
        } else {
            scopeTips.setText("");
            NodeStyleTools.setTooltip(scopeTips, "");
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
