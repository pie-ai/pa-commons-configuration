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

import de.pa2.commons.configuration.resolvers.EnvironmentResolver;
import de.pa2.commons.configuration.resolvers.PropertyResolver;

/**
 * configuration factory that uses environment as source of configuration values
 */
public class EnvironmentPropertyConfigurationFactory extends AbstractConfigurationFactory implements Factory {

	public EnvironmentPropertyConfigurationFactory() {
		super();
	}

	@Override
	protected PropertyResolver getResolver() {
		return new EnvironmentResolver();
	}
}
