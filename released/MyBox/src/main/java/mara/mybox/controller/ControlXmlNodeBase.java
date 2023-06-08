package mara.mybox.controller;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * @Author Mara
 * @CreateDate 2023-5-16
 * @License Apache License Version 2.0
 */
public class ControlXmlNodeBase extends BaseController {

    protected ControlXmlTree treeController;
    protected ObservableList<Node> attributesData;

    @FXML
    protected VBox setBox, docBox, valueBox, attrBox;
    @FXML
    protected TextField typeInput, baseUriInput, nameInput, namespaceInput, prefixInput,
            uriInput, versionInput, encodingInput;
    @FXML
    protected TextArea valueArea;
    @FXML
    protected TableView<Node> attributesTable;
    @FXML
    protected TableColumn<Node, String> attrColumn, valueColumn;
    @FXML
    protected CheckBox standaloneCheck;

    @Override
    public void initControls() {
        try {
            super.initControls();

            attributesData = FXCollections.observableArrayList();
            attributesTable.setItems(attributesData);
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
                    attr.setValue(attributesData.get(row).getNodeValue());
                    attributesData.set(row, attr);
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

            thisPane.getChildren().remove(tabPane);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
        attributesData.clear();
        setBox.getChildren().clear();
    }

    @FXML
    public void addAttribute() {
        if (treeController == null) {
            return;
        }
        Attr attr = treeController.doc.createAttribute("attr");
        attr.setValue("value");
        attributesData.add(attr);
    }

    @FXML
    public void deleteAttributes() {
        try {
            List<Node> selected = attributesTable.getSelectionModel().getSelectedItems();
            if (selected == null || selected.isEmpty()) {
                return;
            }
            attributesData.removeAll(selected);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void clearAttributes() {
        attributesData.clear();
    }

}
