package org.gmalliaris.rental.rooms.repository;

import org.gmalliaris.rental.rooms.PostgresTestContainer;
import org.gmalliaris.rental.rooms.entity.UserRole;
import org.gmalliaris.rental.rooms.entity.UserRoleName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        PostgresTestContainer.DATA_JPA_TEST_JDBC_URL_PROPERTY
})
class UserRoleRepositoryTest{

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Test
    void saveTest(){
        var userRole = new UserRole();
        userRole.setName(UserRoleName.ROLE_HOST);

        userRoleRepository.saveAndFlush(userRole);
        assertNotNull(userRole.getId());
    }

    @Test
    void findByNameTest(){
        var userRole = new UserRole();
        userRole.setName(UserRoleName.ROLE_HOST);
        userRoleRepository.saveAndFlush(userRole);

        assertTrue(userRoleRepository.findByName(UserRoleName.ROLE_HOST).isPresent());
        assertFalse(userRoleRepository.findByName(UserRoleName.ROLE_ADMIN).isPresent());
    }

    @Test
    void updateTest_immutable(){
        var userRole = new UserRole();
        userRole.setName(UserRoleName.ROLE_HOST);
        userRoleRepository.saveAndFlush(userRole);

        userRole.setName(UserRoleName.ROLE_GUEST);
        userRoleRepository.save(userRole);

        assertFalse(userRoleRepository.findByName(UserRoleName.ROLE_GUEST).isPresent());

        var userRoleInDb = userRoleRepository.findByName(UserRoleName.ROLE_HOST);
        assertTrue(userRoleInDb.isPresent());
        assertEquals(userRole, userRoleInDb.get());
    }
}