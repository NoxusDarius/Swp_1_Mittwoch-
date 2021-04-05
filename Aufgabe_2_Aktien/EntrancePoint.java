import java.awt.image.RenderedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.chart.*;
import javafx.scene.image.WritableImage;
import org.json.JSONException;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Application;
import javax.imageio.ImageIO;
import java.io.File;



public class EntrancePoint extends Application  {
    static String Stock;
    static String apikey;
    static String pawd;
    public static void main(String[] args) throws IOException, JSONException {
        Application.launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        File file;
        try {
            File companies = new File("C:\\Users\\Anwender\\IdeaProjects\\Aufgabe_2_Aktien\\Aktien\\src\\companies");
            File apiKeyLocation = new File("C:\\Users\\Anwender\\IdeaProjects\\Aufgabe_2_Aktien\\Aktien\\src\\Key.txt");
            File passwort= new File("C:\\Users\\Anwender\\IdeaProjects\\Aufgabe_2_Aktien\\Aktien\\src\\Password.txt");
            ReadTxt c = new ReadTxt(companies,apiKeyLocation,passwort);
            c.read();
            c.apiReadM();
            c.pwdEinlesen();
            apikey = c.api;
            pawd = c.pwd;
            System.out.println((c.dataCompanies));
            c.close();


            for( int firma=0; firma<c.dataCompanies.size(); firma++) {

                Stock = c.dataCompanies.get(firma);

               try {
                   String URL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=" + Stock + "&outputsize=full&apikey=" + apikey + "";


                   SQL newSQL = new SQL();
                   newSQL.getAktienwert2(URL);
                   newSQL.connectToMySql(pawd);
                   newSQL.createTable(Stock);
                   newSQL.writeDataInDB(Stock);
                   newSQL.getData(Stock);
                   newSQL.avgBerechnen();
                   newSQL.createTableAVG(Stock);
                   newSQL.writeDataInDBAVG(Stock);
                   newSQL.getDataAVG(Stock);

                   //Angaben wie die Axen sein sollten
                   final NumberAxis yAxis = new NumberAxis();
                   final CategoryAxis xAxis = new CategoryAxis();

                   // Anlegen der BarChart und Angabe wie die Anordnung
                   final LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
                   lineChart.setTitle("Aktienkurs von " + Stock);
                   xAxis.setLabel("Datum ");
                   yAxis.setLabel("CloseWerte");

                   XYChart.Series<String, Number> CloseWerte = new XYChart.Series();

                   for (int i = 0; i < SQL.closeWerteDB.size() - 1; i++) {
                       CloseWerte.getData().add(new XYChart.Data(SQL.DatumDB.get(i), SQL.adjustedcloseWerteDB.get(i)));
                   }
                   XYChart.Series<String, Number> AVG = new XYChart.Series();

                   for (int i = 0; i < SQL.closeWerteDB.size() - 1; i++) {
                       AVG.getData().add(new XYChart.Data(SQL.DatumDB.get(i), SQL.durchschnitt.get(i)));
                   }

                   Scene scene = new Scene(lineChart, 1000, 600);
                   lineChart.getData().add(CloseWerte);
                   lineChart.getData().add(AVG);
                   lineChart.setCreateSymbols(false);

                   for (int i = 0; i < SQL.closeWerteDB.size(); i++) {
                       if (SQL.closeWerteDB.get(i) > SQL.durchschnitt.get(i)) {
                           scene.getStylesheets().add("site.css");

                       }
                       if (SQL.closeWerteDB.get(i) < SQL.durchschnitt.get(i)) {
                           scene.getStylesheets().add("site2.css");

                       }
                   }

                   // Collections.reverse(closeWerteDB);
                   yAxis.setAutoRanging(false);
                   double AbstandOBEN = Collections.max(SQL.closeWerteDB);
                   double AbstandUNTEN = Collections.min(SQL.closeWerteDB);

                   yAxis.setLowerBound(AbstandUNTEN - 20);
                   yAxis.setUpperBound(AbstandOBEN + 20);
                   CloseWerte.setName("CloseWerte");
                   AVG.setName("AVG");
                   primaryStage.setScene(scene);
                   //  primaryStage.show();

                   SQL.closeWerte.clear();
                   SQL.dateList.clear();
                   SQL.DatumDB.clear();
                   SQL.closeWerteDB.clear();
                   SQL.durchschnitt.clear();
                   SQL.adjustedCloseWerte.clear();
                   SQL.adjustedcloseWerteDB.clear();

                   file = new File("C:\\Users\\Anwender\\IdeaProjects\\Aufgabe_2_Aktien\\PNG\\" + Stock + LocalDate.now() + ".png");
                   WritableImage writableImage = new WritableImage((int) lineChart.getWidth(), (int) lineChart.getHeight());
                   lineChart.snapshot(null, writableImage);
                   RenderedImage rImage = SwingFXUtils.fromFXImage(writableImage,
                           null);
                   ImageIO.write(rImage, "png", file);
                   primaryStage.close();
                   System.out.println("Finished");
               }
               catch (Exception r){
                   System.out.println("Fehler flasche eingabe");

               }



            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }



    }



