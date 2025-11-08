package com.DAT250Project.PollApp.security;

import com.DAT250Project.PollApp.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {
    private final User user;

    // constructor
    public UserPrincipal(User user) {
        this.user = user;
    }

    // return authorities (roles) as GrantedAuthority
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // we use a single role string from user.getRole()
        return List.of(new SimpleGrantedAuthority(user.getRole()));
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // hashed password
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // we use email as username for auth
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    // additional helper to access original User
    public User getUser() {
        return user;
    }
}

