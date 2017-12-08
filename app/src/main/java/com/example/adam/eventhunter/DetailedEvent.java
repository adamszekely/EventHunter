package com.example.adam.eventhunter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailedEvent extends AppCompatActivity {

    TextView title,start_date,end_date,location,going,interested,invited,description,host,detail,textInterested,textGoing,textInvited;
    ImageView mainImage,clockStartIcon,clockEndIcon,locationIcon,checkIcon,starIcon,invitedIcon,hostIcon;
    Drawable drawable;
    ProgressBar progressBar;
    String pageId,mTitle,mStart_date,mEnd_date,mLocation,mGoing,mInterested,mInvited,mDescription,mHost;
    AccessToken accessToken;
    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_event);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar_detail);
        int colorCodeDark = Color.parseColor("#FF4052B5");
        progressBar.getIndeterminateDrawable().setColorFilter(colorCodeDark, PorterDuff.Mode.SRC_IN);
        title=(TextView) findViewById(R.id.detailed_title);
        start_date=(TextView) findViewById(R.id.detailed_date_start);
        end_date=(TextView) findViewById(R.id.detailed_date_end);
        location=(TextView) findViewById(R.id.detailed_location);
        host=(TextView) findViewById(R.id.detailed_hostedby);
        going=(TextView) findViewById(R.id.detailed_going_number);
        interested=(TextView) findViewById(R.id.detailed_interested_number);
        invited=(TextView) findViewById(R.id.detailed_invited_number);
        description=(TextView) findViewById(R.id.detailed_description);
        mainImage=(ImageView) findViewById(R.id.detailed_image);
        detail=(TextView) findViewById(R.id.detailed_details);
        clockStartIcon=(ImageView) findViewById(R.id.clock2);
        clockEndIcon=(ImageView) findViewById(R.id.clockEnd);
        locationIcon=(ImageView) findViewById(R.id.location2);
        checkIcon=(ImageView) findViewById(R.id.going_check);
        starIcon=(ImageView) findViewById(R.id.interested_star);
        invitedIcon=(ImageView) findViewById(R.id.detailed_invited_icon);
        hostIcon=(ImageView) findViewById(R.id.host);
        textGoing=(TextView) findViewById(R.id.detailed_going);
        textInterested=(TextView) findViewById(R.id.detailed_interested);
        textInvited=(TextView) findViewById(R.id.detailed_invited);
        mContext = this.getApplicationContext();
        setTitle("");
        accessToken = AccessToken.getCurrentAccessToken();

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                pageId= null;
            } else {
                pageId= extras.getString("pageId");
            }
        } else {
            pageId= (String) savedInstanceState.getSerializable("pageId");
        }

        new getEventDetailsAsync().execute(pageId);
    }

    private class getEventDetailsAsync extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(final String... strings) {
            Bundle params = new Bundle();
            new GraphRequest(
                    accessToken,
                    strings[0] + "?fields=name,place,start_time,end_time,cover,attending_count,maybe_count," +
                            "owner,noreply_count,description",
                    params,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            JSONObject jsonObject = response.getJSONObject();

                            //Add all the ids of the pages a user likes into an arraylist
                            if (response != null) {
                                try {
                                    mTitle = (jsonObject.getString("name"));
                                    mStart_date = (jsonObject.getString("start_time"));
                                    mEnd_date = (jsonObject.getString("end_time"));
                                    mLocation = (jsonObject.getJSONObject("place").getString("name").toString());
                                    drawable = drawableFromUrl(jsonObject.getJSONObject("cover")
                                            .getString("source"));
                                    mGoing=(jsonObject.getString("attending_count"));
                                    mInterested=(jsonObject.getString("maybe_count"));
                                    mInvited=(jsonObject.getString("noreply_count"));
                                    mDescription=(jsonObject.getString("description"));
                                    mHost=(jsonObject.getJSONObject("owner").getString("name"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).executeAndWait();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            try {
                Date myDate = dateFormat.parse(mStart_date);
                dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String finalDate = dateFormat.format(myDate);
                start_date.setText(finalDate);

                dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date myDate2=dateFormat.parse(mEnd_date);
                dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String finalDate2=dateFormat.format(myDate2);
                end_date.setText(finalDate2);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            title.setText(mTitle);
            location.setText("At "+mLocation);
            host.setText("Hosted by "+mHost);
            going.setText(mGoing);
            interested.setText(mInterested);
            invited.setText(mInvited);
            description.setText(mDescription);
            mainImage.setImageDrawable(drawable);
            detail.setVisibility(View.VISIBLE);
            clockStartIcon.setVisibility(View.VISIBLE);
            clockEndIcon.setVisibility(View.VISIBLE);
            locationIcon.setVisibility(View.VISIBLE);
            checkIcon.setVisibility(View.VISIBLE);
            starIcon.setVisibility(View.VISIBLE);
            invitedIcon.setVisibility(View.VISIBLE);
            hostIcon.setVisibility(View.VISIBLE);
            textGoing.setVisibility(View.VISIBLE);
            textInterested.setVisibility(View.VISIBLE);
            textInvited.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public static Drawable drawableFromUrl(String url) throws IOException {
        Bitmap x;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();

        x = BitmapFactory.decodeStream(input);
        return new BitmapDrawable(mContext.getResources(), x);
    }
}
