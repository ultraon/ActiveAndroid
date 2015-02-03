package com.activeandroid.sebbia;

public interface IDeepModel {
    interface IDeepActionHandler {
        <T extends Model> boolean shouldProcess(String fieldName, T model);
    }

    Long saveDeep();
    Long saveDeep(boolean force);
    Long saveDeep(boolean force, IDeepActionHandler deepActionHandler);

    void deleteDeep();
    void deleteDeep(boolean force);
    void deleteDeep(boolean force, IDeepActionHandler deepActionHandler);
}
