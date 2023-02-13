package mara.mybox.data;

import org.jsoup.nodes.Element;

/**
 * @Author Mara
 * @CreateDate 2023-2-13
 * @License Apache License Version 2.0
 */
public class HtmlNode {

    protected Element element;
    protected String tag, id, name, text, ownText, rawText, data, value, innerHtml, classname;

    public HtmlNode() {
        init();
    }

    public HtmlNode(String name) {
        tag = name;
    }

    public HtmlNode(Element element) {
        setElement(element);
    }

    private void init() {
        element = null;
        tag = null;
        id = null;
        name = null;
        text = null;
        ownText = null;
        rawText = null;
        data = null;
        value = null;
        innerHtml = null;
        classname = null;
    }


    /*
        set
     */
    final public void setElement(Element element) {
        if (element == null) {
            init();
            return;
        }
        this.element = element;
        tag = element.tagName();
        id = element.id();
        name = element.nodeName();
        text = element.text();
        ownText = element.wholeOwnText();
        rawText = element.wholeText();
        data = element.data();
        value = element.val();
        innerHtml = element.html();
        classname = element.className();
    }

    /*
    get
     */
    public Element getElement() {
        return element;
    }

    public String getTag() {
        return tag;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public String getOwnText() {
        return ownText;
    }

    public String getRawText() {
        return rawText;
    }

    public String getValue() {
        return value;
    }

    public String getData() {
        return data;
    }

    public String getInnerHtml() {
        return innerHtml;
    }

    public String getClassname() {
        return classname;
    }

}
