package com.mmq.chattest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.os.*;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.twitter.TwitterEmojiProvider;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Widgets
    private View chatView;
    private Button btnSend;
    private EmojiEditText edtMessage;
    private RecyclerView recyclerView;
    private ImageView imgEmoticon;
    private ProgressDialog progressDialog;

    private EmojiPopup emojiPopup;

    // Firebase
    private ValueEventListener getCurrentUEventLisnter;

    Users currentLoggedInUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Install Emoji on Create
        EmojiManager.install(new TwitterEmojiProvider());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Widgets mapping
        chatView = findViewById(R.id.chatView);
        btnSend = findViewById(R.id.btnSend);
        edtMessage = findViewById(R.id.edtMsg);
        recyclerView = findViewById(R.id.chatRoom);
        imgEmoticon = findViewById(R.id.imgEmoticon);

        // Emoji inits
        EmojiPopup.Builder builder = EmojiPopup.Builder.fromRootView(chatView);
        builder.setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
            @Override
            public void onEmojiPopupDismiss() {
                imgEmoticon.setBackgroundResource(R.drawable.ic_insert_emoticon_black_24dp);
            }
        }).setOnEmojiPopupShownListener(new OnEmojiPopupShownListener() {
            @Override
            public void onEmojiPopupShown() {
                imgEmoticon.setBackgroundResource(R.drawable.ic_keyboard_black_24dp);
            }
        });
        emojiPopup = builder.build(edtMessage);

        // Nếu user chưa đăng nhập thì chuyển sang màn hình Login
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
        {
            Intent login = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(login);
        }

        // Hiện progress dialog load dữ liệu
        progressDialog = ProgressDialog.show(MainActivity.this, "Please wait...", "Loading data", true);

        // Lấy Uid của user hiện tại.
        API.currentUid = API.firebaseAuth.getCurrentUser().getUid();

        // EventListener với chức năng lấy Display Name của user hiện tại.
        getCurrentUEventLisnter = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    // Lấy thông tin user đã log in gán vào biến
                    currentLoggedInUser = dataSnapshot.getValue(Users.class);

                    setTitle(currentLoggedInUser.getDisplayName());

                    // Lay cac tin nhan tu firebase
                    loadMessagesFromFirebase();

                    // Hủy đăng ký sự kiện khi đã lấy được thông tin Display name của User hiện tại.
                    API.firebaseRef.child("USERS").child(API.currentUid).removeEventListener(getCurrentUEventLisnter);
                    progressDialog.dismiss();
                }
                catch (java.lang.NullPointerException ex) {
                    Toast.makeText(MainActivity.this, ex.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    API.firebaseAuth.signOut();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        // Đăng ký sự kiện để lấy thông tin của user hiện tại trên firebase
        API.firebaseRef.child("USERS").child(API.currentUid).addListenerForSingleValueEvent(getCurrentUEventLisnter);

        // Button khi click thì gửi tin nhắn mới lên firebase
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!edtMessage.getText().toString().isEmpty()) {
                    API.addMessage(currentLoggedInUser, edtMessage.getText().toString());
                    edtMessage.setText("");
                }
            }
        });

        // Emoticon
        imgEmoticon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (emojiPopup.isShowing()) {
                    emojiPopup.dismiss();
                }
                else {
                    emojiPopup.toggle();
                }
            }
        });
    }

    public void loadMessagesFromFirebase() {
        API.firebaseRef.child("MESSAGES").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<Messages> list = new ArrayList<>();
                ChatAdapter chatAdapter = new ChatAdapter(list);
                LinearLayoutManager chatLayoutManager = new LinearLayoutManager(MainActivity.this);

                for (DataSnapshot db : dataSnapshot.getChildren()) {
                    // Lay tung message
                    Messages msg = db.getValue(Messages.class);

                    // Neu message nay la cua toi
                    if (msg.getUid().toLowerCase().contains(currentLoggedInUser.getUid().toLowerCase())) {
                        msg.setMe(true);
                    }
                    else {
                        msg.setMe(false);
                    }
                    // Cho tung message vao danh sach
                    list.add(msg);
                }

                // Set orientation
                chatLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                // Move to the last item of layout
                chatLayoutManager.scrollToPosition(list.size() - 1);

                recyclerView.setAdapter(chatAdapter);
                recyclerView.setLayoutManager(chatLayoutManager);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.context_menu, menu);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                API.firebaseRef.child("USERS").child(API.currentUid).addListenerForSingleValueEvent(getCurrentUEventLisnter);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_sign_out:
                API.firebaseAuth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                break;

            case R.id.m_profile:
                Intent profile = new Intent(MainActivity.this, ProfileActivity.class);
                profile.putExtra("PROFILE", currentLoggedInUser);
                startActivityForResult(profile, 0);
                break;

            case R.id.m_delete:

                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
