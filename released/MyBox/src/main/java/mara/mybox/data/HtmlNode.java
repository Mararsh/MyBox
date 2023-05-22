package mara.mybox.data;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;

/**
 * @Author Mara
 * @CreateDate 2023-2-13
 * @License Apache License Version 2.0
 */
public class HtmlNode {

    protected Element element;
    protected final BooleanProperty selected = new SimpleBooleanProperty(false);

    public HtmlNode() {
        element = null;
    }

    public HtmlNode(String tag) {
        setElement(new Element(tag));
    }

    public HtmlNode(Element element) {
        setElement(element);
    }

    public boolean equal(HtmlNode node) {
        Element nodeElement = node.getElement();
        if (element == null || nodeElement == null) {
            return false;
        }
        return element.equals(nodeElement);
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

    public String getTitle() {
        return getTag();
    }

    public String getValue() {
        return getWholeText();
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

    public String getElementValue() {
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

    public BooleanProperty getSelected() {
        return selected;
    }

}
