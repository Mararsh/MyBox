package mara.mybox.calculation;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

/**
 * @Author Mara
 * @CreateDate 2022-8-18
 * @License Apache License Version 2.0
 */
public class OLSLinearRegression extends OLSMultipleLinearRegression {

    protected String yName;
    protected List<String> xNames;
    protected int n, k;
    protected int scale = 8;
    protected double invalidAs, intercept;
    protected double[][] x;
    protected double[] y, coefficients;
    protected SingletonTask<Void> task;

    public OLSLinearRegression(boolean includeIntercept) {
        super();
        this.setNoIntercept(!includeIntercept);
    }

    public boolean calculate(List<List<String>> data) {
        try {
            n = data.size();
            k = xNames.size();
            y = new double[n];
            x = new double[n][k];
            Normalization normalization = Normalization.create()
                    .setA(Normalization.Algorithm.ZScore)
                    .setInvalidAs(invalidAs);
            for (int i = 0; i < n; i++) {
                List<String> row = data.get(i);
                y[i] = DoubleTools.toDouble(row.get(0), invalidAs);
                for (int j = 0; j < k; j++) {
                    x[i][j] = DoubleTools.toDouble(row.get(j + 1), invalidAs);
                }
            }
            y = normalization.setSourceVector(y).calculate();
            x = normalization.setSourceMatrix(x).columnsNormalize();
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
            columns.add(new Data2DColumn(message("RowNumber"), ColumnDefinition.ColumnType.Long));
            columns.add(new Data2DColumn(message("DependentVariable") + "_" + yName, ColumnDefinition.ColumnType.Double));
            for (String name : xNames) {
                columns.add(new Data2DColumn(message("IndependentVariable") + "_" + name, ColumnDefinition.ColumnType.Double));
                columns.add(new Data2DColumn(message("Coefficient") + "_" + name, ColumnDefinition.ColumnType.Double));
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
            double[] beta = estimateRegressionParameters();
            double[] residuals = estimateResiduals();
            for (int i = 0; i < x.length; i++) {
                List<String> row = new ArrayList<>();
                row.add("" + (i + 1));
                row.add(y[i] + "");
                for (int j = 0; j < x[i].length; j++) {
                    row.add(x[i][j] + "");
                    double coef = isNoIntercept() ? beta[j] : beta[j + 1];
                    row.add(coef + "");
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

    public double scaledIntercept() {
        return DoubleTools.scale(intercept, scale);
    }

    public double[] scaledCoefficients() {
        if (coefficients == null) {
            return null;
        }
        double[] scaled = new double[coefficients.length];
        for (int i = 0; i < coefficients.length; i++) {
            scaled[i] = DoubleTools.scale(coefficients[i], scale);
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

    public OLSLinearRegression setInvalidAs(double invalidAs) {
        this.invalidAs = invalidAs;
        return this;
    }

    public OLSLinearRegression setTask(SingletonTask<Void> task) {
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

}
