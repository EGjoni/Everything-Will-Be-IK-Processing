uniform mat4 transform;
uniform mat4 modelviewMatrix;
uniform mat4 modelMatrix;
uniform mat3 normalMatrix;
uniform vec3 lightNormal;
uniform mat4 modelViewInv; 


attribute vec4 position;
attribute vec4 color;
attribute vec3 normal;

varying vec4 vertColor;
varying vec3 vertNormal;
varying vec3 vertLightDir;
varying vec3 vertWorldNormal;
varying vec4 vertWorldPos; 
varying vec4 posN;

void main() {
  gl_Position = transform * position;
  vec4 f_normal = vec4(normal.x, normal.y, normal.z, 1.0); // vec4(normalize(normalMatrix * normal), 1.0);//
  f_normal = position * modelViewInv;
  //posN =  
  vertColor = color;
  vertNormal = f_normal.xyz;
  vertLightDir = normal;
  vertWorldNormal  = normalMatrix * normal;
  vertWorldPos = transform * position;
}