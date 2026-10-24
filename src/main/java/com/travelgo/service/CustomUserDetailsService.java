package com.travelgo.service;

import com.travelgo.entity.User;
import com.travelgo.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Custom UserDetailsService for Spring Security.
 * Loads user from MySQL by email and maps role to a GrantedAuthority.
 * Also checks if the account is active before allowing login.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Injects UserRepository to fetch user records from the database.
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Core authentication method called automatically by Spring Security whenever a user submits the login form.
     * 
     *    because Spring Security's hasRole() expects this naming convention.
     * 4. Constructs a Spring Security UserDetails object:
     *    - Passes user email as username.
     *    - Passes the BCrypt-hashed password.
     *    - Passes user.isActive() as the 'enabled' flag (if false, deactivated accounts cannot log in!).
     *    - Sets account non-expired, credentials non-expired, and non-locked to true.
     *    - Attaches the granted authority list.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Map role to Spring Security authority (e.g., ROLE_ADMIN, ROLE_CUSTOMER)
        String authority = "ROLE_" + user.getRole().getRoleName();

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isActive(),       // enabled (inactive users cannot login)
                true,                  // accountNonExpired
                true,                  // credentialsNonExpired
                true,                  // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority(authority))
        );
    }
}
