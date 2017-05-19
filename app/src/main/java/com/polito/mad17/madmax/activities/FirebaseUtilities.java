package com.polito.mad17.madmax.activities;


class FirebaseUtilities {
    private static final FirebaseUtilities ourInstance = new FirebaseUtilities();

    static FirebaseUtilities getInstance() {
        return ourInstance;
    }

    private FirebaseUtilities() {
    }
}
