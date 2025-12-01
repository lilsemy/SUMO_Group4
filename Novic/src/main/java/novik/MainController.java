package novik;

// Import AnimationTimer for game/simulation loop
// Import AnimationTimer để tạo vòng lặp mô phỏng hoặc game
import javafx.animation.AnimationTimer;
// Import FXML annotation for linking UI components
// Import annotation FXML để liên kết các thành phần giao diện
import javafx.fxml.FXML;
// Import Canvas for drawing graphics
// Import Canvas để vẽ đồ họa
import javafx.scene.canvas.Canvas;
// Import GraphicsContext for drawing on Canvas
// Import GraphicsContext để vẽ lên Canvas
import javafx.scene.canvas.GraphicsContext;
// Import Pane for layer containers
// Import Pane để làm lớp chứa các thành phần giao diện
import javafx.scene.layout.Pane;
// Import VBox for vertical layout (used for info panels)
// Import VBox để bố trí các thành phần theo chiều dọc (panel thông tin)
import javafx.scene.layout.VBox;
// Import Label for displaying text
// Import Label để hiển thị văn bản
import javafx.scene.control.Label;

// Import file and path utilities for file existence checks
// Import các tiện ích file và đường dẫn để kiểm tra file tồn tại
import java.nio.file.Files;
import java.nio.file.Path;

public class MainController {
    // ---------- FXML for general----------
    @FXML private Canvas canvas;

    // ---------- FXML for traffic lights----------
    @FXML private Pane trafficLightLayer;
    @FXML private VBox tlsInfoBox;
    @FXML private Label tlsIdLabel;
    @FXML private Label tlsStateLabel;
    @FXML private Label tlsPhaseLabel;
    @FXML private Label tlsFeedbackLabel;
    @FXML private VBox tlsPhasesContainer;

    // ---------- FXML for lanes----------
    @FXML private Pane laneLayer;
    @FXML private Pane carLayer;


    // ---------- Rendering / transform ----------
    static GraphicsContext g; // Used for all drawing operations
    // Dùng cho tất cả thao tác vẽ
    private final double MARGIN = 30;            // screen margin in px
    // Lề màn hình (pixel)

    // ---------- Simulation state ----------
    private AnimationTimer timer; // Main simulation loop
    // Vòng lặp mô phỏng chính
    private boolean running = false; // Simulation running state
    // Trạng thái chạy mô phỏng
    private CarLayer carLayerInstance;
    private TrafficLightLayer trafficLightLayerInstance;
    private InfoPanel infoPanelInstance;
    private LaneLayer laneLayerInstance;

    // SUMO config location
    // Đường dẫn file cấu hình SUMO
    private final Path sumoDir = Path.of("sumo");
    // Use simple scenario config
    // Đổi sang cấu hình đơn giản
    private final Path sumoCfgPath  = sumoDir.resolve("./simple/simple.sumocfg");
    private final Path sumoNetPath = sumoDir.resolve("./simple/simple.net.xml"); // for traffic lights

    // ---------- Lifecycle from MainApp ----------
    // Called by MainApp.java to start the simulation
    // Được gọi từ MainApp.java để khởi động mô phỏng
    public void startup() {
        // Sanity check: files exist
        // Kiểm tra file cấu hình và mạng có tồn tại không
        if (!Files.exists(sumoCfgPath)) {
            throw new IllegalStateException("SUMO config not found: " + sumoCfgPath.toAbsolutePath());
        }
        if (!Files.exists(sumoNetPath)) {
            throw new IllegalStateException("SUMO net file not found: " + sumoNetPath.toAbsolutePath());
        }

        // Start SUMO simulation process
        // Khởi động tiến trình mô phỏng SUMO
        SumoBridge.startSUMO(sumoCfgPath);

        // Load network geometry and initialize managers
        // Load hình học mạng lưới và khởi tạo các manager
        MapUtil.setup(canvas.getWidth(), canvas.getHeight(), MARGIN);
        g = canvas.getGraphicsContext2D();
        carLayerInstance = new CarLayer(carLayer);
        laneLayerInstance = new LaneLayer(laneLayer);
        trafficLightLayerInstance = new TrafficLightLayer(trafficLightLayer, sumoNetPath);
        infoPanelInstance = new InfoPanel();

        // Start the animation/simulation loop
        // Bắt đầu vòng lặp mô phỏng/animation
        startLoop();
    }

    // Called by MainApp.java to stop the simulation and cleanup
    // Được gọi từ MainApp.java để dừng mô phỏng và giải phóng tài nguyên
    public void shutdown() {
        if (timer != null) timer.stop(); // Stop the animation loop
        // Dừng vòng lặp animation
        if (SumoBridge.isServerActive) SumoBridge.stopSUMO(); // Stop SUMO process if running
        // Dừng tiến trình SUMO nếu còn chạy
    }

    @FXML
    private void pause(){
        // Pause simulation (set running to false)
        // Tạm dừng mô phỏng (gán running = false)
        if (running) {
            running = false;
        }
    }

    @FXML
    private void resume(){
        // Resume simulation (set running to true)
        // Tiếp tục mô phỏng (gán running = true)
        if(!running){
            running = true;
        }
    }

    // Start the main simulation loop using AnimationTimer
    // Khởi động vòng lặp mô phỏng chính bằng AnimationTimer
    private void startLoop() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if(running) {
                    // Step the SUMO simulation forward
                    // Tiến mô phỏng SUMO thêm một bước
                    SumoBridge.step();
                    // Redraw the frame
                    // Vẽ lại khung hình
                    drawFrame();
                    if (MapUtil.boundsReady) {
                        // Update all cars, traffic lights, and info panel
                        // Cập nhật tất cả xe, đèn giao thông, và panel thông tin
                        carLayerInstance.updateCars();
                        trafficLightLayerInstance.updateTrafficLightStates();
                        infoPanelInstance.updatePanel(carLayerInstance, trafficLightLayerInstance, laneLayerInstance);
                    }
                }
            }
        };
        timer.start();
        running = true;
    }

    // Draw the background and (optionally) other static elements
    // Vẽ nền và (nếu cần) các thành phần tĩnh khác
    private void drawFrame() {
        // Background
        g.setFill(javafx.scene.paint.Color.web("#f4f4f4"));
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());


    }
}
