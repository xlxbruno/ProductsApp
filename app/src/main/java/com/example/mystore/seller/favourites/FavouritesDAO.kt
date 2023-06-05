package com.example.mystore.seller.favourites

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouritesDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFav(favourites: Favourites) : Unit
    @Query("SELECT * FROM favourites")
    fun getFavs() : Flow<List<Favourites>>
    @Update
    suspend fun updateFav(favourites: Favourites) : Unit
    @Delete
    suspend fun deleteFav(favourites: Favourites) : Unit
}