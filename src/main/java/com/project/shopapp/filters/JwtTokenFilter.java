package com.project.shopapp.filters;

import com.project.shopapp.components.JwtTokenUtils;
import com.project.shopapp.models.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.modelmapper.internal.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {
    @Value("${api.prefix}")
    private String apiPrefix;
    private final JwtTokenUtils jwtTokenUtil;
    private final UserDetailsService userDetailsService;
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException
    {
        try{
            // without authentication
            if(isBypassToken(request)){
                filterChain.doFilter(request, response);
                return;
            }

            // need authentication
            final String authHeader = request.getHeader("Authorization");
            if(authHeader == null || !authHeader.startsWith("Bearer ")){
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return;
            }


            final String token = authHeader.substring(7);
            final String phoneNumner = jwtTokenUtil.extractPhoneNumber(token);

            if(phoneNumner != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null){

                // chưa đc authenticate

                // phải ép kiểu -> User để getAuthorities() method có thể gọi đến method của ta
                User userDetails =  (User) userDetailsService.loadUserByUsername(phoneNumner);
                // check phoneNumner from token and expriration
                if(jwtTokenUtil.validateToken(token, userDetails)){
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    // ROLE_user:
                                    userDetails.getAuthorities()
                            );
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource()
                            .buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
            filterChain.doFilter(request, response);

        }catch (Exception e){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(e.getMessage());
        }




    }

    private boolean isBypassToken(@NonNull HttpServletRequest request){
        final List<Pair<String, String>> bypassTokens = Arrays.asList(
                Pair.of(String.format("%s/healthcheck/health", apiPrefix), "GET"),
                Pair.of(String.format("%s/actuator/health", apiPrefix), "GET"),

                Pair.of(String.format("%s/roles", apiPrefix), "GET"),

                Pair.of(String.format("%s/products", apiPrefix), "GET"),
                Pair.of(String.format("%s/categories", apiPrefix), "GET"),
                Pair.of(String.format("%s/users/register", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/login", apiPrefix), "POST"),


                // Swagger
                Pair.of("/api-docs","GET"),
//                Pair.of("/api-docs/**","GET"),
                Pair.of("/swagger-resources","GET"),
//                Pair.of("/swagger-resources/**","GET"),
                Pair.of("/configuration/ui","GET"),
                Pair.of("/configuration/security","GET"),

//                Pair.of("/swagger-ui/**","GET"),
                Pair.of("/swagger-ui/swagger-ui.css","GET"),
                Pair.of("/swagger-ui/index.css","GET"),

                Pair.of("/swagger-ui/swagger-ui-bundle.js","GET"),
                Pair.of("/swagger-ui/swagger-initializer.js","GET"),
                Pair.of("/swagger-ui/swagger-ui-standalone-preset.js","GET"),

                Pair.of("swagger-ui/favicon-32x32.png","GET"),
                Pair.of("swagger-ui/favicon-16x16.png","GET"),

                Pair.of("/swagger-ui.html", "GET"),
                Pair.of("/swagger-ui/index.html", "GET")

        );
        // tách ra vì tránh truy cập vào /get-orders-by-keyword
        if(request.getServletPath().equals(String.format("%s/orders", apiPrefix))
                && request.getMethod().equals("GET")
        ){
            return true;
        }
        for(Pair<String, String> bypassToken: bypassTokens){
            if(request.getServletPath().contains(bypassToken.getLeft()) &&
                    request.getMethod().equals(bypassToken.getRight())){
                return true;
            }
        }
        return false;
    }
}
