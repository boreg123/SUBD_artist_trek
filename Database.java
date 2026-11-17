import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.*;

public class Database {
    private Map<String, Table> tables = new HashMap<>();
    private final String folder = "data";
    private Gson gson = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd")
        .setPrettyPrinting()
        .registerTypeAdapter(Map.class, new NumberFixer())
        .create();

    public Database() {
        new File(folder).mkdirs();
    }

    public Table getTable(String name) {
        return tables.get(name.toLowerCase());
    }

    public void addTable(Table table) {
        tables.put(table.getName().toLowerCase(), table);
        save(table);
    }

    public void dropTable(String name) {
        tables.remove(name.toLowerCase());
        File file = new File(folder + "/" + name.toLowerCase() + ".json");
        if (file.exists()) file.delete();
    }

    public void save(Table table) {
        try (Writer w = new FileWriter(folder + "/" + table.getName().toLowerCase() + ".json")) {
            gson.toJson(table, w);
        } catch (IOException e) {
            System.out.println("Не удалось сохранить: " + e.getMessage());
        }
    }

    public void saveAll() {
        for (Table t : tables.values()) save(t);
    }

    public void loadAll() {
        File[] files = new File(folder).listFiles((d, n) -> n.endsWith(".json"));
        if (files == null) return;
        for (File f : files) {
            try (Reader r = new FileReader(f)) {
                Table t = gson.fromJson(r, Table.class);
                if (t != null) {
                    tables.put(t.getName().toLowerCase(), t);
                    t.rebuildIndexes();
                }
            } catch (IOException e) {
                System.out.println("Не удалось загрузить " + f.getName());
            }
        }
    }
}
