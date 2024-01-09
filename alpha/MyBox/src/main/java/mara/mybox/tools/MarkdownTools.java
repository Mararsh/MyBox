package mara.mybox.tools;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.ImageRef;
import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mara.mybox.data.Link;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.value.UserConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * @Author Mara
 * @CreateDate 2020-10-21
 * @License Apache License Version 2.0
 */
public class MarkdownTools {

    public static String string(BasedSequence string) {
        if (string == null) {
            return null;
        }
        try {
            return URLDecoder.decode(string.toStringOrNull(), "UTF-8");
        } catch (Exception e) {
            return null;
        }
    }

    //  https://github.com/vsch/flexmark-java/blob/master/flexmark-java-samples/src/com/vladsch/flexmark/java/samples/TitleExtract.java
    public static void links(Node node, List<Link> links) {
        if (node == null || links == null) {
            return;
        }
        if (node instanceof com.vladsch.flexmark.ast.Link) {
            com.vladsch.flexmark.ast.Link mdLink = (com.vladsch.flexmark.ast.Link) node;
            try {
                Link link = Link.create()
                        .setAddress(string(mdLink.getUrl()))
                        .setName(string(mdLink.getText()))
                        .setTitle(string(mdLink.getTitle()))
                        .setIndex(links.size());
                links.add(link);
            } catch (Exception e) {
            }

        }
        if (node instanceof Block && node.hasChildren()) {
            Node child = node.getFirstChild();
            while (child != null) {
                links(child, links);
                child = child.getNext();
            }
        }
    }

    public static void heads(Node node, List<Heading> heads) {
        if (node == null || heads == null) {
            return;
        }
        if (node instanceof Heading) {
            Heading head = (Heading) node;
            heads.add(head);
        }
        if (node instanceof Block && node.hasChildren()) {
            Node child = node.getFirstChild();
            while (child != null) {
                heads(child, heads);
                child = child.getNext();
            }
        }
    }

    public static String toc(Node node, int indentSize) {
        if (node == null) {
            return null;
        }
        List<Heading> heads = new ArrayList<>();
        heads(node, heads);
        String toc = "";
        for (Heading head : heads) {
            for (int i = 0; i < head.getLevel() * indentSize; i++) {
                toc += " ";
            }
            toc += head.getText() + "\n";
        }
        return toc;
    }

    public static void images(Node node, List<Image> images) {
        if (node == null || images == null) {
            return;
        }
        if (node instanceof Image) {
            Image image = (Image) node;
            images.add(image);
        }
        if (node instanceof Block && node.hasChildren()) {
            Node child = node.getFirstChild();
            while (child != null) {
                images(child, images);
                child = child.getNext();
            }
        }
    }

    public static void imageRefs(Node node, List<ImageRef> imageRefs) {
        if (node == null || imageRefs == null) {
            return;
        }
        if (node instanceof ImageRef) {
            ImageRef imageRef = (ImageRef) node;
            imageRefs.add(imageRef);
        }
        if (node instanceof Block && node.hasChildren()) {
            Node child = node.getFirstChild();
            while (child != null) {
                imageRefs(child, imageRefs);
                child = child.getNext();
            }
        }
    }

    public static MutableDataHolder htmlOptions(String emulation,
            int indentSize, boolean trim, boolean discard, boolean append) {
        try {
            MutableDataHolder htmlOptions = new MutableDataSet();
            htmlOptions.setFrom(ParserEmulationProfile.valueOf(emulation));
            htmlOptions.set(Parser.EXTENSIONS, Arrays.asList(
                    AbbreviationExtension.create(),
                    DefinitionExtension.create(),
                    FootnoteExtension.create(),
                    TypographicExtension.create(),
                    TablesExtension.create()
            ));
            htmlOptions.set(HtmlRenderer.INDENT_SIZE, indentSize)
                    //                    .set(HtmlRenderer.PERCENT_ENCODE_URLS, true)
                    //                    .set(TablesExtension.COLUMN_SPANS, false)
                    .set(TablesExtension.TRIM_CELL_WHITESPACE, trim)
                    .set(TablesExtension.DISCARD_EXTRA_COLUMNS, discard)
                    .set(TablesExtension.APPEND_MISSING_COLUMNS, append);

            return htmlOptions;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static MutableDataHolder htmlOptions() {
        try (Connection conn = DerbyBase.getConnection()) {
            return htmlOptions(UserConfig.getString(conn, "MarkdownEmulation", "PEGDOWN"),
                    UserConfig.getInt(conn, "MarkdownIndent", 4),
                    UserConfig.getBoolean(conn, "MarkdownTrim", false),
                    UserConfig.getBoolean(conn, "MarkdownDiscard", false),
                    UserConfig.getBoolean(conn, "MarkdownAppend", false));
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }

    }

    public static String md2html(FxTask task, MutableDataHolder htmlOptions, File mdFile, String style) {
        try {
            if (mdFile == null || !mdFile.exists()) {
                return null;
            }
            Parser htmlParser = Parser.builder(htmlOptions).build();
            HtmlRenderer htmlRender = HtmlRenderer.builder(htmlOptions).build();
            Node document = htmlParser.parse(TextFileTools.readTexts(task, mdFile));
            String html = htmlRender.render(document);
            Document doc = Jsoup.parse(html);
            if (doc == null) {
                return null;
            }
            HtmlWriteTools.setCharset(task, doc, Charset.forName("UTF-8"));
            if (task != null && !task.isWorking()) {
                return null;
            }
            doc.head().appendChild(new Element("style").text(style));
            return doc.outerHtml();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
