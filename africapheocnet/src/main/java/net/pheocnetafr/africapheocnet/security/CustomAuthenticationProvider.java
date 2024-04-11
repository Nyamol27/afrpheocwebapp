import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (!userDetails.isEnabled()) {
            throw new DisabledException("Account is disabled");
        }

        if (!userDetails.getPassword().equals(password)) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // Authentication successful
        String firstName = ((CustomUserDetails) userDetails).getFirstName();
        String lastName = ((CustomUserDetails) userDetails).getLastName();
        String role = ((CustomUserDetails) userDetails).getRole();

        // Store user details in the session
        UserDetails sessionUserDetails = new UserDetails(firstName, lastName, email, role);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(sessionUserDetails, password, userDetails.getAuthorities()));

        // Assign roles to authenticated user
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
        return new UsernamePasswordAuthenticationToken(email, password, Collections.singletonList(authority));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
