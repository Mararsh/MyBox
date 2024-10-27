package mara.mybox.data;

import java.io.File;
import java.util.Date;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.PdfTools.PdfImageFormat;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2018-9-13 22:17:17
 * @License Apache License Version 2.0
 */
public class WeiboSnapParameters {

    private File targetPath = new File(FileTmpTools.generatePath("pdf"));
    private int webWidth, retry, startPage, loadInterval, snapInterval, likeStartPage, retried, fontSize = 20;
    private boolean imagePerScreen, isImageSize, addPageNumber, createPDF, createHtml, savePictures, keepPagePdf;
    private boolean miao, expandComments, expandPicture, fullScreen, openPathWhenStop, useTempFiles, dithering;
    private String webAddress, author, title, fontFile;
    private int marginSize, pageWidth, pageHeight, jpegQuality, threshold, maxMergeSize, category, pdfScale, dpi;
    private Date startMonth, endMonth;
    private float zoomScale;
    private File tempdir;
    private PdfImageFormat format;

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

    public PdfImageFormat getFormat() {
        return format;
    }

    public void setFormat(PdfImageFormat format) {
        this.format = format;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
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
            this.tempdir = AppVariables.MyBoxTempPath;
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

    public String getFontFile() {
        return fontFile;
    }

    public void setFontFile(String fontFile) {
        this.fontFile = fontFile;
    }

    public boolean isDithering() {
        return dithering;
    }

    public void setDithering(boolean dithering) {
        this.dithering = dithering;
    }

    public int getStartPage() {
        return startPage;
    }

    public void setStartPage(int startPage) {
        this.startPage = startPage;
    }

    public int getLoadInterval() {
        return loadInterval;
    }

    public void setLoadInterval(int loadInterval) {
        this.loadInterval = loadInterval;
    }

    public int getSnapInterval() {
        return snapInterval;
    }

    public void setSnapInterval(int snapInterval) {
        this.snapInterval = snapInterval;
    }

    public int getDpi() {
        return dpi;
    }

    public void setDpi(int dpi) {
        this.dpi = dpi;
    }

    public int getLikeStartPage() {
        return likeStartPage;
    }

    public void setLikeStartPage(int likeStartPage) {
        this.likeStartPage = likeStartPage;
    }

    public int getRetried() {
        return retried;
    }

    public void setRetried(int retried) {
        this.retried = retried;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

}
