package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.ImageItem;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.cell.ListImageItemCell;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-2-17
 * @License Apache License Version 2.0
 */
public class ImageExampleSelectController extends BaseInputController {

    @FXML
    protected ListView<ImageItem> listView;
    @FXML
    protected ControlWebView viewController;

    public ImageExampleSelectController() {
        baseTitle = message("Image");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            listView.setCellFactory((ListView<ImageItem> param) -> {
                ListImageItemCell cell = new ListImageItemCell();
                return cell;
            });

            listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ImageItem>() {
                @Override
                public void changed(ObservableValue ov, ImageItem oldVal, ImageItem newVal) {
                    viewImage();
                }
            });

            listView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() > 1) {
                        okAction();
                    }
                }
            });

            viewController.setParent(this);

            loadList();

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void loadList() {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private List<ImageItem> items;

            @Override
            protected boolean handle() {
                try {
                    items = new ArrayList<>();
                    List<ImageItem> predefinedItems = ImageItem.predefined(false);
                    items.addAll(predefinedItems);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                listView.getItems().setAll(items);
                listView.getSelectionModel().select(0);
            }

        };
        start(task);
    }

    public void viewImage() {
        if (isSettingValues) {
            return;
        }
        viewController.clear();
        ImageItem selected = listView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.isColor()) {
            return;
        }
        File file = selected.getFile();
        if (file == null || !file.exists()) {
            return;
        }
        String body = "<Img src='" + file.toURI().toString() + "' width=" + selected.getWidth() + ">\n";
        String comments = selected.getComments();
        if (comments != null && !comments.isBlank()) {
            body += "<BR>" + message(comments);
        }
        viewController.loadContent(HtmlWriteTools.html(body));
    }

    public ImageItem selectedItem() {
        try {
            ImageItem item = listView.getSelectionModel().getSelectedItem();
            return item != null ? item : listView.getItems().get(0);
        } catch (Exception e) {
            return null;
        }
    }

    /*
        static
     */
    public static ImageExampleSelectController open(BaseController parent) {
        try {
            ImageExampleSelectController controller = (ImageExampleSelectController) WindowTools.childStage(
                    parent, Fxmls.ImageExampleSelectFxml);
            controller.setParameters(parent, null);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
