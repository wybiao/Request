package com.wyb.requestlibrary;

import com.google.gson.Gson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by wyb on 18/6/25  025.
 */

public class BaseResponse<T> {

    private int code;
    private String msg;
    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static BaseResponse fromJson(String json, final Class clazz, final boolean isList) {
        Gson gson = new Gson();
        final Type listType = new ParameterizedType() {
            public Type getRawType() {
                if (isList) {
                    return List.class;
                }
                return clazz;
            }

            public Type[] getActualTypeArguments() {
                return new Type[]{clazz};
            }

            public Type getOwnerType() {
                return null;
            }
        };
        Type objectType = new ParameterizedType() {
            public Type getRawType() {
                return BaseResponse.class;
            }

            public Type[] getActualTypeArguments() {
                return new Type[]{listType};
            }

            public Type getOwnerType() {
                return null;
            }
        };
        return gson.fromJson(json, objectType);
    }

}
