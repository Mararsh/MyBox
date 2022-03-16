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
import mara.mybox.db.data.TreeLeafTag;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableTag;
import mara.mybox.db.table.TableTree;
import mara.mybox.db.table.TableTreeLeaf;
import mara.mybox.db.table.TableTreeLeafTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import mara.mybox.fxml.cell.TableColorCommitCell;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-9-23
 * @License Apache License Version 2.0
 */
public class TreeTagsController extends BaseSysTableController<Tag> {

    protected TreeManageController treeController;
    protected TableTree tableTree;
    protected TableTreeLeaf tableTreeLeaf;
    protected TableTag tableTag;
    protected TableTreeLeafTag tableTreeLeafTag;

    protected String category;

    @FXML
    protected TableColumn<Tag, String> tagColumn;
    @FXML
    protected TableColumn<Tag, Color> colorColumn;
    @FXML
    protected Button queryTagsButton, deleteTagsButton;

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
                            public void commitEdit(String value) {
                                try {
                                    if (value == null || value.isBlank()) {
                                        return;
                                    }
                                    for (int i = 0; i < tableData.size(); i++) {
                                        String tagName = tableData.get(i).getTag();
                                        if (value.equals(tagName)) {
                                            cancelEdit();
                                            return;
                                        }
                                    }
                                    int rowIndex = rowIndex();
                                    if (rowIndex < 0) {
                                        cancelEdit();
                                        return;
                                    }
                                    Tag row = tableData.get(rowIndex);
                                    if (row == null) {
                                        cancelEdit();
                                        return;
                                    }
                                    super.commitEdit(value);
                                    row.setTag(value);
                                    saveTag(row);
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
            tagColumn.getStyleClass().add("editable-column");

            colorColumn.setEditable(true);
            TableColor tableColor = new TableColor();
            colorColumn.setCellValueFactory(new PropertyValueFactory<>("color"));
            colorColumn.setCellFactory(new Callback<TableColumn<Tag, Color>, TableCell<Tag, Color>>() {
                @Override
                public TableCell<Tag, Color> call(TableColumn<Tag, Color> param) {
                    TableColorCommitCell<Tag> cell = new TableColorCommitCell<Tag>(myController, tableColor) {
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
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(TreeManageController treeController) {
        try {
            this.treeController = treeController;
            this.baseName = treeController.baseName;
            category = treeController.category;
            tableTree = treeController.tableTree;
            tableTreeLeaf = treeController.tableTreeLeaf;
            tableTag = treeController.tableTag;
            tableTreeLeafTag = treeController.tableTreeLeafTag;
            setTableDefinition(tableTag);

            queryConditions = "category='" + treeController.category + "'";
            currentPage = 0;
            pageSize = Integer.MAX_VALUE;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void checkButtons() {
        super.checkButtons();
        boolean isEmpty = tableData == null || tableData.isEmpty();
        boolean none = isEmpty || tableView.getSelectionModel().getSelectedItem() == null;
        if (queryTagsButton != null) {
            queryTagsButton.setDisable(none);
        }
        if (deleteTagsButton != null) {
            deleteTagsButton.setDisable(none);
        }
    }

    @FXML
    public void addTag() {
        addTag(false);
    }

    public void addTag(boolean forCurrentLeaf) {
        synchronized (this) {
            String name = PopTools.askValue(getBaseTitle(),
                    message("Add"), message("Tag"), message("Tag") + new Date().getTime());
            if (name == null || name.isBlank()) {
                return;
            }
            for (Tag tag : tableData) {
                if (name.equals(tag.getTag())) {
                    popError(message("AlreadyExisted"));
                    return;
                }
            }
            thisPane.setDisable(true);
            SingletonTask tagTask = new SingletonTask<Void>(this) {
                private Tag tag = null;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        tag = tableTag.insertData(conn, new Tag(category, name));
                        if (forCurrentLeaf && tag != null && treeController.currentLeaf != null) {
                            tableTreeLeafTag.insertData(conn,
                                    new TreeLeafTag(treeController.currentLeaf.getLeafid(), tag.getTgid()));
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return tag != null;
                }

                @Override
                protected void whenSucceeded() {
                    tableData.add(0, tag);
                    treeController.leafController.tableData.add(0, tag);
                    if (forCurrentLeaf) {
                        treeController.leafController.tableView.getSelectionModel().select(tag);
                    }
                    treeController.leafController.leafChanged(true);
                    popSuccessful();
                }

                @Override
                protected void finalAction() {
                    thisPane.setDisable(false);
                }

            };
            start(tagTask, false);
        }
    }

    public void saveTag(Tag tag) {
        if (isSettingValues) {
            return;
        }
        SingletonTask saveTask = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                return tableTag.writeData(tag) != null;
            }

            @Override
            protected void whenSucceeded() {
                synchronizedTables();
            }

        };
        start(saveTask, false);
    }

    @FXML
    public void queryTags() {
        List<Tag> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            popError(message("SelectToHandle"));
            return;
        }
        treeController.clearQuery();
        treeController.queryConditions = TableTreeLeaf.tagsCondition(selected);
        treeController.queryConditionsString = message("Tag") + ": ";
        for (Tag tag : selected) {
            treeController.queryConditionsString += " " + tag.getTag();
        }
        treeController.loadTableData();
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
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void saveAction() {
        if (isSettingValues) {
            return;
        }
        SingletonTask saveTask = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                return tableTag.updateList(tableData) >= 0;
            }

            @Override
            protected void whenSucceeded() {
                synchronizedTables();
            }

        };
        start(saveTask, false);
    }

    public void synchronizedTables() {
        if (this.equals(treeController.leafController)) {
            treeController.tagsController.isSettingValues = true;
            treeController.tagsController.tableData.setAll(tableData);
            treeController.tagsController.isSettingValues = false;
        } else {
            treeController.leafController.isSettingValues = true;
            treeController.leafController.tableData.setAll(tableData);
            treeController.leafController.isSettingValues = false;
            treeController.leafController.markTags();
        }
    }

}
