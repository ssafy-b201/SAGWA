package com.ssafy.sagwa.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ssafy.sagwa.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SplashActivity extends AppCompatActivity {
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 111;

    // Firebase 인증 객체
    private FirebaseAuth auth;
    private static final int SPLASH_TIME_OUT = 500;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        auth = FirebaseAuth.getInstance();

//        auth.signOut(); // 테스트용 로그아웃

        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    proceedToMain();
                } else {
                    Toast.makeText(this, "이미지 읽기 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                }
            }
        );

        checkPermissionAndLoadImages();
    }

    private void checkUserRegistration(FirebaseUser user) {
        String userEmail = user.getEmail();
        sendEmailToServer(userEmail);
    }

    private void sendEmailToServer(String email) {
        try {

            OkHttpClient client = getSecureOkHttpClient();

            HttpUrl.Builder urlBuilder = HttpUrl.parse("https://k10b201.p.ssafy.io/sagwa/api/valid").newBuilder();
            urlBuilder.addQueryParameter("email", email);
            String url = urlBuilder.build().toString();

            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(new okhttp3.Callback() {

                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    Log.e("SplashActivity", "Email 검증 실패", e);
                    promptLogin();
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> promptLogin());
                        return;
                    }
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        boolean isRegistered = jsonObject.getBoolean("data");
                        runOnUiThread(() -> {
                            if (isRegistered) {
                                proceedToMain();
                            } else {
                                promptSignup();
                            }
                        });
                    } catch (JSONException e) {
                        Log.e("SplashActivity", "JSON 파싱 오류", e);
                        runOnUiThread(() -> promptLogin());
                    }
                }
            });
        } catch (Exception e) {
            Log.e("SplashActivity", "///// OkHttp Client Error /////", e);
        }
    }

    private OkHttpClient getSecureOkHttpClient() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream caInput = getResources().openRawResource(R.raw.sagwa);
        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
        } finally {
            caInput.close();
        }

        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        X509TrustManager trustManager = (X509TrustManager) tmf.getTrustManagers()[0];

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new javax.net.ssl.TrustManager[]{trustManager}, null);

        return new OkHttpClient.Builder()
            .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
            .build();
    }

    private void proceedToMain() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startActivity(new Intent(SplashActivity.this, CalendarActivity.class));
        }
        finish();
    }

    private void promptLogin() {
        startActivity(new Intent(SplashActivity.this, SigninActivity.class));
        finish();
    }

    private void promptSignup() {
        startActivity(new Intent(SplashActivity.this, SignupActivity.class));
        finish();
    }

    private void checkPermissionAndLoadImages() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
            != PackageManager.PERMISSION_GRANTED) {
            // 권한 요청
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            new Handler().postDelayed(() -> {
                FirebaseUser currentUser = auth.getCurrentUser();
                if (currentUser != null) {
                    // 이미 로그인된 사용자
                    checkUserRegistration(currentUser);
                } else {
                    // 로그인 되어있지 않은 사용자
                    promptLogin();
                }

            }, SPLASH_TIME_OUT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                proceedToMain();
            } else {
                // 권한 거부됨
                Toast.makeText(this, "갤러리 접근 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}

