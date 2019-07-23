package com.enes.urgan;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class AdapterItemList extends BaseAdapter {

    private LayoutInflater inflater;
    private Activity activity;
    private List<Item> itemList;
    private String sessionId;
    private final String[] ayListesi = {"", "Oca", "Şub", "Mar", "Nis", "May", "Haz", "Tem", "Ağu", "Eyl", "Eki", "Kas", "Ara"};
    private final boolean DEBUG = true;

    AdapterItemList(Activity activity, List<Item> itemler, String session) {
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        itemList = itemler;
        sessionId = session;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        @SuppressLint({"ViewHolder", "InflateParams"}) final View satirView = inflater.inflate(R.layout.layout_listview_adapter_new, null);
        Item item = itemList.get(position);

        TextView textEntryName = satirView.findViewById(R.id.text_entry_name);
        TextView textTaglar = satirView.findViewById(R.id.text_taglar);
        TextView textUpdateTime = satirView.findViewById(R.id.text_update_time);
        TextView textItemContent = satirView.findViewById(R.id.text_ambar_content);
        ImageView imageEntry = satirView.findViewById(R.id.image_entry);
        Button buttonAyarlar = satirView.findViewById(R.id.button_entry_ayar);
        Button buttonGit = satirView.findViewById(R.id.button_entry_git);
        Button buttonIndir = satirView.findViewById(R.id.button_entry_indir);
        Button buttonKopyala = satirView.findViewById(R.id.button_entry_kopyala);

        textEntryName.setText(item.getIsim());
        try {
            String tarih = item.getUpdate().split(" ")[0];
            String saat = item.getUpdate().split(" ")[1].substring(0, 5);
            String yil = tarih.split("-")[0];
            String ay = ayListesi[Integer.valueOf(tarih.split("-")[1])];
            String gun = tarih.split("-")[2];
            String updateTime = gun + " " + ay + " " + yil + " " + saat;
            textUpdateTime.setText(updateTime);
        } catch (Exception e) {
            if (DEBUG) Log.e("AdapterItemList", "Time bilgisi yok.");
        }

        ArrayList taglar = item.getTaglar();
        textTaglar.setText("");
        if (taglar.size() > 0) {
            for (int i = 0; i < taglar.size(); i++) {
                String dump = textTaglar.getText().toString();
                String newStr = dump + "#" + taglar.get(i) + " ";
                textTaglar.setText(newStr);
            }
        } else {
            // TODO: Hic tag olmamasi durumu.
            textTaglar.setVisibility(View.GONE);
        }


        switch (item.getTip()) {
            case 1: // item tip : not
                buttonKopyala.setVisibility(View.VISIBLE);
                textItemContent.setVisibility(View.VISIBLE);
                textItemContent.setText(item.getContent());
                imageEntry.setImageResource(R.drawable.ic_note);
                break;
            case 2: // item tip : link
                buttonGit.setVisibility(View.VISIBLE);
                textItemContent.setVisibility(View.VISIBLE);
                textItemContent.setText(item.getContent());
                imageEntry.setImageResource(R.drawable.ic_link);
                break;
            case 3: // item tip : img
                buttonIndir.setVisibility(View.VISIBLE);
                Uri imgUri = Uri.parse(item.getContent());
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imgUri);
                    imageEntry.setImageBitmap(bitmap);
                } catch (Exception e) {
                    if (DEBUG) Log.e("AdapterItemList imgEntrySetError", e.toString());
                    imageEntry.setImageResource(android.R.drawable.ic_dialog_alert);
                }
                /*try {
                    String imgBase64 = item.getContent();
                    Log.e("imgBase64", imgBase64);
                    byte[] decodedString = Base64.decode(imgBase64, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    imageEntry.setImageBitmap(decodedByte); // TODO: Gelen base64 eksik.
                } catch (Exception e) {
                    Log.e("itemTipgetImageError", e.toString());
                }*/
                break;
            default:
                break;
        }

        final Item finalItem = item;
        final Context context = satirView.getContext();
        final String changeName = context.getResources().getString(R.string.baslik_degistir);
        final String changeContent = context.getResources().getString(R.string.icerik_degistir);
        final String changeTag = context.getResources().getString(R.string.taglari_degistir);
        final String delete = context.getResources().getString(R.string.sil);
        buttonAyarlar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] actions;
                if (finalItem.getTip() == 3) {
                    actions = new String[]{changeName, changeTag, delete};
                } else {
                    actions = new String[]{changeName, changeContent, changeTag, delete};
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(finalItem.getIsim());
                builder.setItems(actions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String secim = actions[which];
                        if (secim.equals(changeName)) { // degistir baslik
                            updateEntry(finalItem, "title");
                        } else if (secim.equals(changeContent)) { // degistir icerik
                            updateEntry(finalItem, "content");
                        } else if (secim.equals(changeTag)) { // degistir tag
                            editTaglar(context, finalItem.getId(), finalItem.getTaglar());
                        } else if (secim.equals(delete)) {  // sil
                            deleteEntry(finalItem.getId(), context);
                        }
                    }
                });
                builder.show();
            }
        });

        buttonKopyala.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard(finalItem.getIsim(), finalItem.getContent(), context);
            }
        });

        buttonGit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser(satirView.getContext(), finalItem.getContent());
            }
        });

        buttonIndir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent main = new Intent(context, FullscreenImage.class);
                main.putExtra("imgUri", finalItem.getContent());
                context.startActivity(main);
            }
        });

        return satirView;
    }

    private void updateEntry(final Item item, final String updateName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        switch (updateName) {
            case "title":
                builder.setMessage(activity.getResources().getString(R.string.prompt_enter_new_title));
                break;
            case "content":
                builder.setMessage(activity.getResources().getString(R.string.prompt_enter_new_content));
                break;
            default:
                builder.setMessage(activity.getResources().getString(R.string.prompt_enter_new_unknown));
                break;
        }

        // Set up the input
        final EditText input = new EditText(activity);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        if (updateName.equals("title")) {
            input.setText(item.getIsim());
        } else if (updateName.equals("content")) {
            input.setText(item.getContent());
        }
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(activity.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newEntryName = input.getText().toString();
                if (newEntryName.trim().isEmpty()) {
                    AmbarActivity.showMessage("Lütfen geçerli bir isim belirleyin.");
                } else if (item.getTip() == 2 && !(newEntryName.startsWith("www.") || newEntryName.endsWith(".com") || newEntryName.contains("."))) {
                    AmbarActivity.showMessage("Bu düzgün bir adres gibi görünmüyor.");
                } else {
                    ItemAPI itemapi = new ItemAPI(new ItemAPI.AsyncResponse() {
                        @Override
                        public void processFinish(JSONObject response) {
                            AmbarActivity.showProgress(false);
                            if (DEBUG) Log.e("AdapterItemList entryTitleRename", response.toString());
                            try {
                                if (response.getString("$success").equals("true")) {
                                    AmbarActivity.fillAmbar();
                                    AmbarActivity.showMessage(activity.getResources().getString(R.string.prompt_success));
                                } else {
                                    AmbarActivity.showMessage(activity.getResources().getString(R.string.prompt_fail));
                                }
                            } catch (JSONException e) {
                                if (DEBUG) Log.e("AdapterItemList entryTitleRename JSONError", e.toString());
                            }
                        }

                        @Override
                        public void processStart() {
                            AmbarActivity.showProgress(true);
                        }
                    });
                    itemapi.execute(sessionId, "updateEntry", item.getId(), updateName, newEntryName);
                }
            }
        });
        builder.setNegativeButton(activity.getResources().getString(R.string.iptal), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void deleteEntry(final String entryId, Context context) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        ItemAPI itemapi = new ItemAPI(new ItemAPI.AsyncResponse() {
                            @Override
                            public void processFinish(JSONObject response) {
                                AmbarActivity.showProgress(false);
                                if (DEBUG) Log.e("AdapterItemList deleteEntryResponse", response.toString());
                                try {
                                    if (response.getString("$success").equals("true")) {
                                        AmbarActivity.showMessage(activity.getResources().getString(R.string.prompt_success));
                                        AmbarActivity.fillAmbar();
                                    } else {
                                        AmbarActivity.showMessage(activity.getResources().getString(R.string.prompt_fail));
                                    }
                                } catch (JSONException e) {
                                    if (DEBUG) Log.e("AdapterItemList deleteEntry JSONError", e.toString());
                                }
                            }

                            @Override
                            public void processStart() {
                                AmbarActivity.showProgress(true);
                            }
                        });
                        itemapi.execute(sessionId, "deleteEntry", entryId);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //Aksiyon yok
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(activity.getResources().getString(R.string.prompt_delete_entry)).setPositiveButton(activity.getResources().getString(R.string.evet), dialogClickListener)
                .setNegativeButton(activity.getResources().getString(R.string.hayir), dialogClickListener).show();
    }

    private void editTaglar(final Context context, final String itemId, final ArrayList<String> entryTaglar) {
        final ArrayList<String> taglarId = new ArrayList<>();
        final ArrayList<String> taglar = new ArrayList<>();

        ItemAPI itemapi = new ItemAPI(new ItemAPI.AsyncResponse() {
            @Override
            public void processFinish(JSONObject response) {
                AmbarActivity.showProgress(false);
                try {
                    if (response.getString("$success").equals("true")) {
                        if (response.getString("$data").equals("null")) {
                            AmbarActivity.showMessage(context.getResources().getString(R.string.error_unknown)); // islem basarili fakat data bos
                        } else {
                            JSONArray jsonArray = new JSONArray(response.getString("$data"));
                            for (int i = 0; i < jsonArray.length(); i++) {
                                Log.e("tagNameId", jsonArray.getJSONObject(i).getString("name") + jsonArray.getJSONObject(i).getString("id"));
                                if (jsonArray.getJSONObject(i).getString("deleted").equals("0")) {
                                    taglar.add(jsonArray.getJSONObject(i).getString("name"));
                                    taglarId.add(jsonArray.getJSONObject(i).getString("id"));
                                }
                            }
                            if (taglar.size() == 0 || taglarId.size() == 0) {
                                AmbarActivity.showMessage(context.getResources().getString(R.string.tag_empty));
                            } else {
                                editTaglarShowList(context, taglar, taglarId, itemId, entryTaglar);
                            }
                        }
                    } else {
                        AmbarActivity.showMessage(context.getResources().getString(R.string.prompt_fail));
                    }
                } catch (JSONException e) {
                    if (DEBUG) Log.e("AdapterItemList EditTaglarJSONError", e.toString());
                }
            }

            @Override
            public void processStart() {
                AmbarActivity.showProgress(true);
            }
        });
        itemapi.execute(sessionId, "getAllTag");
    }

    private void editTaglarShowList(final Context context, ArrayList<String> tagName, ArrayList<String> tagId, final String itemId, ArrayList<String> entryTaglar) {
        final ArrayList<Integer> tagIdSecilen = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String[] taglar = new String[tagName.size()];
        taglar = tagName.toArray(taglar);
        String[] taglarId = new String[tagId.size()];
        taglarId = tagId.toArray(taglarId);
        boolean[] checkedValues = new boolean[taglar.length];
        Log.e("entryTaglar", entryTaglar.toString());
        for (int a=0; a<entryTaglar.size(); a++) {
            int index = tagName.indexOf(entryTaglar.get(a));
            checkedValues[index] = true;
        }
        builder.setTitle(context.getResources().getString(R.string.taglari_degistir))
                .setMultiChoiceItems(taglar, checkedValues,
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
                            Log.d("TagSecimi", String.valueOf(a.get(a.keyAt(i))));
                            if(a.get(a.keyAt(i))) {
                                tagIdSecilen.add(Integer.valueOf(finalTaglarId[a.keyAt(i)]));
                            }
                        }
                        Log.e("tagIdSecilen", tagIdSecilen.toString());
                        //ListView has boolean array like {1=true, 3=true}, that shows checked items

                        ItemAPI api = new ItemAPI(new ItemAPI.AsyncResponse() {
                            @Override
                            public void processFinish(JSONObject response) {
                                AmbarActivity.showProgress(false);
                                try {
                                    if (response.getString("$success").equals("true")) {
                                        AmbarActivity.showMessage(context.getResources().getString(R.string.prompt_success));
                                        AmbarActivity.fillAmbar();
                                    } else {
                                        AmbarActivity.showMessage(context.getResources().getString(R.string.prompt_fail));
                                    }
                                } catch (JSONException e) {
                                    Log.e("updateEntryTagJSONError", e.toString());
                                }
                            }

                            @Override
                            public void processStart() {
                                AmbarActivity.showProgress(true);
                            }
                        });
                        for(int o=0; o<tagIdSecilen.size(); o++) {
                            api.addTagId(tagIdSecilen.get(o));
                        }
                        api.execute(sessionId, "updateEntryTag", itemId);
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

    private void copyToClipboard(String title, String content, Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(title, content);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Panoya kopyalandı.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openBrowser(Context activity, String url) {
        Log.e("BrowserRequest", url);
        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
            url = "http://" + url;
            Log.e("BrowserRequestAfterIf", url);
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        activity.startActivity(browserIntent);
    }

    /*private String encodeImage(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();

        return Base64.encodeToString(b, Base64.DEFAULT);
    }*/
}
