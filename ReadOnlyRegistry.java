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
package de.pa2.commons.configuration.registry;

import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadOnlyRegistry implements Registry {
	private Properties properties;

	private static final Logger LOG = LoggerFactory.getLogger(ReadOnlyRegistry.class);

	public ReadOnlyRegistry(Map<String, String> defaultConfiguration) {
		this.properties = new Properties();
		if (defaultConfiguration != null) {
			for (String key : defaultConfiguration.keySet()) {
				this.properties.setProperty(key, defaultConfiguration.get(key));
			}
		}
	}

	public ReadOnlyRegistry(Properties properties) {
		super();
		this.properties = properties;
	}

	@Override
	public boolean containsEntry(Class configurationClass, String name) {
		return properties.containsKey(name);
	}

	@Override
	public String getEntryValue(Class configurationClass, String name) {
		return properties.getProperty(name);
	}

	@Override
	public void setEntryValue(Class configurationClass, String name, String value) {
		LOG.warn("registry value '{}' for property '{}' and class '{}' only gets stored in memory, not persisted",
				value, name, configurationClass.getName());
		synchronized (properties) {
			String key = configurationClass.getName() + "." + name;
			if (properties.containsKey(key)) {
				properties.remove(key);
			}
			properties.setProperty(key, value);
		}
	}
}
