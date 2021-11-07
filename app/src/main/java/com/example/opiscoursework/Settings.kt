package com.example.opiscoursework



data class Settings(var canny: CannySettings, var size: SizeSettings, var lineColor: LineColor, var lineThickness: Int) {
}

data class CannySettings(var borders: Pair<Double, Double> = Pair(10.0, 250.0))

data class SizeSettings(var borders: Pair<Double, Double> = Pair(2.0, 2.0))

data class LineColor(var color1: Double = 0.0, var color2: Double = 255.0, var color3: Double = 0.0)