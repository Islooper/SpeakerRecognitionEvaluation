package com.example.speakerrecognitionevaluation;

import android.app.Application;
import android.content.Context;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

/**
 * Created by looper on 12/18/20.
 */
public class MyApplication extends Application {
    Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        SpeechUtility.createUtility(context, SpeechConstant.APPID +"=5fdc25a2");
    }
}
