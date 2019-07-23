package com.enes.urgan;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AmbarActivity extends Fragment {

    static List<Item> itemler = new ArrayList<Item>();
    static ArrayList<Integer> filteredTagIdleri = new ArrayList<Integer>();
    static ArrayList<String> tagName = new ArrayList<String>();
    static ArrayList<String> tagId = new ArrayList<String>();
    static TextView textEmptyAmbar;
    static ListView listAmbar;
    static AdapterItemList adapter;
    static ProgressDialog progressDialog;
    static AlertDialog.Builder alertDialog;
    static String sessionId;
    static ImageButton uploadButton;
    static Context context;
    static Activity staticActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup containter, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle(getResources().getString(R.string.title_activity_main));
        return inflater.inflate(R.layout.activity_ambar, containter, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle hash = getArguments();
        sessionId = hash.getString("hash");
        Log.d("sessionIdToAmbar", sessionId);
        Log.d("filteredTaglar", filteredTagIdleri.toString());
        context = getContext();
        staticActivity = getActivity();

        progressDialog = new ProgressDialog(getActivity());
        alertDialog = new AlertDialog.Builder(getContext());
        uploadButton = getActivity().findViewById(R.id.button_upload);
        listAmbar = (ListView) getActivity().findViewById(R.id.list_ambar);
        textEmptyAmbar = (TextView) getActivity().findViewById(android.R.id.empty);
        listAmbar.setEmptyView(textEmptyAmbar);
        adapter = new AdapterItemList(getActivity(), itemler, sessionId);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent upload = new Intent(getContext(), UploadActivity.class);
                upload.putExtra("hash", sessionId);
                startActivityForResult(upload, 1);
            }
        });

        fillAmbar();
    }

    public static void fillAmbar() {
        final Activity activity = staticActivity;
        ItemAPI itemapi = new ItemAPI(new ItemAPI.AsyncResponse() {
            @Override
            public void processFinish(JSONObject response) {
                showProgress(false);
                Log.e("AmbarProcessFinish", response.toString());
                try {
                    if (response.getString("$success").equals("true")) {
                        itemler = new ArrayList<Item>();
                        boolean isMulti = response.getString("$multi").equals("true");
                        if (isMulti) {
                            JSONArray jsonArray = new JSONArray(response.getString("$data"));
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                                String title = jsonObject.getString("title");
                                String content = jsonObject.getString("content");
                                String createdAt = jsonObject.getString("createdAt");
                                Log.e("ambarActivity JSON entryToTags", jsonObject.getJSONArray("entryToTags").toString());
                                JSONArray jsonArrayTaglar = jsonObject.getJSONArray("entryToTags");
                                int type = Integer.valueOf(jsonObject.getString("type"));
                                Item item = new Item(title, content, type);
                                item.setUpdate(createdAt); // TODO: createdAt ve updateAt verisi yanlis eslestirildi.
                                item.setId(jsonObject.getString("id"));
                                ArrayList taglar = new ArrayList<String>();
                                for (int k = 0; k < jsonArrayTaglar.length(); k++) {
                                    JSONObject tag = new JSONObject();
                                    tag = jsonArrayTaglar.getJSONObject(k).getJSONObject("tag");
                                    if (tag.getString("deleted").equals("0")) {
                                        taglar.add(tag.getString("name"));
                                    }
                                }
                                item.setTaglar(taglar);
                                if (filteredTagIdleri.size() == 0) {
                                    itemler.add(item);
                                    textEmptyAmbar.setText(context.getResources().getText(R.string.entry_empty));
                                } else {
                                    Log.e("filteredTagIdleri", filteredTagIdleri.toString());
                                    Log.e("tagId", tagId.toString());
                                    Log.e("tagName", tagName.toString());
                                    textEmptyAmbar.setText(context.getResources().getText(R.string.entry_empty_with_filter));
                                    for (int j = 0; j < filteredTagIdleri.size(); j++) {
                                        int index = tagId.indexOf(String.valueOf(filteredTagIdleri.get(j)));
                                        String tagWithIndex = tagName.get(index);
                                        if (item.tagVarMi(tagWithIndex)) {
                                            if (!itemler.contains(item)) {
                                                itemler.add(item);
                                            }
                                        }
                                    }
                                }
                            }
                            adapter = new AdapterItemList(activity, itemler, sessionId);
                            listAmbar.setAdapter(adapter);
                        }
                    } else if (response.getString("$success").equals("false") && response.getString("$data").equals("null")){
                        // bos oldugu zaman entryler false donuyor.
                        itemler = new ArrayList<Item>();
                        adapter = new AdapterItemList(activity, itemler, sessionId);
                        listAmbar.setAdapter(adapter);
                    }
                } catch (JSONException e) {
                    Log.e("fillAmbarJSONObjectError", e.toString());
                }
            }

            @Override
            public void processStart() {
                showProgress(true);
            }
        });
        itemapi.execute(sessionId, "getAllEntry");
    }

    public static void clearFilterTaglar() {
        filteredTagIdleri.clear();
        fillAmbar();
    }

    public static void filterTaglar() {

        ItemAPI itemapi = new ItemAPI(new ItemAPI.AsyncResponse() {
            @Override
            public void processFinish(JSONObject response) {
                showProgress(false);
                try {
                    if (response.getString("$success").equals("true")) {
                        tagId.clear();
                        tagName.clear();
                        filteredTagIdleri.clear();
                        if (response.getString("$multi").equals("true")) {
                            JSONArray data = response.getJSONArray("$data"); // multi ise null olamaz.
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject dataObj = data.getJSONObject(i);
                                if (dataObj.getString("deleted").equals("0")) {
                                    tagName.add(dataObj.getString("name"));
                                    tagId.add(dataObj.getString("id"));
                                }
                            }
                            Log.d("ambarGetTaglarProcessFinishMultiTrue", data.toString());
                        } else {
                            JSONObject data = response.getJSONObject("$data"); // TODO: null gelebilir.
                            if (data.getString("deleted").equals("0")) {
                                tagName.add(data.getString("name"));
                                tagId.add(data.getString("id"));
                            }
                            Log.d("getTaglarProcessFinishMultiFalse", data.toString());
                        }
                        if (tagName.size() == 0 || tagId.size() == 0) {
                            AmbarActivity.showMessage(context.getResources().getString(R.string.tag_empty));
                        } else {
                            filterTagSelect();
                        }
                    } else {
                        showMessage(context.getResources().getString(R.string.prompt_fail));
                    }
                    Log.e("Taglar", tagName.toString() + " " + tagId.toString());
                } catch (JSONException e) {
                    Log.e("ambarPickTaglarJSONError", e.toString());
                }

            }

            @Override
            public void processStart() {
                showProgress(true);
            }
        });
        itemapi.execute(sessionId, "getAllTag");
    }

    private static void filterTagSelect() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String[] taglar = new String[tagName.size()];
        taglar = tagName.toArray(taglar);
        String[] taglarId = new String[tagId.size()];
        taglarId = tagId.toArray(taglarId);
        // TODO: dialog kapatilip acilinca önceden secilenler ekranda gorunsun.
        builder.setTitle(context.getResources().getString(R.string.title_filtre))
                .setMultiChoiceItems(taglar, null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            public void onClick(DialogInterface dialog, int position, boolean isChecked) {
                            }
                        });
        final String[] finalTaglarId = taglarId;
        builder.setPositiveButton(context.getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ListView list = ((android.app.AlertDialog) dialog).getListView();
                        SparseBooleanArray a = list.getCheckedItemPositions();
                        Log.d("TagSecimi", list.getCheckedItemPositions().toString());
                        for (int i = 0; i < a.size(); i++) {
                            filteredTagIdleri.add(Integer.valueOf(finalTaglarId[a.keyAt(i)]));
                        }
                        Log.d("Secilen Tag ID", filteredTagIdleri.toString());
                        fillAmbar();
                        //ListView has boolean array like {1=true, 3=true}, that shows checked items
                    }
                });

        builder.setNegativeButton(context.getResources().getString(R.string.iptal),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                fillAmbar();
            }
        }
    }

    @Override
    public void onDestroy() {
        progressDialog.dismiss();
        super.onDestroy();
    }

    Runnable openProgress() {
        return new Runnable() {
            @Override
            public void run() {
                showProgress(true);
            }
        };
    }

    Runnable closeProgress() {
        return new Runnable() {
            @Override
            public void run() {
                showProgress(false);
            }
        };
    }

    public static void showProgress(boolean state) {
        if (state) {
            progressDialog.setMessage("Verileriniz getirliyor.");
            progressDialog.setCancelable(false);
            progressDialog.show();
        } else {
            progressDialog.dismiss();
        }
    }

    public static void showMessage(String msg) {
        alertDialog.setMessage(msg);
        alertDialog.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Tamam butonuna basılınca yapılacaklar
            }
        });
        alertDialog.show();
    }
}
