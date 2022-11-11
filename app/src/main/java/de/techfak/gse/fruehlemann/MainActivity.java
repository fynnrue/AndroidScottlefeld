package de.techfak.gse.fruehlemann;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import com.google.android.material.snackbar.Snackbar;

import android.widget.EditText;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onStartClick(View view) {
        Spinner dropdown = (Spinner) findViewById(R.id.dropdown);

        if(dropdown.getSelectedItem() == null){
            Snackbar.make(view, "Keine Karte ausgewählt!", Snackbar.LENGTH_SHORT).show();
        }
    }
}
