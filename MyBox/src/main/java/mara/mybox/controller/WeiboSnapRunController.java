package mara.mybox.controller;

import com.sun.management.OperatingSystemMXBean;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
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
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.StageStyle;
import mara.mybox.data.WeiboSnapParameters;
import mara.mybox.db.TableBrowserBypassSSL;
import mara.mybox.db.TableStringValues;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FloatTools;
import mara.mybox.tools.NetworkTools;
import mara.mybox.tools.PdfTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonValues;
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

    protected WebEngine webEngine;
    protected WeiboSnapParameters parameters;
    protected int currentPage, currentPagePicturesNumber, currentMonthPageCount, retried, loadedPicturesNumber;
    protected int savedHtmlCount, savedMonthPdfCount, savedPagePdfCount, completedMonthsCount, totalMonthsCount,
            savedPixCount, totalLikeCount;
    protected Timer loadTimer, snapTimer;
    protected long loadStartTime, snapStartTime, startTime;
    protected boolean loadFailed, loadCompleted, mainCompleted,
            commentsLoaded, picturedsLoaded, startPageChecked, skipPage, parentOpened;
    protected WeiboSnapingInfoController loadingController;
    protected String pdfFilename, htmlFilename, pixFilePrefix;
    protected String currentAddress, currentMonthString, accountName, baseText, errorString;
    protected Date currentMonth, firstMonth, lastMonth, lastPictureLoad;
    protected File rootPath, pdfPath, htmlPath, pixPath;
    protected WeiboSnapController parent;
    protected Map<String, List<File>> pdfs;
    protected Runtime r;
    protected final int MaxStasisTimes;
    protected int MinAccessInterval, MaxAccessInterval, loadLoopInterval, pageAccessDelay, snapLoopInterval;
    protected final long mb;
    protected final String expandCommentsScript, findPicturesScript, picturesNumberScript, expandPicturesScript, clearScripts;
    protected final String AllPicturesLoaded, PictureLoaded, PictureTimeOver, CommentsLoaded, CommentsTimeOver;
    protected File tempdir;
    protected MemoryUsageSetting memSettings;
    protected ChangeListener webEngineStateListener;
    protected SnapType snapType;
    protected SnapshotParameters snapParameters;
    protected int pageHeight, screenHeight, screenWidth, startPage, snapsTotal,
            snapImageWidth, snapImageHeight, snapHeight, snapStep, dpi;
    protected double snapScale;
    protected List<String> imageFiles;

    public enum SnapType {
        Posts, Like
    }

    @FXML
    protected WebView webView;

    public WeiboSnapRunController() {
        baseTitle = AppVariables.message("WeiboSnap");

        mb = 1024 * 1024;
        AllPicturesLoaded = "MyBoxAllPicturesLoaded";
        PictureLoaded = "MyBoxPictureLoaded";
        PictureTimeOver = "MyBoxPictureTimeOver";
        CommentsLoaded = "MyBoxCommentsLoaded";
        CommentsTimeOver = "MyBoxCommentsTimeOver";
        MaxStasisTimes = 6;
        snapLoopInterval = 2000;
        MinAccessInterval = 2000;
        MaxAccessInterval = MinAccessInterval * 10;
        loadLoopInterval = MinAccessInterval * 3;
        pageAccessDelay = MinAccessInterval * 15;

        expandCommentsScript
                = " function myboxExpandComments() { "
                + "   var items = document.getElementsByClassName('S_txt2');  "
                + "   for(var i = 0; i <items.length; ++i) { "
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
                + "     for(var i = 0; i <items.length; ++i) { "
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
                + "     for(var i = 0; i <items.length; ++i) { "
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
    public void initializeNext() {
        snapType = SnapType.Posts;
        r = Runtime.getRuntime();

        initWebView();
    }

    public void initWebView() {
        try {
            webView.setCache(false);
            webEngine = webView.getEngine();
            webEngine.setJavaScriptEnabled(true);
            // To avoid 414
            webEngine.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0) Gecko/20100101 Firefox/6.0");

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
//                        logger.debug(e.toString());
                    }
                }
            });
