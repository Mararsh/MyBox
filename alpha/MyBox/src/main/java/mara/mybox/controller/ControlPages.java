package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.FlowPane;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2025-2-24
 * @License Apache License Version 2.0
 */
public class ControlPages extends BaseController {

    protected long totalSize, currentPage, pagesNumber,
            startRowOfCurrentPage, endRowOfCurrentPage; // 0-based
    protected int pageSize, selectedRows;

    @FXML
    protected Button pagesButton;
    @FXML
    protected Label sizeLabel, pageLabel, selectedLabel;
    @FXML
    protected FlowPane flowPane;

    public void setParameters(BaseController parent) {
        try {
            parentController = parent;
            baseName = parentController.baseName + "_Pages";

            pageSize = UserConfig.getInt(baseName + "PageSize", 50);
            if (pageSize < 1) {
                pageSize = 50;
            }
            reset();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void reset() {
        totalSize = 0;
        startRowOfCurrentPage = 0;
        endRowOfCurrentPage = 0;
        currentPage = 1;
        pagesNumber = 1;
        selectedRows = 0;
    }

    public void updateLabels() {
        try {
            long start = startRowOfCurrentPage + 1;
            long end = endRowOfCurrentPage + 1;
            sizeLabel.setText(message("Rows") + ": "
                    + "[" + start + "-" + end + "]"
                    + (end - start + 1)
                    + (totalSize > 0 ? "/" + totalSize : ""));
            pageLabel.setText(pageSize <= 0 ? ""
                    : (message("Page") + ":" + currentPage + "/" + pagesNumber));
            selectedLabel.setText(selectedRows <= 0 ? ""
                    : (message("Selected") + ":" + selectedRows));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setRightOrientation() {
        try {
            flowPane.getChildren().clear();
            flowPane.getChildren().addAll(sizeLabel, selectedLabel, pageLabel, pagesButton);
            refreshStyle(flowPane);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void hide() {
        thisPane.setVisible(false);
    }

    public void show() {
        thisPane.setVisible(true);
        updateLabels();
    }

    public boolean isVisible() {
        return thisPane.isVisible();
    }

    @FXML
    public void popPagesMemu(Event event) {
        if (UserConfig.getBoolean(baseName + "MenuPopWhenMouseHovering", true)) {
            showPagesMemu(event);
        }
    }

    @FXML
    public void showPagesMemu(Event event) {
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu;

        if (pagesNumber > 1) {
            if (currentPage < pagesNumber) {
                menu = new MenuItem(message("NextPage") + "  ALT+PAGE_DOWN",
                        StyleTools.getIconImageView("iconNext.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    parentController.pageNextAction();
                });
                items.add(menu);
            }

            if (currentPage > 1) {
                menu = new MenuItem(message("PreviousPage") + "  ALT+PAGE_UP",
                        StyleTools.getIconImageView("iconPrevious.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    parentController.pagePreviousAction();
                });
                items.add(menu);
            }

            if (currentPage > 1) {
                menu = new MenuItem(message("FirstPage") + "  ALT+HOME",
                        StyleTools.getIconImageView("iconFirst.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    parentController.pageFirstAction();
                });
                items.add(menu);
            }

            if (currentPage < pagesNumber) {
                menu = new MenuItem(message("LastPage") + "  ALT+END",
                        StyleTools.getIconImageView("iconLast.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    parentController.pageLastAction();
                });
                items.add(menu);
            }

            menu = new MenuItem(message("PageTo") + "...");
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                String value = PopTools.askValue(parentController.getTitle(),
                        null, message("PageTo"), currentPage + "");
                if (value == null || value.isBlank()) {
                    return;
                }
                try {
                    int v = Integer.parseInt(value) - 1;
                    if (v == currentPage) {
                        return;
                    }
                    parentController.goPage(v);
                } catch (Exception e) {
                    popError(e.toString());
                }
            });
            items.add(menu);
        }

        menu = new MenuItem(message("PageSize") + "...");
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            String value = PopTools.askValue(parentController.getTitle(),
                    null, message("PageSize"), pageSize + "");
            if (value == null || value.isBlank()) {
                return;
            }
            try {
                int v = Integer.parseInt(value);
                if (v == pageSize || v <= 0) {
                    return;
                }
                pageSize = v;
                UserConfig.setInt(baseName + "PageSize", pageSize);
                parentController.pageSize(v);
            } catch (Exception e) {
                popError(e.toString());
            }
        });
        items.add(menu);

        menu = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            parentController.refreshAction();
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean(baseName + "PopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean(baseName + "PopWhenMouseHovering", popItem.isSelected());
            }
        });
        items.add(popItem);

        popNodeMenu(pagesButton, items);
    }

    /*
        get/set
     */
    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(long currentPage) {
        this.currentPage = currentPage;
    }

    public long getPagesNumber() {
        return pagesNumber;
    }

    public void setPagesNumber(long pagesNumber) {
        this.pagesNumber = pagesNumber;
    }

    public long getStartRowOfCurrentPage() {
        return startRowOfCurrentPage;
    }

    public void setStartRowOfCurrentPage(long startRowOfCurrentPage) {
        this.startRowOfCurrentPage = startRowOfCurrentPage;
    }

    public long getEndRowOfCurrentPage() {
        return endRowOfCurrentPage;
    }

    public void setEndRowOfCurrentPage(long endRowOfCurrentPage) {
        this.endRowOfCurrentPage = endRowOfCurrentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getSelectedRows() {
        return selectedRows;
    }

    public void setSelectedRows(int selectedRows) {
        this.selectedRows = selectedRows;
    }

}
