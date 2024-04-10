package com.example.attendance;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton fab;
    RecyclerView recyclerView;
    ClassAdapter classAdapter;
    RecyclerView.LayoutManager layoutManager;
    Toolbar toolbar;
    ArrayList <ClassItem> classItems= new ArrayList<>();
    EditText class_edt;
    EditText subject_edt;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = findViewById(R.id.fab_main);
        fab.setOnClickListener(v -> showDialog());

        recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        classAdapter = new ClassAdapter(this,classItems);
        recyclerView.setAdapter(classAdapter);
        classAdapter.setOnItemClickListener(position -> gotoItemActivity(position));
        setToolbar();
    }

    private void setToolbar() {
        toolbar=findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.title_toolbar);
        TextView subtitle= toolbar.findViewById(R.id.subtitle_toolbar);
        ImageButton back=toolbar.findViewById(R.id.back);
        ImageButton save =toolbar.findViewById(R.id.save);

        title.setText("Attendance App");
        subtitle.setVisibility(View.GONE);
        back.setVisibility(View.INVISIBLE);
        save.setVisibility(View.INVISIBLE);

    }

    private void gotoItemActivity(int position) {
        Intent intent= new Intent(this,StudentActivity.class);

        intent.putExtra( "ClassName",classItems.get(position).getClassName());
        intent.putExtra("SubjectName",classItems.get(position).getSubjectName());
        intent.putExtra("position",position);
        startActivity(intent);
    }

    @SuppressLint("MissingInflatedId")
    private void showDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.class_dialog,null);
        builder.setView(view);
        AlertDialog dialog= builder.create();
        dialog.show();

        class_edt = view.findViewById(R.id.class_edt);
        subject_edt = view.findViewById(R.id.subject_edt);

        Button cancel =view.findViewById(R.id.cancel_btn);
        Button add=view.findViewById(R.id.add_btn);

        cancel.setOnClickListener(v-> dialog.dismiss());
        add.setOnClickListener(v->{
            addClass();
            dialog.dismiss();
                });
    }

    private void addClass() {
        String className = class_edt.getText().toString();
        String subjectName = subject_edt.getText().toString();
        classItems.add(new ClassItem(className,subjectName));
        classAdapter.notifyDataSetChanged();
    }
}






















