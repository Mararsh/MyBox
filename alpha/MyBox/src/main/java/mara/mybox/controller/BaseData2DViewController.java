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
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.TextTools;
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
    protected VBox dataBox;
    @FXML
    protected Button fileMenuButton, verifyButton, optionsButton, delimiterButton,
            viewDataButton, editDataButton;
    @FXML
    protected CheckBox wrapCheck;

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
                    checkFormat();
                }
            });
            checkFormat();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            if (htmlRadio != null) {
                StyleTools.setIconTooltips(htmlRadio, "iconHtml.png", message("HtmlReadOnly"));
            }
            if (tableRadio != null) {
                StyleTools.setIconTooltips(tableRadio, "iconGrid.png",
                        editDataButton != null ? message("Table") : message("TableEdit"));
            }
            if (textsRadio != null) {
                StyleTools.setIconTooltips(textsRadio, "iconTxt.png", message("CsvReadOnly"));
            }
            if (csvRadio != null) {
                StyleTools.setIconTooltips(csvRadio, "iconCSV.png", message("CsvEdit"));
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

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
        checkFormat();
    }

    public void checkFormat() {
        try {
            toolbar.getChildren().remove(fileMenuButton);
            buttonsPane.getChildren().clear();
            pageBox.getChildren().clear();
            webViewController.loadContents("");
            textsArea.clear();
            tableView.setItems(null);
            if (csvRadio != null) {
                csvArea.clear();
                columnsLabel.setText("");
            }
            if (data2D == null) {
                return;
            }
            if (data2D.isDataFile()) {
                toolbar.getChildren().add(toolbar.getChildren().indexOf(htmlRadio), fileMenuButton);
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

            refreshStyle(pageBox);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void showHtml() {
        try {
            buttonsPane.getChildren().addAll(optionsButton, menuButton, viewDataButton);
            if (editDataButton != null) {
                buttonsPane.getChildren().add(editDataButton);
            }
            pageBox.getChildren().add(htmlBox);
            VBox.setVgrow(htmlBox, Priority.ALWAYS);

            loadHtml();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadHtml() {
        try {
            String html = Data2DTools.dataToHtml(data2D, styleFilter,
                    UserConfig.getBoolean(baseName + "HtmlShowForm", false),
                    UserConfig.getBoolean(baseName + "HtmlShowColumns", true),
                    UserConfig.getBoolean(baseName + "HtmlShowRowNumber", true),
                    UserConfig.getBoolean(baseName + "HtmlShowTitle", true));

            webViewController.loadContents(html);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void showTexts() {
        try {
            buttonsPane.getChildren().addAll(wrapCheck, delimiterButton,
                    optionsButton, menuButton, viewDataButton);
            if (editDataButton != null) {
                buttonsPane.getChildren().add(editDataButton);
            }
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

    public void loadTexts() {
        try {
            String texts = Data2DTools.dataToTexts(data2D, delimiterName,
                    UserConfig.getBoolean(baseName + "TextsShowForm", false),
                    UserConfig.getBoolean(baseName + "TextsShowColumns", true),
                    UserConfig.getBoolean(baseName + "TextsShowRowNumber", true),
                    UserConfig.getBoolean(baseName + "TextsShowTitle", true));

            textsArea.setText(texts);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void showTable() {
        try {
            showTableButtons();
            pageBox.getChildren().add(tableBox);
            VBox.setVgrow(tableBox, Priority.ALWAYS);

            super.makeColumns();
            isSettingValues = true;
            tableData.setAll(data2D.getPageData());
            tableView.setItems(tableData);
            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void showTableButtons() {
        buttonsPane.getChildren().setAll(menuButton, viewDataButton, editDataButton);
    }

    public void showCsv() {
        try {
            if (csvRadio == null || !csvRadio.isSelected()) {
                return;
            }
            buttonsPane.getChildren().addAll(wrapCheck, delimiterButton, menuButton, viewDataButton);
            pageBox.getChildren().add(csvBox);
            VBox.setVgrow(csvBox, Priority.ALWAYS);

            delimiterName = UserConfig.getString(baseName + "CsvDelimiter", ",");
            isSettingValues = true;
            wrapCheck.setSelected(UserConfig.getBoolean(baseName + "CsvWrap", true));
            csvArea.setWrapText(wrapCheck.isSelected());
            columnsLabel.setWrapText(wrapCheck.isSelected());
            isSettingValues = false;

            loadCsv();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadCsv() {
        try {
            if (!data2D.isValid()) {
                csvArea.setText("");
                columnsLabel.setText("");
                return;
            }
            String texts = data2D.encodeCSV(null, delimiterName, false, false, false);
            csvArea.setText(texts);
            String label = "";
            String delimiter = TextTools.delimiterValue(delimiterName);
            for (String name : data2D.columnNames()) {
                if (!label.isEmpty()) {
                    label += delimiter;
                }
                label += name;
            }
            columnsLabel.setText(label);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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

    @FXML
    public void popOptionsMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "OptionsPopWhenMouseHovering", true)) {
            showOptionsMenu(event);
        }
    }

    @FXML
    public void showOptionsMenu(Event mevent) {
        try {
            List<MenuItem> items = null;

            if (htmlRadio.isSelected()) {
                items = htmlOptions(mevent);

            } else if (tableRadio.isSelected()) {
                items = tableOptions(mevent);

            } else if (textsRadio.isSelected()) {
                items = textsOptions(mevent);

            } else if (csvRadio != null && csvRadio.isSelected()) {
                items = csvOptions(mevent);

            }

            if (items == null || items.isEmpty()) {
                return;
            }

            items.add(new SeparatorMenuItem());

            CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"),
                    StyleTools.getIconImageView("iconPop.png"));
            popItem.setSelected(UserConfig.getBoolean(baseName + "OptionsPopWhenMouseHovering", true));
            popItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "OptionsPopWhenMouseHovering", popItem.isSelected());
                }
            });
            items.add(popItem);

            popEventMenu(mevent, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public List<MenuItem> htmlOptions(Event mevent) {
        try {
            List<MenuItem> items = new ArrayList<>();

            CheckMenuItem formItem = new CheckMenuItem(message("Form"));
            formItem.setSelected(UserConfig.getBoolean(baseName + "HtmlShowForm", false));
            formItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "HtmlShowForm", formItem.isSelected());
                    loadHtml();
                }
            });
            items.add(formItem);

            CheckMenuItem titleItem = new CheckMenuItem(message("Title"));
            titleItem.setSelected(UserConfig.getBoolean(baseName + "HtmlShowTitle", true));
            titleItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "HtmlShowTitle", titleItem.isSelected());
                    loadHtml();
                }
            });
            items.add(titleItem);

            CheckMenuItem columnNameItem = new CheckMenuItem(message("ColumnName"));
            columnNameItem.setSelected(UserConfig.getBoolean(baseName + "HtmlShowColumns", true));
            columnNameItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "HtmlShowColumns", columnNameItem.isSelected());
                    loadHtml();
                }
            });
            items.add(columnNameItem);

            CheckMenuItem rowNumberItem = new CheckMenuItem(message("RowNumber"));
            rowNumberItem.setSelected(UserConfig.getBoolean(baseName + "HtmlShowRowNumber", true));
            rowNumberItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "HtmlShowRowNumber", rowNumberItem.isSelected());
                    loadHtml();
                }
            });
            items.add(rowNumberItem);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<MenuItem> tableOptions(Event mevent) {
        try {
            List<MenuItem> items = new ArrayList<>();

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<MenuItem> textsOptions(Event mevent) {
        try {
            List<MenuItem> items = new ArrayList<>();

            CheckMenuItem formItem = new CheckMenuItem(message("Form"));
            formItem.setSelected(UserConfig.getBoolean(baseName + "TextsShowForm", false));
            formItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "TextsShowForm", formItem.isSelected());
                    loadTexts();
                }
            });
            items.add(formItem);

            CheckMenuItem titleItem = new CheckMenuItem(message("Title"));
            titleItem.setSelected(UserConfig.getBoolean(baseName + "TextsShowTitle", true));
            titleItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "TextsShowTitle", titleItem.isSelected());
                    loadTexts();
                }
            });
            items.add(titleItem);

            CheckMenuItem columnNameItem = new CheckMenuItem(message("ColumnName"));
            columnNameItem.setSelected(UserConfig.getBoolean(baseName + "TextsShowColumns", true));
            columnNameItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "TextsShowColumns", columnNameItem.isSelected());
                    loadTexts();
                }
            });
            items.add(columnNameItem);

            CheckMenuItem rowNumberItem = new CheckMenuItem(message("RowNumber"));
            rowNumberItem.setSelected(UserConfig.getBoolean(baseName + "TextsShowRowNumber", true));
            rowNumberItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "TextsShowRowNumber", rowNumberItem.isSelected());
                    loadTexts();
                }
            });
            items.add(rowNumberItem);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<MenuItem> csvOptions(Event mevent) {
        return null;
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

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
