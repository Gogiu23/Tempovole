# Prueba cerrada

Guion de subida al canal de prueba cerrada. Vale para cualquier versión: lo único
que cambia entre una y otra es el `versionCode` y el fichero de novedades.

Todo lo que aquí se pide pegar está ya generado en esta carpeta. Las respuestas de
los formularios están en `play-console-answers.md`.

## El artefacto

- **Fichero**: `app/build/outputs/bundle/release/app-release.aab` (16 MB)
- **versionCode**: 7 · **versionName**: 1.5
  (ya estan subidos el 4, el 5 y el 6. Un versionCode NO se puede reutilizar: si se sube
  un .aab con uno ya usado, la consola lo rechaza con un error de version. Por eso cada
  release sube el numero, aunque la anterior no llegara a publicarse.)
- Firmado con `tiempo-release.jks` (el `signingConfig release` de
  `app/build.gradle.kts`), no con la clave de debug.
- Se regenera con:
  `JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-21.jdk/Contents/Home ./gradlew bundleRelease`
  (con el JDK por defecto del sistema, el 27, Gradle 8.9 falla con un error `27`).

Si se vuelve a tocar código hay que **reconstruir el .aab**: el que haya en `outputs/`
es de la última compilación, no del último commit.

## Pasos en Play Console

1. **Pruebas → Prueba cerrada**. Si no existe ningún canal, créalo; nombre sugerido:
   `Beta cerrada`.
2. **Testers**: lista de correos de Google o un grupo de Google. Para una cuenta de
   desarrollador personal, Play exige **12 testers que sigan dentro del canal 14 días
   seguidos** antes de permitir el paso a producción, así que conviene meterlos todos
   de golpe el primer día — el contador se reinicia si alguno sale.
3. **Crear versión** → sube `app-release.aab`.
4. **Novedades de esta versión**: pega el contenido de `novedades-<version>-<idioma>.txt`
   (uno por ficha de idioma; los cuatro están por debajo del límite de 500 caracteres).
5. **Revisar y lanzar**.

## Ficha de la tienda

Solo hace falta tocarla si quieres que refleje la 1.4:

| Campo | Fichero |
|---|---|
| Descripción completa | `description-full.txt` (lleva ya la sección EN TU IDIOMA) |
| Descripción corta | `description-short.txt` |
| Capturas (teléfono) | `screenshot-01..06-*.png`, 1280×2540 |
| Gráfico de funciones | `feature-graphic.png` |
| Icono | `icon-512.png` |

Las capturas 05 (rejilla de datos) y 06 (selector de idioma) son nuevas de esta versión.

## Formularios que normalmente NO hay que rehacer

Las respuestas siguen siendo las de `play-console-answers.md` mientras una versión no
añada permisos (hoy: `INTERNET`, `ACCESS_NETWORK_STATE` y `POST_NOTIFICATIONS`) ni
datos nuevos recogidos o compartidos. Si alguna vez se añade un destinatario nuevo
—por ejemplo un proveedor de IA— hay que rehacer Seguridad de los datos y actualizar
la política de privacidad.

- Política de privacidad: https://gogiu23.github.io/Tempovole/privacidad.html

## Pendiente, si quieres

- **Ficha traducida**: la app ya habla español, inglés, italiano y ruso, pero la ficha
  de Play solo está en español. Faltaría traducir `description-full` y
  `description-short` para las otras tres fichas de idioma.

- **Activar R8 cuando acabe la prueba cerrada.** El explorador de app bundles marca
  "Optimización de código DEX: Baja" y "Porcentaje de ofuscación: 1%" porque el build
  de release lleva `isMinifyEnabled = false`; ese 1% viene de librerías que ya llegan
  ofuscadas, el código propio va entero y legible. La línea `proguardFiles(...)` está
  declarada pero no se aplica mientras minify esté en false, y `proguard-rules.pro`
  está vacío.

  Qué se gana: entre 1 y 3 MB. El grueso de los 15,7 MB son recursos (5,6 MB de WebP
  animados en `res/raw` más drawables) y R8 solo toca el dex. De paso desaparece la
  advertencia de "no hay archivo de desofuscación" que sale en cada release, porque
  AGP sube el mapeo junto al bundle.

  Lo que NO se gana: ocultar la clave de Unsplash. Vive en `BuildConfig` como constante
  de texto y R8 no cifra cadenas.

  Riesgo: las librerías que usan reflexión pueden romperse solo en release, que es el
  build que no se prueba a diario. Candidatas aquí: kotlinx.serialization, Retrofit,
  Glance y Play Core. Todas traen reglas propias, pero hay que comprobarlo.

  Cómo hacerlo sin sustos:
  1. Una release dedicada, sin ningún otro cambio dentro, para que si algo peta se
     sepa qué fue.
  2. `./gradlew installRelease` en un móvil real y recorrer todas las pantallas
     (hoy, semana, día, detalle, ajustes, idioma, qué me pongo, widget y el morning
     report) ANTES de subir nada.
  3. Valorar también `isShrinkResources = true`, con cuidado: elimina recursos que
     solo se referencian de forma dinámica.

- **AGP 8.7.3 → 9.0**: el mismo panel de Play lo sugiere. Es un salto mayor y va
  aparte, nunca en la misma release que lo de R8.
