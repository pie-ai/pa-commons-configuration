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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
 *
 * Patrick Stricker - http://pa2.de
 */
package de.pa2.commons.configuration;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import de.pa2.commons.configuration.TestConfiguration.TestEnum;

public class SystemPropertyConfigurationFactoryTest {

    @Test
    public void prefixTests() {
        PrefixedConfiguration cfg =
                new SystemPropertyConfigurationFactory().getInstance(PrefixedConfiguration.class);
        assertNull(cfg.getNullLong());
    }

    @Test
    public void getInstance() {
        SystemPropertyConfigurationFactory factory = new SystemPropertyConfigurationFactory();

        TestConfiguration configuration = factory.getInstance(TestConfiguration.class);
        assertFalse(configuration.getActive());
        assertFalse(configuration.isActive());
        assertNull(configuration.isDisabled());
        System.setProperty("disabled", "false");
        assertFalse(configuration.isDisabled());

        assertFalse(configuration.isDisabledDefault());
        System.setProperty("disabled.default", "true");
        assertTrue(configuration.isDisabledDefault());

        assertTrue(configuration.isActiveDefault());
        assertFalse(configuration.isInactiveDefault());

        assertNull(configuration.getName());
        System.setProperty("name", "Horst");
        assertEquals(configuration.getName(), "Horst");

        assertEquals(configuration.getValue(), 0);
        System.setProperty("value", "-666");
        assertEquals(configuration.getValue(), - 666);

        assertEquals(configuration.getLongValue(), 0);
        System.setProperty("long.value", "55555");
        assertEquals(configuration.getLongValue(), 55555);

        assertNull(configuration.getChoice());
        System.setProperty("choice", TestEnum.A.name());
        assertEquals(configuration.getChoice(), TestEnum.A);

    }
}
