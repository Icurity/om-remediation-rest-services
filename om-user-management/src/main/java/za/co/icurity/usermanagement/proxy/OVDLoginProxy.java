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
		Hashtable env = new Hashtable();
		Properties properties = null;
		
		//final String searchBase = "cn=users";
		//String searchFilter = "(|(uid=Ripple)(omUserAlias=Ripple))";
		final String username = "cn= ";
		final String password = "password here";
 
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		//env.put(Context.PROVIDER_URL, "url here");		
		env.put(Context.PROVIDER_URL, "url here");
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, username);
		env.put(Context.SECURITY_CREDENTIALS, password);

		DirContext dirContext = new InitialDirContext(env);
		
		/*try {
			properties = PropertiesUtil.getProperties();
		} catch (Exception e) {
			  LOG.warn("DirContext getProperties " + e.getClass().getName() + " : " + e.getMessage());
	          return null;
		}
		try {
			env.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
			env.put(Context.SECURITY_AUTHENTICATION, "simple");

			env.put(Context.SECURITY_PRINCIPAL, "weblogic");

			env.put(Context.SECURITY_CREDENTIALS, "*PaHuR8r");

			env.put(Context.PROVIDER_URL, "t3://zaomtappv046.za.omlac.net:14000");

			InitialContext itx = new InitialContext(env);
			LOG.info(this + " Connecting to provider");
			return (DirContext) itx.lookup("testJNDILink");
		} catch (NamingException e) {
			LOG.error("Error on DirContext ");
		} catch (Exception e) {
			LOG.error("Error on DirContext ");
		}
		*/
		/*//code to read the values from properties file
		try {
			env.put(Context.INITIAL_CONTEXT_FACTORY,properties.getProperty("weblogic_jndi"));
			env.put(Context.SECURITY_AUTHENTICATION, properties.getProperty("security_auth"));
			env.put(Context.SECURITY_PRINCIPAL, properties.getProperty("security_principal"));
			env.put(Context.SECURITY_CREDENTIALS, properties.getProperty("password"));
			env.put(Context.PROVIDER_URL, properties.getProperty("provider_url"));

			InitialContext itx = new InitialContext(env);
			LOG.info(this + " Connecting to provider");
			return (DirContext) itx.lookup("testJNDILink");
		} catch (NamingException e) {
			LOG.error("Error on DirContext ");
		} catch (Exception e) {
			LOG.error("Error on DirContext ");
		}*/
		return dirContext;

	}

	public SearchResult findUserAttributes(DirContext ctx, String searchBase, String searchFilter) throws NamingException {

		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

		NamingEnumeration<SearchResult> results = ctx.search(searchBase, searchFilter, searchControls);

		SearchResult searchResult = null;
		if (results.hasMoreElements()) {
			searchResult = (SearchResult) results.nextElement();

		}
		return searchResult;
	}
}
