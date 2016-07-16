package com.shoping.mall.study.okhttp.lib.callback;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.google.gson.internal.$Gson$Types;
import com.squareup.okhttp.Request;

public abstract class ResultCallback<T>
{
    public Type mType;

    public ResultCallback()
    {
        mType = getSuperclassTypeParameter(getClass());
    }

    static Type getSuperclassTypeParameter(Class<?> subclass)
    {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class)
        {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
    }

    public void onBefore(Request request){
    	
    }

    public void onAfter()
    {
    
    }

    public void inProgress(float progress){

    }


    public abstract void onResponse(T response);

    public abstract void onError(Request request, Exception e);

    public static final ResultCallback<String> DEFAULT_RESULT_CALLBACK = new ResultCallback<String>()
    {
        @Override
        public void onError(Request request, Exception e)
        {

        }

        @Override
        public void onResponse(String response)
        {

        }
    };
}