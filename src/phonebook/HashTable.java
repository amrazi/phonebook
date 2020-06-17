package phonebook;

import java.util.Arrays;

public class HashTable<T> {
    private int size;
    private phoneTableEntry[] table;
    private int count;

    public HashTable(int size) {
        this.size=size;
        table=new phoneTableEntry[size];
        count=0;
    }

    public boolean put(String key, int value) {

        if (count>=size) {
            rehash();
        }

        int idx = findEntryIndex(key);

        if (idx == -1) {
            return false;
        }

        table[idx] = new phoneTableEntry(key, value);
        return true;
    }

    public int get(String key) {
        int idx = findEntryIndex(key);

        if (idx == -1 || table[idx] == null) {
            return -1;
        }

        return table[idx].getValue();
    }

    public int findKey(String key) {
        for (int i=0; i<table.length; i++) {
            if (table[i].getKey().equals(key)) {
                return i; //returns hash
            }
        }
        return -1;
    }

    private void rehash() {
        phoneTableEntry[] oldTable = Arrays.copyOf(table, size);

        size = size * 2;
        table = new phoneTableEntry[size];
        count = 0;

        for (int i=0; i<oldTable.length; i++) {
            put(oldTable[i].getKey(), oldTable[i].getValue());
        }
    }

    public int findEntryIndex(String key) {
        int hash = Math.abs(key.hashCode()) % size;

        int origHash = hash;

        while (!(table[hash]==null || table[hash].getKey().equals(key))) {
            hash = (hash + 1) % size;

            if (hash == origHash) {
                return -1;
            }
        }

        return hash;
    }

    @Override
    public String toString() {
        StringBuilder tableStringBuilder = new StringBuilder();

        for (int i=0; i<table.length; i++) {
            if (table[i] == null) {
                tableStringBuilder.append(i+": null");
            } else {
                tableStringBuilder.append(i+": key="+table[i].getKey()+", value="+table[i].getValue());
            }

            if (i < table.length - 1) {
                tableStringBuilder.append("\n");
            }
        }

        return tableStringBuilder.toString();
    }
}
