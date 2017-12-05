package com.mmq.chattest;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    // Widget mapping
    Button buttonLogin;
    EditText inputEmail;
    EditText inputPassword;
    TextView linkSignup;

    // Objects
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Widget mapping
        buttonLogin = findViewById(R.id.buttonLogin);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        linkSignup = findViewById(R.id.linkSignup);

        // Khong hien ban phim khi load man hinh
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Check if the user has logged in or not
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
        {
            Intent login = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(login);
        }

        linkSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                // Initialize dialog components
                AlertDialog.Builder aBuilder = new AlertDialog.Builder(LoginActivity.this);
                final View dialogView = getLayoutInflater().inflate(R.layout.dialog_register, null);

                // Dialog widgets mapping.
                final EditText regEmail = dialogView.findViewById(R.id.regEmail);
                final EditText regPassword = dialogView.findViewById(R.id.regPassword);
                final EditText regName = dialogView.findViewById(R.id.regName);
                final RadioGroup radioGender = dialogView.findViewById(R.id.radioGender);
                final RadioButton radioRegMale = dialogView.findViewById(R.id.radioRegMale);
                Button buttonReg = dialogView.findViewById(R.id.buttonReg);

                aBuilder.setView(dialogView);
                aBuilder.setTitle("Create new account");
                aBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Do nothing.
                    }
                });

                final AlertDialog dialog = aBuilder.create();

                buttonReg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String email = regEmail.getText().toString();
                        String password = regPassword.getText().toString();
                        final String displayName = regName.getText().toString();

                        // Neu email hoac password rong thi bao loi
                        if (email.isEmpty() || password.isEmpty()) {
                            Toast.makeText(LoginActivity.this, "E-mail or password must not empty", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            if (password.toString().length() < 6)
                                Toast.makeText(LoginActivity.this, "Password must be 6 digits or over", Toast.LENGTH_SHORT).show();
                            else {
                                progressDialog = ProgressDialog.show(LoginActivity.this, "Please wait...", "Registering...", true);

                                // Thuc hien dang ky user moi tren firebase
                                API.firebaseAuth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {

                                                    // Set user profiles
                                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                                    // Tao user moi
                                                    Users newUser = new Users();
                                                    newUser.setUid(user.getUid());
                                                    newUser.setAccount(email);
                                                    newUser.setAvatar("");
                                                    newUser.setDisplayName(displayName);


                                                    if (radioGender.getCheckedRadioButtonId() == radioRegMale.getId())
                                                        newUser.setGender(true);
                                                    else
                                                        newUser.setGender(false);

                                                    // Tao mot nhanh user moi tren database
                                                    API.firebaseRef.child("USERS").child(user.getUid()).setValue(newUser);

                                                    Toast.makeText(LoginActivity.this, "Successful creating new account!", Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();
                                                    progressDialog.dismiss();

                                                    // Mo man hinh chat khi thanh cong.
                                                    Intent listIntent = new Intent(LoginActivity.this, MainActivity.class);
                                                    startActivity(listIntent);

                                                } else {
                                                    progressDialog.dismiss();
                                                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                                        Toast.makeText(LoginActivity.this, "This account already exists.", Toast.LENGTH_SHORT).show();
                                                    } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                                        Toast.makeText(LoginActivity.this, "Invalid e-mail.", Toast.LENGTH_SHORT).show();
                                                    } else if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                                                        Toast.makeText(LoginActivity.this, "Weak password.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });
                dialog.show();
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();
                try {
                    progressDialog = ProgressDialog.show(LoginActivity.this, "Please wait...", "Authenticating...", true);
                    API.firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {

                                // Start List Activity when success.
                                Intent listIntent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(listIntent);

                            } else {
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    Toast.makeText(LoginActivity.this, "Wrong password!", Toast.LENGTH_SHORT).show();
                                } catch (FirebaseAuthInvalidUserException e) {
                                    Toast.makeText(LoginActivity.this, "The e-mail doesn't exist or has been disabled!", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                } catch (IllegalArgumentException e) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "E-mail & password must not null!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
