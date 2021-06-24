package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.image.ImageManufacture;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;

/**
 * @Author Mara
 * @CreateDate 2021-5-22
 * @License Apache License Version 2.0
 */
public class PptViewController extends BaseFileImagesViewController {

    @FXML
    protected TextArea notesArea, slideArea;
    @FXML
    protected Label notesLabel, slideLabel;

    public PptViewController() {
        baseTitle = AppVariables.message("PptView");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.PPTS, VisitHistory.FileType.Image);
    }

    @Override
    public void sourceFileChanged(File file) {
        if (file == null) {
            return;
        }
        loadFile(file, 1);
    }

    public void loadFile(File file, int page) {
        try {
            initPage(file, page);
            if (file == null) {
                return;
            }
            loadInformation();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void loadInformation() {
        if (sourceFile == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
                    setTotalPages(0);
                    try ( SlideShow ppt = SlideShowFactory.create(sourceFile)) {
                        setTotalPages(ppt.getSlides().size());
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return framesNumber > 0;
                }

                @Override
                protected void whenSucceeded() {
                    List<String> pages = new ArrayList<>();
                    for (int i = 1; i <= framesNumber; i++) {
                        pages.add(i + "");
                    }
                    isSettingValues = true;
                    pageSelector.getItems().clear();
                    pageSelector.getItems().setAll(pages);
                    pageSelector.setValue("1");
                    pageLabel.setText("/" + framesNumber);
                    isSettingValues = false;
                    checkCurrentPage();
                    checkThumbs();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL, message("LoadingFileInfo"));
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @Override
    protected void loadPage() {
        notesArea.clear();
        notesLabel.setText("");
        slideArea.clear();
        slideLabel.setText("");
        initCurrentPage();
        if (sourceFile == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                task.cancel();
            }
            task = new SingletonTask<Void>() {
                private String slideTexts, notes;

                @Override
                protected boolean handle() {
                    image = null;
                    slideTexts = "";
                    notes = "";
                    try ( SlideShow ppt = SlideShowFactory.create(sourceFile)) {
                        Slide slide = (Slide) (ppt.getSlides().get(frameIndex));
                        int width = ppt.getPageSize().width;
                        int height = ppt.getPageSize().height;
                        BufferedImage slideImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                        slide.draw(slideImage.createGraphics());
                        if (dpi != 72) {
                            slideImage = ImageManufacture.scaleImageByScale(slideImage, dpi / 72f);
                        }
                        image = SwingFXUtils.toFXImage(slideImage, null);

                        SlideShowExtractor extractor = new SlideShowExtractor(ppt);
                        extractor.setSlidesByDefault(true);
                        extractor.setMasterByDefault(false);
                        extractor.setNotesByDefault(false);
                        extractor.setCommentsByDefault(false);
                        slideTexts = extractor.getText(slide);
                        extractor.setSlidesByDefault(false);
                        extractor.setNotesByDefault(true);
                        notes = extractor.getText(slide);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return image != null;
                }

                @Override
                protected void whenSucceeded() {
                    setImage(image, percent);
                    notesArea.setText(notes);
                    notesLabel.setText(message("Count") + ": " + notes.length());
                    slideArea.setText(slideTexts);
                    slideLabel.setText(message("Count") + ": " + slideTexts.length());
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL, MessageFormat.format(message("LoadingPageNumber"), (frameIndex + 1) + ""));
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @Override
    protected Map<Integer, Image> readThumbs(int pos, int end) {
        Map<Integer, Image> images = null;
        try ( SlideShow ppt = SlideShowFactory.create(sourceFile)) {
            images = new HashMap<>();
            List<Slide> slides = ppt.getSlides();
            int width = ppt.getPageSize().width;
            int height = ppt.getPageSize().height;
            for (int i = pos; i < end; ++i) {
                ImageView view = (ImageView) thumbBox.getChildren().get(2 * i);
                if (view.getImage() != null) {
                    continue;
                }
                try {
                    Slide slide = slides.get(i);
                    BufferedImage slideImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    slide.draw(slideImage.createGraphics());
                    if (slideImage.getWidth() > ThumbWidth) {
                        slideImage = ImageManufacture.scaleImageWidthKeep(slideImage, ThumbWidth);
                    }
                    Image thumb = SwingFXUtils.toFXImage(slideImage, null);
                    images.put(i, thumb);
                } catch (Exception e) {
                    MyBoxLog.debug(e.toString());
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return images;
    }

}
