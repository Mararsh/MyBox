/**
 * PDFDomTree.java
 * (c) Radek Burget, 2011
 *
 * Pdf2Dom is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Pdf2Dom is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with CSSBox. If not, see <http://www.gnu.org/licenses/>.
 *
 * Created on 13.9.2011, 14:17:24 by burgetr
 */
package thridparty.pdfdom;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import mara.mybox.dev.MyBoxLog;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 * A DOM representation of a PDF file.
 *
 * @author burgetr
 *
 * Updated by Mara
 */
public class PDFDomTree extends PDFBoxTree {

    /**
     * Default style placed in the begining of the resulting document
     */
    protected String defaultStyle = ".page{position:relative; border:1px solid blue;margin:0.5em}\n"
            + ".p,.r{position:absolute;}\n"
            + ".p{white-space:nowrap;}\n"
            + // disable text-shadow fallback for text stroke if stroke supported by browser
            "@supports(-webkit-text-stroke: 1px black) {"
            + ".p{text-shadow:none !important;}"
            + "}";

    /**
     * The resulting document representing the PDF file.
     */
    protected Document doc;
    /**
     * The head element of the resulting document.
     */
    protected Element head;
    /**
     * The body element of the resulting document.
     */
    protected Element body;
    /**
     * The title element of the resulting document.
     */
    protected Element title;
    /**
     * The global style element of the resulting document.
     */
    protected Element globalStyle;
    /**
     * The element representing the page currently being created in the
     * resulting document.
     */
    protected Element curpage;

    /**
     * Text element counter for assigning IDs to the text elements.
     */
    protected int textcnt;
    /**
     * Page counter for assigning IDs to the pages.
     */
    protected int pagecnt;

    protected PDFDomTreeConfig config;

    /**
     * Creates a new PDF DOM parser.
     *
     * @throws IOException
     */
    public PDFDomTree() throws IOException {
        super();
        init();
    }

    /**
     * Creates a new PDF DOM parser.
     *
     * @param config
     * @throws IOException
     */
    public PDFDomTree(PDFDomTreeConfig config) throws IOException {
        this();
        if (config != null) {
            this.config = config;
        }
    }

    /**
     * Internal initialization.
     */
    private void init() {
        pagecnt = 0;
        textcnt = 0;
        this.config = PDFDomTreeConfig.createDefaultConfig();
    }

