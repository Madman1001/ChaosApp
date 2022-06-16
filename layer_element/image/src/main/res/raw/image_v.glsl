attribute vec2 a_Position;
attribute vec2 a_TexCoord;
varying vec2 v_TexCoord;
void main() {
    v_TexCoord = a_TexCoord;
    gl_Position = vec4 (a_Position.x, a_Position.y, 1.0, 1.0);
}
