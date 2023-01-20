package de.techfak.gse.fruehlemann.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import de.techfak.gse.fruehlemann.R;

public class LobbyActivity extends AppCompatActivity {
    String url;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        url = getIntent().getStringExtra("url");
    }
}
