package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2019-12-18
 * @License Apache License Version 2.0
 */
public class TableController<P> extends BaseController {

    protected ObservableList<P> tableData;

    @FXML
    protected TableView<P> tableView;

    public TableController() {

    }

    @Override
    public void initializeNext() {
        try {
            tableData = FXCollections.observableArrayList();

            initColumns();

            tableData.addListener(new ListChangeListener<P>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends P> change) {
                    if (!isSettingValues) {
                        tableChanged();
                    }
                }
            });

            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    checkSelected();
                }
            });
            checkSelected();

            tableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() == 1) {
                        itemClicked();
                    } else if (event.getClickCount() > 1) {
                        itemDoubleClicked();
                    }
                }
            });

            tableView.setItems(tableData);

            load();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initColumns() {
        try {

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void tableChanged() {

    }

    protected void checkSelected() {
        if (isSettingValues) {
            return;
        }
        P selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            deleteButton.setDisable(true);
        } else {
            deleteButton.setDisable(false);
        }
    }

    public void itemClicked() {

    }

    public void itemDoubleClicked() {

    }

    public void load() {
        try {
            tableView.refresh();
            checkSelected();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void deleteAction() {
        if (deleteSelectedData()) {
            load();
        }
    }

    protected boolean deleteSelectedData() {
        List<P> selected = tableView.getSelectionModel().getSelectedItems();
        return true;
    }

    @FXML
    @Override
    public void clearAction() {
        if (clearData()) {
            tableData.clear();
            tableView.refresh();
            checkSelected();
        }
    }

    protected boolean clearData() {
        return true;
    }

    @FXML
    public void addAction() {
        try {

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    public void refreshAction() {
        load();
    }

}
