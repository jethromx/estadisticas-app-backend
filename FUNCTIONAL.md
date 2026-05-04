# Lottery API — Documentación Funcional

Este documento describe en lenguaje no técnico qué hace el sistema, para qué sirve cada funcionalidad y cómo un usuario puede sacarle provecho.

---

## ¿Qué es Lottery API?

**Lottery API** es un sistema de análisis estadístico para los juegos de pronósticos de la Lotería Nacional de México. Su objetivo es ayudarte a tomar decisiones más informadas al generar tus combinaciones de lotería, basándose en el histórico real de sorteos.

> **Aviso importante:** Los análisis son puramente estadísticos. La lotería es un juego de azar y ningún análisis garantiza resultados. El sistema te da información para jugar con mayor criterio, no para predecir el futuro.

---

## Juegos soportados

| Juego | Formato | Rango |
|---|---|---|
| **Melate** | 6 números principales + 1 adicional | 1–56 |
| **Revancha** | 6 números | 1–56 |
| **Revanchita** | 6 números | 1–56 |
| **GanaGato** | 8 posiciones (F1-F8) | 1–5 (pueden repetirse) |

---

## Funcionalidades principales

### 1. Registro e inicio de sesión

Para usar el sistema necesitas crear una cuenta. Con tu cuenta:
- Tus predicciones son **privadas** y solo tú puedes verlas
- Tienes tu **espacio personal** de análisis
- El sistema guarda **trazabilidad** de tu actividad

**Cómo hacerlo:**
1. Regístrate con usuario, email y contraseña
2. Inicia sesión → recibes un token de acceso (válido 24 horas)
3. Usa ese token en todas tus consultas

---

### 2. Sincronización de datos históricos

El sistema descarga el histórico completo de sorteos directamente desde la fuente oficial de la Lotería Nacional (archivos CSV). Esto te garantiza datos reales y actualizados.

**Cuándo sincronizar:**
- La primera vez que uses el sistema (para cargar el histórico completo)
- Antes de hacer un análisis si quieres los datos más recientes
- Puedes sincronizar un juego específico o todos a la vez

**Qué pasa al sincronizar:**
- Se descargan todos los sorteos disponibles
- Se insertan solo los sorteos nuevos (no duplica datos)
- Recibes un resumen: cuántos sorteos nuevos se encontraron

---

### 3. Análisis estadístico

Una vez con datos sincronizados, puedes consultar distintos tipos de análisis:

#### Estadísticas generales
Resumen completo del juego: total de sorteos, fechas, números más y menos frecuentes.

#### Frecuencia de números
¿Cuántas veces ha salido cada número en toda la historia? Te muestra un ranking de más a menos frecuente.

#### Números calientes y fríos
- **Calientes**: los que han salido más veces recientemente
- **Fríos**: los que llevan más tiempo sin aparecer
- Puedes filtrar por los últimos N sorteos (ventana temporal)

#### Números "debidos"
Calcula qué números han superado su intervalo promedio de aparición. Un número con "due score" mayor a 1.0 lleva más sorteos sin aparecer de lo que le correspondería según su historial.

#### Balance par/impar y alto/bajo
Muestra qué combinación de pares/impares y números altos/bajos ha ganado más veces. Por ejemplo: "4 pares + 2 impares" o "3 altos + 3 bajos".

#### Distribución de sumas
La suma de los 6 números de una combinación ganadora no es aleatoria — tiende a caer en un rango específico. El sistema te muestra ese rango óptimo histórico.

#### Pares de números frecuentes
¿Cuáles dos números salen juntos con más frecuencia en el mismo sorteo? Útil para incluir pares "calientes" en tus combinaciones.

#### Prueba chi-cuadrado
Análisis estadístico para determinar qué tan uniforme es la distribución de cada número. Te indica si algún número se desvía significativamente de lo esperado por azar.

#### Backtesting
Evalúa cómo hubieran funcionado distintas estrategias de apuesta en el pasado histórico.

#### Análisis bayesiano
Calcula la probabilidad actualizada de cada número basándose en su frecuencia reciente vs. histórica.

