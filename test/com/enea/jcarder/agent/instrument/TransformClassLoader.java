package com.enea.jcarder.agent.instrument;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.enea.jcarder.agent.instrument.ClassTransformer;

/**
 * This class loader is able to transform classes while they are loaded.
 * It uses the classpath from its parent class loader.
 *
 * The class loader that loaded the TransformClassLoader will be used
 * as parrent class loader.
 *
 * This TransformClassLoader is not able to transform classes that begin
 * with java.*.
 */
public final class TransformClassLoader extends ClassLoader {
    private final ClassFileTransformer mTransformer;
    private final ClassLoader mParentClassLoader;
    private final Pattern mClassNamePattern;
    private final Set<String> mTansformedClasses = new HashSet<String>();

    /**
     * Create a TransformClassLoader that only transforms classes that are
     * explicitly loaded with the transform(Class) method. All other
     * requests are forwarded to the parent class loader.
     *
     */
    public TransformClassLoader(final ClassFileTransformer transformer) {
        this(transformer, null);
    }

    /**
     * Create a TransformClassLoader that transforms all classes with names
     * that match the given classNamePattern regexp. All other requests are
     * forwarded to the parent class loader.
     */
    public TransformClassLoader(final ClassFileTransformer transformer,
                                final Pattern classNamePattern) {
        mTransformer = transformer;
        mParentClassLoader = TransformClassLoader.class.getClassLoader();
        mClassNamePattern = classNamePattern;
    }

    /**
     * This method transforms a class even if its name does not match the
     * classNamePattern regexp that this TransformClassLoader may have been
     * configured
     * with.
     */
    public Class<?> transform(Class clazz)
    throws ClassNotFoundException,
           IllegalClassFormatException,
           ClassNotTransformedException {
        byte[] classBuffer = ClassTransformer.getClassBytes(clazz);
        return createTransformedClass(clazz.getName(), classBuffer);
    }


    /**
     * Load a new non-instrumented class with this class loader.
     */
    public Class<?> loadNew(Class clazz) throws ClassNotFoundException {
        byte[] classBuffer = ClassTransformer.getClassBytes(clazz);
        return defineClass(clazz.getName(),
                           classBuffer,
                           0,
                           classBuffer.length);
    }


    @Override
    protected synchronized Class<?> loadClass(String className, boolean resolve)
    throws ClassNotFoundException {
        Class c = findLoadedClass(className);
        if (c == null) {
            if (isClassNameToBeTransformed(className)) {
                c = findClass(className);
            } else {
                c = mParentClassLoader.loadClass(className);
            }
        }
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }

    private boolean isClassNameToBeTransformed(String className) {
        return mClassNamePattern != null
               && mClassNamePattern.matcher(className).matches();
    }

    @Override
    protected synchronized Class<?> findClass(String className)
    throws ClassNotFoundException {
        byte[] classBuffer = ClassTransformer.getClassBytes(className,
                                                            mParentClassLoader);
        try {
            return createTransformedClass(className, classBuffer);
        } catch (IllegalClassFormatException e) {
            throw new ClassNotFoundException(e.getMessage(), e);
        } catch (ClassNotTransformedException e) {
            throw new ClassNotFoundException(e.getMessage(), e);
        }
    }

    private Class<?> createTransformedClass(String className,
                                            byte[] classBuffer)
    throws ClassNotFoundException,
           IllegalClassFormatException,
           ClassNotTransformedException {
        byte[] transformedClassBuffer = mTransformer.transform(this,
                                                               className,
                                                               null,
                                                               null,
                                                               classBuffer);
        if (transformedClassBuffer == null) {
            throw new ClassNotTransformedException("Class not transformed: "
                                                   + className);
        }
        mTansformedClasses.add(className);
        return defineClass(className,
                           transformedClassBuffer,
                           0,
                           transformedClassBuffer.length);
    }

    public boolean hasBeenTransformed(String className) {
        return mTansformedClasses.contains(className);
    }
}