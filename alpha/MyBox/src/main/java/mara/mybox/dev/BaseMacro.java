package mara.mybox.dev;

import java.io.File;
import java.util.LinkedHashMap;
import mara.mybox.controller.BaseTaskController;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-8-29
 * @License Apache License Version 2.0
 */
public class BaseMacro {

    public static final String ParameterPrefix = "MacroPara_";

    protected String script, error, logs;
    protected LinkedHashMap<String, String> arguments;  // values defined in macro
    protected LinkedHashMap<String, Object> parameters;  // values referred when run
    protected BaseTaskController controller;
    protected boolean defaultOpenResult, ok;
    protected FxTask<Void> task;

    /*
        build macro
     */
    public BaseMacro() {
        init();
    }

    public BaseMacro(boolean openResult) {
        init();
        this.defaultOpenResult = openResult;
        MyBoxLog.console(defaultOpenResult);
    }

    public final void init() {
        script = null;
        arguments = null;
        reset();
    }

    public void reset() {
        parameters = null;
        error = null;
        logs = null;
        controller = null;
        task = null;
        ok = false;
        defaultOpenResult = false;
    }

    public void copyTo(BaseMacro macro) {
        if (macro == null) {
            return;
        }
        macro.setScript(script);
        macro.setArguments(arguments);
        macro.setParameters(parameters);
        macro.setController(controller);
        macro.setTask(task);
        macro.setError(error);
        macro.setLogs(logs);
        macro.setOk(ok);
        macro.setDefaultOpenResult(defaultOpenResult);
    }

    public void copyFrom(BaseMacro macro) {
        if (macro == null) {
            return;
        }
        script = macro.getScript();
        arguments = macro.getArguments();
        parameters = macro.getParameters();
        controller = macro.getController();
        task = macro.getTask();
        error = macro.getError();
        logs = macro.getLogs();
        ok = macro.isOk();
        defaultOpenResult = macro.isDefaultOpenResult();
    }

    public void info() {
        if (arguments == null) {
            return;
        }
        MyBoxLog.console(arguments);
    }