#### Sugerencias de combinaciones
El sistema genera combinaciones recomendadas usando múltiples metodologías estadísticas:
- **Números calientes**: basado en frecuencia histórica alta
- **Balance**: equilibrio entre pares/impares y altos/bajos
- **Bayesiano**: probabilidades actualizadas
- **Números debidos**: los que llevan más tiempo sin aparecer

---

### 4. Predicciones guardadas

Puedes guardar tus combinaciones para hacerles seguimiento en el tiempo.

**Al guardar una predicción:**
- Registras las combinaciones que vas a jugar
- El sistema guarda la fecha del último sorteo conocido al momento de guardar
- Puedes incluir con qué metodología la generaste (parámetros de generación)
- Cada predicción queda **vinculada a tu usuario** — nadie más puede verla

**Información que se guarda:**
- Etiqueta descriptiva (ej: "Melate 15 enero 2025")
- Las combinaciones de números
- El tipo de juego (Melate, Revancha, etc.)
- Los parámetros con los que la generaste (opcional)
- La fecha del sorteo más reciente cuando la guardaste

---

### 5. Análisis de precisión de predicciones

Esta es una de las funcionalidades más poderosas: después de que pasan algunos sorteos, puedes pedirle al sistema que **compare tus predicciones guardadas contra los sorteos reales que ocurrieron después**.

**Qué te muestra el análisis:**
- Cuántos sorteos se analizaron desde que guardaste la predicción
- **Por cada combinación**: cuántos números acertaste en cada sorteo
- El mejor resultado que lograste (máximo de aciertos)
- El promedio de aciertos por sorteo
- **Sugerencias de mejora** basadas en los patrones que fallaron

**Sugerencias automáticas de mejora:**
El sistema analiza por qué fallaste y te da consejos concretos. Ejemplos:
- *"Los sorteos recientes tuvieron 70% de números calientes, pero tus combinaciones solo los cubren en un 30%. Considera incluir más números frecuentes."*
- *"Tus combinaciones no tienen balance par/impar. Intenta incluir aproximadamente la mitad de números pares e impares."*
- *"El mejor resultado fue 2 aciertos. Considera incrementar la ventana de sorteos usada para el análisis."*

**Opción de sincronizar antes de analizar:**
Al pedir el análisis puedes solicitar que el sistema sincronice primero los sorteos más recientes (`syncFirst=true`), garantizando que la comparación incluya los últimos juegos disponibles.

---

### 6. Trazabilidad de uso

El sistema registra automáticamente tu actividad:
- Cuántas predicciones has guardado
- Qué análisis consultas con más frecuencia
- Cuándo sincronizas datos
- Cuántas predicciones analizas y cuáles son sus resultados

Esta información está disponible para fines de análisis personal futuro y mejora continua del servicio.

---

## Flujo típico de uso

```
1. REGISTRO       → Crear cuenta (una sola vez)
2. LOGIN          → Obtener token de acceso
3. SYNC           → Sincronizar datos históricos
4. ANÁLISIS       → Consultar estadísticas para fundamentar tu combinación
5. GUARDAR        → Guardar tu predicción antes del sorteo
6. ESPERAR        → El sorteo ocurre
7. ANALIZAR       → Comparar tu predicción con el resultado real
8. MEJORAR        → Leer las sugerencias y ajustar tu estrategia
9. Repetir desde 3
```

---

## Privacidad y aislamiento

- **Tus predicciones son tuyas**: ningún otro usuario puede ver, modificar ni eliminar tus predicciones
- **Solo tú puedes analizarlas o borrarlas**: el sistema verifica que eres el dueño antes de cualquier acción
- **Los datos de sorteos son públicos**: el histórico de la Lotería Nacional está disponible para todos los usuarios

---

## Límites y consideraciones

- Los sorteos históricos datan desde el inicio de cada juego (Melate desde 1990)
- La sincronización requiere conexión a internet para descargar desde la fuente oficial
- El token de acceso dura 24 horas; deberás volver a iniciar sesión después
- Las sugerencias estadísticas se basan en patrones históricos y **no garantizan resultados**
