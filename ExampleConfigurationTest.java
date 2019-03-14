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
package de.pa2.commons.configuration.examples;

import static org.fest.assertions.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import de.pa2.commons.configuration.ConfigurationFactory;
import de.pa2.commons.configuration.registry.ConfigurationEntry;
import de.pa2.commons.configuration.registry.ReadOnlyRegistry;

public class ExampleConfigurationTest {
	@Test
	public void notConfigured() {
		ExampleConfiguration cfg = ConfigurationFactory.getInstance(ExampleConfiguration.class);
		assertThat(cfg.isEnabledByDefault()).isTrue();
		assertThat(cfg.isNotEnabledByDefault()).isFalse();
		assertThat(cfg.isNotDefaultAnnotated()).isFalse();
	}

	@Test
	public void systemProperties() {
		System.setProperty("example.enabled.by.default", Boolean.FALSE.toString());
		System.setProperty("example.not.enabled.by.default", Boolean.TRUE.toString());
		System.setProperty("example.not.default.annotated", Boolean.TRUE.toString());

		ExampleConfiguration cfg = ConfigurationFactory.getInstance(ExampleConfiguration.class);
		assertThat(cfg.isEnabledByDefault()).isFalse();
		assertThat(cfg.isNotEnabledByDefault()).isTrue();
		assertThat(cfg.isNotDefaultAnnotated()).isTrue();
	}

	@Test
	public void configurationEntry() {

		ExampleConfiguration cfg = ConfigurationFactory.getInstance(ExampleConfiguration.class,
				new ConfigurationEntry("example.enabled.by.default", Boolean.FALSE.toString()),
				new ConfigurationEntry("example.not.enabled.by.default", Boolean.TRUE.toString()),
				new ConfigurationEntry("example.not.default.annotated", Boolean.TRUE.toString()));
		assertThat(cfg.isEnabledByDefault()).isFalse();
		assertThat(cfg.isNotEnabledByDefault()).isTrue();
		assertThat(cfg.isNotDefaultAnnotated()).isTrue();
	}

	@Test
	public void map() {
		Map<String, String> configuration = new HashMap<String, String>();
		configuration.put("example.enabled.by.default", Boolean.FALSE.toString());
		configuration.put("example.not.enabled.by.default", Boolean.TRUE.toString());
		configuration.put("example.not.default.annotated", Boolean.TRUE.toString());

		ExampleConfiguration cfg = ConfigurationFactory.getInstance(ExampleConfiguration.class, configuration);
		assertThat(cfg.isEnabledByDefault()).isFalse();
		assertThat(cfg.isNotEnabledByDefault()).isTrue();
		assertThat(cfg.isNotDefaultAnnotated()).isTrue();
	}

	@Test
	public void registry() {
		Map<String, String> configuration = new HashMap<String, String>();
		configuration.put("example.enabled.by.default", Boolean.FALSE.toString());
		configuration.put("example.not.enabled.by.default", Boolean.TRUE.toString());
		configuration.put("example.not.default.annotated", Boolean.TRUE.toString());

		ExampleConfiguration cfg = ConfigurationFactory.getInstance(ExampleConfiguration.class,
				new ReadOnlyRegistry(configuration));
		assertThat(cfg.isEnabledByDefault()).isFalse();
		assertThat(cfg.isNotEnabledByDefault()).isTrue();
		assertThat(cfg.isNotDefaultAnnotated()).isTrue();
	}
}
