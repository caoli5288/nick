package com.mengcraft.nick;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

/**
 * Created on 16-5-6.
 */
@EqualsAndHashCode(of = "id")
@Entity
@Data
public class Nick {

    @Id
    private UUID id;

    @Column
    private String name;

    @Column(unique = true)
    private String nick;

    @Column
    private String fmt;

    @Column
    private String color;

    @Column
    private boolean hide;
}
