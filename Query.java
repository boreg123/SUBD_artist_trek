import java.util.*;

public class Query {
    public enum Type { INSERT, SELECT, UPDATE, DELETE, CREATE_TABLE, DROP_TABLE, CREATE_INDEX, UNKNOWN }

    private Type type;
    private String tableName;
    private List<String> columns = new ArrayList<>();
    private List<Object> values = new ArrayList<>();
    private Map<String, Object> setValues = new LinkedHashMap<>();
    private String whereColumn;
    private String whereOperator;
    private Object whereValue;
    private String indexName;
    private String indexColumn;
    private List<Column> createColumns = new ArrayList<>();
    private String primaryKey;

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public List<String> getColumns() { return columns; }
    public List<Object> getValues() { return values; }
    public Map<String, Object> getSetValues() { return setValues; }
    public String getWhereColumn() { return whereColumn; }
    public void setWhereColumn(String whereColumn) { this.whereColumn = whereColumn; }
    public String getWhereOperator() { return whereOperator; }
    public void setWhereOperator(String whereOperator) { this.whereOperator = whereOperator; }
    public Object getWhereValue() { return whereValue; }
    public void setWhereValue(Object whereValue) { this.whereValue = whereValue; }
    public String getIndexName() { return indexName; }
    public void setIndexName(String indexName) { this.indexName = indexName; }
    public String getIndexColumn() { return indexColumn; }
    public void setIndexColumn(String indexColumn) { this.indexColumn = indexColumn; }
    public List<Column> getCreateColumns() { return createColumns; }
    public String getPrimaryKey() { return primaryKey; }
    public void setPrimaryKey(String primaryKey) { this.primaryKey = primaryKey; }
}
