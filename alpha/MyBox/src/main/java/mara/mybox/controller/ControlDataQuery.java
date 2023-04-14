package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.QueryCondition;
import mara.mybox.db.data.QueryCondition.DataOperation;
import mara.mybox.db.table.TableQueryCondition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-05-14
 * @License Apache License Version 2.0
 */
public class ControlDataQuery extends BaseController {

    protected BaseDataManageController dataController;
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

    public ControlDataQuery() {
        baseTitle = Languages.message("DataQuery");
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
                                titleInput.setStyle(UserConfig.badStyle());
                            } else {
                                titleInput.setStyle(null);
                            }
                        });
            }

            if (prefixInput != null) {
                prefixInput.textProperty().addListener(
                        (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                            if (newValue == null || newValue.isBlank()) {
                                prefixInput.setStyle(UserConfig.badStyle());
                            } else {
                                prefixInput.setStyle(null);
                            }
                        });
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setControls(BaseDataManageController dataController,
            QueryCondition initCondition, String tableDefinition,
            boolean prefixEditable, boolean supportTop) {
        if (dataController == null || initCondition == null) {
            return;
        }
        try {
            this.dataController = dataController;
            this.initCondition = initCondition;
            this.baseName = dataController.baseName;
            this.baseTitle = dataController.baseTitle + " " + baseTitle;
            dataOperation = initCondition.getDataOperation();
            qcid = initCondition.getQcid();

            loadList();

            tableDefinitonView.getEngine().loadContent(tableDefinition);
            titleInput.setText(initCondition.getTitle() == null ? ""
                    : StringTools.replaceLineBreak(initCondition.getTitle()));
            prefixInput.setText(initCondition.getPrefix());
            prefixInput.setEditable(prefixEditable);
            whereInput.setText(initCondition.getWhere());
            orderInput.setText(initCondition.getOrder());
            fetchInput.setText(initCondition.getFetch());
            if (!supportTop) {
                conditionBox.getChildren().remove(topBox);
            } else {
                NodeStyleTools.setTooltip(topInput, Languages.message("TopNumberComments"));
                topInput.setText(initCondition.getTop() + "");
            }
            titleInput.requestFocus();
            getMyStage().setTitle(dataController.baseTitle + " " + baseTitle);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
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
                Text node = new Text(StringTools.replaceLineBreak(condition.getTitle()));
                node.setUserData(condition);
                listView.getItems().add(node);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            titleInput.setStyle(UserConfig.badStyle());
            return null;
        }
        titleInput.setStyle(null);

        String prefix = prefixInput.getText() == null ? null : prefixInput.getText().trim();
        if (prefix == null || prefix.isEmpty()) {
            prefixInput.setStyle(UserConfig.badStyle());
            return null;
        }
        prefixInput.setStyle(null);

        int top = -1;
        if (conditionBox.getChildren().contains(topBox) && !topInput.getText().trim().isBlank()) {
            try {
                top = Integer.parseInt(topInput.getText().trim());
            } catch (Exception e) {
                topInput.setStyle(UserConfig.badStyle());
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
        titleInput.setText(titleInput.getText() + " - " + Languages.message("Copy"));
        popInformation(Languages.message("DataCopyComments"));
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
            List<Node> buttons = new ArrayList<>();
            List<ColumnDefinition> columns = dataController.viewDefinition.getColumns();
            for (ColumnDefinition column : columns) {
                String name = column.getColumnName();
                Button button = new Button(name);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        textArea.insertText(textArea.getAnchor(), name);
                    }
                });
                buttons.add(button);
            }

            MenuController controller = MenuController.open(this, textArea, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            controller.addFlowPane(buttons);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void popWhereOperator(MouseEvent mouseEvent) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem("( )");
            menu.setOnAction((ActionEvent event) -> {
                String s = whereInput.getText();
                whereInput.insertText(whereInput.getAnchor(), "( )");
            });
            items.add(menu);

            menu = new MenuItem("AND");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), " AND ");
            });
            items.add(menu);

            menu = new MenuItem("OR");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), " OR ");
            });
            items.add(menu);

            menu = new MenuItem("=");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), "=");
            });
            items.add(menu);

            menu = new MenuItem("<");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), "<");
            });
            items.add(menu);

            menu = new MenuItem("<=");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), "<=");
            });
            items.add(menu);

            menu = new MenuItem(">");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), ">");
            });
            items.add(menu);

            menu = new MenuItem(">=");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), ">=");
            });
            items.add(menu);

            menu = new MenuItem("<>");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), "<>");
            });
            items.add(menu);

            menu = new MenuItem("IS NULL");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), " IS NULL ");
            });
            items.add(menu);

            menu = new MenuItem("IS NOT NULL");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), " IS NOT NULL ");
            });
            items.add(menu);

            menu = new MenuItem("NOT");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), " NOT ");
            });
            items.add(menu);

            menu = new MenuItem("IN");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), " IN ( , ) ");
            });
            items.add(menu);

            menu = new MenuItem("LIKE");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), " LIKE '%' ");
            });
            items.add(menu);

            menu = new MenuItem("BETWEEN");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), " BETWEEN   AND   ");
            });
            items.add(menu);

            popEventMenu(mouseEvent, items);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void popWhereValue(MouseEvent mouseEvent) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem("'2020-07-27 14:33:56'");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), "'2020-07-27 14:33:56' ");
            });
            items.add(menu);

            menu = new MenuItem("''");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), "'' ");
            });
            items.add(menu);

            menu = new MenuItem("TRUE");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), "TRUE ");
            });
            items.add(menu);

            menu = new MenuItem("FALSE");
            menu.setOnAction((ActionEvent event) -> {
                whereInput.insertText(whereInput.getAnchor(), "FALSE ");
            });
            items.add(menu);

            popEventMenu(mouseEvent, items);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
