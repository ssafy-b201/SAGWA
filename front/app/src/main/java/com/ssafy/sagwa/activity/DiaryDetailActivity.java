package com.ssafy.sagwa.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.ssafy.sagwa.R;
import com.ssafy.sagwa.api.Diary.DiaryDetailResDto;
import com.ssafy.sagwa.api.Diary.DiaryService;
import com.ssafy.sagwa.api.TrustOkHttpClientUtil;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@RequiresApi(api = Build.VERSION_CODES.O)
public class DiaryDetailActivity extends AppCompatActivity {
    private Long memberId;
    private String API_URL;

    private View headerView, calendarLayout, writeLayout, callListLayout;
    private TextView yearView, dateView, titleView, contentView;
    private ImageView imgView;
    private String headerYear, headerMonth, headerDay;
    private Long diaryId;
    private DiaryDetailResDto dto;
    private boolean hasToday;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_diary_detail);

        API_URL = getString(R.string.APIURL);
        dto = null;

        // 로그인 정보
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        memberId = sharedPref.getLong("loginId", 0);

        // 오늘 날짜 세팅
        headerView = findViewById(R.id.diaryDetail_headerLayout);
        yearView = headerView.findViewById(R.id.diaryDetail_header_year);
        dateView = headerView.findViewById(R.id.diaryDetail_header_date);

        // 인텐트에서 날짜 데이터 받기
        String selectedDate = getIntent().getStringExtra("date");
        if (selectedDate != null) {
            setHeaderDate(selectedDate);
        } else {
            setHeaderToday();
        }

        // 해당 날짜 일기 조회
        getDiaryDetail(selectedDate);

        // 일기 삭제
        TextView deleteView = findViewById(R.id.diaryDetail_delete_btn);
        deleteView.setOnClickListener(v -> clickDelete());

        // 하단바 이동
        calendarLayout = findViewById(R.id.diaryDetail_footer_calendarLayout);
        writeLayout = findViewById(R.id.diaryDetail_footer_writeLayout);
        callListLayout = findViewById(R.id.diaryDetail_footer_callListLayout);
        ImageView calendarView = findViewById(R.id.diaryDetail_footer_calendar);
        calendarView.setColorFilter(getColor(R.color.point));
        moveOtherCategory();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.diaryDetailActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /* 특정 날짜로 헤더 설정 */
    private void setHeaderDate(String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        DayOfWeek dayOfWeek = parsedDate.getDayOfWeek();
        String day = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN);

        headerYear = String.valueOf(parsedDate.getYear());
        headerMonth = String.valueOf(parsedDate.getMonthValue());
        headerDay = String.valueOf(parsedDate.getDayOfMonth());

        yearView.setText(headerYear + "년");
        dateView.setText(headerMonth + "월 " + headerDay + "일 (" + day + ")");
    }

    /* 오늘 날짜로 헤더 설정 */
    private void setHeaderToday() {
        LocalDate parsedNow = LocalDate.now();
        DayOfWeek dayOfWeek = parsedNow.getDayOfWeek();
        String day = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN);

        headerYear = String.valueOf(parsedNow.getYear());
        headerMonth = String.valueOf(parsedNow.getMonthValue());
        headerDay = String.valueOf(parsedNow.getDayOfMonth());

        yearView.setText(headerYear + "년");
        dateView.setText(headerMonth + "월 " + headerDay + "일 (" + day + ")");
    }

    /* 해당 날짜의 일기 조회 */
    private void getDiaryDetail(String date) {
        ViewGroup diaryDetailLayout = findViewById(R.id.diaryDetail_contentLayout);
        diaryDetailLayout.removeAllViews();

        LayoutInflater inflater = getLayoutInflater();
        View detailView = inflater.inflate(R.layout.sample_detail_content_view, diaryDetailLayout, false);
        titleView = detailView.findViewById(R.id.detailContent_title);
        contentView = detailView.findViewById(R.id.detailContent_content);
        ImageView weatherView = detailView.findViewById(R.id.detailContent_weather);
        imgView = detailView.findViewById(R.id.detailContent_img);

        OkHttpClient client = TrustOkHttpClientUtil.getUnsafeOkHttpClient();
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build();

        Call<DiaryDetailResDto> call = retrofit.create(DiaryService.class).getDiaryByDate(date, memberId);
        call.enqueue(new Callback<DiaryDetailResDto>() {
            @Override
            public void onResponse(Call<DiaryDetailResDto> call, Response<DiaryDetailResDto> response) {
                dto = response.body();
                runOnUiThread((new Runnable() {
                    @Override
                    public void run() {
                        if (dto != null) {
                            diaryId = dto.getDiaryId();
                            titleView.setText(dto.getDiaryTitle());
                            contentView.setText(dto.getDiaryContent());
                            int weather = setImgByWeather(dto.getDiaryWeather());
                            weatherView.setImageResource(weather);
                            diaryDetailLayout.addView(detailView);
                            disabledWrite();
                            hasToday = true;
                            Glide.with(DiaryDetailActivity.this)
                                .load(dto.getDiaryImg())
                                .error(R.drawable.error_icon) // 로딩 실패 시 보여줄 이미지
                                .into(imgView);
                        } else {
                            Toast.makeText(DiaryDetailActivity.this, "조회되는 일기가 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }));
            }

            @Override
            public void onFailure(Call<DiaryDetailResDto> call, Throwable t) {
                Toast.makeText(DiaryDetailActivity.this, "일기 작성에 실패했습니다: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    /* 오늘 날짜의 일기가 있으면 calendar 이동 막기 */
    private void disabledWrite() {
        ImageView writeView = findViewById(R.id.diaryDetail_footer_write);
        writeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(DiaryDetailActivity.this)
                    .setTitle("오늘 날짜의 일기가 이미 있어요!")
                    .setPositiveButton("확인", null)
                    .show();
            }
        });
    }

    /* 날씨에 맞는 이미지 출력 */
    private int setImgByWeather(String weather) {
        int result = R.drawable.weather_none;
        switch (weather) {
            case "Clear":
                result = R.drawable.weather_clear;
                break;
            case "Clouds":
                result = R.drawable.weather_clouds;
                break;
            case "Thunderstorms":
                result = R.drawable.weather_thunderstorm;
                break;
            case "Drizzle":
                result = R.drawable.weather_drizzle;
                break;
            case "Mist":
                result = R.drawable.weather_mist;
                break;
            case "Haze":
            case "Fog":
                result = R.drawable.weather_fog;
                break;
            case "Ash":
            case "Dust":
            case "Sand":
            case "Smoke":
                result = R.drawable.weather_ash;
                break;
            case "Tornado":
                result = R.drawable.weather_tornado;
                break;
            case "Rain":
            case "Squall":
                result = R.drawable.weather_rainy;
                break;
            case "Snow":
                result = R.drawable.weather_snow;
                break;
        }
        return result;
    }

    /* 하단바 이동 */
    private void moveOtherCategory() {
        calendarLayout.setOnClickListener(v -> navigateTo(v.getId()));
        writeLayout.setOnClickListener(v -> navigateTo(v.getId()));
        callListLayout.setOnClickListener(v -> navigateTo(v.getId()));
    }

    private void navigateTo(int viewId) {
        Intent intent = null;
        if (viewId == R.id.diaryDetail_footer_calendarLayout) {
            intent = new Intent(DiaryDetailActivity.this, CalendarActivity.class);
        } else if (viewId == R.id.diaryDetail_footer_writeLayout) {
            intent = new Intent(DiaryDetailActivity.this, WriteActivity.class);
        } else if (viewId == R.id.diaryDetail_footer_callListLayout) {
            intent = new Intent(DiaryDetailActivity.this, CallListActivity.class);
            intent.putExtra("hasToday", hasToday);
        }
        if (intent != null) {
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }

    /* 일기 삭제 */
    private void clickDelete() {
        new AlertDialog.Builder(DiaryDetailActivity.this)
            .setTitle("정말 삭제하시겠습니까?")
            .setPositiveButton("삭제하기", (dialog, which) -> {
                deleteDiary();
            })
            .setNegativeButton("취소", null)
            .show();
    }

    private void deleteDiary() {
        OkHttpClient client = TrustOkHttpClientUtil.getUnsafeOkHttpClient();
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build();

        Call<Void> call = retrofit.create(DiaryService.class).deleteDiary(diaryId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(DiaryDetailActivity.this, "삭제되었습니다!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(DiaryDetailActivity.this, CalendarActivity.class));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(DiaryDetailActivity.this, "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}