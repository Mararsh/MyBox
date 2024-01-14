package mara.mybox.controller;

import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.FlowPane;
import mara.mybox.data.DoubleShape;
import mara.mybox.data.XmlTreeNode;
import static mara.mybox.data.XmlTreeNode.NodeType.Element;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @Author Mara
 * @CreateDate 2023-6-13
 * @License Apache License Version 2.0
 */
public class ControlSvgNodeEdit extends ControlXmlNodeEdit {

    protected SvgEditorController editor;

    @FXML
    protected Tab pathTab, styleTab;
    @FXML
    protected TextArea styleArea;
    @FXML
    protected ControlPath2D pathController;
    @FXML
    protected FlowPane shapeOpPane;

    @Override
    public void editNode(TreeItem<XmlTreeNode> item) {
        super.editNode(item);
        if (treeItem != null) {
            XmlTreeNode currentTreeNode = treeItem.getValue();
            if (currentTreeNode != null && currentTreeNode.getType() == Element) {
                String name = currentTreeNode.getNode().getNodeName();
                if (!name.equalsIgnoreCase("svg")) {
                    if (name.equalsIgnoreCase("path")) {
                        tabPane.getTabs().add(0, pathTab);
                        tabPane.getTabs().add(2, styleTab);
                    } else {
                        tabPane.getTabs().add(1, styleTab);
                    }
                    tabPane.getSelectionModel().select(0);
                    refreshStyle(tabPane);
                }
            }
        }
        Node focusedNode = null;
        try {
            focusedNode = treeItem.getValue().getNode();
        } catch (Exception e) {
        }
        editor.drawSVG(focusedNode);
        shapeOpPane.setVisible(item != null && item.getValue() != null
                && item.getValue().isSvgShape());
    }

    @Override
    public void setAttributes() {
        if (node == null) {
            return;
        }
        boolean isPath = "path".equalsIgnoreCase(node.getNodeName());
        NamedNodeMap attrs = node.getAttributes();
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                Node attr = attrs.item(i);
                String attrName = attr.getNodeName();
                String value = attr.getNodeValue();
                if (isPath && "d".equalsIgnoreCase(attrName)) {
                    pathController.loadPath(value);
                    continue;
                }
                if ("style".equalsIgnoreCase(attrName)) {
                    styleArea.setText(value);
                    continue;
                }
                tableData.add(attrs.item(i));
            }
        }
    }

    @Override
    public Node pickValue() {
        try {
            if (node == null) {
                return null;
            }
            super.pickValue();
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                return node;
            }
            Element element = (Element) node;
            String style = styleArea.getText();
            if (style != null && !style.isBlank()) {
                style = StringTools.trimBlanks(style);
                element.setAttribute("style", style);
                TableStringValues.add("SvgStyleHistories", style);
            } else {
                element.removeAttribute("style");
            }
            if ("path".equalsIgnoreCase(node.getNodeName())) {
                pathController.pickValue();
                String path = pathController.getText();
                if (path != null && !path.isBlank()) {
                    path = StringTools.trimBlanks(path);
                    element.setAttribute("d", path);
                } else {
                    element.removeAttribute("d");
                }
            }
            return node;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public void clearNode() {
        super.clearNode();
        pathController.loadPath("");
        styleArea.clear();
        tabPane.getTabs().removeAll(pathTab, styleTab);
    }

    /*
        style
     */
    @FXML
    public void popExamplesStyleMenu(Event event) {
        if (UserConfig.getBoolean("SvgStyleExamplesPopWhenMouseHovering", false)) {
            showExamplesStyleMenu(event);
        }
    }

    @FXML
    public void showExamplesStyleMenu(Event event) {
        PopTools.popValues(this, styleArea, "SvgStyleExamples", HelpTools.svgStyleExamples(), event);
    }

    @FXML
    protected void popStyleHistories(Event event) {
        if (UserConfig.getBoolean("SvgStyleHistoriesPopWhenMouseHovering", false)) {
            showStyleHistories(event);
        }
    }

    @FXML
    protected void showStyleHistories(Event event) {
        PopTools.popStringValues(this, styleArea, event, "SvgStyleHistories", false);
    }

    @FXML
    protected void clearStyle() {
        styleArea.clear();
    }

    /*
        shape
     */
    @FXML
    public void popShapeMenu(Event event) {
        if (UserConfig.getBoolean("SvgNodeShapeMenuPopWhenMouseHovering", true)) {
            showShapeMenu(event);
        }
    }

    @FXML
    public void showShapeMenu(Event event) {
        if (node == null || !(node instanceof Element)) {
            return;
        }
        List<MenuItem> items = DoubleShape.elementMenu(this, (Element) node);
        if (items == null) {
            return;
        }

        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean("SvgNodeShapeMenuPopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent cevent) {
                UserConfig.setBoolean("SvgNodeShapeMenuPopWhenMouseHovering", popItem.isSelected());
            }
        });
        items.add(popItem);

        popEventMenu(event, items);
    }

    public void loadPath(String content) {
        if (content == null || content.isBlank()) {
            popError(message("NoData"));
            return;
        }
        pathController.loadPath(content);
    }

}
