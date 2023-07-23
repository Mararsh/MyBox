package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.data.ColorPaletteName;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableColorPalette;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-22
 * @License Apache License Version 2.0
 */
public class ColorsPickingController extends BaseChildController {

    protected TableColorPalette tableColorPalette;
    protected TableColor tableColor;
    protected ColorPaletteName currentPalette;

    @FXML
    protected ControlColorPaletteSelector palettesController;
    @FXML
    protected ControlColorsPane colorsController;
    @FXML
    protected CheckBox onlyNewCheck;
    @FXML
    protected Label paletteLabel;

    public ColorsPickingController() {
        baseTitle = message("PickColors");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            palettesController.setParameter(null, false);
            colorsController.setParameter(null, true);

            onlyNewCheck.setSelected(UserConfig.getBoolean("ColorsOnlyPickNew", true));
            onlyNewCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean("ColorsOnlyPickNew", nv);
                }
            });

            tableColorPalette = new TableColorPalette();
            tableColor = new TableColor();
            tableColorPalette.setTableColor(tableColor);

            colorsController.loadedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    paletteLabel.setText(currentPalette.getName() + ": "
                            + colorsController.colorsPane.getChildren().size());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(BaseController parent) {
        try {
            this.parentController = parent;

            palettesController.palettesList.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends ColorPaletteName> ov, ColorPaletteName t, ColorPaletteName t1) -> {
                        paletteSelected();
                    });

            palettesController.loadPalettes();

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void paletteSelected() {
        currentPalette = palettesController.selected();
        if (currentPalette == null) {
            setTitle(baseTitle);
            paletteLabel.setText("");
            colorsController.loadColors(null, null);
            return;
        }
        String name = currentPalette.getName();
        UserConfig.setString(baseName + "Palette", name);
        setTitle(baseTitle + " - " + name);
        paletteLabel.setText(name);
        colorsController.loadPalette(currentPalette, false);
    }

    protected void pickColor(Color color) {
        if (color == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    ColorData colorData = tableColor.write(conn, new ColorData(color), false);
                    if (colorData == null) {
                        return false;
                    }
                    colorData.setPaletteid(currentPalette.getCpnid());
                    tableColorPalette.findAndCreate(conn, colorData, false, onlyNewCheck.isSelected());
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                colorsController.loadPalette(currentPalette, true);
            }

        };
        start(task);
    }

    @Override
    public void cleanPane() {
        try {
            if (parentController != null && (parentController instanceof BaseImageController)) {
                BaseImageController c = (BaseImageController) parentController;
                if (c.pickColorCheck != null) {
                    c.pickColorCheck.setSelected(false);
                }
                c.isPickingColor = false;
            }
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        static methods
     */
    public static ColorsPickingController oneOpen(BaseController parent) {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof ColorsPickingController) {
                ((ColorsPickingController) object).close();
            }
        }
        ColorsPickingController controller
                = (ColorsPickingController) WindowTools.openChildStage(parent.getMyStage(),
                        Fxmls.ColorsPickingFxml, false);
        controller.setParameters(parent);
        return controller;
    }

}
