package mara.mybox.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-09-11
 * @License Apache License Version 2.0
 */
public class ControlListCheckBox extends BaseController {

    protected Map<Integer, CheckBox> checkBoxs;
    protected List<Integer> checkedOrders;  // in order of checked
    protected SimpleBooleanProperty checkedNotify, rightClickedNotify;
    protected MouseEvent mouseEvent;

    @FXML
    protected ListView<String> listView;

    public ControlListCheckBox() {
        checkedNotify = new SimpleBooleanProperty(false);
        rightClickedNotify = new SimpleBooleanProperty(false);
    }

    public void setParent(BaseController parent) {
        try {
            this.parentController = parent;
            this.baseName = parent.baseName;

            checkBoxs = new HashMap<>();
            checkedOrders = new ArrayList<>();

            listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

            listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
                @Override
                public ListCell<String> call(ListView<String> param) {

                    ListCell<String> cell = new ListCell<String>() {
                        final CheckBox checkbox = new CheckBox();
                        Integer index = -1;
                        boolean settingCell;

                        {
                            checkbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                                @Override
                                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                                    if (isSettingValues || settingCell || index < 0) {
                                        return;
                                    }
                                    if (newValue) {
                                        if (!checkedOrders.contains(index)) {
                                            checkedOrders.add(index);
                                        }
                                    } else {
                                        if (checkedOrders.contains(index)) {
                                            checkedOrders.remove(index);
                                        }
                                    }
                                    checkedNotify.set(!checkedNotify.get());
                                }
                            });

                            checkbox.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent event) {
                                    if (event.getButton() == MouseButton.SECONDARY) {
                                        mouseEvent = event;
                                        rightClickedNotify.set(!rightClickedNotify.get());
                                    }
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
                            settingCell = true;
                            checkbox.setSelected(checkedOrders != null && checkedOrders.contains(index));
                            settingCell = false;
                            setGraphic(checkbox);
                            setText(item);
                            checkBoxs.put(index, checkbox);
                        }
                    };
                    return cell;
                }
            });

            listView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getButton() == MouseButton.SECONDARY) {
                        mouseEvent = event;
                        rightClickedNotify.set(!rightClickedNotify.get());
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        change
     */
    public void clear() {
        checkedOrders = null;
        listView.getItems().clear();
    }

    public void setValues(List<String> values) {
        checkedOrders = null;
        listView.getItems().clear();
        if (values != null) {
            listView.getItems().setAll(values);
        }
    }

    public void setCheckIndices(List<Integer> indices) {
        checkedOrders = indices;
        listView.refresh();
    }

    public void check(String value, boolean check) {
        if (value == null) {
            return;
        }
        if (checkedOrders == null) {
            if (!check) {
                return;
            }
        }
        List<String> items = listView.getItems();
        for (int i = 0; i < items.size(); i++) {
            Integer index = (Integer) i;
            if (value.equals(items.get(i))) {
                if (check) {
                    if (!checkedOrders.contains(index)) {
                        checkedOrders.add(index);
                    }
                } else {
                    if (checkedOrders.contains(index)) {
                        checkedOrders.remove(index);
                    }
                }
                listView.refresh();
                return;
            }
        }
    }

    public void checkValues(List<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        if (checkedOrders == null) {
            checkedOrders = new ArrayList<>();
        }
        List<String> items = listView.getItems();
        for (int i = 0; i < items.size(); i++) {
            Integer index = (Integer) i;
            if (values.contains(items.get(i))) {
                if (!checkedOrders.contains(index)) {
                    checkedOrders.add(index);
                }
            }
        }
        listView.refresh();
    }

    public void add(int index, String value, boolean check) {
        List<Integer> newSelected = new ArrayList<>();
        if (check) {
            newSelected.add((Integer) index);
        }
        if (checkedOrders != null && !checkedOrders.isEmpty()) {
            for (Integer i : checkedOrders) {
                if (i >= index) {
                    newSelected.add((Integer) (i + 1));
                } else {
                    newSelected.add((Integer) i);
                }
            }
        }
        List<String> values = new ArrayList<>();
        if (listView.getItems() == null) {
            values.add(value);
        } else {
            values.addAll(listView.getItems());
            values.add(index, value);
        }
        setValues(values);
        setCheckIndices(newSelected);
    }

    public void setValue(int index, String value) {
        listView.getItems().set(index, value);
        listView.refresh();
    }

    public void checkAll() {
        checkedOrders = new ArrayList<>();
        for (int i = 0; i < listView.getItems().size(); i++) {
            checkedOrders.add(i);
        }
        listView.refresh();
    }

    public void checkNone() {
        checkedOrders = null;
        listView.refresh();
    }

    /*
        query
     */
    public List<String> getValues() {
        return listView.getItems();
    }

    public List<Integer> checkedOrders() {
        return checkedOrders;
    }

    public boolean hasChecked() {
        return checkedOrders != null && !checkedOrders.isEmpty();
    }

    public List<String> checkedValues() {
        if (checkedOrders == null || checkedOrders.isEmpty()) {
            return null;
        }
        List<String> checked = new ArrayList<>();
        for (int i : checkedOrders) {
            checked.add(listView.getItems().get(i));
        }
        return checked;
    }

    public List<Integer> checkedIndices() {
        List<Integer> checked = new ArrayList<>();
        for (int i = 0; i < listView.getItems().size(); i++) {
            Integer index = (Integer) i;
            CheckBox cb = checkBoxs.get(index);
            if (cb != null && cb.isSelected()) {
                checked.add(index);
            }
        }
        return checked;
    }

    public int selectedIndex() {
        return listView.getSelectionModel().getSelectedIndex();
    }

}
