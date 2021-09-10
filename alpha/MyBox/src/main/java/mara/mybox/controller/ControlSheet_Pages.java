package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-22
 * @License Apache License Version 2.0
 */
public abstract class ControlSheet_Pages extends ControlSheet_Sheet {

    protected void initPagination() {
        try {
            if (pageSelector == null) {
                return;
            }
            pageSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        checkCurrentPage();
                    });

            pageSize = UserConfig.getInt(baseName + "PageSize", 50);
            pageSize = pageSize < 1 ? 50 : pageSize;
            pageSizeSelector.getItems().addAll(Arrays.asList("50", "30", "100", "20", "60", "200", "300",
                    "500", "1000", "2000", "5000", "10000"));
            pageSizeSelector.setValue(pageSize + "");
            pageSizeSelector.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                if (newValue == null) {
                    return;
                }
                try {
                    int v = Integer.parseInt(newValue.trim());
                    if (v <= 0) {
                        pageSizeSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                    } else {
                        pageSize = v;
                        UserConfig.setInt(baseName + "PageSize", pageSize);
                        pageSizeSelector.getEditor().setStyle(null);
                        if (!isSettingValues) {
                            loadPage(currentPage);
                        }
                    }
                } catch (Exception e) {
                    pageSizeSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadPage(int pageNumber) {
        if (sourceFile == null || columns.isEmpty() || totalSize <= 0) {
            makeSheet(null);
            return;
        }
        if (!checkBeforeNextAction()) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            task = new SingletonTask<Void>() {
                String[][] data;

                @Override
                protected boolean handle() {
                    countPagination(pageNumber);
                    data = readPageData();
                    return !isCancelled() && error == null && loadError == null;
                }

                @Override
                protected void whenSucceeded() {
                    makeSheet(data, false);
                    setPagination();
                }

                @Override
                protected void whenFailed() {
                    if (isCancelled()) {
                        return;
                    }
                    if (error != null) {
                        popError(message(error));
                    } else if (loadError != null) {
                        popError(message(loadError));
                    } else {
                        popFailed();
                    }
                }

                @Override
                protected void finalAction() {
                    updateLabel();
                }

            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    protected void countPagination(int pageNumber) {
        if (totalSize <= pageSize) {
            pagesNumber = 1;
        } else {
            pagesNumber = (int) (totalSize % pageSize == 0 ? totalSize / pageSize : (totalSize / pageSize + 1));
        }
        currentPage = pageNumber;
        if (currentPage <= 0) {   // 1-based
            currentPage = 1;
        }
        if (currentPage > pagesNumber) {
            currentPage = pagesNumber;
        }
        currentPageStart = pageSize * (currentPage - 1) + 1; // 1-based
    }

    protected void setPagination() {
        try {
            if (pageSelector == null || (paginationBox != null && !paginationBox.isVisible())) {
                return;
            }
            isSettingValues = true;
            pageSelector.setDisable(false);
            List<String> pages = new ArrayList<>();
            for (int i = Math.max(1, currentPage - 20);
                    i <= Math.min(pagesNumber, currentPage + 20); i++) {
                pages.add(i + "");
            }
            pageSelector.getItems().clear();
            pageSelector.getItems().addAll(pages);
            pageSelector.getSelectionModel().select(currentPage + "");

            pageLabel.setText("/" + pagesNumber);
            if (currentPage > 1) {
                pagePreviousButton.setDisable(false);
                pageFirstButton.setDisable(false);
            } else {
                pagePreviousButton.setDisable(true);
                pageFirstButton.setDisable(true);
            }
            if (currentPage >= pagesNumber) {
                pageNextButton.setDisable(true);
                pageLastButton.setDisable(true);
            } else {
                pageNextButton.setDisable(false);
                pageLastButton.setDisable(false);
            }
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    protected void updateLabel() {
        totalLabel.setText(message("Total") + ":" + totalSize);
    }

    protected boolean checkCurrentPage() {
        if (isSettingValues || pageSelector == null) {
            return false;
        }
        String value = pageSelector.getEditor().getText();
        try {
            int v = Integer.parseInt(value);
            if (v < 0) {
                pageSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                return false;
            } else {
                pageSelector.getEditor().setStyle(null);
                loadPage(v);
                return true;
            }
        } catch (Exception e) {
            pageSelector.getEditor().setStyle(NodeStyleTools.badStyle);
            return false;
        }
    }

    protected String[][] readPageData() {
        return null;
    }

    @FXML
    public void goPage() {
        checkCurrentPage();
    }

    @FXML
    @Override
    public void pageNextAction() {
        loadPage(currentPage + sheetInputs.length / pageSize);
    }

    @FXML
    @Override
    public void pagePreviousAction() {
        loadPage(currentPage - 1);
    }

    @FXML
    @Override
    public void pageFirstAction() {
        loadPage(1);
    }

    @FXML
    @Override
    public void pageLastAction() {
        loadPage(Integer.MAX_VALUE);
    }

}
