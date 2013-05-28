package com.jason.lawgarden;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jason.lawgarden.db.DataBaseHelper;
import com.jason.lawgarden.model.PurchaseSubject;
import com.jason.lawgarden.model.User;
import com.jason.util.JsonUtil;
import com.jason.util.NetworkUtil;

public class UserFragment extends Fragment implements OnClickListener {
    private static final String TAG = "UserFragment";
    private User mUser;

    private Button mBtnUpdate;

    private DataBaseHelper mDbHelper;

    private FragmentManager mFragmentManager;

    private ArticleFragement mArticleFragement;

    private ArticleListFragment mArticleListFragment;

    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = JsonUtil.sUser;
        mDbHelper = DataBaseHelper.getSingleInstance(getActivity());
        mFragmentManager = getActivity().getSupportFragmentManager();
    }

    private ListView mListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_info_layout, null);
        TextView txtUserName = (TextView) view.findViewById(R.id.txt_user_name);

        txtUserName.setText(getString(R.string.txt_user_name_format_text, mUser.getUserName()));
        mBtnUpdate = (Button) view.findViewById(R.id.btn_update);
        mBtnUpdate.setOnClickListener(this);
        ((Button) view.findViewById(R.id.btn_logout)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDbHelper.deleteUser();
                getActivity().finish();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });
        mListView = (ListView) view.findViewById(R.id.list_subjects);
        new QueryPurchaseSubjectsTask().execute();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mArticleFragement != null) {
            mArticleListFragment.getView().setVisibility(View.GONE);
            mArticleFragement.getView().setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mArticleListFragment = (ArticleListFragment) mFragmentManager
                .findFragmentById(R.id.fragment_detail_article_list);
        mArticleFragement = (ArticleFragement) mFragmentManager
                .findFragmentById(R.id.fragment_detail_article);
    }

    private class QueryPurchaseSubjectsTask extends AsyncTask<Void, Void, String> {

        ArrayList<PurchaseSubject> subjects = new ArrayList<PurchaseSubject>();

        @Override
        protected String doInBackground(Void... params) {
            subjects = mDbHelper.getPurchaseSubjects();
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            MyAdapter adpater = new MyAdapter(subjects);
            mListView.setAdapter(adpater);
        }
    }

    class MyAdapter extends BaseAdapter {

        ArrayList<PurchaseSubject> mSubjects = new ArrayList<PurchaseSubject>();

        MyAdapter(ArrayList<PurchaseSubject> subjects) {
            mSubjects = subjects;
        }

        @Override
        public int getCount() {
            return mSubjects.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(
                        R.layout.purchase_subject_item_layout, null);
            }

            PurchaseSubject subject = mSubjects.get(position);
            TextView txtOverdueDate = (TextView) convertView.findViewById(R.id.txt_overdue_date);
            String str = TextUtils.isEmpty(subject.getName()) ? "" : subject.getName();
            txtOverdueDate.setText(str + " 截至" + mSimpleDateFormat.format(subject.getOurdueDate())
                    + "有效");
            return convertView;
        }
    }

    private ProgressDialog mProgressDialog;
    private boolean mIsCaneled = false;
    private TextView mTxtLoadingInfo;
    private Button mBtnOk;
    private Button mBtnCancel;
    private ProgressBar mProgressBar;
    private ProgressBar mProgressLogin;
    private MyAsyncTask mMyAsyncTask;

    private void initDialog() {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        mProgressDialog.setContentView(R.layout.loading_dialog_layout);
        mBtnOk = (Button) mProgressDialog.findViewById(R.id.btn_ok);
        mBtnCancel = (Button) mProgressDialog.findViewById(R.id.btn_cancel);
        mTxtLoadingInfo = (TextView) mProgressDialog.findViewById(R.id.txt_loading_info);
        mProgressBar = (ProgressBar) mProgressDialog.findViewById(R.id.progress_loading);
        mProgressLogin = (ProgressBar) mProgressDialog.findViewById(R.id.progress_login);

        mBtnOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mBtnOk.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                mBtnCancel.setVisibility(View.VISIBLE);
                mProgressLogin.setVisibility(View.GONE);

                mMyAsyncTask = new MyAsyncTask();
                mMyAsyncTask.execute();
            }
        });
        mBtnCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mIsCaneled = true;
                if (mMyAsyncTask == null) {
                    mProgressDialog.dismiss();
                } else {
                    mMyAsyncTask.cancel(true);
                }
                Log.d(TAG, "cancel");
            }
        });
    }

    private class MyAsyncTask extends AsyncTask<Void, Progress, Boolean> {

        @Override
        protected void onProgressUpdate(Progress... values) {
            Progress progress = values[0];
            mProgressBar.setProgress(progress.progress);
            mProgressBar.setMax(progress.total);
            mTxtLoadingInfo.setText(progress.message);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                Progress progress = new Progress();
                if (!mIsCaneled) {
                    progress.progress = 50;
                    progress.message = "同步用户购买专题";
                    publishProgress(progress);
                    JsonUtil.updateUserSubjects(getActivity());
                    progress.progress = 100;
                    progress.message = "完成同步用户购买专题";
                    publishProgress(progress);
                }
                if (!mIsCaneled) {
                    progress.progress = 50;
                    progress.message = "同步专题";
                    publishProgress(progress);
                    JsonUtil.updateSubjects(getActivity());
                    progress.progress = 100;
                    progress.message = "完成同步专题";
                    publishProgress(progress);
                }
                if (!mIsCaneled) {
                    progress.progress = 50;
                    progress.message = "更新新闻";
                    publishProgress(progress);
                    JsonUtil.updateNews(getActivity());
                    progress.progress = 100;
                    progress.message = "新闻更新完成";
                    publishProgress(progress);
                }

                if (!mIsCaneled) {
                    // update the articles
                    progress.progress = 0;
                    progress.message = "更新法条";
                    publishProgress(progress);
                    String lastUpdateTime = mDbHelper.getLastUpdateArticleTime();
                    int pageIndex = 0;
                    int totalPages = JsonUtil.updateArticles(getActivity(), pageIndex,
                            lastUpdateTime);
                    progress.progress = 1;
                    progress.total = totalPages;
                    while (pageIndex < totalPages && !mIsCaneled) {
                        Log.d("LoginActivity", "pageIndex:" + pageIndex + "totalPages:"
                                + totalPages);
                        pageIndex++;
                        JsonUtil.updateArticles(getActivity(), pageIndex, lastUpdateTime);
                        progress.progress = pageIndex;
                        progress.message = "更新法条";
                        publishProgress(progress);
                    }
                }
            } catch (JSONException e) {
                Log.e("UserFragement", e.getMessage());
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mProgressDialog.dismiss();
            if (result) {
                Toast.makeText(getActivity(), "更新完毕", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "登录失败", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onClick(View v) {
        if (!NetworkUtil.isNetworkConnected(getActivity())) {
            Toast.makeText(getActivity(), "请先链接网络", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mProgressDialog == null) {
            initDialog();
        } else if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }

        mTxtLoadingInfo.setText("正在更新...");
        new LoginPwdTask().execute();

    }

    private class LoginPwdTask extends AsyncTask<Void, Void, String> {

        private boolean mHasUpdateData = false;

        @Override
        protected String doInBackground(Void... params) {
            try {
                JSONObject object = JsonUtil.ValidateToken(getActivity());
                if (object == null) {
                    return "服务器异常";
                }
                if (object.getBoolean("ExecutionResult")) {
                    if (!object.getBoolean("Valid")) {
                        return "Token过期";
                    }
                }

                JsonUtil.updatePurchaseSubjects(getActivity());

            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                mHasUpdateData = JsonUtil.CheckAllUpdates(getActivity());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            new QueryPurchaseSubjectsTask().execute();
            if (mHasUpdateData) {
                mTxtLoadingInfo.setText("当前应用有更新数据，你是否要更新？");
                mProgressLogin.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                mBtnOk.setVisibility(View.VISIBLE);
                mBtnCancel.setVisibility(View.VISIBLE);

            } else {
                mProgressDialog.dismiss();
                if (!TextUtils.isEmpty(result)) {
                    Toast.makeText(getActivity(), result + "", Toast.LENGTH_SHORT).show();
                    if ("服务器异常" != result) {
                        getActivity().finish();
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(getActivity(), "已经更新到最新", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    class Progress {
        int progress;
        int total = 100;
        String message;
    }
}
