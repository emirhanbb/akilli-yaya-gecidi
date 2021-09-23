### GÖRME ENGELLİLER İÇİN AKILLI YAYA GEÇİDİ: KABLOSUZ SENSÖR AĞI KULLANAN BİR AKILLI ŞEHİR UYGULAMASI

# Özet
Görme engelli bireylerin yaya geçitlerinde karşıdan karşıya geçişlerini kolaylaştırmak amacıyla trafiğin yoğun olduğu noktalarda trafik ışıklarına entegre edilmiş sesli uyarı sistemlerinden faydalanılmaktadır. Öte yandan trafiğin göreceli olarak az olduğu ve trafik ışığı bulunmayan yaya geçitlerinde görme engelli bireyler için geliştirilmiş bir güvenlik çözümü bildiğimiz kadarıyla mevcut değildir. Bu motivasyonla bu proje kapsamında bir akıllı şehir tasarımı hayata geçirilecektir. Bu amaçla söz konusu yaya geçitlerinin araç trafiği olan her bir yönünün yaya geçidinden uzaklığı bilinen belirli noktalarına sensör düğümleri yerleştirilecektir. Bu düğümlerin sadece birisi master düğüm, diğerleri ise slave düğüm olarak hizmet verecektir ve tüm düğümler aynı zamanda bir kablosuz sensör ağının bir parçası olacaktır. Her bir sensör düğümü bir aracın varlığını algıladığında öncelikle hızını tespit edecek ve bu aracın ne kadar süre sonra yaya geçidine ulaşmış olacağını master düğüme iletecektir. Master düğüm ise her bir düğümden gelen bilgileri bir araya getirerek internet üzerinden buluttaki bir veri tabanına kaydedecektir. Yine bu proje kapsamında geliştirilecek bir mobil uygulama görme engelli bireylerin kullanımına sunulacaktır. Görme engelli bireyin yaya geçidine yaklaştığını GPS aracılığıyla algılayan bu uygulama buluttaki veri tabanına kaydedilen verileri kullanarak karşıya geçmenin güvenli olup olmadığına karar verecektir. Güvenli bir geçiş söz konusu ise mobil uygulama sesli bir mesaj ile görme engelli bireyi bilgilendirecektir. Bildiğimiz kadarıyla ulusal ve uluslararası literatürde proje çıktısına benzer bir ürün mevcut değildir. Bu nedenle projenin özgünlük değeri oldukça yüksektir.

![](https://i.hizliresim.com/mlqyhnx.png)
<center>Sistem Mimarisi</center>

## Mobil Uygulama Çalışma Prensibi
Sensör düğümleri tarafından buluttaki veri tabanına kaydedilen veriler mobil uygulamaya ait olan kod bloğu ile uygulama ortamına çekilmektedir. Bu
kod bloğu ile öncelikle kullanıcının mevcut konumunun koordinatları elde edilmekte ve ilgili değişkenlere atanmaktadır. Sonrasında yaya geçidi koordinatlarına haritada marker eklenmektedir. mesafe fonksiyonuna kullanıcının ve yaya geçidinin koordinatları gönderilmektedir. Bu şekilde kullanıcının yaya geçidine kaç metre yaklaştığı hesaplanmaktadır. Bu amaçla Haversine formülünden yararlanılmaktadır. Fonksiyon içerisinde bulunan 60 değeri bir derece içerisindeki dakika sayısını, 1.1515 bir deniz milinin kara mili değerini, 1.609344 ise bir milin kilometre değerini ifade etmektedir.

Eğer kullanıcının yaya geçidine olan mesafesi daha önceden belirlenmiş olan eşik değerden küçük olursa **veriCek()** fonksiyonu ile veri tabanından gelecek veriler dinlenmeye başlanmaktadır.

veriCek() isimli fonksiyon, Google Firebase üzerinde koşan Realtime Database'den (buluttaki veri tabanı) verileri çekmektedir. Realtime Database, gerçek zamanlı bir veri tabanı olduğu için her seferinde güncellenmiş verilerle işlem yapılmaktadır.

![](https://i.hizliresim.com/iad6g3l.png)
<center>Firebase Veritabanına Yazılan Veriler</center>

## Ekran Görüntüsü
![](https://i.hizliresim.com/848y6uv.png)


