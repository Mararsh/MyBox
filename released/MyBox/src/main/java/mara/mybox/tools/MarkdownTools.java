package mara.mybox.tools;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.ImageRef;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.Link;

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

}
