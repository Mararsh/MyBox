package mara.mybox.data;

import mara.mybox.tools.StringTools;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;

/**
 * @Author Mara
 * @CreateDate 2023-2-13
 * @License Apache License Version 2.0
 */
public class HtmlNode {

    protected Element element;

    public HtmlNode() {
        element = null;
    }

    public HtmlNode(String tag) {
        setElement(new Element(tag));
    }

    public HtmlNode(Element element) {
        setElement(element);
    }

    public HtmlNode copy() {
        return new HtmlNode(element.clone());
    }

    /*
        set
     */
    final public void setElement(Element element) {
        this.element = element;
    }

    /*
        get
     */
    public Element getElement() {
        return element;
    }

    public String getTag() {
        return element == null ? null : element.tagName();
    }

    public String getId() {
        return element == null ? null : element.id();
    }

    public String getName() {
        return element == null ? null : element.nodeName();
    }

    public String getWholeOwnText() {
        return element == null ? null : element.wholeOwnText();
    }

    public String getWholeText() {
        return element == null ? null : element.wholeText();
    }

    public String getText() {
        return element == null ? null : element.text();
    }

    public String getTextStart() {
        String s = getWholeOwnText();
        if (s == null) {
            return null;
        }
        return StringTools.start(s.trim().replaceAll("\n", " "), 60);
    }

    public String getValue() {
        return element == null ? null : element.val();
    }

    public String getData() {
        return element == null ? null : element.data();
    }

    public String getInnerHtml() {
        return element == null ? null : element.html();
    }

    public String getOuterHtml() {
        return element == null ? null : element.outerHtml();
    }

    public String getClassname() {
        return element == null ? null : element.className();
    }

    public Attributes getAttributes() {
        return element == null ? null : element.attributes();
    }

}
