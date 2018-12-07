package fm.bernardo.risinghub.classes;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public final class ShowAlert
{

    public static void showAlert (String title, String content, Alert.AlertType type)
    {
        try {
            final Alert alert = new Alert(type);
            alert.setHeaderText(null);
            alert.setTitle(title);
            alert.setContentText(content);
            alert.initStyle(StageStyle.UTILITY);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            alert.showAndWait();
        } catch (IllegalStateException e) {
            Platform.exit();
        }
    }

}
