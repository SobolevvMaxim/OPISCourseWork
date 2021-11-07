package com.example.opiscoursework



data class Settings(var canny: CannySettings, var size: SizeSettings, var lineColor: LineColor, var lineThickness: Int = 0) {
}

data class CannySettings(var border1: Double = 10.0, var border2: Double = 250.0)

data class SizeSettings(var border1: Double = 2.0, var border2: Double = 2.0)

data class LineColor(var color1: Double = 0.0, var color2: Double = 255.0, var color3: Double = 0.0)