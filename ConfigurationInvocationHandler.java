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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationInvocationHandler implements InvocationHandler {
	private static final Logger LOG = LoggerFactory.getLogger(ConfigurationInvocationHandler.class);

	private ConfigurationHolderImpl configuration;
	private Class interfaceClazz;

	private ConfigurationPrefix prefix;
	private String configurationPropertyNamePrefix = null;

	public ConfigurationInvocationHandler(Class interfaceClazz, ConfigurationHolderImpl configuration) {
		super();
		this.interfaceClazz = interfaceClazz;
		this.configuration = configuration;

		// detect prefix
		this.prefix = (ConfigurationPrefix) interfaceClazz.getAnnotation(ConfigurationPrefix.class);
		if (this.prefix != null) {
			configurationPropertyNamePrefix = this.prefix.value();
		}
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (LOG.isDebugEnabled()) {
			LOG.debug("invoking method:" + method.toString());
		}
		final Class returnType = method.getReturnType();
		final String propertyName = getConfigurationPropertyName(method.getName());
		/**
		 * void setXXX
		 */
		if (method.getName().startsWith("set")) {
			throw new IllegalArgumentException("setters are not supported");
			// if (args != null && args.length == 1) {
			// String value = args[0].toString();
			// configuration.getRegistry().setEntryValue(interfaceClazz,
			// propertyName, value);
			// return null;
			// } else {
			// throw new IllegalArgumentException("expected setter to contain 1
			// parameter");
			// }
		} else if (method.getName().startsWith("get") || method.getName().startsWith("is")) {
			/**
			 * String getXXX
			 */
			if (String.class.equals(returnType)) {
				DefaultStringValue defaulValueString = method.getAnnotation(DefaultStringValue.class);
				String defaultValue = null;
				if (defaulValueString != null) {
					defaultValue = defaulValueString.value();
				}

				if (configuration.getRegistry().containsEntry(interfaceClazz, propertyName)) {
					defaultValue = configuration.getRegistry().getEntryValue(interfaceClazz, propertyName);
				}

				return getConfigurationPropertyValue(propertyName, defaultValue);
			} else if (int.class.equals(returnType)) {
				DefaultIntValue defaultIntValue = method.getAnnotation(DefaultIntValue.class);
				int defaultValue = 0;
				if (defaultIntValue != null) {
					defaultValue = defaultIntValue.value();
				}

				if (configuration.getRegistry().containsEntry(interfaceClazz, propertyName)) {
					defaultValue = Integer
							.parseInt(configuration.getRegistry().getEntryValue(interfaceClazz, propertyName));
				}

				return getConfigurationPropertyValue(propertyName, defaultValue);
			} else if (boolean.class.equals(returnType)) {
				DefaultBooleanValue defaultBooleanValue = method.getAnnotation(DefaultBooleanValue.class);
				boolean defaultValue = false;
				if (defaultBooleanValue != null) {
					defaultValue = defaultBooleanValue.value();
				}

				if (configuration.getRegistry().containsEntry(interfaceClazz, propertyName)) {
					defaultValue = "TRUE"
							.equalsIgnoreCase(configuration.getRegistry().getEntryValue(interfaceClazz, propertyName));
				}

				return getConfigurationPropertyValue(propertyName, defaultValue);
			} else if (returnType.isEnum()) {
				DefaultStringValue defaulValueString = method.getAnnotation(DefaultStringValue.class);
				String defaultValue = null;
				if (defaulValueString != null) {
					defaultValue = defaulValueString.value();
				}

				if (configuration.getRegistry().containsEntry(interfaceClazz, propertyName)) {
					defaultValue = configuration.getRegistry().getEntryValue(interfaceClazz, propertyName);
				}

				String enumValue = getConfigurationPropertyValue(propertyName, defaultValue);

				return Enum.valueOf(returnType, enumValue.toUpperCase());
			}

			LOG.error("could not determine configuration value for " + interfaceClazz.getName() + "." + method.getName()
					+ " returning object of type '" + returnType.getName() + "'");
			return null;
		} else if (method.getName().equals("toString")) {
			StringBuilder buf = new StringBuilder();
			buf.append(interfaceClazz.getName()).append("(");
			for (int i = 0; i < interfaceClazz.getMethods().length; i++) {
				if (i > 0) {
					buf.append(",");
				}
				buf.append("\n\t");
				Method m = interfaceClazz.getMethods()[i];
				Class methodReturnType = m.getReturnType();
				String methodPropertyName = getConfigurationPropertyName(m.getName());
				if (String.class.equals(methodReturnType)) {
					DefaultStringValue defaulValueString = m.getAnnotation(DefaultStringValue.class);
					String defaultValue = null;
					if (defaulValueString != null) {
						defaultValue = defaulValueString.value();
					}

					if (configuration.getRegistry().containsEntry(interfaceClazz, methodPropertyName)) {
						defaultValue = configuration.getRegistry().getEntryValue(interfaceClazz, methodPropertyName);
					}

					buf.append(methodPropertyName).append(":")
							.append(getConfigurationPropertyValue(methodPropertyName, defaultValue));
				} else if (int.class.equals(methodReturnType)) {
					DefaultIntValue defaultIntValue = m.getAnnotation(DefaultIntValue.class);
					int defaultValue = 0;
					if (defaultIntValue != null) {
						defaultValue = defaultIntValue.value();
					}

					buf.append(methodPropertyName).append(":")
							.append(getConfigurationPropertyValue(methodPropertyName, defaultValue));
				}

			}

			buf.append(")");
			return buf.toString();
		} else {
			throw new RuntimeException("unimplemented method:" + method.toString());
		}
	}

	private String getConfigurationPropertyValue(String propertyName, String defaultValue) {
		String value = System.getProperty(propertyName,
				configuration.getProperties().getProperty(propertyName, defaultValue));
		if (LOG.isDebugEnabled()) {
			LOG.debug("configuration property '" + propertyName + "': " + value + " (default:'" + defaultValue + "')");
		}

		return value;
	}

	private boolean getConfigurationPropertyValue(String propertyName, boolean defaultValue) {
		boolean value = "TRUE".equalsIgnoreCase(System.getProperty(propertyName,
				configuration.getProperties().getProperty(propertyName, new Boolean(defaultValue).toString())));
		if (LOG.isDebugEnabled()) {
			LOG.debug("configuration property '" + propertyName + "': " + value + " (default:'" + defaultValue + "')");
		}
		return value;
	}

	private int getConfigurationPropertyValue(String propertyName, int defaultValue) throws NumberFormatException {
		return Integer.parseInt(getConfigurationPropertyValue(propertyName, "" + defaultValue));
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
