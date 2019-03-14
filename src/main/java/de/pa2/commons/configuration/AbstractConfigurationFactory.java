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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pa2.commons.configuration.resolvers.PropertyResolver;

/**
 * base implementation
 */
abstract class AbstractConfigurationFactory implements Factory {
    /**
     * invocation handler
     */
    protected static class ConfigurationInvocationHandler implements InvocationHandler {
        /**
         * resolves the values for properties
         */
        private PropertyResolver resolver = null;

        /**
         * prefix to translate method names into configuration property names
         */
        private String configurationPropertyNamePrefix = null;

        /**
         * requested configuration interface
         */
        @SuppressWarnings("rawtypes")
        private Class interfaceClazz;

        private static final Logger LOG =
                LoggerFactory.getLogger(ConfigurationInvocationHandler.class);

        public ConfigurationInvocationHandler(PropertyResolver resolver,
                @SuppressWarnings("rawtypes") Class interfaceClazz,
                String configurationPropertyNamePrefix) {
            super();
            this.resolver = resolver;
            this.interfaceClazz = interfaceClazz;
            this.configurationPropertyNamePrefix = configurationPropertyNamePrefix;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            LOG.debug("invoking method: {}", method);
            @SuppressWarnings("rawtypes")
            final Class returnType = method.getReturnType();
            final String propertyName = this.getConfigurationPropertyName(method.getName());

            if (method.getName().startsWith("set")) {
                /**
                 * void setXXX
                 */
                throw new IllegalArgumentException(
                        "setters are not supported with this implementation");
            } else if (method.getName().startsWith("get") || method.getName().startsWith("is")) {
                /**
                 * String getXXX
                 */
                if (String.class.equals(returnType)) {
                    String defaultValue = getDefaultStringValue(method);
                    return this.getConfigurationPropertyValue(propertyName, defaultValue);
                } else if (int.class.equals(returnType)) {
                    int defaultValue = getDefaultIntValue(method);
                    return this.getConfigurationPropertyValue(propertyName, defaultValue);
                } else if (Integer.class.equals(returnType)) {
                    Integer defaultValue = getDefaultIntegerValue(method);
                    return this.getConfigurationPropertyValue(propertyName, defaultValue);
                } else if (long.class.equals(returnType)) {
                    Long defaultValue = getDefaultLongValue(method);
                    if (defaultValue == null) {
                        defaultValue = 0l;
                    }
                    return this.getConfigurationPropertyValue(propertyName, defaultValue);
                } else if (Long.class.equals(returnType)) {
                    Long defaultValue = getDefaultLongValue(method);
                    return this.getConfigurationPropertyValue(propertyName, defaultValue);
                } else if (boolean.class.equals(returnType)) {
                    Boolean defaultBooleanValue = getDefaultBooleanValue(method);
                    boolean defaultValue =
                            defaultBooleanValue != null ? defaultBooleanValue.booleanValue()
                                    : false;
                    return this.getBooleanConfigurationPropertyValue(propertyName, defaultValue);
                } else if (Boolean.class.equals(returnType)) {
                    Boolean defaultBooleanValue = getDefaultBooleanValue(method);
                    String value = this.getConfigurationPropertyValue(propertyName,
                            defaultBooleanValue != null ? Boolean.toString(defaultBooleanValue)
                                    : null);
                    if (value != null) {
                        return Boolean.valueOf(value);
                    } else {
                        return null;
                    }
                } else if (returnType.isEnum()) {
                    String defaultValue = null;
                    String enumValue =
                            this.getConfigurationPropertyValue(propertyName, defaultValue);
                    return enumValue != null ?

                            Enum.valueOf(returnType, enumValue.toUpperCase()) : null;
                } else if (byte[].class.equals(returnType)) {
                    String defaultValue = null;
                    String value = this.getConfigurationPropertyValue(propertyName, defaultValue);

                    if (value.startsWith("resource://")) {
                        String resourcePath = value.substring("resource://".length());
                        Path pathIn = Paths.get(
                                this.getClass().getClassLoader().getResource(resourcePath).toURI());
                        if (pathIn != null) {
                            byte[] data = Files.readAllBytes(pathIn);

                            return data;
                        }
                    }
                }

            } else if (method.getName().equals("toString")) {
                StringBuilder buf = new StringBuilder();
                buf.append(this.interfaceClazz.getName()).append("(");
                for (int i = 0; i < this.interfaceClazz.getMethods().length; i++) {
                    if (i > 0) {
                        buf.append(",");
                    }
                    buf.append("\n\t");
                    Method m = this.interfaceClazz.getMethods()[i];
                    @SuppressWarnings("rawtypes")
                    Class methodReturnType = m.getReturnType();
                    String methodPropertyName = this.getConfigurationPropertyName(m.getName());
                    if (String.class.equals(methodReturnType)) {
                        String defaultValue = getDefaultStringValue(method);
                        buf.append(methodPropertyName).append(":").append(this
                                .getConfigurationPropertyValue(methodPropertyName, defaultValue));
                    } else if (int.class.equals(methodReturnType)) {
                        int defaultValue = getDefaultIntValue(method);
                        buf.append(methodPropertyName).append(":").append(this
                                .getConfigurationPropertyValue(methodPropertyName, defaultValue));
                    } else if (Integer.class.equals(methodReturnType)) {
                        Integer defaultValue = getDefaultIntegerValue(method);
                        buf.append(methodPropertyName).append(":").append(this
                                .getConfigurationPropertyValue(methodPropertyName, defaultValue));
                    } else if (long.class.equals(methodReturnType)) {
                        Long defaultValue = getDefaultLongValue(method);
                        if (defaultValue == null) {
                            defaultValue = 0l;
                        }
                        buf.append(methodPropertyName).append(":").append(this
                                .getConfigurationPropertyValue(methodPropertyName, defaultValue));
                    } else if (Long.class.equals(methodReturnType)) {
                        Long defaultValue = getDefaultLongValue(method);
                        buf.append(methodPropertyName).append(":").append(this
                                .getConfigurationPropertyValue(methodPropertyName, defaultValue));
                    } else if (boolean.class.equals(methodReturnType)) {
                        Boolean defaultBooleanValue = getDefaultBooleanValue(method);
                        boolean defaultValue =
                                defaultBooleanValue != null ? defaultBooleanValue.booleanValue()
                                        : false;
                        buf.append(methodPropertyName).append(":").append(
                                this.getBooleanConfigurationPropertyValue(methodPropertyName,
                                        defaultValue));
                    } else if (Boolean.class.equals(methodReturnType)) {
                        Boolean defaultBooleanValue = getDefaultBooleanValue(method);
                        String value = this.getConfigurationPropertyValue(propertyName,
                                defaultBooleanValue != null ? Boolean.toString(defaultBooleanValue)
                                        : null);
                        buf.append(methodPropertyName).append(":").append(
                                this.getConfigurationPropertyValue(methodPropertyName, value));
                    } else if (methodReturnType.isEnum()) {
                        String defaultValue = null;
                        String enumValue =
                                this.getConfigurationPropertyValue(propertyName, defaultValue);
                        buf.append(methodPropertyName).append(":").append(enumValue);
                    }
                }
                buf.append(")");
                return buf.toString();
            } else {
                throw new RuntimeException("unimplemented method:" + method.toString());
            }

            LOG.error("could not determine configuration value for " + this.interfaceClazz.getName()
                    + "." + method.getName() + " returning object of type '" + returnType.getName()
                    + "'");
            return null;
        }

