package com.enes.urgan;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ItemAPI extends AsyncTask<String, String, JSONObject> {

    private final String root = "http://vachammer.com/urgan/urgan/model/";
    private List<String> sonucKumesi = new ArrayList<>();
    private JSONArray secilenTagId = new JSONArray();

    ItemAPI(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    public interface AsyncResponse {
        void processFinish(JSONObject response);

        void processStart();
    }

    public AsyncResponse delegate = null;

    public void addTagId(int tagId) {
        secilenTagId.put(tagId);
    }

    private JSONObject getAllEntry(String hash) {
        JSONObject json = new JSONObject();
        try {
            URL url = new URL(root + "entry/getAll");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("BASEAUTH", hash);
            //conn.setDoOutput(true); // POST method ile kullan
            /*DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            json.put("BASEAUTH", strings[0]);
            wr.writeBytes(json.toString());
            wr.flush();
            wr.close();*/
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                sonucKumesi.add(line);
            }
            reader.close();
            try {
                json = new JSONArray(sonucKumesi.toString()).getJSONObject(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            Log.e("ItemAPI-getAllEntryError", e.toString());
        }
        return json;
    }

    private JSONObject postEntry(String hash, String title, String content, int type) {
        // type --> 1:not 2:link 3:img
        JSONObject json = new JSONObject();
        try {
            URL url = new URL(root + "entry/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("BASEAUTH", hash);
            conn.setDoOutput(true); // POST method ile kullan
            //DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
            json.put("title", title);
            json.put("content", content);
            json.put("type", type);
            if (secilenTagId.length() > 0) {
                json.put("tagsToEntry", secilenTagId);
            }
            wr.write(json.toString());
            Log.e("atilanistek", json.toString());
            wr.flush();
            wr.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                sonucKumesi.add(line);
            }
            reader.close();
            try {
                json = new JSONArray(sonucKumesi.toString()).getJSONObject(0);
            } catch (JSONException e) {
                Log.e("ItemAPI postEntryJSONArrayError", e.toString());
            }
        } catch (Exception e) {
            Log.e("ItemAPI postEntryError", e.toString());
        }
        return json;
    }

    private JSONObject deleteTag(String hash, String tagId) {
        JSONObject json = new JSONObject();
        try {
            URL url = new URL(root + "tag/" + tagId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("BASEAUTH", hash);
            //conn.setDoOutput(true); // POST method ile kullan
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
            json.put("deleted", "1");
            wr.write(json.toString());
            wr.flush();
            wr.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                sonucKumesi.add(line);
            }
            reader.close();
            try {
                json = new JSONArray(sonucKumesi.toString()).getJSONObject(0);
            } catch (JSONException e) {
                Log.e("ItemAPI deleteTagErrorJSON", e.toString());
            }
        } catch (Exception e) {
            Log.e("ItemAPI deleteTagError", e.toString());
        }
        return json;
    }

    private JSONObject updateEntryTag(String hash, String entryId) {
        JSONObject json = new JSONObject();
        try {
            URL url = new URL(root + "tagToEntry/setAll/" + entryId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("BASEAUTH", hash);
            conn.setDoOutput(true); // POST method ile kullan
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
            json.put("tags", secilenTagId);
            wr.write(json.toString());
            wr.flush();
            wr.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                sonucKumesi.add(line);
            }
            reader.close();
            try {
                json = new JSONArray(sonucKumesi.toString()).getJSONObject(0);
            } catch (JSONException e) {
                Log.e("ItemAPI updateEntryTagErrorJSON", e.toString());
            }
        } catch (Exception e) {
            Log.e("ItemAPI updateEntryTagError", e.toString());
        }
        return json;
    }

    private JSONObject updateEntry(String hash, String entryId, String updateType, String newValue, String... isDel) {
        JSONObject json = new JSONObject();
        try {
            URL url = new URL(root + "entry/" + entryId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (isDel.length > 0) {
                conn.setRequestMethod("DELETE");
            } else {
                conn.setRequestMethod("PUT");
            }
            conn.setRequestProperty("BASEAUTH", hash);
            //conn.setDoOutput(true); // POST method ile kullan
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
            json.put(updateType, newValue);
            wr.write(json.toString());
            wr.flush();
            wr.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                sonucKumesi.add(line);
            }
            reader.close();
            try {
                json = new JSONArray(sonucKumesi.toString()).getJSONObject(0);
            } catch (JSONException e) {
                Log.e("ItemAPI updateEntryErrorJSON", e.toString());
            }
        } catch (Exception e) {
            Log.e("ItemAPI updateEntryError", e.toString());
        }
        return json;
    }

    private JSONObject renameTag(String hash, String tagId, String newName) {
        JSONObject json = new JSONObject();
        try {
            URL url = new URL(root + "tag/" + tagId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("BASEAUTH", hash);
            //conn.setDoOutput(true); // POST method ile kullan
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
            json.put("name", newName);
            wr.write(json.toString());
            wr.flush();
            wr.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                sonucKumesi.add(line);
            }
            reader.close();
            try {
                json = new JSONArray(sonucKumesi.toString()).getJSONObject(0);
            } catch (JSONException e) {
                Log.e("ItemAPI renameTagErrorJSON", e.toString());
            }
        } catch (Exception e) {
            Log.e("ItemAPI renameTagError", e.toString());
        }
        return json;
    }

    private JSONObject postTag(String hash, String name, String colorCode, int deletable) {
        JSONObject json = new JSONObject();
        try {
            URL url = new URL(root + "tag/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("BASEAUTH", hash);
            conn.setDoOutput(true); // POST method ile kullan
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
            json.put("name", name);
            json.put("colorCode", colorCode);
            json.put("deletable", deletable);
            wr.write(json.toString());
            wr.flush();
            wr.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                sonucKumesi.add(line);
            }
            reader.close();
            try {
                json = new JSONArray(sonucKumesi.toString()).getJSONObject(0);
            } catch (JSONException e) {
                Log.e("ItemAPI postTagError", e.toString());
            }
        } catch (Exception e) {
            Log.e("ItemAPI postTagError", e.toString());
        }
        return json;
    }

    private JSONObject getAllTag(String hash) {
        JSONObject json = new JSONObject();
        try {
            URL url = new URL(root + "tag/getAll");
            Log.d("url", url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("BASEAUTH", hash);
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                sonucKumesi.add(line);
            }
            reader.close();
            try {
                json = new JSONArray(sonucKumesi.toString()).getJSONObject(0);
            } catch (JSONException e) {
                Log.e("ItemAPI getAllTagJSONArrayError", e.toString());
            }
        } catch (Exception e) {
            Log.e("ItemAPI getAllTagError", e.toString());
        }
        return json;
    }

    private JSONObject tagToEntry(String hash, int tagId, int entryId) {
        JSONObject json = new JSONObject();
        try {
            URL url = new URL(root + "tagToEntry/");
            Log.d("url", url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("BASEAUTH", hash);
            conn.setDoOutput(true); // POST method ile kullan
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
            json.put("tagId", tagId);
            json.put("entryId", entryId);
            wr.write(json.toString());
            wr.flush();
            wr.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                sonucKumesi.add(line);
            }
            reader.close();
            try {
                json = new JSONArray(sonucKumesi.toString()).getJSONObject(0);
            } catch (JSONException e) {
                Log.e("ItemAPI tagToEntryJSONArrayError", e.toString());
            }
        } catch (Exception e) {
            Log.e("ItemAPI tagToEntryError", e.toString());
        }
        return json;
    }

    @Override
    protected JSONObject doInBackground(String... strings) {
        // strings yapısı her zaman = [ "hash", "command", "geri kalan params"str .. ]
        JSONObject json = new JSONObject();
        switch (strings[1]) {
            case "getAllEntry":
                json = getAllEntry(strings[0]);
                break;
            case "postEntry":
                json = postEntry(strings[0], strings[2], strings[3], Integer.valueOf(strings[4]));
                break;

            case "getAllTag":
                json = getAllTag(strings[0]);
                break;

            case "postTag":
                json = postTag(strings[0], strings[2], strings[3], Integer.valueOf(strings[4]));
                break;

            case "renameTag":
                json = renameTag(strings[0], strings[2], strings[3]);
                break;

            case "deleteTag":
                json = deleteTag(strings[0], strings[2]);
                break;

            case "tagToEntry":
                json = tagToEntry(strings[0], Integer.valueOf(strings[2]), Integer.valueOf(strings[3]));
                break;

            case "updateEntry":
                json = updateEntry(strings[0], strings[2], strings[3], strings[4]);
                break;

            case "deleteEntry":
                json = updateEntry(strings[0], strings[2], "", "", "1");
                break;

            case "updateEntryTag":
                json = updateEntryTag(strings[0], strings[2]);
        }
        return json;
    }

    @Override
    protected void onPreExecute() {
        delegate.processStart();
    }

    @Override
    protected void onPostExecute(JSONObject respond) {
        /*activityReady.run();
        try {
            Log.e("respond", URLDecoder.decode(respond.toString(), "utf-8"));
            JSONObject json = new JSONArray(sonucKumesi.toString()).getJSONObject(1);
            Log.e("ItemAPI response", json.getString("title"));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            if (respond.getString("$success").equals("true")) {
                showDialogWithFinisher.run("İşlem başarıyla tamamlandı.");
            } else UploadActivity.showMessage("Yükleme başarısız oldu.");
        } catch (JSONException e) {
            UploadActivity.showMessage("Yükleme başarısız oldu.\n\nDebug mesajı: " + e.toString());
        }*/
        secilenTagId = new JSONArray();
        delegate.processFinish(respond);
    }

}
