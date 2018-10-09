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
    private int webWidth, retry;
    private boolean imagePerScreen, isImageSize, addPageNumber, createPDF, createHtml, savePictures, keepPagePdf;
    private boolean miao, expandComments, expandPicture, fullScreen, openPathWhenStop, useTempFiles;
    private String webAddress, author, title, fontName;
    private int marginSize, pageWidth, pageHeight, jpegQuality, format, threshold, maxMergeSize, category, pdfScale;
    private PDRectangle pageSize;
    private Date startMonth, endMonth;
    private float zoomScale;
    private File tempdir;

    public static class FileCategoryType {

        public static int InMonthsPaths = 0;
        public static int InYearsPaths = 1;
        public static int InOnePath = 2;
    }

    public File getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(File targetPath) {
        this.targetPath = targetPath;
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

    public boolean isSavePictures() {
        return savePictures;
    }

    public void setSavePictures(boolean savePictures) {
        this.savePictures = savePictures;
    }

    public boolean isExpandPicture() {
        return expandPicture;
    }

    public void setExpandPicture(boolean expandPicture) {
        this.expandPicture = expandPicture;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getWebWidth() {
        return webWidth;
    }

    public void setWebWidth(int webWidth) {
        this.webWidth = webWidth;
    }

    public float getZoomScale() {
        return zoomScale;
    }

    public void setZoomScale(float zoomScale) {
        this.zoomScale = zoomScale;
    }

    public File getTempdir() {
        return tempdir;
    }

    public void setTempdir(File tempdir) {
        if (tempdir == null || !tempdir.exists() || !tempdir.isDirectory()) {
            this.tempdir = new File(System.getProperty("user.home"));
        } else {
            this.tempdir = tempdir;
        }
    }

    public int getPdfScale() {
        return pdfScale;
    }

    public void setPdfScale(int pdfScale) {
        this.pdfScale = pdfScale;
    }

    public boolean isOpenPathWhenStop() {
        return openPathWhenStop;
    }

    public void setOpenPathWhenStop(boolean openPathWhenStop) {
        this.openPathWhenStop = openPathWhenStop;
    }

    public boolean isUseTempFiles() {
        return useTempFiles;
    }

    public void setUseTempFiles(boolean useTempFiles) {
        this.useTempFiles = useTempFiles;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

}
