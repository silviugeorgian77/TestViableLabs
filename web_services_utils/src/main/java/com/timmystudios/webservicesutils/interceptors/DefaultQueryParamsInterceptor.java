package com.timmystudios.webservicesutils.interceptors;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class DefaultQueryParamsInterceptor implements Interceptor {

    private HashMap<String, String> defaultQueryParams;

    public DefaultQueryParamsInterceptor(HashMap<String, String> defaultQueryParams) {
        this.defaultQueryParams = defaultQueryParams;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        final Request original = chain.request();
        final HttpUrl originalHttpUrl = original.url();

        final HttpUrl.Builder urlBuilder = originalHttpUrl.newBuilder();
        if (defaultQueryParams != null) {
            for (Map.Entry<String, String> queryParam : defaultQueryParams.entrySet()) {
                String key = queryParam.getKey();
                String value = queryParam.getValue();
                urlBuilder.addQueryParameter(key, value);
            }
        }
        HttpUrl url = urlBuilder.build();
        // Request customization: add request headers
        final Request.Builder requestBuilder = original.newBuilder()
                .url(url);
        final Request request = requestBuilder.build();
        return chain.proceed(request);
    }
}
