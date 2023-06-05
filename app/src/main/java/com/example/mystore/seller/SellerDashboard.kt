package com.example.mystore.seller

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mystore.OTPVerification
import com.example.mystore.R
import com.example.mystore.SideNavItems
import com.example.mystore.seller.models.ProductsObj
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SellerDashboard : ComponentActivity() {
    private lateinit var storageReference: StorageReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
            // CoroutineScope : ability to toggle the drawer state
            val scope = rememberCoroutineScope()
            val navController = rememberNavController()

            Scaffold(scaffoldState = scaffoldState,
            topBar = { SideTopBar(scope = scope, scaffoldState = scaffoldState)
            },
                drawerBackgroundColor = Color.DarkGray,
                drawerContent = {
                    Drawer(scope = scope, scaffoldState = scaffoldState, navController = navController)
                },
                content = {

                    Box(modifier = Modifier.padding(it)) {
                        Surface(
                                  modifier = Modifier.fillMaxSize(),
                                  color = MaterialTheme.colors.background
                        ) {
                            NavHost(navController = navController, startDestination = SideNavItems.SellerDashboard.route ){
                                composable(SideNavItems.SellerDashboard.route){
                                    val firebaseDatabase = FirebaseDatabase.getInstance()
                                    val databaseReference = firebaseDatabase.getReference("ProductDB")
                                    val storage  = Firebase.storage
                                    storageReference = storage.reference.child("MyStore")
                                    ProductForm(LocalContext.current, databaseReference , storageReference)
                                }
                                composable(SideNavItems.ViewInventory.route){
                                    val intent = Intent(this@SellerDashboard, ViewInventory::class.java)
                                    startActivity(intent)

                                }
                            }

                        }

                    }
                },
                backgroundColor = Color.Yellow
            )
        }
    }
}
@Composable
fun ProductForm(
    context: Context,
    databaseReference: DatabaseReference,
    storageReference: StorageReference
){
    val productname = remember{ mutableStateOf(TextFieldValue()) }
    val productcontact = remember{ mutableStateOf(TextFieldValue()) }
    val productprice = remember{ mutableStateOf(TextFieldValue()) }
    Column(modifier = Modifier
        .fillMaxHeight()
        .fillMaxWidth()
        .background(Color.Yellow),
        verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier
            .background(Color.Black)
            .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Text(text = "Add your products to the MYStore",modifier = Modifier.padding(7.dp), style= TextStyle(
                color = Color.White, fontSize = 20.sp
            ), fontWeight = FontWeight.Bold)
        }

        TextField(value = productname.value, onValueChange = {productname.value = it},
            placeholder = { Text(text = "Enter the Product Name")}, modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .background(Color.Black),
            textStyle = TextStyle(color = Color.White, fontSize = 15.sp), singleLine = true
        )
        Spacer(modifier = Modifier.height(5.dp))
        TextField(value = productcontact.value, onValueChange = {productcontact.value = it},
            placeholder = { Text(text = "Enter the Product Contact")}, modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .background(Color.Black),
            textStyle = TextStyle(color = Color.White, fontSize = 15.sp), singleLine = true
        )
        Spacer(modifier = Modifier.height(5.dp))

        // Button action to select an image from my phone gallery
        // 1. A state to hold our upload value
        // 2. A launcherforActivityResult instance : start an activity : access other apps within our android device (gallery,documents)

        //state to hold file uri
        val selectedUri = remember { mutableStateOf<Uri?>(null) }
        //reference to the launcher
        val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()){
            // save selection path to our state variable
            selectedUri.value = it
        }
        // button for the onclick event to select a file
        Button(onClick = {
            launcher.launch("image/*")
        }, modifier = Modifier.background(Color.Black)) {
            Text(text = "Upload Product Image", style = TextStyle(color = Color.White))
        }

        Spacer(modifier = Modifier.height(5.dp))
        TextField(value = productprice.value, onValueChange = {productprice.value = it},
            placeholder = { Text(text = "Enter the Product Price")}, modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .background(Color.Black),
            textStyle = TextStyle(color = Color.White, fontSize = 15.sp), singleLine = true
        )
        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = {
//            push our data to the realtime database
//   first we send the image/file to the storage bucket
            selectedUri.value?.let{
                val imageName = "image_${System.currentTimeMillis()}"
                val imageRef = storageReference.child(imageName)
                imageRef.putFile(it).addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener {
                        // after getting our download url save the details to real time database
                        // use the push method to generate a unique key for the record
                        // converting the storage download url to a string
                        val imagePath = it.toString()
                        val newProduct_Reference = databaseReference.push()
                        // key / unique identifier
                        val productId = newProduct_Reference.key
                        val productObj = productId?.let {
                            ProductsObj(
                                it,productname.value.text,productcontact.value.text,imagePath,
                                productprice.value.text)
                        }

                        // we use a class in firebase called the addValueEventListener
                        databaseReference.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                newProduct_Reference.setValue(productObj)
                                Toast.makeText(context,"Product has been added successfully!!,", Toast.LENGTH_LONG).show()
                                Log.d("Product Push",snapshot.toString())
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(context,"Product failed to be  added!!,", Toast.LENGTH_LONG).show()
                                Log.d("Product Push",error.message)
                            }

                        })
                    }
                }
            }



        }, modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), enabled = selectedUri.value != null) {

            Text(text = "Add Product Details", modifier = Modifier.padding(5.dp))
        }
    }
}

