package com.example.mystore.seller.favourites

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "favourites")
data class Favourites (
    @PrimaryKey
    @ColumnInfo(name = "favId")
    val favouriteId: String,
    @ColumnInfo(name = "favName")
    val favouriteName: String,
    @ColumnInfo(name = "favContact")
    val favouriteContact: String,
    @ColumnInfo(name = "favImage")
    val favouriteImage: String,
    @ColumnInfo(name = "favPrice")
    val favouritePrice: String,
    @ColumnInfo(name = "dateAdded")
    val date: Date
)




// primary key : integer
// @PrimaryKey(autoGenerate = true)