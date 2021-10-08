package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Callback;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.cell.ListColorCell;
import mara.mybox.value.Languages;
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
                    colorsSizeLabel.setText(Languages.message("Count") + ": " + size);
                    if (size > 100) {
                        colorsSizeLabel.setStyle(NodeStyleTools.redText);
                    } else {
                        colorsSizeLabel.setStyle(NodeStyleTools.blueText);
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
            MyBoxLog.error(e.toString());
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
        scopeTips.setStyle(NodeStyleTools.darkRedText);
        NodeStyleTools.setTooltip(scopeTips, tips);
        NodeStyleTools.setTooltip(scopeTipsView, tips);
        scopeTipsView.setVisible(!tips.isBlank());
        popInformation(tips);
    }

    @Override
    protected void stopPickingColor() {
        if (imageLabelOriginal != null) {
            String tips = imageLabelOriginal.getText();
            scopeTips.setText(tips);
            scopeTips.setStyle(imageLabelOriginal.getStyle());
            NodeStyleTools.setTooltip(scopeTipsView, tips);
            NodeStyleTools.setTooltip(scopeTips, tips);
            scopeTipsView.setVisible(!tips.isBlank());
            imageLabelOriginal = null;
        } else {
            scopeTips.setText("");
            scopeTipsView.setVisible(false);
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
            case RectangleColor:
            case CircleColor:
            case EllipseColor:
            case PolygonColor:
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
    public void popSaveColorsMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu = new MenuItem(Languages.message("SaveInPalette"));
            menu.setOnAction((ActionEvent event) -> {
                saveColorsInPalette();
            });
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("SaveInColorsLibrary"));
            menu.setOnAction((ActionEvent event) -> {
                saveColorsInTable();
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            LocateTools.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void saveColorsInPalette() {
        List<Color> colors = colorsList.getSelectionModel().getSelectedItems();
        if (colors == null || colors.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
//                    TableColor.addColorsInPalette(colors);
                    return true;
                }

            };
            parentController.start(task);
        }
    }

    public void saveColorsInTable() {
        List<Color> colors = colorsList.getSelectionModel().getSelectedItems();
        if (colors == null || colors.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
                    tableColor.writeColors(colors, false);
                    return true;
                }

            };
            parentController.start(task);
        }
    }

}
