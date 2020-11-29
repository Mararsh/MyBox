package mara.mybox.data;

import java.awt.image.BufferedImage;
import java.io.File;
import mara.mybox.tools.PdfTools;
import mara.mybox.value.AppVariables;
import mara.mybox.dev.MyBoxLog;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * @Author Mara
 * @CreateDate 2018-6-9 12:18:38
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfInformation extends FileInformation {

    protected String userPassword, ownerPassword, title, subject, author, creator, producer, keywords;
    protected float version;
    protected int numberOfPages, fromPage, toPage;
    protected String firstPageSize, firstPageSize2;
    protected PDDocument doc;
    protected PDDocumentOutline outline;
    protected AccessPermission access;
    protected boolean infoLoaded;

    public PdfInformation() {
    }

    public PdfInformation(File file) {
        super(file);
        fromPage = 1;
        toPage = -1;
        doc = null;
    }

    public void openDocument(String password) {
        try {
            this.userPassword = password;
            if (doc == null) {
                doc = PDDocument.load(file, password, AppVariables.pdfMemUsage);
            }
            infoLoaded = false;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void closeDocument() {
        try {
            if (doc != null) {
                doc.close();
            }
            doc = null;
            infoLoaded = false;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadInformation() {
        try {
            if (doc == null) {
                return;
            }

            PDDocumentInformation docInfo = doc.getDocumentInformation();
            if (docInfo.getCreationDate() != null) {
                createTime = docInfo.getCreationDate().getTimeInMillis();
            }
            if (docInfo.getModificationDate() != null) {
                modifyTime = docInfo.getModificationDate().getTimeInMillis();
            }
            creator = docInfo.getCreator();
            producer = docInfo.getProducer();
            title = docInfo.getTitle();
            subject = docInfo.getSubject();
            author = docInfo.getAuthor();
            numberOfPages = doc.getNumberOfPages();
            keywords = docInfo.getKeywords();
            version = doc.getVersion();
            access = doc.getCurrentAccessPermission();

            PDPage page = doc.getPage(0);
            String size = "";
            PDRectangle box = page.getMediaBox();
            if (box != null) {
                size += "MediaBox: " + PdfTools.pixels2mm(box.getWidth()) + "mm * "
                        + PdfTools.pixels2mm(box.getHeight()) + "mm";
            }
            box = page.getTrimBox();
            if (box != null) {
                size += "  TrimBox: " + PdfTools.pixels2mm(box.getWidth()) + "mm * "
                        + PdfTools.pixels2mm(box.getHeight()) + "mm";
            }
            firstPageSize = size;
            size = "";
            box = page.getCropBox();
            if (box != null) {
                size += "CropBox: " + +PdfTools.pixels2mm(box.getWidth()) + "mm * "
                        + PdfTools.pixels2mm(box.getHeight()) + "mm";
            }
            box = page.getBleedBox();
            if (box != null) {
                size += "  BleedBox: " + +PdfTools.pixels2mm(box.getWidth()) + "mm * "
                        + PdfTools.pixels2mm(box.getHeight()) + "mm";
            }
            firstPageSize2 = size;
            outline = doc.getDocumentCatalog().getDocumentOutline();
            infoLoaded = true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadInfo(String password) {
        try {
            openDocument(password);
            if (doc == null) {
                return;
            }
            loadInformation();
            closeDocument();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void readInfo(PDDocument doc) {
        this.doc = doc;
        loadInformation();
    }

    public BufferedImage readPageAsImage(int page) {
        return readPageAsImage(page, ImageType.ARGB);
    }

    public BufferedImage readPageAsImage(int page, ImageType imageType) {
        try {
            if (doc == null) {
                return null;
            }
            PDFRenderer renderer = new PDFRenderer(doc);
            BufferedImage image = renderer.renderImage(page, 1, imageType);
            return image;
        } catch (Exception e) {
            return null;
        }
    }

    /*
        get/set
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public float getVersion() {
        return version;
    }

    public void setVersion(float version) {
        this.version = version;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getFirstPageSize() {
        return firstPageSize;
    }

    public void setFirstPageSize(String firstPageSize) {
        this.firstPageSize = firstPageSize;
    }

    public String getFirstPageSize2() {
        return firstPageSize2;
    }

    public void setFirstPageSize2(String firstPageSize2) {
        this.firstPageSize2 = firstPageSize2;
    }

    public PDDocument getDoc() {
        return doc;
    }

    public void setDoc(PDDocument doc) {
        this.doc = doc;
    }

    public boolean isInfoLoaded() {
        return infoLoaded;
    }

    public void setInfoLoaded(boolean infoLoaded) {
        this.infoLoaded = infoLoaded;
    }

    public PDDocumentOutline getOutline() {
        return outline;
    }

    public void setOutline(PDDocumentOutline outline) {
        this.outline = outline;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public AccessPermission getAccess() {
        return access;
    }

    public void setAccess(AccessPermission access) {
        this.access = access;
    }

    public String getOwnerPassword() {
        return ownerPassword;
    }

    public void setOwnerPassword(String ownerPassword) {
        this.ownerPassword = ownerPassword;
    }

    public int getFromPage() {
        return fromPage;
    }

    public void setFromPage(int fromPage) {
        this.fromPage = fromPage;
    }

    public int getToPage() {
        return toPage;
    }

    public void setToPage(int toPage) {
        this.toPage = toPage;
    }

}
