package com.wyb.requestlibrary;

import android.net.Uri;

import java.io.Serializable;

/**
 * Created by wyb on 19/9/20  020 13:57.
 */
public class PostFileInfo implements Serializable {
    public String name;
    public String mimeType;
    public Uri uri;
    public long size;
}
