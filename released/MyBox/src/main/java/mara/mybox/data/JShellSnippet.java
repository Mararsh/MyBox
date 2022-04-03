package mara.mybox.data;

import jdk.jshell.ErroneousSnippet;
import jdk.jshell.ExpressionSnippet;
import jdk.jshell.ImportSnippet;
import jdk.jshell.JShell;
import jdk.jshell.MethodSnippet;
import jdk.jshell.Snippet;
import jdk.jshell.VarSnippet;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-5
 * @License Apache License Version 2.0
 */
public class JShellSnippet {

    protected Snippet snippet;
    protected String id, name, type, subType, status, source, value, some1, some2;

    public JShellSnippet(JShell jShell, Snippet snippet) {
        if (jShell == null || snippet == null) {
            return;
        }
        this.snippet = snippet;
        id = snippet.id();
        type = snippet.kind().name();
        subType = snippet.subKind().name();
        switch (jShell.status(snippet)) {
            case DROPPED:
                status = message("Dropped");
                break;
            case NONEXISTENT:
                status = message("Nonexistent");
                break;
            case OVERWRITTEN:
                status = message("Overwritten");
                break;
            case RECOVERABLE_DEFINED:
            case RECOVERABLE_NOT_DEFINED:
                status = message("RecoverableUnresolved");
                break;
            case REJECTED:
                status = message("Rejected");
                break;
            case VALID:
                status = message("Valid");
                break;
        }
        source = snippet.source();
        if (snippet instanceof VarSnippet) {
            makeVar(jShell, (VarSnippet) snippet);
        } else if (snippet instanceof ExpressionSnippet) {
            makeExpression((ExpressionSnippet) snippet);
        } else if (snippet instanceof MethodSnippet) {
            makeMethod((MethodSnippet) snippet);
        } else if (snippet instanceof ImportSnippet) {
            makeImport((ImportSnippet) snippet);
        } else if (snippet instanceof ErroneousSnippet) {
            makeError((ErroneousSnippet) snippet);
        }
    }

    private void makeVar(JShell jShell, VarSnippet varSnippet) {
        if (jShell == null || varSnippet == null) {
            return;
        }
        name = varSnippet.name();
        some1 = varSnippet.typeName();
        value = jShell.varValue(varSnippet);
    }

    private void makeExpression(ExpressionSnippet expSnippet) {
        if (expSnippet == null) {
            return;
        }
        name = expSnippet.name();
        some1 = expSnippet.typeName();
    }

    private void makeMethod(MethodSnippet methodSnippet) {
        if (methodSnippet == null) {
            return;
        }
        name = methodSnippet.name();
        some1 = methodSnippet.parameterTypes();
        some2 = methodSnippet.signature();
    }

    private void makeImport(ImportSnippet importSnippet) {
        if (importSnippet == null) {
            return;
        }
        name = importSnippet.name();
        some1 = importSnippet.fullname();
        some2 = importSnippet.isStatic() ? "Static" : "";
    }

    private void makeError(ErroneousSnippet errorSnippet) {
        if (errorSnippet == null) {
            return;
        }
        some1 = errorSnippet.probableKind().name();
    }

    /*
        get/set
     */
    public Snippet getSnippet() {
        return snippet;
    }

    public void setSnippet(Snippet snippet) {
        this.snippet = snippet;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSome1() {
        return some1;
    }

    public void setSome1(String some1) {
        this.some1 = some1;
    }

    public String getSome2() {
        return some2;
    }

    public void setSome2(String some2) {
        this.some2 = some2;
    }

}
