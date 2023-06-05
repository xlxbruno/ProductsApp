package com.example.mystore.seller

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberImagePainter
import com.example.mystore.seller.favourites.Favourites
import com.example.mystore.seller.favourites.FavouritesDAO
import com.example.mystore.seller.favourites.FavouritesDatabase
import com.example.mystore.seller.models.ProductsObj
import com.example.mystore.ui.theme.MyStoreTheme
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class ViewInventory : ComponentActivity() {
    // lazy is used to avoid unnecessary intialization of props
    // can improve performance and reduce memory usage
    private val favouritesDatabase by lazy { FavouritesDatabase.getDatabase(this).favouritesDao() }
    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyStoreTheme() {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Scaffold(topBar = {
                        TopAppBar(backgroundColor = Color.Black,
                            title = {
                                Text(
                                    text = "Vendor Inventory",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    color = Color.White
                                )
                            })
                    }) {
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
                                    Toast.makeText(this@ViewInventory,"Error !!," + error.message, Toast.LENGTH_LONG).show()
                                    Log.d("FirebaseReading","Error is " + error.message)
                                    Log.d("FirebaseReading","Error is " + error.details)
                                    Log.d("FirebaseReading","Error is " + error.code)
                                }

                            })
                            // call to composable to display our user interface
                            listOfProducts(LocalContext.current,productList,lifecycleScope,favouritesDatabase)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun listOfProducts(
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

        Row() {
            Text(
                text = "Products World",
                modifier = Modifier.padding(10.dp),
                style = TextStyle(
                    color = Color.Black, fontSize = 16.sp
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
                ProductCard(product = product!!, context,lifecycleScope,favouritesDatabase)
            }
        }
    }
}

@Composable
fun ShowFavs(favouritesDatabase: FavouritesDAO, lifecycleScope: LifecycleCoroutineScope) {
    // state variable to track whether my pop up interface is open or closed
    var showFavDialog by remember { mutableStateOf(false) }
    // we need our list of favourites.
    var favouriteList by remember {
        mutableStateOf(emptyList<Favourites>())
    }
    // call the button
    Button(onClick = {
        // fetching data from the room database and setting the state of our alert box
        showFavDialog = true
        // get our list
        lifecycleScope.launch{
            favouriteList = favouritesDatabase.getFavs().first()
        }

    }) {
        Text(text = "View Favourites")
    }
    var dialogHeight  = 600.dp
    if (showFavDialog){
        AlertDialog(
            onDismissRequest = {  showFavDialog = false},
            title = { Text(text = "My Favourites")},
            text = {
                Box(modifier = Modifier
                    .height(dialogHeight)
                    .fillMaxWidth() ){
                    LazyColumn(modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()){
                        items(favouriteList) {
                            Card(modifier = Modifier.padding(8.dp), elevation = 4.dp) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = it.favouriteName, fontWeight = FontWeight.Bold)
                                    Text("Image download link : ${it.favouriteImage}")
                                    Text("Product Seller : ${it.favouriteContact}")
                                    Text("Product Price : ${it.favouritePrice}")
                                }
                            }
                        }
                    }
                }


            } ,
            confirmButton = {
                Button(onClick = { showFavDialog = false}) {
                    Text(text = "Close Dialog")
                }
            }
        )
    }

}


@Composable
fun ProductCard(
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
            Row() {
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
