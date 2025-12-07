import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class PPMGeneratorExample {

    static void main() {
        for (int i = 0; i < 60; i++) {
            generateImage("output_%02d.ppm".formatted(i), 16 * 60, 9 * 60, i);
        }

    }

    public static void generateImage(String filename, int width, int height, int frame) {
        Path filepath = Paths.get("output/" + filename);
        System.out.println(filepath.toAbsolutePath());

        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(
                filepath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        ))) {
            os.write("P6\n%d %d\n255\n".formatted(width, height).getBytes(StandardCharsets.US_ASCII));
            drawCheckers(os, width, height, frame);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void drawCheckers(OutputStream os, int width, int height, int frame) throws IOException {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (((x + frame) / 60 + (y + frame) / 60) % 2 == 1) {
                    os.write(0xFF);
                    os.write(0x00);
                    os.write(0x00);
                } else {
                    os.write(0x00);
                    os.write(0x00);
                    os.write(0x00);
                }
            }
        }
    }
}
