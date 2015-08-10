precision mediump float;

varying vec3 v_LightPos;
varying vec3 v_Position;
varying vec4 v_Color;
varying vec3 v_Normal;
varying float v_SpecularIntensity;
varying float v_SpecularPower;
varying vec3 v_EyePosition;

varying vec3 v_EyeSpacePosition;

vec3 lightVector;
vec3 normal;
vec4 lightColor = vec4(1, 1, 1, 1);

vec4 getAmbientLighting();
vec4 getDiffuseLighting();
vec4 getSpecularLighting();
void main() {

	// Get a lighting direction vector from the light to the vertex.
	lightVector = normalize(v_LightPos - v_Position);
	normal = normalize(v_Normal);

	gl_FragColor = v_Color * (getSpecularLighting() + getDiffuseLighting() + getAmbientLighting());
}
vec4 getAmbientLighting()
{
	return vec4(lightColor) * 0.3;
}
vec4 getDiffuseLighting()
{
	// Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
    // pointing in the same direction then it will get max illumination.
    float distance = length(v_LightPos - v_Position);
	float diffuse = max(dot(normal, lightVector), 0.0);
	diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));
	return vec4(lightColor) * diffuse;
}
vec4 getSpecularLighting()
{
	//Look for v_EyeSpacePosition for edge??? edge normal at 45 degrees???
    vec3 VertexToEye = normalize(v_EyeSpacePosition - v_EyePosition);
    vec3 LightReflect = normalize(reflect(lightVector, normal));
    float SpecularFactor = max(dot(VertexToEye, LightReflect), 0.0);
    SpecularFactor = max(pow(SpecularFactor, v_SpecularPower), 0.0);
    return vec4(lightColor) * v_SpecularIntensity * SpecularFactor;
}