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
package de.pa2.commons.configuration.resolvers;

/**
 * property resolver that uses env variables
 */
public class EnvironmentResolver implements PropertyResolver {

    @Override
    public String getProperty(String propertyName, String defaultValue) {
        // foo.bar looks up FOO_BAR
        String envName = propertyName.replaceAll("\\.", "_").toUpperCase();
        if (System.getenv().containsKey(envName)) {
            return System.getenv(envName);
        } else {
            return defaultValue;
        }
    }

}
