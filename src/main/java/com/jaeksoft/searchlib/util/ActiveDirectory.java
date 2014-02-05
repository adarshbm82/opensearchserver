package com.jaeksoft.searchlib.util;

import java.io.Closeable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import com.jaeksoft.searchlib.Logging;

/**
 * License Agreement for OpenSearchServer
 * 
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 * 
 * OpenSearchServer is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * OpenSearchServer is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * OpenSearchServer. If not, see <http://www.gnu.org/licenses/>.
 **/

public class ActiveDirectory implements Closeable {

	private DirContext dirContext = null;
	private String domainSearchName = null;

	public ActiveDirectory(String username, String password, String domain)
			throws NamingException {
		Properties properties = new Properties();
		properties.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		properties.put(Context.PROVIDER_URL,
				StringUtils.fastConcat("LDAP://", domain));
		properties.put(Context.SECURITY_PRINCIPAL,
				StringUtils.fastConcat(username, "@", domain));
		properties.put(Context.SECURITY_CREDENTIALS, password);
		dirContext = new InitialDirContext(properties);
		domainSearchName = getDomainSearch(domainSearchName);
	}

	public final static String[] DefaultReturningAttributes = { "cn", "mail",
			"givenName", "objectSid", "sAMAccountName", };

	public NamingEnumeration<SearchResult> findUser(String username,
			String... returningAttributes) throws NamingException {

		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		if (returningAttributes == null || returningAttributes.length == 0)
			returningAttributes = DefaultReturningAttributes;
		searchControls.setReturningAttributes(returningAttributes);
		String filterExpr = StringUtils
				.fastConcat(
						"(&((&(objectCategory=Person)(objectClass=User)))(samaccountname=",
						username, "))");
		return dirContext.search(domainSearchName, filterExpr, searchControls);
	}

	@Override
	public void close() {
		try {
			if (dirContext != null)
				dirContext.close();
			dirContext = null;
		} catch (NamingException e) {
			Logging.warn(e);
		}
	}

	private static String getDomainSearch(String domain) {
		String[] dcs = StringUtils.split(domain.toUpperCase(), '.');
		StringBuilder sb = new StringBuilder();
		for (String dc : dcs) {
			if (sb.length() > 0)
				sb.append(',');
			sb.append("DC=");
			sb.append(dc);
		}
		return sb.toString();
	}

	public static String getStringAttribute(Attributes attrs, String name) {
		String s = attrs.get(name).toString();
		if (StringUtils.isEmpty(s))
			return s;
		int i = s.indexOf(':');
		if (i == -1)
			throw new IllegalArgumentException(StringUtils.fastConcat(
					"Wrong returned value: ", s));
		return s.substring(i + 1);
	}

	public static String getObjectSID(Attributes attrs) throws NamingException {
		byte[] sidBytes = (byte[]) attrs.get("objectsid").get();
		return decodeSID(sidBytes);
	}

	/**
	 * The binary data is in the form: byte[0] - revision level byte[1] - count
	 * of sub-authorities byte[2-7] - 48 bit authority (big-endian) and then
	 * count x 32 bit sub authorities (little-endian)
	 * 
	 * The String value is: S-Revision-Authority-SubAuthority[n]...
	 * 
	 * Based on code from here -
	 * http://forums.oracle.com/forums/thread.jspa?threadID=1155740&tstart=0
	 */
	public static String decodeSID(byte[] sid) {

		final StringBuilder strSid = new StringBuilder("S-");

		// get version
		final int revision = sid[0];
		strSid.append(Integer.toString(revision));

		// next byte is the count of sub-authorities
		final int countSubAuths = sid[1] & 0xFF;

		// get the authority
		long authority = 0;
		// String rid = "";
		for (int i = 2; i <= 7; i++) {
			authority |= ((long) sid[i]) << (8 * (5 - (i - 2)));
		}
		strSid.append("-");
		strSid.append(Long.toHexString(authority));

		// iterate all the sub-auths
		int offset = 8;
		int size = 4; // 4 bytes for each sub auth
		for (int j = 0; j < countSubAuths; j++) {
			long subAuthority = 0;
			for (int k = 0; k < size; k++) {
				subAuthority |= (long) (sid[offset + k] & 0xFF) << (8 * k);
			}

			strSid.append("-");
			strSid.append(subAuthority);

			offset += size;
		}

		return strSid.toString();
	}

}