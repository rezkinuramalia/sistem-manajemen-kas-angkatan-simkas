package com.polstat.simkas.service;

import com.polstat.simkas.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
public class MyUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = user.getRole() != null ? user.getRole().getName() : "ANGGOTA";
        System.out.println("===> Role dari user " + user.getNim() + ": " + role);
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // use nim as username
    @Override
    public String getUsername() {
        return user.getNim() != null ? user.getNim() : user.getEmail();
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return user.getAktif(); }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return user.getAktif(); }

    public Long getUserId() { return user.getId(); }
}
