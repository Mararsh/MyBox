package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Tag;
import mara.mybox.db.data.TreeLeafTag;
import mara.mybox.db.table.TableNote;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-9-23
 * @License Apache License Version 2.0
 */
public class TreeTagsController extends BaseController {

    protected TreeManageController treeController;
    protected List<Tag> tags;

    @FXML
    protected ControlCheckBoxList tagsController;
    @FXML
    protected Button queryTagsButton, deleteTagsButton, renameTagButton, refreshTagsButton;

    public void setParameters(TreeManageController treeController) {
        try {
            this.treeController = treeController;
            this.baseName = treeController.baseName;
            tags = new ArrayList<>();

            tagsController.setParent(this);

            tagsController.checkedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldV, Boolean newV) {
                    checkTagsButtons();
                }
            });

            tagsController.rightClickedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldV, Boolean newV) {
                    popTagMenu(tagsController.mouseEvent);
                }
            });

            refreshTags();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkTagsButtons() {
        boolean none = !tagsController.hasChecked();
        queryTagsButton.setDisable(none);
        deleteTagsButton.setDisable(none);
        renameTagButton.setDisable(tagsController.selectedIndex() < 0);
    }

    @FXML
    public void refreshTags() {
        tags.clear();
        synchronized (this) {
            tagsController.clear();
            thisPane.setDisable(true);
            SingletonTask tagsTask = new SingletonTask<Void>(this) {
                private List<String> tagsString;

                @Override
                protected boolean handle() {
                    tags = treeController.tableTag.readAll();
                    if (tags != null && !tags.isEmpty()) {
                        tagsString = new ArrayList<>();
                        for (Tag tag : tags) {
                            tagsString.add(tag.getTag());
                        }
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    tagsController.setValues(tagsString);
                }

                @Override
                protected void finalAction() {
                    thisPane.setDisable(false);
                }

            };
            start(tagsTask, false);
        }
    }

    @FXML
    public void addTag() {
        addTag(false);
    }

    public void addTag(boolean forCurrentNote) {
        synchronized (this) {
            String name = PopTools.askValue(getBaseTitle(),
                    message("Add"), message("Tag"), message("Tag") + new Date().getTime());
            if (name == null || name.isBlank()) {
                return;
            }
            List<String> names = tagsController.getValues();
            if (names != null && names.contains(name)) {
                popError(message("AlreadyExisted"));
                return;
            }
            thisPane.setDisable(true);
            treeController.leafController.tagsController.thisPane.setDisable(true);
            SingletonTask tagTask = new SingletonTask<Void>(this) {
                private Tag tag = null;
                private boolean updateCurrent;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        tag = treeController.tableTag.insertData(conn, new Tag(name));
                        updateCurrent = forCurrentNote && tag != null && treeController.currentLeaf != null;
                        if (updateCurrent) {
                            treeController.tableTreeLeafTag.insertData(conn,
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
                    tags.add(0, tag);
                    tagsController.add(0, name, true);
                    if (updateCurrent) {
                        treeController.leafController.tagsController.add(0, name, true);
                        treeController.leafController.tagsChanged(true);
                    } else {
                        treeController.leafController.refreshTags();
                    }
                    popSuccessful();
                }

                @Override
                protected void finalAction() {
                    thisPane.setDisable(false);
                    treeController.leafController.tagsController.thisPane.setDisable(false);
                }

            };
            start(tagTask, false);
        }
    }

    @FXML
    public void selectAllTags() {
        tagsController.checkAll();
    }

    @FXML
    public void selectNoneTags() {
        tagsController.checkNone();
    }

    @FXML
    protected void renameTag() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            int index = tagsController.selectedIndex();
            if (index < 0 || index > tags.size()) {
                popError(message("SelectToHandle"));
                return;
            }
            Tag tag = tags.get(index);
            String name = PopTools.askValue(getBaseTitle(), tag.getTag(), message("Rename"), tag.getTag() + "m");
            if (name == null || name.isBlank()) {
                return;
            }
            task = new SingletonTask<Void>(this) {
                private Tag updated;

                @Override
                protected boolean handle() {
                    updated = new Tag(name).setTgid(tag.getTgid());
                    updated = treeController.tableTag.updateData(updated);
                    return updated != null;
                }

                @Override
                protected void whenSucceeded() {
                    tagsController.setValue(index, name);
                    treeController.leafController.refreshTags();
                    popSuccessful();
                }
            };
            start(task);
        }
    }

    @FXML
    public void queryTags() {
        List<Tag> checked = checkedTags();
        if (checked == null || checked.isEmpty()) {
            popError(message("SelectToHandle"));
            return;
        }
        treeController.clearQuery();
        treeController.queryConditions = TableNote.tagsCondition(checked);
        treeController.queryConditionsString = message("Tag") + ": ";
        for (Tag tag : checked) {
            treeController.queryConditionsString += " " + tag.getTag();
        }
        treeController.loadTableData();
    }

    public List<Tag> checkedTags() {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        List<String> values = tagsController.checkedValues();
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<Tag> checked = new ArrayList<>();
        for (Tag tag : tags) {
            if (values.contains(tag.getTag())) {
                checked.add(tag);
            }
        }
        return checked;
    }

    @FXML
    public void clearTags() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            if (!PopTools.askSure(this, getBaseTitle(), message("Tags"), message("SureDeleteAll"))) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    return treeController.tableTag.clearData() >= 0;
                }

                @Override
                protected void whenSucceeded() {
                    tagsController.clear();
                    treeController.leafController.tagsController.clear();
                    popSuccessful();
                }

            };
            start(task);
        }
    }

    @FXML
    public void deleteTags() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            List<Tag> checked = checkedTags();
            if (checked == null || checked.isEmpty()) {
                popError(message("SelectToHandle"));
                return;
            }
            if (!PopTools.askSure(this, getBaseTitle(), message("Tags"), message("SureDelete"))) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    return treeController.tableTag.deleteData(checked) >= 0;
                }

                @Override
                protected void whenSucceeded() {
                    refreshTags();
                    treeController.leafController.refreshTags();
                    popSuccessful();
                }

            };
            start(task);
        }
    }

    protected void popTagMenu(MouseEvent event) {
        if (isSettingValues) {
            return;
        }
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;

        menu = new MenuItem(message("Add"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            addTag();
        });
        items.add(menu);

        boolean none = !tagsController.hasChecked();

        menu = new MenuItem(message("Delete"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            deleteTags();
        });
        menu.setDisable(none);
        items.add(menu);

        menu = new MenuItem(message("Rename"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            renameTag();
        });
        menu.setDisable(tagsController.selectedIndex() < 0);
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("Query"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            queryTags();
        });
        menu.setDisable(none);
        items.add(menu);

        menu = new MenuItem(message("Refresh"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            refreshTags();
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());
        menu = new MenuItem(message("PopupClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = null;
        });
        items.add(menu);

        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(items);
        if (event != null) {
            popMenu.show(treeController.tableView, event.getScreenX(), event.getScreenY());
        } else {
            LocateTools.locateCenter(treeController.tableView, popMenu);
        }
    }

}
