package com.polito.mad17.madmax.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.entities.User;

public class BarDetailFragment extends Fragment {

    private static final String TAG = BarDetailFragment.class.getSimpleName();
    private OnItemClickInterface onClickGroupInterface;
    private boolean availableGroupData;
    private View mainView;
    private ImageView imageView;
    private TextView nameTextView;
    private String activityName;
    private User friendDetail;
    private LinearLayout balanceLayout;
    private TextView balanceView;
    private TextView balanceTextView;

    private String friendID;
    private String groupID;
    private String userID;

    private FirebaseDatabase firebaseDatabase = MainActivity.getDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private Double totBalance;


    public void setInterface(OnItemClickInterface onItemClickInterface) {
        onClickGroupInterface = onItemClickInterface;
    }

    public BarDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setInterface((OnItemClickInterface) getActivity());

        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_bar_detail, container, false);

        imageView = (ImageView) mainView.findViewById(R.id.img_photo);
        nameTextView = (TextView) mainView.findViewById(R.id.tv_bar_name);
        balanceLayout = (LinearLayout) mainView.findViewById(R.id.lv_balance_layout);
        balanceTextView = (TextView)mainView.findViewById(R.id.tv_balance_text);
        balanceView = (TextView)mainView.findViewById(R.id.tv_balance);

        initCollapsingToolbar();

        //Extract data from bundle
        Bundle bundle = this.getArguments();

        if(activityName.equals("FriendDetailActivity")){
            if(bundle != null){
                //Extract data from bundle
                friendID = bundle.getString("friendID");
                //Show data of friend
                databaseReference.child("users").child(friendID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String name = dataSnapshot.child("name").getValue(String.class);
                        String surname = dataSnapshot.child("surname").getValue(String.class);
                        nameTextView.setText(name + " " + surname);

                        // Loading profile image
                        Glide.with(getActivity()).load(dataSnapshot.child("image").getValue(String.class))
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(imageView);

                        balanceLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        }}
        else if(activityName.equals("GroupDetailActivity")){

            if(bundle != null){
                //Extract data from bundle
                groupID = bundle.getString("groupID");
                userID = bundle.getString("userID");

                //retrieve data of group
                databaseReference.child("groups").child(groupID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        totBalance = 0d;
                        String name = dataSnapshot.child("name").getValue(String.class);
                        nameTextView.setText(name);

                        // Loading group image into bar
                        Glide.with(getActivity()).load(dataSnapshot.child("image").getValue(String.class))
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(imageView);

                        // retrieve group balance
                        for (DataSnapshot groupExpenseSnapshot: dataSnapshot.child("expenses").getChildren())
                        {
                            //Ascolto ogni singola spesa del gruppo
                            final String expenseID = groupExpenseSnapshot.getKey();
                            Log.d(TAG, "considero la spesa "+expenseID);
                            databaseReference.child("expenses").child(expenseID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    Boolean involved = false; //dice se user contribuisce o no a quella spesa

                                    for (DataSnapshot participantSnapshot: dataSnapshot.child("participants").getChildren())
                                    {
                                        if (participantSnapshot.getKey().equals(userID))
                                            involved = true;
                                    }

                                    //se user ha partecipato alla spesa

                                    if (involved)
                                    {
                                        //alreadyPaid = soldi già messi dallo user per quella spesa
                                        //dueImport = quota che user deve mettere per quella spesa
                                        //balance = credito/debito dello user per quella spesa
                                        Double alreadyPaid = dataSnapshot.child("participants").child(userID).child("alreadyPaid").getValue(Double.class);
                                        Double dueImport = dataSnapshot.child("participants").child(userID).child("fraction").getValue(Double.class) * dataSnapshot.child("amount").getValue(Double.class);
                                        Double balance = alreadyPaid - dueImport;
                                        //se user per quella spesa ha già pagato più soldi della sua quota, il balance è positivo
                                        totBalance += balance;

                                        if(totBalance<0)
                                            balanceTextView.setText(getString(R.string.negative_balance));
                                        else
                                            balanceTextView.setText(getString(R.string.positive_balance));

                                        String balanceString = String.valueOf(totBalance.doubleValue())+ " €";
                                        balanceView.setText(balanceString);
                                        balanceLayout.setVisibility(View.VISIBLE);

                                        Log.d(TAG, "sono coinvolto nella spesa "+expenseID+", dovevo "+dueImport+", ho dato "+alreadyPaid+" -> totBalance: "+totBalance);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    balanceTextView.setText("Balance not available");
                                    balanceLayout.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        availableGroupData = false;
                    }
                });
            }
        }

        return mainView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // to know in which activity we are
        activityName = getActivity().getClass().getSimpleName();
     /*   if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

   /* *//**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     *//*
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }*/


    // Initializing collapsing toolbar: it will show and hide the toolbar title on scroll
    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout)((BasicActivity)getActivity()).findViewById(R.id.collapsingToolbar);

        collapsingToolbar.setTitle(" ");

        AppBarLayout appBarLayout = (AppBarLayout) ((BasicActivity)getActivity()).findViewById(R.id.app_bar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(nameTextView.getText());
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }
}
