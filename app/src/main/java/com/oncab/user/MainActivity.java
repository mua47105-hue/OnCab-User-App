package com.oncab.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.oncab.user.Utils.Constants;
import com.oncab.user.databinding.ActivityMainBinding;
import com.oncab.user.databinding.AppBarMainBinding;
import com.oncab.user.ui.nightops.NightOpsActivity;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Faithful port of MainActivity.kt — drawer + bottom navigation + nav host with the
 * original five top-level destinations.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AppBarMainBinding appBarMain;
    private AppBarConfiguration appBarConfiguration;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        appBarMain = binding.appBarMain;

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        setDefaultView();
        loadHeaderProfile();
        setDrawerMenu();
        routeFromNightOps(getIntent());
    }

    /**
     * Night Ops bottom-nav deep link: the activity can be launched with an
     * extra carrying a top-level destination id to route to after restore.
     * Also handles redelivery via onNewIntent (CLEAR_TOP|SINGLE_TOP path).
     */
    private void routeFromNightOps(Intent intent) {
        if (intent == null) {
            return;
        }
        int dest = intent.getIntExtra(
                NightOpsActivity.EXTRA_NAV_DESTINATION, -1);
        if (dest <= 0) {
            return;
        }
        intent.removeExtra(NightOpsActivity.EXTRA_NAV_DESTINATION);
        try {
            navController.navigate(dest);
        } catch (IllegalArgumentException e) {
            // destination not on the current graph; ignore and stay on home
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        routeFromNightOps(intent);
    }

    private void setDefaultView() {
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_new_home_map,
                R.id.navigation_booking_history,
                R.id.navigation_shuttle_bookings,
                R.id.navigation_account,
                R.id.navigation_inbox).build();

        NavigationUI.setupWithNavController(appBarMain.bottomNavView, navController);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    private void loadHeaderProfile() {
        SharedPreferences sp = getSharedPreferences(Constants.FILE_NAME, 0);
        String photo = sp.getString("user_photo", "");
        CircleImageView pic = binding.navView.getHeaderView(0).findViewById(R.id.header_profile_pic);
        if (pic == null) {
            return;
        }
        if (photo.length() > 0 && !"null".equalsIgnoreCase(photo)) {
            Glide.with(this).load(photo).placeholder(R.drawable.avatar_1)
                    .error(R.drawable.avatar_1).into(pic);
        } else {
            pic.setImageResource(R.drawable.avatar_1);
        }
    }

    private void setDrawerMenu() {
        // Original drawer list is built empty; NavigationView supplies no menu items.
        // Header opens account; drawer item routing mirrors the original options.
        binding.navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_account) {
                navController.navigate(R.id.navigation_account);
            } else if (id == R.id.navigation_booking_history) {
                navController.navigate(R.id.navigation_booking_history);
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    public void openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START);
    }

    public NavController getNavController() {
        return navController;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            return NavigationUI.navigateUp(navController, appBarConfiguration)
                    || super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
