#version 120
attribute vec2 aPos;

void main() {
    gl_Position = vec4(aPos.x, aPos.y, 1., 1.);
}
