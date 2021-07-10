package com.hllbr.kotlintravelbook

import java.io.Serializable

class Place (var address: String?,var latitude : Double?,var longitude : Double?):Serializable{//Location ismini kullanmadım bunun sebebi bu isimde sınıf yada sınıfların bulunma ihtimalidir.Karşılıklığa yol açmaması için böyle bir tanımlama yoluna gittim.

}