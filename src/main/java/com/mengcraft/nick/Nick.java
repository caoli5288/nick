package com.mengcraft.nick;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.UUID;

/**
 * Created on 16-5-6.
 */
@Entity @Getter @Setter public class Nick {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String nick;

    @Column(nullable = false)
    private String fmt;

    @Column(nullable = false)
    private String color;

    private boolean hide;

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        return getClass() == o.getClass() && id.equals(((Nick) o).id);
    }

}
