package com.guikai.latte.sign;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.guikai.latte.fragments.LatteFragment;
import com.guikai.latte.main.EcBottomFragment;
import com.guikai.latte.net.RestClient;
import com.guikai.latte.net.callback.ISuccess;
import com.guikai.latte.util.log.LogUtils;
import com.guikai.latte.wechat.LatteWeChat;
import com.guikai.latte.wechat.callbacks.IWeChatSignInCallback;
import com.guikai.latteec.R;

import static com.blankj.utilcode.util.BarUtils.getStatusBarHeight;

public class SignInFragment extends LatteFragment implements View.OnClickListener {

    private TextInputEditText mEmail = null;
    private TextInputEditText mPassword = null;
    private ISignListener mISignListener = null;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof ISignListener) {
            mISignListener = (ISignListener) activity;
        }
    }

    @Override
    public Object setLayout() {
        return R.layout.fragment_sign_in;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, @NonNull View root) {
        mEmail = $(R.id.edit_sign_in_email);
        mPassword = $(R.id.edit_sign_in_password);
        $(R.id.btn_sign_in).setOnClickListener(this);
        $(R.id.tv_link_sign_up).setOnClickListener(this);
        $(R.id.icon_sign_in_wechat).setOnClickListener(this);
        $(R.id.tb_sign_in).setPadding(0, getStatusBarHeight(), 0, 0);
    }

    private void onClickSignIn() {
        if (checkFrom()) {
            RestClient.builder()
                    .url("user_profile.json")
                    .params("email", mEmail.getText().toString())
                    .params("password", mPassword.getText().toString())
                    .success(new ISuccess() {
                        @Override
                        public void onSuccess(String response) {
                            LogUtils.json("USER_PROFILE", response);
                            SignHandler.onSignIn(response, mISignListener);
                            getSupportDelegate().startWithPop(new EcBottomFragment());
                        }
                    })
                    .loader(getContext())
                    .build()
                    .post();
        }
    }

    private boolean checkFrom() {
        final String email = mEmail.getText().toString();
        final String password = mPassword.getText().toString();

        boolean isPass = true;

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmail.setError("错误的邮箱格式");
            isPass = false;
        } else {
            mEmail.setError(null);
        }

        if (password.isEmpty() || password.length() < 6) {
            mPassword.setError("请填写至少6位数的密码");
            isPass = false;
        } else {
            mPassword.setError(null);
        }

        return isPass;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_sign_in) {
            onClickSignIn();
        } else if (i == R.id.tv_link_sign_up) {
            onClickLink();
        } else if (i == R.id.icon_sign_in_wechat) {
            onClickWeChat();
        }
    }


    private void onClickLink() {
        getSupportDelegate().start(new SignUpFragment(), SINGLETASK);
    }

    private void onClickWeChat() {
        LatteWeChat.getInstance().onSignSuccess(new IWeChatSignInCallback() {
            @Override
            public void onSignInsuccess(String userInfo) {
                Toast.makeText(getContext(), userInfo, Toast.LENGTH_LONG).show();
                Log.e("xxxx", "userInfo");
            }
        }).signIn();
    }
}
