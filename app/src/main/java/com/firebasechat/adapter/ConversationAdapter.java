package com.firebasechat.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebasechat.controller.ConstantData;
import com.firebasechat.controller.Utils;
import com.firebasechat.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.MyViewHolder> {

    private Context context;
    private SharedPreferences preferences;
    private List<HashMap<String, String>> listOfConversation;
    private static final int RIGHT_MSG = 0;
    private static final int LEFT_MSG = 1;
    private static final int RIGHT_MSG_IMG = 2;
    private static final int LEFT_MSG_IMG = 3;
    private DisplayImageOptions options;
    private ImageLoadingListener animateFirstListener = new ConversationAdapter.AnimateFirstDisplayListener();

    public ConversationAdapter(Context context1, List<HashMap<String, String>> list) {
        this.context = context1;
        this.listOfConversation = list;
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
        preferences = context1.getSharedPreferences(ConstantData.PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public int getItemCount() {
        return listOfConversation.size();
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == RIGHT_MSG) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right, parent, false);
            return new MyViewHolder(view);
        } else if (viewType == LEFT_MSG) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left, parent, false);
            return new MyViewHolder(view);
        } else if (viewType == RIGHT_MSG_IMG) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right_img, parent, false);
            return new MyViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left_img, parent, false);
            return new MyViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        String strEmail = preferences.getString("email", "");
        String sender = listOfConversation.get(position).get("sender");
        String type = listOfConversation.get(position).get("type");
        if (!Utils.isStringNull(strEmail) && !Utils.isStringNull(sender)) {
            if (type.equals("0")) {
                if (strEmail.equalsIgnoreCase(sender)) {
                    return RIGHT_MSG;
                } else {
                    return LEFT_MSG;
                }
            } else {
                if (strEmail.equalsIgnoreCase(sender)) {
                    return RIGHT_MSG_IMG;
                } else {
                    return LEFT_MSG_IMG;
                }
            }
        } else {
            return RIGHT_MSG;
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTimesTamp;
        private EmojiconTextView tvMessage;
        private ImageView imgChatPhoto;
        private ProgressBar progressBar;


        public MyViewHolder(View view) {
            super(view);
            tvTimesTamp = (TextView) itemView.findViewById(R.id.tvTimeStamp);
            tvMessage = (EmojiconTextView) itemView.findViewById(R.id.txtMessage);
            imgChatPhoto = (ImageView) itemView.findViewById(R.id.img_chat);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
        }
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Map<String, String> map = listOfConversation.get(position);

        String message = map.get("message");
        String time_stamp = map.get("time");
        String image = map.get("image");
        String lat = map.get("lat");
        String lng = map.get("lng");

        //MESSAGE
        if (!Utils.isStringNull(message)) {
            if (holder.tvMessage == null) return;
            holder.tvMessage.setText(message);
        }

        //TIME STAMP
        if (!Utils.isStringNull(time_stamp)) {
            if (holder.tvTimesTamp == null) return;
            holder.tvTimesTamp.setText(Utils.convertTimeStamp(time_stamp));
        }

        //CHAT PHOTO
        if (!Utils.isStringNull(image)) {
            if (holder.imgChatPhoto == null) return;
            if (holder.progressBar == null) return;


            ImageLoader.getInstance().displayImage(image, holder.imgChatPhoto, options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    holder.progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    holder.progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    holder.progressBar.setVisibility(View.GONE);
                }
            });

        }

        //LOCATION
        if (!Utils.isStringNull(lat) && !Utils.isStringNull(lng)) {
            if (holder.imgChatPhoto == null) return;
            if (holder.progressBar == null) return;
            holder.progressBar.setVisibility(View.GONE);
            holder.imgChatPhoto.setImageResource(R.drawable.map_icon);
        }
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