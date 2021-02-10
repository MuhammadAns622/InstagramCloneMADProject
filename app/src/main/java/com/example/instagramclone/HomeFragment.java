package com.example.instagramclone;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {



    private PostAdapter postAdapter;
    private List<Post> postLists;
    private List<String> followingList;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home,container,false);

        recyclerView = view.findViewById(R.id.recycler_viewhome);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);

        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        progressBar  = view.findViewById(R.id.progress_circular);

        postLists = new ArrayList<Post>();
        postAdapter = new PostAdapter(getContext(),postLists);

        recyclerView.setAdapter(postAdapter);


        checkFollowing();

        return view;
    }
    private  void  checkFollowing(){

        followingList = new ArrayList<String>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                followingList.clear();

                for(DataSnapshot snapshot: dataSnapshot.getChildren()){

                    followingList.add(snapshot.getKey());
                }
                readPost();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }
    private  void  readPost(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                postLists.clear();
                for(DataSnapshot Snapshot : dataSnapshot.getChildren()){

                    Post post  = Snapshot.getValue(Post.class);
                    for(String id : followingList){

                        if (post.getPublisher().equals(id)){
                            postLists.add(post);


                        }

                    }

                }
                postAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



}