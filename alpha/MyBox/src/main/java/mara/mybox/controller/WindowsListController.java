package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.Window;
import mara.mybox.data.FxWindow;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TableBooleanCell;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-9-13
 * @License Apache License Version 2.0
 */
public class WindowsListController extends BaseTableViewController<FxWindow> {

    @FXML
    protected TableColumn<FxWindow, String> titleColumn, typeColumn,
            widthColumn, heightColumn, xColumn, yColumn;
    @FXML
    protected TableColumn<FxWindow, Boolean> showColumn, modalityColumn,
            focusedColumn, topColumn, childColumn,
            fullScreenColumn, iconifiedColumn, maximizedColumn, resizableColumn;
    @FXML
    protected Button onTopButton, disableOnTopButton, closeItemsButton;

    public WindowsListController() {
        baseTitle = message("WindowsList");
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();

            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
            showColumn.setCellValueFactory(new PropertyValueFactory<>("isShowing"));
            showColumn.setCellFactory(new TableBooleanCell());
            modalityColumn.setCellValueFactory(new PropertyValueFactory<>("modality"));
            focusedColumn.setCellValueFactory(new PropertyValueFactory<>("isFocused"));
            focusedColumn.setCellFactory(new TableBooleanCell());
            topColumn.setCellValueFactory(new PropertyValueFactory<>("isAlwaysOnTop"));
            topColumn.setCellFactory(new TableBooleanCell());
            childColumn.setCellValueFactory(new PropertyValueFactory<>("isChild"));
            childColumn.setCellFactory(new TableBooleanCell());
            fullScreenColumn.setCellValueFactory(new PropertyValueFactory<>("isFullScreen"));
            fullScreenColumn.setCellFactory(new TableBooleanCell());
            iconifiedColumn.setCellValueFactory(new PropertyValueFactory<>("isIconified"));
            iconifiedColumn.setCellFactory(new TableBooleanCell());
            maximizedColumn.setCellValueFactory(new PropertyValueFactory<>("isMaximized"));
            maximizedColumn.setCellFactory(new TableBooleanCell());
            resizableColumn.setCellValueFactory(new PropertyValueFactory<>("isResizable"));
            resizableColumn.setCellFactory(new TableBooleanCell());
            widthColumn.setCellValueFactory(new PropertyValueFactory<>("width"));
            heightColumn.setCellValueFactory(new PropertyValueFactory<>("height"));
            xColumn.setCellValueFactory(new PropertyValueFactory<>("x"));
            yColumn.setCellValueFactory(new PropertyValueFactory<>("y"));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();
        closeItemsButton.setDisable(isNoneSelected());
        onTopButton.setDisable(isNoneSelected());
        disableOnTopButton.setDisable(isNoneSelected());
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            refreshAction();

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }

    }

    @FXML
    @Override
    public void refreshAction() {
        try {
            tableData.clear();
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                tableData.add(new FxWindow(window));
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void closeItems() {
        try {
            List<FxWindow> selected = selectedItems();
            if (selected == null || selected.isEmpty()) {
                popError(message("SelectToHandle"));
                return;
            }
            for (FxWindow w : selected) {
                if (w.getWindow() != null) {
                    w.getWindow().hide();
                }
            }
            refreshAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void onTop() {
        try {
            List<FxWindow> selected = selectedItems();
            if (selected == null || selected.isEmpty()) {
                popError(message("SelectToHandle"));
                return;
            }
            for (FxWindow w : selected) {
                if (w.getWindow() != null && w.getWindow() instanceof Stage) {
                    ((Stage) w.getWindow()).setAlwaysOnTop(true);
                }
            }
            refreshAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void disableOnTop() {
        try {
            List<FxWindow> selected = selectedItems();
            if (selected == null || selected.isEmpty()) {
                popError(message("SelectToHandle"));
                return;
            }
            for (FxWindow w : selected) {
                if (w.getWindow() != null && w.getWindow() instanceof Stage) {
                    ((Stage) w.getWindow()).setAlwaysOnTop(false);
                }
            }
            refreshAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
