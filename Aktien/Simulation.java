import com.mysql.cj.protocol.Resultset;

import java.sql.*;
import java.text.NumberFormat;
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
    static double gesamtWithout;
    static double gesamtWith;
    static double gesamthold;

    static  NumberFormat n = NumberFormat.getInstance();

    /*
    -----------------------------------------------------------------------------------------------------------------
    Datenbank Teil
     */

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

    static void createTable(String Stock, String auswahl, double money, LocalDate tempDate) throws SQLException {

        try {

            myStmt = connection.createStatement();
            String createtable = "create table if not exists sim_" + auswahl + " (datum varchar(32) , aktien int , restwert double, status double,stock varchar(4), primary key(datum,stock));";
            myStmt.executeUpdate(createtable);


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    static void dummyLine(String Stock, double kapital, String auswahl, LocalDate tempdate) {
        LocalDate dummyDate = LocalDate.of(1000, 1, 1);
        try {
            myStmt = connection.createStatement();

            String writeData = "insert ignore into sim_" + auswahl + " (datum,aktien,restwert,status,stock ) values('" + tempdate.minusDays(1) + "', '" + 0 + "','" + kapital + "','" + 0 + "','"+Stock+"');";
            myStmt.executeUpdate(writeData);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void dropTable(String Stock, String auswahl) {
        try {
            myStmt = connection.createStatement();

            String droptable = "drop table if exists sim_" + auswahl + " ;";
            myStmt.executeUpdate(droptable);

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
        n.setMaximumFractionDigits(2);
        String auswahl = "withpercent";



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
            gesamtWith = gesamtWith + endwet;
            System.out.println("Der Endbetrag mit beträgt mit "+Stock+" "+toleranz+"%: " + n.format(endwet)+"€");
        } else if (status == 0) {
            gesamtWith = gesamtWith + endDepo;

            System.out.println("Der Endbetrag mit beträgt mit  "+toleranz+"%:"+Stock+": " + n.format(endDepo)+"€");
        }

    }

    /*
    -----------------------------------------------------------------------------------
        Methode ohne Prozentangabe
     */


    static void normalway(LocalDate startdate, String Stock, double money) throws SQLException {
        n.setMaximumFractionDigits(2);
        String auswahl = "normalway";



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
            gesamtWithout = gesamtWithout+endwet;

            System.out.println("Der Endbetrag beträgt bei der 200er Strategie:"+Stock+": " + n.format(endwet)+"€");


        } else if (status == 0) {

            gesamtWithout = gesamtWithout + endDepo;

            System.out.println("Der Endbetrag beträgt bei der 200er Strategie: "+Stock+": " + n.format(endDepo)+"€");
        }

    }

    /*
    ----------------------------------------------------------------------------------------
        Buy & Hold Methode

     */
    public static void hold(String Stock, LocalDate tmepDate, double Kapital) {
        n.setMaximumFractionDigits(2);

            try {

                List<String> lastdate = getfirstDate(Stock, Arrays.asList("datum"), tmepDate);
                LocalDate today = LocalDate.parse(lastdate.get(0));


                List<Double> closewert = selectbyDate(Stock, today, Arrays.asList("adjustedclose"));
                double closetemp = closewert.get(0);
                //System.out.println(today);

                double aktien, rest;
                if (Kapital % closetemp == 0) {
                    aktien = Kapital / closetemp;
                    rest = 0;
                } else {
                    aktien = Math.floor(Kapital / closetemp);
                    rest = Kapital % closetemp;
                }
                insert(rest,Stock,aktien,today,1,"hold");
                List<String> lastdatetmep = getLastDate(Stock, Arrays.asList("datum"));
                LocalDate lastsate = LocalDate.parse(lastdatetmep.get(0));
                List<Double> closewertend = selectbyDate(Stock, lastsate, Arrays.asList("adjustedclose"));
               // System.out.println(lastsate);
                double closetempend = closewertend.get(0);
                double endwert = (aktien * closetempend) + rest;
                gesamthold = gesamthold + endwert;
                insert(endwert,Stock,0,lastsate,0,"hold");

                System.out.println("Der Endbertrag für die Hold Strategie beträgt: "+Stock+": " + n.format(endwert)+"€");
                //System.out.println("DerProzentuelle Gewinn beträgt:" +(endwert/Kapital));



            } catch (IndexOutOfBoundsException e) {
                System.out.println("Falsche EIngabe bitte versuchen sie es enrneut(Wähle einen Vorhandenen Aktientag aus)");
            }


    }

    /*
    --------------------------------------------------------------------------------------------
        Restliche Methoden

     */
    public static void buy(LocalDate tempDate, String Stock, double dep, double toleranz, String auswahl) {
        try {
            List<Double> result = selectbyDate(Stock, tempDate, Arrays.asList("adjustedclose", "closeAVG"));

            double adjustedclosed = result.get(0), closeAVG = result.get(1);
            closeAVG *= (1 + (toleranz / 100));
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

    public static List<String> getfirstDate(String Stock, List<String> werte, LocalDate tempDate) {
        List<String> results = new ArrayList();
        String getvaluedummy = "select * from " + Stock + " where datum >= '" + tempDate + "' order by datum asc limit 1;";
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
            closeAVG *= (1 - (toleranz / 100));
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
        String getvaluedummy = "select * from sim_" + auswahl + " where stock='"+Stock+"' order by datum desc limit 1;";


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
        String getClose = "select * from " + Stock + " where Datum ='" + tempDate + "'having datum is not null;";
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

            String insertAktien = "insert into sim_" + auswahl + " (datum,aktien,restwert,status,stock ) values('" + tempDate + "', '" + aktien + "','" + depo + "','" + status + "','"+Stock+"');";
            myStmt.executeUpdate(insertAktien);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


    }
    public static void gesamtAusgabe(){
        n.setMaximumFractionDigits(2);

        System.out.println("Gesamtausgabe");
        System.out.println("Gesamt ohne Prozent: "+n.format(gesamtWithout)+"€");
        System.out.println("Gesamt mit Prozent: "+n.format(gesamtWith)+"€");
        System.out.println("Gesamt hold: "+n.format(gesamthold)+"€");
    }
}



