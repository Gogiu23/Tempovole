# Respuestas para Play Console

Basado en el código real de la app (revisado: solo usa `INTERNET`, `ACCESS_NETWORK_STATE`
y `POST_NOTIFICATIONS`; no hay SDKs de analítica, anuncios ni crash-reporting).

---

## 1. Categoría de la app

- **Categoría**: Tiempo (Weather)
- **Tipo**: App (no juego)

---

## 2. Cuestionario de clasificación de contenido (IARC)

Selecciona la categoría **"Utilidad, productividad, comunicación u otro"** (no es un
juego ni tiene redes sociales). Luego, en las preguntas de contenido, la respuesta es
**"No"** a todas:

| Pregunta | Respuesta |
|---|---|
| ¿Violencia? | No |
| ¿Contenido sexual o desnudos? | No |
| ¿Lenguaje soez? | No |
| ¿Referencias a drogas, alcohol o tabaco? | No |
| ¿Juego de apuestas (real o simulado)? | No |
| ¿Terror / contenido inquietante? | No |
| ¿Permite a los usuarios interactuar entre sí, compartir contenido, chatear o compartir su ubicación con otros usuarios? | No |
| ¿Comparte la ubicación del usuario con otros usuarios de la app? | No |
| ¿Permite compras dentro de la app? | No |

Con estas respuestas, la app debería obtener la clasificación más baja en todas las
regiones (PEGI 3, ESRB Everyone, USK 0, etc.). Nota: la pregunta de "compartir
ubicación" aquí se refiere a compartirla **con otros usuarios** (función social) — no
aplica al hecho de que la app envíe coordenadas a Open-Meteo para mostrar el tiempo;
eso se declara aparte, en el formulario de Seguridad de los datos (abajo).

---

## 3. Formulario "Seguridad de los datos" (Data safety)

**¿Tu app recoge o comparte alguno de los tipos de datos de usuario requeridos?** → **Sí**

### Ubicación

- **Tipo de dato**: Ubicación aproximada
  *(la ciudad que el usuario busca/elige en Ajustes — la app no usa GPS ni ubicación
  precisa del dispositivo en ningún momento)*
- **¿Se recoge?** No queda almacenada en ningún servidor propio (la app no tiene
  backend). Márcalo según lo que ofrezca el formulario: si te obliga a elegir, usa
  **"Compartida"**, no "Recogida" — Tiempo no guarda ni retiene el dato, solo lo
  reenvía para obtener la respuesta.
- **¿Se comparte?** Sí, con **Open-Meteo** (previsión, calidad del aire/polen,
  histórico, marina, ríos, clima, ensemble) — todas ellas llamadas independientes al
  mismo proveedor gratuito, bajo distintos subdominios de `open-meteo.com`.
- **Finalidad**: Funcionalidad de la app (App functionality) — es imprescindible para
  mostrar el tiempo, no es opcional ni se usa para publicidad ni perfilado.
- **¿Cifrado en tránsito?** Sí (HTTPS en todas las llamadas).
- **¿El usuario puede pedir que se borre?** No aplica un mecanismo dedicado porque no
  hay cuenta ni servidor propio reteniendo el dato; el usuario lo controla localmente
  (cambiarlo o borrarlo en Ajustes, o desinstalar la app).

### El resto de categorías del formulario (info personal, financiera, salud,
mensajes, contactos, calendario, fotos/vídeos del usuario, archivos, navegación web,
identificadores de dispositivo o publicidad, datos de rendimiento/diagnóstico)

→ **Ninguna se recoge ni se comparte.** Todas se marcan como "No" / se dejan sin
seleccionar.

*(La foto de fondo funciona al revés: la app **descarga** una imagen de Unsplash, no
sube ninguna del usuario. El único dato enviado a Unsplash es un término de búsqueda
fijo definido en la app — "landscape,nature,sky" — no es información personal.)*

### Prácticas de seguridad

- **¿Los datos se cifran en tránsito?** Sí.
- **¿Puedes confirmar que sigues las prácticas del Play Families Policy / Data
  safety?** Sí, si aplica la pregunta.
- **Enlace a la política de privacidad**: pega aquí la URL del artifact que
  publicamos (política de privacidad + términos de uso).

---

## Notas

- Google actualiza el wording exacto de estas preguntas de vez en cuando — si alguna
  pregunta no coincide literalmente con lo de aquí, la respuesta correcta se deduce
  igual a partir de lo descrito arriba (qué se envía, a quién, y para qué).
- Si en el futuro añades analítica, anuncios, o un backend propio, este documento
  quedará desactualizado y habrá que revisar el formulario de nuevo.
