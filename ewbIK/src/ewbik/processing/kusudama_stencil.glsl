#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform float fraction;

varying vec4 vertColor;
varying vec3 vertNormal;
varying vec3 vertWorldNormal;
varying vec4 posN;
varying vec3 vertLightDir;
varying vec4 vertWorldPos; 

uniform vec4 coneSequence[30]; //this shader can display up to 10 cones (represented by 30 4d vectors)  
uniform int coneCount; 
uniform int frame;

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

float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

vec4 drawInterConePathDisparity(in vec3 normalDir, in vec4 tangent1, in vec4 cone1, in vec4 tangent2, in vec4 cone2) {		
	vec3 c1xc2 = cross(cone1.xyz, cone2.xyz);		
	float c1c2dir = dot(normalDir, c1xc2);
	vec4 result = vec4(0.0,0.0,0.0,1.0);	
	
	if(c1c2dir < 0.0) { 
		vec3 c1xt1 = cross(cone1.xyz, tangent1.xyz); 
		vec3 t1xc2 = cross(tangent1.xyz, cone2.xyz);	
		float c1t1dir = dot(normalDir, c1xt1);
		float t1c2dir = dot(normalDir, t1xc2);
		
	 	if(c1t1dir > 0.0 && t1c2dir > 0.0) { 
			result = vec4(0.5, 0.5, 0.0, 1.0);
		}
	}else {
		vec3 t2xc1 = cross(tangent2.xyz, cone1.xyz);	
		vec3 c2xt2 = cross(cone2.xyz, tangent2.xyz);	
		float t2c1dir = dot(normalDir, t2xc1);
		float c2t2dir = dot(normalDir, c2xt2);
		
		if(c2t2dir > 0.0 && t2c1dir > 0.0) { 
			result  = vec4(0.0, 0.5, 0.5, 1.0);
		}
	}	
	return result;
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
	vec4 distCol = vec4(0.0, 0.0, 0.0, 1.0);
	if(coneCount == 1) {
		//ivec2 coneIDX = ivec2(1, 0);
		vec4 cone = coneSequence[0];//texelFetch(coneSequence, coneIDX, 0);
		int inCone = isInCone(normalDir, cone, boundaryWidth);
		inCone = inCone == 0 ? -1 : inCone < 0 ? 0 : -3;
		currentCondition = getAllowabilityCondition(currentCondition, inCone);

	} else {
		for(int i=0; i<coneCount-1; i++) {
			
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
				bool visualize = false;
				if( ! visualize ) {
					bool inIntercone = isInInterConePath(normalDir, tangent1, cone1, tangent2, cone2);
					int interconeCondition = inIntercone ? 0 : -3;// && (inTangent1 > 0 || inTangent2 > 0) ? -3 : 0;
					currentCondition = getAllowabilityCondition(currentCondition, interconeCondition);
				} else { 
					//visualization code, set visualize = true to get a sense of how the math works.	
					vec4 intervec = drawInterConePathDisparity(normalDir, tangent1, cone1, tangent2, cone2);
					if(inCone1 == 0 || inCone2 == 0) intervec.rgb += .3;				
					return intervec;
				}
			}
		}
	}	
	
	vec4 result = vertColor;
	
	if(currentCondition == -3
	|| currentCondition == -2
	|| currentCondition == -1
	//|| currentCondition == 0
	|| currentCondition == 1
	//|| currentCondition == 2
	) {
		return result;
	} else { 
		discard;
	}
	return result;
		
	
}

void main() {

  vec3 normalDir = normalize(vertNormal);
  vec4 colorAllowed = colorAllowed(normalDir, coneCount, 0.02);   	
  if(vertWorldNormal.z < 0.0) {
  	colorAllowed.gb /= 2.0;
  }
  vec4 scvertWorldPos = vertWorldPos; 
  scvertWorldPos.a += sin(frame)*0.0000000000001;
  scvertWorldPos.z += 0.5; 
  scvertWorldPos.z /= -2.0;
  gl_FragColor = colorAllowed;//vec4(scvertWorldPos.z, scvertWorldPos.z, scvertWorldPos.z, 1.0); ;
}
