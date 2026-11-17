import java.util.*;

public class Table {
    private String name;
    private List<Column> columns = new ArrayList<>();
    private List<Map<String, Object>> rows = new ArrayList<>();
    private String primaryKey;
    private Map<String, Index> indexes = new HashMap<>();

    public Table() {}

    public Table(String name, List<Column> columns, String primaryKey) {
        this.name = name;
        this.columns = columns;
        this.primaryKey = primaryKey;
    }

    public String getName() { return name; }
    public List<Column> getColumns() { return columns; }
    public List<Map<String, Object>> getRows() { return rows; }
    public String getPrimaryKey() { return primaryKey; }
    public Map<String, Index> getIndexes() { return indexes; }

    public void setName(String name) { this.name = name; }
    public void setColumns(List<Column> columns) { this.columns = columns; }
    public void setRows(List<Map<String, Object>> rows) { this.rows = rows; }
    public void setPrimaryKey(String primaryKey) { this.primaryKey = primaryKey; }
    public void setIndexes(Map<String, Index> indexes) { this.indexes = indexes; }

    public boolean hasColumn(String columnName) {
        for (Column c : columns) {
            if (c.getName().equalsIgnoreCase(columnName)) return true;
        }
        return false;
    }

    public Map<String, Object> findByPrimaryKey(Object key) {
        for (Map<String, Object> row : rows) {
            if (Objects.equals(row.get(primaryKey), key)) return row;
        }
        return null;
    }

    public void addRow(Map<String, Object> row) {
        rows.add(row);
    }

    public void rebuildIndexes() {
        for (Index idx : indexes.values()) {
            idx.rebuild(rows);
        }
    }

    public void printRows(List<Map<String, Object>> data) {
        if (data.isEmpty()) {
            System.out.println("(пусто)");
            return;
        }
        StringBuilder header = new StringBuilder();
        for (Column c : columns) {
            header.append(String.format("%-18s", c.getName()));
        }
        System.out.println(header);
        System.out.println("-".repeat(header.length()));
        for (Map<String, Object> row : data) {
            StringBuilder line = new StringBuilder();
            for (Column c : columns) {
                Object val = row.get(c.getName());
                line.append(String.format("%-18s", val == null ? "NULL" : val));
            }
            System.out.println(line);
        }
    }
}
