Bukkit Asynchronous SQL
-----------------------

Library for using SQL in bukkit plugins.

[TeamCity Development Builds - CI Server](http://ci.dabo.guru/p/BukkitAsyncSql)

### Example usage / How to use

Here are some examples of how to use bukkit-async-sql.

#### Starting connection

This is typically something that you would do once in your plugin. This creates an AsyncSQL instance that you can then use to do stuff with the database.

```
String sqlHost = "localhost"
int sqlPort = 3306;
String sqlDatabase = "minecraft"
String sqlUsername = "root"
String sqlPassword = "qwerty"
SQLConnectionInfo connectionInfo = new SQLConnectionInfo(sqlHost, sqlPort, sqlDatabase, sqlUsername, sqlPassword);

AsyncSQL sql = new AsyncSQL(skywars, skywars.getLogger(), connectionInfo); // Typically this would set an instance variable.
```

#### Creating a table
This statement creates a table with the given table structure. This is basically just running the SQL statement: ```CREATE TABLE IF NOT EXISTS `" + tableName + "` (`username` VARCHAR(32), `user_score` INT, PRIMARY KEY (`username`));```. Your table structure will probably differ, so you will want to change this statement to whatever SQL you want to run.

You can do something similar to this whenever you want to run an update without registering any result.
```java
final String tableName = "myTable" // This would be set earlier, just declaring it here for example
sql.run("create user table", new SQLRunnable() {
    @Override
    public void run(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + tableName + "` (`username` VARCHAR(32), `user_score` INT, PRIMARY KEY (`username`));");
        try {
            statement.execute();
        } finally {
            statement.close();
        }
    }
});
```

### Setting data
This is very similar to creating a table, just with a different query statement. Again, your table structure will probably differ, so just replace the query statement string with what your own.

The only thing that is different about this, is that it has query parameters. Notice how I have the '?' query parameter. You will want to use that instead of just adding the username to the query string, as that allows for sql injection. You can instead use the statement.setString and statement.setInt methods to add user input to the string. On the MySQL server, the ? will be replaced with the int/string you set.

```
// These variables are probably going to want to be method parameters
final int diff = 5;
final String name = "daboross"
sql.run("add " + diff + " score to " + name, new SQLRunnable() {
    @Override
    public void run(final Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO `" + tableName + "` (username, user_score) VALUES (?, ?) ON DUPLICATE KEY UPDATE `user_score` = `user_score` + ?;");
        statement.setString(1, name);
        statement.setInt(2, diff);
        statement.setInt(3, diff);
    }
});
```

### Retrieving data
