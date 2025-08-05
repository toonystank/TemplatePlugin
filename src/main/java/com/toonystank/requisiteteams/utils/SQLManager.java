package com.toonystank.requisiteteams.utils;

import com.toonystank.requisiteteams.RequisiteTeams;
import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.HashSet;

@Getter
@SuppressWarnings("unused")
public class SQLManager {

    private File file;
    private final Logger logger;
    private String fileName;
    private Connection connection;
    @Nullable
    protected RequisiteTeams plugin;
    private final boolean isInFolder;
    private String dbVersion;
    private boolean consoleLogger = true;
    @Nullable
    private String corePath = "";

    /**
     * Initializes the SQLite database.
     *
     * @param fileName String
     * @param force    boolean enable/disable force database update
     * @param copy     boolean not used for SQLite but kept for consistency
     */
    public SQLManager(String fileName, boolean force, boolean copy) throws SQLException, IOException {
        this.plugin = RequisiteTeams.getInstance();
        if (plugin != null) this.logger = plugin.getLogger();
        else this.logger = Logger.getGlobal();
        this.fileName = fileName.endsWith(".db") ? fileName : fileName + ".db";
        process(plugin, fileName, force);
        isInFolder = false;
    }

    /**
     * Initializes the SQLite database in a given path.
     *
     * @param fileName String
     * @param path     String path to initialize the database in
     * @param force    boolean enable/disable force database update
     * @param copy     boolean not used for SQLite but kept for consistency
     */
    public SQLManager(String fileName, @Nullable String path, boolean force, boolean copy) throws SQLException, IOException {
        this.plugin = RequisiteTeams.getInstance();
        if (plugin != null) this.logger = plugin.getLogger();
        else this.logger = Logger.getGlobal();
        this.corePath = path;
        this.fileName = fileName.endsWith(".db") ? fileName : fileName + ".db";
        process(plugin, fileName, path, force);
        isInFolder = true;
    }

    /**
     * Initializes the SQLManager without creating a database file.
     *
     * @param fileName String
     * @param path     String path to initialize the database in
     */
    public SQLManager(String fileName, @Nullable String path) {
        this.plugin = RequisiteTeams.getInstance();
        this.logger = plugin.getLogger();
        this.corePath = path;
        this.fileName = fileName.endsWith(".db") ? fileName : fileName + ".db";
        isInFolder = true;
    }

    private void process(Plugin plugin, String fileName, boolean force) throws SQLException, IOException {
        if (plugin == null) {
            this.file = new File(fileName);
        } else {
            this.file = new File(plugin.getDataFolder(), fileName);
        }
        setupConnection();
        if (force) {
            updateDatabaseFile();
        }
    }

    private void process(Plugin plugin, String fileName, String path, boolean force) throws SQLException, IOException {
        String corePath = plugin != null ? plugin.getDataFolder() + File.separator + path : path;
        this.file = new File(corePath, fileName);
        File parentDir = new File(corePath);
        if (!parentDir.exists()) {
            if (parentDir.mkdirs() && consoleLogger) {
                MessageUtils.toConsole("Created new directory: " + parentDir.getName(), false);
            }
        }
        setupConnection();
        if (force) {
            updateDatabaseFile();
        }
    }

    private void setupConnection() throws SQLException {
        if (!file.exists()) {
            try {
                if (file.createNewFile() && consoleLogger) {
                    MessageUtils.toConsole("Created new database file: " + file.getName(), false);
                }
            } catch (IOException e) {
                MessageUtils.error("Failed to create database file: " + e.getMessage());
            }
        }
        String url = "jdbc:sqlite:" + file.getAbsolutePath();
        this.connection = DriverManager.getConnection(url);
        connection.setAutoCommit(true);
    }

    /**
     * Check if the database file exists.
     *
     * @return boolean
     */
    public boolean isFileExist() {
        String corePath = plugin.getDataFolder() + File.separator + this.corePath;
        this.file = new File(corePath, fileName);
        return file.exists();
    }

    /**
     * Create the database file. Use with constructor that doesn't create a file.
     */
    public void make() throws SQLException, IOException {
        process(plugin, fileName, corePath, false);
    }

    /**
     * Set logger for database events. By default, it's enabled.
     *
     * @param consoleLogger boolean
     * @return SQLManager
     */
    public SQLManager setConsoleLogger(boolean consoleLogger) {
        this.consoleLogger = consoleLogger;
        return this;
    }

    /**
     * Set the version of the database. Useful for updating the database.
     *
     * @param dbVersion Version of the database
     * @return SQLManager
     */
    public SQLManager setDbVersion(String dbVersion) {
        this.dbVersion = dbVersion;
        return this;
    }

