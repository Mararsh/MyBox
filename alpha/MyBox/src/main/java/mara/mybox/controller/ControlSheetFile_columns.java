package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-28
 * @License Apache License Version 2.0
 */
public abstract class ControlSheetFile_columns extends ControlSheetFile_Base {

    @Override
    public void makeDefintionPane() {
        defBox.getChildren().clear();
        if (sourceFile == null) {
            if (tabPane.getTabs().contains(defTab)) {
                tabPane.getTabs().removeAll(defTab, reportTab);
            }
            return;
        }
        if (!tabPane.getTabs().contains(defTab)) {
            tabPane.getTabs().add(4, defTab);
            tabPane.getTabs().add(5, reportTab);
        }
        if (columns == null && pageData != null && pageData.length > 0) {
            makeColumns(pageData[0].length);
        }
        if (columns == null) {
            return;
        }
        int index = 1;
        for (ColumnDefinition column : columns) {
            HBox line = new HBox();
            line.setAlignment(Pos.CENTER_LEFT);
            line.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            line.setSpacing(5);

            Label indexLabel = new Label("" + index++);
            indexLabel.setPrefWidth(50);
            indexLabel.setMinWidth(Region.USE_PREF_SIZE);
            indexLabel.setAlignment(Pos.CENTER_RIGHT);

            TextField nameInput = new TextField(column.getName());
            nameInput.setPrefWidth(200);
            nameInput.setMinWidth(Region.USE_PREF_SIZE);

            TextField widthInput = new TextField(column.getWidth() + "");
            widthInput.setPrefWidth(80);
            widthInput.setMinWidth(Region.USE_PREF_SIZE);

            CheckBox notNull = new CheckBox(message("Yes"));
            notNull.setPrefWidth(80);
            notNull.setMinWidth(Region.USE_PREF_SIZE);
            notNull.setSelected(column.isNotNull());

            ComboBox<String> typeSelector = new ComboBox<>();
            for (ColumnDefinition.ColumnType type : ColumnDefinition.editTypes()) {
                typeSelector.getItems().add(message(type.name()));
            }
            typeSelector.setValue(message(column.getType().name()));

            line.getChildren().addAll(indexLabel, nameInput, typeSelector, notNull, widthInput);
            defBox.getChildren().add(line);

        }
        refreshStyle(defBox);
    }

    @FXML
    public void okDefAction() {
        if (defBox == null) {
            return;
        }
        List<ColumnDefinition> newValues = new ArrayList<>();
        boolean ok = true;
        for (Node node : defBox.getChildren()) {
            ColumnDefinition column = new ColumnDefinition();
            HBox line = (HBox) node;

            TextField nameInput = (TextField) (line.getChildren().get(1));
            if (nameInput.getText().isBlank()) {
                nameInput.setStyle(NodeStyleTools.badStyle);
                ok = false;
            } else {
                column.setName(nameInput.getText().trim());
                nameInput.setStyle(null);
            }

            TextField widthInput = (TextField) (line.getChildren().get(4));
            try {
                double v = Double.parseDouble(widthInput.getText());
                if (v > 10) {
                    column.setWidth((int) v);
                    widthInput.setStyle(null);
                } else {
                    ok = false;
                    widthInput.setStyle(NodeStyleTools.badStyle);
                }
            } catch (Exception e) {
                ok = false;
                widthInput.setStyle(NodeStyleTools.badStyle);
            }

            if (ok) {
                CheckBox notNull = (CheckBox) (line.getChildren().get(3));
                column.setNotNull(notNull.isSelected());
            }

            ComboBox<String> typeSelector = (ComboBox) (line.getChildren().get(2));
            String ctype = typeSelector.getValue();
            if (ctype == null) {
                ok = false;
            } else if (ok) {
                for (ColumnDefinition.ColumnType type : ColumnDefinition.ColumnType.values()) {
                    if (ctype.equals(type.name()) || ctype.equals(message(type.name()))) {
                        column.setType(type);
                        break;
                    }
                }
            }

            if (ok) {
                newValues.add(column);
                if (!ColumnDefinition.valid(this, newValues)) {
                    return;
                }
            }
        }
        if (ok) {
            popSuccessful();
            makeSheet(pageData, newValues);
        } else {
            popError(message("InvalidData"));
        }
    }

    @Override
    protected StringTable validate() {
        try {
            StringTable table = super.validate();
            reportViewController.webEngine.getLoadWorker().cancel();
            if (dataInvalid) {
                reportViewController.webEngine.loadContent(table.html());
//                tabPane.getSelectionModel().select(reportTab);
            } else {
                reportViewController.webEngine.loadContent("<H2 align=\"center\">" + message("DataAreValid") + "</H2>");
            }
            return table;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @FXML
    public void recoverDefAction() {
        makeDefintionPane();
    }

    @FXML
    public void renameDefAction() {
        if (defBox == null) {
            return;
        }
        List<Node> nodes = defBox.getChildren();
        for (int i = 0; i < nodes.size(); i++) {
            HBox line = (HBox) nodes.get(i);
            TextField nameInput = (TextField) (line.getChildren().get(1));
            nameInput.setText(message(colPrefix) + (i + 1));
        }
    }

}
