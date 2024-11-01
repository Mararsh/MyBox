package mara.mybox.controller;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import mara.mybox.fxml.cell.TableColorEditCell;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-9-23
 * @License Apache License Version 2.0
 */
public class ControlDataTreeNodeTags extends BaseSysTableController<DataTag> {

    protected BaseDataTreeController dataController;
    protected TableDataNode dataNodeTable;
    protected TableDataTag dataTagTable;
    protected TableDataNodeTag dataNodeTagTable;
    protected BaseTable dataTable;
    protected DataNode currentNode;

    @FXML
    protected TableColumn<DataTag, String> tagColumn;
    @FXML
    protected TableColumn<DataTag, Color> colorColumn;
    @FXML
    protected Button queryTagsButton, deleteTagsButton;

    public ControlDataTreeNodeTags() {
        loadInBackground = true;
    }

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
                        TableAutoCommitCell<DataTag, String> cell = new TableAutoCommitCell<DataTag, String>(new DefaultStringConverter()) {

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
                                    saveTag(row);
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
                            saveTag(tableData.get(index));
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

    public void setParameters(BaseDataTreeController controller) {
        try {
            this.dataController = controller;
            this.parentController = dataController;
            this.baseName = dataController.baseName;
            dataTable = dataController.dataTable;
            dataNodeTable = dataController.dataNodeTable;
            dataTagTable = dataController.dataTagTable;
            dataNodeTagTable = dataController.dataNodeTagTable;
            setTableDefinition(dataTagTable);

            currentPage = 0;
            pageSize = Integer.MAX_VALUE;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void checkButtons() {
        super.checkButtons();
        boolean none = isNoneSelected();
        if (deleteTagsButton != null) {
            deleteTagsButton.setDisable(none);
        }
        if (queryTagsButton != null) {
            queryTagsButton.setDisable(tableData == null || tableData.isEmpty());
        }
    }

    public void loadTags(DataNode node) {
        currentNode = node;
        tableData.clear();
        if (currentNode == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxTask<Void>(this) {
            private List<DataTag> tags;
            private List<String> nodeTagNames;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    tags = tableDefinition.readAll(conn);
                    nodeTagNames = dataNodeTagTable.nodeTagNames(conn, currentNode.getNodeid());
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
                }
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

        };
        start(task, thisPane);
    }

    public String askName() {
        String name = PopTools.askValue(getBaseTitle(),
                message("Add"), message("Tag"), message("Tag") + new Date().getTime());
        if (name == null || name.isBlank()) {
            return null;
        }
        for (DataTag tag : tableData) {
            if (name.equals(tag.getTag())) {
                popError(message("AlreadyExisted"));
                return null;
            }
        }
        return name;
    }

    @FXML
    public void addTag() {
        String name = askName();
        if (name == null || name.isBlank()) {
            return;
        }
        FxTask tagTask = new FxTask<Void>(this) {
            private DataTag tag = null;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
//                    tag = dataTagTable.insertData(conn, new Tag(dataNodeTable.getTableName(), name));
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return tag != null;
            }

            @Override
            protected void whenSucceeded() {
                tableData.add(0, tag);
                popSuccessful();
                tagsChanged();
            }

        };
        start(tagTask, thisPane);
    }

    public void saveTag(DataTag tag) {
        if (isSettingValues) {
            return;
        }
        FxTask saveTask = new FxTask<Void>(this) {

            @Override
            protected boolean handle() {
//                return dataTagTable.writeData(tag) != null;
                return false;
            }

            @Override
            protected void whenSucceeded() {
                tagsChanged();
            }

        };
        start(saveTask, false);
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
            saveAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void saveAction() {
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
                tagsChanged();
            }

        };
        start(saveTask, false);
    }

    @Override
    public void notifyLoaded() {
        super.notifyLoaded();
        tagsChanged();
    }

    @Override
    public void tableChanged(boolean changed) {
        super.tableChanged(changed);
        if (!changed || isSettingValues || isSettingTable) {
            return;
        }
        tagsChanged();
    }

    public void tagsChanged() {
        if (isSettingValues) {
            return;
        }
//        dataController.tagsChanged();
    }

}
