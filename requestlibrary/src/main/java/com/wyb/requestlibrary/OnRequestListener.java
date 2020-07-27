package com.wyb.requestlibrary;

/**
 * Created by wyb on 18/6/25  025.
 */

public interface OnRequestListener {

    void onRequestError(BaseRequest request, BaseResponse response);

    void onRequestSuccessful(BaseRequest request, BaseResponse response);

}
