package com.example.ggoggodong.friend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ggoggodong.R;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private List<Friend> friends;

    public FriendsAdapter(List<Friend> friends) {
        this.friends = friends;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Friend friend = friends.get(position);
        holder.friendNameTextView.setText(friend.getName());

        Glide.with(holder.itemView.getContext())
                .load(friend.getProfileImageUrl())
                .placeholder(R.drawable.default_profile)
                .into(holder.friendImageView);
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView friendNameTextView;
        ImageView friendImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            friendNameTextView = itemView.findViewById(R.id.friendName);
            friendImageView = itemView.findViewById(R.id.friendImage);
        }
    }
}
