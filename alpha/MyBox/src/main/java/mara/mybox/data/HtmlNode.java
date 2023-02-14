package mara.mybox.data;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;

/**
 * @Author Mara
 * @CreateDate 2023-2-13
 * @License Apache License Version 2.0
 */
public class HtmlNode {

    protected Element element;
    protected String tag;

    public HtmlNode() {
        element = null;
    }

    public HtmlNode(String name) {
        tag = name;
    }

    public HtmlNode(Element element) {
        setElement(element);
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
        return element == null ? tag : element.tagName();
    }

    public String getId() {
        return element == null ? null : element.id();
    }

    public String getName() {
        return element == null ? null : element.nodeName();
    }

    public String getText() {
        return element == null ? null : element.wholeOwnText();
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

    public String getClassname() {
        return element == null ? null : element.className();
    }

    public Attributes getAttributes() {
        return element == null ? null : element.attributes();
    }

}
