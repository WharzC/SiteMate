@Composable
fun SiteMateApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Logic to hide bottom bar on the Role Selection screen
    val showBottomBar = currentRoute != Screen.RoleSelection.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Color(0xFF2F4F4F)) { // Slate Grey
                    val items = listOf(
                        Screen.Dashboard to Icons.Default.Home,
                        Screen.Projects to Icons.Default.List,
                        Screen.Progress to Icons.Default.Build,
                        Screen.Reports to Icons.Default.Info,
                        Screen.Profile to Icons.Default.Person
                    )
                    items.forEach { (screen, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = screen.route) },
                            label = { Text(screen.route.replaceFirstChar { it.uppercase() }) },
                            selected = currentRoute == screen.route,
                            onClick = { navController.navigate(screen.route) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFFFF8C00), // Safety Orange
                                unselectedIconColor = Color.White
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.RoleSelection.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.RoleSelection.route) {
                RoleSelectionScreen(onRoleSelected = { navController.navigate(Screen.Dashboard.route) })
            }
            composable(Screen.Dashboard.route) { DashboardScreen("Reyes", UserRole.ENGINEER) }
            composable(Screen.Projects.route) { /* Projects UI */ }
            composable(Screen.Progress.route) { /* Progress UI */ }
            composable(Screen.Reports.route) { /* Reports UI */ }
            composable(Screen.Profile.route) { /* Profile UI */ }
        }
    }
}