package com.calmpuchia.userapp.data;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class FirebaseRepository {
    private FirebaseFirestore db;
    public FirebaseRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public CollectionReference getAllOrders() {
        return db.collection("orders");
    }

    public CollectionReference getAllProducts() {
        return db.collection("products");
    }

}
