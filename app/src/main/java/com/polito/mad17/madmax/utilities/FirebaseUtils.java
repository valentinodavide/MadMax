package com.polito.mad17.madmax.utilities;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.polito.mad17.madmax.R;
import com.polito.mad17.madmax.activities.MainActivity;
import com.polito.mad17.madmax.activities.expenses.ExpenseCommentsViewAdapter;
import com.polito.mad17.madmax.activities.expenses.ExpensesViewAdapter;
import com.polito.mad17.madmax.activities.expenses.ParticipantsViewAdapter;
import com.polito.mad17.madmax.activities.expenses.PendingExpenseViewAdapter;
import com.polito.mad17.madmax.activities.expenses.VotersViewAdapter;
import com.polito.mad17.madmax.activities.groups.EventsViewAdapter;
import com.polito.mad17.madmax.activities.groups.GroupDetailActivity;
import com.polito.mad17.madmax.activities.groups.GroupsFragment;
import com.polito.mad17.madmax.activities.groups.GroupsViewAdapter;
import com.polito.mad17.madmax.activities.users.HashMapFriendsAdapter;
import com.polito.mad17.madmax.entities.Comment;
import com.polito.mad17.madmax.entities.Event;
import com.polito.mad17.madmax.entities.Expense;
import com.polito.mad17.madmax.entities.Group;
import com.polito.mad17.madmax.entities.User;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static android.R.attr.bitmap;
import static android.R.attr.data;
import static com.polito.mad17.madmax.activities.users.NewMemberActivity.alreadySelected;

public class FirebaseUtils {

    private static final String TAG = FirebaseUtils.class.getSimpleName();

    private final static FirebaseUtils INSTANCE = new FirebaseUtils();

    public static FirebaseDatabase firebaseDatabase;
    private static DatabaseReference databaseReference;
    private static FirebaseAuth auth;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    private Boolean firstStart = true;

    // created only to defeat instantiation
    private FirebaseUtils() {
    }
    public void setUp()
    {
        firebaseDatabase = FirebaseDatabase.getInstance();

        if(firstStart) {
            firebaseDatabase.setPersistenceEnabled(true);
            firstStart = false;
        }

        Log.d("FirebaseUtils", "prima di getReference()");
        databaseReference = firebaseDatabase.getReference();
        auth = FirebaseAuth.getInstance();

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
    }

    public static FirebaseUtils getInstance()
    {
        return INSTANCE;
    }

    public static FirebaseDatabase getFirebaseDatabase()
    {
        return firebaseDatabase;
    }

    public static DatabaseReference getDatabaseReference()
    {
        return databaseReference;
    }

    public static FirebaseAuth getAuth()
    {
        return auth;
    }

    public FirebaseStorage getFirebaseStorage()
    {
        return firebaseStorage;
    }

    public StorageReference getStorageReference()
    {
        return storageReference;
    }

