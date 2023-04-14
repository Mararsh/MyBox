package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import mara.mybox.tools.ConfigTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-12-27
 * @License Apache License Version 2.0
 */
public class MyBoxLanguagesController extends BaseController {

    protected ObservableList<LanguageItem> tableData;
    protected String langName;
    protected ChangeListener<Boolean> getListener;
    protected boolean changed;

    @FXML
    protected ListView<String> listView;
    @FXML
    protected TableView<LanguageItem> tableView;
    @FXML
    protected TableColumn<LanguageItem, String> keyColumn, englishColumn, chineseColumn, valueColumn;
    @FXML
    protected Label langLabel;
    @FXML
    protected Button useButton;

    public MyBoxLanguagesController() {
        baseTitle = message("ManageLanguages");
        TipsLabelKey = "MyBoxLanguagesTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initTableView();
            initListView();

            saveButton.disableProperty().bind(Bindings.isEmpty(tableData));
            copyButton.disableProperty().bind(Bindings.isNull(tableView.getSelectionModel().selectedItemProperty()));

            changed = false;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initListView() {
        try {
            listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    checkListSelected();
                }
            });
            listView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    checkListSelected();
                }
            });

            checkListSelected();
            loadList();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public class LanguageCell extends TableAutoCommitCell {

        private ChangeListener<Boolean> getListener;

        public LanguageCell() {
            super(new DefaultStringConverter());
        }

        protected void setCellValue(int rowIndex, String value) {
            if (isSettingValues || rowIndex < 0 || rowIndex >= tableData.size()) {
                return;
            }
            LanguageItem item = tableData.get(rowIndex);
            String currentValue = item.getValue();
            if ((currentValue == null && value == null)
                    || (currentValue != null && currentValue.equals(value))) {
                return;
            }
            item.setValue(value);
            tableData.set(rowIndex, item);
        }

        @Override
        public void editCell() {
            LanguageItem item = tableData.get(editingRow);
            String en = item.getEnglish();
            String value = item.getValue();
            if (value != null && value.contains("\n") || en != null && en.contains("\n")) {
                MyBoxLanguageInputController inputController
                        = MyBoxLanguageInputController.open((MyBoxLanguagesController) myController, item);
                getListener = new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        String value = inputController.getInput();
                        inputController.getNotify().removeListener(getListener);
                        getListener = null;
                        setCellValue(editingRow, value);
                        inputController.closeStage();
                    }
                };
                inputController.getNotify().addListener(getListener);
            } else {
                super.editCell();
            }
        }

    }

    public void initTableView() {
        try {
            tableData = FXCollections.observableArrayList();

            keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
            englishColumn.setCellValueFactory(new PropertyValueFactory<>("english"));
            chineseColumn.setCellValueFactory(new PropertyValueFactory<>("chinese"));
            valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
            valueColumn.setCellFactory(new Callback<TableColumn<LanguageItem, String>, TableCell<LanguageItem, String>>() {
                @Override
                public TableCell<LanguageItem, String> call(TableColumn<LanguageItem, String> param) {
                    return new LanguageCell();
                }
            });
            valueColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<LanguageItem, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<LanguageItem, String> t) {
                    if (t == null) {
                        return;
                    }
                    LanguageItem row = t.getRowValue();
                    if (row == null) {
                        return;
                    }
                    String v = t.getNewValue();
                    String o = row.getValue();
                    if (v == null && o == null
                            || v != null && v.equals(o)) {
                        return;
                    }
                    row.setValue(v);
                    tableChanged(true);
                }
            });
            valueColumn.setEditable(true);
            valueColumn.getStyleClass().add("editable-column");

            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getButton() == MouseButton.SECONDARY) {
                        popCopyMenu(event);
                    }
                }
            });

            tableView.setItems(tableData);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean controlAltE() {
        copyEnglish();
        return true;
    }

    protected void checkListSelected() {
        if (isSettingValues) {
            return;
        }
        String selected = listView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            deleteButton.setDisable(true);
            useButton.setDisable(true);
        } else {
            deleteButton.setDisable(false);
            useButton.setDisable(false);
            langName = selected;
            langLabel.setText(langName);
            loadLanguage(langName);
        }
    }

    public void loadList() {
        try {
            isSettingValues = true;
            listView.getItems().clear();
            listView.getItems().addAll(Languages.userLanguages());
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void loadLanguage(String name) {
        if (task != null) {
            task.cancel();
        }
        tableData.clear();
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    error = null;
                    Map<String, String> interfaceItems = null;
                    File interfaceFile = Languages.interfaceLanguageFile(name);
                    if (interfaceFile != null && interfaceFile.exists()) {
                        interfaceItems = ConfigTools.readValues(interfaceFile);
                    }
                    Enumeration<String> interfaceKeys = Languages.BundleEn.getKeys();
                    while (interfaceKeys.hasMoreElements()) {
                        String key = interfaceKeys.nextElement();
                        LanguageItem item = new LanguageItem(key,
                                Languages.BundleEn.getString(key), Languages.BundleZhCN.getString(key));
                        if (interfaceItems != null) {
                            item.setValue(interfaceItems.get(key));
                        }
                        tableData.add(item);
                    }

                } catch (Exception e) {
                    error = e.toString();
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (error == null) {
                    tableView.refresh();
                    tableChanged(name == null);
                } else {
                    popError(error);
                }
            }
        };
        start(task);
    }

    @FXML
    @Override
    public void addAction() {
        if (changed) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getTitle());
            alert.setHeaderText(getTitle());
            alert.setContentText(Languages.message("NeedSaveBeforeAction"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(Languages.message("Save"));
            ButtonType buttonNotSave = new ButtonType(Languages.message("NotSave"));
            ButtonType buttonCancel = new ButtonType(Languages.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result == null || !result.isPresent()) {
                return;
            }
            if (result.get() == buttonSave) {
                saveAction();
                return;
            } else if (result.get() == buttonCancel) {
                return;
            }
        }
        String name = PopTools.askValue(getTitle(), message("InputLangaugeName"), "", null);
        if (name == null) {
            return;
        }
        langName = name.trim();
        langLabel.setText(langName);
        loadLanguage(null);
    }

    @FXML
    @Override
    public void deleteAction() {
        List<String> selected = listView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        if (!PopTools.askSure(getTitle(), Languages.message("SureDelete"))) {
            return;
        }
        String lang = Languages.getLanguage();
        for (String name : selected) {
            File interfaceFile = Languages.interfaceLanguageFile(name);
            FileDeleteTools.delete(interfaceFile);
            if (name.equals(lang)) {
                UserConfig.deleteValue("language");
            }
        }
        isSettingValues = true;
        listView.getItems().removeAll(selected);
        isSettingValues = false;
        langName = null;
        langLabel.setText("");
        tableData.clear();
        checkListSelected();
        Languages.refreshBundle();
        popSuccessful();

    }

    @FXML
    public void copyEnglish() {
        List<LanguageItem> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        for (LanguageItem item : selected) {
            item.setValue(item.getEnglish());
        }
        tableView.refresh();
    }

    @FXML
    public void copyChinese() {
        List<LanguageItem> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        for (LanguageItem item : selected) {
            item.setValue(item.getChinese());
        }
        tableView.refresh();
    }

    protected void tableChanged(boolean changed) {
        this.changed = changed;
        setTitle(baseTitle + " - " + langName + (changed ? "*" : ""));
        langLabel.setText(langName + (changed ? "*" : ""));
    }

    @FXML
    @Override
    public void saveAction() {
        try {
            if (langName == null) {
                return;
            }
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>(this) {

                    @Override
                    protected boolean handle() {
                        try {
                            error = null;
                            Map<String, String> interfaceItems = new HashMap();
                            File interfaceFile = Languages.interfaceLanguageFile(langName);
                            for (LanguageItem item : tableView.getItems()) {
                                String value = item.getValue();
                                if (value != null && !value.isBlank()) {
                                    interfaceItems.put(item.getKey(), value);
                                }
                            }
                            ConfigTools.writeValues(interfaceFile, interfaceItems);

                        } catch (Exception e) {
                            error = e.toString();
                        }
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        if (error == null) {
                            if (!listView.getItems().contains(langName)) {
                                listView.getItems().add(0, langName);
                            }
                            tableChanged(false);
                            popSuccessful();
                        } else {
                            popError(error);
                        }
                    }
                };
                start(task);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void useAction() {
        String name = listView.getSelectionModel().getSelectedItem();
        if (name == null) {
            return;
        }
        Languages.setLanguage(name);
        refreshInterfaceAndFile();
        Platform.runLater(() -> {
            PopTools.alertInformation(null, message("CurrentLanguage") + ": " + name);
        });
    }

    @FXML
    public void popCopyMenu(MouseEvent event) {
        if (tableView.getSelectionModel().getSelectedItem() == null) {
            return;
        }
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;
        menu = new MenuItem(Languages.message("CopyEnglish"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            copyEnglish();
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("CopyChinese"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            copyChinese();
        });
        items.add(menu);

        popEventMenu(event, items);
    }

    @FXML
    public void openPath() {
        browseURI(AppVariables.MyBoxLanguagesPath.toURI());
    }

    protected class LanguageItem {

        protected String key, english, chinese, value;

        public LanguageItem(String key, String english, String chinese) {
            this.key = key;
            this.english = english;
            this.chinese = chinese;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getEnglish() {
            return english;
        }

        public void setEnglish(String english) {
            this.english = english;
        }

        public String getChinese() {
            return chinese;
        }

        public void setChinese(String chinese) {
            this.chinese = chinese;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }

}
