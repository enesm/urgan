package com.enes.urgan;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TaglarActivity extends Fragment {

    AlertDialog.Builder alertDialog;
    ProgressDialog progressDialog;
    ListView listTaglar;
    ArrayList<String> taglar = new ArrayList<String>();
    ArrayList<String> taglarId = new ArrayList<String>();
    AdapterTagList adapter;
    TextView textEmptyTagList;
    static String sessionId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup containter, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle(getResources().getString(R.string.title_activity_taglar));
        return inflater.inflate(R.layout.activity_taglar, containter, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle hash = getArguments();
        sessionId = hash.getString("hash");
        Log.d("sessionIdToTag", sessionId);
        alertDialog = new AlertDialog.Builder(getContext());
        progressDialog = new ProgressDialog(getContext());
        listTaglar = (ListView) getActivity().findViewById(R.id.list_taglar);
        adapter = new AdapterTagList(getActivity(), taglar, taglarId);
        textEmptyTagList = getActivity().findViewById(android.R.id.empty);
        listTaglar.setEmptyView(textEmptyTagList);
        ImageButton buttonTagEkle = (ImageButton) getActivity().findViewById(R.id.button_taglar_ekle);

        //getDefaultTaglar();
        getTaglar();

        listTaglar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String secilenTagId = adapter.getTagId(position);
                String[] actions = {getResources().getString(R.string.yeniden_adlandir), getResources().getString(R.string.sil)};

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(adapter.getItem(position));
                builder.setItems(actions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e("Tag Aksiyon Seçimi", String.valueOf(which));
                        switch (which) {
                            case 0: // rename
                                tagRename(Integer.valueOf(secilenTagId));
                                break;
                            case 1: // sil
                                tagDelete(Integer.valueOf(secilenTagId));
                                break;
                        }
                    }
                });
                builder.show();
            }
        });

        buttonTagEkle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagEkle();
            }
        });

        /*ArrayList<String> taglar = new ArrayList<String>();
        taglar.add("#Link");
        taglar.add("#Fotoğraf");
        taglar.add("#Not");
        taglar.add("#Dersler");
        taglar.add("#Fikirler");
        for (int i = 0; i < 50; i++) {
            taglar.add("#For Döngülü Tag");
        }
        fillTaglar(taglar);*/
    }

    private void tagDelete(final int id) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        ItemAPI itemapi = new ItemAPI(new ItemAPI.AsyncResponse() {
                            @Override
                            public void processFinish(JSONObject response) {
                                showProgressDialog(false);
                                Log.e("tagDeleteResponse", response.toString());
                                try {
                                    if (response.getString("$success").equals("true")) {
                                        showMessage(getResources().getString(R.string.prompt_success));
                                        getTaglar();
                                    } else {
                                        showMessage(getResources().getString(R.string.prompt_fail));
                                    }
                                } catch (JSONException e) {
                                    Log.e("tagDeleteProcessFinish-JSONError", e.toString());
                                }
                            }

                            @Override
                            public void processStart() {
                                showProgressDialog(true, getResources().getString(R.string.prompt_sunucuya_yukleniyor));
                            }
                        });
                        itemapi.execute(sessionId, "deleteTag", String.valueOf(id));
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //Aksiyon yok
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getResources().getString(R.string.prompt_delete_tag)).setPositiveButton(getResources().getString(R.string.evet), dialogClickListener)
                .setNegativeButton(getResources().getString(R.string.hayir), dialogClickListener).show();
    }

    private void tagRename(final int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources().getString(R.string.prompt_enter_new_tag_name));

        // Set up the input
        final EditText input = new EditText(getActivity());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newTagName = input.getText().toString();
                if (newTagName.trim().isEmpty()) {
                    showMessage("Lütfen geçerli bir isim belirleyin.");
                } else {
                    ItemAPI itemapi = new ItemAPI(new ItemAPI.AsyncResponse() {
                        @Override
                        public void processFinish(JSONObject response) {
                            showProgressDialog(false);
                            Log.e("tagRenameResponse", response.toString());
                            try {
                                if (response.getString("$success").equals("true")) {
                                    showMessage(getResources().getString(R.string.prompt_success));
                                    getTaglar();
                                } else {
                                    showMessage(getResources().getString(R.string.prompt_fail));
                                }
                            } catch (JSONException e) {
                                Log.e("tagRenameProcessFinish-JSONError", e.toString());
                            }
                        }

                        @Override
                        public void processStart() {
                            showProgressDialog(true, getResources().getString(R.string.prompt_sunucuya_yukleniyor));
                        }
                    });
                    itemapi.execute(sessionId, "renameTag", String.valueOf(id), newTagName);
                }
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.iptal), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void getTaglar() {
        ItemAPI itemapi = new ItemAPI(new ItemAPI.AsyncResponse() {
            @Override
            public void processFinish(JSONObject response) {
                showProgressDialog(false);
                Log.e("getTaglar", response.toString());
                try {
                    if (response.getString("$success").equals("true")) {
                        taglarId = new ArrayList<>();
                        taglar = new ArrayList<>();
                        if (response.getString("$data").equals("null")) {
                        } else {
                            JSONArray jsonArray = new JSONArray(response.getString("$data"));
                            Log.e("getTaglarProcessFinishDataNotNull", response.getString("$data"));
                            for (int i = 0; i < jsonArray.length(); i++) {
                                Log.e("tagNameId", jsonArray.getJSONObject(i).getString("name") + jsonArray.getJSONObject(i).getString("id"));
                                if (jsonArray.getJSONObject(i).getString("deleted").equals("0")) {
                                    taglar.add(jsonArray.getJSONObject(i).getString("name"));
                                    taglarId.add(jsonArray.getJSONObject(i).getString("id"));
                                }
                            }
                            fillTaglar(getActivity());
                        }
                    } else {
                        showMessage(getResources().getString(R.string.prompt_fail));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void processStart() {
                showProgressDialog(true, getResources().getString(R.string.prompt_yanit_bekleniyor));
            }
        });
        itemapi.execute(sessionId, "getAllTag");
    }

    private void tagEkle() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources().getString(R.string.tag_ekle_isim));

        // Set up the input
        final EditText input = new EditText(getActivity());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String tagName = input.getText().toString();
                if (tagName.trim().isEmpty()) {
                    showMessage("Lütfen geçerli bir isim belirleyin.");
                } else {
                    ItemAPI itemapi = new ItemAPI(new ItemAPI.AsyncResponse() {
                        @Override
                        public void processFinish(JSONObject response) {
                            showProgressDialog(false);
                            Log.e("tagEkleProcessFinish", response.toString());
                            try {
                                if (response.getString("$success").equals("true")) {
                                    showMessage(getResources().getString(R.string.prompt_success));
                                    JSONObject jsonObject = new JSONObject(response.getString("$data"));
                                    taglar.add(jsonObject.getString("name"));
                                    taglarId.add(jsonObject.getString("id"));
                                    // adapter.notifyDataSetChanged();
                                    getTaglar();
                                } else showMessage(getResources().getString(R.string.prompt_fail));
                            } catch (JSONException e) {
                                Log.e("tagEkleProcessFinishJSONError", e.toString());
                                showMessage(getResources().getString(R.string.prompt_fail));
                            }
                        }

                        @Override
                        public void processStart() {
                            showProgressDialog(true, getResources().getString(R.string.prompt_sunucuya_yukleniyor));
                        }
                    });
                    itemapi.execute(sessionId, "postTag", tagName, "#000000", "1");
                }
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.iptal), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void fillTaglar(Activity activity) {
        adapter = new AdapterTagList(activity, taglar, taglarId);
        listTaglar.setAdapter(adapter);
    }

    private void getDefaultTaglar() {
        taglar.add(getResources().getString(R.string.tag_name_not));
        taglar.add(getResources().getString(R.string.tag_name_link));
        taglar.add(getResources().getString(R.string.tag_name_img));
        fillTaglar(getActivity());
    }

    private void showProgressDialog(boolean state, String... msg) {
        if (state) {
            progressDialog.setMessage(msg[0]);
            progressDialog.setCancelable(false);
            progressDialog.show();
        } else {
            progressDialog.dismiss();
        }
    }

    private void showMessage(String msg) {
        alertDialog.setMessage(msg);
        alertDialog.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Tamam butonuna basılınca yapılacaklar
            }
        });
        alertDialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        progressDialog.dismiss();
    }
}
