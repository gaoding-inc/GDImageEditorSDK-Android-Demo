package com.gaoding.editor.image.demo.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

/**
 * 描述: 基于上层实现的多媒体信息工具类，用于判断多媒体类别等
 */
public class MediaUtils {

    private static final String TAG = MediaUtils.class.getSimpleName();

    // Audio file types
    public static final int FILE_TYPE_MP3 = 1;
    public static final int FILE_TYPE_M4A = 2;
    public static final int FILE_TYPE_WAV = 3;
    public static final int FILE_TYPE_AMR = 4;
    public static final int FILE_TYPE_AWB = 5;
    public static final int FILE_TYPE_WMA = 6;
    public static final int FILE_TYPE_OGG = 7;
    public static final int FILE_TYPE_AAC = 8;
    // MIDI file types
    public static final int FILE_TYPE_MID = 11;
    public static final int FILE_TYPE_SMF = 12;
    public static final int FILE_TYPE_IMY = 13;
    // Video file types
    public static final int FILE_TYPE_MP4 = 21;
    public static final int FILE_TYPE_M4V = 22;
    public static final int FILE_TYPE_3GPP = 23;
    public static final int FILE_TYPE_3GPP2 = 24;
    public static final int FILE_TYPE_WMV = 25;
    public static final int FILE_TYPE_AVI = 26;
    public static final int FILE_TYPE_MOV = 27;
    // Image file types
    public static final int FILE_TYPE_JPEG = 31;
    public static final int FILE_TYPE_GIF = 32;
    public static final int FILE_TYPE_PNG = 33;
    public static final int FILE_TYPE_BMP = 34;
    public static final int FILE_TYPE_WBMP = 35;
    public static final int FILE_TYPE_WEBP = 36;
    // Playlist file types
    public static final int FILE_TYPE_M3U = 41;
    public static final int FILE_TYPE_PLS = 42;
    public static final int FILE_TYPE_WPL = 43;

    public static final String UNKNOWN_STRING = "<unknown>";

    private static final int FIRST_AUDIO_FILE_TYPE = FILE_TYPE_MP3;
    private static final int LAST_AUDIO_FILE_TYPE = FILE_TYPE_OGG;
    private static final int FIRST_MIDI_FILE_TYPE = FILE_TYPE_MID;
    private static final int LAST_MIDI_FILE_TYPE = FILE_TYPE_IMY;
    private static final int FIRST_VIDEO_FILE_TYPE = FILE_TYPE_MP4;
    private static final int LAST_VIDEO_FILE_TYPE = FILE_TYPE_MOV;
    private static final int FIRST_IMAGE_FILE_TYPE = FILE_TYPE_JPEG;
    private static final int LAST_IMAGE_FILE_TYPE = FILE_TYPE_WEBP;
    private static final int FIRST_PLAYLIST_FILE_TYPE = FILE_TYPE_M3U;
    private static final int LAST_PLAYLIST_FILE_TYPE = FILE_TYPE_WPL;

    public static String sFileExtensions;
    private static HashMap<String, MediaFileType> sFileTypeMap = new HashMap<String, MediaFileType>();
    private static HashMap<String, Integer> sMimeTypeMap = new HashMap<String, Integer>();

