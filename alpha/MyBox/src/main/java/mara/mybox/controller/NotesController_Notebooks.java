package mara.mybox.controller;

import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Notebook;
import mara.mybox.db.table.TableNote;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-8-11
 * @License Apache License Version 2.0
 */
public abstract class NotesController_Notebooks extends NotesController_Base {

    protected void initNotebooks() {
        try {
            notebooksController.setParameters((NotesController) this);
            searchController.init(this, baseName + "Saved", Languages.message("Note"), 20);

            notebooksController.selectedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldTab, Boolean newTab) {
                    loadBook(notebooksController.selectedNode);
                }
            });
            initTimes();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void loadBooks() {
        notebooksController.loadTree();
        refreshTimes();
    }

    protected void clearQuery() {
        notebooksController.selectedNode = null;
        notebooksController.changedNode = null;
        queryConditions = null;
        queryConditionsString = null;
        tableData.clear();
        notesConditionBox.getChildren().clear();
        namesPane.getChildren().clear();
        currentPageStart = 1;
    }

    protected abstract void loadBook(Notebook book);


    /*
        Times
     */
    public void initTimes() {
        try {
            timeController.setParent(this, false);

            timeController.queryNodesButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    queryTimes();
                }
            });
            timeController.refreshNodesButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    refreshTimes();
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void refreshTimes() {
        synchronized (this) {
            timeController.clearTree();
            timesBox.setDisable(true);
            SingletonTask timesTask = new SingletonTask<Void>() {
                private List<Date> times;

                @Override
                protected boolean handle() {
                    times = TableNote.times();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    timeController.loadTree("update_time", times, false);
                }

                @Override
                protected void finalAction() {
                    timesBox.setDisable(false);
                }

            };
            start(timesTask, false);
        }
    }

    @FXML
    protected void queryTimes() {
        String c = timeController.check();
        if (c == null) {
            popError(Languages.message("MissTime"));
            return;
        }
        clearQuery();
        queryConditions = c;
        queryConditionsString = timeController.getFinalTitle();
        loadTableData();
    }

    /*
        Search
     */
    @FXML
    protected void search() {
        String s = searchController.value();
        if (s == null || s.isBlank()) {
            popError(Languages.message("InvalidData"));
            return;
        }
        String[] values = StringTools.splitBySpace(s);
        if (values == null || values.length == 0) {
            popError(Languages.message("InvalidData"));
            return;
        }
        searchController.refreshList();
        clearQuery();
        if (titleRadio.isSelected()) {
            queryConditions = null;
            queryConditionsString = Languages.message("Title") + ":";
            for (String v : values) {
                if (queryConditions != null) {
                    queryConditions += " OR ";
                } else {
                    queryConditions = " ";
                }
                queryConditions += " ( title like '%" + DerbyBase.stringValue(v) + "%' ) ";
                queryConditionsString += " " + v;
            }

        } else {
            queryConditions = null;
            queryConditionsString = Languages.message("Contents") + ":";
            for (String v : values) {
                if (queryConditions != null) {
                    queryConditions += " OR ";
                } else {
                    queryConditions = " ";
                }
                queryConditions += " ( html like '%" + DerbyBase.stringValue(v) + "%' ) ";
                queryConditionsString += " " + v;
            }
        }
        loadTableData();
    }

}
