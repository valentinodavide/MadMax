package com.polito.mad17.madmax.entities;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.polito.mad17.madmax.activities.MainActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

public class User implements Parcelable {
    private String ID;
    private String username;
    private String name;
    private String surname;
    private String email;
    private String password;
    private String profileImage; // optional, URL dell'immagine da Firebase
    private String defaultCurrency;

    private String vote;

    //Servono per stampare quota già pagata e quota dovuta da un utente per una certa spesa
    private Double alreadyPaid;
    private Double dueImport;

    //Bilancio dello user verso un certo gruppo
    private Double balanceWithGroup;

    private TreeMap<String, Group> userGroups;  //String = groupID, Group = oggetto Group di cui user fa parte
    private TreeMap<String, User> userFriends;  //String = UID, User = friend of this user

    private TreeMap<String, Double> balanceWithUsers;   //String = userID,  Double = debito(-) o credito (+) verso gli altri utenti
    private TreeMap<String, Double> balanceWithGroups;  //String = groupID, Double = debito(-) o credito (+) verso gli altri gruppi
    private TreeMap<String, Expense> addedExpenses;   //String = timestamp dell'aggiunta, Expense = oggetto spesa

    private TreeMap<String, HashMap<String, Group>> sharedGroupPerFriend;   //String = UID of the friend, HashMap<String, Group> : String = GID of the shared group, Group = shared group

    // constructor typically used for creting the current user logged in
    public User(String ID, String username, String name, String surname, String email, String password, String profileImage, String defaultCurrency) {
        this.ID = ID;
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = encryptPassword(password);
        this.profileImage = profileImage;
        this.defaultCurrency = defaultCurrency;
        this.userGroups = new TreeMap<>(Collections.reverseOrder());
        this.userFriends = new TreeMap<>(Collections.reverseOrder());
        this.balanceWithUsers = new TreeMap<>();
        this.balanceWithGroups = new TreeMap<>();
        this.addedExpenses = new TreeMap<>(Collections.reverseOrder());
        this.sharedGroupPerFriend = new TreeMap<>(Collections.reverseOrder());
    }

    // constructor typically used for populate userFriends of current User
    public User(String ID, String username, String name, String surname, String email, String profileImage) {
        this.ID = ID;
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.profileImage = profileImage;
    }

    /*
        PARCELABLE
     */
    public User() {
        // altrimenti da null pointer exception se l'utente viene creato senza argomenti
        this.userGroups = new TreeMap<>(Collections.reverseOrder());
        this.userFriends = new TreeMap<>(Collections.reverseOrder());

        this.balanceWithUsers = new TreeMap<>();
        this.balanceWithGroups = new TreeMap<>();
        this.addedExpenses = new TreeMap<>(Collections.reverseOrder());

        this.sharedGroupPerFriend = new TreeMap<>(Collections.reverseOrder());
    }

