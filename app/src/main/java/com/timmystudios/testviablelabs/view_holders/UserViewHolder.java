package com.timmystudios.testviablelabs.view_holders;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.timmystudios.testviablelabs.R;
import com.timmystudios.testviablelabs.activities.UserDetailsActivity;
import com.timmystudios.testviablelabs.models.User;
import com.timmystudios.testviablelabs.utils.FlagUtils;
import com.timmystudios.webservicesutils.WebServicesUtils;
import com.timmystudios.webservicesutils.utils.PictureFitHelper;

public class UserViewHolder extends RecyclerView.ViewHolder {

    private Context context;
    private User user;
    private ImageView pictureImageView;
    private ImageView nationalityIconImageView;
    private TextView nameTextView;
    private TextView ageTextView;

    public UserViewHolder(@NonNull View itemView) {
        super(itemView);
        context = itemView.getContext();
        pictureImageView = itemView.findViewById(R.id.picture);
        nationalityIconImageView = itemView.findViewById(R.id.nationality_icon);
        nameTextView = itemView.findViewById(R.id.name);
        ageTextView = itemView.findViewById(R.id.age);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UserDetailsActivity.class);
                intent.putExtra(UserDetailsActivity.KEY_USER, user);
                context.startActivity(intent);
            }
        });
    }

    public void updateModel(User user) {
        this.user = user;
        WebServicesUtils.setImage(
                user.getPictureThumbnailUrl(),
                pictureImageView,
                PictureFitHelper.FitType.FILL,
                PictureFitHelper.DEFAULT_DURATION_FADE_ANIMATION
        );

        String countryCode = user.getNationality();
        String flagImageUrl = FlagUtils.getFlagUrl(
                countryCode,
                FlagUtils.Width.PX_32,
                FlagUtils.Style.FLAT
        );
        WebServicesUtils.setImage(
                flagImageUrl,
                nationalityIconImageView,
                PictureFitHelper.FitType.FILL,
                PictureFitHelper.DEFAULT_DURATION_FADE_ANIMATION
        );

        String name = user.getFirstName() + " " + user.getLastName();
        nameTextView.setText(name);

        ageTextView.setText(String.valueOf(user.getAge()));
    }
}
