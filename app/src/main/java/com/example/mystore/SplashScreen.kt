package com.example.mystore

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.mystore.buyer.BuyerDashboard
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "splash_screen" ){
                composable("splash_screen"){
                    SplashScreen(navController = navController)
                }

                composable("select_screen"){
                    Box() {
                        Column(modifier = androidx.compose.ui.Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                            , horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center) {
                            Text(text = "Select User Type", color = Color.Black, fontSize = 20.sp)
                            Spacer(modifier = androidx.compose.ui.Modifier.height(10.dp))
                            Button(onClick = { startSellerActivity() }) {
                                Text(text = "Seller")
                                
                            }
                            Spacer(modifier = androidx.compose.ui.Modifier.height(10.dp))
                            Button(onClick = { startBuyerActivity() }) {
                                Text(text = "Buyer")

                            }
                        }


                    }
                }
            }

        }
    }


    private fun startBuyerActivity() {
        val intent = Intent(this, BuyerDashboard::class.java)
        startActivity(intent)
    }

    private fun startSellerActivity() {
        val intent = Intent(this,OTPVerification::class.java)
        startActivity(intent)
    }
}

@Composable
fun SplashScreen(navController: NavController){
    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.Yellow), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center){
            Image(painter = rememberImagePainter(data = R.drawable.welcome), contentDescription = "Welcome")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "WELCOME TO", style = TextStyle(fontSize = 30.sp))
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "MYSTORE", style = TextStyle(fontSize = 30.sp))

    }

    val scale  = remember {
        androidx.compose.animation.core.Animatable(0f)
    }
    //animation effect
    LaunchedEffect(key1 = true){
        scale.animateTo(
            targetValue = 0.7f,
            animationSpec = tween(
                durationMillis = 800,
                easing = {
                    OvershootInterpolator(4f).getInterpolation(it)
                }
            )
        )
        delay(3000L)
        // after time elapses
        navController.navigate("select_screen")
    }
}




