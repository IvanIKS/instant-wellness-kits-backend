package ua.trinity.iwk.backend.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name  = oAuth2User.getAttribute("name");

        // Find existing user by email or create a new admin user
        userRepository.findByEmail(email).orElseGet(() -> {
            String username = (name != null ? name.replaceAll("\\s+", ".").toLowerCase() : "user")
                    + "." + UUID.randomUUID().toString().substring(0, 6);
            User newUser = new User(username, email, null);
            return userRepository.save(newUser);
        });

        return oAuth2User;
    }
}

