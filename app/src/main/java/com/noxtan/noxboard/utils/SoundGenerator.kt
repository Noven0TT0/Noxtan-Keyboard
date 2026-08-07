package com.noxtan.noxboard.utils

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import kotlin.math.exp
import kotlin.math.sin

object SoundGenerator {
    fun generateSounds(context: Context): Map<String, String> {
        val paths = mutableMapOf<String, String>()
        paths["DEFAULT"] = createWav(context, "default_click.wav", generateDefault())
        paths["BUBBLE"] = createWav(context, "bubble.wav", generateBubble())
        paths["IOS"] = createWav(context, "ios.wav", generateIos())
        paths["TYPEWRITER"] = createWav(context, "typewriter.wav", generateTypewriter())
        paths["WOODEN"] = createWav(context, "wooden.wav", generateWooden())
        paths["SOFT_THUD"] = createWav(context, "soft_thud.wav", generateSoftThud())
        paths["SCI_FI"] = createWav(context, "scifi.wav", generateSciFi())
        paths["MECH_CLICKY"] = createWav(context, "mech_clicky.wav", generateClickySwitch())
        paths["MECH_THOCKY"] = createWav(context, "mech_thocky.wav", generateThockySwitch())
        paths["MECH_LINEAR"] = createWav(context, "mech_linear.wav", generateLinearClack())
        paths["MECH_TACTILE"] = createWav(context, "mech_tactile.wav", generateTactileSwitch())
        paths["MECH_SILENT"] = createWav(context, "mech_silent.wav", generateSilentSwitch())

        return paths
    }

    private fun generateTypewriter(): ShortArray {
        val samples = ShortArray((44100 * 0.03).toInt())
        for (i in samples.indices) {
            val t = i / 44100.0
            val env = kotlin.math.exp(-t * 300)
            val noise = Math.random() * 2 - 1
            val wave = (noise * 0.7 + kotlin.math.sin(2 * Math.PI * 2500 * t) * 0.3) * env
            samples[i] = (wave * Short.MAX_VALUE * 0.6).toInt().toShort()
        }
        return samples
    }

    private fun generateWooden(): ShortArray {
        val samples = ShortArray((44100 * 0.04).toInt())
        for (i in samples.indices) {
            val t = i / 44100.0
            val env = kotlin.math.exp(-t * 120)
            val wave = kotlin.math.sin(2 * Math.PI * 400 * t) * env
            samples[i] = (wave * Short.MAX_VALUE * 0.7).toInt().toShort()
        }
        return samples
    }

    private fun generateSoftThud(): ShortArray {
        val samples = ShortArray((44100 * 0.025).toInt())
        for (i in samples.indices) {
            val t = i / 44100.0
            val env = kotlin.math.exp(-t * 200)
            val wave = kotlin.math.sin(2 * Math.PI * 150 * t) * env
            samples[i] = (wave * Short.MAX_VALUE * 0.5).toInt().toShort()
        }
        return samples
    }

    private fun generateSciFi(): ShortArray {
        val samples = ShortArray((44100 * 0.03).toInt())
        for (i in samples.indices) {
            val t = i / 44100.0
            val env = kotlin.math.exp(-t * 150)
            val freq = 2000 - (10000 * t)
            val wave = kotlin.math.sin(2 * Math.PI * freq * t) * env
            samples[i] = (wave * Short.MAX_VALUE * 0.4).toInt().toShort()
        }
        return samples
    }

    private fun generateDefault(): ShortArray {
        val samples = ShortArray((44100 * 0.015).toInt())
        for (i in samples.indices) {
            val t = i / 44100.0
            val env = exp(-t * 400)
            val wave = sin(2 * Math.PI * 600 * t) * env
            samples[i] = (wave * Short.MAX_VALUE * 0.3).toInt().toShort()
        }
        return samples
    }

    private fun generateClickySwitch(): ShortArray {
        val samples = ShortArray((44100 * 0.03).toInt())
        for (i in samples.indices) {
            val t = i / 44100.0
            val env = kotlin.math.exp(-t * 220)
            val noise = (Math.random() * 2 - 1) * kotlin.math.exp(-t * 800)
            val click = kotlin.math.sin(2 * Math.PI * 3200 * t) * kotlin.math.exp(-t * 500)
            val body = kotlin.math.sin(2 * Math.PI * 450 * t)
            val wave = (noise * 0.5 + click * 0.3 + body * 0.2) * env
            samples[i] = (wave * Short.MAX_VALUE * 0.6).toInt().toShort()
        }
        return samples
    }

