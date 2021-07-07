package mara.mybox.controller;

import java.util.Date;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.stage.Modality;
import mara.mybox.db.data.TextClipboard;
import mara.mybox.db.table.TableTextClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.TableDateCell;
import mara.mybox.fxml.TableNumberCell;
import mara.mybox.fxml.TableTextCell;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2021-7-4
 * @License Apache License Version 2.0
 */
public class ControlTextsClipboard extends BaseDataTableController<TextClipboard> {

    protected String lastSystemClip;
    protected Clipboard clipboard;
    protected TextInputControl textInput;

    @FXML
    protected TableColumn<TextClipboard, String> textColumn;
    @FXML
    protected TableColumn<TextClipboard, Date> timeColumn;
    @FXML
    protected TableColumn<TextClipboard, Long> lengthColumn;
    @FXML
    protected Button useButton;

    public ControlTextsClipboard() {
        baseTitle = message("TextInMyBoxClipboard");
    }

    @Override
    public void setTableDefinition() {
        tableDefinition = new TableTextClipboard();
    }

    @Override
    protected void initColumns() {
        try {
            textColumn.setCellValueFactory(new PropertyValueFactory<>("text"));
            textColumn.setCellFactory(new TableTextCell());

            lengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));
            lengthColumn.setCellFactory(new TableNumberCell());

            timeColumn.setCellValueFactory(new PropertyValueFactory<>("createTime"));
            timeColumn.setCellFactory(new TableDateCell());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(TextInputControl textInput, boolean use) {
        try {
            this.textInput = textInput;
            useButton.setVisible(use && textInput.isEditable() && !textInput.isDisable());
            clipboard = Clipboard.getSystemClipboard();

            loadTableData();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void pasteAction() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            String clip = clipboard.getString();
            if (clip == null || (lastSystemClip != null && clip.equals(lastSystemClip))) {
                popInformation(message("NoTextInClipboard"));
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    return tableDefinition.insertData(new TextClipboard(clip)) != null;
                }

                @Override
                protected void whenSucceeded() {
                    lastSystemClip = clip;
                    refreshAction();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @Override
    protected void checkSelected() {
        if (isSettingValues) {
            return;
        }
        TextClipboard selected = tableView.getSelectionModel().getSelectedItem();
        deleteButton.setDisable(selected == null);
        useButton.setDisable(selected == null);
        selectedLabel.setText(message("Selected") + ": " + tableView.getSelectionModel().getSelectedIndices().size());
    }

    @Override
    public void itemDoubleClicked() {
        if (useButton.isVisible()) {
            useAction();
        }
    }

    @FXML
    public void useAction() {
        TextClipboard selected = tableView.getSelectionModel().getSelectedItem();
        if (textInput == null || selected == null) {
            return;
        }
        textInput.insertText(textInput.getAnchor(), selected.getText());
    }

}
