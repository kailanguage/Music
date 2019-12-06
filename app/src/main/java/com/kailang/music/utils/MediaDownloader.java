package com.kailang.music.utils;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class MediaDownloader {
    private OnDownloadListener onDownloadListener;

    public MediaDownloader(OnDownloadListener onDownloadListener) {
        this.onDownloadListener = onDownloadListener;
    }

    /**
     * 下载媒体文件
     * @param url 下载地址
     * @param savePath 保存路径
     */
    public void download(String url, String savePath) {
        try {
            URL link = new URL(url);
            HttpURLConnection con = (HttpURLConnection) link.openConnection();
            int code = con.getResponseCode();
            if (code == 200) {
                //获取下载总大小
                int len = con.getContentLength();
                if (null != onDownloadListener) {
                    onDownloadListener.onStart(len,savePath);
                }
                RandomAccessFile rf = new RandomAccessFile(savePath, "rw");
                rf.setLength(len);
                byte[] buf = new byte[1024];
                //当次读取的数量
                int num;
                //当前下载的量
                int count = 0;
                InputStream in = con.getInputStream();
                while ((num = in.read(buf)) != -1) {
                    rf.write(buf, 0, num);
                    count += num;
                    if (null != onDownloadListener) {
                        onDownloadListener.onDownloading(count);
                    }
                }
                rf.close();
                in.close();
            }
            con.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnDownloadListener {
        void onStart(int size,String path);

        void onDownloading(int currentSize);
    }

    public OnDownloadListener getOnDownloadListener() {
        return onDownloadListener;
    }

    public void setOnDownloadListener(OnDownloadListener onDownloadListener) {
        this.onDownloadListener = onDownloadListener;
    }
}
