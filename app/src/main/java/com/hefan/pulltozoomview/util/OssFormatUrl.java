package com.hefan.pulltozoomview.util;

/**
 * Created by len on 2016/10/24.
 */
public class OssFormatUrl {
    private static OssFormatUrl ossFormatUrl = new OssFormatUrl();

    public static OssFormatUrl getInstance() {
        return ossFormatUrl;
    }

    private String imgUrl;

    /**
     * @param url
     * @return 用于普通图片 @400w
     */
    public String formatUrl(String url) {
        imgUrl = url;
        if (imgUrl != null && !"".equals(imgUrl)) {
            if ((imgUrl.contains("img.hefantv.com") || imgUrl.contains("img1.hefantv.com")) && !imgUrl.contains("@")) {
                imgUrl = imgUrl + Constant.OSS_SUFFIX;
            }

        }
        return imgUrl;
    }

    /**
     * @param url
     * @return 用于头像图片 @115w
     */
    public String formatHeadUrl(String url) {
        imgUrl = url;
        if (imgUrl != null && !"".equals(imgUrl)) {
            if ((imgUrl.contains("img.hefantv.com") || imgUrl.contains("img1.hefantv.com")) && !imgUrl.contains("@")) {
                imgUrl = imgUrl + Constant.OSS_SUFFIX_HEADER;
            }

        }
        return imgUrl;
    }

    /**
     * @param url
     * @return 用于大图 @500w
     */
    public String formatBigImgUrl(String url) {
        imgUrl = url;
        if (imgUrl != null && !"".equals(imgUrl)) {
            if ((imgUrl.contains("img.hefantv.com") || imgUrl.contains("img1.hefantv.com")) && !imgUrl.contains("@")) {
                imgUrl = imgUrl + Constant.OSS_SUFFIX_BIGIMG;
            }

        }
        return imgUrl;
    }
}
