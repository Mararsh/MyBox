package mara.mybox.objects;

import java.io.File;
import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

/**
 * @Author Mara
 * @CreateDate 2018-6-9 12:18:38
 * @Version 1.0
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

    public PdfInformation(File file) {
        this.file = file;
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

    public void loadInformation() {
        try {
            System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
            try (PDDocument doc = PDDocument.load(file)) {
                PDDocumentInformation info = doc.getDocumentInformation();
                if (info.getCreationDate() != null) {
                    createTime = info.getCreationDate().getTime();
                }
                if (info.getModificationDate() != null) {
                    modifyTime = info.getModificationDate().getTime();
                }
                creator = info.getCreator();
                producer = info.getProducer();
                title = info.getTitle();
                subject = info.getSubject();
                author = info.getAuthor();
                numberOfPages = doc.getNumberOfPages();
                version = doc.getVersion();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
