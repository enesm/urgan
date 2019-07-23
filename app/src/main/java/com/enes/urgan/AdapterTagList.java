package com.enes.urgan;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class AdapterTagList extends BaseAdapter {

    private LayoutInflater inflater;
    private List<String> tagNameList;
    private List<String> tagIdList;

    AdapterTagList(Activity activity, List<String> taglar, List<String> taglarId) {
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        tagNameList = taglar;
        tagIdList = taglarId;
        Log.e("taglarIdfromAdapter", taglarId.toString());
    }

    @Override
    public int getCount() {
        return tagNameList.size();
    }

    @Override
    public String getItem(int position) {
        return tagNameList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    String getTagId(int position) {
        return tagIdList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        @SuppressLint({"ViewHolder", "InflateParams"}) View satirView = inflater.inflate(R.layout.layout_listview_tag, null);
        String tagName = tagNameList.get(position);
        String tagId = tagIdList.get(position);

        TextView textTagName = satirView.findViewById(R.id.text_tag_name);

        String tagToShow = "#" + tagName;
        textTagName.setText(tagToShow);

        Log.e("Tag", tagName + " ID: " + tagId);

        return satirView;
    }
}
