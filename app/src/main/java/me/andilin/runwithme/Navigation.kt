package me.andilin.runwithme

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun Navigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // --- Pantalla de Login ---
        composable("login") {
            LoginScreen(
                navController = navController,
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") },
                onRegisterClick = { navController.navigate("register") }
            )
        }

        // --- Pantalla de Registro ---
        composable("register") {
            RegisterScreen(
                navController = navController,
                onClickBack = { navController.popBackStack() },
                onSuccessfulRegister = {
                    navController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        // --- Pantalla principal ---
        composable("home") {
            HomeScreen(
                navController = navController,
                onCrearPublicacion = { navController.navigate("create_publication") }
            )
        }

        // --- Crear publicación ---
        composable("create_publication") {
            CreatePublication(
                onPublicar = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable("crear_grupo") {
            Grupos(navController = navController)
        }

        composable("mapa") {
            Mapa(navController = navController)
        }

        // --- Perfil ---
        composable("profile") {
            ProfileScreen(navController = navController)
        }
        composable(
            "historia/{historiaId}"
        ) { backStackEntry ->
            val historiaId = backStackEntry.arguments?.getString("historiaId") ?: ""
            HistoriaScreen(
                navController = navController,
                historiaId = historiaId
            )
        }

        composable("comentarios/{publicacionId}") { backStackEntry ->
            val publicacionId = backStackEntry.arguments?.getString("publicacionId") ?: ""
            ComentarioScreen(
                navController = navController,
                publicacionId = publicacionId
            )
        }
    }
}