    public void getGroup(final String id, final TreeMap<String, Group> groups, final GroupsViewAdapter groupsViewAdapter)
    {
        databaseReference.child("groups").child(id).addValueEventListener(new ValueEventListener()
        {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                final Boolean deleted = dataSnapshot.child("deleted").getValue(Boolean.class);

                Group g = new Group();
                g.setName(dataSnapshot.child("name").getValue(String.class));
                g.setImage(dataSnapshot.child("image").getValue(String.class));

                if (deleted!= null && !deleted &&
                        dataSnapshot.child("members").hasChild(MainActivity.getCurrentUID()) &&
                        !dataSnapshot.child("members").child(MainActivity.getCurrentUID()).child("deleted").getValue(Boolean.class))
                {
                    groups.put(id, g);
                }
                else
                {
                    groups.remove(id);
                }

                groupsViewAdapter.update(groups);
                groupsViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.w(TAG, databaseError.getMessage());
            }
        });
    }

    public Integer leaveGroupFirebase (String userID, final String groupID)
    {

        Group g = GroupsFragment.groups.get(groupID);
        if (g != null)
        {
            HashMap<String, Double> balances = g.getCurrencyBalances();

            Boolean debt = false;
            Log.d (TAG, g.getName() + " contains those balances: ");
            for (Map.Entry<String, Double> entry : balances.entrySet())
            {
                Log.d (TAG, entry.getKey() + " " + entry.getValue());
                if (entry.getValue() < 0)
                    debt = true;
            }

            if (debt)
            {
                Log.d (TAG, "Cannot leave group");
                return 0;
            }
            else
            {
                Log.d (TAG, "Nessuno debito, abbandono in corso");
                //Elimino gruppo da lista gruppi dello user
                databaseReference.child("users").child(userID).child("groups").child(groupID).setValue(false);
                //Elimino user dalla lista dei members del gruppo
                databaseReference.child("groups").child(groupID).child("members").child(userID).child("deleted").setValue(true);

                //Elimino gruppo da cache
                GroupsFragment.groups.remove(groupID);

                //Elimino gruppo dagli shared groups tra me e ogni membro del gruppo
                deleteSharedGroup(MainActivity.getCurrentUID(), groupID);

                return 2;
            }
        }
        else
        {
            Log.d (TAG, "Bilancio del gruppo: " + groupID + " non disponibile adesso. Riprovare.");

            return null;
        }
    }

    public void deleteSharedGroup(final String userID, final String groupID)
    {
        databaseReference.child("groups").child(groupID).child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot memberSnap : dataSnapshot.getChildren())
                {
                    //If members is still part of the group and it's not me
                    if (memberSnap.hasChild("deleted") && !memberSnap.child("deleted").getValue(Boolean.class) && !memberSnap.getKey().equals(userID))
                    {
                        String friendID = memberSnap.getKey();
                        //Group is no more in shared groups between user and friend
                        databaseReference.child("users").child(userID).child("friends")
                                .child(friendID).child("sharedGroups").child(groupID).setValue(false);
                        //Group is no more in shared groups between friend and user
                        databaseReference.child("users").child(friendID).child("friends")
                                .child(userID).child("sharedGroups").child(groupID).setValue(false);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addSharedGroup(final String userID, final String groupID)
    {
        databaseReference.child("groups").child(groupID).child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot memberSnap : dataSnapshot.getChildren())
                {
                    //If members is still part of the group and it's not me
                    if (memberSnap.hasChild("deleted") && !memberSnap.child("deleted").getValue(Boolean.class) && !memberSnap.getKey().equals(userID))
                    {
                        String friendID = memberSnap.getKey();
                        //Group is now in shared groups between user and friend
                        databaseReference.child("users").child(userID).child("friends")
                                .child(friendID).child("sharedGroups").child(groupID).setValue(true);
                        //Group is now in shared groups between friend and user
                        databaseReference.child("users").child(friendID).child("friends")
                                .child(userID).child("sharedGroups").child(groupID).setValue(true);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void removeGroupFirebase (final String userID, final String groupID, final Context context)
    {
        databaseReference.child("groups").child(groupID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Se io sono admin posso eliminare il gruppo, altrimenti no
                if (dataSnapshot.child("members").child(userID).child("admin").getValue(Boolean.class))
                {
                    Log.d (TAG, "Sono admin, posso eliminare il gruppo");

                    //todo controllare se almeno un membro ha debito/credito verso il gruppo. Se sì visualizzare messaggio per chiedere conferma dell'eliminazione gruppo

                    //For each member of the group
                    for (DataSnapshot memberSnapshot: dataSnapshot.child("members").getChildren())
                    {
                        String memberID = memberSnapshot.getKey();
                        //In user's groups, set this group to deleted
                        databaseReference.child("users").child(memberID).child("groups").child(groupID).setValue(false);
                        //Update shared groups between this member and any other member
                        deleteSharedGroup(memberID, groupID);
                    }


                    //Segno il group come eliminato nei groups
                    databaseReference.child("groups").child(groupID).child("deleted").setValue(true);

                    Toast.makeText(context,context.getString(R.string.group_removed),Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Log.d (TAG, "Non sono admin, non posso eliminare il gruppo");
                    Toast.makeText(context,context.getString(R.string.error_not_admin),Toast.LENGTH_SHORT).show();

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void joinGroupFirebase (final String userID, String groupID)
    {
        //Aggiungo gruppo alla lista gruppi dello user
    //    databaseReference.child("users").child(userID).child("groups").push(); tolto perchè non ci serve la chiave
        databaseReference.child("users").child(userID).child("groups").child(groupID).setValue(true);
        //Aggiungo user (con sottocampi admin e timestamp) alla lista membri del gruppo
    //    databaseReference.child("groups").child(groupID).child("members").push(); tolto perchè non ci serve la chiave
    //    databaseReference.child("groups").child(groupID).child("members").child(userID).push(); tolto perchè non ci serve la chiave
        if(userID.equals(MainActivity.getCurrentUID())) {
            databaseReference.child("groups").child(groupID).child("members").child(userID).child("admin").setValue(true);
        }
        else {
            databaseReference.child("groups").child(groupID).child("members").child(userID).child("admin").setValue(false);
        }
  //      databaseReference.child("groups").child(groupID).child("members").child(userID).push();   tolto perchè non ci serve la chiave
        String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        databaseReference.child("groups").child(groupID).child("members").child(userID).child("timestamp").setValue(timestamp);
        databaseReference.child("groups").child(groupID).child("members").child(userID).child("deleted").setValue(false);

        addSharedGroup(userID, groupID);
    }

    //To remove member from group
    public void removeMemberFirebase (String memberID, String groupID)
    {
        databaseReference.child("groups").child(groupID).child("members").child(memberID).child("deleted").setValue(true);
        databaseReference.child("users").child(memberID).child("groups").child(groupID).setValue(false);
        deleteSharedGroup(memberID, groupID);
    }

    public String addExpenseFirebase(final Expense expense, ImageView expensePhoto, ImageView billPhoto, Context context) {
        Log.d(TAG, "addExpenseFirebase");

        //Aggiungo spesa a Firebase
        final String eID = databaseReference.child("expenses").push().getKey();
        databaseReference.child("expenses").child(eID).setValue(expense);

        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        databaseReference.child("expenses").child(eID).child("timestamp").setValue(timeStamp);
        //timestamp già settato prima
        //String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        //databaseReference.child("expenses").child(eID).child("timestamp").setValue(timeStamp);
        //databaseReference.child("expenses").child(eID).child("deleted").setValue(false);

        Bitmap bitmap;
        ByteArrayOutputStream baos;
        byte[] data;
        UploadTask uploadTask;

        StorageReference uExpensePhotoFilenameRef = storageReference.child("expenses").child(eID).child(eID + "_expensePhoto.jpg");
        // Get the data from an ImageView as bytes
        if (expensePhoto != null) {
            expensePhoto.setDrawingCacheEnabled(true);
            expensePhoto.buildDrawingCache();
            bitmap = expensePhoto.getDrawingCache();
        }
        else{
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.expense_default);
        }
            baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            data = baos.toByteArray();

            uploadTask = uExpensePhotoFilenameRef.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // todo Handle unsuccessful uploads
                    Log.e(TAG, "image upload failed");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    databaseReference.child("expenses").child(eID).child("expensePhoto").setValue(taskSnapshot.getMetadata().getDownloadUrl().toString());
                }
            });

        if (billPhoto != null)
        {
            StorageReference uBillPhotoFilenameRef = storageReference.child("expenses").child(eID).child(eID+"_billPhoto.jpg");

            // Get the data from an ImageView as bytes
            billPhoto.setDrawingCacheEnabled(true);
            billPhoto.buildDrawingCache();
            bitmap = billPhoto.getDrawingCache();
            baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            data = baos.toByteArray();

            uploadTask = uBillPhotoFilenameRef.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // todo Handle unsuccessful uploads
                    Log.e(TAG, "image upload failed");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    databaseReference.child("expenses").child(eID).child("billPhoto").setValue(taskSnapshot.getMetadata().getDownloadUrl().toString());
                }
            });
        }
        else if (expense.getBillPhoto() != null)
        {
            databaseReference.child("expenses").child(eID).child("billPhoto").setValue(expense.getBillPhoto());

        }

        Log.d(TAG, "creator expense " + expense.getCreatorID());

        //Per ogni participant setto la quota che ha già pagato per questa spesa
        //e aggiungo spesa alla lista spese di ogni participant
        for (Map.Entry<String, Double> participant : expense.getParticipants().entrySet())
        {
            Log.d(TAG, "partecipant " + participant.getKey());
            //Se il participant corrente è il creatore della spesa
            if (participant.getKey().equals(expense.getCreatorID()))
            {
                //paga tutto lui
                databaseReference.child("expenses").child(eID).child("participants").child(participant.getKey()).child("alreadyPaid").setValue(expense.getAmount());
            }
            else
            {
                //gli altri participant inizialmente non pagano niente
                databaseReference.child("expenses").child(eID).child("participants").child(participant.getKey()).child("alreadyPaid").setValue(0);
            }

            //risetto fraction di spesa che deve pagare l'utente, visto che prima si sputtana
            databaseReference.child("expenses").child(eID).child("participants").child(participant.getKey()).child("fraction").setValue(expense.getParticipants().get(participant.getKey()));


            //Aggiungo spesaID a elenco spese dello user
            //todo controllare se utile
            databaseReference.child("users").child(participant.getKey()).child("expenses").child(eID).setValue(true);
        }

        //Aggiungo spesa alla lista spese del gruppo
        databaseReference.child("groups").child(expense.getGroupID()).child("expenses").push();
        databaseReference.child("groups").child(expense.getGroupID()).child("expenses").child(eID).setValue(true);

        return eID;
    }

    public void getExpense(final String id, final TreeMap<String, Expense> expensesMap, final ExpensesViewAdapter expensesViewAdapter) {
        databaseReference.child("expenses").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.d (TAG, "Spesa: " + dataSnapshot.getKey());
                for (DataSnapshot d : dataSnapshot.getChildren())
                {
                    Log.d (TAG, "Campo " + d.getKey() + ": " + d.getValue());
                }
                Log.d(TAG, " ");

                //Se io sono tra i participant e la spesa non è stata eliminata
                if (dataSnapshot.child("participants").hasChild(MainActivity.getCurrentUID()) &&
                        dataSnapshot.hasChild("deleted") &&
                        !dataSnapshot.child("deleted").getValue(Boolean.class))
                {
                    //Retrieve my balance for this expense
                    User provaCurrent = MainActivity.getCurrentUser();
                    Double dueImport;
                    Double alreadyPaid;
                    if (dataSnapshot.child("participants").child(MainActivity.getCurrentUID()).hasChild("fraction") &&
                            dataSnapshot.hasChild("amount"))
                    {
                        dueImport = Double.parseDouble(String.valueOf(dataSnapshot.child("participants").child(MainActivity.getCurrentUID()).child("fraction").getValue())) * dataSnapshot.child("amount").getValue(Double.class);
                    }
                    else
                    {
                        dueImport = null;
                    }
                    if (dataSnapshot.child("participants").child(MainActivity.getCurrentUID()).hasChild("alreadyPaid"))
                    {
                        alreadyPaid = dataSnapshot.child("participants").child(MainActivity.getCurrentUID()).child("alreadyPaid").getValue(Double.class);
                    }
                    else
                    {
                        alreadyPaid = null;
                    }

                    Double expenseBalance;
                    if (dueImport != null && alreadyPaid != null)
                    {
                        expenseBalance = alreadyPaid - dueImport;
                        expenseBalance = Math.floor(expenseBalance * 100) / 100;

                        Expense expense = new Expense();
                        expense.setDescription(dataSnapshot.child("description").getValue(String.class));
                        expense.setAmount(dataSnapshot.child("amount").getValue(Double.class));
                        expense.setCurrency(dataSnapshot.child("currency").getValue(String.class));
                        expense.setBalance(expenseBalance);

                        expensesMap.put(id, expense);
                        expensesViewAdapter.update(expensesMap);
                        expensesViewAdapter.notifyDataSetChanged();
                    }
                    else
                    {
                        Log.d (TAG, "Qui sarebbe crashato");
                    }

                }
                //Se user non è più participant della spesa
                else
                {
                    expensesMap.remove(id);
                    expensesViewAdapter.update(expensesMap);
                    expensesViewAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.e(TAG, databaseError.getMessage());
            }
        });
    }

    public void removeExpenseFirebase (final String expenseID, final Context context)
    {
        databaseReference.child("expenses").child(expenseID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String groupID = dataSnapshot.child("groupID").getValue(String.class);

                // aggiunto da riky per mandare la notifica a tutti tranne a chi ha eliminato la spesa
                databaseReference.child("expenses").child(expenseID).child("deletedBy").setValue(MainActivity.getCurrentUID());

                //Elimino spesa dal gruppo
                databaseReference.child("groups").child(groupID).child("expenses").child(expenseID).setValue(false);

                //Per ogni participant elimino la spesa dal suo elenco spese
                for (DataSnapshot participantSnapshot : dataSnapshot.child("participants").getChildren())
                {
                    String participantID = participantSnapshot.getKey();
                    databaseReference.child("users").child(participantID).child("expenses").child(expenseID).setValue(false);
                }
                //Elimino commenti sulla spesa
                databaseReference.child("comments").child(groupID).removeValue();
                //Elimino spesa
                databaseReference.child("expenses").child(expenseID).child("deleted").setValue(true);
                Toast.makeText(context,context.getString(R.string.expense_removed),Toast.LENGTH_SHORT).show();

                // add event for EXPENSE_REMOVE
                databaseReference.child("expenses").child(expenseID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                User currentUser = MainActivity.getCurrentUser();
                                Event event = new Event(
                                        dataSnapshot.child("groupID").getValue(String.class),
                                        Event.EventType.EXPENSE_REMOVE,
                                        currentUser.getName() + " " + currentUser.getSurname(),
                                        dataSnapshot.child("description").getValue(String.class)
                                );
                                event.setDate(new SimpleDateFormat("yyyy.MM.dd").format(new java.util.Date()));
                                event.setTime(new SimpleDateFormat("HH:mm").format(new java.util.Date()));
                                FirebaseUtils.getInstance().addEvent(event);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.w(TAG, databaseError.toException());
                            }
                        });

                /*Intent intent = new Intent(context, GroupDetailActivity.class);
                intent.putExtra("groupID", groupID);
                intent.putExtra("userID", MainActivity.getCurrentUID());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);*/
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void addPendingExpenseFirebase (Expense expense, ImageView expensePhoto, Context context)
    {
        Log.d(TAG, "addPendingExpenseFirebase");

        //Aggiungo pending expense a Firebase
        final String eID = databaseReference.child("proposedExpenses").push().getKey();
        databaseReference.child("proposedExpenses").child(eID).setValue(expense);

        StorageReference uExpensePhotoFilenameRef = storageReference.child("proposedExpenses").child(eID).child(eID+"_expensePhoto.jpg");

        if (expensePhoto != null) {
            Bitmap bitmap;
            expensePhoto.setDrawingCacheEnabled(true);
            expensePhoto.buildDrawingCache();
            bitmap = expensePhoto.getDrawingCache();


        // Get the data from an ImageView as bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = uExpensePhotoFilenameRef.putBytes(data);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // todo Handle unsuccessful uploads
                Log.e(TAG, "image upload failed");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                databaseReference.child("proposedExpenses").child(eID).child("expensePhoto").setValue(taskSnapshot.getMetadata().getDownloadUrl().toString());
            }
        });
        }


        Log.d(TAG, "creator expense " + expense.getCreatorID());

        //Per ogni participant setto la quota che ha già pagato per questa spesa
        //e aggiungo spesa alla lista spese di ogni participant
        for (Map.Entry<String, Double> participant : expense.getParticipants().entrySet())
        {
            Log.d(TAG, "participant " + participant.getKey());

            //Setto voto nel participant a null
            databaseReference.child("proposedExpenses").child(eID).child("participants").child(participant.getKey()).child("vote").setValue("null");
            //Aggiungo campo deleted al participant
            databaseReference.child("proposedExpenses").child(eID).child("participants").child(participant.getKey()).child("deleted").setValue(false);
            //Aggiungo spesaID a elenco spese pending dello user
            databaseReference.child("users").child(participant.getKey()).child("proposedExpenses").child(eID).setValue(true);
        }

        //Aggiungo spesa pending alla lista spese pending del gruppo
        databaseReference.child("groups").child(expense.getGroupID()).child("proposedExpenses").push();
        databaseReference.child("groups").child(expense.getGroupID()).child("proposedExpenses").child(eID).setValue(true);
    }

    public void getPendingExpense (final String pendingID, final TreeMap<String, Expense> pendingExpensesMap, final PendingExpenseViewAdapter pendingExpenseViewAdapter)
    {
        databaseReference.child("proposedExpenses").child(pendingID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Integer participantsCount = 0;
                Integer yes = 0;
                Integer no = 0;
                String myvote = "null";

                //Questo listener è chiamato ogni volta che questa spesa pending è modificata, quindi devo controllare
                //che io faccia ancora parte di questa spesa e che la spesa pending esista ancora (NELLE PROPOSED EXPESES)
                if (dataSnapshot.child("participants").hasChild(MainActivity.getCurrentUID()) &&
                        dataSnapshot.child("participants").child(MainActivity.getCurrentUID()).hasChild("deleted") &&
                        !dataSnapshot.child("participants").child(MainActivity.getCurrentUID()).child("deleted").getValue(Boolean.class) &&
                        dataSnapshot.hasChild("deleted") &&
                        !dataSnapshot.child("deleted").getValue(Boolean.class))
                {
                    for (DataSnapshot participantSnap : dataSnapshot.child("participants").getChildren())
                    {
                        //Se il partecipante alla spesa pending esiste ancora (nella spesa pending)
                        if (participantSnap.child("deleted").getValue(Boolean.class) == false)
                        {
                            participantsCount++;
                            if (participantSnap.hasChild("vote"))
                            {
                                if (participantSnap.child("vote").getValue(String.class).equals("yes"))
                                {
                                    yes++;
                                    if (participantSnap.getKey().equals(MainActivity.getCurrentUID()))
                                        myvote = "yes";

                                }
                                if (participantSnap.child("vote").getValue(String.class).equals("no"))
                                {
                                    no++;
                                    if (participantSnap.getKey().equals(MainActivity.getCurrentUID()))
                                        myvote = "no";
                                }
                            }
                        }
                    }

                    Expense pendingExpense = new Expense();
                    pendingExpense.setID(dataSnapshot.getKey());
                    pendingExpense.setDescription(dataSnapshot.child("description").getValue(String.class));
                    pendingExpense.setAmount(dataSnapshot.child("amount").getValue(Double.class));
                    pendingExpense.setGroupName(dataSnapshot.child("groupName").getValue(String.class));
                    pendingExpense.setGroupImage(dataSnapshot.child("groupImage").getValue(String.class));

                    Log.d(TAG, pendingExpense.getDescription() + " pendingExpense.getGroupImage()" + pendingExpense.getGroupImage());

                    pendingExpense.setExpensePhoto(dataSnapshot.child("expensePhoto").getValue(String.class));
                    pendingExpense.setParticipantsCount(participantsCount);
                    pendingExpense.setYes(yes);
                    pendingExpense.setNo(no);
                    pendingExpense.setMyVote(myvote);
                    pendingExpense.setCreatorID(dataSnapshot.child("creatorID").getValue(String.class));
                    pendingExpense.setCurrency(dataSnapshot.child("currency").getValue(String .class));
                    pendingExpensesMap.put(pendingID, pendingExpense);
                    pendingExpenseViewAdapter.update(pendingExpensesMap);
                    pendingExpenseViewAdapter.notifyDataSetChanged();
                }
                //Se io non faccio più parte di questa spesa pending
                else
                {
                    pendingExpensesMap.remove(pendingID);
                    pendingExpenseViewAdapter.update(pendingExpensesMap);
                    pendingExpenseViewAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    public void removePendingExpenseFirebase (final String expenseID, final Context context)
    {
        databaseReference.child("proposedExpenses").child(expenseID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String groupID = dataSnapshot.child("groupID").getValue(String.class);

                //Elimino spesa pending dal gruppo
                databaseReference.child("groups").child(groupID).child("proposedExpenses").child(expenseID).setValue(false);

                //Per ogni participant elimino la spesa pending dal suo elenco spese pending
                for (DataSnapshot participantSnapshot : dataSnapshot.child("participants").getChildren())
                {
                    String participantID = participantSnapshot.getKey();
                    databaseReference.child("users").child(participantID).child("proposedExpenses").child(expenseID).setValue(false);
                }
                //todo Elimino commenti sulla spesa pending
                //databaseReference.child("comments").child(groupID).removeValue();

                //Elimino spesa pending
                databaseReference.child("proposedExpenses").child(expenseID).child("deleted").setValue(true);
                Toast.makeText(context, context.getString(R.string.expense_removed), Toast.LENGTH_SHORT).show();

                // add event for PENDING_EXPENSE_REMOVE
                databaseReference.child("proposedExpenses").child(expenseID)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User currentUser = MainActivity.getCurrentUser();
                            Event event = new Event(
                                    dataSnapshot.child("groupID").getValue(String.class),
                                    Event.EventType.PENDING_EXPENSE_REMOVE,
                                    currentUser.getName() + " " + currentUser.getSurname(),
                                    dataSnapshot.child("description").getValue(String.class)
                            );
                            event.setDate(new SimpleDateFormat("yyyy.MM.dd").format(new java.util.Date()));
                            event.setTime(new SimpleDateFormat("HH:mm").format(new java.util.Date()));
                            FirebaseUtils.getInstance().addEvent(event);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, databaseError.toException());
                        }
                    });

                /*Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("currentFragment", 2);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);*/
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /*
        EVENT
     */
    public String addEvent(Event event) {
        Log.d(TAG, "addEvent");

        final String ID = databaseReference.child("events").push().getKey();
        DatabaseReference eventReference = databaseReference.child("events").child(ID);
        DatabaseReference groupReference = databaseReference.child("groups").child(event.getGroupID());

        eventReference.setValue(event);
        groupReference.child("events").child(ID).setValue(true);

        return ID;
    }

    public void getEvent(final String ID, final Map<String, Event> eventMap, final EventsViewAdapter eventsViewAdapter) {
        databaseReference.child("events").child(ID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d (TAG, "Evento: " + dataSnapshot.getKey());

                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    Log.d (TAG, "Campo " + d.getKey() + ": " + d.getValue());
                }
                Log.d(TAG, " ");

                Event event = new Event(
                        dataSnapshot.getKey(),
                        dataSnapshot.child("groupID").getValue(String .class),
                        dataSnapshot.child("eventType").getValue(Event.EventType.class),
                        dataSnapshot.child("subject").getValue(String.class),
                        dataSnapshot.child("object").getValue(String.class),
                        dataSnapshot.child("date").getValue(String.class),
                        dataSnapshot.child("time").getValue(String.class),
                        dataSnapshot.child("amount").getValue(Double.class),
                        dataSnapshot.child("description").getValue(String.class)
                );

                eventMap.put(ID, event);
                eventsViewAdapter.update(eventMap);
                eventsViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
            }
        });
    }
    /*
        END EVENT
     */

    /*
        COMMENT
     */
    public String addComment (Comment comment, Boolean expense) {
        Log.d(TAG, "addComment");

        final String ID = databaseReference.child("comments").push().getKey();
        DatabaseReference commentReference = databaseReference.child("comments").child(ID);
        commentReference.setValue(comment);

        if(expense) {
            DatabaseReference expenseReference = databaseReference.child("expenses").child(comment.getExpenseID());
            expenseReference.child("comments").child(ID).setValue(true);
        }
        else
        {
            DatabaseReference proposedExpensesReference = databaseReference.child("proposedExpenses").child(comment.getExpenseID());
            proposedExpensesReference.child("comments").child(ID).setValue(true);
        }

        return ID;
    }

    public void getComment(final String ID, final Map<String, Comment> commentsMap, final ExpenseCommentsViewAdapter expenseCommentsViewAdapter) {
        databaseReference.child("comments").child(ID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d (TAG, "Commento: " + dataSnapshot.getKey());

                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    Log.d (TAG, "Campo " + d.getKey() + ": " + d.getValue());
                }
                Log.d(TAG, " ");

                Comment comment = new Comment(
                        dataSnapshot.getKey(),
                        dataSnapshot.child("expenseID").getValue(String.class),
                        dataSnapshot.child("author").getValue(String.class),
                        dataSnapshot.child("authorPhoto").getValue(String.class),
                        dataSnapshot.child("message").getValue(String.class),
                        dataSnapshot.child("date").getValue(String.class),
                        dataSnapshot.child("time").getValue(String.class)
                );

                commentsMap.put(ID, comment);
                expenseCommentsViewAdapter.update(commentsMap);
                expenseCommentsViewAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
            }
        });
    }
    /*
        END COMMENT
     */
