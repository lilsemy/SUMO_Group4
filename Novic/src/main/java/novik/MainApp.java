package novik;

// Import the JavaFX Application base class (for main app lifecycle)
// Import lớp cơ sở Application của JavaFX (quản lý vòng đời ứng dụng)
import javafx.application.Application;
// Import the Stage class, representing the main window
// Import lớp Stage, đại diện cho cửa sổ chính
import javafx.stage.Stage;
// Import the Scene class, representing the content inside a window
// Import lớp Scene, đại diện cho nội dung bên trong cửa sổ
import javafx.scene.Scene;
// Import the FXMLLoader class, used to load FXML files (UI layout)
// Import lớp FXMLLoader, dùng để load file FXML (giao diện)
import javafx.fxml.FXMLLoader;

// Import the new MainController class
import novik.MainController;

public class MainApp extends Application {
    // The controller for the UI, will be obtained from the FXML loader
    // Controller của giao diện, sẽ được lấy từ FXML loader
    private MainController controller;

    // This method is called by JavaFX when the application starts
    // Hàm này được JavaFX gọi khi ứng dụng bắt đầu chạy
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Create an FXMLLoader to load the FXML UI file from the resources folder
        // Tạo một FXMLLoader để load file FXML giao diện từ thư mục resources
        // getResource("/main-view.fxml") will look for main-view.fxml in src/main/resources
        // getResource("/main-view.fxml") sẽ tìm file main-view.fxml trong src/main/resources
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/main-view.fxml"));
        // Load the UI from FXML and create the main Scene
        // Load giao diện từ FXML và tạo Scene (màn hình chính)
        Scene scene = new Scene(loader.load());
        // Get the instance of the Controller declared in the FXML (fx:controller)
        // Lấy ra instance của Controller đã được khai báo trong FXML (fx:controller)
        controller = loader.getController();

        // Set the window title
        // Đặt tiêu đề cửa sổ ứng dụng
        primaryStage.setTitle("Traffic Flow Simulation");
        // Set the created Scene to the main Stage (window)
        // Gán Scene vừa tạo vào Stage (cửa sổ chính)
        primaryStage.setScene(scene);
        // Show the application window
        // Hiển thị cửa sổ ứng dụng
        primaryStage.show();
        
        // Call the startup method to initialize simulation logic, defined by you in Controller.java
        // Gọi hàm khởi động logic mô phỏng, hàm này do bạn định nghĩa trong Controller.java
        controller.startup();
    }

    // This method is called by JavaFX when the application is closing (window is closed)
    // Hàm này được JavaFX gọi khi ứng dụng đóng lại (tắt cửa sổ)
    @Override
    public void stop() {
        // Check if the controller was successfully initialized
        // Kiểm tra controller đã được khởi tạo thành công chưa
        if (controller != null) {
            // Call the shutdown method to stop the simulation and release resources (SUMO, threads, etc.)
            // Gọi hàm shutdown để dừng mô phỏng, giải phóng tài nguyên (SUMO, thread, v.v.)
            // This method is defined by you in Controller.java
            // Hàm này do bạn định nghĩa trong Controller.java
            controller.shutdown();
        }
    }

    // The main method: entry point of the Java application
    // Hàm main: điểm bắt đầu của ứng dụng Java
    public static void main(String[] args) {
        // Call launch to start the JavaFX Application
        // Gọi launch để khởi động JavaFX Application
        launch(args);
    }
}
