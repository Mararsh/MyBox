package mara.mybox.fxml.cell;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import mara.mybox.db.data.Data2DColumn;

/**
 * @Author Mara
 * @CreateDate 2025-12-14
 * @License Apache License Version 2.0
 */
public class CellTools {

    public static void makeColumnComboBox(ComboBox<Data2DColumn> colSelector) {
        if (colSelector == null) {
            return;
        }
        Callback<ListView<Data2DColumn>, ListCell<Data2DColumn>> colFactory
                = new Callback<ListView<Data2DColumn>, ListCell<Data2DColumn>>() {
            @Override
            public ListCell<Data2DColumn> call(ListView<Data2DColumn> param) {

                ListCell<Data2DColumn> cell = new ListCell<Data2DColumn>() {
                    @Override
                    public void updateItem(Data2DColumn item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                            return;
                        }
                        setText(item.getLabel());
                    }
                };
                return cell;
            }
        };
        colSelector.setButtonCell(colFactory.call(null));
        colSelector.setCellFactory(colFactory);
    }

    public static void selectItem(ComboBox<Data2DColumn> colSelector, String name) {
        if (colSelector == null || name == null) {
            return;
        }
        for (Data2DColumn col : colSelector.getItems()) {
            if (name.equals(col.getColumnName()) || name.equals(col.getLabel())) {
                colSelector.getSelectionModel().select(col);
                return;
            }
        }
    }

}
