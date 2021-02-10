package com.example.instagramclone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class ProfileFragment extends Fragment {


    private static final String TAG = "  " ;
    TextView posts,followers, following ,fullname,bio,username ;
    ImageView image_profile,options;
    Button edit_profile;
    FirebaseUser firebaseUser;
    String profileid;
    ImageView my_photos, saved_Photos;

    RecyclerView recyclerView ;
    MyPhotosAdapter myPhotosAdapter;
    List<Post> postList ;


    private List<String> mySaves;
    RecyclerView recyclerView_saves ;
    MyPhotosAdapter myPhotosAdapter_saves;
    List<Post> postList_saves ;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileid = prefs.getString("profileid", "none");

        image_profile = view.findViewById(R.id.imageProfile);
        options = view.findViewById(R.id.options);
        posts = view.findViewById(R.id.posts);
        followers = view.findViewById(R.id.followers);
        following = view.findViewById(R.id.followering);
        fullname = view.findViewById(R.id.fullName);
        bio = view.findViewById(R.id.bio);
        username = view.findViewById(R.id.username);
        edit_profile = view.findViewById(R.id.editProfile);
        my_photos = view.findViewById(R.id.my_photos);
        saved_Photos = view.findViewById(R.id.save_photos);


        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(),3);
        recyclerView.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<Post>();
        myPhotosAdapter  = new MyPhotosAdapter(getContext(), postList);

        recyclerView.setAdapter(myPhotosAdapter);


        recyclerView_saves = view.findViewById(R.id.recycler_view_save);
        recyclerView_saves.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager1 = new GridLayoutManager(getContext(),3);
        recyclerView_saves.setLayoutManager(linearLayoutManager1);
        postList_saves = new ArrayList<Post>();
        myPhotosAdapter_saves  = new MyPhotosAdapter(getContext(), postList_saves);

        recyclerView_saves.setAdapter(myPhotosAdapter_saves);

        recyclerView.setVisibility(View.VISIBLE);
        recyclerView_saves.setVisibility(View.GONE);


        userInfo();
        getFollowers();
        getNrPost();
        myPhotos();
        mysaves();


        if(profileid.equals(firebaseUser.getUid())){
            edit_profile.setText("Edit Profile");

        }else {
            checkFollow();
            saved_Photos.setVisibility(View.GONE);
        }

        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String btn = edit_profile.getText().toString();
                if(btn.equals("Edit Profile")){
                    startActivity(new Intent(getContext(), EditProfileActivity.class));

                }else if(btn.equals("follow")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(profileid).setValue(true);

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                            .child("followers").child(firebaseUser.getUid()).setValue(true);

                    addNotifications();

                }else if(btn.equals("following")){

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(profileid).removeValue();

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                            .child("followers").child(firebaseUser.getUid()).removeValue();


                }
            }
        });

        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), OptionsActivity.class);

                startActivity(intent);
            }
        });
        my_photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                recyclerView.setVisibility(View.VISIBLE);
                recyclerView_saves.setVisibility(View.GONE);
                saved_Photos.setImageResource(R.drawable.ic_save);
            }
        });
        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileid);
                intent.putExtra("title","followers");
                startActivity(intent);
            }
        });
        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileid);
                intent.putExtra("title","following");
                startActivity(intent);
            }
        });
        saved_Photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                recyclerView.setVisibility(View.GONE);
                recyclerView_saves.setVisibility(View.VISIBLE);

                saved_Photos.setImageResource(R.drawable.ic_save_black);
            }
        });


        return view;

    }
    private  void  addNotifications(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(profileid);

        HashMap<String,Object> hashMap = new HashMap<String,Object>();

        hashMap.put("userid",firebaseUser.getUid());
        hashMap.put("text","started following you");
        hashMap.put("postid","");
        hashMap.put("ispost",false);


        reference.push().setValue(hashMap);
    }

    private  void  userInfo(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(profileid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(getContext() == null){

                    return;
                }

                User user = dataSnapshot.getValue(User.class);
                try{
                Glide.with(getContext()).load(user.getImageurl()).into(image_profile);
                username.setText(user.getUsername());
                fullname.setText(user.getFullname());
                bio.setText(user.getBio());}
                catch(NullPointerException e){
                    Log.e(TAG, e.toString());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private  void  checkFollow(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow")
                .child(firebaseUser.getUid()).child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.child(profileid).exists()){
                    edit_profile.setText("following");

                }else {
                    edit_profile.setText("follow");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    private void getFollowers(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow")
                .child(profileid).child("followers");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                followers.setText(""+dataSnapshot.getChildrenCount());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference().child("Follow")
                .child(profileid).child("following");

        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                following.setText(""+dataSnapshot.getChildrenCount());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    private void  getNrPost(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){

                    Post post = snapshot.getValue(Post.class);
                    if (post.getPublisher().equals(profileid)){
                        i++;


                    }
                }

                posts.setText(""+i);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private  void  myPhotos(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                postList.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                    Post post = snapshot.getValue(Post.class);

                    if(post.getPublisher().equals(profileid)){
                        postList.add(post);


                    }

                }
                Collections.reverse(postList);
                myPhotosAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private  void mysaves(){

        mySaves = new ArrayList<String>();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Saves")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){

                    mySaves.add(snapshot.getKey());

                }
                readSaves();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readSaves() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                postList_saves.clear();

                for(DataSnapshot snapshot: dataSnapshot.getChildren()){

                    Post  post = snapshot.getValue(Post.class);

                    for(String id : mySaves){
                        if(post.getPostid().equals(id)){

                            postList_saves.add(post);

                        }

                    }

                }

                myPhotosAdapter_saves.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



}