// Setting Spinner options from: https://stackoverflow.com/a/5241720

package de.techfak.gse.fruehlemann.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.lang.reflect.Field;

import de.techfak.gse.fruehlemann.R;
import de.techfak.gse.fruehlemann.exceptions.NoMapSelectedException;

public class MainActivity extends AppCompatActivity {
    Spinner dropdown;
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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mapNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);
    }

    public void onStartClick(View view) {
        if (isMapSelected()) {
            String selectedMap = dropdown.getSelectedItem().toString();

            Snackbar.make(view, "Spiel startet", Snackbar.LENGTH_SHORT).show();

            Intent gameI = new Intent(MainActivity.this, GameActivity.class);
            gameI.putExtra("map", selectedMap);
            startActivity(gameI);

            Log.i("Ausgewählte Karte", selectedMap);
        } else {
            Snackbar.make(view, "Keine Karte ausgewählt!", Snackbar.LENGTH_SHORT).show();
        }
    }

    private boolean isMapSelected() {
            try {
                checkSelected();
                return true;
            } catch (NoMapSelectedException exception) {
                Log.i("Exception", "No map selected when tried to start game!");
                exception.printStackTrace();
            }
        return false;
    }

    private void checkSelected() throws NoMapSelectedException {
        if(dropdown.getSelectedItem().equals(noSelection)) {
            throw new NoMapSelectedException("No map Selected");
        }
    }

}