    public boolean deleteConfig() {
        try {
            if (connection != null) {
                connection.close();
            }
            this.connection = null;
            return file.delete();
        } catch (SQLException e) {
            MessageUtils.error("Failed to close database: " + e.getMessage());
            return false;
        }
    }

    public boolean regenerateConfig() throws SQLException, IOException {
        deleteConfig();
        if (isInFolder) {
            process(plugin, fileName, corePath, true);
            return true;
        }
        process(plugin, fileName, true);
        return true;
    }

    public ResultSet getConfigurationSection(String table) throws SQLException {
        String query = "SELECT * FROM " + table;
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(query);
    }

    public @Nullable Set<String> getConfigurationSection(String table, boolean key) throws SQLException {
        if (!tableExists(table)) return null;
        Set<String> columns = new HashSet<>();
        ResultSet rs = connection.getMetaData().getColumns(null, null, table, null);
        while (rs.next()) {
            columns.add(rs.getString("COLUMN_NAME"));
        }
        rs.close();
        return columns;
    }

    public @NotNull Set<String> getConfigurationSection(String table, boolean key, boolean notNull) throws SQLException {
        if (!tableExists(table)) {
            createTable(table);
        }
        return getConfigurationSection(table, key);
    }

    /**
     * Save changes to the database (commit).
     */
    public void save() throws SQLException {
        if (connection != null && !connection.getAutoCommit()) {
            connection.commit();
        }
    }

    /**
     * Reload the database connection.
     */
    public void reload() throws SQLException, IOException {
        if (consoleLogger) MessageUtils.toConsole("Reloading database for " + file.getName(), false);
        if (connection != null) {
            connection.close();
        }
        if (isInFolder) {
            process(plugin, fileName, corePath, false);
        } else {
            process(plugin, fileName, false);
        }
    }

    /**
     * Update the database with a newer version.
     *
     * @param versionPath String (table.column where version is stored)
     */
    public void updateConfig(@NotNull String versionPath) throws SQLException {
        String[] parts = versionPath.split("\\.");
        if (parts.length != 2) {
            MessageUtils.error("Invalid version path format. Expected table.column");
            return;
        }
        String table = parts[0];
        String column = parts[1];
        String version = getString(table, column);
        if (version == null) {
            MessageUtils.toConsole("No version found in " + file.getName() + ". Creating new version of the database", false);
        } else if (!version.equals(this.dbVersion)) {
            try {
                File backupFile = new File(file.getParentFile(), "backup/old_" + version + "_" + file.getName());
                backupDatabaseFile(backupFile);
                updateDatabaseFile();
            } catch (IOException e) {
                MessageUtils.error("Failed to update database: " + e.getMessage());
            }
        }
    }

    private void backupDatabaseFile(File backupFile) throws IOException {
        if (file.exists()) {
            File parent = backupFile.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            file.renameTo(backupFile);
            if (consoleLogger) {
                MessageUtils.toConsole("Backed up " + file.getName() + " to " + backupFile.toString(), false);
            }
        }
    }

    private void updateDatabaseFile() throws IOException, SQLException {
        deleteConfig();
        if (isInFolder) {
            process(plugin, fileName, corePath, true);
        } else {
            process(plugin, fileName, true);
        }
        if (consoleLogger) {
            MessageUtils.toConsole("Updated " + file.getName() + " to version " + this.dbVersion, false);
        }
    }

