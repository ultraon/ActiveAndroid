package com.activeandroid.sebbia;

import com.activeandroid.sebbia.util.Log;
import com.google.common.collect.Lists;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;


public abstract class DeepModel extends Model implements IDeepModel {

    @Override
    public final Long saveDeep() {
        return saveDeep(false, null);
    }

    @Override
    public final Long saveDeep(final boolean force) {
        return saveDeep(force, null);
    }

    @Override
    public final Long saveDeep(final boolean force, final IDeepActionHandler deepActionHandler) {
        List<Field> fields = getAllModelFields(getClass());
        Long savedId = -1l;
        try {
            if (!force) ActiveAndroid.beginTransaction();
            for (Field field : fields) {
                try {
                    Long id = -1l;
                    boolean deep = false;
                    final boolean accessible = field.isAccessible();
                    if (!accessible) field.setAccessible(true);
                    final Object model = field.get(this);
                    if (null == model) continue;
                    Exception ex = null;
                    try {
                        if (null != deepActionHandler && model instanceof Model && deepActionHandler.shouldProcess(field.getName(), (Model) model)) {
                            if (model instanceof DeepModel) {
                                deep = true;
                                id = ((IDeepModel) model).saveDeep(force);
                            } else {
                                id = ((Model) model).save();
                            }
                        } else if (null == deepActionHandler) {
                            if (model instanceof DeepModel) {
                                deep = true;
                                id = ((IDeepModel) model).saveDeep(force);
                            } else if (model instanceof Model){
                                id = ((Model) model).save();
                            }
                        } else {
                            Log.i("Skipped processing field %s for saving operation", field.getName());
                            id = 0l;
                        }
                    } catch (Exception e) {
                        ex = e;
                        Log.e("", e);
                    }
                    if (ex != null) {
                        if (!accessible) field.setAccessible(false);
                        if (force) {
                            Log.e(String.format("Can't save field %s.%s in DB, deep: %b", field.getClass().getSimpleName(), field.getName(), deep), ex);
                        } else {
                            final String errMsg = String.format("Can't save field %s.%s in DB, deep: %b", field.getClass().getSimpleName(), field.getName(), deep);
                            Log.e(errMsg);
                            throw new RuntimeException(errMsg, ex);
                        }
                    } else if (-1l == id) {
                        if (!accessible) field.setAccessible(false);
                        if (force) {
                            Log.e(String.format("Can't save field %s.%s in DB, deep: %b", field.getClass().getSimpleName(), field.getName(), deep));
                        } else {
                            final String errMsg = String.format("Can't save field %s.%s in DB, deep: %b", field.getClass().getSimpleName(), field.getName(), deep);
                            Log.e(errMsg);
                            throw new RuntimeException(errMsg);
                        }
                    } else {
                        if (!accessible) field.setAccessible(false);

                    }
                } catch (IllegalAccessException e) {
                    Log.e("Can't get field", e);
                }
            }

            savedId = this.save();
            if (!force && savedId > 0) ActiveAndroid.setTransactionSuccessful();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (!force) ActiveAndroid.endTransaction();
        }

        return savedId;
    }

    @Override
    public final int deleteDeep() {
        return deleteDeep(false, null);
    }

    @Override
    public final int deleteDeep(final boolean force) {
        return deleteDeep(force, null);
    }

    @Override
    public final int deleteDeep(final boolean force, final IDeepActionHandler deepActionHandler) {
        List<Field> fields = getAllModelFields(getClass());
        int deleted = 0;

        try {
            if (!force) ActiveAndroid.beginTransaction();
            for (Field field : fields) {
                try {
                    boolean deep = false;
                    final boolean accessible = field.isAccessible();
                    if (!accessible) field.setAccessible(true);
                    final Object model = field.get(this);
                    if (null == model) continue;
                    try {
                        if (null != deepActionHandler && model instanceof Model && deepActionHandler.shouldProcess(field.getName(), (Model) model)) {
                            if (model instanceof DeepModel) {
                                deep = true;
                                ((IDeepModel) model).deleteDeep(force);
                            } else {
                                ((Model) model).delete();
                            }
                        } else if (null == deepActionHandler) {
                            if (model instanceof IDeepModel) {
                                deep = true;
                                ((IDeepModel) model).deleteDeep(force);
                            } else if (model instanceof Model){
                                ((Model) model).delete();
                            }
                        } else {
                            Log.i("Skipped processing field %s for deleting operation", field.getName());
                        }
                    } catch (Exception e) {
                        if (force) {
                            Log.e(String.format("Can't delete field %s.%s in DB, deep: %b", field.getClass().getSimpleName(), field.getName(), deep), e);
                        } else {
                            final String errMsg = String.format("Can't delete field %s.%s in DB, deep: %b", field.getClass().getSimpleName(), field.getName(), deep);
                            Log.e(errMsg, e);
                            throw new RuntimeException(errMsg, e);
                        }
                    } finally {
                        if (!accessible) field.setAccessible(false);
                    }
                } catch (IllegalAccessException e) {
                    Log.e("Can't get field", e);
                }
            }

            deleted = this.delete();
            if (!force) ActiveAndroid.setTransactionSuccessful();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (!force) ActiveAndroid.endTransaction();
        }

        return deleted;
    }

    private static List<Field> getAllModelFields(Class<? extends Model> clazz) {
        final List<Field> fields = Lists.newArrayList();
        final Field[] declaredFields = clazz.getDeclaredFields();

        if (clazz == Model.class) return fields;

        if (null != declaredFields && declaredFields.length > 0) {
            for (Field input : declaredFields) {
                if (null != input
                        && Model.class.isAssignableFrom(input.getType())
                        && !Modifier.isPrivate(input.getModifiers())
                        && !Modifier.isStatic(input.getModifiers()))
                    fields.add(input);
            }
        }

        Class<?> superClazz = clazz.getSuperclass();
        if (null == superClazz || !DeepModel.class.isAssignableFrom(superClazz)) return fields;

        //noinspection unchecked
        fields.addAll(getAllModelFields((Class<? extends Model>) superClazz));

        return fields;
    }
}
