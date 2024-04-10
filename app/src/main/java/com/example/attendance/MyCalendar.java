package com.example.attendance;import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class MyCalendar extends DialogFragment {
    Calendar calendar = Calendar.getInstance();
    public OnCalendarOkClickListener onCalendarOkClickListener;

    public interface OnCalendarOkClickListener {
        void onClick(int year, int month, int dayOfMonth);
    }

    public void setOnCalendarOkClickListener(OnCalendarOkClickListener onCalendarOkClickListener) {
        this.onCalendarOkClickListener = onCalendarOkClickListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                (view, year, month, dayOfMonth) -> {
                    // Check if the listener is not null before invoking it
                    if (onCalendarOkClickListener != null) {
                        onCalendarOkClickListener.onClick(year, month, dayOfMonth);
                    }
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        // Set the minimum date to a very old date to allow past dates
        datePickerDialog.getDatePicker().setMinDate(-2209017600000L); // Set to year 1900

        return datePickerDialog;
    }

    void setDate(int year, int month, int day) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
    }

    String getDate() {
        return DateFormat.format("dd.MM.yyyy", calendar).toString();
    }
}
