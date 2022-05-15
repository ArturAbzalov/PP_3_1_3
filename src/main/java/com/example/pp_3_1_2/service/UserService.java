package com.example.pp_3_1_2.service;

import com.example.pp_3_1_2.model.Role;
import com.example.pp_3_1_2.model.User;
import com.example.pp_3_1_2.repository.RoleRepository;
import com.example.pp_3_1_2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private RoleRepository roleRepository;

    public User findById(Long id){
        return userRepository.findById(id).orElse(null);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User saveUser(User user) {
        System.out.println(user+"userservice");
        if(user.getRoles().get(0).getName().equals("ROLE_ADMIN")) {
            user.getRoles().clear();
            user.addRole(roleRepository.findByName("ROLE_USER"));
            user.addRole(roleRepository.findByName("ROLE_ADMIN"));
        } else {
            user.getRoles().clear();
            user.addRole(roleRepository.findByName("ROLE_USER"));
        }

        if(findByName(user.getUsername())!=null) {
            return userRepository.save(user);
        }

        user.setPassword(encoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User findByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public User findByName(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = findByEmail(email);
        if(user == null) {
            throw new UsernameNotFoundException(String.format("User '%s' not found",email));
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(),user.getPassword(),
                getRoles(user.getRoles()));
    }
    private Collection<?extends GrantedAuthority>getRoles(Collection<Role>roles) {
        return roles.stream().map(x->new SimpleGrantedAuthority(x.getName())).collect(Collectors.toList());
    }
}
