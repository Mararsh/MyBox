package mara.mybox.controller;

import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import mara.mybox.data.DoublePath;
import mara.mybox.data.DoublePathSegment;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.cell.TableAutoCommitCell;
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

    public int Scale = 2;
    protected DoublePath path;

    @FXML
    protected TableColumn<DoublePathSegment, String> indexColumn, typeColumn, pointsColumn;
    @FXML
    protected TextArea textArea;

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

            pointsColumn.setCellValueFactory(new PropertyValueFactory<>("pointsText"));
            pointsColumn.setCellFactory(TableAutoCommitCell.forStringColumn());
            pointsColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<DoublePathSegment, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<DoublePathSegment, String> e) {
                    if (e == null) {
                        return;
                    }
                    int row = e.getTablePosition().getRow();
                    String s = e.getNewValue();
                    if (row < 0 || s == null || s.isBlank()) {
                        return;
                    }
                    List<DoublePoint> points = DoublePoint.parseList(s);
                    if (points == null || points.isEmpty()) {
                        return;
                    }
                    DoublePathSegment seg = e.getRowValue();
                    seg.setPoints(points);
                    tableData.set(row, seg);
                }
            });
            pointsColumn.setEditable(true);
            pointsColumn.getStyleClass().add("editable-column");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void tableChanged(boolean changed) {
    }

    public void loadPath(String contents) {
        path = new DoublePath(contents);
        load();
    }

    public void load() {
        if (path == null) {
            tableData.clear();
            textArea.clear();
            return;
        }
        textArea.setText(path.getContent());
        List<DoublePathSegment> segments = path.getSegments();
        if (segments == null || segments.isEmpty()) {
            tableData.clear();
        } else {
            tableData.setAll(segments);
        }
    }

    public String pickPath(String separator, int scale) {
        return DoublePath.segmentsToPath(tableData, separator, scale);
    }

    @FXML
    public void checkAbsolute() {

    }

    @FXML
    @Override
    public void addAction() {
//        List<DoublePoint> line = new ArrayList<>();
//        line.add(new DoublePoint(0, 0));
//        tableData.add(line);
    }

    @FXML
    @Override
    public void recoverAction() {
        load();
    }

    @FXML
    public void typesettingAction() {
        String text = textArea.getText();
        if (text == null || text.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        textArea.setText(DoublePath.typesetting(text, "\n", 2));
    }

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

}
