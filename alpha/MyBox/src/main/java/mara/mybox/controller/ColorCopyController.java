package mara.mybox.controller;

import java.sql.Connection;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.data.ColorPalette;
import mara.mybox.db.data.ColorPaletteName;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableColorPalette;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-4-4
 * @License Apache License Version 2.0
 */
public class ColorCopyController extends ControlColorPaletteSelector {

    protected TableColor tableColor;
    protected TableColorPalette tableColorPalette;
    protected List<Color> colors;

    public ColorCopyController() {
        baseTitle = Languages.message("SelectColorPalette");
    }

    @Override
    public void setValues(ColorsManageController colorsManager) {
        try {
            super.setValues(colorsManager);
            if (!colorsManager.colorsController.isAllColors()) {
                ignore = colorsManager.colorsController.currentPalette.getName();
            }
            okButton.disableProperty().bind(palettesList.getSelectionModel().selectedItemProperty().isNull());
            loadPalettes();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void setValues(ColorsManageController colorsManager, List<Color> colors) {
        try {
            if (colors == null || colors.isEmpty()) {
                cancelAction();
                return;
            }
            this.colors = colors;

            super.setValues(colorsManager);
            tableColor = colorsManager.colorsController.tableColor;
            tableColorPalette = colorsManager.colorsController.tableColorPalette;
            loadPalettes();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (colorsManager == null || !colorsManager.getMyStage().isShowing()) {
            colorsManager = ColorsManageController.oneOpen();
        }
        ColorPaletteName palette = palettesList.getSelectionModel().getSelectedItem();
        if (palette == null) {
            popError(Languages.message("SelectPaletteCopyColors"));
            return;
        }
        if (colors == null) {
            copyColors(palette);
        } else {
            addColors(palette);
        }
    }

    protected void copyColors(ColorPaletteName palette) {
        List<ColorData> selectedColors = colorsManager.colorsController.tableView.getSelectionModel().getSelectedItems();
        if (selectedColors == null || selectedColors.isEmpty()) {
            popError(Languages.message("SelectColorsCopy"));
            return;
        }
        synchronized (this) {
            if (task != null) {
                task.cancel();
                task = null;
            }
            task = new SingletonTask<Void>() {
                private int count;

                @Override
                protected boolean handle() {
                    List<ColorPalette> cpList
                            = colorsManager.colorsController.tableColorPalette.write(palette.getCpnid(), selectedColors, false);
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
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    protected void addColors(ColorPaletteName palette) {
        synchronized (this) {
            if (task != null) {
                task.cancel();
                task = null;
            }
            task = new SingletonTask<Void>() {

                private int count;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        List<ColorData> colorsList = tableColor.writeColors(conn, colors, false);
                        if (colorsList == null) {
                            return false;
                        }
                        if (!colorsList.isEmpty()) {
                            List<ColorPalette> cpList = tableColorPalette.write(conn, palette.getCpnid(), colorsList, false);
                            if (cpList == null) {
                                return false;
                            }
                            count = cpList.size();
                            UserConfig.setUserConfigString(baseName + "Palette", palette.getName());
                        }
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
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }

    }

    protected void afterCopied(ColorPaletteName palette, int count) {
        if (colorsManager == null || !colorsManager.getMyStage().isShowing()) {
            colorsManager = ColorsManageController.oneOpen();
        } else {
            colorsManager.colorsController.loadPaletteLast(palette);
            colorsManager.toFront();
        }
        colorsManager.popInformation(Languages.message("Copied") + ": " + count);
        closeStage();
    }
}
