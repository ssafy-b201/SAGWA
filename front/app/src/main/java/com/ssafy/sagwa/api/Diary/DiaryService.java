package com.ssafy.sagwa.api.Diary;

import com.ssafy.sagwa.api.Member.Member;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DiaryService {

    @GET("diary/{date}")
    Call<DiaryDetailResDto> getDiaryByDate(@Path("date") String date, @Query("memberId") Long memberId);

    @Multipart
    @POST("diary")
    Call<ResponseBody> postDiary(@Query("memberId") Long memberId,
                                 @Query("date") String date,
                                 @Part MultipartBody.Part imgFile,
                                 @Part("dto") RequestBody dto);

    @DELETE("diary/{diaryId}")
    Call<Void> deleteDiary(@Path("diaryId") Long diaryId);


}
