package com.example.mystore.buyer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberImagePainter
import com.example.mystore.buyer.checkout.MainActivity
import com.example.mystore.seller.ShowFavs
import com.example.mystore.seller.favourites.Favourites
import com.example.mystore.seller.favourites.FavouritesDAO
import com.example.mystore.seller.favourites.FavouritesDatabase
import com.example.mystore.seller.models.ProductsObj
import com.example.mystore.ui.theme.MyStoreTheme
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


class BuyerDashboard : ComponentActivity() {
    private val favouritesDatabase by lazy { FavouritesDatabase.getDatabase(this).favouritesDao() }
    private lateinit var storageReference: StorageReference

    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyStoreTheme {
                Scaffold(topBar = {}) {
                    Column(modifier = Modifier.padding(it)) {

                        // mutableStateListOf<String?>()
                        val productList = mutableStateListOf<ProductsObj?>()
                        // getting firebase instance and the database reference
                        val firebaseDatabase = FirebaseDatabase.getInstance()
                        val databaseReference = firebaseDatabase.getReference("ProductDB")
                        // to read data values ,we use the addChildEventListener
                        databaseReference.addChildEventListener(object : ChildEventListener {
                            override fun onChildAdded(
                                snapshot: DataSnapshot,
                                previousChildName: String?
                            ) {
                                // this method is called when a new child/record is added to our db
                                // we are adding that item to the list
                                val product = snapshot.getValue(ProductsObj::class.java)
                                productList.add(product)
                            }

                            override fun onChildChanged(
                                snapshot: DataSnapshot,
                                previousChildName: String?
                            ) {
                                // this method is called when a new child is added
                                // when a new child is added to our list of
                            }

                            override fun onChildRemoved(snapshot: DataSnapshot) {
                                // method is called when we remove a child from the db
                            }

                            override fun onChildMoved(
                                snapshot: DataSnapshot,
                                previousChildName: String?
                            ) {
                                // method is called when we move a record/child in the db.
                            }

                            override fun onCancelled(error: DatabaseError) {
//                                    if we get any firebase error
                                Toast.makeText(this@BuyerDashboard,"Error !!," + error.message, Toast.LENGTH_LONG).show()
                                Log.d("FirebaseReading","Error is " + error.message)
                                Log.d("FirebaseReading","Error is " + error.details)
                                Log.d("FirebaseReading","Error is " + error.code)
                            }

                        })
                        // call to composable to display our user interface
                        ListOfProducts(LocalContext.current,productList,lifecycleScope,favouritesDatabase)


                    }

                }

            }
        }
    }
}



@Composable
fun ListOfProducts(
    context: Context,
    productList: SnapshotStateList<ProductsObj?>,
    lifecycleScope: LifecycleCoroutineScope,
    favouritesDatabase: FavouritesDAO,
){
    Column(modifier = Modifier
        .fillMaxHeight()
        .fillMaxWidth()
        .background(Color.Yellow),
        verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(modifier = Modifier.background(Color.Black).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Text(
                text = "MY STORE",
                style = TextStyle(
                    color = Color.White, fontSize = 16.sp
                ), fontWeight = FontWeight.Bold
            )

            // show favourites composable
            ShowFavs(
                favouritesDatabase = favouritesDatabase,
                lifecycleScope = lifecycleScope
            )
        }

        LazyColumn{
            items(productList) {product ->
                // here have a custom UI for the list or quick set up
//                 make my composable
                // !! this is called the safe call operator
                // its use here is to unwrap the opting String? value from product list.
                Product_Card(product = product!!, context,lifecycleScope,favouritesDatabase)
            }
        }
    }
}

@Composable
fun Product_Card(
    product: ProductsObj,
    context: Context,
    lifecycleScope: LifecycleCoroutineScope,
    favouritesDatabase: FavouritesDAO
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // load our image composable
            Image(
                painter = rememberImagePainter(data = product.productImage),
                contentDescription = "Product Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Text(
                text = product.productName,
                style = TextStyle(color = Color.White),
                fontWeight = FontWeight.Bold
            )
            Text(text = "Seller Contact: ${product.contactPhone}")
            Text(text = "Seller Price: ${product.productPrice}")
            Spacer(modifier = Modifier.height(5.dp))
            // define state to track loading process
            var isLoading by remember {
                mutableStateOf(false)
            }
            // row
            Row(modifier = Modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    isLoading = true
                    // get the current time and date
                    val newFavAdded  = Date()
                    // add product to favourite
                    val newFav = Favourites(product.productId,product.productName,product.contactPhone,product.productImage
                        ,product.productPrice,newFavAdded)
                    // adding the product to the room db
                    lifecycleScope.launch{
                        favouritesDatabase.addFav(newFav)
                        delay(3000)
                        isLoading = false
                    }
                }) {
                    if (isLoading){
                        LoadingProgress()
//                        CircularProgressIndicator()
                    } else {
                        Text(text = "Add to Favourites")
                    }
                }

                val activitylauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()){ activityResult ->

                }
                Button(onClick = {
                    val intent = Intent(context, MainActivity::class.java)
                    activitylauncher.launch(intent)
                }) {
                    Text(text = "Checkout")
                }

            }
        }
    }
}

@Composable
fun LoadingProgress() {
    val strokeWidth = 5.dp
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = Color.Yellow,
            strokeWidth = strokeWidth
        )
    }
}






