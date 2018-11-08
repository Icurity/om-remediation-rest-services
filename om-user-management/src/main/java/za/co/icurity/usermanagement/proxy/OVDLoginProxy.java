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
		final String password = " ";
 
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		//env.put(Context.PROVIDER_URL, " ");		
		env.put(Context.PROVIDER_URL, " ");
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, username);
		env.put(Context.SECURITY_CREDENTIALS, password);
		DirContext dirContext = new InitialDirContext(env);		
		
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
