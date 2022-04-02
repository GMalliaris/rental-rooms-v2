package org.gmalliaris.rental.rooms.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "CONFIRMATION_TOKEN")
public class ConfirmationToken extends DefaultPersistable{

    @Column(name = "EXPIRATION_DATE", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private ConfirmationTokenStatus status;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private AccountUser accountUser;

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public ConfirmationTokenStatus getStatus() {
        return status;
    }

    public void setStatus(ConfirmationTokenStatus status) {
        this.status = status;
    }

    public AccountUser getAccountUser() {
        return accountUser;
    }

    public void setAccountUser(AccountUser accountUser) {
        this.accountUser = accountUser;
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
        return Objects.hash(super.hashCode(), expirationDate, accountUser);
    }
}
