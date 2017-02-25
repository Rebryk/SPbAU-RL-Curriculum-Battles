package ru.spbau.mit

import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Paths

fun mergeResults(folder: String, resultPath: String) {
    val result = mutableMapOf<Int, MutableList<Double>>()
    var filesCount = 0

    File(folder)
            .listFiles()
            .filter { it.extension == "csv" }
            .forEach {
                ++filesCount

                val data = FileReader(it)
                        .readLines()
                        .filterIndexed { i, data -> i != 0 }
                        .map {
                            it.split(',')
                        }

                data.forEachIndexed { index, row ->
                    if (!result.containsKey(index)) {
                        result[index] = mutableListOf(0.0, 0.0, 0.0, 0.0)
                    }

                    for (i in 0..3) {
                        result[index]!![i] += row[i + 3].toDouble()
                    }
                }
            }

    val data = mutableListOf<Array<String>>()

    result.forEach {
        val row = mutableListOf(it.key.toString())
        row.addAll(it.value.map { it / filesCount }.map { it.toString() })
        data.add(row.toTypedArray())
    }

    if (!File(resultPath).exists()) {
        Files.createFile(Paths.get(resultPath))
    }

    val writer = FileWriter(File(resultPath))
    writer.write("episode,cumulativeReward,averageReward,cumulativeSteps,numSteps\n")
    data.forEach {
        writer.write(it.joinToString(",", "", "\n"))
    }
    writer.flush()
}

fun main(args: Array<String>) {
    mergeResults("data/hard", "data/hard_result.csv")
    mergeResults("data/flexible", "data/flexible_result.csv")
    mergeResults("data/flexible3", "data/flexible_result3.csv")
}