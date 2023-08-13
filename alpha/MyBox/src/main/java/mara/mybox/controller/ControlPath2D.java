package mara.mybox.controller;

import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import mara.mybox.data.DoublePath;
import mara.mybox.data.DoublePathSegment;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.cell.TableBooleanCell;
import mara.mybox.fxml.cell.TableRowIndexCell;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-7-30
 * @License Apache License Version 2.0
 */
public class ControlPath2D extends BaseTableViewController<DoublePathSegment> {

    protected ControlShapeOptions optionsOontroller;
    public int Scale = 3;

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
    protected CheckBox wrapTextsCheck, typesettingCheck;

    @Override
    public void initControls() {
        try {
            super.initControls();

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

            typesettingCheck.setSelected(UserConfig.getBoolean(baseName + "Typesetting", true));
            typesettingCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Typesetting", typesettingCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        data
     */
    public void loadPath(String contents) {
        List<DoublePathSegment> segments = DoublePath.stringToSegments(this, contents, Scale);
        if (segments == null || segments.isEmpty()) {
            tableData.clear();
            textArea.clear();
            return;
        } else {
            tableData.setAll(segments);
        }
        String s = contents;
        if (typesettingCheck.isSelected()) {
            s = DoublePath.segmentsToString(segments, "\n");
        }
        textArea.setText(s);

    }

    public boolean pickPath() {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if (tab == codesTab) {
            if (tableData.isEmpty()) {
                popError(message("NoData"));
                return false;
            }
            String s = DoublePath.segmentsToString(tableData, typesettingCheck.isSelected() ? "\n" : " ");
            textArea.setText(s);

        } else {
            String s = textArea.getText();
            List<DoublePathSegment> segments = DoublePath.stringToSegments(this, s, Scale);
            if (segments == null) {
                return false;
            }
            if (segments.isEmpty()) {
                popError(message("NoData"));
                return false;
            }
            tableData.setAll(segments);
        }

        return true;
    }

    @FXML
    @Override
    public void goAction() {
        if (pickPath() && optionsOontroller != null) {
            optionsOontroller.goShape();
        }
    }

    public String getText() {
        return textArea.getText();
    }

    public List<DoublePathSegment> getSegments() {
        return tableData;
    }

    /*
        table
     */
    @FXML
    @Override
    public void addAction() {

    }

    @FXML
    @Override
    public void editAction() {

    }


    /*
        text
     */
    @FXML
    public void editTexts() {
        if (textArea.getText().isEmpty()) {
            popError(message("NoData"));
            return;
        }
        TextEditorController controller = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
        controller.loadContents(textArea.getText());
    }

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
        PopTools.popValues(this, textArea, "SvgPathExamples", HelpTools.svgPathExamples(), event);
    }

    @FXML
    protected void popPathHistories(Event event) {
        if (UserConfig.getBoolean("SvgPathHistoriesPopWhenMouseHovering", false)) {
            showPathHistories(event);
        }
    }

    @FXML
    protected void showPathHistories(Event event) {
        PopTools.popStringValues(this, textArea, event, "SvgPathHistories", false, true);
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
