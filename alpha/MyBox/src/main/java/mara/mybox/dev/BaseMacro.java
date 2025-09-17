package mara.mybox.dev;

import java.io.File;
import java.util.LinkedHashMap;
import mara.mybox.controller.BaseTaskController;
import mara.mybox.fxml.FxTask;
import mara.mybox.value.AppValues;

/**
 * @Author Mara
 * @CreateDate 2025-8-29
 * @License Apache License Version 2.0
 */
public class BaseMacro {

    public static final String ParameterPrefix = "MacroPara_";

    protected String script, error, logs;
    protected LinkedHashMap<String, String> parameters;
    protected File file;
    protected BaseTaskController controller;
    protected boolean ok;
    protected FxTask<Void> task;

    /*
        init
     */
    public BaseMacro() {
        init();
    }

    public final void init() {
        script = null;
        parameters = null;
        reset();
    }

    public void reset() {
        file = null;
        error = null;
        logs = null;
        controller = null;
        task = null;
        ok = false;
    }

    public void copyTo(BaseMacro macro) {
        if (macro == null) {
            return;
        }
        macro.setScript(script);
        macro.setParameters(parameters);
    }

    public void copyFrom(BaseMacro macro) {
        if (macro == null) {
            return;
        }
        script = macro.getScript();
        parameters = macro.getParameters();
    }

    public void info() {
        if (parameters == null) {
            return;
        }
        MyBoxLog.console(parameters);
    }

    /*
        parse
     */
    public BaseMacro make(String inScript) {
        try {
            parseString(inScript);
            String func = getFunction();
            if (func == null) {
                return this;
            }
            func = func.toLowerCase();
            switch (func) {
                case "image":
                    ImageMacro imageMacro = new ImageMacro();
                    imageMacro.copyFrom(this);
                    return imageMacro;
                case "pdf":
                    PdfMacro pdfMacro = new PdfMacro();
                    pdfMacro.copyFrom(this);
                    return pdfMacro;
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return this;
    }

    public boolean parseArray(String[] args) {
        try {
            init();
            if (args == null) {
                return false;
            }
            for (String arg : args) {
                processToken(arg);
                if (script == null) {
                    script = arg;
                } else {
                    script += " " + arg;
                }
            }

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }

    }

    // helped with deepseek
    public boolean parseString(String inScript) {
        try {
            init();
            script = inScript;
            if (script == null || script.isBlank()) {
                return false;
            }

            boolean inDoubleQuotes = false;
            boolean inSingleQuotes = false;
            StringBuilder currentToken = new StringBuilder();

            for (int i = 0; i < script.length(); i++) {
                char c = script.charAt(i);

                if (c == '"' && !inSingleQuotes) {
                    inDoubleQuotes = !inDoubleQuotes;
                    currentToken.append(c);
                    continue;
                } else if (c == '\'' && !inDoubleQuotes) {
                    inSingleQuotes = !inSingleQuotes;
                    currentToken.append(c);
                    continue;
                }

                if (c == ' ' && !inDoubleQuotes && !inSingleQuotes) {
                    if (currentToken.length() > 0) {
                        processToken(currentToken.toString());
                        currentToken.setLength(0);
                    }
                    continue;
                }

                currentToken.append(c);
            }

            if (currentToken.length() > 0) {
                processToken(currentToken.toString());
            }

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    private void processToken(String token) {
        if (token.contains("=")) {
            int equalsIndex = token.indexOf('=');
            String key = token.substring(0, equalsIndex);
            String value = token.substring(equalsIndex + 1);

            value = removeQuotes(value);
            put(key, value);
        } else {
            String value = removeQuotes(token);
            put(ParameterPrefix + (parameters != null ? parameters.size() + 1 : 1), value);
        }
    }

    private static String removeQuotes(String str) {
        if ((str.startsWith("\"") && str.endsWith("\""))
                || (str.startsWith("'") && str.endsWith("'"))) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    public void put(String key, String value) {
        if (key == null) {
            return;
        }
        if (parameters == null) {
            parameters = new LinkedHashMap<>();
        }
        parameters.put(key.toLowerCase(), value);
    }

    public String get(String key) {
        if (key == null || parameters == null) {
            return null;
        }
        return parameters.get(key.toLowerCase());
    }

    public int getInt(String key) {
        try {
            return Integer.parseInt(parameters.get(key));
        } catch (Exception e) {
            return AppValues.InvalidInteger;
        }
    }

    public short getShort(String key) {
        try {
            return Short.parseShort(parameters.get(key));
        } catch (Exception e) {
            return AppValues.InvalidShort;
        }
    }

    public String getFunction() {
        try {
            return get(ParameterPrefix + "1");
        } catch (Exception e) {
            return null;
        }
    }

    public String getOperation() {
        try {
            return get(ParameterPrefix + "2");
        } catch (Exception e) {
            return null;
        }
    }

    public File getFile() {
        try {
            file = new File(get("file"));
            return file;
        } catch (Exception e) {
            return null;
        }
    }

    /*
        run
     */
    public boolean valid() {
        return false;
    }

    public boolean run() {
        return false;
    }

    public void afterSuccess() {

    }

    public void displayError(String info) {
        display(info, true);
    }

    public void displayInfo(String info) {
        display(info, false);
    }

    public void display(String info, boolean isError) {
        if (controller != null) {
            controller.showLogs(info);
        } else if (task != null) {
            task.setInfo(info);
        } else if (isError) {
            MyBoxLog.error(info);
        } else {
            MyBoxLog.console(info);
        }
    }


    /*
        static
     */
    public static BaseMacro create() {
        return new BaseMacro();
    }

    public static BaseMacro create(String inScript) {
        BaseMacro macro = new BaseMacro();
        return macro.make(inScript);
    }

    /*
        get/set
     */
    public LinkedHashMap<String, String> getParameters() {
        return parameters;
    }

    public BaseMacro setParameters(LinkedHashMap<String, String> parameters) {
        this.parameters = parameters;
        return this;
    }

    public String getScript() {
        return script;
    }

    public BaseMacro setScript(String script) {
        this.script = script;
        return this;
    }

    public String getError() {
        return error;
    }

    public BaseMacro setError(String error) {
        this.error = error;
        return this;
    }

    public String getLogs() {
        return logs;
    }

    public BaseMacro setLogs(String logs) {
        this.logs = logs;
        return this;
    }

    public BaseTaskController getController() {
        return controller;
    }

    public BaseMacro setController(BaseTaskController controller) {
        this.controller = controller;
        return this;
    }

    public boolean isOk() {
        return ok;
    }

    public BaseMacro setOk(boolean ok) {
        this.ok = ok;
        return this;
    }

    public FxTask<Void> getTask() {
        return task;
    }

    public BaseMacro setTask(FxTask<Void> task) {
        this.task = task;
        return this;
    }

}
