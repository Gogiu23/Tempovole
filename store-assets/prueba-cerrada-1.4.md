# Prueba cerrada — versión 1.4

Guion para subir la 1.4 al canal de prueba cerrada. Todo lo que aquí se pide pegar
está ya generado en esta carpeta. Las respuestas de los formularios están en
`play-console-answers.md`.

## El artefacto

- **Fichero**: `app/build/outputs/bundle/release/app-release.aab` (16 MB)
- **versionCode**: 6 · **versionName**: 1.4
  (el 4 y el 5 ya estan subidos a Prueba cerrada - Alpha, los dos como 1.3, y Play no
  permite reutilizar un versionCode: de ahi el error de version si se sube un .aab con
  el 5. El 5 tuvo lanzamiento completo en el canal cerrado el 30 sept 2026.)
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
4. **Novedades de esta versión**: pega el contenido de `novedades-1.4-<idioma>.txt`
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

## Formularios que NO cambian en la 1.4

Las respuestas siguen siendo las de `play-console-answers.md`: la 1.4 no añade
permisos (siguen `INTERNET`, `ACCESS_NETWORK_STATE` y `POST_NOTIFICATIONS`) ni nuevos
datos recogidos o compartidos, así que ni la clasificación de contenido ni Seguridad
de los datos necesitan rehacerse.

- Política de privacidad: https://gogiu23.github.io/Tempovole/privacidad.html

## Pendiente, si quieres

- **Ficha traducida**: la app ya habla español, inglés, italiano y ruso, pero la ficha
  de Play solo está en español. Faltaría traducir `description-full` y
  `description-short` para las otras tres fichas de idioma.
