CREATE TABLE USERS(
    ID uuid,
    EMAIL VARCHAR(255) UNIQUE NOT NULL,
    PASSWORD CHAR(60) NOT NULL,
    FIRST_NAME VARCHAR(255) NOT NULL,
    LAST_NAME VARCHAR(255),
    PHONE_NUMBER VARCHAR(255) UNIQUE,
    ENABLED boolean,
    PRIMARY KEY (ID)
);

CREATE TABLE USER_ROLES(
    ID uuid,
    NAME VARCHAR(255) UNIQUE NOT NULL,
    PRIMARY KEY (ID)
);

CREATE TABLE USERS_X_USER_ROLES(
    USER_ID uuid,
    USER_ROLE_ID uuid,
    PRIMARY KEY (USER_ID, USER_ROLE_ID),
    CONSTRAINT USERS_X_USER_ROLES_USER_ID
       FOREIGN KEY (USER_ID) REFERENCES USERS(ID),
    CONSTRAINT USERS_X_USER_ROLES_ROLE_ID
        FOREIGN KEY (USER_ROLE_ID) REFERENCES USER_ROLES(ID)
);
