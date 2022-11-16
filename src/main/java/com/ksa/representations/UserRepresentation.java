package com.ksa.representations;

import com.ksa.dao.UserDAO;
import com.ksa.model.UserDto;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JBossLog
public class UserRepresentation extends AbstractUserAdapterFederatedStorage {
    private UserDto userDto;
    private  UserDAO userDAO;

    public UserRepresentation(KeycloakSession session,
                              RealmModel realm,
                              ComponentModel storageProviderModel, UserDAO userDAO,
                              UserDto userDto) {
        super(session, realm, storageProviderModel);
        this.userDto = userDto;
        this.userDAO = userDAO;
    }

    @Override
    public String getUsername() {
        return userDto.getCustomerCode();
    }

    @Override
    public void setUsername(String username) {
//        userDto.setUserName(username);
//        userDto = userDAO.updateUser(userDto);
    }

    @Override
    public void setEmail(String email) {
//        userDto.setEmail(email);
//        userDto = userDAO.updateUser(userDto);
    }

    @Override
    public String getEmail() {
        log.info("getEmail " + userDto.getEmail());
        return userDto.getEmail();
    }

    @Override
    public void setSingleAttribute(String name, String value) {
        if (name.equals(IS_RESET_PASSWORD)) {
            userDto.setIsReset(value);
//            userDAO.updateIsReset(userDto.getCustomerCode(), Integer.parseInt(value));
        }
    }

    @Override
    public void removeAttribute(String name) {
//        if (name.equals("phone")) {
//            userDto.setPhone(null);
//        } else {
//            super.removeAttribute(name);
//        }
//        userDto = userDAO.updateUser(userDto);
    }

    @Override
    public void setAttribute(String name, List<String> values) {
        if (name.equals(IS_RESET_PASSWORD)) {
            userDto.setIsReset(values.get(0));
        }
    }

    public static String PHONE_ATTRIBUTE = "PHONE_ATTRIBUTE";
    public static String IS_RESET_PASSWORD = "IS_RESET_PASSWORD";

    @Override
    public String getFirstAttribute(String name) {
        log.info("getFirstAttribute: " + name);
        if (name.equalsIgnoreCase(PHONE_ATTRIBUTE)) {
            return userDto.getPhone();
        } else if (name.equalsIgnoreCase(IS_RESET_PASSWORD)) {
            return userDto.getIsReset();
        } else {
            return super.getFirstAttribute(name);
        }
    }

    @Override
    public Map<String, List<String>> getAttributes() {

        Map<String, List<String>> attrs = super.getAttributes();
        MultivaluedHashMap<String, String> all = new MultivaluedHashMap<>();
        all.putAll(attrs);
        all.add(PHONE_ATTRIBUTE, userDto.getPhone());
        all.add(IS_RESET_PASSWORD, userDto.getIsReset());
        log.info("getAttributes: " + all);
        return all;
    }

    @Override
    public boolean isEmailVerified() {
        return true;
    }

    @Override
    public List<String> getAttribute(String name) {
        log.info("getAttribute: " + name);

        if (name.equals(PHONE_ATTRIBUTE)) {
            return Collections.singletonList(userDto.getPhone());
        } else if (name.equals(IS_RESET_PASSWORD)) {
            return Collections.singletonList(userDto.getIsReset());
        } else {
            return super.getAttribute(name);
        }
    }

    @Override
    public String getFirstName() {
        return userDto.getFullName();
    }


    @Override
    public String getId() {
        return StorageId.keycloakId(storageProviderModel, userDto.getId());
    }

    @Override
    public Set<RoleModel> getRealmRoleMappings() {
        return super.getRealmRoleMappings();
    }
}
