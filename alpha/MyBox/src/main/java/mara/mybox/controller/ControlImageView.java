package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-9-18
 * @License Apache License Version 2.0
 */
public class ControlImageView extends BaseImageController {

    protected boolean sizeFixed;

    @FXML
    protected Button fileMenuButton;

    @Override
    public void initControls() {
        try {
            super.initControls();
            reset();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void reset() {
        try {
            image = null;
            imageInformation = null;
            imageView.setImage(null);
            imageView.setTranslateX(0);
            fileMenuButton.setVisible(false);
            sizeFixed = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded()) {
                return false;
            }

            fileMenuButton.setVisible(sourceFile != null);

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void loadImage(BaseController controller, Image inImage, int fNumber, int fIndex) {
        try {

            image = inImage;
            imageView.setPreserveRatio(true);
            imageView.setImage(image);
            framesNumber = fNumber;
            frameIndex = fIndex;
            imageChanged = false;
            updateImageLabel();
            updateSizeLabel();
            imageView.requestFocus();
            if (!sizeFixed) {
                fitSize();
                sizeFixed = true;
            }
            if (controller != null) {
                String t = controller.getBaseTitle() + " ";
                if (controller.sourceFile != null) {
                    t += controller.sourceFile.getAbsolutePath();
                }
                if (framesNumber > 1) {
                    t += " - " + message("Page") + " " + frameIndex + "/" + framesNumber;
                }
                getMyStage().setTitle(t);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public List<MenuItem> fileMenuItems(Event fevent) {
        try {
            if (sourceFile == null || imageView.getImage() == null) {
                return null;
            }
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            if (imageInformation != null) {
                menu = new MenuItem(message("Information") + "    Ctrl+I " + message("Or") + " Alt+I",
                        StyleTools.getIconImageView("iconInfo.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    infoAction();
                });
                items.add(menu);

                menu = new MenuItem(message("MetaData"), StyleTools.getIconImageView("iconMeta.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    metaAction();
                });
                items.add(menu);
            }

            menu = new MenuItem(message("SaveAs") + "    Ctrl+B " + message("Or") + " Alt+B",
                    StyleTools.getIconImageView("iconSaveAs.png"));
            menu.setOnAction((ActionEvent event) -> {
                saveAsAction();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("OpenDirectory"), StyleTools.getIconImageView("iconOpenPath.png"));
            menu.setOnAction((ActionEvent event) -> {
                openSourcePath();
            });
            items.add(menu);

            menu = new MenuItem(message("ImagesBrowser"), StyleTools.getIconImageView("iconBrowse.png"));
            menu.setOnAction((ActionEvent event) -> {
                browseAction();
            });
            items.add(menu);

            menu = new MenuItem(message("BrowseFiles"), StyleTools.getIconImageView("iconList.png"));
            menu.setOnAction((ActionEvent event) -> {
                FileBrowseController.open(this);
            });
            items.add(menu);

            menu = new MenuItem(message("SystemMethod"), StyleTools.getIconImageView("iconSystemOpen.png"));
            menu.setOnAction((ActionEvent event) -> {
                systemMethod();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("Rename"), StyleTools.getIconImageView("iconInput.png"));
            menu.setOnAction((ActionEvent event) -> {
                renameAction();
            });
            items.add(menu);

            menu = new MenuItem(message("Delete") + "    Ctrl+D " + message("Or") + " Alt+D",
                    StyleTools.getIconImageView("iconDelete.png"));
            menu.setOnAction((ActionEvent event) -> {
                deleteAction();
            });
            items.add(menu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
