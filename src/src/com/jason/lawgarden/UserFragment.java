package com.jason.lawgarden;

import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jason.lawgarden.model.User;
import com.jason.util.JsonUtil;

public class UserFragment extends Fragment {
    private User mUser;

    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = JsonUtil.sUser;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_info_layout, null);
        TextView txtUserName = (TextView) view.findViewById(R.id.txt_user_name);
        TextView txtPurchaseDate = (TextView) view.findViewById(R.id.txt_purchase_date);
        TextView txtOverdueDate = (TextView) view.findViewById(R.id.txt_overdue_date);
        TextView txtAboutUs = (TextView) view.findViewById(R.id.txt_about_us_content);

        txtUserName.setText(getString(R.string.txt_user_name_format_text, mUser.getUserName()));
        txtPurchaseDate.setText(getString(R.string.txt_purchase_date_format_text,
                mSimpleDateFormat.format(mUser.getPurchaseDate())));
        txtOverdueDate.setText(getString(R.string.txt_overdue_date_format_text,
                mSimpleDateFormat.format(mUser.getOverdueDate())));
//        txtAboutUs.setText(mUser.getAboutUs());
        return view;
    }

}
