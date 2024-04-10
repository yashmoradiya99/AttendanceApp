package com.example.attendance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton fab;
    RecyclerView recyclerView;
    ClassAdapter classAdapter;
    RecyclerView.LayoutManager layoutManager;
    Toolbar toolbar;
    ArrayList<ClassItem> classItems = new ArrayList<>();
    EditText class_edt;
    EditText subject_edt;

    DatabaseReference databaseReference;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView myImageView = findViewById(R.id.myImageView);
        if (myImageView != null) {
            myImageView.setImageResource(R.drawable.ic_launcher_round);
        } else {
            // Handle the case where myImageView is null
        }



        FirebaseApp.initializeApp(this);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Teacher").child("classes");

        fab = findViewById(R.id.fab_main);
        fab.setOnClickListener(v -> showDialog());

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        classAdapter = new ClassAdapter(this, classItems);
        recyclerView.setAdapter(classAdapter);
        classAdapter.setOnItemClickListener(position -> gotoItemActivity(position));
        setToolbar();
        setDatabaseListener();
    }

    private void setDatabaseListener() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                classItems.clear();
                for (DataSnapshot classSnapshot : dataSnapshot.getChildren()) {
                    String rawJsonData = classSnapshot.getValue().toString();
                    Log.d("FirebaseDebug", "Raw JSON Data: " + rawJsonData);

                    // Get the unique key from the snapshot
                    String key = classSnapshot.getKey().toString();
                    Log.d("FirebaseDebug", "Unique Key: " + key);

                    try {
                        ClassItem classItem = classSnapshot.getValue(ClassItem.class);
                        if (classItem != null) {
                            classItems.add(classItem);
                        }
                    } catch (Exception e) {
                        Log.e("FirebaseDebug", "Error converting data: " + e.getMessage());
                    }
                }
                classAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
            }
        });
    }




    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.title_toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);

        title.setText("Add New Class");

        back.setOnClickListener(v -> onBackPressed());
    }

    private void gotoItemActivity(int position) {
        Intent intent = new Intent(this, StudentActivity.class);
        intent.putExtra("ClassName", classItems.get(position).getClassName());
        intent.putExtra("SubjectName", classItems.get(position).getSubjectName());
        intent.putExtra("position", position);
        startActivity(intent);
    }

    @SuppressLint("MissingInflatedId")
    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.class_dialog, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        class_edt = view.findViewById(R.id.class_edt);
        subject_edt = view.findViewById(R.id.subject_edt);

        Button cancel = view.findViewById(R.id.cancel_btn);
        Button add = view.findViewById(R.id.add_btn);

        cancel.setOnClickListener(v -> dialog.dismiss());
        add.setOnClickListener(v -> {
            addClass();
            dialog.dismiss();
        });
    }

    private void addClass() {
        String className = class_edt.getText().toString();
        String subjectName = subject_edt.getText().toString();
        ClassItem classItem = new ClassItem(className, subjectName, databaseReference.getKey());

        // Generate a unique key for the data
        String key = databaseReference.push().getKey();

        // Insert the classItem into the Firebase Realtime Database under the generated key
        databaseReference.child(key).setValue(classItem);

        classItems.add(classItem);
        classAdapter.notifyDataSetChanged();
    }
}
