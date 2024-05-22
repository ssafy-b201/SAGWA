package com.ssafy.sagwa.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ssafy.sagwa.R;
import com.ssafy.sagwa.api.Diary.DiaryCUReqDto;
import com.ssafy.sagwa.api.Diary.DiaryService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@RequiresApi(api = Build.VERSION_CODES.O)
public class WriteActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 132;
    private String API_URL;
    private Long memberId;

    private View headerView, calendarLayout, writeLayout, callListLayout;
    ;
    private TextView yearView, dateView;
    private int headerYear, headerMonth, headerDay;

    private String title, content;
    private boolean isclicked;
    private MultipartBody.Part imgFilePart = null;
    private DiaryCUReqDto dto = null;
    private Long diaryId;
    private String resultPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_write);
        API_URL = getString(R.string.APIURL);

        // 로그인 정보
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        memberId = sharedPref.getLong("loginId", 0);

        // 오늘 날짜 세팅
        headerView = findViewById(R.id.write_headerLayout);
        yearView = headerView.findViewById(R.id.write_header_year);
        dateView = headerView.findViewById(R.id.write_header_date);

        String selectedDate = getIntent().getStringExtra("date");
        if (selectedDate != null) {
            setHeaderDate(selectedDate);
        } else {
            setHeaderToday();
        }

        // 사진 선택
        TextView picView = findViewById(R.id.write_input_pic);
        picView.setOnClickListener(v -> getAlbum());

        // 일기 등록
        selectImg();

        // 하단바 이동
        calendarLayout = findViewById(R.id.write_footer_calendarLayout);
        writeLayout = findViewById(R.id.write_footer_writeLayout);
        callListLayout = findViewById(R.id.write_footer_callListLayout);
        ImageView writeView = findViewById(R.id.write_footer_write);
        writeView.setColorFilter(getColor(R.color.point));
        moveOtherCategory();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.writeActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build();
    }

    /* 특정 날짜로 헤더 설정 */
    private void setHeaderDate(String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        DayOfWeek dayOfWeek = parsedDate.getDayOfWeek();
        String day = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN);

        headerYear = parsedDate.getYear();
        headerMonth = parsedDate.getMonth().getValue();
        headerDay = parsedDate.getDayOfMonth();

        yearView.setText(headerYear + "년");
        dateView.setText(headerMonth + "월 " + headerDay + "일 (" + day + ")");
    }

    /* 오늘 날짜로 헤더 설정 */
    private void setHeaderToday() {
        LocalDate parsedNow = LocalDate.now();
        DayOfWeek dayOfWeek = parsedNow.getDayOfWeek();
        String day = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN);

        headerYear = parsedNow.getYear();
        headerMonth = parsedNow.getMonth().getValue();
        headerDay = parsedNow.getDayOfMonth();

        yearView.setText(headerYear + "년");
        dateView.setText(headerMonth + "월 " + headerDay + "일 (" + day + ")");
    }


    /* 사진 선택 */
    private void selectImg() {
        TextView buttonView = findViewById(R.id.write_button);
        buttonView.setOnClickListener(v -> {
            if (imgFilePart != null) {
                EditText titleView = findViewById(R.id.write_input_title);
                EditText contentView = findViewById(R.id.write_input_content);
                title = titleView.getText().toString();
                content = contentView.getText().toString();

                if (!title.isEmpty() && !content.isEmpty()) {
                    dto = DiaryCUReqDto.builder().title(title).content(content).build();
                    writeDiary(imgFilePart, dto);
                }
            } else {
                Toast.makeText(WriteActivity.this, "이미지 또는 일기 데이터가 준비되지 않았습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* 갤러리에서 이미지 가져오기 */
    private void getAlbum() {
            isclicked = true;
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            String fileName = getFileName(selectedImageUri);
            RequestBody fileBody = createRequestBodyFromUri(selectedImageUri);
            imgFilePart = MultipartBody.Part.createFormData("imgFile", fileName, fileBody);

            TextView nameView = findViewById(R.id.write_input_picName);
            nameView.setText(fileName);
        }
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = WriteActivity.this.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private RequestBody createRequestBodyFromUri(Uri uri) {
        try {
            InputStream iStream = WriteActivity.this.getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            int len = 0;
            while ((len = iStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return RequestBody.create(MediaType.parse("image/*"), byteBuffer.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null; // 오류 처리를 적절히 해주어야 합니다.
        }
    }

    /* 일기 작성 */
    private String writeDiary(MultipartBody.Part imgFilePart, DiaryCUReqDto dto) {
        Gson gson = new Gson();
        String jsonDto = gson.toJson(dto);
        RequestBody dtoBody = RequestBody.create(MediaType.parse("application/json"), jsonDto);
        String formatMonth = String.format("%02d", headerMonth);
        String formatDay = String.format("%02d", headerDay);
        String date = headerYear + "-" + formatMonth + "-" + formatDay;

        Gson gsonParam = new GsonBuilder().setLenient().create();

        try {
            OkHttpClient client = getSecureOkHttpClient();
            Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create(gsonParam))
                .client(client)
                .build();

            Call<ResponseBody> call = retrofit.create(DiaryService.class).postDiary(memberId, date, imgFilePart, dtoBody);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    resultPath = response.body().toString();
                    Toast.makeText(WriteActivity.this, "일기가 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(WriteActivity.this, CalendarActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(WriteActivity.this, "일기 작성에 실패했습니다: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("WriteActivity", "///// OkHttp Client Error /////", e);
        }
        return resultPath;

    }


    /* 하단바 이동 */
    private void moveOtherCategory() {
        View.OnClickListener navigationListener = v -> {
            new AlertDialog.Builder(WriteActivity.this)
                .setTitle("이 페이지를 벗어나면 변경 사항이 저장되지 않습니다. 계속하시겠습니까?")
                .setPositiveButton("이동하기", (dialog, which) -> {
                    navigateTo(v.getId());
                })
                .setNegativeButton("취소", null)
                .show();
        };

        calendarLayout.setOnClickListener(navigationListener);
        writeLayout.setOnClickListener(navigationListener);
        callListLayout.setOnClickListener(navigationListener);
    }

    private void navigateTo(int viewId) {
        Intent intent = null;
        if (viewId == R.id.write_footer_calendarLayout) {
            intent = new Intent(WriteActivity.this, CalendarActivity.class);
        } else if (viewId == R.id.write_footer_writeLayout) {
            intent = new Intent(WriteActivity.this, WriteActivity.class);
        } else if (viewId == R.id.write_footer_callListLayout) {
            intent = new Intent(WriteActivity.this, CallListActivity.class);
        }
        if (intent != null) {
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }

}