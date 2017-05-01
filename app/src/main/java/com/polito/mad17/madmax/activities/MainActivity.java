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
import com.polito.mad17.madmax.entities.Expense;
import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.entities.User;

import java.util.HashMap;
import java.util.Map;

import static com.polito.mad17.madmax.R.string.friends;
import static com.polito.mad17.madmax.activities.groups.GroupsViewAdapter.groups;
import static com.polito.mad17.madmax.activities.groups.GroupsViewAdapter.myself;

public class MainActivity extends AppCompatActivity implements OnItemClickInterface {

    private static final String TAG = MainActivity.class.getSimpleName();

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private String[] drawerOptions;
    private DrawerLayout drawerLayout;
    private ListView drawerList;

 //   private ActionBarDrawerToggle drawerToggle;

    private FirebaseAuth auth;
    private static final int REQUEST_INVITE = 0;


    public static User myself;

    /*
    public static HashMap<String, Group> groups = new HashMap<>();
    public static HashMap<String, User> users = new HashMap<>();


    Integer[] imgid={
            R.drawable.ale,
            R.drawable.davide,
            R.drawable.chiara,
            R.drawable.riki,
            R.drawable.rossella,
            R.drawable.vacanze,
            R.drawable.calcetto,
            R.drawable.casa,
            R.drawable.pasquetta,
            R.drawable.fantacalcio,
            R.drawable.alcolisti
    };

    Integer[] img_expense={
            R.drawable.expense1,
            R.drawable.expense2,
            R.drawable.expense3,
            R.drawable.expense4,
            R.drawable.expense5,
            R.drawable.expense6,
            R.drawable.expense7
    };
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*
        if (users.isEmpty())
        {
            //Create users
            User u0 = new User(String.valueOf(0), "mariux",         "Mario", "Rossi",           "email0@email.it", "password0", null);
            User u1 = new User(String.valueOf(1), "Alero3",         "Alessandro", "Rota",       "email1@email.it", "password1", String.valueOf(imgid[0]));
            User u2 = new User(String.valueOf(2), "deviz92",        "Davide", "Valentino",      "email2@email.it", "password2", String.valueOf(imgid[1]));
            User u3 = new User(String.valueOf(3), "missArmstrong",  "Chiara", "Di Nardo",       "email3@email.it", "password3", String.valueOf(imgid[2]));
            User u4 = new User(String.valueOf(4), "rickydivi",      "Riccardo", "Di Vittorio",  "email4@email.it", "password4", String.valueOf(imgid[3]));
            User u5 = new User(String.valueOf(5), "roxy",           "Rossella", "Mangiardi",    "email5@email.it", "password5", String.valueOf(imgid[4]));

            //Add users to database
            mDatabase.child("users").child(u0.getID()).setValue(u0);
            mDatabase.child("users").child(u1.getID()).setValue(u1);
            mDatabase.child("users").child(u2.getID()).setValue(u2);
            mDatabase.child("users").child(u3.getID()).setValue(u3);
            mDatabase.child("users").child(u4.getID()).setValue(u4);
            mDatabase.child("users").child(u5.getID()).setValue(u5);

            //Add to users list (needed to share data with other activities)
            users.put(u0.getID(), u0);
            users.put(u1.getID(), u1);
            users.put(u2.getID(), u2);
            users.put(u3.getID(), u3);
            users.put(u4.getID(), u4);
            users.put(u4.getID(), u5);

            Group g1 = new Group(String.valueOf(0), "Vacanze",      String.valueOf(imgid[5]), "description0");
            Group g2 = new Group(String.valueOf(1), "Calcetto",     String.valueOf(imgid[6]), "description1");
            Group g3 = new Group(String.valueOf(2), "Spese Casa",   String.valueOf(imgid[7]), "description2");
            Group g4 = new Group(String.valueOf(3), "Pasquetta",   String.valueOf(imgid[8]), "description3");
            Group g5 = new Group(String.valueOf(4), "Fantacalcio",   String.valueOf(imgid[9]), "description4");
            Group g6 = new Group(String.valueOf(5), "Alcolisti Anonimi",   String.valueOf(imgid[10]), "description5");

            //Add groups to database
            mDatabase.child("groups").child(g1.getID()).setValue(g1);
            mDatabase.child("groups").child(g2.getID()).setValue(g2);
            mDatabase.child("groups").child(g3.getID()).setValue(g3);
            mDatabase.child("groups").child(g4.getID()).setValue(g4);
            mDatabase.child("groups").child(g5.getID()).setValue(g5);
            mDatabase.child("groups").child(g6.getID()).setValue(g6);

            //Aggiungo utente a lista membri del gruppo e gruppo a lista gruppi nell'utente in Firebase
            joinGroupFirebase(u0,g1);
            joinGroupFirebase(u1,g1);
            joinGroupFirebase(u2,g1);
            joinGroupFirebase(u3,g1);
            joinGroupFirebase(u4,g1);
            joinGroupFirebase(u5,g1);


            joinGroupFirebase(u0,g2);
            joinGroupFirebase(u1,g2);
            joinGroupFirebase(u2,g2);
            joinGroupFirebase(u4,g2);

            joinGroupFirebase(u0,g3);
            joinGroupFirebase(u4,g3);

            joinGroupFirebase(u0,g4);
            joinGroupFirebase(u2,g4);

            joinGroupFirebase(u0,g5);
            joinGroupFirebase(u2,g5);

            joinGroupFirebase(u0,g6);
            joinGroupFirebase(u2,g6);


            //Add users to group
            u0.joinGroup(g1);
            u1.joinGroup(g1);
            u2.joinGroup(g1);
            u3.joinGroup(g1);
            u4.joinGroup(g1);
            u5.joinGroup(g1);

            u0.joinGroup(g2);
            u1.joinGroup(g2);
            u2.joinGroup(g2);
            u4.joinGroup(g2);

            u0.joinGroup(g3);
            u4.joinGroup(g3);

            u0.joinGroup(g4);
            u2.joinGroup(g4);

            u0.joinGroup(g5);
            u2.joinGroup(g5);

            u0.joinGroup(g6);
            u2.joinGroup(g6);

            groups.put(g1.getID(), g1);
            groups.put(g2.getID(), g2);
            groups.put(g3.getID(), g3);
            groups.put(g4.getID(), g4);
            groups.put(g5.getID(), g5);
            groups.put(g6.getID(), g6);

            //Spese in g1
            Expense e1 = new Expense(String.valueOf(0), "Nutella", "Cibo",          30d, "€",   String.valueOf(img_expense[0]), true, g1.getID());
            Expense e2 = new Expense(String.valueOf(1), "Spese cucina", "Altro",    20d, "€",   String.valueOf(img_expense[1]), true, g1.getID());
            //u0.addExpense(e1);
            //u3.addExpense(e2);
            addExpenseFirebase(u0,e1);
            addExpenseFirebase(u3,e2);

            //Spese in g2
            Expense e3 = new Expense(String.valueOf(2), "Partita", "Sport",         5d, "€",    String.valueOf(img_expense[2]), true, g2.getID());
            //u0.addExpense(e3);
            addExpenseFirebase(u0,e3);

            //Spese in g3
            Expense e4 = new Expense(String.valueOf(3), "Affitto", "Altro",         500d, "€",  String.valueOf(img_expense[3]), true, g3.getID());
            //u4.addExpense(e4);
            addExpenseFirebase(u4,e4);

            //Add expenses to Firebase

            mDatabase.child("expenses").child(e1.getID()).setValue(e1);
            mDatabase.child("expenses").child(e2.getID()).setValue(e2);
            mDatabase.child("expenses").child(e3.getID()).setValue(e3);
            mDatabase.child("expenses").child(e4.getID()).setValue(e4);

            myself = u0;
        }
        */



        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        setContentView(R.layout.activity_main);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        auth = FirebaseAuth.getInstance();

