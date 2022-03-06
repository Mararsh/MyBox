package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableColorPalette;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-09-01
 * @License Apache License Version 2.0
 */
public class ColorInputController extends BaseController {

    protected ColorsManageController colorsManager;

    @FXML
    protected TextArea valuesArea;
    @FXML
    protected Button examplesButton;
    @FXML
    protected ColorPicker colorPicker;

    public ColorInputController() {
        baseTitle = Languages.message("InputColors");
    }

    public void setParameters(ColorsManageController colorsManager) {
        try {
            this.colorsManager = colorsManager;
            NodeStyleTools.removeTooltip(examplesButton);

            colorPicker.valueProperty().addListener((ObservableValue<? extends Color> ov, Color oldVal, Color newVal) -> {
                if (isSettingValues || newVal == null) {
                    return;
                }
                valuesArea.appendText(FxColorTools.color2rgba(newVal) + "\n");
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    public void popExamples(MouseEvent mouseEvent) {
        PopTools.popColorExamples(this, valuesArea, mouseEvent);
    }

    @FXML
    @Override
    public void okAction() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    String[] values = valuesArea.getText().split("\n");
                    if (values == null || values.length == 0) {
                        return true;
                    }
                    try ( Connection conn = DerbyBase.getConnection();) {
                        TableColor tableColor = null;
                        TableColorPalette tableColorPalette = null;
                        long paletteid = -1;
                        if (colorsManager != null) {
                            tableColor = colorsManager.colorsController.tableColor;
                            if (!colorsManager.colorsController.isAllColors()) {
                                paletteid = colorsManager.colorsController.currentPalette.getCpnid();
                            }
                            tableColorPalette = colorsManager.colorsController.tableColorPalette;
                        }
                        if (tableColor == null) {
                            tableColor = new TableColor();
                        }
                        if (tableColorPalette == null) {
                            tableColorPalette = new TableColorPalette();
                        }
                        conn.setAutoCommit(false);
                        for (String value : values) {
                            value = value.trim();
                            ColorData color = new ColorData(value).calculate();
                            if (color.getSrgb() == null) {
                                continue;
                            }
                            tableColor.write(conn, color, true);
                            if (paletteid >= 0) {
                                tableColorPalette.findAndCreate(conn, paletteid, color, false);
                            }
                        }
                        conn.commit();
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (colorsManager == null || !colorsManager.getMyStage().isShowing()) {
                        colorsManager = ColorsManageController.oneOpen();
                    } else {
                        colorsManager.colorsController.refreshPalette();
                    }
                    closeStage();
                }

            };
            start(task);
        }
    }

    @FXML
    public void queryAction() {
        openStage(Fxmls.ColorQueryFxml);
    }

    @FXML
    @Override
    public void clearAction() {
        valuesArea.clear();
    }

    @FXML
    @Override
    public void cancelAction() {
        closeStage();
    }

    /*
        static methods
     */
    public static ColorInputController oneOpen(ColorsManageController parent) {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof ColorInputController) {
                ((ColorInputController) object).close();
            }
        }
        ColorInputController controller = (ColorInputController) WindowTools.openChildStage(parent.getMyStage(), Fxmls.ColorInputFxml, false);
        controller.setParameters(parent);
        return controller;
    }
}
