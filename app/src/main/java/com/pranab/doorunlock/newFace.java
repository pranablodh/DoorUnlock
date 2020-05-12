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
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class newFace extends AppCompatActivity
{

    private CircleImageView image;
    private EditText finger_id;
    private Button reg;

    private static final String url = "http://3.7.156.85:4030/registration";

    //Variables for Permission
    private static final int INITIAL_REQUEST = 1337;
    private String[] Permissions = {android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE};

    //Image Picker Elements
    private static final int PICK_FROM_CAMERA = 0;
    private String FinalEncodedImage = "";

    //Progress Dialog Box
    private Dialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_face);

        image = (CircleImageView) findViewById(R.id.image);
        finger_id = (EditText) findViewById(R.id.finger_id);
        reg = (Button) findViewById(R.id.reg);

        //Dialog Initializer
        progressDialog = new Dialog(newFace.this);

        reg.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!validate_test_id() |!validate_image())
                {
                    return;
                }

                showProgressDialog();
                postRequest();
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
    }

    @Override
    public void onBackPressed()
    {
        go_to_new_page();
        super.onBackPressed();
    }

    private void go_to_new_page()
    {
        Intent go = new Intent(newFace.this, MainActivity.class);
        startActivity(go);
        finish();
    }

    private Boolean validate_test_id()
    {
        if(finger_id.getText().toString().length() == 0)
        {
            finger_id.setError("Finger ID Cannot be Blank.");
            return false;
        }

        else
        {
            finger_id.setError(null);
            return true;
        }
    }

    private Boolean validate_image()
    {
        if(FinalEncodedImage.length() == 0)
        {
            Toast.makeText(newFace.this, "Please Capture a Photo!", Toast.LENGTH_SHORT).show();
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

    //Show Progress Dialog
    private void showProgressDialog()
    {
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.progress);
        Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.show();
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
                Log.d("Image_###", FinalEncodedImage);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //Encoding Image Into Base64
    private String base64Encoding(Bitmap mphoto) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assert mphoto != null;
        mphoto.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
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
        StringRequest loginRequest = new StringRequest(Request.Method.POST, url,
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
                MyData.put("finger_id", finger_id.getText().toString());
                return MyData;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(loginRequest);
    }
}
