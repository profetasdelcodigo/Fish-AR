package com.example.ui.components

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.model.FishModelCatalog
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/** Real-time fish mesh renderer with GPU swimming, frenzy, charge and shock motion. */
class FishMeshSurfaceView(context: Context) : GLSurfaceView(context) {
  private val renderer = FishMeshRenderer(context)

  init {
    setEGLContextClientVersion(2)
    setEGLConfigChooser(8, 8, 8, 8, 16, 0)
    holder.setFormat(PixelFormat.TRANSLUCENT)
    setZOrderOnTop(true)
    setRenderer(renderer)
    renderMode = RENDERMODE_CONTINUOUSLY
  }

  /** Species changes are queued onto the OpenGL thread. */
  fun setSpecies(speciesId: String) {
    queueEvent { renderer.setSpecies(speciesId) }
  }

  fun setMotion(swim: Float, yaw: Float, pitch: Float, scale: Float, behavior: String = "SWIMMING_IDLE") =
    renderer.setMotion(swim, yaw, pitch, scale, behavior)
}

private class FishMeshRenderer(private val context: Context) : GLSurfaceView.Renderer {
  // Empty until the first species is explicitly selected. This guarantees that
  // even bonito loads correctly on the first AndroidView update.
  private var speciesId = ""
  private var vertices = FloatArray(0)
  private var normals = FloatArray(0)
  private var vertexBuffer: FloatBuffer? = null
  private var normalBuffer: FloatBuffer? = null
  private var program = 0
  private var count = 0

  @Volatile private var swim = 0f
  @Volatile private var yaw = 0f
  @Volatile private var pitch = 0f
  @Volatile private var scale = 1f
  @Volatile private var behavior = "SWIMMING_IDLE"

  private val projection = FloatArray(16)
  private val view = FloatArray(16)
  private val model = FloatArray(16)
  private val mvp = FloatArray(16)

  fun setSpecies(id: String) {
    if (speciesId == id && count > 0) return
    speciesId = id
    loadMesh()
  }

  fun setMotion(swim: Float, yaw: Float, pitch: Float, scale: Float, behavior: String) {
    this.swim = swim
    this.yaw = yaw
    this.pitch = pitch
    this.scale = scale
    this.behavior = behavior
  }

  override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
    GLES20.glClearColor(0f, 0f, 0f, 0f)
    GLES20.glEnable(GLES20.GL_BLEND)
    GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

    val vs = """
      attribute vec3 aPosition;
      attribute vec3 aNormal;
      uniform mat4 uMvp;
      uniform float uTime;
      uniform float uBehavior;
      varying vec3 vNormal;
      varying float vDepth;
      void main() {
        vec3 p = aPosition;
        float phase = uTime * 6.2831853;
        float lengthCoord = clamp((p.x + 1.0) * 0.5, 0.0, 1.0);
        float tailWeight = smoothstep(0.12, 0.95, 1.0 - lengthCoord);

        // Base swimming: tail follows the body instead of translating a flat image.
        p.y += sin(phase * 1.35 + p.x * 7.0) * 0.075 * tailWeight;
        p.z += cos(phase * 1.35 + p.x * 6.0) * 0.035 * tailWeight;

        // Frenzy: much faster and less predictable tail/body movement.
        if (uBehavior > 3.5 && uBehavior < 4.5) {
          p.y += sin(phase * 7.0 + p.x * 13.0) * 0.10 * tailWeight;
          p.z += cos(phase * 9.0 + p.y * 11.0) * 0.07;
        }

        // Shock: rapid electrical twitch.
        if (uBehavior > 6.5) {
          p.y += sin(phase * 16.0 + p.x * 10.0) * 0.045;
          p.z += cos(phase * 13.0 + p.x * 8.0) * 0.035;
        }

        gl_Position = uMvp * vec4(p, 1.0);
        vNormal = aNormal;
        vDepth = clamp((p.y + 0.8) * 0.55, 0.0, 1.0);
      }
    """.trimIndent()

