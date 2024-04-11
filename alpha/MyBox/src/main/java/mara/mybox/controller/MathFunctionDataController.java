package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.tools.FileTmpTools.generateFile;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.commons.csv.CSVPrinter;

/**
 * @Author Mara
 * @CreateDate 2022-9-2
 * @License Apache License Version 2.0
 */
public class MathFunctionDataController extends BaseChildController {

    protected MathFunctionEditor editorController;
    protected String expression, domain, outputs = "";

    protected int dataScale, variablesSize;
    protected List<String> variables;
    protected List<ControlDataSplit> splits;
    protected CSVPrinter csvPrinter;
    protected long count;
    protected List<Object> row;

    @FXML
    protected TabPane dataTabPane;
    @FXML
    protected ComboBox<String> dataScaleSelector;

    public MathFunctionDataController() {
        baseTitle = message("MathFunction");
    }

    public void setParameters(MathFunctionEditor editor) {
        try {
            this.editorController = editor;

            splits = new ArrayList<>();

            dataScale = UserConfig.getInt(baseName + "DataScale", 8);
            if (dataScale < 0) {
                dataScale = 8;
            }
            dataScaleSelector.getItems().addAll(
                    Arrays.asList("2", "1", "0", "3", "4", "5", "6", "7", "8", "10", "12", "15")
            );
            dataScaleSelector.getSelectionModel().select(dataScale + "");

            dataTabPane.getTabs().clear();
            splits.clear();
            variables = editorController.variableNames();
            variablesSize = 0;
            if (variables != null) {
                variablesSize = variables.size();
                for (String variable : variables) {
                    Tab tab = new Tab(variable);
                    tab.setClosable(false);
                    dataTabPane.getTabs().add(tab);
                    FXMLLoader fxmlLoader = new FXMLLoader(WindowTools.class.getResource(
                            Fxmls.ControlDataSplitFxml), AppVariables.CurrentBundle);
                    Pane pane = fxmlLoader.load();
                    tab.setContent(pane);

                    ControlDataSplit controller = (ControlDataSplit) fxmlLoader.getController();
                    controller.name = variable;
                    splits.add(controller);
                }
            }
            refreshStyle(thisPane);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public boolean checkSplits() {
        try {
            long num = 1;
            for (ControlDataSplit split : splits) {
                if (!split.checkInputs()) {
                    return false;
                }
                num *= Math.ceil((split.to - split.from) / split.interval()) + 1;
            }
            return num <= 5000 || PopTools.askSure(null,
                    message("SureContinueGenerateLotsData") + "\n"
                    + message("DataSize") + " ~= " + num);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    /*
        data set
     */
    protected DataFileCSV generateData() {
        try {
            if (variables == null || variables.isEmpty()) {
                return null;
            }
            count = 0;
            File csvFile = generateFile(editorController.titleName(), "csv");
            List<Data2DColumn> db2Columns = new ArrayList<>();
            try (CSVPrinter printer = CsvTools.csvPrinter(csvFile)) {
                csvPrinter = printer;
                String resultName = editorController.functionName();
                row = new ArrayList<>();
                row.addAll(variables);
                row.add(resultName);
                csvPrinter.printRecord(row);
                for (Object name : row) {
                    db2Columns.add(new Data2DColumn((String) name, ColumnType.Double, true));
                }
                List<Object> values = new ArrayList<>();
                makeRow(values);
                csvPrinter.flush();
                csvPrinter.close();
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.error(e);
                csvPrinter = null;
                return null;
            }
            if (task == null || task.isCancelled()) {
                return null;
            }
            DataFileCSV data = new DataFileCSV();
            data.setColumns(db2Columns)
                    .setFile(csvFile).setDataName(interfaceName)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(2).setRowsNumber(count);
            return data;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }

    public void makeRow(List<Object> values) {
        try {
            if (task == null || task.isCancelled()) {
                return;
            }
            int index = values.size();
            if (index >= variables.size()) {
                calculateRow(values);
                return;
            }
            ControlDataSplit split = splits.get(index);
            double interval = split.interval();
            for (double d = split.from; d <= split.to; d += interval) {
                if (task == null || task.isCancelled()) {
                    return;
                }
                values.add(d);
                makeRow(values);
                values.remove(index);
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);

        }
    }

    public void calculateRow(List<Object> values) {
        try {
            if (!editorController.inDomain(fillValues(domain, values))) {
                return;
            }
            String finalScript = fillValues(expression, values);
            String fx = editorController.eval(finalScript);
            if (fx == null) {
                return;
            }
            double d = DoubleTools.scale(fx, InvalidAs.Empty, dataScale);
            row.clear();
            row.addAll(values);
            row.add(d);
            csvPrinter.printRecord(row);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);

        }
    }

    public String fillValues(String script, List<Object> values) {
        try {
            if (script == null || script.isBlank()
                    || variables == null || variables.size() > values.size()) {
                return script;
            }
            String vars = "";
            for (int i = 0; i < variables.size(); i++) {
                vars += "var " + variables.get(i) + "=" + values.get(i) + ";\n";
            }
            return vars + script;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public boolean initData() {
        if (!editorController.checkScripts() || !checkSplits()) {
            return false;
        }
        int v = editorController.checkScale(dataScaleSelector);
        if (v >= 0) {
            dataScale = v;
            UserConfig.setInt(baseName + "DataScale", v);
        } else {
            popError(message("InvalidParameter") + ": " + message("DecimalScale"));
            return false;
        }
        expression = editorController.script();
        domain = editorController.domain();
        return true;
    }

    @FXML
    @Override
    public void okAction() {
        if (!initData()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private DataFileCSV data;

            @Override
            protected boolean handle() {
                data = generateData();
                return data != null && data.saveAttributes();
            }

            @Override
            protected void whenSucceeded() {
                Data2DManufactureController.openDef(data);
                if (closeAfterCheck.isSelected()) {
                    close();
                }
            }

        };
        start(task);
    }

    public String title() {
        String title = editorController.attributesController.nameInput.getText();
        if (title == null || title.isBlank()) {
            int pos = expression.indexOf("\n");
            title = pos < 0 ? expression : expression.substring(0, pos);
        }
        return title;
    }

    /*
        static
     */
    public static MathFunctionDataController open(MathFunctionEditor editorController) {
        try {
            MathFunctionDataController controller = (MathFunctionDataController) WindowTools.branchStage(
                    editorController, Fxmls.MathFunctionDataFxml);
            controller.setParameters(editorController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }
}
