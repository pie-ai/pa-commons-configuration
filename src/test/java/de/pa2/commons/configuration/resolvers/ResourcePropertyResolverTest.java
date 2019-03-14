package de.pa2.commons.configuration.resolvers;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

public class ResourcePropertyResolverTest {

    @Test
    public void getProperty() {
        ResourcePropertyResolver resolver = null;
        String source = null;

         resolver = new ResourcePropertyResolver("default.properties");
         source = resolver.getProperty("source", null);
         assertNotNull(source);
         assertEquals(source, "default properties resource");

        resolver = new ResourcePropertyResolver(
                this.getClass().getResourceAsStream("/default.properties"));

        source = resolver.getProperty("source", null);
        assertNotNull(source);
        assertEquals(source, "default properties resource");

        resolver = new ResourcePropertyResolver(
                this.getClass().getClassLoader().getResourceAsStream("default.properties"));

        source = resolver.getProperty("source", null);
        assertNotNull(source);
        assertEquals(source, "default properties resource");
    }
}
