package org.gmalliaris.rental.rooms.repository;

import org.gmalliaris.rental.rooms.entity.AccountUser;
import org.gmalliaris.rental.rooms.entity.UserRole;
import org.gmalliaris.rental.rooms.entity.UserRoleName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AccountUserRepositoryTest {

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private AccountUserRepository accountUserRepository;

    @Test
    void saveTest_emailNotNull(){

        var user = new AccountUser();
        user.setPassword("12345678");
        user.setFirstName("firstName");

        assertThrows(DataIntegrityViolationException.class,
                () -> accountUserRepository.saveAndFlush(user));
    }

    @Test
    void saveTest_passwordNotNull(){

        var user = new AccountUser();
        user.setEmail("test@example.eg");
        user.setFirstName("firstName");

        assertThrows(DataIntegrityViolationException.class,
                () -> accountUserRepository.saveAndFlush(user));
    }

    @Test
    void saveTest_firstNameNotNull(){

        var user = new AccountUser();
        user.setPassword("12345678");
        user.setFirstName("firstName");

        assertThrows(DataIntegrityViolationException.class,
                () -> accountUserRepository.saveAndFlush(user));
    }

    @Test
    void saveTest(){

        var user = new AccountUser();
        user.setEmail("test@example.eg");
        user.setPassword("12345678");
        user.setFirstName("firstName");

        accountUserRepository.saveAndFlush(user);
        assertNotNull(user.getId());
    }

    @Test
    void saveTest_emailUnique(){

        var commonEmail = "test@example.eg";

        var user = new AccountUser();
        user.setEmail(commonEmail);
        user.setPassword("12345678");
        user.setFirstName("firstName");

        accountUserRepository.saveAndFlush(user);

        var secondUser = new AccountUser();
        secondUser.setEmail(commonEmail);
        secondUser.setPassword("87654321");
        secondUser.setFirstName("first_name");

        assertThrows(DataIntegrityViolationException.class,
                () -> accountUserRepository.saveAndFlush(secondUser));
    }

    @Test
    void saveTest_phoneNumberUnique(){

        String commonPhoneNumber = "12345678";

        var user = new AccountUser();
        user.setEmail("test@example.eg");
        user.setPassword("12345678");
        user.setFirstName("firstName");
        user.setPhoneNumber(commonPhoneNumber);

        accountUserRepository.saveAndFlush(user);

        var secondUser = new AccountUser();
        secondUser.setEmail("test2@example.eg");
        secondUser.setPassword("87654321");
        secondUser.setFirstName("first_name");
        secondUser.setPhoneNumber(commonPhoneNumber);

        assertThrows(DataIntegrityViolationException.class,
                () -> accountUserRepository.saveAndFlush(secondUser));
    }

    @Test
    void findByEmailTest(){

        var email = "test@example.eg";

        var user = new AccountUser();
        user.setEmail(email);
        user.setPassword("12345678");
        user.setFirstName("firstName");
        accountUserRepository.saveAndFlush(user);

        var optionalUser = accountUserRepository.findByEmail(email);
        assertNotNull(optionalUser);
        assertTrue(optionalUser.isPresent());
        assertEquals(user, optionalUser.get());

        var notExistingEmail = "test@test.test";
        assertFalse(accountUserRepository.findByEmail(notExistingEmail).isPresent());
    }

    @Test
    void countByEmailTest(){

        var email = "test@example.eg";
        var notExistingEmail = "test@test.test";

        var user = new AccountUser();
        user.setEmail(email);
        user.setPassword("12345678");
        user.setFirstName("firstName");
        accountUserRepository.saveAndFlush(user);

        assertEquals(1, accountUserRepository.countByEmail(email));
        assertEquals(0, accountUserRepository.countByEmail(notExistingEmail));
    }

    @Test
    void countByPhoneNumberTest(){

        var phoneNumber = "123456789";
        var notExistingPhoneNumber = "123456788";

        var user = new AccountUser();
        user.setEmail("test@example.eg");
        user.setPassword("12345678");
        user.setFirstName("firstName");
        user.setPhoneNumber(phoneNumber);
        accountUserRepository.saveAndFlush(user);

        assertEquals(1, accountUserRepository
                .countByPhoneNumber(phoneNumber));
        assertEquals(0, accountUserRepository
                .countByPhoneNumber(notExistingPhoneNumber));
    }

    @Test
    void countAdminAccountUsersTest(){

        var userRole = new UserRole();
        userRole.setName(UserRoleName.ROLE_ADMIN);
        userRoleRepository.saveAndFlush(userRole);

        var firstAdmin = new AccountUser();
        firstAdmin.setEmail("test1@example.eg");
        firstAdmin.setPassword("12345678");
        firstAdmin.setFirstName("firstName");
        firstAdmin.addRole(userRole);
        accountUserRepository.saveAndFlush(firstAdmin);

        var secondAdmin = new AccountUser();
        secondAdmin.setEmail("test2@example.eg");
        secondAdmin.setPassword("12345678");
        secondAdmin.setFirstName("firstName");
        secondAdmin.addRole(userRole);
        accountUserRepository.saveAndFlush(secondAdmin);

        assertEquals(2, accountUserRepository.countAdminAccountUsers());
    }
}