uniform mat4 u_MVPMatrix;
uniform mat4 u_MVMatrix;
uniform vec4 u_Color;
uniform vec3 u_LightPos;
uniform float u_SpecularIntensity;
uniform float u_SpecularPower;
uniform vec3 u_EyePosition;

attribute vec4 a_Position;
attribute vec3 a_Normal;

varying vec3 v_Position;
varying vec4 v_Color;
varying vec3 v_Normal;
varying vec3 v_LightPos;
varying float v_SpecularIntensity;
varying float v_SpecularPower;
varying vec3 v_EyePosition;

varying vec3 v_EyeSpacePosition;


void main() {
	// Transform the vertex into eye space.
	v_Position = vec3(u_MVMatrix * a_Position);
	v_EyeSpacePosition = vec3(u_MVMatrix * a_Position).xyz;

	// Pass through the uniforms to the fragment shader.
	v_Color = u_Color;
	v_LightPos = u_LightPos;
	v_SpecularIntensity = u_SpecularIntensity;
	v_SpecularPower = u_SpecularPower;
	v_EyePosition = u_EyePosition;

	// Transform the normal's orientation into eye space.
	v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));

	gl_Position = u_MVPMatrix * a_Position;
}