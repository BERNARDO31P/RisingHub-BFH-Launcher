package fm.bernardo.risinghub.classes;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.json.JSONObject;
import org.json.XML;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;

import static com.jcabi.manifests.Manifests.read;
import static fm.bernardo.risinghub.Main.operatingSystem;
import static fm.bernardo.risinghub.Main.folderSymbol;
import static fm.bernardo.risinghub.Main.gameLocation;
import static fm.bernardo.risinghub.Main.api;
import static fm.bernardo.risinghub.classes.ComputerIdentifier.generateLicenseKey;
import static fm.bernardo.risinghub.classes.ExecuteBashCommand.executeCommand;
import static fm.bernardo.risinghub.classes.HTTPRequest.send;
import static fm.bernardo.risinghub.classes.ShowAlert.showAlert;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public final class Controller {
    public TextField tbx_username;
    public PasswordField tbx_password;
    public CheckMenuItem startupCheck;
    public Button playButton;
    public Label infoLabelAll, infoLabelSingle;
    public ProgressBar progressBarAll, progressBarSingle;
    private Downloader downloader;


    public final void initialize() throws Exception {
        if (operatingSystem.contains("Windows")) {
            if (new File(System.getProperty("user.home") + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\" + new File(Controller.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName()).exists())
                startupCheck.setSelected(true);
        }

        String response = send("http://dvnm.tech/osh/project.xml", "");

        final JSONObject xmlData = XML.toJSONObject(Objects.requireNonNull(response)).getJSONObject("sharpUpdate").getJSONObject("update"), fileList = xmlData.getJSONObject("filesUpdates");
        final ArrayList<String> toDownload = new ArrayList<>();

        int alreadyDownloaded = 0;
        final int fileCount = (Integer) xmlData.get("filesNumber") + 1;
        for (int i = 1; i < fileCount; i++) {
            String fileName = (String) fileList.get("file" + i);
            fileName = fileName.replace("\\", folderSymbol);

            try (final InputStream is = Files.newInputStream(Paths.get(gameLocation + folderSymbol + fileName))) {
                final String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(is);
                if (!md5.toUpperCase().equals(fileList.get("hash" + i))) {
                    throw new NoSuchFileException(null);
                } else {
                    alreadyDownloaded++;
                }
            } catch (final NoSuchFileException e) {
                toDownload.add(fileName);
            }
        }

        this.downloader = new Downloader(toDownload, this.infoLabelAll, this.playButton, this.progressBarAll, this.infoLabelSingle, this.progressBarSingle, alreadyDownloaded, fileCount);
        this.downloader.start();

    }

    public final void enterForm() {
        playButton.fire();
    }

    private String getGameToken (final String loginToken) throws Exception {
        return new JSONObject(Objects.requireNonNull(send(api + "api/user?token=", loginToken))).get("game_token").toString();
    }

    private void startGame(final String gameToken) {
        executeCommand("WINEPREFIX=~/Battlefield\\ Heroes wine \"C:\\users\\" + System.getProperty("user.name") + "\\Battlefield Heroes\\BFHeroes.exe\" +sessionId " + gameToken + " +magma 1 +magmaProtocol https +punkbuster 0 +developer 1 +lang en");
    }

    public final void loginAndPlay() throws Exception {
        final String username = this.tbx_username.getText(), password = this.tbx_password.getText();

        if (!(username.equals("") || password.equals(""))) {
            final JSONObject response = new JSONObject(Objects.requireNonNull(send(api + "api/token?", "obt=1&username=" + username + "&password=" + password + "&hwid=" + generateLicenseKey())));

            if ("403".equals(response.get("response").toString())) {
                showAlert("Error on Login", response.get("error").toString(), Alert.AlertType.ERROR);
            } else {
                startGame(this.getGameToken(response.get("token").toString()));
                showAlert("Information", "You've been successfully logged in.\nStarting the game.", Alert.AlertType.INFORMATION);
            }
        } else {
            showAlert("Error on Login", "Please fill out all fields.", Alert.AlertType.ERROR);
        }
    }

    public void shutdown() {
        this.downloader.interrupt();
        Platform.exit();
    }

    public final void startup(final ActionEvent event) throws Exception {
        final CheckMenuItem object = (CheckMenuItem) event.getSource();

        if (object.isSelected()) {
            if (operatingSystem.contains("Windows")) {
                try {
                    Files.copy(Paths.get(new File(Controller.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath()).toAbsolutePath().normalize(), Paths.get(System.getProperty("user.home") + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\" + new File(Controller.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName()), REPLACE_EXISTING);
                    showAlert("Success", "The launcher will now run on every startup.", Alert.AlertType.INFORMATION);
                } catch (AccessDeniedException e) {
                    showAlert("No permission", "This application will start with administration permission.", Alert.AlertType.WARNING);
                    Runtime.getRuntime().exec("powershell.exe Start-Process -FilePath javaw.exe -Argument '-jar " + new File(Controller.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath() + "' -verb RunAs");
                    Platform.exit();
                }
            } else {
                showAlert("Not implemented", "We haven't implemented this function yet for your operating system. (" + operatingSystem + ")", Alert.AlertType.ERROR);
                startupCheck.setSelected(false);
            }
        } else {
            final String tempFolder = System.getProperty("user.home") + "\\AppData\\Local\\Temp\\risingHub\\";
            if (!new File(tempFolder).exists())
                new File(tempFolder).mkdirs();

            Files.copy(Paths.get(new File(Controller.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath()).toAbsolutePath().normalize(), Paths.get(tempFolder + new File(Controller.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName()), REPLACE_EXISTING);
            Runtime.getRuntime().exec("powershell.exe Start-Process -FilePath javaw.exe -Argument '-jar " + tempFolder + new File(Controller.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName() + " removeStartup'");
            Platform.exit();
        }

    }

    public final void showInformation() {
        showAlert("Copyright and information", "Launcher version: " + read("Application-Version") + " // " + read("Last-Change") + "\n© 2018 BERNARDO.FM - All rights reserved.", Alert.AlertType.INFORMATION);
    }

    public final void openWebsite() throws Exception {
        Desktop.getDesktop().browse(new URI("https://risinghub.net/"));
    }

    public final void openRegister() throws Exception {
        Desktop.getDesktop().browse(new URI("https://risinghub.net/register"));
    }
}
