package com.polito.mad17.madmax.entities;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.polito.mad17.madmax.activities.MainActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class User implements Parcelable {

    private String ID;
    private String username;
    private String name;
    private String surname;
    private String email;
    private String password;
    private String profileImage; // optional, URL dell'immagine da Firebase

    private HashMap<String, Group> userGroups;  //String = groupID, Group = oggetto Group di cui user fa parte
    private HashMap<String, User> userFriends;  //String = UID, User = friend of this user

    private HashMap<String, Double> balanceWithUsers;   //String = userID,  Double = debito(-) o credito (+) verso gli altri utenti
    private HashMap<String, Double> balanceWithGroups;  //String = groupID, Double = debito(-) o credito (+) verso gli altri gruppi
    private SortedMap<String, Expense> addedExpenses;   //String = timestamp dell'aggiunta, Expense = oggetto spesa

    // todo siamo sicuri di fare sharedGroup?
    private HashMap<String, HashMap<String, Group>> sharedGroupPerFriend;   //String = UID of the friend, HashMap<String, Group> : String = GID of the shared group, Group = shared group

    // constructor typically for creting the current user logged in
    public User(String ID, String username, String name, String surname, String email, String password, String profileImage) {
        this.ID = ID;
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = encryptPassword(password);
        this.profileImage = profileImage;

        this.userGroups = new HashMap<>();
        this.userFriends = new HashMap<>();

        this.balanceWithUsers = new HashMap<>();
        this.balanceWithGroups = new HashMap<>();
        this.addedExpenses = new TreeMap<>();

        // todo siamo sicuri di fare sharedGroup?
        this.sharedGroupPerFriend = new HashMap<>();
    }

    // constructor typically used for populet userFriends of current User
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
    public User() {}

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
        this.password = password;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public HashMap<String, Group> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(HashMap<String, Group> userGroups) {
        this.userGroups = userGroups;
    }

    public HashMap<String, User> getUserFriends() {
        return userFriends;
    }

    public void setUserFriends(HashMap<String, User> friends) {
        this.userFriends = friends;
    }

    public HashMap<String, Double> getBalanceWithUsers() {
        return balanceWithUsers;
    }

    public void setBalanceWithUsers(HashMap<String, Double> balanceWithUsers) {
        this.balanceWithUsers = balanceWithUsers;
    }

    public HashMap<String, Double> getBalanceWithGroups() {
        return balanceWithGroups;
    }

    public void setBalanceWithGroups(HashMap<String, Double> balanceWithGroups) {
        this.balanceWithGroups = balanceWithGroups;
    }

    public SortedMap<String, Expense> getAddedExpenses() {
        return addedExpenses;
    }

    public void setAddedExpenses(SortedMap<String, Expense> addedExpenses) {
        this.addedExpenses = addedExpenses;
    }

    // todo siamo sicuri di fare sharedGroup?
    public HashMap<String, HashMap<String, Group>> getSharedGroupPerFriend() {
        return sharedGroupPerFriend;
    }

    // todo siamo sicuri di fare sharedGroup?
    public void setSharedGroupPerFriend(HashMap<String, HashMap<String, Group>> sharedGroupPerFriend) {
        this.sharedGroupPerFriend = sharedGroupPerFriend;
    }
    /*
        END GETTERS & SETTERS
     */


    // method to encrypt password (Davide found on the internet)
    public String encryptPassword (String passwordToHash) {
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
            for(int i=0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            // get complete hashed password in hex format
            generatedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
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

    /*
    // update balance among other users and among the group this user is part of
    private void updateBalance(Expense expense) {
        // todo per ora fa il calcolo come se le spese fossero sempre equamente divise fra tutti i
        // todo     membri del gruppo (cioè come se expense.equallyDivided fosse sempre = true

        Group g = expense.getGroup();   //gruppo in cui è stato inserita la spesa

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

    public void joinGroup (Group group) {
        group.getMembers().put(this.getID(), this); //aggiunto user alla lista di membri del gruppo
        //this.userGroups.add(g); //gruppo aggiunto alla lista di gruppi di cui user fa parte
        this.userGroups.put(group.getID(), group);

        //creo un debito verso il gruppo
        balanceWithGroups.put(group.getID(), 0d);

        //per ogni membro del gruppo
        for (HashMap.Entry<String, User> friend : group.getMembers().entrySet()) {
            //se non sono io stesso
            if (!friend.getKey().equals(this.getID())) {
                balanceWithUsers.put(friend.getKey(), 0d); //creo un debito verso il membro
                friend.getValue().getBalanceWithUsers().put(this.getID(), 0d); //creo un debito dal membro verso di me
            }
        }
    }



    public HashMap<String, Group> getSharedGroupsMap(User friend)
    {
        HashMap<String, Group> mygroups = new HashMap<>();
        mygroups = this.getUserGroups();

        mygroups.keySet().retainAll(friend.getUserGroups().keySet());

        return  mygroups;

    }


    public ArrayList<Group> getSharedGroupsList (User friend)
    {
        ArrayList<Group> mygroups = new ArrayList<>();
        HashMap<String, Group> mygroupsmap = new HashMap<>(this.getUserGroups());
        HashMap<String, Group> friendgroupsmap = new HashMap<>(friend.getUserGroups());
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



    public String toString() {
        return name + " " + surname + " ";
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

    // update balance among other users and among the group this user is part of
    public void updateBalance(Expense expense) {
        // todo per ora fa il calcolo come se le spese fossero sempre equamente divise fra tutti i
        // todo     membri del gruppo (cioè come se expense.equallyDivided fosse sempre = true

        String groupID = expense.getGroupID();   //gruppo in cui è stato inserita la spesa
        Group g = MainActivity.myself.getUserGroups().get(groupID);

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



}
