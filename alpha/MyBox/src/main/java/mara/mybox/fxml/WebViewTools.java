package mara.mybox.fxml;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import mara.mybox.dev.MyBoxLog;
import org.w3c.dom.Document;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public class WebViewTools {

    public static String HttpUserAgent = new WebView().getEngine().getUserAgent();

    public static String getHtml(WebView webView) {
        if (webView == null) {
            return "";
        }
        return getHtml(webView.getEngine());
    }

    public static String getHtml(WebEngine engine) {
        try {
            if (engine == null) {
                return "";
            }
            Object c = engine.executeScript("document.documentElement.outerHTML");
            if (c == null) {
                return "";
            }
            return (String) c;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return "";
        }
    }

    public static void selectAll(WebEngine webEngine) {
        try {
            String js = "window.getSelection().removeAllRanges(); "
                    + "var selection = window.getSelection();\n"
                    + "var range = document.createRange();\n"
                    + "range.selectNode(document.documentElement);\n"
                    + "selection.addRange(range);";
            webEngine.executeScript(js);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public static void selectNone(WebEngine webEngine) {
        try {
            String js = "window.getSelection().removeAllRanges(); ";
            webEngine.executeScript(js);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public static void selectNode(WebEngine webEngine, String id) {
        try {
            String js = "window.getSelection().removeAllRanges(); "
                    + "var selection = window.getSelection();\n"
                    + "var range = document.createRange();\n"
                    + "range.selectNode(document.getElementById('" + id + "'));\n"
                    + "selection.addRange(range);";
            webEngine.executeScript(js);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public static String selectedText(WebEngine webEngine) {
        try {
            Object ret = webEngine.executeScript("window.getSelection().toString();");
            if (ret == null) {
                return null;
            }
            return ((String) ret);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static String selectedHtml(WebEngine webEngine) {
        try {
            String js = " 　　　var selectionObj = window.getSelection();\n"
                    + " 　　　var rangeObj = selectionObj.getRangeAt(0);\n"
                    + " 　　　var docFragment = rangeObj.cloneContents();\n"
                    + " 　　　var div = document.createElement(\"div\");\n"
                    + " 　　　div.appendChild(docFragment);\n"
                    + " 　　　div.innerHTML;";
            Object ret = webEngine.executeScript(js);
            if (ret == null) {
                return null;
            }
            return ((String) ret);
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static String getFrame(WebEngine engine, int index) {
        try {
            if (engine == null || index < 0) {
                return "";
            }
            Object c = engine.executeScript("window.frames[" + index + "].document.documentElement.outerHTML");
            if (c == null) {
                return "";
            }
            return (String) c;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return "";
        }
    }

    public static String getFrame(WebEngine engine, String frameName) {
        try {
            if (engine == null || frameName == null) {
                return "";
            }
            Object c = engine.executeScript("window.frames." + frameName + ".document.documentElement.outerHTML");
            if (c == null) {
                return "";
            }
            return (String) c;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return "";
        }
    }

    public static Document getFrameDocument(WebEngine engine, String frameName) {
        try {
            if (engine == null) {
                return null;
            }
            Object c = engine.executeScript("window.frames." + frameName + ".document");
            if (c == null) {
                return null;
            }
            return (Document) c;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static int frameIndex(WebEngine webEngine, String frameName) {
        try {
            if (frameName == null) {
                return -1;
            }
            Object c = webEngine.executeScript("function checkFrameIndex(frameName) { "
                    + "  for (i=0; i<window.frames.length; i++) { "
                    + "     if ( window.frames[i].name == frameName ) return i ;"
                    + "  };"
                    + "  return -1; "
                    + "};"
                    + "checkFrameIndex(\"" + frameName + "\");");
            if (c == null) {
                return -1;
            }
            return ((int) c);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return -1;
        }
    }

    public static Map<String, String> readCookie(WebEngine webEngine) {
        try {
            String s = (String) webEngine.executeScript("document.cookie;");
            String[] vs = s.split(";");
            Map<String, String> m = new HashMap<>();
            for (String v : vs) {
                String[] vv = v.split("=");
                if (vv.length < 2) {
                    continue;
                }
                m.put(vv[0].trim(), vv[1].trim());
            }
            return m;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

}
