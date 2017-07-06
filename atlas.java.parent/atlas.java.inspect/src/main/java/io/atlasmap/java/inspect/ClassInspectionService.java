/**
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.java.inspect;

import io.atlasmap.java.v2.AtlasJavaModelFactory;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.java.v2.JavaEnumField;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.FieldStatus;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.StringList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.*;

public class ClassInspectionService implements Serializable {

	private static final long serialVersionUID = 6634950157813704038L;
	private static final Logger logger = LoggerFactory.getLogger(ClassInspectionService.class);
	public static final int MAX_REENTRY_LIMIT = 1;
	public static final int MAX_ARRAY_DIM_LIMIT = 256; // JVM specification limit

	private List<String> primitiveClasses = new ArrayList<String>(Arrays.asList("byte", "short", "int", "long", "float", "double", "boolean", "char"));
	private List<String> boxedPrimitiveClasses = new ArrayList<String>(Arrays.asList("java.lang.Byte", "java.lang.Short", "java.lang.Integer", "java.lang.Long", "java.lang.Float", "java.lang.Double", "java.lang.Boolean", "java.lang.Character", "java.lang.String"));
	private List<String> fieldBlacklist = new ArrayList<String>(Arrays.asList("serialVersionUID"));
	private List<String> classNameBlacklist = new ArrayList<String>();
	private Boolean disableProtectedOnlyFields = false;
	private Boolean disablePrivateOnlyFields = false;
	private Boolean disablePublicOnlyFields = false;
	private Boolean disablePublicGetterSetterFields = false;

	public List<String> getPrimitiveClasses() {
		return this.primitiveClasses;
	}

	public List<String> getBoxedPrimitiveClasses() {
		return this.boxedPrimitiveClasses;
	}

	public List<String> getClassNameBlacklist() {
		return this.classNameBlacklist;
	}

	public List<String> getFieldBlacklist() {
		return this.fieldBlacklist;
	}

	public Boolean getDisableProtectedOnlyFields() {
		return disableProtectedOnlyFields;
	}

	public void setDisableProtectedOnlyFields(Boolean disableProtectedOnlyFields) {
		this.disableProtectedOnlyFields = disableProtectedOnlyFields;
	}

	public Boolean getDisablePrivateOnlyFields() {
		return disablePrivateOnlyFields;
	}

	public void setDisablePrivateOnlyFields(Boolean disablePrivateOnlyFields) {
		this.disablePrivateOnlyFields = disablePrivateOnlyFields;
	}

	public Boolean getDisablePublicOnlyFields() {
		return disablePublicOnlyFields;
	}

	public void setDisablePublicOnlyFields(Boolean disablePublicOnlyFields) {
		this.disablePublicOnlyFields = disablePublicOnlyFields;
	}

	public Boolean getDisablePublicGetterSetterFields() {
		return disablePublicGetterSetterFields;
	}

	public void setDisablePublicGetterSetterFields(Boolean disablePublicGetterSetterFields) {
		this.disablePublicGetterSetterFields = disablePublicGetterSetterFields;
	}

	public Map<String, JavaClass> inspectPackage(String packageName) throws ClassNotFoundException, InspectionException {
		return inspectPackages(Arrays.asList(packageName), false);
	}

	public Map<String, JavaClass> inspectPackages(String packageName, boolean inspectChildren) throws ClassNotFoundException, InspectionException {
		return inspectPackages(Arrays.asList(packageName), inspectChildren);
	}

	public Map<String, JavaClass> inspectPackages(List<String> packages, boolean inspectChildren) throws ClassNotFoundException, InspectionException {
		packages = inspectChildren ? findChildPackages(packages) : packages;
		Map<String, JavaClass> classes = new HashMap<>();
		for (String p : packages) {
			classes.putAll(inspectClasses(findClassesForPackage(p)));
		}
		return classes;
	}

	public List<String> findClassesForPackage(String packageName) {
		List<String> classNames = new LinkedList<>();
		List<Class<?>> classes = ClassFinder.find(packageName);
		if (classes != null) {
			for (Class<?> clz : classes) {
				classNames.add(clz.getCanonicalName());
			}
		}
		return classNames;
	}

	public List<String> findChildPackages(List<String> packages) {
		List<String> foundPackages = new LinkedList<>();
		for (String p : packages) {
			foundPackages.addAll(findChildPackages(p));
		}
		return foundPackages;
	}

	public List<String> findChildPackages(String packageName) {
		List<String> packageNames = new LinkedList<>();
		Package originalPackage = Package.getPackage(packageName);
		Package[] allPackages = Package.getPackages();
		if (allPackages != null) {
			for (Package tmpPackage : allPackages) {
				if (tmpPackage.getName().startsWith(originalPackage.getName())) {
					packageNames.add(tmpPackage.getName());
				}
			}
		}
		return packageNames;
	}

	public Map<String,JavaClass> inspectClasses(List<String> classNames) throws InspectionException {
		Map<String,JavaClass> classes = new HashMap<>();
		for (String c : classNames) {
			JavaClass d = inspectClass(c);
			classes.put(d.getClassName(), d);
		}
		return classes;
	}

	public JavaClass inspectClass(String className) throws InspectionException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if( loader == null ) {
            loader = getClass().getClassLoader();
        }
        return inspectClass(loader, className);
	}

    public JavaClass inspectClass(String className, String classpath) throws InspectionException {
		if(className == null || classpath == null) {
			throw new InspectionException("ClassName and Classpath must be specified");
		}
		// URLClassLoader is auto closable.
        try( URLClassLoader classLoader = createClassLoader(classpath) ) {
            return inspectClass(classLoader, className);
        } catch (IOException e) {
            throw new InspectionException("Could not close the classloader");
        }
    }

    private URLClassLoader createClassLoader(String classpath) {
        URL[] urls = Arrays.stream(classpath.split(":")).map(x -> {
            try {
                return Paths.get(x).toUri().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException("Could not get URL to jar file: " + x, e);
            }
        }).toArray(x -> new URL[x]);

        // In a Spring Boot app, our classes are NOT loaded by the System classloader!  The System
        // class loader just has a few launcher classes.  So lets make the parent of the classloader
        // System classloader since that has a much smaller conflicting with the classes that we
        // are goign to introspect.
        ClassLoader parent = ClassLoader.getSystemClassLoader();
        return new URLClassLoader(urls, parent);
    }

    private JavaClass inspectClass(ClassLoader loader, String className) throws InspectionException {
        if(className == null || loader == null) {
            throw new InspectionException("ClassName and ClassLoader must be specified");
        }
        Class<?> clazz;
        JavaClass d = null;
        try {
            clazz = loader.loadClass(className);
            d = inspectClass(clazz);
        } catch (ClassNotFoundException cnfe) {
            d = AtlasJavaModelFactory.createJavaClass();
            d.setClassName(className);
            d.setStatus(FieldStatus.NOT_FOUND);
        }
        return d;
    }

    public JavaClass inspectClass(Class<?> clazz) {
        ClassLoader loader = clazz.getClassLoader();
        if( loader==null ) {
            loader = getClass().getClassLoader();
        }
        return inspectClass(loader, clazz);
    }

	public JavaClass inspectClass(ClassLoader loader, Class<?> clazz) {
		if(clazz == null) {
			throw new IllegalArgumentException("Class must be specified");
		}

		JavaClass javaClass = AtlasJavaModelFactory.createJavaClass();
		inspectClass(loader, clazz, javaClass, new HashSet<String>());
		return javaClass;
	}

	protected void inspectClass(ClassLoader loader, Class<?> clazz, JavaClass javaClass, Set<String> cachedClasses) {

		Class<?> clz = clazz;
		if(clazz.isArray()) {
			javaClass.setArray(clazz.isArray());
			javaClass.setArrayDimensions(detectArrayDimensions(clazz));
			clz = detectArrayClass(clazz);
		} else {
			if(javaClass.isArray() == null) {
				javaClass.setArray(clazz.isArray());
			}
			clz = clazz;
		}

		javaClass.setClassName(clz.getCanonicalName());
		javaClass.setPackageName((clz.getPackage() != null ? clz.getPackage().getName() : null));
		javaClass.setAnnotation(clz.isAnnotation());
		javaClass.setAnnonymous(clz.isAnonymousClass());
		javaClass.setEnumeration(clz.isEnum());
		javaClass.setInterface(clz.isInterface());
		javaClass.setLocalClass(clz.isLocalClass());
		javaClass.setMemberClass(clz.isMemberClass());
		javaClass.setPrimitive(clz.isPrimitive());
		javaClass.setSynthetic(clz.isSynthetic());

		if(javaClass.getUri() == null) {
			javaClass.setUri(String.format(AtlasJavaModelFactory.URI_FORMAT, clz.getCanonicalName()));
		}

		Field[] fields = clz.getDeclaredFields();
		if (fields != null && !javaClass.isEnumeration()) {
			for (Field f : fields) {
				if (Enum.class.isAssignableFrom(f.getType())) {
					continue;
				}
				JavaField s = inspectField(loader, f, cachedClasses);

				if(getFieldBlacklist().contains(f.getName())) {
					s.setStatus(FieldStatus.BLACK_LIST);
				}

				if(s.getGetMethod() == null && s.getSetMethod() == null) {
					if(s.getModifiers().getModifier().contains(io.atlasmap.java.v2.Modifier.PRIVATE) && !getDisablePrivateOnlyFields()) {
						javaClass.getJavaFields().getJavaField().add(s);
					} else if(s.getModifiers().getModifier().contains(io.atlasmap.java.v2.Modifier.PROTECTED) && !getDisableProtectedOnlyFields()) {
						javaClass.getJavaFields().getJavaField().add(s);
					} else if(s.getModifiers().getModifier().contains(io.atlasmap.java.v2.Modifier.PUBLIC) && !getDisablePublicOnlyFields()) {
						javaClass.getJavaFields().getJavaField().add(s);
					}
				} else if(!getDisablePublicGetterSetterFields()) {
					javaClass.getJavaFields().getJavaField().add(s);
				}
			}
		}

		Object[] enumConstants = clz.getEnumConstants();
		if (enumConstants != null) {
			javaClass.setEnumeration(true);
			for (Object o : enumConstants) {
				JavaEnumField out = new JavaEnumField();
				if (o instanceof Enum) {
					Enum<?> in = (Enum<?>) o;
					out.setName(in.name());
					out.setOrdinal(in.ordinal());
					javaClass.getJavaEnumFields().getJavaEnumField().add(out);
					out.setStatus(FieldStatus.SUPPORTED);
				} else {
					out.setClassName(o.getClass().getCanonicalName());
					out.setStatus(FieldStatus.ERROR);
				}
			}
		} else {
			javaClass.setEnumeration(false);
		}

		Method[] methods = clz.getDeclaredMethods();
		if (methods != null && !javaClass.isEnumeration()) {
			for (Method m : methods) {
				JavaField s = AtlasJavaModelFactory.createJavaField();
				s.setName(m.getName());
				s.setSynthetic(m.isSynthetic());

				if(m.isVarArgs() || m.isBridge() || m.isSynthetic() || m.isDefault()) {
					s.setStatus(FieldStatus.UNSUPPORTED);
					logger.warn("VarArg, Bridge, Synthetic or Default method " + m.getName() + " detected");
					continue;
				} else {
					s.setSynthetic(m.isSynthetic());
				}

				if(m.getName().startsWith("get") || m.getName().startsWith("is")) {
					s = inspectGetMethod(loader, m, s, cachedClasses);
				}

				if(m.getName().startsWith("set")) {
					s = inspectSetMethod(loader, m, s, cachedClasses);
				}

				boolean found = false;
				for(int i=0; i < javaClass.getJavaFields().getJavaField().size(); i++) {
					JavaField exists = javaClass.getJavaFields().getJavaField().get(i);
					if(s.getName().equals(exists.getName())) {
						found = true;

						// Merge get/set method info for interfaces that don't have fields
						if(exists.getGetMethod() == null && s.getGetMethod() != null) {
							exists.setGetMethod(s.getGetMethod());
						}
						if(exists.getSetMethod() == null && s.getSetMethod() != null) {
							exists.setSetMethod(s.getSetMethod());
						}
					}
				}

				if(found) {
					if(logger.isDebugEnabled()) {
						logger.debug("Field already defined for method: " + m.getName() + " class: " + clz.getName());
					}
				} else if(s.getGetMethod() != null || s.getSetMethod() != null) {
						javaClass.getJavaFields().getJavaField().add(s);
				} else {
					if(logger.isDebugEnabled()) {
						logger.debug("Ignoring non-field method: " + m.getName() + " class: " + clz.getName());
					}
				}

			}
		}

		//TODO: annotations, generics, enums, class modifiers (public, synchronized, etc),
		//more of these here: https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html#isPrimitive--
		//TODO: exceptions
		//TODO: lists
		//return javaClass;
	}

	protected JavaField inspectGetMethod(ClassLoader loader, Method m, JavaField s, Set<String> cachedClasses) {
        s.setName(StringUtil.removeGetterAndLowercaseFirstLetter(m.getName()));

		if(m.getParameterCount() != 0) {
			s.setStatus(FieldStatus.UNSUPPORTED);
			return s;
		}

		if(m.getReturnType().equals(Void.TYPE)) {
			s.setStatus(FieldStatus.UNSUPPORTED);
			return s;
		}

		Class<?> returnType = m.getReturnType();
		if(returnType.isArray()) {
			s.setArray(true);
			s.setArrayDimensions(detectArrayDimensions(returnType));
			returnType = detectArrayClass(returnType);
		} else {
			s.setArray(false);
		}

		s.setClassName(returnType.getCanonicalName());
		s.setGetMethod(m.getName());
		if(isFieldPrimitive(returnType.getCanonicalName())) {
			s.setPrimitive(true);
			s.setType(AtlasModelFactory.fieldTypeFromClassName(returnType.getCanonicalName()));
			s.setStatus(FieldStatus.SUPPORTED);
		} else if(isFieldBoxedPrimitive(returnType.getCanonicalName())) {
			s.setPrimitive(true);
			s.setType(AtlasModelFactory.fieldTypeFromClassName(returnType.getCanonicalName()));
			s.setStatus(FieldStatus.SUPPORTED);
		} else {
			s.setPrimitive(false);
			s.setType(FieldType.COMPLEX);

			Class<?> complexClazz = null;
			JavaClass tmpField = convertJavaFieldToJavaClass(s);
			s = tmpField;

			if(returnType.getCanonicalName() == null) {
				s.setStatus(FieldStatus.UNSUPPORTED);
			} else if(!cachedClasses.contains(returnType.getCanonicalName())) {
				try {
					complexClazz = loader.loadClass(returnType.getCanonicalName());
					cachedClasses.add(returnType.getCanonicalName());
					inspectClass(loader, complexClazz, tmpField, cachedClasses);
					if(tmpField.getStatus() == null) {
						s.setStatus(FieldStatus.SUPPORTED);
					}
				} catch (ClassNotFoundException cnfe) {
					s.setStatus(FieldStatus.NOT_FOUND);
				}
			} else {
				s.setStatus(FieldStatus.CACHED);
			}
		}

		return s;
	}

	protected JavaField inspectSetMethod(ClassLoader loader, Method m, JavaField s, Set<String> cachedClasses) {
        s.setName(StringUtil.removeSetterAndLowercaseFirstLetter(m.getName()));

		if(m.getParameterCount() != 1) {
			s.setStatus(FieldStatus.UNSUPPORTED);
			return s;
		}

		if(!m.getReturnType().equals(Void.TYPE)) {
			s.setStatus(FieldStatus.UNSUPPORTED);
			return s;
		}

		Class<?>[] params = m.getParameterTypes();
		if(params == null || params.length != 1) {
			s.setStatus(FieldStatus.UNSUPPORTED);
			return s;
		}

		Class<?> paramType = params[0];
		if(paramType.isArray()) {
			s.setArray(true);
			s.setArrayDimensions(detectArrayDimensions(paramType));
			paramType = detectArrayClass(paramType);
		} else {
			s.setArray(false);
		}

		s.setClassName(paramType.getCanonicalName());
		s.setSetMethod(m.getName());
		if(isFieldPrimitive(paramType.getCanonicalName())) {
			s.setPrimitive(true);
			s.setType(AtlasModelFactory.fieldTypeFromClassName(paramType.getCanonicalName()));
			s.setStatus(FieldStatus.SUPPORTED);
		} else if(isFieldBoxedPrimitive(paramType.getCanonicalName())) {
			s.setPrimitive(true);
			s.setType(AtlasModelFactory.fieldTypeFromClassName(paramType.getCanonicalName()));
			s.setStatus(FieldStatus.SUPPORTED);
		} else {
			s.setPrimitive(false);
			s.setType(FieldType.COMPLEX);

			Class<?> complexClazz = null;
			JavaClass tmpField = convertJavaFieldToJavaClass(s);
			s = tmpField;

			if(paramType.getCanonicalName() == null) {
				s.setStatus(FieldStatus.UNSUPPORTED);
			} else if(!cachedClasses.contains(paramType.getCanonicalName())) {
				try {
					complexClazz = loader.loadClass(paramType.getCanonicalName());
					cachedClasses.add(paramType.getCanonicalName());
					inspectClass(loader, complexClazz, tmpField, cachedClasses);
					if(tmpField.getStatus() == null) {
						s.setStatus(FieldStatus.SUPPORTED);
					}
				} catch (ClassNotFoundException cnfe) {
					s.setStatus(FieldStatus.NOT_FOUND);
				}
			} else {
				s.setStatus(FieldStatus.CACHED);
			}
		}

		return s;
	}

	protected JavaField inspectField(ClassLoader loader, Field f, Set<String> cachedClasses) {
        JavaField s = AtlasJavaModelFactory.createJavaField();
		Class<?> clazz = f.getType();

		if(clazz.isArray()) {
			s.setArray(true);
			s.setArrayDimensions(detectArrayDimensions(clazz));
			clazz = detectArrayClass(clazz);
		} else {
			s.setArray(false);
		}

		s.setName(f.getName());

		if(isFieldPrimitive(clazz.getCanonicalName())) {
			s.setPrimitive(true);
			s.setType(AtlasModelFactory.fieldTypeFromClassName(clazz.getCanonicalName()));
			s.setStatus(FieldStatus.SUPPORTED);
		} else if(isFieldBoxedPrimitive(clazz.getCanonicalName())) {
			s.setPrimitive(true);
			s.setType(AtlasModelFactory.fieldTypeFromClassName(clazz.getCanonicalName()));
			s.setStatus(FieldStatus.SUPPORTED);
		} else {
			s.setPrimitive(false);
			s.setType(FieldType.COMPLEX);

			Class<?> complexClazz = null;
			JavaClass tmpField = convertJavaFieldToJavaClass(s);
			s = tmpField;

			if(clazz.getCanonicalName() == null) {
				s.setStatus(FieldStatus.UNSUPPORTED);
			} else if(!cachedClasses.contains(clazz.getCanonicalName())) {
				try {
					complexClazz = loader.loadClass(clazz.getCanonicalName());
					cachedClasses.add(clazz.getCanonicalName());
					inspectClass(loader, complexClazz, tmpField, cachedClasses);
					if(tmpField.getStatus() == null) {
						s.setStatus(FieldStatus.SUPPORTED);
					}
				} catch (ClassNotFoundException cnfe) {
					s.setStatus(FieldStatus.NOT_FOUND);
				}
			} else {
				s.setStatus(FieldStatus.CACHED);
			}
		}

		s.setClassName(clazz.getCanonicalName());
		s.setSynthetic(f.isSynthetic());

		Annotation[] annotations = f.getAnnotations();
		if (annotations != null && annotations.length > 0) {
            s.setAnnotations(new StringList());
            for (Annotation a : annotations) {
				s.getAnnotations().getString().add(a.annotationType().getCanonicalName());
			}
		}

		s.getModifiers().getModifier().addAll(detectModifiers(f.getModifiers()));

		try {
			String getterName = "get" + StringUtil.capitalizeFirstLetter(f.getName());
			f.getDeclaringClass().getMethod(getterName);
			s.setGetMethod(getterName);
		} catch (NoSuchMethodException e) {
			if(logger.isDebugEnabled()) {
				logger.debug("No 'get' method for field named: " + f.getName() + " in class: " + f.getDeclaringClass().getName());
			}
		}
		if (s.getGetMethod() == null && ("boolean".equals(s.getClassName()) || "java.lang.Boolean".equals(s.getClassName()))) {
			try {
				String getterName = "is" + StringUtil.capitalizeFirstLetter(f.getName());
				f.getDeclaringClass().getMethod(getterName);
				s.setGetMethod(getterName);
			} catch (NoSuchMethodException e) {
				if(logger.isDebugEnabled()) {
					logger.debug("No 'is' method for field named: " + f.getName() + " in class: " + f.getDeclaringClass().getName());
				}
			}
		}
		try {
			String setterName = "set" + StringUtil.capitalizeFirstLetter(f.getName());
			f.getDeclaringClass().getMethod(setterName, clazz);
			s.setSetMethod(setterName);
		} catch (NoSuchMethodException e) {
			if(logger.isDebugEnabled()) {
				logger.debug("No 'set' method for field named: " + f.getName() + " in class: " + f.getDeclaringClass().getName());
			}
		}
		return s;
	}

	protected boolean isFieldPrimitive(String fieldType) {
		return getPrimitiveClasses().contains(fieldType);
	}

	protected boolean isFieldBoxedPrimitive(String fieldType) {
		return getBoxedPrimitiveClasses().contains(fieldType);
	}

	protected Integer detectArrayDimensions(Class<?> clazz) {
		Integer arrayDim = new Integer(0);
		if(clazz == null) {
			return null;
		}

		if(!clazz.isArray()) {
			return arrayDim;
		} else {
			arrayDim++;
		}

		Class<?> tmpClazz = clazz.getComponentType();
		while(tmpClazz != null && tmpClazz.isArray() && arrayDim < MAX_ARRAY_DIM_LIMIT) {
			arrayDim++;
			tmpClazz = tmpClazz.getComponentType();
		}
		return arrayDim;
	}

	protected List<io.atlasmap.java.v2.Modifier> detectModifiers(int m) {
		List<io.atlasmap.java.v2.Modifier> modifiers = new ArrayList<io.atlasmap.java.v2.Modifier>();
		if(Modifier.isAbstract(m)) { modifiers.add(io.atlasmap.java.v2.Modifier.ABSTRACT); }
		if(Modifier.isFinal(m)) { modifiers.add(io.atlasmap.java.v2.Modifier.FINAL); }
		if(Modifier.isInterface(m)) { modifiers.add(io.atlasmap.java.v2.Modifier.INTERFACE); }
		if(Modifier.isNative(m)) { modifiers.add(io.atlasmap.java.v2.Modifier.NATIVE); }
		if(Modifier.isPrivate(m)) { modifiers.add(io.atlasmap.java.v2.Modifier.PRIVATE); }
		if(Modifier.isProtected(m)) { modifiers.add(io.atlasmap.java.v2.Modifier.PROTECTED); }
		if(Modifier.isPublic(m)) { modifiers.add(io.atlasmap.java.v2.Modifier.PUBLIC); }
		if(Modifier.isStatic(m)) { modifiers.add(io.atlasmap.java.v2.Modifier.STATIC); }
		if(Modifier.isStrict(m)) { modifiers.add(io.atlasmap.java.v2.Modifier.STRICT); }
		if(Modifier.isSynchronized(m)) { modifiers.add(io.atlasmap.java.v2.Modifier.SYNCHRONIZED); }
		if(Modifier.isTransient(m)) { modifiers.add(io.atlasmap.java.v2.Modifier.TRANSIENT); }
		if(Modifier.isVolatile(m)) { modifiers.add(io.atlasmap.java.v2.Modifier.VOLATILE); }
		return modifiers;
	}

	protected Class<?> detectArrayClass(Class<?> clazz) {
		Integer arrayDim = new Integer(0);
		if(clazz == null) {
			return null;
		}

		if(!clazz.isArray()) {
			return clazz;
		} else {
			arrayDim++;
		}

		Class<?> tmpClazz = clazz.getComponentType();
		while(tmpClazz != null && tmpClazz.isArray() && arrayDim < MAX_ARRAY_DIM_LIMIT) {
			arrayDim++;
			tmpClazz = tmpClazz.getComponentType();
		}
		return tmpClazz;
	}

	protected JavaClass convertJavaFieldToJavaClass(JavaField javaField) {
		JavaClass javaClass = AtlasJavaModelFactory.createJavaClass();
		javaClass.setArray(javaField.isArray());
		javaClass.setArrayDimensions(javaField.getArrayDimensions());
		javaClass.setCollection(javaField.isCollection());
		javaClass.setPrimitive(javaField.isPrimitive());
		javaClass.setSynthetic(javaField.isSynthetic());
		javaClass.setClassName(javaField.getClassName());
		javaClass.setGetMethod(javaField.getGetMethod());
		javaClass.setName(javaField.getName());
		javaClass.setSetMethod(javaField.getSetMethod());
		javaClass.setStatus(javaField.getStatus());
		javaClass.setType(javaField.getType());
		if(javaField.getClassName() != null) {
			javaClass.setUri(String.format(AtlasJavaModelFactory.URI_FORMAT, javaField.getClassName()));
		}
		javaClass.setValue(javaField.getValue());
		javaClass.setAnnotations(javaField.getAnnotations());
		javaClass.setModifiers(javaField.getModifiers());
		return javaClass;
	}

	protected List<String> classpathStringToList(String classpath) {
		if(classpath == null) {
			return null;
		}

		List<String> jars = new ArrayList<String>();

		if(classpath.isEmpty()) {
			return jars;
		}

		if(!classpath.contains(":")) {
			jars.add(classpath);
			return jars;
		}

		String[] items = classpath.split(":", 256);
		if(items == null) {
			return jars;
		}

		jars.addAll(Arrays.asList(items));
		return jars;
	}
}
