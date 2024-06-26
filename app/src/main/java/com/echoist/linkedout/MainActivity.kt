package com.echoist.linkedout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.echoist.linkedout.ui.theme.LinkedOutTheme
import com.echoist.linkedout.viewModels.GoogleLoginSuccessDialog
import com.echoist.linkedout.viewModels.SocialLoginViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "screen1") {
                composable("screen1") {
                    AppPreview(navController = navController)
                }
                composable("screen2") {
                    Greeting("Google")
                }
                composable("screen3") {
                    GoogleLoginBtn(navController)

                }
            }

        }
    }
}
//구글로그인 버튼
@Composable
fun GoogleLoginBtn( navController: NavController) {
    val viewModel : SocialLoginViewModel = viewModel()

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        viewModel.handleGoogleSignInResult(result.data, navController)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Button(
            onClick = {
                viewModel.signInWithGoogle(launcher,context)
            }
        ) {
            Text(text = "Google Sign In")
        }
        if (viewModel.state.value) {
            GoogleLoginSuccessDialog(viewModel.state)
        }
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val viewModel : SocialLoginViewModel = viewModel()
    Scaffold {
        Box(modifier= Modifier
            .padding(it)
            .fillMaxSize(),
            contentAlignment = Alignment.Center) {
            Text(
                text = "Hello ${viewModel.userName}!",
                modifier = modifier
            )
        }

    }

}

@Composable
fun AppPreview(navController: NavController) {

    LinkedOutTheme {
        Scaffold (
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {

                    GoogleLoginBtn(navController = navController)

                }
            }
        )
    }
}
