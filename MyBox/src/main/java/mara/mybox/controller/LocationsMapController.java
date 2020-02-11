package mara.mybox.controller;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.NetworkTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2020-1-19
 * @License Apache License Version 2.0
 */
public class LocationsMapController extends BaseController {

    protected WebEngine webEngine;
    protected String status;

    @FXML
    protected Button loadButton, plusButton;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected CheckBox bypassCheck;
    @FXML
    protected TextField bottomText, findInput;
    @FXML
    protected WebView webView;

    public LocationsMapController() {
        baseTitle = AppVariables.message("Locations");

        SourceFileType = VisitHistory.FileType.Html;
        SourcePathType = VisitHistory.FileType.Html;
        TargetPathType = VisitHistory.FileType.Html;
        TargetFileType = VisitHistory.FileType.Html;
        AddFileType = VisitHistory.FileType.Html;
        AddPathType = VisitHistory.FileType.Html;

        sourcePathKey = "HtmlFilePath";
        targetPathKey = "HtmlFilePath";

        sourceExtensionFilter = CommonFxValues.HtmlExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;

    }

    @Override
    public void initializeNext() {
        try {
            initWebEngine();

            File map = FxmlControl.getInternalFile("/js/map.html", "js", "map.html");
            NetworkTools.trustAll();
//            NetworkTools.myBoxSSL();
//            TableBrowserBypassSSL.write("amap.com");
//            TableBrowserBypassSSL.write("webapi.amap.com");
//            TableBrowserBypassSSL.write("alibabacorp.com");
            webEngine.loadContent(FileTools.readTexts(map));

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void initWebEngine() {
        try {
            webEngine = webView.getEngine();
            webEngine.setJavaScriptEnabled(true);

            webEngine.setOnAlert((WebEvent<String> ev) -> {
                FxmlStage.alertError(getMyStage(), ev.getData());
                logger.debug(ev.getData());
            });

            webEngine.setOnError((WebErrorEvent event) -> {
                popError(event.getMessage());
                logger.debug(event.getMessage());
            });
            webEngine.setOnStatusChanged((WebEvent<String> ev) -> {
                bottomLabel.setText(ev.getData());
//                    logger.debug(ev.getData());
            });

            webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue ov, Worker.State oldState,
                        Worker.State newState) {
                    try {
                        status = newState.name();
                        bottomLabel.setText(status);
                        switch (newState) {
                            case RUNNING:
                                bottomLabel.setText(AppVariables.message("Loading..."));
                                break;
                            case SUCCEEDED:
                                NetworkTools.defaultSSL();
                                break;
                            case CANCELLED:
                                bottomLabel.setText(message("Canceled"));
                                break;
                            case FAILED:
                                bottomLabel.setText(message("Failed"));
                                break;
                        }

                    } catch (Exception e) {
                        logger.debug(e.toString());
                    }

                }

            });

            webEngine.getLoadWorker().exceptionProperty().addListener(
                    (ObservableValue<? extends Throwable> ov, Throwable ot, Throwable nt) -> {
                        if (nt == null) {
                            return;
                        }
                        bottomLabel.setText(nt.getMessage());
                    });

            webEngine.locationProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        bottomLabel.setText(newv);
                    });

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

}
