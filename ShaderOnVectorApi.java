import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorOperators;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


public class ShaderOnVectorApi {
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
            System.gc();
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

        double avgTimeMs = (totalTime / (double) frames) / 1_000_000.0;
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
        Path filepath = Paths.get("output/vector/shader/" + filename);
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


    static void shader(int[] pixels, int width, int height, int frame) {
        var r = FloatVector.fromArray(FloatVector.SPECIES_64, new float[]{width, height}, 0);
        var increment = FloatVector.fromArray(FloatVector.SPECIES_64, new float[]{0.0f, 1.0f}, 0);

        float t = frame / 60.0f;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                var o = FloatVector.fromArray(FloatVector.SPECIES_128, new float[]{0.0f, 0.0f, 0.0f, 0.0f}, 0);
                var FC = FloatVector.fromArray(FloatVector.SPECIES_64, new float[]{x, height - y}, 0);
                var i = FloatVector.fromArray(FloatVector.SPECIES_64, new float[]{0.0f, 0.0f}, 0);
                var p = FC.mul(2.0f).sub(r).mul(1 / r.lane(1));
                float temp = 4.0f - 4.0f * Math.abs(0.7f - p.mul(p).reduceLanes(VectorOperators.ADD));
                var l = FloatVector.fromArray(FloatVector.SPECIES_64, new float[]{temp, temp}, 0);
                var v = p.mul(l.lane(0));

                for(int j = 1; j <= 8; j++){
                    i = FloatVector.fromArray(FloatVector.SPECIES_64, new float[]{0.0f, j}, 0);
                    v = updateV(v, i, t);
                    o = o.add((updateO(v)));
                }

                o = calculateOutput(l, p, o);

                pixels[y * width + x] = (int) (o.lane(0) * 255) << 24 | (int) (o.lane(1) * 255) << 16 | (int) (o.lane(2) * 255) << 8;
            }
        }

    }

    static FloatVector updateO(FloatVector v) {
        FloatVector xyyxResult = xyyx(v);
        FloatVector sinResult = xyyxResult.lanewise(VectorOperators.SIN);
        FloatVector sinPlusOne = sinResult.add(1.0f);
        float diff = Math.abs(v.lane(0) - v.lane(1));
        FloatVector finalResult = sinPlusOne.mul(diff);
        FloatVector o = FloatVector.fromArray(FloatVector.SPECIES_128,
                new float[]{0.0f, 0.0f, 0.0f, 0.0f}, 0);

        return o.add(finalResult);
    }

    static FloatVector updateV(FloatVector v, FloatVector i, float t){
        FloatVector v_yx = yx(v);
        FloatVector expr = v_yx.mul(i.lane(1)).add(i).add(t);

        FloatVector cosResult = expr.lanewise(VectorOperators.COS);
        float iy = i.lane(1);
        FloatVector term = cosResult.div(iy).add(0.7f);

        return v.add(term);
    }

    static FloatVector calculateOutput(FloatVector l, FloatVector p, FloatVector o) {
        float lx = l.lane(0);
        float py = p.lane(1);
        float base = lx - 4.0f;

        float[] temp = {
                base - (-1.0f * py),
                base - (py),
                base - (2.0f * py),
                base - (0.0f * py)
        };

        FloatVector vec = FloatVector.fromArray(FloatVector.SPECIES_128, temp, 0);
        FloatVector expVec = vec.lanewise(VectorOperators.EXP);
        FloatVector result = expVec.mul(5.0f).div(o);

        return result.lanewise(VectorOperators.TANH);
    }

    static FloatVector xyyx(FloatVector vec) {
        float x = vec.lane(0);
        float y = vec.lane(1);

        float[] pattern = {x, y, y, x};
        return FloatVector.fromArray(FloatVector.SPECIES_128, pattern, 0);
    }

    static FloatVector yx(FloatVector vec) {
        return FloatVector.fromArray(FloatVector.SPECIES_64, new float[]{vec.lane(1), vec.lane(0)}, 0);
    }
}
