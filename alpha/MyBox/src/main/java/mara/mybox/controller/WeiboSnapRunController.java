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
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import mara.mybox.data.WeiboSnapParameters;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.image.CropTools;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FloatTools;
import mara.mybox.tools.PdfTools;
import mara.mybox.tools.UrlTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;

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
    protected ChangeListener webEngineStateListener;
    protected SnapType snapType;
    protected SnapshotParameters snapParameters;
    protected int pageHeight, windowHeight, windowWidth, startPage, snapsTotal,
            snapImageWidth, snapImageHeight, snapHeight, snapStep;
    protected double snapScale;
    protected List<String> imageFiles;

    public enum SnapType {
        Posts, Like
    }

    @FXML
    protected WebView webView;

    public WeiboSnapRunController() {
        baseTitle = Languages.message("WeiboSnap");

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
    public void initControls() {
        super.initControls();
        snapType = SnapType.Posts;
        r = Runtime.getRuntime();

        initWebView();
    }

    public void initWebView() {
        try {
            webEngine = webView.getEngine();
            webView.setCache(false);
            webEngine.setJavaScriptEnabled(true);
            webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/610.2 (KHTML, like Gecko) JavaFX/17 Safari/610.2");

            webEngine.setOnAlert(new EventHandler<WebEvent<String>>() {
                @Override
                public void handle(WebEvent<String> ev) {
                    try {
//                        MyBoxLog.debug(loadedPicturesNumber + "  setOnAlert:" + ev.getData());
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
//                        MyBoxLog.debug(e);
                    }
                }
            });
//            NetworkTools.readCookie(webEngine);

            webEngine.setOnError(new EventHandler<WebErrorEvent>() {

                @Override
                public void handle(WebErrorEvent event) {
                    MyBoxLog.debug(event.getMessage());
                }
            });

            webEngine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
                @Override
                public void handle(WebEvent<String> ev) {
                    MyBoxLog.debug(ev.getData());
                }
            });

            webEngine.getLoadWorker().exceptionProperty().addListener(new ChangeListener<Throwable>() {
                @Override
                public void changed(ObservableValue<? extends Throwable> ov, Throwable ot, Throwable nt) {
                    if (nt == null) {
                        return;
                    }
                    MyBoxLog.debug(nt.getMessage());
                }
            });

//            webEngine.locationProperty().addListener(new ChangeListener<String>() {
//                @Override
//                public void changed(ObservableValue ov, String oldv, String newv) {
//                    MyBoxLog.debug(newv);
//                }
//            });
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }

    }

    public void start(final WeiboSnapParameters parameters) {
        try {
            setValues(parameters);
//            MyBoxLog.debug(parameters.getWebAddress());
            if (parameters.getWebAddress() == null || parameters.getWebAddress().isEmpty() || parameters.getTargetPath() == null) {
                PopTools.alertError(this, message("InvalidParameters"));
                closeStage();
                return;
            }
            loadMain();

        } catch (Exception e) {
            alertError(e.toString());
            closeStage();
        }
    }

    public void setValues(final WeiboSnapParameters parameters) {
        try {
            this.parameters = parameters;

            MinAccessInterval = parameters.getLoadInterval();  // To avoid 414
            MaxAccessInterval = MinAccessInterval * 10;
            loadLoopInterval = MinAccessInterval * 3;
            pageAccessDelay = 30 * MinAccessInterval;
            snapLoopInterval = parameters.getSnapInterval();
            dpi = parameters.getDpi();

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
            loadingController.setInfo(Languages.message("CheckingWeiBoMain"));

            showMemInfo();

            webEngine.load(parameters.getWebAddress());
//            MyBoxLog.debug(parameters.getWebAddress());
//            MyBoxLog.debug("spleep: " + loadLoopInterval);
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
//            MyBoxLog.debug("loadLoopInterval:" + loadLoopInterval + "    maxDelay: " + maxDelay);

            TimerTask mainTask = new TimerTask() {
                int lastHeight = 0, newHeight = -1;

                protected String contents;

                @Override
                public void run() {
                    try {
                        if (new Date().getTime() - loadStartTime >= maxDelay) {
                            loadFailed = loadCompleted = true;
                            errorString = Languages.message("TimeOver");
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
                                    loadingController.setText(Languages.message("PageHeightLoaded") + ": " + newHeight);
                                    loadingController.addLine(Languages.message("CharactersLoaded") + ": " + contents.length());
                                    loadingController.addLine(Languages.message("SnapingStartTime") + ": " + DateTools.datetimeToString(loadStartTime)
                                            + " (" + Languages.message("ElapsedTime") + ": " + DateTools.datetimeMsDuration(new Date().getTime() - loadStartTime) + ")");
                                    showMemInfo();
//                                    MyBoxLog.debug("newHeight: " + newHeight);
                                    if (contents.contains("Request-URI Too Large") || contents.contains("Request-URI Too Long")) {
                                        MyBoxLog.debug(Languages.message("WeiBo414"));
                                        loadingController.setInfo(Languages.message("WeiBo414"));
                                        loadFailed = loadCompleted = true;
                                        errorString = Languages.message("WeiBo414");
                                        quit();
                                        return;
                                    } else if (contents.contains("帐号登录")) {
                                        loadFailed = loadCompleted = true;
                                        errorString = Languages.message("NonExistedWeiboAccount");
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
                                                    lastMonth = DateTools.encodeDate(s.substring(0, 4) + "-" + s.substring(4, 6));
//                                                    MyBoxLog.debug(DateTools.datetimeToString(lastMonth));
                                                    int posLast1 = contents.lastIndexOf("&stat_date=");
                                                    if (posLast1 > 0) {
                                                        s = contents.substring(posLast1 + "&stat_date=".length());
                                                        int posLast2 = s.indexOf("&page=");
                                                        if (posLast2 <= 0) {
                                                            posLast2 = s.indexOf('\\');
                                                        }
                                                        if (posLast2 > 0) {
                                                            try {
                                                                s = s.substring(0, posLast2);
                                                                firstMonth = DateTools.encodeDate(s.substring(0, 4) + "-" + s.substring(4, 6));
//                                                                MyBoxLog.debug(DateTools.datetimeToString(firstMonth));
                                                                loadCompleted = true;
                                                            } catch (Exception e) {
                                                                MyBoxLog.error(e);
                                                            }
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    MyBoxLog.error(e);
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
                                    MyBoxLog.error(e);
                                }
                            }
                        });
                    } catch (Exception e) {
                        loadFailed = loadCompleted = true;
                        MyBoxLog.error(e);
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
                                    errorString = Languages.message("NonExistedWeiboAccount");
                                }
                                loadFailed = true;
                            }
                            if (loadFailed) {
                                mainFailed();
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

    public void mainFailed() {
        if (Languages.message("NonExistedWeiboAccount").equals(errorString)) {
            int mainRetried = parameters.getRetried();
            if (mainRetried < 2) {
                parameters.setRetried(mainRetried + 1);
                WeiboSnapPostsController pageController = (WeiboSnapPostsController) openStage(Fxmls.WeiboSnapPostsFxml);
                pageController.start(parameters);
                closeStage();
            } else {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(baseTitle);
                alert.setContentText(errorString);
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                ButtonType buttonRetry = new ButtonType(Languages.message("Retry"));
                ButtonType buttonExample = new ButtonType(Languages.message("Example"));
                ButtonType buttonISee = new ButtonType(Languages.message("ISee"));
                alert.getButtonTypes().setAll(buttonRetry, buttonExample, buttonISee);
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.setAlwaysOnTop(true);
                stage.toFront();

                Optional<ButtonType> result = alert.showAndWait();
                if (result == null || !result.isPresent()) {
                    return;
                }
                if (result.get() == buttonRetry) {
                    parameters.setRetried(mainRetried + 1);
                    WeiboSnapPostsController pageController = (WeiboSnapPostsController) openStage(Fxmls.WeiboSnapPostsFxml);
                    pageController.start(parameters);
                    closeStage();
                } else if (result.get() == buttonExample) {
                    WeiboSnapPostsController pageController = (WeiboSnapPostsController) openStage(Fxmls.WeiboSnapPostsFxml);
                    parameters.setWebAddress(WeiboSnapController.exmapleAddress);
                    parameters.setStartMonth(DateTools.encodeDate("2014-09"));
                    parameters.setEndMonth(DateTools.encodeDate("2014-10"));
                    pageController.start(parameters);
                    closeStage();
                } else {
                    if (parent == null && !parentOpened) {
                        openStage(Fxmls.WeiboSnapFxml);
                        parentOpened = true;
                    }
                    endSnap();
                }
            }
        } else {
            alertError(errorString);
            if (parent == null && !parentOpened) {
                openStage(Fxmls.WeiboSnapFxml);
                parentOpened = true;
            }
            endSnap();
        }
    }

    protected boolean openLoadingStage() {
        try {
            if (loadingController != null) {
                return true;
            }

            loadingController = (WeiboSnapingInfoController) WindowTools.popupStage(this, Fxmls.WeiboSnapingInfoFxml);
            loadingController.setParent(this);

            return true;

        } catch (Exception e) {
            alertError(e.toString());
            MyBoxLog.debug(e);
            closeStage();
            return false;
        }
    }

    protected void loadPages() {
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
            clearPdfPath(pdfPath);

            retried = 0;
            currentPage++;

            if (!startPageChecked && currentMonthPageCount > 1) {
                while (currentPage < startPage) {
                    currentPage++;
                }
                startPageChecked = true;
            }
            if (currentPage > currentMonthPageCount) {

                if (snapType == SnapType.Like) {
                    missionCompleted();
                    return;
                }
                completedMonthsCount++;
                if (parameters.isCreatePDF()) {
                    mergeMonthPdf(pdfPath, currentMonthString, currentMonthPageCount);
                }
                FileDeleteTools.deleteEmptyDir(null, pdfPath, false);
                FileDeleteTools.deleteEmptyDir(null, htmlPath, false);
                FileDeleteTools.deleteEmptyDir(null, pixPath, false);
                Calendar c = Calendar.getInstance();
                c.setTime(currentMonth);
                c.add(Calendar.MONTH, 1);
                currentMonth = c.getTime();
                if (currentMonth.getTime() > parameters.getEndMonth().getTime()) {
                    missionCompleted();
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

    protected void missionCompleted() {
        if (parameters.isMiao()) {
            SoundTools.miao3();
        }
        if (parent != null) {
            parent.popInformation(Languages.message("MissionCompleted"));
        }
        alertInformation(Languages.message("MissionCompleted"));
        endSnap();
    }

    protected void updateParameters() {

    }

    protected void showBaseInfo() {
        if (!openLoadingStage()) {
            return;
        }
        loadingController.setText(Languages.message("WeiboAddress") + ": " + parameters.getWebAddress());
        loadingController.addLine(Languages.message("Account") + ": " + accountName);
        loadingController.addLine(Languages.message("FirstWeiboMonth") + ": " + DateTools.dateToMonthString(firstMonth));
        loadingController.addLine(Languages.message("LastWeiboMonth") + ": " + DateTools.dateToMonthString(lastMonth));
        loadingController.addLine(Languages.message("SnapDuration") + ": "
                + DateTools.dateToMonthString(parameters.getStartMonth()) + " ~ " + DateTools.dateToMonthString(parameters.getEndMonth()));
        loadingController.addLine(Languages.message("MonthsNumberWillSnap") + ": " + totalMonthsCount);
        loadingController.addLine(Languages.message("CompletedMonths") + ": " + completedMonthsCount);
        loadingController.addLine(Languages.message("CurrentLoadingMonth") + ": " + DateTools.dateToMonthString(currentMonth));
        loadingController.addLine(Languages.message("PagesNumberThisMonth") + ": " + currentMonthPageCount);
        loadingController.addLine(Languages.message("CurrentLoadingPage") + ": " + currentPage);
        loadingController.addLine(Languages.message("PdfFilesSaved") + ": " + (savedPagePdfCount + savedMonthPdfCount));
        loadingController.addLine(Languages.message("HtmlFilesSaved") + ": " + savedHtmlCount);
        loadingController.addLine(Languages.message("PicturesSaved") + ": " + savedPixCount);

        showMemInfo();

    }

    protected void showDynamicInfo() {
        long passed = new Date().getTime() - startTime;
        if (currentPagePicturesNumber > 0) {
            loadingController.addLine(Languages.message("CurrentPagePicturesNumber") + ": " + currentPagePicturesNumber);
        }
        loadingController.addLine(Languages.message("SnapingStartTime") + ": " + DateTools.datetimeToString(startTime)
                + " (" + Languages.message("ElapsedTime") + ": " + DateTools.datetimeMsDuration(passed) + ")");
        if (completedMonthsCount > 0) {
            long speed = passed / completedMonthsCount;
            loadingController.addLine(Languages.message("SpeedOfSnapingMonth") + ": " + DateTools.datetimeMsDuration(speed));
            long total = passed * totalMonthsCount / completedMonthsCount;
            long left = passed * (totalMonthsCount - completedMonthsCount) / completedMonthsCount;
            Calendar c = Calendar.getInstance();
            c.setTime(new Date(startTime));
            c.add(Calendar.SECOND, (int) (total / 1000));
            loadingController.addLine(Languages.message("PredictedCompleteTime") + ": " + DateTools.datetimeToString(c.getTime())
                    + " (" + Languages.message("LeftTime") + ": " + DateTools.datetimeMsDuration(left) + ")");
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
        long physicalFree = osmxb.getFreeMemorySize() / mb;
        long physicalTotal = osmxb.getTotalMemorySize() / mb;
        long physicalUse = physicalTotal - physicalFree;

        String memInfo = "MyBox"
                //                    + "  " + AppVariables.getMessage("AvailableProcessors") + ":" + availableProcessors
                + "  " + Languages.message("AvaliableMemory") + ":" + maxMemory + "MB"
                + "  " + Languages.message("RequiredMemory") + ":" + totalMemory + "MB(" + FloatTools.roundFloat2(totalMemory * 100.0f / physicalTotal) + "%)"
                + "  " + Languages.message("UsedMemory") + ":" + usedMemory + "MB(" + FloatTools.roundFloat2(usedMemory * 100.0f / physicalTotal) + "%)";

        memInfo += "\n" + System.getProperty("os.name")
                + "  " + Languages.message("TotalPhysicalMemory") + ":" + physicalTotal + "MB"
                + "  " + Languages.message("UsedPhysicalMemory") + ":" + physicalUse + "MB (" + FloatTools.roundFloat2(physicalUse * 100.0f / physicalTotal) + "%)";

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

            loadingController.setInfo(Languages.message("LoadingPage"));
            showBaseInfo();
            if (parameters.isCreateHtml()) {
                loadingController.addLine(Languages.message("LoadingForHtml") + ": " + htmlFilename);
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

                                    loadingController.addLine(Languages.message("CurrentPageHeight") + ": " + newHeight);
                                    if (loadedPicturesNumber > 0) {
                                        loadingController.addLine(Languages.message("LoadedPicturesNumber") + ": " + loadedPicturesNumber);
                                    }
//                                    loadingController.addLine(AppVariables.getMessage("Loadint timeDuration") + ": " + (new Date().getTime() - loadStartTime) / 1000);
                                    if (totalLikeCount <= 0) {
                                        contents = (String) webEngine.executeScript("document.documentElement.outerHTML");
                                        int pos4 = contents.indexOf("赞过的微博<em class=\"S_txt2\">(共");
                                        if (pos4 > 0) {
                                            String s1 = contents.substring(pos4 + "赞过的微博<em class=\"S_txt2\">(共".length());
                                            int pos5 = s1.indexOf("条)</em></span>");
                                            if (pos5 > 0) {
                                                try {
                                                    totalLikeCount = Integer.parseInt(s1.substring(0, pos5));
                                                    showBaseInfo();
                                                } catch (Exception e) {
                                                    MyBoxLog.debug(e);
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
                                            loadingController.setInfo(Languages.message("WeiBoSkipPicture"));
                                            MyBoxLog.debug("skip picture:" + loadedPicturesNumber);
                                            loadedPicturesNumber++;
                                            lastPictureLoad = new Date();
                                            webEngine.executeScript("myboxExpandPicture(" + loadedPicturesNumber + "); ");
                                            return;
                                        }
                                        if (stasisTimes >= MaxStasisTimes) {
//                                            errorString = AppVariables.message("TimeOver");
////                                            MyBoxLog.debug(errorString);
//                                            loadFailed = loadCompleted = true;
                                            loadCompleted = true;  // Snap anyway
                                            return;
                                        }

                                        if (!mainCompleted) {
                                            if (contents == null) {
                                                contents = (String) webEngine.executeScript("document.documentElement.outerHTML");
                                            }
                                            if (contents.contains("Request-URI Too Large") || contents.contains("Request-URI Too Long")) {
//                                                MyBoxLog.debug("WeiBo414: " + currentPage);
                                                loadingController.setInfo(Languages.message("WeiBo414"));
                                                loadFailed = loadCompleted = true;
                                                errorString = Languages.message("WeiBo414");
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
                                                                currentPage = Integer.parseInt(s1.substring(0, pos2));
                                                                currentMonthPageCount = Integer.parseInt(s1.substring(pos2 + "&amp;countPage=".length(), pos3));
                                                                mainCompleted();
                                                            } catch (Exception e) {
//                                                            loadFailed = loadCompleted = true;
//                                                            errorString = e.toString();
                                                                MyBoxLog.debug(e);
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
                                        expandHeight += windowHeight;
                                        webEngine.executeScript("window.scrollTo(0," + expandHeight + ");");
                                    } else {
                                        webEngine.executeScript("window.scrollTo(0," + newHeight + ");");
                                    }

                                } catch (Exception e) {
//                                    loadFailed = loadCompleted = true;
//                                    errorString = e.toString();
//                                    MyBoxLog.debug(e);
                                }
                            }
                        });

                    } catch (Exception e) {
                        MyBoxLog.error(e);
//                        loadFailed = loadCompleted = true;
//                        errorString = e.toString();
//                        quit();
                    }
                }

                protected void mainCompleted() {
                    try {
                        skipPage = !startPageChecked && (startPage > currentPage);
//                        MyBoxLog.debug(skipPage);
                        if (skipPage) {
                            return;
                        }
                        //                        NetworkTools.readCookie(webEngine);
                        if (parameters.isSavePictures()) {
                            loadingController.setInfo(Languages.message("SavingPictures"));
                            savePictures(webEngine.executeScript(findPicturesScript));
                        }
                        if (!parameters.isExpandPicture() && !parameters.isExpandComments()) {
                            mainCompleted = loadCompleted = true;
                            return;
                        }
                        if (parameters.isExpandComments()) {
                            loadingController.setInfo(Languages.message("ExpandingComments"));
                            webEngine.executeScript(expandCommentsScript);
                        } else {
                            commentsLoaded = true;
                        }

                        if (parameters.isExpandPicture()) {
                            currentPagePicturesNumber = (int) webEngine.executeScript(picturesNumberScript);
                            if (currentPagePicturesNumber > 0) {
                                loadingController.setInfo(Languages.message("ExpandingPictures"));
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
                        windowHeight = (Integer) webEngine.executeScript("document.documentElement.clientHeight || document.body.clientHeight;");
                        showBaseInfo();
                    } catch (Exception e) {
                        MyBoxLog.error(e);
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
        FxTask<Void> saveHtmlTask = new FxTask<Void>() {

            @Override
            protected boolean handle() {
                try {
                    try (BufferedWriter out = new BufferedWriter(new FileWriter(filename, Charset.forName("utf-8"), false))) {
                        out.write(contents);
                        out.flush();
                        savedHtmlCount++;
                    }
                    return true;
                } catch (Exception e) {
                    loadFailed = true;
                    errorString = e.toString();
                    return false;
                }
            }
        };
        new Thread(saveHtmlTask).start();
    }

    protected void savePictures(final Object address) {
        if (address == null) {
            return;
        }
        FxTask<Void> savePicturesTask = new FxTask<Void>() {
            @Override
            protected boolean handle() {
                try {
                    String s = (String) address;
                    s = s.replaceAll("/thumb150/", "/large/");
                    s = s.replaceAll("/orj360/", "/large/");
                    s = s.replaceAll("/thumb180/", "/large/");
                    String[] pix = s.split(",");
//                    MyBoxLog.debug(Arrays.asList(pix));
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
                        suffix = FileNameTools.ext(pix[i]);
                        if (suffix.isEmpty()) {
                            suffix = "jpg";
                            fname += "." + suffix;
                        }
                        try {
                            URL url = UrlTools.url("http:" + fname);
                            if (url == null) {
                                return false;
                            }
                            URLConnection con = url.openConnection();
                            con.setConnectTimeout(30000);
                            try (InputStream is = con.getInputStream()) {
                                byte[] bs = new byte[AppValues.IOBufferLength];
                                int len;
                                saveName = prefix + (i + 1) + "." + suffix;
                                try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(saveName))) {
                                    while ((len = is.read(bs)) > 0) {
                                        os.write(bs, 0, len);
                                    }
                                    savedPixCount++;
//                                try {
//                                    File f = new File(fname);
//                                    f.setLastModified(currentMonth.getTime());
//                                } catch (Exception e) {
//                                    MyBoxLog.error(e);
//                                }
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
                return true;
            }
        };
        new Thread(savePicturesTask).start();

    }

    public void reloadPage() {
        retried = 0;
//        loadingController.getReloadButton().setDisable(true);
        loadingController.showError("");
        loadPage(currentAddress);
    }

    protected void pageFailed(final WeiboSnapingInfoController controller) {
        try {
            if (errorString == null) {
                errorString = Languages.message("FailedWeiboSnap");
            }
            if (retried < parameters.getRetry()) {
                retried++;
                loadingController.showError(errorString + "\n"
                        + MessageFormat.format(Languages.message("RetryingTimes"), retried));

                loadingController.setInfo(Languages.message("WeiboSpleeping"));
                showBaseInfo();
                Thread.sleep(pageAccessDelay);

                loadPage(currentAddress);
                return;
            }
            if (parameters.isMiao()) {
                SoundTools.miao3();
            }
            if (controller != null) {
                controller.showError(errorString);
//                controller.getReloadButton().setDisable(false);
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
//            myStage.setHeight(windowHeight);
            webEngine.executeScript("window.scrollTo(0,0 );");
            webEngine.executeScript("document.getElementsByTagName('body')[0].style.zoom = " + parameters.getZoomScale() + ";");
            pageHeight = (Integer) webEngine.executeScript("document.documentElement.scrollHeight || document.body.scrollHeight;");
            windowHeight = (Integer) webEngine.executeScript("document.documentElement.clientHeight || document.body.clientHeight;");
            windowWidth = (Integer) webEngine.executeScript("document.documentElement.clientWidth || document.body.clientWidth;");
            if (pageHeight == 0 || windowHeight == 0 || windowWidth == 0) {
                errorString = "";
                pageFailed(loadingController);
            }

            if (!openLoadingStage()) {
                errorString = "";
                pageFailed(loadingController);
            }
            loadingController.setInfo(Languages.message("SnapingPage"));
            showBaseInfo();
            if (currentPagePicturesNumber > 0) {
                loadingController.addLine(Languages.message("CurrentPagePicturesNumber") + ": " + currentPagePicturesNumber);
            }
            loadingController.addLine(Languages.message("PageZoomScale") + ": " + parameters.getZoomScale());
            loadingController.addLine(Languages.message("CurrentWindowHeight") + ": " + windowHeight);
            loadingController.addLine(Languages.message("CurrentWindowWidth") + ": " + windowWidth);
            if (parameters.isCreatePDF()) {
                loadingController.addLine(Languages.message("SnapingForPDF") + ": " + pdfFilename);
            }
            baseText = loadingController.getText();

            // http://news.kynosarges.org/2017/02/01/javafx-snapshot-scaling/
            final Bounds bounds = webView.getLayoutBounds();
            snapScale = NodeTools.dpiScale(dpi);
            snapScale = snapScale > 1 ? snapScale : 1;
            snapImageWidth = (int) Math.round(bounds.getWidth() * snapScale);
            snapImageHeight = (int) Math.round(bounds.getHeight() * snapScale);
            snapParameters = new SnapshotParameters();
            snapParameters.setFill(Color.TRANSPARENT);
            snapParameters.setTransform(javafx.scene.transform.Transform.scale(snapScale, snapScale));

            snapStep = windowHeight - 100; // Offset due to the pop bar in the page.
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
            Image snapshot = webView.snapshot(snapParameters, null);
            loadingController.addLine(Languages.message("CurrentSnapshotNumber") + ": " + imageFiles.size());
            try {
                // Save as png images temporarily. Quicker than jpg since no compression.
                // Final format is determined when write PDF file.
                String filename = pdfFilename + "-Image" + imageFiles.size() + ".png";
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);
                ImageFileWriters.writeImageFile(task, bufferedImage, "png", filename);
                imageFiles.add(filename);
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            snapHeight += snapStep;
            loadingController.addLine(Languages.message("CurrentPageHeight") + ": " + snapHeight);

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
//                                            MyBoxLog.debug(pageHeight + " " + snapHeight + " " + windowHeight + " " + y1);
                    String filename = imageFiles.get(imageFiles.size() - 1);
                    Image lastSnap = CropTools.cropOutsideFx(task, snapshot, 0, y1 * snapScale,
                            (int) snapshot.getWidth(), (int) snapshot.getHeight());
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(lastSnap, null);
                    FileDeleteTools.delete(new File(filename));
                    ImageFileWriters.writeImageFile(task, bufferedImage, "png", filename);
                }

                File currentPdf = new File(pdfFilename);
                loadingController.addLine(Languages.message("Generateing") + ": " + currentPdf.getAbsolutePath());
                Boolean isOK = PdfTools.imagesFiles2Pdf(task, imageFiles, currentPdf, parameters, true);
                for (String file : imageFiles) {
                    FileDeleteTools.delete(file);
                }
                imageFiles = null;

                if (!isOK || !currentPdf.exists()) {
                    errorString = Languages.message("FailedWeiboSnap");
                    pageFailed(loadingController);
                } else {
                    List<File> files = pdfs.get(currentMonthString);
                    if (files == null) {
                        files = new ArrayList<>();
                    }
                    files.add(currentPdf);
                    pdfs.put(currentMonthString, files);
                    savedPagePdfCount++;
                    loadNextPage();
                }

            }

        } catch (Exception e) {
            MyBoxLog.error(e);
            errorString = e.toString();
            pageFailed(loadingController);
        }

    }

    protected void clearPdfPath(final File path) {
        if (path == null || !path.exists() || !path.isDirectory()) {
            return;
        }
        FxTask<Void> clearTask = new FxTask<Void>() {
            @Override
            protected boolean handle() {
                try {
                    Thread.sleep(5000);
                    File[] pathFiles = path.listFiles();
                    if (pathFiles != null) {
                        for (File pathFile : pathFiles) {
                            if (pathFile.isFile() && pathFile.getAbsolutePath().endsWith(".png")) {
                                FileDeleteTools.delete(pathFile);
                            }
                        }
                    }
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
                return true;
            }
        };
        new Thread(clearTask).start();
    }

    protected void mergeMonthPdf(final File path, final String month, final int pageCount) {
        FxTask<Void> mergeTask = new FxTask<Void>() {
            @Override
            protected boolean handle() {
                try {
                    List<File> files = new ArrayList<>();
                    for (int i = 1; i <= pageCount; ++i) {
                        String name = path.getAbsolutePath() + File.separator + accountName + "-"
                                + month + "-第" + i + "页.pdf";
                        File file = new File(name);
                        if (file.exists()) {
                            files.add(file);
                        }
                    }
                    if (files.isEmpty()) {
                        pdfs.remove(month);
                        return true;
                    }

                    String monthFileName = path.getAbsolutePath() + File.separator + accountName + "-" + month + ".pdf";
                    File monthFile = new File(monthFileName);
                    FileDeleteTools.delete(monthFile);
                    if (files.size() == 1) {
                        FileTools.override(files.get(0), monthFile);
                        savedMonthPdfCount++;
                        savedPagePdfCount--;
                        pdfs.remove(month);
                        return true;
                    }

                    boolean keep = parameters.isKeepPagePdf();
                    PDFMergerUtility mergePdf = new PDFMergerUtility();
                    for (File file : files) {
                        mergePdf.addSource(file);
                    }
                    mergePdf.setDestinationFileName(monthFileName);
                    mergePdf.mergeDocuments(null);

                    try (PDDocument doc = Loader.loadPDF(monthFile)) {
                        PdfTools.setAttributes(doc, parameters.getAuthor(), parameters.getPdfScale());

                        doc.save(monthFile);
                        doc.close();
                        savedMonthPdfCount++;
                    }

                    if (!keep) {
                        for (File file : files) {
                            try {
                                FileDeleteTools.delete(file);
                                savedPagePdfCount--;
                            } catch (Exception e) {
                                MyBoxLog.error(e);
                            }
                        }
                    }
                    pdfs.remove(month);
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
                return true;
            }
        };
        new Thread(mergeTask).start();
    }

    public void endSnap() {
        try {
            if (openLoadingStage()) {
                loadingController.setInfo(Languages.message("DeleteEmptyDirectories"));
            }
            FileDeleteTools.deleteEmptyDir(null, rootPath, false);
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
    public void cleanPane() {
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
        super.cleanPane();
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
