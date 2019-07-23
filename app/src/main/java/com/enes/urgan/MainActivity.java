package com.enes.urgan;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    TextView navBarHesapAdi;
    TextView navBarHesapSeviyeAdi;
    NavigationView navigationView;
    Gson gson = new Gson();
    Account acc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String json = intent.getStringExtra("account");
        acc = gson.fromJson(json, Account.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        onNavigationItemSelected(navigationView.getMenu().getItem(0));  // baslangicta amabari ac
        navigationView.getMenu().getItem(0).setChecked(true);           // secili yap
        //listview_item_doldur();
    }

    public void updateNavBar() {
        navBarHesapAdi.setText(acc.getAccountName());
        navBarHesapSeviyeAdi.setText(acc.getAccountLevelName());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        Log.d("onCreateOptionsMenu", "True");
        navBarHesapAdi = findViewById(R.id.navBarOturumAdi);
        navBarHesapSeviyeAdi = findViewById(R.id.navBarHesapSeviyesi);
        updateNavBar();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        int idAyarlarOnNav = 2; // Ayarlar seceneginin nav drawer daki indexi

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            onNavigationItemSelected(navigationView.getMenu().getItem(idAyarlarOnNav));
            navigationView.getMenu().getItem(idAyarlarOnNav).setChecked(true);
            return true;
        } else if (id == R.id.action_exit_session) {
            exitSession();
        } else if (id == R.id.action_filtre) {
            AmbarActivity.filterTaglar();
        } else if (id == R.id.action_filtre_temizle) {
            AmbarActivity.clearFilterTaglar();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment = null;
        boolean needFragmentUpdate = false;
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            fragment = new AmbarActivity();
            Bundle hash = new Bundle();
            hash.putString("hash", acc.getSessionId());
            fragment.setArguments(hash);
            needFragmentUpdate = true;
        } else if (id == R.id.nav_tag) {
            fragment = new TaglarActivity();
            Bundle hash = new Bundle();
            hash.putString("hash", acc.getSessionId());
            fragment.setArguments(hash);
            needFragmentUpdate = true;
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_ayarlar) {
            fragment = new AyarlarActivity();
            needFragmentUpdate = true;
        } else if (id == R.id.nav_logoff) {
            exitSession();
        }
        if (needFragmentUpdate) {
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.content_main, fragment).commit();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void exitSession() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        exitSessionNow();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //Aksiyon yok
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.prompt_oturum_kapat)).setPositiveButton(getResources().getString(R.string.evet), dialogClickListener)
                .setNegativeButton(getResources().getString(R.string.hayir), dialogClickListener).show();
    }

    public void exitSessionNow() {
        SharedPreferences sp = getSharedPreferences("loginPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("account");
        editor.apply();
        Intent login = new Intent(getBaseContext(), LoginActivity.class);
        startActivity(login);
        finish();
    }
}
