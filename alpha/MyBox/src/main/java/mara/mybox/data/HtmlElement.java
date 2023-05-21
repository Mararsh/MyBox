package mara.mybox.data;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.UrlTools;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 * @Author Mara
 * @CreateDate 2022-9-7
 * @License Apache License Version 2.0
 */
public class HtmlElement {

    protected Element element;
    protected Charset charset;
    protected String tag, href, name, address, decodedHref, decodedAddress;

    public HtmlElement(Element element, Charset charset) {
        this.element = element;
        this.charset = charset;
        parse();
    }

    public final void parse() {
        try {
            href = null;
            decodedHref = null;
            tag = null;
            name = null;
            address = null;
            decodedAddress = null;
            if (element == null) {
                return;
            }
            tag = element.getTagName();
            if (tag == null) {
                return;
            }
            if (tag.equalsIgnoreCase("a")) {
                href = element.getAttribute("href");
                name = element.getTextContent();
                if (href == null) {
                    NamedNodeMap m = element.getAttributes();
                    if (m != null) {
                        for (int k = 0; k < m.getLength(); k++) {
                            if ("href".equalsIgnoreCase(m.item(k).getNodeName())) {
                                href = m.item(k).getNodeValue();
                            } else if ("title".equalsIgnoreCase(m.item(k).getNodeName())) {
                                name = m.item(k).getNodeValue();
                            }
                        }
                    }
                }

            } else if (tag.equalsIgnoreCase("img")) {
                href = element.getAttribute("src");
                name = element.getAttribute("alt");
                if (href == null) {
                    NamedNodeMap m = element.getAttributes();
                    if (m != null) {
                        for (int k = 0; k < m.getLength(); k++) {
                            if ("src".equalsIgnoreCase(m.item(k).getNodeName())) {
                                href = m.item(k).getNodeValue();
                            } else if ("alt".equalsIgnoreCase(m.item(k).getNodeName())) {
                                name = m.item(k).getNodeValue();
                            }
                        }
                    }
                }
            }
            if (href == null) {
                return;
            }
            try {
                address = UrlTools.fullAddress(element.getBaseURI(), href);
            } catch (Exception e) {
                address = href;
            }
            if (charset != null) {
                decodedHref = URLDecoder.decode(href, charset);
                decodedAddress = URLDecoder.decode(address, charset);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public boolean isLink() {
        return decodedHref != null && "a".equalsIgnoreCase(tag);
    }

    public boolean isImage() {
        return decodedHref != null && "img".equalsIgnoreCase(tag);
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDecodedAddress() {
        return decodedAddress;
    }

    public void setDecodedAddress(String decodedAddress) {
        this.decodedAddress = decodedAddress;
    }

    public String getDecodedHref() {
        return decodedHref;
    }

    public void setDecodedHref(String decodedHref) {
        this.decodedHref = decodedHref;
    }

}
