package mara.mybox.controller;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import mara.mybox.data.QueryCondition;
import mara.mybox.data.QueryCondition.DataOperation;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-07-16
 * @License Apache License Version 2.0
 */
public class TableManageConditionController extends BaseController {

    protected TableManageController tableManageController;
    protected LoadingController loading;
    protected QueryCondition initCondition, savedCondition;
    protected DataOperation dataOperation;
    protected long qcid = -1;

    @FXML
    protected ListView<Text> listView;
    @FXML
    protected TextField titleInput, prefixInput, whereInput, orderInput, fetchInput, topInput;
    @FXML
    protected WebView tableDefinitonView;
    @FXML
    protected VBox inputBox;
    @FXML
    protected HBox topBox;

    public TableManageConditionController() {
        baseTitle = message("DataQuery");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            if (listView != null) {
                listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                listView.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends Text> observable, Text oldValue, Text newValue) -> {
                            if (newValue == null || newValue.getUserData() == null) {
                                return;
                            }
                            QueryCondition condition = (QueryCondition) newValue.getUserData();
                            titleInput.setText(condition.getTitle());
                            prefixInput.setText(condition.getPrefix());
                            whereInput.setText(condition.getWhere());
                            orderInput.setText(condition.getOrder());
                            fetchInput.setText(condition.getFetch());
                            if (inputBox.getChildren().contains(topBox)) {
                                topInput.setText(condition.getTop() + "");
                            }
                            qcid = condition.getQcid();
                        });

            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
