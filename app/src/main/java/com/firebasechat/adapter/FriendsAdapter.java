package com.firebasechat.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebasechat.controller.Utils;
import com.firebasechat.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.MyViewHolder> {

    private Context context;
    private List<HashMap<String, String>> listOfFriends;
    private DisplayImageOptions options;
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

    public FriendsAdapter(Context context1, List<HashMap<String, String>> list) {
        this.context = context1;
        this.listOfFriends = list;
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.defaultuserwhite)
                .showImageForEmptyUri(R.drawable.defaultuserwhite)
                .showImageOnFail(R.drawable.defaultuserwhite)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
    }

    @Override
    public int getItemCount() {
        return listOfFriends.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUnread, tvName, tvStatus;
        private ImageView imgProfilePicture;

        public MyViewHolder(View view) {
            super(view);
            tvUnread = (TextView) itemView.findViewById(R.id.tvUnread);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvStatus = (TextView) itemView.findViewById(R.id.tvStatus);
            imgProfilePicture = (ImageView) itemView.findViewById(R.id.imgProfilePicture);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_friends, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Map<String, String> map = listOfFriends.get(position);
        String unread = map.get("unread");
        String name = map.get("name");
        String status = map.get("status");
        String profile_picture = map.get("profile_picture");

        //Un read
        if (!Utils.isStringNull(unread)) {
            if (unread.equals("0")) {
                holder.tvUnread.setVisibility(View.GONE);
            } else {
                holder.tvUnread.setText(unread);
                holder.tvUnread.setVisibility(View.VISIBLE);
            }
        } else {
            holder.tvUnread.setVisibility(View.GONE);
        }

        //Name
        if (!Utils.isStringNull(name)) {
            holder.tvName.setText(name);
        } else {
            holder.tvName.setText("");
        }

        //STATUS
        if (!Utils.isStringNull(status)) {
            if (status.equals("1")) {
                holder.tvStatus.setBackgroundResource(R.drawable.online);
            } else {
                holder.tvStatus.setBackgroundResource(R.drawable.offline);
            }
        } else {
            holder.tvStatus.setVisibility(View.GONE);
        }
        //PROFILE PICTURE
        ImageLoader.getInstance().displayImage(profile_picture, holder.imgProfilePicture, options, animateFirstListener);
    }

    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }
}