package mara.mybox.tools;

import java.util.List;
import jdk.jshell.JShell;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis;
import mara.mybox.data.JShellSnippet;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-6-1
 * @License Apache License Version 2.0
 */
public class JShellTools {

    public static String runSnippet(JShell jShell, String orignalSource, String source) {
        try {
            if (source == null || source.isBlank()) {
                return source;
            }
            String snippet = source.trim();
            List<SnippetEvent> events = jShell.eval(snippet);
            String results = "";
            for (int i = 0; i < events.size(); i++) {
                SnippetEvent e = events.get(i);
                JShellSnippet jShellSnippet = new JShellSnippet(jShell, e.snippet());
                if (i > 0) {
                    results += "\n";
                }
                results += "id: " + jShellSnippet.getId() + "\n";
                if (jShellSnippet.getStatus() != null) {
                    results += message("Status") + ": " + jShellSnippet.getStatus() + "\n";
                }
                if (jShellSnippet.getType() != null) {
                    results += message("Type") + ": " + jShellSnippet.getType() + "\n";
                }
                if (jShellSnippet.getName() != null) {
                    results += message("Name") + ": " + jShellSnippet.getName() + "\n";
                }
                if (jShellSnippet.getValue() != null) {
                    results += message("Value") + ": " + jShellSnippet.getValue() + "\n";
                }
            }
            return results;
        } catch (Exception e) {
            return e.toString();
        }
    }

    public static String runSnippet(JShell jShell, String source) {
        return runSnippet(jShell, source, source);
    }

    public static boolean runScript(JShell jShell, String script) {
        try {
            if (script == null || script.isBlank()) {
                return false;
            }
            String leftCodes = script;
            while (leftCodes != null && !leftCodes.isBlank()) {
                SourceCodeAnalysis.CompletionInfo info = jShell.sourceCodeAnalysis().analyzeCompletion(leftCodes);
                String snippet = info.source().trim();
                jShell.eval(snippet);
                leftCodes = info.remaining();
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static String expValue(JShell jShell, String exp) {
        try {
            return new JShellSnippet(jShell, jShell.eval(exp).get(0).snippet()).getValue();
        } catch (Exception e) {
            MyBoxLog.error(e, exp);
            return null;
        }
    }

    public static String classPath(JShell jShell) {
        try {
            String paths = expValue(jShell, "System.getProperty(\"java.class.path\")");
            if (paths.startsWith("\"") && paths.endsWith("\"")) {
                paths = paths.substring(1, paths.length() - 1);
            }
            return paths;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static JShell initJEXL() {
        try {
            JShell jShell = JShell.create();
            jShell.addToClasspath(System.getProperty("java.class.path"));
            String initCodes = "import org.apache.commons.jexl3.JexlBuilder;\n"
                    + "import org.apache.commons.jexl3.JexlEngine;\n"
                    + "import org.apache.commons.jexl3.JexlScript;\n"
                    + "import org.apache.commons.jexl3.MapContext;\n"
                    + "JexlEngine  jexlEngine = new JexlBuilder().cache(512).strict(true).silent(false).create();\n"
                    + "MapContext jexlContext = new MapContext();"
                    + "JexlScript jexlScript;";
            runScript(jShell, initCodes);
            return jShell;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
