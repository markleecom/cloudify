package org.cloudifysource.rest.security;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.ppolicy.PasswordPolicyControl;
import org.springframework.security.ldap.ppolicy.PasswordPolicyResponseControl;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.util.Assert;

/**
 * A custom implementation for Spring's UserDetailsContextMapper.
 * The context mapper used by the LDAP authentication provider to create an LDAP user object with both groups and roles.
 *
 * @author Noak
 */
public class CustomLdapUserDetailsMapper {
	//~ Instance fields ================================================================================================

    private final Log logger = LogFactory.getLog(LdapUserDetailsMapper.class);
    private String passwordAttributeName = "userPassword";
    private String[] roleAttributes = null;
    private boolean convertToUpperCase = true;

    /**
     * 
     * @param ctx User object
     * @param username the user name
     * @param authorities A collection of authorities
     * @param authGroups A collection of authentication groups
     * @return UserDetails
     */
    public UserDetails mapUserFromContext(final DirContextOperations ctx, final String username, 
    		final Collection<GrantedAuthority> authorities, final Collection<String> authGroups) {
        String dn = ctx.getNameInNamespace();

        logger.debug("Mapping user details from context with DN: " + dn);

        ExtendedLdapUserDetailsImpl.Essence essence = new ExtendedLdapUserDetailsImpl.Essence();
        essence.setDn(dn);

        Object passwordValue = ctx.getObjectAttribute(passwordAttributeName);

        if (passwordValue != null) {
            essence.setPassword(mapPassword(passwordValue));
        }

        essence.setUsername(username);

        // Map the roles
        for (int i = 0; (roleAttributes != null) && (i < roleAttributes.length); i++) {
            String[] rolesForAttribute = ctx.getStringAttributes(roleAttributes[i]);

            if (rolesForAttribute == null) {
                logger.debug("Couldn't read role attribute '" + roleAttributes[i] + "' for user " + dn);
                continue;
            }

            for (int j = 0; j < rolesForAttribute.length; j++) {
                GrantedAuthority authority = createAuthority(rolesForAttribute[j]);

                if (authority != null) {
                    essence.addAuthority(authority);
                }
            }
        }

        // Add the supplied authorities

        for (GrantedAuthority authority : authorities) {
            essence.addAuthority(authority);
        }
        
        // Add the supplied authorization groups
        for (String authGroup : authGroups) {
        	essence.addAuthGroup(authGroup);
        }

        // Check for PPolicy data

        PasswordPolicyResponseControl ppolicy = (PasswordPolicyResponseControl) ctx.
        		getObjectAttribute(PasswordPolicyControl.OID);

        if (ppolicy != null) {
            essence.setTimeBeforeExpiration(ppolicy.getTimeBeforeExpiration());
            essence.setGraceLoginsRemaining(ppolicy.getGraceLoginsRemaining());
        }

        return essence.createUserDetails();

    }

    /**
     * Not implemented.
     * @param user .
     * @param ctx .
     */
    public void mapUserToContext(final UserDetails user, final DirContextAdapter ctx) {
        throw new UnsupportedOperationException("LdapUserDetailsMapper only supports reading from a context. Please" 
        		+ "use a subclass if mapUserToContext() is required.");
    }

    /**
     * Extension point to allow customized creation of the user's password from
     * the attribute stored in the directory.
     *
     * @param passwordValue the value of the password attribute
     * @return a String representation of the password.
     */
    protected String mapPassword(Object passwordValue) {

        if (!(passwordValue instanceof String)) {
            // Assume it's binary
            passwordValue = new String((byte[]) passwordValue);
        }

        return (String) passwordValue;

    }

    /**
     * Creates a GrantedAuthority from a role attribute. Override to customize
     * authority object creation.
     * <p>
     * The default implementation converts string attributes to roles, making use of the <tt>rolePrefix</tt>
     * and <tt>convertToUpperCase</tt> properties. Non-String attributes are ignored.
     * </p>
     *
     * @param role the attribute returned from
     * @return the authority to be added to the list of authorities for the user, or null
     * if this attribute should be ignored.
     */
    protected GrantedAuthority createAuthority(Object role) {
        if (role instanceof String) {
            if (convertToUpperCase) {
                role = ((String) role).toUpperCase();
            }
            return new GrantedAuthorityImpl(role.toString());
        }
        return null;
    }

    /**
     * Determines whether role field values will be converted to upper case when loaded.
     * The default is true.
     *
     * @param convertToUpperCase true if the roles should be converted to upper case.
     */
    public void setConvertToUpperCase(final boolean convertToUpperCase) {
        this.convertToUpperCase = convertToUpperCase;
    }

    /**
     * The name of the attribute which contains the user's password.
     * Defaults to "userPassword".
     *
     * @param passwordAttributeName the name of the attribute
     */
    public void setPasswordAttributeName(final String passwordAttributeName) {
        this.passwordAttributeName = passwordAttributeName;
    }

    /**
     * The names of any attributes in the user's  entry which represent application
     * roles. These will be converted to <tt>GrantedAuthority</tt>s and added to the
     * list in the returned LdapUserDetails object. The attribute values must be Strings by default.
     *
     * @param roleAttributes the names of the role attributes.
     */
    public void setRoleAttributes(final String[] roleAttributes) {
        Assert.notNull(roleAttributes, "roleAttributes array cannot be null");
        this.roleAttributes = roleAttributes;
    }

}