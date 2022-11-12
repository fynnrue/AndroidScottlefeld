// Setzen der Spinner Auswahlmöglichkeiten von: https://stackoverflow.com/a/5241720

package de.techfak.gse.fruehlemann;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {
    // Dropdown Menü zur Karten auswahl
    Spinner dropdown;
    // Variable um wiederkehrende Strings besser zu behandeln
    String keineAuswahl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dropdown = findViewById(R.id.dropdown);
        keineAuswahl = "Karte auswählen";

        Field[] maps = R.raw.class.getFields();
        String[] mapNames = new String[maps.length + 1];
        mapNames[0] = keineAuswahl;
        for (int i = 0; i < maps.length; i++) {
            mapNames[i + 1] = maps[i].getName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mapNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);
    }

    public void onStartClick(View view) {
        if (dropdown.getSelectedItem().equals(keineAuswahl)) {
            Snackbar.make(view, "Keine Karte ausgewählt!", Snackbar.LENGTH_SHORT).show();
        }
    }
}