    protected User(Parcel in) {
        ID = in.readString();
        name = in.readString();
        surname = in.readString();
        profileImage = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ID);
        dest.writeString(name);
        dest.writeString(surname);
        dest.writeString(profileImage);

    }
    /*
        END PARCELABLE
     */


    /*
        GETTERS & SETTERS
     */
    public String getID() { return ID; }

    public void setID(String ID) { this.ID = ID; }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = encryptPassword(password);
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getDefaultCurrency() { return defaultCurrency;}

    public void setDefaultCurrency(String defaultCurrency) { this.defaultCurrency = defaultCurrency;}

    public TreeMap<String, Group> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(TreeMap<String, Group> userGroups) {
        this.userGroups = userGroups;
    }

    public TreeMap<String, User> getUserFriends() {
        return userFriends;
    }

    public void setUserFriends(TreeMap<String, User> friends) {
        this.userFriends = friends;
    }

    public TreeMap<String, Double> getBalanceWithUsers() {
        return balanceWithUsers;
    }

    public void setBalanceWithUsers(TreeMap<String, Double> balanceWithUsers) {
        this.balanceWithUsers = balanceWithUsers;
    }

    public TreeMap<String, Double> getBalanceWithGroups() {
        return balanceWithGroups;
    }

    public void setBalanceWithGroups(TreeMap<String, Double> balanceWithGroups) {
        this.balanceWithGroups = balanceWithGroups;
    }

    public SortedMap<String, Expense> getAddedExpenses() {
        return addedExpenses;
    }

    public void setAddedExpenses(TreeMap<String, Expense> addedExpenses) {
        this.addedExpenses = addedExpenses;
    }

    public TreeMap<String, HashMap<String, Group>> getSharedGroupPerFriend() {
        return sharedGroupPerFriend;
    }

    public void setSharedGroupPerFriend(TreeMap<String, HashMap<String, Group>> sharedGroupPerFriend) {
        this.sharedGroupPerFriend = sharedGroupPerFriend;
    }

    public String getVote() {return vote;}

    public void setVote(String vote) {this.vote = vote;}

    public Double getAlreadyPaid() {return alreadyPaid;}

    public void setAlreadyPaid(Double alreadyPaid) {this.alreadyPaid = alreadyPaid;}

    public Double getDueImport() {return dueImport;}

    public void setDueImport(Double dueImport) {this.dueImport = dueImport;}

    public Double getBalanceWithGroup() {return balanceWithGroup;}

    public void setBalanceWithGroup(Double balanceWithGroup) {this.balanceWithGroup = balanceWithGroup;}
    /*
        END GETTERS & SETTERS
     */


    // method to encrypt password
    public static String encryptPassword (String passwordToHash) {
        String generatedPassword = null;

        try {
            // create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            // add password bytes to digest
            md.update(passwordToHash.getBytes());
            // get the hash's bytes
            byte[] bytes = md.digest();
            // this bytes[] has bytes in decimal format;
            // convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for (int i=0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            // get complete hashed password in hex format
            generatedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }


    // Scommentato per far funzionare tutto
    // todo, implementa con firebase
    public Double myTotalDebts() {
        Double total = 0d;

        for (HashMap.Entry<String, Double> debt : balanceWithUsers.entrySet()) {
            Log.d("Ciao ", debt.getValue().toString());
            total += debt.getValue();
        }

        return total;

    }
    

    public TreeMap<String, Group> getSharedGroupsMap(User friend)
    {
        TreeMap<String, Group> mygroups = this.getUserGroups();

        mygroups.keySet().retainAll(friend.getUserGroups().keySet());

        return  mygroups;

    }

    public ArrayList<Group> getSharedGroupsList (User friend) {
        ArrayList<Group> mygroups = new ArrayList<>();
        TreeMap<String, Group> mygroupsmap = new TreeMap<>(this.getUserGroups());
        TreeMap<String, Group> friendgroupsmap = new TreeMap<>(friend.getUserGroups());
        //mygroupsmap = this.getUserGroups();
        //friendgroupsmap = friend.getUserGroups();

        mygroupsmap.keySet().retainAll(friendgroupsmap.keySet());

        for(Map.Entry<String, Group> entry : mygroupsmap.entrySet()) {
            //String key = entry.getKey();
            //HashMap value = entry.getValue();
            mygroups.add(entry.getValue());
        }

        return mygroups;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("ID", ID);
        result.put("name", name);
        result.put("surname", surname);
        result.put("email", email);
        result.put ("password", password);
        result.put("profileImage", profileImage);

        return result;
    }

    public void addFriend (final String friendID) {
        final DatabaseReference databaseReference = MainActivity.getDatabase().getReference();
        final DatabaseReference usersRef = databaseReference.child("users");
        final DatabaseReference groupsRef = databaseReference.child("groups");
        final String currentUID = this.getID();

        // getting friend data from db
        DatabaseReference friendRef = usersRef.child(friendID);
        User friend = new User(
                friendID,
                friendRef.child("username").toString(),
                friendRef.child("name").toString(),
                friendRef.child("surname").toString(),
                friendRef.child("email").toString(),
                friendRef.child("profileImage").toString()
        );

        //Add friendID to friend list of currentUID
        this.userFriends.put(friendID, friend); // add friend to current user local HashMap
        usersRef.child(currentUID).child("friends").push();
        usersRef.child(currentUID).child("friends").child(friendID).child("deleted").setValue(false);

        //Add currentUID to friend list of friendID
        usersRef.child(friendID).child("friends").push();
        usersRef.child(friendID).child("friends").child(currentUID).child("deleted").setValue(false);

        //Read groups currentUID belongs to
        Query query = databaseReference.child("users").child(currentUID).child("groups");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ArrayList<String> u1Groups = new ArrayList<String>();

                for (DataSnapshot groupSnapshot: dataSnapshot.getChildren()) {
                    Log.d(TAG,groupSnapshot.getKey()+" : "+groupSnapshot.getValue());
                    if(groupSnapshot.getValue(Boolean.class))
                        u1Groups.add(groupSnapshot.getKey());
                }

                Query query = databaseReference.child("users").child(friendID).child("groups");
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<String> sharedGroups = new ArrayList<String>();

                        for (DataSnapshot groupSnapshot: dataSnapshot.getChildren()) {
                            if (u1Groups.contains(groupSnapshot.getKey()) && groupSnapshot.getValue(Boolean.class))
                                sharedGroups.add(groupSnapshot.getKey());
                        }

                        final HashMap<String, Group> groups = new HashMap<>();

                        //ora in sharedGroups ci sono solo i gruppi di cui fanno parte entrambi gli utenti
                        for (String groupID : sharedGroups) {
                            // getting group data from db
                            DatabaseReference groupRef = groupsRef.child(groupID);
                            Group group = new Group(
                                    groupID,
                                    groupRef.child("name").toString(),
                                    groupRef.child("image").toString(),
                                    groupRef.child("description").toString(),
                                    Integer.parseInt(groupRef.child("numberMembers").toString())
                            );

                            groups.put(friendID, group);
                            usersRef.child(currentUID).child("friends").child(friendID).child(groupID).setValue(true);
                            usersRef.child(friendID).child("friends").child(currentUID).child(groupID).setValue(true);
                        }

                        // add shared groups to local sharedGroupPerFriend HashMap
                        sharedGroupPerFriend.put(friendID, groups);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("addFriendFirebase", databaseError.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("addFriendFirebase", databaseError.getMessage());
            }
        });
    }

    public void joinGroup (String groupID, String inviterUID) {
        final DatabaseReference databaseReference = MainActivity.getDatabase().getReference();
        final String currentUID = this.getID();

        //Aggiungo gruppo alla lista gruppi dello user
        databaseReference.child("users").child(currentUID).child("groups").push();
        databaseReference.child("users").child(currentUID).child("groups").child(groupID).setValue("true");

        //Aggiungo user (con sottocampi admin e timestamp) alla lista membri del gruppo
        databaseReference.child("groups").child(groupID).child("members").push();
        databaseReference.child("groups").child(groupID).child("members").child(currentUID).push();
        databaseReference.child("groups").child(groupID).child("members").child(currentUID).child("admin").setValue(false);
        databaseReference.child("groups").child(groupID).child("members").child(currentUID).push();
        databaseReference.child("groups").child(groupID).child("members").child(currentUID).child("timestamp").setValue("time");
        // aggiunto da riky
        databaseReference.child("groups").child(groupID).child("members").child(currentUID).push();
        databaseReference.child("groups").child(groupID).child("members").child(currentUID).child("deleted").setValue(false);

        // aggiungo l'invitante agli amici se non lo è già
        if(!userFriends.containsKey(inviterUID)){
            addFriend(inviterUID);//todo aggiungere l'invitato tra gli amici dell'invitante
        }

        //Incremento il numero di partecipanti
        databaseReference.child("groups").child(groupID).child("numberMembers").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer numberMembers = mutableData.getValue(Integer.class);
                if(numberMembers == null){
                    return Transaction.success(mutableData);
                }
                // Set value and report transaction success
                mutableData.setValue(numberMembers+1);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    // static method to loading an image from firebase
    public boolean loadImage (Activity activity, ImageView imageView) {
        String profileImage = getProfileImage();
        if (profileImage.isEmpty()) {
            return false;
        }

        // Loading image
        Glide.with(activity).load(profileImage).centerCrop()
                .bitmapTransform(new CropCircleTransformation(activity))
                .diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
        Log.d(TAG, "image url: "+MainActivity.getCurrentUser().getProfileImage());

        return true;
    }

    public String toString() {
        return name + " " + surname + " ";
    }
}
