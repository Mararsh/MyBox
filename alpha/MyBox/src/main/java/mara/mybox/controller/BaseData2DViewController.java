package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import mara.mybox.data2d.tools.Data2DPageTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-12-17
 * @License Apache License Version 2.0
 */
public class BaseData2DViewController extends BaseData2DLoadController {

    protected FxTask loadTask;
    protected String delimiterName;
    protected WebEngine webEngine;

    @FXML
    protected ToggleGroup formatGroup;
    @FXML
    protected RadioButton htmlRadio, tableRadio, textsRadio, csvRadio;
    @FXML
    protected TabPane tmpPane;
    @FXML
    protected VBox pageBox, tableBox, htmlBox, csvBox;
    @FXML
    protected TextArea textsArea, csvArea;
    @FXML
    protected WebView webView;
    @FXML
    protected Label columnsLabel;
    @FXML
    protected FlowPane buttonsPane;
    @FXML
    protected Button delimiterButton, viewDataButton, editHtmlButton;
    @FXML
    protected CheckBox wrapCheck, formCheck, titleCheck, columnCheck, rowCheck;

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            if (htmlRadio != null) {
                StyleTools.setIconTooltips(htmlRadio, "iconHtml.png", message("PageDataInHtml") + " - " + message("ReadOnly"));
            }
            if (tableRadio != null) {
                StyleTools.setIconTooltips(tableRadio, "iconGrid.png",
                        dataManufactureButton != null ? message("Table") : message("TableEdit"));
            }
            if (textsRadio != null) {
                StyleTools.setIconTooltips(textsRadio, "iconTxt.png", message("PageDataInText") + " - " + message("ReadOnly"));
            }
            if (csvRadio != null) {
                StyleTools.setIconTooltips(csvRadio, "iconCSV.png", message("CsvEdit"));
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void initMoreControls() {
        try {
            if (mainAreaBox == null || tmpPane == null) {
                return;
            }
            mainAreaBox.getChildren().remove(tmpPane);
            toolbar.getChildren().remove(leftPaneControl);
            refreshStyle(mainAreaBox);

            webEngine = webView.getEngine();
            webView.setCache(false);
            webEngine.setJavaScriptEnabled(true);

            wrapCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    if (isSettingValues) {
                        return;
                    }
                    if (textsRadio.isSelected()) {
                        UserConfig.setBoolean(baseName + "TextsWrap", nv);
                        textsArea.setWrapText(nv);
                    } else if (csvRadio != null && csvRadio.isSelected()) {
                        UserConfig.setBoolean(baseName + "CsvWrap", nv);
                        csvArea.setWrapText(nv);
                    }
                }
            });

