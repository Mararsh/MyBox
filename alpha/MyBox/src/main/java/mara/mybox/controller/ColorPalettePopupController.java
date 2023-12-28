package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
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
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(cancelButton, message("PopupClose"));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            colorsController.setParameter(this);

            colorsController.clickNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    takeColor(colorsController.clickedColor());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
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
            MyBoxLog.error(e);
        }
    }

    public synchronized void loadColors() {
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {

            protected List<ColorData> colors;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    ColorPaletteName defaultPalette = PaletteTools.defaultPalette(Languages.getLangName(), conn);
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

        };
        start(task, thisPane);
    }

    public void takeColor(ColorData colorData) {
        if (isSettingValues || colorData == null
                || parentController == null || parentRect == null) {
            return;
        }
        try {
            parentRect.setFill(colorData.getColor());
            parentRect.setUserData(colorData);
            NodeStyleTools.setTooltip(parentRect,
                    message("ClickColorToPalette") + "\n---------\n" + colorData.display());
            parentController.closePopup();
            setNotify.set(!setNotify.get());
        } catch (Exception e) {
            MyBoxLog.debug(e);
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
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void popFunctionsMenu(Event event) {
        showFunctionsMenu(event);
    }

    @FXML
    public void showFunctionsMenu(Event fevent) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem(message("ManageColors"), StyleTools.getIconImageView("iconManage.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                ColorsManageController.oneOpen();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("Examples"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            items.add(menu);

            items.addAll(PaletteTools.paletteExamplesMenu(parentController == null ? myController : parentController));

            items.add(new SeparatorMenuItem());

            popEventMenu(fevent, items);

        } catch (Exception e) {
            MyBoxLog.error(e);
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
        FxTask addTask = new FxTask<Void>(this) {
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
            ColorPalettePopupController controller = (ColorPalettePopupController) WindowTools.popupStage(
                    parent, Fxmls.ColorPalettePopupFxml);
            controller.load(parent, rect);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
