package com.polito.mad17.madmax.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.expenses.PendingExpensesFragment;
import com.polito.mad17.madmax.activities.groups.GroupDetailActivity;
import com.polito.mad17.madmax.activities.groups.GroupsFragment;
import com.polito.mad17.madmax.activities.groups.NewGroupActivity;
import com.polito.mad17.madmax.activities.login.LogInActivity;
import com.polito.mad17.madmax.activities.users.FriendDetailActivity;
import com.polito.mad17.madmax.activities.users.FriendsFragment;
import com.polito.mad17.madmax.entities.Comment;
import com.polito.mad17.madmax.entities.Expense;
import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.entities.User;

import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.polito.mad17.madmax.R.string.friends;
//import static com.polito.mad17.madmax.activities.groups.GroupsViewAdapter.groups;
import static com.polito.mad17.madmax.activities.groups.GroupsViewAdapter.myself;

public class MainActivity extends AppCompatActivity implements OnItemClickInterface {

    private static final String TAG = MainActivity.class.getSimpleName();

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mDatabase;
    private String[] drawerOptions;
    private DrawerLayout drawerLayout;
    private ListView drawerList;

 //   private ActionBarDrawerToggle drawerToggle;

    private FirebaseAuth auth;
    private static final int REQUEST_INVITE = 0;

    public static User myself;
    //ID di Mario Rossi, preso dal db. Questo id mi serve per stampare le sue liste amici, gruppi ecc..
    //todo da sostituire con l'id dell'utente che si è loggato
    String myselfID = "-KjTCeDmpYY7gEOlYuSo";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = firebaseDatabase.getReference();

