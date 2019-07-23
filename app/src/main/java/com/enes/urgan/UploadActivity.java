package com.enes.urgan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class UploadActivity extends AppCompatActivity {

    static String sessionId;
    ProgressDialog progressDialog;
    AlertDialog.Builder alertDialog;
    EditText inputTitle;
    EditText inputContent;
    TextView textPickName;
    TextView textContent;
    Spinner spinnerTipler;
    private String[] tipler = {"Not", "Bağlantı", "Resim"};
    LinearLayout layoutContent;
    LinearLayout layoutPick;
    int itemTip;
    Uri selectedImage = null;
    String selectedImageBase64;
    ArrayList<String> tagName;
    ArrayList<String> tagId;
    ArrayList<Integer> tagIdSecilen;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        Intent intent = getIntent();
        sessionId = intent.getStringExtra("hash");

        context = getApplicationContext();

        progressDialog = new ProgressDialog(this);
        alertDialog = new AlertDialog.Builder(this);

        Button buttonPick = findViewById(R.id.button_upload_pick);
        Button buttonYukle = findViewById(R.id.button_upload_yukle);
        Button buttonIptal = findViewById(R.id.button_upload_iptal);
        Button buttonTagEkle = findViewById(R.id.button_upload_tag_sec);
        inputTitle = findViewById(R.id.input_upload_title);
        inputContent = findViewById(R.id.input_upload_content);
        layoutContent = findViewById(R.id.layout_upload_icerik);
        layoutPick = findViewById(R.id.layout_upload_goruntu);
        textPickName = findViewById(R.id.text_upload_pick_name);
        textContent = findViewById(R.id.text_upload_content);
        spinnerTipler = findViewById(R.id.spinner_upload_tipler);
        itemTip = 1; // Seçili olan Not-->1
        tagName = new ArrayList<>();
        tagId = new ArrayList<>();
        tagIdSecilen = new ArrayList<>();
        spinnerFill();

        buttonPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean durum = izinYonetimi();
                if (durum) {
                    Log.d("izin durumu", "true");
                    pickFromGaleri();
                }
            }
        });

        buttonIptal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        buttonYukle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publish();
            }
        });

        buttonTagEkle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTaglar();
            }
        });
    }

    private void publish() {
        ItemAPI itemapi = new ItemAPI(new ItemAPI.AsyncResponse() {

            @Override
            public void processFinish(JSONObject response) {
                Log.e("processFinish", response.toString());
                showProgress(false);
                try {
                    boolean success = response.getString("$success").equals("true");
                    if (success) {
                        showMessageWithFinisher(getResources().getString(R.string.prompt_success));
                        Intent returnIntent = new Intent();
                        setResult(Activity.RESULT_OK, returnIntent);
                    } else {
                        showMessage(getResources().getString(R.string.prompt_fail) + "\nHata mesajı:" + response.getString("$message"));
                        Intent returnIntent = new Intent();
                        setResult(Activity.RESULT_CANCELED, returnIntent);
                    }
                } catch (JSONException e) {
                    Log.e("processFinish JSONError", e.toString());
                    showMessage(getResources().getString(R.string.prompt_fail));
                    Intent returnIntent = new Intent();
                    setResult(Activity.RESULT_CANCELED, returnIntent);
                }
            }

            @Override
            public void processStart() {
                showProgress(true);
            }
        });
        String title = inputTitle.getText().toString();
        if (title.trim().isEmpty()) title = "Başlıksız";
        String content = inputContent.getText().toString();
        for (int j = 0; j < tagIdSecilen.size(); j++) {
            itemapi.addTagId(tagIdSecilen.get(j));
        }
        switch (itemTip) {
            case 1: // Not
                if (content.trim().isEmpty()) {
                    inputContent.setError("Bu alanı boş bırakamazsınız.");
                    inputContent.requestFocus();
                } else {
                    itemapi.execute(sessionId, "postEntry", title, content, "1");
                }
                break;
            case 2: // Bağlantı
                if (content.trim().isEmpty()) {
                    inputContent.setError("Bu alanı boş bırakamazsınız.");
                    inputContent.requestFocus();
                } else if (!isLink(content)) {
                    inputContent.setError("Bu düzgün bir adres gibi görünmüyor.");
                    inputContent.requestFocus();
                } else {
                    Log.e("debug upload", title + content);
                    itemapi.execute(sessionId, "postEntry", title, content, "2");
                }
                break;
            case 3: // Görüntü
                if (selectedImage == null) {
                    Toast.makeText(this, "Lütfen bir görüntü seçin.", Toast.LENGTH_LONG).show();
                } else {
                    itemapi.execute(sessionId, "postEntry", title, selectedImage.toString(), "3");
                }
                break;
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    public void pickFromGaleri() {
        /*Intent pickPhoto = new Intent(Intent.ACTION_OPEN_DOCUMENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, 1);//one can be replaced with any action code*/
        Intent intent;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        }else{
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.tag_name_img)), 1);
    }

     @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        Log.d("onActivityResult", "RUN");
        /*if (requestCode == 1 && resultCode == RESULT_OK) {
            selectedImage = imageReturnedIntent.getData();
            String fileName = getFileName(selectedImage);
            textPickName.setText(fileName);
            final InputStream imageStream;
            try {
                imageStream = getContentResolver().openInputStream(selectedImage);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                selectedImageBase64 = encodeImage(selectedImage);
                Log.e("base64", selectedImageBase64);
            } catch (FileNotFoundException e) {
                Log.e("pickImageEncodeError", e.toString());
            }
            Log.d("pickPict", fileName);
        } else {
            textPickName.setText("");
        }*/
        if (requestCode == 1 && resultCode == RESULT_OK) {
            final Uri imageUri = imageReturnedIntent.getData();
            String fileName = getFileName(imageUri);
            textPickName.setText(fileName);
            selectedImage = imageUri;/*
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                final int takeFlags = imageReturnedIntent.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                ContentResolver resolver = context.getContentResolver();
                resolver.takePersistableUriPermission(imageUri, takeFlags);
            }*/
            /*final InputStream imageStream;
            try {
                assert imageUri != null;
                imageStream = getContentResolver().openInputStream(imageUri);
                this.selectedImage = BitmapFactory.decodeStream(imageStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }*/
        }
    }

    private String encodeImage(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] b = baos.toByteArray();

        return Base64.encodeToString(b, Base64.DEFAULT);
    }


    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void spinnerFill() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_style, tipler);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipler.setAdapter(adapter);
        spinnerTipler.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: // Tip: Not
                        layoutPick.setVisibility(View.GONE);
                        layoutContent.setVisibility(View.VISIBLE);
                        textPickName.setVisibility(View.GONE);
                        textContent.setText(getResources().getText(R.string.content));
                        itemTip = 1;
                        break;
                    case 1: // Tip: Bağlantı
                        layoutPick.setVisibility(View.GONE);
                        layoutContent.setVisibility(View.VISIBLE);
                        textPickName.setVisibility(View.GONE);
                        textContent.setText(getResources().getText(R.string.link));
                        itemTip = 2;
                        break;
                    case 2: // Tip: Görüntü
                        layoutPick.setVisibility(View.VISIBLE);
                        layoutContent.setVisibility(View.GONE);
                        textPickName.setVisibility(View.VISIBLE);
                        itemTip = 3;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void pickTaglar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this);
        String[] taglar = new String[tagName.size()];
        taglar = tagName.toArray(taglar);
        String[] taglarId = new String[tagId.size()];
        taglarId = tagId.toArray(taglarId);
        builder.setTitle(getResources().getString(R.string.prompt_tag_sec))
                .setMultiChoiceItems(taglar, null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            public void onClick(DialogInterface dialog, int position, boolean isChecked) {
                            }
                        });
        final String[] finalTaglarId = taglarId;
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ListView list = ((android.app.AlertDialog) dialog).getListView();
                        SparseBooleanArray a = list.getCheckedItemPositions();
                        Log.d("TagSecimi", list.getCheckedItemPositions().toString());
                        for (int i = 0; i < a.size(); i++) {
                            tagIdSecilen.add(Integer.valueOf(finalTaglarId[a.keyAt(i)]));
                        }
                        Log.d("Secilen Tag ID", tagIdSecilen.toString());
                        //ListView has boolean array like {1=true, 3=true}, that shows checked items
                    }
                });

        builder.setNegativeButton(getResources().getString(R.string.iptal),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    private void getTaglar() {

        ItemAPI itemapi = new ItemAPI(new ItemAPI.AsyncResponse() {
            @Override
            public void processFinish(JSONObject response) {
                showProgress(false);
                try {
                    if (response.getString("$success").equals("true")) {
                        tagId.clear();
                        tagName.clear();
                        tagIdSecilen.clear();
                        if (response.getString("$multi").equals("true")) {
                            JSONArray data = response.getJSONArray("$data"); // multi ise null olamaz.
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject dataObj = data.getJSONObject(i);
                                if (dataObj.getString("deleted").equals("0")) {
                                    tagName.add(dataObj.getString("name"));
                                    tagId.add(dataObj.getString("id"));
                                }
                            }
                            Log.d("getTaglarProcessFinishMultiTrue", data.toString());
                        } else {
                            JSONObject data = response.getJSONObject("$data"); // TODO: null gelebilir.
                            if (data.getString("deleted").equals("0")) {
                                tagName.add(data.getString("name"));
                                tagId.add(data.getString("id"));
                            }
                            Log.d("getTaglarProcessFinishMultiFalse", data.toString());
                        }
                        if (tagId.size()==0 || tagName.size()==0) {
                            showMessage(context.getResources().getString(R.string.tag_empty));
                        } else {
                            pickTaglar();
                        }
                    } else {
                        showMessage(getResources().getString(R.string.prompt_fail));
                    }
                    Log.e("Taglar", tagName.toString() + " " + tagId.toString());
                } catch (JSONException e) {
                    Log.e("pickTaglarJSONError", e.toString());
                }

            }

            @Override
            public void processStart() {
                showProgress(true);
            }
        });
        itemapi.execute(sessionId, "getAllTag");
    }

    public boolean isLink(String url) {
        return url.startsWith("www.") || url.endsWith(".com") || url.contains(".");
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

    private void showMessageWithFinisher(String msg) {
        alertDialog.setMessage(msg);
        alertDialog.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        alertDialog.show();
    }

    private void showProgress(boolean state) {
        if (state) {
            progressDialog.setMessage("Veriler sunucuya aktarılıyor.");
            progressDialog.setCancelable(false);
            progressDialog.show();
        } else {
            progressDialog.dismiss();
        }
    }

    private boolean izinYonetimi() {
        boolean perm = false;
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Görüntü seçebilmek için uygulamanın dosya erişimine izin vermelisiniz.");
            builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //Tamam butonuna basılınca yapılacaklar
                    ActivityCompat.requestPermissions(UploadActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
            });
            builder.show();
        } else perm = true;
        return perm;
    }

    @Override
    public void onDestroy() {
        progressDialog.dismiss();
        super.onDestroy();
    }
}
