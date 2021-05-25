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

    static void connectToMySql(String pwd) {
        {
            try {
                connection = DriverManager.getConnection(DBurl, "root", pwd);
                System.out.println("Datenbank für Simulation verknüpft verknüpft");

            } catch (SQLException e) {
                System.out.println("Datenbank wurde nicht verknüpft");
                e.printStackTrace();
            }
        }
    }

    static void createTable(String Stock) throws SQLException {

        try {

            myStmt = connection.createStatement();
            String createtable = "create table if not exists sim_" + Stock + " (datum varchar(32) primary key, aktien int , restwert double, status double);";
            //String createtableAVG = "create table if not exists "+Stock+"AVG (datumAVG varchar(255) primary key, avg double);";
            myStmt.executeUpdate(createtable);
            System.out.println("Create Table abgeschlossen");
            //  myStmt.executeUpdate(createtableAVG);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    static void dummyLine(String Stock, double kapital) {
        LocalDate dummyDate = LocalDate.of(1000, 1, 1);
        try {
            myStmt = connection.createStatement();

            String writeData = "insert ignore into sim_" + Stock + " (datum,aktien,restwert,status ) values('" + dummyDate + "', '" + 0 + "','" + kapital + "','" + 0 + "');";
            myStmt.executeUpdate(writeData);

            System.out.println("Datensatz eingetragen");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void normalway(LocalDate startdate,String Stock) {

        for (LocalDate currentDate = startdate; currentDate.isBefore(LocalDate.now()); currentDate=currentDate.plusDays(1)) {
            if (börsenfeiertage(startdate)) {
               // System.out.println(currentDate);
                List<Double> result = selectLastDate(Stock, Arrays.asList("status","restwert","aktien"));
                double status =result.get(0), rest=result.get(1), aktien= result.get(2);
              // System.out.println("Event"+event +" dep"+dep+" shares"+share);


                if(status == 0){
                    if(!currentDate.isEqual(LocalDate.now().minusDays(1))) {
                        buy(currentDate, Stock, rest);
                    }

                }

                else if( status == 1){

                    sell(currentDate,Stock,aktien,rest);
                }


            }

        }
        List<Double> resultend = selectLastDate(Stock,Arrays.asList("restwert"));
        double endDepo = resultend.get(0);
        System.out.println("Das ist das Enddepot: "+endDepo);
    }

    public static void buy(LocalDate tempDate,String Stock,double dep){
        try{
        List<Double> result = selectbyDate(Stock,tempDate,Arrays.asList("adjustedclose","closeAVG"));

        double adjustedclosed = result.get(0), closeAVG=result.get(1);
        if(adjustedclosed > closeAVG) {
            double aktien, rest;
            if (dep % adjustedclosed == 0) {
                aktien = dep / adjustedclosed;
                rest = 0;
            } else {
                aktien = Math.floor(dep / adjustedclosed);
                rest = dep % adjustedclosed;
            }
            insert(rest, Stock, aktien, tempDate,1);
        }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    public static void sell(LocalDate tempDate,String Stock,double aktien,double rest){
        try{


            List<Double> result = selectbyDate(Stock,tempDate,Arrays.asList("adjustedclose","closeAVG"));
            double adjustedclosed = result.get(0), closeAVG = result.get(1);
            if(adjustedclosed < closeAVG){
                double depo;
                depo = (adjustedclosed*aktien)+rest;
                insert(depo,Stock,0,tempDate,0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static List<Double>  selectLastDate(String Stock, List<String> werte){
        List<Double> results = new ArrayList();
        String getvaluedummy = "select * from sim_"+Stock+" order by datum desc limit 1;";


        try{
            myStmt = connection.createStatement();
            ResultSet rsdummy =  myStmt.executeQuery(getvaluedummy);
            while (rsdummy.next()){
                for(String a:werte){
                    results.add(rsdummy.getDouble(a));

                }
            }

        }catch (SQLException e){
            e.printStackTrace();
        }
        return results;
    }
    public static List<Double> selectbyDate(String Stock,LocalDate tempDate, List<String> werte){
        List<Double> result = new ArrayList<>();
        String getClose = "select * from "+ Stock +" where Datum =\'"+tempDate+"\'having datum is not null;";
        try{

            myStmt = connection.createStatement();
            ResultSet rsCLose = myStmt.executeQuery(getClose);

            while(rsCLose.next()){
                for( String a:werte){

                    result.add(rsCLose.getDouble(a));
                }
            }

        }catch(SQLException e){
            e.printStackTrace();
        }
        return result;
    }
    public static void insert(double depo, String Stock, double aktien,LocalDate tempDate,double status){
        try {
            myStmt = connection.createStatement();

            String insertAktien = "insert into sim_" + Stock + " (datum,aktien,restwert,status ) values('" + tempDate + "', '" + aktien + "','" + depo + "','" + status + "');";
            myStmt.executeUpdate(insertAktien);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }


    public static boolean börsenfeiertage(LocalDate tempDate){
        if(DayOfWeek.SUNDAY.equals(tempDate.getDayOfWeek())){
            return false;
        } else if (DayOfWeek.SATURDAY.equals(tempDate.getDayOfWeek())){
            return false;
        }else if(tempDate.equals(LocalDate.of(tempDate.getYear(),1,1))){
            return false;
        }
        else if(tempDate.equals(LocalDate.of(tempDate.getYear(),4,2))){
            return false;
        }
        else if(tempDate.equals(LocalDate.of(tempDate.getYear(),4,6))){
            return false;
        }
        else if(tempDate.equals(LocalDate.of(tempDate.getYear(),5,24))){
            return false;
        }
        else if(tempDate.equals(LocalDate.of(tempDate.getYear(),10,26))){
            return false;
        }
        else if(tempDate.equals(LocalDate.of(tempDate.getYear(),11,1))){
            return false;
        }
        else if(tempDate.equals(LocalDate.of(tempDate.getYear(),12,8))){
            return false;
        }
        else if(tempDate.equals(LocalDate.of(tempDate.getYear(),12,24))){
            return false;
        }
        else if(tempDate.equals(LocalDate.of(tempDate.getYear(),12,31))){
            return false;
        }
        return true;
    }
}
