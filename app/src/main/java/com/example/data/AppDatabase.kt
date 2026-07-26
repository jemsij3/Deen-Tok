package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Video::class,
        Comment::class,
        Message::class,
        Notification::class,
        User::class,
        Category::class,
        Report::class,
        Advertisement::class,
        AdminLog::class,
        UserPreferences::class,
        RecentSearch::class,
        UserWallet::class,
        CoinPackage::class,
        PaymentTransaction::class,
        CoinTransaction::class,
        VirtualGift::class,
        GiftTransaction::class,
        CreatorWallet::class,
        WithdrawalRequest::class,
        LiveStream::class,
        LiveChatMessage::class,
        LiveViewer::class,
        LiveModerator::class,
        LiveReport::class,
        LiveAnalytics::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deenTokDao(): DeenTokDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "deentok_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
