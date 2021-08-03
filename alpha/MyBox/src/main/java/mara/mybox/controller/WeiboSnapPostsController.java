package mara.mybox.controller;

import java.io.File;
import java.util.Date;
import mara.mybox.data.WeiboSnapParameters;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-9-13
 * @Description
 * @License Apache License Version 2.0
 */
public class WeiboSnapPostsController extends WeiboSnapRunController {

    public WeiboSnapPostsController() {
        baseTitle = Languages.message("WeiboSnap");
    }

    @Override
    public void initControls() {
        super.initControls();
        snapType = SnapType.Posts;
    }

    @Override
    protected void setStartPage() {
        startPage = parameters.getStartPage();
    }

    @Override
    protected void updateParameters() {

        currentMonthString = DateTools.dateToMonthString(currentMonth);
        currentAddress = parameters.getWebAddress() + "?is_all=1&stat_date="
                + currentMonthString.replace("-", "")
                + "&page=" + currentPage + "&mmts=" + new Date().getTime();
        UserConfig.setUserConfigString("WeiboPostsLastMonth", currentMonthString);
        UserConfig.setUserConfigString("WeiboPostsLastPage", currentPage + "");

        if (parameters.isCreatePDF()) {
            if (parameters.getCategory() == WeiboSnapParameters.FileCategoryType.InMonthsPaths) {
                pdfPath = new File(rootPath.getAbsolutePath() + File.separator
                        + DateTools.dateToYearString(currentMonth) + "-pdf");
            } else {
                pdfPath = new File(rootPath.getAbsolutePath() + File.separator + "pdf");
            }
            if (!pdfPath.exists()) {
                pdfPath.mkdirs();
            }
            pdfFilename = pdfPath.getAbsolutePath() + File.separator + accountName + "-"
                    + currentMonthString + "-第" + currentPage + "页.pdf";
        }
        if (parameters.isCreateHtml()) {
            if (parameters.getCategory() == WeiboSnapParameters.FileCategoryType.InMonthsPaths) {
                htmlPath = new File(rootPath.getAbsolutePath() + File.separator
                        + DateTools.dateToYearString(currentMonth) + "-html");
            } else {
                htmlPath = new File(rootPath.getAbsolutePath() + File.separator + "html");
            }
            if (!htmlPath.exists()) {
                htmlPath.mkdirs();
            }
            htmlFilename = htmlPath.getAbsolutePath() + File.separator + accountName + "-"
                    + currentMonthString + "-第" + currentPage + "页.html";
        }
        if (parameters.isSavePictures()) {
            if (parameters.getCategory() == WeiboSnapParameters.FileCategoryType.InMonthsPaths) {
                pixPath = new File(rootPath.getAbsolutePath() + File.separator
                        + DateTools.dateToYearString(currentMonth) + "-picture");
                if (!pixPath.exists()) {
                    pixPath.mkdirs();
                }
                pixPath = new File(pixPath + File.separator
                        + DateTools.dateToMonthString(currentMonth) + "-picture");
            } else {
                pixPath = new File(rootPath.getAbsolutePath() + File.separator + "picture");
            }
            if (!pixPath.exists()) {
                pixPath.mkdirs();
            }
            pixFilePrefix = pixPath.getAbsolutePath() + File.separator + accountName + "-"
                    + currentMonthString + "-第" + currentPage + "页-图";
        }
        parameters.setTitle(accountName + "-" + currentMonthString + "-第" + currentPage + "页");

    }

}
