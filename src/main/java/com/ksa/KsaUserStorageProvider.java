package com.ksa;

import com.ksa.dao.UserDAO;
import com.ksa.model.UserDto;
import com.ksa.representations.UserRepresentation;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.*;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class KsaUserStorageProvider implements UserStorageProvider,
        UserLookupProvider,
        UserQueryProvider,
        CredentialInputUpdater,
        CredentialInputValidator {
    private static final Logger log = Logger.getLogger(KsaUserStorageProvider.class);

    protected ComponentModel model;

    protected KeycloakSession session;

    private final UserDAO userDAO;

    protected EntityManager em;

    KsaUserStorageProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
        em = session.getProvider(JpaConnectionProvider.class, "ksa-user-store").getEntityManager();
        userDAO = new UserDAO(em);
    }


    @Override
    public void close() {
        userDAO.close();
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        log.info("isConfiguredFor(" + realm + ", " + user + ", " + credentialType + ")");
        return supportsCredentialType(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
            return false;
        }
        UserCredentialModel cred = (UserCredentialModel) input;
        return userDAO.validateCredentials(user.getUsername(), cred.getChallengeResponse());
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        log.info("supportsCredentialType(" + credentialType + ")");
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean updateCredential(RealmModel realm, UserModel userModel, CredentialInput input) {
        return true;
    }

    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {
        log.info("disableCredentialType(" + realm + ", " + user + ", " + credentialType + ")");
    }

    @Override
    public Set<String> getDisableableCredentialTypes(RealmModel realm, UserModel user) {
        return Collections.emptySet();
    }


    @Override
    public int getUsersCount(RealmModel realm) {
        log.info("getUsersCount(" + realm + ")");
        return userDAO.size();
    }

    @Override
    public List<UserModel> getUsers(RealmModel realm) {
        log.info("getUsers(" + realm + ")");
        return userDAO.findAll()
                .stream()
                .map(user -> getUserRepresentation(user,realm))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserModel> getUsers(RealmModel realm, int firstResult, int maxResults) {
        log.info("getUsers(RealmModel realm, int firstResult, int maxResults)");
        return new ArrayList<>();
    }

    @Override
    public List<UserModel> searchForUser(String search, RealmModel realm) {
        log.info("searchForUser(String search, RealmModel realm)");
        return userDAO.searchForUserByUsernameOrEmail(search)
                .stream()
                .map(user -> getUserRepresentation(user, realm))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserModel> searchForUser(String search, RealmModel realm, int firstResult, int maxResults) {
        return new ArrayList<>();
    }

    @Override
    public List<UserModel> searchForUser(Map<String, String> params, RealmModel realm) {
        return new ArrayList<>();
    }

    @Override
    public List<UserModel> searchForUser(Map<String, String> params, RealmModel realm, int firstResult,
                                         int maxResults) {
        return new ArrayList<>();
    }

    @Override
    public List<UserModel> getGroupMembers(RealmModel realm, GroupModel group, int firstResult, int maxResults) {
        // TODO Will probably never implement
        return new ArrayList<>();
    }

    @Override
    public List<UserModel> getGroupMembers(RealmModel realm, GroupModel group) {
        // TODO Will probably never implement
        return new ArrayList<>();
    }

    @Override
    public List<UserModel> searchForUserByUserAttribute(String attrName, String attrValue, RealmModel realm) {

        return new ArrayList<>();
    }

    @Override
    public UserModel getUserById(String keycloakId, RealmModel realm) {
        // keycloakId := keycloak internal id; needs to be mapped to external id
        log.info("getUserById(String keycloakId, RealmModel realm)" + keycloakId);
        String id = StorageId.externalId(keycloakId);
        Optional<UserDto> userById = userDAO.getUserById(id);
        return userById.map(userDto -> getUserRepresentation(userDto, realm)).orElse(null);
    }

    public UserRepresentation getUserRepresentation(UserDto user, RealmModel realm) {
        return new UserRepresentation(session, realm, model, user);
    }

    @Override
    public UserModel getUserByUsername(String username, RealmModel realm) {
        log.info("getUserByUsername(String username, RealmModel realm)");
        Optional<UserDto> userByUsername = userDAO.getUserByUsername(username);
        return userByUsername.map(userDto -> getUserRepresentation(userDto, realm)).orElse(null);
    }

    @Override
    public UserModel getUserByEmail(String email, RealmModel realm) {
        log.info("getUserByEmail(String email, RealmModel realm)");
        Optional<UserDto> userByEmail = userDAO.getUserByEmail(email);
        return userByEmail.map(userDto -> getUserRepresentation(userDto, realm)).orElse(null);
    }
}
