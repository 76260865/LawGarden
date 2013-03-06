package com.jason.lawgarden;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.jason.lawgarden.MainActivity.SubjectCallBack;
import com.jason.lawgarden.db.DataBaseHelper;
import com.jason.lawgarden.model.Subject;

public class LawsFragement extends Fragment {
    private static final String TAG = "LawsFragement";

    public static final String EXTRA_KEY_SUBJECT_ID = "extra_subject_id";

    private int mSubjectId;

    private DataBaseHelper mDbHelper;

    private ArrayList<Subject> mSubjects = new ArrayList<Subject>();

    private SubjectCallBack mSubjectCallBack;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new DataBaseHelper(getActivity());

        mSubjectId = getArguments().getInt(EXTRA_KEY_SUBJECT_ID, 0);
        mSubjects = mDbHelper.getSubjectsByParentId(mSubjectId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.law_list_layout, container, false);
        ListView listLaw = (ListView) view.findViewById(R.id.list_law);
        listLaw.setAdapter(new LawsAdapter(mSubjects));
        listLaw.setOnItemClickListener(mOnItemClickListener);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubjectCallBack = null;
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int postion, long id) {
            if (mSubjectCallBack != null) {
                mSubjectCallBack.openSubjectByParentId(mSubjects.get(postion).getId());
            }
        }
    };

    public void setSubjectCallBack(SubjectCallBack callBack) {
        mSubjectCallBack = callBack;
    }

    private class LawsAdapter extends BaseAdapter {
        private ArrayList<Subject> mSubjects;

        public LawsAdapter(ArrayList<Subject> subjects) {
            mSubjects = subjects;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
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
                        R.layout.law_list_item_layout, null);
            }
            return convertView;
        }

    }
}
