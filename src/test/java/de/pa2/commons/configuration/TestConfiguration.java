package de.pa2.commons.configuration;

public interface TestConfiguration extends Configuration {
    boolean isActive();

    @DefaultBooleanValue(true)
    boolean isActiveDefault();
    
    @DefaultBooleanValue(false)
    boolean isInactiveDefault();
    
    boolean getActive();

    Boolean isDisabled();
    
    @DefaultBooleanValue(false)
    Boolean isDisabledDefault();

    String getName();

    int getValue();
    
    long getLongValue();

    enum TestEnum {
        A,
        B
    }

    TestEnum getChoice();
}
