package UI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.*;

import java.awt.*;
import java.io.File;

public class dFMMUIController {
    @FXML
    private Button buttonChooseDirectory;

    @FXML
    private Label pathLabel;

    public void startFolderDialogue(ActionEvent actionEvent) {
        Stage myStage = (Stage) buttonChooseDirectory.getScene().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(myStage);
        System.out.println(selectedDirectory.getAbsolutePath());
        setPathlable(selectedDirectory);
    }

    public void setPathlable(File myPath){
        if(myPath == null){
            pathLabel.setText("No Directory selected");
        }else{
            pathLabel.setText(myPath.getAbsolutePath());
        }
    }

}
