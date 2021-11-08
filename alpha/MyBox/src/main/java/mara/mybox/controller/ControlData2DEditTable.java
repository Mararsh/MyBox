package mara.mybox.controller;

import java.util.List;
import java.util.Optional;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.cell.TableRowSelectionCell;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import thridparty.TableAutoCommitCell;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2DEditTable extends ControlData2DEditTable_Operations {

    public ControlData2DEditTable() {
    }

    protected void setParameters(ControlData2DEdit editController) {
        try {
            this.editController = editController;
            dataController = editController.dataController;
            tableData2DDefinition = dataController.tableData2DDefinition;
            tableData2DColumn = dataController.tableData2DColumn;
            this.data2D = dataController.data2D;
            this.baseName = dataController.baseName;

            paginationBox = dataController.paginationBox;
            pageSizeSelector = dataController.pageSizeSelector;
            pageSelector = dataController.pageSelector;
            pageLabel = dataController.pageLabel;
            dataSizeLabel = dataController.dataSizeLabel;
            pagePreviousButton = dataController.pagePreviousButton;
            pageNextButton = dataController.pageNextButton;
            pageFirstButton = dataController.pageFirstButton;
            pageLastButton = dataController.pageLastButton;

            initPagination();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadData() {
        try {
            data2D.setTableChanged(false);
            makeColumns();
            loadPage(0);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkBeforeLoadingTableData() {
        return checkBeforeNextAction();
    }

    @Override
    public long readDataSize() {
        return data2D.getDataSize();
    }

    @Override
    public List<List<String>> readPageData() {
        data2D.setTask(task);
        return data2D.readPageData();
    }

    @Override
    protected void countPagination(long page) {
        super.countPagination(page);
        data2D.setPageSize(pageSize);
        data2D.setPagesNumber(pagesNumber);
        data2D.setCurrentPage(currentPage);
        data2D.setStartRowOfCurrentPage(startRowOfCurrentPage);
    }

    @Override
    public void postLoadedTableData() {
        super.postLoadedTableData();
        data2D.setTask(null);
        tableChanged(false);
        data2D.notifyPageLoaded();
    }

    @Override
    protected void setPagination() {
        try {
            if (data2D == null) {
                return;
            }
            if (!data2D.isTotalRead()) {
                paginationBox.setVisible(false);
                return;
            }
            paginationBox.setVisible(true);
            super.setPagination();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    protected void refreshPagination() {
        countPagination(currentPage);
        setPagination();
        updateSizeLabel();
    }

    public void makeColumns() {
        try {
            tableData.clear();
            tableView.getColumns().remove(1, tableView.getColumns().size());
            if (data2D == null || !data2D.isColumnsValid()) {
                return;
            }
            tableView.setEditable(true);
            rowsSelectionColumn.setCellFactory(TableRowSelectionCell.create(tableView, data2D.getStartRowOfCurrentPage()));

            List<Data2DColumn> columns = data2D.getColumns();
            for (int i = 0; i < columns.size(); i++) {
                Data2DColumn column = columns.get(i);
                String name = column.getName();
                TableColumn tableColumn = new TableColumn<List<String>, String>(name);
                tableColumn.setPrefWidth(column.getWidth());
                tableColumn.setEditable(true);
                int col = i;

                tableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<List<String>, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<List<String>, String> param) {
                        try {
                            List<String> row = (List<String>) param.getValue();
                            String value = row.get(col);
                            if (value == null) {
                                return null;
                            }
                            return new SimpleStringProperty(value);
                        } catch (Exception e) {
                            return null;
                        }
                    }
                });

                tableColumn.setCellFactory(new Callback<TableColumn<List<String>, String>, TableCell<List<String>, String>>() {
                    @Override
                    public TableCell<List<String>, String> call(TableColumn<List<String>, String> param) {
                        try {
                            TableAutoCommitCell<List<String>, String> cell
                                    = new TableAutoCommitCell<List<String>, String>(new DefaultStringConverter()) {
                                @Override
                                public boolean valid(final String value) {
                                    return column.validValue(value);
                                }
                            };
                            return cell;
                        } catch (Exception e) {
                            return null;
                        }
                    }
                });
                tableColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<List<String>, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<List<String>, String> t) {
                        if (t == null) {
                            return;
                        }
                        List<String> row = t.getRowValue();
                        String newValue = t.getNewValue();
                        if (row == null || column.validValue(newValue)) {
                            return;
                        }
                        String oldValue = row.get(col);
                        if ((newValue == null && oldValue != null)
                                || (newValue != null && !newValue.equals(oldValue))) {
                            row.set(col, newValue);
                            tableChanged(true);
                        }
                    }
                });
                tableView.getColumns().add(tableColumn);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void tableChanged(boolean changed) {
        editController.tableTab.setText(message("Table") + (changed ? "*" : ""));

        data2D.setTableChanged(changed);
    }

    @FXML
    @Override
    public void copyAction() {

    }

    @FXML
    public void insertAction() {

    }

    @FXML
    @Override
    public void saveAction() {
        data2D.savePageData();
    }

    @FXML
    public void popFunctionsMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem(message("Copy"));
            menu.setOnAction((ActionEvent event) -> {
                copyAction();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Pop"));
            menu.setOnAction((ActionEvent event) -> {
                popAction();
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            LocateTools.locateBelow((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkBeforeNextAction() {
        boolean goOn;
        if (!data2D.isTableChanged()) {
            goOn = true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setHeaderText(getMyStage().getTitle());
            alert.setContentText(Languages.message("NeedSaveBeforeAction"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(Languages.message("Save"));
            ButtonType buttonNotSave = new ButtonType(Languages.message("NotSave"));
            ButtonType buttonCancel = new ButtonType(Languages.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSave) {
                saveAction();
                goOn = false;
            } else {
                goOn = result.get() == buttonNotSave;
            }
        }
        if (goOn) {
            if (task != null) {
                task.cancel();
            }
            if (backgroundTask != null) {
                backgroundTask.cancel();
            }
            data2D.setTableChanged(false);
        }
        return goOn;
    }

    @Override
    public void cleanPane() {
        try {
            editController = null;
            dataController = null;
            data2D = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }
}
