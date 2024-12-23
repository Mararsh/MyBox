package mara.mybox.controller;

import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import mara.mybox.data.HtmlNode;
import mara.mybox.dev.MyBoxLog;
import org.jsoup.nodes.Element;

/**
 * @Author Mara
 * @CreateDate 2023-2-20
 * @License Apache License Version 2.0
 */
public class ControlHtmlDomSource extends BaseHtmlTreeController {

    @FXML
    protected Label topLabel;

    @Override
    public BooleanProperty getSelectedProperty(HtmlNode node) {
        return node.getSelected();
    }

    public void load(Element element, TreeItem<HtmlNode> item) {
        try {
            super.loadElement(element);

            if (item != null) {
                TreeItem<HtmlNode> sourceItem = findSequenceNumber(makeHierarchyNumber(item));
                if (sourceItem != null) {
                    focusItem(sourceItem);
                    sourceItem.getValue().getSelected().set(true);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setLabel(String label) {
        topLabel.setText(label);
    }

}
