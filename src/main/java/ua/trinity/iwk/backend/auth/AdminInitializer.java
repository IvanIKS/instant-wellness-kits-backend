package ua.trinity.iwk.backend.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.findById("admin").isEmpty()) {
            User admin = new User("admin", "admin@example.com", passwordEncoder.encode("admin"));
            userRepository.save(admin);
            log.info("Default admin user created. Username: admin, Password: admin");
        }
    }
}

