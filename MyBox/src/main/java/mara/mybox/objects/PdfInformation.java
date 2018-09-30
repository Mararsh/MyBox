package mara.mybox.objects;

import java.io.File;
import java.util.Date;
import mara.mybox.tools.PdfTools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * @Author Mara
 * @CreateDate 2018-6-9 12:18:38
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfInformation {

    private static final Logger logger = LogManager.getLogger();

    private File file;
    private String title, subject, author, creator, producer;
    private float version;
    private int numberOfPages;
    private Date createTime, modifyTime;
    private String firstPageSize, firstPageSize2;

    public PdfInformation() {
    }

    public PdfInformation(File file) {
        this.file = file;
    }

    public void loadDocument(String password) {
        try {
            try (PDDocument doc = PDDocument.load(file, password, AppVaribles.PdfMemUsage)) {
                PDDocumentInformation docInfo = doc.getDocumentInformation();
                if (docInfo.getCreationDate() != null) {
                    createTime = docInfo.getCreationDate().getTime();
                }
                if (docInfo.getModificationDate() != null) {
                    modifyTime = docInfo.getModificationDate().getTime();
                }
                creator = docInfo.getCreator();
                producer = docInfo.getProducer();
                title = docInfo.getTitle();
                subject = docInfo.getSubject();
                author = docInfo.getAuthor();
                numberOfPages = doc.getNumberOfPages();
                version = doc.getVersion();

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
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
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

}
