package mara.mybox.data;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import mara.mybox.dev.MyBoxLog;
import org.w3c.dom.Element;

/**
 * @Author Mara
 * @CreateDate 2022-9-7
 * @License Apache License Version 2.0
 */
public class HtmlElement {

    protected Element element;
    protected Charset charset;
    protected String tag, href, name, linkAddress, finalAddress;

    public HtmlElement(Element element, Charset charset) {
        this.element = element;
        this.charset = charset;
        parse();
    }

    public final void parse() {
        try {
            href = null;
            tag = null;
            name = null;
            linkAddress = null;
            finalAddress = null;
            if (element == null) {
                return;
            }
            tag = element.getTagName();
            if (tag == null) {
                return;
            }
            if (tag.equalsIgnoreCase("a")) {
                href = element.getAttribute("href");
            } else if (tag.equalsIgnoreCase("img")) {
                href = element.getAttribute("src");
            }
            if (href == null) {
                return;
            }
            try {
                linkAddress = new URL(new URL(element.getBaseURI()), href).toString();
            } catch (Exception e) {
                linkAddress = href;
            }
            finalAddress = URLDecoder.decode(linkAddress, charset);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    /*
        get/set
     */
    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLinkAddress() {
        return linkAddress;
    }

    public void setLinkAddress(String linkAddress) {
        this.linkAddress = linkAddress;
    }

    public String getFinalAddress() {
        return finalAddress;
    }

    public void setFinalAddress(String finalAddress) {
        this.finalAddress = finalAddress;
    }

}
