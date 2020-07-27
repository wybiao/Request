package com.wyb.requestlibrary;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * Created by wyb on 18/6/25  025.
 */

public abstract class BaseRequest {

    private OnRequestListener mListener;
    private Call mCall;
    protected StringBuilder fieldSb;
    protected boolean hasFile = false;
    MultipartBody.Builder multipartBodyBuilder;

    public OnRequestListener getListener() {
        return mListener;
    }

    public void setListener(OnRequestListener listener) {
        this.mListener = listener;
    }

    public Call getCall() {
        return mCall;
    }

    public void setCall(Call call) {
        this.mCall = call;
    }

    public abstract String getUrl();

    public boolean isGetType() {
        return false;
    }

    public boolean hasFile() {
        return hasFile;
    }

    protected RequestBody getRequestBody() {
        RequestBodyInjector.injectBody(this);
        if (multipartBodyBuilder == null) {
            return null;
        }
        try {
            return multipartBodyBuilder.build();
        } catch (Exception e) {
            return null;
        }
    }

    public String getGetTypeUrl() {
        RequestBodyInjector.injectBody(this);
        if (fieldSb == null) {
            return getUrl();
        }
        return getUrl() + fieldSb.toString();
    }

    public void addField(String key, Object value) {
        if (multipartBodyBuilder == null) {
            multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        }
        if (fieldSb == null) {
            fieldSb = new StringBuilder();
        }
        if (isGetType()) {
            fieldSb.append("&");
            fieldSb.append(key);
            fieldSb.append("=");
            fieldSb.append(value);
        } else {
            if (fieldSb.length() > 0) {
                fieldSb.append(",");
            }
            fieldSb.append(key);
            fieldSb.append("=");
            if (value instanceof PostFileInfo) {
                hasFile = true;
                PostFileInfo selFileInfo = (PostFileInfo) value;
                multipartBodyBuilder.addFormDataPart(key, selFileInfo.name, getFileRequestBody(selFileInfo.uri));
                fieldSb.append(selFileInfo.name);
            } else if (value instanceof List) {
                hasFile = true;
                List<PostFileInfo> list = (List<PostFileInfo>) value;
                fieldSb.append("(");
                for (int i = 0; i < list.size(); i++) {
                    PostFileInfo selFileInfo = list.get(i);
                    if (selFileInfo.uri == null) {
                        continue;
                    }
                    multipartBodyBuilder.addFormDataPart(key + "[]", selFileInfo.name, getFileRequestBody(selFileInfo.uri));
                    fieldSb.append(selFileInfo.name);
                    fieldSb.append(",");
                }
                fieldSb.append(")");
            } else {
                multipartBodyBuilder.addFormDataPart(key, String.valueOf(value));
                fieldSb.append(String.valueOf(value));
            }
        }
    }

    public RequestBody getFileRequestBody(final Uri uri) {
        RequestBody requestBody = new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse("multipart/form-data");
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                Source source = null;
                InputStream inputStream = null;
                try {
                    inputStream = RequestManager.getInstance().getContext().getContentResolver().openInputStream(uri);
                    source = Okio.source(inputStream);
                    sink.writeAll(source);
                } finally {
                    Util.closeQuietly(source);
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            }
        };
        return requestBody;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!getUrl().startsWith("http")) {
            sb.append(RequestManager.BASE_URL);
        }
        sb.append(getUrl());
        if (fieldSb != null) {
            sb.append(fieldSb);
        }
        return sb.toString();
    }

}
