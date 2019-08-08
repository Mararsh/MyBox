package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import mara.mybox.controller.base.BaseController;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.MatrixTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.message;

/**
 * @Author Mara
 * @CreateDate 2019-5-18
 * @Description
 * @License Apache License Version 2.0
 */
public class MatrixCalculationController extends BaseController {

    public int scale = 6;
    public double[][] matrix;
    public boolean init;
    public long startTime;
    public String currentEdit, currentCalculation;
    public final String matrixIgnoreChars
            = "\t|,|，|\\||\\{|\\}|\\[|\\]|\\\"|\\\'|=|\\#|\\*|\\(|\\)|\\%|\\@|\\$|\\&";

    @FXML
    public ComboBox<String> editBox, valueBox;
    @FXML
    public Button editOkButton, calculateButton, copyAsAButton, copyAsBButton, editButton;
    @FXML
    public TextArea editArea, valueArea;
    @FXML
    public TextField editInput1, editInput2, valueInput1, valueInput2;
    @FXML
    public HBox editOpBox, valueOpBox;
    @FXML
    public VBox editPBox, editVBox, valuePBox, valueVBox;
    @FXML
    public TitledPane editPane, valuePane;

    public MatrixCalculationController() {
        baseTitle = AppVaribles.message("MatricesCalculation");

    }

