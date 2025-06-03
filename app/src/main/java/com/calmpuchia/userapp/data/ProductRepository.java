package com.calmpuchia.userapp.data;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ProductRepository {
    private FirebaseFirestore db;
    public ProductRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void getAllProducts(OnCompleteListener<QuerySnapshot> onCompleteListener) {
        db.collection("products").get().addOnCompleteListener(onCompleteListener);
    }
}
