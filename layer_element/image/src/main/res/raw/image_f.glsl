precision mediump float;
varying vec2 v_TexCoord;
uniform sampler2D u_TextureUnit;
void main() {
    gl_FragColor = texture2D(u_TextureUnit, v_TexCoord) * vec4(v_TexCoord.x, v_TexCoord.y, 1.0, 1.0);
}