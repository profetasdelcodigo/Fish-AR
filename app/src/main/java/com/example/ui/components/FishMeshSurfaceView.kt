package com.example.ui.components

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.model.FishModelCatalog
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.sqrt
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/** Lightweight native OpenGL ES fish mesh renderer. No external 3D SDK is required. */
class FishMeshSurfaceView(context: Context) : GLSurfaceView(context) {
  private val renderer = FishMeshRenderer(context)
  init {
    setEGLContextClientVersion(2)
    setRenderer(renderer)
    renderMode = RENDERMODE_CONTINUOUSLY
    setZOrderOnTop(false)
  }
  fun setSpecies(speciesId: String) { renderer.setSpecies(speciesId) }
  fun setMotion(swim: Float, yaw: Float, pitch: Float, scale: Float) { renderer.setMotion(swim, yaw, pitch, scale) }
}

private class FishMeshRenderer(private val context: Context) : GLSurfaceView.Renderer {
  private var speciesId = "bonito"
  private var vertices = FloatArray(0)
  private var vertexBuffer: FloatBuffer? = null
  private var program = 0
  private var count = 0
  private var swim = 0f
  private var yaw = 0f
  private var pitch = 0f
  private var scale = 1f
  private val projection = FloatArray(16)
  private val view = FloatArray(16)
  private val model = FloatArray(16)
  private val mvp = FloatArray(16)

  fun setSpecies(id: String) {
    if (speciesId != id) { speciesId = id; loadMesh() }
  }
  fun setMotion(swim: Float, yaw: Float, pitch: Float, scale: Float) { this.swim = swim; this.yaw = yaw; this.pitch = pitch; this.scale = scale }

  override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
    GLES20.glClearColor(0f, 0f, 0f, 0f)
    val vs = """
      attribute vec3 aPosition;
      uniform mat4 uMvp;
      varying float vDepth;
      void main(){ gl_Position=uMvp*vec4(aPosition,1.0); vDepth=clamp((aPosition.y+0.5)*0.7,0.0,1.0); }
    """.trimIndent()
    val fs = """
      precision mediump float;
      varying float vDepth;
      void main(){
        vec3 deep=vec3(0.035,0.12,0.17);
        vec3 light=vec3(0.35,0.62,0.72);
        vec3 c=mix(deep,light,vDepth);
        gl_FragColor=vec4(c,0.96);
      }
    """.trimIndent()
    program = linkProgram(vs, fs)
    loadMesh()
    Matrix.setLookAtM(view, 0, 0f, 0.15f, 3.2f, 0f, 0f, 0f, 0f, 1f, 0f)
  }

  override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
    GLES20.glViewport(0, 0, width, height)
    Matrix.perspectiveM(projection, 0, 42f, width.toFloat() / height.coerceAtLeast(1), 0.1f, 100f)
  }

  override fun onDrawFrame(gl: GL10?) {
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
    if (vertexBuffer == null || count == 0) return
    GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    Matrix.setIdentityM(model, 0)
    Matrix.translateM(model, 0, 0f, 0f, 0f)
    Matrix.rotateM(model, 0, pitch, 1f, 0f, 0f)
    Matrix.rotateM(model, 0, yaw + (kotlin.math.sin(swim * 6.28318f) * 4f), 0f, 1f, 0f)
    Matrix.scaleM(model, 0, scale, scale, scale)
    Matrix.multiplyMM(mvp, 0, view, 0, model, 0)
    Matrix.multiplyMM(mvp, 0, projection, 0, mvp, 0)

    GLES20.glUseProgram(program)
    val pos = GLES20.glGetAttribLocation(program, "aPosition")
    val matrix = GLES20.glGetUniformLocation(program, "uMvp")
    GLES20.glUniformMatrix4fv(matrix, 1, false, mvp, 0)
    vertexBuffer!!.position(0)
    GLES20.glEnableVertexAttribArray(pos)
    GLES20.glVertexAttribPointer(pos, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)
    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, count)
    GLES20.glDisableVertexAttribArray(pos)
  }

  private fun loadMesh() {
    val model = FishModelCatalog.forSpecies(speciesId) ?: return
    try {
      val raw = context.assets.open(model.objAsset).bufferedReader().use { it.readLines() }
      val positions = mutableListOf<FloatArray>()
      val triangles = mutableListOf<Float>()
      raw.forEach { line ->
        val p = line.trim().split(" ").filter { it.isNotBlank() }
        if (p.isEmpty()) return@forEach
        if (p[0] == "v" && p.size >= 4) positions += floatArrayOf(p[1].toFloat(), p[2].toFloat(), p[3].toFloat())
        if (p[0] == "f" && p.size >= 4) {
          val ids = p.drop(1).mapNotNull { it.substringBefore('/').toIntOrNull()?.minus(1) }
          for (i in 1 until ids.size - 1) {
            listOf(ids[0], ids[i], ids[i + 1]).forEach { idx -> positions.getOrNull(idx)?.let { triangles.addAll(it.asList()) } }
          }
        }
      }
      val max = triangles.maxOfOrNull { kotlin.math.abs(it) }?.coerceAtLeast(0.001f) ?: 1f
      vertices = triangles.map { it / max }.toFloatArray()
      vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(vertices); position(0) }
      count = vertices.size / 3
    } catch (_: Exception) {
      vertexBuffer = null
      count = 0
    }
  }

  private fun compile(type: Int, source: String): Int {
    val shader = GLES20.glCreateShader(type)
    GLES20.glShaderSource(shader, source)
    GLES20.glCompileShader(shader)
    return shader
  }
  private fun linkProgram(vs: String, fs: String): Int {
    val p = GLES20.glCreateProgram()
    GLES20.glAttachShader(p, compile(GLES20.GL_VERTEX_SHADER, vs))
    GLES20.glAttachShader(p, compile(GLES20.GL_FRAGMENT_SHADER, fs))
    GLES20.glLinkProgram(p)
    return p
  }
}
