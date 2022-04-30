package net.ivanvega.androidfacebooka;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareMediaContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";

    private static final String EMAIL = "email";
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private Button btnPublicar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "net.ivanvega.androidfacebooka",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("GIVOKeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        callbackManager = CallbackManager.Factory.create();
    
        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList(EMAIL));

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Toast.makeText(MainActivity.this, "Acceso exitoso",
                        Toast.LENGTH_SHORT).show();

                Toast.makeText(MainActivity.this, loginResult.toString(),
                        Toast.LENGTH_SHORT).show();

                Log.d(TAG, loginResult.toString());

            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Toast.makeText(MainActivity.this, "error en acceso",
                        Toast.LENGTH_SHORT).show();
            }
        });


        ActivityResultLauncher<String[]> getDocument = registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                uri -> {
                    Log.d(TAG, uri.toString());
                    ContentResolver resolver = getApplicationContext().getContentResolver();
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        try {
                            ImageDecoder.Source source = ImageDecoder.createSource(resolver, uri);
                            Bitmap bitmap = ImageDecoder.decodeBitmap(source);
                            SharePhoto photo = new SharePhoto.Builder()
                                    .setBitmap(bitmap)
                                    .build();
                            SharePhotoContent content = new SharePhotoContent.Builder()
                                    .addPhoto(photo)
                                    .build();
                            ShareDialog shareDialog = new ShareDialog(this);
                            shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });


        btnPublicar = findViewById(R.id.btnPublicar);

        btnPublicar.setOnClickListener(v -> {
            getDocument.launch(null);
        });

    }
}