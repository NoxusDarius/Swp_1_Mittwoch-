import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
public class ReadTxt {

    Scanner companiesRead;
    Scanner Length;
    Scanner apiread;
    Scanner password;
    public int length;
    public int sLength;
    public String api;
    public String pwd;
    ArrayList<String> dataCompanies = new ArrayList<>();

    ReadTxt(File companies,File apiKey, File passwort) throws FileNotFoundException {
        companiesRead = new Scanner(companies);
        apiread = new Scanner(apiKey);
        Length = new Scanner(companies);
        password = new Scanner(passwort);
        length = companiesRead.nextInt();

    }
    public ArrayList<String> read() {
        if (companiesRead.hasNext()) {

           // System.out.println(length);
            for (int i = 0; i < length; i++) {
                dataCompanies.add(companiesRead.next()) ;
            }
            return dataCompanies;
        }
        return null;
    }
    public void close(){
        companiesRead.close();
    }
    public String apiReadM(){
            if(apiread.hasNext()){
                api = apiread.next();

                return api;
            }
            return null;

    }
    public String pwdEinlesen(){
        if(password.hasNext()){
            pwd = password.next();

            return pwd;
        }
        return null;
    }
}
