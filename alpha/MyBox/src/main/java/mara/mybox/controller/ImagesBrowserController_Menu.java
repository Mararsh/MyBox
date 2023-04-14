package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-8-21
 * @License Apache License Version 2.0
 */
public abstract class ImagesBrowserController_Menu extends ImagesBrowserController_Action {

    protected void popImageMenu(int index, ImageView iView, MouseEvent event) {
        if (iView == null || iView.getImage() == null) {
            return;
        }
        if (index >= tableData.size()) {
            return;
        }

        ImageInformation info = tableData.get(index);

        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;

        menu = new MenuItem(StringTools.menuSuffix(info.getFile().getAbsolutePath()));
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);

        items.add(new SeparatorMenuItem());

        if (displayMode == ImagesBrowserController_Load.DisplayMode.ImagesGrid) {
            menu = new MenuItem(Languages.message("PaneSize"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                paneSize(index);
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("ImageSize"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                imageSize(index);
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("ZoomIn"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                zoomIn(index);
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("ZoomOut"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                zoomOut(index);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());
        }

        if (!info.isIsMultipleFrames()) {

            menu = new MenuItem(Languages.message("RotateLeft"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                rotateImages(index, 270);
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("RotateRight"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                rotateImages(index, 90);
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("TurnOver"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                rotateImages(index, 180);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());
        }

        menu = new MenuItem(Languages.message("View"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            view(index);
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("Information"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            info(index);
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("MetaData"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            meta(index);
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("SelectAll"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            selectAllImages();
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("SelectNone"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            selectNoneImages();
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(Languages.message("Rename"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            rename(index);
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("Delete"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            delete(index);
        });
        items.add(menu);

        popMenu(iView, items, event.getScreenX(), event.getScreenY());

    }

    protected void popTableMenu(MouseEvent event, int index) {
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;

        menu = new MenuItem(Languages.message("View"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            view(index);
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("Information"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            info(index);
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("MetaData"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            meta(index);
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(Languages.message("RotateLeft"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            rotateImages(index, 270);
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("RotateRight"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            rotateImages(index, 90);
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("TurnOver"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            rotateImages(index, 180);
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("SelectAll"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            selectAllImages();
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("SelectNone"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            selectNoneImages();
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(Languages.message("Rename"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            rename(index);
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("Delete"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            delete(index);
        });
        items.add(menu);

        popEventMenu(event, items);

    }

}
