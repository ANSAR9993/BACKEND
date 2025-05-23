package com.dgapr.demo.Security;

import com.dgapr.demo.Security.JwtTokenProvider;
import com.dgapr.demo.Repository.UserRepository;
import com.dgapr.demo.Service.CustomUserDetailsService;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest req,
                                    @Nonnull HttpServletResponse res,
                                    @Nonnull FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader(JwtTokenProvider.AUTH_HEADER);
        if (header != null && header.startsWith("Bearer ")) {
            String rawToken = header.substring("Bearer ".length()).trim();

            if (tokenProvider.validateToken(rawToken)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                String username   = tokenProvider.getUsernameFromJwt(rawToken);
                long   jwtVersion = tokenProvider.getTokenVersionFromJwt(rawToken);

                userRepository.findByUsername(username).ifPresent(appUser -> {
                    if (appUser.getTokenVersion().equals(jwtVersion)) {
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

        chain.doFilter(req, res);
    }
}
