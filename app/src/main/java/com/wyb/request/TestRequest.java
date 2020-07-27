package com.wyb.request;


import com.wyb.requestannotation.FieldName;
import com.wyb.requestlibrary.BaseRequest;

/**
 * Created by wyb on 20/7/17  017 17:26.
 */
public class TestRequest extends BaseRequest {

    @FieldName("mobile")
    public String mobile;
    @FieldName("password")
    public String password;
    @FieldName("type")
    public String type;

    public TestRequest(String mobile, String password, String type) {
        this.mobile = mobile;
        this.password = password;
        this.type = type;
    }

    @Override
    public String getUrl() {
//        return "/api/login/login";
        return "http://123/api/login/login";
    }
}
