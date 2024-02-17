package mara.mybox.controller;

import java.util.Arrays;
import java.util.List;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.InfoNode;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-9-2
 * @License Apache License Version 2.0
 */
public class MathFunctionController extends InfoTreeManageController {

    protected String expression, domain, outputs = "";
    protected int calculateScale;

    @FXML
    protected MathFunctionEditor editorController;
    @FXML
    protected VBox inputsBox;
    @FXML
    protected ComboBox<String> calculateScaleSelector;
    @FXML
    protected ControlWebView outputController;
    @FXML
    protected Button calculateButton, dataButton, XYChartButton;

    public MathFunctionController() {
        baseTitle = message("MathFunction");
        TipsLabelKey = "MathFunctionTips";
        category = InfoNode.MathFunction;
        nameMsg = message("Title");
        valueMsg = message("MathFunction");
    }

    @Override
    public void initControls() {
        try {
            editor = editorController;
            super.initControls();

            outputController.setParent(this, ControlWebView.ScrollType.Bottom);

            calculateScale = UserConfig.getInt(baseName + "CalculateScale", 8);
            if (calculateScale < 0) {
                calculateScale = 8;
            }
            calculateScaleSelector.getItems().addAll(
                    Arrays.asList("2", "1", "0", "3", "4", "5", "6", "7", "8", "10", "12", "15")
            );
            calculateScaleSelector.getSelectionModel().select(calculateScale + "");

            calculateButton.disableProperty().bind(editorController.valueInput.textProperty().isEmpty());
            dataButton.disableProperty().bind(editorController.valueInput.textProperty().isEmpty());

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void variablesChanged() {
        try {
            inputsBox.getChildren().clear();
            XYChartButton.setDisable(true);
            List<String> variables = editorController.variableNames();
            if (variables != null && !variables.isEmpty()) {
                for (String variable : variables) {
                    FlowPane fp = new FlowPane();
                    fp.setAlignment(Pos.CENTER_LEFT);
                    fp.setVgap(2);
                    fp.setHgap(2);
                    TextField input = new TextField();
                    input.setPrefWidth(80);
                    fp.getChildren().addAll(new Label(variable), input);
                    inputsBox.getChildren().add(fp);
                }
                if (variables.size() <= 2) {
                    XYChartButton.setDisable(false);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public String script() {
        return editorController.valueInput.getText();
    }

    public String domain() {
        String d = editorController.moreInput.getText();
        if (d == null || d.isBlank()) {
            return null;
        }
        return StringTools.replaceLineBreak(d);
    }

    public int checkScale(ComboBox<String> selector) {
        try {
            int v = Integer.parseInt(selector.getValue());
            if (v >= 0) {
                selector.getEditor().setStyle(null);
                return v;
            } else {
                selector.getEditor().setStyle(UserConfig.badStyle());
                return -1;
            }
        } catch (Exception e) {
            selector.getEditor().setStyle(UserConfig.badStyle());
            return -2;
        }
    }

    @FXML
    public void calculateAction() {
        try {
            if (!editorController.checkScripts()) {
                return;
            }
            int v = editorController.checkScale(calculateScaleSelector);
            if (v >= 0) {
                calculateScale = v;
                UserConfig.setInt(baseName + "CalculateScale", v);
            } else {
                popError(message("InvalidParameter") + ": " + message("DecimalScale"));
                return;
            }
            expression = script();
            domain = domain();
            if (!editorController.inDomain(fillInputs(domain))) {
                popError(message("NotInDomain"));
                return;
            }
            String finalScript = fillInputs(expression);
            String ret = editorController.eval(finalScript);
            if (ret == null) {
                popError(message("Failed"));
                return;
            }
            double d = DoubleTools.scale(ret, ColumnDefinition.InvalidAs.Blank, calculateScale);
            ret = DoubleTools.invalidDouble(d) ? ret : (d + "");
            outputs += DateTools.nowString() + "<div class=\"valueText\" >"
                    + HtmlWriteTools.stringToHtml(finalScript)
                    + "</div>";
            outputs += "<div class=\"valueBox\">"
                    + HtmlWriteTools.stringToHtml(editorController.functionName() + "=" + ret)
                    + "</div><br><br>";
            String html = HtmlWriteTools.html(null, HtmlStyles.DefaultStyle, "<body>" + outputs + "</body>");
            outputController.loadContents(html);
            TableStringValues.add("FunctionScriptHistories", expression);
            if (domain != null && !domain.isBlank()) {
                TableStringValues.add("FunctionDomainHistories", domain);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public String fillInputs(String script) {
        try {
            List<String> variables = editorController.variableNames();
            if (script == null || script.isBlank() || variables == null) {
                return script;
            }
            String vars = "";
            List<Node> nodes = inputsBox.getChildren();
            for (int i = 0; i < nodes.size(); i++) {
                FlowPane fp = (FlowPane) nodes.get(i);
                Label label = (Label) fp.getChildren().get(0);
                TextField input = (TextField) fp.getChildren().get(1);
                double d = DoubleTools.toDouble(input.getText(), ColumnDefinition.InvalidAs.Blank);
                vars += "var " + label.getText() + "=" + d + ";\n";
            }
            return vars + script;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @FXML
    protected void showHtmlStyle(Event event) {
        PopTools.popHtmlStyle(event, outputController);
    }

    @FXML
    protected void popHtmlStyle(Event event) {
        if (UserConfig.getBoolean("HtmlStylesPopWhenMouseHovering", false)) {
            showHtmlStyle(event);
        }
    }

    @FXML
    public void editResults() {
        outputController.editAction();
    }

    @FXML
    public void clearResults() {
        outputs = "";
        outputController.clear();
    }

    @FXML
    public void dataAction() {
        MathFunctionData.open(editorController);
    }

    @FXML
    public void chartAction() {
        MathFunctionChart.open(editorController);
    }

    @FXML
    public void popMathFunctionHelps(Event event) {
        if (UserConfig.getBoolean("JavaScriptHelpsPopWhenMouseHovering", false)) {
            showMathFunctionHelps(event);
        }
    }

    @FXML
    public void showMathFunctionHelps(Event event) {
        popEventMenu(event, HelpTools.javascriptHelps());
    }

    /*
        static
     */
    public static MathFunctionController open() {
        try {
            MathFunctionController controller = (MathFunctionController) WindowTools.openStage(Fxmls.MathFunctionFxml);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
