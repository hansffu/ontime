package hansffu.ontime.conversion

class Deg2UTM(Lat: Double, Lon: Double) {
    var easting: Double = 0.toDouble()
        private set
    var northing: Double = 0.toDouble()
        private set
    val zone: Int = Math.floor(Lon / 6 + 31).toInt()
    var letter: Char = ' '
        private set

    init {
        when {
            Lat < -72 -> letter = 'C'
            Lat < -64 -> letter = 'D'
            Lat < -56 -> letter = 'E'
            Lat < -48 -> letter = 'F'
            Lat < -40 -> letter = 'G'
            Lat < -32 -> letter = 'H'
            Lat < -24 -> letter = 'J'
            Lat < -16 -> letter = 'K'
            Lat < -8 -> letter = 'L'
            Lat < 0 -> letter = 'M'
            Lat < 8 -> letter = 'N'
            Lat < 16 -> letter = 'P'
            Lat < 24 -> letter = 'Q'
            Lat < 32 -> letter = 'R'
            Lat < 40 -> letter = 'S'
            Lat < 48 -> letter = 'T'
            Lat < 56 -> letter = 'U'
            Lat < 64 -> letter = 'V'
            Lat < 72 -> letter = 'W'
            else -> letter = 'X'
        }

        easting = 0.5 * Math.log((1 + Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180)) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180))) * 0.9996 * 6399593.62 / Math.pow(1 + Math.pow(0.0820944379, 2.0) * Math.pow(Math.cos(Lat * Math.PI / 180), 2.0), 0.5) * (1 + Math.pow(0.0820944379, 2.0) / 2 * Math.pow(0.5 * Math.log((1 + Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180)) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180))), 2.0) * Math.pow(Math.cos(Lat * Math.PI / 180), 2.0) / 3) + 500000
        easting = Math.round(easting * 100) * 0.01


        northing = (Math.atan(Math.tan(Lat * Math.PI / 180) / Math.cos(Lon * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180)) - Lat * Math.PI / 180) * 0.9996 * 6399593.625 / Math.sqrt(1 + 0.006739496742 * Math.pow(Math.cos(Lat * Math.PI / 180), 2.0)) * (1 + 0.006739496742 / 2 * Math.pow(0.5 * Math.log((1 + Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180)) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180))), 2.0) * Math.pow(Math.cos(Lat * Math.PI / 180), 2.0)) + 0.9996 * 6399593.625 * (Lat * Math.PI / 180 - 0.005054622556 * (Lat * Math.PI / 180 + Math.sin(2.0 * Lat * Math.PI / 180) / 2) + 4.258201531e-05 * (3 * (Lat * Math.PI / 180 + Math.sin(2.0 * Lat * Math.PI / 180) / 2) + Math.sin(2.0 * Lat * Math.PI / 180) * Math.pow(Math.cos(Lat * Math.PI / 180), 2.0)) / 4 - 1.674057895e-07 * (5 * (3 * (Lat * Math.PI / 180 + Math.sin(2.0 * Lat * Math.PI / 180) / 2) + Math.sin(2.0 * Lat * Math.PI / 180) * Math.pow(Math.cos(Lat * Math.PI / 180), 2.0)) / 4 + Math.sin(2.0 * Lat * Math.PI / 180) * Math.pow(Math.cos(Lat * Math.PI / 180), 2.0) * Math.pow(Math.cos(Lat * Math.PI / 180), 2.0)) / 3)
        if (letter < 'M')
            northing += 10000000
        northing = Math.round(northing * 100) * 0.01
    }
}
