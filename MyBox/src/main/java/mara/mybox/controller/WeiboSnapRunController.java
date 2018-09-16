package mara.mybox.controller;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.WeiboSnapParameters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FxmlImageTools;
import mara.mybox.tools.FxmlTools;
import mara.mybox.tools.PdfTools;
import mara.mybox.tools.SoundTools;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

/**
 * @Author Mara
 * @CreateDate 2018-9-13
 * @Description
 * @License Apache License Version 2.0
 */
public class WeiboSnapRunController extends BaseController {

    private final String WeiboLastStartMonthKey;
    private WebEngine webEngine;
    private List<Image> images;
    private WeiboSnapParameters parameters;
    private int snapHeight, snapCount, pageHeight, screenHeight, screenWidth, currentPage, pageCount;
    private int htmlCount, monthPdfCount, pagePdfCount, windowWidth, completedMonthsCount, totalMonthsCount;
    private Timer loadTimer, snapTimer;
    private long loadStartTime, snapStartTime, startTime;
    private boolean loadFailed, loadCompleted, snapFailed, snapCompleted;
    private WeiboSnapingInfoController loadingController;
    private Stage loadingStage;
    private String pdfFilename, htmlFilename, currentMonthString, accountName, setFontScript, baseText, errorString;
    private String currentAddress;
    private Date currentMonth, firstMonth, lastMonth;
    private File rootPath, pdfPath, htmlPath;
    private WeiboSnapController parent;
    private Map<String, List<File>> pdfs;

    @FXML
    private TextField bottomText;
    @FXML
    private WebView webView;

    public WeiboSnapRunController() {
        WeiboLastStartMonthKey = "WeiboLastStartMonthKey";

    }

    @Override
    protected void initializeNext() {
        webEngine = webView.getEngine();

    }

    public void start(WeiboSnapParameters parameters) {
        try {
            this.parameters = parameters;
            getMyStage();
            logger.debug(parameters.getWebAddress());
            if (parameters.getWebAddress() == null || parameters.getWebAddress().isEmpty()) {
                closeStage();
                return;
            }

            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            if (parameters.getWebWidth() <= 0) {
                myStage.setX(0);
                windowWidth = (int) primaryScreenBounds.getWidth();
            } else {
                windowWidth = parameters.getWebWidth();
            }
            myStage.setWidth(windowWidth);
            myStage.setY(0);
            myStage.setHeight(primaryScreenBounds.getHeight());

            htmlCount = monthPdfCount = pagePdfCount = completedMonthsCount = 0;
            loadFailed = loadCompleted = snapFailed = snapCompleted = false;
            startTime = new Date().getTime();

            loadMain();
        } catch (Exception e) {
            alertError(e.toString());
            closeStage();
        }

    }

    private boolean openLoadingStage() {
        try {
            if (loadingStage != null && loadingController != null) {
                return true;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.WeiboSnapingInfoFxml), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            loadingController = fxmlLoader.getController();
            loadingController.setParent(this);
            loadingStage = new Stage();

            loadingStage.initModality(Modality.WINDOW_MODAL);
            loadingStage.initStyle(StageStyle.TRANSPARENT);
            loadingStage.initOwner(getMyStage());
            loadingStage.setScene(new Scene(pane));
            loadingStage.show();
            return true;

        } catch (Exception e) {
            alertError(e.toString());
            closeStage();
            return false;
        }
    }

