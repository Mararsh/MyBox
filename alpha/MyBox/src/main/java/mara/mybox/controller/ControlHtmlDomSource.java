package mara.mybox.controller;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
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
public class ControlHtmlDomSource extends BaseHtmlDomTreeController {

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

    public void load(Element element, int rowIndex) {
        try {
            super.load(element);

            TreeItem<HtmlNode> target = domTree.getTreeItem(rowIndex);
            if (target != null) {
                target.getValue().getSelected().set(true);
                domTree.scrollTo(rowIndex);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

}
