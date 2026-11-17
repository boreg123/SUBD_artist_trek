import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Database db = new Database();
        db.loadAll();
        QueryExecutor executor = new QueryExecutor(db);
        Scanner scanner = new Scanner(System.in);

        System.out.println("Music DB");
        System.out.println("Введите запрос или exit");
        System.out.println("Пример: CREATE TABLE artists (id INT PRIMARY KEY, name STRING);");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            if (line.equalsIgnoreCase("exit")) break;
            try {
                executor.execute(QueryParser.parse(line));
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
        db.saveAll();
        System.out.println("Готово");
    }
}
