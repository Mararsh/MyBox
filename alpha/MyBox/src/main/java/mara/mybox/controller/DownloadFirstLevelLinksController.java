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
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import mara.mybox.data.Link;
import mara.mybox.data.Link.FilenameType;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlReadTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.MarkdownTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.UrlTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-10-11
 * @License Apache License Version 2.0
 */
public class DownloadFirstLevelLinksController extends BaseTablePagesController<Link> {

    protected final ObservableList<Link> downloadingData, failedData;
    protected int maxThreadsNumber, maxLogs, maxRetries;
    protected final List<DownloadThread> downloadThreads;
    protected final List<PathThread> pathThreads;
    protected final Map<File, Integer> paths;
    protected final Map<Link, Integer> retries;
    protected final Map<String, File> completedAddresses;
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
    protected Tab linksTab, optionsTab, downloadingTab, failedTab, logsTab;
    @FXML
    protected TextField addressInput;
    @FXML
    protected TextField maxLogsinput, webConnectTimeoutInput, webReadTimeoutInput;
    @FXML
    protected TableView<Link> downloadingTableView, failedTableView;
    @FXML
    protected TableColumn<Link, String> addressPathColumn, addressFileColumn,
            filenameColumn, nameColumn, titleColumn, pathColumn, fileColumn,
            downloadingLinkColumn, downloadingFileColumn, failedLinkColumn, failedFileColumn;
    @FXML
    protected TableColumn<Link, Integer> indexColumn;
    @FXML
    protected ControlPathInput targetPathInputController;
    @FXML
    protected Button equalButton, linkButton, htmlButton,
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
    protected VBox optionsBox, htmlOptionsBox, pdfOptionsBox;
    @FXML
    protected ControlTTFSelector ttfController;
    @FXML
    protected TextArea cssArea;

