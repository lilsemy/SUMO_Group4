package com.example.guifx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;


    /**
    *Main is the Initializer of the GUI
    *@see class Application
    */

public class Main extends Application {
    @Override

    /**
    *starts scene
    *@throws Exception
    */

    //Test

    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("SumoController");
        stage.setScene(scene);
        stage.show();

        GUI controller = fxmlLoader.getController();

        Connection conn = new Connection();
        controller.setVehiclesManager(conn.getVehiclesManager());
        // Connection.makeConnection(controller);
    }











}
