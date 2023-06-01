package mara.mybox.controller;

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
import mara.mybox.data.XmlTreeNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @Author Mara
 * @CreateDate 2023-5-16
 * @License Apache License Version 2.0
 */
public class ControlXmlNodeBase extends BaseController {

    protected ControlXmlTree treeController;

    protected ObservableList<Node> attributesData;
    protected Node node;

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
                    if (e == null) {
                        return;
                    }
                    int row = e.getTablePosition().getRow();
                    if (row < 0) {
                        return;
                    }
                    Attr attr = node.getOwnerDocument().createAttribute(e.getNewValue());
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

    public void load(Node node) {
        clearNode();
        this.node = node;
        if (node == null) {
            return;
        }
        typeInput.setText(XmlTreeNode.type(node).name());
        baseUriInput.setText(node.getBaseURI());
        namespaceInput.setText(node.getNamespaceURI());

        nameInput.setText(node.getNodeName());
        nameInput.setDisable(true);

        prefixInput.setText(node.getPrefix());

        switch (node.getNodeType()) {
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
            case Node.COMMENT_NODE:
            case Node.ATTRIBUTE_NODE:
            case Node.PROCESSING_INSTRUCTION_NODE:
                valueArea.setText(node.getNodeValue());
                valueArea.setDisable(false);
                valueArea.setEditable(true);
                setBox.getChildren().add(valueBox);
                break;
            case Node.ELEMENT_NODE:
                NamedNodeMap attrs = node.getAttributes();
                if (attrs != null) {
                    for (int i = 0; i < attrs.getLength(); i++) {
                        attributesData.add(attrs.item(i));
                    }
                }
                setBox.getChildren().add(attrBox);
                break;
            case Node.DOCUMENT_NODE:
                Document document = (Document) node;
                uriInput.setText(document.getDocumentURI());
                versionInput.setText(document.getXmlVersion());
                encodingInput.setText(document.getXmlEncoding());
                standaloneCheck.setSelected(document.getXmlStandalone());
                setBox.getChildren().add(docBox);
                break;
            case Node.DOCUMENT_TYPE_NODE:
                DocumentType documentType = (DocumentType) node;
                valueArea.setText(documentType.getTextContent());
                valueArea.setDisable(true);
                valueArea.setEditable(false);
                setBox.getChildren().add(valueBox);
                break;
            default:
        }
        MyBoxLog.console(node.getTextContent());

        thisPane.setDisable(false);
    }

    public Node pickValue() {
        try {
            if (node == null) {
                return null;
            }

            NamedNodeMap attrs = node.getAttributes();
            if (attrs != null) {
                Element element = (Element) node;
                for (int i = attrs.getLength() - 1; i >= 0; i--) {
                    element.removeAttribute(attrs.item(i).getNodeName());
                }
                for (Node attr : attributesData) {
                    element.setAttribute(attr.getNodeName(), attr.getNodeValue());
                }
            }

            node.setNodeValue(valueArea.getText());
            return node;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public void clearNode() {
        node = null;
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

}
