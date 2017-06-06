package com.polito.mad17.madmax.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.groups.BalancesActivity;
import com.polito.mad17.madmax.activities.groups.PayGroupActivity;
import com.polito.mad17.madmax.entities.User;
import com.polito.mad17.madmax.utilities.FirebaseUtils;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class BarDetailFragment extends Fragment {

    private static final String TAG = BarDetailFragment.class.getSimpleName();
    private OnItemClickInterface onClickGroupInterface;
    private boolean availableGroupData;
    private View mainView;
    private ImageView imageView;
    private TextView nameTextView;
    private String activityName;
    private User friendDetail;
    private RelativeLayout balanceLayout;
    private TextView balanceView;
    private TextView balanceTextView;

    private String friendID;
    private String groupID;
    private String userID;
    private String groupName;
    private String defaultCurrency;

    private FirebaseDatabase firebaseDatabase = FirebaseUtils.getFirebaseDatabase();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    //private Double totBalance;
    private ValueEventListener groupListener;
    Boolean listenedGroup = false;
    private Button payButton;
    DecimalFormat df = new DecimalFormat("#.##");
    String shownCurr;
    Double shownBal;


    //key = currency
    //value = balance for that currency
    private HashMap<String, Double> totBalances = new HashMap<>();

    static final int PAY_GROUP_REQUEST = 1;  // The request code

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

        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_bar_detail, container, false);

        imageView = (ImageView) mainView.findViewById(R.id.img_photo);
        nameTextView = (TextView) mainView.findViewById(R.id.tv_bar_name);
        balanceLayout = (RelativeLayout) mainView.findViewById(R.id.lv_balance_layout);
        balanceTextView = (TextView)mainView.findViewById(R.id.tv_balance_text);
        balanceView = (TextView)mainView.findViewById(R.id.tv_balance);
        payButton = (Button) mainView.findViewById(R.id.btn_pay_debt);

        initCollapsingToolbar();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        defaultCurrency = sharedPref.getString(SettingsFragment.DEFAULT_CURRENCY, "");
        //Extract data from bundle
        Bundle bundle = this.getArguments();

        if(activityName.equals("FriendDetailActivity")){
            if(bundle != null){

                payButton.setVisibility(View.GONE);
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
                                .into(imageView);

                        balanceLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        }}
        else if(activityName.equals("GroupDetailActivity"))
        {
            if(bundle != null){
                //Extract data from bundle
                groupID = bundle.getString("groupID");
                userID = bundle.getString("userID");

                setInterface((OnItemClickInterface) getActivity());

                payButton.setOnClickListener( new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Log.d (TAG, "Clicked payButton");

                        //Suppongo di non avere debiti in nessuna valuta
                        Boolean mustPay = false;
                        String currency = null;
                        //Se ho debiti in anche una sola valuta, allora posso entrare nella PayGroupActivity
                        for (Map.Entry<String, Double> entry : totBalances.entrySet())
                        {
                            if (entry.getValue() < 0)
                            {
                                mustPay = true;
                                currency = entry.getKey();
                            }

                        }

                        //Se ho debiti in almeno una valuta
                        if (mustPay)
                        {
                            Intent intent = new Intent(getActivity(), PayGroupActivity.class);
                            intent.putExtra("groupID", groupID);
                            intent.putExtra("userID", userID);
                            intent.putExtra("totBalances", totBalances);
                            intent.putExtra("shownCurrency", currency);
                            intent.putExtra("groupName", groupName);
                            startActivity(intent);
                        }
                        //Se non ho debiti in nessuna valuta
                        else
                        {
                            Toast.makeText(getActivity(),getString(R.string.no_debts_to_pay),Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                balanceView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d (TAG, "Clicked balance");
                        Intent intent = new Intent(getActivity(), BalancesActivity.class);
                        intent.putExtra("balances", totBalances);
                        intent.putExtra("groupID", groupID);
                        startActivity(intent);

                    }
                });


                // todo qui currency
                //retrieve data of group
                groupListener = databaseReference.child("groups").child(groupID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (!listenedGroup)
                            listenedGroup = true;

                        //totBalance = 0d;

                        totBalances.clear();

                        //Retrieve group name
                        groupName = dataSnapshot.child("name").getValue(String.class);
                        if (groupName != null)
                            nameTextView.setText(groupName);

                        //Retrieve group image
                        String image = dataSnapshot.child("image").getValue(String.class);
                        if (image != null && !image.equals("noImage"))
                        {
                            Log.d (TAG, "Nome gruppo: " + dataSnapshot.child("name").getValue(String.class) + "  Immagine: " + image);
                            // Loading group image into bar
                            Glide.with(getActivity()).load(dataSnapshot.child("image").getValue(String.class))
                                    .centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(imageView);
                        }
                        else {
                            Log.d (TAG, "Nome gruppo: " + dataSnapshot.child("name").getValue(String.class) + "  Immagine di default");
                            // Loading group image into bar
                            Glide.with(getActivity()).load(R.drawable.group_default)
                                    .centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(imageView);
                        }

                        //Retrieve group balances in all currencies
                        for (DataSnapshot groupExpenseSnapshot : dataSnapshot.child("expenses").getChildren())
                        {
                            //Se la spesa non è stata eliminata
                            if (groupExpenseSnapshot.getValue(Boolean.class) == true)
                            {
                                //Ascolto la singola spesa del gruppo
                                final String expenseID = groupExpenseSnapshot.getKey();
                                Log.d(TAG, "considero la spesa " + expenseID);
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
                                            String currency = dataSnapshot.child("currency").getValue(String.class);

                                            //current balance for that currency
                                            Double temp = totBalances.get(currency);
                                            //update balance for that currency
                                            if (temp != null)
                                            {
                                                totBalances.put(currency, temp + balance);
                                            }
                                            else
                                            {
                                                totBalances.put(currency, balance);
                                            }

                                            //se user per quella spesa ha già pagato più soldi della sua quota, il balance è positivo
                                            //totBalance += balance;

                                            Boolean multipleCurrencies = false;

                                            balanceLayout.setVisibility(View.VISIBLE);



                                            if (!totBalances.isEmpty())
                                            {
                                                //If there is more than one currency
                                                if (totBalances.size() > 1)
                                                {
                                                    multipleCurrencies = true;

                                                }
                                                //If there is just one currency
                                                else
                                                {
                                                    multipleCurrencies = false;
                                                }

                                                if (totBalances.containsKey(defaultCurrency))
                                                {
                                                    shownBal = totBalances.get(defaultCurrency);
                                                    shownCurr = defaultCurrency;
                                                }
                                                else
                                                {
                                                    shownCurr = (String) totBalances.keySet().toArray()[0];
                                                    shownBal = totBalances.get(shownCurr);
                                                }

                                                //Print balance
                                                if (shownBal > 0)
                                                {
                                                    balanceTextView.setText(R.string.you_should_receive);

                                                    if (multipleCurrencies)
                                                        balanceView.setText(df.format(shownBal) + " " + shownCurr + "*");
                                                    else
                                                        balanceView.setText(df.format(shownBal) + " " + shownCurr);
                                                }
                                                else if (shownBal < 0)
                                                {
                                                    balanceTextView.setText(R.string.you_owe);

                                                    if (multipleCurrencies)
                                                        balanceView.setText(df.format(Math.abs(shownBal)) + " " + shownCurr + "*");
                                                    else
                                                        balanceView.setText(df.format(Math.abs(shownBal)) + " " + shownCurr);
                                                }
                                                else if (shownBal == 0)
                                                {
                                                    balanceTextView.setText(R.string.no_debts);
                                                    balanceView.setText("0 " + defaultCurrency);
                                                }

                                            }
                                            //If there are no balances in the map
                                            else
                                            {
                                                balanceTextView.setText(R.string.no_debts);
                                                balanceView.setText("0 " + defaultCurrency);
                                            }


                                            Log.d(TAG, "sono coinvolto nella spesa "+expenseID+", dovevo "+dueImport+", ho dato "+alreadyPaid);
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
                        //Ora ho finito di calcolare i bilanci
                        /*
                        if(totBalance<0)
                            balanceTextView.setText(getString(R.string.negative_balance));
                        else
                            balanceTextView.setText(getString(R.string.positive_balance));
                            */



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
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout)((AppCompatActivity)getActivity()).findViewById(R.id.collapsingToolbar);

        collapsingToolbar.setTitle(" ");

        AppBarLayout appBarLayout = (AppBarLayout) ((AppCompatActivity)getActivity()).findViewById(R.id.app_bar);
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

    //When i return from PayGroupActivity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAY_GROUP_REQUEST) {
            if(resultCode == RESULT_OK) {
                userID = data.getStringExtra("userID");
                groupID = data.getStringExtra("groupID");

            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d (TAG, "OnStop");
        if (listenedGroup)
            databaseReference.child("groups").child(groupID).removeEventListener(groupListener);

    }
}