    static {
        addFileType("MP3", FILE_TYPE_MP3, "audio/mpeg");
        addFileType("M4A", FILE_TYPE_M4A, "audio/mp4");
        addFileType("WAV", FILE_TYPE_WAV, "audio/x-wav");
        addFileType("AMR", FILE_TYPE_AMR, "audio/amr");
        addFileType("AWB", FILE_TYPE_AWB, "audio/amr-wb");
        addFileType("WMA", FILE_TYPE_WMA, "audio/x-ms-wma");
        addFileType("OGG", FILE_TYPE_OGG, "application/ogg");

        addFileType("MID", FILE_TYPE_MID, "audio/midi");
        addFileType("XMF", FILE_TYPE_MID, "audio/midi");
        addFileType("RTTTL", FILE_TYPE_MID, "audio/midi");
        addFileType("SMF", FILE_TYPE_SMF, "audio/sp-midi");
        addFileType("IMY", FILE_TYPE_IMY, "audio/imelody");

        addFileType("MP4", FILE_TYPE_MP4, "video/mp4");
        addFileType("M4V", FILE_TYPE_M4V, "video/mp4");
        addFileType("3GP", FILE_TYPE_3GPP, "video/3gpp");
        addFileType("3GPP", FILE_TYPE_3GPP, "video/3gpp");
        addFileType("3G2", FILE_TYPE_3GPP2, "video/3gpp2");
        addFileType("3GPP2", FILE_TYPE_3GPP2, "video/3gpp2");
        addFileType("WMV", FILE_TYPE_WMV, "video/x-ms-wmv");
        addFileType("AVI", FILE_TYPE_AVI, "video/avi");
        addFileType("MOV", FILE_TYPE_MOV, "video/quicktime");

        addFileType("JPG", FILE_TYPE_JPEG, "image/jpeg");
        addFileType("JPEG", FILE_TYPE_JPEG, "image/jpeg");
        addFileType("GIF", FILE_TYPE_GIF, "image/gif");
        addFileType("PNG", FILE_TYPE_PNG, "image/png");
        addFileType("BMP", FILE_TYPE_BMP, "image/x-ms-bmp");
        addFileType("WBMP", FILE_TYPE_WBMP, "image/vnd.wap.wbmp");
        addFileType("WEBP", FILE_TYPE_WEBP, "image/webp");

        addFileType("M3U", FILE_TYPE_M3U, "audio/x-mpegurl");
        addFileType("PLS", FILE_TYPE_PLS, "audio/x-scpls");
        addFileType("WPL", FILE_TYPE_WPL, "application/vnd.ms-wpl");

        // compute file extensions list for native Media Scanner
        StringBuilder builder = new StringBuilder();
        Iterator<String> iterator = sFileTypeMap.keySet().iterator();

        while (iterator.hasNext()) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(iterator.next());
        }
        sFileExtensions = builder.toString();
    }

    /**
     * 添加方法类别
     *
     * @param extension 扩展名
     * @param fileType  文件类型
     * @param mimeType  mime类型
     */
    static void addFileType(String extension, int fileType, String mimeType) {
        sFileTypeMap.put(extension, new MediaFileType(fileType, mimeType));
        sMimeTypeMap.put(mimeType, fileType);
    }

    /**
     * 媒体类型
     */
    public static class MediaFileType {
        public int fileType;
        public String mimeType;

        MediaFileType(int fileType, String mimeType) {
            this.fileType = fileType;
            this.mimeType = mimeType;
        }
    }

    /**
     * 判断是否是gif类型
     *
     * @param path 路径
     * @return 是否是gif类型
     */
    public static boolean isGifFileType(String path) {
        MediaFileType type = getFileType(path);
        if (null != type) {
            return isGifFileType(type.fileType);
        }
        return false;
    }

    /**
     * 是否是gif类型
     *
     * @param fileType 文件类型
     * @return 是否是gif类型
     */
    public static boolean isGifFileType(int fileType) {
        return (fileType == FILE_TYPE_GIF);
    }

    /**
     * 是否是视频文件
     *
     * @param fileType 文件类型
     * @return 是否是视频文件
     */
    public static boolean isVideoFileType(int fileType) {
        return (fileType >= FIRST_VIDEO_FILE_TYPE && fileType <= LAST_VIDEO_FILE_TYPE);
    }

    /**
     * 是否是视频文件
     *
     * @param path 路径
     * @return 是否视频
     */
    public static boolean isVideoFileType(String path) {
        MediaFileType type = getFileType(path);
        if (null != type) {
            return isVideoFileType(type.fileType);
        }
        return false;
    }

    /**
     * 是否是图片文件
     *
     * @param fileType 文件类型
     * @return 是否是图片文件
     */
    public static boolean isImageFileType(int fileType) {
        return (fileType >= FIRST_IMAGE_FILE_TYPE && fileType <= LAST_IMAGE_FILE_TYPE);
    }

    /**
     * 是否是图片文件
     *
     * @param path 路径
     * @return 是否图片
     */
    public static boolean isImageFileType(String path) {
        MediaFileType type = getFileType(path);
        if (null != type) {
            return isImageFileType(type.fileType);
        }
        return false;
    }

    /**
     * 获取文件类型
     *
     * @param path 文件路径
     * @return 文件类型
     */
    public static MediaFileType getFileType(String path) {
        if (path == null) {
            return null;
        }
        int lastDot = path.lastIndexOf(".");
        if (lastDot < 0) {
            return null;
        }
        return sFileTypeMap.get(path.substring(lastDot + 1).toUpperCase());
    }

    /**
     * 获取媒体时长
     */
    public static long getMediaDuration(String uri) {

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            mmr.setDataSource(uri);

            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            duration = TextUtils.isEmpty(duration) ? "0" : duration;
            return Long.parseLong(duration);

        } catch (Exception ex) {

            ex.printStackTrace();
        } finally {
            mmr.release();
        }

        return 0;
    }

    /**
     * 图库数据库插入 插入数据
     */
    public static void insertSysPictureDatabase(Context context, String filePath) {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, filePath);
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            ContentResolver contentResolver = context.getContentResolver();
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            scanPhoto(context, filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 视频数据库插入数据
     *
     * @param context
     * @param filePath
     */
    public static void insertSysVideoDatabase(Context context, String filePath) {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.DATA, filePath);
            values.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Video.Media.DURATION, MediaUtils.getMediaDuration(filePath));
            ContentResolver contentResolver = context.getContentResolver();
            contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            scanPhoto(context, filePath);
        } catch (Exception e) {
        }
    }

    /**
     * 扫描图库
     */
    public static void scanPhoto(Context context, String imgFileName) {
        File file = new File(imgFileName);
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(file)));
    }

}
