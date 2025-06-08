package me.ele.uetool

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Rect
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.*
import androidx.annotation.RequiresApi
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 支持视图树截图和分层绘制的 GLSurfaceView
 * 通过OpenGL ES 2.0实现视图的层级渲染和截图功能
 */
class ScalpelGLSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private var renderer: CustomRenderer? = null

    init {
        init()
    }

    private fun init() {
        // 设置OpenGL ES 2.0上下文
        setEGLContextClientVersion(2)
        // 创建自定义渲染器
        renderer = CustomRenderer()
        setRenderer(renderer)
        // 设置为按需渲染模式
        renderMode = RENDERMODE_WHEN_DIRTY
        // 设置透明背景
        holder.setFormat(PixelFormat.TRANSLUCENT)
        setZOrderOnTop(true)
    }

    // region 公共方法

    /**
     * 设置背景颜色
     * @param red 红色分量 [0,1]
     * @param green 绿色分量 [0,1]
     * @param blue 蓝色分量 [0,1]
     * @param alpha 透明度 [0,1]
     */
    fun setBackgroundColor(red: Float, green: Float, blue: Float, alpha: Float) {
        renderer?.setClearColor(red, green, blue, alpha)
    }

    /**
     * 捕获视图树并绘制到OpenGL表面
     * @param root 根视图
     * @param window 窗口对象，用于PixelCopy操作
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun captureAndDrawViewTree(root: View, window: Window) {
        // 遍历视图树获取所有可见视图
        val views = traverseViewTree(root)
        // 清除所有现有纹理
        renderer?.clearAllTextures()

        // 对每个视图进行截图并创建纹理
        views.forEachIndexed { index, view ->
            viewToBitmapWithPixelCopy(view, window) { bitmap ->
                post {
                    bitmap?.let {
                        // 计算视图相对于根视图的偏移并添加纹理
                        renderer?.addTexture(it, calculateViewOffset(view, root))
                        // 如果是最后一个视图，触发渲染
                        if (index == views.lastIndex) {
                            requestRender()
                        }
                    }
                }
            }
        }
    }

    /**
     * 遍历视图树获取所有可见视图
     * @param root 根视图
     * @return 可见视图列表
     */
    private fun traverseViewTree(root: View): List<View> {
        val queue: Queue<View> = LinkedList()
        val result = mutableListOf<View>()
        queue.add(root)

        while (queue.isNotEmpty()) {
            val current = queue.poll()
            // 只处理可见且不是自身的视图
            if (current.visibility == View.VISIBLE && current != this) {
                result.add(current)
                // 如果是视图组，递归处理子视图
                if (current is ViewGroup) {
                    for (i in 0 until current.childCount) {
                        queue.add(current.getChildAt(i))
                    }
                }
            }
        }
        return result
    }

    /**
     * 计算视图相对于根视图的归一化偏移
     * @param view 当前视图
     * @param root 根视图
     * @return 归一化后的偏移数组 [x, y]
     */
    private fun calculateViewOffset(view: View, root: View): FloatArray {
        val viewLocation = IntArray(2)
        val rootLocation = IntArray(2)
        view.getLocationInWindow(viewLocation)
        root.getLocationInWindow(rootLocation)

        // 计算相对于根视图的偏移并归一化
        return floatArrayOf(
            (viewLocation[0] - rootLocation[0]).toFloat() / root.width,
            (viewLocation[1] - rootLocation[1]).toFloat() / root.height
        )
    }

    /**
     * 使用PixelCopy API将视图转换为位图
     * @param view 要截图的视图
     * @param window 窗口对象
     * @param callback 完成回调
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun viewToBitmapWithPixelCopy(view: View, window: Window, callback: (Bitmap?) -> Unit) {
        // 创建与视图大小相同的位图
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val location = IntArray(2)
        view.getLocationInWindow(location)

        // 使用PixelCopy API进行截图
        PixelCopy.request(
            window,
            Rect(location[0], location[1], location[0] + view.width, location[1] + view.height),
            bitmap,
            { copyResult -> callback(if (copyResult == PixelCopy.SUCCESS) bitmap else null) },
            Handler(Looper.getMainLooper())
        )
    }

    /**
     * 自定义渲染器实现
     * 负责OpenGL ES 2.0的渲染管线配置和绘制操作
     */
    private inner class CustomRenderer : Renderer {
        // 顶点着色器代码
        // 作用：处理顶点位置和纹理坐标的变换
        private val vertexShaderCode = """
        // 统一变量：模型-视图-投影矩阵（4x4）
        uniform mat4 uMVPMatrix;
        // 属性变量：顶点位置（vec4表示齐次坐标）
        attribute vec4 vPosition;
        // 属性变量：纹理坐标（vec2表示二维坐标）
        attribute vec2 vTexCoord;
        // 可变变量：传递给片段着色器的纹理坐标
        varying vec2 texCoord;
        
        void main() {
            // 顶点位置经过MVP矩阵变换得到裁剪空间坐标
            gl_Position = uMVPMatrix * vPosition;
            // 将纹理坐标传递给片段着色器
            texCoord = vTexCoord;
        }
    """.trimIndent()

        // 片段着色器代码
        // 作用：处理每个像素的颜色计算
        private val fragmentShaderCode = """
        // 设置浮点数精度（中等精度）
        precision mediump float;
        // 统一变量：纹理采样器
        uniform sampler2D texSampler;
        // 从顶点着色器传递过来的纹理坐标
        varying vec2 texCoord;
        
        void main() {
            // 使用纹理坐标从纹理采样器中获取颜色值
            // texture2D函数：第一个参数是采样器，第二个参数是纹理坐标
            // 返回的是RGBA颜色值
            gl_FragColor = texture2D(texSampler, texCoord);
            
            /* 
             * 注意：
             * 1. 这里实现的是最简单的纹理采样，没有考虑：
             *    - 多重纹理混合
             *    - 颜色混合模式
             *    - 特殊效果（如透明度混合）
             * 2. 如果需要实现更复杂的效果，可以在此处添加额外的处理逻辑
             * 3. texCoord的范围是[0,1]，对应纹理的完整范围
             */
        }
    """.trimIndent()

        // 顶点坐标
        private val vertices = floatArrayOf(
            -1.0f, 1.0f, 0.0f, -1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f, 1.0f, 1.0f, 0.0f
        )

        // 纹理坐标
        private val texCoords = floatArrayOf(
            0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f
        )

        // 绘制顺序索引
        private val indices = shortArrayOf(0, 1, 2, 0, 2, 3)

        // 顶点缓冲区
        private val vertexBuffer: FloatBuffer
        // 纹理坐标缓冲区
        private val texCoordBuffer: FloatBuffer
        // 索引缓冲区
        private val indexBuffer: ShortBuffer
        // OpenGL程序ID
        private var program = 0
        // 纹理数据列表
        private val textures = mutableListOf<TextureData>()
        // 模型视图投影矩阵
        private val mvpMatrix = FloatArray(16)
        // 投影矩阵
        private val projectionMatrix = FloatArray(16)
        // 视图矩阵
        private val viewMatrix = FloatArray(16)
        // 清除颜色RGBA分量
        private var clearRed = 0f
        private var clearGreen = 0f
        private var clearBlue = 0f
        private var clearAlpha = 1f

        init {
            // 初始化顶点缓冲区
            vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(vertices); position(0) }

            // 初始化纹理坐标缓冲区
            texCoordBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(texCoords); position(0) }

            // 初始化索引缓冲区
            indexBuffer = ByteBuffer.allocateDirect(indices.size * 2)
                .order(ByteOrder.nativeOrder()).asShortBuffer().apply { put(indices); position(0) }
        }

        /**
         * 添加纹理
         * @param bitmap 位图数据
         * @param offset 纹理偏移 [x, y]
         */
        fun addTexture(bitmap: Bitmap, offset: FloatArray) {
            // 加载纹理到OpenGL
            val textureId = loadTexture(bitmap)
            // 计算纹理尺寸并保存纹理数据
            textures.add(TextureData(textureId, offset[0], offset[1],
                bitmap.width.toFloat() / width, bitmap.height.toFloat() / height))
            // 回收位图
            bitmap.recycle()
        }

        /**
         * 清除所有纹理
         */
        fun clearAllTextures() {
            // 删除所有纹理并清空列表
            textures.forEach { GLES20.glDeleteTextures(1, intArrayOf(it.textureId), 0) }
            textures.clear()
        }

        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
            // 设置清除颜色
            GLES20.glClearColor(clearRed, clearGreen, clearBlue, clearAlpha)
            // 启用混合
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

            // 加载并编译着色器
            val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
            val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

            // 创建OpenGL程序并链接着色器
            program = GLES20.glCreateProgram().apply {
                GLES20.glAttachShader(this, vertexShader)
                GLES20.glAttachShader(this, fragmentShader)
                GLES20.glLinkProgram(this)
            }
        }

        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            // 设置视口
            GLES20.glViewport(0, 0, width, height)
            // 设置正交投影矩阵
            Matrix.orthoM(projectionMatrix, 0, -1f, 1f, -1f, 1f, -1f, 1f)
            // 设置视图矩阵
            Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f)
        }

        override fun onDrawFrame(gl: GL10) {
            // 清除颜色缓冲区
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

            // 绘制所有纹理
            textures.forEach { texture ->
                // 计算模型矩阵
                val modelMatrix = FloatArray(16).apply {
                    Matrix.setIdentityM(this, 0)
                    // 根据纹理偏移和尺寸设置位置
                    Matrix.translateM(this, 0, texture.offsetX * 2 - 1 + texture.width,
                        1 - texture.offsetY * 2 - texture.height, 0f)
                    Matrix.scaleM(this, 0, texture.width, texture.height, 1f)
                }

                // 计算MVP矩阵
                Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

                // 使用程序
                GLES20.glUseProgram(program)
                // 传递MVP矩阵
                GLES20.glUniformMatrix4fv(
                    GLES20.glGetUniformLocation(program, "uMVPMatrix"), 1, false, mvpMatrix, 0)

                // 绑定纹理
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture.textureId)
                GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "texSampler"), 0)

                // 设置顶点属性
                GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(program, "vPosition"))
                GLES20.glVertexAttribPointer(
                    GLES20.glGetAttribLocation(program, "vPosition"), 3,
                    GLES20.GL_FLOAT, false, 0, vertexBuffer)

                // 设置纹理坐标属性
                GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(program, "vTexCoord"))
                GLES20.glVertexAttribPointer(
                    GLES20.glGetAttribLocation(program, "vTexCoord"), 2,
                    GLES20.GL_FLOAT, false, 0, texCoordBuffer)

                // 绘制
                GLES20.glDrawElements(
                    GLES20.GL_TRIANGLES, indices.size,
                    GLES20.GL_UNSIGNED_SHORT, indexBuffer)
            }
        }

        /**
         * 加载纹理
         * @param bitmap 位图数据
         * @return 纹理ID
         */
        private fun loadTexture(bitmap: Bitmap): Int {
            return IntArray(1).also { textureId ->
                // 生成纹理
                GLES20.glGenTextures(1, textureId, 0)
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0])
                // 设置纹理参数
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
                // 加载位图数据到纹理
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
            }[0]
        }

        /**
         * 加载着色器
         * @param type 着色器类型
         * @param shaderCode 着色器代码
         * @return 着色器ID
         */
        private fun loadShader(type: Int, shaderCode: String): Int {
            return GLES20.glCreateShader(type).also { shader ->
                GLES20.glShaderSource(shader, shaderCode)
                GLES20.glCompileShader(shader)
            }
        }

        /**
         * 设置清除颜色
         * @param red 红色分量 [0,1]
         * @param green 绿色分量 [0,1]
         * @param blue 蓝色分量 [0,1]
         * @param alpha 透明度 [0,1]
         */
        fun setClearColor(red: Float, green: Float, blue: Float, alpha: Float) {
            clearRed = red
            clearGreen = green
            clearBlue = blue
            clearAlpha = alpha
            GLES20.glClearColor(red, green, blue, alpha)
        }
    }

    /**
     * 纹理数据类
     * @property textureId 纹理ID
     * @property offsetX X轴偏移
     * @property offsetY Y轴偏移
     * @property width 纹理宽度(归一化)
     * @property height 纹理高度(归一化)
     */
    private data class TextureData(
        val textureId: Int,
        val offsetX: Float,
        val offsetY: Float,
        val width: Float,
        val height: Float
    )
}