import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
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

        for (int firma = 0; firma < dataCompanies.size(); firma++) {
            Stock = dataCompanies.get(firma);
            Simulation.connectToMySql(pawd);
            Simulation.normalway(dateStart, Stock, money);
            Simulation.withpercent(dateStart, Stock, toleranz, money);
            Simulation.hold(Stock, dateStart, money);
        }
        Simulation.gesamtAusgabe();
    }

    public static void eingabe() {
        System.out.println("How much money do u want do invest?: ");
        money = scanner.nextDouble();
        System.out.println("At which Date do you want to start investing your money? [d/MM/yyyy]");
        String beginDate = scanner.next();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/MM/yyyy");
        dateStart = LocalDate.parse(beginDate, formatter);
        System.out.println("Toleranz:[%] ");
        toleranz = scanner.nextDouble();
    }
}
