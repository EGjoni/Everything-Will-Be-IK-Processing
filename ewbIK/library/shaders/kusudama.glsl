#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

varying vec4 vertColor;

//Model space normal direction of the current fragment
//since we're on a sphere, this is literally just the fragment's position in 
//modelspace
varying vec3 vertNormal;

//This shader can display up to 30 cones (represented by 30 4d vectors) 
// alphachannel represents radius, rgb channels represent xyz coordinates of 
// the cone direction vector in model space
uniform vec4 coneSequence[30];
uniform int coneCount; 
 
//Make this "true" for sceendoor transparency (randomly discarding fragments)
//so that you can blur the result in another pass. Otherwise make it  
//false for a solid shell.  
uniform bool multiPass;

//Following three varyings are 
//Only used for fake lighting. 
//Not conceptually relevant
varying vec3 vertWorldNormal;
varying vec3 vertLightDir;
varying vec4 vertWorldPos;


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
///END OF NOISE FUNCTIONS FOR FANCY TRANSPARENCY RENDERING.

bool isInInterConePath(in vec3 normalDir, in vec4 tangent1, in vec4 cone1, in vec4 tangent2, in vec4 cone2) {			
	vec3 c1xc2 = cross(cone1.xyz, cone2.xyz);		
	float c1c2dir = dot(normalDir, c1xc2);
		
	if(c1c2dir < 0.0) { 
		vec3 c1xt1 = cross(cone1.xyz, tangent1.xyz); 
		vec3 t1xc2 = cross(tangent1.xyz, cone2.xyz);	
		float c1t1dir = dot(normalDir, c1xt1);
		float t1c2dir = dot(normalDir, t1xc2);
		
	 	return (c1t1dir > 0.0 && t1c2dir > 0.0); 
			
	}else {
		vec3 t2xc1 = cross(tangent2.xyz, cone1.xyz);	
		vec3 c2xt2 = cross(cone2.xyz, tangent2.xyz);	
		float t2c1dir = dot(normalDir, t2xc1);
		float c2t2dir = dot(normalDir, c2xt2);
		
		return (c2t2dir > 0.0 && t2c1dir > 0.0);
	}	
	return false;
}

//determines the current draw condition based on the desired draw condition in the setToArgument
// -3 = disallowed entirely; 
// -2 = disallowed and on tangentCone boundary
// -1 = disallowed and on controlCone boundary
// 0 =  allowed and empty; 
// 1 =  allowed and on controlCone boundary
// 2  = allowed and on tangentCone boundary
int getAllowabilityCondition(in int currentCondition, in int setTo) {
	if((currentCondition == -1 || currentCondition == -2)
		&& setTo >= 0) {
		return currentCondition *= -1;
	} else if(currentCondition == 0 && (setTo == -1 || setTo == -2)) {
		return setTo *=-2;
	}  	
	return max(currentCondition, setTo);
}



//returns 1 if normalDir is beyond (cone.a) radians from cone.rgb
//returns 0 if normalDir is within (cone.a + boundaryWidth) radians from cone.rgb 
//return -1 if normalDir is less than (cone.a) radians from cone.rgb
int isInCone(in vec3 normalDir, in vec4 cone, in float boundaryWidth) {
	float arcDistToCone = acos(dot(normalDir, cone.rgb));
	if(arcDistToCone > (cone.a+(boundaryWidth/2.))) {
		return 1; 
	}
	if(arcDistToCone < cone.a-(boundaryWidth/2.)) {
		return -1;
	}
	return 0;
} 

