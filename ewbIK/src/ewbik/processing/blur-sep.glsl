// Adapted from:
// http://callumhay.blogspot.com/2010/09/gaussian-blur-shader-glsl.html
// Original shader available as part of the processing library under the "SepBlur" demo.
// Modifications made by Eron Gjoni for proper handling of gamma (see: https://www.youtube.com/watch?v=LKnqECcg6Gw)
// and (rudimentary) stencil masking suppor. 


#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

#define PROCESSING_TEXTURE_SHADER

uniform sampler2D texture;
uniform sampler2D mask;

// The inverse of the texture dimensions along X and Y
uniform vec2 texOffset;

varying vec4 vertColor;
varying vec4 vertTexCoord;

uniform int blurSize;       
uniform int horizontalPass; // 0 or 1 to indicate vertical or horizontal pass
uniform float sigma;        // The sigma value for the gaussian function: higher value means more blur
                            // A good value for 9x9 is around 3 to 5
                            // A good value for 7x7 is around 2.5 to 4
                            // A good value for 5x5 is around 2 to 3.5
                            // ... play around with this based on what you need :)

const float pi = 3.14159265;

bool isZero(in vec3 v) {
	return v.x+v.y+v.z == 0.0; 
}

bool isZero(in vec4 v) {
	return v.w+v.x+v.y+v.z == 0.0; 
}

bool isWorthy(vec4 center, vec4 contender) {
	return center == contender;
}

void main() {  

    float numBlurPixelsPerSide = float(blurSize / 2); 
    vec2 blurMultiplyVec = 0 < horizontalPass ? vec2(1.0, 0.0) : vec2(0.0, 1.0);

    // Incremental Gaussian Coefficent Calculation (See GPU Gems 3 pp. 877 - 889)
    vec3 incrementalGaussian;
    incrementalGaussian.x = 1.0 / (sqrt(2.0 * pi) * sigma);
    incrementalGaussian.y = exp(-0.5 / (sigma * sigma));
    incrementalGaussian.z = incrementalGaussian.y * incrementalGaussian.y;

    vec4 avgValue = vec4(0.0, 0.0, 0.0, 0.0);
    float coefficientSum = 0.0;

    // Take the central sample first...
    avgValue = texture2D(texture, vertTexCoord.st);
    vec4 baseMask = texture2D(mask, vertTexCoord.st);
    if(!isZero(baseMask.rgb)) {
        avgValue *= avgValue;
        avgValue = avgValue * incrementalGaussian.x;
        coefficientSum += incrementalGaussian.x;
        incrementalGaussian.xy *= incrementalGaussian.yz;

        // Go through the remaining 8 vertical samples (4 on each side of the center)
        for (float i = 1.0; i <= numBlurPixelsPerSide; i++) { 
        	vec4 maskL = texture2D(mask, vertTexCoord.st - i * texOffset *  blurMultiplyVec); 
                                    
            if(isWorthy(baseMask, maskL) && !isZero(maskL.rgb)) {
            
            	vec4 left = texture2D(texture, vertTexCoord.st - i * texOffset * 
                                    blurMultiplyVec, 0);
                left *= left;
                left = left * incrementalGaussian.x;
                avgValue += left;  
                coefficientSum += incrementalGaussian.x;
            } else {
            	//discard;
            }            
            
            vec4 maskR = texture2D(mask, vertTexCoord.st + i * texOffset * 
                                    blurMultiplyVec);
            
            if(isWorthy(baseMask, maskR) && !isZero(maskR.rgb)) {
            
            	vec4 right = texture2D(texture, vertTexCoord.st + i * texOffset * 
                                    blurMultiplyVec);
                right *= right;
                right = right * incrementalGaussian.x; 
                avgValue +=  right;
                coefficientSum += incrementalGaussian.x;
            } else {
            	//discard;
            }
            
            
            incrementalGaussian.xy *= incrementalGaussian.yz;
        }
    
        avgValue /= coefficientSum;
        avgValue = sqrt(avgValue); 
    }

  gl_FragColor = avgValue;
}