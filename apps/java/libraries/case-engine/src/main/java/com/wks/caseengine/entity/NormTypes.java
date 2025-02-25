package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.*;

@Entity
@Data
@Table(name = "NormTypes")
public class NormTypes {

    @Id
    @Column(name = "Id", nullable = false)
    private int id;

    @Column(name = "NormName", nullable = false, length = 255)
    private String normName;

   

}
