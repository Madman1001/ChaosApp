precision mediump float;
uniform sampler2D uTextureUnit;
uniform float randR;
uniform float randG;
uniform float randB;
uniform float randA;

varying vec2 vTexCoord;

void main() {
   vec4 texColor = texture2D(uTextureUnit, vTexCoord);
   gl_FragColor =  vec4(fract(texColor.r + randR), fract(texColor.g + randG), fract(texColor.b + randB), fract(texColor.a + randA));
}