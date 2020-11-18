package mara.mybox.controller;

import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.TextCollectingVisitor;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import mara.mybox.data.DownloadTask;
import mara.mybox.data.Link;
import mara.mybox.data.Link.FilenameType;
import mara.mybox.data.StringTable;
import mara.mybox.data.tools.VisitHistoryTools;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-10-11
 * @License Apache License Version 2.0
 */
public class DownloadFirstLevelLinksController extends BaseController {

    protected final ObservableList<Link> linksData, downloadingData, failedData;
    protected static final Boolean downloadingLock = false, failedLock = false;
    protected int maxThreadsNumber, maxLogs, maxRetries;
    protected final List<DownloadThread> downloadThreads;
    protected final List<PathThread> pathThreads;
    protected final Map<File, Integer> paths;
    protected final Map<Link, Integer> retries;
    protected final Map<URL, File> completedAddresses;
    protected final Map<File, Link> completedLinks;
    protected boolean stopped;
    protected Link addressLink;
    protected String subPath;
    protected FilenameType filenameType;
    protected Parser htmlParser;
    protected HtmlRenderer htmlRender;
    protected MutableDataHolder htmlOptions;
    protected FlexmarkHtmlConverter mdConverter;
    protected DataHolder pdfOptions;
    protected MutableDataSet textOptions;
    protected Parser textParser;
    protected TextCollectingVisitor textCollectingVisitor;
    protected String ttf;

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab linksTab, optionsTab, downloadingTab, failedTab, logsTab;
    @FXML
    protected ComboBox<String> urlBox;
    @FXML
    protected TextField maxLogsinput, webConnectTimeoutInput, webReadTimeoutInput;
    @FXML
    protected TableView<Link> linksTableView, downloadingTableView, failedTableView;
    @FXML
    protected TableColumn<Link, String> addressPathColumn, addressFileColumn,
            filenameColumn, nameColumn, titleColumn, pathColumn, fileColumn,
            downloadingLinkColumn, downloadingFileColumn, failedLinkColumn, failedFileColumn;
    @FXML
    protected TableColumn<Link, Integer> indexColumn;
    @FXML
    protected ControlFileSelecter targetPathController;
    @FXML
    protected Button downloadButton, equalButton, linkButton, htmlButton,
            clearDownloadingButton, deleteDownloadingButton, copyDownloadingButton,
            infoDownloadingButton, viewDownloadingButton, linkDownloadingButton,
            downloadFailedButton, linkFailedButton, clearFailedButton,
            deleteFailedButton, copyFailedButton, infoFailedButton, viewFailedButton;
    @FXML
    protected CheckBox indexCheck, relinksCheck, miaowCheck, utf8Check,
            htmlCheck, textCheck, mdCheck, pdfMarkdownCheck, pdfTextCheck, pdfHtmlCheck;
    @FXML
    protected ControlTimeLength intervalController;
    @FXML
    protected Label downloadingsLabel, linksLabel;
    @FXML
    protected ComboBox<String> threadsSelector, retriesSelector;
    @FXML
    protected TextArea logsTextArea;
    @FXML
    protected VBox optionsBox, htmlOptionsBox, pdfOptionsBox;
    @FXML
    protected ComboBox<String> ttfSelector;
    @FXML
    protected TextArea cssArea;

