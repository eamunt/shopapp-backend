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



        }catch (Exception e){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }
        filterChain.doFilter(request, response);



    }

    private boolean isBypassToken(@NonNull HttpServletRequest request){
        final List<Pair<String, String>> bypassTokens = Arrays.asList(
                Pair.of(String.format("%s/products", apiPrefix), "GET"),
                Pair.of(String.format("%s/categories", apiPrefix), "GET"),
                Pair.of(String.format("%s/users/login", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/register", apiPrefix), "POST"),
                Pair.of(String.format("%s/roles", apiPrefix), "GET")
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
