package com.example.chelonia.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.chelonia.R;

public class AddNoteDialog extends DialogFragment {

    public interface NoteDialogListener {
        void onNoteCreated(String title, double amount, boolean isHourly, double hourlyRate, int hoursWorked);
    }

    private NoteDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof NoteDialogListener) {
            listener = (NoteDialogListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement NoteDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_note, null);

        EditText titleInput = view.findViewById(R.id.titleInput);
        EditText amountInput = view.findViewById(R.id.amountInput);
        EditText hourlyRateInput = view.findViewById(R.id.hourlyRateInput);
        EditText hoursWorkedInput = view.findViewById(R.id.hoursWorkedInput);
        RadioGroup paymentTypeGroup = view.findViewById(R.id.paymentTypeGroup);
        RadioButton fixedPaymentRadio = view.findViewById(R.id.fixedPaymentRadio);
        Button saveButton = view.findViewById(R.id.saveButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);

        paymentTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isHourly = checkedId != R.id.fixedPaymentRadio;
            hourlyRateInput.setEnabled(isHourly);
            hoursWorkedInput.setEnabled(isHourly);
            amountInput.setEnabled(!isHourly);
        });

        saveButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString();
            boolean isHourly = !fixedPaymentRadio.isChecked();
            double amount = isHourly ? 0 : Double.parseDouble(amountInput.getText().toString());
            double hourlyRate = isHourly ? Double.parseDouble(hourlyRateInput.getText().toString()) : 0;
            int hoursWorked = isHourly ? Integer.parseInt(hoursWorkedInput.getText().toString()) : 0;

            if (listener != null) {
                listener.onNoteCreated(title, amount, isHourly, hourlyRate, hoursWorked);
            }
            dismiss();
        });

        cancelButton.setOnClickListener(v -> dismiss());

        return new android.app.AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }
}
