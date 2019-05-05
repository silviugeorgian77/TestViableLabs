package com.timmystudios.testviablelabs.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.timmystudios.testviablelabs.R;
import com.timmystudios.testviablelabs.models.User;
import com.timmystudios.testviablelabs.view_holders.LoadingViewHolder;
import com.timmystudios.testviablelabs.view_holders.UserViewHolder;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int TYPE_USER = 0;
    private int TYPE_LOADING = 1;

    private List itemList;

    public void setItemList(List itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        if (i == TYPE_USER) {
            View view = inflater.inflate(R.layout.layout_item_user, viewGroup, false);
            return new UserViewHolder(view);
        }
        View view = inflater.inflate(R.layout.layout_item_loading, viewGroup, false);
        return new LoadingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof UserViewHolder) {
            UserViewHolder userViewHolder = (UserViewHolder) viewHolder;
            Object item = itemList.get(i);
            if (item instanceof User) {
                User user = (User) item;
                userViewHolder.updateModel(user);
                userViewHolder.updateModel(user);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (itemList != null) {
            return itemList.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = itemList.get(position);
        if (item instanceof User) {
            return TYPE_USER;
        }
        return TYPE_LOADING;
    }
}
