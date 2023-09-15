package mara.mybox.controller;

import java.util.List;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.data2d.Data2DColumnTools;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TableCheckboxCell;

/**
 * @Author Mara
 * @CreateDate 2023-9-10
 * @License Apache License Version 2.0
 */
public class ControlData2DDefColumns extends BaseData2DColumnsController {

    protected Data2DDefinitionEditor editor;

    public ControlData2DDefColumns() {
    }

    @Override
    public void initColumns() {
        try {
            super.initColumns();

            primaryColumn.setCellFactory(new Callback<TableColumn<Data2DColumn, Boolean>, TableCell<Data2DColumn, Boolean>>() {
                @Override
                public TableCell<Data2DColumn, Boolean> call(TableColumn<Data2DColumn, Boolean> param) {
                    try {
                        TableCheckboxCell<Data2DColumn, Boolean> cell = new TableCheckboxCell<>() {
                            @Override
                            protected boolean getCellValue(int rowIndex) {
                                try {
                                    return tableData.get(rowIndex).isIsPrimaryKey();
                                } catch (Exception e) {
                                    return false;
                                }
                            }

                            @Override
                            protected void setCellValue(int rowIndex, boolean value) {
                                try {
                                    if (isChanging || rowIndex < 0) {
                                        return;
                                    }
                                    Data2DColumn column = tableData.get(rowIndex);
                                    if (column == null) {
                                        return;
                                    }
                                    if (value != column.isIsPrimaryKey()) {
                                        isChanging = true;
                                        column.setIsPrimaryKey(value);
                                        status(Status.Modified);
                                        isChanging = false;
                                    }
                                } catch (Exception e) {
                                    MyBoxLog.debug(e);
                                }
                            }
                        };
                        return cell;
                    } catch (Exception e) {
                        return null;
                    }
                }
            });
            primaryColumn.setEditable(true);
            primaryColumn.getStyleClass().add("editable-column");

            autoColumn.setCellFactory(new Callback<TableColumn<Data2DColumn, Boolean>, TableCell<Data2DColumn, Boolean>>() {
                @Override
                public TableCell<Data2DColumn, Boolean> call(TableColumn<Data2DColumn, Boolean> param) {
                    try {
                        TableCheckboxCell<Data2DColumn, Boolean> cell = new TableCheckboxCell<>() {
                            @Override
                            protected boolean getCellValue(int rowIndex) {
                                try {
                                    return tableData.get(rowIndex).isAuto();
                                } catch (Exception e) {
                                    return false;
                                }
                            }

                            @Override
                            protected void setCellValue(int rowIndex, boolean value) {
                                try {
                                    if (isChanging || rowIndex < 0) {
                                        return;
                                    }
                                    Data2DColumn column = tableData.get(rowIndex);
                                    if (column == null) {
                                        return;
                                    }
                                    if (value != column.isAuto()) {
                                        isChanging = true;
                                        column.setAuto(value);
                                        status(Status.Modified);
                                        isChanging = false;
                                    }
                                } catch (Exception e) {
                                    MyBoxLog.debug(e);
                                }
                            }
                        };
                        return cell;
                    } catch (Exception e) {
                        return null;
                    }
                }
            });
            autoColumn.setEditable(true);
            autoColumn.getStyleClass().add("editable-column");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void status(Status newStatus) {
        if (isSettingValues) {
            return;
        }
        status = newStatus;
        if (editor == null) {
            return;
        }
        editor.valueChanged(status == Status.Modified);
    }

    public void load(String def) {
        try {
            isSettingValues = true;
            tableData.clear();
            List<Data2DColumn> columns = Data2DColumnTools.fromXML(def);
            if (columns != null) {
                tableData.setAll(columns);
            }
            isSettingValues = false;
            checkSelected();
            status(Status.Loaded);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public String toXML() {
        return Data2DColumnTools.toXML(tableData);
    }

}
