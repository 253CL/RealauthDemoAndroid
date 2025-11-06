package com.chuanglan.alive.demo.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.chuanglan.alive.demo.R;

public class LoadingDialog extends Dialog {

    private String message = "加载中...";

    public LoadingDialog(Context context) {
        super(context);
    }

    public LoadingDialog(Context context, String message) {
        super(context);
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_loading_dialog);
        TextView tvMessage = findViewById(R.id.message);
        tvMessage.setText(message);
        setCanceledOnTouchOutside(false);
        setCancelable(true);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.dimAmount = 0.2f;
        layoutParams.gravity = Gravity.CENTER;
        getWindow().setAttributes(layoutParams);
        setOnCancelListener(dialog -> {
            Toast.makeText(getContext(), "取消~", Toast.LENGTH_SHORT).show();
        });
    }
}
