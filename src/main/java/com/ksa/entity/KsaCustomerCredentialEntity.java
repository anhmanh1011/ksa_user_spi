package com.ksa.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Accessors(chain = true)
@Table(name = "KSA_CUSTOMER_CREDENTIALS")
@Entity
public class KsaCustomerCredentialEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    private String id;

    @Column(name = "CUSTOMER_CODE", nullable = false)
    private String customerCode;

    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Column(name = "IS_RESET", nullable = false)
    private String isReset;

    @Column(name = "ACTIVE", nullable = false)
    private String active;

}
