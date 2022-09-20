package mara.mybox.fxml.cell;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.input.MouseEvent;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.TextInputController;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-12-03
 * @License Apache License Version 2.0
 */
public class TableTextAreaCommitCell<S> extends TableCell<S, String> {

    protected BaseController parent;
    protected ChangeListener<Boolean> setListener;
    protected ChangeListener<Boolean> getListener;

    public TableTextAreaCommitCell(BaseController parent) {
        this.parent = parent;
        setContentDisplay(ContentDisplay.TEXT_ONLY);
        this.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                editText();
            }
        });
    }

    protected String getCellValue(int rowIndex) {
        return null;
    }

    protected void setCellValue(int rowIndex, String value) {
    }

    protected String name(int rowIndex) {
        return message("TableRowNumber") + " " + (rowIndex + 1);
    }

    public int rowIndex() {
        TableRow row = getTableRow();
        return row == null ? -1 : row.getIndex();
    }

    public void editText() {
        int row = rowIndex();
        if (row < 0) {
            return;
        }
        TextInputController inputController = TextInputController.open(parent, name(row), getCellValue(row));
        getListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                String value = inputController.getText();
                inputController.getNotify().removeListener(getListener);
                setCellValue(row, value);
                inputController.closeStage();
            }
        };
        inputController.getNotify().addListener(getListener);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            return;
        }
        setText(item);
    }

}
