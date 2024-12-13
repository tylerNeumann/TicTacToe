package fvtc.edu.tictactoe;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SetUser extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    TextView textViewName;
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_user);

        initSubmitButton();
        preferences = getApplicationContext().getSharedPreferences("GamesPreferences", MODE_PRIVATE);
        textViewName = findViewById(R.id.editTextPersonName);
        String username = preferences.getString("username", "human");
        textViewName.setText(username);
        Log.d(TAG, "onCreate: Completed..." + username);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initSubmitButton() {
        Button btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
                SharedPreferences.Editor editor = preferences.edit();  // Begin Trans and open for editing

                String username = textViewName.getText().toString();
                Log.d(TAG, "onClick: " + username);
                // Change it
                editor.putString("username", username);

                // Commit the changes
                editor.commit();
                Log.d(TAG, "onClick: Completed");
            }
        });
    }
}