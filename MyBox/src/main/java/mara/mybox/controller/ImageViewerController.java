/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageViewerController extends ImageBaseController {

    protected double mouseX, mouseY;

    @FXML
    protected ScrollPane scrollPane;
    @FXML
    protected ImageView imageView;
    @FXML
    private Label imageFile;
    @FXML
    protected HBox toolBar;
    @FXML
    protected Button iButton, mButton, oButton, wButton, inButton, outButton, lButton, rButton, tButton, bButton;

    @Override
    protected void initializeNext2() {
        try {

            toolBar.disableProperty().bind(
                    Bindings.isEmpty(sourceFileInput.textProperty())
                            .or(sourceFileInput.styleProperty().isEqualTo(badStyle))
            );
            setTips();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void sourceFileChanged(final File file) {
        loadImage(file, false);
    }

    @Override
    public void afterImageLoaded() {
        if (scrollPane.getHeight() < imageInformation.getyPixels()) {
            imageView.setFitHeight(scrollPane.getHeight() - 5);
            imageView.setFitWidth(scrollPane.getWidth() - 1);
        } else {
            imageView.setFitHeight(imageInformation.getyPixels());
            imageView.setFitWidth(imageInformation.getxPixels());
        }
        try {
            imageView.setImage(SwingFXUtils.toFXImage(image, null));
//                        imageView.setImage(new Image("file:" + fileName, true));
            if (imageFile != null) {
                imageFile.setText(sourceFile.getName());
            }
        } catch (Exception e) {
            imageView.setImage(null);
            popInformation(AppVaribles.getMessage("NotSupported"));
        }
    }

    @FXML
    public void imageMousePressed(MouseEvent event) {
        mouseX = event.getX();
        mouseY = event.getY();
    }

    @FXML
    public void imageMouseReleased(MouseEvent event) {
        FxmlTools.setScrollPane(scrollPane, mouseX - event.getX(), mouseY - event.getY());
    }

    @FXML
    public void popImageInformation(ActionEvent event) {
        showImageInformation();
    }

    @FXML
    public void popImageInformation2(MouseEvent event) {
        showImageInformation();
    }

    @FXML
    public void popMetaData(ActionEvent event) {
        showImageMetaData();
    }

    @FXML
    public void popMetaData2(MouseEvent event) {
        showImageMetaData();
    }

    @FXML
    public void zoomIn(ActionEvent event) {
        imageView.setFitHeight(imageView.getFitHeight() * 1.1);
        imageView.setFitWidth(imageView.getFitWidth() * 1.1);
    }

    @FXML
    public void zoomOut(ActionEvent event) {
        imageView.setFitHeight(imageView.getFitHeight() * 0.9);
        imageView.setFitWidth(imageView.getFitWidth() * 0.9);

    }

    @FXML
    public void originalSize(ActionEvent event) {
        imageView.setFitHeight(imageInformation.getyPixels());
        imageView.setFitWidth(imageInformation.getxPixels());
    }

    @FXML
    public void windowSize(ActionEvent event) {
        imageView.setFitHeight(scrollPane.getHeight() - 5);
        imageView.setFitWidth(scrollPane.getWidth() - 1);
    }

    @FXML
    public void rotateRight(ActionEvent event) {
        imageView.setRotate(90);
    }

    @FXML
    public void rotateLeft(ActionEvent event) {
        imageView.setRotate(270);
    }

    @FXML
    public void turnOver(ActionEvent event) {
        imageView.setRotate(180);
    }

    @FXML
    public void back(ActionEvent event) {
        imageView.setRotate(0);
    }

    public void setTips() {
        if (iButton != null) {
            FxmlTools.quickTooltip(iButton, new Tooltip(AppVaribles.getMessage("ImageInformation")));
            FxmlTools.quickTooltip(mButton, new Tooltip(AppVaribles.getMessage("ImageMetaData")));
        }
        if (wButton != null) {
            FxmlTools.quickTooltip(wButton, new Tooltip(AppVaribles.getMessage("WindowSize")));
            FxmlTools.quickTooltip(oButton, new Tooltip(AppVaribles.getMessage("OriginalSize")));
            FxmlTools.quickTooltip(inButton, new Tooltip(AppVaribles.getMessage("ZoomIn")));
            FxmlTools.quickTooltip(outButton, new Tooltip(AppVaribles.getMessage("ZoomOut")));
            FxmlTools.quickTooltip(lButton, new Tooltip(AppVaribles.getMessage("RotateLeft")));
            FxmlTools.quickTooltip(rButton, new Tooltip(AppVaribles.getMessage("RotateRight")));
            FxmlTools.quickTooltip(tButton, new Tooltip(AppVaribles.getMessage("TurnOver")));
            FxmlTools.quickTooltip(bButton, new Tooltip(AppVaribles.getMessage("Back")));
        }
    }

    public void loadImage(final String fileName) {
        try {
            sourceFileInput.setText(new File(fileName).getAbsolutePath());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void showImageInformation() {
        try {
            if (imageInformation == null) {
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.ImageInformationFxml), AppVaribles.CurrentBundle);
            Pane root = fxmlLoader.load();
            ImageInformationController controller = fxmlLoader.getController();
            controller.loadInformation(imageInformation);

            Stage imageInformationStage = new Stage();
            controller.setMyStage(imageInformationStage);
            imageInformationStage.setTitle(AppVaribles.getMessage("AppTitle"));
            imageInformationStage.initModality(Modality.NONE);
            imageInformationStage.initStyle(StageStyle.DECORATED);
            imageInformationStage.initOwner(null);
            imageInformationStage.getIcons().add(CommonValues.AppIcon);
            imageInformationStage.setScene(new Scene(root));
            imageInformationStage.show();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void showImageMetaData() {
        try {
            if (imageInformation == null) {
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.ImageMetaDataFxml), AppVaribles.CurrentBundle);
            Pane root = fxmlLoader.load();
            ImageMetaDataController controller = fxmlLoader.getController();
            controller.loadData(imageInformation);

            Stage imageInformationStage = new Stage();
            controller.setMyStage(imageInformationStage);
            imageInformationStage.setTitle(AppVaribles.getMessage("AppTitle"));
            imageInformationStage.initModality(Modality.NONE);
            imageInformationStage.initStyle(StageStyle.DECORATED);
            imageInformationStage.initOwner(null);
            imageInformationStage.getIcons().add(CommonValues.AppIcon);
            imageInformationStage.setScene(new Scene(root));
            imageInformationStage.show();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public double getMouseX() {
        return mouseX;
    }

    public void setMouseX(double mouseX) {
        this.mouseX = mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }

    public void setMouseY(double mouseY) {
        this.mouseY = mouseY;
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }

    public void setScrollPane(ScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public HBox getToolBar() {
        return toolBar;
    }

    public void setToolBar(HBox toolBar) {
        this.toolBar = toolBar;
    }

    public Button getiButton() {
        return iButton;
    }

    public void setiButton(Button iButton) {
        this.iButton = iButton;
    }

    public Button getmButton() {
        return mButton;
    }

    public void setmButton(Button mButton) {
        this.mButton = mButton;
    }

    public Button getoButton() {
        return oButton;
    }

    public void setoButton(Button oButton) {
        this.oButton = oButton;
    }

    public Button getwButton() {
        return wButton;
    }

    public void setwButton(Button wButton) {
        this.wButton = wButton;
    }

    public Button getInButton() {
        return inButton;
    }

    public void setInButton(Button inButton) {
        this.inButton = inButton;
    }

    public Button getOutButton() {
        return outButton;
    }

    public void setOutButton(Button outButton) {
        this.outButton = outButton;
    }

    public Button getlButton() {
        return lButton;
    }

    public void setlButton(Button lButton) {
        this.lButton = lButton;
    }

    public Button getrButton() {
        return rButton;
    }

    public void setrButton(Button rButton) {
        this.rButton = rButton;
    }

}
