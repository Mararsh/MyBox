package mara.mybox.db.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2024-8-2
 * @License Apache License Version 2.0
 */
public class MathFunction extends BaseData {

    protected long funcid;
    protected String name, expression, domain, variables;

    @Override

    public boolean valid() {
        return valid(this);
    }

    @Override
    public boolean setValue(String column, Object value) {
        return setValue(this, column, value);
    }

    @Override
    public Object getValue(String column) {
        return getValue(this, column);
    }

    @Override
    public String values() {
        try {
            return columnValues.toString();
        } catch (Exception e) {
//            MyBoxLog.debug(e);
            return null;
        }
    }

    /*
        Static methods
     */
    public static MathFunction create() {
        return new MathFunction();
    }

    public static boolean setValue(MathFunction data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "funcid":
                    data.setFuncid(value == null ? -1 : (long) value);
                    return true;
                case "name":
                    data.setName(value == null ? null : (String) value);
                    return true;
                case "variables":
                    data.setVariables(value == null ? null : (String) value);
                    return true;
                case "expression":
                    data.setExpression(value == null ? null : (String) value);
                    return true;
                case "domain":
                    data.setDomain(value == null ? null : (String) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    public static Object getValue(MathFunction data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "funcid":
                return data.getFuncid();
            case "name":
                return data.getName();
            case "variables":
                return data.getVariables();
            case "expression":
                return data.getExpression();
            case "domain":
                return data.getDomain();
        }
        return null;
    }

    public static boolean valid(MathFunction data) {
        return data != null
                && data.getName() != null && !data.getName().isBlank();
    }


    /*
        get/set
     */
    public long getFuncid() {
        return funcid;
    }

    public MathFunction setFuncid(long funcid) {
        this.funcid = funcid;
        return this;
    }

    public String getName() {
        return name;
    }

    public MathFunction setName(String name) {
        this.name = name;
        return this;
    }

    public String getExpression() {
        return expression;
    }

    public MathFunction setExpression(String expression) {
        this.expression = expression;
        return this;
    }

    public String getDomain() {
        return domain;
    }

    public MathFunction setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public String getVariables() {
        return variables;
    }

    public MathFunction setVariables(String variables) {
        this.variables = variables;
        return this;
    }

}