@Composable
fun SideTopBar(scope: CoroutineScope, scaffoldState: ScaffoldState){
    TopAppBar(
        title = { Text(text = "Main Menu") },
        navigationIcon = {
            IconButton(onClick = {
                scope.launch {
                    scaffoldState.drawerState.open()
                }
            }) {
                Icon(Icons.Filled.Menu, "")
            }
        },
        backgroundColor = colorResource(id = R.color.yellow),
        contentColor = Color.Black
    )
}


fun changeUi(
    context: Context,
    activitylauncher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    val intent = Intent(context,OTPVerification::class.java)
    activitylauncher.launch(intent)
}

@Composable
fun ImageUploader(activity: ComponentActivity, storageReference: StorageReference, databaseReference: DatabaseReference) {
    // state to hold image uri
    val selectedImageUri = remember { mutableStateOf<Uri?>(null) }
    //activity result launcher to start image picker
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()){
        selectedImageUri.value = it
    }

    Column() {
        //button to launch the image picker
        Button(onClick = {
            launcher.launch("image/*")
        }, modifier = Modifier.background(Color.Black)) {
            Text(text = "select product image", style = TextStyle(color = Color.White))
        }
    }
}

@Composable
fun Drawer(scope: CoroutineScope, scaffoldState: ScaffoldState, navController: NavController){
//    list of my nav items
    val nav_items = listOf(
        SideNavItems.ViewInventory,SideNavItems.Logout,
        SideNavItems.SellerDashboard)
    // create the view
    Column(modifier =  Modifier.background(colorResource(id = R.color.white))) {
//        navigation header
        Image(
            painterResource(id = R.drawable.mystore), contentDescription = "nav header",
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
                .padding(10.dp) )
//        space in between
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(5.dp))
//        List of items
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        nav_items.forEach{ item ->
            DrawerItem(item = item, selected = currentRoute == item.route,
                onItemClick = {
                    navController.navigate(item.route){
                        navController.graph.startDestinationRoute?.let {
                            popUpTo(route = it){
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                    //close the drawer
                    scope.launch {
                        scaffoldState.drawerState.close()
                    }

                })
        }
//        footer
        Spacer(modifier = Modifier.weight(1f))
        Text(
            "B.H.R", color = Color.Black, textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold, modifier = Modifier
                .padding(12.dp)
                .align(Alignment.CenterHorizontally))
    }

}

@Composable
fun DrawerItem(item: SideNavItems, selected: Boolean, onItemClick: (SideNavItems) -> Unit) {
    val background = if (selected) R.color.yellow else android.R.color.transparent
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onItemClick(item) })
            .height(45.dp)
            .background(colorResource(id = background))
            .padding(start = 10.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.sellerdashboard),
            contentDescription = item.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(35.dp)
                .width(35.dp)
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = item.title,
            fontSize = 18.sp,
            color = Color.Black
        )
    }
}


