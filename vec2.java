public class vec2 {
    float x;
    float y;

    public vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public vec2 Add(vec2 other) {
        return new vec2(this.x + other.x, this.y + other.y);
    }

    public void AddSelf(vec2 other) {
        this.x += other.x;
        this.y += other.y;
    }

    public vec2 AddScalar(float scalar) {
        return new vec2(this.x + scalar, this.y + scalar);
    }

    public vec2 ScaleScalar(float scalar) {
        return new vec2(x * scalar, y * scalar);
    }

    public vec2 Sub(vec2 other) {
        return new vec2(this.x - other.x, this.y - other.y);
    }

    public float Dot(vec2 other) {
        return this.x * other.x + this.y * other.y;
    }

    public vec2 DivScalar(float scalar) {
        return new vec2(this.x / scalar, this.y / scalar);
    }

    public vec4 xyyx() {
        return new vec4(this.x, this.y, this.y, this.x);
    }

    public vec2 yx() {
        return new vec2(this.y, this.x);
    }
}