        //non cancellare anche se commentato! Serve per popolare il db
        /*
        if (!populated)
        {
            //Create users
            User u0 = new User(String.valueOf(0), "mariux",         "Mario", "Rossi",           "email0@email.it", "password0", "url", "€");
            User u1 = new User(String.valueOf(1), "Alero3",         "Alessandro", "Rota",       "email1@email.it", "password1", "url", "€" );
            User u2 = new User(String.valueOf(2), "deviz92",        "Davide", "Valentino",      "email2@email.it", "password2", "url", "€");
            User u3 = new User(String.valueOf(3), "missArmstrong",  "Chiara", "Di Nardo",       "email3@email.it", "password3", "url", "€");
            User u4 = new User(String.valueOf(4), "rickydivi",      "Riccardo", "Di Vittorio",  "email4@email.it", "password4", "url", "€");
            User u5 = new User(String.valueOf(5), "roxy",           "Rossella", "Mangiardi",    "email5@email.it", "password5", "url", "€");

            //Add users to Firebase
            String u0_id = mDatabase.child("users").push().getKey();
            mDatabase.child("users").child(u0_id).setValue(u0);
            String u1_id = mDatabase.child("users").push().getKey();
            mDatabase.child("users").child(u1_id).setValue(u1);
            String u2_id = mDatabase.child("users").push().getKey();
            mDatabase.child("users").child(u2_id).setValue(u2);
            String u3_id = mDatabase.child("users").push().getKey();
            mDatabase.child("users").child(u3_id).setValue(u3);
            String u4_id = mDatabase.child("users").push().getKey();
            mDatabase.child("users").child(u4_id).setValue(u4);
            String u5_id = mDatabase.child("users").push().getKey();
            mDatabase.child("users").child(u5_id).setValue(u5);

            //Create groups
            Group g1 = new Group(String.valueOf(0), "Vacanze",      "url", "description0");
            Group g2 = new Group(String.valueOf(1), "Calcetto",     "url", "description1");
            Group g3 = new Group(String.valueOf(2), "Spese Casa",   "url", "description2");
            Group g4 = new Group(String.valueOf(3), "Pasquetta",   "url", "description3");
            Group g5 = new Group(String.valueOf(4), "Fantacalcio",   "url", "description4");
            Group g6 = new Group(String.valueOf(5), "Alcolisti Anonimi",   "url", "description5");

            //Add groups to Firebase
            String g1_id = mDatabase.child("groups").push().getKey();
            mDatabase.child("groups").child(g1_id).setValue(g1);
            String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
            mDatabase.child("groups").child(g1_id).child("timestamp").setValue(timeStamp);

            String g2_id = mDatabase.child("groups").push().getKey();
            mDatabase.child("groups").child(g2_id).setValue(g2);
            timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
            mDatabase.child("groups").child(g2_id).child("timestamp").setValue(timeStamp);

            String g3_id = mDatabase.child("groups").push().getKey();
            mDatabase.child("groups").child(g3_id).setValue(g3);
            timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
            mDatabase.child("groups").child(g3_id).child("timestamp").setValue(timeStamp);

            String g4_id = mDatabase.child("groups").push().getKey();
            mDatabase.child("groups").child(g4_id).setValue(g4);
            timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
            mDatabase.child("groups").child(g4_id).child("timestamp").setValue(timeStamp);

            String g5_id = mDatabase.child("groups").push().getKey();
            mDatabase.child("groups").child(g5_id).setValue(g5);
            timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
            mDatabase.child("groups").child(g5_id).child("timestamp").setValue(timeStamp);

            String g6_id = mDatabase.child("groups").push().getKey();
            mDatabase.child("groups").child(g6_id).setValue(g6);
            timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
            mDatabase.child("groups").child(g6_id).child("timestamp").setValue(timeStamp);


            //Aggiungo utente a lista membri del gruppo e gruppo a lista gruppi nell'utente in Firebase
            joinGroupFirebase(u0_id,g1_id);
            joinGroupFirebase(u1_id,g1_id);
            joinGroupFirebase(u2_id,g1_id);
            joinGroupFirebase(u3_id,g1_id);
            joinGroupFirebase(u4_id,g1_id);
            joinGroupFirebase(u5_id,g1_id);


            joinGroupFirebase(u0_id,g2_id);
            joinGroupFirebase(u1_id,g2_id);
            joinGroupFirebase(u2_id,g2_id);
            joinGroupFirebase(u4_id,g2_id);

            joinGroupFirebase(u0_id,g3_id);
            joinGroupFirebase(u4_id,g3_id);

            joinGroupFirebase(u0_id,g4_id);
            joinGroupFirebase(u2_id,g4_id);

            joinGroupFirebase(u0_id,g5_id);
            joinGroupFirebase(u2_id,g5_id);

            joinGroupFirebase(u0_id,g6_id);
            joinGroupFirebase(u2_id,g6_id);

            //Add friends to users
            //u0 is friend of u1,u2,u3,u4,u5
            addFriendFirebase(u0_id,u1_id);
            addFriendFirebase(u0_id,u2_id);
            addFriendFirebase(u0_id,u3_id);
            addFriendFirebase(u0_id,u4_id);
            addFriendFirebase(u0_id,u5_id);




            //Spese in g1
            Expense e1 = new Expense(String.valueOf(0), "Nutella", "Cibo",          30d, "€", "urlBill",  "url", true, g1_id, u0_id);
            //Aggiungo i partecipanti alla spesa (tutti i membri del gruppo in questo caso)
            e1.getParticipants().put(u0_id, 0d);
            e1.getParticipants().put(u1_id, 0d);
            e1.getParticipants().put(u2_id, 0d);
            e1.getParticipants().put(u3_id, 0d);
            e1.getParticipants().put(u4_id, 0d);
            e1.getParticipants().put(u5_id, 0d);
            //Setto le percentuali da pagare di ogni participant, supponendo equallyDivided
            Integer t = e1.getParticipants().size();
            double percentage = (1 / (double) e1.getParticipants().size());

            for (Map.Entry<String, Double> participant : e1.getParticipants().entrySet())
            {
                participant.setValue(percentage);
            }



            Expense e2 = new Expense(String.valueOf(1), "Spese cucina", "Altro",    20d, "€",  "urlBill", "url", true, g1_id, u3_id);
            //Aggiungo i partecipanti alla spesa (tutti i membri del gruppo in questo caso)
            e2.getParticipants().put(u0_id, 0d);
            e2.getParticipants().put(u1_id, 0d);
            e2.getParticipants().put(u2_id, 0d);
            e2.getParticipants().put(u3_id, 0d);
            e2.getParticipants().put(u4_id, 0d);
            e2.getParticipants().put(u5_id, 0d);
            //Setto le percentuali da pagare di ogni participant, supponendo equallyDivided
            percentage = (1 / (double) e2.getParticipants().size());

            for (Map.Entry<String, Double> participant : e2.getParticipants().entrySet())
            {
                participant.setValue(percentage);
            }

            //returns id of the expense in db
            String e1_id = addExpenseFirebase(e1);
            String e2_id = addExpenseFirebase(e2);

            //Spese in g2
            Expense e3 = new Expense(String.valueOf(2), "Partita", "Sport",         5d, "€",  "urlBill",  "url", true, g2_id, u0_id);
            //Aggiungo i partecipanti alla spesa (tutti i membri del gruppo in questo caso)
            e3.getParticipants().put(u0_id, 0d);
            e3.getParticipants().put(u1_id, 0d);
            e3.getParticipants().put(u2_id, 0d);
            e3.getParticipants().put(u4_id, 0d);
            //Setto le percentuali da pagare di ogni participant, supponendo equallyDivided
            percentage = (1 / (double) e3.getParticipants().size());
            for (Map.Entry<String, Double> participant : e3.getParticipants().entrySet())
            {
                participant.setValue(percentage);
            }

            String e3_id = addExpenseFirebase(e3);


            //Spese in g3
            Expense e4 = new Expense(String.valueOf(3), "Affitto", "Altro",         500d, "€", "urlBill",  "url", true, g3_id, u4_id);
            //Aggiungo i partecipanti alla spesa (tutti i membri del gruppo in questo caso)
            e4.getParticipants().put(u0_id, 0d);
            e4.getParticipants().put(u4_id, 0d);
            //Setto le percentuali da pagare di ogni participant, supponendo equallyDivided
            percentage = (1 / (double) e4.getParticipants().size());
            for (Map.Entry<String, Double> participant : e4.getParticipants().entrySet())
            {
                participant.setValue(percentage);
            }
            String e4_id = addExpenseFirebase(e4);

            //Create comments
            String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
            Comment c1 = new Comment(u0_id, "This expense was not necessary", timestamp);
            Comment c2 = new Comment(u1_id, "Wow, that's great!", timestamp);
            Comment c3 = new Comment(u4_id, "No, it's a shit", timestamp);

            //Add comment to expense 1
            String c1_id = mDatabase.child("comments").child(e1_id).push().getKey();
            mDatabase.child("comments").child(e1_id).child(c1_id).setValue(c1);


            //Add comments to expense 2
            String c2_id = mDatabase.child("comments").child(e2_id).push().getKey();
            mDatabase.child("comments").child(e2_id).child(c2_id).setValue(c2);
            String c3_id = mDatabase.child("comments").child(e2_id).push().getKey();
            mDatabase.child("comments").child(e2_id).child(c3_id).setValue(c3);





            myself = u0;
            populated = true;
        }
        */








        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        setContentView(R.layout.activity_main);



