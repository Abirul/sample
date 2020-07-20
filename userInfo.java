package com.gabwire.socialmedia;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserInfo extends AppCompatActivity {

    public EditText firstName,lastName;
    public Button userInfoSaveBtn;
    public CircleImageView userInfoCircleImageView;
    public static final int GALLERY_ADD_PROFILE = 1;
    private Bitmap bitmap = null;
    private SharedPreferences userpref;
    private ProgressDialog dialog;
    private String tokenUser ;
    Uri filePath;
    SharedPreferences userPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        dialog = new ProgressDialog(this);
        userpref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        firstName = findViewById(R.id.user_info_firstname);
        lastName = findViewById(R.id.user_info_lastName);
        userInfoSaveBtn = findViewById(R.id.user_info_save_btn);
        userInfoCircleImageView = findViewById(R.id.user_info_circle_image);

        userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);

        tokenUser = userPref.getString("token","");
        String photo = userPref.getString("photo","");

        userInfoCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, GALLERY_ADD_PROFILE);

                updateOrUploadPhoto();
            }
        });

//        Glide.with(this).load(photo).into(userInfoCircleImageView);
//
//        userInfoCircleImageView.setImageURI(Uri.parse("http://192.168.0.103/blog/storage/images/1594877634.png"));

        userInfoSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                saveUserInfo();
            }
        });

//        firstName.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                checkInputs();
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
//
//        lastName.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                checkInputs();
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
        SharedPreferences userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);

        tokenUser = userPref.getString("token","");

    }

    private void updateOrUploadPhoto() {
        String url = "http://192.168.0.105/blog/public/api/auth/save_user_photo";
        SimpleMultiPartRequest simpleMultiPartRequest = new SimpleMultiPartRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(UserInfo.this, response.toString(), Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error",error.toString());
                error.printStackTrace();
                Toast.makeText(UserInfo.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map = new HashMap<>();
                map.put("token",userpref.getString("token",""));
                return map;
            }
        };

        simpleMultiPartRequest.addFile("photo", getRealPathFromUri(UserInfo.this,filePath));

        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        mRequestQueue.add(simpleMultiPartRequest);
        mRequestQueue.start();
    }

    private void saveUserInfo() {
        dialog.show();
        final String firstname = firstName.getText().toString().trim();
        final String lastname = lastName.getText().toString().trim();

        String url = "http://192.168.0.105/blog/public/api/auth/save_user_info";

        SimpleMultiPartRequest simpleMultiPartRequest = new SimpleMultiPartRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(UserInfo.this, response.toString(), Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error",error.toString());
                error.printStackTrace();
                Toast.makeText(UserInfo.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map = new HashMap<>();
                map.put("token",userpref.getString("token",""));
                return map;
            }
        };

        simpleMultiPartRequest.addFile("photo", getRealPathFromUri(this,filePath));

        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        mRequestQueue.add(simpleMultiPartRequest);
        mRequestQueue.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 1 && resultCode == RESULT_OK){
            filePath = data.getData();
            Uri file = data.getData();
            try{
                InputStream inputStream = getContentResolver().openInputStream(file);
                bitmap = BitmapFactory.decodeStream(inputStream);
                userInfoCircleImageView.setImageBitmap(bitmap);
            }catch (Exception e){

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

//    private void checkInputs(){
//        if(!TextUtils.isEmpty(firstName.getText())){
//            if(!TextUtils.isEmpty(lastName.getText())){
//                userInfoSaveBtn.setEnabled(true);
//            }else{
//                userInfoSaveBtn.setEnabled(false);
//            }
//        }else{
//            userInfoSaveBtn.setEnabled(false);
//        }
//    }
    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
