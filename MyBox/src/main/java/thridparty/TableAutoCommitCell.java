package thridparty;

import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import static mara.mybox.fxml.FxmlControl.badStyle;

/**
 * @Author Ogmios
 * https://stackoverflow.com/questions/24694616/how-to-enable-commit-on-focuslost-for-tableview-treetableview
 *
 * Changed by Mara
 *
 * This is an old issue since 2011
 * https://bugs.openjdk.java.net/browse/JDK-8089514
 * https://bugs.openjdk.java.net/browse/JDK-8089311
 */
public class TableAutoCommitCell<S, T> extends TextFieldTableCell<S, T> {

    protected TextField txtFldRef;
    protected boolean isEdit;

    public TableAutoCommitCell() {
        this(null);
    }

    public TableAutoCommitCell(final StringConverter<T> conv) {
        super(conv);
    }

    public static <S> Callback<TableColumn<S, String>, TableCell<S, String>> forTableColumn() {
        return forTableColumn(new DefaultStringConverter());
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(final StringConverter<T> conv) {
        return list -> new TableAutoCommitCell<>(conv);
    }

    @Override
    public void startEdit() {
        super.startEdit();
        isEdit = true;
        if (updTxtFldRef()) {
            txtFldRef.focusedProperty().addListener(this::onFocusChg);
            txtFldRef.setOnKeyPressed(this::onKeyPrs);
        }
    }

    /**
     * @return whether {@link #txtFldRef} has been changed
     */
    protected boolean updTxtFldRef() {
        final Node g = getGraphic();
        final boolean isUpd = g != null && txtFldRef != g;
        if (isUpd) {
            txtFldRef = g instanceof TextField ? (TextField) g : null;
        }
        return isUpd;
    }

    @Override
    public void commitEdit(final T valNew) {
        if (isEditing()) {
            super.commitEdit(valNew);
        } else {
            final TableView<S> tbl = getTableView();
            if (tbl != null) {
                final TablePosition<S, T> pos = new TablePosition<>(tbl, getTableRow().getIndex(), getTableColumn()); // instead of tbl.getEditingCell()
                final CellEditEvent<S, T> ev = new CellEditEvent<>(tbl, pos, TableColumn.editCommitEvent(), valNew);
                Event.fireEvent(getTableColumn(), ev);
            }
            updateItem(valNew, false);
            if (tbl != null) {
                tbl.edit(-1, null);
            }
            // TODO ControlUtils.requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(tbl);
        }
    }

    public void onFocusChg(final ObservableValue<? extends Boolean> obs, final boolean v0, final boolean v1) {
        try {
            if (isEdit && !v1) {
                commitEdit(getConverter().fromString(txtFldRef.getText()));
                txtFldRef.setStyle(null);
            }
        } catch (Exception e) {
            txtFldRef.setStyle(badStyle);
        }
    }

    protected void onKeyPrs(final KeyEvent e) {
        switch (e.getCode()) {
            case ESCAPE:
                isEdit = false;
                cancelEdit(); // see CellUtils#createTextField(...)
                e.consume();
                break;
            case TAB:
                if (e.isShiftDown()) {
                    getTableView().getSelectionModel().selectPrevious();
                } else {
                    getTableView().getSelectionModel().selectNext();
                }
                e.consume();
                break;
            case UP:
                getTableView().getSelectionModel().selectAboveCell();
                e.consume();
                break;
            case DOWN:
                getTableView().getSelectionModel().selectBelowCell();
                e.consume();
                break;
            default:
                break;
        }
    }
}

/**
 * @Author Mara
 * @CreateDate 2019-12-10
 * @License Apache License Version 2.0
 */
//public class TableEditCell<T, P> extends TextFieldTableCell<T, P> {
//
//    protected TextField textField;
//
//    public TableEditCell() {
//    }
//
//    @Override
//    public void startEdit() {
//        if (!isEmpty()) {
//            super.startEdit();
//            createTextField();
//            setText(null);
//            setGraphic(textField);
//            textField.selectAll();
//        }
//    }
//
//    @Override
//    public void cancelEdit() {
//        super.cancelEdit();
//
//        setText(getString());
//        setGraphic(null);
//    }
//
//    @Override
//    public void updateItem(P item, boolean empty) {
//        super.updateItem(item, empty);
//
//        if (empty) {
//            setText(null);
//            setGraphic(null);
//        } else {
//            if (isEditing()) {
//                if (textField != null) {
//                    textField.setText(getString());
//                }
//                setText(null);
//                setGraphic(textField);
//            } else {
//                setText(getString());
//                setGraphic(null);
//            }
//        }
//    }
//
//    private void createTextField() {
//        textField = new TextField(getString(getItem()));
//        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
//        textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
//            @Override
//            public void changed(ObservableValue<? extends Boolean> arg0,
//                    Boolean arg1, Boolean arg2) {
//                if (!arg2) {
//                    commitEdit(getConverter().fromString(textField.getText()));
//                }
//            }
//        });
//    }
//
//    private String getString() {
//        return getItem() == null ? "" : getConverter().toString(getItem());
//    }
//
//    private String getString(P v) {
//        return getConverter().toString(v);
//    }
//
//    private P fromString(String v) {
//        return getConverter().fromString(v);
//    }
//}
