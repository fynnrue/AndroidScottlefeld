// Setzen der Spinner Auswahlmöglichkeiten von: https://stackoverflow.com/a/5241720

package de.techfak.gse.fruehlemann;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {
    // Dropdown Menü zur Karten auswahl
    Spinner dropdown;
    // Variable um wiederkehrende Strings besser zu behandeln
    String noSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dropdown = findViewById(R.id.dropdown);
        noSelection = "Karte auswählen";

        Field[] maps = R.raw.class.getFields();
        String[] mapNames = new String[maps.length + 1];
        mapNames[0] = noSelection;
        for (int i = 0; i < maps.length; i++) {
            mapNames[i + 1] = maps[i].getName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mapNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);
    }

    public void onStartClick(View view) {
        try {
            checkSelected();

            String selectedMap = dropdown.getSelectedItem().toString();

            Snackbar.make(view, "Spiel startet", Snackbar.LENGTH_SHORT).show();

            Intent gameI = new Intent(MainActivity.this, GameActivity.class);
            gameI.putExtra("map", selectedMap);
            startActivity(gameI);

            Log.i("Ausgewählte Karte", selectedMap);
        } catch (NoMapSelectedException noMap) {
            Snackbar.make(view, "Keine Karte ausgewählt!", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void checkSelected() throws NoMapSelectedException {
        if (dropdown.getSelectedItem().equals(noSelection)) {
            throw new NoMapSelectedException("Keine Karte ausgewählt!");
        }
    }

    private class NoMapSelectedException extends Exception {
        public NoMapSelectedException(String errorMessage) {
            super(errorMessage);
        }
    }
}
