package me.kodysimpson.littybank;

import me.kodysimpson.littybank.models.ATM;
import me.kodysimpson.littybank.models.AccountTier;
import me.kodysimpson.littybank.models.SavingsAccount;
import me.kodysimpson.littybank.utils.SavingsAccountsComparator;
import me.kodysimpson.littybank.utils.Serializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Database {

    //Create and establish connection with SQL Database
    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("org.h2.Driver");

            try {
                connection = DriverManager.getConnection(LittyBank.getConnectionUrl());

            } catch (SQLException e) {
                System.out.println("Unable to establish a connection with the database");
            }
        } catch (ClassNotFoundException ex) {
            System.out.println("Unable to find the h2 DB sql driver");
        }
        return connection;
    }

    //Initialize database tables
    public static void initializeDatabase() {

        try {

            //Create the desired tables for our database if they don't exist
            Statement statement = getConnection().createStatement();
            //Table for storing all of the accounts
            statement.execute("CREATE TABLE IF NOT EXISTS SavingsAccounts(AccountID int NOT NULL IDENTITY(1, 1), AccountTier varchar(255), OwnerUUID varchar(255), Balance DECIMAL(30,3), LastUpdated DATE, LastChecked DATE);");
            //Table for storing all ATMs
            statement.execute("CREATE TABLE IF NOT EXISTS ATM(Owner varchar(255), Location varchar(255));");

            System.out.println("Database loaded");

            statement.close();

        } catch (SQLException e) {
            System.out.println("Database initialization error.");
            e.printStackTrace();
        }


    }

    //Create a new collector in the database
    public static int createAccount(SavingsAccount savingsAccount) {

        try {
            PreparedStatement statement = getConnection()
                    .prepareStatement("INSERT INTO SavingsAccounts(AccountTier, OwnerUUID, Balance, LastUpdated, LastChecked) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, savingsAccount.getTier().getAsString());
            statement.setString(2, savingsAccount.getAccountOwner().toString());
            statement.setDouble(3, savingsAccount.getBalance());
            statement.setDate(4, new Date(new java.util.Date().getTime()));
            statement.setDate(5, new Date(new java.util.Date().getTime()));

            statement.execute();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating account failed, no ID obtained.");
                }
            }


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return 0;
    }

    public static List<SavingsAccount> getAllAccounts() {
        List<SavingsAccount> accounts = new ArrayList<>();

        try {
            PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM SavingsAccounts");
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                int AccountID = result.getInt(1);
                AccountTier tier = AccountTier.matchTier(result.getString(2));
                UUID playerUUID = UUID.fromString(result.getString(3));
                double balance = result.getDouble(4);
                Date lastUpdated = result.getDate(5);
                Date lastChecked = result.getDate(6);
                accounts.add(new SavingsAccount(AccountID, playerUUID, tier, balance, lastUpdated, lastChecked));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        Collections.sort(accounts, new SavingsAccountsComparator());
        return accounts;
    }

    public static List<SavingsAccount> getAccounts(Player player) {
        String uuid = player.getUniqueId().toString();
        List<SavingsAccount> accounts = new ArrayList<>();

        try {
            PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM SavingsAccounts WHERE OwnerUUID = ?");
            statement.setString(1, uuid);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                int AccountID = result.getInt(1);
                AccountTier tier = AccountTier.matchTier(result.getString(2));
                double balance = result.getDouble(4);
                Date lastUpdated = result.getDate(5);
                Date lastChecked = result.getDate(6);
                accounts.add(new SavingsAccount(AccountID, player.getUniqueId(), tier, balance, lastUpdated, lastChecked));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        Collections.sort(accounts, new SavingsAccountsComparator());
        return accounts;
    }

//    public static void updateSavingsAccounts(Collection<SavingsAccount> accounts) {
//
//        PreparedStatement statement;
//
//        try {
//
//            statement = getConnection()
//                    .prepareStatement("UPDATE SavingsAccount SET OwnerUUID = ?, Tier = ?, Balance = ? WHERE AccountID = ?");
//            statement.setString(1, account.getAccountOwner().toString());
//            statement.setString(2, account.getTier().getAsString());
//            statement.setDouble(3, account.getBalance());
//            statement.setInt(4, account.getId());
//
//            statement.executeUpdate();
//
//        } catch (SQLException ex) {
//            System.out.println("Error updating savings account in the database. #" + account.getId());
//        }
//
//
//    }

    public static void updateSavingsAccount(SavingsAccount account) {

        PreparedStatement statement;

        try {

            statement = getConnection()
                    .prepareStatement("UPDATE SavingsAccounts SET OwnerUUID = ?, AccountTier = ?, Balance = ?, LastUpdated = ?, LastChecked = ? WHERE AccountID = ?");
            statement.setString(1, account.getAccountOwner().toString());
            statement.setString(2, account.getTier().getAsString());
            statement.setDouble(3, account.getBalance());
            statement.setDate(4, new Date(account.getLastUpdated().getTime()));
            statement.setDate(5, new Date(account.getLastChecked().getTime()));
            statement.setInt(6, account.getId());

            statement.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex);
            System.out.println("Error updating savings account in the database. #" + account.getId());
        }


    }

    public static void deleteAccount(int id) {

        try {

            PreparedStatement statement = getConnection().prepareStatement("DELETE FROM SavingsAccounts WHERE AccountID = ?");
            statement.setInt(1, id);

            statement.execute();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public static double getBalance(int id) {

        try {

            PreparedStatement statement = getConnection().prepareStatement("SELECT Balance FROM SavingsAccounts WHERE AccountID = ?");
            statement.setInt(1, id);

            ResultSet result = statement.executeQuery();
            result.next();
            return result.getDouble(1);


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return 0;
    }

    public static void setBalance(int id, double balance) {

        try {

            PreparedStatement statement = getConnection().prepareStatement("UPDATE SavingsAccounts SET Balance = ? WHERE AccountID = ?");
            statement.setDouble(1, balance);
            statement.setInt(2, id);

            statement.execute();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }







    // ------------------------------------ < ATM STUFF > -----------------------------------------------


    public static void addATM(ATM atm) {

        String uuid = atm.getOwner().getUniqueId().toString();
        Location location = atm.getLocation();

        try {
            PreparedStatement statement = getConnection().prepareStatement("INSERT INTO ATM(Owner, Location) VALUES (?, ?);");
            statement.setString(1, uuid);
            statement.setString(2, Serializer.serializeLocation(location));

            statement.execute();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public static boolean isATMLocation(Location location) {

        try {
            PreparedStatement statement = getConnection().prepareStatement("SELECT COUNT(Location) FROM ATM WHERE Location = ?;");
            statement.setString(1, Serializer.serializeLocation(location));

            ResultSet result = statement.executeQuery();
            result.next();

            return result.getInt(1) > 0;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return false;
    }

    public static void deleteATM(ATM atm) {

        Location location = atm.getLocation();

        try {
            PreparedStatement statement = getConnection().prepareStatement("DELETE FROM ATM WHERE Location = ?;");
            statement.setString(1, Serializer.serializeLocation(location));

            statement.execute();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static ATM getATM(Location location) {

        try {

            PreparedStatement statement = getConnection().prepareStatement("SELECT Owner FROM ATM WHERE Location = ?");
            statement.setString(1, Serializer.serializeLocation(location));

            ResultSet result = statement.executeQuery();
            result.next();

            Player player = Bukkit.getPlayer(UUID.fromString(result.getString(1)));
            return new ATM(player, location);


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }

}

