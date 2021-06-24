package mara.mybox.controller;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Modality;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColorPaletteName;
import mara.mybox.db.table.TableColorPaletteName;
import static mara.mybox.db.table.TableColorPaletteName.DefaultPalette;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-2
 * @License Apache License Version 2.0
 */
public class ControlColorPaletteSelector extends BaseController {

    protected ColorsManageController colorsManager;
    protected TableColorPaletteName tableColorPaletteName;
    protected ColorPaletteName allColors, defaultPalette;
    protected SimpleBooleanProperty selected;
    protected boolean includeAll;
    protected String ignore;

    @FXML
    protected ListView<ColorPaletteName> palettesList;

    public ControlColorPaletteSelector() {
        baseTitle = message("ColorPalettes");
    }

    public void setParent(ColorsManageController colorsManager) {
        parentController = colorsManager;
        includeAll = true;
        setValues(colorsManager);
    }

    public void setValues(ColorsManageController colorsManager) {
        this.colorsManager = colorsManager;
        baseName = colorsManager.baseName;
        tableColorPaletteName = colorsManager.tableColorPaletteName;
        ignore = null;

        if (includeAll) {
            allColors = new ColorPaletteName(message("AllColors"));
            palettesList.getItems().addAll(allColors);
        } else {
            allColors = null;
        }
        palettesList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        palettesList.setCellFactory(p -> new ListCell<ColorPaletteName>() {
            @Override
            public void updateItem(ColorPaletteName item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(item.getName());
            }
        });
    }

    @FXML
    public void loadPalettes() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            if (allColors != null) {
                palettesList.getItems().setAll(allColors);
            } else {
                palettesList.getItems().clear();
            }
            task = new SingletonTask<Void>() {
                private List<ColorPaletteName> palettes;

                @Override
                protected boolean handle() {
                    defaultPalette = tableColorPaletteName.defaultPalette();
                    palettes = tableColorPaletteName.query();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (palettes != null) {
                        if (ignore != null) {
                            for (ColorPaletteName palette : palettes) {
                                if (!palette.getName().equals(ignore)) {
                                    palettesList.getItems().add(palette);
                                }
                            }
                        } else {
                            palettesList.getItems().addAll(palettes);
                        }
                        String s = AppVariables.getUserConfigValue(baseName + "Palette", DefaultPalette);
                        for (ColorPaletteName palette : palettes) {
                            if (palette.getName().equals(s)) {
                                palettesList.getSelectionModel().select(palette);
                                return;
                            }
                        }
                    }
                    palettesList.getSelectionModel().select(0);
                }

            };
            if (parentController != null) {
                parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            } else {
                openHandlingStage(task, Modality.WINDOW_MODAL);
            }
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    public void addPaltte() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            String name = FxmlControl.askValue(baseTitle, message("AddPalette"), message("Name"),
                    message("ColorPalette") + new Date().getTime());
            if (name == null || name.isBlank()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private ColorPaletteName newPalatte;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        if (tableColorPaletteName.find(conn, name) != null) {
                            error = "AlreadyExisted";
                            return false;
                        }
                        newPalatte = new ColorPaletteName(name);
                        newPalatte = tableColorPaletteName.insertData(conn, newPalatte);
                        return newPalatte != null;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    palettesList.getItems().add(newPalatte);
                    if (parentController != null) {
                        parentController.popSuccessful();
                    } else {
                        popSuccessful();
                        colorsManager.palettesController.palettesList.getItems().add(newPalatte);
                    }
                    palettesList.getSelectionModel().select(newPalatte);
                }

            };
            if (parentController != null) {
                parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            } else {
                openHandlingStage(task, Modality.WINDOW_MODAL);
            }
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    @Override
    public void cancelAction() {
        closeStage();
    }

}
