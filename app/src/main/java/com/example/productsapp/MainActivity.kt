package com.example.productsapp

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productsapp.ui.theme.ProductsAppTheme
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProductsAppTheme() {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    Scaffold(topBar = {
                        TopAppBar(backgroundColor = Color.Black,
                            title = {
                                Text(
                                    text = "Products App",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    color = Color.White
                                    ) })
                        }){
                        Column(modifier = Modifier.padding(it)) {
                            AuthenticationUI(LocalContext.current)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuthenticationUI(context : Context){
    // mutable state of to capture text entry
    val phoneNumber = remember{
        mutableStateOf("")
    }

    // otp reference
    val otp = remember{
        mutableStateOf("")
    }
    // verification id if token is valid
    val verificationId = remember {
        mutableStateOf("")
    }
    // custom message to save/store user info
    val message = remember {
        mutableStateOf("")
    }

    //Firebase Initialization
    val mAuth : FirebaseAuth = FirebaseAuth.getInstance()
    lateinit var callback : PhoneAuthProvider.OnVerificationStateChangedCallbacks

    // create UI: edit text , button clicks
    Column(modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()
        .background(Color.White),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(value = phoneNumber.value,
            onValueChange = {phoneNumber.value = it},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text(text = "Enter your phone number")},
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            textStyle = TextStyle(color = Color.Black, fontSize = 15.sp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(10.dp))
        // button to generate otp / make the otp call
        Button(
            onClick = {
                //check if the phone number is empty
                if (TextUtils.isEmpty(phoneNumber.value)){
                    Toast.makeText(context, "Phone Number cannot be empty", Toast.LENGTH_SHORT).show()
                } else {
                    // country code
                    val number = "+254${phoneNumber.value}"
                    SendVerificationCode(number,mAuth,context as Activity, callback)
                }
            }
        ){
            Text(text = "Get OTP", modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))

        // OTP Fields

        TextField(value = otp.value,
            onValueChange = {otp.value = it},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text(text = "Enter your otp number")},
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            textStyle = TextStyle(color = Color.Black, fontSize = 15.sp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(10.dp))
        // button to generate otp / make the otp call
        Button(
            onClick = {
                //check if the phone number is empty
                if (TextUtils.isEmpty(otp.value)){
                    Toast.makeText(context, "OTP cannot be empty", Toast.LENGTH_SHORT).show()
                } else {
                    val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(verificationId.value, otp.value)
                    // loging in with otp credentials
                    SignInWithPhoneAuthCredentials(credential,mAuth,context as Activity, context,message)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = "Verify OTP", modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.height(5.dp))

        // Text field to communicate to user whether the otp was verified or not
        Text(
            text = message.value,
            style = TextStyle(color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Bold))

        // interacting with the callback to know the status of the verification
        callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                message.value = "Verification Successful"
                Toast.makeText(context, "Verification Successful", Toast.LENGTH_SHORT).show()
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                message.value = "Verification Failed..."
                Toast.makeText(context, "Verification Failed", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                verificationId.value
            }
        }
    }
}

private fun SendVerificationCode(
    number :String,
    mAuth: FirebaseAuth,
     activity: Activity,
    callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks
){
// generating the actual code
    val options = PhoneAuthOptions.newBuilder(mAuth)
        .setPhoneNumber(number)
        .setTimeout(60L, TimeUnit.SECONDS)
        .setActivity(activity)
        .setCallbacks(callback)
        .build()
    PhoneAuthProvider.verifyPhoneNumber(options)
}

private fun SignInWithPhoneAuthCredentials(
    credential: PhoneAuthCredential,
    mAuth: FirebaseAuth,
    activity: Activity,
    context: Activity,
    message: MutableState<String>
){
    // signing in using verified OTP code
    mAuth.signInWithCredential(credential).addOnCompleteListener(activity){
        if (it.isSuccessful){
            message.value = "Verification Successful"
            Toast.makeText(context, "Verification Successful", Toast.LENGTH_SHORT).show()
        } else {
            if(it.exception is FirebaseAuthInvalidCredentialsException){
                Toast.makeText(context, "Verification Failed..." + (it.exception as FirebaseAuthInvalidCredentialsException).message,
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}











