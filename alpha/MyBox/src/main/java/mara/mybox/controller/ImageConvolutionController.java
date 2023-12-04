package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import mara.mybox.bufferedimage.ImageConvolution;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.db.table.TableConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ImageConvolutionController extends BasePixelsController {

    protected ConvolutionKernel kernel, loadedKernel;

    @FXML
    protected ListView<ConvolutionKernel> listView;

    public ImageConvolutionController() {
        baseTitle = message("Convolution");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            if (editor == null) {
                close();
                return;
            }

            listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            listView.setCellFactory(new Callback<ListView<ConvolutionKernel>, ListCell<ConvolutionKernel>>() {
                @Override
                public ListCell<ConvolutionKernel> call(ListView<ConvolutionKernel> param) {

                    ListCell<ConvolutionKernel> cell = new ListCell<ConvolutionKernel>() {
                        @Override
                        public void updateItem(ConvolutionKernel item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(null);
                            setGraphic(null);
                            if (empty || item == null) {
                                return;
                            }
                            setText(item.getName());
                        }
                    };
                    return cell;
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

            refreshAction();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            List<ConvolutionKernel> kernels;

            @Override
            protected boolean handle() {
                kernels = TableConvolutionKernel.read();
                return true;
            }

            @Override
            protected void whenSucceeded() {
                listView.getItems().setAll(kernels);
            }
        };
        start(task);
    }

    @FXML
    public void manageAction() {
        openStage(Fxmls.ConvolutionKernelManagerFxml);
    }

    @Override
    protected boolean checkOptions() {
        if (!super.checkOptions()) {
            return false;
        }
        kernel = listView.getSelectionModel().getSelectedItem();
        if (kernel == null) {
            popError(message("SelectToHandle"));
            return false;
        }
        return true;
    }

    @Override
    protected Image handleImage(Image inImage, ImageScope inScope) {
        try {
            ImageConvolution convolution = ImageConvolution.create();
            convolution.setImage(inImage)
                    .setScope(inScope)
                    .setKernel(kernel)
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent());
            operation = message("Convolution");
            opInfo = kernel.getName();
            return convolution.operateFxImage();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    @FXML
    @Override
    protected void demo() {
        if (!checkOptions()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        reset();
        task = new SingletonCurrentTask<Void>(this) {
            private Image demoImage = null;

            @Override
            protected boolean handle() {
                try {
//                    demoImage = ScaleTools.demoImage(srcImage());
//                    demoImage = handleImage(demoImage, scope());
                    return demoImage != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                ImagePopController.openImage(myController, demoImage);
            }

        };
        start(task);
    }


    /*
        static methods
     */
    public static ImageConvolutionController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageConvolutionController controller = (ImageConvolutionController) WindowTools.branch(
                    parent.getMyWindow(), Fxmls.ImageConvolutionFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static void updateList() {
        Platform.runLater(() -> {
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                if (!(window instanceof Stage)) {
                    continue;
                }
                Stage stage = (Stage) window;
                Object controller = stage.getUserData();
                if (controller == null) {
                    continue;
                }
                if (controller instanceof ImageConvolutionController) {
                    try {
                        ((ImageConvolutionController) controller).refreshAction();
                    } catch (Exception e) {
                    }
                }
            }
        });
    }

}
