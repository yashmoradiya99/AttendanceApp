package com.example.attendance;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class RegistrationActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etFullName;
    private Button btnRegister;
    private TextView textViewRegistration;

    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etFullName = findViewById(R.id.etFullName);
        btnRegister = findViewById(R.id.btnRegister);
        textViewRegistration = findViewById(R.id.textViewRegistration);

        textViewRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Redirect to the registration activity when the TextView is clicked
                Intent intent = new Intent(RegistrationActivity.this, Login.class);
                startActivity(intent);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                final String fullName = etFullName.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                    Toast.makeText(RegistrationActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Create a new user with email and password
                    mAuth.createUserWithEmailAndPassword(username, password)
                            .addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        // Registration successful
                                        Toast.makeText(RegistrationActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();

                                        // Store a flag indicating that the user has registered
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putBoolean("isRegistered", true);
                                        editor.apply();

                                        startActivity(new Intent(RegistrationActivity.this, Login.class));
                                        finish();
                                    } else {
                                        // Registration failed
                                        Toast.makeText(RegistrationActivity.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }
}
