package de.techfak.gse.fruehlemann;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.google.android.material.snackbar.Snackbar;

import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {
    Spinner dropdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dropdown = findViewById(R.id.dropdown);

        Field[] maps = R.raw.class.getFields(); //alle Maps erhalten
        String[] mapNames = new String[maps.length+1];
        mapNames[0] = "Karte auswählen";
        for(int i = 0; i < maps.length; i++){
            mapNames[i+1] = maps[i].getName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mapNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);
    }

    public void onStartClick(View view) {
        if(dropdown.getSelectedItem() == "Karte auswählen"){
            Snackbar.make(view, "Keine Karte ausgewählt!", Snackbar.LENGTH_SHORT).show();
        }
    }
}
