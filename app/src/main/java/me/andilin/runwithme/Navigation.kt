package me.andilin.runwithme

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun Navigationp(navController1: NavHostController) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {  },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onClickBack = { navController.popBackStack() },
                onSuccessfulRegister = { navController.navigate("login") }
            )
        }
    }
}
