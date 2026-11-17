import java.util.*;

public class Index {
    private String name;
    private String columnName;
    private Map<Object, List<Integer>> indexMap = new HashMap<>();

    public Index() {}

    public Index(String name, String columnName) {
        this.name = name;
        this.columnName = columnName;
    }

    public String getName() { return name; }
    public String getColumnName() { return columnName; }
    public Map<Object, List<Integer>> getIndexMap() { return indexMap; }

    public void setName(String name) { this.name = name; }
    public void setColumnName(String columnName) { this.columnName = columnName; }
    public void setIndexMap(Map<Object, List<Integer>> indexMap) { this.indexMap = indexMap; }

    public void rebuild(List<Map<String, Object>> rows) {
        indexMap.clear();
        for (int i = 0; i < rows.size(); i++) {
            Object val = rows.get(i).get(columnName);
            if (val != null) {
                indexMap.computeIfAbsent(val, k -> new ArrayList<>()).add(i);
            }
        }
    }

    public List<Integer> find(Object value) {
        return indexMap.getOrDefault(value, Collections.emptyList());
    }
}