        private static String getDefaultStringValue(Method method) {
            DefaultStringValue defaulValueString = method.getAnnotation(DefaultStringValue.class);
            String defaultValue = null;
            if (defaulValueString != null) {
                defaultValue = defaulValueString.value();
            }
            return defaultValue;
        }

        private static Integer getDefaultIntegerValue(Method method) {
            DefaultIntValue defaultIntValue = method.getAnnotation(DefaultIntValue.class);
            Integer defaultValue = null;
            if (defaultIntValue != null) {
                defaultValue = defaultIntValue.value();
            }
            return defaultValue;
        }

        private static int getDefaultIntValue(Method method) {
            DefaultIntValue defaultIntValue = method.getAnnotation(DefaultIntValue.class);
            int defaultValue = 0;
            if (defaultIntValue != null) {
                defaultValue = defaultIntValue.value();
            }
            return defaultValue;
        }

        private static Long getDefaultLongValue(Method method) {
            DefaultLongValue defaultLongValue = method.getAnnotation(DefaultLongValue.class);
            Long defaultValue = null;
            if (defaultLongValue != null) {
                defaultValue = defaultLongValue.value();
            }
            return defaultValue;
        }

        private static Boolean getDefaultBooleanValue(Method method) {
            DefaultBooleanValue defaultBooleanValue =
                    method.getAnnotation(DefaultBooleanValue.class);
            Boolean defaultValue = null;
            if (defaultBooleanValue != null) {
                defaultValue = defaultBooleanValue.value();
            }
            return defaultValue;
        }

