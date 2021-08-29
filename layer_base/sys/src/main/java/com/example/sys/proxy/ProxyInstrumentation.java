package com.example.sys.proxy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * @author lhr
 * @date 2021/6/3
 * @des Instrumentation代理
 */
public class ProxyInstrumentation extends Instrumentation {
    private final String PI_TAG = "PILog";
    private final Instrumentation base;
    public ProxyInstrumentation(Instrumentation ins){
        base = ins;
    }

    @SuppressLint("DiscouragedPrivateApi")
    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options){
        Log.e(PI_TAG,"Context: "+who+" , IBinder: "+contextThread+" , IBinder: "+token+" , Activity: "+target+" " +
                "Intent: "+intent+" , Int: "+requestCode+" , Bundle: "+options+"");
        try {
             Method realExec = Instrumentation.class.getDeclaredMethod("execStartActivity",
                    Context.class,IBinder.class,IBinder.class,Activity.class,
                    Intent.class,int.class,Bundle.class);
            realExec.setAccessible(true);
            return (ActivityResult) realExec.invoke(base,who,contextThread,token,target,intent,requestCode,options);
        }catch (Exception e){
            throw new RuntimeException("start activity exception");
        }
    }
}
