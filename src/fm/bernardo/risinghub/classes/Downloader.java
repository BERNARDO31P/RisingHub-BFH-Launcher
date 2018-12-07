package fm.bernardo.risinghub.classes;


import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static com.jcabi.manifests.Manifests.read;
import static fm.bernardo.risinghub.Main.folderSymbol;
import static fm.bernardo.risinghub.Main.gameLocation;
import static fm.bernardo.risinghub.classes.ShowAlert.showAlert;

public final class Downloader extends Thread {
    private ArrayList<String> fileList;
    private Label infoLabelAll, infoLabelSingle;
    private Button playButton;
    private ProgressBar progressBarAll, progressBarSingle;
    private int alreadyDownloaded, fileCount;

    Downloader(ArrayList<String> fileList, Label infoLabelAll, Button playButton, ProgressBar progressBarAll, Label infoLabelSingle, ProgressBar progressBarSingle, int alreadyDownloaded, int fileCount) {
        this.fileList = fileList;
        this.infoLabelAll = infoLabelAll;
        this.playButton = playButton;
        this.progressBarAll = progressBarAll;
        this.infoLabelSingle = infoLabelSingle;
        this.progressBarSingle = progressBarSingle;
        this.alreadyDownloaded = alreadyDownloaded;
        this.fileCount = fileCount;
    }

    private double round(final double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public void run() {
        try {
            Platform.runLater(() -> {
                this.progressBarSingle.setVisible(true);
                this.infoLabelSingle.setVisible(true);
            });
            for (final String fileName : this.fileList) {
                if (Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().stop();
                }

                final double progressAll = round((100.0 / this.fileCount) * this.alreadyDownloaded + (100.0 / this.fileCount) * fileList.indexOf(fileName));
                Platform.runLater(() -> {
                    this.infoLabelAll.setText(progressAll + "/100%");
                    this.progressBarAll.setProgress(progressAll / 100);
                });

                String[] parts = fileName.split(Pattern.quote(folderSymbol));

                if (parts.length > 1) {
                    final StringBuilder location = new StringBuilder();
                    for (int i = 0; i < (parts.length - 1); i++) {
                        location.append(folderSymbol).append(parts[i]);
                    }
                    if (!new File(gameLocation + location).exists())
                        new File(gameLocation + location).mkdirs();
                }

                try {
                    final HttpURLConnection http = (HttpURLConnection) new URL("http://dvnm.tech/osh/" + fileName.replace(folderSymbol, "/")).openConnection();
                    http.setRequestProperty("User-Agent", "Mozilla/5.0 (" + System.getProperty("os.name") + ") RisingHub/" + read("Application-Version") + " DOWNLOADER");
                    final double fileSize = (double) http.getContentLengthLong();
                    double downloaded = 0.00, percentageDownloaded;
                    final BufferedInputStream in = new BufferedInputStream(http.getInputStream());
                    final FileOutputStream fos = new FileOutputStream(gameLocation + fileName);
                    final BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
                    final byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer, 0, 1024)) >= 0) {
                        bout.write(buffer, 0, read);
                        downloaded += read;
                        percentageDownloaded = (downloaded * 100) / fileSize;
                        final double progressSingle = round(percentageDownloaded);
                        Platform.runLater(() -> {
                            this.infoLabelSingle.setText("Downloading: " + fileName + " - " + progressSingle + "/100%");
                            this.progressBarSingle.setProgress(progressSingle / 100);
                        });
                    }
                    bout.close();
                    in.close();
                } catch (final SocketException | UnknownHostException e) {
                    showAlert("Connection failed", "Connection error.\nPlease check your internet connection.", Alert.AlertType.ERROR);
                    Platform.exit();
                }
            }

        } catch (final Exception ignore) {
        }

        Platform.runLater(() -> {
            this.infoLabelSingle.setVisible(false);
            this.progressBarSingle.setVisible(false);
            this.playButton.setDisable(false);
            this.infoLabelAll.setText("Download complete. Game is ready!");
            this.progressBarAll.setProgress(0);
        });
    }
}