            if (formCheck != null) {
                formCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                        if (isSettingValues) {
                            return;
                        }
                        if (textsRadio.isSelected()) {
                            UserConfig.setBoolean(baseName + "TextsShowForm", nv);
                            loadTexts(false);
                        } else if (htmlRadio.isSelected()) {
                            UserConfig.setBoolean(baseName + "HtmlShowForm", nv);
                            loadHtml(false);
                        }
                    }
                });
            }

            if (titleCheck != null) {
                titleCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                        if (isSettingValues) {
                            return;
                        }
                        if (textsRadio.isSelected()) {
                            UserConfig.setBoolean(baseName + "TextsShowTitle", nv);
                            loadTexts(false);
                        } else if (htmlRadio.isSelected()) {
                            UserConfig.setBoolean(baseName + "HtmlShowTitle", nv);
                            loadHtml(false);
                        }
                    }
                });
            }

            if (columnCheck != null) {
                columnCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                        if (isSettingValues) {
                            return;
                        }
                        if (textsRadio.isSelected()) {
                            UserConfig.setBoolean(baseName + "TextsShowColumns", nv);
                            loadTexts(false);
                        } else if (htmlRadio.isSelected()) {
                            UserConfig.setBoolean(baseName + "HtmlShowColumns", nv);
                            loadHtml(false);
                        }
                    }
                });
            }

            if (rowCheck != null) {
                rowCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                        if (isSettingValues) {
                            return;
                        }
                        if (textsRadio.isSelected()) {
                            UserConfig.setBoolean(baseName + "TextsShowRowNumber", nv);
                            loadTexts(false);
                        } else if (htmlRadio.isSelected()) {
                            UserConfig.setBoolean(baseName + "HtmlShowRowNumber", nv);
                            loadHtml(false);
                        }
                    }
                });
            }

            formatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    checkFormat(ov);
                }
            });
            checkFormat(null);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        format
     */
    public void checkFormat(Toggle ov) {
        switchFormat();
    }

    public void switchFormat() {
        try {
            if (isSettingValues || pageBox == null) {
                return;
            }
            isSettingValues = true;
            buttonsPane.getChildren().clear();
            pageBox.getChildren().clear();
            webEngine.getLoadWorker().cancel();
            webEngine.loadContent("");
            textsArea.clear();
            tableView.setItems(null);
            if (csvRadio != null) {
                csvArea.clear();
                columnsLabel.setText("");
            }
            isSettingValues = false;
            if (data2D == null) {
                return;
            }

            if (htmlRadio.isSelected()) {
                showHtml();

            } else if (tableRadio.isSelected()) {
                showTable();
                checkSelected();

            } else if (textsRadio.isSelected()) {
                showTexts();

            } else if (csvRadio != null && csvRadio.isSelected()) {
                showCsv();

            }

            refreshStyle(mainAreaBox);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void showHtml() {
        try {
            showHtmlButtons();
            pageBox.getChildren().add(htmlBox);
            VBox.setVgrow(htmlBox, Priority.ALWAYS);

            loadHtml(false);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void showHtmlButtons() {
        buttonsPane.getChildren().setAll(editHtmlButton, viewDataButton, dataManufactureButton);
    }

    public void loadHtml(boolean pop) {
        if (!data2D.isValidDefinition()) {
            if (pop) {
                popError(message("NoData"));
            } else {
                webEngine.getLoadWorker().cancel();
                webEngine.loadContent("");
            }
            return;
        }
        if (loadTask != null) {
            loadTask.cancel();
        }
        loadTask = new FxSingletonTask<Void>(this) {
            private String html;

            @Override
            protected boolean handle() {
                html = Data2DPageTools.pageToHtml(data2D, styleFilter,
                        UserConfig.getBoolean(baseName + "HtmlShowForm", false),
                        UserConfig.getBoolean(baseName + "HtmlShowColumns", true),
                        UserConfig.getBoolean(baseName + "HtmlShowRowNumber", true),
                        UserConfig.getBoolean(baseName + "HtmlShowTitle", true));
                return html != null;
            }

            @Override
            protected void whenCanceled() {
            }

            @Override
            protected void whenFailed() {
            }

            @Override
            protected void whenSucceeded() {
                if (pop) {
                    HtmlPopController.openHtml(myController, html);
                } else {
                    webEngine.getLoadWorker().cancel();
                    webEngine.loadContent(html);
                }
            }

        };
        start(loadTask, false);
    }

    public void showTexts() {
        try {
            showTextsButtons();
            pageBox.getChildren().add(textsArea);
            VBox.setVgrow(textsArea, Priority.ALWAYS);

            delimiterName = UserConfig.getString(baseName + "TextsDelimiter", ",");
            isSettingValues = true;
            wrapCheck.setSelected(UserConfig.getBoolean(baseName + "TextsWrap", true));
            textsArea.setWrapText(wrapCheck.isSelected());
            isSettingValues = false;

            loadTexts(false);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void showTextsButtons() {
        buttonsPane.getChildren().setAll(wrapCheck, delimiterButton,
                menuButton, viewDataButton, dataManufactureButton);
    }

    public void loadTexts(boolean pop) {
        if (!data2D.isValidDefinition()) {
            if (pop) {
                popError(message("NoData"));
            } else {
                textsArea.setText("");
            }
            return;
        }
        if (loadTask != null) {
            loadTask.cancel();
        }
        loadTask = new FxSingletonTask<Void>(this) {
            private String texts;

            @Override
            protected boolean handle() {
                texts = Data2DPageTools.pageToTexts(data2D, delimiterName,
                        UserConfig.getBoolean(baseName + "TextsShowForm", false),
                        UserConfig.getBoolean(baseName + "TextsShowColumns", true),
                        UserConfig.getBoolean(baseName + "TextsShowRowNumber", true),
                        UserConfig.getBoolean(baseName + "TextsShowTitle", true));
                return texts != null;
            }

            @Override
            protected void whenCanceled() {
            }

            @Override
            protected void whenFailed() {
            }

            @Override
            protected void whenSucceeded() {
                if (pop) {
                    TextPopController.loadText(texts);
                } else {
                    textsArea.setText(texts);
                }
            }

        };
        start(loadTask, false);
    }

    public void showTable() {
        try {
            showTableButtons();
            pageBox.getChildren().add(tableBox);
            VBox.setVgrow(tableBox, Priority.ALWAYS);

            loadTable();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void showTableButtons() {
        buttonsPane.getChildren().setAll(menuButton, viewDataButton, dataManufactureButton);
    }

    public void loadTable() {
        try {
            List<List<String>> data = new ArrayList<>();
            data.addAll(tableData);

            super.makeColumns();

            super.updateTable(data);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void showCsv() {
    }

    public void loadCsv() {
    }

    /*
        data
     */
    @Override
    public void makeColumns() {
        if (tableRadio != null && !tableRadio.isSelected()) {
            return;
        }
        super.makeColumns();
    }

    @Override
    protected void setPagination() {
        super.setPagination();
        switchFormat();
    }

    @Override
    public void updateTable(List<List<String>> data) {
        try {
            super.updateTable(data);
            switchFormat();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkBeforeLoadingTableData() {
        return checkBeforeNextAction();
    }

    @Override
    public boolean checkBeforeNextAction() {
        saveWidths();
        return true;
    }

    public void saveWidths() {
        try {
            if (data2D == null || !dataSizeLoaded
                    || !isValidPageData() || !widthChanged) {
                return;
            }
            data2D.saveAttributes();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        action
     */
    @FXML
    public void editHtml() {
        if (htmlRadio.isSelected()) {
            HtmlEditorController.openHtml(WebViewTools.getHtml(webEngine));
        }
    }

    @Override
    public boolean controlAltB() {
        saveAsAction();
        return true;
    }

    @FXML
    public void delimiterActon() {
        TextDelimiterController controller = TextDelimiterController.open(this, delimiterName, false, false);
        controller.okNotify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                delimiterName = controller.delimiterName;
                if (textsRadio.isSelected()) {
                    UserConfig.setString(baseName + "TextsDelimiter", delimiterName);
                    loadTexts(false);
                } else if (csvRadio != null && csvRadio.isSelected()) {
                    UserConfig.setString(baseName + "CsvDelimiter", delimiterName);
                    loadCsv();
                }
                popDone();
            }
        });
        if (data2D.isCSV() || data2D.isTexts()) {
            controller.label.setText(message("DelimiterNotAffectSource"));
        }
    }

    @FXML
    @Override
    public boolean menuAction() {
        try {
            closePopup();
            if (data2D == null) {
                return false;
            }

            if (htmlRadio.isSelected()) {
                return false;

            } else if (tableRadio.isSelected()) {
                popTableMenu();
                return true;

            } else if (textsRadio.isSelected()) {
                MenuTextEditController.textMenu(this, textsArea);
                return true;

            } else if (csvRadio != null && csvRadio.isSelected()) {
                MenuTextEditController.textMenu(this, csvArea);
                return true;

            }

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    @FXML
    @Override
    public boolean popAction() {
        try {
            closePopup();
            if (data2D == null) {
                return false;
            }

            if (htmlRadio.isSelected()) {
                HtmlPopController.openWebView(this, webView);
                return true;

            } else if (tableRadio.isSelected()) {
                loadHtml(true);
                return true;

            } else if (textsRadio.isSelected()) {
                TextPopController.openInput(this, textsArea);
                return true;

            } else if (csvRadio != null && csvRadio.isSelected()) {
                TextPopController.openInput(this, csvArea);
                return true;

            }

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    @Override
    public List<MenuItem> viewMenuItems(Event fevent) {
        try {
            if (data2D == null || !data2D.isValidDefinition()) {
                return null;
            }
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem(message("DataDefinition") + "    Ctrl+I " + message("Or") + " Alt+I",
                    StyleTools.getIconImageView("iconInfo.png"));
            menu.setOnAction((ActionEvent event) -> {
                infoAction();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("PageDataInHtml") + " - " + message("Pop"), StyleTools.getIconImageView("iconHtml.png"));
            menu.setOnAction((ActionEvent event) -> {
                loadHtml(true);
            });
            items.add(menu);

            menu = new MenuItem(message("PageDataInText") + " - " + message("Pop"), StyleTools.getIconImageView("iconTxt.png"));
            menu.setOnAction((ActionEvent event) -> {
                loadTexts(true);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            CheckMenuItem formItem = new CheckMenuItem(message("Html") + " - " + message("Form"));
            formItem.setSelected(UserConfig.getBoolean(baseName + "HtmlShowForm", false));
            formItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (htmlRadio.isSelected() && formCheck != null) {
                        formCheck.setSelected(formItem.isSelected());
                    } else {
                        UserConfig.setBoolean(baseName + "HtmlShowForm", formItem.isSelected());
                        if (htmlRadio.isSelected()) {
                            loadHtml(false);
                        }
                    }
                }
            });
            items.add(formItem);

            CheckMenuItem titleItem = new CheckMenuItem(message("Html") + " - " + message("Title"));
            titleItem.setSelected(UserConfig.getBoolean(baseName + "HtmlShowTitle", true));
            titleItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (htmlRadio.isSelected() && titleCheck != null) {
                        titleCheck.setSelected(titleItem.isSelected());
                    } else {
                        UserConfig.setBoolean(baseName + "HtmlShowTitle", titleItem.isSelected());
                        if (htmlRadio.isSelected()) {
                            loadHtml(false);
                        }
                    }
                }
            });
            items.add(titleItem);

            CheckMenuItem columnNameItem = new CheckMenuItem(message("Html") + " - " + message("ColumnName"));
            columnNameItem.setSelected(UserConfig.getBoolean(baseName + "HtmlShowColumns", true));
            columnNameItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (htmlRadio.isSelected() && columnCheck != null) {
                        columnCheck.setSelected(columnNameItem.isSelected());
                    } else {
                        UserConfig.setBoolean(baseName + "HtmlShowColumns", columnNameItem.isSelected());
                        if (htmlRadio.isSelected()) {
                            loadHtml(false);
                        }
                    }
                }
            });
            items.add(columnNameItem);

            CheckMenuItem rowNumberItem = new CheckMenuItem(message("Html") + " - " + message("RowNumber"));
            rowNumberItem.setSelected(UserConfig.getBoolean(baseName + "HtmlShowRowNumber", true));
            rowNumberItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (htmlRadio.isSelected() && rowCheck != null) {
                        rowCheck.setSelected(rowNumberItem.isSelected());
                    } else {
                        UserConfig.setBoolean(baseName + "HtmlShowRowNumber", rowNumberItem.isSelected());
                        if (htmlRadio.isSelected()) {
                            loadHtml(false);
                        }
                    }
                }
            });
            items.add(rowNumberItem);

            CheckMenuItem textFormItem = new CheckMenuItem(message("Texts") + " - " + message("Form"));
            textFormItem.setSelected(UserConfig.getBoolean(baseName + "TextsShowForm", false));
            textFormItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (textsRadio.isSelected() && formCheck != null) {
                        formCheck.setSelected(textFormItem.isSelected());
                    } else {
                        UserConfig.setBoolean(baseName + "TextsShowForm", textFormItem.isSelected());
                        if (textsRadio.isSelected()) {
                            loadTexts(false);
                        }
                    }
                }
            });
            items.add(textFormItem);

            CheckMenuItem textTitleItem = new CheckMenuItem(message("Texts") + " - " + message("Title"));
            textTitleItem.setSelected(UserConfig.getBoolean(baseName + "TextsShowTitle", true));
            textTitleItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (textsRadio.isSelected() && titleCheck != null) {
                        titleCheck.setSelected(textTitleItem.isSelected());
                    } else {
                        UserConfig.setBoolean(baseName + "TextsShowTitle", textTitleItem.isSelected());
                        if (textsRadio.isSelected()) {
                            loadTexts(false);
                        }
                    }
                }
            });
            items.add(textTitleItem);

            CheckMenuItem textColumnItem = new CheckMenuItem(message("Texts") + " - " + message("ColumnName"));
            textColumnItem.setSelected(UserConfig.getBoolean(baseName + "TextsShowColumns", true));
            textColumnItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (textsRadio.isSelected() && columnCheck != null) {
                        columnCheck.setSelected(textColumnItem.isSelected());
                    } else {
                        UserConfig.setBoolean(baseName + "TextsShowColumns", textColumnItem.isSelected());
                        if (textsRadio.isSelected()) {
                            loadTexts(false);
                        }
                    }
                }
            });
            items.add(textColumnItem);

            CheckMenuItem textRowNumberItem = new CheckMenuItem(message("Texts") + " - " + message("RowNumber"));
            textRowNumberItem.setSelected(UserConfig.getBoolean(baseName + "TextsShowRowNumber", true));
            textRowNumberItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (textsRadio.isSelected() && rowCheck != null) {
                        rowCheck.setSelected(textRowNumberItem.isSelected());
                    } else {
                        UserConfig.setBoolean(baseName + "TextsShowRowNumber", textRowNumberItem.isSelected());
                        if (textsRadio.isSelected()) {
                            loadTexts(false);
                        }
                    }
                }
            });
            items.add(textRowNumberItem);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    protected List<MenuItem> makeTableContextMenu() {
        try {
            if (data2D == null || !data2D.isValidDefinition()) {
                return null;
            }
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem(message("PageDataInHtml") + " - " + message("Pop"),
                    StyleTools.getIconImageView("iconHtml.png"));
            menu.setOnAction((ActionEvent event) -> {
                loadHtml(true);
            });
            items.add(menu);

            menu = new MenuItem(message("PageDataInText") + " - " + message("Pop"),
                    StyleTools.getIconImageView("iconTxt.png"));
            menu.setOnAction((ActionEvent event) -> {
                loadTexts(true);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            items.addAll(super.makeTableContextMenu());

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public void cleanPane() {
        try {
            if (loadTask != null) {
                loadTask.cancel();
                loadTask = null;
            }
        } catch (Exception e) {
        }
        super.cleanPane();
    }
}
