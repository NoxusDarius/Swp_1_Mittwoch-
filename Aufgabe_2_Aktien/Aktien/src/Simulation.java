import com.mysql.cj.protocol.Resultset;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Simulation {
    public static String DBurl = "jdbc:mysql://localhost:3306/db_Aktien?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    public static Connection connection;
    static Statement myStmt;
    static LocalDate lastDayinDB;
    /*
    -----------------------------------------------------------------------------------------------------------------
    Datenbank Teil
     */

    static void connectToMySql(String pwd) {
        {
            try {
                connection = DriverManager.getConnection(DBurl, "root", pwd);
               // System.out.println("Datenbank für Simulation verknüpft verknüpft");

            } catch (SQLException e) {
                System.out.println("Datenbank wurde nicht verknüpft");
                e.printStackTrace();
            }
        }
    }

    static void createTable(String Stock, String auswahl, double money) throws SQLException {

        try {

            myStmt = connection.createStatement();
            String createtable = "create table if not exists sim_" + auswahl + "" + Stock + " (datum varchar(32) primary key, aktien int , restwert double, status double);";
            //String createtableAVG = "create table if not exists "+Stock+"AVG (datumAVG varchar(255) primary key, avg double);";
            myStmt.executeUpdate(createtable);
           // System.out.println("Create Table abgeschlossen");
            //  myStmt.executeUpdate(createtableAVG);
            dummyLine(Stock, money, auswahl);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    static void dummyLine(String Stock, double kapital, String auswahl) {
        LocalDate dummyDate = LocalDate.of(1000, 1, 1);
        try {
            myStmt = connection.createStatement();

            String writeData = "insert ignore into sim_" + auswahl + "" + Stock + " (datum,aktien,restwert,status ) values('" + dummyDate + "', '" + 0 + "','" + kapital + "','" + 0 + "');";
            myStmt.executeUpdate(writeData);

           // System.out.println("Datensatz eingetragen");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }static void dropTable(String Stock,String auswahl){
        try {
            myStmt = connection.createStatement();

            String droptable = "drop table if exists sim_" + auswahl + "" + Stock + " ;";
            myStmt.executeUpdate(droptable);

            // System.out.println("Datensatz eingetragen");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
        Datenbank Teil fertig
    --------------------------------------------------------------------------------------
        Methode mit Prozentangabe
     */
    static void withpercent(LocalDate startdate, String Stock, double toleranz, double money) throws SQLException {

        String auswahl = "withpercent";
        dropTable(Stock,auswahl);
        createTable(Stock, auswahl, money);
        for (LocalDate currentDate = startdate; currentDate.isBefore(LocalDate.now()); currentDate = currentDate.plusDays(1)) {

            List<Double> result = selectLastDate(Stock, Arrays.asList("status", "restwert", "aktien"), auswahl);
            double status = result.get(0), rest = result.get(1), aktien = result.get(2);

            if (status == 0) {
                if (!currentDate.isEqual(LocalDate.now().minusDays(1))) {
                    buy(currentDate, Stock, rest, toleranz, auswahl);
                }

            } else if (status == 1) {

                sell(currentDate, Stock, aktien, rest, toleranz, auswahl);
            }
        }

        List<Double> resultend = selectLastDate(Stock, Arrays.asList("restwert", "status", "aktien"), auswahl);
        double endDepo = resultend.get(0), status = resultend.get(1), aktienend = resultend.get(2);
        List<String> datumend = getLastDate(Stock, Arrays.asList("datum"));
        String datumfinal = datumend.get(0);
        LocalDate today = LocalDate.parse(datumfinal);

        if (status == 1) {

            List<Double> closeend = selectbyDate(Stock, today, Arrays.asList("adjustedclose"));

            double closetemp = closeend.get(0);

            double endwet = (closetemp * aktienend) + endDepo;
            System.out.println("Der Endbetrag mit prozentzugabe beträgt:" + endwet);
        } else if (status == 0) {

            System.out.println("Der Endbetrag mit prozentzugabe beträgt:" + endDepo);
        }

    }

    /*
    -----------------------------------------------------------------------------------
        Methode ohne Prozentangabe
     */
    static void normalway(LocalDate startdate, String Stock, double money) throws SQLException {
        String auswahl = "normalway";
        dropTable(Stock,auswahl);
        createTable(Stock, auswahl, money);
        for (LocalDate currentDate = startdate; currentDate.isBefore(LocalDate.now()); currentDate = currentDate.plusDays(1)) {
            // if (börsenfeiertage(currentDate)) {
            // System.out.println(currentDate);
            List<Double> result = selectLastDate(Stock, Arrays.asList("status", "restwert", "aktien"), auswahl);
            double status = result.get(0), rest = result.get(1), aktien = result.get(2);
            // System.out.println("Event"+event +" dep"+dep+" shares"+share);

            if (status == 0) {
                if (!currentDate.isEqual(LocalDate.now().minusDays(1))) {
                    double toleranz = 0;
                    buy(currentDate, Stock, rest, toleranz, auswahl);
                }

            } else if (status == 1) {
                double toleranz = 0;
                sell(currentDate, Stock, aktien, rest, toleranz, auswahl);
            }


        }

        // }
        List<Double> resultend = selectLastDate(Stock, Arrays.asList("restwert", "status", "aktien"), auswahl);
        double endDepo = resultend.get(0), status = resultend.get(1), aktienend = resultend.get(2);
        List<String> datumend = getLastDate(Stock, Arrays.asList("datum"));
        String datumfinal = datumend.get(0);
        LocalDate today = LocalDate.parse(datumfinal);

        if (status == 1) {

            List<Double> closeend = selectbyDate(Stock, today, Arrays.asList("adjustedclose"));

            double closetemp = closeend.get(0);

            double endwet = (closetemp * aktienend) + endDepo;
            System.out.println("Der Endbetrag   ohne prozentzugabe beträgt:" + endwet);
        } else if (status == 0) {

            System.out.println("Der Endbetrag ohne prozentzugabe  beträgt:" + endDepo);
        }

    }
    /*
    ----------------------------------------------------------------------------------------
        Buy & Hold Methode

     */
    public static void buyHold(LocalDate tempDate,String Stock,double Kapital){

    }

    /*
    --------------------------------------------------------------------------------------------
        Restliche Methoden

     */
    public static void buy(LocalDate tempDate, String Stock, double dep, double toleranz, String auswahl) {
        try {
            List<Double> result = selectbyDate(Stock, tempDate, Arrays.asList("adjustedclose", "closeAVG"));

            double adjustedclosed = result.get(0), closeAVG = result.get(1);
            adjustedclosed *= (1 + (toleranz / 100));
            if (adjustedclosed > closeAVG) {
                double aktien, rest;
                if (dep % adjustedclosed == 0) {
                    aktien = dep / adjustedclosed;
                    rest = 0;
                } else {
                    aktien = Math.floor(dep / adjustedclosed);
                    rest = dep % adjustedclosed;
                }
                insert(rest, Stock, aktien, tempDate, 1, auswahl);
            }

        } catch (IndexOutOfBoundsException e) {
           // System.out.println("Datenbank eintrag fehlt" + tempDate);
        }


    }

    public static List<String> getLastDate(String Stock, List<String> werte) {
        List<String> results = new ArrayList();
        String getvaluedummy = "select * from " + Stock + " order by datum desc limit 1;";
        try {
            myStmt = connection.createStatement();
            ResultSet rsdummy = myStmt.executeQuery(getvaluedummy);
            while (rsdummy.next()) {
                for (String a : werte) {
                    results.add(rsdummy.getString(a));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    public static void sell(LocalDate tempDate, String Stock, double aktien, double rest, double toleranz, String auswahl) {
        try {


            List<Double> result = selectbyDate(Stock, tempDate, Arrays.asList("adjustedclose", "closeAVG"));
            double adjustedclosed = result.get(0), closeAVG = result.get(1);
            adjustedclosed *= (1 + (toleranz / 100));
            if (adjustedclosed < closeAVG) {
                double depo;
                depo = (adjustedclosed * aktien) + rest;
                insert(depo, Stock, 0, tempDate, 0, auswahl);
            }
        } catch (IndexOutOfBoundsException e) {
          //  System.out.println("Der Datenbank eintrag fehlt" + tempDate);
        }

    }


    public static List<Double> selectLastDate(String Stock, List<String> werte, String auswahl) {
        List<Double> results = new ArrayList();
        String getvaluedummy = "select * from sim_" + auswahl + "" + Stock + " order by datum desc limit 1;";


        try {
            myStmt = connection.createStatement();
            ResultSet rsdummy = myStmt.executeQuery(getvaluedummy);
            while (rsdummy.next()) {
                for (String a : werte) {
                    results.add(rsdummy.getDouble(a));

                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    public static List<Double> selectbyDate(String Stock, LocalDate tempDate, List<String> werte) {
        List<Double> result = new ArrayList<>();
        String getClose = "select * from " + Stock + " where Datum =\'" + tempDate + "\'having datum is not null;";
        try {

            myStmt = connection.createStatement();
            ResultSet rsCLose = myStmt.executeQuery(getClose);

            while (rsCLose.next()) {
                for (String a : werte) {

                    result.add(rsCLose.getDouble(a));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void insert(double depo, String Stock, double aktien, LocalDate tempDate, double status, String auswahl) {
        try {
            myStmt = connection.createStatement();

            String insertAktien = "insert into sim_" + auswahl + "" + Stock + " (datum,aktien,restwert,status ) values('" + tempDate + "', '" + aktien + "','" + depo + "','" + status + "');";
            myStmt.executeUpdate(insertAktien);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }
}



