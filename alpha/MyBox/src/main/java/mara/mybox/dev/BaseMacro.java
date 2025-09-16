package mara.mybox.dev;

import java.io.File;
import java.util.LinkedHashMap;
import mara.mybox.value.AppValues;

/**
 * @Author Mara
 * @CreateDate 2025-8-29
 * @License Apache License Version 2.0
 */
public class BaseMacro {

    public static final String ParameterPrefix = "MacroPara_";

    protected String script;
    protected LinkedHashMap<String, String> parameters;
    protected File file;

    public BaseMacro() {
        init();
    }

    public BaseMacro(String inScript) {
        parseString(inScript);
    }

    public BaseMacro(LinkedHashMap<String, String> paras) {
        parameters = paras;
    }

    public final void init() {
        script = null;
        parameters = null;
        file = null;
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
    public final boolean parseString(String inScript) {
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

    public boolean run() {
        return false;
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

    public static BaseMacro parse(String inScript) {
        BaseMacro macro = new BaseMacro(inScript);
        try {
            String func = macro.getFunction();
            if (func == null) {
                return macro;
            }
            func = func.toLowerCase();
            switch (func) {
                case "image":
                    ImageMacro imageMacro = new ImageMacro();
                    imageMacro.copyFrom(macro);
                    return imageMacro;
                case "pdf":
                    PdfMacro pdfMacro = new PdfMacro();
                    pdfMacro.copyFrom(macro);
                    return pdfMacro;
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return macro;
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

}
