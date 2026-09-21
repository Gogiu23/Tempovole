package com.example.tiempo.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UnsplashPhoto(
    val urls: UnsplashUrls,
    val user: UnsplashUser? = null,
    val links: UnsplashLinks? = null
)

@Serializable
data class UnsplashUrls(
    val regular: String,
    val full: String,
    /** Original sin recortar: sirve para pedir a imgix justo el tamaño que hace falta. */
    val raw: String? = null
)

/** Autor de la foto. Unsplash exige dar crédito con su nombre y enlace a su perfil. */
@Serializable
data class UnsplashUser(
    val name: String? = null,
    val links: UnsplashUserLinks? = null
)

@Serializable
data class UnsplashUserLinks(val html: String? = null)

@Serializable
data class UnsplashLinks(
    val html: String? = null,
    /**
     * Endpoint que hay que "pinchar" cada vez que se usa la foto. Unsplash lo exige en sus
     * normas de uso de la API: es como contabilizan las descargas para sus fotografos.
     */
    @SerialName("download_location") val downloadLocation: String? = null
)

/** Foto de fondo ya resuelta: la URL que se pinta y los datos para dar crédito al autor. */
data class BackgroundPhoto(
    val url: String,
    val authorName: String?,
    val authorUrl: String?,
    val photoUrl: String?,
    val downloadLocation: String? = null
)
