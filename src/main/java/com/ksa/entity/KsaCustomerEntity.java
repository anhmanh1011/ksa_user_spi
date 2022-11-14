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
@Table(name = "KSA_CUSTOMER")
@Entity
public class KsaCustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "PK_KSA_CUSTOMER", nullable = false)
    private String id;
    @Column(name = "C_CUST_EMAIL", nullable = false)
    private String email;
    @Column(name = "C_CUST_FULL_NAME", nullable = false)
    private String name;
    @Column(name = "C_CUST_MOBILE", nullable = false)
    private String mobile;
    @Column(name = "C_CUSTOMER_CODE", nullable = false)
    private String customerCode;
    @Column(name = "C_STATUS", nullable = false)
    private Integer status;
}
