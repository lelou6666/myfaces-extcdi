/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.extensions.cdi.core.api.util;

<<<<<<< HEAD
import javax.enterprise.inject.Typed;
import java.security.AccessController;
import java.security.PrivilegedAction;
=======
>>>>>>> refs/remotes/apache/branch_for_jsf_1_2
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.net.URL;

/**
<<<<<<< HEAD
 * Util methods for classes, {@link ClassLoader} and {@link Manifest} handling
 */
@Typed()
public abstract class ClassUtils
{
    /**
     * Constructor which prevents the instantiation of this class
     */
    private ClassUtils()
    {
        // prevent instantiation
    }

    /**
     * Detect the right ClassLoader.
     * The lookup order is determined by:
     * <ol>
     * <li>ContextClassLoader of the current Thread</li>
     * <li>ClassLoader of the given Object 'o'</li>
     * <li>ClassLoader of this very ClassUtils class</li>
     * </ol>
     *
     * @param o if not <code>null</code> it may get used to detect the classloader.
     * @return The {@link ClassLoader} which should get used to create new instances
     */
    public static ClassLoader getClassLoader(Object o)
    {
        if (System.getSecurityManager() != null)
        {
            return AccessController.doPrivileged(new GetClassLoaderAction(o));
        }
        else
        {
            return getClassLoaderInternal(o);
        }
    }

    static class GetClassLoaderAction implements PrivilegedAction<ClassLoader>
    {
        private Object object;
        GetClassLoaderAction(Object object)
        {
            this.object = object;
        }

        @Override
        public ClassLoader run()
        {
            try
            {
                return getClassLoaderInternal(object);
            }
            catch (Exception e)
            {
                return null;
            }
        }
    }

    private static ClassLoader getClassLoaderInternal(Object o)
    {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        if (loader == null && o != null)
        {
            loader = o.getClass().getClassLoader();
        }

        if (loader == null)
        {
            loader = ClassUtils.class.getClassLoader();
        }

        return loader;
    }

    /**
     * Tries to load a class based on the given name and interface or abstract class.
     * @param name name of the concrete class
     * @param targetType target type (interface or abstract class)
     * @param <T> current type
     * @return loaded class or null if it isn't in the classpath
     */
    public static <T> Class<T> tryToLoadClassForName(String name, Class<T> targetType)
    {
        return (Class<T>) tryToLoadClassForName(name);
    }

    /**
     * Tries to load a class based on the given name
     * @param name name of the class
     * @return loaded class or null if it isn't in the classpath
     */
=======
 * keep in sync with extval!
 *
 * @author Gerhard Petracek
 */
public class ClassUtils
{
>>>>>>> refs/remotes/apache/branch_for_jsf_1_2
    public static Class tryToLoadClassForName(String name)
    {
        try
        {
            return loadClassForName(name);
        }
        catch (ClassNotFoundException e)
        {
            //do nothing - it's just a try
            return null;
        }
    }

<<<<<<< HEAD
    /**
     * Loads class for the given name
     * @param name name of the class
     * @return loaded class
     * @throws ClassNotFoundException if the class can't be loaded
     */
=======
>>>>>>> refs/remotes/apache/branch_for_jsf_1_2
    public static Class loadClassForName(String name) throws ClassNotFoundException
    {
        try
        {
            // Try WebApp ClassLoader first
            return Class.forName(name, false, // do not initialize for faster startup
<<<<<<< HEAD
                    getClassLoader(null));
=======
                Thread.currentThread().getContextClassLoader());
>>>>>>> refs/remotes/apache/branch_for_jsf_1_2
        }
        catch (ClassNotFoundException ignore)
        {
            // fallback: Try ClassLoader for ClassUtils (i.e. the myfaces.jar lib)
            return Class.forName(name, false, // do not initialize for faster startup
<<<<<<< HEAD
                    ClassUtils.class.getClassLoader());
        }
    }

    /**
     * Instantiates a given class via the default constructor
     * @param targetClass class which should be instantiated
     * @param <T> current type
     * @return created instance or null if the instantiation failed
     */
=======
                ClassUtils.class.getClassLoader());
        }
    }

>>>>>>> refs/remotes/apache/branch_for_jsf_1_2
    public static <T> T tryToInstantiateClass(Class<T> targetClass)
    {
        try
        {
            return targetClass.newInstance();
        }
<<<<<<< HEAD
        catch (Exception e)
=======
        catch (Throwable t)
>>>>>>> refs/remotes/apache/branch_for_jsf_1_2
        {
            //do nothing - it was just a try
        }
        return null;
    }

