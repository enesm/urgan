package com.enes.urgan;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class AccountAPI extends AsyncTask<String, String, JSONObject> {

    private List<String> sonucKumesi = new ArrayList<>();
    private Runnable progressWait;
    private Runnable progressDone;
    private final static boolean DEBUG = true;

    AccountAPI(Runnable progressWait, Runnable progressDone) {
        this.progressWait = progressWait;
        this.progressDone = progressDone;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressWait.run();
    }

    @Override
    protected void onPostExecute(JSONObject a) {
        progressDone.run();
        if (DEBUG) Log.e("AccountAPI Respond", a.toString());
        try {
            if (a.getString("$success").equals("true") && a.getString("$path").contains("user")) {
                LoginActivity.setVisibleRegisterSuccess(true);
            } else if (a.getString("$success").equals("false")) {
                LoginActivity.createDialog("Hata", a.getString("$message"));
            } else if (a.getString("$success").equals("true") && a.getString("$path").contains("login")) {
                // Giris yapildi. uygulama yeni intent icin beklemeli. Butonlar devre disi.
                progressWait.run();
                JSONObject jsonHash = new JSONObject(a.getString("$data"));
                JSONObject jsonUser = new JSONObject(jsonHash.getString("user"));
                Account account = new Account(jsonUser.getInt("id"), jsonHash.getString("hash"), jsonUser.getString("email"), 0);
                // TODO: DB account level int ve default deger 0
                LoginActivity loginActivity = new LoginActivity();
                loginActivity.beginSession(account, true);
            }
        } catch (JSONException e) {
            if (DEBUG) Log.e("AccountAPI postExecute JSONError", e.toString());
            LoginActivity.createDialog("Hata", "Sunucuyla bağlantı kurulamadı.");
        }
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        String URL_CON;
        if (params[0].equals("login")) {
            URL_CON = "http://vachammer.com/urgan/urgan/model/session/login";
        } else {
            URL_CON = "http://vachammer.com/urgan/urgan/model/user/";
        }
        JSONObject json = new JSONObject();
        try {
            json.put("email", params[1]);
            json.put("password", params[2]);
        } catch (JSONException e) {
            if (DEBUG) Log.e("AccountAPI doInBackground JSONError", e.toString());
        }

        try {
            URL url = new URL(URL_CON);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(json.toString());
            wr.flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                sonucKumesi.add(line);
            }
            wr.close();
            reader.close();
            try {
                json = new JSONArray(sonucKumesi.toString()).getJSONObject(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) Log.e("AccountAPI doInBackground Error", e.toString());
        }
        return json;
    }
}
