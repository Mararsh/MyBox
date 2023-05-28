package mara.mybox.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @Author Mara
 * @CreateDate 2023-5-16
 * @License Apache License Version 2.0
 */
public class ControlXmlNodeBase extends BaseController {

    protected ControlXmlTree treeController;

    protected ObservableList<Node> attributesData;
    protected Node node;
    protected Document doc;

    @FXML
    protected TextField tagInput;
    @FXML
    protected TextArea textArea;
    @FXML
    protected TableView<Node> attributesTable;
    @FXML
    protected TableColumn<Node, String> attrColumn, valueColumn;

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
                    if (e == null | doc == null) {
                        return;
                    }
                    int row = e.getTablePosition().getRow();
                    if (row < 0) {
                        return;
                    }
                    Attr attr = doc.createAttribute(e.getNewValue());
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

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void load(Document doc, Node node) {
        clearNode();
        this.doc = doc;
        this.node = node;
        if (doc == null || node == null || !(node instanceof Element)) {
            return;
        }
        Element element = (Element) node;

        tagInput.setText(element.getNodeName());
        tagInput.setDisable(true);

        NamedNodeMap attrs = element.getAttributes();
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                attributesData.add(attrs.item(i));
            }
        }
        NodeList children = element.getChildNodes();
        if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.TEXT_NODE) {
                    textArea.setText(child.getNodeValue());
                    break;
                }
            }
        }
        thisPane.setDisable(false);
    }

    public Node pickValue() {
        try {
            if (doc == null || node == null || !(node instanceof Element)) {
                return null;
            }
            Element element = (Element) node;
            NamedNodeMap attrs = node.getAttributes();
            if (attrs != null) {
                for (int i = attrs.getLength() - 1; i >= 0; i--) {
                    element.removeAttribute(attrs.item(i).getNodeName());
                }
                for (Node attr : attributesData) {
                    element.setAttribute(attr.getNodeName(), attr.getNodeValue());
                }
            }

            NodeList children = element.getChildNodes();
            if (children != null) {
                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);
                    if (child.getNodeType() == Node.TEXT_NODE) {
                        child.setNodeValue(textArea.getText());
                        break;
                    }
                }
            }
            return element;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public void clearNode() {
        doc = null;
        node = null;

        tagInput.clear();
        textArea.clear();
        attributesData.clear();
    }

}
