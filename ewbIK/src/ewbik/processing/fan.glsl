#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

varying vec4 vertColor;
varying vec3 vertNormal;
varying vec3 vertWorldNormal;
varying vec3 vertLightDir;
varying vec4 vertWorldPos;

//Make this "true" for sceendoor transparency (randomly discarding fragments)
//so that you can blur the result in another pass. Otherwise make it
//false for a solid shell.
uniform bool multiPass;
uniform int frame;

///NOISE FUNCTIONS FOR FANCY TRANSPARENCY RENDERING
float hash( uint n ) { // from https://www.shadertoy.com/view/llGSzw  Base: Hugo Elias. ToFloat: http://iquilezles.org/www/articles/sfrand/sfrand.htm
	n = (n << 13U) ^ n;
    n = n * (n * n * 15731U + 789221U) + 1376312589U;
    return uintBitsToFloat( (n>>9U) | 0x3f800000U ) - 1.;
}

float noise(vec2 U) {
    return hash(uint(U.x+5000.0*U.y));
}

bool randBit(vec2 U) {
	float dist2 = 1.0;
	return 0.5 < (noise(U) * 4. -(noise(U+vec2(dist2,0.))+noise(U+vec2(0.,dist2))+noise(U-vec2(0.,dist2))+noise(U-vec2(dist2,0.))) + 0.5);
}

void main() {

  if(multiPass) {
	  //vec4 screenWorldCoord = ((vertWorldPos * gl_FragCoord.x) + (vertWorldPos * gl_FragCoord.y)) /2.;
	  bool nix = randBit(gl_FragCoord.xy*(sin(frame)+4.));//randBit(vec2(screenWorldCoord.x+screenWorldCoord.z, screenWorldCoord.y+screenWorldCoord.z));
	  if(nix == true)
		  discard;
  }
  gl_FragColor = vertColor;
}
