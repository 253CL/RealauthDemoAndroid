package com.chuanglan.alive.demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.chuanglan.alive.demo.R;
import com.chuanglan.alive.demo.entities.IdCardEntity;

public class IdCardOcrResultActivity extends Activity {

    private String name;
    private String idCardNo;
    private TextView tvName;
    private TextView tvSex;
    private TextView tvNation;
    private TextView tvBirthday;
    private TextView tvAddress;
    private TextView tvIdNum;
    private TextView tvIssuingAuthority;
    private TextView tvExpireDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_card_ocr_result);
        findViewById(R.id.img_btn_back).setOnClickListener(v -> finish());
        tvName = findViewById(R.id.tvName);
        tvSex = findViewById(R.id.tvSex);
        tvNation = findViewById(R.id.tvNation);
        tvBirthday = findViewById(R.id.tvBirthday);
        tvAddress = findViewById(R.id.tvAddress);
        tvIdNum = findViewById(R.id.tvIdNum);
        tvIssuingAuthority = findViewById(R.id.tvIssuingAuthority);
        tvExpireDate = findViewById(R.id.tvExpireDate);

        initData();
    }

    private void initData() {
        try {
            Intent intent = getIntent();
            String result = intent.getStringExtra("result");

            IdCardEntity idCardEntity = new Gson().fromJson(result, IdCardEntity.class);
            IdCardEntity.DataBean data = idCardEntity.getData();
            IdCardEntity.DataBean.FrontBean front = data.getFront();
            IdCardEntity.DataBean.BackBean back = data.getBack();

            Object frontMsg = front.getMsg();
            if (frontMsg != null && !"不完整".equals(frontMsg)) {
                Toast.makeText(getApplicationContext(), "正面" + frontMsg, Toast.LENGTH_SHORT).show();
            } else {
                name = front.getName();
                String address = front.getAddress();
                String sex = front.getSex();
                String nation = front.getNation();
                String birthday = front.getBrith_day();
                idCardNo = front.getId_card_no();

                tvName.setText(name);
                tvSex.setText(sex);
                tvNation.setText(nation);
                tvBirthday.setText(birthday);
                tvAddress.setText(address);
                tvIdNum.setText(idCardNo);
            }

            Object backMsg = back.getMsg();
            if (backMsg != null && !"不完整".equals(backMsg)) {
                Toast.makeText(getApplicationContext(), "背面" + backMsg, Toast.LENGTH_SHORT).show();
            } else {
                String issuingAuthority = back.getIssuing_authority();
                String issuingDate = back.getIssuing_date();
                String expireDate = back.getExpire_date();

                tvIssuingAuthority.setText(issuingAuthority);
                tvExpireDate.setText(issuingDate + "-" + expireDate);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}