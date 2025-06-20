package com.example.ggoggodong.friend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ggoggodong.R;

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private List<Friend> friends;
    private List<String> selectedFriendUids;

    public FriendAdapter(List<Friend> friends, List<String> selectedFriendUids) {
        this.friends = friends;
        this.selectedFriendUids = selectedFriendUids;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_select, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friend friend = friends.get(position);

        holder.nicknameText.setText(friend.getName());

        Glide.with(holder.itemView.getContext())
                .load(friend.getProfileImageUrl())
                .placeholder(R.drawable.default_profile)
                .into(holder.friendImage);

        holder.checkBox.setOnCheckedChangeListener(null); // 리스너 초기화
        holder.checkBox.setChecked(selectedFriendUids.contains(friend.getUid()));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedFriendUids.contains(friend.getUid())) {
                    selectedFriendUids.add(friend.getUid());
                }
            } else {
                selectedFriendUids.remove(friend.getUid());
            }
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        ImageView friendImage;
        TextView nicknameText;
        CheckBox checkBox;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            friendImage = itemView.findViewById(R.id.friendImage);
            nicknameText = itemView.findViewById(R.id.textNickname);
            checkBox = itemView.findViewById(R.id.checkboxSelect);
        }
    }
}
