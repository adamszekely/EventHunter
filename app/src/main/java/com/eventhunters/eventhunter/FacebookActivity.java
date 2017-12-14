package com.eventhunters.eventhunter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.eventhunters.eventhunter.Fragments.WirelessConnectionDialogFragment;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.Manifest;

public class FacebookActivity extends FragmentActivity {

    LoginButton loginButton;
    CallbackManager callbackManager;
    private FirebaseAuth mAuth;
    ConnectivityManager cm;
    Context mContext;
    DialogFragment wcd;
    NetworkInfo ni;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_facebook);
        mContext = this.getApplicationContext();
        cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        wcd = new WirelessConnectionDialogFragment();
        progressBar = (ProgressBar) findViewById(R.id.progressBar3);
        int colorCodeDark = Color.parseColor("#FF4052B5");
        progressBar.getIndeterminateDrawable().setColorFilter(colorCodeDark, PorterDuff.Mode.SRC_IN);

        // Initialize Firebase Auth
        FirebaseApp.initializeApp(this);


        mAuth = FirebaseAuth.getInstance();
        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.button_facebook_login);
        FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {


            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("facebookLoginSuccess", "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
                loginButton.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancel() {
                Log.d("facebookLoginCancel", "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.d("facebookLoginError", "facebook:onError", exception);
            }
        };
        loginButton.registerCallback(callbackManager, callback);
        mAuth = FirebaseAuth.getInstance();

        //Request permission to see the user's liked pages and events
        loginButton.setReadPermissions("user_likes", "user_events");
    }

    @Override
    protected void onResume() {
        super.onResume();  if (ActivityCompat.checkSelfPermission(FacebookActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(FacebookActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(FacebookActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        else if (ActivityCompat.checkSelfPermission(FacebookActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(FacebookActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(FacebookActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return;
        }else {
            ni = cm.getActiveNetworkInfo();
            mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) {
                        return;
                    } else {

                        if (ni != null) {
                            Intent intent = new Intent(FacebookActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            wcd.show(getSupportFragmentManager(), "Connection");

                        }
                    }
                }
            });
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    // [START auth_with_facebook]
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("AccessToken", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("SignInSuccess", "signInWithCredential:success");

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("SignInFail", "signInWithCredential:failure", task.getException());
                            Toast.makeText(FacebookActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                    }
                });
    }
    // [END auth_with_facebook]

    public void facebookLoginClick(View v) {

    }


}
