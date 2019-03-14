# de.pa2.commons-configuration
This is our common used very slim API based configuration library.

## usage
Examples can be found at https://github.com/pie-ai/pa-commons-configuration/tree/master/src/test/java/de/pa2/commons/configuration
### Create a configuration interface:

```java
public interface ExampleConfiguration extends Configuration {
	@DefaultBooleanValue(true)
	boolean isEnabledByDefault();

	@DefaultBooleanValue(false)
	boolean isNotEnabledByDefault();

	boolean isNotDefaultAnnotated();
}
```

### Get an instance:
```java
ExampleConfiguration cfg = ConfigurationFactory.getInstance(ExampleConfiguration.class);
assertThat(cfg.isEnabledByDefault()).isTrue();
```

### Override configuration using System Properties:
```java
System.setProperty("example.enabled.by.default", Boolean.FALSE.toString());
ExampleConfiguration cfg = ConfigurationFactory.getInstance(ExampleConfiguration.class);
assertThat(cfg.isEnabledByDefault()).isFalse();
```


### Override configuration using Map:
```java
Map<String, String> configuration = new HashMap<String, String>();
configuration.put("example.enabled.by.default", Boolean.FALSE.toString());
ExampleConfiguration cfg = ConfigurationFactory.getInstance(ExampleConfiguration.class, configuration);
assertThat(cfg.isEnabledByDefault()).isFalse();
```
