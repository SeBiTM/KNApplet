package tools;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author SeBi
 */
public class KClass {
    private Class clazz;
    private Object instance;
    private Map<String, Method> methods;
    private Map<String, Field> fields;
    
    public KClass(Class clazz, Object instance) {
        this.clazz = clazz;
        this.instance = instance;
        this.methods = new HashMap<>();
        this.fields = new HashMap<>();
    }
    public KClass(KLoader loader, String className) {
        try {
            this.clazz = loader.findClass(className);
            this.instance = this.clazz.newInstance();
            this.methods = new HashMap<>();
            this.fields = new HashMap<>();
        } catch (InstantiationException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }
    
    public Object getField(String field) {
        try {
            if (!this.fields.containsKey(field)) {
                Field f = this.clazz.getField(field);
                this.fields.put(field, f);
            }
            if (this.fields.containsKey(field))
                return this.fields.get(field).get(this.instance);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public Object invokeMethod(String method) {
        try {
            if (this.methods == null)
                return null;
            if (!this.methods.containsKey(method)) {
                Method m = this.clazz.getMethod(method, new Class[] { });
                this.methods.put(method, m);
            }
            if (this.methods.containsKey(method)) {
                return this.methods.get(method).invoke(instance, new Object[] { });
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    public Object invokeMethod(String method, Object... params) {
        try {
            if (this.methods == null)
                return null;
            if (!this.methods.containsKey(method)) {
                Class[] types = new Class[params.length];
                for (int i = 0; i < types.length; i++) {
                    types[i] = params[i].getClass();
                    if (params[i].getClass().getName().equals("java.lang.Integer"))
                        types[i] = int.class;
                }
                Method m = this.clazz.getMethod(method, types);
                this.methods.put(method, m);
            }
            if (this.methods.containsKey(method)) {
                return this.methods.get(method).invoke(instance, params);
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public Class getSuperclass() {
        return this.clazz.getSuperclass();
    }
    public Object getInstance() {
        return this.instance;
    }
}
