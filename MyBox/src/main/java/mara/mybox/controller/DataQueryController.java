package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import mara.mybox.data.QueryCondition;
import mara.mybox.data.QueryCondition.DataOperation;
import mara.mybox.db.ColumnDefinition;
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
    protected TextField titleInput, prefixInput, topInput;
    @FXML
    protected TextArea whereInput, orderInput, fetchInput;
    @FXML
    protected WebView tableDefinitonView;
    @FXML
    protected VBox conditionBox;
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
                            if (conditionBox.getChildren().contains(topBox)) {
                                topInput.setText(condition.getTop() + "");
                            }
                            qcid = condition.getQcid();
                        });
            }

            if (titleInput != null) {
                titleInput.textProperty().addListener(
                        (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                            if (newValue == null || newValue.isBlank()) {
                                titleInput.setStyle(badStyle);
                            } else {
                                titleInput.setStyle(null);
                            }
                        });
            }

            if (prefixInput != null) {
                prefixInput.textProperty().addListener(
                        (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                            if (newValue == null || newValue.isBlank()) {
                                prefixInput.setStyle(badStyle);
                            } else {
                                prefixInput.setStyle(null);
                            }
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
        try {
            this.dataController = dataController;
            this.initCondition = initCondition;
            this.baseName = dataController.baseName;
            dataOperation = initCondition.getDataOperation();
            qcid = initCondition.getQcid();

            setControls();
            loadList();

            tableDefinitonView.getEngine().loadContent(tableDefinition);
            titleInput.setText(initCondition.getTitle() == null ? "" : initCondition.getTitle().replaceAll("\n", " "));
            prefixInput.setText(initCondition.getPrefix());
            prefixInput.setEditable(prefixEditable);
            whereInput.setText(initCondition.getWhere());
            orderInput.setText(initCondition.getOrder());
            fetchInput.setText(initCondition.getFetch());
            if (!supportTop) {
                conditionBox.getChildren().remove(topBox);
            } else {
                FxmlControl.setTooltip(topInput, message("TopNumberComments"));
                topInput.setText(initCondition.getTop() + "");
            }
            okButton.setText(buttonName());
            titleInput.requestFocus();
            getMyStage().setTitle(baseTitle + " - " + dataController.baseTitle + " - " + okButton.getText());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void setControls() {

    }

    protected void loadList() {
        try {
            if (dataOperation == null) {
                return;
            }
            listView.getItems().clear();
            List<QueryCondition> list = TableQueryCondition.readList(dataController.tableName, dataOperation);
            if (list == null || list.isEmpty()) {
                return;
            }
            for (QueryCondition condition : list) {
                Text node = new Text(condition.getTitle().replaceAll("\n", " "));
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
        if (conditionBox.getChildren().contains(topBox) && !topInput.getText().trim().isBlank()) {
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
                .setDataName(dataController.tableName)
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
        titleInput.clear();
        if (prefixInput.isEditable()) {
            prefixInput.clear();
        }
        whereInput.clear();
        orderInput.clear();
        fetchInput.clear();
        topInput.clear();
    }

    @FXML
    @Override
    public void copyAction() {
        qcid = -1;
        titleInput.setText(titleInput.getText() + " - " + message("Copy"));
        popInformation(message("DataCopyComments"));
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

    @FXML
    public void popWhereColumn(MouseEvent mouseEvent) {
        popColumn(mouseEvent, whereInput);
    }

    @FXML
    public void popOrderColumn(MouseEvent mouseEvent) {
        popColumn(mouseEvent, orderInput);
    }

    public void popColumn(MouseEvent mouseEvent, TextArea textArea) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            List<ColumnDefinition> columns = dataController.viewDefinition.getColumns();
            for (ColumnDefinition column : columns) {
                menu = new MenuItem(column.getLabel());
                menu.setOnAction((ActionEvent event) -> {
                    textArea.insertText(textArea.getAnchor(), column.getName());
                });
                popMenu.getItems().add(menu);
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("MenuClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void popWhereOperator(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem("( )");
            menu.setOnAction((ActionEvent event) -> {
                String s = whereInput.getText();
                whereInput.insertText(whereInput.getAnchor(), "( )");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem("AND");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), " AND ");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem("OR");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), " OR ");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem("=");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), "=");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem("<");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), "<");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem("<=");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), "<=");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(">");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), ">");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(">=");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), ">=");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem("<>");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), "<>");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem("IS NULL");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), " IS NULL ");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem("IS NOT NULL");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), " IS NOT NULL ");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem("NOT");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), " NOT ");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem("IN");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), " IN ( , ) ");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem("LIKE");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), " LIKE '%' ");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem("BETWEEN");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), " BETWEEN   AND   ");
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("MenuClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void popWhereValue(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem("'2020-07-27 14:33:56'");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), "'2020-07-27 14:33:56' ");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem("''");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), "'' ");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem("TRUE");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), "TRUE ");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem("FALSE");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), "FALSE ");
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("MenuClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void ascAction() {
        orderInput.insertText(orderInput.getAnchor(), " ASC ");
    }

    @FXML
    public void descAction() {
        orderInput.insertText(orderInput.getAnchor(), " DESC ");
    }

    @FXML
    public void commaAction() {
        orderInput.insertText(orderInput.getAnchor(), ", ");
    }

    @FXML
    public void clearWhere() {
        clear(whereInput);
    }

    @FXML
    public void clearOrder() {
        clear(orderInput);
    }

    @FXML
    public void clearFetch() {
        clear(fetchInput);
    }

    public void clear(TextArea textArea) {
        int anchor = textArea.getAnchor();
        int pos = textArea.getCaretPosition();
        if (anchor != pos) {
            textArea.deleteText(Math.min(anchor, pos), Math.max(anchor, pos));
        } else {
            textArea.clear();
        }
    }

    @FXML
    public void exampleFetch() {
        fetchInput.setText("OFFSET 50 ROWS FETCH NEXT 100 ROWS ONLY");
    }

}
