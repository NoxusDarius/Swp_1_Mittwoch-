import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.json.JSONException;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;


public class EntrancePoint extends Application {
    static String Stock;
    static String apikey;
    static String pawd;

    private static List<String> dataCompanies = null;

    public static void main(String[] args) throws IOException, JSONException {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //File extern einlesen weil sonst immer wenn auf anderen gerät pfade und alles ändern
        ReadTxt c = new ReadTxt(new File("Aktien/src/companies"),
                new File("Aktien/src/Key.txt"),
                new File("Aktien/src/Password.txt"));
        dataCompanies = c.read();
        apikey = c.apiReadM();
        pawd = c.pwdEinlesen();
        System.out.println((dataCompanies));

        for (int firma = 0; firma < dataCompanies.size(); firma++) {

            Stock = dataCompanies.get(firma);
            String URL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=" + Stock + "&outputsize=full&apikey=" + apikey + "";

            try(SQL newSQL = new SQL()) {
                newSQL.getAktienwert2(URL);
                newSQL.connectToMySql(pawd);

                newSQL.createTable(Stock);
                newSQL.writeDataInDB(Stock);
                newSQL.getData(Stock);
                newSQL.avgBerechnen();
                newSQL.updateDurchschnitt(Stock);
            }

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

            File file = new File("C:\\Users\\Anwender\\Desktop\\Aufgabe_2_Aktien\\PNG" + Stock + LocalDate.now() + ".png");
            WritableImage writableImage = new WritableImage((int) lineChart.getWidth(), (int) lineChart.getHeight());
            lineChart.snapshot(null, writableImage);
            RenderedImage rImage = SwingFXUtils.fromFXImage(writableImage,
                    null);
            ImageIO.write(rImage, "png", file);
            primaryStage.close();
            System.out.println("Finished");
        }
    }
}



