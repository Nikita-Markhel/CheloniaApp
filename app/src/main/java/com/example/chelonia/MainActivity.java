package com.example.chelonia;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.chelonia.dialogs.AddNoteDialog;
import com.example.chelonia.dialogs.WelcomeDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements AddNoteDialog.NoteDialogListener {


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_main_activity);
        if (navHost != null) {
            NavController navController = navHost.getNavController();
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        }

        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        boolean isUserDataSaved = prefs.getBoolean("isUserDataSaved", false);
        if (!isUserDataSaved){
            new WelcomeDialog(this).show();
        }
    }

    @Override
    public void onNoteCreated(String title, double amount, boolean isHourly, double hourlyRate, int hoursWorked) {
        String message = isHourly
                ? "Создана почасовая запись: " + title + ", ставка: " + hourlyRate + ", часы: " + hoursWorked
                : "Создана фиксированная запись: " + title + ", сумма: " + amount;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}