sealed class Screen(val route: String) {
    object RoleSelection : Screen("role_selection")
    object Dashboard : Screen("dashboard")
    object Projects : Screen("projects")
    object Progress : Screen("progress")
    object Reports : Screen("reports")
    object Profile : Screen("profile")
}

enum class UserRole { ENGINEER, FOREMAN, CLIENT }