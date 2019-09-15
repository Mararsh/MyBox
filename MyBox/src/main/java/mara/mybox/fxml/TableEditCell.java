package mara.mybox.fxml;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2019-3-15 14:24:30
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableEditCell<T, P> extends TableCell<T, P>
        implements Callback<TableColumn<T, P>, TableCell<T, P>> {

    @Override
    public TableCell<T, P> call(TableColumn<T, P> param) {
        TableCell<T, P> cell = new TableCell<T, P>() {
            private final TextField textField = new TextField();

            @Override
            public void startEdit() {
                logger.debug("here");
                super.startEdit();
                if (isEditing()) {
                    logger.debug("Inside is editing  ");

                    textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue arg0,
                                Boolean oldv, Boolean newv) {
                            logger.debug("here");
                            if (newv) {
                                logger.debug("here");
                                commitEdit(setString(textField.getText()));
                            }
                        }
                    });

                }
            }

//            @Override
//            public void cancelEdit() {
//                logger.debug("here");
//                super.cancelEdit();
//                setText(getString());
//                setGraphic(null);
//            }
//            @Override
//            public void updateItem(P item, boolean empty) {
//                super.updateItem(item, empty);
//                logger.debug("here");
//                if (empty) {
//                    setText(null);
//                    setGraphic(null);
//                } else {
//                    if (isEditing()) {
//                        if (textField != null) {
//                            textField.setText(getString());
//                        }
//                        setText(null);
//                        setGraphic(textField);
//                    } else {
//                        setText(getString());
//                        setGraphic(null);
//                    }
//                }
//            }
//
//            private void createTextField() {
//                textField = new TextField(getString());
//                textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
//                textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
//                    @Override
//                    public void changed(ObservableValue arg0,
//                            Boolean oldv, Boolean newv) {
//                        logger.debug("here");
//                        if (newv) {
//                            logger.debug("here");
//                            commitEdit(setString(textField.getText()));
//                        }
//                    }
//                });
//            }
//
            protected P setString(String v) {
                P p = (P) v;
                return p;
            }
//
//            protected String getString() {
//                if (getItem() == null) {
//                    return "";
//                } else {
//                    return (String) getItem();
//                }
//            }
        };

        return cell;
    }
}
