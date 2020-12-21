package com.example.speakerrecognitionevaluation;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.IdentityListener;
import com.iflytek.cloud.IdentityResult;
import com.iflytek.cloud.IdentityVerifier;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // 身份验证对象
    private IdentityVerifier mIdVerifier;

    private Toast mToast;
    // 会话类型
    private int mSST = 0;

    // 注册
    private static final int SST_ENROLL = 0;
    // 验证
    private static final int SST_VERIFY = 1;

    // 数字声纹密码段，默认有5段
    private String[] mNumPwdSegs;
    private String authid;

    // 默认为数字密码
    private int mPwdType = 3;
    // 数字声纹密码
    private String mNumPwd = "";

    // 数字密码类型为3，其他类型暂未开放
    private static final int PWD_TYPE_NUM = 3;

    ImageView mic;


    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findId();

        mIdVerifier = IdentityVerifier.createVerifier(MainActivity.this, new InitListener() {
            @Override
            public void onInit(int i) {
                if (ErrorCode.SUCCESS == i) {
                    showTip("引擎初始化成功");
                } else {
                    showTip("引擎初始化失败，错误码：" + i);
                }
            }
        });
    }

    private void showTip(String s) {
        mToast.setText(s);
        mToast.show();
    }


    private void findId() {
        mic = findViewById(R.id.voiceImageView);
        mic.setOnClickListener(this);


        mToast = Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.voiceImageView:
                if (null == mNumPwdSegs) {
                    // 首次注册密码为空时，调用下载密码
                    downloadPwd();
                }
                break;
        }
    }


    /**
     * 下载密码
     */
    private void downloadPwd() {
        authid = "123456";
        if (TextUtils.isEmpty(authid)) {
            showTip("请输入authid");
            return;
        }
        // 获取密码之前先终止之前的操作
        mIdVerifier.cancel();
        mNumPwd = null;
        // 下载密码时，按住说话触摸无效
        mic.setClickable(false);

        showTip("下载中...");

        // 设置下载密码参数
        // 清空参数
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置会话场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ivp");

        // 子业务执行参数，若无可以传空字符传
        StringBuffer params = new StringBuffer();
        // 设置模型操作的密码类型
        params.append("pwdt=" + mPwdType + ",");
        // 执行密码下载操作
        int ret = mIdVerifier.execute("ivp", "download", params.toString(), mDownloadPwdListener);
        if (ret != 0)
            showTip("下载完成");
    }


//    private String getAuthid(){
//        String id = mAuthidEditText.getText()==null?null:mAuthidEditText.getText().toString();
//        return id;
//    }


    /**
     * 下载密码监听器
     */
    private IdentityListener mDownloadPwdListener = new IdentityListener() {

        @Override
        public void onResult(IdentityResult result, boolean islast) {
            mic.setClickable(true);
            switch (mPwdType) {
                case PWD_TYPE_NUM:
                    StringBuffer numberString = new StringBuffer();
                    try {
                        JSONObject object = new JSONObject(result.getResultString());
                        if (!object.has("num_pwd")) {
                            mNumPwd = null;
                            return;
                        }

                        JSONArray pwdArray = object.optJSONArray("num_pwd");
                        numberString.append(pwdArray.get(0));
                        for (int i = 1; i < pwdArray.length(); i++) {
                            numberString.append("-" + pwdArray.get(i));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mNumPwd = numberString.toString();
                    mNumPwdSegs = mNumPwd.split("-");

                    // TODO:展示注册密码
                    showTip(mNumPwd);
//                    mResultEditText.setText("您的注册密码：\n" + mNumPwd + "\n请长按“按住说话”按钮进行注册\n");
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }

        @Override
        public void onError(SpeechError error) {
            // 下载密码时，恢复按住说话触摸
            // 下载密码时，恢复按住说话触摸
            mic.setClickable(true);
            showTip("下载失败");
        }
    };
}