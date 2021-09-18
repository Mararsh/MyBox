package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.util.Callback;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-09-11
 * @License Apache License Version 2.0
 */
public class ControlListCheckBox extends BaseController {

    protected List<Integer> selectedIndexs;
    protected SimpleBooleanProperty changedNotify;

    @FXML
    protected ListView<String> listView;

    public ControlListCheckBox() {
        baseTitle = "ColorImport";
        changedNotify = new SimpleBooleanProperty(false);
    }

    public void setParent(BaseController parent) {
        try {
            this.parentController = parent;
            this.baseName = parent.baseName;

            listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
                @Override
                public ListCell<String> call(ListView<String> param) {
                    ListCell<String> cell = new ListCell<String>() {
                        final CheckBox checkbox = new CheckBox();
                        Integer index = -1;
                        boolean setting;

                        {
                            checkbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                                @Override
                                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                                    if (setting || index < 0) {
                                        return;
                                    }
                                    if (selectedIndexs == null) {
                                        selectedIndexs = new ArrayList<>();
                                    }
                                    if (newValue) {
                                        if (!selectedIndexs.contains(index)) {
                                            selectedIndexs.add(index);
                                        }
                                    } else {
                                        if (selectedIndexs.contains(index)) {
                                            selectedIndexs.remove(index);
                                        }
                                    }
                                    changedNotify.set(!changedNotify.get());
                                }
                            });
                        }

                        @Override
                        public void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            index = (Integer) getIndex();
                            if (empty || item == null) {
                                setGraphic(null);
                                setText(null);
                                return;
                            }
                            setting = true;
                            checkbox.setText(item);
                            checkbox.setSelected(selectedIndexs != null && selectedIndexs.contains(index));
                            setting = false;
                            setGraphic(checkbox);
                            setText(null);
                        }
                    };
                    return cell;
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void clear() {
        selectedIndexs = null;
        listView.getItems().clear();
    }

    public void setValues(List<String> values) {
        selectedIndexs = null;
        listView.getItems().setAll(values);
    }

    public List<String> getValues() {
        return listView.getItems();
    }

    public List<Integer> getSelectedIndex() {
        return selectedIndexs;
    }

    public void selectIndex(List<Integer> values) {
        selectedIndexs = values;
        listView.refresh();
    }

    public void select(int index, boolean select) {
        if (selectedIndexs == null) {
            selectedIndexs = new ArrayList<>();
        }
        if (select) {
            if (!selectedIndexs.contains(index)) {
                selectedIndexs.add(index);
            }
        } else {
            if (selectedIndexs.contains(index)) {
                selectedIndexs.remove(index);
            }
        }
        listView.refresh();
    }

    public List<String> getSelectedValues() {
        if (selectedIndexs == null) {
            return null;
        }
        List<String> selected = new ArrayList<>();
        for (int i = 0; i < listView.getItems().size(); i++) {
            if (selectedIndexs.contains(i)) {
                selected.add(listView.getItems().get(i));
            }
        }
        return selected;
    }

    public void selectAll() {
        selectedIndexs = new ArrayList<>();
        for (int i = 0; i < listView.getItems().size(); i++) {
            selectedIndexs.add(i);
        }
        listView.refresh();
    }

    public void selectNone() {
        selectedIndexs = null;
        listView.refresh();
    }

}