//returns a color corresponding to the allowability of this region, or otherwise the boundaries corresponding 
//to various cones and tangentCone 
vec4 colorAllowed(in vec3 normalDir,  in int coneCount, in float boundaryWidth) {
	normalDir = normalize(normalDir);
	int currentCondition = -3;
	
	if(coneCount == 1) {
		vec4 cone = coneSequence[0];
		int inCone = isInCone(normalDir, cone, boundaryWidth);
		inCone = inCone == 0 ? -1 : inCone < 0 ? 0 : -3;
		currentCondition = getAllowabilityCondition(currentCondition, inCone);
	} else {
		for(int i=0; i<coneCount-1; i+=3) {
			
			int idx = i*3; 
			vec4 cone1 = coneSequence[idx];
			vec4 tangent1 = coneSequence[idx+1];			
			vec4 tangent2 = coneSequence[idx+2];			
			vec4 cone2 = coneSequence[idx+3];
										
			int inCone1 = isInCone(normalDir, cone1, boundaryWidth);
			
			inCone1 = inCone1 == 0 ? -1 : inCone1 < 0 ? 0 : -3;
			currentCondition = getAllowabilityCondition(currentCondition, inCone1);
				
			int inCone2 = isInCone(normalDir, cone2, boundaryWidth);
			inCone2 =  inCone2 == 0 ? -1 : inCone2  < 0 ? 0 : -3;
			currentCondition = getAllowabilityCondition(currentCondition, inCone2);
		
			int inTan1 = isInCone(normalDir, tangent1, boundaryWidth); 
			int inTan2 = isInCone(normalDir, tangent2, boundaryWidth);
			
			if( inTan1 < 1. || inTan2  < 1.) {			
				inTan1 =  inTan1 == 0 ? -2 : -3;
				currentCondition = getAllowabilityCondition(currentCondition, inTan1);
				inTan2 =  inTan2 == 0 ? -2 : -3;
				currentCondition = getAllowabilityCondition(currentCondition, inTan2);
			} else {				 
				bool inIntercone = isInInterConePath(normalDir, tangent1, cone1, tangent2, cone2);
				int interconeCondition = inIntercone ? 0 : -3; 
				currentCondition = getAllowabilityCondition(currentCondition, interconeCondition);					
			}
		}
	}	
	
	vec4 result = vertColor;
	
	if(multiPass && (currentCondition == -3 || currentCondition > 0)) {
		
		/////////
		//CODE FOR FANCY BLURRED TRANSPARENCY. 
		//NOT OTHERWISE CONCEPTUALLY RELEVANT TO 
		//TO VISUALIZATION
		////////
		
		vec3 randDir = vec3(normalDir.x  * noise(normalDir.xy)/50.0,  normalDir.y  * noise(normalDir.yz)/50.0, normalDir.z  * noise(normalDir.zx)/50.0);
		randDir = normalDir;
		float zAdd = abs(vertWorldPos.z);
		float lon = atan(randDir.x/randDir.z) + 3.14159265/2.0;
		float lat = atan(randDir.y/randDir.x) + 3.14159265/2.0;
				
		bool latDraw = randBit(vec2(lat, lon));//mod(lat, 0.005) < 0.00499;
		bool lonDraw = randBit(vec2(lon, lat));//mod(lon, 0.005) < 0.00499;
			
		if(randBit(vec2(lon, lat))) {		
			result = vec4(0.0,0.0,0.0,0.0);	
		}
		////////
		//END CODE FOR FANCY BLURRED TRANSPARENCY
		///////
	} else if (currentCondition != 0) {
	
		float onTanBoundary = abs(currentCondition) == 2 ? 0.3 : 0.0; 
		float onConeBoundary = abs(currentCondition) == 1 ? 0.3 : 0.0;	
	
		//return distCol;
		result += vec4(0.0, onConeBoundary, onTanBoundary, 1.0);
	} else {
		discard;
	}
	return result;
			
}

void main() {

  vec3 normalDir = normalize(vertNormal); // the vertex normal in Model Space.
  float lightScalar = dot(vertLightDir, vec3(0.5,-1.,0.5)); 
  lightScalar *= lightScalar*lightScalar;
  vec4 colorAllowed = colorAllowed(normalDir, coneCount, 0.02);  

  if(colorAllowed.a == 0.0)
  	discard;
  	
  colorAllowed += (colorAllowed + fwidth(colorAllowed)); 
  colorAllowed /= 2.0;
  vec3 lightCol = vec3(1.0,0.8,0.0);
  float gain = vertWorldNormal.z < 0 ? -0.3 : 0.5;
 colorAllowed.rgb = (colorAllowed.rgb + lightCol*(lightScalar + gain)) / 2.;
 vec4 specCol = vec4(1.0, 1.0, 0.6, colorAllowed.a);  
 colorAllowed = colorAllowed.g > 0.8 ? colorAllowed+specCol : colorAllowed;  	
  	
  gl_FragColor = colorAllowed;
}