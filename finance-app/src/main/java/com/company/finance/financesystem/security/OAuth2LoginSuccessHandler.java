package com.company.finance.financesystem.security;

import com.company.finance.financesystem.domain.entity.Role;
import com.company.finance.financesystem.domain.entity.User;
import com.company.finance.financesystem.repository.RoleRepository;
import com.company.finance.financesystem.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String DEFAULT_ROLE = "AUDITOR";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewUser(email, oauthUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format(
                "{\"accessToken\":\"%s\",\"refreshToken\":\"%s\",\"tokenType\":\"Bearer\"}",
                accessToken, refreshToken
        ));
    }

    private User createNewUser(String email, OAuth2User oauthUser) {
        Role role = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new IllegalStateException("Default role not found: " + DEFAULT_ROLE));

        String fullName = oauthUser.getAttribute("name");
        if (fullName == null || fullName.isBlank()) {
            fullName = email;
        }

        User newUser = User.builder()
                .email(email)
                .fullName(fullName)
                .passwordHash(null)
                .enabled(true)
                .roles(Set.of(role))
                .build();

        return userRepository.save(newUser);
    }
}