    public DownloadFirstLevelLinksController() {
        baseTitle = AppVariables.message("DownloadFirstLevelLinks");
        TipsLabelKey = "DownloadFirstLevelLinksComments";

        linksData = FXCollections.observableArrayList();
        downloadingData = FXCollections.observableArrayList();
        failedData = FXCollections.observableArrayList();
        downloadThreads = new ArrayList<>();
        pathThreads = new ArrayList<>();
        paths = new HashMap<>();
        retries = new HashMap<>();
        completedAddresses = new HashMap<>();
        completedLinks = new HashMap<>();
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            htmlOptions = new MutableDataSet();
            htmlParser = Parser.builder(htmlOptions).build();
            htmlRender = HtmlRenderer.builder(htmlOptions).build();

            mdConverter = FlexmarkHtmlConverter.builder(htmlOptions).build();

            pdfOptions = PegdownOptionsAdapter.flexmarkOptions(Extensions.ALL
                    & ~(Extensions.ANCHORLINKS | Extensions.EXTANCHORLINKS_WRAP), TocExtension.create())
                    .toMutable()
                    .set(TocExtension.LIST_CLASS, PdfConverterExtension.DEFAULT_TOC_LIST_CLASS)
                    .toImmutable();

            DataHolder textHolder = PegdownOptionsAdapter.flexmarkOptions(Extensions.ALL);
            textOptions = new MutableDataSet();
            textOptions.set(Parser.EXTENSIONS, textHolder.get(Parser.EXTENSIONS));
            textParser = Parser.builder(textOptions).build();
            textCollectingVisitor = new TextCollectingVisitor();

            targetPathController.label(message("TargetPath"))
                    .name(baseName + "TargatPath", true)
                    .isSource(false).isDirectory(true).mustExist(false);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initLinksTab();
            initDownloadingTab();
            initFailedTab();
            initOptionsTab();
            initLogsTab();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void initLinksTab() {
        try {
            List<String> urls = VisitHistoryTools.recentDownloadAddress();
            if (urls == null || urls.isEmpty()) {
                urlBox.getItems().add("https://www.luoxia.com/xiyouji/");
            } else {
                urlBox.getItems().addAll(urls);
            }
            urlBox.getSelectionModel().select(0);

            addressPathColumn.setCellValueFactory(new PropertyValueFactory<>("addressPath"));
            addressFileColumn.setCellValueFactory(new PropertyValueFactory<>("addressFile"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            pathColumn.setCellValueFactory(new PropertyValueFactory<>("fileParent"));
            filenameColumn.setCellValueFactory(new PropertyValueFactory<>("filename"));
            indexColumn.setCellValueFactory(new PropertyValueFactory<>("index"));
            fileColumn.setCellValueFactory(new PropertyValueFactory<>("file"));

            linksTableView.setItems(linksData);
            linksTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            linksTableView.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() > 1) {
                    link();
                }
            });

            goButton.disableProperty().bind(
                    targetPathController.fileInput.styleProperty().isEqualTo(badStyle)
                            .or(urlBox.getSelectionModel().selectedItemProperty().isNull())
            );
            downloadButton.disableProperty().bind(
                    targetPathController.fileInput.styleProperty().isEqualTo(badStyle)
                            .or(linksTableView.getSelectionModel().selectedItemProperty().isNull())
            );
            copyButton.disableProperty().bind(linksTableView.getSelectionModel().selectedItemProperty().isNull());
            equalButton.disableProperty().bind(copyButton.disableProperty());
            viewButton.disableProperty().bind(copyButton.disableProperty());
            infoButton.disableProperty().bind(copyButton.disableProperty());
            linkButton.disableProperty().bind(copyButton.disableProperty());
            htmlButton.disableProperty().bind(linksTableView.itemsProperty().isNull());

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void initDownloadingTab() {
        try {
            downloadingLinkColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
            downloadingFileColumn.setCellValueFactory(new PropertyValueFactory<>("filename"));

            downloadingTableView.setItems(downloadingData);
            downloadingTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            deleteDownloadingButton.disableProperty().bind(downloadingTableView.getSelectionModel().selectedItemProperty().isNull());
            copyDownloadingButton.disableProperty().bind(deleteDownloadingButton.disableProperty());
            viewDownloadingButton.disableProperty().bind(copyDownloadingButton.disableProperty());
            infoDownloadingButton.disableProperty().bind(copyDownloadingButton.disableProperty());
            linkDownloadingButton.disableProperty().bind(copyDownloadingButton.disableProperty());
            downloadingTableView.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() > 1) {
                    linkDownloading();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void initFailedTab() {
        try {
            failedLinkColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
            failedFileColumn.setCellValueFactory(new PropertyValueFactory<>("filename"));

            failedTableView.setItems(failedData);
            failedTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            deleteFailedButton.disableProperty().bind(failedTableView.getSelectionModel().selectedItemProperty().isNull());
            copyFailedButton.disableProperty().bind(deleteFailedButton.disableProperty());
            viewFailedButton.disableProperty().bind(copyFailedButton.disableProperty());
            infoFailedButton.disableProperty().bind(copyFailedButton.disableProperty());
            downloadFailedButton.disableProperty().bind(copyFailedButton.disableProperty());
            linkFailedButton.disableProperty().bind(copyFailedButton.disableProperty());
            failedTableView.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() > 1) {
                    linkFailed();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void initOptionsTab() {
        try {

            relinksCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Relinks", true));
            relinksCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                AppVariables.setUserConfigValue(baseName + "Relinks", relinksCheck.isSelected());
            });

            indexCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "GenerateIndex", true));
            indexCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                        AppVariables.setUserConfigValue(baseName + "GenerateIndex", indexCheck.isSelected());
                    });

            pdfTextCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "MergeTextPDF", true));
            pdfMarkdownCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "MergeMarkdownPDF", false));
            pdfHtmlCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "MergeHtmlPDF", false));
            pdfOptionsBox.setDisable(!pdfTextCheck.isSelected() && !pdfMarkdownCheck.isSelected() && !pdfHtmlCheck.isSelected());

            pdfTextCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                        AppVariables.setUserConfigValue(baseName + "MergeTextPDF", pdfTextCheck.isSelected());
                        pdfOptionsBox.setDisable(!pdfTextCheck.isSelected() && !pdfMarkdownCheck.isSelected() && !pdfHtmlCheck.isSelected());
                    });
            pdfMarkdownCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                        AppVariables.setUserConfigValue(baseName + "MergeMarkdownPDF", pdfMarkdownCheck.isSelected());
                        pdfOptionsBox.setDisable(!pdfTextCheck.isSelected() && !pdfMarkdownCheck.isSelected() && !pdfHtmlCheck.isSelected());
                    });
            pdfHtmlCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                        AppVariables.setUserConfigValue(baseName + "MergeHtmlPDF", pdfHtmlCheck.isSelected());
                        pdfOptionsBox.setDisable(!pdfTextCheck.isSelected() && !pdfMarkdownCheck.isSelected() && !pdfHtmlCheck.isSelected());
                    });

            textCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "MergeText", true));
            textCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                        AppVariables.setUserConfigValue(baseName + "MergeText", textCheck.isSelected());
                    });
            htmlCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "MergeHtml", true));
            htmlCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                        AppVariables.setUserConfigValue(baseName + "MergeHtml", htmlCheck.isSelected());
                    });
            mdCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "MergeMarkdown", true));
            mdCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                        AppVariables.setUserConfigValue(baseName + "MergeMarkdown", mdCheck.isSelected());
                    });

            List<String> ttfList = SystemTools.ttfList();
            ttfSelector.getItems().addAll(ttfList);
            ttfSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue == null || newValue.isBlank()) {
                        return;
                    }
                    int pos = newValue.indexOf("    ");
                    if (pos < 0) {
                        ttf = newValue;
                    } else {
                        ttf = newValue.substring(0, pos);
                    }
                    AppVariables.setUserConfigValue(baseName + "TTF", newValue);
                    String css = "@font-face {\n"
                            + "  font-family: 'myFont';\n"
                            + "  src: url('file:///" + ttf.replaceAll("\\\\", "/") + "');\n"
                            + "  font-weight: normal;\n"
                            + "  font-style: normal;\n"
                            + "}\n"
                            + " body { font-family:  'myFont';}";
                    cssArea.setText(css);
                }
            });
            String d = AppVariables.getUserConfigValue(baseName + "TTF", null);
            if (d == null) {
                ttfSelector.getSelectionModel().select(0);
            } else {
                ttfSelector.setValue(d);
            }

            webConnectTimeoutInput.setText(AppVariables.getUserConfigInt("WebConnectTimeout", 10000) + "");
            webReadTimeoutInput.setText(AppVariables.getUserConfigInt("WebReadTimeout", 10000) + "");

            intervalController.isSeconds(false).init(baseName + "Inteval", 1000);

            threadsSelector.getItems().addAll(Arrays.asList("6", "3", "1", "2", "5", "8"));
            maxThreadsNumber = AppVariables.getUserConfigInt(baseName + "ThreadsNumber", 6);
            if (maxThreadsNumber <= 0) {
                maxThreadsNumber = 6;
            }
            threadsSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v >= 0) {
                                maxThreadsNumber = v;
                                AppVariables.setUserConfigInt(baseName + "ThreadsNumber", v);
                                threadsSelector.getEditor().setStyle(null);
                                checkThreads();
                            } else {
                                threadsSelector.getEditor().setStyle(badStyle);
                            }
                        } catch (Exception e) {
                            threadsSelector.getEditor().setStyle(badStyle);
                        }
                    });
            threadsSelector.getSelectionModel().select(maxThreadsNumber + "");

            retriesSelector.getItems().addAll(Arrays.asList("3", "2", "1", "4", "5", "6"));
            maxRetries = AppVariables.getUserConfigInt(baseName + "MaxRetries", 3);
            if (maxRetries <= 0) {
                maxRetries = 3;
            }
            retriesSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                maxRetries = v;
                                AppVariables.setUserConfigInt(baseName + "MaxRetries", v);
                                retriesSelector.getEditor().setStyle(null);
                            } else {
                                retriesSelector.getEditor().setStyle(badStyle);
                            }
                        } catch (Exception e) {
                            retriesSelector.getEditor().setStyle(badStyle);
                        }
                    });
            retriesSelector.getSelectionModel().select(maxRetries + "");

            miaowCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Miaow", true));
            miaowCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal, Boolean newVal) {
                    AppVariables.setUserConfigValue(baseName + "Miaow", miaowCheck.isSelected());
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void initLogsTab() {
        try {
            maxLogs = AppVariables.getUserConfigInt(baseName + "MaxLogs", 50000);
            maxLogsinput.setText(maxLogs + "");

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();
            FxmlControl.setTooltip(copyButton, message("CopyLink") + "\nCTRL+c");
            FxmlControl.setTooltip(copyDownloadingButton, message("CopyLink"));
            FxmlControl.setTooltip(linkFailedButton, message("CopyLink"));
            FxmlControl.setTooltip(htmlButton, message("AddressHtml"));
            FxmlControl.removeTooltip(equalButton);
            stopped = true;

            tipsView.setFitWidth(AppVariables.iconSize * 1.5);
            tipsView.setFitHeight(AppVariables.iconSize * 1.5);
//            tableDownloadHistory.clearData();
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void checkThreads() {
        synchronized (downloadThreads) {
            try {
                int size = downloadThreads.size();
                if (size <= maxThreadsNumber) {
                    return;
                }
                for (int i = size - 1; i >= maxThreadsNumber; i--) {
                    DownloadThread linkTask = downloadThreads.get(i);
                    if (linkTask != null) {
                        linkTask.setCancel(true);
                    }
                    downloadThreads.remove(linkTask);
                }
                updateLogs(message("DownloadThread") + ": " + downloadThreads.size());
            } catch (Exception e) {
                logger.error(e.toString());
            }
        }
    }

    @FXML
    public void goAction() {
        String address = urlBox.getValue();
        if (address == null) {
            popError(message("InvalidParameters"));
            return;
        }
        VisitHistoryTools.visitURI(address);
        File downloadPath = targetPathController.file;
        if (downloadPath == null) {
            popError(message("InvalidParameters"));
            tabPane.getSelectionModel().select(optionsTab);
            return;
        }
        updateLogs(message("WebPageAddress") + ": " + address);
        updateLogs(message("TargetPath") + ": " + downloadPath);
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private String title;

                @Override
                protected boolean handle() {
                    try {
                        URL url = new URL(address);
                        File httpFile = FileTools.getTempFile();
                        HtmlTools.downloadHttp(url, httpFile);
                        title = HtmlTools.title(httpFile);
                        addressLink = Link.create().setUrl(url).setAddress(url.toString()).setName(title).setTitle(title);
                        addressLink.setFile(httpFile.getAbsolutePath());
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    setValues(title);
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    public void setValues(String title) {
        if (addressLink == null) {
            return;
        }
        DownloadFirstLevelLinksSetController controller
                = (DownloadFirstLevelLinksSetController) openStage(CommonValues.DownloadFirstLevelLinksSetFxml, true);
        controller.setValues(this, title);
    }

    public void readLinks(String subPath, Link.FilenameType nameType) {
        if (addressLink == null || subPath == null) {
            return;
        }
        this.subPath = subPath;
        filenameType = nameType;
        linksData.clear();
        File downloadPath = targetPathController.file;
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private List<Link> links;

                @Override
                protected boolean handle() {
                    File path = new File(downloadPath.getAbsolutePath() + File.separator + subPath);
                    links = HtmlTools.addressLinks(addressLink, htmlParser, mdConverter, path, nameType);
                    return links != null;
                }

                @Override
                protected void whenSucceeded() {
                    if (!links.isEmpty()) {
                        linksData.addAll(links);
                        linksTableView.getSortOrder().clear();
                        linksTableView.getSortOrder().addAll(addressPathColumn, indexColumn);

                        for (Link link : links) {
                            if (link.getAddressPath().startsWith(addressLink.getAddressPath())) {
                                linksTableView.getSelectionModel().select(link);
                            }
                        }
                    }

                    String txt = message("Links") + ": " + linksData.size();
                    linksLabel.setText(txt);
                    updateLogs(txt);

                    FadeTransition fade = new FadeTransition(Duration.millis(500));
                    fade.setFromValue(1.0);
                    fade.setToValue(0f);
                    fade.setCycleCount(6);
                    fade.setAutoReverse(true);
                    fade.setNode(tipsView);
                    fade.play();

                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void downloadAction() {
        try {
            stopped = false;
            List<Link> selected = linksTableView.getSelectionModel().getSelectedItems();
            if (selected == null || selected.isEmpty()) {
                return;
            }
            synchronized (paths) {
                for (int i = 0; i < selected.size(); i++) {
                    Link link = selected.get(i);
                    link.setIndex(i + 1);
                    File file = new File(link.getFile());
                    file.mkdirs();
                    link.setFile(file.getAbsolutePath());
                    File path = file.getParentFile();
                    if (paths.containsKey(path)) {
                        paths.put(path, paths.get(path) + 1);
                    } else {
                        paths.put(path, 1);
                    }
                }
            }
            synchronized (downloadingLock) {
                downloadingData.addAll(selected);
            }
            checkData();
            tabPane.getSelectionModel().select(logsTab);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void clearHistories() {
        String address = urlBox.getValue();
        if (address == null) {
            popError(message("InvalidParameters"));
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
//                    tableDownloadHistory.deleteAddressHistory(address);
                    return true;
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void stop() {
        stopped = true;
    }

    @FXML
    public void popSetMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem(message("SetSubdirectoryName"));
            menu.setOnAction((ActionEvent event) -> {
                setPath();
            });
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("AddOrderBeforeFilename"));
            menu.setOnAction((ActionEvent event) -> {
                addOrderBeforeFilename();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("SetLinkNameAsFilename"));
            menu.setOnAction((ActionEvent event) -> {
                setLinkNameAsFilename();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("SetLinkTitleAsFilename"));
            menu.setOnAction((ActionEvent event) -> {
                setLinkTitleAsFilename();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("SetLinkAddressAsFilename"));
            menu.setOnAction((ActionEvent event) -> {
                setLinkAddressAsFilename();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void setPath() {
        List<Link> selected = linksTableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle(message("DownloadLinks"));
        dialog.setHeaderText(message("SubdirectoryName"));
        dialog.setContentText("");
        dialog.getEditor().setPrefWidth(300);
        dialog.getEditor().setText("");
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();
        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent() || result.get().trim().isBlank()) {
            return;
        }
        String path = result.get().trim();
        for (Link link : selected) {
            File fullpath = new File(targetPathController.file.getAbsolutePath() + File.separator + path);
            String filename = link.filename(fullpath, filenameType);
            link.setFile(filename);
        }
        linksTableView.refresh();
    }

    public void addOrderBeforeFilename() {
        List<Link> selected = linksTableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        for (int i = 0; i < selected.size(); i++) {
            Link link = selected.get(i);
            String filename = link.getFile();
            if (filename == null) {
                filename = link.filename(new File(targetPathController.file.getAbsolutePath()), filenameType);
                link.setFile(filename);
            }
            File file = new File(filename);
            String newName = file.getParent() + File.separator + (i + 1) + "_" + file.getName();
            link.setFile(newName);
        }
        linksTableView.refresh();
    }

    public void setFilename() {
        List<Link> selected = linksTableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        for (Link link : selected) {
            String filename = link.getFile();
            if (filename == null) {
                filename = link.filename(new File(targetPathController.file.getAbsolutePath()), filenameType);
                link.setFile(filename);
            }
            File file = new File(filename);
            String suffix = FileTools.getFileSuffix(filename);
            suffix = (suffix != null && !suffix.isBlank()) ? "." + suffix : "";
            String newName = file.getParent() + File.separator + link.pageName(filenameType) + suffix;
            link.setFile(newName);
        }
        linksTableView.refresh();
    }

    public void setLinkNameAsFilename() {
        filenameType = FilenameType.ByLinkName;
        setFilename();
    }

    public void setLinkTitleAsFilename() {
        filenameType = FilenameType.ByLinkTitle;
        setFilename();
    }

    public void setLinkAddressAsFilename() {
        filenameType = FilenameType.ByLinkAddress;
        setFilename();
    }

    @FXML
    @Override
    public void copyAction() {
        if (tabPane.getSelectionModel().getSelectedItem() != linksTab) {
            return;
        }
        Link link = linksTableView.getSelectionModel().getSelectedItem();
        if (link == null) {
            return;
        }
        ClipboardContent content = new ClipboardContent();
        content.putString(link.getAddress());
        Clipboard.getSystemClipboard().setContent(content);
        String txt = message("Copied") + ": " + link.getAddress();
        popInformation(txt);
        updateLogs(txt);
    }

    @FXML
    @Override
    public void infoAction() {
        Link link = linksTableView.getSelectionModel().getSelectedItem();
        if (link == null) {
            return;
        }
        info(link.getAddress());
    }

    @FXML
    public void info(String link) {
        if (link == null) {
            return;
        }
        Task infoTask = new DownloadTask() {

            @Override
            protected boolean initValues() {
                readHead = true;
                address = link;
                return super.initValues();
            }

            @Override
            protected void whenSucceeded() {
                if (head == null) {
                    popError(AppVariables.message("InvalidData"));
                    return;
                }
                StringBuilder s = new StringBuilder();
                s.append("<h1  class=\"center\">").append(address).append("</h1>\n");
                s.append("<hr>\n");
                List<String> names = new ArrayList<>();
                names.addAll(Arrays.asList(message("Name"), message("Value")));
                StringTable table = new StringTable(names);
                for (Object key : head.keySet()) {
                    String name = (String) key;
                    if (name.startsWith("HeaderField_") || name.startsWith("RequestProperty_")) {
                        continue;
                    }
                    List<String> row = new ArrayList<>();
                    row.addAll(Arrays.asList(name, (String) head.get(key)));
                    table.add(row);
                }
                s.append(StringTable.tableDiv(table));
                s.append("<h2  class=\"center\">").append("Header Fields").append("</h2>\n");
                int hlen = "HeaderField_".length();
                for (Object key : head.keySet()) {
                    String name = (String) key;
                    if (!name.startsWith("HeaderField_")) {
                        continue;
                    }
                    List<String> row = new ArrayList<>();
                    row.addAll(Arrays.asList(name.substring(hlen), (String) head.get(key)));
                    table.add(row);
                }
                s.append(StringTable.tableDiv(table));
                s.append("<h2  class=\"center\">").append("Request Property").append("</h2>\n");
                int rlen = "RequestProperty_".length();
                for (Object key : head.keySet()) {
                    String name = (String) key;
                    if (!name.startsWith("RequestProperty_")) {
                        continue;
                    }
                    List<String> row = new ArrayList<>();
                    row.addAll(Arrays.asList(name.substring(rlen), (String) head.get(key)));
                    table.add(row);
                }
                s.append(StringTable.tableDiv(table));
                FxmlStage.openHtmlViewer(null, s.toString());
            }

            @Override
            protected void whenFailed() {
                if (error != null) {
                    popError(error);
                } else {
                    popFailed();
                }
            }

        };
        Thread thread = new Thread(infoTask);
        openHandlingStage(infoTask, Modality.WINDOW_MODAL);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void infoDownloading() {
        Link link = downloadingTableView.getSelectionModel().getSelectedItem();
        if (link == null) {
            return;
        }
        info(link.getAddress());
    }

    @FXML
    public void infoFailed() {
        Link link = failedTableView.getSelectionModel().getSelectedItem();
        if (link == null) {
            return;
        }
        info(link.getAddress());
    }

    @FXML
    public void html() {
        try {
            if (addressLink == null) {
                return;
            }
            TextEditerController controller = (TextEditerController) openStage(CommonValues.TextEditerFxml);
            controller.hideLeftPane();
            controller.hideRightPane();
            controller.openTextFile(new File(addressLink.getFile()));
        } catch (Exception e) {
        }
    }

    @FXML
    protected void link() {
        Link link = linksTableView.getSelectionModel().getSelectedItem();
        link(link);
    }

    @FXML
    protected void linkDownloading() {
        Link link = downloadingTableView.getSelectionModel().getSelectedItem();
        link(link);
    }

    @FXML
    protected void linkFailed() {
        Link link = failedTableView.getSelectionModel().getSelectedItem();
        link(link);
    }

    protected void link(Link link) {
        try {
            if (link == null) {
                return;
            }
            browseURI(link.getUrl().toURI());
        } catch (Exception e) {
        }
    }

    @FXML
    public void viewAction() {
        Link link = linksTableView.getSelectionModel().getSelectedItem();
        view(link);
    }

    @FXML
    public void view(Link link) {
        if (link == null || targetPathController.file == null) {
            return;
        }
        String s = message("Address") + ": " + link.getAddress() + "<br>"
                + message("Name") + ": " + (link.getName() == null ? "" : link.getName()) + "<br>"
                + message("Title") + ": " + (link.getTitle() == null ? "" : link.getTitle()) + "<br>"
                + message("TargetFile") + ": " + link.getFile();
        HtmlTools.viewHtml(message("Link"), s);
    }

    @FXML
    public void viewDownloading() {
        Link link = downloadingTableView.getSelectionModel().getSelectedItem();
        view(link);
    }

    @FXML
    public void viewFailed() {
        Link link = failedTableView.getSelectionModel().getSelectedItem();
        view(link);
    }

    @FXML
    @Override
    public void pasteAction() {
        String string = Clipboard.getSystemClipboard().getString();
        if (string != null && !string.isBlank()) {
            urlBox.setValue(string);
        }
        goAction();
    }

    @FXML
    public void clearDownloading() {
        synchronized (downloadingLock) {
            for (Link link : downloadingData) {
                File file = new File(link.getFile());
                File path = file.getParentFile();
                synchronized (paths) {
                    if (paths.containsKey(path)) {
                        paths.put(path, paths.get(path) - 1);
                    }
                }
            }
            downloadingData.clear();
        }
        checkData();
    }

    @FXML
    public void deleteDownloading() {
        List<Link> selected = downloadingTableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        List<Link> links = new ArrayList<>();
        links.addAll(selected);
        synchronized (paths) {
            for (Link link : selected) {
                File file = new File(link.getFile());
                File path = file.getParentFile();
                if (paths.containsKey(path)) {
                    paths.put(path, paths.get(path) - 1);
                }
            }
        }
        synchronized (downloadingLock) {
            downloadingData.removeAll(links);
        }
        checkData();
    }

    @FXML
    public void copyDownloading() {
        Link link = downloadingTableView.getSelectionModel().getSelectedItem();
        if (link == null) {
            return;
        }
        ClipboardContent content = new ClipboardContent();
        content.putString(link.getAddress());
        Clipboard.getSystemClipboard().setContent(content);
        popInformation(message("Copied") + ": " + link.getAddress());
    }

    @FXML
    public void refreshDownloading() {
        stopped = false;
        synchronized (downloadThreads) {
            try {
                int size = downloadThreads.size();
                for (int i = size - 1; i > 0; i--) {
                    DownloadThread linkTask = downloadThreads.get(i);
                    if (linkTask != null) {
                        linkTask.setCancel(true);
                    }
                }
                downloadThreads.clear();
                updateLogs(message("DownloadThread") + ": " + downloadThreads.size());
            } catch (Exception e) {
                logger.error(e.toString());
            }
        }
        checkData();
    }

    @FXML
    public void clearFailed() {
        synchronized (failedLock) {
            failedData.clear();
        }
    }

    @FXML
    public void deleteFailed() {
        List<Link> selected = failedTableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        List<Link> links = new ArrayList<>();
        links.addAll(selected);
        synchronized (failedLock) {
            failedData.removeAll(links);
        }
    }

    @FXML
    public void copyFailed() {
        Link link = failedTableView.getSelectionModel().getSelectedItem();
        if (link == null) {
            return;
        }
        ClipboardContent content = new ClipboardContent();
        content.putString(link.getAddress());
        Clipboard.getSystemClipboard().setContent(content);
        popInformation(message("Copied") + ": " + link.getAddress());
    }

    @FXML
    public void downloadFailed() {
        stopped = false;
        List<Link> selected = failedTableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        List<Link> links = new ArrayList<>();
        links.addAll(selected);
        boolean added = false;
        for (Link link : links) {
            synchronized (downloadingLock) {
                if (!downloadingData.contains(link)) {
                    downloadingData.add(0, link);
                    added = true;
                }
            }
            synchronized (failedLock) {
                failedData.remove(link);
            }
        }
        if (added) {
            checkData();
            tabPane.getSelectionModel().select(logsTab);
        }
    }

    public void checkData() {
        int dataSize;
        synchronized (downloadingLock) {
            dataSize = downloadingData.size();
        }
        if (!stopped) {
            synchronized (downloadThreads) {
                try {
                    int number = Math.min(dataSize, maxThreadsNumber);
                    int threadsSize = downloadThreads.size();
                    for (int i = threadsSize; i < number; i++) {
                        DownloadThread linkTask = new DownloadThread();
                        linkTask.setSelf(linkTask);
                        linkTask.setDaemon(false);
                        downloadThreads.add(linkTask);
                        linkTask.start();
                        updateLogs(message("Started") + ": " + message("DownloadThread") + linkTask.getId() + "    "
                                + message("Count") + ": " + downloadThreads.size());
                    }
                } catch (Exception e) {
                    logger.error(e.toString());
                }
            }
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                downloadingsLabel.setText((stopped ? message("Stopped") + "  " : "")
                        + message("Links") + ": " + dataSize);
            }
        });
    }

    @FXML
    protected void okTimeout() {
        int connValue, readValue;
        try {
            int v = Integer.parseInt(webConnectTimeoutInput.getText());
            if (v > 0) {
                connValue = v;
                webConnectTimeoutInput.setStyle(null);
            } else {
                webConnectTimeoutInput.setStyle(badStyle);
                return;
            }
        } catch (Exception e) {
            webConnectTimeoutInput.setStyle(badStyle);
            return;
        }
        try {
            int v = Integer.parseInt(webReadTimeoutInput.getText());
            if (v > 0) {
                readValue = v;
                webReadTimeoutInput.setStyle(null);
            } else {
                webReadTimeoutInput.setStyle(badStyle);
                return;
            }
        } catch (Exception e) {
            webReadTimeoutInput.setStyle(badStyle);
            return;
        }
        AppVariables.setUserConfigInt("WebConnectTimeout", connValue);
        AppVariables.setUserConfigInt("WebReadTimeout", readValue);
        popSuccessful();
    }

    protected class DownloadThread extends Thread {

        private DownloadThread self;
        private long interval, emptyTime;
        private Timer dlTimer;
        private boolean cancel;

        @Override
        public void run() {
            if (stopped || cancel) {
                quit();
                return;
            }
            this.interval = intervalController.value > 0 ? intervalController.value : 1000;
            dlTimer = new Timer();
            emptyTime = -1;
            dlTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (dlTimer == null || getMyStage() == null || !getMyStage().isShowing()) {
                        quit();
                        return;
                    }
                    if (stopped || cancel
                            || (emptyTime > 0 && (new Date().getTime() - emptyTime > 10000))) {
                        dlTimer.cancel();
                        dlTimer = null;
                        quit();
                        return;
                    }
                    download();
                }
            }, 0, interval);

        }

        protected void download() {
            Link link = null;
            try {
                synchronized (downloadingLock) {
                    if (downloadingData.isEmpty()) {
                        if (emptyTime <= 0) {
                            emptyTime = new Date().getTime();
                        }
                        return;
                    }
                    link = downloadingData.get(0);
                    downloadingData.remove(link);
                    emptyTime = -1;
                }
                checkData();
                if (link == null || link.getFile() == null) {
                    return;
                }
                URL url = link.getUrl();
                File file = new File(link.getFile());
                file.mkdirs();
                link.setFile(file.getAbsolutePath());

//                updateLogs(message("Downloading") + ": " + url + " --> " + file);
                String error = HtmlTools.downloadHttp(url, file);
                if (error == null) {
                    link.setDlTime(new Date());
                    updateLogs(message("Downloaded") + ": " + url + " --> " + file);
                    if (utf8Check.isSelected()) {
                        String utf8 = HtmlTools.toUTF8(file, false);
                        if (utf8 == null) {
                            updateLogs(message("Failed") + ": " + file);
                        } else if (!"NeedNot".equals(utf8)) {
                            updateLogs(message("HtmlToUTF8") + ": " + file);
                            FileTools.writeFile(file, utf8, Charset.forName("utf-8"));
                        }
                    }
                    if (relinksCheck.isSelected()) {
                        synchronized (completedAddresses) {
                            completedAddresses.put(url, file);
                            completedLinks.put(file, link);
                        }
                    }
                    if (relinksCheck.isSelected() || indexCheck.isSelected()
                            || mdCheck.isSelected() || textCheck.isSelected()
                            || pdfTextCheck.isSelected() || pdfMarkdownCheck.isSelected()
                            || pdfHtmlCheck.isSelected()) {
                        synchronized (paths) {
                            File path = file.getParentFile();
                            if (paths.containsKey(path)) {
                                int number = paths.get(path);
                                if (number <= 1) {
                                    paths.put(path, 0);
                                } else {
                                    paths.put(path, number - 1);
                                }
                            }
                        }
                    }
                    return;
                }
                failed(link, error);
            } catch (Exception e) {
                failed(link, e.toString());
            }
        }

        protected void failed(Link link, String error) {
            if (link != null && link.getFile() != null) {
                synchronized (downloadingLock) {
                    if (downloadingData.contains(link)) {
                        return;
                    }
                }
                synchronized (retries) {
                    int currentRetries = 1;
                    if (retries.containsKey(link)) {
                        currentRetries = retries.get(link) + 1;
                    }
                    retries.put(link, currentRetries);
                    if (currentRetries <= maxRetries) {
                        synchronized (downloadingLock) {
                            downloadingData.add(0, link);
                        }
                        updateLogs(message("Retry") + " " + currentRetries
                                + ": " + link.getUrl() + " --> " + link.getFile());
                    } else {
                        synchronized (failedLock) {
                            if (!failedData.contains(link)) {
                                failedData.add(0, link);
                            }
                        }
                        updateLogs(message("Failed") + ": " + link.getUrl() + " --> " + link.getFile());
                    }
                }
            }
            try {
                Thread.sleep(interval * 5);
            } catch (Exception e) {
            }
        }

        protected void quit() {
            try {
                synchronized (downloadThreads) {
                    if (self != null) {
                        downloadThreads.remove(self);
                        updateLogs(message("Stopped") + ": " + message("DownloadThread") + self.getId() + "    "
                                + message("Count") + ": " + downloadThreads.size());
                    }
                    if (!downloadThreads.isEmpty()) {
                        return;
                    }
                }
                updateLogs(message("DownloadCompleted"));
                if (!stopped && !cancel
                        && (relinksCheck.isSelected() || indexCheck.isSelected()
                        || mdCheck.isSelected() || textCheck.isSelected()
                        || pdfTextCheck.isSelected() || pdfMarkdownCheck.isSelected()
                        || pdfHtmlCheck.isSelected())) {
                    synchronized (paths) {
                        for (File path : paths.keySet()) {
                            synchronized (pathThreads) {
                                PathThread pThread = new PathThread();
                                pThread.setPath(path);
                                pThread.setSelf(pThread);
                                pThread.setDaemon(false);
                                pathThreads.add(pThread);
                                pThread.start();
                                updateLogs(message("Started") + ": " + message("PathThread") + pThread.getId() + "    "
                                        + message("Count") + ": " + pathThreads.size());
                            }
                        }
                        paths.clear();
                    }
                }
                checkData();
            } catch (Exception e) {
                logger.debug(e.toString());
            }
        }

        public void setCancel(boolean cancel) {
            this.cancel = cancel;
        }

        public void setSelf(DownloadThread self) {
            this.self = self;
        }

    };

    protected class PathThread extends Thread {

        private PathThread self;
        private File path;
        private List<File> files;

        @Override
        public void run() {
            if (stopped || path == null || !path.exists() || !path.isDirectory()) {
                quit();
                return;
            }
            File[] pathFiles = path.listFiles();
            if (pathFiles == null || pathFiles.length == 0) {
                quit();
                return;
            }
            files = new ArrayList<>();
            String indexPrefix = "0000_" + message("PathIndex");
            for (File file : pathFiles) {
                if (file.isFile() && !file.getName().startsWith(indexPrefix)) {
                    files.add(file);
                }
            }
            if (files.isEmpty()) {
                quit();
                return;
            }
            Collections.sort(files, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    return FileTools.compareFilename(f1, f2);
                }
            });

            relink();
            if (stopped) {
                quit();
                return;
            }
            frameset();
            if (stopped) {
                quit();
                return;
            }
            merge();
            quit();
        }

        public void relink() {
            if (stopped || files == null || files.isEmpty() || !relinksCheck.isSelected()) {
                return;
            }
            for (File file : files) {
                if (stopped) {
                    return;
                }
                HtmlTools.relinkPage(file, htmlParser, mdConverter, completedLinks, completedAddresses);
                updateLogs(message("HtmlLinksRewritten") + ": " + file);
            }
        }

        public void frameset() {
            if (stopped || files == null || files.isEmpty() || !indexCheck.isSelected()) {
                return;
            }
            updateLogs(message("GeneratingPathIndex") + ": " + path + " ...");
            HtmlTools.makePathList(path, files, completedLinks);
            File frameFile = new File(path.getAbsolutePath() + File.separator + "0000_" + message("PathIndex") + ".html");
            if (HtmlTools.generateFrameset(files, frameFile)) {
                updateLogs(message("HtmlFrameset") + ": " + frameFile + "");
            } else {
                updateLogs(message("Failed") + ": " + frameFile + "");
            }
        }

        public void merge() {
            if (stopped || files == null || files.isEmpty()
                    || (!htmlCheck.isSelected() && !textCheck.isSelected() && !mdCheck.isSelected()
                    && !pdfTextCheck.isSelected() && !pdfMarkdownCheck.isSelected()
                    && !pdfHtmlCheck.isSelected())) {
                return;
            }
            String filePrefix = path.getAbsolutePath() + File.separator + path.getName();

            StringBuilder htmlBuilder = new StringBuilder();
            String head
                    = "<html>\n"
                    + "    <head>\n"
                    + "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n"
                    + "    </head>\n"
                    + "    <body>\n";
            htmlBuilder.append(head);
            for (File file : files) {
                if (stopped) {
                    return;
                }
                try {
                    String html = FileTools.readTexts(file);
                    String body = HtmlTools.body(html);
                    htmlBuilder.append(body);
                } catch (Exception e) {
                    updateLogs(e.toString());
                }
            }
            htmlBuilder.append("    </body>\n</html>\n");
            String html = htmlBuilder.toString();
            if (stopped) {
                return;
            }
            if (htmlCheck.isSelected()) {
                String htmlFile = filePrefix + ".html";
                updateLogs(message("MergeAsOneHtml") + ": " + htmlFile + " ...");
                FileTools.writeFile(new File(htmlFile), html);
                updateLogs(message("Generated") + ": " + htmlFile);
            }
            String md = null, text = null;
            if (textCheck.isSelected() || mdCheck.isSelected()
                    || pdfTextCheck.isSelected() || pdfMarkdownCheck.isSelected()) {
                updateLogs(message("ConvertToMarkdown") + ": " + filePrefix + " ...");
                md = mdConverter.convert(html);
                if (stopped) {
                    return;
                }
                if (textCheck.isSelected() || pdfTextCheck.isSelected()) {
                    updateLogs(message("ConvertToText") + ": " + filePrefix + " ...");
                    Node document = textParser.parse(md);
                    text = textCollectingVisitor.collectAndGetText(document);
                }

            }
            if (stopped) {
                return;
            }
            if (textCheck.isSelected() && text != null) {
                String textFile = filePrefix + ".txt";
                FileTools.writeFile(new File(textFile), text);
                updateLogs(message("Generated") + ": " + textFile);
            }
            if (stopped) {
                return;
            }
            if (mdCheck.isSelected() && md != null) {
                String mdFile = filePrefix + ".md";
                FileTools.writeFile(new File(mdFile), md);
                updateLogs(message("Generated") + ": " + mdFile);
            }
            if (stopped) {
                return;
            }
            if (pdfTextCheck.isSelected() && text != null) {
                String pdfFile = filePrefix + "_text.pdf";
                String textHtml = HtmlTools.textToHtml(text);
                mergePDF(pdfFile, textHtml);
            }
            if (stopped) {
                return;
            }
            if (pdfMarkdownCheck.isSelected() && md != null) {
                String pdfFile = filePrefix + "_md.pdf";
                Node document = htmlParser.parse(md);
                String mdHtml = htmlRender.render(document);
                mergePDF(pdfFile, mdHtml);
            }
            if (stopped) {
                return;
            }
            if (pdfHtmlCheck.isSelected() && html != null) {
                String pdfFile = filePrefix + "_html.pdf";
                mergePDF(pdfFile, html);
            }
        }

        public void mergePDF(String file, String html) {
            updateLogs(message("MergeAsPDF") + ": " + file + " ...");
            String css = cssArea.getText().trim();
            if (!css.isBlank()) {
                try {
                    html = PdfConverterExtension.embedCss(html, css);
                } catch (Exception e) {
                    updateLogs(e.toString());
                }
            }
            if (stopped) {
                return;
            }
            try {
                PdfConverterExtension.exportToPdf(file, html, "", pdfOptions);
                File ffile = new File(file);
                if (!ffile.exists()) {
                    updateLogs(message("Failed") + ": " + file);
                } else if (ffile.length() == 0) {
                    ffile.delete();
                    updateLogs(message("Failed") + ": " + file);
                } else {
                    updateLogs(message("Generated") + ": " + file);
                }
            } catch (Exception e) {
                updateLogs(message("Failed") + ": " + file + "\n" + e.toString());
            }
        }

        public void quit() {
            synchronized (pathThreads) {
                if (self != null) {
                    pathThreads.remove(self);
                    updateLogs(message("Stopped") + ": " + message("PathThread") + self.getId() + "    "
                            + message("Count") + ": " + pathThreads.size());
                }
                if (pathThreads.isEmpty()) {
                    synchronized (completedAddresses) {
                        completedAddresses.clear();
                        completedLinks.clear();
                        updateLogs(message("DataCleared"));
                    }
                    if (miaowCheck.isSelected()) {
                        FxmlControl.miao7();
                    }
                }
            }
            checkData();
        }

        public void setPath(File path) {
            this.path = path;
        }

        public void setSelf(PathThread self) {
            this.self = self;
        }

    };

    @FXML
    public void okLogsNumber() {
        try {
            int v = Integer.parseInt(maxLogsinput.getText());
            if (v > 0) {
                maxLogs = v;
                maxLogsinput.setStyle(null);
                AppVariables.setUserConfigInt(baseName + "MaxLogs", maxLogs);
            } else {
                maxLogsinput.setStyle(badStyle);
            }
        } catch (Exception e) {
            maxLogsinput.setStyle(badStyle);
        }
    }

    protected void updateLogs(final String line) {
        Platform.runLater(() -> {
            try {
                String newLogs = DateTools.datetimeToString(new Date()) + "  " + line + "\n";
                logsTextArea.insertText(0, newLogs);
                int len = logsTextArea.getLength();
                if (len > maxLogs) {
                    logsTextArea.deleteText(len - len / 4, len - 1);
                }
                logsTextArea.setScrollTop(0);
            } catch (Exception e) {
                logger.debug(e.toString());
            }
        });
    }

    @FXML
    protected void clearLogs() {
        logsTextArea.setText("");
    }

    @FXML
    protected void openFolder() {
        try {
            browseURI(targetPathController.file.toURI());
        } catch (Exception e) {
        }
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (!stopped) {
            boolean ask = false;
            synchronized (downloadingLock) {
                if (!downloadingData.isEmpty()) {
                    ask = true;
                }
            }
            synchronized (paths) {
                if (!paths.isEmpty()) {
                    ask = true;
                }
            }
            if (ask) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(getMyStage().getTitle());
                alert.setContentText(AppVariables.message("TaskRunning"));
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                ButtonType buttonSure = new ButtonType(AppVariables.message("Sure"));
                ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
                alert.getButtonTypes().setAll(buttonSure, buttonCancel);
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.setAlwaysOnTop(true);
                stage.toFront();

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == buttonSure) {
                    stopped = true;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

}
