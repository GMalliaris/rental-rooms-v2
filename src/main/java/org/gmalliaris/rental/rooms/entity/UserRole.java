package org.gmalliaris.rental.rooms.entity;

import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Immutable
@Table(name = "USER_ROLES")
public class UserRole extends DefaultPersistable{

    @Column(name = "NAME", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRoleName name;

    public UserRoleName getName() {
        return name;
    }

    public void setName(UserRoleName name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }
}
