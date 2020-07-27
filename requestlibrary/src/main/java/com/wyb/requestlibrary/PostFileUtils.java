package com.wyb.requestlibrary;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.net.URLConnection;

/**
 * Created by wyb on 19/12/31  031 16:45.
 */
public class PostFileUtils {


    public static PostFileInfo getPostFileByUri(Context context, Uri uri) {
        PostFileInfo postFileInfo = new PostFileInfo();
        postFileInfo.uri = uri;
        String path = null;
        // 以 file:// 开头的
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            path = uri.getPath();
            initFile(path, postFileInfo);
        }
        // 以 content:// 开头的，比如 content://media/extenral/images/media/17766
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    if (columnIndex > -1) {
                        path = cursor.getString(columnIndex);
                    }
                }
                cursor.close();
            }
            initFile(path, postFileInfo);
        }
        // 4.4及之后的 是以 content:// 开头的，比如 content://com.android.providers.media.documents/document/image%3A235700
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    // ExternalStorageProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        path = Environment.getExternalStorageDirectory() + "/" + split[1];
                        initFile(path, postFileInfo);
                    }
                } else if (isDownloadsDocument(uri)) {
                    // DownloadsProvider
                    String id = DocumentsContract.getDocumentId(uri);
                    if (id.startsWith("raw:")) {
                        path = id.replaceFirst("raw:", "");
                        initFile(path, postFileInfo);
                    }
                    Uri contentUri = uri;
//                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
//                        contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
//                    }
                    initDataColumn(context, contentUri, null, null, postFileInfo);
                } else if (isMediaDocument(uri)) {
                    // MediaProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    initDataColumn(context, contentUri, selection, selectionArgs, postFileInfo);
                }
            } else {
                initDataColumn(context, uri, null, null, postFileInfo);
            }
        }
        return postFileInfo;
    }

    private static void initFile(String path, PostFileInfo fileInfo) {
        File file = new File(path);
        if (file != null) {
            fileInfo.name = file.getName();
            fileInfo.mimeType = URLConnection.guessContentTypeFromName(fileInfo.name);
        }
    }

    private static void initDataColumn(Context context, Uri uri, String selection, String[]
            selectionArgs, PostFileInfo fileInfo) {
        Cursor cursor = null;
        final String[] projection = {"mime_type", "_display_name", "_size", "_data"};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
//                String[] aaas = cursor.getColumnNames();
//                for (int i = 0; i < aaas.length; i++) {
//                    Log.d("wyblog", "initDataColumn: " + aaas[i] + "----" + cursor.getString(cursor.getColumnIndexOrThrow(aaas[i])));
//                }
                int columen = cursor.getColumnIndex("_display_name");
                if (columen != -1) {
                    fileInfo.name = cursor.getString(columen);
                }
                if (TextUtils.isEmpty(fileInfo.name)) {
                    columen = cursor.getColumnIndex("_data");
                    if (columen != -1) {
                        String filePath = cursor.getString(columen);
                        if (!TextUtils.isEmpty(filePath)) {
                            fileInfo.name = filePath.substring(filePath.lastIndexOf("/") + 1);
                        }
                    }
                }
                columen = cursor.getColumnIndex("_size");
                if (columen != -1) {
                    fileInfo.size = cursor.getLong(columen);
                }
                columen = cursor.getColumnIndex("mime_type");
                if (columen != -1) {
                    fileInfo.mimeType = cursor.getString(columen);
                } else {
                    if (!TextUtils.isEmpty(fileInfo.name)) {
                        fileInfo.mimeType = getFileMimeType(fileInfo.name);
                    }
                }
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    public static String getFileMimeType(String fileName) {
        for (int i = 0; i < MATCH_ARRAY.length; i++) {
            //判断文件的格式
            if (fileName.contains(MATCH_ARRAY[i][0].toString())) {
                return MATCH_ARRAY[i][1];
            }
        }
        return "*/*";
    }

    //建立一个文件类型与文件后缀名的匹配表
    private static final String[][] MATCH_ARRAY = {
            //{后缀名，    文件类型}
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".prop", "text/plain"},
            {".rar", "application/x-rar-compressed"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-workdata"},
            {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/zip"},
            {"", "*/*"}
    };

}
