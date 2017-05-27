package com.polito.mad17.madmax.activities.groups;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.entities.Event;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.util.Collections;
import java.util.TreeMap;


public class EventsFragment extends Fragment {
    private static final String TAG = EventsFragment.class.getSimpleName();

    private EventsViewAdapter eventsViewAdapter;
    private DatabaseReference databaseReference = FirebaseUtils.getDatabaseReference();
    private TreeMap<String, Event> eventMap = new TreeMap<>(Collections.reverseOrder());

    public EventsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView recyclerView;
        RecyclerView.LayoutManager layoutManager;
        String groupID;

        View view = inflater.inflate(R.layout.skeleton_list, container, false);

        Bundle fragmentArguments = getArguments();
        groupID = fragmentArguments.getString("groupID");

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_skeleton);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        eventsViewAdapter = new EventsViewAdapter(this.getContext(), eventMap);
        recyclerView.setAdapter(eventsViewAdapter);

        DatabaseReference groupRef = databaseReference.child("groups");

        Log.d(TAG, "groupID: " + groupID);
        // retrieving history of events for current group
        groupRef.child(groupID).child("events").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot eventSnapshot) {
                for(DataSnapshot event : eventSnapshot.getChildren()) {
                    FirebaseUtils.getInstance().getEvent(event.getKey(), eventMap, eventsViewAdapter);
                    Log.d(TAG, event.getKey());
                }

                eventsViewAdapter.update(eventMap);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
}
