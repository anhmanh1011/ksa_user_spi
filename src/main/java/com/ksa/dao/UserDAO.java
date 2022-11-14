package com.ksa.dao;


import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//@JBossLog
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
        sql.append("and KCC.ACTIVE = 1 and ROWNUM = 1 ");


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
}
