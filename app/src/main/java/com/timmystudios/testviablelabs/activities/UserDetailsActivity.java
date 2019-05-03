package com.timmystudios.testviablelabs.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.timmystudios.testviablelabs.R;
import com.timmystudios.testviablelabs.models.User;
import com.timmystudios.webservicesutils.WebServicesUtils;
import com.timmystudios.webservicesutils.utils.PictureFitHelper;

public class UserDetailsActivity extends AppCompatActivity {

    public static final String KEY_USER = "KEY_USER";

    private ImageView pictureImageView;
    private TextView firstNameTextView;
    private TextView lastNameTextView;
    private TextView emailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        pictureImageView = findViewById(R.id.picture);
        firstNameTextView = findViewById(R.id.first_name);
        lastNameTextView = findViewById(R.id.last_name);
        emailTextView = findViewById(R.id.email);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object userObject = bundle.get(KEY_USER);
                if (userObject instanceof User) {
                    User user = (User) userObject;

                    WebServicesUtils.setImage(
                            user.getPictureLargeUrl(),
                            pictureImageView,
                            PictureFitHelper.FitType.FILL,
                            PictureFitHelper.DEFAULT_DURATION_FADE_ANIMATION
                    );

                    firstNameTextView.setText(user.getFirstName());
                    lastNameTextView.setText(user.getLastName());
                    emailTextView.setText(user.getEmail());
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id==android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