    @Override
    public void initializeNext() {
        initEditPane();
        initValuePane();

        thisPane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (getMyStage() == null) {
                    return;
                }
                thisPane.setPrefHeight(myStage.getHeight());
            }
        });
    }

    /*
        edit tab
     */
    public void initEditPane() {
        if (editPane == null) {
            return;
        }

        editPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (editPane.isExpanded()) {
                    if (!editPBox.getChildren().contains(editVBox)) {
                        editPBox.getChildren().add(editVBox);
                    }
                    VBox.setVgrow(editVBox, Priority.ALWAYS);
                    editPBox.setPrefHeight(thisPane.getHeight());
                } else {
                    editPBox.getChildren().remove(editVBox);
                    valuePane.setExpanded(true);
                    valueVBox.setPrefHeight(thisPane.getHeight());
                }
            }
        });

        List<String> opList = Arrays.asList(message("RowEachLine"), message("SetAsColumnVector"), message("SetAsRowVector"),
                message("SetColumnsNumber"), message("IdentifyMatrix"),
                message("RandomMatrix"), message("RandomSquareMatrix")
        );
        editBox.getItems().addAll(opList);
        editBox.setVisibleRowCount(opList.size());
        editBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                checkEditBox(false);
            }
        });
        editBox.getSelectionModel().select(0);

        // Have not found way to set line breaks in PromptText of TextArea. All metheds in stackoverflow do not work :(
        init = true;
        editArea.setStyle(" -fx-text-fill: gray;");
        editArea.setText(AppVaribles.message("MatrixInputComments"));
        editArea.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (init) {
                    editArea.clear();
                    init = false;
                }
            }
        });
        editArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                validateEdit(false);
            }
        });

        editOpBox.getChildren().removeAll(editInput1, editInput2);
        editInput1.textProperty().addListener(new editInput1Listener());
        editInput2.textProperty().addListener(new editInput2Listener());

    }

    private void checkEditBox(boolean update) {
        if (isSettingValues) {
            return;
        }
        editInput1.setStyle(null);
        editInput2.setStyle(null);
        editOpBox.getChildren().removeAll(editInput1, editInput2);
        editArea.setStyle(null);
        editOkButton.disableProperty().unbind();
        popInformation(message("InputDataParametersAndOk"));

        currentEdit = editBox.getSelectionModel().getSelectedItem();
        if (AppVaribles.message("IdentifyMatrix").equals(currentEdit)
                || message("RandomSquareMatrix").equals(currentEdit)) {
            editOkButton.disableProperty().bind(editInput1.styleProperty().isEqualTo(badStyle));

        } else if (message("RandomMatrix").equals(currentEdit)) {
            editOkButton.disableProperty().bind(editInput1.styleProperty().isEqualTo(badStyle)
                    .or(editInput2.styleProperty().isEqualTo(badStyle))
            );

        } else {
            editOkButton.disableProperty().bind(Bindings.isEmpty(editArea.textProperty())
                    .or(editArea.styleProperty().isEqualTo(badStyle))
                    .or(editInput1.styleProperty().isEqualTo(badStyle))
            );
        }

        if (AppVaribles.message("SetAsColumnVector").equals(currentEdit)) {
            columnVector();

        } else if (AppVaribles.message("SetAsRowVector").equals(currentEdit)) {
            rowVector();

        } else if (AppVaribles.message("RandomMatrix").equals(currentEdit)) {
            editOpBox.getChildren().add(1, editInput1);
            editInput1.setPrefWidth(100);
            editInput1.clear();
            editInput1.setPromptText(AppVaribles.message("RowsNumber"));
            FxmlControl.setTooltip(editInput1, new Tooltip(AppVaribles.message("RowsNumber")));
            int m = makeNumber(9);
            editInput1.setText(m + "");

            editOpBox.getChildren().add(2, editInput2);
            editInput2.setPrefWidth(100);
            editInput2.clear();
            editInput2.setPromptText(AppVaribles.message("ColumnsNumber"));
            FxmlControl.setTooltip(editInput1, new Tooltip(AppVaribles.message("ColumnsNumber")));
            int n = makeNumber(9);
            editInput2.setText(n + "");

        } else if (AppVaribles.message("RandomSquareMatrix").equals(currentEdit)) {
            editOpBox.getChildren().add(1, editInput1);
            editInput1.setPrefWidth(100);
            editInput1.clear();
            editInput1.setPromptText(AppVaribles.message("PositiveInteger"));
            FxmlControl.setTooltip(editInput1, new Tooltip(AppVaribles.message("PositiveInteger")));

            int n = makeNumber(9);
            editInput1.setText(n + "");

        } else if (AppVaribles.message("SetColumnsNumber").equals(currentEdit)
                || message("IdentifyMatrix").equals(currentEdit)) {
            editOpBox.getChildren().add(1, editInput1);
            editInput1.setPrefWidth(100);
            editInput1.clear();
            editInput1.setPromptText(AppVaribles.message("PositiveInteger"));
            FxmlControl.setTooltip(editInput1, new Tooltip(AppVaribles.message("PositiveInteger")));

            editInput1.setStyle(badStyle);

        } else {
            validateEdit(update);
        }

    }

    private int makeNumber(int max) {
        int n = 0, count = 0;
        while (n <= 0 && count < 20) {
            n = new Random().nextInt(max);
            count++;
        }
        if (n == 0) {
            n = 5;
        }
        return n;
    }

    public List<Double> readEditValues() {
        String s = editArea.getText();
        s = s.replaceAll(matrixIgnoreChars + "|\n", " ");
        s = s.trim();
        if (s.isEmpty()) {
            editArea.setStyle(badStyle);
            return null;
        }
        String[] values = s.split("\\s+");
        List<Double> dList = new ArrayList();
        for (String v : values) {
            try {
                double d = Double.parseDouble(v);
                dList.add(d);
            } catch (Exception e) {
                editArea.setStyle(badStyle);
                return null;
            }
        }

        if (dList.isEmpty()) {
            editArea.setStyle(badStyle);
            return null;
        }
        return dList;
    }

    public void updateEditArea(final String s) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                isSettingValues = true;
                editArea.setText(s);
                isSettingValues = false;
                init = false;
            }
        });

    }

    public class editInput1Listener implements ChangeListener<String> {

        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if (currentEdit == null || !editOpBox.getChildren().contains(editInput1)) {
                return;
            }
            if (AppVaribles.message("RandomMatrix").equals(currentEdit)) {
                random();
            }
            int intValue;
            try {
                intValue = Integer.parseInt(editInput1.getText());
                if (intValue <= 0) {
                    editInput1.setStyle(badStyle);
                    return;
                } else {
                    editInput1.setStyle(null);
                }
            } catch (Exception e) {
                editInput1.setStyle(badStyle);
                return;
            }
            if (AppVaribles.message("SetColumnsNumber").equals(currentEdit)) {
                columnsNumber(intValue);
            } else if (AppVaribles.message("IdentifyMatrix").equals(currentEdit)) {
                identify(intValue);
            } else if (AppVaribles.message("RandomSquareMatrix").equals(currentEdit)) {
                random(intValue);
            }
        }

    }

    public class editInput2Listener implements ChangeListener<String> {

        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if (currentEdit == null || !editOpBox.getChildren().contains(editInput2)) {
                return;
            }
            if (AppVaribles.message("RandomMatrix").equals(currentEdit)) {
                random();
            }
        }

    }

    public void columnVector() {
        List<Double> dList = readEditValues();
        if (dList == null || dList.isEmpty()) {
            editArea.setStyle(badStyle);
            return;
        }
        String s = "";
        for (int i = 0; i < dList.size(); i++) {
            s += dList.get(i) + "\n";
        }
        editArea.setStyle(null);
        updateEditArea(s);
    }

    public void rowVector() {
        List<Double> dList = readEditValues();
        if (dList == null || dList.isEmpty()) {
            editArea.setStyle(badStyle);
            return;
        }
        String s = "";
        for (int i = 0; i < dList.size(); i++) {
            s += dList.get(i) + "\t";
        }
        editArea.setStyle(null);
        updateEditArea(s);
    }

    private void columnsNumber(int columns) {
        if (columns <= 0) {
            return;
        }
        List<Double> dList = readEditValues();
        if (dList == null || dList.isEmpty()) {
            editArea.setStyle(badStyle);
            return;
        }
        String s = "";
        boolean f = false;
        int rows = dList.size() / columns;
        if (dList.size() % columns == 0) {
            editArea.setStyle(null);
        } else {
            rows++;
            editArea.setStyle(badStyle);
        }
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                int index = i * columns + j;
                if (index >= dList.size()) {
                    f = true;
                    break;
                }
                s += dList.get(index) + "\t";
            }
            if (f) {
                break;
            }
            s += "\n";
        }
        updateEditArea(s);
    }

    private void identify(int n) {
        if (n <= 0) {
            return;
        }
        double[][] m = MatrixTools.identityDouble(n);
        String s = "";
        int rows = m.length, columns = m[0].length;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                double d = DoubleTools.scale(m[i][j], scale);
                s += d + "\t";
            }
            s += "\n";
        }
        updateEditArea(s);
    }

    private void random(int n) {
        if (n <= 0) {
            return;
        }
        double[][] m = MatrixTools.randomMatrix(n);
        String s = "";
        int rows = m.length, columns = m[0].length;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                double d = DoubleTools.scale(m[i][j], scale);
                s += d + "\t";
            }
            s += "\n";
        }
        updateEditArea(s);
    }

    private void random() {
        int m;
        try {
            m = Integer.parseInt(editInput1.getText());
            if (m <= 0) {
                editInput1.setStyle(badStyle);
                return;
            } else {
                editInput1.setStyle(null);
            }
        } catch (Exception e) {
            editInput1.setStyle(badStyle);
            return;
        }
        int n;
        try {
            n = Integer.parseInt(editInput2.getText());
            if (n <= 0) {
                editInput2.setStyle(badStyle);
                return;
            } else {
                editInput2.setStyle(null);
            }
        } catch (Exception e) {
            editInput2.setStyle(badStyle);
            return;
        }
        double[][] mx = MatrixTools.randomMatrix(m, n);
        String s = "";
        int rows = mx.length, columns = mx[0].length;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                double d = DoubleTools.scale(mx[i][j], scale);
                s += d + "\t";
            }
            s += "\n";
        }
        updateEditArea(s);
    }

    private void validateEdit(boolean update) {
        String s = editArea.getText();
        String[] lines = s.split("\n");
        int width = 0;
        StringBuilder ss = new StringBuilder();
        for (String line : lines) {
            line = line.replaceAll(matrixIgnoreChars, " ");
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] values = line.split("\\s+");
            int size = 0;
            for (String v : values) {
                try {
                    double d = Double.parseDouble(v);
                    size++;
                    if (update) {
                        d = DoubleTools.scale(d, scale);
                        ss.append(StringTools.fillRightBlank(d, scale + 4)).append("   ");
                    }
                } catch (Exception e) {
                    editArea.setStyle(badStyle);
                    return;
                }
            }
            if (width == 0) {
                width = size;
            } else if (width != size) {
                editArea.setStyle(badStyle);
                return;
            }
            if (update) {
                ss.append("\n");
            }
        }
        if (width == 0) {
            editArea.setStyle(badStyle);
        } else {
            editArea.setStyle(null);
            if (update) {
                updateEditArea(ss.toString());
            }
        }

    }

    @FXML
    public void editOKAction() {
        makeMatrix(editArea);
        if (matrix != null) {
            bottomLabel.setText("");
            setMatrix();
        }
    }

    @FXML
    public void clearAction() {
        isSettingValues = true;
        editArea.clear();
        init = true;
        editArea.setStyle(" -fx-text-fill: gray;");
        editArea.setText(AppVaribles.message("MatrixInputComments"));
        isSettingValues = false;
    }

    @FXML
    public void refreshAction() {
        init = false;
        checkEditBox(true);
    }

    /*
        Shared methods
     */
    public double[][] makeMatrix(TextArea area) {
        String s = area.getText();
        String[] lines = s.split("\n");
        int col = 0;
        List< List<Double>> m = new ArrayList();
        for (String line : lines) {
            line = line.replaceAll(matrixIgnoreChars, " ");
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] values = line.split("\\s+");
            List<Double> ds = new ArrayList();
            for (String v : values) {
                try {
                    double d = Double.parseDouble(v);
                    ds.add(d);
                } catch (Exception e) {
                    area.setStyle(badStyle);
                    return null;
                }
            }
            if (col == 0) {
                col = ds.size();
            } else if (col != ds.size()) {
                area.setStyle(badStyle);
                return null;
            }
            m.add(ds);
        }
        if (col == 0) {
            area.setStyle(badStyle);
            return null;
        } else {
            area.setStyle(null);
        }

        matrix = new double[m.size()][col];
        for (int i = 0; i < m.size(); i++) {
            List<Double> ds = m.get(i);
            for (int j = 0; j < col; j++) {
                matrix[i][j] = DoubleTools.scale(ds.get(j), scale);
            }
        }
        return matrix;
    }

    public void setMatrix() {
        startTime = -1;
        writeMatrix(false);
    }

    /*
        Value tab
     */
    public void initValuePane() {
        if (valuePane != null) {
            valuePane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (valuePane.isExpanded()) {
                        if (!valuePBox.getChildren().contains(valueVBox)) {
                            valuePBox.getChildren().add(valueVBox);
                        }
                        VBox.setVgrow(valueVBox, Priority.ALWAYS);
                        valueVBox.setPrefHeight(thisPane.getHeight());
                    } else {
                        valuePBox.getChildren().remove(valueVBox);
                        editPane.setExpanded(true);
                        editVBox.setPrefHeight(thisPane.getHeight());
                    }
                }
            });
        }

        valueBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                checkValueBox();
            }
        });

        valueInput1.textProperty().addListener(new valueInput1Listener());
        valueInput2.textProperty().addListener(new valueInput2Listener());
        valueOpBox.getChildren().removeAll(valueInput1, valueInput2);

        calculateButton.disableProperty().bind(
                Bindings.isEmpty(valueArea.textProperty())
                        .or(valueArea.styleProperty().isEqualTo(badStyle))
                        .or(valueInput1.styleProperty().isEqualTo(badStyle))
                        .or(valueInput2.styleProperty().isEqualTo(badStyle))
                        .or(valueBox.getSelectionModel().selectedItemProperty().isNull())
        );

        if (copyAsAButton != null) {
            copyAsAButton.disableProperty().bind(Bindings.isEmpty(valueArea.textProperty())
                    .or(valueArea.styleProperty().isEqualTo(badStyle))
            );
            copyAsBButton.disableProperty().bind(Bindings.isEmpty(valueArea.textProperty())
                    .or(valueArea.styleProperty().isEqualTo(badStyle))
            );
        }

        if (editButton != null) {
            editButton.disableProperty().bind(Bindings.isEmpty(valueArea.textProperty())
                    .or(valueArea.styleProperty().isEqualTo(badStyle))
            );
        }
    }

    private void checkValueBox() {
        if (isSettingValues) {
            return;
        }
        valueInput1.setStyle(null);
        valueInput2.setStyle(null);
        valueOpBox.getChildren().removeAll(valueInput1, valueInput2);
        valueArea.setStyle(null);
        popInformation(message("InputParametersAndCalculate"));

        currentCalculation = valueBox.getSelectionModel().getSelectedItem();

        if (AppVaribles.message("SetDecimalScale").equals(currentCalculation)
                || message("MultiplyNumber").equals(currentCalculation)
                || message("DivideNumber").equals(currentCalculation)
                || message("Power").equals(currentCalculation)
                || message("ComplementMinor").equals(currentCalculation)) {
            valueOpBox.getChildren().add(1, valueInput1);
            valueInput1.setPrefWidth(100);
            valueInput1.clear();
            valueInput1.setPromptText(null);
            FxmlControl.removeTooltip(valueInput1);
            valueInput1.setStyle(badStyle);

            String m = null;
            if (AppVaribles.message("SetDecimalScale").equals(currentCalculation)
                    || message("Power").equals(currentCalculation)) {
                m = message("PositiveInteger");

            } else if (AppVaribles.message("DivideNumber").equals(currentCalculation)) {
                m = message("Nonzero");

            } else if (AppVaribles.message("ComplementMinor").equals(currentCalculation)) {
                m = message("Row");

            }
            if (m != null) {
                valueInput1.setPromptText(m);
                FxmlControl.setTooltip(valueInput1, new Tooltip(m));
            }
        }

        if (AppVaribles.message("ComplementMinor").equals(currentCalculation)) {
            valueOpBox.getChildren().add(2, valueInput2);
            valueInput2.setPrefWidth(100);
            valueInput2.clear();
            valueInput2.setStyle(badStyle);
            valueInput2.setPromptText(AppVaribles.message("Column"));
            FxmlControl.setTooltip(valueInput2, new Tooltip(AppVaribles.message("Column")));
        }

    }

    public class valueInput1Listener implements ChangeListener<String> {

        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            currentCalculation = valueBox.getSelectionModel().getSelectedItem();
            if (currentCalculation == null) {
                return;
            }
            if (AppVaribles.message("SetDecimalScale").equals(currentCalculation)) {
                checkScale();
            } else if (AppVaribles.message("Power").equals(currentCalculation)) {
                checkPower();
            } else if (AppVaribles.message("DivideNumber").equals(currentCalculation)) {
                checkDivide();
            } else if (AppVaribles.message("MultiplyNumber").equals(currentCalculation)) {
                checkmultiply();
            } else if (AppVaribles.message("ComplementMinor").equals(currentCalculation)) {
                checkComplementMinor1();
            }
        }

    }

    public class valueInput2Listener implements ChangeListener<String> {

        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            currentCalculation = valueBox.getSelectionModel().getSelectedItem();
            if (currentCalculation == null) {
                return;
            }
            if (AppVaribles.message("ComplementMinor").equals(currentCalculation)) {
                checkComplementMinor2();
            }
        }

    }

    public void writeMatrix() {
        writeMatrix(false);
    }

    public void writeMatrix(double value) {
        matrix = new double[1][1];
        matrix[0][0] = value;
        writeMatrix();
    }

    public void writeMatrix(final boolean isInteger) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (matrix == null) {
                    popError(AppVaribles.message("InvalidData"));
                    bottomLabel.setText(AppVaribles.message("Failed") + ":" + currentCalculation + "  "
                            + message("InvalidData"));
                    startTime = -1;
                    return;
                }
                String s = "";
                int rows = matrix.length, columns = matrix[0].length;
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < columns; j++) {
                        if (isInteger) {
                            matrix[i][j] = (int) Math.round(matrix[i][j]);
                        } else {
                            matrix[i][j] = DoubleTools.scale(matrix[i][j], scale);
                        }
                        s += StringTools.fillRightBlank(matrix[i][j], scale + 4) + "   ";
                    }
                    s += "\n";
                }
                valueArea.setStyle(null);
                valueArea.setText(s);
                if (startTime > 0) {
                    long cost = new Date().getTime() - startTime;
                    bottomLabel.setText(AppVaribles.message("Successful") + ":" + currentCalculation + "  "
                            + message("Cost") + ":" + cost + " ms");
                    startTime = -1;
                }

                List<String> opList;
                if (rows == columns) {
                    opList = Arrays.asList(message("Transpose"), message("RowEchelonForm"), message("ReducedRowEchelonForm"),
                            message("DeterminantByElimination"), message("DeterminantByComplementMinor"),
                            message("InverseMatrixByElimination"), message("InverseMatrixByAdjoint"),
                            message("MatrixRank"),
                            message("AdjointMatrix"), message("ComplementMinor"),
                            message("Normalize"),
                            message("SetDecimalScale"), message("SetAsInteger"), message("MultiplyNumber"),
                            message("DivideNumber"), message("Power")
                    );
                } else {
                    opList = Arrays.asList(message("Transpose"), message("RowEchelonForm"), message("ReducedRowEchelonForm"),
                            message("ComplementMinor"), message("Normalize"),
                            message("SetDecimalScale"), message("SetAsInteger"), message("MultiplyNumber"),
                            message("DivideNumber")
                    );
                }
                isSettingValues = true;
                valueBox.getItems().clear();
                valueBox.getItems().addAll(opList);
                valueBox.setVisibleRowCount(opList.size());
                isSettingValues = false;
                if (valueBox.getItems().contains(currentCalculation)) {
                    valueBox.getSelectionModel().select(currentCalculation);
                } else {
                    currentCalculation = null;
                }

            }
        });

    }

    private void transpose() {
        if (matrix == null) {
            return;
        }
        matrix = MatrixTools.transpose(matrix);
        writeMatrix();
    }

    private void normalize() {
        if (matrix == null) {
            return;
        }
        matrix = MatrixTools.normalize(matrix);
        writeMatrix();
    }

    private void integer() {
        if (matrix == null) {
            return;
        }
        matrix = MatrixTools.integer(matrix);
        writeMatrix(true);

    }

    private int checkScale() {
        int p = -1;
        try {
            p = Integer.parseInt(valueInput1.getText());
            if (p < 0 || p > 16) {
                valueInput1.setStyle(badStyle);
            } else {
                valueInput1.setStyle(null);
            }
        } catch (Exception e) {
            valueInput1.setStyle(badStyle);
        }
        return p;
    }

    private void scale() {
        int p = checkScale();
        if (valueInput1.getStyleClass().contains(badStyle)) {
            return;
        }
        matrix = MatrixTools.scale(matrix, p);
        writeMatrix();
    }

    private double checkmultiply() {
        double n = -1;
        try {
            n = Double.parseDouble(valueInput1.getText());
            valueInput1.setStyle(null);
        } catch (Exception e) {
            valueInput1.setStyle(badStyle);
        }
        return n;
    }

    private void multiply() {
        double d = checkmultiply();
        if (valueInput1.getStyleClass().contains(badStyle)) {
            return;
        }
        matrix = MatrixTools.multiply(matrix, d);
        writeMatrix();
    }

    private double checkDivide() {
        double n = -1;
        try {
            n = Double.parseDouble(valueInput1.getText());
            if (n == 0) {
                valueInput1.setStyle(badStyle);
            } else {
                valueInput1.setStyle(null);
            }
        } catch (Exception e) {
            valueInput1.setStyle(badStyle);
        }
        return n;
    }

    private void divide() {
        double d = checkDivide();
        if (valueInput1.getStyleClass().contains(badStyle)) {
            return;
        }
        matrix = MatrixTools.divide(matrix, d);
        writeMatrix();
    }

    private int checkPower() {
        int n = -1;
        try {
            n = Integer.parseInt(valueInput1.getText());
            if (n <= 0) {
                valueInput1.setStyle(badStyle);
            } else {
                valueInput1.setStyle(null);
            }
        } catch (Exception e) {
            valueInput1.setStyle(badStyle);
        }
        return n;
    }

    private void power() {
        int n = checkPower();
        if (valueInput1.getStyleClass().contains(badStyle)) {
            return;
        }
        matrix = MatrixTools.power(matrix, n);
        writeMatrix();
    }

    private void inverseByAdjoint() {
        matrix = MatrixTools.inverseByAdjoint(matrix);
        writeMatrix();
    }

    private void inverseByElimination() {
        matrix = MatrixTools.inverseByElimination(matrix);
        writeMatrix();
    }

    private void adjoint() {
        matrix = MatrixTools.adjoint(matrix);
        writeMatrix();
    }

    private void determinantByComplementMinor() {
        try {
            double det = MatrixTools.determinantByComplementMinor(matrix);
            writeMatrix(det);
        } catch (Exception e) {
            matrix = null;
            writeMatrix();
        }
    }

    private void determinantByElimination() {
        try {
            double det = MatrixTools.determinantByElimination(matrix);
            writeMatrix(det);
        } catch (Exception e) {
            matrix = null;
            writeMatrix();
        }
    }

    private int checkComplementMinor1() {
        int n;
        try {
            n = Integer.parseInt(valueInput1.getText());
            if (n <= 0 || n > matrix.length) {
                valueInput1.setStyle(badStyle);
                n = -1;
            } else {
                valueInput1.setStyle(null);
            }
        } catch (Exception e) {
            valueInput1.setStyle(badStyle);
            n = -1;
        }
        return n;
    }

    private int checkComplementMinor2() {
        int n;
        try {
            n = Integer.parseInt(valueInput2.getText());
            if (n <= 0 || n > matrix[0].length) {
                valueInput2.setStyle(badStyle);
                n = -1;
            } else {
                valueInput2.setStyle(null);
            }
        } catch (Exception e) {
            n = -1;
            valueInput2.setStyle(badStyle);
        }
        return n;
    }

    private void complementMinor() {
        int i = checkComplementMinor1();
        if (i <= 0 || valueInput1.getStyleClass().contains(badStyle)) {
            return;
        }
        int j = checkComplementMinor2();
        if (j <= 0 || valueInput2.getStyleClass().contains(badStyle)) {
            return;
        }
        matrix = MatrixTools.complementMinor(matrix, i - 1, j - 1);
        writeMatrix();
    }

    private void rowEchelonForm() {
        matrix = MatrixTools.rowEchelonForm(matrix);
        writeMatrix();
    }

    private void reducedRowEchelonForm() {
        matrix = MatrixTools.reducedRowEchelonForm(matrix);
        writeMatrix();
    }

    private void rank() {
        try {
            double det = MatrixTools.rank(matrix);
            writeMatrix(det);
        } catch (Exception e) {
            matrix = null;
            writeMatrix();
        }
    }

    @FXML
    public void calculateAction() {
        if (matrix == null) {
            makeMatrix(valueArea);
            if (matrix == null) {
                popError(AppVaribles.message("InvalidData"));
                bottomLabel.setText(AppVaribles.message("Failed") + ":" + currentCalculation + "  "
                        + message("InvalidData"));
                startTime = -1;
                return;
            }
        }
        startTime = new Date().getTime();
        currentCalculation = valueBox.getSelectionModel().getSelectedItem();
        if (task != null && task.isRunning()) {
            task.cancel();
        }
        task = new Task<Void>() {
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                if (AppVaribles.message("Transpose").equals(currentCalculation)) {
                    transpose();

                } else if (AppVaribles.message("SetAsInteger").equals(currentCalculation)) {
                    integer();

                } else if (AppVaribles.message("SetDecimalScale").equals(currentCalculation)) {
                    scale();

                } else if (AppVaribles.message("Normalize").equals(currentCalculation)) {
                    normalize();

                } else if (AppVaribles.message("MultiplyNumber").equals(currentCalculation)) {
                    multiply();

                } else if (AppVaribles.message("DivideNumber").equals(currentCalculation)) {
                    divide();

                } else if (AppVaribles.message("Power").equals(currentCalculation)) {
                    power();

                } else if (AppVaribles.message("AdjointMatrix").equals(currentCalculation)) {
                    adjoint();

                } else if (AppVaribles.message("DeterminantByComplementMinor").equals(currentCalculation)) {
                    determinantByComplementMinor();

                } else if (AppVaribles.message("DeterminantByElimination").equals(currentCalculation)) {
                    determinantByElimination();

                } else if (AppVaribles.message("InverseMatrixByAdjoint").equals(currentCalculation)) {
                    inverseByAdjoint();

                } else if (AppVaribles.message("InverseMatrixByElimination").equals(currentCalculation)) {
                    inverseByElimination();

                } else if (AppVaribles.message("ComplementMinor").equals(currentCalculation)) {
                    complementMinor();

                } else if (AppVaribles.message("RowEchelonForm").equals(currentCalculation)) {
                    rowEchelonForm();

                } else if (AppVaribles.message("ReducedRowEchelonForm").equals(currentCalculation)) {
                    reducedRowEchelonForm();

                } else if (AppVaribles.message("MatrixRank").equals(currentCalculation)) {
                    rank();
                }
                return null;
            }

        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    @FXML
    public void copyAsAAction() {
        MatricesCalculationController p = (MatricesCalculationController) parentController;
        p.copyAsAAction();
    }

    @FXML
    public void editAction() {
        if (valueArea.getText().isEmpty()) {
            return;
        }
        editArea.setText(valueArea.getText());
        init = false;
    }

    @FXML
    public void copyAsBAction() {
        MatricesCalculationController p = (MatricesCalculationController) parentController;
        p.copyAsBAction();
    }

}
