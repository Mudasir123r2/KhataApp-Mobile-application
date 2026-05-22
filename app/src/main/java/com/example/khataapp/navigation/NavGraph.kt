package com.example.khataapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.khataapp.ui.screens.AddCustomerScreen
import com.example.khataapp.ui.screens.AddTransactionScreen
import com.example.khataapp.ui.screens.AllTransactionsScreen
import com.example.khataapp.ui.screens.AnalyticsScreen
import com.example.khataapp.ui.screens.CustomerListScreen
import com.example.khataapp.ui.screens.CustomerStatementScreen
import com.example.khataapp.ui.screens.EditCustomerScreen
import com.example.khataapp.ui.screens.ExportScreen
import com.example.khataapp.ui.screens.HomeScreen
import com.example.khataapp.ui.screens.LoginScreen
import com.example.khataapp.ui.screens.OnboardingScreen
import com.example.khataapp.ui.screens.OverdueScreen
import com.example.khataapp.ui.screens.ProfileScreen
import com.example.khataapp.ui.screens.ReportsScreen
import com.example.khataapp.ui.screens.SetupScreen
import com.example.khataapp.ui.screens.SplashScreen
import com.example.khataapp.ui.screens.TransactionScreen

object Routes {
    const val SPLASH      = "splash"
    const val ONBOARDING  = "onboarding"
    const val SETUP       = "setup"
    const val LOGIN       = "login"
    const val HOME        = "home"
    const val CUSTOMERS   = "customers"
    const val REPORTS     = "reports"
    const val PROFILE     = "profile"
    const val OVERDUE     = "overdue"
    const val EXPORT      = "export"
    const val ALL_TRANSACTIONS  = "all_transactions"
    const val ANALYTICS         = "analytics"
    const val ADD_CUSTOMER      = "add_customer"
    const val TRANSACTION_SCREEN  = "transaction_screen/{customerId}"
    const val ADD_TRANSACTION     = "add_transaction/{customerId}"
    const val EDIT_CUSTOMER       = "edit_customer/{customerId}"
    const val CUSTOMER_STATEMENT  = "customer_statement/{customerId}"

    fun transactionScreen(customerId: Int) = "transaction_screen/$customerId"
    fun addTransaction(customerId: Int)    = "add_transaction/$customerId"
    fun editCustomer(customerId: Int)      = "edit_customer/$customerId"
    fun customerStatement(customerId: Int) = "customer_statement/$customerId"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier
    ) {

        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onGetStarted = {
                    navController.navigate(Routes.SETUP) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SETUP) {
            SetupScreen(
                onSetupComplete = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SETUP) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onAddCustomer         = { navController.navigate(Routes.ADD_CUSTOMER) },
                onViewCustomers       = { navController.navigate(Routes.CUSTOMERS) },
                onViewAllTransactions = { navController.navigate(Routes.ALL_TRANSACTIONS) },
                onViewOverdue         = { navController.navigate(Routes.OVERDUE) }
            )
        }

        composable(Routes.CUSTOMERS) {
            CustomerListScreen(
                onAddCustomer   = { navController.navigate(Routes.ADD_CUSTOMER) },
                onCustomerClick = { customerId ->
                    navController.navigate(Routes.transactionScreen(customerId))
                }
            )
        }

        composable(Routes.REPORTS) {
            ReportsScreen(onViewAnalytics = { navController.navigate(Routes.ANALYTICS) })
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onExport = { navController.navigate(Routes.EXPORT) }
            )
        }

        composable(Routes.OVERDUE) {
            OverdueScreen(
                onNavigateBack  = { navController.popBackStack() },
                onCustomerClick = { customerId ->
                    navController.navigate(Routes.transactionScreen(customerId))
                }
            )
        }

        composable(Routes.EXPORT) {
            ExportScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.ALL_TRANSACTIONS) {
            AllTransactionsScreen(
                onNavigateBack  = { navController.popBackStack() },
                onCustomerClick = { customerId ->
                    navController.navigate(Routes.transactionScreen(customerId))
                }
            )
        }

        composable(Routes.ANALYTICS) {
            AnalyticsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.ADD_CUSTOMER) {
            AddCustomerScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.TRANSACTION_SCREEN,
            arguments = listOf(navArgument("customerId") { type = NavType.IntType })
        ) { back ->
            val customerId = back.arguments?.getInt("customerId") ?: return@composable
            TransactionScreen(
                customerId       = customerId,
                onNavigateBack   = { navController.popBackStack() },
                onAddTransaction = { navController.navigate(Routes.addTransaction(customerId)) },
                onEditCustomer   = { navController.navigate(Routes.editCustomer(customerId)) },
                onViewStatement  = { navController.navigate(Routes.customerStatement(customerId)) }
            )
        }

        composable(
            route = Routes.ADD_TRANSACTION,
            arguments = listOf(navArgument("customerId") { type = NavType.IntType })
        ) { back ->
            val customerId = back.arguments?.getInt("customerId") ?: return@composable
            AddTransactionScreen(
                customerId     = customerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.EDIT_CUSTOMER,
            arguments = listOf(navArgument("customerId") { type = NavType.IntType })
        ) { back ->
            val customerId = back.arguments?.getInt("customerId") ?: return@composable
            EditCustomerScreen(
                customerId     = customerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.CUSTOMER_STATEMENT,
            arguments = listOf(navArgument("customerId") { type = NavType.IntType })
        ) { back ->
            val customerId = back.arguments?.getInt("customerId") ?: return@composable
            CustomerStatementScreen(
                customerId     = customerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
