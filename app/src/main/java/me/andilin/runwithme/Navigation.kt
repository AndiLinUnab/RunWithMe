package me.andilin.runwithme

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

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

        // --- Crear grupo ---
        composable("crear_grupo") {
            Grupos(navController = navController)
        }

        // --- NUEVO: Unirse a grupos ---
        composable("unir_grupo") {
            UnirGrupo(navController = navController)
        }

        // --- NUEVO: Notificaciones ---
        composable("notificaciones") {
            NotificacionesScreen(navController = navController)
        }

        // --- NUEVO: Comentarios (con parámetro publicacionId) ---
        composable(
            route = "comentarios/{publicacionId}",
            arguments = listOf(
                navArgument("publicacionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val publicacionId = backStackEntry.arguments?.getString("publicacionId") ?: ""
            ComentarioScreen(
                navController = navController,
                publicacionId = publicacionId
            )
        }

        // --- Mapa ---
        composable("mapa") {
            Mapa(navController = navController)
        }

        // --- Perfil ---
        composable("profile") {
            ProfileScreen(navController = navController)
        }
    }
}