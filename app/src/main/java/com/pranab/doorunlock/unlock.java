package com.pranab.doorunlock;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class unlock extends AppCompatActivity
{

    private CircleImageView image;
    private TextView counter;
    private Button unlock;
    private Button lock;

    //Progress Dialog Box
    private Dialog progressDialog;

    private static final String urlUnLock = "http://3.7.156.85:4030/faceUnlock";
    private static final String urlLock = "http://3.7.156.85:4030/faceLock";
    private String facial_id = "";

    //Variables for Permission
    private static final int INITIAL_REQUEST = 1337;
    private String[] Permissions = {android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE};

    //Image Picker Elements
    private static final int PICK_FROM_CAMERA = 0;
    private String FinalEncodedImage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);

        image = (CircleImageView) findViewById(R.id.image);
        unlock = (Button) findViewById(R.id.unlock);
        lock = (Button) findViewById(R.id.lock);
        counter = (TextView) findViewById(R.id.counter);

        //Dialog Initializer
        progressDialog = new Dialog(unlock.this);

        unlock.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!validate_image())
                {
                    return;
                }

                postRequest();
                showProgressDialog();
            }
        });

        lock.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showProgressDialog();
                postRequestLock();
            }
        });

        image.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!canAccessCamera() | !canReadExternalStorage())
                {
                    requestPermissions(Permissions, INITIAL_REQUEST);
                    return;
                }

                pick_image_from_camera();
            }
        });

        counter.setVisibility(View.INVISIBLE);
        lock.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed()
    {
        go_to_new_page();
        super.onBackPressed();
    }

    private void go_to_new_page()
    {
        Intent go = new Intent(unlock.this, MainActivity.class);
        startActivity(go);
        finish();
    }

    private Boolean validate_image()
    {
        if(FinalEncodedImage.length() == 0)
        {
            Toast.makeText(unlock.this, "Please Capture a Photo!", Toast.LENGTH_SHORT).show();
            return false;
        }

        else
        {
            return true;
        }
    }

    //Checking Permission
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean canAccessCamera()
    {
        return(hasPermission(Manifest.permission.CAMERA));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean canReadExternalStorage()
    {
        return(hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean hasPermission(String perm)
    {
        return(PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm));
    }

    //Pick Image From Camera
    private void pick_image_from_camera()
    {
        Intent intentCamera = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intentCamera, PICK_FROM_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_FROM_CAMERA && resultCode == RESULT_OK)
        {
            try
            {
                Bitmap mphoto = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                assert mphoto != null;
                image.setImageDrawable(null);
                image.setBackground(null);
                image.setImageBitmap(mphoto);
                FinalEncodedImage = base64Encoding(mphoto);
                Log.d("Image", FinalEncodedImage.toString());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //Encoding Image Into Base64
    private String base64Encoding(Bitmap mphoto)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assert mphoto != null;
        mphoto.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void countdown_timer()
    {
        counter.setVisibility(View.VISIBLE);
        new CountDownTimer(30000, 1000)
        {
            @Override
            public void onTick(final long millisUntilFinished)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        counter.setText(String.format("%s", "Time Remaining: " + millisUntilFinished / 1000 + 's'));
                    }
                });
            }

            @Override
            public void onFinish()
            {
                lock.setVisibility(View.VISIBLE);
                showProgressDialog();
                postRequestLock();
            }
        }.start();
    }

    //Show Progress Dialog
    private void showProgressDialog()
    {
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.progress);
        Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.show();
    }

    //Showing Alert Dialog
    private void showDialog(String data)
    {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(data);
        builder1.setCancelable(false);

        builder1.setPositiveButton(
                "Okay",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {

                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void postRequest()
    {
        StringRequest loginRequest = new StringRequest(Request.Method.POST, urlUnLock,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        try
                        {
                            JSONObject server_response = new JSONObject(response);
                            Log.d("data_###", server_response.toString());
                            progressDialog.dismiss();
                            showDialog(server_response.getString("Message"));
                            if(!server_response.getBoolean("Status"))
                            {
                                return;
                            }

                            facial_id = server_response.getString("ID");
                            countdown_timer();
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                            Log.d("data_###", e.toString() + "x");
                            progressDialog.dismiss();
                            showDialog("Error.");
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.d("data_###", error.toString() + "y");
                        progressDialog.dismiss();
                        showDialog("Internet Connectivity Issue.");
                    }
                })
        {
            protected Map<String, String> getParams()
            {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.clear();
                MyData.put("image", FinalEncodedImage);
                return MyData;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(loginRequest);
    }

    private void postRequestLock()
    {
        StringRequest loginRequest = new StringRequest(Request.Method.POST, urlLock,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        try
                        {
                            JSONObject server_response = new JSONObject(response);
                            Log.d("data_###", server_response.toString());
                            progressDialog.dismiss();
                            showDialog(server_response.getString("Message"));
                            if(!server_response.getBoolean("Status"))
                            {
                                return;
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                            Log.d("data_###", e.toString() + "x");
                            progressDialog.dismiss();
                            showDialog("Error.");
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.d("data_###", error.toString() + "y");
                        progressDialog.dismiss();
                        showDialog("Internet Connectivity Issue.");
                    }
                })
        {
            protected Map<String, String> getParams()
            {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.clear();
                MyData.put("facial_id", facial_id);
                return MyData;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(loginRequest);
    }
}
