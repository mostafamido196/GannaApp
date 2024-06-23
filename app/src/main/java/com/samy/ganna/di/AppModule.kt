package com.samy.ganna.di

import android.app.Application
import android.content.Context
import com.samy.ganna.data.BookServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun getAllZiker(): BookServices = BookServices()

//    @Provides
//    fun provideContext(
//        app: Application
//    ): MyAdapter {
//        return MyAdapter(activity, listOf("Item 1", "Item 2", "Item 3"))
//    }



//    @Provides
//    @Singleton
//    fun providesOkHttp(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
//        OkHttpClient.Builder().apply {
//            addInterceptor(httpLoggingInterceptor)
//            connectTimeout(TIMEOUT, TimeUnit.SECONDS)
//            writeTimeout(TIMEOUT, TimeUnit.SECONDS)
//            readTimeout(TIMEOUT, TimeUnit.SECONDS)
//        }.build()
}