    val fs = """
      precision mediump float;
      varying vec3 vNormal;
      varying float vDepth;
      uniform float uBehavior;
      void main() {
        vec3 N = normalize(vNormal);
        vec3 L = normalize(vec3(-0.35, 0.75, 0.65));
        float diffuse = max(dot(N, L), 0.0);
        float rim = pow(1.0 - max(dot(N, vec3(0.0, 0.0, 1.0)), 0.0), 2.2);
        vec3 deep = vec3(0.025, 0.075, 0.095);
        vec3 silver = vec3(0.42, 0.62, 0.67);
        vec3 body = mix(deep, silver, clamp(vDepth * 0.75 + diffuse * 0.65, 0.0, 1.0));
        body += vec3(0.15, 0.32, 0.36) * rim;
        if (uBehavior > 3.5 && uBehavior < 6.5) body += vec3(0.28, 0.025, 0.015);
        if (uBehavior > 6.5) body += vec3(0.05, 0.28, 0.34);
        gl_FragColor = vec4(body, 0.96);
      }
    """.trimIndent()

    program = linkProgram(vs, fs)
    // If AndroidView has not sent a species yet, use the first catalog entry as
    // a safe visual fallback. A later setSpecies() will replace it on the GL thread.
    if (speciesId.isEmpty()) speciesId = "bonito"
    loadMesh()
    Matrix.setLookAtM(view, 0, 0f, 0.15f, 3.2f, 0f, 0f, 0f, 0f, 1f, 0f)
  }

  override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
    GLES20.glViewport(0, 0, width, height)
    Matrix.perspectiveM(projection, 0, 42f, width.toFloat() / height.coerceAtLeast(1), 0.1f, 100f)
  }

  override fun onDrawFrame(gl: GL10?) {
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
    if (vertexBuffer == null || normalBuffer == null || count == 0) return

    GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    val t = swim
    val wave = sin(t * 6.2831853f)
    val fastWave = sin(t * 18.0f)

    Matrix.setIdentityM(model, 0)
    var currentYaw = yaw + wave * 4f
    var currentPitch = pitch
    var z = 0f
    var extraScale = 1f

    when (behavior) {
      "STALKING_CIRCLING" -> currentYaw += sin(t * 6.2831853f) * 18f
      "AMBUSH_PREPARE" -> {
        currentPitch += sin(t * 18f) * 2.5f
        extraScale = 1f + kotlin.math.abs(sin(t * 9f)) * 0.025f
      }
      "FRENZY_HAYWIRE" -> {
        currentYaw += sin(t * 31f) * 22f
        currentPitch += cos(t * 27f) * 13f
        Matrix.rotateM(model, 0, sin(t * 35f) * 16f, 0f, 0f, 1f)
      }
      "CHARGING_FAST" -> {
        // Face the player and surge forward in short attack pulses.
        currentYaw += 90f
        z = 0.65f + (1f - (0.5f + 0.5f * wave)) * 0.42f
        extraScale = 1f + 0.055f * kotlin.math.abs(fastWave)
      }
      "FEINT_DISSOLVE" -> {
        currentYaw += 90f + sin(t * 8f) * 10f
        extraScale = 1f - (0.5f + 0.5f * wave) * 0.18f
      }
      "ELECTROCUTED" -> {
        currentYaw += sin(t * 28f) * 7f
        currentPitch += cos(t * 32f) * 10f
      }
    }

    Matrix.translateM(model, 0, 0f, 0f, z)
    Matrix.rotateM(model, 0, currentPitch, 1f, 0f, 0f)
    Matrix.rotateM(model, 0, currentYaw, 0f, 1f, 0f)
    Matrix.scaleM(model, 0, scale * extraScale, scale * extraScale, scale * extraScale)
    Matrix.multiplyMM(mvp, 0, view, 0, model, 0)
    Matrix.multiplyMM(mvp, 0, projection, 0, mvp, 0)

    GLES20.glUseProgram(program)
    val pos = GLES20.glGetAttribLocation(program, "aPosition")
    val normal = GLES20.glGetAttribLocation(program, "aNormal")
    GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program, "uMvp"), 1, false, mvp, 0)
    GLES20.glUniform1f(GLES20.glGetUniformLocation(program, "uTime"), t)
    GLES20.glUniform1f(GLES20.glGetUniformLocation(program, "uBehavior"), behaviorCode())

    vertexBuffer!!.position(0)
    normalBuffer!!.position(0)
    GLES20.glEnableVertexAttribArray(pos)
    GLES20.glEnableVertexAttribArray(normal)
    GLES20.glVertexAttribPointer(pos, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)
    GLES20.glVertexAttribPointer(normal, 3, GLES20.GL_FLOAT, false, 12, normalBuffer)
    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, count)
    GLES20.glDisableVertexAttribArray(pos)
    GLES20.glDisableVertexAttribArray(normal)
  }

  private fun behaviorCode(): Float = when (behavior) {
    "SWIMMING_IDLE" -> 1f
    "STALKING_CIRCLING" -> 2f
    "AMBUSH_PREPARE" -> 3f
    "FRENZY_HAYWIRE" -> 4f
    "CHARGING_FAST" -> 5f
    "FEINT_DISSOLVE" -> 6f
    "ELECTROCUTED" -> 7f
    else -> 1f
  }

  private fun loadMesh() {
    val model = FishModelCatalog.forSpecies(speciesId) ?: return
    try {
      val raw = context.assets.open(model.objAsset).bufferedReader().use { it.readLines() }
      val positions = mutableListOf<FloatArray>()
      val trianglePositions = mutableListOf<FloatArray>()
      raw.forEach { line ->
        val p = line.trim().split(" ").filter { it.isNotBlank() }
        if (p.isEmpty()) return@forEach
        if (p[0] == "v" && p.size >= 4) positions += floatArrayOf(p[1].toFloat(), p[2].toFloat(), p[3].toFloat())
        if (p[0] == "f" && p.size >= 4) {
          val ids = p.drop(1).mapNotNull { it.substringBefore('/').toIntOrNull()?.minus(1) }
          for (i in 1 until ids.size - 1) {
            val a = positions.getOrNull(ids[0])
            val b = positions.getOrNull(ids[i])
            val c = positions.getOrNull(ids[i + 1])
            if (a != null && b != null && c != null) {
              trianglePositions += a; trianglePositions += b; trianglePositions += c
            }
          }
        }
      }

      val max = trianglePositions.flatMap { it.asList() }.maxOfOrNull { kotlin.math.abs(it) }?.coerceAtLeast(0.001f) ?: 1f
      vertices = trianglePositions.flatMap { it.map { value -> value / max } }.toFloatArray()
      normals = FloatArray(vertices.size)

      for (i in trianglePositions.indices step 3) {
        val a = trianglePositions[i]
        val b = trianglePositions[i + 1]
        val c = trianglePositions[i + 2]
        val ux = b[0] - a[0]; val uy = b[1] - a[1]; val uz = b[2] - a[2]
        val vx = c[0] - a[0]; val vy = c[1] - a[1]; val vz = c[2] - a[2]
        var nx = uy * vz - uz * vy
        var ny = uz * vx - ux * vz
        var nz = ux * vy - uy * vx
        val len = kotlin.math.sqrt(nx * nx + ny * ny + nz * nz).coerceAtLeast(0.0001f)
        nx /= len; ny /= len; nz /= len
        val out = i * 3
        repeat(3) { k ->
          normals[out + k * 3] = nx
          normals[out + k * 3 + 1] = ny
          normals[out + k * 3 + 2] = nz
        }
      }

      vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(vertices); position(0) }
      normalBuffer = ByteBuffer.allocateDirect(normals.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(normals); position(0) }
      count = vertices.size / 3
    } catch (_: Exception) {
      vertexBuffer = null
      normalBuffer = null
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
