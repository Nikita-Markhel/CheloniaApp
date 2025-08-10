package com.example.chelonia;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.chelonia.Interfaces.NoteEditable;
import com.example.chelonia.dialogs.WelcomeDialog;
import com.example.chelonia.fragments.Calendar.CalendarFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    public FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // FAB
        fab = findViewById(R.id.fabAddNote);
        fab.setOnClickListener(v -> {
            NavHostFragment navHost =
                    (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_main_activity);
            if (navHost == null) return;

            Fragment current = navHost.getChildFragmentManager().getPrimaryNavigationFragment();
            if (current instanceof CalendarFragment) {
                CalendarFragment calendarFragment = (CalendarFragment) current;
                NoteEditable noteEditable = calendarFragment.getActiveNoteEditable();
                if (noteEditable != null) {
                    noteEditable.addEditableNote();
                    return;
                }
            }

            if (current instanceof NoteEditable) {
                ((NoteEditable) current).addEditableNote();
                return;
            }

            Toast.makeText(this, "Действие недоступно на этом экране", Toast.LENGTH_SHORT).show();
        });


        NavHostFragment navHost = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_main_activity);
        if (navHost != null) {
            NavController navController = navHost.getNavController();
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        }

        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        boolean isUserDataSaved = prefs.getBoolean("isUserDataSaved", false);
        if (!isUserDataSaved) {
            new WelcomeDialog(this).show();
        }
    }

    public void updateFabStyle(boolean isEditing) {
        if (fab == null) return;

        if (isEditing) {
            fab.setImageResource(R.drawable.ic_check);
            fab.setSupportBackgroundTintList(ContextCompat.getColorStateList(this, R.color.slate));
        } else {
            fab.setImageResource(R.drawable.ic_plus);
            fab.setSupportBackgroundTintList(ContextCompat.getColorStateList(this, R.color.organic_pale));
        }
    }
}
