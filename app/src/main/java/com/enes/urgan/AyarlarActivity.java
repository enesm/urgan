package com.enes.urgan;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AyarlarActivity extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup containter, @Nullable Bundle savedInstanceState){
        getActivity().setTitle(getResources().getString(R.string.title_activity_ayarlar));
        return inflater.inflate(R.layout.activity_ayarlar, containter, false);
    }
}
