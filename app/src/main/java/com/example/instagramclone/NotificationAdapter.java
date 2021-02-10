package com.example.instagramclone;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    public Context mContext;
    public List<Notification> mNotification;
    private FirebaseUser firebaseUser;

    public NotificationAdapter(Context mContext, List<Notification> mNotification) {
        this.mContext = mContext;
        this.mNotification = mNotification;
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item,parent,false);


        return new NotificationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.ViewHolder holder, int position) {



    }

    @Override
    public int getItemCount() {
        return mNotification.size();

    }

    public  class  ViewHolder extends RecyclerView.ViewHolder{

        public ImageView image_profile,post_image;
        public TextView username,text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile  = itemView.findViewById(R.id.image_profile);
            post_image  = itemView.findViewById(R.id.postImage);
            username  = itemView.findViewById(R.id.username);
            text  = itemView.findViewById(R.id.comment);
        }
    }







}