        private String getConfigurationPropertyValue(String propertyName, String defaultValue) {
            String value = this.resolver.getProperty(propertyName, defaultValue);
            LOG.debug("configuration property '" + propertyName + "': " + value + " (default:'"
                    + defaultValue + "')");

            return value;
        }

        private boolean getBooleanConfigurationPropertyValue(String propertyName,
                boolean defaultValue) {
            boolean value = "TRUE".equalsIgnoreCase(this.getConfigurationPropertyValue(propertyName,
                    new Boolean(defaultValue).toString()));
            LOG.debug("configuration property '" + propertyName + "': " + value + " (default:'"
                    + defaultValue + "')");
            return value;
        }

        private Boolean getBooleanConfigurationPropertyValue(String propertyName,
                Boolean defaultValue) {
            String value = this.getConfigurationPropertyValue(propertyName,
                    defaultValue != null ? defaultValue.toString() : null);
            LOG.debug("configuration property '" + propertyName + "': " + value + " (default:'"
                    + defaultValue + "')");

            if (value == null) {
                return null;
            } else {
                return "true".equalsIgnoreCase(value);
            }
        }

        private int getConfigurationPropertyValue(String propertyName, int defaultValue)
                throws NumberFormatException {
            return Integer
                    .parseInt(this.getConfigurationPropertyValue(propertyName, "" + defaultValue));
        }

        private Long getConfigurationPropertyValue(String propertyName, Long defaultValue)
                throws NumberFormatException {
            String value = this.getConfigurationPropertyValue(propertyName,
                    defaultValue != null ? "" + defaultValue : null);
            if (value != null) {
                return Long.parseLong(value);
            } else {
                return null;
            }
        }

        protected String getConfigurationPropertyName(String methodName) {
            String methodNameWithoutGet = null;
            if (methodName.startsWith("get")) {
                methodNameWithoutGet = methodName.substring("get".length());
            } else if (methodName.startsWith("set")) {
                methodNameWithoutGet = methodName.substring("set".length());
            } else if (methodName.startsWith("is")) {
                methodNameWithoutGet = methodName.substring("is".length());
            } else {
                methodNameWithoutGet = methodName;
            }

            StringBuilder buf = new StringBuilder();
            if (this.configurationPropertyNamePrefix != null) {
                if (this.configurationPropertyNamePrefix.endsWith(".")) {
                    buf.append(this.configurationPropertyNamePrefix);
                } else {
                    buf.append(this.configurationPropertyNamePrefix).append(".");
                }
            }
            for (int i = 0; i < methodNameWithoutGet.length(); i++) {
                String c = methodNameWithoutGet.substring(i, i + 1);
                if (c.toUpperCase().equals(c) && i > 0) {
                    buf.append(".");
                    buf.append(c.toLowerCase());
                } else {
                    buf.append(c.toLowerCase());
                }
            }

            return buf.toString();
        }
    }

    protected abstract PropertyResolver getResolver();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <E extends Configuration> E getInstance(Class<E> clazz) {
        Class[] proxyInterfaces = new Class[] { clazz };

        PropertyResolver resolver = this.getResolver();

        String configurationPropertyNamePrefix = null;

        ConfigurationPrefix prefixAnnotation = clazz.getAnnotation(ConfigurationPrefix.class);
        if (prefixAnnotation != null) {
            configurationPropertyNamePrefix = prefixAnnotation.value();
        }
        Object configuration = Proxy.newProxyInstance(clazz.getClassLoader(), proxyInterfaces,
                new ConfigurationInvocationHandler(resolver, clazz,
                        configurationPropertyNamePrefix));

        return (E) configuration;
    }

}
