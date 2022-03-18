package org.gmalliaris.rental.rooms.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.Objects;
import java.util.UUID;

@MappedSuperclass
public abstract class DefaultPersistable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id")
    private UUID id;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public boolean isNew(){
        return id == null;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultPersistable)){
            return false;
        }
        var that = (DefaultPersistable) o;
        if (id == null){
            return false;
        }
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
