package mara.mybox.controller;

import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.cell.TableRowSelectionCell;
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
            this.data2D = dataController.data2D;
            this.baseName = dataController.baseName;
            this.tableDefinition = dataController.tableDataColumn;

            deleteButton = editController.deleteRowsButton;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void loadTableData() {
        try {
            makeColumns();
            if (data2D == null || !data2D.hasData()) {
                return;
            }
            int rowsNumber = data2D.pageRowsNumber();
            if (rowsNumber <= 0) {
                tableData.clear();
            } else {
                tableData.addAll(data2D.getPageData());
            }
            data2D.setPageData(tableData);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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

            List<ColumnDefinition> columns = data2D.getColumns();
            for (int i = 0; i < columns.size(); i++) {
                ColumnDefinition column = columns.get(i);
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
                            changed(true);
                        }
                    }
                });
                tableView.getColumns().add(tableColumn);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void changed(boolean changed) {
        this.changed = changed;
        editController.tableTab.setText(message("Table") + (changed ? "*" : ""));

        data2D.setPageDataChanged(changed);
    }

    @FXML
    @Override
    public void recoverAction() {

    }

    @FXML
    @Override
    public void copyAction() {

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

}
