import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class SimMain {
    static double money;
    static LocalDate dateStart;
    static double toleranz;
    static String apikey;
    static String pawd;
    static String Stock;

    private static List<String> dataCompanies = null;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws SQLException, FileNotFoundException {
        eingabe();

        File companies = new File("Aktien/src/companies");
        File apiKeyLocation = new File("Aktien/src/Key.txt");
        File passwort = new File("Aktien/src/Password.txt");
        ReadTxt c = new ReadTxt(companies, apiKeyLocation, passwort);
        dataCompanies = c.read();
        apikey = c.apiReadM();
        pawd = c.pwdEinlesen();
        System.out.println((dataCompanies));
        money = money / dataCompanies.size();

        System.out.println(money);
        Simulation.connectToMySql(pawd);
        droptables();
        createtables();


        for (int firma = 0; firma < dataCompanies.size(); firma++) {
            Stock = dataCompanies.get(firma);
            System.out.println("Firma: " + Stock);
            Simulation.dummyLine(Stock, money,"withpercent" , dateStart);
            Simulation.dummyLine(Stock, money,"normalway" , dateStart);
            Simulation.normalway(dateStart, Stock, money);
            Simulation.withpercent(dateStart, Stock, toleranz, money);
            Simulation.hold(Stock, dateStart, money);
            System.out.println(" ");
        }
        Simulation.gesamtAusgabe();


    }

    public static void eingabe() {
        boolean eingabe = false;
        String temp;
        do{
             System.out.println("How much money do u want do invest?: ");
             temp = scanner.next();
             eingabe = zahlÜp(temp);
        }while (eingabe != true);

        do{
            System.out.println("At which Date do you want to start investing your money? [d/MM/yyyy]");
            temp = scanner.next();
            eingabe = dateÜp(temp);
        }while (eingabe != true);
        do {
            System.out.println("Toleranz:[%] ");
            temp = scanner.next();
            eingabe = tolÜp(temp);
        }while (eingabe != true);





    }
    public static boolean zahlÜp(String moneytemp){
        try{
            money = Integer.parseInt(moneytemp);
            if( money <0 ){
                System.out.println("Eine Positive Zahl muss eingegeben werden");
                return false;
            }
            return true;
        }catch(NumberFormatException e){
            System.out.println("Es muss eine Zahl eingegeben werden");
        }
        return false;
    }

    public static boolean dateÜp(String tempdate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/MM/yyyy");
            dateStart = LocalDate.parse(tempdate, formatter);
            return true;

        } catch (Exception e) {
            System.out.println("Es muss ein Datum angegeben werden [d/mm/yy]");
            return false;
        }
    }
    public static boolean tolÜp(String temptol){
        try{
            toleranz = Integer.parseInt(temptol);
            if( toleranz <0 || toleranz >100){
                System.out.println("Eine Positive Zahl muss eingegeben werden");
                return false;
            }
            return true;
        }catch(NumberFormatException e){
            System.out.println("Es muss eine Zahl eingegeben werden");
        }
        return false;
    }
    public static void droptables(){
        Simulation.dropTable(Stock, "withpercent");
        Simulation.dropTable(Stock, "normalway");
        Simulation.dropTable(Stock,"hold");
    }
    public static void createtables() throws SQLException {
        Simulation.createTable(Stock, "withpercent", money, dateStart);
        Simulation.createTable(Stock, "normalway", money, dateStart);
        Simulation.createTable(Stock, "hold", money, dateStart);
    }
}
