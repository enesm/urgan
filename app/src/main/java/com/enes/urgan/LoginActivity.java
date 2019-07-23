package com.enes.urgan;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

public class LoginActivity extends AppCompatActivity {

    EditText viewEditTextEposta;
    EditText viewEditTextSifre;
    static TextView viewTextErrorMesaji;
    static TextView viewTextRegisterSuccess;
    static ProgressBar viewProgressLogin;
    static Context context;
    static Activity activity;
    static Button viewButtonGiris;
    static Button viewButtonKayit;
    ImageButton viewButtonQr;
    SharedPreferences sharedPreferences;
    Gson gson = new Gson();
    static boolean ACTIVITY_MUST_WAIT = false;

    protected static void setActivityBusy(boolean state) {
        ACTIVITY_MUST_WAIT = state;
        if (state) {
            // Uygulama cevap icin beklemeli. Butonlar devre disi.
            viewProgressLogin.setVisibility(View.VISIBLE);
            viewButtonGiris.setEnabled(false);
            viewButtonKayit.setEnabled(false);
        } else {
            // Uygulama hazir. Butonlar aktif.
            viewProgressLogin.setVisibility(View.INVISIBLE);
            viewButtonGiris.setEnabled(true);
            viewButtonKayit.setEnabled(true);
        }
    }

    Runnable setActivityState(final boolean state) {
        Runnable r = new Runnable() {
            public void run() {
                clearInfoElements();
                setActivityBusy(state);
            }
        };
        return r;
    }

    public boolean getActivityBusy() {
        return ACTIVITY_MUST_WAIT;
    }

    public void beginSession(Account account, boolean firstTime) {
        String json = gson.toJson(account);
        if (firstTime) {
            sharedPreferences = context.getSharedPreferences("loginPreferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("account", json);
            editor.putString("email", account.getAccountName());
            editor.commit();
        }
        Intent main = new Intent(context, MainActivity.class);
        main.putExtra("account", json);
        context.startActivity(main);
        activity.finish();
    }

    protected void girisYap() {
        String inputMail = viewEditTextEposta.getText().toString();
        String inputSifre = viewEditTextSifre.getText().toString();

/*        if (inputMail.equals("admin@urgan.com") && inputSifre.equals("password")) {
            viewTextErrorMesaji.setVisibility(View.INVISIBLE);
            viewProgressLogin.setVisibility(View.VISIBLE);
            Account acc = new Account(0, sessionId, inputMail, 3);
            beginSession(acc, true);
        } else {
            viewTextErrorMesaji.setVisibility(View.VISIBLE);
        }*/

        AccountAPI api = new AccountAPI(setActivityState(true), setActivityState(false));
        api.execute("login", inputMail, inputSifre);
    }

    protected boolean checkInputMail(EditText inputMail) {
        String mail = inputMail.getText().toString();
        boolean durum = mail.contains("@") && mail.contains(".com") && mail.length() > 7;
        if (!durum) {
            inputMail.setError("Lütfen geçerli bir e-posta adresi girin.");
            inputMail.requestFocus();
        }
        return durum;
    }

    protected boolean checkInputParola(EditText inputParola) {
        String parola = inputParola.getText().toString();
        boolean durum = !parola.trim().isEmpty() && parola.length() > 3;
        if (!durum) {
            inputParola.setError("Parolanız en az 4 karakterden oluşmalı.");
            inputParola.requestFocus();
        }
        return durum;
    }

    public static void createDialog(String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setNeutralButton("Tamam", null);
        builder.show();
    }

    protected static void clearInfoElements() {
        viewTextRegisterSuccess.setVisibility(View.INVISIBLE);
        viewTextErrorMesaji.setVisibility(View.INVISIBLE);
        viewProgressLogin.setVisibility(View.INVISIBLE);
    }

    public static void setVisibleLoginError(boolean state) {
        if (state) {
            clearInfoElements();
            viewTextErrorMesaji.setVisibility(View.VISIBLE);
        } else viewTextErrorMesaji.setVisibility(View.INVISIBLE);
    }

    public static void setVisibleRegisterSuccess(boolean state) {
        if (state) {
            clearInfoElements();
            viewTextRegisterSuccess.setVisibility(View.VISIBLE);
        } else viewTextRegisterSuccess.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = LoginActivity.this;
        activity = (Activity) context;
        SharedPreferences sp = getSharedPreferences("loginPreferences", Context.MODE_PRIVATE);
        if (sp.contains("account")) {
            String json = sp.getString("account", "");
            Account acc = gson.fromJson(json, Account.class);
            beginSession(acc, false);
        } else {
            setContentView(R.layout.activity_login);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

            viewEditTextEposta = (EditText) findViewById(R.id.inputMail);
            viewEditTextSifre = (EditText) findViewById(R.id.inputSifre);
            viewTextErrorMesaji = (TextView) findViewById(R.id.text_login_error);
            viewTextRegisterSuccess = (TextView) findViewById(R.id.text_register_success);
            viewProgressLogin = (ProgressBar) findViewById(R.id.progress_giris);
            viewButtonGiris = (Button) findViewById(R.id.button_giris);
            viewButtonKayit = (Button) findViewById(R.id.button_kayit);
            viewButtonQr = (ImageButton) findViewById(R.id.button_qr);

            // Acilista gorunmez yap
            clearInfoElements();
            // Butonlar oturum kapatildiktan sonra devre disi olabilir. Aktif et.
            setActivityBusy(false);

            // Onceden basarili bir session varsa eposta adresini otomatik yaz.
            if (sp.contains("email")) {
                viewEditTextEposta.setText(sp.getString("email", ""));
                viewEditTextSifre.requestFocus();
            }

            viewButtonGiris.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!ACTIVITY_MUST_WAIT) {
                        if (checkInputMail(viewEditTextEposta) && checkInputParola(viewEditTextSifre)) {
                            clearInfoElements();
                            girisYap();
                        }
                    }
                }
            });

            viewButtonKayit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!ACTIVITY_MUST_WAIT) {
                        if (checkInputMail(viewEditTextEposta) && checkInputParola(viewEditTextSifre)) {
                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            clearInfoElements();
                                            register();
                                            break;

                                        case DialogInterface.BUTTON_NEGATIVE:
                                            //Aksiyon yok
                                            break;
                                    }
                                }
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            builder.setMessage(viewEditTextEposta.getText().toString() + "\n\n" + getResources().getString(R.string.prompt_hesap_ac)).setPositiveButton(getResources().getString(R.string.evet), dialogClickListener)
                                    .setNegativeButton(getResources().getString(R.string.hayir), dialogClickListener).show();
                        }
                    }
                }
            });
        }
    }

    protected void register() {
        AccountAPI api = new AccountAPI(setActivityState(true), setActivityState(false));
        api.execute("register", viewEditTextEposta.getText().toString(), viewEditTextSifre.getText().toString());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            // Navbar ve status bar gizle
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }
}
