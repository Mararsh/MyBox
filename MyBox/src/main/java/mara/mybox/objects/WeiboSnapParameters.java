package mara.mybox.objects;

import java.io.File;
import java.util.Date;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * @Author Mara
 * @CreateDate 2018-9-13 22:17:17
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class WeiboSnapParameters {

    private File targetPath;
    private int loadDelay, scrollDelay, maxDelay, retry;
    private boolean imagePerScreen, isImageSize, addPageNumber, createPDF, createHtml, keepPagePdf;
    private boolean miao, expandComments, fullScreen;
    private String webAddress, author, title;
    private int marginSize, pageWidth, pageHeight, jpegQuality, format, threshold, maxMergeSize;
    private PDRectangle pageSize;
    private Date startMonth, endMonth;

    public File getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(File targetPath) {
        this.targetPath = targetPath;
    }

    public int getLoadDelay() {
        return loadDelay;
    }

    public void setLoadDelay(int loadDelay) {
        this.loadDelay = loadDelay;
    }

    public int getScrollDelay() {
        return scrollDelay;
    }

    public void setScrollDelay(int scrollDelay) {
        this.scrollDelay = scrollDelay;
    }

    public int getMaxDelay() {
        return maxDelay;
    }

    public void setMaxDelay(int maxDelay) {
        this.maxDelay = maxDelay;
    }

    public boolean isImagePerScreen() {
        return imagePerScreen;
    }

    public void setImagePerScreen(boolean imagePerScreen) {
        this.imagePerScreen = imagePerScreen;
    }

    public boolean isIsImageSize() {
        return isImageSize;
    }

    public void setIsImageSize(boolean isImageSize) {
        this.isImageSize = isImageSize;
    }

    public String getWebAddress() {
        return webAddress;
    }

    public void setWebAddress(String webAddress) {
        this.webAddress = webAddress;
    }

    public Date getStartMonth() {
        return startMonth;
    }

    public void setStartMonth(Date startMonth) {
        this.startMonth = startMonth;
    }

    public Date getEndMonth() {
        return endMonth;
    }

    public void setEndMonth(Date endMonth) {
        this.endMonth = endMonth;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getMarginSize() {
        return marginSize;
    }

    public void setMarginSize(int marginSize) {
        this.marginSize = marginSize;
    }

    public int getPageWidth() {
        return pageWidth;
    }

    public void setPageWidth(int pageWidth) {
        this.pageWidth = pageWidth;
    }

    public int getPageHeight() {
        return pageHeight;
    }

    public void setPageHeight(int pageHeight) {
        this.pageHeight = pageHeight;
    }

    public int getJpegQuality() {
        return jpegQuality;
    }

    public void setJpegQuality(int jpegQuality) {
        this.jpegQuality = jpegQuality;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public PDRectangle getPageSize() {
        return pageSize;
    }

    public void setPageSize(PDRectangle pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isAddPageNumber() {
        return addPageNumber;
    }

    public void setAddPageNumber(boolean addPageNumber) {
        this.addPageNumber = addPageNumber;
    }

    public boolean isCreatePDF() {
        return createPDF;
    }

    public void setCreatePDF(boolean createPDF) {
        this.createPDF = createPDF;
    }

    public boolean isCreateHtml() {
        return createHtml;
    }

    public void setCreateHtml(boolean createHtml) {
        this.createHtml = createHtml;
    }

    public boolean isKeepPagePdf() {
        return keepPagePdf;
    }

    public void setKeepPagePdf(boolean keepPagePdf) {
        this.keepPagePdf = keepPagePdf;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isMiao() {
        return miao;
    }

    public void setMiao(boolean miao) {
        this.miao = miao;
    }

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public int getMaxMergeSize() {
        return maxMergeSize;
    }

    public void setMaxMergeSize(int maxMergeSize) {
        this.maxMergeSize = maxMergeSize;
    }

    public boolean isExpandComments() {
        return expandComments;
    }

    public void setExpandComments(boolean expandComments) {
        this.expandComments = expandComments;
    }

    public boolean isFullScreen() {
        return fullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
    }

}
