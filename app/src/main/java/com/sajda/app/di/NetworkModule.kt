package com.sajda.app.di

import com.sajda.app.BuildConfig
import com.sajda.app.data.api.DuaApi
import com.sajda.app.data.api.EquranApi
import com.sajda.app.data.api.GithubUpdateApi
import com.sajda.app.data.api.HadithApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                    .addHeader("User-Agent", "NurApp/${BuildConfig.VERSION_NAME}")
                    .apply {
                        if (BuildConfig.HADITH_API_KEY.isNotBlank()) {
                            addHeader("x-api-key", BuildConfig.HADITH_API_KEY)
                            addHeader("apikey", BuildConfig.HADITH_API_KEY)
                        }
                    }
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val baseUrl = BuildConfig.HADITH_API_BASE_URL.let {
            if (!it.endsWith("/")) "$it/" else it
        }.ifBlank { "https://hadithapi.com/public/api/" }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideHadithApi(retrofit: Retrofit): HadithApi = retrofit.create(HadithApi::class.java)

    @Provides
    @Singleton
    fun provideDuaApi(retrofit: Retrofit): DuaApi = retrofit.create(DuaApi::class.java)

    @Provides
    @Singleton
    fun provideEquranApi(retrofit: Retrofit): EquranApi = retrofit.create(EquranApi::class.java)

    @Provides
    @Singleton
    fun provideGithubUpdateApi(retrofit: Retrofit): GithubUpdateApi = retrofit.create(GithubUpdateApi::class.java)
}
