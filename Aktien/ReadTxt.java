import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public final class ReadTxt {

    private final File companies, apiKey, password;

    ReadTxt(File companies, File apiKey, File passwort) {
        this.companies = companies;
        this.apiKey = apiKey;
        this.password = passwort;
    }

    public ArrayList<String> read() {
        return readFile(companies);
    }

    public static <T> ArrayList<T> readFile(File f) {
        ArrayList<T> list = new ArrayList<>();
        try (BufferedReader fileReader = new BufferedReader(new FileReader(f))) {
            T line;
            while ((line = (T) fileReader.readLine()) != null) {
                list.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public String apiReadM() {
        return (String) readFile(apiKey).get(0);
    }

    public String pwdEinlesen() {
        return (String) readFile(password).get(0);
    }
}
