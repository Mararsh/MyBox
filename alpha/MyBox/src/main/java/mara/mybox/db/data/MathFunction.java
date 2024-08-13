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
                && data.getName() != null && !data.getName().isBlank()
                && data.getExpression() != null && !data.getExpression().isBlank();
    }


    /*
        get/set
     */
    public long getFuncid() {
        return funcid;
    }

    public void setFuncid(long funcid) {
        this.funcid = funcid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getVariables() {
        return variables;
    }

    public void setVariables(String variables) {
        this.variables = variables;
    }

}
