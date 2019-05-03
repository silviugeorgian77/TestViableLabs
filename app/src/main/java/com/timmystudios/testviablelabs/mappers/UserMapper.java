package com.timmystudios.testviablelabs.mappers;

import com.timmystudios.testviablelabs.models.User;
import com.timmystudios.testviablelabs.models.UserUnmapped;

public class UserMapper {

    public static User mapUser(UserUnmapped userUnmapped) {
        return new User(
                userUnmapped.name.first,
                userUnmapped.name.second,
                userUnmapped.dob.age,
                userUnmapped.email,
                userUnmapped.nationality,
                userUnmapped.picture.thumbnail,
                userUnmapped.picture.medium,
                userUnmapped.picture.large
        );
    }

}