    /**
     * Creates a new empty HTML document tree.
     */
    protected void createDocument() {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            DocumentType doctype = builder.getDOMImplementation().createDocumentType("html", "-//W3C//DTD XHTML 1.1//EN", "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd");
            doc = builder.getDOMImplementation().createDocument("http://www.w3.org/1999/xhtml", "html", doctype);

            head = doc.createElement("head");
            Element meta = doc.createElement("meta");
            meta.setAttribute("http-equiv", "content-type");
            meta.setAttribute("content", "text/html;charset=utf-8");
            head.appendChild(meta);
            title = doc.createElement("title");
            title.setTextContent("PDF Document");
            head.appendChild(title);
            globalStyle = doc.createElement("style");
            globalStyle.setAttribute("type", "text/css");
            //globalStyle.setTextContent(createGlobalStyle());
            head.appendChild(globalStyle);

            body = doc.createElement("body");

            Element root = doc.getDocumentElement();
            root.appendChild(head);
            root.appendChild(body);
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    /**
     * Obtains the resulting document tree.
     *
     * @return The DOM root element.
     */
    public Document getDocument() {
        return doc;
    }

    @Override
    public void startDocument(PDDocument document) {
        createDocument();
    }

    @Override
    protected void endDocument(PDDocument document) {
        try {
            //use the PDF title
            String doctitle = document.getDocumentInformation().getTitle();
            if (doctitle != null && doctitle.trim().length() > 0) {
                title.setTextContent(doctitle);
            }
            //set the main style
            globalStyle.setTextContent(createGlobalStyle());
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    /**
     * Parses a PDF document and serializes the resulting DOM tree to an output.
     * This requires a DOM Level 3 capable implementation to be available.
     */
    @Override
    public void writeText(PDDocument doc, Writer outputStream) {
        try {
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            LSSerializer writer = impl.createLSSerializer();
            writer.getDomConfig().setParameter("format-pretty-print", true);
            LSOutput lsOutput = impl.createLSOutput();
            lsOutput.setCharacterStream(outputStream);
            createDOM(doc);
            writer.write(getDocument(), lsOutput);
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    /**
     * Loads a PDF document and creates a DOM tree from it.
     *
     * @param doc the source document
     * @return a DOM Document representing the DOM tree
     */
    public Document createDOM(PDDocument doc) {
        try {
            /* We call the original PDFTextStripper.writeText but nothing should
           be printed actually because our processing methods produce no output.
           They create the DOM structures instead */
            super.writeText(doc, new OutputStreamWriter(System.out));
            return this.doc;
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
            return null;
        }
    }

    //===========================================================================================
    @Override
    protected void startNewPage() {
        try {
            curpage = createPageElement();
            body.appendChild(curpage);
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @Override
    protected void renderText(String data, TextMetrics metrics) {
        try {
            curpage.appendChild(createTextElement(data, metrics.getWidth()));
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @Override
    protected void renderPath(List<PathSegment> path, boolean stroke, boolean fill) throws IOException {
        float[] rect = toRectangle(path);
        if (rect != null) {
            curpage.appendChild(createRectangleElement(rect[0], rect[1], rect[2] - rect[0], rect[3] - rect[1], stroke, fill));
        } else if (stroke) {
            for (PathSegment segm : path) {
                curpage.appendChild(createLineElement(segm.getX1(), segm.getY1(), segm.getX2(), segm.getY2()));
            }
        } else {
            Element pathImage = createPathImage(path);
            if (pathImage != null) {
                curpage.appendChild(pathImage);
            }
        }
    }

    @Override
    protected void renderImage(float x, float y, float width, float height, ImageResource resource) throws IOException {
        curpage.appendChild(createImageElement(x, y, width, height, resource));
    }

    //===========================================================================================
    /**
     * Creates an element that represents a single page.
     *
     * @return the resulting DOM element
     */
    protected Element createPageElement() {
        String pstyle = "";
        PDRectangle layout = getCurrentMediaBox();
        if (layout != null) {
            /*System.out.println("x1 " + layout.getLowerLeftX());
            System.out.println("y1 " + layout.getLowerLeftY());
            System.out.println("x2 " + layout.getUpperRightX());
            System.out.println("y2 " + layout.getUpperRightY());
            System.out.println("rot " + pdpage.findRotation());*/

            float w = layout.getWidth();
            float h = layout.getHeight();
            final int rot = pdpage.getRotation();
            if (rot == 90 || rot == 270) {
                float x = w;
                w = h;
                h = x;
            }

            pstyle = "width:" + w + UNIT + ";" + "height:" + h + UNIT + ";";
            pstyle += "overflow:hidden;";
        } else {
            MyBoxLog.console("No media box found");
        }

        Element el = doc.createElement("div");
        el.setAttribute("id", "page_" + (pagecnt++));
        el.setAttribute("class", "page");
        el.setAttribute("style", pstyle);
        return el;
    }

    /**
     * Creates an element that represents a single positioned box with no
     * content.
     *
     * @return the resulting DOM element
     */
    protected Element createTextElement(float width) {
        Element el = doc.createElement("div");
        el.setAttribute("id", "p" + (textcnt++));
        el.setAttribute("class", "p");
        String style = curstyle.toString();
        style += "width:" + width + UNIT + ";";
        el.setAttribute("style", style);
        return el;
    }

    /**
     * Creates an element that represents a single positioned box containing the
     * specified text string.
     *
     * @param data the text string to be contained in the created box.
     * @return the resulting DOM element
     */
    protected Element createTextElement(String data, float width) {
        Element el = createTextElement(width);
        Text text = doc.createTextNode(data);
        el.appendChild(text);
        return el;
    }

    /**
     * Creates an element that represents a rectangle drawn at the specified
     * coordinates in the page.
     *
     * @param x the X coordinate of the rectangle
     * @param y the Y coordinate of the rectangle
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     * @param stroke should there be a stroke around?
     * @param fill should the rectangle be filled?
     * @return the resulting DOM element
     */
    protected Element createRectangleElement(float x, float y, float width, float height, boolean stroke, boolean fill) {
        float lineWidth = transformWidth(getGraphicsState().getLineWidth());
        float wcor = stroke ? lineWidth : 0.0f;
        float strokeOffset = wcor == 0 ? 0 : wcor / 2;
        width = width - wcor < 0 ? 1 : width - wcor;
        height = height - wcor < 0 ? 1 : height - wcor;

        StringBuilder pstyle = new StringBuilder(50);
        pstyle.append("left:").append(style.formatLength(x - strokeOffset)).append(';');
        pstyle.append("top:").append(style.formatLength(y - strokeOffset)).append(';');
        pstyle.append("width:").append(style.formatLength(width)).append(';');
        pstyle.append("height:").append(style.formatLength(height)).append(';');

        if (stroke) {
            String color = colorString(getGraphicsState().getStrokingColor());
            pstyle.append("border:").append(style.formatLength(lineWidth)).append(" solid ").append(color).append(';');
        }

        if (fill) {
            String fcolor = colorString(getGraphicsState().getNonStrokingColor());
            pstyle.append("background-color:").append(fcolor).append(';');
        }

        Element el = doc.createElement("div");
        el.setAttribute("class", "r");
        el.setAttribute("style", pstyle.toString());
        el.appendChild(doc.createEntityReference("nbsp"));
        return el;
    }

    /**
     * Create an element that represents a horizntal or vertical line.
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return the created DOM element
     */
    protected Element createLineElement(float x1, float y1, float x2, float y2) {
        HtmlDivLine line = new HtmlDivLine(x1, y1, x2, y2, transformWidth(getGraphicsState().getLineWidth()));
        String color = colorString(getGraphicsState().getStrokingColor());

        StringBuilder pstyle = new StringBuilder(50);
        pstyle.append("left:").append(style.formatLength(line.getLeft())).append(';');
        pstyle.append("top:").append(style.formatLength(line.getTop())).append(';');
        pstyle.append("width:").append(style.formatLength(line.getWidth())).append(';');
        pstyle.append("height:").append(style.formatLength(line.getHeight())).append(';');
        pstyle.append(line.getBorderSide()).append(':').append(style.formatLength(line.getLineStrokeWidth())).append(" solid ").append(color).append(';');
        if (line.getAngleDegrees() != 0) {
            pstyle.append("transform:").append("rotate(").append(line.getAngleDegrees()).append("deg);");
        }

        Element el = doc.createElement("div");
        el.setAttribute("class", "r");
        el.setAttribute("style", pstyle.toString());
        el.appendChild(doc.createEntityReference("nbsp"));
        return el;
    }

    protected Element createPathImage(List<PathSegment> path) throws IOException {
        PathDrawer drawer = new PathDrawer(getGraphicsState());
        ImageResource renderedPath = drawer.drawPath(path);

        if (renderedPath != null) {
            return createImageElement((float) renderedPath.getX(), (float) renderedPath.getY(),
                    renderedPath.getWidth(), renderedPath.getHeight(), renderedPath);
        } else {
            return null;
        }
    }

    /**
     * Creates an element that represents an image drawn at the specified
     * coordinates in the page.
     *
     * @param x the X coordinate of the image
     * @param y the Y coordinate of the image
     * @param width the width coordinate of the image
     * @param height the height coordinate of the image
     * @param type the image type: <code>"png"</code> or <code>"jpeg"</code>
     * @param resource the image data depending on the specified type
     * @return
     */
    protected Element createImageElement(float x, float y, float width, float height, ImageResource resource) throws IOException {
        StringBuilder pstyle = new StringBuilder("position:absolute;");
        pstyle.append("left:").append(x).append(UNIT).append(';');
        pstyle.append("top:").append(y).append(UNIT).append(';');
        pstyle.append("width:").append(width).append(UNIT).append(';');
        pstyle.append("height:").append(height).append(UNIT).append(';');
        //pstyle.append("border:1px solid red;");

        Element el = doc.createElement("img");
        el.setAttribute("style", pstyle.toString());

        String imgSrc = config.getImageHandler().handleResource(resource);

        if (!disableImageData && !imgSrc.isEmpty()) {
            el.setAttribute("src", imgSrc);
        } else {
            el.setAttribute("src", "");
        }

        return el;
    }

    /**
     * Generate the global CSS style for the whole document.
     *
     * @return the CSS code used in the generated document header
     */
    protected String createGlobalStyle() {
        StringBuilder ret = new StringBuilder();
        ret.append(createFontFaces());
        ret.append("\n");
        ret.append(defaultStyle);
        return ret.toString();
    }

    @Override
    protected void updateFontTable() {
        // skip font processing completley if ignore fonts mode to optimize processing speed
        if (!(config.getFontHandler() instanceof IgnoreResourceHandler)) {
            super.updateFontTable();
        }
    }

    protected String createFontFaces() {
        StringBuilder ret = new StringBuilder();
        for (FontTable.Entry font : fontTable.getEntries()) {
            createFontFace(ret, font);
        }

        return ret.toString();
    }

    private void createFontFace(StringBuilder ret, FontTable.Entry font) {
        try {
            final String src = config.getFontHandler().handleResource(font);
            if (src != null && !src.trim().isEmpty()) {
                ret.append("@font-face {");
                ret.append("font-family:\"").append(font.usedName).append("\";");
                ret.append("src:url('");
                ret.append(src);
                ret.append("');");
                ret.append("}\n");
            }
        } catch (IOException e) {
            MyBoxLog.console("Error writing font face data for font: " + font.getName()
                    + "Exception: " + e.getMessage() + "\n" + e.getClass()
            );
        }
    }
}