    /**
     * Update an existing value in the database.
     *
     * @param tableColumn String (table.column)
     * @param value       Object
     */
    public void update(String tableColumn, Object value) throws SQLException {
        String[] parts = tableColumn.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid path format. Expected table.column");
        }
        String table = parts[0];
        String column = parts[1];
        if (!tableExists(table)) {
            throw new IllegalArgumentException("Table " + table + " does not exist in " + file.getName());
        }
        String query = "UPDATE " + table + " SET " + column + " = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setObject(1, value);
            pstmt.executeUpdate();
        }
        save();
    }

    /**
     * Set a value in the database.
     *
     * @param tableColumn String (table.column)
     * @param value       Object
     */
    public void set(String tableColumn, Object value) throws SQLException {
        String[] parts = tableColumn.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid path format. Expected table.column");
        }
        String table = parts[0];
        String column = parts[1];
        if (!tableExists(table)) {
            createTable(table);
        }
        String query = "INSERT OR REPLACE INTO " + table + " (" + column + ") VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setObject(1, value);
            pstmt.executeUpdate();
        }
        save();
    }

    /**
     * Get a value from the database.
     *
     * @param tableColumn String (table.column)
     * @return Object
     */
    public @Nullable Object get(String tableColumn) throws SQLException {
        String[] parts = tableColumn.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid path format. Expected table.column");
        }
        String table = parts[0];
        String column = parts[1];
        if (!tableExists(table)) {
            return null;
        }
        String query = "SELECT " + column + " FROM " + table + " LIMIT 1";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getObject(column);
            }
        }
        return null;
    }

    /**
     * Get a list of strings from the database.
     *
     * @param tableColumn String (table.column)
     * @return List<String>
     */
    public List<String> getStringList(String tableColumn) throws SQLException {
        List<String> result = new ArrayList<>();
        String[] parts = tableColumn.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid path format. Expected table.column");
        }
        String table = parts[0];
        String column = parts[1];
        if (!tableExists(table)) {
            if (consoleLogger) {
                MessageUtils.toConsole("Table " + table + " does not exist", true);
            }
            return result;
        }
        String query = "SELECT " + column + " FROM " + table;
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String value = rs.getString(column);
                if (value != null) {
                    result.add(value);
                }
            }
        }
        if (consoleLogger) {
            MessageUtils.toConsole("Retrieved string list from " + tableColumn, true);
        }
        return result;
    }

    /**
     * Add a value to a list of strings in the database.
     *
     * @param tableColumn String (table.column)
     * @param value       String to add
     */
    public void addToStringList(String tableColumn, String value) throws SQLException {
        String[] parts = tableColumn.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid path format. Expected table.column");
        }
        String table = parts[0];
        String column = parts[1];
        if (!tableExists(table)) {
            throw new IllegalArgumentException("Table " + table + " does not exist in " + file.getName());
        }
        String query = "INSERT INTO " + table + " (" + column + ") VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, value);
            pstmt.executeUpdate();
        }
        save();
    }

    public void addToStringList(String tableColumn, String value, boolean createIfNotExist) throws SQLException {
        String[] parts = tableColumn.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid path format. Expected table.column");
        }
        String table = parts[0];
        if (createIfNotExist && !tableExists(table)) {
            createTable(table);
        }
        addToStringList(tableColumn, value);
    }

    /**
     * Get a boolean from the database.
     *
     * @param tableColumn String (table.column)
     * @return boolean
     */
    public boolean getBoolean(String tableColumn) throws SQLException {
        Object value = get(tableColumn);
        return value instanceof Boolean && (Boolean) value;
    }

    /**
     * Get an int from the database.
     *
     * @param tableColumn String (table.column)
     * @return int
     */
    public int getInt(String tableColumn) throws SQLException {
        Object value = get(tableColumn);
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    /**
     * Get a double from the database.
     *
     * @param tableColumn String (table.column)
     * @return double
     */
    public double getDouble(String tableColumn) throws SQLException {
        Object value = get(tableColumn);
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0;
    }

    /**
     * Get a string from the database.
     *
     * @param tableColumn String (table.column)
     * @return String
     */
    public String getString(String tableColumn) throws SQLException {
        Object value = get(tableColumn);
        return value != null ? value.toString() : null;
    }

    public String getString(String tableColumn, String defaultValue) throws SQLException {
        String value = getString(tableColumn);
        if (value == null) {
            set(tableColumn, defaultValue);
            return defaultValue;
        }
        return value;
    }

    public int getInt(String tableColumn, int defaultValue) throws SQLException {
        Object value = get(tableColumn);
        if (value == null) {
            set(tableColumn, defaultValue);
            return defaultValue;
        }
        return getInt(tableColumn);
    }

    public boolean getBoolean(String tableColumn, boolean defaultValue) throws SQLException {
        Object value = get(tableColumn);
        if (value == null) {
            set(tableColumn, defaultValue);
            return defaultValue;
        }
        return getBoolean(tableColumn);
    }

    public List<String> getStringList(String tableColumn, List<String> defaultValue) throws SQLException {
        List<String> value = getStringList(tableColumn);
        if (value.isEmpty()) {
            for (String item : defaultValue) {
                addToStringList(tableColumn, item);
            }
            return defaultValue;
        }
        return value;
    }

    private boolean tableExists(String table) throws SQLException {
        try (ResultSet rs = connection.getMetaData().getTables(null, null, table, null)) {
            return rs.next();
        }
    }

    private void createTable(String table) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS " + table + " (id INTEGER PRIMARY KEY AUTOINCREMENT, value TEXT)";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(query);
        }
        if (consoleLogger) {
            MessageUtils.toConsole("Created table: " + table, false);
        }
    }
}