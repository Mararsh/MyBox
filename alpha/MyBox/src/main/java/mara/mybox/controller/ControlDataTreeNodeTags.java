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
import mara.mybox.db.data.Tag;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.table.BaseTable;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableTag;
import mara.mybox.db.table.TableNode;
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
public class ControlDataTreeNodeTags extends BaseSysTableController<Tag> {

    protected BaseDataTreeController dataController;
    protected TableNode tableTree;
    protected TableTag tableTag;
    protected TableDataTag tableTreeTag;
    protected BaseTable dataTable;
    protected TreeNode currentNode;

    @FXML
    protected TableColumn<Tag, String> tagColumn;
    @FXML
    protected TableColumn<Tag, Color> colorColumn;
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
            tagColumn.setCellFactory(new Callback<TableColumn<Tag, String>, TableCell<Tag, String>>() {
                @Override
                public TableCell<Tag, String> call(TableColumn<Tag, String> param) {
                    try {
                        TableAutoCommitCell<Tag, String> cell = new TableAutoCommitCell<Tag, String>(new DefaultStringConverter()) {

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
                                    Tag row = tableData.get(editingRow);
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
            colorColumn.setCellFactory(new Callback<TableColumn<Tag, Color>, TableCell<Tag, Color>>() {
                @Override
                public TableCell<Tag, Color> call(TableColumn<Tag, Color> param) {
                    TableColorEditCell<Tag> cell = new TableColorEditCell<Tag>(myController, tableColor) {
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
            tableTree = dataController.treeTable;
            tableTag = dataController.tagTable;
            tableTreeTag = dataController.treeTagTable;
            setTableDefinition(tableTag);

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

    public String askName() {
        String name = PopTools.askValue(getBaseTitle(),
                message("Add"), message("Tag"), message("Tag") + new Date().getTime());
        if (name == null || name.isBlank()) {
            return null;
        }
        for (Tag tag : tableData) {
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
            private Tag tag = null;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    tag = tableTag.insertData(conn, new Tag(tableTree.getTableName(), name));
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

    public void saveTag(Tag tag) {
        if (isSettingValues) {
            return;
        }
        FxTask saveTask = new FxTask<Void>(this) {

            @Override
            protected boolean handle() {
                return tableTag.writeData(tag) != null;
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
        List<Tag> selected = selectedItems();
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
                return tableTag.updateList(tableData) >= 0;
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
