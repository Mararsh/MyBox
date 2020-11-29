package mara.mybox.controller;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.ConfigTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import thridparty.TableAutoCommitCell;

/**
 * @Author Mara
 * @CreateDate 2019-12-27
 * @License Apache License Version 2.0
 */
public class MyBoxLanguagesController extends BaseController {

    protected ObservableList<LanguageItem> tableData;
    protected String langName;

    @FXML
    protected ListView<String> listView;
    @FXML
    protected TableView<LanguageItem> tableView;
    @FXML
    protected TableColumn<LanguageItem, String> keyColumn, englishColumn, valueColumn;
    @FXML
    protected Label langLabel;
    @FXML
    protected Button useButton, copyEnglishButton;

    public MyBoxLanguagesController() {
        baseTitle = AppVariables.message("ManageLanguages");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            tableData = FXCollections.observableArrayList();

            keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
            englishColumn.setCellValueFactory(new PropertyValueFactory<>("english"));
            valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
            valueColumn.setCellFactory(TableAutoCommitCell.forTableColumn());
            valueColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<LanguageItem, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<LanguageItem, String> t) {
                    if (t == null) {
                        return;
                    }
                    LanguageItem row = t.getRowValue();
                    row.setValue(t.getNewValue());
                }
            });
            valueColumn.getStyleClass().add("editable-column");

            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    checkTableSelected();
                }
            });
            checkTableSelected();

            tableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() > 1) {
                        playAction();
                    }
                }
            });

            tableView.setItems(tableData);

            listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    checkListSelected();
                }
            });
            checkListSelected();

            loadList();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void controlHandler(KeyEvent event) {
        if (event.isControlDown() && event.getCode() != null) {
            switch (event.getCode()) {
                case E:
                    copyEnglish();
                    return;
            }
        }
        super.controlHandler(event);
    }

    @Override
    public void altHandler(KeyEvent event) {
        if (event.isAltDown() && event.getCode() != null) {
            switch (event.getCode()) {
                case E:
                    copyEnglish();
                    return;
            }
        }
        super.altHandler(event);
    }

    protected void checkListSelected() {
        if (isSettingValues) {
            return;
        }
        String selected = listView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            deleteButton.setDisable(true);
            useButton.setDisable(true);
            tableData.clear();
        } else {
            deleteButton.setDisable(false);
            useButton.setDisable(false);
            langName = selected;
            langLabel.setText(langName);
            String fname = AppVariables.MyBoxLanguagesPath + File.separator + langName;
            loadLanguage(new File(fname));
        }
    }

    protected void checkTableSelected() {
        if (isSettingValues) {
            return;
        }
        LanguageItem selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            copyEnglishButton.setDisable(true);
        } else {
            copyEnglishButton.setDisable(false);
        }
    }

    public void loadList() {
        try {
            isSettingValues = true;
            listView.getItems().clear();
            listView.getItems().addAll(ConfigTools.languages());
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void loadLanguage(File file) {
        tableData.clear();
        try {
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    @Override
                    protected boolean handle() {
                        try {
                            error = null;
                            Map<String, String> items = null;
                            if (file != null) {
                                items = ConfigTools.readValues(file);
                            }
                            Enumeration<String> keys = CommonValues.BundleEn.getKeys();
                            while (keys.hasMoreElements()) {
                                String key = keys.nextElement();
                                LanguageItem item = new LanguageItem(key, CommonValues.BundleEn.getString(key));
                                if (items != null) {
                                    item.setValue(items.get(key));
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
                        } else {
                            popError(error);
                        }
                    }
                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @FXML
    @Override
    public void addAction(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle(message("ManageLanguages"));
        dialog.setHeaderText(message("InputLangaugeName"));
        dialog.setContentText("");
        dialog.getEditor().setPrefWidth(200);
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();

        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent() || result.get().trim().isBlank()) {
            return;
        }
        langName = result.get().trim();
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
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getMyStage().getTitle());
        alert.setContentText(AppVariables.message("SureDelete"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ButtonType buttonSure = new ButtonType(AppVariables.message("Sure"));
        ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
        alert.getButtonTypes().setAll(buttonSure, buttonCancel);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != buttonSure) {
            return;
        }
        for (String name : selected) {
            File file = new File(AppVariables.MyBoxLanguagesPath + File.separator + name);
            FileTools.delete(file);
        }
        isSettingValues = true;
        listView.getItems().removeAll(selected);
        isSettingValues = false;
        langName = null;
        langLabel.setText("");
        checkListSelected();
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
                task = new SingletonTask<Void>() {

                    @Override
                    protected boolean handle() {
                        try {
                            error = null;
                            Map<String, String> items = new HashMap();
                            for (LanguageItem item : tableData) {
                                if (item.getValue() != null && !item.getValue().isBlank()) {
                                    items.put(item.getKey(), item.getValue());
                                }
                            }
                            String fname = AppVariables.MyBoxLanguagesPath + File.separator + langName;
                            File file = new File(fname);
                            ConfigTools.writeValues(file, items);
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
                            popSuccessful();
                        } else {
                            popError(error);
                        }
                    }
                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
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
        AppVariables.setLanguage(name);
        refresh();
    }

    protected class LanguageItem {

        protected String key, english, value;

        public LanguageItem(String key, String english) {
            this.key = key;
            this.english = english;
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

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }

}
