package com.obiangetfils.videochat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class RegistrationActivity extends AppCompatActivity {

    private CountryCodePicker ccp;
    private EditText phoneText, codeText;
    private Button continueAndNextBtn;
    private String checker = "", phoneNumber = "";
    private RelativeLayout relativeLayout;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);

        /* Link field*/
        phoneText = (EditText) findViewById(R.id.phoneText);
        codeText = (EditText) findViewById(R.id.codeText);
        continueAndNextBtn = (Button) findViewById(R.id.continueNextButton);
        relativeLayout = (RelativeLayout) findViewById(R.id.phoneAuth);
        ccp = (CountryCodePicker) findViewById(R.id.ccp);

        ccp.registerCarrierNumberEditText(phoneText);

        continueAndNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (continueAndNextBtn.getText().toString().equals("Submit") || checker.equals("Code Sent")) {

                    String verificationCode = codeText.getText().toString();
                    if (verificationCode.equals("")){
                        Toast.makeText(RegistrationActivity.this, "Please write a verification at first", Toast.LENGTH_SHORT).show();
                    } else {

                        loadingBar.setTitle("Code Verification");
                        loadingBar.setMessage("Please wait, while we are verifying your code.");
                        loadingBar.setCancelable(false);
                        loadingBar.show();

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                        signInWithPhoneAuthCredential(credential);

                    }

                } else {
                    phoneNumber = ccp.getFullNumberWithPlus();
                    if (!phoneNumber.equals("")) {
                        loadingBar.setTitle("Phone Number Verification");
                        loadingBar.setMessage("Please wait, while we are verifying your phone number!");
                        loadingBar.setCancelable(false);
                        loadingBar.show();

                        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, RegistrationActivity.this, mCallbacks);

                    } else {
                        Toast.makeText(RegistrationActivity.this, "Please write a valid phone number!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(RegistrationActivity.this, "Invalid Phone Number...", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
                relativeLayout.setVisibility(View.VISIBLE);

                continueAndNextBtn.setText("Continue");
                codeText.setVisibility(View.GONE);

            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                mVerificationId = s;
                mResendToken = forceResendingToken;
                relativeLayout.setVisibility(View.GONE);
                checker = "Code Sent";
                continueAndNextBtn.setText("Submit");
                codeText.setVisibility(View.VISIBLE);

                loadingBar.dismiss();
                Toast.makeText(RegistrationActivity.this, "Code Has been sent, please check.", Toast.LENGTH_SHORT).show();
            }
        };

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            loadingBar.dismiss();
                            Toast.makeText(RegistrationActivity.this, "Congratulation, Your are logged in successfully!!", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();

                        } else {
                            loadingBar.dismiss();
                            String e = task.getException().getMessage().toString();
                            Toast.makeText(RegistrationActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}