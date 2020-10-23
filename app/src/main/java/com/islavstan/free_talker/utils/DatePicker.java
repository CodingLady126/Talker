package com.islavstan.free_talker.utils;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Button;
import android.widget.EditText;


import com.islavstan.free_talker.R;

import java.util.Calendar;

//change date dialog
public class DatePicker extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);


        Dialog picker = new DatePickerDialog(getActivity()/*, R.style.DialogTheme*/, this,
                year, month, day);


        return picker;
    }

    @Override
    public void onStart() {
        super.onStart();
        // добавляем кастомный текст для кнопки
        Button nButton = ((AlertDialog) getDialog())
                .getButton(DialogInterface.BUTTON_POSITIVE);
        nButton.setText(R.string.ok);
        Button nButton2 = ((AlertDialog) getDialog())
                .getButton(DialogInterface.BUTTON_NEGATIVE);
        nButton2.setText(R.string.cancel);
    }

    @Override
    public void onDateSet(android.widget.DatePicker datePicker, int year, int month, int day) {

        EditText tv = (EditText) getActivity().findViewById(R.id.date_of_birth);
        month++;
        String dayS = day + "", monthS = month + "";
        if (day < 10) dayS = "0" + day;
        if (month < 10) monthS = "0" + (month);
        tv.setText(dayS + "." + monthS + "." + year);

    }
}