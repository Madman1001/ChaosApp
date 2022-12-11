attribute vec2 vPosition;
attribute vec2 aTexCoord;
varying vec2 vTexCoord;
void main() {
   vTexCoord = aTexCoord;
   gl_Position = vec4 (vPosition.x, vPosition.y, 1.0, 1.0);
}