    private void loadMain() {
        try {
            webEngine.load(parameters.getWebAddress());
            logger.debug(parameters.getWebAddress());

            if (!openLoadingStage()) {
                return;
            }
            loadingController.setInfo(AppVaribles.getMessage("CheckingWeiBoMain"));

            loadFailed = loadCompleted = false;
            accountName = null;
            firstMonth = lastMonth = null;

            if (loadTimer != null) {
                loadTimer.cancel();
            }
            loadTimer = new Timer();
            loadStartTime = new Date().getTime();
            loadTimer.schedule(new TimerTask() {
                int lastHeight = 0, newHeight = -1;
                private String contents;

                @Override
                public void run() {
                    try {
                        if (new Date().getTime() - loadStartTime >= parameters.getMaxDelay()) {
                            loadFailed = loadCompleted = true;
//                            errorString = AppVaribles.getMessage("OverTime");
                        }
                        if (loadFailed || loadCompleted) {
                            quit();
                            return;
                        }
                        lastHeight = newHeight;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    newHeight = (Integer) webEngine.executeScript("document.documentElement.scrollHeight || document.body.scrollHeight;");
                                    contents = (String) webEngine.executeScript("document.documentElement.outerHTML");
                                    loadingController.setText(AppVaribles.getMessage("PageHeightLoaded") + ": " + newHeight);
                                    loadingController.addLine(AppVaribles.getMessage("CharactersLoaded") + ": " + +contents.length());

                                    if (contents.contains("帐号登录")) {
                                        loadFailed = loadCompleted = true;
                                        errorString = AppVaribles.getMessage("NonExistedAccount");
                                        return;
                                    }
                                    int posAccount1 = contents.indexOf("<title>");
                                    int posAccount2 = contents.indexOf("_微博</title>");
                                    if (posAccount1 > 0 && posAccount2 > 0) {
                                        accountName = contents.substring(posAccount1 + "<title>".length(), posAccount2);
                                        int posfirst1 = contents.indexOf("&stat_date=");
                                        if (posfirst1 > 0) {
                                            String s = contents.substring(posfirst1 + "&stat_date=".length());
                                            int posfirst2 = s.indexOf("\\");
                                            if (posfirst2 > 0) {
                                                try {
                                                    s = s.substring(0, posfirst2);
                                                    lastMonth = DateTools.parseMonth(s.substring(0, 4) + "-" + s.substring(4, 6));
                                                    logger.debug(DateTools.datetimeToString(lastMonth));
                                                    int posLast1 = contents.lastIndexOf("&stat_date=");
                                                    if (posLast1 > 0) {
                                                        s = contents.substring(posLast1 + "&stat_date=".length());
                                                        int posLast2 = s.indexOf("&page=");
                                                        if (posLast2 > 0) {
                                                            try {
                                                                s = s.substring(0, posLast2);
                                                                firstMonth = DateTools.parseMonth(s.substring(0, 4) + "-" + s.substring(4, 6));
                                                                logger.debug(DateTools.datetimeToString(firstMonth));
                                                                loadCompleted = true;
                                                            } catch (Exception e) {
                                                            }
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                }
                                            }
                                        }
                                    }
                                    if (!loadCompleted) {
                                        webEngine.executeScript("window.scrollTo(0," + newHeight + ");");
                                    }
                                } catch (Exception e) {
//                                    loadFailed = loadCompleted = true;
//                                    errorString = e.toString();
                                    logger.error(e.toString());
                                }
                            }
                        });
                    } catch (Exception e) {
                        loadFailed = loadCompleted = true;
                        logger.error(e.toString());
                        errorString = e.toString();
                        quit();
                    }
                }

                private void quit() {
                    this.cancel();
                    loadCompleted = true;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (accountName == null || firstMonth == null || lastMonth == null) {
                                if (errorString == null) {
                                    errorString = AppVaribles.getMessage("NonExistedAccount");
                                }
                                loadFailed = true;
                            }
                            if (loadFailed) {
                                alertError(errorString);
                                closeStage();
                            } else {
                                loadPages();
                            }
                        }
                    });
                }

            }, parameters.getLoadDelay(), parameters.getLoadDelay());

        } catch (Exception e) {
            alertError(e.toString());
            endSnap();
        }

    }

    private void loadPages() {
        try {
            rootPath = new File(parameters.getTargetPath().getAbsolutePath() + File.separator + accountName);
            if (!rootPath.exists()) {
                rootPath.mkdirs();
            }
            if (parameters.isCreatePDF()) {
                pdfPath = new File(rootPath.getAbsolutePath() + File.separator + "pdf");
                if (!pdfPath.exists()) {
                    pdfPath.mkdirs();
                }
            }
            if (parameters.isCreateHtml()) {
                htmlPath = new File(rootPath.getAbsolutePath() + File.separator + "html");
                if (!htmlPath.exists()) {
                    htmlPath.mkdirs();
                }
            }
            if (parameters.getStartMonth().getTime() < firstMonth.getTime()) {
                parameters.setStartMonth(firstMonth);
            }
            if (parameters.getEndMonth().getTime() > lastMonth.getTime()) {
                parameters.setEndMonth(lastMonth);
            }
            Calendar c = Calendar.getInstance();
            Date m = parameters.getStartMonth();
            c.setTime(m);
            totalMonthsCount = 0;
            while (m.getTime() <= parameters.getEndMonth().getTime()) {
                totalMonthsCount++;
                c.add(Calendar.MONTH, 1);
                m = c.getTime();
            }

            currentMonth = parameters.getStartMonth();
            currentPage = 0;
            pageCount = 1;
            loadFailed = loadCompleted = false;
            pdfs = new HashMap<>();

            loadNextPage();
        } catch (Exception e) {
            alertError(e.toString());
            closeStage();
        }
    }

    private void loadNextPage() {
        try {
            currentPage++;
            Calendar c = Calendar.getInstance();
            c.setTime(currentMonth);
            if (currentPage > pageCount) {
                completedMonthsCount++;
                if (parameters.isCreatePDF()) {
                    mergeMonthPdf();
                }
                c.setTime(currentMonth);
                c.add(Calendar.MONTH, 1);
                currentMonth = c.getTime();
                if (currentMonth.getTime() > parameters.getEndMonth().getTime()) {
                    miao();
                    if (parent != null) {
                        parent.popInformation(AppVaribles.getMessage("MissCompleted"));
                    }
                    alertInformation(AppVaribles.getMessage("MissCompleted"));
                    Desktop.getDesktop().browse(rootPath.toURI());
                    endSnap();
                    return;
                }
                currentPage = 0;
                pageCount = 1;
                loadNextPage();
                return;
            }
            currentMonthString = DateTools.dateToMonthString(currentMonth);
            currentAddress = parameters.getWebAddress() + "?is_all=1&stat_date="
                    + currentMonthString.replace("-", "")
                    + "&page=" + currentPage;
            if (parameters.isCreatePDF()) {
                pdfFilename = pdfPath.getAbsolutePath() + File.separator + accountName + "-"
                        + currentMonthString + "-第" + currentPage + "页.pdf";
            }
            if (parameters.isCreateHtml()) {
                htmlFilename = htmlPath.getAbsolutePath() + File.separator + accountName + "-"
                        + currentMonthString + "-第" + currentPage + "页.html";
            }
            parameters.setTitle(accountName + "-" + currentMonthString + "-第" + currentPage + "页");

            loadPage(currentAddress);
        } catch (Exception e) {
            errorString = e.toString();
            pageFailed(loadingController);
        }
    }

    private void setBaseInfo() {

        if (!openLoadingStage()) {
            return;
        }
        loadingController.setText(AppVaribles.getMessage("WeiboAddress") + ": " + parameters.getWebAddress());
        loadingController.addLine(AppVaribles.getMessage("Account") + ": " + accountName);
        loadingController.addLine(AppVaribles.getMessage("FirstWeiboMonth") + ": " + DateTools.dateToMonthString(firstMonth));
        loadingController.addLine(AppVaribles.getMessage("LastWeiboMonth") + ": " + DateTools.dateToMonthString(lastMonth));
        loadingController.addLine(AppVaribles.getMessage("SnapDuration") + ": "
                + DateTools.dateToMonthString(parameters.getStartMonth()) + " ~ " + DateTools.dateToMonthString(parameters.getEndMonth()));
        loadingController.addLine(AppVaribles.getMessage("MonthsNumberWillSnap") + ": " + totalMonthsCount);
        long passed = new Date().getTime() - startTime;
        loadingController.addLine(AppVaribles.getMessage("SnapingStartTime") + ": " + DateTools.datetimeToString(startTime)
                + " (" + AppVaribles.getMessage("ElapsedTime") + ": " + DateTools.showTime(passed) + ")");
        loadingController.addLine(AppVaribles.getMessage("CompletedMonths") + ": " + completedMonthsCount);
        loadingController.addLine(AppVaribles.getMessage("PdfFilesSaved") + ": " + (pagePdfCount + monthPdfCount));
        loadingController.addLine(AppVaribles.getMessage("HtmlFilesSaved") + ": " + htmlCount);
        if (completedMonthsCount > 0) {
            long speed = passed / completedMonthsCount;
            loadingController.addLine(AppVaribles.getMessage("SpeedOfSnapingMonth") + ": " + DateTools.showTime(speed));
            long total = passed * totalMonthsCount / completedMonthsCount;
            long left = passed * (totalMonthsCount - completedMonthsCount) / completedMonthsCount;
            Calendar c = Calendar.getInstance();
            c.setTime(new Date(startTime));
            c.add(Calendar.SECOND, (int) (total / 1000));
            loadingController.addLine(AppVaribles.getMessage("PredictedCompleteTime") + ": " + DateTools.datetimeToString(c.getTime())
                    + " (" + AppVaribles.getMessage("LeftTime") + ": " + DateTools.showTime(left) + ")");
        }
        loadingController.addLine(AppVaribles.getMessage("CurrentLoadingMonth") + ": " + DateTools.dateToMonthString(currentMonth));
        loadingController.addLine(AppVaribles.getMessage("PagesNumberThisMonth") + ": " + pageCount);
        loadingController.addLine(AppVaribles.getMessage("CurrentLoadingPage") + ": " + currentPage);

    }

    private void loadPage(String address) {
        try {
            webEngine.load(address);
            logger.debug(address);
            if (loadTimer != null) {
                loadTimer.cancel();
            }

            if (!openLoadingStage()) {
                return;
            }
            loadingController.setInfo(AppVaribles.getMessage("LoadingPage"));
            setBaseInfo();
            if (parameters.isCreateHtml()) {
                loadingController.addLine(AppVaribles.getMessage("LoadingForHtml") + ": " + htmlFilename);
            }
            baseText = loadingController.getText();

            loadTimer = new Timer();
            loadStartTime = new Date().getTime();
            loadFailed = loadCompleted = false;
            loadTimer.schedule(new TimerTask() {
                int lastHeight = 0, newHeight = -1;
                boolean emptyPage = false;
                String contents;

                @Override
                public void run() {
                    try {
                        if (new Date().getTime() - loadStartTime >= parameters.getMaxDelay()) {
                            loadFailed = loadCompleted = true;
                            errorString = AppVaribles.getMessage("OverTime");
                        }
                        if (loadFailed || loadCompleted) {
                            quit();
                            return;
                        }
                        lastHeight = newHeight;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    newHeight = (Integer) webEngine.executeScript("document.documentElement.scrollHeight || document.body.scrollHeight;");
                                    loadingController.setText(baseText);
                                    loadingController.addLine(AppVaribles.getMessage("CurrentPageHeight") + ": " + newHeight);
                                    contents = null;
                                    if (newHeight == lastHeight) {
                                        contents = (String) webEngine.executeScript("document.documentElement.outerHTML");
                                        if (contents.contains("查看更早微博")) {
                                            currentPage = 1;
                                            pageCount = 1;
                                            loadCompleted = true;
                                        } else if (contents.contains("还没有发过微博")) {
                                            currentPage = 0;
                                            pageCount = 0;
                                            loadCompleted = true;
                                            emptyPage = true;
                                        } else {
                                            int pos1 = contents.indexOf("action-data=\"currentPage=");
                                            if (pos1 > 0) {
                                                String s1 = contents.substring(pos1 + "action-data=\"currentPage=".length());
                                                int pos2 = s1.indexOf("&amp;countPage=");
                                                if (pos2 > 0) {
                                                    int pos3 = s1.indexOf("\"");
                                                    if (pos3 > 0) {
                                                        try {
                                                            currentPage = Integer.valueOf(s1.substring(0, pos2));
                                                            pageCount = Integer.valueOf(s1.substring(pos2 + "&amp;countPage=".length(), pos3));
                                                            loadCompleted = true;
//                                                    logger.debug("currentPage:" + currentPage + "  pageCount:" + pageCount);
                                                        } catch (Exception e) {
                                                            loadFailed = loadCompleted = true;
                                                            errorString = e.toString();
                                                            logger.debug(e.toString());
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (!loadCompleted) {
                                        webEngine.executeScript("window.scrollTo(0," + newHeight + ");");
                                    }

                                } catch (Exception e) {
//                                    loadFailed = loadCompleted = true;
//                                    errorString = e.toString();
                                    logger.debug(e.toString());
                                }
                            }
                        });

                    } catch (Exception e) {
                        loadFailed = loadCompleted = true;
                        logger.error(e.toString());
                        errorString = e.toString();
                        quit();
                    }
                }

                private void quit() {
                    this.cancel();
                    loadCompleted = true;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (!loadFailed) {
                                if (parameters.isCreateHtml()) {
                                    try {
                                        try (BufferedWriter out = new BufferedWriter(new FileWriter(htmlFilename, false))) {
                                            out.write(contents);
                                            out.flush();
                                            htmlCount++;
                                        }
                                    } catch (Exception e) {
                                        loadFailed = true;
                                        errorString = e.toString();
                                    }
                                }
                            }
                            if (loadFailed) {
                                pageFailed(loadingController);
                            } else {
                                if (emptyPage) {
                                    loadNextPage();
                                } else {
                                    if (parameters.isCreatePDF()) {
                                        snapPage();
                                    } else {
                                        loadNextPage();
                                    }
                                }
                            }
                        }
                    });
                }

            }, parameters.getLoadDelay(), parameters.getLoadDelay());

        } catch (Exception e) {
            errorString = e.toString();
            pageFailed(loadingController);
        }

    }

    public void reloadPage() {
        loadingController.getReloadButton().setDisable(true);
        loadingController.setError("");
        loadPage(currentAddress);
    }

    private void pageFailed(WeiboSnapingInfoController controller) {
        try {
            miao();
            if (errorString == null) {
                errorString = AppVaribles.getMessage("FailedWeiboSnap");
            }
            if (controller != null) {
                controller.setError(errorString);
                controller.getReloadButton().setDisable(false);
            } else {
                alertError(errorString);
                endSnap();
            }
        } catch (Exception e) {
            endSnap();
        }
    }

    private void snapPage() {
        try {
            if (snapTimer != null) {
                snapTimer.cancel();
            }
            webEngine.executeScript("window.scrollTo(0,0 );");
            webEngine.executeScript("document.getElementsByTagName('body')[0].style.zoom = " + parameters.getZoomScale() + ";");
            Thread.sleep(parameters.getLoadDelay());
            pageHeight = (Integer) webEngine.executeScript("document.documentElement.scrollHeight || document.body.scrollHeight;");
            screenHeight = (Integer) webEngine.executeScript("document.documentElement.clientHeight || document.body.clientHeight;");
            screenWidth = (Integer) webEngine.executeScript("document.documentElement.clientWidth || document.body.clientWidth;");
            if (pageHeight == 0 || screenHeight == 0 || screenWidth == 0) {
                errorString = "";
                pageFailed(loadingController);
            }
            final int snapStep = screenHeight - 100; // Offset due to the pop bar in the page.

            if (!openLoadingStage()) {
                errorString = "";
                pageFailed(loadingController);
            }
            loadingController.setInfo(AppVaribles.getMessage("SnapingPage"));
            setBaseInfo();
            loadingController.addLine(AppVaribles.getMessage("PageZoomScale") + ": " + parameters.getZoomScale());
            loadingController.addLine(AppVaribles.getMessage("CurrentWindowHeight") + ": " + screenHeight);
            loadingController.addLine(AppVaribles.getMessage("CurrentWindowWidth") + ": " + screenWidth);
            if (parameters.isCreatePDF()) {
                loadingController.addLine(AppVaribles.getMessage("SnapingForPDF") + ": " + pdfFilename);
            }
            baseText = loadingController.getText();

            snapHeight = 0;
            snapCount = 0;
            images = new ArrayList<>();
            snapFailed = snapCompleted = false;

            snapTimer = new Timer();
            snapStartTime = new Date().getTime();
            snapTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    try {
                        if (new Date().getTime() - snapStartTime >= parameters.getMaxDelay()) {
                            loadFailed = loadCompleted = true;
                            errorString = AppVaribles.getMessage("OverTime");
                        }
                        if (snapFailed || snapCompleted || pageHeight <= snapHeight) {
                            quit();
                            return;
                        }
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    snapStartTime = new Date().getTime();
                                    final SnapshotParameters snapPara = new SnapshotParameters();
                                    snapPara.setFill(Color.TRANSPARENT);
                                    Image snapshot = webView.snapshot(snapPara, null);
                                    if (pageHeight < snapHeight + screenHeight) { // last snap
                                        snapshot = FxmlImageTools.cropImage(snapshot, 0, screenHeight - (pageHeight - snapHeight),
                                                screenWidth - 1, (int) snapshot.getHeight() - 1);
                                    } else {
                                        snapshot = FxmlImageTools.cropImage(snapshot, 0, 0,
                                                screenWidth - 1, (int) snapshot.getHeight() - 1);
                                    }
                                    images.add(snapshot);
                                    snapHeight += snapStep;
                                    if (pageHeight <= snapHeight) { // last snap
                                        snapCompleted = true;
                                    } else {
                                        webEngine.executeScript("window.scrollTo(0, " + snapHeight + ");");
                                    }
                                } catch (Exception e) {
                                    images = new ArrayList<>();
                                    snapFailed = snapCompleted = true;
                                    errorString = e.toString();
                                    logger.error(e.toString());
                                }
                            }
                        });

                    } catch (Exception e) {
                        images = new ArrayList<>();
                        snapFailed = snapCompleted = true;
                        logger.error(e.toString());
                        errorString = e.toString();
                        quit();
                    }
                }

                private void quit() {
                    this.cancel();
                    snapCompleted = true;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {

                            if (!snapFailed && !images.isEmpty()) {
                                if (!parameters.isImagePerScreen()) {
                                    Image finalImage = FxmlImageTools.combineSingleColumn(images);
                                    logger.debug("combineSingleColumn");
                                    images = new ArrayList<>();
                                    if (finalImage == null) {
                                        snapFailed = true;
                                        errorString = AppVaribles.getMessage("ImageGenerateError");
                                    } else {
                                        images.add(finalImage);
                                    }
                                }
                                if (!snapFailed) {
                                    File currentPdf = new File(pdfFilename);
                                    if (!PdfTools.images2Pdf(images, currentPdf, parameters) || !currentPdf.exists()) {
                                        snapFailed = true;
                                        errorString = AppVaribles.getMessage("FailedWeiboSnap");
                                    } else {
                                        List<File> files = pdfs.get(currentMonthString);
                                        if (files == null) {
                                            files = new ArrayList<>();
                                        }
                                        files.add(currentPdf);
                                        pdfs.put(currentMonthString, files);
                                    }
                                }
                            }
                            if (!snapFailed) {
                                loadNextPage();
                            } else {
                                pageFailed(loadingController);
                            }
                        }
                    });
                }

            }, parameters.getLoadDelay(), parameters.getScrollDelay());

        } catch (Exception e) {
            errorString = e.toString();
            pageFailed(loadingController);
        }

    }

    public void endSnap() {
        try {
            if (parent != null) {
                parent.setDuration(currentMonthString, "");
                AppVaribles.setConfigValue("WeiboLastStartMonthKey", currentMonthString);
            }
            Desktop.getDesktop().browse(rootPath.toURI());
            closeStage();
        } catch (Exception e) {
            closeStage();
        }
    }

    private void mergeMonthPdf() {
        final String month = currentMonthString;
        final List<File> files = pdfs.get(month);
        if (files == null || files.isEmpty()) {
            return;
        }

        Task<Void> mergeTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {

                    PDFMergerUtility mergePdf = new PDFMergerUtility();
                    for (File file : files) {
                        mergePdf.addSource(file);
                    }
                    String monthFile = pdfPath.getAbsolutePath() + File.separator + accountName + "-" + month + ".pdf";
                    mergePdf.setDestinationFileName(monthFile);
                    mergePdf.mergeDocuments(null);

                    try (PDDocument doc = PDDocument.load(new File(monthFile))) {
                        PDDocumentInformation info = new PDDocumentInformation();
                        info.setCreationDate(Calendar.getInstance());
                        info.setModificationDate(Calendar.getInstance());
                        info.setProducer("MyBox v" + CommonValues.AppVersion);
                        info.setAuthor(parameters.getAuthor());
                        doc.setDocumentInformation(info);
                        doc.save(new File(monthFile));
                        monthPdfCount++;
                        logger.debug(monthFile);
                    }

                    if (!parameters.isKeepPagePdf()) {
                        for (File file : files) {
                            try {
                                file.delete();
                            } catch (Exception e) {
                                logger.error(e.toString());
                            }
                        }
                    } else {
                        pagePdfCount += files.size();
                    }

                    pdfs.remove(month);

                } catch (Exception e) {
                    logger.error(e.toString());
                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
                return null;
            }
        };
        new Thread(mergeTask).start();
    }

    @Override
    public boolean stageClosing() {
        if (!super.stageClosing()) {
            return false;
        }
        if (loadingStage != null) {
            loadingStage.close();
        }
        if (loadTimer != null) {
            loadTimer.cancel();
        }
        if (snapTimer != null) {
            snapTimer.cancel();
        }

        return true;
    }

    public void openPath() {
        try {
            Desktop.getDesktop().browse(rootPath.toURI());
        } catch (Exception e) {
        }
    }

    protected void miao() {
        if (!parameters.isMiao()) {
            return;
        }
        Task miaoTask = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    File miao = FxmlTools.getUserFile(getClass(), "/sound/miao3.mp3", "miao3.mp3");
                    FloatControl control = SoundTools.getControl(miao);
                    Clip player = SoundTools.playback(miao, control.getMaximum() * 0.6f);
                    player.start();
                } catch (Exception e) {
                }
                return null;
            }
        };
        Thread thread = new Thread(miaoTask);
        thread.setDaemon(true);
        thread.start();

    }

    public WeiboSnapController getParent() {
        return parent;
    }

    public void setParent(WeiboSnapController parent) {
        this.parent = parent;
    }

}
