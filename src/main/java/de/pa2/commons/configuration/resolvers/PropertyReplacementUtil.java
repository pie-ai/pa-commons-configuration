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

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertyReplacementUtil {
	public final static Pattern systemPropertyPattern = Pattern.compile("(\\$\\{([^\\}]*)\\})");

	private static class SourceAndReplacement {
		private String source = null;
		private String replacement = null;

		public SourceAndReplacement(String source, String replacement) {
			super();
			this.source = source;
			this.replacement = replacement;
		}
	}

	public static String replacePlaceholders(String source, PropertyResolver resolver) {
		String result = null;
		if (source != null) {
			ArrayList replacements = new ArrayList();
			Matcher m = systemPropertyPattern.matcher(source);
			while (m.find()) {
				String replacementSource = m.group(1);
				String systemPropertyValue = resolver.getProperty(m.group(2), null);
				if (systemPropertyValue != null) {
					replacements.add(new SourceAndReplacement(replacementSource, systemPropertyValue));
				}
			}
			result = source;
			for (int i = 0; i < replacements.size(); i++) {
				SourceAndReplacement sr = (SourceAndReplacement) replacements.get(i);
				result = result.replace(sr.source, sr.replacement);
			}
		}
		return result;
	}

}
