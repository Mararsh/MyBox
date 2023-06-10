package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.data.ColorPalette;
import mara.mybox.db.data.ColorPaletteName;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableColorPalette;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-4-4
 * @License Apache License Version 2.0
 */
public class ColorCopyController extends BaseChildController {

    protected TableColor tableColor;
    protected TableColorPalette tableColorPalette;
    protected List<Color> colors;

    @FXML
    protected ControlColorPaletteSelector palettesController;

    public ColorCopyController() {
        baseTitle = message("SelectColorPalette");
    }

    public void setParameters(ColorsManageController colorsManager) {
        try {
            palettesController.setParameter(colorsManager, false);

            if (!colorsManager.palettesController.isAllColors()) {
                palettesController.ignore = colorsManager.palettesController.currentPaletteName();
            }
            loadPalettes();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void setParameters(ColorsManageController colorsManager, List<Color> colors) {
        try {
            if (colors == null || colors.isEmpty()) {
                cancelAction();
                return;
            }
            this.colors = colors;
            palettesController.setParameter(colorsManager, false);
            loadPalettes();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void loadPalettes() {
        try {
            checkManage();
            palettesController.loadPalettes();
            palettesController.palettesList.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() > 1) {
                    okAction();
                }
            });
            okButton.disableProperty().bind(palettesController.palettesList.getSelectionModel().selectedItemProperty().isNull());
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        ColorPaletteName palette = palettesController.palettesList.getSelectionModel().getSelectedItem();
        if (palette == null) {
            popError(message("SelectPaletteCopyColors"));
            return;
        }
        if (colors == null) {
            copyColors(palette);
        } else {
            addColors(palette);
        }
    }

    protected ColorsManageController checkManage() {
        try {
            ColorsManageController colorsManager = palettesController.colorsManager();
            tableColor = colorsManager.tableColor;
            tableColorPalette = colorsManager.tableColorPalette;
            return colorsManager;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    protected void copyColors(ColorPaletteName palette) {
        ColorsManageController colorsManager = checkManage();
        List<ColorData> selectedColors = colorsManager.selectedItems();
        if (selectedColors == null || selectedColors.isEmpty()) {
            popError(message("SelectColorsCopy"));
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {
            private int count;

            @Override
            protected boolean handle() {
                List<ColorPalette> cpList = tableColorPalette.write(palette.getCpnid(), selectedColors, false, false);
                if (cpList == null) {
                    return false;
                }
                count = cpList.size();
                return count >= 0;
            }

            @Override
            protected void whenSucceeded() {
                afterCopied(palette, count);
            }
        };
        start(task);
    }

    protected void addColors(ColorPaletteName palette) {
        if (colors == null || palette == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {

            private int count;

            @Override
            protected boolean handle() {
                List<ColorData> colorsList = new ArrayList<>();
                for (Color color : colors) {
                    colorsList.add(new ColorData(color).calculate());
                }
                try (Connection conn = DerbyBase.getConnection()) {
                    List<ColorPalette> cpList
                            = tableColorPalette.write(conn, palette.getCpnid(), colorsList, false, false);
                    if (cpList == null) {
                        return false;
                    }
                    count = cpList.size();
                    UserConfig.setString(baseName + "Palette", palette.getName());
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return count >= 0;
            }

            @Override
            protected void whenSucceeded() {
                afterCopied(palette, count);
            }

        };
        start(task);
    }

    protected void afterCopied(ColorPaletteName palette, int count) {
        ColorsManageController colorsManager = checkManage();
        colorsManager.loadPaletteLast(palette);
        colorsManager.requestMouse();
        closeStage();
        colorsManager.popInformation(message("Copied") + ": " + count);
    }

    @FXML
    @Override
    public void cancelAction() {
        closeStage();
    }

}
