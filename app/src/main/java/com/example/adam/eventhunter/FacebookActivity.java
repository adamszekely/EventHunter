package com.example.adam.eventhunter;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class FacebookActivity extends FragmentActivity {

    LoginButton loginButton;
    CallbackManager callbackManager;
    private FirebaseAuth mAuth;
    ConnectivityManager cm;
    Context mContext;
    DialogFragment wcd;
    NetworkInfo ni;
    Intent main;

    private String firstName,lastName, email,birthday,gender;
    private URL profilePicture;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_facebook);
        mContext = this.getApplicationContext();
        cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        wcd = new WirelessConnectionDialogFragment();


        // Initialize Firebase Auth
        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();
        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.button_facebook_login);
        FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback(){

                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    userId = object.getString("id");
                                    profilePicture = new URL("https://graph.facebook.com/" + userId + "/picture?width=500&height=500");
                                    if(object.has("first_name"))
                                        firstName = object.getString("first_name");
                                    if(object.has("last_name"))
                                        lastName = object.getString("last_name");
                                    if (object.has("email"))
                                        email = object.getString("email");
                                    if (object.has("birthday"))
                                        birthday = object.getString("birthday");
                                    if (object.has("gender"))
                                        gender = object.getString("gender");

                                    main = new Intent(FacebookActivity.this,MainActivity.class);
                                    main.putExtra("name",firstName);
                                    main.putExtra("surname",lastName);
                                    main.putExtra("imageUrl",profilePicture.toString());
                                    //startActivity(main);
                                    //finish();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                }
                            }

                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, first_name, last_name, email, birthday, gender");
                request.setParameters(parameters);
                request.executeAsync();
                Log.d("facebookLoginSuccess", "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
                loginButton.setVisibility(View.INVISIBLE);
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
        loginButton.setReadPermissions("email", "user_birthday","user_posts");
        loginButton.registerCallback(callbackManager, callback);
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onResume() {
        super.onResume();
        ni = cm.getActiveNetworkInfo();
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user == null) {
                    return;
                } else {

                    if (ni != null) {
                        //Intent intent = new Intent(FacebookActivity.this, MainActivity.class);
                        startActivity(main);
                        finish();
                    } else {
                        wcd.show(getSupportFragmentManager(), "Connection");

                    }
                }
            }
        });
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
