package it.srv.ThermoWrapper.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "info")
@Getter
@Setter
public class Info implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "webversion", nullable = false)
    private String webversion;

    @Column(name = "toolsversion", nullable = false)
    private String toolsversion;

    @Column(name = "webextra")
    private String webextra;

    @Column(name = "toolsextra")
    private String toolsextra;

    @Column(name = "weburl")
    private String webeurl;

    @Column(name = "toolsurl")
    private String toolsurl;

    @Column(name = "lastsearch", nullable = false)
    private LocalDateTime lastsearch;

    @Column(name = "lastupdate", nullable = false)
    private LocalDateTime lastupdate;

}
