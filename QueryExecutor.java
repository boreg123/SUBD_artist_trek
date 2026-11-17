import java.util.*;

public class QueryExecutor {
    private Database db;

    public QueryExecutor(Database db) {
        this.db = db;
    }

    public void execute(Query q) {
        switch (q.getType()) {
            case INSERT -> insert(q);
            case SELECT -> select(q);
            case UPDATE -> update(q);
            case DELETE -> delete(q);
            case CREATE_TABLE -> createTable(q);
            case DROP_TABLE -> dropTable(q);
            case CREATE_INDEX -> createIndex(q);
            default -> System.out.println("Неизвестный запрос");
        }
    }

    private void insert(Query q) {
        Table t = db.getTable(q.getTableName());
        if (t == null) {
            System.out.println("Таблица не найдена");
            return;
        }

        Map<String, Object> row = new LinkedHashMap<>();
        if (q.getColumns().isEmpty()) {
            if (q.getValues().size() != t.getColumns().size()) {
                System.out.println("Неверное число значений");
                return;
            }
            for (int i = 0; i < t.getColumns().size(); i++) {
                row.put(t.getColumns().get(i).getName(), q.getValues().get(i));
            }
        } else {
            for (Column c : t.getColumns()) row.put(c.getName(), null);
            for (int i = 0; i < q.getColumns().size(); i++) {
                row.put(q.getColumns().get(i), q.getValues().get(i));
            }
        }

        Object pk = row.get(t.getPrimaryKey());
        if (pk != null && t.findByPrimaryKey(pk) != null) {
            System.out.println("Такой первичный ключ уже есть");
            return;
        }

        t.addRow(row);
        t.rebuildIndexes();
        db.save(t);
        System.out.println("Добавлено");
    }

    private void select(Query q) {
        Table t = db.getTable(q.getTableName());
        if (t == null) {
            System.out.println("Таблица не найдена");
            return;
        }

        List<Map<String, Object>> result = new ArrayList<>();
        Index idx = q.getWhereColumn() == null ? null : t.getIndexes().get(q.getWhereColumn().toLowerCase());

        if (idx != null && "=".equals(q.getWhereOperator())) {
            for (int pos : idx.find(q.getWhereValue())) {
                if (pos < t.getRows().size()) {
                    Map<String, Object> row = t.getRows().get(pos);
                    if (matches(row, q)) result.add(row);
                }
            }
        } else {
            for (Map<String, Object> row : t.getRows()) {
                if (matches(row, q)) result.add(row);
            }
        }

        System.out.println("Найдено: " + result.size());
        t.printRows(result);
    }

    private void update(Query q) {
        Table t = db.getTable(q.getTableName());
        if (t == null) {
            System.out.println("Таблица не найдена");
            return;
        }
        int count = 0;
        for (Map<String, Object> row : t.getRows()) {
            if (matches(row, q)) {
                row.putAll(q.getSetValues());
                count++;
            }
        }
        t.rebuildIndexes();
        db.save(t);
        System.out.println("Обновлено: " + count);
    }

    private void delete(Query q) {
        Table t = db.getTable(q.getTableName());
        if (t == null) {
            System.out.println("Таблица не найдена");
            return;
        }
        List<Map<String, Object>> toRemove = new ArrayList<>();
        for (Map<String, Object> row : t.getRows()) {
            if (matches(row, q)) toRemove.add(row);
        }
        t.getRows().removeAll(toRemove);
        t.rebuildIndexes();
        db.save(t);
        System.out.println("Удалено: " + toRemove.size());
    }

    private void createTable(Query q) {
        if (db.getTable(q.getTableName()) != null) {
            System.out.println("Таблица уже есть");
            return;
        }
        if (q.getPrimaryKey() == null) {
            System.out.println("Нужен первичный ключ");
            return;
        }
        db.addTable(new Table(q.getTableName(), q.getCreateColumns(), q.getPrimaryKey()));
        System.out.println("Таблица создана");
    }

    private void dropTable(Query q) {
        if (db.getTable(q.getTableName()) == null) {
            System.out.println("Таблица не найдена");
            return;
        }
        db.dropTable(q.getTableName());
        System.out.println("Таблица удалена");
    }

    private void createIndex(Query q) {
        Table t = db.getTable(q.getTableName());
        if (t == null) {
            System.out.println("Таблица не найдена");
            return;
        }
        if (!t.hasColumn(q.getIndexColumn())) {
            System.out.println("Колонка не найдена");
            return;
        }
        Index idx = new Index(q.getIndexName(), q.getIndexColumn());
        idx.rebuild(t.getRows());
        t.getIndexes().put(q.getIndexColumn().toLowerCase(), idx);
        db.save(t);
        System.out.println("Индекс создан");
    }

    private boolean matches(Map<String, Object> row, Query q) {
        if (q.getWhereColumn() == null) return true;
        Object val = row.get(q.getWhereColumn());
        if (val == null) return false;
        Object target = q.getWhereValue();
        int cmp = (val instanceof Number && target instanceof Number)
                ? Double.compare(((Number) val).doubleValue(), ((Number) target).doubleValue())
                : val.toString().compareTo(target.toString());
        return switch (q.getWhereOperator()) {
            case "=" -> cmp == 0;
            case "!=" -> cmp != 0;
            case ">" -> cmp > 0;
            case "<" -> cmp < 0;
            case ">=" -> cmp >= 0;
            case "<=" -> cmp <= 0;
            default -> false;
        };
    }
}
