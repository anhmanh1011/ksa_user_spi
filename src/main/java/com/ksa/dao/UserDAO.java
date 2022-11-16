package com.ksa.dao;


import com.ksa.entity.KsaCustomerCredentialEntity;
import com.ksa.entity.KsaCustomerEntity;
import com.ksa.model.UserDto;
import com.ksa.utils.ConvertUtils;
import lombok.extern.jbosslog.JBossLog;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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


        log.info("query: " + sql);
        Query nativeQuery = entityManager.createNativeQuery(sql.toString());
        try {
            Object singleResult = nativeQuery.getSingleResult();
            log.info("singleResult: " + singleResult);
            return singleResult != null;
        } catch (NoResultException e) {
            log.error(e);
            return false;
        }

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

    public void updateIsReset(String customerCode, int isReset) {
        Query nativeQuery = entityManager.createNativeQuery("update KSA_CUSTOMER_CREDENTIALS set IS_RESET = :is_reset WHERE CUSTOMER_CODE = :customerCode ");
        nativeQuery.setParameter("is_reset", isReset);
        nativeQuery.setParameter("customerCode", customerCode);
        int i = nativeQuery.executeUpdate();
        log.info("exec update isreset password: " + i);
    }

    public List<UserDto> findAll() {
        TypedQuery<KsaCustomerEntity> query = entityManager.createQuery("SELECT KC FROM KsaCustomerEntity KC WHERE KC.status = 1", KsaCustomerEntity.class);
        List<KsaCustomerEntity> resultList = query.getResultList();
        if (resultList == null || resultList.isEmpty())
            return new ArrayList<>();
        return resultList.stream().map(ConvertUtils::convertKsaCustomerEntityToUserDto).collect(Collectors.toList());
    }

    public int size() {
        return 100;
    }

    public Optional<UserDto> getUserById(String id) {
        log.info("getUserById: " + id);
        Query query = entityManager.createQuery("SELECT KC, KCC FROM KsaCustomerEntity KC inner join KsaCustomerCredentialEntity KCC on KC.customerCode = KCC.customerCode WHERE KC.id = :id");
        query.setParameter("id", id);
        Object[] resultList = (Object[]) query.getSingleResult();
        return getUserDtoFromResultList(resultList);
    }

    public List<UserDto> searchForUserByUsernameOrEmail(String search) {
        log.info("searchForUserByUsernameOrEmail: " + search);
        Query query = entityManager.createQuery("SELECT KC, KCC FROM KsaCustomerEntity KC inner join KsaCustomerCredentialEntity KCC on KC.customerCode = KCC.customerCode WHERE KC.email = :email or KC.customerCode = :customerCode ");
        query.setParameter("email", search);
        query.setParameter("customerCode", search);

        List resultList = query.getResultList();
        List<UserDto> userDtos = new ArrayList<>();
        for (Object o : resultList) {
            Optional<UserDto> userDtoFromResultList = getUserDtoFromResultList((Object[]) o);
            userDtos.add(userDtoFromResultList.get());
        }
        return userDtos;
    }

    public Optional<UserDto> getUserByUsername(String username) {
        log.info("getUserByUsername: " + username);

        Matcher matcherMobile = PATTERN_MOBILE.matcher(username);
        Matcher matcherCustomerCode = PATTERN_CUSTOMER_CODE.matcher(username);
        Matcher matcherEmail = PATTERN_EMAIL.matcher(username);
        StringBuilder sql = new StringBuilder("SELECT KC, KCC FROM KsaCustomerEntity KC inner join KsaCustomerCredentialEntity KCC on KC.customerCode = KCC.customerCode WHERE KC.status = 1 and KCC.active = 1 ");
        if (matcherMobile.find())
            sql.append("    and KC.mobile = :username ");
        else if (matcherCustomerCode.find()) {
            sql.append("    and KC.customerCode = :username ");
        } else if (matcherEmail.find()) {
            sql.append("    and KC.email = :username ");
        }
        Query query = entityManager.createQuery(sql.toString());

        query.setParameter("username", username);
        try {
            Object[] singleResult = (Object[]) query.getSingleResult();
            return getUserDtoFromResultList(singleResult);
        } catch (NoResultException ex) {
            log.info("login failed");
            return Optional.empty();
        } catch (Exception ex){
            log.info("login error");
            log.error(ex);
            return Optional.empty();
        }
    }

    private Optional<UserDto> getUserDtoFromResultList(Object[] resultList) {
        KsaCustomerEntity ksaCustomerEntity = (KsaCustomerEntity) resultList[0];
        KsaCustomerCredentialEntity ksaCustomerCredentialEntity = (KsaCustomerCredentialEntity) resultList[1];

        UserDto userDto = ConvertUtils.convertKsaCustomerEntityToUserDto(ksaCustomerEntity);
        if(userDto!= null) {
            userDto.setIsReset(ksaCustomerCredentialEntity.getIsReset());
        }
        return Optional.of(userDto);
    }

    public Optional<UserDto> getUserByEmail(String email) {
        log.info("getUserByEmail: " + email);
        return getUserByUsername(email);
    }
}
