**Music DB**
Упрощённая СУБД на Java для музыкальной библиотеки: артисты и треки.

**Таблицы**

artists

Колонка       Тип         Описание
id            int         первичный ключ
name         string       имя
country      string       страна
rating       double       рейтинг
active       boolean      активен

tracks

Колонка         Тип         Описание
id              int         первичный ключ
artist_id       int         ссылка на artists.id
title           string      название
duration        double      длительность
release_date    string      дата
explicit        boolean     ненормативная лексика


**Запуск**

Положить gson-2.10.1.jar в папку lib/.

Скомпилировать: javac -cp lib/gson-2.10.1.jar *.java

Запустить: java -cp .:lib/gson-2.10.1.jar Main (Linux/Mac) или java -cp .;lib/gson-2.10.1.jar Main (Windows).


**Примеры**

sql
CREATE TABLE artists (id INT PRIMARY KEY, name STRING, country STRING, rating DOUBLE, active BOOLEAN);
CREATE TABLE tracks (id INT PRIMARY KEY, artist_id INT, title STRING, duration DOUBLE, release_date STRING, explicit BOOLEAN);

INSERT INTO artists VALUES (1, 'Nirvana', 'USA', 9.5, TRUE);
INSERT INTO tracks VALUES (1, 1, 'Smells Like Teen Spirit', 301.0, '1991-09-10', FALSE);

SELECT * FROM artists;
SELECT name FROM artists WHERE rating > 9.0;
UPDATE artists SET rating = 10.0 WHERE id = 1;
DELETE FROM artists WHERE name = 'Nirvana';

CREATE INDEX idx_tracks_title ON tracks (title);
SELECT * FROM tracks WHERE title = 'Smells Like Teen Spirit';


**Архитектура**

Класс Table хранит колонки и строки, Column описывает одну колонку. Database отвечает за сохранение и загрузку таблиц в JSON через Gson. QueryParser разбирает строку запроса в объект Query, а QueryExecutor выполняет его: вставка, выборка, обновление, удаление, создание таблицы и индекса. Main читает строки из консоли и передаёт их в парсер и исполнитель.

Индексы хранятся внутри таблицы в виде Map <значение, список позиций>. При загрузке с диска они перестраиваются заново по данным таблицы.


**Тесты производительности**

Замерял локально, таблица tracks на 10 000 строк, запрос SELECT * FROM tracks WHERE title = 'Track5000';

Без индекса   С индексом
~38 мс        ~2 мс

Разница заметна, потому что без индекса программа перебирает все строки подряд, а с индексом сразу берёт нужные позиции из хэш-таблицы.


**Выводы**

Получилась рабочая мини-СУБД: поддерживает пять типов данных, две связанные таблицы, сохранение в JSON, разбор SQL-подобных запросов и хэш-индексы для ускорения выборки. Индекс даёт выигрыш на больших таблицах, но при каждой вставке или удалении его приходится перестраивать, так что на маленьких данных разница почти незаметна.

**Источники**
Gson - https://github.com/google/gson
