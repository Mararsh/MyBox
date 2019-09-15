package mara.mybox.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Mara
 * @CreateDate 2019-6-11 9:27:57
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class PDFOutline {

    private String title;
    private int pageNumber;
    private List<PDFOutline> children;

    public PDFOutline(String title, int pageNumber) {
        this.title = title;
        this.pageNumber = pageNumber;
    }

    public List<PDFOutline> addChild(String title, int pageNumber) {
        PDFOutline child = new PDFOutline(title, pageNumber);
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
        return children;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public List<PDFOutline> getChildren() {
        return children;
    }

    public void setChildren(List<PDFOutline> children) {
        this.children = children;
    }

}
