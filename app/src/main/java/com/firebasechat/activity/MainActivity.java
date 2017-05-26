package com.firebasechat.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.firebasechat.controller.ConstantData;
import com.firebasechat.controller.Utils;
import com.firebasechat.conversation.ConversationActivity;
import com.firebasechat.R;
import com.firebasechat.adapter.FriendsAdapter;
import com.firebasechat.controller.RecyclerItemClickListener;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private RecyclerView recyclerView;
    private CoordinatorLayout coordinatorLayout;
    private DatabaseReference databaseReference;
    private Boolean isBoolean = false;
    private List<HashMap<String, String>> listOfAllFriends = new ArrayList<>();
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("All Friends");

        preferences = getSharedPreferences(ConstantData.PREFERENCES, MODE_PRIVATE);
        FirebaseCrash.report(new Exception("MainActivity"));
        databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl(ConstantData.URL);

        initUI();
        isBoolean = true;
        getAllFriends();
        onLineOffLine(true);

    }

    private void initUI() {
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(MainActivity.this, ConversationActivity.class);
                        intent.putExtra("map", listOfAllFriends.get(position));
                        startActivity(intent);
                    }
                })
        );
    }

    private void getAllFriends() {
        if (Utils.isNetworkAvailable(this)) {
            progressBar.setVisibility(View.VISIBLE);
            databaseReference.child("user").addValueEventListener(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (isBoolean) {
                                isBoolean = false;
                                if (dataSnapshot.getValue() != null) {
                                    for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                                        HashMap<String, String> map = new HashMap<>();
                                        map.put("dob", (String) messageSnapshot.child("dob").getValue());
                                        map.put("email", (String) messageSnapshot.child("email").getValue());
                                        map.put("gender", (String) messageSnapshot.child("gender").getValue());
                                        map.put("name", (String) messageSnapshot.child("name").getValue());
                                        map.put("phone", (String) messageSnapshot.child("phone").getValue());
                                        map.put("profile_picture", (String) messageSnapshot.child("profile_picture").getValue());
                                        map.put("status", (String) messageSnapshot.child("status").getValue());
                                        String userEmail = preferences.getString("email", "");
                                        String email = (String) messageSnapshot.child("email").getValue();
                                        if (!userEmail.equals(email)) {
                                            listOfAllFriends.add(map);
                                        }
                                    }
                                    setData();
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    Utils.showSnackbar(coordinatorLayout, "Friends not found.");
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            progressBar.setVisibility(View.GONE);
                            Utils.showSnackbar(coordinatorLayout, "" + databaseError);
                        }
                    });
        } else {
            Utils.showSnackbar(coordinatorLayout, getResources().getString(R.string.internet_error));
        }
    }

    public void setData() {
        progressBar.setVisibility(View.GONE);
        if (listOfAllFriends.size() != 0) {
            FriendsAdapter friendsAdapter = new FriendsAdapter(this, listOfAllFriends);
            recyclerView.setAdapter(friendsAdapter);
        } else {
            Utils.showSnackbar(coordinatorLayout, "Friends not found.");
        }
    }

    @Override
    public void onBackPressed() {
        onLineOffLine(false);
        super.onBackPressed();
    }

    public void onLineOffLine(boolean isBoolean) {
        String key = preferences.getString("key", "");
        if (!Utils.isStringNull(key)) {
            if (isBoolean) {
                databaseReference.child("user").child(key).child("status").setValue("1");
            } else {
                databaseReference.child("user").child(key).child("status").setValue("0");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_homescreen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_logout:
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.commit();
                startActivity(new Intent(this, LoginActivity.class));
                MainActivity.this.finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

