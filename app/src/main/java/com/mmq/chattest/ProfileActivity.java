package com.mmq.chattest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgViewProfile;
    private TextView txtViewName;
    private EditText edtDisplayName;
    private EditText edtBirthday;
    private Button buttonSaveProfile;
    private Button buttonCancelProfile;
    private Menu editingButton;
    private Users user;
    private ProgressDialog progressDialog;
    private RadioGroup radioProfileGroup;
    private RadioButton radioMale;
    private RadioButton radioFemale;
    private TextView txtViewPassword;

    // Firebase
    private ValueEventListener updateDataEventListener;

    private boolean editingFlag = false;
    private final int REQUEST_CODE = 1;
    private Uri selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Mapping
        imgViewProfile = findViewById(R.id.imgViewProfile);
        txtViewName = findViewById(R.id.txtViewName);
        edtDisplayName = findViewById(R.id.edtDisplayName);
        edtBirthday = findViewById(R.id.edtBirthday);
        buttonSaveProfile = findViewById(R.id.buttonSaveProfile);
        buttonCancelProfile = findViewById(R.id.buttonCancelProfile);
        radioMale = findViewById(R.id.radioMale);
        radioFemale = findViewById(R.id.radioFemale);
        radioProfileGroup = findViewById(R.id.radioProfileGroup);
        txtViewPassword = findViewById(R.id.txtViewPassword);

        // Lấy extra.
        Intent intent = getIntent();
        user = (Users)intent.getExtras().getSerializable("PROFILE");

        loadUserProfile(user);

        buttonCancelProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        buttonSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = ProgressDialog.show(ProfileActivity.this, null, "Updating profile...", true);
                // Lấy thông tin người đã cập nhật.
                final Map updatedUser = getMapUpdatedUserProfile();

                // Up ảnh lên server.
                if (selectedImage != null) {
                    API.firebaseStorage.child("USERS/" + API.currentUID + ".avatar")
                            .putFile(selectedImage)
                            .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    // Lấy link ảnh đã upload.
                                    String avatarURL = task.getResult().getDownloadUrl().toString();

                                    // Gán vào đối tượng.
                                    updatedUser.put("avatar", avatarURL);

                                    // Update lên database.
                                    API.firebaseRef.child("USERS")
                                            .child(API.currentUser.getUid())
                                            .updateChildren(updatedUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressDialog.dismiss();
                                            Toast.makeText(ProfileActivity.this, "Profile updated successful!", Toast.LENGTH_SHORT).show();

                                            // Cập nhật giao diện.
                                            loadUserProfile(getUpdatedUserProfile());

                                            // Cập nhật lại thông tin tin nhắn.
                                            updateMessagesData(getUpdatedUserProfile());
                                        }
                                    });
                                }
                            });
                } else {
                    // Nếu không chọn up ảnh thì chỉ update thông tin cơ bản thôi.
                    API.firebaseRef.child("USERS")
                            .child(API.currentUser.getUid())
                            .updateChildren(updatedUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "Profile updated successful!", Toast.LENGTH_SHORT).show();

                            // Cập nhật giao diện.
                            loadUserProfile(getUpdatedUserProfile());

                            // Cập nhật lại thông tin tin nhắn.
                            updateMessagesData(getUpdatedUserProfile());
                        }
                    });
                }
            }
        });

        imgViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select image"), REQUEST_CODE);
            }
        });
    }

    public void updateMessagesData(final Users user) {
        updateDataEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot db : dataSnapshot.getChildren()) {

                    // Lấy về từng message.
                    Messages msg = db.getValue(Messages.class);

                    // Kiểm tra từng message, nếu trùng uid với user thì update thông tin.
                    if (msg.getUid().toLowerCase().contains(API.currentUser.getUid().toLowerCase())) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("sender", API.currentUser.getDisplayName());
                        map.put("avatar", API.currentUser.getAvatar());
                        API.firebaseRef.child("MESSAGES").child(db.getKey()).updateChildren(map);
                    }
                }

                // Hủy đăng kí sự kiện sau khi update xong.
                API.firebaseRef.child("MESSAGES").removeEventListener(updateDataEventListener);

                progressDialog.dismiss();

                // Update xong thì tắt activity.
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.putExtra("PROFILE", getUpdatedUserProfile());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                API.firebaseRef.child("MESSAGES").removeEventListener(updateDataEventListener);
            }
        };

        // Thực hiện refresh dữ liệu
        API.firebaseRef.child("MESSAGES").addValueEventListener(updateDataEventListener);
    }
    public Map<String, Object> getMapUpdatedUserProfile() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("displayName", edtDisplayName.getText().toString());
        map.put("birthday", edtBirthday.getText().toString());

        if (radioProfileGroup.getCheckedRadioButtonId() == radioMale.getId())
            map.put("gender", true);
        else
            map.put("gender", false);
        return map;
    }

    public Users getUpdatedUserProfile() {
        Users profile = new Users();
        profile.setDisplayName(edtDisplayName.getText().toString());
        profile.setAccount(user.getAccount());

        if (radioProfileGroup.getCheckedRadioButtonId() == radioMale.getId())
            profile.setGender(true);
        else
            profile.setGender(false);
        return profile;
    }

    public void loadUserProfile(Users user)
    {
        setTitle(user.getAccount());

        // Load avatar
        if (URLUtil.isValidUrl(user.getAvatar()))
        Picasso.with(this)
                .load(user.getAvatar())
                .into(imgViewProfile);

        if (user.isGender())
            radioMale.setChecked(true);
        else
            radioFemale.setChecked(true);

        txtViewName.setText(user.getDisplayName());
        edtDisplayName.setText(user.getDisplayName());
        edtBirthday.setText(user.getBirthday());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.editingButton = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_edit_profile:
                if (editingFlag == false) {
                    editingButton.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_cancel_black_24dp));
                    editingFlag = true;
                    edtDisplayName.setEnabled(true);
                    edtBirthday.setEnabled(true);
                    radioMale.setEnabled(true);
                    radioFemale.setEnabled(true);
                    txtViewPassword.setEnabled(true);
                    buttonSaveProfile.setEnabled(true);
                } else {
                    editingButton.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_edit_black_24dp));
                    editingFlag = false;
                    edtDisplayName.setEnabled(false);
                    edtBirthday.setEnabled(false);
                    radioMale.setEnabled(false);
                    radioFemale.setEnabled(false);
                    txtViewPassword.setEnabled(false);
                    buttonSaveProfile.setEnabled(false);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            selectedImage = data.getData();

            try {
                Bitmap bitMap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                imgViewProfile.setImageBitmap(bitMap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