<<<<<<< HEAD
    /**
     * Tries to instantiate a class for the given name and type via the default constructor
     * @param className name of the class
     * @param targetType target type
     * @param <T> current type
     * @return created instance or null if the instantiation failed
     */
    public static <T> T tryToInstantiateClassForName(String className, Class<T> targetType)
    {
        Object result = tryToInstantiateClassForName(className);

        //noinspection unchecked
        return result != null ? (T) result : null;
    }

    /**
     * Tries to instantiate a class for the given name via the default constructor
     * @param className name of the class
     * @return created instance or null if the instantiation failed
     */
=======
    public static <T> T tryToInstantiateClass(Class targetClass, Class<T> type)
    {
        try
        {
            return (T)targetClass.newInstance();
        }
        catch (Throwable t)
        {
            //do nothing - it was just a try
        }
        return null;
    }

>>>>>>> refs/remotes/apache/branch_for_jsf_1_2
    public static Object tryToInstantiateClassForName(String className)
    {
        try
        {
            return instantiateClassForName(className);
        }
<<<<<<< HEAD
        catch (Exception e)
=======
        catch (Throwable t)
>>>>>>> refs/remotes/apache/branch_for_jsf_1_2
        {
            //do nothing - it was just a try
        }
        return null;
    }

<<<<<<< HEAD
    /**
     * Creates an instance for the given class-name
     * @param className name of the class which should be instantiated
     * @return created instance
     * @throws ClassNotFoundException if the instantiation failed
     * @throws IllegalAccessException if the instantiation failed
     * @throws InstantiationException if the instantiation failed
     */
    public static Object instantiateClassForName(String className)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException
=======
    public static Object instantiateClassForName(String className)
        throws ClassNotFoundException, IllegalAccessException, InstantiationException
>>>>>>> refs/remotes/apache/branch_for_jsf_1_2
    {
        return loadClassForName(className).newInstance();
    }

<<<<<<< HEAD
    /**
     * Reads the version of the jar which contains the given class
     * @param targetClass class within the jar
     * @return version-string which has been found in the manifest or null if there is no version information available
     */
    public static String getJarVersion(Class targetClass)
    {
        String manifestFileLocation = getManifestFileLocationOfClass(targetClass);

        try
        {
            return new Manifest(new URL(manifestFileLocation).openStream())
                    .getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Reads the VCS revision which was used for creating the jar
     * @param targetClass class within the jar
     * @return revision-string which has been found in the manifest or null if there is no information available
     */
    public static String getRevision(Class targetClass)
    {
        String manifestFileLocation = getManifestFileLocationOfClass(targetClass);
=======
    public static String getJarVersion(Class targetClass)
    {
        String classFilePath = targetClass.getCanonicalName().replace('.', '/') + ".class";
        String manifestFilePath = "/META-INF/MANIFEST.MF";

        String classLocation = targetClass.getResource(targetClass.getSimpleName() + ".class").toString();
        String manifestFileLocation = classLocation
                .substring(0, classLocation.indexOf(classFilePath) - 1) + manifestFilePath;
>>>>>>> refs/remotes/apache/branch_for_jsf_1_2

        try
        {
            return new Manifest(new URL(manifestFileLocation).openStream())
<<<<<<< HEAD
                    .getMainAttributes().getValue("Revision");
        }
        catch (Exception e)
=======
                    .getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        }
        catch (Throwable t)
>>>>>>> refs/remotes/apache/branch_for_jsf_1_2
        {
            return null;
        }
    }
<<<<<<< HEAD

    private static String getManifestFileLocationOfClass(Class targetClass)
    {
        String manifestFileLocation;

        try
        {
            manifestFileLocation = getManifestLocation(targetClass);
        }
        catch (Exception e)
        {
            //in this case we have a proxy
            manifestFileLocation = getManifestLocation(targetClass.getSuperclass());
        }
        return manifestFileLocation;
    }

    private static String getManifestLocation(Class targetClass)
    {
        String classFilePath = targetClass.getCanonicalName().replace('.', '/') + ".class";
        String manifestFilePath = "/META-INF/MANIFEST.MF";

        String classLocation = targetClass.getResource(targetClass.getSimpleName() + ".class").toString();
        return classLocation.substring(0, classLocation.indexOf(classFilePath) - 1) + manifestFilePath;
    }
=======
>>>>>>> refs/remotes/apache/branch_for_jsf_1_2
}
