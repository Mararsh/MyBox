package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import mara.mybox.calculation.Normalization;
import mara.mybox.calculation.Normalization.Algorithm;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.UserConfig;
import static mara.mybox.value.UserConfig.badStyle;

/**
 * @Author Mara
 * @CreateDate 2021-12-28
 * @License Apache License Version 2.0
 */
public class ControlData2DNormalize extends BaseController {

    protected double from, to;

    @FXML
    protected ToggleGroup objectGroup, algorithmGroup;
    @FXML
    protected RadioButton columnsRadio, rowsRadio, allRadio, sumRadio, zscoreRadio, minmaxRadio;
    @FXML
    protected TextField fromInput, toInput;
    @FXML
    protected FlowPane rangePane;
    @FXML
    protected Label rangeLabel;

    @Override
    public void initControls() {
        try {
            super.initControls();

            try {
                from = Double.parseDouble(UserConfig.getString(baseName + "From", "0"));
            } catch (Exception e) {
                from = 0;
            }
            try {
                to = Double.parseDouble(UserConfig.getString(baseName + "To", "1"));
            } catch (Exception e) {
                to = 1;
            }
            fromInput.setText(from + "");
            toInput.setText(to + "");
            fromInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        double d = Double.parseDouble(fromInput.getText());
                        if (d < to) {
                            from = d;
                            toInput.setStyle(null);
                            rangeLabel.setText("(" + from + "," + to + ")");
                            UserConfig.setString(baseName + "From", from + "");
                        } else {
                            toInput.setStyle(badStyle());
                        }
                    } catch (Exception e) {
                        fromInput.setStyle(badStyle());
                    }
                }
            });

            toInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        double d = Double.parseDouble(toInput.getText());
                        if (d > from) {
                            to = d;
                            toInput.setStyle(null);
                            rangeLabel.setText("(" + from + "," + to + ")");
                            UserConfig.setString(baseName + "To", to + "");
                        } else {
                            toInput.setStyle(badStyle());
                        }
                    } catch (Exception e) {
                        toInput.setStyle(badStyle());
                    }
                }
            });
            rangeLabel.setText("(" + from + "," + to + ")");

            rangePane.visibleProperty().bind(minmaxRadio.selectedProperty());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void range01() {
        fromInput.setText("0");
        toInput.setText("1");
    }

    @FXML
    public void range11() {
        fromInput.setText("-1");
        toInput.setText("1");
    }

    public double[][] calculateDoubles(double[][] matrix, InvalidAs invalidAs) {
        String[][] results = calculate(StringTools.toString(matrix), invalidAs);
        return DoubleTools.toDouble(results, invalidAs);
    }

    public String[][] calculate(String[][] matrix, InvalidAs invalidAs) {
        try {
            if (matrix == null || matrix.length == 0) {
                return matrix;
            }
            Normalization n = Normalization.create()
                    .setFrom(from).setTo(to)
                    .setInvalidAs(invalidAs)
                    .setSourceMatrix(matrix);
            if (sumRadio.isSelected()) {
                n.setA(Algorithm.Sum);
            } else if (zscoreRadio.isSelected()) {
                n.setA(Algorithm.ZScore);
            } else {
                n.setA(Algorithm.MinMax);
            }
            if (columnsRadio.isSelected()) {
                return n.columnsNormalize();
            } else if (rowsRadio.isSelected()) {
                return n.rowsNormalize();
            } else if (allRadio.isSelected()) {
                return n.allNormalize();
            }
            return null;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
