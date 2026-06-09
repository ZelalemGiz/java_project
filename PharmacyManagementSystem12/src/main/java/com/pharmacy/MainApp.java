package com.pharmacy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainApp extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/pharmacy/view/welcome.fxml"));
        Scene scene = new Scene(root);
        
        // Get screen dimensions
        Screen screen = Screen.getPrimary();
        double screenWidth = screen.getVisualBounds().getWidth();
        double screenHeight = screen.getVisualBounds().getHeight();
        
        // Set min and max window size
        primaryStage.setMinWidth(600);      
        primaryStage.setMinHeight(400);    
        primaryStage.setMaxWidth(screenWidth);  
        primaryStage.setMaxHeight(screenHeight); 
        primaryStage.setWidth(screenWidth);
        primaryStage.setHeight(screenHeight);
        primaryStage.setMaximized(true);
        
        primaryStage.setTitle("Pharmacy Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}