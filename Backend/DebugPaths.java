
// Quick test to verify path matching logic
import java.util.Arrays;
import java.util.List;

public class DebugPaths {
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/public/",
            "/api/auth/",
            "/auth/",
            "/api/bus-company/registration/",
            "/api/user/bus-companies/",
            "/api/schedules/",
            "/api/routes/",
            "/api/reviews/",
            "/api/payment/",
            "/api/test/",
            "/api-docs/",
            "/swagger-ui/",
            "/v3/api-docs/");

    public static void main(String[] args) {
        String testPath = "/api/user/bus-companies";
        System.out.println("Testing path: " + testPath);

        for (String publicPath : PUBLIC_PATHS) {
            System.out.println("Checking against: " + publicPath);
            if (testPath.startsWith(publicPath)) {
                System.out.println("✅ MATCH FOUND!");
                return;
            }
        }
        System.out.println("❌ NO MATCH FOUND");
    }
}