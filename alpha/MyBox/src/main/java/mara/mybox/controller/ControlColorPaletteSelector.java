package mara.mybox.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColorPalette;
import mara.mybox.db.data.ColorPaletteName;
import mara.mybox.db.table.TableColorPalette;
import mara.mybox.db.table.TableColorPaletteName;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.PaletteTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
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
    protected TableColorPalette tableColorPalette;
    protected ColorPaletteName allColors, defaultPalette, currentPalette;
    protected boolean isManager;
    protected String ignore;
    protected SimpleBooleanProperty selectedNotify, doubleClickedNotify, renamedNotify;

    @FXML
    protected ListView<ColorPaletteName> palettesList;
    @FXML
    protected HBox selectOpBox, manageOpBox;
    @FXML
    protected Button customizeButton, examplesButton, functionsButton;

    public ControlColorPaletteSelector() {
        baseTitle = message("ColorPalettes");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            tableColorPalette = new TableColorPalette();
            tableColorPaletteName = new TableColorPaletteName();
            allColors = new ColorPaletteName(message("AllColors"));
            selectedNotify = new SimpleBooleanProperty(false);
            doubleClickedNotify = new SimpleBooleanProperty(false);
            renamedNotify = new SimpleBooleanProperty(false);
            isManager = false;
            ignore = null;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(customizeButton, message("CustomizeColorPalette"));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void setParameter(ColorsManageController manageController, boolean isManager) {
        try {
            this.manageController = manageController;
            this.isManager = isManager;
            if (manageController != null) {
                baseName = manageController.baseName;
            }
            ignore = null;
            if (isManager) {
                if (!thisPane.getChildren().contains(manageOpBox)) {
                    thisPane.getChildren().add(0, manageOpBox);
                }
                if (thisPane.getChildren().contains(selectOpBox)) {
                    thisPane.getChildren().remove(selectOpBox);
                }
            } else {
                if (!thisPane.getChildren().contains(selectOpBox)) {
                    thisPane.getChildren().add(0, selectOpBox);
                }
                if (thisPane.getChildren().contains(manageOpBox)) {
                    thisPane.getChildren().remove(manageOpBox);
                }
            }
            thisPane.applyCss();

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

            palettesList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            palettesList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ColorPaletteName>() {
                @Override
                public void changed(ObservableValue v, ColorPaletteName oldV, ColorPaletteName newV) {
                    paletteSelected();
                }
            });

            palettesList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event == null || isSettingValues) {
                        return;
                    }
                    if (event.getClickCount() > 1) {
                        doubleClickedNotify.set(!doubleClickedNotify.get());
                    } else if (event.getButton() == MouseButton.SECONDARY) {
                        if (isManager) {
                            popNodeMenu(palettesList, makeFunctionsMenu());
                        }
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void loadPalettes() {
        if (task != null && !task.isQuit()) {
            return;
        }
        if (isManager) {
            palettesList.getItems().setAll(allColors);
        } else {
            palettesList.getItems().clear();
        }
        task = new SingletonCurrentTask<Void>(this) {
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
                    palettesList.refresh();
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
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {
            private ColorPaletteName newPalatte;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    if (tableColorPaletteName.find(conn, name) != null) {
                        error = message("AlreadyExisted");
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
                palettesList.getSelectionModel().select(newPalatte);
                popSuccessful();
            }

        };
        start(task);
    }

    public void paletteSelected() {
        if (isSettingValues) {
            return;
        }
        currentPalette = selected();
        if (!isAllColors()) {
            UserConfig.setString(baseName + "Palette", currentPalette.getName());
        }
        selectedNotify.set(!selectedNotify.get());
    }

    protected boolean isAllColors() {
        return currentPalette == null || currentPalette.getCpnid() < 0;
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

    public ColorPaletteName currentPalette() {
        return currentPalette;
    }

    public String currentPaletteName() {
        return currentPalette == null ? null : currentPalette.getName();
    }

    public long currentPaletteId() {
        return currentPalette == null ? -1 : currentPalette.getCpnid();
    }

    public List<MenuItem> makeFunctionsMenu() {
        ColorPaletteName palette = selected();
        boolean isAll = palette.getName().equals(allColors.getName());
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu = new MenuItem(StringTools.menuPrefix(palette.getName()));
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("AddPalette"), StyleTools.getIconImageView("iconAdd.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            addPalette();
        });
        items.add(menu);

        menu = new MenuItem(message("DeletePalette"), StyleTools.getIconImageView("iconDelete.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            deletePalette();
        });
        menu.setDisable(isAll);
        items.add(menu);

        menu = new MenuItem(message("RenamePalette"), StyleTools.getIconImageView("iconInput.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            renamePalette();
        });
        menu.setDisable(isAll);
        items.add(menu);

        menu = new MenuItem(message("CopyPalette"), StyleTools.getIconImageView("iconCopy.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            copyPalette();
        });
        menu.setDisable(isAll);
        items.add(menu);

        menu = new MenuItem(message("DeleteAllPalettes"), StyleTools.getIconImageView("iconClear.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            deleteAllPalettes();
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("Export"), StyleTools.getIconImageView("iconExport.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            manageController.exportCSV("all");
        });
        items.add(menu);

        menu = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            loadPalettes();
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        return items;
    }

    @FXML
    public void popFunctionsMenu(Event event) {
        if (UserConfig.getBoolean("ColorsFunctionsPopWhenMouseHovering", true)) {
            showFunctionsMenu(event);
        }
    }

    @FXML
    public void showFunctionsMenu(Event event) {
        List<MenuItem> items = makeFunctionsMenu();

        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean("ColorsFunctionsPopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean("ColorsFunctionsPopWhenMouseHovering", popItem.isSelected());
            }
        });
        items.add(popItem);

        popEventMenu(event, items);
    }

    public void deletePalette() {
        if (task != null && !task.isQuit()) {
            return;
        }
        ColorPaletteName selected = selected();
        if (selected == null) {
            popError(message("SelectColorPalette"));
            return;
        }
        if (!PopTools.askSure(getTitle(), selected.getName(), message("DeletePalette"))) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {

            @Override
            protected boolean handle() {
                return tableColorPaletteName.deleteData(selected) > 0;
            }

            @Override
            protected void whenSucceeded() {
                palettesList.getItems().remove(selected);
                popSuccessful();

            }

        };
        start(task);
    }

    public void deleteAllPalettes() {
        if (task != null && !task.isQuit()) {
            return;
        }
        if (!PopTools.askSure(getTitle(), message("DeleteAllPalettes"))) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {

            @Override
            protected boolean handle() {
                return tableColorPaletteName.clearData() >= 0;
            }

            @Override
            protected void whenSucceeded() {
                loadPalettes();
                popSuccessful();

            }

        };
        start(task);
    }

    @FXML
    public void renamePalette() {
        if (task != null && !task.isQuit()) {
            return;
        }
        ColorPaletteName selected = selected();
        if (selected == null || selected.getName().equals(message("AllColors"))) {
            popError(message("SelectColorPalette"));
            return;
        }
        String name = PopTools.askValue(baseTitle, message("RenamePalette") + "\n" + selected.getName(),
                message("NewName"), selected.getName() + "m");
        if (name == null || name.isBlank()) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {
            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    if (tableColorPaletteName.find(conn, name) != null) {
                        error = message("AlreadyExisted");
                        return false;
                    }
                    selected.setName(name);
                    return tableColorPaletteName.updateData(conn, selected) != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                palettesList.refresh();
                renamedNotify.set(!renamedNotify.get());
                popSuccessful();
            }
        };
        start(task);
    }

    @FXML
    protected void copyPalette() {
        if (task != null && !task.isQuit()) {
            return;
        }
        ColorPaletteName selected = selected();
        if (selected == null || selected.getName().equals(message("AllColors"))) {
            popError(message("SelectColorPalette"));
            return;
        }
        String name = PopTools.askValue(baseTitle, message("CopyPalette") + "\n" + selected.getName(),
                message("Name"), selected.getName() + " " + message("Copy"));
        if (name == null || name.isBlank()) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {
            private ColorPaletteName newPalatte;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection();
                        PreparedStatement query = conn.prepareStatement(TableColorPalette.QueryPalette)) {
                    if (tableColorPaletteName.find(conn, name) != null) {
                        error = message("AlreadyExisted");
                        return false;
                    }
                    newPalatte = new ColorPaletteName(name);
                    newPalatte = tableColorPaletteName.insertData(conn, newPalatte);
                    long paletteid = newPalatte.getCpnid();
                    query.setLong(1, selected.getCpnid());
                    conn.setAutoCommit(true);
                    try (ResultSet results = query.executeQuery()) {
                        conn.setAutoCommit(false);
                        while (results.next()) {
                            ColorPalette data = tableColorPalette.readData(results);
                            data.setPaletteid(paletteid);
                            tableColorPalette.insertData(conn, data);
                        }
                        conn.commit();
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                palettesList.getItems().add(newPalatte);
                popSuccessful();
            }
        };
        start(task);
    }

    public void loadPalette(String paletteName) {
        currentPalette = allColors;
        if (paletteName != null) {
            for (ColorPaletteName p : palettesList.getItems()) {
                if (p.getName().equals(paletteName)) {
                    currentPalette = p;
                    break;
                }
            }
        }
        isSettingValues = true;
        palettesList.getSelectionModel().clearSelection();
        isSettingValues = false;
        palettesList.getSelectionModel().select(currentPalette);
    }

    public boolean isCurrent(String paletteName) {
        return currentPalette != null && currentPalette.getName().equals(paletteName);
    }

    @FXML
    protected void popExamplesMenu(MouseEvent event) {
        try {
            List<MenuItem> items = new ArrayList<>();
            items.addAll(PaletteTools.paletteExamplesMenu(this));
            items.add(new SeparatorMenuItem());
            popEventMenu(event, items);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void customizePalette() {
        ColorPaletteCustomizeController.open(this);
    }

}