//            NetworkTools.readCookie(webEngine);

            webEngine.setOnError(new EventHandler<WebErrorEvent>() {

                @Override
                public void handle(WebErrorEvent event) {
                    logger.debug(event.getMessage());
                }
            });

            webEngine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
                @Override
                public void handle(WebEvent<String> ev) {
                    logger.debug(ev.getData());
                }
            });

            webEngine.getLoadWorker().exceptionProperty().addListener(new ChangeListener<Throwable>() {
                @Override
                public void changed(ObservableValue<? extends Throwable> ov,
                        Throwable ot, Throwable nt) {
                    if (nt == null) {
                        return;
                    }
                    logger.debug(nt.getMessage());
                }
            });

//            webEngine.locationProperty().addListener(new ChangeListener<String>() {
//                @Override
//                public void changed(ObservableValue ov, String oldv, String newv) {
//                    logger.debug(newv);
//                }
//            });
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    public void start(final WeiboSnapParameters parameters) {
        try {
            this.parameters = parameters;

            MinAccessInterval = parameters.getLoadInterval();  // To avoid 414
            MaxAccessInterval = MinAccessInterval * 10;
            loadLoopInterval = MinAccessInterval * 3;
            pageAccessDelay = 30 * MinAccessInterval;
            snapLoopInterval = parameters.getSnapInterval();
            dpi = parameters.getDpi();

//            logger.debug(parameters.getWebAddress());
            if (parameters.getWebAddress() == null || parameters.getWebAddress().isEmpty()) {
                closeStage();
                return;
            }

            if (AppVariables.getUserConfigBoolean("SSLBypassAll", false)) {
                NetworkTools.trustAll();

            } else {
                NetworkTools.myBoxSSL();

//                NetworkTools.installCertificateByHost("www.sina.com", "sina");
//                NetworkTools.installCertificateByHost("www.sina.com.cn", "sina.cn");
//                NetworkTools.installCertificateByHost("www.weibo.cn", "weibo.cn");
//                NetworkTools.installCertificateByHost("www.weibo.com", "weibo.com");
                // SSL handshake still fails even when above certficates imported!
                // This is workaround
                TableBrowserBypassSSL.write("weibo.com");

                Thread.sleep(loadLoopInterval);
            }

            savedHtmlCount = savedMonthPdfCount = savedPagePdfCount = completedMonthsCount = savedPixCount = retried = 0;
            loadFailed = loadCompleted = mainCompleted = false;
            startTime = new Date().getTime();
            tempdir = parameters.getTempdir();
            if (tempdir == null || !tempdir.exists() || !tempdir.isDirectory()) {
                tempdir = new File(AppVariables.MyboxDataPath);
            } else if (!tempdir.exists()) {
                if (!tempdir.mkdirs()) {
                    tempdir = AppVariables.MyBoxTempPath;
                }
            }
            memSettings = AppVariables.pdfMemUsage.setTempDir(tempdir);

            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            if (parameters.getWebWidth() <= 0) {
                myStage.setWidth((int) primaryScreenBounds.getWidth());
            } else {
                myStage.setWidth(parameters.getWebWidth());
            }
            if (snapType == SnapType.Posts) {
                myStage.setX(0);
            } else {
                myStage.setX(parameters.getWebWidth() - myStage.getWidth());
            }
            myStage.setY(0);
            myStage.setHeight(primaryScreenBounds.getHeight());

            loadMain();

        } catch (Exception e) {
            alertError(e.toString());
            closeStage();
        }

    }

    protected void loadMain() {
        try {
            if (!openLoadingStage()) {
                return;
            }
            loadingController.setInfo(AppVariables.message("CheckingWeiBoMain"));

            showMemInfo();

            webEngine.load(parameters.getWebAddress());
//            logger.debug(parameters.getWebAddress());
//            logger.debug("spleep: " + loadLoopInterval);
            Thread.sleep(loadLoopInterval);
//            NetworkTools.readCookie(webEngine);

            loadFailed = loadCompleted = mainCompleted = false;
            accountName = null;
            firstMonth = lastMonth = null;

            if (loadTimer != null) {
                loadTimer.cancel();
            }
            loadTimer = new Timer();
            loadStartTime = new Date().getTime();
            final long maxDelay;
            maxDelay = loadLoopInterval * 30;
//            logger.debug("loadLoopInterval:" + loadLoopInterval + "    maxDelay: " + maxDelay);

            TimerTask mainTask = new TimerTask() {
                int lastHeight = 0, newHeight = -1;

                protected String contents;

                @Override
                public void run() {
                    try {
                        if (new Date().getTime() - loadStartTime >= maxDelay) {
                            loadFailed = loadCompleted = true;
                            errorString = AppVariables.message("TimeOver");
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
                                    loadingController.setText(AppVariables.message("PageHeightLoaded") + ": " + newHeight);
                                    loadingController.addLine(AppVariables.message("CharactersLoaded") + ": " + contents.length());
                                    loadingController.addLine(AppVariables.message("SnapingStartTime") + ": " + DateTools.datetimeToString(loadStartTime)
                                            + " (" + AppVariables.message("ElapsedTime") + ": " + DateTools.showTime(new Date().getTime() - loadStartTime) + ")");
                                    showMemInfo();
//                                    logger.debug("newHeight: " + newHeight);
                                    if (contents.contains("Request-URI Too Large") || contents.contains("Request-URI Too Long")) {
                                        logger.debug(AppVariables.message("WeiBo414"));
                                        loadingController.setInfo(AppVariables.message("WeiBo414"));
                                        loadFailed = loadCompleted = true;
                                        errorString = AppVariables.message("WeiBo414");
                                        quit();
                                        return;
                                    } else if (contents.contains("帐号登录")) {
                                        loadFailed = loadCompleted = true;
                                        errorString = AppVariables.message("NonExistedWeiboAccount");
                                        quit();
                                        return;
                                    }
                                    int posAccount1 = contents.indexOf("<title>");
                                    int posAccount2 = contents.indexOf("_微博</title>");
                                    if (posAccount1 > 0 && posAccount2 > 0) {
                                        accountName = contents.substring(posAccount1 + "<title>".length(), posAccount2);
                                        TableStringValues.delete("WeiBoAddress", parameters.getWebAddress());
                                        TableStringValues.add("WeiBoAddress", accountName + "   " + parameters.getWebAddress());
                                        int posfirst1 = contents.indexOf("&stat_date=");
                                        if (posfirst1 > 0) {
                                            String s = contents.substring(posfirst1 + "&stat_date=".length());
                                            int posfirst2 = s.indexOf('\\');
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

                protected void quit() {
                    this.cancel();
                    loadCompleted = true;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            webEngine.getLoadWorker().cancel();
                            if (accountName == null || firstMonth == null || lastMonth == null) {
                                if (errorString == null) {
                                    errorString = AppVariables.message("NonExistedWeiboAccount");
                                }
                                loadFailed = true;
                            }
                            if (loadFailed) {
                                alertError(errorString);
                                if (parent == null && !parentOpened) {
                                    openStage(CommonValues.WeiboSnapFxml);
                                    parentOpened = true;
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

    protected boolean openLoadingStage() {
        try {
            if (loadingController != null) {
                return true;
            }

            loadingController
                    = (WeiboSnapingInfoController) FxmlStage.openStage(myStage,
                            CommonValues.WeiboSnapingInfoFxml,
                            true, Modality.WINDOW_MODAL, StageStyle.TRANSPARENT);
            loadingController.setParent(this);

            return true;

        } catch (Exception e) {
            alertError(e.toString());
            logger.debug(e.toString());
            closeStage();
            return false;
        }
    }

    protected void loadPages() {
        try {
            rootPath = new File(parameters.getTargetPath().getAbsolutePath() + File.separator + accountName);
//            logger.debug("rootPath: " + rootPath);
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
            totalMonthsCount = totalLikeCount = 0;
            while (m.getTime() <= parameters.getEndMonth().getTime()) {
                totalMonthsCount++;
                c.add(Calendar.MONTH, 1);
                m = c.getTime();
            }

            currentMonth = parameters.getStartMonth();
            if (currentPage < 0) {
                currentPage = 0;
            }
            currentMonthPageCount = 1;
            setStartPage();
            startPageChecked = false;
            loadFailed = loadCompleted = false;
            pdfs = new HashMap<>();

            loadNextPage();
        } catch (Exception e) {
            alertError(e.toString());
            closeStage();
        }
    }

    protected void setStartPage() {
        startPage = parameters.getStartPage();
    }

    protected void loadNextPage() {
        try {
            retried = 0;
            currentPage++;

//            logger.debug("currentPage: " + currentPage);
            if (!startPageChecked && currentMonthPageCount > 1) {
                while (currentPage < startPage) {
                    currentPage++;
                }
                startPageChecked = true;
            }
            if (currentPage > currentMonthPageCount) {
                completedMonthsCount++;
                if (parameters.isCreatePDF()) {
                    mergeMonthPdf(pdfPath, currentMonthString, currentMonthPageCount);
                }
                Calendar c = Calendar.getInstance();
                c.setTime(currentMonth);
                c.add(Calendar.MONTH, 1);
                currentMonth = c.getTime();
                if (currentMonth.getTime() > parameters.getEndMonth().getTime()) {
                    missCompleted();
                    return;
                }
                currentPage = 0;
                currentMonthPageCount = 1;
                startPageChecked = true;
                loadNextPage();
                return;
            }

            updateParameters();

            loadPage(currentAddress);
        } catch (Exception e) {
            retried = Integer.MAX_VALUE;
            errorString = e.toString();
            pageFailed(loadingController);
        }
    }

    protected void missCompleted() {
        if (openLoadingStage()) {
            loadingController.setInfo(AppVariables.message("DeleteEmptyDirectories"));
        }
        FileTools.deleteEmptyDir(rootPath, false);
        if (parameters.isMiao()) {
            FxmlControl.miao3();
        }
        if (parent != null) {
            parent.popInformation(AppVariables.message("MissCompleted"));
        }
        alertInformation(AppVariables.message("MissCompleted"));
        endSnap();
    }

    protected void updateParameters() {

    }

    protected void showBaseInfo() {
        if (!openLoadingStage()) {
            return;
        }
        loadingController.setText(AppVariables.message("WeiboAddress") + ": " + parameters.getWebAddress());
        loadingController.addLine(AppVariables.message("Account") + ": " + accountName);
        loadingController.addLine(AppVariables.message("FirstWeiboMonth") + ": " + DateTools.dateToMonthString(firstMonth));
        loadingController.addLine(AppVariables.message("LastWeiboMonth") + ": " + DateTools.dateToMonthString(lastMonth));
        loadingController.addLine(AppVariables.message("SnapDuration") + ": "
                + DateTools.dateToMonthString(parameters.getStartMonth()) + " ~ " + DateTools.dateToMonthString(parameters.getEndMonth()));
        loadingController.addLine(AppVariables.message("MonthsNumberWillSnap") + ": " + totalMonthsCount);
        loadingController.addLine(AppVariables.message("CompletedMonths") + ": " + completedMonthsCount);
        loadingController.addLine(AppVariables.message("CurrentLoadingMonth") + ": " + DateTools.dateToMonthString(currentMonth));
        loadingController.addLine(AppVariables.message("PagesNumberThisMonth") + ": " + currentMonthPageCount);
        loadingController.addLine(AppVariables.message("CurrentLoadingPage") + ": " + currentPage);
        loadingController.addLine(AppVariables.message("PdfFilesSaved") + ": " + (savedPagePdfCount + savedMonthPdfCount));
        loadingController.addLine(AppVariables.message("HtmlFilesSaved") + ": " + savedHtmlCount);
        loadingController.addLine(AppVariables.message("PicturesSaved") + ": " + savedPixCount);

        showMemInfo();

    }

    protected void showDynamicInfo() {
        long passed = new Date().getTime() - startTime;
        if (currentPagePicturesNumber > 0) {
            loadingController.addLine(AppVariables.message("CurrentPagePicturesNumber") + ": " + currentPagePicturesNumber);
        }
        loadingController.addLine(AppVariables.message("SnapingStartTime") + ": " + DateTools.datetimeToString(startTime)
                + " (" + AppVariables.message("ElapsedTime") + ": " + DateTools.showTime(passed) + ")");
        if (completedMonthsCount > 0) {
            long speed = passed / completedMonthsCount;
            loadingController.addLine(AppVariables.message("SpeedOfSnapingMonth") + ": " + DateTools.showTime(speed));
            long total = passed * totalMonthsCount / completedMonthsCount;
            long left = passed * (totalMonthsCount - completedMonthsCount) / completedMonthsCount;
            Calendar c = Calendar.getInstance();
            c.setTime(new Date(startTime));
            c.add(Calendar.SECOND, (int) (total / 1000));
            loadingController.addLine(AppVariables.message("PredictedCompleteTime") + ": " + DateTools.datetimeToString(c.getTime())
                    + " (" + AppVariables.message("LeftTime") + ": " + DateTools.showTime(left) + ")");
        }
    }

    protected void showMemInfo() {
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
                //                    + "  " + AppVariables.getMessage("AvailableProcessors") + ":" + availableProcessors
                + "  " + AppVariables.message("AvaliableMemory") + ":" + maxMemory + "MB"
                + "  " + AppVariables.message("RequiredMemory") + ":" + totalMemory + "MB(" + FloatTools.roundFloat2(totalMemory * 100.0f / physicalTotal) + "%)"
                + "  " + AppVariables.message("UsedMemory") + ":" + usedMemory + "MB(" + FloatTools.roundFloat2(usedMemory * 100.0f / physicalTotal) + "%)";

        memInfo += "\n" + System.getProperty("os.name")
                + "  " + AppVariables.message("TotalPhysicalMemory") + ":" + physicalTotal + "MB"
                + "  " + AppVariables.message("UsedPhysicalMemory") + ":" + physicalUse + "MB (" + FloatTools.roundFloat2(physicalUse * 100.0f / physicalTotal) + "%)";

        loadingController.showMem(memInfo);

    }

    protected void loadPage(final String address) {
        try {
            webEngine.load(address);
//            webEngine.executeScript("window.location.href='" + address + "';");
//            NetworkTools.readCookie(webEngine);

            if (loadTimer != null) {
                loadTimer.cancel();
                loadTimer = null;
            }
            if (!openLoadingStage()) {
                return;
            }

            loadedPicturesNumber = currentPagePicturesNumber = 0;
            loadFailed = loadCompleted = mainCompleted = false;
            commentsLoaded = picturedsLoaded = false;

            loadingController.setInfo(AppVariables.message("LoadingPage"));
            showBaseInfo();
            if (parameters.isCreateHtml()) {
                loadingController.addLine(AppVariables.message("LoadingForHtml") + ": " + htmlFilename);
            }
            baseText = loadingController.getText();

            loadTimer = new Timer();
            loadStartTime = new Date().getTime();
            skipPage = false;
            loadTimer.schedule(new TimerTask() {
                protected int lastHeight = 0, newHeight = -1, expandHeight;
                protected int stasisTimes = 0;
                protected boolean emptyPage = false;
                protected String contents;

                @Override
                public void run() {
                    try {
                        if (skipPage || loadFailed || loadCompleted) {
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

                                    loadingController.addLine(AppVariables.message("CurrentPageHeight") + ": " + newHeight);
                                    if (loadedPicturesNumber > 0) {
                                        loadingController.addLine(AppVariables.message("LoadedPicturesNumber") + ": " + loadedPicturesNumber);
                                    }
//                                    loadingController.addLine(AppVariables.getMessage("Loadint time") + ": " + (new Date().getTime() - loadStartTime) / 1000);
                                    if (totalLikeCount <= 0) {
                                        contents = (String) webEngine.executeScript("document.documentElement.outerHTML");
                                        int pos4 = contents.indexOf("赞过的微博<em class=\"S_txt2\">(共");
                                        if (pos4 > 0) {
                                            String s1 = contents.substring(pos4 + "赞过的微博<em class=\"S_txt2\">(共".length());
                                            int pos5 = s1.indexOf("条)</em></span>");
                                            if (pos5 > 0) {
                                                try {
                                                    totalLikeCount = Integer.valueOf(s1.substring(0, pos5));
                                                    showBaseInfo();
                                                } catch (Exception e) {
                                                    logger.debug(e.toString());
                                                }
                                            }
                                        }
                                    } else {
                                        contents = null;
                                    }
                                    if (newHeight == lastHeight) {
                                        stasisTimes++;
                                        if (mainCompleted && currentPagePicturesNumber > 0
                                                && (new Date().getTime() - lastPictureLoad.getTime()) > MaxAccessInterval) {
                                            loadingController.setInfo(AppVariables.message("WeiBoSkipPicture"));
                                            logger.debug("skip picture:" + loadedPicturesNumber);
                                            loadedPicturesNumber++;
                                            lastPictureLoad = new Date();
                                            webEngine.executeScript("myboxExpandPicture(" + loadedPicturesNumber + "); ");
                                            return;
                                        }
                                        if (stasisTimes >= MaxStasisTimes) {
                                            errorString = AppVariables.message("TimeOver");
//                                            logger.debug(errorString);
                                            loadFailed = loadCompleted = true;
                                            return;
                                        }

                                        if (!mainCompleted) {
                                            if (contents == null) {
                                                contents = (String) webEngine.executeScript("document.documentElement.outerHTML");
                                            }
                                            if (contents.contains("Request-URI Too Large") || contents.contains("Request-URI Too Long")) {
//                                                logger.debug("WeiBo414: " + currentPage);
                                                loadingController.setInfo(AppVariables.message("WeiBo414"));
                                                loadFailed = loadCompleted = true;
                                                errorString = AppVariables.message("WeiBo414");
                                                quit();
                                                return;
                                            } else if (contents.contains("查看更早微博")) {
                                                mainCompleted();
                                            } else if (contents.contains("还没有发过微博") || contents.contains("这里还没有内容")) {
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

                protected void mainCompleted() {
                    try {
                        skipPage = !startPageChecked && (startPage > currentPage);
//                        logger.debug(skipPage);
                        if (skipPage) {
                            return;
                        }
                        //                        NetworkTools.readCookie(webEngine);
                        if (parameters.isSavePictures()) {
                            loadingController.setInfo(AppVariables.message("SavingPictures"));
                            savePictures(webEngine.executeScript(findPicturesScript));
                        }
                        if (!parameters.isExpandPicture() && !parameters.isExpandComments()) {
                            mainCompleted = loadCompleted = true;
                            return;
                        }
                        if (parameters.isExpandComments()) {
                            loadingController.setInfo(AppVariables.message("ExpandingComments"));
                            webEngine.executeScript(expandCommentsScript);
                        } else {
                            commentsLoaded = true;
                        }

                        if (parameters.isExpandPicture()) {
                            currentPagePicturesNumber = (int) webEngine.executeScript(picturesNumberScript);
                            if (currentPagePicturesNumber > 0) {
                                loadingController.setInfo(AppVariables.message("ExpandingPictures"));
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
                        showBaseInfo();
                    } catch (Exception e) {
                        logger.error(e.toString());
//                        loadFailed = loadCompleted = true;
//                        errorString = e.toString();
//                        quit();
                    }
                }

                protected void quit() {
                    this.cancel();
                    loadCompleted = true;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (skipPage) {
                                loadNextPage();
                                return;
                            }
//                            NetworkTools.readCookie(webEngine);
                            webEngine.executeScript(clearScripts);
                            if (!loadFailed) {
                                retried = 0;
                                if (!emptyPage && parameters.isCreateHtml()) {
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

    protected void saveHtml(final String filename, final String contents) {
        if (filename == null || contents == null) {
            return;
        }
        Task<Void> saveHtmlTask = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    try ( BufferedWriter out = new BufferedWriter(new FileWriter(filename, Charset.forName("utf-8"), false))) {
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

    protected void savePictures(final Object address) {
        if (address == null) {
            return;
        }
        Task<Void> savePicturesTask = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    String s = (String) address;
                    s = s.replaceAll("/thumb150/", "/large/");
                    s = s.replaceAll("/orj360/", "/large/");
                    s = s.replaceAll("/thumb180/", "/large/");
                    String[] pix = s.split(",");
//                    logger.debug(Arrays.asList(pix));
                    final String prefix = pixFilePrefix;
                    String fname, suffix, saveName;
                    for (int i = 0; i < pix.length; ++i) {
                        fname = pix[i].trim();
                        int pos = fname.indexOf("&");
                        if (pos > 0) {
                            fname = fname.substring(0, pos);
                        }
                        if (fname.isEmpty()) {
                            continue;
                        }
                        suffix = FileTools.getFileSuffix(pix[i]);
                        if (suffix.isEmpty()) {
                            suffix = "jpg";
                            fname += "." + suffix;
                        }
                        try {
                            URL url = new URL("http:" + fname);
                            URLConnection con = url.openConnection();
                            con.setConnectTimeout(30000);
                            try ( InputStream is = con.getInputStream()) {
                                byte[] bs = new byte[CommonValues.IOBufferLength];
                                int len;
                                saveName = prefix + (i + 1) + "." + suffix;
                                try ( BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(saveName))) {
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

    protected void pageFailed(final WeiboSnapingInfoController controller) {
        try {
            if (errorString == null) {
                errorString = AppVariables.message("FailedWeiboSnap");
            }
            if (retried < parameters.getRetry()) {
                retried++;
                loadingController.showError(errorString + "\n"
                        + MessageFormat.format(AppVariables.message("RetryingTimes"), retried));

                loadingController.setInfo(AppVariables.message("WeiboSpleeping"));
                showBaseInfo();
                Thread.sleep(pageAccessDelay);

                loadPage(currentAddress);
                return;
            }
            if (parameters.isMiao()) {
                FxmlControl.miao3();
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

    protected void snapPage() {
        try {
            webEngine.executeScript("window.scrollTo(0,0 );");
            webEngine.executeScript("document.getElementsByTagName('body')[0].style.zoom = " + parameters.getZoomScale() + ";");
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
            loadingController.setInfo(AppVariables.message("SnapingPage"));
            showBaseInfo();
            if (currentPagePicturesNumber > 0) {
                loadingController.addLine(AppVariables.message("CurrentPagePicturesNumber") + ": " + currentPagePicturesNumber);
            }
            loadingController.addLine(AppVariables.message("PageZoomScale") + ": " + parameters.getZoomScale());
            loadingController.addLine(AppVariables.message("CurrentWindowHeight") + ": " + screenHeight);
            loadingController.addLine(AppVariables.message("CurrentWindowWidth") + ": " + screenWidth);
            if (parameters.isCreatePDF()) {
                loadingController.addLine(AppVariables.message("SnapingForPDF") + ": " + pdfFilename);
            }
            baseText = loadingController.getText();

            // http://news.kynosarges.org/2017/02/01/javafx-snapshot-scaling/
            final Bounds bounds = webView.getLayoutBounds();
            snapScale = dpi / Screen.getPrimary().getDpi();
            snapScale = snapScale > 1 ? snapScale : 1;
            snapImageWidth = (int) Math.round(bounds.getWidth() * snapScale);
            snapImageHeight = (int) Math.round(bounds.getHeight() * snapScale);
            snapParameters = new SnapshotParameters();
            snapParameters.setFill(Color.TRANSPARENT);
            snapParameters.setTransform(javafx.scene.transform.Transform.scale(snapScale, snapScale));

            snapStep = screenHeight - 100; // Offset due to the pop bar in the page.
            snapHeight = 0;

            imageFiles = new ArrayList<>();
            snapsTotal = pageHeight % snapStep == 0 ? pageHeight / snapStep : pageHeight / snapStep + 1;

            if (snapTimer != null) {
                snapTimer.cancel();
            }
            snapTimer = new Timer();
            snapTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        snap();
                    });
                }
            }, snapLoopInterval + 2000);

        } catch (Exception e) {
            errorString = e.toString();
            pageFailed(loadingController);
        }

    }

    protected void snap() {
        try {
            loadingController.setText(baseText);
            showMemInfo();
            showDynamicInfo();
            snapStartTime = new Date().getTime();
            WritableImage snapshot = new WritableImage(snapImageWidth, snapImageHeight);
            snapshot = webView.snapshot(snapParameters, snapshot);
            loadingController.addLine(AppVariables.message("CurrentSnapshotNumber") + ": " + imageFiles.size());
            try {
                // Save as png images temporarily. Quicker than jpg since no compression.
                // Final format is determined when write PDF file.
                String filename = pdfFilename + "-Image" + imageFiles.size() + ".png";
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);
                ImageFileWriters.writeImageFile(bufferedImage, "png", filename);
                imageFiles.add(filename);
            } catch (Exception e) {
                logger.debug(e.toString());
            }
            snapHeight += snapStep;
            loadingController.addLine(AppVariables.message("CurrentPageHeight") + ": " + snapHeight);

            if (pageHeight > snapHeight) {
                webEngine.executeScript("window.scrollTo(0, " + snapHeight + ");");
                if (snapTimer != null) {
                    snapTimer.cancel();
                }
                snapTimer = new Timer();
                snapTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            snap();
                        });
                    }
                }, snapLoopInterval);    // make sure page is loaded before snapping

            } else {  // all snapped
                if (pageHeight < snapHeight) {
                    int y1 = snapHeight - pageHeight + 100;
//                                            logger.debug(pageHeight + " " + snapHeight + " " + screenHeight + " " + y1);
                    Image lastSnap = FxmlImageManufacture.cropOutsideFx(snapshot, 0, y1 * snapScale,
                            (int) snapshot.getWidth() - 1, (int) snapshot.getHeight() - 1);
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(lastSnap, null);
                    ImageFileWriters.writeImageFile(bufferedImage, "png", imageFiles.get(imageFiles.size() - 1));
                }

                File currentPdf = new File(pdfFilename);
                loadingController.addLine(AppVariables.message("Generateing") + ": " + currentPdf.getAbsolutePath());
                Boolean isOK = PdfTools.imagesFiles2Pdf(imageFiles, currentPdf, parameters, true);
                for (String file : imageFiles) {
                    File f = new File(file);
                    if (f.exists()) {
                        f.delete();
                    }
                }
                imageFiles = null;

                if (!isOK || !currentPdf.exists()) {
                    errorString = AppVariables.message("FailedWeiboSnap");
                    pageFailed(loadingController);
                } else {
                    List<File> files = pdfs.get(currentMonthString);
                    if (files == null) {
                        files = new ArrayList<>();
                    }
                    files.add(currentPdf);
                    pdfs.put(currentMonthString, files);
                    if (snapType == SnapType.Like) {
                        savedPagePdfCount++;
                    }
                    loadNextPage();
                }

            }

        } catch (Exception e) {
            logger.error(e.toString());
            errorString = e.toString();
            pageFailed(loadingController);
        }

    }

    protected void mergeMonthPdf(final File path, final String month,
            final int pageCount) {

        Task<Void> mergeTask = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    List<File> files = new ArrayList<>();
                    long totalSize = 0;
                    for (int i = 1; i <= pageCount; ++i) {
                        String name = path.getAbsolutePath() + File.separator + accountName + "-"
                                + month + "-第" + i + "页.pdf";
                        File file = new File(name);
                        if (file.exists()) {
                            files.add(file);
                            totalSize += file.length();
                        }
                    }
                    if (files.isEmpty() || totalSize > 500 * 1024 * 1024) {
                        pdfs.remove(month);
                        savedPagePdfCount += files.size();
                        return null;
                    }

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

                    try ( PDDocument doc = PDDocument.load(monthFile, memSettings)) {
                        PDDocumentInformation info = new PDDocumentInformation();
                        info.setCreationDate(Calendar.getInstance());
                        info.setModificationDate(Calendar.getInstance());
                        info.setProducer("MyBox v" + CommonValues.AppVersion);
                        info.setAuthor(parameters.getAuthor());
                        doc.setDocumentInformation(info);
                        doc.setVersion(1.0f);

                        PDPage page = doc.getPage(0);
                        PDPageXYZDestination dest = new PDPageXYZDestination();
                        dest.setPage(page);
                        dest.setZoom(parameters.getPdfScale() / 100.0f);
                        dest.setTop((int) page.getCropBox().getHeight());
                        PDActionGoTo action = new PDActionGoTo();
                        action.setDestination(dest);
                        doc.getDocumentCatalog().setOpenAction(action);

                        doc.save(monthFile);
                        doc.close();
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

                return null;
            }
        };
        new Thread(mergeTask).start();
    }

    public void endSnap() {
        try {
            if (parent != null && snapType == SnapType.Posts) {
                parent.setDuration(currentMonthString, "");
            }
            if (parameters.isOpenPathWhenStop() && rootPath != null) {
                view(rootPath);
            }
            closeStage();
        } catch (Exception e) {
            closeStage();
        }
    }

    @Override
    public boolean leavingScene() {
        try {
            if (loadTimer != null) {
                loadTimer.cancel();
            }
            if (snapTimer != null) {
                snapTimer.cancel();
            }
            if (loadingController != null) {
                loadingController.closeStage();
            }

            if (webEngine != null) {
                webEngine.getLoadWorker().cancel();
                webEngine = null;
            }
        } catch (Exception e) {

        }

        return super.leavingScene();

    }

    public void openPath() {
        if (rootPath == null) {
            return;
        }
        view(rootPath);
    }

    public WeiboSnapController getParent() {
        return parent;
    }

    public void setParent(final WeiboSnapController parent) {
        this.parent = parent;
    }

}
