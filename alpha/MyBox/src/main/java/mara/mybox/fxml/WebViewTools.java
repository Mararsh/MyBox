package mara.mybox.fxml;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollBar;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.StringTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public class WebViewTools {

    public static String userAgent() {
        return new WebView().getEngine().getUserAgent();
    }

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
            MyBoxLog.debug(e);
        }
    }

    public static void selectNone(WebEngine webEngine) {
        try {
            String js = "window.getSelection().removeAllRanges(); ";
            webEngine.executeScript(js);
        } catch (Exception e) {
            MyBoxLog.debug(e);
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
            MyBoxLog.debug(e);
        }
    }

    public static void selectElement(WebView webView, Element element) {
        try {
            if (webView == null || element == null) {
                return;
            }
            String id = element.getAttribute("id");
            String newid = DateTools.nowFileString();
            element.setAttribute("id", newid);
            selectNode(webView.getEngine(), newid);
            if (id != null) {
                element.setAttribute("id", id);
            } else {
                element.removeAttribute("id");
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
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
            MyBoxLog.debug(e);
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
//            MyBoxLog.debug(e);
            return null;
        }
    }

    // https://blog.csdn.net/weixin_29251337/article/details/117888001
    public static void addStyle(WebEngine webEngine, String style, String styleid) {
        try {
            if (webEngine == null || styleid == null || styleid.isBlank()
                    || style == null || style.isBlank()) {
                return;
            }
            removeNode(webEngine, styleid);
            String js = "var node = document.createElement(\"style\");\n"
                    + "node.id = \"" + styleid + "\";\n"
                    + "node.type = \"text/css\";\n"
                    + "node.innerHTML = \"" + StringTools.replaceLineBreak(style) + "\";\n"
                    + "document.body.appendChild(node);";
            webEngine.executeScript(js);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public static void removeNode(WebEngine webEngine, String id) {
        try {
            if (webEngine == null || id == null || id.isBlank()) {
                return;
            }
            String js = "var node = document.getElementById(\"" + id + "\");\n"
                    + "if ( node != null ) node.parentNode.removeChild(node);";
            webEngine.executeScript(js);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public static HTMLEditor editor(WebView webView) {
        if (webView == null) {
            return null;
        }
        Parent p = webView.getParent();
        while (p != null) {
            if (p instanceof HTMLEditor) {
                return (HTMLEditor) p;
            }
            p = p.getParent();
        }
        return null;
    }

    public static WebView webview(Parent node) {
        if (node == null) {
            return null;
        }
        for (Node child : node.getChildrenUnmodifiable()) {
            if (child instanceof WebView) {
                return (WebView) child;
            }
            if (child instanceof Parent) {
                WebView w = webview((Parent) child);
                if (w != null) {
                    return w;
                }
            }
        }
        return null;
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
            MyBoxLog.debug(e);
            return null;
        }
    }

    // https://stackoverflow.com/questions/31264847/how-to-set-remember-scrollbar-thumb-position-in-javafx-8-webview?r=SearchResults
    public static ScrollBar getVScrollBar(WebView webView) {
        try {
            Set<Node> scrolls = webView.lookupAll(".scroll-bar");
            for (Node scrollNode : scrolls) {
                if (ScrollBar.class.isInstance(scrollNode)) {
                    ScrollBar scroll = (ScrollBar) scrollNode;
                    if (scroll.getOrientation() == Orientation.VERTICAL) {
                        return scroll;
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    // https://bbs.csdn.net/topics/310020841
    public static int px(WebEngine webEngine, String s) {
        try {
            String js = "function stringPX(s){"
                    + "    var span = document.createElement(\"span\");\n"
                    + "    span.innerHTML = s;\n"
                    + "    document.body.appendChild(span);\n"
                    + "    var width = span.offsetWidth;\n"
                    + "    document.body.removeChild(span); "
                    + "    return width; "
                    + "}"
                    + "stringPX(\"" + s + "\");";
            Object c = webEngine.executeScript(js);
            if (c == null) {
                return -1;
            }
            return ((int) c);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return -1;
        }
    }

    public static Rectangle tagRect(WebEngine webEngine, String tag, int index) {
        try {
            Rectangle rect = new Rectangle();
            String js = "var rect = document.getElementsByTagName('" + tag + "')[" + index + "].getBoundingClientRect();"
                    + "rect.x + ' ' + rect.y + ' ' + rect.width + ' ' + rect.height ";
            Object c = webEngine.executeScript(js);
            if (c == null) {
                return null;
            }
            String[] values = ((String) c).split("\\s+");
            rect.setX(Double.parseDouble(values[0]));
            rect.setY(Double.parseDouble(values[1]));
            rect.setWidth(Double.parseDouble(values[2]));
            rect.setHeight(Double.parseDouble(values[3]));
            return rect;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

}
