package com.activeandroid.sebbia;

public interface IDeepModel {
    interface IDeepActionHandler {
        <T extends Model> boolean shouldProcess(String fieldName, T model);
    }

    Long saveDeep();
    Long saveDeep(boolean force);
    Long saveDeep(boolean force, IDeepActionHandler deepActionHandler);

    int deleteDeep();
    int deleteDeep(boolean force);
    int deleteDeep(boolean force, IDeepActionHandler deepActionHandler);
}
