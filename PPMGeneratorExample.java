import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class PPMGeneratorExample {

    public static final String OUTPUT_FILE_PATH = "./output/output.ppm";

    public static void main() {
        Path filepath = Paths.get(OUTPUT_FILE_PATH);
        int width = 16 * 60;
        int height = 9 * 60;

        try (
            var os = Files.newOutputStream(
                filepath,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
        ) {
            os.write(
                ("P6\n%d %d\n255\n".formatted(width, height)).getBytes(
                    StandardCharsets.US_ASCII
                )
            );

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    os.write(0xFF);
                    os.write(0x00);
                    os.write(0x00);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Generated");
    }
}
