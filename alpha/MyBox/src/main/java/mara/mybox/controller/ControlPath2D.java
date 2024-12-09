package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import mara.mybox.data.DoublePath;
import mara.mybox.data.DoublePathSegment;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.cell.TableBooleanCell;
import mara.mybox.fxml.cell.TableRowIndexCell;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-7-30
 * @License Apache License Version 2.0
 */
public class ControlPath2D extends BaseTableViewController<DoublePathSegment> {

    public int Scale;

    @FXML
    protected Tab codesTab, textsTab;
    @FXML
    protected TableColumn<DoublePathSegment, String> indexColumn, typeColumn,
            commandColumn, parametersColumn;
    @FXML
    protected TableColumn<DoublePathSegment, Boolean> absoluteColumn;
    @FXML
    protected TextArea textArea;
    @FXML
    protected CheckBox wrapTextsCheck;
    @FXML
    protected Label textsLabel;

    @Override
    public void initControls() {
        try {
            super.initControls();

            Scale = UserConfig.imageScale();

            indexColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DoublePathSegment, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<DoublePathSegment, String> param) {
                    return new SimpleStringProperty("x");
                }
            });
            indexColumn.setCellFactory(new TableRowIndexCell());

            typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeName"));

            absoluteColumn.setCellValueFactory(new PropertyValueFactory<>("isAbsolute"));
            absoluteColumn.setCellFactory(new TableBooleanCell());

            commandColumn.setCellValueFactory(new PropertyValueFactory<>("command"));

            parametersColumn.setCellValueFactory(new PropertyValueFactory<>("parameters"));

            wrapTextsCheck.setSelected(UserConfig.getBoolean(baseName + "WrapText", true));
            textArea.setWrapText(wrapTextsCheck.isSelected());
            wrapTextsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "WrapText", wrapTextsCheck.isSelected());
                    textArea.setWrapText(wrapTextsCheck.isSelected());
                }
            });
            textArea.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    updateTextSize();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void updateTextSize() {
        String s = textArea.getText();
        if (s == null || s.isBlank()) {
            textsLabel.setText("");
        } else {
            textsLabel.setText(message("Count") + ": " + s.length());
        }
    }

    /*
        data
     */
    @Override
    public void tableChanged(boolean changed) {
        if (isSettingValues || isSettingTable) {
            return;
        }
        super.tableChanged(changed);
        if (changed) {
            pickTableValue();
        }
    }

    public void pickTableValue() {
        if (tableData.isEmpty()) {
            textArea.clear();
        } else {
            String s = DoublePath.segmentsToString(tableData, "\n");
            textArea.setText(s);
        }
        updateTextSize();
    }

    public void loadPath(String contents) {
        List<DoublePathSegment> segments = DoublePath.stringToSegments(this, contents, Scale);
        isSettingValues = true;
        if (segments != null && !segments.isEmpty()) {
            tableData.setAll(segments);
        } else {
            tableData.clear();
        }
        isSettingValues = false;
        tableChanged();
    }

    public boolean pickValue() {
        try {
            String s = textArea.getText();
            loadPath(s);
            if (!tableData.isEmpty()) {
                TableStringValues.add("SvgPathHistories", s);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public String getText() {
        return textArea.getText();
    }

    public List<DoublePathSegment> getSegments() {
        List<DoublePathSegment> list = new ArrayList<>();
        for (DoublePathSegment seg : tableData) {
            list.add(seg.copy());
        }
        return list;
    }

    /*
        table
     */
    @FXML
    @Override
    public void addAction() {
        ShapePathSegmentEditController.open(this, -1, null);
    }

    @FXML
    public void insertAction() {
        int index = selectedIndix();
        if (index < 0) {
            popError(message("SelectToHandle"));
            return;
        }
        ShapePathSegmentEditController.open(this, index, null);
    }

    @FXML
    @Override
    public void editAction() {
        int index = selectedIndix();
        if (index < 0) {
            popError(message("SelectToHandle"));
            return;
        }
        ShapePathSegmentEditController.open(this, index, tableData.get(index));
    }

    /*
        text
     */
    @FXML
    public void typesettingAction() {
        String text = textArea.getText();
        if (text == null || text.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        String s = DoublePath.typesetting(this, text, "\n", Scale);
        if (s == null) {
            return;
        }
        textArea.setText(s);
    }

    @FXML
    public void popExamplesPathMenu(Event event) {
        if (UserConfig.getBoolean("SvgPathExamplesPopWhenMouseHovering", false)) {
            showExamplesPathMenu(event);
        }
    }

    @FXML
    public void showExamplesPathMenu(Event event) {
        PopTools.popMappedValues(this, textArea, "SvgPathExamples", HelpTools.svgPathExamples(), event);
    }

    @FXML
    protected void popPathHistories(Event event) {
        if (UserConfig.getBoolean("SvgPathHistoriesPopWhenMouseHovering", false)) {
            showPathHistories(event);
        }
    }

    @FXML
    protected void showPathHistories(Event event) {
        PopTools.popSavedValues(this, textArea, event, "SvgPathHistories", false);
    }

    @FXML
    protected void popHelps(Event event) {
        if (UserConfig.getBoolean("SvgPathHelpsPopWhenMouseHovering", false)) {
            showHelps(event);
        }
    }

    @FXML
    protected void showHelps(Event event) {
        popEventMenu(event, HelpTools.svgPathHelps());
    }

}
