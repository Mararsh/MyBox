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

    protected String script, error, logs, command, function, operation;
    protected LinkedHashMap<String, String> parameters;
    protected File inputFile, outputFile;
    protected BaseTaskController controller;
    protected boolean openResult, ok;
    protected FxTask<Void> task;

    /*
        init
     */
    public BaseMacro() {
        init();
    }

    public BaseMacro(boolean openResult) {
        init();
        this.openResult = openResult;
    }

    public final void init() {
        script = null;
        parameters = null;
        reset();
    }

    public void reset() {
        command = null;
        inputFile = null;
        outputFile = null;
        error = null;
        logs = null;
        controller = null;
        task = null;
        openResult = false;
        ok = false;
    }

    public void copyTo(BaseMacro macro) {
        if (macro == null) {
            return;
        }
        macro.setScript(script);
        macro.setParameters(parameters);
        macro.setController(controller);
        macro.setOpenResult(openResult);
        macro.setTask(task);
        macro.setError(error);
        macro.setLogs(logs);
        macro.setCommand(command);
        macro.setOk(ok);
        macro.setInputFile(inputFile);
        macro.setOutputFile(outputFile);
    }

    public void copyFrom(BaseMacro macro) {
        if (macro == null) {
            return;
        }
        script = macro.getScript();
        parameters = macro.getParameters();
        controller = macro.getController();
        openResult = macro.isOpenResult();
        task = macro.getTask();
        error = macro.getError();
        logs = macro.getLogs();
        command = macro.getCommand();
        ok = macro.isOk();
        inputFile = macro.getInputFile();
        outputFile = macro.getOutputFile();
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
            String func = readFunction();
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
            write(key, value);
        } else {
            String value = removeQuotes(token);
            write(ParameterPrefix + (parameters != null ? parameters.size() + 1 : 1), value);
        }
    }

    private static String removeQuotes(String str) {
        if ((str.startsWith("\"") && str.endsWith("\""))
                || (str.startsWith("'") && str.endsWith("'"))) {
            return str.substring(1, str.length() - 1);
        }
        return str;
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

    public void write(String key, String value) {
        if (key == null) {
            return;
        }
        if (parameters == null) {
            parameters = new LinkedHashMap<>();
        }
        parameters.put(key.toLowerCase(), value);
    }

    public String read(String key) {
        if (key == null || parameters == null) {
            return null;
        }
        return parameters.get(key.toLowerCase());
    }

    public int readInt(String key) {
        try {
            return Integer.parseInt(read(key));
        } catch (Exception e) {
            return AppValues.InvalidInteger;
        }
    }

    public short readShort(String key) {
        try {
            return Short.parseShort(read(key));
        } catch (Exception e) {
            return AppValues.InvalidShort;
        }
    }

    public boolean readBoolean(String key) {
        try {
            return StringTools.isTrue(read(key));
        } catch (Exception e) {
            return false;
        }
    }

    public String readFunction() {
        try {
            return read(ParameterPrefix + "1");
        } catch (Exception e) {
            return null;
        }
    }

    public String readOperation() {
        try {
            return read(ParameterPrefix + "2");
        } catch (Exception e) {
            return null;
        }
    }

    public File readInputFile() {
        try {
            inputFile = new File(read("inputFile"));
            return inputFile;
        } catch (Exception e) {
            return null;
        }
    }

    public File readOutputFile() {
        try {
            outputFile = new File(read("outputFile"));
            return outputFile;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean readOpenResult() {
        try {
            String v = read("openResult");
            if (v != null) {
                openResult = StringTools.isTrue(v);
            }
            return openResult;
        } catch (Exception e) {
            return false;
        }
    }

    public String commandIO() {
        String s = "";
        if (inputFile != null) {
            s += " inputFile=\"" + inputFile.getAbsolutePath() + "\"";
        }
        if (outputFile != null) {
            s += " outputFile=\"" + outputFile.getAbsolutePath() + "\"";
        }
        if (openResult) {
            s += " openResult=true";
        }
        return s;
    }

    /*
        run
     */
    public boolean readParameters() {
        try {
            function = readFunction();
            if (function == null) {
                error = message("Invalid" + ": " + message("Function"));
                return false;
            }
            operation = readOperation();
            if (operation == null) {
                operation = defaultOperation();
            }
            if (operation == null) {
                error = message("Invalid" + ": " + message("Operation"));
                return false;
            }
            command = "function=" + function + " operation=" + operation;
            inputFile = readInputFile();
            if (inputFile == null) {
                inputFile = defaultInputFile();
            }
            outputFile = readOutputFile();
            if (outputFile == null) {
                outputFile = defaultOutputFile();
            }
            openResult = readOpenResult();
            return true;
        } catch (Exception e) {
            displayError(e.toString());
            return false;
        }
    }

    public boolean run() {
        return false;
    }

    public void displayResult() {
        displayEnd();
        if (openResult) {
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

    public String getCommand() {
        return command;
    }

    public BaseMacro setCommand(String command) {
        this.command = command;
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

    public boolean isOpenResult() {
        return openResult;
    }

    public BaseMacro setOpenResult(boolean openResult) {
        this.openResult = openResult;
        return this;
    }

    public FxTask<Void> getTask() {
        return task;
    }

    public BaseMacro setTask(FxTask<Void> task) {
        this.task = task;
        return this;
    }

    public File getInputFile() {
        return inputFile;
    }

    public BaseMacro setInputFile(File inputFile) {
        this.inputFile = inputFile;
        return this;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public BaseMacro setOutputFile(File outputFile) {
        this.outputFile = outputFile;
        return this;
    }

}
