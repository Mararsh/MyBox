package mara.mybox.controller;

import java.util.Date;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.db.table.TableDataNodeTag;
import mara.mybox.db.table.TableDataTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.fxml.cell.TableIDCell;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-5-14
 * @License Apache License Version 2.0
 */
public class ControlDataTreePages extends BaseTablePagesController<DataNode> {

    protected BaseNodeTable nodeTable;
    protected TableDataTag tagTable;
    protected TableDataNodeTag nodeTagsTable;
    protected String dataName;
    protected ControlWebView viewController;

    @FXML
    protected TableColumn<DataNode, String> hierarchyColumn, titleColumn;
    @FXML
    protected TableColumn<DataNode, Long> idColumn, childrenColumn;
    @FXML
    protected TableColumn<DataNode, Float> orderColumn;
    @FXML
    protected TableColumn<DataNode, Date> timeColumn;

    @Override
    public void initColumns() {
        try {
            super.initColumns();

            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            titleColumn.setCellFactory(new Callback<TableColumn<DataNode, String>, TableCell<DataNode, String>>() {

                @Override
                public TableCell<DataNode, String> call(TableColumn<DataNode, String> param) {
                    try {
                        final Hyperlink link = new Hyperlink();
                        NodeStyleTools.setTooltip(link, new Tooltip(message("View")));

                        TableCell<DataNode, String> cell = new TableCell<DataNode, String>() {

                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                setText(null);
                                if (empty || item == null) {
                                    setGraphic(null);
                                    return;
                                }
                                link.setText(StringTools.abbreviate(item, AppVariables.titleTrimSize));
                                link.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        showNode(getTableRow().getItem());
                                    }
                                });
                                setGraphic(link);
                                if (isSourceNode(getTableRow().getItem())) {
                                    setStyle(NodeStyleTools.darkRedTextStyle());
                                } else {
                                    setStyle(null);
                                }
                            }

                        };

                        return cell;
                    } catch (Exception e) {
                        return null;
                    }
                }
            });

            hierarchyColumn.setCellValueFactory(new PropertyValueFactory<>("hierarchyNumber"));
            hierarchyColumn.setCellFactory(new Callback<TableColumn<DataNode, String>, TableCell<DataNode, String>>() {

                @Override
                public TableCell<DataNode, String> call(TableColumn<DataNode, String> param) {
                    try {
                        final Hyperlink link = new Hyperlink();
                        NodeStyleTools.setTooltip(link, new Tooltip(message("Unfold")));

                        TableCell<DataNode, String> cell = new TableCell<DataNode, String>() {

                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                setText(null);
                                setGraphic(null);
                                if (empty || item == null) {
                                    return;
                                }
                                DataNode node = getTableRow().getItem();
                                if (node == null) {
                                    return;
                                }
                                if (node.getChildrenSize() > 0) {
                                    link.setText(item);
                                    link.setOnAction(new EventHandler<ActionEvent>() {
                                        @Override
                                        public void handle(ActionEvent event) {
                                            loadNode(node);
                                        }
                                    });
                                    setGraphic(link);
                                } else {
                                    setText(item);
                                }

                            }

                        };

                        return cell;
                    } catch (Exception e) {
                        return null;
                    }
                }
            });

            idColumn.setCellValueFactory(new PropertyValueFactory<>("nodeid"));
            idColumn.setCellFactory(new TableIDCell());

            childrenColumn.setCellValueFactory(new PropertyValueFactory<>("childrenSize"));
            childrenColumn.setCellFactory(new TableIDCell());

            orderColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));

            timeColumn.setCellValueFactory(new PropertyValueFactory<>("updateTime"));
            timeColumn.setCellFactory(new TableDateCell());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean isSourceNode(DataNode node) {
        return false;
    }

    public void showNode(DataNode node) {

    }

    public void loadNode(DataNode node) {

    }

}
