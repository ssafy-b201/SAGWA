package com.ssafy.sagwa.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ssafy.sagwa.R;
import com.ssafy.sagwa.api.Diary.DiaryDetailResDto;
import com.ssafy.sagwa.api.Diary.DiaryService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@RequiresApi(api = Build.VERSION_CODES.O)
public class CalendarActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private String API_URL;
    private Long memberId;
    private View headerView;
    private TextView yearView;
    private TextView dateView;
    private CalendarView calendarView;
    private ViewGroup diaryLayout;
    private View diaryView;
    private TextView titleView;
    private TextView contentView;
    private TextView nullView;
    private TextView buttonView;

    private int clickYear, clickMonth, clickDay;
    private String clickDate;
    private boolean hasToday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendar);
        API_URL = getString(R.string.APIURL);

        headerView = findViewById(R.id.calendar_headerLayout);
        yearView = headerView.findViewById(R.id.calendar_header_year);
        dateView = headerView.findViewById(R.id.calendar_header_date);
        calendarView = findViewById(R.id.calendar_calendar_calendar);
        diaryLayout = findViewById(R.id.calendar_diaryLayout);
        diaryView = getLayoutInflater().inflate(R.layout.sample_calendar_diary_view, diaryLayout, false);
        titleView = diaryView.findViewById(R.id.calendarDiary_title);
        contentView = diaryView.findViewById(R.id.calendarDiary_content);
        nullView = diaryView.findViewById(R.id.calendarDiary_null);
        buttonView = diaryView.findViewById(R.id.calendarDiary_button);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        assert user != null;
        getId(user.getEmail());

        // 로그인 정보
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        memberId = sharedPref.getLong("loginId", 0);

        // 헤더
        setHeader(); // 기본
        getDiaryData(clickDate);
        chooseDate(); // 날짜 선택

        // 오늘 이후 날짜 비활성화
        long maxDate = System.currentTimeMillis();
        calendarView.setMaxDate(maxDate);

        // 일기 상세로 이동
        if (titleView.getVisibility() == View.VISIBLE && contentView.getVisibility() == View.VISIBLE) {
            View.OnClickListener click = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    moveToDiaryDetail(clickDate);
                }
            };

            titleView.setOnClickListener(click);
            contentView.setOnClickListener(click);
        }

        // 하단바 이동
        ImageView calendarView = findViewById(R.id.calendar_footer_calendar);
        calendarView.setColorFilter(getColor(R.color.point));
        ImageView writeView = findViewById(R.id.calendar_footer_write);
        ImageView callListView = findViewById(R.id.calendar_footer_callList);

        moveOtherActivity(calendarView, writeView, callListView);

        // 오늘로 돌아가기
        returnToday();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.calendarActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // 헤더에 날짜 세팅
    private void setHeader() {
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        String yoil = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN);

        yearView.setText(today.getYear() + "년");
        dateView.setText(today.getMonthValue() + "월 " + today.getDayOfMonth() + "일 (" + yoil + ")");

        clickDate = String.valueOf(LocalDate.of(today.getYear(), today.getMonth(), today.getDayOfMonth()));
        getDiaryData(String.valueOf(today));
    }

    // 날짜 선택
    private void chooseDate() {
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                clickYear = year;
                clickMonth = month + 1;
                clickDay = dayOfMonth;
                DayOfWeek dayOfWeek = LocalDate.of(clickYear, clickMonth, clickDay).getDayOfWeek();
                String clickYoil = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN);

                yearView.setText(clickYear + "년");
                dateView.setText(clickMonth + "월 " + clickDay + "일 (" + clickYoil + ")");

                clickDate = String.valueOf(LocalDate.of(clickYear, clickMonth, clickDay));

                getDiaryData(clickDate);
            }
        });
    }

    // 일기 조회
    private void getDiaryData(String date) {
        // 뷰가 이미 생성되어 있다면 재사용
        if (diaryLayout.getChildCount() == 0) {
            diaryLayout.addView(diaryView);
        } else {
            diaryView = diaryLayout.getChildAt(0);
        }


        OkHttpClient client = null;
        try {
            client = getSecureOkHttpClient();
            Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

            Call<DiaryDetailResDto> call = retrofit.create(DiaryService.class).getDiaryByDate(date, memberId);
            call.enqueue(new Callback<DiaryDetailResDto>() {
                @Override
                public void onResponse(Call<DiaryDetailResDto> call, Response<DiaryDetailResDto> response) {
                    DiaryDetailResDto dto = response.body();
                    if (dto != null) {
                        titleView.setText(dto.getDiaryTitle());
                        contentView.setText(dto.getDiaryContent());
                        titleView.setVisibility(View.VISIBLE);
                        contentView.setVisibility(View.VISIBLE);
                        nullView.setVisibility(View.GONE);
                        buttonView.setVisibility(View.GONE);

                        if (clickDate.equals(String.valueOf(LocalDate.now()))) {
                            hasToday = true;
                            disabledWriteActivity();
                        }
                    } else {
                        titleView.setVisibility(View.GONE);
                        contentView.setVisibility(View.GONE);
                        nullView.setVisibility(View.VISIBLE);
                        buttonView.setVisibility(View.VISIBLE);

                        buttonView.setOnClickListener(v -> goToWriteActivity());
                    }
                }

                @Override
                public void onFailure(Call<DiaryDetailResDto> call, Throwable t) {
                    Toast.makeText(CalendarActivity.this, "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("DiaryActivity", "///// OkHttp Client Error /////", e);
        }
    }

    // 작성하러 가기 버튼
    private void goToWriteActivity() {
        Intent intent = new Intent(CalendarActivity.this, WriteActivity.class);
        intent.putExtra("date", clickDate);
        startActivity(intent);
        overridePendingTransition(0, 0);

    }

    // 일기 상세보기로 이동
    private void moveToDiaryDetail(String date) {
        Intent intent = new Intent(CalendarActivity.this, DiaryDetailActivity.class);
        intent.putExtra("date", date);
        startActivity(intent);
    }


    // 하단바 이동
    private void moveOtherActivity(ImageView calendarView, ImageView writeView, ImageView callListView) {
        View calendarLayout = findViewById(R.id.calendar_footer_calendarLayout);
        View writeLayout = findViewById(R.id.calendar_footer_writeLayout);
        View callListLayout = findViewById(R.id.calendar_footer_callListLayout);
        calendarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(CalendarActivity.this instanceof CalendarActivity)) {
                    calendarView.setColorFilter(getColor(R.color.point));
                    writeView.setColorFilter(getColor(R.color.black));
                    callListView.setColorFilter(getColor(R.color.black));
                    Intent intent = new Intent(CalendarActivity.this, CalendarActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            }
        });
        writeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarView.setColorFilter(getColor(R.color.black));
                writeView.setColorFilter(getColor(R.color.point));
                callListView.setColorFilter(getColor(R.color.black));
                Intent intent = new Intent(CalendarActivity.this, WriteActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        callListLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarView.setColorFilter(getColor(R.color.black));
                writeView.setColorFilter(getColor(R.color.black));
                callListView.setColorFilter(getColor(R.color.point));
                Intent intent = new Intent(CalendarActivity.this, CallListActivity.class);
                intent.putExtra("hasToday", hasToday);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }

    // 오늘 날짜 글이 있으면 이동 막기
    private void disabledWriteActivity() {
        View writeLayout = findViewById(R.id.calendar_footer_writeLayout);
        writeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(CalendarActivity.this)
                    .setTitle("오늘 날짜의 일기가 이미 있어요!")
                    .setPositiveButton("확인", null)
                    .show();
            }
        });
    }

    // 로그인
    private void getId(String email) {
        try {
            OkHttpClient client = getSecureOkHttpClient();

            HttpUrl.Builder urlBuilder = HttpUrl.parse("https://k10b201.p.ssafy.io/sagwa/api/signin").newBuilder();
            urlBuilder.addQueryParameter("email", email);
            String url = urlBuilder.build().toString();

            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(new okhttp3.Callback() {

                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    Log.e("CalendarActivity", "로그인 정보 통신 실패", e);
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    Log.d("HTTP Status Code", String.valueOf(response.code()));
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = null;
                        jsonObject = new JSONObject(jsonResponse);
                        long loginId = jsonObject.getLong("data");
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putLong("loginId", loginId);
                        editor.apply();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    response.close();
                }
            });
        } catch (Exception e) {
            Log.e("CalendarActivity", "///// OkHttp Client Error /////", e);
        }
    }

    // okhttp
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

    // 오늘 날짜로 돌아오기
    private void returnToday() {
        TextView buttonView = findViewById(R.id.calendar_header_today);
        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long today = System.currentTimeMillis();
                calendarView.setDate(today, true, true);

                setHeader();
            }
        });
    }


}