        auth = FirebaseAuth.getInstance();

        // getting currenUID from Intent (from LogInActivity or EmailVerificationActivity)
        Intent i = getIntent();
        final String currentUID = i.getStringExtra("UID");

        final DatabaseReference usersRef = mDatabase.child("users");
        final DatabaseReference groupRef = mDatabase.child("groups");

        // getting currentUserRef from db
        DatabaseReference currentUserRef = usersRef.child(currentUID);
        if (currentUserRef == null) {
            Log.e(TAG, "unable to retrieve logged user from db");

            Toast.makeText(MainActivity.this, "unable to retrieve logged user from db", Toast.LENGTH_LONG).show();
            return;
        }

        // creating an object for current user
        final User currentUser = new User(
                currentUserRef.getKey(),
                currentUserRef.child("username").toString(),
                currentUserRef.child("name").toString(),
                currentUserRef.child("surname").toString(),
                currentUserRef.child("email").toString(),
                currentUserRef.child("password").toString(),
                currentUserRef.child("profileImage").toString(),
                currentUserRef.child("defaultCurrency").toString()
        );


        final HashMap<String, Double> balanceWithUsers = currentUser.getBalanceWithUsers();
        final HashMap<String, User> userFriends = currentUser.getUserFriends();

        // retrieving friends of the logged user from db
        usersRef.child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot friendsSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot friendSnapshot : friendsSnapshot.getChildren()) {
                    // populate balanceWithUsers for the logged User
                    String friendID = friendSnapshot.getKey();
                    DatabaseReference friendRef = friendSnapshot.getRef();

                    balanceWithUsers.put(
                            friendID,
                            Double.parseDouble(friendRef.child("balanceWithUser").toString())
                    );

                    // populate userFriends of the logged User
                    DatabaseReference userFriendsRef = usersRef.child(friendID);
                    User userFriend = new User(
                            friendID,
                            userFriendsRef.child("username").toString(),
                            userFriendsRef.child("name").toString(),
                            userFriendsRef.child("surname").toString(),
                            userFriendsRef.child("email").toString(),
                            userFriendsRef.child("profileImage").toString()
                    );

                    userFriends.put(
                            friendID,
                            userFriend
                    );

                    // todo siamo sicuri di volere shared_groups? sono da recuperare qui da db
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


        final HashMap<String, Double> balanceWithGroups = currentUser.getBalanceWithGroups();
        final HashMap<String, Group> userGroups = currentUser.getUserGroups();

        // retrieving groups of the logged user from db
        usersRef.child("groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot groupsSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot groupSnapshot : groupsSnapshot.getChildren()) {
                    // populate balanceWithUsers for the logged User
                    String groupID = groupSnapshot.getKey();
                    DatabaseReference userGroupRef = groupSnapshot.getRef();

                    balanceWithGroups.put(
                            groupID,
                            Double.parseDouble(userGroupRef.child("balanceWithGroup").toString())
                    );

                    // populate userGroups of the logged User
                    DatabaseReference userGroupsRef = groupRef.child(groupID);
                    Group userGroup = new Group(
                            groupID,
                            userGroupsRef.child("name").toString(),
                            userGroupsRef.child("image").toString(),
                            userGroupsRef.child("description").toString()
                    );

                    // todo qui ci sono ancora da recuperare le spese e i membri del gruppo corrente

                    userGroups.put(
                            groupID,
                            userGroup
                    );
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        myself = currentUser;

        /*

        README!

        a questo punto il solito myself dovrebbe essere caricato con tutte le info dell'utente loggato
        recuperate da db: per il momento invece di passarlo in tutti i punti in cui serve (non saprei
        bene come al momento), l'ho dichiarato pubblico com'era prima e recuperato i dati direttamente
        da lì negli altri file, non ho avuto molto tempo per ricontrollare tutto ma alla veloce ho cambiato
        tutti i punti in cui ad esempio si caricavano i gruppi facendo MainActivity.groups ->
        MainActivity.myself.getUserGroups() eccetera anche per gli amici => se avete idee migliori e
        correzioni varie fate pure

        a questo punto dentro myself ci sono caricate tutte le info che servono nell'app: tutti i suoi
        amici e tutti i suoi gruppi (che prima erano l'HashMap users e l'HashMap groups), a parte le cose
        che ho segnalato che non sono riuscito a finire di recuperare da db (sharedGroups per i friends,
        expenses e users per i groups) => i bug che ci sono ora credo siano dovuto a questo, ovvero che
        a volte il codice fa accesso a punti che ancora non sono stati recuperati da db (perchè non ho
        finito la query) ma per darvi codice funzionante ho preferito cominciare a darvi questo che è
        tutto il giorno che ci lavoro, almeno da qui potete ripartire per continuare in settimana visto
        che io non riesco ad esserci quando vi vedete

        per il momento non ho ottimizzato un granchè e penso che si possa ottimizzare in questo senso:
        per ora viene caricato tutto appena si carica la MainActivity ma può essere che sia meglio
        caricare fragment per fragment le info di cui c'è bisogno: cioè per il fragment della lista
        degli amici si caricano i friends dello user loggato con la query che ho fatto sopra, e all'interno
        del fragment della lista dei gruppi si caricano i groups, eccetera => vedete se riuscite a farlo
        voi, ma se funziona così possiamo pure lasciar perdere per ora

        ora non si vedono più liste di amici o di gruppi se vi loggate nell'app perchè non ho avuto
        tempo di creare nel formato giusto del db che abbiamo fatto l'altro giorno le entry di prova
        che avevamo fatto a codice staticamente, se avete tempo di sistemarle quando vi vedete, avendo
        il JSON del db corretto sottomano (quello che vi ho passato l'altro giorno) e riuscite a passarmele,
        magari di nuovo con un JSON io appena riesco le carico => per "sistemarle" intendo se riuscite
        a fare le associazioni "di default" in modo che senza dover aggiungere gruppi a mano nell'app
        ad esempio io possa caricarli direttamente a db e per ogni utente loggato si vedranno le rispettive
        info (i suoi gruppi, i suoi amici eccetera)

        il db agganciato ovviamente è il mio, con le modifiche che ho fatto ora si salvano anche le
        immagini caricate nello Storage di Firebase, ma non ho avuto tempo di vedere come recuperarle
        per farle vedere (ad esempio nel drawer di sinistra mostrare l'immagina dell'utente loggato)

        poi come vi dicevo su whataspp ora funziona l'invito degli amici perchè ho aggiunto la chiave
        sha1 nel mio db (quindi se riagganciate un vostro db per fare altre prove non andrà di nuovo)

        mi sembra più o meno di avervi detto tutto, ho modificato per lo più la parte di Ricky del login
        perché da li ora quando un utente si registra (SignUp) viene caricato a db con l'UID corrispondente
        a quello fornito dal FirebaseAuth; in più nello User ho aggiunto un costruttore che era utile
        per la lista degli amici dello user loggato

        infine nello User ho aggiunto codice per salvare le password criptate in MD5: l'ho messo in un
        metodo; ma non ho messo in un metodo ad esempio la parte per salvare le immagini (se volete per
        adesso lasciatela perdere e commentatela se vi da problemi visto che è messa lì un po' a caso),
        se aveste bisogno se qualcuno di voi continuerà a lavorare su quella parte poi vi aggiorno

        ho anche sistemato i file in cartelle all'interno delle activities perchè non ci stavo capendo
        più un cazzo, se avete soluzioni migliori fate pure ;)

         */

        drawerOptions = getResources().getStringArray(R.array.drawerItem);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerList = (ListView)findViewById(R.id.left_drawer);

        // set the adapter for the Listview
        drawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, drawerOptions));

        // set the click's listener
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 1){
                    Toast.makeText(MainActivity.this, "Logout selected", Toast.LENGTH_SHORT).show();
                    auth.signOut();

                    Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                    startActivity(intent);
                    finish();
                }
                else if(position == 0){
                    Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                            .setMessage(getString(R.string.invitation_message))
     //                       .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))
     //                       .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                            .setCallToActionText(getString(R.string.invitation_cta))
                            .build();
                    startActivityForResult(intent, REQUEST_INVITE);
                }
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(friends));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.groups));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.pending));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.main_view_pager);
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // todo diverse azioni a seconda del fragment in cui mi trovo
                // getSupportFragmentManager().findFragmentByTag()
                Intent myIntent = new Intent(MainActivity.this, NewGroupActivity.class);
                myIntent.putExtra("UID", myselfID);
                //String tempGroupID = mDatabase.child("temporarygroups").push().getKey();
                //inizialmente l'unico user è il creatore del gruppo stesso
                User myself = new User(myselfID, "mariux",         "Mario", "Rossi",           "email0@email.it", "password0", null, "€");
                //mDatabase.child("temporarygroups").child(tempGroupID).child("members").push();
                //mDatabase.child("temporarygroups").child(tempGroupID).child("members").child(myself.getID()).setValue(myself);
                NewGroupActivity.newmembers.put(myself.getID(), myself);  //inizialmente l'unico membro del nuovo gruppo sono io
                //myIntent.putExtra("groupID", tempGroupID);
                MainActivity.this.startActivity(myIntent);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if(requestCode == REQUEST_INVITE){
            if(resultCode == RESULT_OK){
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.i(TAG, "onActivityResult: sent invitation " + id);
                }
            } else {
                // Sending failed or it was canceled, show failure message to the user
                Log.e(TAG, "onActivityResult: failed sent");

                Toast.makeText(MainActivity.this, "Unable to send invitation", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public class PagerAdapter extends FragmentStatePagerAdapter {

        int numberOfTabs;

        public PagerAdapter(FragmentManager fragmentManager, int numberOfTabs) {
            super(fragmentManager);
            this.numberOfTabs = numberOfTabs;
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    Log.i(TAG, "here in case 0: FriendsFragment");
                    FriendsFragment friendsFragment = new FriendsFragment();
                    return friendsFragment;
                case 1:
                    Log.i(TAG, "here in case 1: GroupsFragment");
                    GroupsFragment groups1Fragment = new GroupsFragment();
                    return groups1Fragment;
                case 2:
                    Log.i(TAG, "here in case 2: PendingExpensesFragment");
                    PendingExpensesFragment pendingExpensesFragment = new PendingExpensesFragment();
                    return pendingExpensesFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return numberOfTabs;
        }
    }

    @Override
    public void itemClicked(String fragmentName, String itemID) {

        Log.i(TAG, "fragmentName " + fragmentName + " itemID " + itemID);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Bundle bundle = new Bundle();
        Intent intent = null;

        switch(fragmentName) {
            case "FriendsFragment":
                User friendDetail = myself.getUserFriends().get(itemID);
                bundle.putParcelable("friendDetail", friendDetail);

                intent = new Intent(this, FriendDetailActivity.class);
                intent.putExtra("friendDetails", friendDetail);
                startActivity(intent);

//                FriendDetailFragment friendDetailFragment = new FriendDetailFragment();
//                friendDetailFragment.setArguments(bundle);
//
//                fragmentTransaction.addToBackStack(null);
//                fragmentTransaction.replace(R.id.main_content, friendDetailFragment);

//                fragmentTransaction.commit();

                break;

            case "GroupsFragment":
                //todo mettere a posto
                Group groupDetail = null; // groups.get(itemID);
                //bundle.putParcelable("groupDetails", groupDetail);

                intent = new Intent(this, GroupDetailActivity.class);
                intent.putExtra("groupID", itemID);
                startActivity(intent);

                break;
        }

    }



    public String addExpenseFirebase(Expense expense) {

        //Aggiungo spesa a Firebase
        String eID = mDatabase.child("expenses").push().getKey();
        mDatabase.child("expenses").child(eID).setValue(expense);
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        mDatabase.child("expenses").child(eID).child("timestamp").setValue(timeStamp);


        //Aggiungo spesa alla lista spese del gruppo
        mDatabase.child("groups").child(expense.getGroupID()).child("expenses").push();
        mDatabase.child("groups").child(expense.getGroupID()).child("expenses").child(eID).setValue("true");

        return eID;

        //u.updateBalance(expense);
        //updateBalanceFirebase(u, expense);
    }

    // update balance among other users and among the group this user is part of
    private void updateBalanceFirebase (final User u, final Expense expense) {
        // todo per ora fa il calcolo come se le spese fossero sempre equamente divise fra tutti i
        // todo     membri del gruppo (cioè come se expense.equallyDivided fosse sempre = true


        final String groupID = expense.getGroupID();


        Query query = mDatabase.child("groups").child(groupID);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Double amount = 0d; //spesa totale nel gruppo

                DataSnapshot groupSnapshot = dataSnapshot.child("groups").child(groupID);

                for (DataSnapshot expense : groupSnapshot.child("expenses").getChildren())
                {
                    amount += expense.child("amount").getValue(Double.class);
                }


                Long membersCount = groupSnapshot.child("members").getChildrenCount();
                Double singlecredit = expense.getAmount() / membersCount;
                Double totalcredit = singlecredit * (membersCount -1); //credito totale che io ho verso tutti gli altri membri del gruppo
                //debito attuale dello user verso il gruppo
                Double actualdebts =  dataSnapshot.child("users").child(u.getID()).child("groups").child(groupID).child("balanceWithGroup").getValue(Double.class);

                if (actualdebts != null) {
                    //aggiorno il mio debito verso il gruppo
                    mDatabase.child("users").child(u.getID()).child("groups").child(groupID).child("balanceWithGroup").setValue(actualdebts+totalcredit);
                }
                else {
                    System.out.println("Group not found");
                }

                for (DataSnapshot member : groupSnapshot.child("members").getChildren())
                {
                    //se non sono io stesso
                    if (!member.getKey().equals(u.getID()))
                    {
                        Double balance = dataSnapshot.child("users").child(u.getID()).child("balancesWithUsers").child(member.getKey()).getValue(Double.class);
                        if (balance != null) {
                            mDatabase.child("users").child(u.getID()).child("balancesWithUsers").child(member.getKey()).setValue(balance+singlecredit);
                        }
                    }

                    //aggiorno debito dell'amico verso di me

                    //debito dell'amico verso di me
                    Double balance = dataSnapshot.child("users").child(member.getKey()).child("balancesWithUsers").child(u.getID()).getValue(Double.class);

                    if (balance != null)
                    {
                        mDatabase.child("users").child(member.getKey()).child("balancesWithUsers").child(u.getID()).setValue(balance-singlecredit);

                    }
                    else
                    {
                        System.out.println("Io non risulto tra i suoi debiti");
                        // => allora devo aggiungermi
                        mDatabase.child("users").child(member.getKey()).child("balancesWithUsers").child(u.getID()).setValue(-singlecredit);
                    }

                    //aggiorno il debito dell'amico verso il gruppo
                    balance = dataSnapshot.child("users").child(member.getKey()).child("groups").child(groupID).child("balanceWithGroup").getValue(Double.class);

                    if (balance != null)
                    {
                        mDatabase.child("users").child(member.getKey()).child("groups").child(groupID).child("balanceWithGroup").setValue(balance-singlecredit);
                    }
                    else
                    {
                        System.out.println("Gruppo non risulta tra i suoi debiti");
                        // => allora lo devo aggiungere
                        mDatabase.child("users").child(member.getKey()).child("groups").child(groupID).child("balanceWithGroup").setValue(-singlecredit);
                    }


                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




        /*
        Double total = g.getTotalExpense(); //spesa totale del gruppo aggiornata
        Double singlecredit = expense.getAmount() / g.getMembers().size();   //credito che io ho verso ogni singolo utente in virtù della spesa che ho fatto
        Double totalcredit = singlecredit * (g.getMembers().size() -1); //credito totale che io ho verso tutti gli altri membri del gruppo
        //es. se in un gruppo di 5 persone io ho pagato 10, ognuno mi deve 2
        //quindi totalcredit = 2*4 dove 4 è il n. di membri del gruppo diversi da me. In tutto devo ricevere 8.

        Double actualdebts = balanceWithGroups.get(g.getID());
        if (actualdebts != null) {
            //aggiorno il mio debito verso il gruppo
            balanceWithGroups.put(g.getID(), actualdebts + totalcredit);
        }
        else {
            System.out.println("Group not found");
        }

        //per ogni amico del gruppo in cui è stata aggiunta la spesa
        for (HashMap.Entry<String, User> friend : g.getMembers().entrySet()) {
            //se non sono io stesso
            if (!friend.getKey().equals(this.getID())) {
                //aggiorno mio credito verso di lui
                Double balance = balanceWithUsers.get(friend.getKey());
                if (balance != null) {
                    balanceWithUsers.put(friend.getKey(), balance+singlecredit);
                }
                else {
                    System.out.println("Friend not found");
                }

                //aggiorno debito dell'amico verso di me
                HashMap<String, Double> friendBalanceWithUsers = friend.getValue().getBalanceWithUsers();
                balance = friendBalanceWithUsers.get(this.getID());;

                if (balance != null) {
                    friend.getValue().getBalanceWithUsers().put(this.getID(), balance-singlecredit);
                }
                else {
                    System.out.println("Io non risulto tra i suoi debiti");
                    // => allora devo aggiungermi
                    friendBalanceWithUsers.put(this.getID(), -singlecredit);
                }

                //aggiorno debito dell'amico verso il gruppo
                HashMap<String, Double> friendBalanceWithGroups = friend.getValue().getBalanceWithGroups();
                balance = friendBalanceWithGroups.get(g.getID());
                if (balance != null) {
                    friend.getValue().getBalanceWithGroups().put(g.getID(), balance-singlecredit);
                }
                else {
                    System.out.println("Gruppo non risulta tra i suoi debiti");
                    // => allora lo devo aggiungere
                    friendBalanceWithGroups.put(g.getID(), -singlecredit);
                }
            }
        }

    }
    */


    /*
    //ritorna i soldi totali spesi dal gruppo (packake-private: visibilità di default)
    Double getTotalExpenseFirebase (String groupID) {

        Query query =  mDatabase.child("groups").child(groupID).child("expenses");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Double total = 0d;

                for (DataSnapshot expenseSnapshot: dataSnapshot.getChildren())
                {
                    total += expenseSnapshot.child("amount").getValue(Double.class);
                }
                return total;

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        for (HashMap.Entry<String, Expense> expense : expenses.entrySet()) {
            total += expense.getValue().getAmount();
        }


        return total;
    }



    }
    */


    }

    public void joinGroupFirebase (final String userID, String groupID)
    {
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //Aggiungo gruppo alla lista gruppi dello user
        mDatabase.child("users").child(userID).child("groups").push();
        mDatabase.child("users").child(userID).child("groups").child(groupID).setValue("true");
        //Aggiungo user (con sottocampi admin e timestamp) alla lista membri del gruppo
        mDatabase.child("groups").child(groupID).child("members").push();
        mDatabase.child("groups").child(groupID).child("members").child(userID).push();
        mDatabase.child("groups").child(groupID).child("members").child(userID).child("admin").setValue("false");
        mDatabase.child("groups").child(groupID).child("members").child(userID).push();
        mDatabase.child("groups").child(groupID).child("members").child(userID).child("timestamp").setValue("time");

    }

    public void addFriendFirebase (final String user1ID, final String user2ID)
    {
        //Add u2 to friend list of u1
        mDatabase.child("users").child(user1ID).child("friends").push();
        mDatabase.child("users").child(user1ID).child("friends").child(user2ID).setValue("true");
        //Add u1 to friend list of u2
        mDatabase.child("users").child(user2ID).child("friends").push();
        mDatabase.child("users").child(user2ID).child("friends").child(user1ID).setValue("true");

        //Read groups u1 belongs to
        Query query = mDatabase.child("users").child(user1ID).child("groups");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final ArrayList<String> u1Groups = new ArrayList<String>();

                for (DataSnapshot groupSnapshot: dataSnapshot.getChildren())
                {
                    u1Groups.add(groupSnapshot.getKey());
                }

                Query query = mDatabase.child("users").child(user2ID).child("groups");
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        ArrayList<String> sharedGroups = new ArrayList<String>();


                        for (DataSnapshot groupSnapshot: dataSnapshot.getChildren())
                        {
                            if (u1Groups.contains(groupSnapshot.getKey()))
                                sharedGroups.add(groupSnapshot.getKey());
                        }

                        //ora in sharedGroups ci sono solo i gruppi di cui fanno parte entrambi gli utenti
                        for (String groupID : sharedGroups)
                        {
                            mDatabase.child("users").child(user1ID).child("friends").child(user2ID).child(groupID).setValue("true");
                            mDatabase.child("users").child(user2ID).child("friends").child(user1ID).child(groupID).setValue("true");
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


}