    private fun generateThockySwitch(): ShortArray {
        val samples = ShortArray((44100 * 0.05).toInt())
        for (i in samples.indices) {
            val t = i / 44100.0
            val env = kotlin.math.exp(-t * 90)
            val bass = kotlin.math.sin(2 * Math.PI * 180 * t) + kotlin.math.sin(2 * Math.PI * 220 * t) * 0.5
            val noise = (Math.random() * 2 - 1) * kotlin.math.exp(-t * 400) * 0.2
            val wave = (bass * 0.8 + noise) * env
            samples[i] = (wave * Short.MAX_VALUE * 0.7).toInt().toShort()
        }
        return samples
    }

    private fun generateLinearClack(): ShortArray {
        val samples = ShortArray((44100 * 0.035).toInt())
        for (i in samples.indices) {
            val t = i / 44100.0
            val env = kotlin.math.exp(-t * 180)
            val clack = kotlin.math.sin(2 * Math.PI * 950 * t) * 0.6
            val noise = (Math.random() * 2 - 1) * kotlin.math.exp(-t * 350) * 0.4
            val wave = (clack + noise) * env
            samples[i] = (wave * Short.MAX_VALUE * 0.6).toInt().toShort()
        }
        return samples
    }

    private fun generateTactileSwitch(): ShortArray {
        val samples = ShortArray((44100 * 0.04).toInt())
        for (i in samples.indices) {
            val t = i / 44100.0
            val env = kotlin.math.exp(-t * 140)
            val bump = kotlin.math.sin(2 * Math.PI * 520 * t) * 0.6
            val noise = (Math.random() * 2 - 1) * kotlin.math.exp(-t * 250) * 0.4
            val wave = (bump + noise) * env
            samples[i] = (wave * Short.MAX_VALUE * 0.55).toInt().toShort()
        }
        return samples
    }

    private fun generateSilentSwitch(): ShortArray {
        val samples = ShortArray((44100 * 0.025).toInt())
        for (i in samples.indices) {
            val t = i / 44100.0
            val env = kotlin.math.exp(-t * 250)
            val mutedThud = kotlin.math.sin(2 * Math.PI * 130 * t) * env
            samples[i] = (mutedThud * Short.MAX_VALUE * 0.35).toInt().toShort()
        }
        return samples
    }

    private fun generateBubble(): ShortArray {
        val samples = ShortArray((44100 * 0.06).toInt())
        for (i in samples.indices) {
            val t = i / 44100.0
            val env = exp(-t * 60)
            val freq = 300 + (2000 * t)
            val wave = sin(2 * Math.PI * freq * t) * env
            samples[i] = (wave * Short.MAX_VALUE * 0.6).toInt().toShort()
        }
        return samples
    }

    private fun generateIos(): ShortArray {
        val samples = ShortArray((44100 * 0.02).toInt())
        for (i in samples.indices) {
            val t = i / 44100.0
            val env = exp(-t * 300)
            val wave = sin(2 * Math.PI * 800 * t) * env
            samples[i] = (wave * Short.MAX_VALUE * 0.4).toInt().toShort()
        }
        return samples
    }

    private fun createWav(context: Context, fileName: String, pcmData: ShortArray): String {
        val file = File(context.cacheDir, fileName)
        val out = FileOutputStream(file)

        val sampleRate = 44100
        val byteRate = sampleRate * 2
        val dataSize = pcmData.size * 2
        val chunkSize = 36 + dataSize

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (chunkSize and 0xff).toByte(); header[5] = ((chunkSize shr 8) and 0xff).toByte(); header[6] = ((chunkSize shr 16) and 0xff).toByte(); header[7] = ((chunkSize shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
        header[20] = 1; header[21] = 0
        header[22] = 1; header[23] = 0
        header[24] = (sampleRate and 0xff).toByte(); header[25] = ((sampleRate shr 8) and 0xff).toByte(); header[26] = ((sampleRate shr 16) and 0xff).toByte(); header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte(); header[29] = ((byteRate shr 8) and 0xff).toByte(); header[30] = ((byteRate shr 16) and 0xff).toByte(); header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = 2; header[33] = 0
        header[34] = 16; header[35] = 0
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (dataSize and 0xff).toByte(); header[41] = ((dataSize shr 8) and 0xff).toByte(); header[42] = ((dataSize shr 16) and 0xff).toByte(); header[43] = ((dataSize shr 24) and 0xff).toByte()

        out.write(header)
        val audioBytes = ByteArray(dataSize)
        for (i in pcmData.indices) {
            audioBytes[i * 2] = (pcmData[i].toInt() and 0x00FF).toByte()
            audioBytes[i * 2 + 1] = (pcmData[i].toInt() shr 8).toByte()
        }
        out.write(audioBytes)
        out.close()

        return file.absolutePath
    }
}