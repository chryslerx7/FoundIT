package com.example.foundit.api;

import com.example.foundit.model.*;
import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @POST("register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("logout")
    Call<ApiMessage> logout(@Header("Authorization") String auth);

    @GET("me")
    Call<User> me(@Header("Authorization") String auth);

    @Multipart
    @POST("profile")
    Call<AuthResponse> updateProfile(
            @Header("Authorization") String auth,
            @Part("_method") RequestBody method,
            @Part("name") RequestBody name,
            @Part("student_id") RequestBody studentId,
            @Part("email") RequestBody email,
            @Part("password") RequestBody password,
            @Part("password_confirmation") RequestBody passwordConfirmation,
            @Part("delete_profile_image") RequestBody deleteImage,
            @Part MultipartBody.Part profileImage
    );

    @GET("items")
    Call<ItemListResponse> getItems(
            @Header("Authorization") String auth,
            @Query("search") String search,
            @Query("type") String type,
            @Query("category") String category
    );

    @GET("items")
    Call<ItemListResponse> getItems(
            @Header("Authorization") String auth,
            @Query("search") String search,
            @Query("type") String type,
            @Query("category") String category,
            @Query("date") String date
    );

    @GET("items/{id}")
    Call<ItemResponse> getItem(@Header("Authorization") String auth, @Path("id") int id);

    @Multipart
    @POST("items")
    Call<ItemResponse> createItem(
            @Header("Authorization") String auth,
            @Part("item_name") RequestBody itemName,
            @Part("category") RequestBody category,
            @Part("description") RequestBody description,
            @Part("location") RequestBody location,
            @Part("date") RequestBody date,
            @Part("type") RequestBody type,
            @Part("contact") RequestBody contact,
            @Part MultipartBody.Part image,
            @Part List<MultipartBody.Part> images
    );

    @Multipart
    @POST("items")
    Call<ItemResponse> createItem(
            @Header("Authorization") String auth,
            @Part("item_name") RequestBody itemName,
            @Part("category") RequestBody category,
            @Part("description") RequestBody description,
            @Part("location") RequestBody location,
            @Part("date") RequestBody date,
            @Part("type") RequestBody type,
            @Part("contact") RequestBody contact,
            @Part MultipartBody.Part image
    );

    @Multipart
    @POST("items/{id}")
    Call<ItemResponse> updateItem(
            @Header("Authorization") String auth,
            @Path("id") int id,
            @Part("_method") RequestBody method,
            @Part("item_name") RequestBody itemName,
            @Part("category") RequestBody category,
            @Part("description") RequestBody description,
            @Part("location") RequestBody location,
            @Part("date") RequestBody date,
            @Part("type") RequestBody type,
            @Part("contact") RequestBody contact,
            @Part MultipartBody.Part image,
            @Part List<MultipartBody.Part> images
    );

    @Multipart
    @POST("items/{id}")
    Call<ItemResponse> updateItem(
            @Header("Authorization") String auth,
            @Path("id") int id,
            @Part("_method") RequestBody method,
            @Part("item_name") RequestBody itemName,
            @Part("category") RequestBody category,
            @Part("description") RequestBody description,
            @Part("location") RequestBody location,
            @Part("date") RequestBody date,
            @Part("type") RequestBody type,
            @Part("contact") RequestBody contact,
            @Part MultipartBody.Part image
    );

    @GET("my-reports")
    Call<ItemListResponse> myReports(@Header("Authorization") String auth);

    @DELETE("items/{id}")
    Call<ApiMessage> deleteItem(@Header("Authorization") String auth, @Path("id") int id);

    @POST("items/{id}/resolve")
    Call<ApiMessage> resolveItem(@Header("Authorization") String auth, @Path("id") int id);

    @GET("items/{id}/matches")
    Call<ItemListResponse> matches(@Header("Authorization") String auth, @Path("id") int id);

    @GET("notifications")
    Call<List<NotificationItem>> notifications(@Header("Authorization") String auth);

    @POST("notifications/{id}/read")
    Call<ApiMessage> markNotificationRead(@Header("Authorization") String auth, @Path("id") int id);

    @GET("conversations")
    Call<ConversationListResponse> getConversations(@Header("Authorization") String auth);

    @FormUrlEncoded
    @POST("conversations")
    Call<ConversationResponse> startConversation(
            @Header("Authorization") String auth,
            @Field("lost_item_id") int lostId,
            @Field("found_item_id") int foundId
    );

    @GET("conversations/{id}/messages")
    Call<MessageListResponse> getMessages(@Header("Authorization") String auth, @Path("id") int id);

    @FormUrlEncoded
    @POST("conversations/{id}/messages")
    Call<MessageResponse> sendMessage(
            @Header("Authorization") String auth,
            @Path("id") int id,
            @Field("message") String message
    );
}
