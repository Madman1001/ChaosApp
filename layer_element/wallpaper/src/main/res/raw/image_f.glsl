#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D uTextureUnit;
varying vec2 vTexCoord;

void main() {
   gl_FragColor = texture2D(uTextureUnit, vTexCoord);
}