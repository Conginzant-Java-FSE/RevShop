package com.revature.revshop.security;

import com.revature.revshop.model.Shipper;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.ShipperRepository;
import com.revature.revshop.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShipperRepository shipperRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Loading user details for email={}", email);

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    user.isActive(), // enabled
                    true, // accountNonExpired
                    true, // credentialsNonExpired
                    true, // accountNonLocked
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        }

        Optional<Shipper> shipperOpt = shipperRepository.findByEmail(email);
        if (shipperOpt.isPresent()) {
            Shipper shipper = shipperOpt.get();
            return new org.springframework.security.core.userdetails.User(
                    shipper.getEmail(),
                    shipper.getPassword() != null ? shipper.getPassword() : "",
                    true, // enabled
                    true, // accountNonExpired
                    true, // credentialsNonExpired
                    true, // accountNonLocked
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_SHIPPER")));
        }

        throw new UsernameNotFoundException("User or Shipper not found with email: " + email);
    }
}
