package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import mara.mybox.data.QueryCondition;
import mara.mybox.data.QueryCondition.DataOperation;
import mara.mybox.db.TableQueryCondition;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-05-14
 * @License Apache License Version 2.0
 */
public class DataQueryController extends BaseController {

    protected DataAnalysisController dataController;
    protected LoadingController loading;
    protected QueryCondition initCondition, savedCondition;
    protected DataOperation dataOperation;
    protected long qcid = -1;

    @FXML
    protected ListView<Text> listView;
    @FXML
    protected TextField titleInput, prefixInput, whereInput, orderInput, fetchInput, topInput;
    @FXML
    protected TextArea tableArea;
    @FXML
    protected VBox inputBox;
    @FXML
    protected HBox topBox;

    public DataQueryController() {
        baseTitle = message("DataQuery");
    }

    @Override
    public void initializeNext() {
        try {
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

    public String buttonName() {
        try {
            switch (dataOperation) {
                case QueryData:
                    return message("Query");
                case ClearData:
                    return message("Clear");
                case DisplayChart:
                    return message("DisplayChart");
                case ExportData:
                    return message("Export");
                default:
                    return message("OK");
            }
        } catch (Exception e) {
            return null;
        }
    }

    public void setValue(DataAnalysisController dataController,
            QueryCondition initCondition, String tableDefinition,
            boolean prefixEditable, boolean supportTop) {
        if (dataController == null || initCondition == null) {
            return;
        }
        this.dataController = dataController;
        this.initCondition = initCondition;
        this.baseName = dataController.baseName;
        dataOperation = initCondition.getDataOperation();
        qcid = initCondition.getQcid();

        setControls();
        loadList();

        tableArea.setText(tableDefinition);
        titleInput.setText(initCondition.getTitle() == null ? "" : initCondition.getTitle().replaceAll("\n", " "));
        prefixInput.setText(initCondition.getPrefix());
        prefixInput.setEditable(!prefixEditable);
        whereInput.setText(initCondition.getWhere());
        orderInput.setText(initCondition.getOrder());
        fetchInput.setText(initCondition.getFetch());
        if (!supportTop) {
            inputBox.getChildren().remove(topBox);
        } else {
            FxmlControl.setTooltip(topInput, message("TopNumberComments"));
            topInput.setText(initCondition.getTop() + "");
        }
        okButton.setText(buttonName());
        titleInput.requestFocus();
        getMyStage().setTitle(baseTitle + " - " + dataController.baseTitle + " - " + okButton.getText());
    }

    protected void setControls() {

    }

    protected void loadList() {
        try {
            if (dataOperation == null) {
                return;
            }
            listView.getItems().clear();
            List<QueryCondition> list = TableQueryCondition.readList(dataController.dataName, dataOperation);
            if (list == null || list.isEmpty()) {
                return;
            }
            for (QueryCondition condition : list) {
                Text node = new Text(condition.getTitle().replaceAll("\n", " "));
                final long id = condition.getQcid();
                node.setUserData(condition);
                listView.getItems().add(node);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void deleteAction() {
        List<Text> selected = listView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        for (Text item : selected) {
            if (item.getUserData() == null) {
                continue;
            }
            QueryCondition condition = (QueryCondition) item.getUserData();
            TableQueryCondition.delete(condition.getQcid());
        }
        loadList();
    }

    @FXML
    @Override
    public void saveAction() {
        if (save() != null) {
            loadList();
        }
    }

    public QueryCondition save() {
        if (dataOperation == null) {
            return null;
        }
        String title = titleInput.getText() == null ? null : titleInput.getText().trim();
        if (title == null || title.isEmpty()) {
            titleInput.setStyle(badStyle);
            return null;
        }
        titleInput.setStyle(null);

        String prefix = prefixInput.getText() == null ? null : prefixInput.getText().trim();
        if (prefix == null || prefix.isEmpty()) {
            prefixInput.setStyle(badStyle);
            return null;
        }
        prefixInput.setStyle(null);

        int top = -1;
        if (inputBox.getChildren().contains(topBox) && !topInput.getText().trim().isBlank()) {
            try {
                top = Integer.parseInt(topInput.getText().trim());
            } catch (Exception e) {
                topInput.setStyle(badStyle);
                return null;
            }
        }
        topInput.setStyle(null);

        String where = whereInput.getText() == null ? null : whereInput.getText().trim();
        String order = orderInput.getText() == null ? null : orderInput.getText().trim();
        String fetch = fetchInput.getText() == null ? null : fetchInput.getText().trim();

        savedCondition = QueryCondition.create()
                .setQcid(qcid)
                .setDataName(dataController.dataName)
                .setOperation(QueryCondition.operation(dataOperation))
                .setTitle(title)
                .setPrefix(prefix)
                .setWhere(where)
                .setOrder(order)
                .setFetch(fetch)
                .setTop(top);
        if (TableQueryCondition.write(savedCondition, false)) {
            return savedCondition;
        } else {
            return null;
        }

    }

    @FXML
    @Override
    public void createAction() {
        qcid = -1;
        titleInput.setText("");
        if (!prefixInput.isDisable()) {
            prefixInput.setText("");
        }
        whereInput.setText("");
        orderInput.setText("");
        fetchInput.setText("");
        topInput.setText("-1");
    }

    @FXML
    @Override
    public void copyAction() {
        qcid = -1;
    }

    @FXML
    @Override
    public void okAction() {
        savedCondition = save();
        if (savedCondition == null) {
            return;
        }
        switch (dataOperation) {
            case QueryData:
                dataController.loadAsConditions(savedCondition);
                break;
            case ClearData:
                dataController.clearAsConditions(savedCondition);
                break;
            default:
                return;
        }
        closeStage();
    }

}
