package Farme_rich.Security;

import java.util.List;

/**
 * Single source of truth for which request paths are public (no JWT required,
 * no authorization check required).
 * <p>
 * Consumed by:
 * - SecurityConfig (Spring's authorizeHttpRequests matcher chain)
 * - JwtAuthenticationFilter (shouldNotFilter / DontFilterThisPath)
 * <p>
 * Previously these two lists were maintained by hand in two different files
 * with two different matching styles (Ant patterns vs startsWith chains) and
 * had already drifted out of sync. Edit this file only; both consumers read
 * from it.
 */
public final class PublicPaths {

    /**
     * Exact path matches only (no trailing-segment or extension logic).
     */
    public static final List<String> EXACT = List.of(
            "/", "/index.html", "/favicon.ico",
            "/purest", "/purest/",
            "/wiseGrocer", "/wiseGrocer/",
            "/dolphin-naturals", "/dolphin-naturals/",
            "/CustomerTemplate.html",
            "/Tailor_Template.html",
            "/PurestTemplate.html"
    );
    /**
     * Prefix matches (path.startsWith(prefix)).
     * Used as-is by the filter; converted to "prefix/**" Ant patterns for SecurityConfig.
     */
    public static final List<String> PREFIXES = List.of(
            // Seller
            "/api/seller/auth",
            "/api/seller/deviceId",
            "/api/seller/signupvalidate",
            "/api/seller/profile",
            "/api/seller/readinvoice/",
            "/api/seller/InvoiceData/",
            "/api/seller/CustomerTemplate/",
            "/api/seller/uploadServiceImages",
            "/api/seller/AboutFR",
            "/api/seller/GetOTP",
            "/api/seller/checkpasscode",
            "/api/seller/GenerateOtp",
            "/api/seller/ValidateOtp",
            "/api/seller/DeleteOtp",
            "/api/seller/UploadImage",
            "/api/seller/uploads",

            // Buyer
            "/api/buyer/GetCategories",
            "/api/buyer/searchProductsManageOrderSeller",
            "/api/buyer/getOrGenerateToken",
            "/api/buyer/GetSellerDetails",
            "/api/buyer/ViewOrderDetails",
            "/api/buyer/verifyPayment",
            "/api/buyer/searchProductById",
            "/api/buyer/FullImages",
            "/api/buyer/suggest",
            "/api/buyer/signup",
            "/api/buyer/login",
            "/api/buyer/ResetPassword",
            "/api/buyer/uploads",
            "/api/buyer/GenerateOtp",
            "/api/buyer/ValidateOtp",
            "/api/buyer/DeleteOtp",
            "/api/buyer/SearchBuyerExist",

            "/api/revolt",

            //otp
            "/api/seller/notifications",
            "/api/buyer/notifications",
            // otp
            "/api/seller/notifications/otp",
            "/api/buyer/notifications/otp",

            // Static / SPA
            "/uploads",
            "/assets/",
            "/purest/",
            "/wiseGrocer/",
            "/dolphin-naturals/",
            "/sample/"
    );
    /**
     * Suffix matches (path.endsWith(suffix)).
     */
    public static final List<String> SUFFIXES = List.of(
            ".js", ".css", ".map"
    );

    private PublicPaths() {
    }

    /**
     * Used by JwtAuthenticationFilter — startsWith/endsWith semantics.
     */
    public static boolean isPublic(String path) {
        if (EXACT.contains(path)) {
            return true;
        }
        for (String prefix : PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        for (String suffix : SUFFIXES) {
            if (path.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Used by SecurityConfig — returns Ant-style patterns ("/prefix/**") for requestMatchers().
     * Exact paths are returned as-is; prefixes get a trailing "/**" (or "**" if no
     * trailing slash) so Spring's AntPathMatcher treats them as a subtree match.
     */
    public static String[] antPatterns() {
        List<String> patterns = new java.util.ArrayList<>(EXACT);
        for (String prefix : PREFIXES) {
            patterns.add(prefix.endsWith("/") ? prefix + "**" : prefix + "/**");
        }
        // Suffix patterns translate directly to Ant's single-segment wildcard.
        for (String suffix : SUFFIXES) {
            patterns.add("/*" + suffix);
        }
        return patterns.toArray(new String[0]);
    }
}
