package de.tobias.simpsocserv.serverManagement;

import java.util.HashMap;

public class DataStorage {

    private HashMap<String, Object> values = new HashMap<>();

    public DataStorage() {}

    public Boolean has(String name) {
        return values.containsKey(name);
    }

    public Object get(String name) {
        if(!has(name)) return null;
        return values.get(name);
    }

    public void set(String name, Object value) {
        values.put(name, value);
    }
}
