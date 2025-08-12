package com.example.chelonia;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.chelonia.Interfaces.FabActionProvider;
import com.example.chelonia.Interfaces.NoteEditable;
import com.example.chelonia.fragments.Calendar.CalendarFragment;
import com.example.chelonia.dialogs.WelcomeDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

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
            Fragment active = findDeepestVisibleFragment(getSupportFragmentManager());
            if (active != null) {
                // 1) primary delegate: FabActionProvider
                if (active instanceof FabActionProvider) {
                    boolean handled = ((FabActionProvider) active).onFabClick();
                    if (handled) return;
                }

                // 2) backwards compatibility: NoteEditable
                if (active instanceof NoteEditable) {
                    ((NoteEditable) active).addEditableNote();
                    return;
                }

                // 3) special-case CalendarFragment fallback (если вы хотите, но обычно
                //     findDeepestVisibleFragment уже найдет Today/Tomorrow)
                if (active instanceof CalendarFragment) {
                    CalendarFragment calendarFragment = (CalendarFragment) active;
                    NoteEditable noteEditable = calendarFragment.getActiveNoteEditable();
                    if (noteEditable != null) {
                        noteEditable.addEditableNote();
                        return;
                    }
                }
            }

            Toast.makeText(this, "Действие недоступно на этом экране", Toast.LENGTH_SHORT).show();
        });

        NavHostFragment navHost = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_main_activity);
        if (navHost != null) {
            NavController navController = navHost.getNavController();
            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            // обновляем FAB при смене destination (чтобы вид/иконка синхронизировались)
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> refreshFabAppearance());
        }

        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        boolean isUserDataSaved = prefs.getBoolean("isUserDataSaved", false);
        if (!isUserDataSaved) {
            new WelcomeDialog(this).show();
        }
    }

    /**
     * Рекурсивно ищет самый глубокий видимый фрагмент (для вложенных NavHost / ViewPager2 и т.д.).
     */
    private Fragment findDeepestVisibleFragment(FragmentManager fm) {
        List<Fragment> fragments = fm.getFragments();
        if (fragments.isEmpty()) return null;
        for (Fragment f : fragments) {
            if (f == null) continue;
            if (!f.isAdded() || !f.isVisible()) continue;

            // сначала пробуем глубже
            Fragment child = findDeepestVisibleFragment(f.getChildFragmentManager());
            if (child != null) return child;

            // если детских видимых фрагментов нет — возвращаем текущий видимый
            return f;
        }
        return null;
    }

    /**
     * Обновляет иконку/цвет/видимость FAB в соответствии с текущим активным фрагментом.
     * Фрагменты могут вызывать этот метод (например, в onResume или при смене editing-state).
     */
    public void refreshFabAppearance() {
        if (fab == null) return;
        Fragment active = findDeepestVisibleFragment(getSupportFragmentManager());
        if (active instanceof FabActionProvider) {
            FabActionProvider provider = (FabActionProvider) active;
            if (!provider.isFabVisible()) {
                fab.hide();
                return;
            } else {
                fab.show();
            }

            int icon = provider.getFabIconRes();
            if (icon != -1) fab.setImageResource(icon);
            int tint = provider.getFabTintColorRes();
            if (tint != -1) fab.setSupportBackgroundTintList(ContextCompat.getColorStateList(this, tint));
            return;
        }

        // дефолтный стиль
        fab.show();
        fab.setImageResource(R.drawable.ic_plus);
        fab.setSupportBackgroundTintList(ContextCompat.getColorStateList(this, R.color.organic_pale));
    }

    // Существующий метод оставляем (фрагменты уже его вызывают)
}
