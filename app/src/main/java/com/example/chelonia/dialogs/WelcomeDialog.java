package com.example.chelonia.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.chelonia.information.User;
import com.example.chelonia.R;

public class WelcomeDialog {

    private AlertDialog dialog;
    private final Context context;
    private final SharedPreferences preferences;
    private ActivityResultLauncher<String[]> avatarPickerLauncher;
    private String selectedAvatarUri = "";

    public WelcomeDialog(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE);
    }

    public void show() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.welcome_dialog, null);
        final ViewFlipper viewFlipper = dialogView.findViewById(R.id.viewFlipper);

        final EditText etFirstName = dialogView.findViewById(R.id.etFirstName);
        final EditText etLastName = dialogView.findViewById(R.id.etLastName);
        Button btnStep1Next = dialogView.findViewById(R.id.btnStep1Next);
        Button btnStep2Next = dialogView.findViewById(R.id.btnStep2Next);
        Button btnStep3Next = dialogView.findViewById(R.id.btnStep3Next);
        final ImageView ivAvatar = dialogView.findViewById(R.id.ivAvatar);
        Button btnFinish = dialogView.findViewById(R.id.btnFinish);
        TextView btnCancel = dialogView.findViewById(R.id.btnCancel);

        final View indicator1 = dialogView.findViewById(R.id.indicator1);
        final View indicator2 = dialogView.findViewById(R.id.indicator2);
        final View indicator3 = dialogView.findViewById(R.id.indicator3);
        final View indicator4 = dialogView.findViewById(R.id.indicator4);

        if (context instanceof AppCompatActivity) {
            avatarPickerLauncher = ((AppCompatActivity) context)
                    .registerForActivityResult(new ActivityResultContracts.OpenDocument(), result -> {
                        if (result != null) {
                            context.getContentResolver().takePersistableUriPermission(
                                    result,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                            ivAvatar.setImageURI(result);
                            selectedAvatarUri = result.toString();
                        }
                    });
        }

        // DESIGN SECTION:
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.WelcomeDialogStyle);
        builder.setView(dialogView).setCancelable(false);
        dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            dialog.getWindow().setGravity(Gravity.BOTTOM);
        }

        final boolean[] isAnimating = new boolean[]{false};
        if (viewFlipper.getInAnimation() != null) {
            viewFlipper.getInAnimation().setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    isAnimating[0] = true;
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    isAnimating[0] = false;
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }

        btnStep1Next.setOnClickListener(v -> {
            if (isAnimating[0]) return;
            viewFlipper.showNext();
            updateIndicators(viewFlipper, indicator1, indicator2, indicator3, indicator4);
        });

        btnStep2Next.setOnClickListener(v -> {
            if (isAnimating[0]) return;
            viewFlipper.showNext();
            updateIndicators(viewFlipper, indicator1, indicator2, indicator3, indicator4);
        });

        btnCancel.setOnClickListener(v -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("firstName", context.getString(R.string.unknown_first_name));
            editor.putString("lastName", context.getString(R.string.unknown_last_name));
            editor.apply();
            if (isAnimating[0]) return;
            viewFlipper.showNext();
            updateIndicators(viewFlipper, indicator1, indicator2, indicator3, indicator4);
        });

        btnStep3Next.setOnClickListener(v -> {
            if (isAnimating[0]) return;
            boolean hasError = false;
            if (etFirstName.getText().toString().trim().isEmpty()) {
                etFirstName.setBackgroundResource(R.drawable.registartion_edit_text_background_error);
                etFirstName.setHintTextColor(ContextCompat.getColor(context, R.color.red));
                hasError = true;
            } else {
                etFirstName.setBackgroundResource(R.drawable.registration_edit_text_background);
                etFirstName.setHintTextColor(ContextCompat.getColor(context, R.color.slate));
            }
            if (etLastName.getText().toString().trim().isEmpty()) {
                etLastName.setBackgroundResource(R.drawable.registartion_edit_text_background_error);
                etLastName.setHintTextColor(ContextCompat.getColor(context, R.color.red));
                hasError = true;
            } else {
                etLastName.setBackgroundResource(R.drawable.registration_edit_text_background);
                etLastName.setHintTextColor(ContextCompat.getColor(context, R.color.slate));
            }
            if (!hasError) {
                viewFlipper.showNext();
                updateIndicators(viewFlipper, indicator1, indicator2, indicator3, indicator4);
            }
        });

        ivAvatar.setOnClickListener(v -> {
            if (avatarPickerLauncher != null) {
                avatarPickerLauncher.launch(new String[]{"image/*"});
            } else {
                Toast.makeText(context, "Error choosing avatar photo", Toast.LENGTH_SHORT).show();
            }
        });

        btnFinish.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            if (firstName.isEmpty()) {
                firstName = context.getString(R.string.unknown_first_name);
            }
            if (lastName.isEmpty()) {
                lastName = context.getString(R.string.unknown_last_name);
            }
            User user = new User(firstName, lastName, selectedAvatarUri);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isUserDataSaved", true);
            editor.putString("firstName", firstName);
            editor.putString("lastName", lastName);
            editor.putString("avatarUri", selectedAvatarUri);
            editor.apply();
            dialog.dismiss();
            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(new Intent("com.example.chelonia.REGISTRATION_COMPLETE"));
        });

        updateIndicators(viewFlipper, indicator1, indicator2, indicator3, indicator4);

        dialog.show();
    }

    private void updateIndicators(ViewFlipper viewFlipper, View indicator1, View indicator2, View indicator3, View indicator4) {
        int currentStep = viewFlipper.getDisplayedChild();

        indicator1.setBackgroundResource(R.drawable.circle_empty);
        indicator2.setBackgroundResource(R.drawable.circle_empty);
        indicator3.setBackgroundResource(R.drawable.circle_empty);
        indicator4.setBackgroundResource(R.drawable.circle_empty);

        switch (currentStep) {
            case 0:
                indicator1.setBackgroundResource(R.drawable.circle_filled);
                break;
            case 1:
                indicator2.setBackgroundResource(R.drawable.circle_filled);
                break;
            case 2:
                indicator3.setBackgroundResource(R.drawable.circle_filled);
                break;
            case 3:
                indicator4.setBackgroundResource(R.drawable.circle_filled);
                break;
        }
    }
}
