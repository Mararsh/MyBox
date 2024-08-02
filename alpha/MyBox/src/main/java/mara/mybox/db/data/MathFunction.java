package mara.mybox.db.data;

import java.util.List;

/**
 * @Author Mara
 * @CreateDate 2024-8-2
 * @License Apache License Version 2.0
 */
public class MathFunction extends TreeNode {

    protected long funcid;
    protected String name, expression, domain;
    protected List<String> variables;

    public MathFunction() {
        tableName = "Math_Function";
        idName = "funcid";
    }

    @Override
    public boolean valid() {
        return name != null && !name.isBlank()
                && expression != null && !expression.isBlank();
    }

    /*
        Static methods
     */
    public static MathFunction create() {
        return new MathFunction();
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

    public List<String> getVariables() {
        return variables;
    }

    public void setVariables(List<String> variables) {
        this.variables = variables;
    }

}
