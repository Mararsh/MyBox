package mara.mybox.controller;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColorPaletteName;
import mara.mybox.db.table.TableColorPaletteName;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.PaletteTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-4-2
 * @License Apache License Version 2.0
 */
public class ControlColorPaletteSelector extends BaseController {

    protected ColorsManageController manageController;
    protected TableColorPaletteName tableColorPaletteName;
    protected ColorPaletteName allColors, defaultPalette;
    protected boolean isManager;
    protected String ignore;

    @FXML
    protected ListView<ColorPaletteName> palettesList;

    public ControlColorPaletteSelector() {
        baseTitle = message("ColorPalettes");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            tableColorPaletteName = new TableColorPaletteName();

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

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameter(ColorsManageController manageController, boolean isManager) {
        this.manageController = manageController;
        this.isManager = isManager;
        baseName = manageController.baseName;
        ignore = null;
        if (isManager) {
            allColors = new ColorPaletteName(message("AllColors"));
            palettesList.getItems().add(allColors);
        } else {
            allColors = null;
        }
    }

    @FXML
    public void loadPalettes() {
        if (task != null) {
            task.cancel();
        }
        if (allColors != null) {
            palettesList.getItems().setAll(allColors);
        } else {
            palettesList.getItems().clear();
        }
        task = new SingletonTask<Void>(this) {
            private List<ColorPaletteName> palettes;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    defaultPalette = PaletteTools.defaultPalette(conn);
                    palettes = tableColorPaletteName.readAll(conn);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
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
                    String s = UserConfig.getString(baseName + "Palette", PaletteTools.defaultPaletteName());
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
        start(task);
    }

    @FXML
    public void addPalette() {
        String name = PopTools.askValue(baseTitle, message("AddPalette"), message("Name"),
                message("ColorPalette") + new Date().getTime());
        if (name == null || name.isBlank()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {
            private ColorPaletteName newPalatte;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
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
                if (manageController != null) {
                    manageController.palettesController.palettesList.getItems().add(newPalatte);
                }
                palettesList.getSelectionModel().select(newPalatte);
                popSuccessful();
            }

        };
        start(task);
    }

    public ColorsManageController colorsManager() {
        if (manageController == null || !manageController.getMyStage().isShowing()) {
            manageController = ColorsManageController.oneOpen();
        }
        return manageController;
    }

    public ColorPaletteName selected() {
        return palettesList.getSelectionModel().getSelectedItem();
    }

}
