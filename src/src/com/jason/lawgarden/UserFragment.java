package com.jason.lawgarden;

import java.text.SimpleDateFormat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jason.lawgarden.db.DataBaseHelper;
import com.jason.lawgarden.model.User;

public class UserFragment extends Fragment {
    private DataBaseHelper mDbHelper;
    private User mUser;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new DataBaseHelper(getActivity());
        mDbHelper.openDataBase();

        mUser = mDbHelper.getUserInfo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_info_layout, null);
        TextView txtUserName = (TextView) view.findViewById(R.id.txt_user_name);
        TextView txtServiceType = (TextView) view.findViewById(R.id.txt_service_type);
        TextView txtPurchaseType = (TextView) view.findViewById(R.id.txt_purchase_type);
        TextView txtOverdueDate = (TextView) view.findViewById(R.id.txt_overdue_date);
        TextView txtAboutUs = (TextView) view.findViewById(R.id.txt_about_us_content);

        txtUserName.setText(getString(R.string.txt_user_name_format_text, mUser.getUserName()));
        txtServiceType.setText(getString(R.string.txt_service_type_format_text,
                mUser.getServiceType()));
//        txtOverdueDate.setText(getString(R.string.txt_overdue_date_format_text,
//                simpleDateFormat.format(mUser.getOverdueDate())));
        txtAboutUs.setText(mUser.getAboutUs());
        return view;
    }

}
