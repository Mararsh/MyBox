package mara.mybox.controller;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
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
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.WeiboSnapParameters;
import mara.mybox.tools.ConfigTools;
import static mara.mybox.tools.ConfigTools.checkWeiboPassport;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FxmlImageTools;
import mara.mybox.tools.FxmlTools;
import mara.mybox.tools.PdfTools;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;

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
    private int snapHeight, snapCount, pageHeight, screenHeight, screenWidth, currentPage, currentMonthPageCount, retried;
    private int savedHtmlCount, savedMonthPdfCount, savedPagePdfCount, completedMonthsCount, totalMonthsCount, savedPixCount;
    private Timer loadTimer, snapTimer, expandTImer;
    private long loadStartTime, snapStartTime, startTime, maxDelay, loadDelay;
    private boolean loadFailed, loadCompleted, snapFailed, snapCompleted, mainCompleted, isLoadingWeiboPassport;
    private WeiboSnapingInfoController loadingController;
    private Stage loadingStage;
    private String pdfFilename, htmlFilename, pixFilePrefix, expandPicturesScript;
    private String currentAddress, currentMonthString, accountName, setFontScript, baseText, errorString;
    private Date currentMonth, firstMonth, lastMonth;
    private File rootPath, pdfPath, htmlPath, pixPath;
    private WeiboSnapController parent;
    private Map<String, List<File>> pdfs;
    private Runtime r;
    private final long mb;
    private final String expandCommentsScript, findPicturesScript;
    private File tempdir;
    private MemoryUsageSetting memSettings;
    private final int VerifiedCompleted = 3;

    @FXML
    private TextField bottomText;
    @FXML
    private WebView webView;

    public WeiboSnapRunController() {
        WeiboLastStartMonthKey = "WeiboLastStartMonthKey";

        mb = 1024 * 1024;
        expandCommentsScript
                = " var items = document.getElementsByClassName('S_txt2');  "
                + " for(var i = 0; i <items.length; i++) { "
                + "     var actionType = items[i].getAttribute('action-type');  "
                + "     if ( actionType == null || actionType != 'fl_comment') continue; "
                + "     if ( items[i].firstElementChild == null) continue;  "
                + "     if ( items[i].firstElementChild.firstElementChild == null) continue;  "
                + "     if ( items[i].firstElementChild.firstElementChild.firstElementChild == null) continue; "
                + "     if ( items[i].firstElementChild.firstElementChild.firstElementChild.children[1] == null) continue; "
                + "     var comments = items[i].firstElementChild.firstElementChild.firstElementChild.children[1].textContent; "
                + "     if ( comments != null &&  comments != '评论' ) items[i].click(); "
                + " };";

        findPicturesScript
                = " function myboxFindPictures() { "
                + "     var items = document.getElementsByClassName('WB_pic');  "
                + "     var pix = ''; "
                + "     for(var i = 0; i <items.length; i++) { "
                + "         var actionType = items[i].getAttribute('action-type');  "
                + "         if ( actionType == null || (actionType != 'fl_pics'&& actionType != 'comment_media_img' && actionType != 'feed_list_media_img' )) continue; "
                + "         if (items[i].firstElementChild == null ) continue;  "
                + "         var src = items[i].firstElementChild.getAttribute('src');  "
                + "         if ( src == null ) continue; "
                + "         pix = pix + src + ', ' ; "
                + "     };"
                + "     return pix;"
                + " }; "
                + " myboxFindPictures(); ";
    }

    @Override
    protected void initializeNext() {
        webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                switch (newState) {
                    case SUCCEEDED:
                        if (isLoadingWeiboPassport) {
                            isLoadingWeiboPassport = false;
                            Timer loadTimer = new Timer();
                            if (ConfigTools.isOtherPlatforms()) {
                                loadTimer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        AppVaribles.setConfigValue("WeiboPassportChecked", "true");
//                                        logger.debug(checkWeiboPassport());
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (checkWeiboPassport()) {
                                                    startLoading();
                                                }
                                            }
                                        });
                                    }
                                }, parameters.getLoadDelay() * 5);

                            } else {
                                loadTimer.schedule(new TimerTask() {
                                    private boolean done = false;

                                    @Override
                                    public void run() {
                                        if (done) {
                                            this.cancel();
                                        } else {
                                            Platform.runLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (checkWeiboPassport()) {
                                                        done = true;
                                                        startLoading();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }, 1000, 1000);
                            }
                        }
                        break;
                    case RUNNING:
                        break;
                    default:
                        break;
                }

            }
        });

        webEngine.setOnAlert(new EventHandler<WebEvent<String>>() {

            @Override
            public void handle(WebEvent<String> event) {
                logger.debug("setOnAlert " + event.getData());
            }
        });

        webEngine.setOnError(new EventHandler<WebErrorEvent>() {

            @Override
            public void handle(WebErrorEvent event) {
                logger.debug("onError " + event.getMessage());
            }
        });

        webEngine.setConfirmHandler(new Callback<String, Boolean>() {

            @Override
            public Boolean call(String param) {
                logger.debug("setConfirmHandler " + param);
                return null;
            }
        });

        webEngine.getLoadWorker().exceptionProperty().addListener(new ChangeListener<Throwable>() {
            @Override
            public void changed(ObservableValue<? extends Throwable> ov, Throwable t, Throwable t1) {
                logger.debug("Received exception: " + t1.getMessage());
            }
        });

        r = Runtime.getRuntime();
    }

    public void start(final WeiboSnapParameters parameters) {
        try {
            this.parameters = parameters;
            getMyStage();
            logger.debug(parameters.getWebAddress());
            if (parameters.getWebAddress() == null || parameters.getWebAddress().isEmpty()) {
                closeStage();
                return;
            }
            if (ConfigTools.checkWeiboPassport()) {
                startLoading();
            } else {
                loadPassport();
            }

        } catch (Exception e) {
            alertError(e.toString());
            closeStage();
        }

    }

    private void loadPassport() {
        try {
            if (!openLoadingStage()) {
                return;
            }
            isLoadingWeiboPassport = true;
            logger.debug(AppVaribles.getMessage("LoadingWeiboCertificate"));
            loadingController.setInfo(AppVaribles.getMessage("LoadingWeiboCertificate"));
            webEngine.load("https://passport.weibo.com/visitor/visitor?entry=miniblog");

        } catch (Exception e) {
            alertError(e.toString());
            closeStage();
        }
    }

    public void startLoading() {
        try {
            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            if (parameters.getWebWidth() <= 0) {
                myStage.setX(0);
                myStage.setWidth((int) primaryScreenBounds.getWidth());
            } else {
                myStage.setWidth(parameters.getWebWidth());
            }
            myStage.setX(0);
            myStage.setY(0);
            myStage.setHeight(primaryScreenBounds.getHeight());

            savedHtmlCount = savedMonthPdfCount = savedPagePdfCount = completedMonthsCount = savedPixCount = retried = 0;
            loadFailed = loadCompleted = mainCompleted = snapFailed = snapCompleted = false;
            startTime = new Date().getTime();
            maxDelay = parameters.getMaxDelay() * 1000;
            loadDelay = parameters.getLoadDelay() * 1000;
            tempdir = parameters.getTempdir();
            if (tempdir != null) {
                if (!tempdir.exists()) {
                    tempdir.mkdirs();
                }
            }
            memSettings = AppVaribles.PdfMemUsage.setTempDir(tempdir);

            expandPicturesScript
                    = " var items = document.getElementsByClassName('WB_pic');  "
                    + " var i=0;  "
                    + " var expandInterval = setInterval( myboxExpandPictures, " + parameters.getScrollDelay() + " );  "
                    //                    + " var expandInterval = setInterval( myboxExpandPictures, 200 );  "
                    + " function myboxExpandPictures() { "
                    + "   var actionType = items[i].getAttribute('action-type');  "
                    + "   if ( i > items.length - 1 )  clearInterval(expandInterval);"
                    + "   if ( actionType != null && (actionType == 'fl_pics'  || actionType == 'comment_media_img'|| actionType == 'feed_list_media_img' )) { "
                    + "      items[i].click();  "
                    + "   }; "
                    + "   i = i + 1; "
                    + " };";
//            expandPicturesScript
//                    = " var items = document.getElementsByClassName('WB_pic');  "
//                    + " for(var i = 0; i <items.length; i++) { "
//                    + "   var actionType = items[i].getAttribute('action-type');  "
//                    + "   if ( actionType == null) continue; "
//                    + "   if ( actionType == 'fl_pics'  || actionType == 'comment_media_img'|| actionType == 'feed_list_media_img' ) { "
//                    + "      items[i].click();  "
//                    + "   }; "
//                    + " };";

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
            Thread.sleep(loadDelay);

            if (!openLoadingStage()) {
                return;
            }
            loadingController.setInfo(AppVaribles.getMessage("CheckingWeiBoMain"));

            loadFailed = loadCompleted = mainCompleted = false;
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
                        if (new Date().getTime() - loadStartTime >= maxDelay) {
                            loadFailed = loadCompleted = true;
//                            errorString = AppVaribles.getMessage("TimeOver");
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
//                                                    logger.debug(DateTools.datetimeToString(lastMonth));
                                                    int posLast1 = contents.lastIndexOf("&stat_date=");
                                                    if (posLast1 > 0) {
                                                        s = contents.substring(posLast1 + "&stat_date=".length());
                                                        int posLast2 = s.indexOf("&page=");
                                                        if (posLast2 > 0) {
                                                            try {
                                                                s = s.substring(0, posLast2);
                                                                firstMonth = DateTools.parseMonth(s.substring(0, 4) + "-" + s.substring(4, 6));
//                                                                logger.debug(DateTools.datetimeToString(firstMonth));
                                                                loadCompleted = true;
                                                            } catch (Exception e) {
                                                                logger.error(e.toString());
                                                            }
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    logger.error(e.toString());
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

            }, loadDelay, loadDelay);

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
            currentMonthPageCount = 1;
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
            retried = 0;
            currentPage++;
            if (currentPage > currentMonthPageCount) {
                completedMonthsCount++;
                if (parameters.isCreatePDF()) {
                    mergeMonthPdf(pdfPath, currentMonthString);
                }
                Calendar c = Calendar.getInstance();
                c.setTime(currentMonth);
                c.add(Calendar.MONTH, 1);
                currentMonth = c.getTime();
                if (currentMonth.getTime() > parameters.getEndMonth().getTime()) {
                    if (parameters.isMiao()) {
                        FxmlTools.miao3();
                    }
                    if (parent != null) {
                        parent.popInformation(AppVaribles.getMessage("MissCompleted"));
                    }
                    alertInformation(AppVaribles.getMessage("MissCompleted"));
                    endSnap();
                    return;
                }
                currentPage = 0;
                currentMonthPageCount = 1;
                loadNextPage();
                return;
            }
            currentMonthString = DateTools.dateToMonthString(currentMonth);
            currentAddress = parameters.getWebAddress() + "?is_all=1&stat_date="
                    + currentMonthString.replace("-", "")
                    + "&page=" + currentPage;

            if (parameters.isCreatePDF()) {
                if (parameters.getCategory() == WeiboSnapParameters.FileCategoryType.InMonthsPaths) {
                    pdfPath = new File(rootPath.getAbsolutePath() + File.separator
                            + DateTools.dateToYearString(currentMonth) + "-pdf");
                } else {
                    pdfPath = new File(rootPath.getAbsolutePath() + File.separator + "pdf");
                }
                if (!pdfPath.exists()) {
                    pdfPath.mkdirs();
                }
                pdfFilename = pdfPath.getAbsolutePath() + File.separator + accountName + "-"
                        + currentMonthString + "-第" + currentPage + "页.pdf";
            }
            if (parameters.isCreateHtml()) {
                if (parameters.getCategory() == WeiboSnapParameters.FileCategoryType.InMonthsPaths) {
                    htmlPath = new File(rootPath.getAbsolutePath() + File.separator
                            + DateTools.dateToYearString(currentMonth) + "-html");
                } else {
                    htmlPath = new File(rootPath.getAbsolutePath() + File.separator + "html");
                }
                if (!htmlPath.exists()) {
                    htmlPath.mkdirs();
                }
                htmlFilename = htmlPath.getAbsolutePath() + File.separator + accountName + "-"
                        + currentMonthString + "-第" + currentPage + "页.html";
            }
            if (parameters.isSavePictures()) {
                if (parameters.getCategory() == WeiboSnapParameters.FileCategoryType.InMonthsPaths) {
                    pixPath = new File(rootPath.getAbsolutePath() + File.separator
                            + DateTools.dateToYearString(currentMonth) + "-picture");
                    if (!pixPath.exists()) {
                        pixPath.mkdirs();
                    }
                    pixPath = new File(pixPath + File.separator
                            + DateTools.dateToMonthString(currentMonth) + "-picture");
                } else {
                    pixPath = new File(rootPath.getAbsolutePath() + File.separator + "picture");
                }
                if (!pixPath.exists()) {
                    pixPath.mkdirs();
                }
                pixFilePrefix = pixPath.getAbsolutePath() + File.separator + accountName + "-"
                        + currentMonthString + "-第" + currentPage + "页-图";
            }

            parameters.setTitle(accountName + "-" + currentMonthString + "-第" + currentPage + "页");

            loadPage(currentAddress);
        } catch (Exception e) {
            retried = Integer.MAX_VALUE;
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
        loadingController.addLine(AppVaribles.getMessage("CompletedMonths") + ": " + completedMonthsCount);
        loadingController.addLine(AppVaribles.getMessage("CurrentLoadingMonth") + ": " + DateTools.dateToMonthString(currentMonth));
        loadingController.addLine(AppVaribles.getMessage("PagesNumberThisMonth") + ": " + currentMonthPageCount);
        loadingController.addLine(AppVaribles.getMessage("CurrentLoadingPage") + ": " + currentPage);
        loadingController.addLine(AppVaribles.getMessage("PdfFilesSaved") + ": " + (savedPagePdfCount + savedMonthPdfCount));
        loadingController.addLine(AppVaribles.getMessage("HtmlFilesSaved") + ": " + savedHtmlCount);
        loadingController.addLine(AppVaribles.getMessage("PicturesSaved") + ": " + savedPixCount);
        long passed = new Date().getTime() - startTime;
        loadingController.addLine(AppVaribles.getMessage("SnapingStartTime") + ": " + DateTools.datetimeToString(startTime)
                + " (" + AppVaribles.getMessage("ElapsedTime") + ": " + DateTools.showTime(passed) + ")");
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

        if (retried > 0) {
            loadingController.showError(errorString + "\n"
                    + MessageFormat.format(AppVaribles.getMessage("RetryingTimes"), retried));
        } else {
            long freeMemory = r.freeMemory() / mb;
            long totalMemory = r.totalMemory() / mb;
            long maxMemory = r.maxMemory() / mb;
            int availableProcessors = r.availableProcessors();

            loadingController.showMem("申请内存:" + maxMemory + "M"
                    + " 已占内存:" + totalMemory + "M"
                    + " 可用内存:" + freeMemory + "M"
                    + " 可用处理器:" + availableProcessors);
        }

    }

    private void loadPage(String address) {
        try {
            webEngine.load(address);
//            logger.debug(address);
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
            loadFailed = loadCompleted = mainCompleted = false;
            loadTimer.schedule(new TimerTask() {
                int lastHeight = 0, newHeight = -1, repeat = 0, expandHeight;
                boolean emptyPage = false;
                String contents;

                @Override
                public void run() {
                    try {
                        if (new Date().getTime() - loadStartTime >= maxDelay) {
                            loadFailed = loadCompleted = true;
                            errorString = AppVaribles.getMessage("TimeOver");
                            logger.debug(errorString);
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
                                        if (mainCompleted) {
                                            repeat++;  // Wait more time when expand pictures.
//                                            logger.debug("Verified: " + repeat);
                                            if (repeat > VerifiedCompleted) {
                                                loadCompleted = true;
                                            }
                                        } else if (contents.contains("查看更早微博")) {
                                            expand();
                                            mainCompleted = true;
                                        } else if (contents.contains("还没有发过微博")) {
                                            currentPage = 0;
                                            currentMonthPageCount = 0;
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
                                                            currentMonthPageCount = Integer.valueOf(s1.substring(pos2 + "&amp;countPage=".length(), pos3));
                                                            expand();
                                                            mainCompleted = true;
                                                        } catch (Exception e) {
//                                                            loadFailed = loadCompleted = true;
//                                                            errorString = e.toString();
                                                            logger.debug(e.toString());
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        repeat = 0;
                                    }
                                    if (mainCompleted) {
                                        expandHeight += screenHeight;
                                        webEngine.executeScript("window.scrollTo(0," + expandHeight + ");");
                                    } else if (!loadCompleted) {
                                        webEngine.executeScript("window.scrollTo(0," + newHeight + ");");
                                    }

                                } catch (Exception e) {
//                                    loadFailed = loadCompleted = true;
//                                    errorString = e.toString();
//                                    logger.debug(e.toString());
                                }
                            }
                        });

                    } catch (Exception e) {
                        logger.error(e.toString());
//                        loadFailed = loadCompleted = true;
//                        errorString = e.toString();
//                        quit();
                    }
                }

                private void expand() {
                    try {
                        if (parameters.isExpandComments()) {
                            loadingController.setInfo(AppVaribles.getMessage("ExpandingComments"));
                            webEngine.executeScript(expandCommentsScript);
//                            Thread.sleep(loadDelay);
                        }
                        if (parameters.isExpandPicture()) {
                            loadingController.setInfo(AppVaribles.getMessage("ExpandingPictures"));
                            webEngine.executeScript(expandPicturesScript);
//                            Thread.sleep(loadDelay);
                        }
                        newHeight = -1;
                        repeat = 0;
                        webEngine.executeScript("window.scrollTo(0,0);");
                        screenHeight = (Integer) webEngine.executeScript("document.documentElement.clientHeight || document.body.clientHeight;");
                        expandHeight = 0;
                    } catch (Exception e) {
                        logger.error(e.toString());
//                        loadFailed = loadCompleted = true;
//                        errorString = e.toString();
//                        quit();
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
                                    saveHtml(htmlFilename, contents);
                                }
                                if (parameters.isSavePictures()) {
                                    loadingController.setInfo(AppVaribles.getMessage("SavingPictures"));
                                    savePictures(webEngine.executeScript(findPicturesScript));
                                }
                            }
                            contents = null;
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

            }, loadDelay, loadDelay);

        } catch (Exception e) {
            errorString = e.toString();
            pageFailed(loadingController);
        }

    }

    private void saveHtml(final String filename, final String contents) {
        if (filename == null || contents == null) {
            return;
        }
        Task<Void> saveHtmlTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    try (BufferedWriter out = new BufferedWriter(new FileWriter(filename, false))) {
                        out.write(contents);
                        out.flush();
                        savedHtmlCount++;
                    }
                } catch (Exception e) {
                    loadFailed = true;
                    errorString = e.toString();
                }
                return null;
            }
        };
        new Thread(saveHtmlTask).start();
    }

    private void savePictures(final Object address) {
        if (address == null) {
            return;
        }
        Task<Void> savePicturesTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    String s = (String) address;
                    s = s.replaceAll("/thumb150/", "/large/");
                    s = s.replaceAll("/orj360/", "/large/");
                    s = s.replaceAll("/thumb180/", "/large/");
                    String[] pix = s.split(",");
//                                        logger.debug(Arrays.asList(pix));
                    final String prefix = pixFilePrefix;
                    for (int i = 0; i < pix.length; i++) {
                        if (pix[i].trim().isEmpty()) {
                            continue;
                        }
                        try {
                            URL url = new URL("http:" + pix[i].trim());
                            URLConnection con = url.openConnection();
                            con.setConnectTimeout(10000);
                            try (InputStream is = con.getInputStream()) {
                                byte[] bs = new byte[1024];
                                int len;
                                String fname = prefix + (i + 1) + "." + FileTools.getFileSuffix(pix[i]);
                                try (OutputStream os = new FileOutputStream(fname)) {
                                    while ((len = is.read(bs)) != -1) {
                                        os.write(bs, 0, len);
                                    }
                                    savedPixCount++;
//                                try {
//                                    File f = new File(fname);
//                                    f.setLastModified(currentMonth.getTime());
//                                } catch (Exception e) {
//                                    logger.error(e.toString());
//                                }
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.toString());
                }
                return null;
            }
        };
        new Thread(savePicturesTask).start();

    }

    public void reloadPage() {
        retried = 0;
        loadingController.getReloadButton().setDisable(true);
        loadingController.showError("");
        loadPage(currentAddress);
    }

    private void pageFailed(final WeiboSnapingInfoController controller) {
        try {
            if (errorString == null) {
                errorString = AppVaribles.getMessage("FailedWeiboSnap");
            }
            if (retried < parameters.getRetry()) {
                if (errorString.equals(AppVaribles.getMessage("TimeOver"))) {
                    maxDelay += maxDelay;
                    logger.debug("Enlarge maxDelay as:" + maxDelay);
                }
                retried++;
                loadPage(currentAddress);
                return;
            }
            if (parameters.isMiao()) {
                FxmlTools.miao3();
            }
            if (controller != null) {
                controller.showError(errorString);
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
            Thread.sleep(loadDelay);
            pageHeight = (Integer) webEngine.executeScript("document.documentElement.scrollHeight || document.body.scrollHeight;");
            screenHeight = (Integer) webEngine.executeScript("document.documentElement.clientHeight || document.body.clientHeight;");
            screenWidth = (Integer) webEngine.executeScript("document.documentElement.clientWidth || document.body.clientWidth;");
            if (pageHeight == 0 || screenHeight == 0 || screenWidth == 0) {
                errorString = "";
                pageFailed(loadingController);
            }

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
            final int snapStep = screenHeight - 100; // Offset due to the pop bar in the page.
            snapCount = 0;
            images = new ArrayList<>();
            snapFailed = snapCompleted = false;

            snapTimer = new Timer();
            snapStartTime = new Date().getTime();
            snapTimer.schedule(new TimerTask() {
                private int lastHeight = 0, newHeight = -1;
                private int repeat = 0;

                @Override
                public void run() {
                    try {
                        if (new Date().getTime() - snapStartTime >= maxDelay) {
                            loadFailed = loadCompleted = true;
                            errorString = AppVaribles.getMessage("TimeOver");
                        }
                        if (snapFailed || snapCompleted) {
                            quit();
                            return;
                        }
                        lastHeight = newHeight;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    newHeight = (Integer) webEngine.executeScript("document.documentElement.scrollHeight || document.body.scrollHeight;");
                                    if (snapHeight < newHeight) {
                                        snapStartTime = new Date().getTime();
                                        final SnapshotParameters snapPara = new SnapshotParameters();
                                        snapPara.setFill(Color.TRANSPARENT);
                                        Image snapshot = webView.snapshot(snapPara, null);
                                        images.add(snapshot);
                                        snapHeight += snapStep;
                                        webEngine.executeScript("window.scrollTo(0, " + snapHeight + ");");
                                        repeat = 0;

                                    } else if (newHeight == lastHeight) {
                                        repeat++;
//                                        logger.debug("Verified: " + repeat);
                                        if (repeat > VerifiedCompleted) {
                                            snapCompleted = true;
                                            pageHeight = newHeight;
                                            if (snapHeight > pageHeight) {
                                                Image lasSnap = images.get(images.size() - 1);
                                                int y1 = snapHeight - pageHeight + 100;
//                                                logger.debug(pageHeight + " " + snapHeight + " " + screenHeight + " " + y1);
                                                lasSnap = FxmlImageTools.cropImage(lasSnap, 0, y1,
                                                        (int) lasSnap.getWidth() - 1, (int) lasSnap.getHeight() - 1);
                                                images.set(images.size() - 1, lasSnap);
                                            }
                                        }
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
                            images = new ArrayList<>();
                            if (!snapFailed) {
                                loadNextPage();
                            } else {
                                pageFailed(loadingController);
                            }
                        }
                    });
                }

            }, loadDelay, parameters.getScrollDelay());

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
            if (parameters.isOpenPathWhenStop()) {
                Desktop.getDesktop().browse(rootPath.toURI());
            }
            closeStage();
        } catch (Exception e) {
            closeStage();
        }
    }

    private void mergeMonthPdf(final File path, final String month) {
        final List<File> files = pdfs.get(month);
//        logger.debug(path + "  " + month);
        if (files == null || files.isEmpty()) {
//            logger.debug(pdfs);
//            logger.debug("null:" + month);
            return;
        }
//        logger.debug(files);

        Task<Void> mergeTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    String monthFile = path.getAbsolutePath() + File.separator + accountName + "-" + month + ".pdf";
                    if (files.size() == 1) {
                        files.get(0).renameTo(new File(monthFile));
                        savedMonthPdfCount++;
                        pdfs.remove(month);
                        return null;
                    }

                    boolean keep = parameters.isKeepPagePdf();
                    PDFMergerUtility mergePdf = new PDFMergerUtility();
                    for (File file : files) {
                        mergePdf.addSource(file);
                    }
                    mergePdf.setDestinationFileName(monthFile);
                    mergePdf.mergeDocuments(memSettings);

                    try (PDDocument doc = PDDocument.load(new File(monthFile), memSettings)) {
                        PDDocumentInformation info = new PDDocumentInformation();
                        info.setCreationDate(Calendar.getInstance());
                        info.setModificationDate(Calendar.getInstance());
                        info.setProducer("MyBox v" + CommonValues.AppVersion);
                        info.setAuthor(parameters.getAuthor());
                        doc.setDocumentInformation(info);

                        PDPage page = doc.getPage(0);
                        PDPageXYZDestination dest = new PDPageXYZDestination();
                        dest.setPage(page);
                        dest.setZoom(parameters.getPdfScale() / 100.0f);
                        dest.setTop((int) page.getCropBox().getHeight());
                        PDActionGoTo action = new PDActionGoTo();
                        action.setDestination(dest);
                        doc.getDocumentCatalog().setOpenAction(action);

                        doc.save(new File(monthFile));
                        savedMonthPdfCount++;
                    }

                    if (!keep) {
                        for (File file : files) {
                            try {
                                file.delete();
                            } catch (Exception e) {
                                logger.error(e.toString());
                            }
                        }
                    } else {
                        savedPagePdfCount += files.size();
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

    public WeiboSnapController getParent() {
        return parent;
    }

    public void setParent(final WeiboSnapController parent) {
        this.parent = parent;
    }

}
