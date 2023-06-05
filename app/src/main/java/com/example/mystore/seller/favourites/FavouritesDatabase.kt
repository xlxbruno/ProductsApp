package com.example.mystore.seller.favourites

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Favourites::class], version = 1, exportSchema = true)
@TypeConverters(DateConverter::class)
abstract class FavouritesDatabase : RoomDatabase() {
    abstract fun favouritesDao() : FavouritesDAO

    companion object {
        // volatile : objects is visible to all threads/activities
        @Volatile
        private var INSTANCE : FavouritesDatabase? = null
        fun getDatabase(context: Context) : FavouritesDatabase {
            // if instance is not null , then simply return it
            // if instance is null , we then need to create the db
            if (INSTANCE == null){
                synchronized(this){
                    INSTANCE = buildDatabase(context)
                }
            }
            return INSTANCE!!
        }

        private fun buildDatabase(context: Context): FavouritesDatabase? {
            // creates the room db
            return Room.databaseBuilder(
                context.applicationContext,
                FavouritesDatabase::class.java,
                "favourites"
            ).build()
        }
    }
}