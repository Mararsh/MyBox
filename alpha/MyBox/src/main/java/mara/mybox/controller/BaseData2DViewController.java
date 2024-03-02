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
import mara.mybox.data2d.Data2DTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-12-17
 * @License Apache License Version 2.0
 */
public class BaseData2DViewController extends BaseData2DLoadController {

    protected String delimiterName;

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
    protected ControlWebView webViewController;
    @FXML
    protected Label columnsLabel;
    @FXML
    protected FlowPane buttonsPane;
    @FXML
    protected Button delimiterButton, viewDataButton;
    @FXML
    protected CheckBox wrapCheck;

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            if (htmlRadio != null) {
                StyleTools.setIconTooltips(htmlRadio, "iconHtml.png", message("HtmlReadOnly"));
            }
            if (tableRadio != null) {
                StyleTools.setIconTooltips(tableRadio, "iconGrid.png",
                        dataManufactureButton != null ? message("Table") : message("TableEdit"));
            }
            if (textsRadio != null) {
                StyleTools.setIconTooltips(textsRadio, "iconTxt.png", message("TextsReadOnly"));
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
            if (isSettingValues) {
                return;
            }
            isSettingValues = true;
            buttonsPane.getChildren().clear();
            pageBox.getChildren().clear();
            webViewController.loadContents("");
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

            } else if (textsRadio.isSelected()) {
                showTexts();

            } else if (csvRadio != null && csvRadio.isSelected()) {
                showCsv();

            }

            refreshStyle(dataBox);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void showHtml() {
        try {
            showHtmlButtons();
            pageBox.getChildren().add(htmlBox);
            VBox.setVgrow(htmlBox, Priority.ALWAYS);

            loadHtml();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void showHtmlButtons() {
        buttonsPane.getChildren().setAll(menuButton, viewDataButton, dataManufactureButton);
    }

    public void loadHtml() {
        if (!data2D.isValid()) {
            webViewController.loadContents("");
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private String html;

            @Override
            protected boolean handle() {
                html = Data2DTools.dataToHtml(data2D, styleFilter,
                        UserConfig.getBoolean(baseName + "HtmlShowForm", false),
                        UserConfig.getBoolean(baseName + "HtmlShowColumns", true),
                        UserConfig.getBoolean(baseName + "HtmlShowRowNumber", true),
                        UserConfig.getBoolean(baseName + "HtmlShowTitle", true));
                return html != null;
            }

            @Override
            protected void whenSucceeded() {
                webViewController.loadContents(html);
            }

        };
        start(task, false);
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

            loadTexts();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void showTextsButtons() {
        buttonsPane.getChildren().setAll(wrapCheck, delimiterButton,
                menuButton, viewDataButton, dataManufactureButton);
    }

    public void loadTexts() {
        if (!data2D.isValid()) {
            textsArea.setText("");
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private String text;

            @Override
            protected boolean handle() {
                text = Data2DTools.dataToTexts(data2D, delimiterName,
                        UserConfig.getBoolean(baseName + "TextsShowForm", false),
                        UserConfig.getBoolean(baseName + "TextsShowColumns", true),
                        UserConfig.getBoolean(baseName + "TextsShowRowNumber", true),
                        UserConfig.getBoolean(baseName + "TextsShowTitle", true));
                return text != null;
            }

            @Override
            protected void whenSucceeded() {
                textsArea.setText(text);
            }

        };
        start(task, false);
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
            isSettingValues = true;
            tableData.setAll(data);
            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void showCsv() {
    }

    public void loadCsv() {
    }

    /*
        page
     */
    @Override
    public void makeColumns() {
        if (tableRadio == null || !tableRadio.isSelected()) {
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
                    loadTexts();
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

    @Override
    public List<MenuItem> viewMenuItems(Event fevent) {
        try {
            if (data2D == null) {
                return null;
            }
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem(message("DataDefinition") + "    Ctrl+i " + message("Or") + " Alt+i",
                    StyleTools.getIconImageView("iconInfo.png"));
            menu.setOnAction((ActionEvent event) -> {
                infoAction();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("Html"), StyleTools.getIconImageView("iconHtml.png"));
            menu.setOnAction((ActionEvent event) -> {
                String html = Data2DTools.dataToHtml(data2D, styleFilter, false, true, true, true);
                HtmlPopController.openHtml(myController, html);
            });
            items.add(menu);

            menu = new MenuItem(message("Texts"), StyleTools.getIconImageView("iconTxt.png"));
            menu.setOnAction((ActionEvent event) -> {
                String texts = Data2DTools.dataToTexts(data2D, delimiterName, false, true, true, true);
                TextPopController.loadText(texts);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            CheckMenuItem formItem = new CheckMenuItem(message("Html") + " - " + message("Form"));
            formItem.setSelected(UserConfig.getBoolean(baseName + "HtmlShowForm", false));
            formItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "HtmlShowForm", formItem.isSelected());
                    loadHtml();
                }
            });
            items.add(formItem);

            CheckMenuItem titleItem = new CheckMenuItem(message("Html") + " - " + message("Title"));
            titleItem.setSelected(UserConfig.getBoolean(baseName + "HtmlShowTitle", true));
            titleItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "HtmlShowTitle", titleItem.isSelected());
                    loadHtml();
                }
            });
            items.add(titleItem);

            CheckMenuItem columnNameItem = new CheckMenuItem(message("Html") + " - " + message("ColumnName"));
            columnNameItem.setSelected(UserConfig.getBoolean(baseName + "HtmlShowColumns", true));
            columnNameItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "HtmlShowColumns", columnNameItem.isSelected());
                    loadHtml();
                }
            });
            items.add(columnNameItem);

            CheckMenuItem rowNumberItem = new CheckMenuItem(message("Html") + " - " + message("RowNumber"));
            rowNumberItem.setSelected(UserConfig.getBoolean(baseName + "HtmlShowRowNumber", true));
            rowNumberItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "HtmlShowRowNumber", rowNumberItem.isSelected());
                    loadHtml();
                }
            });
            items.add(rowNumberItem);

            CheckMenuItem textFormItem = new CheckMenuItem(message("Texts") + " - " + message("Form"));
            textFormItem.setSelected(UserConfig.getBoolean(baseName + "TextsShowForm", false));
            textFormItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "TextsShowForm", textFormItem.isSelected());
                    loadTexts();
                }
            });
            items.add(textFormItem);

            CheckMenuItem textTitleItem = new CheckMenuItem(message("Texts") + " - " + message("Title"));
            textTitleItem.setSelected(UserConfig.getBoolean(baseName + "TextsShowTitle", true));
            textTitleItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "TextsShowTitle", textTitleItem.isSelected());
                    loadTexts();
                }
            });
            items.add(textTitleItem);

            CheckMenuItem textColumnItem = new CheckMenuItem(message("Texts") + " - " + message("ColumnName"));
            textColumnItem.setSelected(UserConfig.getBoolean(baseName + "TextsShowColumns", true));
            textColumnItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "TextsShowColumns", textColumnItem.isSelected());
                    loadTexts();
                }
            });
            items.add(textColumnItem);

            CheckMenuItem textRowNumberItem = new CheckMenuItem(message("RowNumber"));
            textRowNumberItem.setSelected(UserConfig.getBoolean(baseName + "TextsShowRowNumber", true));
            textRowNumberItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "TextsShowRowNumber", textRowNumberItem.isSelected());
                    loadTexts();
                }
            });
            items.add(textRowNumberItem);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        get
     */
    public String getDelimiterName() {
        return delimiterName;
    }

}
