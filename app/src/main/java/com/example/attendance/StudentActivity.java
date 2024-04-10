package com.example.attendance;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StudentActivity extends AppCompatActivity {

    Toolbar toolbar;
    private String className;
    private String subjectName;
    private int position;
    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private DatabaseReference studentRef;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<StudentItem> studentItems = new ArrayList<>();
    private MyCalendar calendar;
    private TextView subtitle;
    private File pdfFile;

    private static final String PDF_FILE_NAME = "attendance_report.pdf";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        calendar = new MyCalendar();
        Intent intent = getIntent();
        className = intent.getStringExtra("ClassName");
        subjectName = intent.getStringExtra("SubjectName");
        position = intent.getIntExtra("position", -1);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("Teacher/classes/Students");
        studentRef = databaseReference.child(className);

        setToolbar();

        recyclerView = findViewById(R.id.student_recycler);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new StudentAdapter(this, studentItems, studentRef);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(position -> changeStatus(position));

        setDatabaseListener();
    }

    private void setDatabaseListener() {
        studentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                studentItems.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    StudentItem student = snapshot.getValue(StudentItem.class);
                    if (student != null) {
                        studentItems.add(student);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors here
            }
        });
    }

    private void changeStatus(int position) {
        StudentItem student = studentItems.get(position);
        String status = student.getStatus();

        if (status.equals("P")) {
            student.setStatus("A");
        } else {
            student.setStatus("P");
        }

        studentRef.child(student.getKey()).child("status").setValue(student.getStatus());

        adapter.notifyItemChanged(position);
    }

    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.title_toolbar);
        subtitle = toolbar.findViewById(R.id.subtitle_toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);

        title.setText(className);
        subtitle.setText(subjectName);
        subtitle.setText(subjectName + " | " + calendar.getDate());
        back.setOnClickListener(v -> onBackPressed());
        toolbar.inflateMenu(R.menu.student_menu);
        toolbar.setOnMenuItemClickListener(menuItem -> onMenuItemClick(menuItem));
    }

    private void loadStatusData() {
        for (StudentItem studentItem : studentItems) {
            String status = studentItem.getStatus();
            if (status != null) studentItem.setStatus(status);
            else studentItem.setStatus("");
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private boolean onMenuItemClick(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.add_student) {
            showAddStudentDialog();
        } else if (menuItem.getItemId() == R.id.show_Calendar) {
            showCalendar();
        } else if (menuItem.getItemId() == R.id.logout) {
            Intent loginIntent = new Intent(this, Login.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginIntent);
            finish();
        } else if (menuItem.getItemId() == R.id.action_open_pdf) {
            // After generating the PDF, open it
            openGeneratedPDF();

        } else if (menuItem.getItemId() == R.id.generate_pdf) {
            generatePDF();
        }
        return true;
    }

    private void fetchLatestDataFromFirebase() {
        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                studentItems.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    StudentItem student = snapshot.getValue(StudentItem.class);
                    if (student != null) {
                        studentItems.add(student);
                    }
                }

                // Once the data is fetched, generate the PDF
                generatePDF();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors here
            }
        });
    }
    private void generatePDF() {
        Calendar currentDate = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy");
        String currentDateStr = dateFormat.format(currentDate.getTime());

        // Include the class name key in the PDF file name
        String classNameKey = className.replaceAll("\\s+", "_"); // Replace spaces with underscores

        // Define the path to the "Downloads" directory
        File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        int pdfNumber = 1;
        String pdfFileName = currentDateStr + "_" + classNameKey + "_" + pdfNumber + ".pdf";
        pdfFile = new File(downloadsDirectory, pdfFileName);

        while (pdfFile.exists()) {
            pdfNumber++;
            pdfFileName = currentDateStr + "_" + classNameKey + "_" + pdfNumber + ".pdf";
            pdfFile = new File(downloadsDirectory, pdfFileName);
        }

        PdfDocument pdfDocument = new PdfDocument();
        int pageWidth = 600;
        int pageHeight = 900;
        int pageNumber = 1;

        // Set text size, style, and italic for the header
        Paint headerPaint = new Paint();
        headerPaint.setColor(Color.BLACK);
        headerPaint.setTextSize(18); // Adjust the text size as needed
        headerPaint.setFakeBoldText(true); // Make the text bold
        headerPaint.setTextSkewX(-0.25f); // Add italic skew

        Paint tableHeaderPaint = new Paint();
        tableHeaderPaint.setColor(Color.BLACK);
        tableHeaderPaint.setTextSize(14);
        tableHeaderPaint.setFakeBoldText(true);

        Paint tableCellPaint = new Paint();
        tableCellPaint.setColor(Color.BLACK);
        tableCellPaint.setTextSize(12);

        int tableHeaderY = 180; // Adjust the initial position for the table headers
        int tableRowHeight = 36;

        // Adjust the page height to fit the table
        int numRows = studentItems.size() + 1; // Include header row
        int totalTableHeight = numRows * tableRowHeight;
        if (totalTableHeight > pageHeight) {
            pageHeight = totalTableHeight + 100; // Add extra space for header and footer
            tableHeaderY = 100; // Adjust the table header position to account for the extra page height
        }

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Calculate the position for the header text
        int headerY = 36;
        String headerText = "Date: " + currentDateStr + "    |    Class: " + className;
        float textWidth = headerPaint.measureText(headerText);
        float x = (pageWidth - textWidth) / 2;

        // Add the header to the PDF content
        canvas.drawText(headerText, x, headerY, headerPaint);

        // Draw the table headers with a white background color
        Paint tableHeaderBgPaint = new Paint();
        tableHeaderBgPaint.setColor(Color.WHITE); // Set the background color to white

        // Define the paint for cell borders
        Paint linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(1); // Set the line width as needed

        String[] tableHeaders = {"Roll", "Name", "Status"};
        float columnWidth = (pageWidth - 72) / tableHeaders.length; // Adjust for left and right margins

        float xCoordinate = 36;
        for (String header : tableHeaders) {
            // Draw a rectangle for each table cell with a white background
            RectF cellRect = new RectF(xCoordinate, tableHeaderY, xCoordinate + columnWidth, tableHeaderY + tableRowHeight);
            canvas.drawRect(cellRect, tableHeaderBgPaint);

            // Draw the cell border
            canvas.drawRect(cellRect, linePaint);

            // Draw the header text in the center of the rectangle
            float textX = xCoordinate + (columnWidth - tableHeaderPaint.measureText(header)) / 2;
            float textY = tableHeaderY + tableRowHeight / 2;
            canvas.drawText(header, textX, textY, tableHeaderPaint);

            xCoordinate += columnWidth;
        }

        tableHeaderY += tableRowHeight;

        // Draw horizontal and vertical lines for the table
        xCoordinate = 36;
        for (int i = 0; i <= tableHeaders.length; i++) {
            // Draw vertical lines between columns
            canvas.drawLine(xCoordinate, tableHeaderY, xCoordinate, tableHeaderY + totalTableHeight, linePaint);

            xCoordinate += columnWidth;
        }

        xCoordinate = 36;
        for (int i = 0; i <= numRows; i++) {
            // Draw horizontal lines between rows
            canvas.drawLine(36, tableHeaderY + tableRowHeight * i, pageWidth - 36, tableHeaderY + tableRowHeight * i, linePaint);
        }

        // Draw student data in table format
        for (StudentItem student : studentItems) {
            String[] rowData = {student.getRoll(), student.getName(), student.getStatus()};

            xCoordinate = 36;
            for (String data : rowData) {
                // Draw the cell background
                RectF cellRect = new RectF(xCoordinate, tableHeaderY, xCoordinate + columnWidth, tableHeaderY + tableRowHeight);
                canvas.drawRect(cellRect, tableHeaderBgPaint);

                // Draw the cell border
                canvas.drawRect(cellRect, linePaint);

                // Calculate text coordinates to center align
                float textX = xCoordinate + (columnWidth - tableCellPaint.measureText(data)) / 2;
                float textY = tableHeaderY + tableRowHeight / 2;

                // Draw the cell content
                canvas.drawText(data, textX, textY, tableCellPaint);

                xCoordinate += columnWidth;
            }

            tableHeaderY += tableRowHeight;
        }

        pdfDocument.finishPage(page);

        try {
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
            Toast.makeText(this, "PDF generated successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("PDF Generation", "Error: " + e.getMessage());
        } finally {
            pdfDocument.close();
        }

        // After generating the PDF, open it
        openGeneratedPDF();
    }


    private void openGeneratedPDF() {
        if (pdfFile != null && pdfFile.exists()) {
            Uri pdfUri = FileProvider.getUriForFile(this, "com.example.attendance.fileprovider", pdfFile);

            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
            viewIntent.setDataAndType(pdfUri, "application/pdf");
            viewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            PackageManager packageManager = getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(viewIntent, 0);
            boolean isIntentSafe = activities.size() > 0;

            if (isIntentSafe) {
                try {
                    startActivity(viewIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, "No PDF viewer app available", Toast.LENGTH_SHORT).show();
                    Log.e("PDF Viewing", "No PDF viewer app available");
                }
            } else {
                Toast.makeText(this, "No app can open this PDF file", Toast.LENGTH_SHORT).show();
                Log.e("PDF Viewing", "No app can open this PDF file");
            }
        } else {
            Toast.makeText(this, "PDF file does not exist", Toast.LENGTH_SHORT).show();
            Log.e("PDF Viewing", "PDF file does not exist");
        }
    }

    private void showCalendar() {
        calendar.show(getSupportFragmentManager(), "");
        calendar.setOnCalendarOkClickListener(this::onCalendarOkClicked);
    }

    private void onCalendarOkClicked(int year, int month, int day) {
        Calendar selectedCalendar = Calendar.getInstance();
        selectedCalendar.set(year, month, day);

        Calendar currentDate = Calendar.getInstance();

        if (selectedCalendar.before(currentDate)) {
            Toast.makeText(this, "Please select a future date.", Toast.LENGTH_SHORT).show();
        } else {
            calendar.setDate(year, month, day);
            subtitle.setText(subjectName + " | " + calendar.getDate());
            resetStudentStatusToN();
        }
    }

    private void resetStudentStatusToN() {
        for (StudentItem studentItem : studentItems) {
            studentItem.setStatus("N");
            String key = studentItem.getKey();
            if (key != null) {
                studentRef.child(key).child("status").setValue("");
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showAddStudentDialog() {
        MyDialog dialog = new MyDialog();
        dialog.show(getSupportFragmentManager(), MyDialog.STUDENT_ADD_DIALOG);
        dialog.setListener((roll, name) -> addStudent(roll, name));
    }

    private void addStudent(String roll, String name) {
        String key = studentRef.push().getKey();

        if (key != null) {
            StudentItem studentItem = new StudentItem(roll, name, "N", key);
            studentRef.child(key).setValue(studentItem);
        }
        Toast.makeText(this, "Student added", Toast.LENGTH_SHORT).show();
    }
}
