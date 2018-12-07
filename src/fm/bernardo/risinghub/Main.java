package fm.bernardo.risinghub;


import fm.bernardo.risinghub.classes.Controller;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.File;
import java.util.Objects;

import static com.jcabi.manifests.Manifests.read;
import static fm.bernardo.risinghub.classes.ExecuteBashCommand.executeCommand;
import static fm.bernardo.risinghub.classes.HTTPRequest.send;
import static fm.bernardo.risinghub.classes.ShowAlert.showAlert;

public final class Main extends Application {

    public static String operatingSystem = System.getProperty("os.name"), gameLocation, folderSymbol, serverName, serverURL, api, gameVersion;

    private void setupEnvironment() {
        if (operatingSystem.contains("Windows")) {
            gameLocation = System.getProperty("user.home") + "\\Battlefield Heroes\\";
            folderSymbol = "\\";
        } else if (operatingSystem.equals("Linux")) {
            if (!new File(System.getProperty("user.home") + "/Battlefield Heroes/").exists()) {
                executeCommand("WINEPREFIX=~/Battlefield\\ Heroes winetricks nocrashdialog win81");
            }
            gameLocation = System.getProperty("user.home") + "/Battlefield Heroes/drive_c/users/" + System.getProperty("user.name") + "/Battlefield Heroes/";
            folderSymbol = "/";
        } else {
            showAlert("Not implemented", "We haven't implemented this function yet for your operating system. (" + operatingSystem + ")", Alert.AlertType.ERROR);
            Platform.exit();
        }
        if (!new File(gameLocation).exists())
            new File(gameLocation).mkdirs();
    }

    @Override
    public final void start(final Stage mainWindow) throws Exception {

        Font.loadFont(Main.class.getResource("fonts/Ubuntu.ttf").toExternalForm(), 10);

        if (getParameters().getRaw().contains("removeStartup")) {
            new File(System.getProperty("user.home") + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\" + new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName()).delete();
            showAlert("Success", "The launcher will no longer run on every startup.", Alert.AlertType.INFORMATION);
        }

        this.setupEnvironment();

        JSONObject response = (JSONObject) new JSONObject(Objects.requireNonNull(send("http://dvnm.tech/servers.php", ""))).getJSONArray("servers").get(0);
        serverName = response.get("name").toString();
        serverURL = response.get("url").toString();
        api = response.get("api").toString();
        gameVersion = response.get("version").toString();

        final FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/Main.fxml"));
        final Parent root = loader.load();
        final Controller controller = loader.getController();
        final Scene scene = new Scene(root);

        mainWindow.setScene(scene);
        mainWindow.setOnHidden(e -> controller.shutdown());
        mainWindow.getIcons().add(new Image(this.getClass().getResourceAsStream("img/logo.png")));
        mainWindow.setResizable(false);
        mainWindow.setTitle(read("Application-Name"));
        mainWindow.show();

    }

    public static void main(final String[] args) {
        new Thread(() -> Platform.runLater(() -> showAlert("Setup and check", "Please wait while your game files are being checked.\nThe launcher will automatically download the missing files.", Alert.AlertType.INFORMATION))).start();
        launch(args);
    }

}
