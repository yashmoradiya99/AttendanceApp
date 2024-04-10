package com.example.attendance;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private ArrayList<StudentItem> studentItems;
    private Context context;
    private DatabaseReference studentRef;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public StudentAdapter(Context context, ArrayList<StudentItem> studentItems, DatabaseReference studentRef) {
        this.studentItems = studentItems;
        this.context = context;
        this.studentRef = studentRef;
    }

    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView rollEditText;
        TextView nameEditText;
        CheckBox statusEditText;
        CardView cardView;
        Button btnEdit;
        Button btnDelete;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            rollEditText = itemView.findViewById(R.id.rollEditText);
            nameEditText = itemView.findViewById(R.id.nameEditText);
            statusEditText = itemView.findViewById(R.id.statusEditText);
            cardView = itemView.findViewById(R.id.cardview);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_item, parent, false);
        return new StudentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, final int position) {
        StudentItem studentItem = studentItems.get(position);
        holder.rollEditText.setText(studentItem.getRoll());
        holder.nameEditText.setText(studentItem.getName());
        holder.statusEditText.setText(studentItem.getStatus());
        holder.cardView.setCardBackgroundColor(getColor(studentItem.getStatus()));

        holder.statusEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    StudentItem currentItem = studentItems.get(adapterPosition);
                    currentItem.setStatus(currentItem.getStatus().equals("P") ? "A" : "P");
                    String key = currentItem.getKey();
                    if (key != null) {
                        Map<String, Object> updateData = new HashMap<>();
                        updateData.put("status", currentItem.getStatus());
                        studentRef.child(key).updateChildren(updateData)
                                .addOnFailureListener(e -> {
                                    Log.e("FirebaseStatusUpdateError", "Failed to update status for key: " + key, e);
                                    Toast.makeText(context, "Status update failed", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.e("FirebaseStatusUpdateError", "Key is null for studentItem: " + currentItem.toString());
                        Toast.makeText(context, "Status update failed: Key is null", Toast.LENGTH_SHORT).show();
                    }
                    holder.statusEditText.setText(currentItem.getStatus());
                }
            }
        });

        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final DialogPlus dialogPlus = DialogPlus.newDialog(context)
                        .setContentHolder(new ViewHolder(R.layout.update_popup))
                        .setExpanded(true, 1200)
                        .create();

                dialogPlus.show();

                View view1 = dialogPlus.getHolderView();

                EditText name = view1.findViewById(R.id.txtName);
                Button btnUpdate = view1.findViewById(R.id.btnUpdate);

                btnUpdate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int adapterPosition = holder.getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            StudentItem currentItem = studentItems.get(adapterPosition);
                            String key = currentItem.getKey();
                            if (key != null) {
                                String newName = name.getText().toString().trim();
                                if (!TextUtils.isEmpty(newName)) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("name", newName);
                                    studentRef.child(key).updateChildren(map)
                                            .addOnSuccessListener(unused -> {
                                                Toast.makeText(context, "Data update successful", Toast.LENGTH_SHORT).show();
                                                dialogPlus.dismiss();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("FirebaseUpdateError", "Failed to update data for key: " + key, e);
                                                Toast.makeText(context, "Data update failed", Toast.LENGTH_SHORT).show();
                                                dialogPlus.dismiss();
                                            });
                                } else {
                                    // Handle the case where the name is empty
                                    Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e("FirebaseUpdateError", "Key is null for studentItem: " + currentItem.toString());
                                Toast.makeText(context, "Data update failed: Key is null", Toast.LENGTH_SHORT).show();
                                dialogPlus.dismiss();
                            }
                        }
                    }
                });
            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Are you sure?");
                builder.setMessage("Delete message can't be undone");

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int adapterPosition = holder.getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            StudentItem currentItem = studentItems.get(adapterPosition);
                            String key = currentItem.getKey();
                            if (key != null) {
                                studentRef.child(key).removeValue()
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(context, "Data deleted successfully", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("FirebaseDeleteError", "Failed to delete data for key: " + key, e);
                                            Toast.makeText(context, "Data delete failed", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Log.e("FirebaseDeleteError", "Key is null for studentItem: " + currentItem.toString());
                                Toast.makeText(context, "Data delete failed: Key is null", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

                builder.show();
            }
        });
    }

    private int getColor(String status) {
        if ("P".equals(status))
            return Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.present)));
        else if ("A".equals(status))
            return Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.absent)));
        else if ("N".equals(status))
            return Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.gray)));
        else
            return Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.normal)));
    }

    @Override
    public int getItemCount() {
        return studentItems.size();
    }
}
