package com.mmq.chattest;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

/**
 * Created by mmq on 09/08/2017.
 */

public class API {

    // Firebase
    public static DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReference();
    public static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    public static String currentUid;
    public static Users currentUser = new Users();
    private static ProgressDialog progressDialog;

    public static void addMessage(Users currentUser, String message) {
        // Tao key moi tren firebase
        String newKey = firebaseRef.child("MESSAGES")
                .push().getKey();

        firebaseRef.child("MESSAGES").child(newKey)
                .setValue(new Messages(currentUser.getUid(), newKey, currentUser.getDisplayName(),
                        message,
                        currentUser.avatar));
    }

    // Ham xoa tin nhan
    public static void removeMessage(String key) {
        // Xoa key
        firebaseRef.child("MESSAGES")
                .child(key)
                .removeValue();
    }

    // Xem thong tin user
    public static void showProfileDialog(Context context, Users user) {
        AlertDialog.Builder aBuilder = new AlertDialog.Builder(context);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_profile, null);

        ImageView imgViewDlgProfile = dialogView.findViewById(R.id.imgViewDlgProfile);
        TextView txtViewDlgEmail = dialogView.findViewById(R.id.txtViewDlgEmail);
        TextView txtViewDlgGender = dialogView.findViewById(R.id.txtViewDlgGender);

        // Load thong tin
        if (URLUtil.isValidUrl(user.getAvatar())) {
            Picasso.with(context)
                    .load(user.getAvatar())
                    .into(imgViewDlgProfile);
        }
        txtViewDlgEmail.setText(user.getAccount());
        txtViewDlgGender.setText("Gender: " + user.stringGender());

        aBuilder.setView(dialogView);
        aBuilder.setTitle(user.getDisplayName());
        aBuilder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Do nothing.
            }
        });
        final AlertDialog dialog = aBuilder.create();
        dialog.show();
    }

    // Xem thong tin user dua vao uid
    public static void showProfileDialog(final Context context, String uid) {
        // Hien progress dialog load du lieu
        progressDialog = ProgressDialog.show(context, "", "Please wait...");

        firebaseRef.child("USERS").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);

                showProfileDialog(context, user);

                // Tat progress
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
