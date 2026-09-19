package Farme_rich.Security;

import Farme_rich.Buyer.Model.Buyer;
import Farme_rich.Buyer.Repo.BuyerRepo;
import Farme_rich.Seller.Model.FrontEnd.User;
import Farme_rich.Seller.Repo.FrontEnd.SellerUserRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final Map<String, String> CANONICAL_SPA_PREFIXES = Map.of(
            "wisegrocer", "wiseGrocer",
            "dolphin-naturals", "dolphin-naturals",
            "purest", "purest"
    );
    private final SellerUserRepo sellerUserRepo;

    private final BuyerRepo buyerRepo;
    @Autowired
    private JwtService jwtService;

    public JwtAuthenticationFilter(SellerUserRepo sellerUserRepo, BuyerRepo buyerRepo) {
        this.sellerUserRepo = sellerUserRepo;
        this.buyerRepo = buyerRepo;
    }

    private String getSubjectWithoutValidation(String token) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            throw new RuntimeException("Invalid JWT");
        }
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));

        Map<String, Object> payload = new ObjectMapper().readValue(payloadJson, Map.class);
        return (String) payload.get("sub");
    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//
//        String path = request.getRequestURI();
//        logger.info("==================================================");
//        logger.info("JWT FILTER START");
//        logger.info("==================================================");
//        logger.info("JwtAuthenticationFilter triggered for: " + path);
//
//        try {
//            Enumeration<String> headerNames = request.getHeaderNames();
//
//            if (redirectIfCasingMismatch(request, response)) {
//                return;
//            }
//
//
//            if (DontFilterThisPath(request)) {
//                logger.info("Public endpoint. Skipping JWT validation.");
//                filterChain.doFilter(request, response);
//                return;
//            }
//
//            String authHeader = request.getHeader("Authorization");
//            String rolesHeader = request.getHeader("x-user-role");
//
//            logger.info("Authorization Header = {}", authHeader);
//            logger.info("x-user-role Header = {}", rolesHeader);
//
//            if ((authHeader == null || !authHeader.startsWith("Bearer ")) && "USER".equalsIgnoreCase(rolesHeader)) {
//
//                logger.info("Guest User Detected");
//
//                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
//
//                UsernamePasswordAuthenticationToken authentication =
//                        new UsernamePasswordAuthenticationToken(
//                                "user",
//                                null,
//                                authorities
//                        );
//
//                SecurityContextHolder
//                        .getContext()
//                        .setAuthentication(authentication);
//
//                logger.info("Guest Authentication Set Successfully");
//
//                filterChain.doFilter(request, response);
//                return;
//            }
//
//            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//                logger.error("Missing Authorization Header");
//                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Authorization Header");
//                return;
//            }
//            String token = authHeader.substring(7);
//            logger.info("JWT Token Received");
//            Claims claims;
//            String[] parts = token.split("\\.");
//            if (parts.length < 2) {
//                logger.error("Invalid JWT Format");
//                throw new RuntimeException("Invalid JWT format");
//            }
//
//            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
//
//            logger.info("JWT Payload = {}", payloadJson);
//
//            Map<String, Object> payload = new ObjectMapper().readValue(payloadJson, Map.class);
//
//            logger.info("Decoded Payload = {}", payload);
//
//            String TokendeviceId = "";
//            String Tokenpin = "";
//            String Tokenemail = "";
//
//            String platform = (String) payload.get("platform");
//
//
//            logger.info("Platform = {}", platform);
//
//
//            boolean isWeb = "WEB".equalsIgnoreCase(platform);
//
//            boolean isMobile = "MOBILE".equalsIgnoreCase(platform);
//
//            logger.info("isWeb = {}", isWeb);
//            logger.info("isMobile = {}", isMobile);
//
//            if (rolesHeader == null || rolesHeader.isBlank()) {
//                logger.error("Missing x-user-role Header");
//                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing x-user-role header");
//                return;
//            }
//
//            List<String> rolesFromHeader = Arrays.stream(rolesHeader.split(",")).map(String::trim)
//                    .filter(s -> !s.isEmpty())
//                    .collect(Collectors.toList());
//
//            logger.info("Roles From Header = {}", rolesFromHeader);
//
//
//            String secret;
//
//            String subject = getSubjectWithoutValidation(token);
//            logger.info("Subject = {}", subject);
//
//            String[] subjectParts = subject.split("\\|");
//
//            if (isWeb) {
//                logger.info("WEB Platform Detected");
//                if (subjectParts.length < 2) {
//                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid subject");
//                    return;
//                }
//                String mobileNum = subjectParts[1];
//                if (mobileNum == null || mobileNum.isBlank()) {
//                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing mobile");
//                    return;
//                }
//                String tokenPin = subjectParts.length > 2 ? subjectParts[2] : "";
//                if (subjectParts != null && subjectParts.length == 2) {
//                    secret = mobileNum;
//                } else {
//                    Buyer user = buyerRepo.findByMobileNum(mobileNum);
//
//                    if (user == null) {
//                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
//                        return;
//                    }
//
//                    String dbPin = "";
//
//                    if (rolesFromHeader.contains("LOGGEDUSER")) {
//                        try {
//                            dbPin = user.getPassword();
//                            if (tokenPin != null && !tokenPin.isBlank() && !tokenPin.equals(dbPin)) {
//                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token PIN mismatch");
//                                return;
//                            }
//                        } catch (Exception ex) {
//                            logger.error("PIN Decryption Error", ex);
//                        }
//                    }
//                    secret = mobileNum;
//
//                    if (dbPin != null && !dbPin.isBlank()) {
//                        secret += dbPin;
//                    }
//
//                    logger.info("Generated WEB Secret");
//                }
//
//            } else {
//                logger.info("MOBILE Platform Detected");
//                if (subjectParts.length < 2) {
//                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid subject");
//                    return;
//                }
//                String tokenDeviceId = subjectParts[1];
//                String tokenPin = subjectParts.length > 2 ? subjectParts[2] : "";
//                User user = sellerUserRepo.findBydeviceId(tokenDeviceId);
//
//                if (user == null) {
//                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
//                    return;
//                }
//
//                String dbPin = "";
//                if (!rolesFromHeader.contains("GUEST")) {
//                    try {
//                        dbPin = user.getFirstPiN();
//
//                        if (tokenPin != null && !tokenPin.isBlank() && !dbPin.equals(tokenPin)) {
//                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token PIN mismatch");
//                            return;
//                        }
//
//                    } catch (Exception ex) {
//                        logger.error("PIN Decryption Error", ex);
//                        response.sendError(
//                                HttpServletResponse.SC_UNAUTHORIZED,
//                                "PIN validation failed"
//                        );
//                        return;
//                    }
//                }
////            dbPin=decrypt(user.getFirstPiN(), user.getDeviceId() + "intellesydetech");
//
//                secret = (dbPin != null && !dbPin.isBlank())
//                        ? dbPin + user.getDeviceId()
//                        : user.getDeviceId();
//            }
//
//            logger.info("Secret Built Successfully");
//
//
//            claims = jwtService.parseClaimsAllowExpired(token, secret);
//            logger.info("JWT Claims = {}", claims);
//
//            if (jwtService.isTokenExpired(claims)) {
//                logger.error("JWT Expired");
//                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
//                return;
//            }
//            if (jwtService.shouldRefresh(claims)) {
//                logger.info("JWT Refresh Required");
//                String newToken = JwtService.generateToken(
//                        claims,
//                        jwtService.extractPin(token, secret),
//                        jwtService.extractDeviceId(token, secret),
//                        jwtService.extractEmail(token, secret)
//                );
//                response.setHeader("Authorization", "Bearer " + newToken);
//            }
//
//            logger.info("JWT Expiry Validation Passed");
//
//            // =========================================================
//            // Role Validation
//            // =========================================================
//

    /// /        String subject = claims.getSubject();
