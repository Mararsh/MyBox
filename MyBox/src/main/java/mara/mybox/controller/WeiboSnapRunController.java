package mara.mybox.controller;

import mara.mybox.fxml.FxmlStage;
import com.sun.management.OperatingSystemMXBean;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
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
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.data.WeiboSnapParameters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.fxml.image.ImageTools;
import mara.mybox.fxml.FxmlTools;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.tools.NetworkTools;
import static mara.mybox.tools.NetworkTools.checkWeiboPassport;
import mara.mybox.tools.PdfTools;
import mara.mybox.tools.ValueTools;
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

    private WebEngine webEngine;
    private WeiboSnapParameters parameters;
    private int snapHeight, pageHeight, screenHeight, screenWidth;
    private int currentPage, currentPagePicturesNumber, currentMonthPageCount, retried, loadedPicturesNumber;
    private int savedHtmlCount, savedMonthPdfCount, savedPagePdfCount, completedMonthsCount, totalMonthsCount, savedPixCount;
    private Timer loadTimer, snapTimer;
    private long loadStartTime, snapStartTime, startTime;
    private boolean loadFailed, loadCompleted, snapFailed, snapCompleted, mainCompleted, isLoadingWeiboPassport, commentsLoaded, picturedsLoaded;
    private WeiboSnapingInfoController loadingController;
    private Stage loadingStage;
    private String pdfFilename, htmlFilename, pixFilePrefix;
    private String currentAddress, currentMonthString, accountName, baseText, errorString;
    private Date currentMonth, firstMonth, lastMonth, lastPictureLoad;
    private File rootPath, pdfPath, htmlPath, pixPath;
    private WeiboSnapController parent;
    private Map<String, List<File>> pdfs;
    private Runtime r;
    private final int MaxStasisTimes, MinAccessInterval, MaxAccessInterval;
    private final long mb, loadLoopInterval, snapLoopInterval;
    private final String expandCommentsScript, findPicturesScript, picturesNumberScript, expandPicturesScript, clearScripts;
    private final String AllPicturesLoaded, PictureLoaded, PictureTimeOver, CommentsLoaded, CommentsTimeOver;
    private File tempdir;
    private MemoryUsageSetting memSettings;
    private ChangeListener webEngineStateListener;

    @FXML
    private WebView webView;

    public WeiboSnapRunController() {
        mb = 1024 * 1024;
        AllPicturesLoaded = "MyBoxAllPicturesLoaded";
        PictureLoaded = "MyBoxPictureLoaded";
        PictureTimeOver = "MyBoxPictureTimeOver";
        CommentsLoaded = "MyBoxCommentsLoaded";
        CommentsTimeOver = "MyBoxCommentsTimeOver";
        MinAccessInterval = 1000;
        MaxAccessInterval = MinAccessInterval * 5;
        loadLoopInterval = MinAccessInterval * 3;
        MaxStasisTimes = 6;
        snapLoopInterval = 300;

        expandCommentsScript
                = " function myboxExpandComments() { "
                + "   var items = document.getElementsByClassName('S_txt2');  "
                + "   for(var i = 0; i <items.length; i++) { "
                + "     var actionType = items[i].getAttribute('action-type');  "
                + "     if ( actionType == null || actionType != 'fl_comment') continue; "
                + "     if ( items[i].firstElementChild == null) continue;  "
                + "     if ( items[i].firstElementChild.firstElementChild == null) continue;  "
                + "     if ( items[i].firstElementChild.firstElementChild.firstElementChild == null) continue; "
                + "     if ( items[i].firstElementChild.firstElementChild.firstElementChild.children[1] == null) continue; "
                + "     var comments = items[i].firstElementChild.firstElementChild.firstElementChild.children[1].textContent; "
                + "     if ( comments != null &&  comments != '评论' ) items[i].click(); "
                + "   };"
                + "   myboxMinitorCommentsLoaded();"
                + " };"
                + " function myboxMinitorCommentsLoaded() { "
                + "   var mmhead = document.getElementsByTagName('head')[0]; "
                + "   var mmold = document.getElementById('MyBoxCommentsScript'); "
                + "   if ( mmold != null )   mmold.parentNode.removeChild(mmold);"
                + "  var mmscript = document.createElement('script'); "
                + "  mmscript.setAttribute('type', 'text/javascript'); "
                + "  mmscript.setAttribute('src', 'myboxdummy.js'); "
                + "  mmscript.setAttribute('id', 'MyBoxCommentsScript'); "
                + "  mmscript.onload=function() { "
                + "   alert('" + CommentsLoaded + "');  "
                + "  }; "
                + "  mmhead.appendChild(mmscript); "
                + " };"
                + " myboxExpandComments();";

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

        picturesNumberScript
                = " function myboxCountPictures() { "
                + "     var items = document.getElementsByClassName('WB_pic');  "
                + "     var count = 0;"
                + "     for(var i = 0; i <items.length; i++) { "
                + "         var actionType = items[i].getAttribute('action-type');  "
                + "         if ( actionType == null || (actionType != 'fl_pics'&& actionType != 'comment_media_img' && actionType != 'feed_list_media_img' )) continue; "
                + "         count++;  "
                + "     };"
                + "     return count;"
                + " }; "
                + " myboxCountPictures(); ";

        expandPicturesScript
                = " function myboxExpandPicture(i) { "
                + "   var items = document.getElementsByClassName('WB_pic');  "
                + "   if ( items == null || i > items.length - 1 ) {"
                + "       alert('" + AllPicturesLoaded + "'); "
                + "       return;"
                + "   }; "
                + "   var actionType = items[i].getAttribute('action-type');  "
                + "   if ( actionType != null && (actionType == 'fl_pics'  || actionType == 'comment_media_img'|| actionType == 'feed_list_media_img' )) { "
                + "       items[i].click();  " // Notice: It is possible the picture is too small to trigger event 'onload'
                + "       myboxMinitorPictureLoaded(i);"
                + "   };"
                + " };"
                + " function myboxMinitorPictureLoaded(i ) { "
                + "   var mmhead = document.getElementsByTagName('head')[0]; "
                + "   var mmold = document.getElementById('MyBoxPictureScript'); "
                + "   if ( mmold != null )   mmold.parentNode.removeChild(mmold);"
                + "  var mmscript = document.createElement('script'); "
                + "  mmscript.setAttribute('type', 'text/javascript'); "
                + "  mmscript.setAttribute('src', 'myboxdummy.js'); "
                + "  mmscript.setAttribute('id', 'MyBoxPictureScript'); "
                + "  mmscript.onload=function() { "
                + "     alert('" + PictureLoaded + "' + i );   "
                + "  }; "
                + "  mmhead.appendChild(mmscript); "
                + " };"
                + " myboxExpandPicture(0);  ";

        clearScripts
                = "   myboxExpandComments = null;  "
                + "   myboxMinitorCommentsLoaded = null; "
                + "   myboxFindPictures = null; "
                + "   myboxCountPictures = null;"
                + "   myboxExpandPicture = null; "
                + "   myboxMinitorPictureLoaded = null;  "
                + "   var mmhead = document.getElementsByTagName('head')[0]; "
                + "   var mmold = document.getElementById('MyBoxCommentsScript'); "
                + "   if ( mmold != null )   mmold.parentNode.removeChild(mmold);"
                + "   mmold = document.getElementById('MyBoxPictureScript'); "
                + "   if ( mmold != null )   mmold.parentNode.removeChild(mmold);";

    }

    @Override
    protected void initializeNext() {
        webView.setCache(false);
        webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        r = Runtime.getRuntime();

    }

    public void start(final WeiboSnapParameters parameters) {
        try {
            this.parameters = parameters;
            getMyStage();
//            logger.debug(parameters.getWebAddress());

            if (parameters.getWebAddress() == null || parameters.getWebAddress().isEmpty()) {
                closeStage();
                return;
            }

            if (checkWeiboPassport()) {
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
            loadingController.setInfo(AppVaribles.getMessage("LoadingWeiboCertificate"));
            showMemInfo();

            webEngineStateListener = new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                    switch (newState) {
                        case SUCCEEDED:
                            if (isLoadingWeiboPassport) {
                                isLoadingWeiboPassport = false;
                                loadTimer = new Timer();
                                if (NetworkTools.isOtherPlatforms()) {
                                    loadTimer.schedule(new TimerTask() {
                                        private boolean done = false;

                                        @Override
                                        public void run() {
                                            AppVaribles.setUserConfigValue("WeiboPassportChecked", "true");
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
                                    }, 15000);

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
                                    }, 2000, 2000);
                                }
                            }
                            break;
                        case RUNNING:
                            break;
                        default:
                            break;
                    }

                }
            };
            webEngine.getLoadWorker().stateProperty().addListener(webEngineStateListener);

            webEngine.load("https://passport.weibo.com/visitor/visitor?entry=miniblog");

        } catch (Exception e) {
            alertError(e.toString());
            closeStage();
        }
    }

    public void startLoading() {
        try {
            if (webEngineStateListener != null) {
                webEngine.getLoadWorker().stateProperty().removeListener(webEngineStateListener);
            }
            lastPictureLoad = new Date();
            webEngine.setOnAlert(new EventHandler<WebEvent<String>>() {
                @Override
                public void handle(WebEvent<String> ev) {
                    try {
//                        logger.debug(loadedPicturesNumber + "  setOnAlert:" + ev.getData());
                        if (CommentsLoaded.equals(ev.getData())) {
                            commentsLoaded = true;
                        } else if (AllPicturesLoaded.equals(ev.getData())) {
                            picturedsLoaded = true;
                        } else if ((PictureLoaded + loadedPicturesNumber).equals(ev.getData())) {
                            loadedPicturesNumber++;
                            try {  // Too quick will be banned.
                                long interval = new Date().getTime() - lastPictureLoad.getTime();
                                if (interval < MinAccessInterval) {
                                    Thread.sleep(MinAccessInterval - interval);
                                }
                            } catch (Exception e) {
                            }
                            lastPictureLoad = new Date();
                            webEngine.executeScript("myboxExpandPicture(" + loadedPicturesNumber + "); ");
                        }

                        if (!parameters.isExpandComments()) {
                            commentsLoaded = true;
                        }
                        if (!parameters.isExpandPicture()) {
                            picturedsLoaded = true;
                        }
                        if (commentsLoaded && picturedsLoaded) {
                            loadCompleted = true;
                        }
                    } catch (Exception e) {
                        logger.debug(e.toString());
                    }
                }
            });
//            NetworkTools.readCookie(webEngine);

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
            tempdir = parameters.getTempdir();
            if (tempdir == null || !tempdir.exists() || !tempdir.isDirectory()) {
                tempdir = new File(CommonValues.AppDataRoot);
            } else if (!tempdir.exists()) {
                if (!tempdir.mkdirs()) {
                    tempdir = CommonValues.AppTempPath;
                }
            }
            memSettings = AppVaribles.PdfMemUsage.setTempDir(tempdir);

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
            logger.debug(e.toString());
            closeStage();
            return false;
        }
    }

    private void loadMain() {
        try {
            webEngine.load(parameters.getWebAddress());
//            logger.debug(parameters.getWebAddress());
//            Thread.sleep(loadLoopInterval);
//            NetworkTools.readCookie(webEngine);

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
            TimerTask mainTask = new TimerTask() {
                int lastHeight = 0, newHeight = -1;
                long maxDelay = loadLoopInterval * 60;
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
                                    showMemInfo();
                                    if (contents.contains("帐号登录")) {
                                        loadFailed = loadCompleted = true;
                                        errorString = AppVaribles.getMessage("NonExistedWeiboAccount");
                                        quit();
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
//                    loadTimer.purge();
//                    System.gc();
//                    loadTimer.cancel();
                    loadCompleted = true;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            webEngine.getLoadWorker().cancel();
                            if (accountName == null || firstMonth == null || lastMonth == null) {
                                if (errorString == null) {
                                    errorString = AppVaribles.getMessage("NonExistedWeiboAccount");
                                }
                                loadFailed = true;
                            }
                            if (loadFailed) {
                                alertError(errorString);
                                if (parent == null) {
                                    openStage(CommonValues.WeiboSnapFxml, AppVaribles.getMessage("WeiboSnap"), false, true);
                                }
                                endSnap();
                            } else {
                                loadPages();
                            }
                        }
                    });
                }

            };
            loadTimer.schedule(mainTask, loadLoopInterval * 3, loadLoopInterval);

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
//            currentPage = AppVaribles.getUserConfigInt("WeiBoCurrentPageKey", 1) - 1;
//            if (currentPage < 0) {
//                currentPage = 0;
//            }
            currentPage = 0;
            currentMonthPageCount = AppVaribles.getUserConfigInt("WeiBoCurrentMonthPageCountKey", 1);
            if (currentMonthPageCount < 1) {
                currentMonthPageCount = 1;
            }
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
                AppVaribles.setUserConfigInt("WeiBoCurrentPageKey", currentPage);
                currentMonthPageCount = 1;
                loadNextPage();
                return;
            }
            AppVaribles.setUserConfigInt("WeiBoCurrentPageKey", currentPage);
            currentMonthString = DateTools.dateToMonthString(currentMonth);
            currentAddress = parameters.getWebAddress() + "?is_all=1&stat_date="
                    + currentMonthString.replace("-", "")
                    + "&page=" + currentPage + "&mmts=" + new Date().getTime();

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

    private void showBaseInfo() {

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

        showMemInfo();

    }

    private void showDynamicInfo() {
        long passed = new Date().getTime() - startTime;
        if (currentPagePicturesNumber > 0) {
            loadingController.addLine(AppVaribles.getMessage("CurrentPagePicturesNumber") + ": " + currentPagePicturesNumber);
        }
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
    }

    private void showMemInfo() {
        if (!openLoadingStage()) {
            return;
        }

        long freeMemory = r.freeMemory() / mb;
        long totalMemory = r.totalMemory() / mb;
        long maxMemory = r.maxMemory() / mb;
        long usedMemory = totalMemory - freeMemory;
//            int availableProcessors = r.availableProcessors();

        OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long physicalFree = osmxb.getFreePhysicalMemorySize() / mb;
        long physicalTotal = osmxb.getTotalPhysicalMemorySize() / mb;
        long physicalUse = physicalTotal - physicalFree;

        String memInfo = "MyBox"
                //                    + "  " + AppVaribles.getMessage("AvailableProcessors") + ":" + availableProcessors
                + "  " + AppVaribles.getMessage("AvaliableMemory") + ":" + maxMemory + "MB"
                + "  " + AppVaribles.getMessage("RequiredMemory") + ":" + totalMemory + "MB(" + ValueTools.roundFloat2(totalMemory * 100.0f / physicalTotal) + "%)"
                + "  " + AppVaribles.getMessage("UsedMemory") + ":" + usedMemory + "MB(" + ValueTools.roundFloat2(usedMemory * 100.0f / physicalTotal) + "%)";

        memInfo += "\n" + System.getProperty("os.name")
                + "  " + AppVaribles.getMessage("TotalPhysicalMemory") + ":" + physicalTotal + "MB"
                + "  " + AppVaribles.getMessage("UsedPhysicalMemory") + ":" + physicalUse + "MB (" + ValueTools.roundFloat2(physicalUse * 100.0f / physicalTotal) + "%)";

        loadingController.showMem(memInfo);

    }

    private void loadPage(final String address) {
        try {
            logger.debug(address);
            webEngine.load(address);
//            webEngine.executeScript("window.location.href='" + address + "';");
//            NetworkTools.readCookie(webEngine);

            if (loadTimer != null) {
                loadTimer.cancel();
            }
            if (!openLoadingStage()) {
                return;
            }

            loadedPicturesNumber = currentPagePicturesNumber = 0;
            loadFailed = loadCompleted = mainCompleted = false;
            commentsLoaded = picturedsLoaded = false;

            loadingController.setInfo(AppVaribles.getMessage("LoadingPage"));
            showBaseInfo();
            if (parameters.isCreateHtml()) {
                loadingController.addLine(AppVaribles.getMessage("LoadingForHtml") + ": " + htmlFilename);
            }
            baseText = loadingController.getText();

            loadTimer = new Timer();
            loadStartTime = new Date().getTime();
            loadTimer.schedule(new TimerTask() {
                private int lastHeight = 0, newHeight = -1, expandHeight;
                private int stasisTimes = 0;
                private boolean emptyPage = false;
                private String contents;

                @Override
                public void run() {
                    try {
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
                                    showMemInfo();
                                    showDynamicInfo();

                                    loadingController.addLine(AppVaribles.getMessage("CurrentPageHeight") + ": " + newHeight);
                                    if (loadedPicturesNumber > 0) {
                                        loadingController.addLine(AppVaribles.getMessage("LoadedPicturesNumber") + ": " + loadedPicturesNumber);
                                    }
//                                    loadingController.addLine(AppVaribles.getMessage("Loadint time") + ": " + (new Date().getTime() - loadStartTime) / 1000);
                                    contents = null;
                                    if (newHeight == lastHeight) {
                                        stasisTimes++;
                                        if (mainCompleted && currentPagePicturesNumber > 0
                                                && (new Date().getTime() - lastPictureLoad.getTime()) > MaxAccessInterval) {
                                            logger.debug("skip picture:" + loadedPicturesNumber);
                                            loadedPicturesNumber++;
                                            lastPictureLoad = new Date();
                                            webEngine.executeScript("myboxExpandPicture(" + loadedPicturesNumber + "); ");
                                            return;
                                        }
                                        if (stasisTimes >= MaxStasisTimes) {
                                            errorString = AppVaribles.getMessage("TimeOver");
                                            logger.debug(errorString);
                                            loadFailed = loadCompleted = true;
                                            return;
                                        }
                                        if (!mainCompleted) {
                                            contents = (String) webEngine.executeScript("document.documentElement.outerHTML");
                                            if (contents.contains("查看更早微博")) {
                                                mainCompleted();
                                            } else if (contents.contains("还没有发过微博")) {
                                                currentPage = 0;
                                                currentMonthPageCount = 0;
                                                emptyPage = true;
                                                mainCompleted = loadCompleted = true;
                                                return;
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
                                                                AppVaribles.setUserConfigInt("WeiBoCurrentMonthPageCountKey", currentMonthPageCount);
                                                                mainCompleted();
                                                            } catch (Exception e) {
//                                                            loadFailed = loadCompleted = true;
//                                                            errorString = e.toString();
                                                                logger.debug(e.toString());
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        stasisTimes = 0;
                                    }
                                    if (loadFailed || loadCompleted) {
                                        return;
                                    }
                                    if (mainCompleted) {
                                        expandHeight += screenHeight;
                                        webEngine.executeScript("window.scrollTo(0," + expandHeight + ");");
                                    } else {
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

                private void mainCompleted() {
                    try {
//                        NetworkTools.readCookie(webEngine);
                        if (parameters.isSavePictures()) {
                            loadingController.setInfo(AppVaribles.getMessage("SavingPictures"));
                            savePictures(webEngine.executeScript(findPicturesScript));
                        }
                        if (!parameters.isExpandPicture() && !parameters.isExpandComments()) {
                            mainCompleted = loadCompleted = true;
                            return;
                        }
                        if (parameters.isExpandComments()) {
                            loadingController.setInfo(AppVaribles.getMessage("ExpandingComments"));
                            webEngine.executeScript(expandCommentsScript);
                        } else {
                            commentsLoaded = true;
                        }

                        if (parameters.isExpandPicture()) {
                            currentPagePicturesNumber = (int) webEngine.executeScript(picturesNumberScript);
                            if (currentPagePicturesNumber > 0) {
                                loadingController.setInfo(AppVaribles.getMessage("ExpandingPictures"));
                                webEngine.executeScript(expandPicturesScript);
                            } else {
                                picturedsLoaded = true;
                            }
                        } else {
                            picturedsLoaded = true;
                        }

                        newHeight = -1;
                        expandHeight = 0;
                        mainCompleted = true;
                        webEngine.executeScript("window.scrollTo(0,0);");
                        screenHeight = (Integer) webEngine.executeScript("document.documentElement.clientHeight || document.body.clientHeight;");
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
//                            NetworkTools.readCookie(webEngine);
                            webEngine.executeScript(clearScripts);
                            if (!loadFailed) {
                                retried = 0;
                                if (parameters.isCreateHtml()) {
                                    contents = (String) webEngine.executeScript("document.documentElement.outerHTML");
                                    saveHtml(htmlFilename, contents);
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

            }, loadLoopInterval * 3, loadLoopInterval);

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
//                    logger.debug(Arrays.asList(pix));
                    final String prefix = pixFilePrefix;
                    String fname, suffix, saveName;
                    for (int i = 0; i < pix.length; i++) {
                        fname = pix[i].trim();
                        int pos = fname.indexOf("&");
                        if (pos > 0) {
                            fname = fname.substring(0, pos);
                        }
                        if (fname.isEmpty()) {
                            continue;
                        }
                        suffix = FileTools.getFileSuffix(FileTools.getFileName(pix[i]));
                        if (suffix.isEmpty()) {
                            suffix = "jpg";
                            fname += "." + suffix;
                        }
                        try {
                            URL url = new URL("http:" + fname);
                            URLConnection con = url.openConnection();
                            con.setConnectTimeout(10000);
                            try (InputStream is = con.getInputStream()) {
                                byte[] bs = new byte[1024];
                                int len;
                                saveName = prefix + (i + 1) + "." + suffix;
                                try (OutputStream os = new FileOutputStream(saveName)) {
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
                retried++;
                loadingController.showError(errorString + "\n"
                        + MessageFormat.format(AppVaribles.getMessage("RetryingTimes"), retried));
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
            Thread.sleep(loadLoopInterval);
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
            showBaseInfo();
            if (currentPagePicturesNumber > 0) {
                loadingController.addLine(AppVaribles.getMessage("CurrentPagePicturesNumber") + ": " + currentPagePicturesNumber);
            }
            loadingController.addLine(AppVaribles.getMessage("PageZoomScale") + ": " + parameters.getZoomScale());
            loadingController.addLine(AppVaribles.getMessage("CurrentWindowHeight") + ": " + screenHeight);
            loadingController.addLine(AppVaribles.getMessage("CurrentWindowWidth") + ": " + screenWidth);
            if (parameters.isCreatePDF()) {
                loadingController.addLine(AppVaribles.getMessage("SnapingForPDF") + ": " + pdfFilename);
            }
            baseText = loadingController.getText();

            snapHeight = 0;
            final int snapStep = screenHeight - 100; // Offset due to the pop bar in the page.
            snapFailed = snapCompleted = false;
            final SnapshotParameters snapPara = new SnapshotParameters();
            snapPara.setFill(Color.TRANSPARENT);

            snapTimer = new Timer();
            snapStartTime = new Date().getTime();
            snapTimer.schedule(new TimerTask() {
                private int lastHeight = 0, newHeight = -1;
                private int imageNumber = 0;
                private List<Image> images = new ArrayList<>();
                private List<String> imageFiles = new ArrayList<>();
                BufferedImage bufferedImage;
                Image snapshot;

                @Override
                public void run() {
                    try {
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
                                    loadingController.setText(baseText);
                                    showMemInfo();
                                    showDynamicInfo();
                                    loadingController.addLine(AppVaribles.getMessage("CurrentPageHeight") + ": " + newHeight);
                                    if (snapHeight < newHeight) {
                                        snapStartTime = new Date().getTime();
                                        snapshot = webView.snapshot(snapPara, null);
                                        imageNumber++;
                                        loadingController.addLine(AppVaribles.getMessage("CurrentSnapshotNumber") + ": " + imageNumber);
                                        if (parameters.isUseTempFiles()) {
                                            try {
                                                String filename = pdfFilename + "-Image" + imageNumber + ".png";
                                                bufferedImage = ImageTools.getBufferedImage(snapshot);
                                                snapshot = null;
                                                ImageFileWriters.writeImageFile(bufferedImage, "png", filename);
                                                bufferedImage = null;
                                                imageFiles.add(filename);
                                            } catch (Exception e) {
                                                logger.debug(e.toString());
                                            }
                                        } else {
                                            images.add(snapshot);
                                        }
                                        snapHeight += snapStep;
                                        webEngine.executeScript("window.scrollTo(0, " + snapHeight + ");");

                                    } else if (newHeight == lastHeight) {

                                        if (images.size() > 0 && snapHeight > pageHeight) {
                                            Image lasSnap = images.get(images.size() - 1);
                                            int y1 = snapHeight - pageHeight + 100;
//                                            logger.debug(pageHeight + " " + snapHeight + " " + screenHeight + " " + y1);
                                            lasSnap = ImageTools.cropImage(lasSnap, 0, y1,
                                                    (int) lasSnap.getWidth() - 1, (int) lasSnap.getHeight() - 1);
                                            images.set(images.size() - 1, lasSnap);
                                        }
                                        snapCompleted = true;
                                        pageHeight = newHeight;

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

                            if (!snapFailed) {
                                if (!parameters.isImagePerScreen() && !images.isEmpty()) {
                                    Image finalImage = ImageTools.combineSingleColumn(images);
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
                                    loadingController.addLine(AppVaribles.getMessage("Generateing") + ": " + currentPdf.getAbsolutePath());
                                    Boolean isOK;
                                    if (parameters.isUseTempFiles()) {
                                        isOK = PdfTools.imagesFiles2Pdf(imageFiles, currentPdf, parameters, true);
                                        imageFiles = new ArrayList<>();
                                    } else {
                                        isOK = PdfTools.images2Pdf(images, currentPdf, parameters);
                                        images = new ArrayList<>();
                                    }
                                    if (!isOK || !currentPdf.exists()) {
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

            }, loadLoopInterval, snapLoopInterval);

        } catch (Exception e) {
            errorString = e.toString();
            pageFailed(loadingController);
        }

    }

    private void mergeMonthPdf(final File path, final String month) {
        final List<File> files = pdfs.get(month);
        if (files == null || files.isEmpty()) {
            return;
        }

        Task<Void> mergeTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    String monthFileName = path.getAbsolutePath() + File.separator + accountName + "-" + month + ".pdf";
                    File monthFile = new File(monthFileName);
                    if (monthFile.exists()) {
                        monthFile.delete();
                    }
                    if (files.size() == 1) {
                        files.get(0).renameTo(monthFile);
                        savedMonthPdfCount++;
                        pdfs.remove(month);
                        return null;
                    }

                    boolean keep = parameters.isKeepPagePdf();
                    PDFMergerUtility mergePdf = new PDFMergerUtility();
                    for (File file : files) {
                        mergePdf.addSource(file);
                    }
                    mergePdf.setDestinationFileName(monthFileName);
                    mergePdf.mergeDocuments(memSettings);

                    try (PDDocument doc = PDDocument.load(monthFile, memSettings)) {
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

                        doc.save(monthFile);
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

    public void endSnap() {
        try {
            if (parent != null) {
                parent.setDuration(currentMonthString, "");
            }
            AppVaribles.setUserConfigValue("WeiboLastStartMonthKey", currentMonthString);
            AppVaribles.setUserConfigValue("WeiBoCurrentPageKey", currentPage + "");
            AppVaribles.setUserConfigValue("WeiBoCurrentMonthPageCountKey", currentMonthPageCount + "");
            if (parameters.isOpenPathWhenStop()) {
                FxmlStage.openTarget(getClass(), null, rootPath.getAbsolutePath());
            }
            closeStage();
        } catch (Exception e) {
            closeStage();
        }
    }

    @Override
    public boolean stageClosing() {
        webEngine.getLoadWorker().cancel();
        webEngine = null;
        if (loadTimer != null) {
            loadTimer.cancel();
        }
        if (snapTimer != null) {
            snapTimer.cancel();
        }
        if (loadingStage != null) {
            loadingStage.close();
        }

        /* When quit whole app from Failed path in "loadMain()", threads may NOT quit
         * and cause wrong results of next execution due to unknow reason
         * With below statement, the thread quit any way as need. DO NOT know what's the bug!
         * Another solution is to open a window but not quit whole app.
         */
        Thread thread = Thread.currentThread();
        System.gc();

        return super.stageClosing();
    }

    public void openPath() {
        FxmlStage.openTarget(getClass(), null, rootPath.getAbsolutePath());
    }

    public WeiboSnapController getParent() {
        return parent;
    }

    public void setParent(final WeiboSnapController parent) {
        this.parent = parent;
    }

}
