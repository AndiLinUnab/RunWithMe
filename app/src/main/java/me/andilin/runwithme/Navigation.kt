package me.andilin.runwithme

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun Navigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("crear_grupo") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onClickBack = { navController.popBackStack() },
                onSuccessfulRegister = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }
        composable("crear_grupo") {
            Mapa() // Tu frame de crear grupos
        }
    }
}
