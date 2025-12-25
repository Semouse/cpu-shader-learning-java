import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ShaderExample {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    static void main() {
        int[] pixels = new int[WIDTH * HEIGHT];

        int frames = 10;
        long totalTime = 0;
        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;

        System.out.println("\nBenchmarking " + frames + " frames:");

        for (int i = 0; i < frames; i++) {
            System.gc(); // Clean memory before each frame

            long startTime = System.nanoTime();
            shader(pixels, WIDTH, HEIGHT, i);
            long endTime = System.nanoTime();

            long frameTime = endTime - startTime;
            totalTime += frameTime;
            minTime = Math.min(minTime, frameTime);
            maxTime = Math.max(maxTime, frameTime);

            double ms = frameTime / 1_000_000.0;
            System.out.printf("Frame %2d: %6.2f ms\n", i, ms);
        }

        double avgTimeMs = (totalTime / (double)frames) / 1_000_000.0;
        double minTimeMs = minTime / 1_000_000.0;
        double maxTimeMs = maxTime / 1_000_000.0;
        double fps = 1000.0 / avgTimeMs;

        System.out.println("\n=== Results ===");
        System.out.printf("Average: %8.2f ms\n", avgTimeMs);
        System.out.printf("Min:     %8.2f ms\n", minTimeMs);
        System.out.printf("Max:     %8.2f ms\n", maxTimeMs);
        System.out.printf("FPS:     %8.2f\n", fps);
        System.out.printf("Total:   %8.2f ms for %d frames\n", totalTime / 1_000_000.0, frames);

        generateImage("image.ppm", WIDTH, HEIGHT, pixels);

    }

    public static void generateImage(String filename, int width, int height, int[] pixels) {
        Path filepath = Paths.get("output/shader/" + filename);
        try {
            Path parentDir = filepath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(
                filepath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        ))) {
            String header = String.format("P6\n%d %d\n255\n", width, height);
            os.write(header.getBytes(StandardCharsets.US_ASCII));

            byte[] buffer = new byte[3];
            for (int i = 0; i < width * height; i++) {
                int pixel = pixels[i];
                buffer[0] = (byte) ((pixel >> 24) & 0xFF);
                buffer[1] = (byte) ((pixel >> 16) & 0xFF);
                buffer[2] = (byte) ((pixel >> 8) & 0xFF);

                os.write(buffer);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static int[] shader(int[] pixels, int width, int height, int frame) {
        var r = new vec2(width, height);

        float t = frame / 60.0f;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                vec4 o = new vec4(0, 0, 0, 0);
                var FC = new vec2(x, height - y);
                var i = new vec2(0, 0);
                var p = FC.ScaleScalar(2).Sub(r).ScaleScalar(1 / r.y);
                var l = new vec2(4 - 4 * Math.abs(0.7f - p.Dot(p)), 4 - 4 * Math.abs(0.7f - p.Dot(p)));
                var v = p.ScaleScalar(l.x);

                while (i.y < 8) {
                    i.y++;
                    o.AddSelf(sin4(v.xyyx()).AddScalar(1).ScaleScalar(Math.abs(v.x - v.y)));
                    v.AddSelf(cos2(v.yx().ScaleScalar(i.y).Add(i).AddScalar(t)).DivScalar(i.y).AddScalar(0.7f));
                }

                o = tanh4(exp4(subVectorFromScalar(l.x - 4, new vec4(-1, 1, 2, 0).ScaleScalar(p.y)))
                        .ScaleScalar(5)
                        .Div(o)
                );

                pixels[y * width + x] = (int) (o.x * 255) << 24 | (int) (o.y * 255) << 16 | (int) (o.z * 255) << 8;
            }
        }

        return pixels;
    }

    public static vec4 sin4(vec4 vec) {
        return new vec4((float) Math.sin(vec.x), (float) Math.sin(vec.y), (float) Math.sin(vec.z), (float) Math.sin(vec.w));
    }

    public static vec2 cos2(vec2 vec) {
        return new vec2((float) Math.cos(vec.x), (float) Math.cos(vec.y));
    }

    public static vec4 tanh4(vec4 vec) {
        return (exp4(vec.ScaleScalar(2.0f)).AddScalar(-1)).Div(exp4(vec.ScaleScalar(2.0f)).AddScalar(1));
    }

    public static vec4 exp4(vec4 vec) {
        return new vec4((float) Math.exp(vec.x), (float) Math.exp(vec.y), (float) Math.exp(vec.z), (float) Math.exp(vec.w));
    }

    public static vec4 subVectorFromScalar(float scalar, vec4 vec) {
        return new vec4(scalar - vec.x, scalar - vec.y, scalar - vec.z, scalar - vec.w);
    }
}
