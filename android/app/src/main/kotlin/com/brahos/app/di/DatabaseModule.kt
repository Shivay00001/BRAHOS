package com.brahos.app.di

import android.content.Context
import androidx.room.Room
import com.brahos.app.data.local.BrahosDatabase
import com.brahos.app.data.local.ConsultationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {
    @Provides
    @Singleton
    fun provideWhisperEngine(@ApplicationContext context: Context): WhisperEngine {
        return WhisperEngine(context)
    }

    @Provides
    @Singleton
    fun provideTriageClassifier(@ApplicationContext context: Context): TfliteClassifier {
        return TfliteClassifier(context, "risk_prediction.tflite")
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BrahosDatabase {
        // In a real production app, the passphrase should be retrieved from Android Keystore.
        // For this initialization, we use a placeholder that can be updated during onboarding.
        val passphrase = "BRAHOS_SECURE_PASS_PHRASE".toByteArray()
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(
            context,
            BrahosDatabase::class.java,
            "brahos_secure.db"
        )
        .openHelperFactory(factory)
        .fallbackToDestructiveMigration() // Only for development/initial phase
        .build()
    }

    @Provides
    fun provideConsultationDao(database: BrahosDatabase): ConsultationDao {
        return database.consultationDao()
    }
}
