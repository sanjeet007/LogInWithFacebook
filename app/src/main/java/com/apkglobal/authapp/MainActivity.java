package com.apkglobal.authapp;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    CallbackManager mCallbackManager;
    private Button fbBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        fbBtn=findViewById(R.id.fbBtn);
        //getHashKey();

        mAuth=FirebaseAuth.getInstance();
        mCallbackManager=CallbackManager.Factory.create();
        fbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fbBtn.setEnabled(false);
                LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("email","public_profile"));
                LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException error) {

                    }
                });
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null)
        {
            updateUI();
        }
    }

    private void updateUI() {

        Toast.makeText(this, "You Already logged in", Toast.LENGTH_SHORT).show();
        Intent intent=new Intent(MainActivity.this,HomeActivity.class);
        startActivity(intent);
        finish();
    }


    private void handleFacebookAccessToken(final AccessToken accessToken) {

        AuthCredential credential= FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        fbBtn.setEnabled(true);
                        Toast.makeText(MainActivity.this, "Failure"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

                String email=authResult.getUser().getEmail();
                Toast.makeText(MainActivity.this, "Email "+email, Toast.LENGTH_SHORT).show();
                fbBtn.setEnabled(true);
                updateUI();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode,resultCode,data);
    }

    private void getHashKey() {

        try {
            PackageInfo info=getPackageManager().getPackageInfo("com.apkglobal.authapp", PackageManager.GET_SIGNATURES);
            for(Signature signature:info.signatures)
            {
                MessageDigest messageDigest=MessageDigest.getInstance("SHA");
                messageDigest.update(signature.toByteArray());
                Log.e("KEYHASH", Base64.encodeToString(messageDigest.digest(),Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
