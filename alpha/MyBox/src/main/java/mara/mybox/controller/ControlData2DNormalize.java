package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleMatrixTools;
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
    protected RadioButton columnsRadio, rowsRadio, matrixRadio, l1Radio, l2Radio, minmaxRadio;
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

    public double[][] calculate(double[][] matrix) {
        try {
            if (matrix == null || matrix.length == 0) {
                return matrix;
            }
            if (columnsRadio.isSelected()) {
                return columnsNormalize(matrix);
            } else if (rowsRadio.isSelected()) {
                return rowsNormalize(matrix);
            } else if (matrixRadio.isSelected()) {
                return matrixsNormalize(matrix);
            }
            return null;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public double[][] columnsNormalize(double[][] matrix) {
        try {
            if (matrix == null || matrix.length == 0) {
                return matrix;
            }
            double[][] result = DoubleMatrixTools.transpose(matrix);
            result = rowsNormalize(result);
            result = DoubleMatrixTools.transpose(result);
            return result;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public double[][] rowsNormalize(double[][] matrix) {
        try {
            if (matrix == null || matrix.length == 0) {
                return matrix;
            }
            int rlen = matrix.length, clen = matrix[0].length;
            double[][] result = new double[rlen][clen];
            if (minmaxRadio.isSelected()) {
                for (int i = 0; i < rlen; i++) {
                    result[i] = minMax(matrix[i]);
                }
            } else if (l1Radio.isSelected()) {
                for (int i = 0; i < rlen; i++) {
                    result[i] = sum(matrix[i]);
                }
            } else if (l2Radio.isSelected()) {
                for (int i = 0; i < rlen; i++) {
                    result[i] = zscore(matrix[i]);
                }
            } else {
                return null;
            }
            return result;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public double[][] matrixsNormalize(double[][] matrix) {
        try {
            if (matrix == null || matrix.length == 0) {
                return matrix;
            }
            int w = matrix[0].length;
            double[] vector = DoubleMatrixTools.matrix2Array(matrix);
            if (minmaxRadio.isSelected()) {
                vector = minMax(vector);
            } else if (l1Radio.isSelected()) {
                vector = sum(vector);
            } else if (l2Radio.isSelected()) {
                vector = zscore(vector);
            } else {
                return null;
            }
            double[][] result = DoubleMatrixTools.array2Matrix(vector, w);
            return result;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public double[] minMax(double[] vector) {
        try {
            if (vector == null) {
                return vector;
            }
            int len = vector.length;
            if (len == 0) {
                return vector;
            }
            double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
            for (double d : vector) {
                if (d > max) {
                    max = d;
                }
                if (d < min) {
                    min = d;
                }
            }
            double k = max - min;
            if (k == 0) {
                k = Float.MIN_VALUE;
            }
            k = (to - from) / k;
            double[] result = new double[len];
            for (int i = 0; i < len; i++) {
                result[i] = from + k * (vector[i] - min);
            }
            return result;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public double[] sum(double[] vector) {
        try {
            if (vector == null) {
                return vector;
            }
            int len = vector.length;
            if (len == 0) {
                return vector;
            }
            double sum = 0;
            for (double d : vector) {
                sum += Math.abs(d);
            }
            if (sum == 0) {
                sum = Float.MIN_VALUE;
            }
            double[] result = new double[len];
            for (int i = 0; i < len; i++) {
                result[i] = vector[i] / sum;
            }
            return result;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public double[] zscore(double[] vector) {
        try {
            if (vector == null) {
                return vector;
            }
            int len = vector.length;
            if (len == 0) {
                return vector;
            }
            double sum = 0;
            for (double d : vector) {
                sum += d;
            }
            double mean = sum / len;
            double variance = 0;
            for (double d : vector) {
                variance += Math.pow(d - mean, 2);
            }
            variance = Math.sqrt(variance / len);
            if (variance == 0) {
                variance = Float.MIN_VALUE;
            }
            double[] result = new double[len];
            for (int i = 0; i < len; i++) {
                result[i] = (vector[i] - mean) / variance;
            }
            return result;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
