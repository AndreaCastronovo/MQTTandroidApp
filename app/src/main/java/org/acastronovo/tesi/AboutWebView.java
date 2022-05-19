package org.acastronovo.tesi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.webkit.WebView;

/**
 *@author Cristian D'Ortona / Andrea Castronovo / Alberto Iantorni
 *
 * TESI DI LAUREA IN INGEGNERIA ELETTRONICA E DELLE TELECOMUNICAZIONI
 *
 */

public class AboutWebView extends AppCompatActivity {

    //Toolbar
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_web_view);

        Intent receivedIntent = getIntent();
        String webUrl = receivedIntent.getStringExtra(StaticResources.WEB_PAGE);

        WebView webView = findViewById(R.id.about_webView);
        webView.loadUrl(webUrl);

        //Toolbar
        toolbar = findViewById(R.id.toolbar_webView);
        setSupportActionBar(toolbar);
        //this is used to include the back arrow on the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_menu_webview, menu);
        return true;
    }

    //this is called whenever the user clicks on the back arrow
    //it works as an UP button
    public boolean onSupportNavigateUp() {
        //this is called when the activity detects the user pressed the back button
        onBackPressed();
        return true;
    }
}
