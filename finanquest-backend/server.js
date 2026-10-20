
require("dotenv").config();

const express = require("express");
const cors = require("cors");
const { admin, db } = require("./firebase");

const app = express();

app.disable("x-powered-by");
app.use(cors());
app.use(express.json({ limit: "16kb" }));

// COMPROBAR SERVIDOR
app.get("/health", (req, res) => {
  res.json({
    ok: true,
    servicio: "FinanQuest Backend"
  });
});

// COMPROBAR FIREBASE
app.get("/health/firebase", async (req, res) => {
  try {
    await db.collection("_conexion_backend")
      .limit(1)
      .get();

    res.json({
      ok: true,
      firebase: "Conectado correctamente"
    });
  } catch (error) {
    console.error("Error de Firebase:", error);

    res.status(500).json({
      ok: false,
      mensaje: "No se pudo conectar con Firestore"
    });
  }
});

// VERIFICAR IDENTIDAD DEL ESTUDIANTE
async function autenticar(req, res, next) {
  const cabecera = req.headers.authorization || "";
  const coincidencia = /^Bearer (\S+)$/.exec(cabecera);

  if (!coincidencia) {
    return res.status(401).json({
      ok: false,
      mensaje: "Falta el token de Firebase Authentication"
    });
  }

  try {
    req.usuarioFirebase = await admin.auth()
      .verifyIdToken(coincidencia[1], true);

    next();
  } catch (error) {
    return res.status(401).json({
      ok: false,
      mensaje: "Sesion no valida. Inicia sesion nuevamente."
    });
  }
}

// CONFIGURACION DE LA MISION
const ID_MISION = "inversionista_novato";
const CLAVE_RESPUESTAS = [1, 2, 1, 0, 2];

const XP_RECOMPENSA = 100;

// Formula provisional de niveles
const XP_POR_NIVEL = 100;

// COMPLETAR MISION INVERSIONISTA NOVATO
app.post(
  "/api/misiones/inversionista-novato/completar",
  autenticar,
  async (req, res) => {

    const { respuestas } = req.body || {};

    // Validar las respuestas recibidas
    if (
      !Array.isArray(respuestas) ||
      respuestas.length !== CLAVE_RESPUESTAS.length ||
      !respuestas.every(
        (r) => Number.isInteger(r) && r >= 0 && r <= 2
      )
    ) {
      return res.status(400).json({
        ok: false,
        mensaje: "Debes enviar cinco respuestas validas."
      });
    }

    // Calcular calificacion
    const correctas = respuestas.reduce(
      (total, respuesta, index) =>
        total + (
          respuesta === CLAVE_RESPUESTAS[index] ? 1 : 0
        ),
      0
    );

    const porcentaje = correctas * 20;
    const aprobada = correctas >= 4;

    // Obtener el UID desde el token verificado
    const uid = req.usuarioFirebase.uid;

    const usuarioRef = db.collection("usuarios").doc(uid);

    const progresoRef = usuarioRef
      .collection("progreso_misiones")
      .doc(ID_MISION);

    const recompensaRef = usuarioRef
      .collection("recompensas_otorgadas")
      .doc(ID_MISION);

    const evaluacionRef = usuarioRef
      .collection("evaluaciones_validadas")
      .doc();

    try {
      const resultado = await db.runTransaction(async (tx) => {

        // Leer los datos antes de escribir
        const usuarioSnap = await tx.get(usuarioRef);
        const recompensaSnap = await tx.get(recompensaRef);

        if (!usuarioSnap.exists) {
          const error = new Error(
            "No existe el perfil del estudiante."
          );
          error.status = 404;
          throw error;
        }

        const yaRecompensada = recompensaSnap.exists;

        let xpTotal = usuarioSnap.get("xp");

        if (!Number.isInteger(xpTotal) || xpTotal < 0) {
          const error = new Error(
            "El perfil tiene un valor de XP no valido."
          );
          error.status = 409;
          throw error;
        }

        let xpOtorgados = 0;

        // Registrar evaluacion validada
        tx.create(evaluacionRef, {
          misionId: ID_MISION,
          respuestas,
          correctas,
          totalPreguntas: CLAVE_RESPUESTAS.length,
          porcentaje,
          aprobada,
          fecha: admin.firestore.FieldValue.serverTimestamp(),
          validadaPor: "backend"
        });

        if (aprobada) {

          // Marcar mision como completada
          tx.set(
            progresoRef,
            {
              progreso: 100,
              completada: true,
              fechaCompletada:
                admin.firestore.FieldValue.serverTimestamp()
            },
            { merge: true }
          );

          // Entregar XP solamente una vez
          if (!yaRecompensada) {

            xpOtorgados = XP_RECOMPENSA;
            xpTotal += XP_RECOMPENSA;

            const nivel =
              Math.floor(xpTotal / XP_POR_NIVEL) + 1;

            tx.create(recompensaRef, {
              misionId: ID_MISION,
              xp: XP_RECOMPENSA,
              fecha:
                admin.firestore.FieldValue.serverTimestamp()
            });

            tx.update(usuarioRef, {
              xp: xpTotal,
              nivel
            });
          }
        }

        return {
          correctas,
          porcentaje,
          aprobada,
          xpOtorgados,
          xpTotal,
          recompensaYaOtorgada: yaRecompensada
        };
      });

      return res.json({
        ok: true,
        ...resultado
      });

    } catch (error) {
      console.error(
        "Error al completar la mision:",
        error
      );

      return res.status(error.status || 500).json({
        ok: false,
        mensaje: error.status
          ? error.message
          : "No se pudo procesar la evaluacion."
      });
    }
  }
);

// INICIAR SERVIDOR
const PORT = process.env.PORT || 3001;

app.listen(PORT, () => {
  console.log(
    `FinanQuest backend funcionando en puerto ${PORT}`
  );
});