        // getting currenUID from Intent (from LogInActivity or EmailVerificationActivity)
        Intent i = getIntent();
        final String currentUID = i.getStringExtra("UID");

        final DatabaseReference usersRef = databaseReference.child("users");
        final DatabaseReference groupRef = databaseReference.child("groups");

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
                currentUserRef.child("profileImage").toString()
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
                String tempGroupID = databaseReference.child("temporarygroups").push().getKey();
                //inizialmente l'unico user è il creatore del gruppo stesso
                NewGroupActivity.newmembers.put(myself.getID(), myself);  //inizialmente l'unico membro del nuovo gruppo sono io
                User myself = new User(String.valueOf(0), "mariux",         "Mario", "Rossi",           "email0@email.it", "password0", null);
                databaseReference.child("temporarygroups").child(tempGroupID).child("members").push();
                databaseReference.child("temporarygroups").child(tempGroupID).child("members").child(myself.getID()).setValue(myself);
                NewGroupActivity.newmembers.put(myself.getID(), myself);  //inizialmente l'unico membro del nuovo gruppo sono io
                myIntent.putExtra("groupID", tempGroupID);
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
                Group groupDetail = groups.get(itemID);
                bundle.putParcelable("groupDetails", groupDetail);

                intent = new Intent(this, GroupDetailActivity.class);
                intent.putExtra("groupDetails", groupDetail);
                startActivity(intent);

