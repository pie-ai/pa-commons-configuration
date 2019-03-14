/*
 * copyright (C) 2008-2016 Patrick Stricker
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pa2.commons.configuration.registry.ConfigurationEntry;
import de.pa2.commons.configuration.registry.ReadOnlyRegistry;
import de.pa2.commons.configuration.registry.Registry;

public class ConfigurationFactory {
	public static String SYSTEM_PROPERTY_CONFIGURATION_FILE_NAME = "configuration";
	public static String DEFAULT_VALUE_CONFIGURATION_FILE_NAME = "configuration.properties";

	public static String SYSTEM_PROPERTY_REGISTRY_FILE_NAME = "registry";
	public static String DEFAULT_VALUE_REGISTRY_FILE_NAME = "registry.properties";

	private static final Logger LOG = LoggerFactory.getLogger(ConfigurationFactory.class);

	private static ConfigurationHolderImpl getConfigurationHolder() {
		LOG.debug("loading configuration");
		ConfigurationHolderImpl holder = new ConfigurationHolderImpl();

		Properties properties = new Properties();
		String configurationFileName = System.getProperty(SYSTEM_PROPERTY_CONFIGURATION_FILE_NAME,
				DEFAULT_VALUE_CONFIGURATION_FILE_NAME);
		if (DEFAULT_VALUE_CONFIGURATION_FILE_NAME.equals(configurationFileName)) {
			// no change of configuration, try to detect application
			// server and use common paths
			if (System.getProperty("catalina.home", null) != null) {
				configurationFileName = System.getProperty("catalina.home", null) + File.separator + "conf"
						+ File.separator + configurationFileName;
			}
		}

		// read properties from resources first and use properties
		// from configuration file to overwrite the values
		try {
			String configurationResourceUrl = System.getProperty(SYSTEM_PROPERTY_CONFIGURATION_FILE_NAME,
					DEFAULT_VALUE_CONFIGURATION_FILE_NAME);
			LOG.debug("loading configuration from resource stream: {}", configurationResourceUrl);
			InputStream in = ConfigurationFactory.class.getClassLoader().getResourceAsStream(configurationResourceUrl);
			if (in != null) {
				properties.load(in);
			} else {
				LOG.info("could not load properties file/resource, using empty properties.");
			}
		} catch (IOException e) {
			LOG.error("could not load properties file/resource:" + e.getMessage(), e);
		}

		File configurationFile = new File(configurationFileName);
		if (configurationFile.exists()) {
			try {
				LOG.debug("loading configuration from file: {}", configurationFile.getAbsolutePath());
				Properties propertiesToOverrideDefaults = new Properties();
				propertiesToOverrideDefaults.load(new FileInputStream(configurationFile));
				for (Object key : propertiesToOverrideDefaults.keySet()) {
					properties.put(key, propertiesToOverrideDefaults.get(key));
				}
			} catch (FileNotFoundException e) {
				LOG.error("could not configuration file: " + e.getMessage(), e);
			} catch (IOException e) {
				LOG.error("could not configuration file: " + e.getMessage(), e);
			}
		} else {

		}

		// load registry to store dynamic values
		/*
		 * setters are not supported and registry not required File registryFile
		 * = new File( System.getProperty(SYSTEM_PROPERTY_REGISTRY_FILE_NAME,
		 * DEFAULT_VALUE_REGISTRY_FILE_NAME));
		 * LOG.debug("using configuration registry file: {}",
		 * registryFile.getAbsolutePath());
		 * 
		 * try { holder.setRegistry(new FileRegistryImpl(registryFile)); } catch
		 * (IOException e) {
		 * LOG.error("could not create configuration registry: {}",
		 * e.getMessage(), e); }
		 */
		holder.setRegistry(new ReadOnlyRegistry(new HashMap<String, String>()));
		holder.setProperties(properties);
		return holder;
	}

	public static <E extends Configuration> E getInstance(Class<E> clazz, Registry registry) {
		Class[] proxyInterfaces = new Class[] { clazz };

		ConfigurationHolderImpl holder = new ConfigurationHolderImpl();
		holder.setRegistry(registry);

		Object configuration = Proxy.newProxyInstance(clazz.getClassLoader(), proxyInterfaces,
				new ConfigurationInvocationHandler(clazz, holder));

		return (E) configuration;
	}

	/**
	 * creates an instance of configuration interface using passed values to
	 * override default ones
	 * 
	 * @param clazz
	 * @param defaultConfiguration
	 * @return
	 */
	public static <E extends Configuration> E getInstance(final Class<E> clazz,
			ConfigurationEntry<String>... defaultConfiguration) {
		Class[] proxyInterfaces = new Class[] { clazz };

		ConfigurationHolderImpl holder = getConfigurationHolder();

		for (ConfigurationEntry<String> cfg : defaultConfiguration) {
			if (holder.getProperties().containsKey(cfg.getName())) {
				holder.getProperties().remove(cfg.getName());
			}
			holder.getProperties().put(cfg.getName(), cfg.getValue());
		}
		Object configuration = Proxy.newProxyInstance(clazz.getClassLoader(), proxyInterfaces,
				new ConfigurationInvocationHandler(clazz, holder));

		return (E) configuration;
	}

	/**
	 * creates an instance of configuration interface using passed values to
	 * override default ones
	 * 
	 * @param clazz
	 * @param defaultConfiguration
	 * @return
	 */
	public static <E extends Configuration> E getInstance(final Class<E> clazz,
			final Map<String, String> defaultConfiguration) {
		Class[] proxyInterfaces = new Class[] { clazz };

		ConfigurationHolderImpl holder = getConfigurationHolder();
		for (String cfgKey : defaultConfiguration.keySet()) {
			if (holder.getProperties().containsKey(cfgKey)) {
				holder.getProperties().remove(cfgKey);
			}
			holder.getProperties().put(cfgKey, defaultConfiguration.get(cfgKey));
		}

		Object configuration = Proxy.newProxyInstance(clazz.getClassLoader(), proxyInterfaces,
				new ConfigurationInvocationHandler(clazz, holder));

		return (E) configuration;
	}

	private Class interfaceClazz;
	private String configurationPropertyNamePrefix = null;
	private Registry instanceRegistry;

	public ConfigurationFactory(Class interfaceClazz, Registry instanceRegistry) {
		super();
		this.instanceRegistry = instanceRegistry;
		this.interfaceClazz = interfaceClazz;
		ConfigurationPrefix prefix = (ConfigurationPrefix) interfaceClazz.getAnnotation(ConfigurationPrefix.class);
		if (prefix != null) {
			configurationPropertyNamePrefix = prefix.value();
		}
	}

	protected String getConfigurationPropertyName(String methodName) {
		String methodNameWithoutGet = null;
		if (methodName.startsWith("get")) {
			methodNameWithoutGet = methodName.substring("get".length());
		} else if (methodName.startsWith("set")) {
			methodNameWithoutGet = methodName.substring("set".length());
		} else if (methodName.startsWith("is")) {
			methodNameWithoutGet = methodName.substring("is".length());
		} else {
			methodNameWithoutGet = methodName;
		}

		StringBuilder buf = new StringBuilder();
		if (configurationPropertyNamePrefix != null) {
			if (configurationPropertyNamePrefix.endsWith(".")) {
				buf.append(configurationPropertyNamePrefix);
			} else {
				buf.append(configurationPropertyNamePrefix).append(".");
			}
		}
		for (int i = 0; i < methodNameWithoutGet.length(); i++) {
			String c = methodNameWithoutGet.substring(i, i + 1);
			if (c.toUpperCase().equals(c) && i > 0) {
				buf.append(".");
				buf.append(c.toLowerCase());
			} else {
				buf.append(c.toLowerCase());
			}
		}

		return buf.toString();
	}
}
