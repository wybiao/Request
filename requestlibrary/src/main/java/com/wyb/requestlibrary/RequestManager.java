package com.wyb.requestlibrary;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by wyb on 18/6/25  025.
 */

public class RequestManager {

    public interface OnRequestLogListener {
        void onLog(String msg, boolean crashLog);
    }

    private Context context;
    private static final String TAG = "RequestManager";
    protected static String BASE_URL = "";
    private static String TOKEN = "";

    private static RequestManager mInstance;
    private OkHttpClient mOkHttpClient;
    private OkHttpClient mOkHttpFileClient;
    private List<BaseRequest> mRequests;
    private OnRequestLogListener onRequestLogListener;

    public static RequestManager getInstance() {
        return mInstance;
    }

    public static void init(Context context, String baseUrl) {
        if (mInstance == null) {
            mInstance = new RequestManager(context);
        }
        BASE_URL = baseUrl;
    }

    public Context getContext() {
        return context;
    }

    public void setOnRequestLogListener(OnRequestLogListener onRequestLogListener) {
        this.onRequestLogListener = onRequestLogListener;
    }

    public void setToken(String token) {
        TOKEN = token;
    }

    private RequestManager(Context context) {
        this.context = context;
        mOkHttpClient = new OkHttpClient();
        mOkHttpFileClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES).build();
        mRequests = new ArrayList<>();
    }

    /**
     * @param baseRequest 请求reueqest
     * @param clazz       返回的数据类型
     * @param isList      返回的数据是否是数组
     * @param listener    请求回调的接口
     */
    public void sendRequest(final BaseRequest baseRequest, final Class clazz, final boolean isList, OnRequestListener listener) {
        mRequests.add(baseRequest);
        baseRequest.setListener(listener);
        String url = baseRequest.getUrl();
        Request.Builder builder = new Request.Builder();
        if (baseRequest.isGetType()) {
            builder.get();
            url = baseRequest.getGetTypeUrl();
        } else {
            RequestBody body = baseRequest.getRequestBody();
            if (body == null) {
                builder.get();
            } else {
                builder.post(body);
            }
        }
        if (!url.startsWith("http")) {
            url = BASE_URL + url;
        }
        builder.url(url);
        builder.addHeader("token", TOKEN);
        Request request = builder.build();
        Call call = null;
        if (baseRequest.hasFile()) {
            call = mOkHttpFileClient.newCall(request);
        } else {
            call = mOkHttpClient.newCall(request);
        }
        baseRequest.setCall(call);
        String log = "sendRequest: " + baseRequest.toString() + ",token=" + TOKEN + ",hasFile=" + baseRequest.hasFile;
        Log.d(TAG, log);
        if (onRequestLogListener != null) {
            onRequestLogListener.onLog(log, false);
        }
        final String finalUrl = url;
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                BaseResponse baseResponse = new BaseResponse();
                baseResponse.setCode(-1);
                baseResponse.setMsg(e.getMessage());
                Message message = new Message();
                message.what = 0;
                message.obj = baseResponse;
                handler.sendMessage(message);
                String log = "onFailure: " + finalUrl + "   response.failure  " + e.toString();
                Log.d(TAG, log);
                if (onRequestLogListener != null) {
                    onRequestLogListener.onLog(log, true);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                String log = "onResponse: " + finalUrl + "   response.body  " + responseBody;
                Log.d(TAG, log);
                boolean crashLog = false;
                BaseResponse baseResponse = null;
                try {
                    baseResponse = new Gson().fromJson(responseBody, BaseResponse.class);
                    if (baseResponse.getCode() == 200) {
                        baseResponse = BaseResponse.fromJson(responseBody, clazz, isList);
                    } else {
                        baseResponse = BaseResponse.fromJson(responseBody, String.class, false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    baseResponse = new BaseResponse();
                    baseResponse.setCode(500);
                    baseResponse.setMsg("请求失败");
                    crashLog = true;
                }
                if (onRequestLogListener != null) {
                    onRequestLogListener.onLog(log, crashLog);
                }
                Message message = new Message();
                message.what = 0;
                message.obj = baseResponse;
                handler.sendMessage(message);
            }

            Handler handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (baseRequest.getListener() == null) {
                        return;
                    }
                    BaseResponse baseResponse = (BaseResponse) msg.obj;
                    if (baseResponse.getCode() == 200) {
                        baseRequest.getListener().onRequestSuccessful(baseRequest, baseResponse);
                    } else {
//                        if (baseResponse.getCode() == 201) {
//                            if (onOtherLoginListener != null) {
//                                onOtherLoginListener.onOtherLogin();
//                            }
//                        } else {
                        baseRequest.getListener().onRequestError(baseRequest, baseResponse);
//                        }
                    }
                    mRequests.remove(baseRequest);
                }
            };
        });
    }

    public void cancel(OnRequestListener listener) {
        List<BaseRequest> temp = new ArrayList<>();
        for (int i = 0; i < mRequests.size(); i++) {
            if (mRequests.get(i).getListener() == listener) {
                mRequests.get(i).getCall().cancel();
                mRequests.get(i).setListener(null);
                temp.add(mRequests.get(i));
            }
        }
        for (int i = 0; i < temp.size(); i++) {
            mRequests.remove(temp.get(i));
        }
    }

}
