package za.co.icurity.usermanagement.proxy;

import java.util.Hashtable;
import java.util.Properties;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.InvalidNameException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import za.co.icurity.usermanagement.util.PropertiesUtil;

@Component
public class OVDLoginProxy {

	private static final Logger LOG = LoggerFactory.getLogger(OVDLoginProxy.class);

	public DirContext connect() throws InvalidNameException, NamingException {
		Properties properties = null;
		LOG.info(this + " connect: Connecting to LDAP  ");
		try {
			properties = PropertiesUtil.getProperties();
			if (properties.getProperty("ovd_connect").equalsIgnoreCase("true")) {
				return directLdapF5Connect();
			}
			return jndiLinkConnect();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error(this + " Login to OVD failed: " + e.getMessage());
			e.printStackTrace();
			return null;
		}

	}

	public DirContext jndiLinkConnect() throws InvalidNameException, NamingException {
		Hashtable env = new Hashtable();
		// Properties properties = null;

		// final String searchBase = "cn=users";
		// String searchFilter = "(|(uid=Ripple)(omUserAlias=Ripple))";
		/*
		 * final String username = "cn=Directory Manager"; final String password =
		 * "";
		 * 
		 * env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		 * //env.put(Context.PROVIDER_URL,
		 * "ldap://zaomtappv046.za.omlac.net:1389/dc=oldmutual,dc=co,dc=za,dc=dev");
		 * env.put(Context.PROVIDER_URL,
		 * "ldap://zaomtappv046.za.omlac.net:2389/dc=oudad,dc=oldmutual,dc=co,dc=za,dc=dev"
		 * ); env.put(Context.SECURITY_AUTHENTICATION, "simple");
		 * env.put(Context.SECURITY_PRINCIPAL, username);
		 * env.put(Context.SECURITY_CREDENTIALS, password);
		 * 
		 * DirContext dirContext = new InitialDirContext(env);
		 */

		Properties properties = null;
		try {
			properties = PropertiesUtil.getProperties();

			env.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_PRINCIPAL, properties.getProperty("oim_username"));
			env.put(Context.SECURITY_CREDENTIALS, properties.getProperty("oim_password"));
			env.put(Context.PROVIDER_URL, properties.getProperty("oim_url"));
			env.put("com.sun.jndi.ldap.connect.pool", "true");
			env.put("com.sun.jndi.ldap.connect.maxsize", properties.getProperty("ldap_connect_maxsize"));
			env.put("com.sun.jndi.ldap.connect.prefsize", properties.getProperty("ldap_connect_prefsize"));
			env.put("com.sun.jndi.ldap.connect.timeout", properties.getProperty("ldap_connect_timeout"));
			env.put("com.sun.jndi.ldap.connect.referral", properties.getProperty("ldap_connect_referral"));

			InitialContext initialContext = new InitialContext(env);
			LOG.info(this + " context initiazed  for the usernme " + properties.getProperty("oim_username"));

			return (DirContext) initialContext.lookup(properties.getProperty("ovd_jndi_link"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error(this + " Login to OVD failed: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public DirContext directLdapF5Connect() throws InvalidNameException, NamingException {
		Hashtable env = new Hashtable();
		Properties properties = null;
		try {
			properties = PropertiesUtil.getProperties();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			// env.put(Context.PROVIDER_URL,
			// "ldap://zaomtappv046.za.omlac.net:1389/dc=oldmutual,dc=co,dc=za,dc=dev");
			env.put(Context.PROVIDER_URL, properties.getProperty("ovd_url") + "/" + properties.getProperty("ovd_base_dn"));
			env.put("com.sun.jndi.ldap.connect.pool", "true");
			env.put("com.sun.jndi.ldap.connect.maxsize", properties.getProperty("ldap_connect_maxsize"));
			env.put("com.sun.jndi.ldap.connect.prefsize", properties.getProperty("ldap_connect_prefsize"));
			env.put("com.sun.jndi.ldap.connect.timeout", properties.getProperty("ldap_connect_timeout"));
			env.put("com.sun.jndi.ldap.connect.referral", properties.getProperty("ldap_connect_referral"));
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_PRINCIPAL,  properties.getProperty("ovd_username"));
			env.put(Context.SECURITY_CREDENTIALS, properties.getProperty("ovd_password"));

			DirContext dirContext = new InitialDirContext(env);
			LOG.info(this + " directLdapF5Connect: context initiazed  for the usernme " + properties.getProperty("ovd_username"));
			return dirContext;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error(this + " Error on directLdapF5Connect " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public SearchResult findUserAttributes(DirContext ctx, String searchBase, String searchFilter) throws NamingException {

		/*
		 * String searchFilter = "(|(uid=" + username + ")(omUserAlias=" + username +
		 * ")(cn=" + username + "))"; SearchResult searchResult =
		 * ovdLoginProxy.findUserAttributes(dirContext, searchBase, searchFilter);
		 */

		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		// String searchFilter = "(|(uid=" + username + ")(omUserAlias=" + username +
		// ")(cn=" + username + "))";
		NamingEnumeration<SearchResult> results = ctx.search(searchBase, searchFilter, searchControls);

		SearchResult searchResult = null;
		if (results.hasMoreElements()) {
			searchResult = (SearchResult) results.nextElement();
			LOG.info(this + " User Attributes for search filter " + searchFilter);
		}
		return searchResult;
	}
}
