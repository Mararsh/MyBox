package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.data.ColorPaletteName;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableColorPalette;
import mara.mybox.db.table.TableColorPaletteName;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.PaletteTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-09-04
 * @License Apache License Version 2.0
 */
public class ColorPalettePopupController extends BaseChildController {

    protected Rectangle parentRect;
    protected TableColorPaletteName tableColorPaletteName;
    protected TableColorPalette tableColorPalette;
    protected TableColor tableColor;
    protected List<ColorPaletteName> palettes;
    protected ColorPaletteName currentPalette;
    protected SimpleBooleanProperty setNotify;

    @FXML
    protected HBox barBox;
    @FXML
    protected ControlColorsPane colorsController;
    @FXML
    protected Label label;
    @FXML
    protected Button paletteButton;
    @FXML
    protected CheckBox popCheck;

    public ColorPalettePopupController() {
        baseTitle = message("ColorPalette");
        setNotify = new SimpleBooleanProperty(false);
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            if (parentController != null) {
                return parentController.keyEventsFilter(event);
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            tableColorPaletteName = new TableColorPaletteName();
            tableColorPalette = new TableColorPalette();
            tableColor = new TableColor();
            tableColorPalette.setTableColor(tableColor);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(popCheck, message("PopColorSetWhenMouseHovering"));
            NodeStyleTools.setTooltip(cancelButton, message("PopupClose"));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            colorsController.setParameter(null, false);

            popCheck.setSelected(UserConfig.getBoolean("PopColorSetWhenMouseHovering", true));
            popCheck.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("PopColorSetWhenMouseHovering", popCheck.isSelected());
                }
            });

            colorsController.clickNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    takeColor(colorsController.clickedColor());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void load(BaseController parent, Rectangle rect) {
        try {
            thisPane.setStyle(" -fx-background-color: white;");
            refreshStyle(thisPane);

            this.parentController = parent;
            parentRect = rect;
            loadColors();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public synchronized void loadColors() {
        if (task != null) {
            task.cancel();
        }
        thisPane.setDisable(true);
        task = new SingletonTask<Void>(this) {

            protected List<ColorData> colors;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    ColorPaletteName defaultPalette = PaletteTools.defaultPalette(conn);
                    if (defaultPalette == null) {
                        return false;
                    }
                    String paletteName = UserConfig.getString("ColorPalettePopupPalette", defaultPalette.getName());
                    currentPalette = tableColorPaletteName.find(conn, paletteName);
                    if (currentPalette == null) {
                        currentPalette = defaultPalette;
                    }
                    if (currentPalette == null) {
                        return false;
                    }
                    paletteName = currentPalette.getName();
                    UserConfig.setString("ColorPalettePopupPalette", paletteName);
                    colors = tableColorPalette.colors(conn, currentPalette.getCpnid());
                    palettes = tableColorPaletteName.recentVisited(conn);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return colors != null;
            }

            @Override
            protected void whenSucceeded() {
                colorsController.loadColors(currentPalette, colors);
                label.setText(currentPalette.getName() + ": " + colors.size());
            }

            @Override
            protected void whenFailed() {
            }

            @Override
            protected void finalAction() {
                task = null;
                thisPane.setDisable(false);
            }

        };
        start(task, false);
    }

    public void takeColor(ColorData colorData) {
        if (isSettingValues || colorData == null
                || parentController == null || parentRect == null) {
            return;
        }
        try {
            parentRect.setFill(colorData.getColor());
            parentRect.setUserData(colorData);
            NodeStyleTools.setTooltip(parentRect, colorData.display());
            parentController.closePopup();
            setNotify.set(!setNotify.get());
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    public void popPaletteMenu(MouseEvent mouseEvent) {
        try {
            List<MenuItem> items = new ArrayList<>();

            if (palettes != null) {
                ToggleGroup pgroup = new ToggleGroup();
                String currentName = UserConfig.getString(baseName + "Palette", null);
                for (ColorPaletteName palette : palettes) {
                    String name = palette.getName();
                    RadioMenuItem rmenu = new RadioMenuItem(name);
                    rmenu.setOnAction((ActionEvent menuItemEvent) -> {
                        UserConfig.setString(baseName + "Palette", name);
                        loadColors();
                    });
                    rmenu.setToggleGroup(pgroup);
                    rmenu.setSelected(name.equals(currentName));
                    items.add(rmenu);
                }
            }

            items.add(new SeparatorMenuItem());

            MenuItem menu = new MenuItem(message("Select..."));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                ColorPaletteSelectorController.open(this);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            popEventMenu(mouseEvent, items);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void popDataMenu(MouseEvent mouseEvent) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem(message("ManageColors"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                ColorsManageController.oneOpen();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());
            items.addAll(PaletteTools.paletteExamplesMenu(parentController == null ? myController : parentController));

            items.add(new SeparatorMenuItem());

            popEventMenu(mouseEvent, items);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void inputAction() {
        ColorPaletteInputController.open(this);
    }

    public void addColor(ColorData colorData) {
        if (colorData == null) {
            popError(message("InvalidParameters") + ": " + message("Color"));
            return;
        }
        SingletonTask addTask = new SingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                return tableColorPalette.findAndCreate(currentPalette.getCpnid(), colorData) != null;
            }

            @Override
            protected void whenSucceeded() {
                loadPalette(currentPalette.getName());
            }
        };
        start(addTask, false);
    }

    public void loadPalette(String palette) {
        if (palette == null) {
            return;
        }
        PaletteTools.afterPaletteChanged(parentController, palette);
    }

    public SimpleBooleanProperty getSetNotify() {
        return setNotify;
    }

    @Override
    public void cleanPane() {
        try {
            setNotify = null;
            parentRect = null;
            tableColorPaletteName = null;
            tableColorPalette = null;
            tableColor = null;
            palettes = null;
            currentPalette = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        static
     */
    public static ColorPalettePopupController open(BaseController parent, Rectangle rect) {
        try {
            ColorPalettePopupController controller = (ColorPalettePopupController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ColorPalettePopupFxml, true);
            controller.load(parent, rect);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
