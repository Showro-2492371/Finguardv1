package org.cts.adm.finguard.Jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtUtil.isTokenValid(token)) {

                String username = jwtUtil.extractUsername(token);
                Long customerId = jwtUtil.extractCustomerId(token);
                String role = jwtUtil.extractRole(token);

                SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority(role);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                new JwtUser(username, customerId),
                                null,
                                Collections.singletonList(authority)
                        );

                SecurityContextHolder.getContext()
                        .setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }
}

//
//public class JwtFilter extends OncePerRequestFilter {
//
//   private final JwtUtil jwtUtil;
//
//   public JwtFilter(JwtUtil jwtUtil){
//       this.jwtUtil = jwtUtil;
//   }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain)
//            throws ServletException, IOException {
//
//        String header = request.getHeader("Authorization");
//
//        if (header != null && header.startsWith("Bearer ")) {
//            String token = header.substring(7);
//
//            if (jwtUtil.isTokenValid(token)) {
//                String username = jwtUtil.extractUsername(token);
//
//                UsernamePasswordAuthenticationToken auth =
//                        new UsernamePasswordAuthenticationToken(
//                                username, null, new ArrayList<>());
//                SecurityContextHolder.getContext().setAuthentication(auth);
//            }
//        }
//        filterChain.doFilter(request,response);
//
//    }
//}