    public BaseMacro make(String inScript) {
        try {
            parseString(inScript);
            String func = pickFunction();
            if (func == null) {
                return null;
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
        return null;
    }

    // helped with deepseek
    public boolean parseString(String inScript) {
        try {
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
            writeArgument(key, value);
        } else {
            String value = removeQuotes(token);
            writeArgument(ParameterPrefix + (arguments != null ? arguments.size() + 1 : 1), value);
        }
    }

    private static String removeQuotes(String str) {
        if ((str.startsWith("\"") && str.endsWith("\""))
                || (str.startsWith("'") && str.endsWith("'"))) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    public boolean parseArray(String[] args) {
        try {
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

    /*
        arguments: values defined in macro
     */
    // key is case-insensitive
    public void writeArgument(String key, String value) {
        if (key == null) {
            return;
        }
        if (arguments == null) {
            arguments = new LinkedHashMap<>();
        }
        arguments.put(key.toLowerCase(), value);
    }

    // key is case-insensitive
    public String readArgument(String key) {
        if (key == null || arguments == null) {
            return null;
        }
        return arguments.get(key.toLowerCase());
    }

    public int readIntArgument(String key) {
        try {
            return Integer.parseInt(readArgument(key));
        } catch (Exception e) {
            return AppValues.InvalidInteger;
        }
    }

    public short readShortArgument(String key) {
        try {
            return Short.parseShort(readArgument(key));
        } catch (Exception e) {
            return AppValues.InvalidShort;
        }
    }

    public boolean readBooleanArgument(String key) {
        try {
            return StringTools.isTrue(readArgument(key));
        } catch (Exception e) {
            return false;
        }
    }

    /*
        parameters: values referred when run
     */
    // key is case-insensitive
    public void writeParameter(String key, Object value) {
        if (key == null) {
            return;
        }
        if (parameters == null) {
            parameters = new LinkedHashMap<>();
        }
        if (value != null) {
            parameters.put(key.toLowerCase(), value);
        } else {
            parameters.remove(key);
        }
    }

    // key is case-insensitive
    public Object readParameter(String key) {
        if (key == null || parameters == null) {
            return null;
        }
        return parameters.get(key.toLowerCase());
    }

    public String pickFunction() {
        try {
            String func = readArgument(ParameterPrefix + "1");
            writeParameter("function", func);
            return func;
        } catch (Exception e) {
            return null;
        }
    }

    public String pickOperation() {
        try {
            String op = defaultOperation();
            String v = readArgument(ParameterPrefix + "2");
            if (v != null) {
                op = v;
            }
            writeParameter("operation", op);
            return op;
        } catch (Exception e) {
            return null;
        }
    }

    public File pickInputFile() {
        try {
            File file = defaultInputFile();
            String v = readArgument("inputFile");
            if (v != null) {
                File fv = new File(v);
                if (fv.exists()) {
                    file = fv;
                }
            }
            writeParameter("inputFile", file);
            return file;
        } catch (Exception e) {
            return null;
        }
    }

    public File pickOutputFile() {
        try {
            File file = defaultOutputFile();
            String v = readArgument("outputFile");
            if (v != null) {
                file = new File(v);
            }
            writeParameter("outputFile", file);
            return file;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean pickOpenResult() {
        try {
            boolean bv = defaultOpenResult;
            String v = readArgument("openResult");
            if (v != null) {
                bv = StringTools.isTrue(v);
            }
            writeParameter("openResult", bv);
            return bv;
        } catch (Exception e) {
            return false;
        }
    }

    public String defaultOperation() {
        return null;
    }

    public File defaultInputFile() {
        return null;
    }

    public File defaultOutputFile() {
        return null;
    }

    public String getFunction() {
        try {
            return (String) readParameter("function");
        } catch (Exception e) {
            return null;
        }
    }

    public String getOperation() {
        try {
            return (String) readParameter("operation");
        } catch (Exception e) {
            return null;
        }
    }

    public File getInputFile() {
        try {
            return (File) readParameter("inputFile");
        } catch (Exception e) {
            return null;
        }
    }

    public File getOutputFile() {
        try {
            return (File) readParameter("outputFile");
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isOpenResult() {
        try {
            return (boolean) readParameter("openResult");
        } catch (Exception e) {
        }
        return defaultOpenResult;
    }

    /*
        run
     */
    public boolean checkParameters() {
        try {
            parameters = null;
            String function = pickFunction();
            if (function == null) {
                error = message("Invalid" + ": " + message("Function"));
                return false;
            }
            String operation = pickOperation();
            if (operation == null) {
                error = message("Invalid" + ": " + message("Operation"));
                return false;
            }
            pickInputFile();
            pickOutputFile();

            if (!checkMoreParameters()) {
                return false;
            }

            pickOpenResult();

            String command = "";
            for (String key : parameters.keySet()) {
                if ("inputFile".equalsIgnoreCase(key) || "outputFile".equalsIgnoreCase(key)) {
                    continue;
                }
                Object v = readParameter(key);
                if (v == null) {
                    continue;
                }
                String s = v.toString();
                if (s.contains(" ")) {
                    s = "\"" + s + "\"";
                }
                if (!command.isBlank()) {
                    command += " ";
                }
                command += key + "=" + s;
            }
            File inputFile = getInputFile();
            if (inputFile != null) {
                command += " inputFile=\"" + inputFile.getAbsolutePath() + "\"";
            }
            File outputFile = getOutputFile();
            if (outputFile != null) {
                command += " outputFile=\"" + outputFile.getAbsolutePath() + "\"";
            }
            displayInfo(message("Parameters") + ": " + command);
            return true;
        } catch (Exception e) {
            displayError(e.toString());
            return false;
        }
    }

    public boolean checkMoreParameters() {
        return true;
    }

    public boolean run() {
        return false;
    }

    public void displayResult() {
        displayEnd();
        if (isOpenResult()) {
            openResult();
        }
    }

    public void displayEnd() {
        try {
            if (ok) {
                displayInfo(message("Completed"));
            } else {
                displayInfo(message("Failed"));
            }
            File outputFile = getOutputFile();
            if (outputFile != null && outputFile.exists()) {
                displayInfo(message("FileGenerated") + ": " + outputFile);
            }
        } catch (Exception e) {
            displayError(e.toString());
        }
    }

    public void openResult() {
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

    public static BaseMacro create(String inScript, boolean openResult) {
        BaseMacro macro = new BaseMacro(openResult);
        return macro.make(inScript);
    }

    public static BaseMacro create(String[] args) {
        BaseMacro macro = new BaseMacro();
        macro.parseArray(args);
        return macro.make(macro.getScript());
    }

    /*
        get/set
     */
    public LinkedHashMap<String, String> getArguments() {
        return arguments;
    }

    public BaseMacro setArguments(LinkedHashMap<String, String> arguments) {
        this.arguments = arguments;
        return this;
    }

    public LinkedHashMap<String, Object> getParameters() {
        return parameters;
    }

    public BaseMacro setParameters(LinkedHashMap<String, Object> parameters) {
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

    public boolean isDefaultOpenResult() {
        return defaultOpenResult;
    }

    public BaseMacro setDefaultOpenResult(boolean defaultOpenResult) {
        this.defaultOpenResult = defaultOpenResult;
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
