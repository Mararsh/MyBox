package mara.mybox.calculation;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

/**
 * @Author Mara
 * @CreateDate 2022-8-18
 * @License Apache License Version 2.0
 */
public class OLSLinearRegression extends OLSMultipleLinearRegression {

    public String yName;
    public List<String> xNames;
    public int n, k;
    public int scale = 8;
    public double intercept, rSqure, adjustedRSqure, standardError, variance;
    public double[][] x;
    public double[] y, coefficients;
    public InvalidAs invalidAs;
    protected FxTask<Void> task;

    public OLSLinearRegression(boolean includeIntercept) {
        super();
        this.setNoIntercept(!includeIntercept);
    }

    public boolean calculate(String[] sy, String[][] sx) {
        try {
            n = sy.length;
            k = sx[0].length;
            Normalization normalization = Normalization.create()
                    .setA(Normalization.Algorithm.ZScore)
                    .setInvalidAs(invalidAs);
            sy = normalization.setSourceVector(sy).calculate();
            sx = normalization.setSourceMatrix(sx).columnsNormalize();
            y = new double[n];
            x = new double[n][k];
            for (int i = 0; i < n; i++) {
                y[i] = DoubleTools.toDouble(sy[i], invalidAs);
            }
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < k; j++) {
                    x[i][j] = DoubleTools.toDouble(sx[i][j], invalidAs);
                }
            }
            newSampleData(y, x);
            double[] beta = estimateRegressionParameters();
            if (isNoIntercept()) {
                intercept = 0;
                coefficients = beta;
            } else {
                intercept = beta[0];
                coefficients = new double[beta.length - 1];
                for (int i = 1; i < beta.length; i++) {
                    coefficients[i - 1] = beta[i];
                }
            }
            rSqure = calculateRSquared();
            adjustedRSqure = calculateAdjustedRSquared();
            standardError = estimateRegressionStandardError();
            variance = estimateRegressandVariance();
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    public List<Data2DColumn> makeColumns() {
        try {
            List<Data2DColumn> columns = new ArrayList<>();
            columns.add(new Data2DColumn(message("Item"), ColumnDefinition.ColumnType.String));
            columns.add(new Data2DColumn(message("DependentVariable") + "_" + yName, ColumnDefinition.ColumnType.Double));
            for (String name : xNames) {
                columns.add(new Data2DColumn(message("IndependentVariable") + "_" + name, ColumnDefinition.ColumnType.Double));
            }
            columns.add(new Data2DColumn(message("Residual"), ColumnDefinition.ColumnType.Double));
            return columns;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            return null;
        }
    }

    public List<List<String>> makeRegressionData() {
        try {
            List<List<String>> data = new ArrayList<>();
            double[] residuals = estimateResiduals();
            List<String> row = new ArrayList<>();
            row.add(message("Coefficient"));
            row.add("");
            for (int j = 0; j < coefficients.length; j++) {
                row.add(coefficients[j] + "");
            }
            row.add("");
            data.add(row);
            for (int i = 0; i < x.length; i++) {
                row = new ArrayList<>();
                row.add("" + (i + 1));
                row.add(y[i] + "");
                for (int j = 0; j < x[i].length; j++) {
                    row.add(x[i][j] + "");
                }
                row.add(residuals[i] + "");
                data.add(row);
            }
            return data;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            return null;
        }
    }

    public double[] scaledCoefficients() {
        if (coefficients == null) {
            return null;
        }
        double[] scaled = new double[coefficients.length];
        for (int i = 0; i < coefficients.length; i++) {
            double d = coefficients[i];
            if (DoubleTools.invalidDouble(d)) {
                scaled[i] = Double.NaN;
            } else {
                scaled[i] = DoubleTools.scale(d, scale);
            }
        }
        return scaled;
    }

    /*
        set
     */
    public OLSLinearRegression setyName(String yName) {
        this.yName = yName;
        return this;
    }

    public OLSLinearRegression setxNames(List<String> xNames) {
        this.xNames = xNames;
        return this;
    }

    public OLSLinearRegression setScale(int scale) {
        this.scale = scale;
        return this;
    }

    public OLSLinearRegression setInvalidAs(InvalidAs invalidAs) {
        this.invalidAs = invalidAs;
        return this;
    }

    public OLSLinearRegression setTask(FxTask<Void> task) {
        this.task = task;
        return this;
    }

    /*
        get
     */
    public int getN() {
        return n;
    }

    public int getK() {
        return k;
    }

    public double[][] getXValues() {
        return x;
    }

    public double[] getYValues() {
        return y;
    }

    public double getIntercept() {
        return intercept;
    }

    public double[] getCoefficients() {
        return coefficients;
    }

    public double getrSqure() {
        return rSqure;
    }

    public double getAdjustedRSqure() {
        return adjustedRSqure;
    }

    public double getStandardError() {
        return standardError;
    }

    public double getVariance() {
        return variance;
    }

}
