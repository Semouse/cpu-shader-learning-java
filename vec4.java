public class vec4 {
    float x;
    float y;
    float z;
    float w;

    public vec4(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public vec4 Add(vec4 other) {
        return new vec4(this.x + other.x, this.y + other.y, this.z + other.z, this.w + other.w);
    }


    public void AddSelf(vec4 other) {
        this.x += other.x;
        this.y += other.y;
        this.z += other.z;
        this.w += other.w;
    }

    public vec4 AddScalar(float scalar) {
        return new vec4(this.x + scalar, this.y + scalar, this.z + scalar, this.w + scalar);
    }

    public vec4 ScaleScalar(float scalar) {
        return new vec4(this.x * scalar, this.y * scalar, this.z * scalar, this.w * scalar);
    }

    public vec4 Div(vec4 other) {
        return new vec4(this.x / other.x, this.y / other.y, this.z / other.z, this.w / other.w);
    }
}
