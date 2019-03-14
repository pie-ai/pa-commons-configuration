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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * property resolver that uses resources
 */
public class ResourcePropertyResolver implements PropertyResolver {
    private Properties properties = null;

    public ResourcePropertyResolver(String resourceName) {
        super();
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(resourceName);
        if (in != null) {
            Properties tmp = new Properties();
            try {
                tmp.load(in);
                in.close();
                this.properties = tmp;
            } catch (IOException e) {
            }

        }
    }

    public ResourcePropertyResolver(InputStream in) {
        super();
        if (in != null) {
            Properties tmp = new Properties();
            try {
                tmp.load(in);
                in.close();
                this.properties = tmp;
            } catch (IOException e) {
            }

        }
    }

    @Override
    public String getProperty(String propertyName, String defaultValue) {
        if (this.properties != null) {
            return this.properties.getProperty(propertyName, defaultValue);
        }
        return defaultValue;
    }

}
