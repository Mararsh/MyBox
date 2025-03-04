package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import mara.mybox.data.Pagination;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFilter;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import static mara.mybox.db.data.ColumnDefinition.isTimeType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxBackgroundTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.cell.TableDataBooleanDisplayCell;
import mara.mybox.fxml.cell.TableDataBooleanEditCell;
import mara.mybox.fxml.cell.TableDataColorEditCell;
import mara.mybox.fxml.cell.TableDataCoordinateEditCell;
import mara.mybox.fxml.cell.TableDataDateEditCell;
import mara.mybox.fxml.cell.TableDataDisplayCell;
import mara.mybox.fxml.cell.TableDataEditCell;
import mara.mybox.fxml.cell.TableDataEnumCell;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-12-17
 * @License Apache License Version 2.0
 */
public class BaseData2DTableController extends BaseTablePagesController<List<String>> {

    protected Data2D data2D;
    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected boolean readOnly, widthChanged;
    protected SimpleBooleanProperty statusNotify;
    protected DataFilter styleFilter;

    @FXML
    protected TableColumn<List<String>, Integer> dataRowColumn;
    @FXML
    protected Label dataLabel;
    @FXML
    protected VBox dataBox;
    @FXML
    protected Button fileMenuButton, dataManufactureButton;

    public BaseData2DTableController() {
        statusNotify = new SimpleBooleanProperty(false);
        readOnly = true;
        styleFilter = new DataFilter();
        pagination = new Pagination(Pagination.ObjectType.Table);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            if (dataRowColumn != null) {
                dataRowColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<List<String>, Integer>, ObservableValue<Integer>>() {
                    @Override
                    public ObservableValue<Integer> call(TableColumn.CellDataFeatures<List<String>, Integer> param) {
                        try {
                            List<String> row = (List<String>) param.getValue();
                            Integer v = Integer.valueOf(row.get(0));
                            if (v < 0) {
                                return null;
                            }
                            return new ReadOnlyObjectWrapper(v);
                        } catch (Exception e) {
                            return null;
                        }
                    }
                });
                dataRowColumn.setEditable(false);
                dataRowColumn.setCellFactory(new Callback<TableColumn<List<String>, Integer>, TableCell<List<String>, Integer>>() {
                    @Override
                    public TableCell<List<String>, Integer> call(TableColumn<List<String>, Integer> param) {
                        try {
                            TableCell<List<String>, Integer> cell = new TableCell<List<String>, Integer>() {
                                @Override
                                public void updateItem(Integer item, boolean empty) {
                                    super.updateItem(item, empty);
                                    setGraphic(null);
                                    if (empty || item == null) {
                                        setText(null);
                                        return;
                                    }
                                    setText(item + "");
                                }
                            };
                            cell.getStyleClass().add("row-number");
                            return cell;
                        } catch (Exception e) {
                            return null;
                        }
                    }
                });

                dataRowColumn.setPrefWidth(UserConfig.getInt("DataRowColumnWidth", 100));
                dataRowColumn.widthProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> o, Number ov, Number nv) {
                        UserConfig.setInt("DataRowColumnWidth", nv.intValue());
                    }
                });
            }

            initMoreControls();

            updateStatus();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initMoreControls() {

    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.DataFile);
    }

    /*
        status
     */
    public void resetData() {
        resetStatus();
        dataSizeLoaded = true;
        if (data2D != null) {
            data2D.resetData();
        }
        isSettingValues = true;
        tableData.clear();
        isSettingValues = false;

        updateInterfaceStatus();

        notifyLoaded();
    }

    public synchronized void resetStatus() {
        if (task != null) {
            task.cancel();
        }
        if (backgroundTask != null) {
            backgroundTask.cancel();
        }
        if (data2D != null) {
            data2D.setTableChanged(false);
        }
    }

    @Override
    public void updateStatus() {
        try {
            updateInterfaceStatus();

            statusNotify.set(!statusNotify.get());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void updateInterfaceStatus() {
        try {
            super.updateStatus();

            sourceFile = data2D != null ? data2D.getFile() : null;
            if (dataManufactureButton != null) {
                dataManufactureButton.setDisable(!isValidData());
            }

            if (dataLabel != null) {
                dataLabel.setText(data2D != null ? data2D.displayName() : "");
            }
            myStage = getMyStage();
            if (myStage == null) {
                return;
            }
            String title = getRootBaseTitle();
            if (data2D != null) {
                title += " : ";
                if (data2D.isTableChanged()) {
                    title += " * ";
                }
                title += data2D.displayName();

            }
            myStage.setTitle(title);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean isValidData() {
        return data2D != null && data2D.isValidDefinition();
    }

    public void notifySaved() {
    }

    @Override
    public void notifyLoaded() {
        if (loadedNotify == null) {
            return;
        }
        loadedNotify.set(!loadedNotify.get());
        if (data2D != null && data2D.getFile() != null) {
            recordFileOpened(data2D.getFile());
        }
    }

    @Override
    public void tableChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
        if (data2D != null) {
            isSettingValues = true;
            data2D.setPageData(tableData);
            data2D.setTableChanged(changed);
            isSettingValues = false;
        }
        updateStatus();
    }

    public void pageDataChanged() {
        tableChanged(true);
    }

    /*
        table
     */
    public void makeColumns() {
        try {
            isSettingValues = true;
            tableData.clear();
            tableView.getColumns().remove(
                    rowsSelectionColumn != null && tableView.getColumns().contains(rowsSelectionColumn) ? 2 : 1,
                    tableView.getColumns().size());
            tableView.setItems(tableData);
            isSettingValues = false;
            widthChanged = false;

            if (!isValidData()) {
                return;
            }
            List<Data2DColumn> columns = data2D.getColumns();
            TableColor tableColor = null;
            for (int i = 0; i < columns.size(); i++) {
                Data2DColumn dataColumn = columns.get(i);
                String name = dataColumn.getColumnName();
                ColumnType type = dataColumn.getType();
                TableColumn tableColumn = new TableColumn<List<String>, String>(name);
                tableColumn.setPrefWidth(dataColumn.getWidth());
                tableColumn.setEditable(!readOnly && dataColumn.isEditable() && !dataColumn.isId());
                tableColumn.setUserData(dataColumn.getIndex());
                int colIndex = i + 1;

                tableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<List<String>, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<List<String>, String> param) {
                        try {
                            List<String> row = (List<String>) param.getValue();
                            return new SimpleStringProperty(row.get(colIndex));
                        } catch (Exception e) {
                            return null;
                        }
                    }
                });

                if (tableColumn.isEditable()) {

                    if (type == ColumnType.Enumeration
                            || type == ColumnType.EnumeratedShort
                            || type == ColumnType.EnumerationEditable) {
                        tableColumn.setCellFactory(TableDataEnumCell.create(this, dataColumn, dataColumn.enumNames(), 12));

                    } else if (type == ColumnType.Boolean) {
                        tableColumn.setCellFactory(TableDataBooleanEditCell.create(this, dataColumn, colIndex));

                    } else if (type == ColumnType.Color) {
                        if (tableColor == null) {
                            tableColor = new TableColor();
                        }
                        tableColumn.setCellFactory(TableDataColorEditCell.create(this, dataColumn, tableColor));

                    } else if (isTimeType(type)) {
                        tableColumn.setCellFactory(TableDataDateEditCell.create(this, dataColumn));

                    } else if (type == ColumnType.Longitude || type == ColumnType.Latitude) {
                        tableColumn.setCellFactory(TableDataCoordinateEditCell.create(this, dataColumn));

                    } else {
                        tableColumn.setCellFactory(TableDataEditCell.create(this, dataColumn));
                    }
                    tableColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<List<String>, String>>() {
                        @Override
                        public void handle(TableColumn.CellEditEvent<List<String>, String> e) {
                            if (e == null) {
                                return;
                            }
                            int rowIndex = e.getTablePosition().getRow();
                            if (rowIndex < 0 || rowIndex >= tableData.size()) {
                                return;
                            }
                            List<String> row = tableData.get(rowIndex);
                            row.set(colIndex, dataColumn.formatValue(e.getNewValue()));
                            tableData.set(rowIndex, row);
                        }
                    });
                    tableColumn.getStyleClass().add("editable-column");
                } else {
                    if (type == ColumnType.Boolean) {
                        tableColumn.setCellFactory(TableDataBooleanDisplayCell.create(this, dataColumn));
                    } else {
                        tableColumn.setCellFactory(TableDataDisplayCell.create(this, dataColumn));
                    }
                }

                tableColumn.setComparator(new Comparator<String>() {
                    @Override
                    public int compare(String v1, String v2) {
                        return dataColumn.compare(v1, v2);
                    }
                });

                if (dataColumn.isAuto()) {
                    tableColumn.getStyleClass().clear();
                    tableColumn.getStyleClass().add("auto-column");
                } else if (dataColumn.isIsPrimaryKey()) {
                    tableColumn.getStyleClass().clear();
                    tableColumn.getStyleClass().add("primary-column");
                }

                tableView.getColumns().add(tableColumn);
                tableColumn.widthProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> o, Number ov, Number nv) {
                        if (nv == null) {
                            return;
                        }
                        widthChanged = true;
                        dataColumn.setWidth(nv.intValue());
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkBeforeLoadingTableData() {
        return isValidData();
    }

    @Override
    public List<List<String>> readPageData(FxTask currentTask, Connection conn) {
        data2D.startFilter(null);
        return data2D.readPageData(conn);
    }

    @Override
    public void postLoadedTableData() {
        if (data2D != null) {
            isSettingValues = true;
            data2D.stopTask();
            sourceFile = data2D.getFile();
            data2D.setPageData(tableData);
            isSettingValues = false;
        }
        super.postLoadedTableData();
    }

    @Override
    public long readDataSize(FxTask currentTask, Connection conn) {
        return pagination.rowsNumber;
    }

    @Override
    public void loadDataSize() {
        if (!isValidData() || dataSizeLoaded
                || data2D.isTmpData() || data2D.isMatrix()) {
            afterLoaded();
            return;
        }
        if (backgroundTask != null) {
            backgroundTask.cancel();
            backgroundTask = null;
        }
        data2D.setRowsNumber(-1);
        dataSizeLoaded = false;
        data2D.setDataLoaded(false);
        if (paginationController != null) {
            paginationController.show(false);
        }
        if (saveButton != null) {
            saveButton.setDisable(true);
        }
        if (recoverButton != null) {
            recoverButton.setDisable(true);
        }
        backgroundTask = new FxBackgroundTask<Void>(this) {

            @Override
            protected boolean handle() {
                data2D.setBackgroundTask(this);
                return data2D.readTotal() >= 0;
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void whenFailed() {
                if (isCancelled()) {
                    return;
                }
                if (error != null) {
                    popError(message(error));
                } else {
                    popFailed();
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.setBackgroundTask(null);
                afterLoaded();
            }

        };
        start(backgroundTask, false);
    }

    protected void afterLoaded() {
        if (!isValidData() || data2D.isTmpData()) {
            setLoaded();
            return;
        }
        FxTask updateTask = new FxTask<Void>(this) {
            @Override
            protected boolean handle() {
                data2D.countPageSize();
                return data2D.saveAttributes();
            }

            @Override
            protected void whenSucceeded() {
                setLoaded();
            }

        };
        start(updateTask, false);
    }

    protected void setLoaded() {
        dataSizeLoaded = true;
        data2D.setDataLoaded(true);
        if (saveButton != null) {
            saveButton.setDisable(false);
        }
        if (data2D.isMatrix()) {
            pagination.pageSize = Integer.MAX_VALUE;
            pagination.pagesNumber = 1;
            pagination.currentPage = 0;
            pagination.startRowOfCurrentPage = 0;
        } else {
            pagination.goPage(pagination.rowsNumber, pagination.currentPage);
        }
        updateStatus();
        notifyLoaded();
    }

    public void correctDataSize() {

    }

    @Override
    public boolean isShowPagination() {
        return isValidData() && dataSizeLoaded
                && !data2D.isMatrix() && !data2D.isTmpData();
    }


    /*
        interface
     */
    @Override
    protected List<MenuItem> makeTableContextMenu() {
        try {
            if (data2D == null || !data2D.isValidDefinition()) {
                return null;
            }
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem(message("DataDefinition") + "    Ctrl+i " + message("Or") + " Alt+i",
                    StyleTools.getIconImageView("iconInfo.png"));
            menu.setOnAction((ActionEvent event) -> {
                infoAction();
            });
            items.add(menu);

            if (this instanceof Data2DManufactureController) {
                menu = new MenuItem(message("DefineData"), StyleTools.getIconImageView("iconMeta.png"));
                menu.setOnAction((ActionEvent event) -> {
                    ((Data2DManufactureController) this).definitonAction();
                });
                items.add(menu);

            } else {
                menu = new MenuItem(message("DataManufacture"), StyleTools.getIconImageView("iconEdit.png"));
                menu.setOnAction((ActionEvent event) -> {
                    dataManufacture();
                });
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());

            if (data2D.isTextFile()) {
                menu = new MenuItem(message("TextFile"), StyleTools.getIconImageView("iconTxt.png"));
                menu.setOnAction((ActionEvent event) -> {
                    editTextFile();
                });
                items.add(menu);
            }

            menu = new MenuItem(message("SnapshotWindow"), StyleTools.getIconImageView("iconSnapshot.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                snapAction();
            });
            items.add(menu);

            menu = new MenuItem("Html", StyleTools.getIconImageView("iconHtml.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                htmlAction();
            });
            items.add(menu);

            menu = new MenuItem(message("Data"), StyleTools.getIconImageView("iconData.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                dataAction();
            });
            items.add(menu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public boolean controlAltC() {
        if (targetIsTextInput()) {
            return false;
        }
        copyToSystemClipboard();
        return true;
    }

    @Override
    public boolean controlAltV() {
        if (targetIsTextInput()) {
            return false;
        }
        pasteContentInSystemClipboard();
        return true;
    }

    @FXML
    @Override
    public boolean infoAction() {
        if (data2D == null) {
            return false;
        }
        String info = data2D.dataInfo();
        if (info != null && !info.isBlank()) {
            HtmlPopController.showHtml(this, HtmlWriteTools.table(info));
            return true;
        }
        return false;
    }

    @FXML
    public void dataManufacture() {
        if (data2D == null) {
            return;
        }
        Data2DManufactureController.openDef(data2D);
    }

    @FXML
    public void editTextFile() {
        if (data2D == null || data2D.getFile() == null) {
            return;
        }
        TextEditorController controller = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
        controller.sourceFileChanged(data2D.getFile());
        controller.requestMouse();
    }

    @Override
    public void cleanPane() {
        try {
            statusNotify = null;
            data2D = null;
            tableData2DDefinition = null;
            tableData2DColumn = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }


    /*
        get
     */
    public Data2D getData2D() {
        return data2D;
    }

    public TableView<List<String>> getTableView() {
        return tableView;
    }

    public ObservableList<List<String>> getTableData() {
        return tableData;
    }

    public DataFilter getStyleFilter() {
        return styleFilter;
    }

}
