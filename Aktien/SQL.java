import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;


public class SQL implements Closeable {
    public static String DBurl = "jdbc:mysql://localhost:3306/db_Aktien?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    static Statement myStmt;
    public static Connection connection;

    static List<LocalDate> dateList = new ArrayList<>();
    static ArrayList<Double> closeWerte = new ArrayList<>();
    static ArrayList<Double> adjustedCloseWerte = new ArrayList<>();
    static ArrayList<Double> durchschnitt = new ArrayList<>();
    static ArrayList<Double> closeWerteDB = new ArrayList<>();
    static ArrayList<String> DatumDB = new ArrayList<>();
    static ArrayList<Double> adjustedcloseWerteDB = new ArrayList<>();

    static void getAktienwert2(String URL) throws JSONException, IOException {
        JSONObject json = new JSONObject(IOUtils.toString(new URL(URL), StandardCharsets.UTF_8));
        json = json.getJSONObject("Time Series (Daily)");
        System.out.println(URL);
        for (int i = 0; i < json.names().length(); i++) {
            dateList.add(LocalDate.parse((CharSequence) json.names().get(i)));
            closeWerte.add(json.getJSONObject(LocalDate.parse((CharSequence) json.names().get(i)).toString()).getDouble("4. close"));
            adjustedCloseWerte.add(json.getJSONObject(LocalDate.parse((CharSequence) json.names().get(i)).toString()).getDouble("5. adjusted close"));
        }
    }

    public static void avgBerechnen() {
        int count = 0;
        double wert = 0, x, avg;
        for (int i = 0; i <= adjustedcloseWerteDB.size() - 1; i++) {
            count++;
            if (count <= 200) {
                wert = wert + adjustedcloseWerteDB.get(i);
                avg = wert / count;
                durchschnitt.add(avg);
            }
            if (count > 200) {
                x = adjustedcloseWerteDB.get(i - 200);
                wert = wert - x;
                wert = wert + adjustedcloseWerteDB.get(i);
                avg = wert / 200;
                durchschnitt.add(avg);
            }
        }

    }

    static void connectToMySql(String pwd) {
        {
            try {
                connection = DriverManager.getConnection(DBurl, "root", pwd);
            } catch (SQLException e) {
                System.out.println("Datenbank wurde nicht verknüpft");
                e.printStackTrace();
            }

        }
    }

    static void createTable(String Stock) throws SQLException {
        if (connection.isValid(5)) {
            //  System.out.println("Connection überprüft");
            try {

                myStmt = connection.createStatement();
                String createtable = "create table if not exists " + Stock + " (datum varchar(255) primary key, close double, adjustedclose double, closeAVG double);";
                //String createtableAVG = "create table if not exists "+Stock+"AVG (datumAVG varchar(255) primary key, avg double);";
                myStmt.executeUpdate(createtable);
                //  System.out.println("Create Table abgeschlossen");
                //  myStmt.executeUpdate(createtableAVG);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else System.out.println("Nicht verbunden");
    }

    static void writeDataInDB(String Stock) {
        try {
            //System.out.println(durchschnitt);
            myStmt = connection.createStatement();
            for (int i = 0; i < dateList.size(); i++) {
                String writeData = "insert ignore into " + Stock + " (datum, close,adjustedclose, closeAVG ) values('" + dateList.get(i) + "', '" + closeWerte.get(i) + "','" + adjustedCloseWerte.get(i) + "','" + null + "');";
                myStmt.executeUpdate(writeData);
            }
            //  System.out.println("Datensatz eingetragen");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void getData(String Stock) {
        try {
            Statement myStmt = connection.createStatement();


            ResultSet rsNormal = myStmt.executeQuery("SELECT * from " + Stock + " order by datum");
            // System.out.println("Stock=" + Stock);
            while (rsNormal.next()) {
                DatumDB.add(rsNormal.getString("datum"));
                closeWerteDB.add(rsNormal.getDouble("close"));
                adjustedcloseWerteDB.add(rsNormal.getDouble("adjustedclose"));
            }
            // System.out.println(adjustedcloseWerteDB);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void updateDurchschnitt(String Stock) {
        try {

            Statement myStmt = connection.createStatement();
            for (int i = 0; i < durchschnitt.size(); i++) {
                String update = "update " + Stock + " set closeAVG=" + durchschnitt.get(i) + "where datum='" + DatumDB.get(i) + "';";
                myStmt.executeUpdate(update);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}