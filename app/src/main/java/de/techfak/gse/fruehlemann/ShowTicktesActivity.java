package de.techfak.gse.fruehlemann;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ShowTicktesActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showtickets);

        String siggiTickts = getIntent().getStringExtra("Siggi");
        String trainTickts = getIntent().getStringExtra("Train");
        String busTickts = getIntent().getStringExtra("Bus");

        TextView showTickets = findViewById(R.id.textTickets);

        showTickets.setText("Siggi-Bike-Tickets: " + siggiTickts
                + "\nStadtbahn-Tickets: " + trainTickts + "\nBus-Tickets: " + busTickts);

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void onBackClick(View view) {
        finish();
    }
}
