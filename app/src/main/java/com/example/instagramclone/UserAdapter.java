package com.example.instagramclone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.fragment.app.Fragment;
import androidx.annotation.Nullable;


import java.util.HashMap;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context myContext ;
    private List<User> myUsers;
    private FirebaseUser firebaseUser;
    private boolean isfragment;

    public UserAdapter(Context myContext, List<User> myUsers,boolean isfragment) {
        this.myContext = myContext;
        this.myUsers = myUsers;
        this.isfragment = isfragment;

    }


    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(myContext).inflate(R.layout.user_item,parent,false);

        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final  User user = myUsers.get(position);

        holder.btn_follow.setVisibility(View.VISIBLE);

        holder.username.setText(user.getUsername());
        holder.fullname.setText(user.getFullname());
        Glide.with(myContext).load(user.getImageurl()).into(holder.image_profile);

        isFollowing(user.getId(),holder.btn_follow);

        if (user.getId().equals(firebaseUser.getUid())){
            holder.btn_follow.setVisibility(View.GONE);

        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isfragment) {


                    SharedPreferences.Editor editor = myContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("profileid", user.getId());
                    editor.apply();

                    ((FragmentActivity) myContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new ProfileFragment()).commit();
                }else {

                    Intent intent = new Intent(myContext, MainActivity.class);
                    intent.putExtra("publisherid",user.getId());
                    myContext.startActivity(intent);
                }
            }
        });
        holder.btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(holder.btn_follow.getText().toString().equals("follow")){

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(user.getId()).setValue(true);

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId())
                            .child("followers").child(firebaseUser.getUid()).setValue(true);

                    addNotifications(user.getId());

                }else {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(user.getId()).removeValue();

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId())
                            .child("followers").child(firebaseUser.getUid()).removeValue();
                }
            }
        });

    }
    private  void  addNotifications(String userid){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);

        HashMap<String,Object> hashMap = new HashMap<String,Object>();

        hashMap.put("userid",firebaseUser.getUid());
        hashMap.put("text","started following you");
        hashMap.put("postid","");
        hashMap.put("ispost",false);


        reference.push().setValue(hashMap);
    }

    @Override
    public int getItemCount() {
        return myUsers.size();
    }
    public  class  ViewHolder extends RecyclerView.ViewHolder{

        public TextView username ;
        public TextView fullname ;
        public ImageView image_profile;
        public Button btn_follow;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            fullname = itemView.findViewById(R.id.fullname);
            image_profile = itemView.findViewById(R.id.image_profile);
            btn_follow = itemView.findViewById(R.id.btn_follow);

        }
    }
    private  void  isFollowing(final String userid, final Button button){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.child(userid).exists()){

                    button.setText("following");

                }else {
                    button.setText("follow");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