                break;
        }

    }







    public void joinGroupFirebase (final User u, Group g) {
        //Creo istanza del gruppo nella lista gruppi dello user
        databaseReference.child("users").child(u.getID()).child("groups").push();
        databaseReference.child("groups").child(g.getID()).child("members").push();

        Map<String, Object> groupValues = g.toMap();
        Map <String, Object> userValues = u.toMap();

        Map <String, Object> childUpdates = new HashMap<>();
        //metto nella map il gruppo a cui appartiene lo user
        childUpdates.put("/users/" + u.getID() + "/groups/" + g.getID(), groupValues);
        childUpdates.put("/groups/" + g.getID() + "/members/" + u.getID(), userValues);

        databaseReference.updateChildren(childUpdates);

        //creo un debito verso il gruppo
        databaseReference.child("users").child(u.getID()).child("groups").child(g.getID()).child("balanceWithGroup").setValue(0);

        Query query = databaseReference.child("groups").child(g.getID()).child("members").orderByKey();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    System.out.println("There is at least one member in this group");
                    for (DataSnapshot memberSnapshot: dataSnapshot.getChildren())
                    {
                        System.out.println(memberSnapshot.getKey());
                        //se il nuovo user non è già presente nel gruppo (teoricamente impossibile)
                        if (!memberSnapshot.getKey().equals(u.getID()))
                        {
                            //se il bilancio tra nuovo user e membro del gruppo non esiste già
                            if (!memberSnapshot.child("balancesWithUsers").hasChild(u.getID()))
                            {
                                //creo bilancio da membro a nuovo user
                                databaseReference.child("users").child(memberSnapshot.getKey()).child("balancesWithUsers").child(u.getID()).setValue(0);
                                //creo bilancio da nuovo user a membro
                                databaseReference.child("users").child(u.getID()).child("balancesWithUsers").child(memberSnapshot.getKey()).setValue(0);
                            }

                        }
                        else
                        {
                            System.out.println("User is already present in the group!");
                        }


                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void addExpenseFirebase(User u, Expense expense) {
        u.getAddedExpenses().put(expense.getID(), expense);  //spesa aggiunta alla lista spese utente
        //expense.getGroup().getExpenses().put(expense.getID(), expense);   //spesa aggiunta alla lista spese del gruppo
        String groupID = expense.getGroupID();
        Group g = groups.get(groupID);
        if ( g != null)
        {
            g.getExpenses().put(expense.getID(), expense);
        }

        //Creo istanza della spesa nella lista spese dello user e del gruppo
        databaseReference.child("users").child(u.getID()).child("expenses").push();
        databaseReference.child("groups").child(expense.getGroupID()).child("expenses").push();

        Map <String, Object> expensesValues = expense.toMap();

        Map <String, Object> childUpdates = new HashMap<>();
        //metto nella map la spesa
        childUpdates.put("/users/" + u.getID() + "/expenses/" + expense.getID(), expensesValues);
        childUpdates.put("/groups/" + expense.getGroupID() + "/expenses/" + expense.getID(), expensesValues);

        //aggiungo la spesa nella lista spese dello user e del group su Firebase
        databaseReference.updateChildren(childUpdates);


        u.updateBalance(expense);
        updateBalanceFirebase(u, expense);
    }

    // update balance among other users and among the group this user is part of
    private void updateBalanceFirebase (final User u, final Expense expense) {
        // todo per ora fa il calcolo come se le spese fossero sempre equamente divise fra tutti i
        // todo     membri del gruppo (cioè come se expense.equallyDivided fosse sempre = true


        final String groupID = expense.getGroupID();


        Query query = databaseReference.child("groups").child(groupID);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    databaseReference.child("users").child(u.getID()).child("groups").child(groupID).child("balanceWithGroup").setValue(actualdebts+totalcredit);
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
                            databaseReference.child("users").child(u.getID()).child("balancesWithUsers").child(member.getKey()).setValue(balance+singlecredit);
                        }
                    }

                    //aggiorno debito dell'amico verso di me

                    //debito dell'amico verso di me
                    Double balance = dataSnapshot.child("users").child(member.getKey()).child("balancesWithUsers").child(u.getID()).getValue(Double.class);

                    if (balance != null)
                    {
                        databaseReference.child("users").child(member.getKey()).child("balancesWithUsers").child(u.getID()).setValue(balance-singlecredit);

                    }
                    else
                    {
                        System.out.println("Io non risulto tra i suoi debiti");
                        // => allora devo aggiungermi
                        databaseReference.child("users").child(member.getKey()).child("balancesWithUsers").child(u.getID()).setValue(-singlecredit);
                    }

                    //aggiorno il debito dell'amico verso il gruppo
                    balance = dataSnapshot.child("users").child(member.getKey()).child("groups").child(groupID).child("balanceWithGroup").getValue(Double.class);

                    if (balance != null)
                    {
                        databaseReference.child("users").child(member.getKey()).child("groups").child(groupID).child("balanceWithGroup").setValue(balance-singlecredit);
                    }
                    else
                    {
                        System.out.println("Gruppo non risulta tra i suoi debiti");
                        // => allora lo devo aggiungere
                        databaseReference.child("users").child(member.getKey()).child("groups").child(groupID).child("balanceWithGroup").setValue(-singlecredit);
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
}
