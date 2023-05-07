package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.util.Callback;
import mara.mybox.data.HtmlNode;
import mara.mybox.dev.MyBoxLog;
import org.jsoup.nodes.Element;

/**
 * @Author Mara
 * @CreateDate 2023-2-20
 * @License Apache License Version 2.0
 */
public class ControlHtmlDomSource extends BaseHtmlTreeController {

    private List<TreeItem<HtmlNode>> selected;

    @FXML
    protected Label topLabel;
    @FXML
    protected TreeTableColumn<HtmlNode, Boolean> selectColumn;

    @Override
    public void initControls() {
        try {
            super.initControls();

            selectColumn.setCellValueFactory(
                    new Callback<TreeTableColumn.CellDataFeatures<HtmlNode, Boolean>, ObservableValue<Boolean>>() {
                @Override
                public ObservableValue<Boolean> call(TreeTableColumn.CellDataFeatures<HtmlNode, Boolean> param) {
                    if (param.getValue() != null) {
                        return param.getValue().getValue().getSelected();
                    }
                    return null;
                }
            });
            selectColumn.setCellFactory(CheckBoxTreeTableCell.forTreeTableColumn(selectColumn));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void load(Element element, TreeItem<HtmlNode> item) {
        try {
            super.loadElement(element);

            if (item != null) {
                TreeItem<HtmlNode> sourceItem = find(hierarchyNumber(item));
                if (sourceItem != null) {
                    focus(sourceItem);
                    sourceItem.getValue().getSelected().set(true);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public List<TreeItem<HtmlNode>> selectedItems() {
        selected = new ArrayList<>();
        checkSelected(domTree.getRoot());
        return selected;
    }

    private void checkSelected(TreeItem<HtmlNode> item) {
        try {
            if (item == null) {
                return;
            }
            HtmlNode node = item.getValue();
            if (node == null) {
                return;
            }
            if (node.getSelected().get()) {
                selected.add(item);
            }
            ObservableList<TreeItem<HtmlNode>> children = item.getChildren();
            if (children == null) {
                return;
            }
            for (TreeItem<HtmlNode> child : children) {
                checkSelected(child);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void setLabel(String label) {
        topLabel.setText(label);
    }

}
