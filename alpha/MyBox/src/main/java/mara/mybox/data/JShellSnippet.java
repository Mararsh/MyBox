package mara.mybox.data;

import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.VarSnippet;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-3-5
 * @License Apache License Version 2.0
 */
public class JShellSnippet {

    protected Snippet snippet;
    protected String id, name, type, subType, status, source, value;

    public JShellSnippet(JShell jShell, Snippet snippet) {
        if (jShell == null || snippet == null) {
            return;
        }
        this.snippet = snippet;
        id = snippet.id();
        type = snippet.kind().name();
        subType = snippet.subKind().name();
        status = jShell.status(snippet).name();
        source = snippet.source();
        if (snippet instanceof VarSnippet) {
            makeVar(jShell, (VarSnippet) snippet);
        }
    }

    private void makeVar(JShell jShell, VarSnippet varSnippet) {
        if (jShell == null || snippet == null) {
            return;
        }
        try {
            name = varSnippet.name();
            subType = varSnippet.typeName();
            value = jShell.varValue(varSnippet);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
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

}
