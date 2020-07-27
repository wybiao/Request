package com.wyb.requestlibrary;

/**
 * Created by wyb on 18/6/29  029.
 */

public class SimpleRequest extends BaseRequest {

    public String url;

    public SimpleRequest(String url) {
        this.url = url;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public boolean isGetType() {
        return true;
    }
}
