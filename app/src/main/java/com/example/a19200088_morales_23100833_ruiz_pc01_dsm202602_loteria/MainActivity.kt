package com.example.a19200088_morales_23100833_ruiz_pc01_dsm202602_loteria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AutenticacionApp()
                }
            }
        }
    }
}

@Composable
fun AutenticacionApp() {
    val auth = remember { FirebaseAuth.getInstance() }

    var pantalla by rememberSaveable {
        mutableStateOf(
            if (auth.currentUser != null) "seguimiento" else "login"
        )
    }

    when (pantalla) {
        "registro" -> PantallaRegistro(
            auth = auth,
            alVolverLogin = { pantalla = "login" },
            alRegistrar = { pantalla = "seguimiento" }
        )

        "seguimiento" -> SeguimientoSorteo(
            sorteoId = "sorteo1",
            jugadaUsuario = listOf(5, 12, 18, 24, 33, 42)
        )

        else -> PantallaLogin(
            auth = auth,
            alIrRegistro = { pantalla = "registro" },
            alIniciarSesion = { pantalla = "seguimiento" }
        )
    }
}

@Composable
fun PantallaRegistro(
    auth: FirebaseAuth,
    alVolverLogin: () -> Unit,
    alRegistrar: () -> Unit
) {
    var correo by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var confirmarContrasena by rememberSaveable { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Crear cuenta",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Regístrate para participar en el sorteo")

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmarContrasena,
            onValueChange = { confirmarContrasena = it },
            label = { Text("Confirmar contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (mensaje.isNotBlank()) {
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                mensaje = ""

                val email = correo.trim()

                when {
                    email.isBlank() ||
                            contrasena.isBlank() ||
                            confirmarContrasena.isBlank() -> {
                        mensaje = "Completa todos los campos."
                    }

                    contrasena.length < 6 -> {
                        mensaje = "La contraseña debe tener al menos 6 caracteres."
                    }

                    contrasena != confirmarContrasena -> {
                        mensaje = "Las contraseñas no coinciden."
                    }

                    else -> {
                        cargando = true

                        auth.createUserWithEmailAndPassword(
                            email,
                            contrasena
                        ).addOnCompleteListener { tarea ->
                            cargando = false

                            if (tarea.isSuccessful) {
                                alRegistrar()
                            } else {
                                mensaje = tarea.exception?.localizedMessage
                                    ?: "No se pudo crear la cuenta."
                            }
                        }
                    }
                }
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (cargando) {
                CircularProgressIndicator()
            } else {
                Text("Crear cuenta")
            }
        }

        TextButton(
            onClick = alVolverLogin,
            enabled = !cargando
        ) {
            Text("¿Ya tienes una cuenta? Inicia sesión")
        }
    }
}

@Composable
fun PantallaLogin(
    auth: FirebaseAuth,
    alIrRegistro: () -> Unit,
    alIniciarSesion: () -> Unit
) {
    var correo by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Lotería",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Inicia sesión para continuar")

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (mensaje.isNotBlank()) {
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                mensaje = ""

                val email = correo.trim()

                if (email.isBlank() || contrasena.isBlank()) {
                    mensaje = "Ingresa tu correo y contraseña."
                } else {
                    cargando = true

                    auth.signInWithEmailAndPassword(
                        email,
                        contrasena
                    ).addOnCompleteListener { tarea ->
                        cargando = false

                        if (tarea.isSuccessful) {
                            alIniciarSesion()
                        } else {
                            mensaje = tarea.exception?.localizedMessage
                                ?: "No se pudo iniciar sesión."
                        }
                    }
                }
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (cargando) {
                CircularProgressIndicator()
            } else {
                Text("Iniciar sesión")
            }
        }

        TextButton(
            onClick = alIrRegistro,
            enabled = !cargando
        ) {
            Text("¿No tienes una cuenta? Regístrate")
        }
    }
}

@Composable
fun PantallaInicio(
    correo: String,
    alCerrarSesion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "¡Bienvenido!",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Sesión iniciada con:")

        Text(correo)

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = alCerrarSesion) {
            Text("Cerrar sesión")
        }
    }
}
