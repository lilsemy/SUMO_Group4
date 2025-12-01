package novik;

// Import Simulation class to control SUMO simulation (start, stop, step)
// Import lớp Simulation để điều khiển mô phỏng SUMO (bắt đầu, dừng, bước)
import org.eclipse.sumo.libtraci.Simulation;
// Import StringVector for passing arguments to SUMO
// Import StringVector để truyền tham số cho SUMO
import org.eclipse.sumo.libtraci.StringVector;

// Import Path for file path handling
// Import Path để xử lý đường dẫn file
import java.nio.file.Path;

public class SumoBridge {
    // Server status: true if SUMO is running
    // Trạng thái server: true nếu SUMO đang chạy
    public static boolean isServerActive=false;

    // Used to connect to SUMO server and start simulation
    // Dùng để kết nối tới server SUMO và khởi động mô phỏng
    static void startSUMO(Path sumoCfgPath){
        // IMPORTANT: run JVM with -Djava.library.path=<SUMO>/bin
        // QUAN TRỌNG: chạy JVM với -Djava.library.path=<SUMO>/bin
        Simulation.preloadLibraries();
        StringVector args = new StringVector(new String[] {
                // use "sumo-gui" if you want the GUI as well
                // dùng "sumo-gui" nếu muốn có giao diện đồ họa
                "sumo",
                "-c", sumoCfgPath.toAbsolutePath().toString(),
                "--start",
                "--no-step-log", "true",
                "--time-to-teleport", "-1"
        });
        Simulation.start(args);
        isServerActive = true;
    }

    // Used to disconnect from SUMO server and stop simulation
    // Dùng để ngắt kết nối với server SUMO và dừng mô phỏng
    static void stopSUMO(){
        Simulation.close();
        isServerActive = false;
    }

    // Used to advance the SUMO simulation by one step (update the next states)
    // Dùng để tiến mô phỏng SUMO thêm một bước (cập nhật trạng thái tiếp theo)
    static void step(){
        Simulation.step();
    }
}
