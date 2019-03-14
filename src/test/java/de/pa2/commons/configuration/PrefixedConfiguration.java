package de.pa2.commons.configuration;

@ConfigurationPrefix("prefixed")
public interface PrefixedConfiguration extends Configuration{
	Long getNullLong();
	
	@DefaultLongValue(Long.MAX_VALUE)
	Long getDefaultLong();
}