    public DownloadFirstLevelLinksController() {
        baseTitle = message("DownloadHtmls");
        TipsLabelKey = "DownloadFirstLevelLinksComments";

        tableData = FXCollections.observableArrayList();
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
            stopped = true;

            htmlOptions = MarkdownTools.htmlOptions();
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
            textOptions.set(Parser.EXTENSIONS, Parser.EXTENSIONS.get(textHolder));
            textParser = Parser.builder(textOptions).build();
            textCollectingVisitor = new TextCollectingVisitor();

            targetPathInputController.parent(this);
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
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
            MyBoxLog.console(e.toString());
        }
    }

    public void initLinksTab() {
        try {
            addressInput.setText("https://www.cnblogs.com/Imageshop/p/10664478.html");

            addressPathColumn.setCellValueFactory(new PropertyValueFactory<>("addressPath"));
            addressFileColumn.setCellValueFactory(new PropertyValueFactory<>("addressFile"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            pathColumn.setCellValueFactory(new PropertyValueFactory<>("fileParent"));
            filenameColumn.setCellValueFactory(new PropertyValueFactory<>("filename"));
            indexColumn.setCellValueFactory(new PropertyValueFactory<>("index"));
            fileColumn.setCellValueFactory(new PropertyValueFactory<>("file"));

            goButton.disableProperty().bind(
                    targetPathInputController.fileInput.styleProperty().isEqualTo(UserConfig.badStyle())
                            .or(addressInput.textProperty().isNull())
            );
            startButton.disableProperty().bind(
                    targetPathInputController.fileInput.styleProperty().isEqualTo(UserConfig.badStyle())
                            .or(tableView.getSelectionModel().selectedItemProperty().isNull())
            );

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();
        boolean none = isNoneSelected();
        copyButton.setDisable(none);
        equalButton.setDisable(none);
        infoButton.setDisable(none);
        linkButton.setDisable(none);
        htmlButton.setDisable(none);
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
            MyBoxLog.console(e.toString());
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
            MyBoxLog.console(e.toString());
        }
    }

    public void initOptionsTab() {
        try {
            relinksCheck.setSelected(UserConfig.getBoolean(baseName + "Relinks", true));
            relinksCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                UserConfig.setBoolean(baseName + "Relinks", relinksCheck.isSelected());
            });

            indexCheck.setSelected(UserConfig.getBoolean(baseName + "GenerateIndex", true));
            indexCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                UserConfig.setBoolean(baseName + "GenerateIndex", indexCheck.isSelected());
            });

            pdfTextCheck.setSelected(UserConfig.getBoolean(baseName + "MergeTextPDF", true));
            pdfMarkdownCheck.setSelected(UserConfig.getBoolean(baseName + "MergeMarkdownPDF", false));
            pdfHtmlCheck.setSelected(UserConfig.getBoolean(baseName + "MergeHtmlPDF", false));
            pdfOptionsBox.setDisable(!pdfTextCheck.isSelected() && !pdfMarkdownCheck.isSelected() && !pdfHtmlCheck.isSelected());

            pdfTextCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                UserConfig.setBoolean(baseName + "MergeTextPDF", pdfTextCheck.isSelected());
                pdfOptionsBox.setDisable(!pdfTextCheck.isSelected() && !pdfMarkdownCheck.isSelected() && !pdfHtmlCheck.isSelected());
            });
            pdfMarkdownCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                UserConfig.setBoolean(baseName + "MergeMarkdownPDF", pdfMarkdownCheck.isSelected());
                pdfOptionsBox.setDisable(!pdfTextCheck.isSelected() && !pdfMarkdownCheck.isSelected() && !pdfHtmlCheck.isSelected());
            });
            pdfHtmlCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                UserConfig.setBoolean(baseName + "MergeHtmlPDF", pdfHtmlCheck.isSelected());
                pdfOptionsBox.setDisable(!pdfTextCheck.isSelected() && !pdfMarkdownCheck.isSelected() && !pdfHtmlCheck.isSelected());
            });

            textCheck.setSelected(UserConfig.getBoolean(baseName + "MergeText", true));
            textCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                UserConfig.setBoolean(baseName + "MergeText", textCheck.isSelected());
            });
            htmlCheck.setSelected(UserConfig.getBoolean(baseName + "MergeHtml", true));
            htmlCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                UserConfig.setBoolean(baseName + "MergeHtml", htmlCheck.isSelected());
            });
            mdCheck.setSelected(UserConfig.getBoolean(baseName + "MergeMarkdown", true));
            mdCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                UserConfig.setBoolean(baseName + "MergeMarkdown", mdCheck.isSelected());
            });

            ttfController.name(baseName);
            ttfController.ttfSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue == null || newValue.isBlank()) {
                        return;
                    }
                    checkTtf();
                }
            });
            checkTtf();

            webConnectTimeoutInput.setText(UserConfig.getInt("WebConnectTimeout", 10000) + "");
            webReadTimeoutInput.setText(UserConfig.getInt("WebReadTimeout", 10000) + "");

            intervalController.isSeconds(false).init(baseName + "Inteval", 1000);

            threadsSelector.getItems().addAll(Arrays.asList("6", "3", "1", "2", "5", "8"));
            maxThreadsNumber = UserConfig.getInt(baseName + "ThreadsNumber", 6);
            if (maxThreadsNumber <= 0) {
                maxThreadsNumber = 6;
            }
            threadsSelector.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                try {
                    int v = Integer.parseInt(newValue);
                    if (v >= 0) {
                        maxThreadsNumber = v;
                        UserConfig.setInt(baseName + "ThreadsNumber", v);
                        threadsSelector.getEditor().setStyle(null);
                        checkThreads();
                    } else {
                        threadsSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                } catch (Exception e) {
                    threadsSelector.getEditor().setStyle(UserConfig.badStyle());
                }
            });
            threadsSelector.getSelectionModel().select(maxThreadsNumber + "");

            retriesSelector.getItems().addAll(Arrays.asList("3", "2", "1", "4", "5", "6"));
            maxRetries = UserConfig.getInt(baseName + "MaxRetries", 3);
            if (maxRetries <= 0) {
                maxRetries = 3;
            }
            retriesSelector.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                try {
                    int v = Integer.parseInt(newValue);
                    if (v > 0) {
                        maxRetries = v;
                        UserConfig.setInt(baseName + "MaxRetries", v);
                        retriesSelector.getEditor().setStyle(null);
                    } else {
                        retriesSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                } catch (Exception e) {
                    retriesSelector.getEditor().setStyle(UserConfig.badStyle());
                }
            });
            retriesSelector.getSelectionModel().select(maxRetries + "");

            miaowCheck.setSelected(UserConfig.getBoolean(baseName + "Miaow", true));
            miaowCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "Miaow", miaowCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    public void checkTtf() {
        String value = ttfController.ttfSelector.getValue();
        int pos = value.indexOf("    ");
        String ttf;
        if (pos < 0) {
            ttf = value;
        } else {
            ttf = value.substring(0, pos);
        }
        String css = "@font-face {\n"
                + "  font-family: 'myFont';\n"
                + "  src: url('file:///" + ttf.replaceAll("\\\\", "/") + "');\n"
                + "  font-weight: normal;\n"
                + "  font-style: normal;\n"
                + "}\n"
                + " body { font-family:  'myFont';}";
        cssArea.setText(css);
        cssArea.setText(css);
    }

    public void initLogsTab() {
        try {
            maxLogs = UserConfig.getInt(baseName + "MaxLogs", 50000);
            maxLogsinput.setText(maxLogs + "");

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(copyButton, message("CopyLink") + "\nCTRL+c");
            NodeStyleTools.setTooltip(copyDownloadingButton, message("CopyLink"));
            NodeStyleTools.setTooltip(linkFailedButton, message("CopyLink"));
            NodeStyleTools.setTooltip(htmlButton, message("AddressHtml"));
            NodeStyleTools.removeTooltip(equalButton);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();
            if (targetPathInputController.pickFile() == null) {
                tabPane.getSelectionModel().select(optionsTab);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
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
                MyBoxLog.console(e.toString());
            }
        }
    }

    @FXML
    @Override
    public void goAction() {
        String address = addressInput.getText();
        if (address == null) {
            popError(message("InvalidParameters") + ": " + message("Address"));
            return;
        }
        TableStringValues.add("DownloadHtmlsHistories", address);
        File downloadPath = targetPathInputController.pickFile();
        if (downloadPath == null) {
            popError(message("InvalidParameters") + ": " + message("TargetPath"));
            tabPane.getSelectionModel().select(optionsTab);
            return;
        }
        updateLogs(message("WebPageAddress") + ": " + address);
        updateLogs(message("TargetPath") + ": " + downloadPath);
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private String title;

            @Override
            protected boolean handle() {
                try {
                    URL url = UrlTools.url(address);
                    if (url == null) {
                        return false;
                    }
                    File urlFile = HtmlReadTools.download(this, address);
                    String html = TextFileTools.readTexts(this, urlFile);
                    if (html == null || !isWorking()) {
                        return false;
                    }
                    title = HtmlReadTools.title(html);
                    addressLink = Link.create().setUrl(url).setAddress(url.toString())
                            .setName(title).setTitle(title).setHtml(html);
                    addressLink.setFile(urlFile.getAbsolutePath());
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
        start(task);
    }

    public void setValues(String title) {
        if (addressLink == null) {
            return;
        }
        DownloadFirstLevelLinksSetController controller
                = (DownloadFirstLevelLinksSetController) childStage(Fxmls.DownloadFirstLevelLinksSetFxml);
        controller.setValues(this, title);
    }

    public void readLinks(String subPath, Link.FilenameType nameType) {
        if (addressLink == null || subPath == null) {
            return;
        }
        this.subPath = subPath;
        filenameType = nameType;
        tableData.clear();
        File downloadPath = targetPathInputController.pickFile();
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private List<Link> links;

            @Override
            protected boolean handle() {
                File path = new File(downloadPath.getAbsolutePath() + File.separator + subPath);
                links = HtmlReadTools.links(this, addressLink, path, nameType);
                return links != null;
            }

            @Override
            protected void whenSucceeded() {
                if (!links.isEmpty()) {
                    tableData.addAll(links);
                    tableView.getSortOrder().clear();
                    tableView.getSortOrder().addAll(addressPathColumn, indexColumn);

                    for (Link link : links) {
                        if (link.getAddressPath().startsWith(addressLink.getAddressPath())) {
                            tableView.getSelectionModel().select(link);
                        }
                    }
                }

                String txt = message("Links") + ": " + tableData.size();
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
        start(task);
    }

    @FXML
    @Override
    public void startAction() {
        try {
            stopped = false;
            List<Link> selected = selectedItems();
            if (selected == null || selected.isEmpty()) {
                return;
            }
            synchronized (paths) {
                for (int i = 0; i < selected.size(); i++) {
                    Link link = selected.get(i);
                    link.setIndex(i + 1);
                    File file = new File(link.getFile());
                    link.setFile(file.getAbsolutePath());
                    File path = file.getParentFile();
                    path.mkdirs();
                    if (paths.containsKey(path)) {
                        paths.put(path, paths.get(path) + 1);
                    } else {
                        paths.put(path, 1);
                    }
                }
            }
            synchronized (downloadingData) {
                downloadingData.addAll(selected);
            }
            checkData();
            tabPane.getSelectionModel().select(logsTab);
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @FXML
    public void stop() {
        stopped = true;
    }

    @FXML
    public void popSetMenu(MouseEvent mouseEvent) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem(message("SetSubdirectoryName"));
            menu.setOnAction((ActionEvent event) -> {
                setPath();
            });
            items.add(menu);
            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("AddOrderBeforeFilename"));
            menu.setOnAction((ActionEvent event) -> {
                addOrderBeforeFilename();
            });
            items.add(menu);

            menu = new MenuItem(message("SetLinkNameAsFilename"));
            menu.setOnAction((ActionEvent event) -> {
                setLinkNameAsFilename();
            });
            items.add(menu);

            menu = new MenuItem(message("SetLinkTitleAsFilename"));
            menu.setOnAction((ActionEvent event) -> {
                setLinkTitleAsFilename();
            });
            items.add(menu);

            menu = new MenuItem(message("SetLinkAddressAsFilename"));
            menu.setOnAction((ActionEvent event) -> {
                setLinkAddressAsFilename();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            popEventMenu(mouseEvent, items);

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    public void setPath() {
        List<Link> selected = selectedItems();
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
            File fullpath = new File(targetPathInputController.pickFile().getAbsolutePath() + File.separator + path);
            String filename = link.filename(fullpath, filenameType);
            link.setFile(filename);
        }
        tableView.refresh();
    }

    public void addOrderBeforeFilename() {
        List<Link> selected = selectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        for (int i = 0; i < selected.size(); i++) {
            Link link = selected.get(i);
            String filename = link.getFile();
            if (filename == null) {
                filename = link.filename(new File(targetPathInputController.pickFile().getAbsolutePath()), filenameType);
                link.setFile(filename);
            }
            File file = new File(filename);
            String newName = file.getParent() + File.separator + (i + 1) + "_" + file.getName();
            link.setFile(newName);
        }
        tableView.refresh();
    }

    public void setFilename() {
        List<Link> selected = selectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        for (Link link : selected) {
            String filename = link.getFile();
            if (filename == null) {
                filename = link.filename(new File(targetPathInputController.pickFile().getAbsolutePath()), filenameType);
                link.setFile(filename);
            }
            File file = new File(filename);
            String suffix = FileNameTools.ext(file.getName());
            suffix = (suffix != null && !suffix.isBlank()) ? "." + suffix : "";
            String newName = file.getParent() + File.separator + link.pageName(filenameType) + suffix;
            link.setFile(newName);
        }
        tableView.refresh();
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
        Link link = selectedItem();
        if (link == null) {
            return;
        }
        TextClipboardTools.copyToSystemClipboard(myController, link.getAddress());
        updateLogs(message("Copied") + ": " + link.getAddress());
    }

    @FXML
    @Override
    public boolean infoAction() {
        Link link = selectedItem();
        if (link == null) {
            return false;
        }
        HtmlReadTools.requestHead(this, link.getAddress());
        return true;
    }

    @FXML
    public void infoDownloading() {
        Link link = downloadingTableView.getSelectionModel().getSelectedItem();
        if (link == null) {
            return;
        }
        HtmlReadTools.requestHead(this, link.getAddress());
    }

    @FXML
    public void infoFailed() {
        Link link = failedTableView.getSelectionModel().getSelectedItem();
        if (link == null) {
            return;
        }
        HtmlReadTools.requestHead(this, link.getAddress());
    }

    @FXML
    public void html() {
        try {
            String addr = addressInput.getText();
            if (addr == null || addr.isBlank()) {
                popError(message("InvalidParameters") + ": " + message("Address"));
                return;
            }
            WebBrowserController.openAddress(addr, true);
        } catch (Exception e) {
        }
    }

    @Override
    public void itemDoubleClicked() {
        openLink();
    }

    @FXML
    protected void openLink() {
        Link link = selectedItem();
        openLink(link);
    }

    @FXML
    protected void linkDownloading() {
        Link link = downloadingTableView.getSelectionModel().getSelectedItem();
        openLink(link);
    }

    @FXML
    protected void linkFailed() {
        Link link = failedTableView.getSelectionModel().getSelectedItem();
        openLink(link);
    }

    protected void openLink(Link link) {
        if (link == null) {
            return;
        }
        openLink(link.getAddress());
    }

    @FXML
    @Override
    public void viewAction() {
        Link link = tableView.getSelectionModel().getSelectedItem();
        view(link);
    }

    @FXML
    public void view(Link link) {
        if (link == null || targetPathInputController.pickFile() == null) {
            return;
        }
        String s = message("Address") + ": " + link.getAddress() + "<br>"
                + message("Name") + ": " + (link.getName() == null ? "" : link.getName()) + "<br>"
                + message("Title") + ": " + (link.getTitle() == null ? "" : link.getTitle()) + "<br>"
                + message("TargetFile") + ": " + link.getFile();
        HtmlTableController.open(message("Link"), s);
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
        String string = TextClipboardTools.getSystemClipboardString();
        if (string != null && !string.isBlank()) {
            addressInput.setText(string);
        }
        goAction();
    }

    @FXML
    public void clearDownloading() {
        synchronized (downloadingData) {
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
        synchronized (downloadingData) {
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
        TextClipboardTools.copyToSystemClipboard(myController, link.getAddress());
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
                MyBoxLog.console(e.toString());
            }
        }
        checkData();
    }

    @FXML
    public void clearFailed() {
        synchronized (failedData) {
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
        synchronized (failedData) {
            failedData.removeAll(links);
        }
    }

    @FXML
    public void copyFailed() {
        Link link = failedTableView.getSelectionModel().getSelectedItem();
        if (link == null) {
            return;
        }
        TextClipboardTools.copyToSystemClipboard(myController, link.getAddress());
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
            synchronized (downloadingData) {
                if (!downloadingData.contains(link)) {
                    downloadingData.add(0, link);
                    added = true;
                }
            }
            synchronized (failedData) {
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
        synchronized (downloadingData) {
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
                        updateLogs(message("Started") + ": " + message("DownloadThread") + linkTask.threadId() + "    "
                                + message("Count") + ": " + downloadThreads.size());
                    }
                } catch (Exception e) {
                    MyBoxLog.console(e.toString());
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
                webConnectTimeoutInput.setStyle(UserConfig.badStyle());
                return;
            }
        } catch (Exception e) {
            webConnectTimeoutInput.setStyle(UserConfig.badStyle());
            return;
        }
        try {
            int v = Integer.parseInt(webReadTimeoutInput.getText());
            if (v > 0) {
                readValue = v;
                webReadTimeoutInput.setStyle(null);
            } else {
                webReadTimeoutInput.setStyle(UserConfig.badStyle());
                return;
            }
        } catch (Exception e) {
            webReadTimeoutInput.setStyle(UserConfig.badStyle());
            return;
        }
        UserConfig.setInt("WebConnectTimeout", connValue);
        UserConfig.setInt("WebReadTimeout", readValue);
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
                synchronized (downloadingData) {
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
                file.getParentFile().mkdirs();
                link.setFile(file.getAbsolutePath());

                updateLogs(message("Downloading") + ": " + url + " --> " + file);
                File tmpFile = HtmlReadTools.download(null, url.toString());
                if (tmpFile != null && tmpFile.exists()) {
                    FileTools.override(tmpFile, file);
                    link.setDlTime(new Date());
                    updateLogs(message("Downloaded") + ": " + url + " --> " + file);
                    if (utf8Check.isSelected()) {
                        String utf8 = HtmlWriteTools.toUTF8(null, file);
                        if (utf8 == null) {
                            updateLogs(message("Failed") + ": " + file);
                        } else if (!"NeedNot".equals(utf8)) {
                            updateLogs(message("HtmlSetCharset") + ": " + file);
                            TextFileTools.writeFile(file, utf8, Charset.forName("utf-8"));
                        }
                    }
                    if (relinksCheck.isSelected()) {
                        synchronized (completedAddresses) {
                            completedAddresses.put(url.toString(), file);
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
                MyBoxLog.console(link);
            } catch (Exception e) {
                MyBoxLog.console(link, e.toString());
            }
        }

        protected void failed(Link link, String error) {
            if (link != null && link.getFile() != null) {
                synchronized (downloadingData) {
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
                        synchronized (downloadingData) {
                            downloadingData.add(0, link);
                        }
                        updateLogs(message("Retry") + " " + currentRetries
                                + ": " + link.getUrl() + " --> " + link.getFile());
                    } else {
                        synchronized (failedData) {
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
                        updateLogs(message("Stopped") + ": " + message("DownloadThread") + self.threadId() + "    "
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
                                updateLogs(message("Started") + ": " + message("PathThread") + pThread.threadId() + "    "
                                        + message("Count") + ": " + pathThreads.size());
                            }
                        }
                        paths.clear();
                    }
                }
                checkData();
            } catch (Exception e) {
                MyBoxLog.debug(e);
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
                    return FileNameTools.compareName(f1, f2);
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
                HtmlWriteTools.relinkPage(null, file, completedLinks, completedAddresses);
                updateLogs(message("HtmlLinksRewritten") + ": " + file);
            }
        }

        public void frameset() {
            if (stopped || files == null || files.isEmpty() || !indexCheck.isSelected()) {
                return;
            }
            updateLogs(message("GeneratingPathIndex") + ": " + path + " ...");
            HtmlWriteTools.makePathList(path, files, completedLinks);
            File frameFile = new File(path.getAbsolutePath() + File.separator + "0000_" + message("PathIndex") + ".html");
            if (HtmlWriteTools.generateFrameset(files, frameFile)) {
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
                    String html = TextFileTools.readTexts(null, file);
                    String body = HtmlReadTools.body(html, true);
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
                TextFileTools.writeFile(new File(htmlFile), html);
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
                TextFileTools.writeFile(new File(textFile), text);
                updateLogs(message("Generated") + ": " + textFile);
            }
            if (stopped) {
                return;
            }
            if (mdCheck.isSelected() && md != null) {
                String mdFile = filePrefix + ".md";
                TextFileTools.writeFile(new File(mdFile), md);
                updateLogs(message("Generated") + ": " + mdFile);
            }
            if (stopped) {
                return;
            }
            if (pdfTextCheck.isSelected() && text != null) {
                String pdfFile = filePrefix + "_text.pdf";
                String textHtml = HtmlWriteTools.textToHtml(text);
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
                    FileDeleteTools.delete(ffile);
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
                    updateLogs(message("Stopped") + ": " + message("PathThread") + self.threadId() + "    "
                            + message("Count") + ": " + pathThreads.size());
                }
                if (pathThreads.isEmpty()) {
                    synchronized (completedAddresses) {
                        completedAddresses.clear();
                        completedLinks.clear();
                        updateLogs(message("DataCleared"));
                    }
                    if (miaowCheck.isSelected()) {
                        SoundTools.miao7();
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
                UserConfig.setInt(baseName + "MaxLogs", maxLogs);
            } else {
                maxLogsinput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            maxLogsinput.setStyle(UserConfig.badStyle());
        }
    }

    @Override
    public void updateLogs(final String line) {
        Platform.runLater(() -> {
            try {
                String logs = DateTools.datetimeToString(new Date()) + "  " + line + "\n";
                logsTextArea.insertText(0, logs);
                int len = logsTextArea.getLength();
                if (len > maxLogs) {
                    logsTextArea.deleteText(len - len / 4, len - 1);
                }
                logsTextArea.setScrollTop(0);
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
        });
    }

    @FXML
    @Override
    public void clearLogs() {
        logsTextArea.setText("");
    }

    @FXML
    protected void openPath() {
        try {
            browseURI(targetPathInputController.pickFile().toURI());
        } catch (Exception e) {
        }
    }

    @FXML
    protected void showAddressHistories(Event event) {
        PopTools.popStringValues(this, addressInput, event, "DownloadHtmlsHistories", false);
    }

    @FXML
    protected void popAddressHistories(Event event) {
        if (UserConfig.getBoolean("DownloadHtmlsHistoriesPopWhenMouseHovering", false)) {
            showAddressHistories(event);
        }
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (!stopped) {
            boolean ask = false;
            synchronized (downloadingData) {
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
                if (PopTools.askSure(getTitle(), message("TaskRunning"))) {
                    stopped = true;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean keyEnter() {
        if (addressInput != null && addressInput.isFocused()) {
            goAction();
            return true;
        }
        return false;
    }

}
