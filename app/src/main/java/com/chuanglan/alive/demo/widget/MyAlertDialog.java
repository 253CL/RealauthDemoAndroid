package com.chuanglan.alive.demo.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.chuanglan.alive.demo.R;


/**
 * Custom global alert dialog
 *
 * @author x-sir :)
 * @date 2021/6/24
 */
public class MyAlertDialog extends Dialog {

    private String title = "温馨提示";
    private String message = "";
    private TextView tvQuit;
    private TextView tvOnceAgain;

    public MyAlertDialog(Context context) {
        super(context);
    }

    public MyAlertDialog(Context context, String title) {
        super(context);
        this.title = title;
    }

    public MyAlertDialog(Context context, String title, String message) {
        super(context);
        this.title = title;
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cl_alive_custom_dialog);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvMessage = findViewById(R.id.tvMessage);
        tvQuit = findViewById(R.id.tvQuit);
        tvOnceAgain = findViewById(R.id.tvOnceAgain);
        if (!TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
        }
        if (!TextUtils.isEmpty(message)) {
            tvMessage.setText(message);
        }
        setCanceledOnTouchOutside(false);
        setCancelable(true);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.dimAmount = 0.8f;
        layoutParams.gravity = Gravity.CENTER;
        getWindow().setAttributes(layoutParams);
        //setOnCancelListener(dialog -> ToastUtils.showShort(getContext(), "取消~"));
        setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (KeyEvent.KEYCODE_BACK == keyCode) {
                    return true;
                }
                return false;
            }
        });
    }

    public void setCancelClickListener(View.OnClickListener listener) {
        if (tvQuit != null) {
            tvQuit.setOnClickListener(listener);
        }
    }

    public void setConfirmClickListener(View.OnClickListener listener) {
        if (tvOnceAgain != null) {
            tvOnceAgain.setOnClickListener(listener);
        }
    }
}
