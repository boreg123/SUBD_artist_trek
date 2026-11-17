import java.util.*;

public class QueryParser {
    public static Query parse(String sql) {
        sql = sql.trim();
        if (sql.endsWith(";")) sql = sql.substring(0, sql.length() - 1).trim();
        String upper = sql.toUpperCase();

        if (upper.startsWith("INSERT")) return insert(sql);
        if (upper.startsWith("SELECT")) return select(sql);
        if (upper.startsWith("UPDATE")) return update(sql);
        if (upper.startsWith("DELETE")) return delete(sql);
        if (upper.startsWith("CREATE TABLE")) return createTable(sql);
        if (upper.startsWith("DROP TABLE")) return dropTable(sql);
        if (upper.startsWith("CREATE INDEX")) return createIndex(sql);

        Query q = new Query();
        q.setType(Query.Type.UNKNOWN);
        return q;
    }

    private static Query insert(String sql) {
        Query q = new Query();
        q.setType(Query.Type.INSERT);
        String rest = sql.substring("INSERT INTO".length()).trim();
        int valuesPos = rest.toUpperCase().indexOf("VALUES");
        String tablePart = rest.substring(0, valuesPos).trim();
        String valuesPart = rest.substring(valuesPos + 6).trim();

        int open = tablePart.indexOf('(');
        if (open >= 0) {
            q.setTableName(tablePart.substring(0, open).trim());
            String cols = tablePart.substring(open + 1, tablePart.indexOf(')'));
            for (String c : cols.split(",")) q.getColumns().add(c.trim());
        } else {
            q.setTableName(tablePart);
        }

        String inner = valuesPart.substring(1, valuesPart.lastIndexOf(')'));
        for (String v : splitComma(inner)) q.getValues().add(value(v));
        return q;
    }

    private static Query select(String sql) {
        Query q = new Query();
        q.setType(Query.Type.SELECT);
        String rest = sql.substring(6).trim();
        int fromPos = rest.toUpperCase().indexOf(" FROM ");
        String cols = rest.substring(0, fromPos).trim();
        String tail = rest.substring(fromPos + 6).trim();

        if (!cols.equals("*")) {
            for (String c : cols.split(",")) q.getColumns().add(c.trim());
        }

        int wherePos = tail.toUpperCase().indexOf(" WHERE ");
        if (wherePos >= 0) {
            q.setTableName(tail.substring(0, wherePos).trim());
            where(q, tail.substring(wherePos + 7).trim());
        } else {
            q.setTableName(tail);
        }
        return q;
    }

    private static Query update(String sql) {
        Query q = new Query();
        q.setType(Query.Type.UPDATE);
        String rest = sql.substring(6).trim();
        int setPos = rest.toUpperCase().indexOf(" SET ");
        q.setTableName(rest.substring(0, setPos).trim());
        String tail = rest.substring(setPos + 5).trim();

        int wherePos = tail.toUpperCase().indexOf(" WHERE ");
        String pairs = wherePos >= 0 ? tail.substring(0, wherePos).trim() : tail;
        for (String pair : pairs.split(",")) {
            String[] kv = pair.split("=");
            q.getSetValues().put(kv[0].trim(), value(kv[1].trim()));
        }
        if (wherePos >= 0) where(q, tail.substring(wherePos + 7).trim());
        return q;
    }

    private static Query delete(String sql) {
        Query q = new Query();
        q.setType(Query.Type.DELETE);
        String rest = sql.substring("DELETE FROM".length()).trim();
        int wherePos = rest.toUpperCase().indexOf(" WHERE ");
        if (wherePos >= 0) {
            q.setTableName(rest.substring(0, wherePos).trim());
            where(q, rest.substring(wherePos + 7).trim());
        } else {
            q.setTableName(rest);
        }
        return q;
    }

    private static Query createTable(String sql) {
        Query q = new Query();
        q.setType(Query.Type.CREATE_TABLE);
        String rest = sql.substring("CREATE TABLE".length()).trim();
        int open = rest.indexOf('(');
        q.setTableName(rest.substring(0, open).trim());
        String inside = rest.substring(open + 1, rest.lastIndexOf(')')).trim();
        for (String part : splitComma(inside)) {
            String[] tokens = part.trim().split("\\s+");
            q.getCreateColumns().add(new Column(tokens[0], tokens[1].toLowerCase()));
            if (part.toUpperCase().contains("PRIMARY KEY")) {
                q.setPrimaryKey(tokens[0]);
            }
        }
        return q;
    }

    private static Query dropTable(String sql) {
        Query q = new Query();
        q.setType(Query.Type.DROP_TABLE);
        q.setTableName(sql.substring("DROP TABLE".length()).trim());
        return q;
    }

    private static Query createIndex(String sql) {
        Query q = new Query();
        q.setType(Query.Type.CREATE_INDEX);
        String rest = sql.substring("CREATE INDEX".length()).trim();
        int onPos = rest.toUpperCase().indexOf(" ON ");
        q.setIndexName(rest.substring(0, onPos).trim());
        String tail = rest.substring(onPos + 4).trim();
        int open = tail.indexOf('(');
        q.setTableName(tail.substring(0, open).trim());
        q.setIndexColumn(tail.substring(open + 1, tail.lastIndexOf(')')).trim());
        return q;
    }

    private static void where(Query q, String text) {
        String[] ops = {">=", "<=", "!=", "=", ">", "<"};
        for (String op : ops) {
            int pos = text.indexOf(op);
            if (pos > 0) {
                q.setWhereColumn(text.substring(0, pos).trim());
                q.setWhereOperator(op);
                q.setWhereValue(value(text.substring(pos + op.length()).trim()));
                return;
            }
        }
    }

    private static List<String> splitComma(String s) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean quoted = false;
        for (char c : s.toCharArray()) {
            if (c == '\'') quoted = !quoted;
            if (c == ',' && !quoted) {
                parts.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        if (cur.length() > 0) parts.add(cur.toString().trim());
        return parts;
    }

    private static Object value(String v) {
        if (v.startsWith("'") && v.endsWith("'")) return v.substring(1, v.length() - 1);
        if (v.equalsIgnoreCase("TRUE")) return true;
        if (v.equalsIgnoreCase("FALSE")) return false;
        try {
            if (v.contains(".")) return Double.parseDouble(v);
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return v;
        }
    }
}
