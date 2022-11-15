package com.ksa.dao;


import com.ksa.entity.KsaCustomerEntity;
import com.ksa.model.UserDto;
import com.ksa.utils.ConvertUtils;
import lombok.extern.jbosslog.JBossLog;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@JBossLog
public class UserDAO {

    public static final String STR_PT_MOBILE = "^0\\d{9}$";
    public static final String STR_PT_047C = "^047C\\d{6}$";
    public static final String STR_PT_MAIL = "^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+$";
    public static final String STR_PT_CUSTOMER_CODE = "^\\d{6}$";
    public static final Pattern PATTERN_MOBILE = Pattern.compile(STR_PT_MOBILE);
    public static final Pattern PATTERN_047C = Pattern.compile(STR_PT_047C);
    public static final Pattern PATTERN_EMAIL = Pattern.compile(STR_PT_MAIL);
    public static final Pattern PATTERN_CUSTOMER_CODE = Pattern.compile(STR_PT_CUSTOMER_CODE);

    private EntityManager entityManager;

    public UserDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    public void close() {
        entityManager.close();
    }

    public boolean validateCredentials(String username, String challengeResponse) {
        StringBuilder sql = new StringBuilder("SELECT 1 " +
                " from KSA_CUSTOMER_CREDENTIALS KCC " +
                " WHERE KCC.CUSTOMER_CODE = (SELECT KU.C_CUSTOMER_CODE " +
                "                            from KSA_CUSTOMER KU " +
                "                            where  KU.C_STATUS = 1 ");
//                " and KCC.ACTIVE = 1 and ROWNUM =1 ";
        if (PATTERN_MOBILE.matcher(username).find()) {
            sql.append(" and KU.C_CUST_MOBILE = '").append(username).append("') ");
        } else if (PATTERN_EMAIL.matcher(username).find()) {
            sql.append(" and KU.C_CUST_EMAIL = '").append(username).append("') ");
        } else if (PATTERN_CUSTOMER_CODE.matcher(username).find()) {
            sql.append(" and KU.C_CUSTOMER_CODE = '").append(username).append("') ");
        } else return false;
        sql.append("      and KCC.ACTIVE = 1 and KCC.PASSWORD='").append(challengeResponse).append("'  and ROWNUM = 1 ");


        Query nativeQuery = entityManager.createNativeQuery(sql.toString());
        int maxResults = nativeQuery.getMaxResults();
        return maxResults > 0;
    }

    public static void main(String[] args) {
        String input = "daomanh2810@aaa.hoy";
        Matcher matcher = PATTERN_MOBILE.matcher(input);
        Matcher pattern_047C = PATTERN_047C.matcher(input);
        Matcher matcherEmail = PATTERN_EMAIL.matcher(input);
        if (matcher.find())
            System.out.println(" is phone number");
        else if (pattern_047C.find()) {
            System.out.println(" is pattern_047C");
        } else if (matcherEmail.find()) {
            System.out.println(" is matcherEmail");

        }

    }

    public void updateIsReset(String customerCode){
        Query nativeQuery = entityManager.createNativeQuery("update KSA_CUSTOMER_CREDENTIALS set IS_RESET = 0 WHERE CUSTOMER_CODE = '" + customerCode + "' ");
        int i = nativeQuery.executeUpdate();
    }

    public List<UserDto> findAll() {
        TypedQuery<KsaCustomerEntity> query = entityManager.createQuery("SELECT KC FROM KsaCustomerEntity KC WHERE KC.status = 1", KsaCustomerEntity.class);
        List<KsaCustomerEntity> resultList = query.getResultList();
        if(resultList == null || resultList.isEmpty())
            return new ArrayList<>();
        return resultList.stream().map(ConvertUtils::convertKsaCustomerEntityToUserDto).collect(Collectors.toList());
    }

    public int size() {
        return 100;
    }

    public Optional<UserDto> getUserById(String id) {
        log.info("getUserById: " + id);
        TypedQuery<KsaCustomerEntity> query = entityManager.createQuery("SELECT KC FROM KsaCustomerEntity KC WHERE KC.id = :id", KsaCustomerEntity.class);
        query.setParameter("id",id);
        KsaCustomerEntity singleResult = query.getSingleResult();
        return Optional.ofNullable(ConvertUtils.convertKsaCustomerEntityToUserDto(singleResult));
    }

    public List<UserDto> searchForUserByUsernameOrEmail(String search) {
        log.info("searchForUserByUsernameOrEmail: " + search);
        TypedQuery<KsaCustomerEntity> query = entityManager.createQuery("SELECT KC FROM KsaCustomerEntity KC WHERE KC.email = :email or KC.customerCode = :customerCode ", KsaCustomerEntity.class);

        query.setParameter("email",search);
        query.setParameter("customerCode",search);
        List<KsaCustomerEntity> resultList = query.getResultList();
        if(resultList == null || resultList.isEmpty())
            return new ArrayList<>();
        return resultList.stream().map(ConvertUtils::convertKsaCustomerEntityToUserDto).collect(Collectors.toList());
    }

    public Optional<UserDto> getUserByUsername(String username) {
        log.info("getUserByUsername: " + username);

        Matcher matcherMobile = PATTERN_MOBILE.matcher(username);
        Matcher matcherCustomerCode = PATTERN_CUSTOMER_CODE.matcher(username);
        Matcher matcherEmail = PATTERN_EMAIL.matcher(username);
        StringBuilder sql = new StringBuilder("SELECT KC FROM KsaCustomerEntity KC WHERE KU.C_STATUS = 1 ");
        if (matcherMobile.find())
            sql.append("    and KU.mobile = :username ");
        else if (matcherCustomerCode.find()) {
            sql.append("    and KU.customerCode = :username ");
        } else if (matcherEmail.find()) {
            sql.append("    and KU.email = :username ");
        }
        TypedQuery<KsaCustomerEntity> query = entityManager.createQuery(sql.toString(), KsaCustomerEntity.class);
        query.setParameter("username", username);
        try {
            KsaCustomerEntity singleResult = query.getSingleResult();
            return Optional.of(ConvertUtils.convertKsaCustomerEntityToUserDto(singleResult));
        }catch (Exception ex){
            return Optional.empty();
        }
    }

    public Optional<UserDto> getUserByEmail(String email) {
        log.info("getUserByEmail: " + email);

        StringBuilder sql = new StringBuilder("SELECT KC FROM KsaCustomerEntity KC WHERE KU.C_STATUS = 1 KC.email = :email ");
        TypedQuery<KsaCustomerEntity> query = entityManager.createQuery(sql.toString(), KsaCustomerEntity.class);
        query.setParameter("email", email);
        try {
            KsaCustomerEntity singleResult = query.getSingleResult();
            return Optional.of(ConvertUtils.convertKsaCustomerEntityToUserDto(singleResult));
        }catch (Exception ex){
            return Optional.empty();
        }
    }
}