// sostituisce la vecchia public void getFriend(final String id, final String vote, final TreeMap<String, User> voters, final VotersViewAdapter votersViewAdapter, final TextView creatorNameTextView)
    public void getVoter(final String id, final String vote, final TreeMap<String, User> voters, final VotersViewAdapter votersViewAdapter)
    {
        databaseReference.child("users").child(id).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                User u = new User();
                u.setName(dataSnapshot.child("name").getValue(String.class));
                u.setSurname(dataSnapshot.child("surname").getValue(String.class));
                u.setVote(vote);
                u.setProfileImage(dataSnapshot.child("image").getValue(String.class));
                voters.put(id, u);
                votersViewAdapter.update(voters);
                votersViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, databaseError.getMessage());
            }
        });
    }

    public void getFriendInviteToGroup(final String userID, final String groupID, final  TreeMap<String, User> friends, final HashMapFriendsAdapter adapter ) {
        databaseReference.child("users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User u = new User();
                u.setName(dataSnapshot.child("name").getValue(String.class));
                u.setSurname(dataSnapshot.child("surname").getValue(String.class));
                u.setID(dataSnapshot.getKey());
                u.setIsInGroupForInvite(groupID);
                u.setProfileImage(dataSnapshot.child("image").getValue(String.class));

                //se l'amico letto da db non è già stato scelto, lo metto nella lista di quelli
                //che saranno stampati
                if (!alreadySelected.containsKey(u.getID())) {
                    friends.put(userID, u);
                    adapter.update(friends);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, databaseError.getMessage());
            }
        });
    }

    public void getParticipantName(final String id, final HashMap<String, User> participants, final ParticipantsViewAdapter participantsViewAdapter, final User u)
    {
        databaseReference.child("users").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                u.setName(dataSnapshot.child("name").getValue(String.class));
                u.setSurname(dataSnapshot.child("surname").getValue(String.class));
                u.setProfileImage(dataSnapshot.child("image").getValue(String.class));
                participants.put(id, u);
                participantsViewAdapter.update(participants);
                participantsViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void addFriend(String friendID){
        databaseReference.child("users").child(MainActivity.getCurrentUID()).child("friends").child(friendID).child("deleted").setValue(false);
        databaseReference.child("users").child(friendID).child("friends").child(MainActivity.getCurrentUID()).child("deleted").setValue(false);
    }

    public void removeFromFriends(final String userID, final String friendID)
    {
        databaseReference.child("users").child(userID).child("friends").child(friendID).child("deleted").setValue(true);
        databaseReference.child("users").child(friendID).child("friends").child(userID).child("deleted").setValue(true);

    }
}
