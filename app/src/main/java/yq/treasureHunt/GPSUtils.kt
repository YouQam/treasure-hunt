package yq.treasureHunt


// Get air distance in meters.
fun getAirDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
    val earthRadius = 6371000.0 //meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLng / 2) * Math.sin(dLng / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return (earthRadius * c).toFloat()
}