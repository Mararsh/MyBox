/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.fxml.cell;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.cell.CheckBoxTreeCell;
import mara.mybox.fxml.ConditionNode;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.NodeTools;

/**
 * @Author Mara
 * @CreateDate 2020-4-18
 * @License Apache License Version 2.0
 */
public class TreeConditionCell extends CheckBoxTreeCell<ConditionNode> {

    @Override
    public void updateItem(ConditionNode item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }
        setText(item.getText());
        Node node = getGraphic();
        if (node != null && node instanceof CheckBox) {
            CheckBox checkBox = (CheckBox) node;
            if (item.getCondition() != null && !item.getCondition().isBlank()) {
                NodeStyleTools.setTooltip(checkBox, item.getCondition());
            } else {
                NodeStyleTools.removeTooltip(checkBox);
            }
        }
    }
}
