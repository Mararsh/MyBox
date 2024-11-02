package mara.mybox.controller;

import java.sql.Connection;
import java.util.List;
import java.util.Random;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.data.DataTag;
import mara.mybox.db.table.BaseTable;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableDataNode;
import mara.mybox.db.table.TableDataNodeTag;
import mara.mybox.db.table.TableDataTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import mara.mybox.fxml.cell.TableColorEditCell;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-9-23
 * @License Apache License Version 2.0
 */
public class ControlDataTreeNodeTags extends BaseTableViewController<DataTag> {

    protected BaseDataTreeNodeController nodeController;
    protected TableDataNode dataNodeTable;
    protected TableDataTag dataTagTable;
    protected TableDataNodeTag dataNodeTagTable;
    protected BaseTable dataTable;
    protected DataNode currentNode;
    protected boolean changed;

    @FXML
    protected TableColumn<DataTag, String> tagColumn;
    @FXML
    protected TableColumn<DataTag, Color> colorColumn;

    @Override
    public void initColumns() {
        try {
            super.initColumns();

            tableView.setEditable(true);

            tagColumn.setEditable(true);
            tagColumn.setCellValueFactory(new PropertyValueFactory<>("tag"));
            tagColumn.setCellFactory(TableAutoCommitCell.forStringColumn());
            tagColumn.setCellFactory(new Callback<TableColumn<DataTag, String>, TableCell<DataTag, String>>() {
                @Override
                public TableCell<DataTag, String> call(TableColumn<DataTag, String> param) {
                    try {
                        TableAutoCommitCell<DataTag, String> cell
                                = new TableAutoCommitCell<DataTag, String>(new DefaultStringConverter()) {

                            @Override
                            public boolean setCellValue(String value) {
                                try {
                                    if (value == null || value.isBlank() || !isEditingRow()) {
                                        return false;
                                    }
                                    for (int i = 0; i < tableData.size(); i++) {
                                        String tagName = tableData.get(i).getTag();
                                        if (value.equals(tagName)) {
                                            cancelEdit();
                                            return false;
                                        }
                                    }
                                    DataTag row = tableData.get(editingRow);
                                    if (row == null) {
                                        cancelEdit();
                                        return false;
                                    }
                                    row.setTag(value);
                                    return super.setCellValue(value);
                                } catch (Exception e) {
                                    MyBoxLog.debug(e);
                                    return false;
                                }
                            }

                        };
                        return cell;
                    } catch (Exception e) {
                        return null;
                    }
                }
            });
            tagColumn.getStyleClass().add("editable-column");

            colorColumn.setEditable(true);
            TableColor tableColor = new TableColor();
            colorColumn.setCellValueFactory(new PropertyValueFactory<>("color"));
            colorColumn.setCellFactory(new Callback<TableColumn<DataTag, Color>, TableCell<DataTag, Color>>() {
                @Override
                public TableCell<DataTag, Color> call(TableColumn<DataTag, Color> param) {
                    TableColorEditCell<DataTag> cell = new TableColorEditCell<DataTag>(myController, tableColor) {
                        @Override
                        public void colorChanged(int index, Color color) {
                            if (isSettingValues || color == null
                                    || index < 0 || index >= tableData.size()) {
                                return;
                            }
                            if (color.equals(tableData.get(index).getColor())) {
                                return;
                            }
                            tableData.get(index).setColor(color);
                        }
                    };
                    return cell;
                }
            });
            colorColumn.getStyleClass().add("editable-column");

            checkButtons();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(BaseDataTreeNodeController controller) {
        try {
            this.nodeController = controller;
            this.parentController = nodeController;
            this.baseName = nodeController.baseName;
            dataTable = nodeController.dataTable;
            dataNodeTable = nodeController.dataNodeTable;
            dataTagTable = nodeController.dataTagTable;
            dataNodeTagTable = nodeController.dataNodeTagTable;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadTags(DataNode node) {
        currentNode = node;
        tableData.clear();
        if (task != null) {
            task.cancel();
        }
        task = new FxTask<Void>(this) {
            private List<DataTag> tags;
            private List<String> nodeTagNames;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    tags = dataTagTable.readAll(conn);
                    if (currentNode != null) {
                        nodeTagNames = dataNodeTagTable.nodeTagNames(conn, currentNode.getNodeid());
                    }
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (tags != null && !tags.isEmpty()) {
                    tableData.setAll(tags);
                    if (nodeTagNames != null && !nodeTagNames.isEmpty()) {
                        isSettingValues = true;
                        for (DataTag tag : tableData) {
                            if (nodeTagNames.contains(tag.getTag())) {
                                tableView.getSelectionModel().select(tag);
                            }
                        }
                        isSettingValues = false;
                    }
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                tableChanged(false);
            }

        };
        start(task, thisPane);
    }

    @FXML
    @Override
    public void addAction() {
        DataTag tag = DataTag.create().setTag(message("Tag"));
        tableData.add(0, tag);
    }

    @FXML
    public void queryTags() {
        List<DataTag> selected = selectedItems();
        if (selected == null || selected.isEmpty()) {
            selected = tableData;
        }
        if (selected == null || selected.isEmpty()) {
            popError(message("SelectToHandle"));
            return;
        }
//        dataController.tableController.queryTags(selected);
    }

    @FXML
    public void randomColors() {
        try {
            isSettingValues = true;
            Random r = new Random();
            for (int i = 0; i < tableData.size(); i++) {
                tableData.get(i).setColor(FxColorTools.randomColor(r));
            }
            tableView.refresh();
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void saveTags() {
        if (isSettingValues) {
            return;
        }
        FxTask saveTask = new FxTask<Void>(this) {

            @Override
            protected boolean handle() {
//                return dataTagTable.updateList(tableData) >= 0;
                return false;
            }

            @Override
            protected void whenSucceeded() {
                tableChanged(false);
            }

        };
        start(saveTask, false);
    }

    //    public void saveTag(DataTag tag) {
//        if (isSettingValues) {
//            return;
//        }
//        FxTask saveTask = new FxTask<Void>(this) {
//
//            @Override
//            protected boolean handle() {
////                return dataTagTable.writeData(tag) != null;
//                return false;
//            }
//
//            @Override
//            protected void whenSucceeded() {
//                tagsChanged();
//            }
//
//        };
//        start(saveTask, false);
//    }
    @FXML
    @Override
    public void recoverAction() {
        loadTags(currentNode);
    }

    @Override
    public void tableChanged(boolean tableChanged) {
        if (isSettingValues || nodeController == null) {
            return;
        }
        super.tableChanged(changed);
        changed = tableChanged;
        nodeController.tagsChanged();
    }

}
