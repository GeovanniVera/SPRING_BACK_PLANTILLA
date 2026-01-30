package com.krouser.backend.rbac;

import com.krouser.backend.rbac.entity.Privilege;
import com.krouser.backend.rbac.entity.Role;
import com.krouser.backend.rbac.repository.PrivilegeRepository;
import com.krouser.backend.rbac.repository.RoleRepository;
import com.krouser.backend.users.entity.User;
import com.krouser.backend.users.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class RbacBootstrapService implements CommandLineRunner {

    private final PrivilegeRepository privilegeRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RbacBootstrapService(PrivilegeRepository privilegeRepository, RoleRepository roleRepository,
            UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.privilegeRepository = privilegeRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Create Privileges
        Privilege pRead = createPrivilegeIfNotFound("USERS_READ_ALL");
        Privilege pCreate = createPrivilegeIfNotFound("USERS_CREATE");
        Privilege pUpdate = createPrivilegeIfNotFound("USERS_UPDATE");
        Privilege pReadSelf = createPrivilegeIfNotFound("USERS_READ_SELF");

        // 2. Create Roles
        List<Privilege> adminPrivileges = Arrays.asList(pRead, pCreate, pUpdate, pReadSelf);
        Role adminRole = createRoleIfNotFound("ADMIN", adminPrivileges);

        List<Privilege> userPrivileges = Arrays.asList(pReadSelf);
        Role userRole = createRoleIfNotFound("USER", userPrivileges);

        // 3. Create Users
        createUserIfNotFound("admin", "admin123", Collections.singletonList(adminRole), "Admin", "System", "Admin",
                "User");
        createUserIfNotFound("user", "user123", Collections.singletonList(userRole), "User", "Normal", "User", "Test");
    }

    private Privilege createPrivilegeIfNotFound(String name) {
        return privilegeRepository.findByName(name).orElseGet(() -> {
            return privilegeRepository.save(new Privilege(name));
        });
    }

    private Role createRoleIfNotFound(String name, List<Privilege> privileges) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role role = new Role(name);
            role.setPrivileges(privileges);
            return roleRepository.save(role);
        });
    }

    private User createUserIfNotFound(String username, String password, List<Role> roles, String alias, String nombre,
            String apellidoPaterno, String apellidoMaterno) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            User user = new User();
            user.setUsername(username);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setRoles(roles);
            user.setEnabled(true);

            // Profile
            user.setAlias(alias);
            user.setNombre(nombre);
            user.setApellidoPaterno(apellidoPaterno);
            user.setApellidoMaterno(apellidoMaterno);

            // Tag Generation
            // Manual generation for bootstrap
            UUID idPublic = UUID.randomUUID();
            user.setIdPublic(idPublic);

            String uuidHex = idPublic.toString().replace("-", "");
            String suffix = uuidHex.substring(0, 6);
            user.setTag(alias + "#" + suffix);

            return userRepository.save(user);
        });
    }
}
