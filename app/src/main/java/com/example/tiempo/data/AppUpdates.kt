package com.example.tiempo.data

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** En qué situación está la app instalada respecto a lo que hay publicado en Google Play. */
sealed interface UpdateStatus {
    /** Todavía no se ha preguntado. */
    object Unknown : UpdateStatus

    /** La instalada ya es la última del canal. */
    object UpToDate : UpdateStatus

    /**
     * Hay versión nueva. [info] es lo que hace falta para lanzar la actualización, y
     * caduca: si se deja reposar hay que volver a pedirlo con [checkForUpdate].
     */
    data class Available(val versionCode: Int, val info: AppUpdateInfo) : UpdateStatus

    /**
     * No se ha podido preguntar. Lo habitual es que la app no se haya instalado desde
     * Google Play, pero también cae aquí un dispositivo sin servicios de Play o sin red.
     */
    object Unavailable : UpdateStatus
}

/**
 * Pregunta a Google Play si hay una versión más nueva que la instalada.
 *
 * OJO, y no es un fallo: esto solo responde de verdad cuando la app se instaló **desde
 * Google Play**. En un build puesto con `adb install` la API contesta que no hay nada o
 * da error de propiedad, así que [UpdateStatus.Unavailable] es el resultado esperado
 * mientras se desarrolla. Para probarlo de verdad hace falta tener la app instalada
 * desde el canal de prueba cerrada y que ahí haya un versionCode mayor ya aprobado.
 *
 * Se consulta por el tipo IMMEDIATE: la actualización la gestiona Play a pantalla
 * completa y reinicia la app al terminar. Frente al modo flexible ahorra tener que
 * escuchar el progreso de la descarga y llamar a completeUpdate() a mano, y para un
 * botón de "buscar actualizaciones" —donde el usuario acaba de pedirla— es lo que se
 * espera que pase.
 */
suspend fun checkForUpdate(context: Context): UpdateStatus =
    suspendCancellableCoroutine { continuation ->
        val manager = AppUpdateManagerFactory.create(context.applicationContext)
        manager.appUpdateInfo
            .addOnSuccessListener { info ->
                val hayNueva = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                continuation.resume(
                    if (hayNueva) {
                        UpdateStatus.Available(info.availableVersionCode(), info)
                    } else {
                        UpdateStatus.UpToDate
                    }
                )
            }
            .addOnFailureListener { continuation.resume(UpdateStatus.Unavailable) }
    }

/**
 * Arranca la actualizacion. La pantalla la pinta Google Play, no la app: descarga,
 * instala y relanza. [launcher] solo hace falta para enterarse de si el usuario la
 * cancela; el resultado no hay que tratarlo para que funcione.
 */
fun startImmediateUpdate(
    activity: Activity,
    available: UpdateStatus.Available,
    launcher: ActivityResultLauncher<IntentSenderRequest>
) {
    AppUpdateManagerFactory.create(activity).startUpdateFlowForResult(
        available.info,
        launcher,
        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
    )
}
