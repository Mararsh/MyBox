package mara.mybox.controller;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import mara.mybox.value.UserConfig;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * @Author Mara
 * @CreateDate 2023-5-16
 * @License Apache License Version 2.0
 */
public class ControlXmlNodeBase extends BaseTableViewController<Node> {

    protected ControlXmlTree treeController;

    @FXML
    protected Tab attrTab, valueTab, docTab, baseTab;
    @FXML
    protected TextField typeInput, baseUriInput, nameInput, namespaceInput, prefixInput,
            uriInput, versionInput, encodingInput;
    @FXML
    protected TextArea valueArea;
    @FXML
    protected TableColumn<Node, String> attrColumn, valueColumn;
    @FXML
    protected CheckBox standaloneCheck;

    @Override
    public void initControls() {
        try {
            super.initControls();

            attrColumn.setCellValueFactory(new PropertyValueFactory<>("nodeName"));
            attrColumn.setCellFactory(TableAutoCommitCell.forStringColumn());
            attrColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Node, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<Node, String> e) {
                    if (e == null || treeController == null) {
                        return;
                    }
                    int row = e.getTablePosition().getRow();
                    if (row < 0) {
                        return;
                    }
                    Attr attr = treeController.doc.createAttribute(e.getNewValue());
                    attr.setValue(tableData.get(row).getNodeValue());
                    tableData.set(row, attr);
                }
            });
            attrColumn.getStyleClass().add("editable-column");

            valueColumn.setCellValueFactory(new PropertyValueFactory<>("nodeValue"));
            valueColumn.setCellFactory(TableAutoCommitCell.forStringColumn());
            valueColumn.setOnEditCommit((TableColumn.CellEditEvent<Node, String> t) -> {
                if (t == null) {
                    return;
                }
                Node row = t.getRowValue();
                if (row == null) {
                    return;
                }
                row.setNodeValue(t.getNewValue());
            });
            valueColumn.getStyleClass().add("editable-column");

            clearNode();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearNode() {
        typeInput.clear();
        nameInput.clear();
        valueArea.clear();
        baseUriInput.clear();
        namespaceInput.clear();
        prefixInput.clear();
        uriInput.clear();
        versionInput.clear();
        encodingInput.clear();
        tableData.clear();
        tabPane.getTabs().removeAll(attrTab, valueTab, docTab);
    }

    @FXML
    @Override
    public void addAction() {
        if (treeController == null) {
            return;
        }
        Attr attr = treeController.doc.createAttribute("attr");
        attr.setValue("value");
        tableData.add(attr);
    }

    /*
        value
     */
    @FXML
    protected void popValueHistories(Event event) {
        if (UserConfig.getBoolean("XmlNodeValueHistoriesPopWhenMouseHovering", false)) {
            showValueHistories(event);
        }
    }

    @FXML
    protected void showValueHistories(Event event) {
        PopTools.popSavedValues(this, valueArea, event, "XmlNodeValueHistories");
    }

    @FXML
    protected void clearValue() {
        valueArea.clear();
    }

}
