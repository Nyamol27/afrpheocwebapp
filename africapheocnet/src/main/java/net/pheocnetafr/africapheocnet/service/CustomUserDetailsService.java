package net.pheocnetafr.africapheocnet.service;


import net.pheocnetafr.africapheocnet.model.User;
import net.pheocnetafr.africapheocnet.repository.UserRepository;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }

        return buildUserDetails(user);
    }


    private UserDetails buildUserDetails(User user) {
        if (user == null || user.getEmail() == null || user.getPassword() == null || user.getRole() == null) {
            throw new IllegalArgumentException("User or its properties cannot be null");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                getAuthorities(user.getRole())
        );
    }


    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        return Set.of(new SimpleGrantedAuthority("ROLE_" + role))
                .stream()
                .collect(Collectors.toList());
    }
}
