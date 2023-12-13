package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import mara.mybox.data.XmlTreeNode;
import mara.mybox.data.XmlTreeNode.NodeType;
import static mara.mybox.data.XmlTreeNode.NodeType.Attribute;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.XmlTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @Author Mara
 * @CreateDate 2023-6-4
 * @License Apache License Version 2.0
 */
public class XmlAddNodeController extends ControlXmlNodeBase {

    protected TreeItem<XmlTreeNode> treeItem;

    @FXML
    protected Label parentLabel, indexLabel;
    @FXML
    protected TextField indexInput;
    @FXML
    protected ToggleGroup typeGroup;
    @FXML
    protected RadioButton elementRadio, textRadio, cdataRadio, commentRadio, xmlRadio;
    @FXML
    protected Tab nameTab;

    public XmlAddNodeController() {
        baseTitle = message("AddNode");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkType();
                }
            });

            checkType();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void checkType() {
        try {
            clearNode();

            if (elementRadio.isSelected()) {
                tabPane.getTabs().addAll(nameTab, attrTab);
                nameInput.requestFocus();

            } else {
                tabPane.getTabs().add(valueTab);
                valueArea.requestFocus();

                if (xmlRadio.isSelected()) {
                    valueTab.setText("XML");
                    valueArea.setText("<tag attr=\"value\">text</tag>");
                } else {
                    valueTab.setText(message("Value"));
                }

            }
            refreshStyle(tabPane);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(ControlXmlTree treeController, TreeItem<XmlTreeNode> treeItem) {
        try {
            this.treeController = treeController;
            this.treeItem = treeItem;
            NodeType type = treeItem.getValue().getType();
            switch (type) {
                case Document:
                    textRadio.setDisable(true);
                    cdataRadio.setDisable(true);
                    if (treeItem.getValue().isDocWithoutElement()) {
                        elementRadio.setDisable(false);
                        elementRadio.setSelected(true);
                    } else {
                        elementRadio.setDisable(true);
                        commentRadio.setSelected(true);
                    }
                    break;
                case Element:
                case Entity:
                case EntityRefrence:
                case DocumentFragment:
                    textRadio.setDisable(false);
                    cdataRadio.setDisable(false);
                    elementRadio.setDisable(false);
                    elementRadio.setSelected(true);
                    break;
                case Attribute:
                    textRadio.setDisable(false);
                    cdataRadio.setDisable(true);
                    elementRadio.setDisable(true);
                    textRadio.setSelected(true);
                    break;
                default:
                    close();
            }

            parentLabel.setText(message("AddInto") + ": "
                    + treeController.hierarchyNumber(treeItem));
            indexInput.setText((treeItem.getChildren().size() + 1) + "");

            setTitle(treeController.getMyStage().getTitle());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void clearNode() {
        nameInput.setText("");
        valueArea.clear();
        tableData.clear();
        tabPane.getTabs().removeAll(attrTab, valueTab, nameTab);
    }

    @FXML
    @Override
    public void okAction() {
        try {
            if (treeItem == null) {
                close();
                return;
            }
            XmlTreeNode treeNode = treeItem.getValue();
            if (treeNode == null) {
                close();
                return;
            }
            Node parentNode = treeNode.getNode();
            if (parentNode == null) {
                close();
                return;
            }
            int index;
            try {
                String v = indexInput.getText();
                if (v == null || v.isBlank()) {
                    index = -1;
                } else {
                    index = Integer.parseInt(v);
                }
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("Index"));
                return;
            }

            Document doc;
            if (parentNode instanceof Document) {
                doc = (Document) parentNode;
            } else {
                doc = parentNode.getOwnerDocument();
            }
            String value = valueArea.getText();
            Node newNode;
            if (elementRadio.isSelected()) {
                String name = nameInput.getText();
                if (name == null || name.isBlank()) {
                    popError(message("InvalidParameter") + ": " + message("Name"));
                    return;
                }
                Element element = doc.createElement(name.trim());
                for (Node attr : tableData) {
                    element.setAttribute(attr.getNodeName(), attr.getNodeValue());
                }
                newNode = element;

            } else if (textRadio.isSelected()) {
                newNode = doc.createTextNode(value);

            } else if (cdataRadio.isSelected()) {
                newNode = doc.createCDATASection(value);

            } else if (commentRadio.isSelected()) {
                newNode = doc.createComment(value);

            } else if (xmlRadio.isSelected()) {
                Element element = XmlTools.toElement(null, this, value);
                if (element == null) {
                    return;
                }
                newNode = doc.importNode(element, true);

            } else {
                return;
            }
            if (value != null && !value.isBlank()) {
                TableStringValues.add("XmlNodeValueHistories", value);
            }
            TreeItem<XmlTreeNode> newItem = new TreeItem(new XmlTreeNode(newNode));
            ObservableList<TreeItem<XmlTreeNode>> children = treeItem.getChildren();
            int tindex = index - 1;
            if (tindex >= 0 && index < children.size()) {
                Node tnode = children.get(tindex).getValue().getNode();
                parentNode.insertBefore(newNode, tnode);
                children.add(tindex, newItem);

            } else {
                parentNode.appendChild(newNode);
                children.add(newItem);
            }

            treeController.focusItem(newItem);
            treeController.xmlEditor.domChanged(true);
            treeController.xmlEditor.popInformation(message("CreatedSuccessfully"));

            close();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void cancelAction() {
        close();
    }

    @Override
    public boolean keyESC() {
        close();
        return false;
    }

    /*
        static methods
     */
    public static XmlAddNodeController open(ControlXmlTree treeController, TreeItem<XmlTreeNode> item) {
        XmlAddNodeController controller = (XmlAddNodeController) WindowTools.childStage(
                treeController, Fxmls.XmlAddNodeFxml);
        if (controller != null) {
            controller.setParameters(treeController, item);
            controller.requestMouse();
        }
        return controller;
    }

}
