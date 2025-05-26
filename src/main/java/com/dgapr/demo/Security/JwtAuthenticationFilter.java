package com.dgapr.demo.Security;

import com.dgapr.demo.Repository.UserRepository;
import com.dgapr.demo.Service.CustomUserDetailsService;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Spring Security filter that:
 *  1. Reads the Authorization header
 *  2. Validates the JWT
 *  3. Loads the UserDetails if the token version matches
 *  4. Populates the SecurityContext
 */
/**
 * {@code JwtAuthenticationFilter} is a Spring Security filter responsible for processing JSON Web Tokens (JWTs)
 * received in the Authorization header of incoming HTTP requests. It extends {@link OncePerRequestFilter}
 * to ensure it's executed only once per request.
 *
 * <p>The primary responsibilities of this filter include:</p>
 * <ol>
 * <li>**Extracting the JWT:** It parses the `Authorization` header to retrieve the "Bearer" token.</li>
 * <li>**Validating the JWT:** It uses {@link JwtTokenProvider} to validate the token's signature and expiration.</li>
 * <li>**Token Version Check (Revocation):** A critical security feature is the comparison of the JWT's embedded
 * token version with the token version stored for the user in the database. This mechanism allows
 * for immediate token revocation (e.g., when an administrator explicitly revokes their tokens).
 * If the versions do not match, the token is considered revoked and authentication is denied.</li>
 * <li>**Loading User Details:** If the token is valid and not revoked, it loads the user's
 * {@link UserDetails} using {@link CustomUserDetailsService}.</li>
 * <li>**Populating the Security Context:** Finally, it creates an {@link UsernamePasswordAuthenticationToken}
 * and sets it in the {@link SecurityContextHolder}, thereby authenticating the user for the current request
 * within Spring Security's framework.</li>
 * </ol>
 *
 * <p>This filter plays a crucial role in the stateless authentication flow of the application,
 * ensuring that only requests with valid and active JWTs are processed as authenticated.</p>
 *
 * @see JwtTokenProvider
 * @see CustomUserDetailsService
 * @see UserRepository
 * @see SecurityContextHolder
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final com.dgapr.demo.Security.JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;

    /**
     * Performs the actual filtering logic for each incoming HTTP request.
     *
     * @param req The {@link HttpServletRequest} being processed.
     * @param res The {@link HttpServletResponse} to which the response is sent.
     * @param chain The {@link FilterChain} to proceed with the next filter in the chain.
     * @throws ServletException If a servlet-specific error occurs.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest req,
                                    @Nonnull HttpServletResponse res,
                                    @Nonnull FilterChain chain)
            throws ServletException, IOException {

        // 1. Extract the Authorization header
        String header = req.getHeader(com.dgapr.demo.Security.JwtTokenProvider.AUTH_HEADER);

        // Check if the header exists and starts with "Bearer "
        if (header != null && header.startsWith("Bearer ")) {
            String rawToken = header.substring("Bearer ".length()).trim();

            // 2. Validate the JWT and ensure no authentication is already set for the current context
            if (tokenProvider.validateToken(rawToken)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                String username   = tokenProvider.getUsernameFromJwt(rawToken);
                long   jwtVersion = tokenProvider.getTokenVersionFromJwt(rawToken);

                // Attempt to find the user in the database
                userRepository.findByUsername(username).ifPresent(appUser -> {
                    // 3. Perform Token Version Check (Revocation mechanism)
                    if (appUser.getTokenVersion().equals(jwtVersion)) {
                        // If token version matches, load UserDetails and authenticate
                        UserDetails ud = userDetailsService.loadUserByUsername(username);
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.debug("Authenticated '{}' v={}", username, jwtVersion);
                    } else {
                        log.warn("Revoked token: jwtV={} dbV={}", jwtVersion, appUser.getTokenVersion());
                    }
                });
            }
        }
        // Proceed to the next filter in the chain
        chain.doFilter(req, res);
    }
}