//
//            String tokenRole = claims.get("role", String.class);
//
//
//            logger.info("Token Role = {}", tokenRole);
//            logger.info("Header Roles = {}", rolesFromHeader);
//            logger.info(
//                    "Role Match = {}",
//                    rolesFromHeader.contains(tokenRole)
//            );
//
//
//            List<GrantedAuthority> authorities =
//                    rolesFromHeader.stream()
//                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
//                            .collect(Collectors.toList());
//
//            UsernamePasswordAuthenticationToken authentication =
//                    new UsernamePasswordAuthenticationToken(
//                            subject,
//                            null,
//                            authorities
//                    );
//
//            SecurityContextHolder
//                    .getContext()
//                    .setAuthentication(authentication);
//
//            request.setAttribute("claims", claims);
//
//            logger.info("Authentication Successful");
//            logger.info("Username = {}", subject);
//            logger.info("Authority = ROLE_{}", tokenRole);
//
//            filterChain.doFilter(request, response);
//
//        } catch (JwtException | IllegalArgumentException e) {
//
//            logger.error("JWT Validation Failed", e);
//
//            response.sendError(
//                    HttpServletResponse.SC_UNAUTHORIZED,
//                    "Invalid JWT"
//            );
//
//        } catch (Exception e) {
//
//            logger.error("Authentication Failed", e);
//
//            response.sendError(
//                    HttpServletResponse.SC_UNAUTHORIZED,
//                    "Authentication failed"
//            );
//        }
//
//        logger.info("JWT FILTER END");
//    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        logger.info("==================================================");
        logger.info("JWT FILTER START");
        logger.info("==================================================");
        logger.info("JwtAuthenticationFilter triggered for: {}", path);

        try {

            // =========================================================
            // Handle casing mismatch
            // =========================================================
            if (redirectIfCasingMismatch(request, response)) {
                return;
            }

            // =========================================================
            // Public endpoints
            // =========================================================
            if (DontFilterThisPath(request)) {
                logger.info("Public endpoint. Skipping JWT validation.");
                filterChain.doFilter(request, response);
                return;
            }

            String authHeader = request.getHeader("Authorization");
            String rolesHeader = request.getHeader("x-user-role");

            logger.info("Authorization Header = {}", authHeader);
            logger.info("x-user-role Header = {}", rolesHeader);

            // =========================================================
            // Guest USER request
            // =========================================================
            if ((authHeader == null || !authHeader.startsWith("Bearer "))
                    && "USER".equalsIgnoreCase(rolesHeader)) {

                logger.info("Guest User Detected");

                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("user", null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.info("Guest Authentication Set Successfully");

                filterChain.doFilter(request, response);
                return;
            }

            // =========================================================
            // Authorization header validation
            // =========================================================
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.error("Missing Authorization Header");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Authorization Header");
                return;
            }

            String token = authHeader.substring(7).trim();

            if (token.isBlank()) {
                logger.error("Empty JWT Token");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT");
                return;
            }

            logger.info("JWT Token Received");

            // =========================================================
            // Basic JWT format validation
            // =========================================================
            String[] parts = token.split("\\.");

            if (parts.length != 3) {
                logger.error("Invalid JWT Format");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT format");
                return;
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));

            logger.info("JWT Payload = {}", payloadJson);

            Map<String, Object> payload = new ObjectMapper().readValue(payloadJson, Map.class);

            logger.info("Decoded Payload = {}", payload);

            // =========================================================
            // Platform
            // =========================================================
            String platform = (String) payload.get("platform");

            logger.info("Platform = {}", platform);

            boolean isWeb = "WEB".equalsIgnoreCase(platform);
            boolean isMobile = "MOBILE".equalsIgnoreCase(platform);

            if (!isWeb && !isMobile) {
                logger.error("Invalid or missing platform: {}", platform);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid platform");
                return;
            }

            logger.info("isWeb = {}", isWeb);
            logger.info("isMobile = {}", isMobile);

            // =========================================================
            // Role header validation
            // =========================================================
            if (rolesHeader == null || rolesHeader.isBlank()) {
                logger.error("Missing x-user-role Header");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing x-user-role header");
                return;
            }

            List<String> rolesFromHeader = Arrays.stream(rolesHeader.split(","))
                    .map(String::trim)
                    .filter(role -> !role.isEmpty())
                    .collect(Collectors.toList());

            logger.info("Roles From Header = {}", rolesFromHeader);

            // =========================================================
            // Get subject without validating signature
            // =========================================================
            String subject = getSubjectWithoutValidation(token);

            if (subject == null || subject.isBlank()) {
                logger.error("Missing JWT Subject");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid subject");
                return;
            }

            logger.info("Subject = {}", subject);

            String[] subjectParts = subject.split("\\|");

            String secret;

            // =========================================================
            // WEB
            // =========================================================

            if (isWeb) {
                logger.info("WEB Platform Detected");

                if (subjectParts.length < 2) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid subject");
                    return;
                }

                String mobileNum = subjectParts[1];

                if (mobileNum == null || mobileNum.isBlank()) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing mobile");
                    return;
                }

                String tokenPin = subjectParts.length > 2 ? subjectParts[2] : "";

                List<Buyer> userList = buyerRepo.findByMobile(mobileNum);

                if (userList == null || userList.isEmpty() && subjectParts.length > 2) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                    return;
                }

                // LOGGEDUSER -> Validate mobile + PIN
                if (rolesFromHeader.contains("WEB-LOGGED")) {

                    if (tokenPin == null || tokenPin.isBlank()) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing PIN");
                        return;
                    }

                    boolean pinMatched = false;

                    for (Buyer user : userList) {
                        String dbPin = user.getPassword();
                        String dbmobile = user.getMobileNum();

                        if (dbPin != null && !dbPin.isBlank() && tokenPin.equals(dbPin)
                                && dbmobile != null && !dbmobile.isBlank() && dbmobile.equals(mobileNum)) {

                            pinMatched = true;
                            break;
                        }
                    }

                    if (!pinMatched) {
                        logger.error("Token PIN mismatch");

                        response.sendError(
                                HttpServletResponse.SC_UNAUTHORIZED,
                                "Token PIN mismatch"
                        );
                        return;
                    }

                    // Mobile + PIN matched
                    secret = mobileNum + tokenPin;

                } else {

                    // Non-LOGGEDUSER -> Validate mobile only
                    secret = mobileNum;
                }

                logger.info("WEB Secret Generated Successfully");
            }


            // =========================================================
            // MOBILE
            // =========================================================
            else {
                logger.info("MOBILE Platform Detected");
                if (subjectParts.length < 2) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid subject");
                    return;
                }
                String tokenDeviceId = subjectParts[1];
                if (tokenDeviceId == null || tokenDeviceId.isBlank()) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing device ID");
                    return;
                }

                String tokenPin = subjectParts.length > 2 ? subjectParts[2] : "";
                User user = sellerUserRepo.findBydeviceId(tokenDeviceId);
                if (user == null) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                    return;
                }
                String dbPin = "";
                // ---------------------------------------------------------
                // Validate PIN except GUEST
                // ---------------------------------------------------------
                if (!rolesFromHeader.contains("GUEST")) {
                    try {
                        dbPin = user.getFirstPiN();
                        if (tokenPin != null && !tokenPin.isBlank() && !tokenPin.equals(dbPin)) {
                            logger.error("Token PIN mismatch");
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token PIN mismatch");
                            return;
                        }
                    } catch (Exception ex) {
                        logger.error("PIN Validation Error", ex);
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "PIN validation failed");
                        return;
                    }
                }

                secret = (dbPin != null && !dbPin.isBlank()) ? dbPin + user.getDeviceId() : user.getDeviceId();
                logger.info("Generated MOBILE Secret");
            }
            // =========================================================
            // Parse and validate JWT
            // =========================================================
            logger.info("Secret Built Successfully");
            Claims claims = jwtService.parseClaimsAllowExpired(token, secret);
            logger.info("JWT Claims = {}", claims);
            // =========================================================
            // Expiry
            // =========================================================
            if (jwtService.isTokenExpired(claims)) {
                logger.error("JWT Expired");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
                return;
            }
            // =========================================================
            // Refresh token if required
            // =========================================================
            if (jwtService.shouldRefresh(claims)) {
                logger.info("JWT Refresh Required");
                String newToken = JwtService.generateToken(
                        claims,
                        jwtService.extractPin(token, secret),
                        jwtService.extractDeviceId(token, secret),
                        jwtService.extractEmail(token, secret)
                );
                response.setHeader("Authorization", "Bearer " + newToken);
            }

            logger.info("JWT Expiry Validation Passed");

            // =========================================================
            // ROLE VALIDATION
            // =========================================================
            String tokenRole = claims.get("role", String.class);

            logger.info("Token Role = {}", tokenRole);
            logger.info("Header Roles = {}", rolesFromHeader);

            if (tokenRole == null || tokenRole.isBlank()) {
                logger.error("JWT does not contain role");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing role in JWT");
                return;
            }

            // IMPORTANT:
            // The token role MUST exist in x-user-role header.
            if (!rolesFromHeader.contains(tokenRole)) {
                logger.error("Role mismatch. Token Role = {}, Header Roles = {}", tokenRole, rolesFromHeader);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Role mismatch");
                return;
            }

            logger.info("Role Match = true");

            // =========================================================
            // Build authorities
            // =========================================================
            List<GrantedAuthority> authorities =
                    rolesFromHeader.stream()
                            .map(role ->
                                    new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(subject, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute("claims", claims);

            logger.info("Authentication Successful");
            logger.info("Username = {}", subject);
            logger.info("Authority = ROLE_{}", tokenRole);

            filterChain.doFilter(request, response);

        } catch (JwtException | IllegalArgumentException e) {

            logger.error("JWT Validation Failed", e);

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid JWT"
            );

        } catch (Exception e) {

            logger.error("Authentication Failed", e);

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication failed"
            );

        } finally {

            logger.info("JWT FILTER END");
        }
    }


    protected boolean DontFilterThisPath(HttpServletRequest request) {
        String path = request.getRequestURI();

        return PublicPaths.isPublic(path);

    }

    private boolean redirectIfCasingMismatch(HttpServletRequest request, HttpServletResponse response) {
        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        String path = uri.startsWith(contextPath) ? uri.substring(contextPath.length()) : uri;

        String trimmed = path.startsWith("/") ? path.substring(1) : path;
        int slashIdx = trimmed.indexOf('/');
        String firstSegment = slashIdx >= 0 ? trimmed.substring(0, slashIdx) : trimmed;
        String rest = slashIdx >= 0 ? trimmed.substring(slashIdx) : "";

        String canonical = CANONICAL_SPA_PREFIXES.get(firstSegment.toLowerCase());

        if (canonical != null && !firstSegment.equals(canonical)) {
            String query = request.getQueryString();
            String redirectUrl = contextPath + "/" + canonical + rest
                    + (query != null ? "?" + query : "");

            logger.info("Case-mismatched SPA path '{}' -> redirecting to '{}'", uri, redirectUrl);

            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY); // 301
            response.setHeader("Location", redirectUrl);
            return true;
        }

        return false;
    }

    private boolean isRoleCheckRequired(String uri) {
        return RolePaths.requiresRole(uri);
    }

//    private boolean isRoleCheckRequired(String uri) {
//        return uri.equals("/api/seller/UserBehaviour")
//                || uri.equals("/api/seller/ServiceInventory")  ; // Add more if needed
//    }

}


