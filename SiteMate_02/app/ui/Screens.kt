// Illustrative example of what Screens.kt looks like in this architecture
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object DigitalID : Screen("digital_id")
    object QRScanner : Screen("qr_scanner")
    object FormSubmission : Screen("form_submission/{formId}") {
        fun createRoute(formId: String) = "form_submission/$formId"
    }
    object History : Screen("history")
}