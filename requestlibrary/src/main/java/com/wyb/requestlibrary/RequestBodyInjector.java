package com.wyb.requestlibrary;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wyb on 20/7/17  017 16:59.
 */
public class RequestBodyInjector {
    private static final String SUFFIX = "$$RequestBodyInject";

    public static void injectBody(BaseRequest request) {
        List<Class> classes = new ArrayList<>();
        Class clazz = request.getClass();
        do {
            classes.add(0,clazz);
            clazz = clazz.getSuperclass();
        }while (!clazz.getSimpleName().equals("BaseRequest"));
        for (int i = 0; i < classes.size(); i++) {
            RequestBodyInject proxyRequest = findProxyActivity(classes.get(i));
            if(proxyRequest != null) {
                proxyRequest.inject(request);
            }
        }
//        RequestBodyInject proxyRequest = findProxyActivity(request);
//        proxyRequest.inject(request);
    }

    private static RequestBodyInject findProxyActivity(Class clazz) {
        try {
            Class injectorClazz = Class.forName(clazz.getName() + SUFFIX);
            return (RequestBodyInject) injectorClazz.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
//        throw new RuntimeException(String.format("can not find %s , something when compiler.", request.getClass().getSimpleName() + SUFFIX));
        return null;
    }

    private static RequestBodyInject findProxyActivity(BaseRequest request) {
        try {
            Class clazz = request.getClass();
            Class injectorClazz = Class.forName(clazz.getName() + SUFFIX);
            return (RequestBodyInject) injectorClazz.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new RuntimeException(String.format("can not find %s , something when compiler.", request.getClass().getSimpleName() + SUFFIX));
    }
}
