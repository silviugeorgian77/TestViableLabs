package com.timmystudios.testviablelabs.mappers;

import com.timmystudios.testviablelabs.models.User;
import com.timmystudios.testviablelabs.models.UserListResponse;

import java.util.ArrayList;
import java.util.List;

public class UserListResponseMapper {

    public static List<User> mapUserList(UserListResponse userListResponse) {
        List<User> userList = new ArrayList<>();
        if (userListResponse != null) {
            List<UserListResponse.Result> resultList = userListResponse.results;
            User user;
            for (UserListResponse.Result result: resultList) {
                user = new User(
                        result.name.first,
                        result.name.last,
                        result.dob.age,
                        result.email,
                        result.nat,
                        result.picture.thumbnail,
                        result.picture.medium,
                        result.picture.large
                );
                userList.add(user);
            }
        }
        return userList;
    }

}
