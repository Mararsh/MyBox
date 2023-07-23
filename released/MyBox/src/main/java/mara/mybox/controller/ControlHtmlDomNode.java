package mara.mybox.controller;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;

/**
 * @Author Mara
 * @CreateDate 2023-2-14
 * @License Apache License Version 2.0
 */
public class ControlHtmlDomNode extends BaseChildController {

    protected ObservableList<Attribute> attributesData;
    protected Element element;

    @FXML
    protected TextField tagInput;
    @FXML
    protected TextArea textInput;
    @FXML
    protected TableView<Attribute> attributesTable;
    @FXML
    protected TableColumn<Attribute, String> keyColumn, valueColumn;

    @Override
    public void initControls() {
        try {
            super.initControls();

            attributesData = FXCollections.observableArrayList();
            attributesTable.setItems(attributesData);
            keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
            keyColumn.setCellFactory(TableAutoCommitCell.forStringColumn());
            keyColumn.setOnEditCommit((TableColumn.CellEditEvent<Attribute, String> t) -> {
                if (t == null) {
                    return;
                }
                Attribute row = t.getRowValue();
                if (row == null) {
                    return;
                }
                row.setKey(t.getNewValue());
            });
            keyColumn.getStyleClass().add("editable-column");

            valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
            valueColumn.setCellFactory(TableAutoCommitCell.forStringColumn());
            valueColumn.setOnEditCommit((TableColumn.CellEditEvent<Attribute, String> t) -> {
                if (t == null) {
                    return;
                }
                Attribute row = t.getRowValue();
                if (row == null) {
                    return;
                }
                row.setValue(t.getNewValue());
            });
            valueColumn.getStyleClass().add("editable-column");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void load(Element element) {
        clear();
        this.element = element;
        if (element == null) {
            return;
        }
        tagInput.setText(element.tagName());
        textInput.setText(element.wholeOwnText());
        Attributes attributes = element.attributes();
        if (attributes != null) {
            for (Attribute a : attributes) {
                attributesData.add(a);
            }
        }
    }

    public void clear() {
        element = null;
        tagInput.clear();
        textInput.clear();
        attributesData.clear();
    }

    public Element pickValues() {
        String tag = tagInput.getText();
        if (tag == null || tag.isBlank()) {
            return null;
        }
        if (element == null) {
            element = new Element(tag);
        } else {
            element.tagName(tag);
        }
        if (!"script".equalsIgnoreCase(tag) && !"style".equalsIgnoreCase(tag)) {
            element.text(textInput.getText());
        }
        element.clearAttributes();
        for (Attribute a : attributesData) {
            element.attr(a.getKey(), a.getValue());
        }
        return element;
    }

    public void recover() {
        load(element);
    }

    @FXML
    public void addAttribute() {
        attributesData.add(new Attribute("k", "v"));
    }

    @FXML
    public void deleteAttributes() {
        try {
            List<Attribute> selected = attributesTable.getSelectionModel().getSelectedItems();
            if (selected == null || selected.isEmpty()) {
                return;
            }
            attributesData.removeAll(selected);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void clearAttributes() {
        attributesData.clear();
    }

    @FXML
    public void popTagExamples(MouseEvent event) {
        PopTools.popHtmlTagExamples(this, tagInput, event);
    }

}
