
package com.example.a19200088_morales_23100833_ruiz_pc01_dsm202602_loteria

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

data class Bolilla(
    val numero: Int,
    val orden: Int
)

@Composable
fun SeguimientoSorteo(
    sorteoId: String,
    jugadaUsuario: List<Int>
) {
    val db = remember { FirebaseFirestore.getInstance() }

    var estado by remember { mutableStateOf("abierto") }
    var bolillas by remember { mutableStateOf<List<Bolilla>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    // Escuchar el sorteo y las bolillas.
    DisposableEffect(sorteoId) {
        val sorteoRef = db.collection("sorteos")
            .document(sorteoId)

        val estadoListener = sorteoRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                error = "No se pudo escuchar el sorteo: ${e.message}"
            } else if (snapshot != null && snapshot.exists()) {
                estado = snapshot.getString("estado") ?: "abierto"
                error = null
            }
        }

        val bolillasListener = sorteoRef.collection("bolillas")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    error = "No se pudieron recibir las bolillas: ${e.message}"
                } else if (snapshot != null) {
                    bolillas = snapshot.documents.mapNotNull { doc ->
                        val numero = doc.getLong("numero")?.toInt()
                        val orden = doc.getLong("orden")?.toInt()

                        if (numero != null && orden != null) {
                            Bolilla(numero, orden)
                        } else {
                            null
                        }
                    }.sortedBy { it.orden }

                    error = null
                }
            }

        onDispose {
            estadoListener.remove()
            bolillasListener.remove()
        }
    }

    val numerosPublicados = bolillas.map { it.numero }
    val aciertos = jugadaUsuario.intersect(
        numerosPublicados.toSet()
    ).size

    val resultado = when {
        estado == "abierto" ->
            "Esperando el inicio del sorteo"

        estado == "en curso" ->
            "Sorteo en curso"

        estado == "finalizado" && bolillas.size < 6 ->
            "Esperando resultados"

        estado == "finalizado" && aciertos == 6 ->
            "GANADOR"

        estado == "finalizado" ->
            "SIGUE INTENTANDO"

        else -> "Estado no reconocido"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Seguimiento del sorteo",
            style = MaterialTheme.typography.headlineSmall
        )

        Text("Estado: $estado")
        Text("Bolillas publicadas: ${bolillas.size} de 6")

        Text("Bolillas sorteadas")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(bolillas, key = { it.orden }) { bolilla ->
                AssistChip(
                    onClick = {},
                    label = {
                        Text("${bolilla.numero} · ${bolilla.orden}")
                    }
                )
            }
        }

        Text("Tu jugada")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            jugadaUsuario.forEach { numero ->
                val acertado = numero in numerosPublicados

                Surface(
                    color = if (acertado) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = numero.toString(),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        Text("Aciertos: $aciertos de 6")
        Text(
            text = resultado,
            style = MaterialTheme.typography.titleLarge
        )

        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
