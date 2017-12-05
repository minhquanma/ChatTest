package com.mmq.chattest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgViewDlgProfile;
    private TextView txtViewProfile;
    private EditText edtDisplayName;
    private EditText edtAvatar;
    private Button buttonSaveProfile;
    private Button buttonCancelProfile;
    private Menu menu;
    private Users user;
    private ProgressDialog progressDialog;
    private RadioGroup radioProfileGroup;
    private RadioButton radioMale;
    private RadioButton radioFemale;

    // Firebase
    DatabaseReference firebaseRef;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    ValueEventListener updateDataEventListener;

    boolean edittingFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Mapping
        imgViewDlgProfile = findViewById(R.id.imgViewDlgProfile);
        txtViewProfile = findViewById(R.id.txtViewDlgEmail);
        edtDisplayName = findViewById(R.id.edtDisplayName);
        edtAvatar = findViewById(R.id.edtAvatar);
        buttonSaveProfile = findViewById(R.id.buttonSaveProfile);
        buttonCancelProfile = findViewById(R.id.buttonCancelProfile);
        radioMale = findViewById(R.id.radioMale);
        radioFemale = findViewById(R.id.radioFemale);
        radioProfileGroup = findViewById(R.id.radioProfileGroup);

        // Firebase.
        database = FirebaseDatabase.getInstance();
        firebaseRef = database.getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        // Get the student extra.
        loadUserProfile();

        buttonCancelProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        buttonSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Hien dialog
                progressDialog = ProgressDialog.show(ProfileActivity.this, "Please wait...", "Applying new data", true);

                // Thuc hien cap nhat profile
                firebaseRef.child("USERS").child(user.getUid()).setValue(getUpdatedUser()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Khi thanh cong thi update du lieu tin nhan
                        updateMessagesData(getUpdatedUser());
                    }
                });
            }
        });
    }

    // Ham cap nhat thong tin cac message tren Firebase
    public void updateMessagesData(final Users user) {

        // Tao mot EventListener moi
        updateDataEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot db : dataSnapshot.getChildren()) {

                    // Lay ve tung doi tuong message
                    Messages msg = db.getValue(Messages.class);

                    // Kiem tra tung message tren firebase, neu trung uid voi user thi update ten moi
                    if (msg.getUid().toLowerCase().contains(user.getUid().toLowerCase())) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("sender", user.getDisplayName());
                        map.put("avatar", user.getAvatar());
                        firebaseRef.child("MESSAGES").child(db.getKey()).updateChildren(map);
                    }
                }

                // Huy dang ky su kien sau khi update xong
                firebaseRef.child("MESSAGES").removeEventListener(updateDataEventListener);

                // Tat dialog
                progressDialog.dismiss();

                // Update xong thi tat activity
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.putExtra("PROFILE", getUpdatedUser());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Neu bi loi thi huy dang ky su kien
                firebaseRef.child("MESSAGES").removeEventListener(updateDataEventListener);
            }
        };

        // Thuc hien refresh du lieu
        firebaseRef.child("MESSAGES").addValueEventListener(updateDataEventListener);
    }

    // Ham lay thong tin moi tu cac widgets
    public Users getUpdatedUser() {
        Users profile = new Users();
        profile.setUid(user.getUid());
        profile.setDisplayName(edtDisplayName.getText().toString());
        profile.setAccount(user.getAccount());
        profile.setAvatar(edtAvatar.getText().toString());

        if (radioProfileGroup.getCheckedRadioButtonId() == radioMale.getId())
            profile.setGender(true);
        else
            profile.setGender(false);
        return profile;
    }

    // Load thong tin user vao widgets
    public void loadUserProfile()
    {
        // Lay extras
        Intent intent = getIntent();
        user = (Users)intent.getExtras().getSerializable("PROFILE");

        setTitle(user.getAccount());

        // Load avatar
        if (URLUtil.isValidUrl(user.getAvatar()))
        Picasso.with(this)
                .load(user.getAvatar())
                .into(imgViewDlgProfile);

        if (user.isGender())
            radioMale.setChecked(true);
        else
            radioFemale.setChecked(true);

        txtViewProfile.setText(user.getDisplayName());
        edtDisplayName.setText(user.getDisplayName());
        edtAvatar.setText(user.getAvatar());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_edit_profile:
                if (edittingFlag == false) {
                    menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_cancel_black_24dp));
                    edittingFlag = true;
                    edtDisplayName.setEnabled(true);
                    edtAvatar.setEnabled(true);
                    radioMale.setEnabled(true);
                    radioFemale.setEnabled(true);
                    buttonSaveProfile.setEnabled(true);
                } else {
                    menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_edit_black_24dp));
                    edittingFlag = false;
                    edtDisplayName.setEnabled(false);
                    edtAvatar.setEnabled(false);
                    radioMale.setEnabled(false);
                    radioFemale.setEnabled(false);
                    buttonSaveProfile.setEnabled(false);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
