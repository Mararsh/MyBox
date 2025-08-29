package mara.mybox.dev;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author Mara
 * @CreateDate 2025-8-29
 * @License Apache License Version 2.0
 */
public class BaseMacro {

    protected Map<String, Object> parameters;

    public BaseMacro() {
        init();
    }

    public final void init() {
        parameters = null;
    }

    public void put(String key, Object value) {
        if (key == null) {
            return;
        }
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        parameters.put(key, value);
    }

    public void info() {
        if (parameters == null) {
            return;
        }
        MyBoxLog.console(parameters);
    }

    /*
        static
     */
    public static BaseMacro create() {
        return new BaseMacro();
    }

    public static BaseMacro parse(String args) {
        if (args == null) {
            return null;
        }
        return parse(args.split(" "));
    }

    public static BaseMacro parse(String[] args) {
        if (args == null) {
            return null;
        }
        BaseMacro macro = create();
        int index = 0, pos;
        for (String arg : args) {
            pos = arg.indexOf("=");
            if (pos > 0) {
                macro.put(arg.substring(0, pos), arg.substring(pos + 1, arg.length()));
            } else {
                macro.put("p" + index++, arg);
            }
        }
        return macro;
    }

    /*
        get/set
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    public BaseMacro setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
        return this;
    }

}
