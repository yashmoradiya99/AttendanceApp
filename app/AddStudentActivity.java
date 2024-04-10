import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddStudentActivity extends AppCompatActivity {
    private EditText nameEditText;
    private EditText rollNumberEditText;
    private Button addButton;
    private DatabaseReference studentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        // Initialize Firebase Realtime Database reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        studentsRef = database.getReference("students");

        // Initialize UI elements
        nameEditText = findViewById(R.id.editTextName);
        rollNumberEditText = findViewById(R.id.editTextRollNumber);
        addButton = findViewById(R.id.addButton);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get student information from EditText fields
                String name = nameEditText.getText().toString().trim();
                int rollNumber = Integer.parseInt(rollNumberEditText.getText().toString().trim());

                // Create a Student object
                Student student = new Student(name, rollNumber);

                // Push the student object to Firebase Realtime Database
                studentsRef.push().setValue(student);

                // Optionally, you can clear the EditText fields after adding the student
                nameEditText.setText("");
                rollNumberEditText.setText("");
            }
        });
    }
}
