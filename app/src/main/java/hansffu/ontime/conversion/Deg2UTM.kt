package hansffu.ontime.conversion

class Deg2UTM(Lat: Double, Lon: Double) {
    var easting: Double
        private set
    var northing: Double
        private set
    val zone: Int = Math.floor(Lon / 6 + 31).toInt()

    val letter: Char = when {
        Lat < -72 -> 'C'
        Lat < -64 -> 'D'
        Lat < -56 -> 'E'
        Lat < -48 -> 'F'
        Lat < -40 -> 'G'
        Lat < -32 -> 'H'
        Lat < -24 -> 'J'
        Lat < -16 -> 'K'
        Lat < -8 -> 'L'
        Lat < 0 -> 'M'
        Lat < 8 -> 'N'
        Lat < 16 -> 'P'
        Lat < 24 -> 'Q'
        Lat < 32 -> 'R'
        Lat < 40 -> 'S'
        Lat < 48 -> 'T'
        Lat < 56 -> 'U'
        Lat < 64 -> 'V'
        Lat < 72 -> 'W'
        else -> 'X'
    }

    init {
        easting = 0.5 * Math.log((1 + Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180)) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180))) * 0.9996 * 6399593.62 / Math.pow(1 + Math.pow(0.0820944379, 2.0) * Math.pow(Math.cos(Lat * Math.PI / 180), 2.0), 0.5) * (1 + Math.pow(0.0820944379, 2.0) / 2 * Math.pow(0.5 * Math.log((1 + Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180)) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180))), 2.0) * Math.pow(Math.cos(Lat * Math.PI / 180), 2.0) / 3) + 500000
        easting = Math.round(easting * 100) * 0.01


        northing = (Math.atan(Math.tan(Lat * Math.PI / 180) / Math.cos(Lon * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180)) - Lat * Math.PI / 180) * 0.9996 * 6399593.625 / Math.sqrt(1 + 0.006739496742 * Math.pow(Math.cos(Lat * Math.PI / 180), 2.0)) * (1 + 0.006739496742 / 2 * Math.pow(0.5 * Math.log((1 + Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180)) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180))), 2.0) * Math.pow(Math.cos(Lat * Math.PI / 180), 2.0)) + 0.9996 * 6399593.625 * (Lat * Math.PI / 180 - 0.005054622556 * (Lat * Math.PI / 180 + Math.sin(2.0 * Lat * Math.PI / 180) / 2) + 4.258201531e-05 * (3 * (Lat * Math.PI / 180 + Math.sin(2.0 * Lat * Math.PI / 180) / 2) + Math.sin(2.0 * Lat * Math.PI / 180) * Math.pow(Math.cos(Lat * Math.PI / 180), 2.0)) / 4 - 1.674057895e-07 * (5 * (3 * (Lat * Math.PI / 180 + Math.sin(2.0 * Lat * Math.PI / 180) / 2) + Math.sin(2.0 * Lat * Math.PI / 180) * Math.pow(Math.cos(Lat * Math.PI / 180), 2.0)) / 4 + Math.sin(2.0 * Lat * Math.PI / 180) * Math.pow(Math.cos(Lat * Math.PI / 180), 2.0) * Math.pow(Math.cos(Lat * Math.PI / 180), 2.0)) / 3)
        if (letter < 'M')
            northing += 10000000
        northing = Math.round(northing * 100) * 0.01
    }
}
