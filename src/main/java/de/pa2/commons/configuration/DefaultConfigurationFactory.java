/*
 * copyright (C) 2008-2019 Patrick Stricker
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA
 *
 * 	Patrick Stricker - http://pa2.de
 */
package de.pa2.commons.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import de.pa2.commons.configuration.resolvers.AggregatedPropertyResolver;
import de.pa2.commons.configuration.resolvers.EnvironmentResolver;
import de.pa2.commons.configuration.resolvers.PropertyResolver;
import de.pa2.commons.configuration.resolvers.ResourcePropertyResolver;
import de.pa2.commons.configuration.resolvers.SystemPropertyResolver;

/**
 * configuration factory that uses multiple sources for configuration values
 */
public class DefaultConfigurationFactory extends AbstractConfigurationFactory implements Factory {

	public DefaultConfigurationFactory() {
		super();
	}

	@Override
	protected PropertyResolver getResolver() {
		AggregatedPropertyResolver resolver = new AggregatedPropertyResolver();

		// system properties get highest priority
		resolver.add(new SystemPropertyResolver());

		// environment gets almost highest priority
		resolver.add(new EnvironmentResolver());

		String hostSpecificPropertyFileName = getHostName() + ".properties";

		// host specific properties provided as file
		File hostSpecificFile = this.detectFile(hostSpecificPropertyFileName);
		if (hostSpecificFile != null && hostSpecificFile.exists()) {
			try {
				resolver.add(new ResourcePropertyResolver(new FileInputStream(hostSpecificFile)));
			} catch (FileNotFoundException e) {
			}
		}

		// host specific properties provided as resource
		resolver.add(new ResourcePropertyResolver(hostSpecificPropertyFileName));

		// default properties provided as file
		File defaultFile = detectFile("default.properties");
		if (defaultFile != null && defaultFile.exists()) {
			try {
				resolver.add(new ResourcePropertyResolver(new FileInputStream(defaultFile)));
			} catch (FileNotFoundException e) {
			}
		}

		// default properties provided as resource
		resolver.add(new ResourcePropertyResolver("default.properties"));
		return resolver;
	}

	public static File detectFile(String fileName) {
		File result = null;

		if (new File(fileName).exists()) {
			result = new File(fileName);
		} else if (System.getProperty("CATALINA_HOME") != null
				&& new File(new File(System.getProperty("CATALINA_HOME") + "/conf"), fileName).exists()) {
			result = new File(new File(System.getProperty("CATALINA_HOME") + "/conf"), fileName);
		} else if (System.getProperty("SERVER_HOME") != null
				&& new File(new File(System.getProperty("SERVER_HOME") + "/conf"), fileName).exists()) {
			result = new File(new File(System.getProperty("SERVER_HOME") + "/conf"), fileName);

		} else if (System.getProperty("jboss.server.config.dir") != null
				&& new File(new File(System.getProperty("jboss.server.config.dir")), fileName).exists()) {
			result = new File(new File(System.getProperty("jboss.server.config.dir")), fileName);
		} else if (System.getProperty("application.home") != null
				&& new File(new File(System.getProperty("application.home") + "/conf"), fileName).exists()) {
			result = new File(new File(System.getProperty("application.home") + "/conf"), fileName);
		}

		return result;
	}

	/**
	 * detects the current host name
	 *
	 * @return
	 */
	private static String getHostName() {
		// windows
		String hostName = System.getenv("COMPUTERNAME");

		if (hostName == null || "".equals(hostName)) {
			// unix
			hostName = System.getenv("HOSTNAME");

			if (hostName == null || "".equals(hostName)) {
				// some special distros like debian and derivates
				try {
					hostName = execute("hostname").trim();
				} catch (IOException | InterruptedException e) {
				}
			}
		}

		return hostName;
	}

	/**
	 * executes command and returnes response, no security mechanism, specific use
	 * only!
	 *
	 * @param command
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private static String execute(String command) throws IOException, InterruptedException {
		StringBuilder result = new StringBuilder();
		Process p = Runtime.getRuntime().exec(command);
		p.waitFor();
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = reader.readLine();
		while (line != null) {
			result.append(line + "\n");
			line = reader.readLine();
		}
		return result.toString();
	}

}
