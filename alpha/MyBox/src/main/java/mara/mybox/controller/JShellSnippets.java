package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import mara.mybox.data.JShellSnippet;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-3-4
 * @License Apache License Version 2.0
 */
public class JShellSnippets extends BaseTablePagesController<JShellSnippet> {

    protected ControlDataJShell jShellController;

    @FXML
    protected Button deleteSnippetsButton;
    @FXML
    protected TableColumn<JShellSnippet, String> sidColumn, typeColumn, subTypeColumn,
            nameColumn, statusColumn, valueColumn, sourceColumn, some1Column, some2Column;
    @FXML
    protected CheckBox variablesCheck, declarationsCheck, statementsCheck, methodsCheck,
            importsCheck, expressionsCheck, errorsCheck;

    @Override
    public void initControls() {
        try {
            super.initControls();

            variablesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    refreshSnippets();
                }
            });
            declarationsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    refreshSnippets();
                }
            });
            statementsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    refreshSnippets();
                }
            });
            methodsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    refreshSnippets();
                }
            });
            importsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    refreshSnippets();
                }
            });
            expressionsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    refreshSnippets();
                }
            });
            errorsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    refreshSnippets();
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void initColumns() {
        try {
            super.initColumns();

            sidColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
            subTypeColumn.setCellValueFactory(new PropertyValueFactory<>("subType"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
            sourceColumn.setCellValueFactory(new PropertyValueFactory<>("source"));
            some1Column.setCellValueFactory(new PropertyValueFactory<>("some1"));
            some2Column.setCellValueFactory(new PropertyValueFactory<>("some2"));

            checkButtons();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();
        boolean none = isNoneSelected();
        deleteSnippetsButton.setDisable(none);
    }

    protected void setParameters(ControlDataJShell jShellController) {
        this.jShellController = jShellController;
    }

    @FXML
    public void resetAction() {
        jShellController.resetJShell();
    }

    public JShell jShell() {
        return jShellController.jShell;
    }

    @FXML
    protected synchronized void refreshSnippets() {
        tableData.clear();
        JShell jShell = jShell();
        if (jShell == null) {
            return;
        }
        for (Snippet snippet : jShell.snippets().toList()) {
            try {
                switch (snippet.kind()) {
                    case VAR:
                        if (variablesCheck.isSelected()) {
                            tableData.add(new JShellSnippet(jShell, snippet));
                        }
                        break;
                    case TYPE_DECL:
                        if (declarationsCheck.isSelected()) {
                            tableData.add(new JShellSnippet(jShell, snippet));
                        }
                        break;
                    case STATEMENT:
                        if (statementsCheck.isSelected()) {
                            tableData.add(new JShellSnippet(jShell, snippet));
                        }
                        break;
                    case METHOD:
                        if (methodsCheck.isSelected()) {
                            tableData.add(new JShellSnippet(jShell, snippet));
                        }
                        break;
                    case IMPORT:
                        if (importsCheck.isSelected()) {
                            tableData.add(new JShellSnippet(jShell, snippet));
                        }
                        break;
                    case EXPRESSION:
                        if (expressionsCheck.isSelected()) {
                            tableData.add(new JShellSnippet(jShell, snippet));
                        }
                        break;
                    default:
                        if (errorsCheck.isSelected()) {
                            tableData.add(new JShellSnippet(jShell, snippet));
                        }
                        break;
                }
            } catch (Exception e) {
//                output(HtmlWriteTools.stringToHtml(e.toString()));
            }
        }
    }

    @FXML
    protected void deleteSnippets() {
        List<JShellSnippet> selected = new ArrayList<>();
        selected.addAll(selectedItems());
        if (selected.isEmpty()) {
            return;
        }
        for (JShellSnippet snippet : selected) {
            jShell().drop(snippet.getSnippet());
        }
        refreshSnippets();
    }

    @FXML
    protected void clearSnippets() {
        for (JShellSnippet snippet : tableData) {
            jShell().drop(snippet.getSnippet());
        }
        refreshSnippets();
    }

}
