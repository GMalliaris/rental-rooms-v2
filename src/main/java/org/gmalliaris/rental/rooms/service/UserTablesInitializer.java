package org.gmalliaris.rental.rooms.service;

import org.gmalliaris.rental.rooms.config.exception.ApiException;
import org.gmalliaris.rental.rooms.config.exception.ApiExceptionMessageConstants;
import org.gmalliaris.rental.rooms.entity.AccountUser;
import org.gmalliaris.rental.rooms.entity.UserRole;
import org.gmalliaris.rental.rooms.entity.UserRoleName;
import org.gmalliaris.rental.rooms.repository.AccountUserRepository;
import org.gmalliaris.rental.rooms.repository.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;

@Component
public class UserTablesInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(UserTablesInitializer.class);

    @Value("${default.admin.email:admin@example.eg}")
    private String adminEmail;
    @Value("${default.admin.password:12345678}")
    private String adminPassword;

    private final AccountUserRepository accountUserRepository;
    private final UserRoleRepository userRoleRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserTablesInitializer(AccountUserRepository accountUserRepository, UserRoleRepository userRoleRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.accountUserRepository = accountUserRepository;
        this.userRoleRepository = userRoleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        logger.info("Running user roles and admin user initializer.");

        var userRoles = initUserRoles();

        if (accountUserRepository.countAdminAccountUsers() == 0){
            initAdminUser(userRoles);
        }
    }

    private HashSet<UserRole> initUserRoles() {
        var userRoles = new HashSet<UserRole>();
        Arrays.stream(UserRoleName.values())
                .forEach( name -> {
                    var roleOptional = userRoleRepository.findByName(name);
                    if (roleOptional.isPresent()){
                        userRoles.add(roleOptional.get());
                    }
                    else{
                        var userRole = new UserRole();
                        userRole.setName(name);
                        userRoleRepository.save(userRole);
                        userRoles.add(userRole);

                        logger.debug("Created user role {}.", name);
                    }
                });
        return userRoles;
    }

    private void initAdminUser(HashSet<UserRole> userRoles) {
        var adminRole = userRoles.stream()
                .filter(role -> UserRoleName.ROLE_ADMIN == role.getName())
                .findFirst()
                .orElseThrow(() -> {
                    throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                            ApiExceptionMessageConstants.USER_TABLES_INIT_ERROR);
                });

        var adminUser = new AccountUser();
        adminUser.setEmail("admin@example.eg");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("instrator");
        var encodedPassword = bCryptPasswordEncoder
                .encode(adminPassword);
        adminUser.setPassword(encodedPassword);
        adminUser.addRole(adminRole);
        adminUser.setEnabled(true);
        accountUserRepository.save(adminUser);

        logger.debug("Created new admin user with email '{}' and password '{}'",
                adminEmail, adminPassword);
    }
}
