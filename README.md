# cpu-shader-learning-java

Attempt to write program to render shader.  
Inspired by [Graphics API is irrelevant](https://www.youtube.com/watch?v=xNX9H_ZkfNE&t).  
As shader example [cool-looking shader](https://x.com/XorDev/status/1894123951401378051).  

# Java
openjdk version "25.0.1" 2025-10-21  
OpenJDK Runtime Environment (build 25.0.1)   
OpenJDK 64-Bit Server VM (build 25.0.1, mixed mode, sharing)  

# Run

```console
java ShaderExample.java
```

# Convert to video:

```console
ffmpeg -i ./output/shader/image_%02d.ppm -r 60 ./output.mp4
```
