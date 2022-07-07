package mara.mybox.data;

import mara.mybox.fxml.ExpressionCalculator;

/**
 * @Author Mara
 * @CreateDate 2022-7-7
 * @License Apache License Version 2.0
 */
public class ColumnFilter {

    protected boolean empty, zero, negative, positive, q3, e3, e4, q1, e2, e1, filterReversed;
    protected double q3value, e3value, e4value, q1value, e2value, e1value;
    protected String script;
    public ExpressionCalculator expressionCalculator;

}
