require("dotenv").config();

const express = require("express");
const cors = require("cors");
const { admin, db } = require("./firebase");

const app = express();

app.disable("x-powered-by");
app.use(cors());
app.use(express.json({ limit: "16kb" }));

// ============================================
// CONFIGURACION GENERAL
// ============================================

const XP_POR_NIVEL = 100;

const MISION_INVERSIONES = "inversionista_novato";
const MISION_AHORRO = "primer_ahorro_semanal";

const CLAVE_RESPUESTAS = [1, 2, 1, 0, 2];

const XP_INVERSIONES = 100;
const XP_AHORRO = 250;

// ============================================
// COMPROBAR SERVIDOR
// ============================================

app.get("/health", (req, res) => {
  res.json({
    ok: true,
    servicio: "FinanQuest Backend"
  });
});

// ============================================
// COMPROBAR FIREBASE
// ============================================

app.get("/health/firebase", async (req, res) => {
  try {
    await db.collection("_conexion_backend")
      .limit(1)
      .get();

    return res.json({
      ok: true,
      firebase: "Conectado correctamente"
    });

  } catch (error) {
    console.error("Error de Firebase:", error);

    return res.status(500).json({
      ok: false,
      mensaje: "No se pudo conectar con Firestore"
    });
  }
});

// ============================================
// AUTENTICAR ESTUDIANTE
// ============================================

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

// ============================================
// CONSULTAR ESTADO DE MISIONES Y RECOMPENSAS
// ============================================

app.get("/api/misiones/estado", autenticar, async (req, res) => {
  try {
    const uid = req.usuarioFirebase.uid;

    const usuarioRef = db.collection("usuarios").doc(uid);

    // Recalcular misiones antes de devolver el estado, sin usar porcentajes viejos.
    await sincronizarMisionesUsuario(usuarioRef);

    const [usuarioSnap, progresosSnap, recompensasSnap] =
      await Promise.all([
        usuarioRef.get(),
        usuarioRef.collection("progreso_misiones").get(),
        usuarioRef.collection("recompensas_otorgadas").get()
      ]);

    if (!usuarioSnap.exists) {
      return res.status(404).json({
        ok: false,
        mensaje: "No existe el perfil del estudiante."
      });
    }

    const progresos = {};

    progresosSnap.forEach((documento) => {
      progresos[documento.id] = documento.data();
    });

    const recompensas = {};

    recompensasSnap.forEach((documento) => {
      const datos = documento.data();

      recompensas[documento.id] = {
        xp: datos.xp || 0,
        otorgada: true
      };
    });

    return res.json({
      ok: true,
      xp: usuarioSnap.get("xp") ?? 0,
      nivel: usuarioSnap.get("nivel") ?? 1,
      progresos,
      recompensas
    });

  } catch (error) {
    console.error("Error consultando misiones:", error);

    return res.status(500).json({
      ok: false,
      mensaje: "No se pudo consultar el estado de las misiones."
    });
  }
});

// ============================================
// COMPLETAR INVERSIONISTA NOVATO
// ============================================

app.post(
  "/api/misiones/inversionista-novato/completar",
  autenticar,
  async (req, res) => {

    const { respuestas } = req.body || {};

    if (
      !Array.isArray(respuestas) ||
      respuestas.length !== CLAVE_RESPUESTAS.length ||
      !respuestas.every(
        (respuesta) =>
          Number.isInteger(respuesta) &&
          respuesta >= 0 &&
          respuesta <= 2
      )
    ) {
      return res.status(400).json({
        ok: false,
        mensaje: "Debes enviar cinco respuestas validas."
      });
    }

    const correctas = respuestas.reduce(
      (total, respuesta, index) =>
        total +
        (
          respuesta === CLAVE_RESPUESTAS[index]
            ? 1
            : 0
        ),
      0
    );

    const porcentaje = correctas * 20;
    const aprobada = correctas >= 4;

    const uid = req.usuarioFirebase.uid;

    const usuarioRef = db.collection("usuarios").doc(uid);

    const progresoRef = usuarioRef
      .collection("progreso_misiones")
      .doc(MISION_INVERSIONES);

    const recompensaRef = usuarioRef
      .collection("recompensas_otorgadas")
      .doc(MISION_INVERSIONES);

    const evaluacionRef = usuarioRef
      .collection("evaluaciones_validadas")
      .doc();

    try {
      const resultado = await db.runTransaction(async (tx) => {

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

        // Registrar evaluacion validada por el backend
        tx.create(evaluacionRef, {
          misionId: MISION_INVERSIONES,
          respuestas,
          correctas,
          totalPreguntas: CLAVE_RESPUESTAS.length,
          porcentaje,
          aprobada,
          fecha:
            admin.firestore.FieldValue.serverTimestamp(),
          validadaPor: "backend"
        });

        if (aprobada) {

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

          // La recompensa solo se entrega una vez
          if (!yaRecompensada) {

            xpOtorgados = XP_INVERSIONES;
            xpTotal += XP_INVERSIONES;

            const nivel =
              Math.floor(xpTotal / XP_POR_NIVEL) + 1;

            tx.create(recompensaRef, {
              misionId: MISION_INVERSIONES,
              xp: XP_INVERSIONES,
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
        "Error al completar Inversionista Novato:",
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

// ============================================
// PRIMER AHORRO SEMANAL (HORA EL SALVADOR)
// ============================================

function inicioSemanaElSalvador() {
  // El Salvador utiliza UTC-06:00 todo el anio.
  const ahoraLocal = new Date(Date.now() - 6 * 60 * 60 * 1000);
  const anio = ahoraLocal.getUTCFullYear();
  const mes = ahoraLocal.getUTCMonth();
  const dia = ahoraLocal.getUTCDate();
  const diaSemana = ahoraLocal.getUTCDay();
  const diasDesdeLunes = (diaSemana + 6) % 7;
  return Date.UTC(anio, mes, dia - diasDesdeLunes, 6, 0, 0);
}

async function calcularAhorroSemanal(usuarioRef) {
  const inicio = inicioSemanaElSalvador();
  const fin = inicio + 7 * 24 * 60 * 60 * 1000;
  const [cuentasSnap, movimientosSnap] = await Promise.all([
    usuarioRef.collection("cuentas").get(),
    usuarioRef.collection("movimientos")
      .where("registradoEn", ">=", inicio)
      .where("registradoEn", "<", fin)
      .get()
  ]);

  const cuentasAhorro = new Set(
    cuentasSnap.docs
      .filter((doc) => doc.get("tipo") === "Ahorro")
      .map((doc) => doc.id)
  );

  let centavos = 0;
  for (const doc of movimientosSnap.docs) {
    const m = doc.data();
    if (!cuentasAhorro.has(m.cuentaId)) continue;
    if (!Number.isFinite(m.monto) || m.monto <= 0) continue;
    if (m.tipo !== "Ingreso" && m.tipo !== "Gasto") continue;
    const valor = Math.round(m.monto * 100);
    centavos += m.tipo === "Ingreso" ? valor : -valor;
  }

  return {
    ahorroCentavos: Math.max(0, centavos),
    inicioSemana: inicio
  };
}

async function actualizarMisionAhorro(usuarioRef) {
  const { ahorroCentavos, inicioSemana } =
    await calcularAhorroSemanal(usuarioRef);
  const progresoRef = usuarioRef.collection("progreso_misiones")
    .doc(MISION_AHORRO);
  const recompensaRef = usuarioRef.collection("recompensas_otorgadas")
    .doc(MISION_AHORRO);
  const completada = ahorroCentavos >= 500;
  const progreso = Math.min(100, Math.floor(ahorroCentavos / 5));

  return db.runTransaction(async (tx) => {
    const [usuarioSnap, recompensaSnap] = await Promise.all([
      tx.get(usuarioRef), tx.get(recompensaRef)
    ]);
    if (!usuarioSnap.exists) {
      const error = new Error("No existe el perfil del estudiante.");
      error.status = 404;
      throw error;
    }
    const xpActual = usuarioSnap.get("xp");
    if (!Number.isInteger(xpActual) || xpActual < 0) {
      const error = new Error("El perfil tiene XP no validos.");
      error.status = 409;
      throw error;
    }

    const yaRecompensada = recompensaSnap.exists;
    const xpOtorgados = completada && !yaRecompensada ? XP_AHORRO : 0;
    const xpTotal = xpActual + xpOtorgados;

    // Una recompensa obtenida se mantiene completada, aunque
    // posteriormente se retire dinero de la cuenta.
    tx.set(progresoRef, {
      progreso: yaRecompensada || completada ? 100 : progreso,
      completada: yaRecompensada || completada,
      ahorroSemanal: ahorroCentavos / 100,
      objetivo: 5,
      inicioSemana,
      ultimaActualizacion: admin.firestore.FieldValue.serverTimestamp()
    }, { merge: true });

    if (xpOtorgados > 0) {
      tx.create(recompensaRef, {
        misionId: MISION_AHORRO,
        xp: XP_AHORRO,
        fecha: admin.firestore.FieldValue.serverTimestamp(),
        validadaPor: "backend"
      });
      tx.update(usuarioRef, {
        xp: xpTotal,
        nivel: Math.floor(xpTotal / XP_POR_NIVEL) + 1
      });
    }

    return {
      id: MISION_AHORRO,
      titulo: "Primer Ahorro Semanal",
      progreso: yaRecompensada || completada ? 100 : progreso,
      completada: yaRecompensada || completada,
      ahorroSemanal: ahorroCentavos / 100,
      xpOtorgados,
      xpTotal,
      recompensaYaOtorgada: yaRecompensada
    };
  });
}

app.post(
  "/api/misiones/primer-ahorro-semanal/completar",
  autenticar,
  async (req, res) => {
    try {
      const usuarioRef = db.collection("usuarios")
        .doc(req.usuarioFirebase.uid);
      const resultado = await actualizarMisionAhorro(usuarioRef);
      return res.json({ ok: true, ...resultado });
    } catch (error) {
      console.error("Error comprobando ahorro semanal:", error);
      return res.status(error.status || 500).json({
        ok: false,
        mensaje: error.status ? error.message :
          "No se pudo comprobar la mision de ahorro."
      });
    }
  }
);

/* ============================================
   MISIONES AUTOMATICAS POR MOVIMIENTOS
   ============================================ */

const MISION_PRIMER_MOVIMIENTO = "primer_movimiento";
const MISION_ORGANIZADOR = "organizador_financiero";

const XP_PRIMER_MOVIMIENTO = 50;
const XP_ORGANIZADOR = 100;

// Requisitos de cada mision
const MISIONES_MOVIMIENTOS = [
  {
    id: MISION_PRIMER_MOVIMIENTO,
    titulo: "Primer Movimiento",
    cantidad: 1,
    xp: XP_PRIMER_MOVIMIENTO
  },
  {
    id: MISION_ORGANIZADOR,
    titulo: "Organizador Financiero",
    cantidad: 5,
    xp: XP_ORGANIZADOR
  }
];

// ============================================
// VERIFICAR Y ACTUALIZAR UNA MISION
// ============================================

async function actualizarMisionMovimiento(
  usuarioRef,
  mision,
  cantidadMovimientos
) {
  const progresoRef = usuarioRef
    .collection("progreso_misiones")
    .doc(mision.id);

  const recompensaRef = usuarioRef
    .collection("recompensas_otorgadas")
    .doc(mision.id);

  const progreso = Math.min(
    100,
    Math.floor(
      (cantidadMovimientos / mision.cantidad) * 100
    )
  );

  const completada =
    cantidadMovimientos >= mision.cantidad;

  const resultado = await db.runTransaction(
    async (tx) => {
      const usuarioSnap = await tx.get(usuarioRef);
      const recompensaSnap = await tx.get(recompensaRef);

      if (!usuarioSnap.exists) {
        const error = new Error(
          "No existe el perfil del estudiante."
        );
        error.status = 404;
        throw error;
      }

      const xpActual = usuarioSnap.get("xp");

      if (
        !Number.isInteger(xpActual) ||
        xpActual < 0
      ) {
        const error = new Error(
          "El perfil tiene un valor de XP no valido."
        );
        error.status = 409;
        throw error;
      }

      const recompensaYaOtorgada =
        recompensaSnap.exists;

      let xpOtorgados = 0;
      let xpTotal = xpActual;

      // Actualizar el porcentaje calculado
      // usando movimientos reales de Firestore.
      tx.set(
        progresoRef,
        {
          progreso: recompensaYaOtorgada ? 100 : progreso,
          completada: recompensaYaOtorgada || completada,
          cantidadActual: cantidadMovimientos,
          cantidadObjetivo: mision.cantidad,
          ultimaActualizacion:
            admin.firestore.FieldValue.serverTimestamp()
        },
        { merge: true }
      );

      // Solo otorgar XP si cumple el requisito
      // y no existe una recompensa anterior.
      if (completada && !recompensaYaOtorgada) {
        xpOtorgados = mision.xp;
        xpTotal += mision.xp;

        const nivel =
          Math.floor(xpTotal / XP_POR_NIVEL) + 1;

        tx.create(recompensaRef, {
          misionId: mision.id,
          xp: mision.xp,
          fecha:
            admin.firestore.FieldValue.serverTimestamp(),
          validadaPor: "backend"
        });

        tx.update(usuarioRef, {
          xp: xpTotal,
          nivel
        });
      }

      return {
        id: mision.id,
        titulo: mision.titulo,
        cantidadActual: cantidadMovimientos,
        cantidadObjetivo: mision.cantidad,
        progreso: recompensaYaOtorgada ? 100 : progreso,
        completada: recompensaYaOtorgada || completada,
        xpOtorgados,
        xpTotal,
        recompensaYaOtorgada
      };
    }
  );

  return resultado;
}

// ============================================
// SINCRONIZACION CENTRAL DE MISIONES
// ============================================
async function sincronizarMisionesUsuario(usuarioRef) {
  const movimientosSnap = await usuarioRef.collection("movimientos").get();
  const cantidadMovimientos = movimientosSnap.docs.filter((documento) => {
    const datos = documento.data();
    return (datos.tipo === "Ingreso" || datos.tipo === "Gasto") &&
      typeof datos.monto === "number" &&
      Number.isFinite(datos.monto) && datos.monto > 0;
  }).length;

  const resultados = [];
  for (const mision of MISIONES_MOVIMIENTOS) {
    resultados.push(await actualizarMisionMovimiento(
      usuarioRef, mision, cantidadMovimientos
    ));
  }
  resultados.push(await actualizarMisionAhorro(usuarioRef));
  return {
    cantidadMovimientos,
    misiones: resultados,
    xpOtorgadosTotal: resultados.reduce(
      (total, mision) => total + mision.xpOtorgados, 0
    )
  };
}

// ============================================
// SINCRONIZAR MISIONES DE MOVIMIENTOS
// ============================================

app.post("/api/misiones/movimientos/sincronizar", autenticar, async (req, res) => {
  try {
    const usuarioRef = db.collection("usuarios").doc(req.usuarioFirebase.uid);
    const resultado = await sincronizarMisionesUsuario(usuarioRef);
    return res.json({ ok: true, mensaje: "Misiones sincronizadas.", ...resultado });
  } catch (error) {
    console.error("Error sincronizando misiones:", error);
    return res.status(error.status || 500).json({
      ok: false,
      mensaje: error.status ? error.message : "No se pudieron sincronizar las misiones."
    });
  }
});

app.post("/api/movimientos/crear", autenticar, async (req, res) => {
  try {
    const uid = req.usuarioFirebase.uid;

    const {
      cuentaId,
      tipo,
      monto,
      categoria,
      fecha,
      nota,
      comercio
    } = req.body || {};

    if (
      typeof cuentaId !== "string" ||
      !/^[A-Za-z0-9]{20}$/.test(cuentaId) ||
      !["Ingreso", "Gasto"].includes(tipo) ||
      typeof monto !== "number" ||
      !Number.isFinite(monto) ||
      monto <= 0 ||
      monto > 1_000_000_000 ||
      typeof categoria !== "string" ||
      !categoria.trim() ||
      categoria.length > 100 ||
      !Number.isSafeInteger(fecha) ||
      fecha <= 0 ||
      fecha > Date.now() + 86400000 ||
      (nota !== undefined &&
        (typeof nota !== "string" || nota.length > 160)) ||
      (comercio !== undefined &&
        (typeof comercio !== "string" || comercio.length > 160))
    ) {
      return res.status(400).json({
        ok: false,
        mensaje: "Datos del movimiento no validos."
      });
    }

    const usuarioRef = db.collection("usuarios").doc(uid);
    const cuentaRef = usuarioRef.collection("cuentas").doc(cuentaId);
    const movimientoRef = usuarioRef.collection("movimientos").doc();

    const resultado = await db.runTransaction(async (tx) => {
      const [usuarioSnap, cuentaSnap] = await Promise.all([
        tx.get(usuarioRef),
        tx.get(cuentaRef)
      ]);

      if (!usuarioSnap.exists || !cuentaSnap.exists) {
        const error = new Error("Usuario o cuenta inexistente.");
        error.status = 404;
        throw error;
      }

      if (cuentaSnap.get("activa") === false) {
        const error = new Error("La cuenta esta inactiva.");
        error.status = 409;
        throw error;
      }

      const saldoActual = cuentaSnap.get("saldo");

      if (
        typeof saldoActual !== "number" ||
        !Number.isFinite(saldoActual)
      ) {
        const error = new Error("Saldo de cuenta no valido.");
        error.status = 409;
        throw error;
      }

      const nuevoSaldo =
        saldoActual + (tipo === "Ingreso" ? monto : -monto);

      if (!Number.isFinite(nuevoSaldo)) {
        const error = new Error("Saldo resultante no valido.");
        error.status = 409;
        throw error;
      }

      tx.update(cuentaRef, { saldo: nuevoSaldo });

      tx.create(movimientoRef, {
        cuentaId,
        tipo,
        monto,
        categoria: categoria.trim(),
        fecha,
        nota: (nota || "").trim(),
        comercio: (comercio || "").trim(),
        // Fecha inmutable asignada por el servidor para misiones.
        registradoEn: Date.now()
      });

      return {
        movimientoId: movimientoRef.id,
        nuevoSaldo
      };
    });

    // El movimiento ya fue guardado: sincronizar sin deshacerlo si hay un error.
    let misionesSincronizadas = false;
    try {
      await sincronizarMisionesUsuario(usuarioRef);
      misionesSincronizadas = true;
    } catch (errorSincronizacion) {
      console.error("Movimiento guardado; error sincronizando XP:", errorSincronizacion);
    }

    return res.status(201).json({
      ok: true,
      mensaje: "Movimiento guardado correctamente.",
      misionesSincronizadas,
      ...resultado
    });

  } catch (error) {
    console.error("Error creando movimiento:", error);

    return res.status(error.status || 500).json({
      ok: false,
      mensaje: error.status
        ? error.message
        : "No se pudo guardar el movimiento."
    });
  }
});

 // ============================================
 // APORTAR DINERO A UNA META
 // ============================================

app.post("/api/metas/aportar", autenticar, async (req, res) => {
  try {
    const uid = req.usuarioFirebase.uid;
    const { metaId, cuentaId, monto, solicitudId } = req.body || {};

    const idValido = (valor) =>
      typeof valor === "string" &&
      /^[A-Za-z0-9]{20}$/.test(valor);

    if (
      !idValido(metaId) ||
      !idValido(cuentaId) ||
      !idValido(solicitudId) ||
      typeof monto !== "number" ||
      !Number.isFinite(monto) ||
      monto <= 0 ||
      monto > 1000000 ||
      Math.abs(monto * 100 - Math.round(monto * 100)) > 0.000001
    ) {
      return res.status(400).json({
        ok: false,
        mensaje: "Datos del aporte no validos."
      });
    }

    const usuarioRef = db.collection("usuarios").doc(uid);
    const metaRef = usuarioRef.collection("metas").doc(metaId);
    const cuentaRef = usuarioRef.collection("cuentas").doc(cuentaId);

    const aporteRef = metaRef.collection("aportes").doc(solicitudId);
    const movimientoRef = usuarioRef.collection("movimientos").doc(solicitudId);

    const centavos = Math.round(monto * 100);

    const resultado = await db.runTransaction(async (tx) => {
      const [
        usuarioSnap,
        metaSnap,
        cuentaSnap,
        aporteSnap,
        movimientoSnap
      ] = await Promise.all([
        tx.get(usuarioRef),
        tx.get(metaRef),
        tx.get(cuentaRef),
        tx.get(aporteRef),
        tx.get(movimientoRef)
      ]);

      // Evitar aportes duplicados.
      if (aporteSnap.exists) {
        const previo = aporteSnap.data();

        if (
          previo.cuentaId !== cuentaId ||
          Math.round(previo.monto * 100) !== centavos ||
          !movimientoSnap.exists
        ) {
          const error = new Error(
            "Identificador de solicitud ya utilizado."
          );
          error.status = 409;
          throw error;
        }

        return {
          aporteId: solicitudId,
          repetido: true
        };
      }

      if (movimientoSnap.exists) {
        const error = new Error(
          "Identificador de solicitud ya utilizado."
        );
        error.status = 409;
        throw error;
      }

      if (
        !usuarioSnap.exists ||
        !metaSnap.exists ||
        !cuentaSnap.exists
      ) {
        const error = new Error(
          "Usuario, meta o cuenta inexistente."
        );
        error.status = 404;
        throw error;
      }

      if (cuentaSnap.get("activa") !== true) {
        const error = new Error("La cuenta no esta activa.");
        error.status = 409;
        throw error;
      }

      const saldo = cuentaSnap.get("saldo");
      const actual = metaSnap.get("actual");
      const objetivo = metaSnap.get("objetivo");

      if (
        ![saldo, actual, objetivo].every(
          (v) => typeof v === "number" && Number.isFinite(v)
        ) ||
        saldo < 0 ||
        actual < 0 ||
        objetivo <= 0 ||
        actual > objetivo
      ) {
        const error = new Error(
          "Los saldos registrados no son validos."
        );
        error.status = 409;
        throw error;
      }

      const saldoCentavos = Math.round(saldo * 100);
      const actualCentavos = Math.round(actual * 100);
      const objetivoCentavos = Math.round(objetivo * 100);

      if (saldoCentavos < centavos) {
        const error = new Error(
          "Saldo insuficiente en la cuenta seleccionada."
        );
        error.status = 409;
        throw error;
      }

      if (actualCentavos + centavos > objetivoCentavos) {
        const error = new Error(
          "El aporte supera el monto pendiente de la meta."
        );
        error.status = 409;
        throw error;
      }

      const nuevoSaldo = (saldoCentavos - centavos) / 100;
      const nuevoActual = (actualCentavos + centavos) / 100;
      const fecha = Date.now();

      // Descontar dinero de la cuenta.
      tx.update(cuentaRef, {
        saldo: nuevoSaldo
      });

      // Aumentar el ahorro de la meta.
      tx.update(metaRef, {
        actual: nuevoActual,
        completada: actualCentavos + centavos >= objetivoCentavos
      });

      // Registrar el aporte.
      tx.create(aporteRef, {
        monto: centavos / 100,
        fecha,
        nota: "Aporte desde cuenta",
        cuentaId,
        movimientoId: solicitudId
      });

      // Registrar transferencia en movimientos.
      tx.create(movimientoRef, {
        cuentaId,
        tipo: "Transferencia",
        monto: centavos / 100,
        categoria: "Ahorro para meta",
        fecha,
        nota: `Aporte a meta ${metaId}`,
        comercio: "",
        metaId,
        aporteId: solicitudId,
        registradoEn: fecha
      });

      return {
        aporteId: solicitudId,
        nuevoSaldo,
        nuevoActual,
        repetido: false
      };
    });

    return res.json({
      ok: true,
      mensaje: "Aporte guardado correctamente.",
      ...resultado
    });

  } catch (error) {
    console.error("Error aportando a meta:", error);

    return res.status(error.status || 500).json({
      ok: false,
      mensaje: error.status
        ? error.message
        : "No se pudo registrar el aporte."
    });
  }
});


// ============================================
// CREAR META CON AHORRO INICIAL DESDE UNA CUENTA
// ============================================
app.post("/api/metas/crear-con-ahorro", autenticar, async (req, res) => {
  try {
    const uid = req.usuarioFirebase.uid;
    const { nombre, objetivo, inicial, fechaLimite, icono, cuentaId, solicitudId } = req.body || {};
    const idValido = x => typeof x === "string" && /^[A-Za-z0-9]{20}$/.test(x);
    const dineroValido = x => typeof x === "number" && Number.isFinite(x) &&
      x >= 0 && x <= 1_000_000_000 && Math.abs(x * 100 - Math.round(x * 100)) < 0.000001;

    if (typeof nombre !== "string" || !nombre.trim() || nombre.length > 60 ||
        !dineroValido(objetivo) || objetivo <= 0 || !dineroValido(inicial) ||
        inicial <= 0 || inicial > objetivo ||
        !Number.isSafeInteger(fechaLimite) || fechaLimite < 0 ||
        typeof icono !== "string" || !icono || icono.length > 20 ||
        !idValido(cuentaId) || !idValido(solicitudId)) {
      return res.status(400).json({ ok: false, mensaje: "Datos de meta inválidos." });
    }

    const usuarioRef = db.collection("usuarios").doc(uid);
    const cuentaRef = usuarioRef.collection("cuentas").doc(cuentaId);
    // ID estable: repetir la solicitud no vuelve a descontar dinero.
    const metaRef = usuarioRef.collection("metas").doc(solicitudId);
    const aporteRef = metaRef.collection("aportes").doc(solicitudId);
    const movimientoRef = usuarioRef.collection("movimientos").doc(solicitudId);
    const montoCentavos = Math.round(inicial * 100);

    const resultado = await db.runTransaction(async tx => {
      const [usuarioSnap, cuentaSnap, metaSnap, movimientoSnap] = await Promise.all([
        tx.get(usuarioRef), tx.get(cuentaRef), tx.get(metaRef), tx.get(movimientoRef)
      ]);
      if (metaSnap.exists) {
        if (metaSnap.get("solicitudId") === solicitudId &&
            metaSnap.get("cuentaInicialId") === cuentaId &&
            metaSnap.get("nombre") === nombre.trim() &&
            Math.round(metaSnap.get("objetivo") * 100) === Math.round(objetivo * 100) &&
            Math.round(metaSnap.get("actual") * 100) >= montoCentavos &&
            movimientoSnap.exists && movimientoSnap.get("metaId") === solicitudId &&
            Math.round(movimientoSnap.get("monto") * 100) === montoCentavos) {
          return { metaId: solicitudId, repetido: true };
        }
        const e = new Error("La solicitud ya fue utilizada con otros datos.");
        e.status = 409;
        throw e;
      }
      if (movimientoSnap.exists) {
        const e = new Error("Identificador ya utilizado.");
        e.status = 409;
        throw e;
      }
      if (!usuarioSnap.exists || !cuentaSnap.exists) {
        const e = new Error("Usuario o cuenta inexistente.");
        e.status = 404;
        throw e;
      }
      if (cuentaSnap.get("activa") !== true) {
        const e = new Error("La cuenta está inactiva.");
        e.status = 409;
        throw e;
      }
      const saldo = cuentaSnap.get("saldo");
      if (typeof saldo !== "number" || !Number.isFinite(saldo) || saldo < 0) {
        const e = new Error("Saldo de cuenta inválido.");
        e.status = 409;
        throw e;
      }
      const saldoCentavos = Math.round(saldo * 100);
      if (saldoCentavos < montoCentavos) {
        const e = new Error("Saldo insuficiente.");
        e.status = 409;
        throw e;
      }
      const fecha = Date.now();
      tx.update(cuentaRef, { saldo: (saldoCentavos - montoCentavos) / 100 });
      tx.create(metaRef, {
        nombre: nombre.trim(), objetivo, actual: montoCentavos / 100,
        fechaLimite, icono, creadaEn: fecha,
        completada: montoCentavos >= Math.round(objetivo * 100),
        solicitudId, cuentaInicialId: cuentaId
      });
      tx.create(aporteRef, {
        monto: montoCentavos / 100, fecha, nota: "Ahorro inicial",
        cuentaId, movimientoId: solicitudId
      });
      tx.create(movimientoRef, {
        cuentaId, tipo: "Transferencia", monto: montoCentavos / 100,
        categoria: "Ahorro para meta", fecha,
        nota: `Ahorro inicial para ${nombre.trim()}`, comercio: "",
        metaId: solicitudId, aporteId: solicitudId, registradoEn: fecha
      });
      return { metaId: solicitudId, repetido: false };
    });
    return res.status(201).json({ ok: true, mensaje: "Meta creada y ahorro descontado.", ...resultado });
  } catch (error) {
    console.error("Error creando meta con ahorro:", error);
    return res.status(error.status || 500).json({
      ok: false, mensaje: error.status ? error.message : "No se pudo crear la meta."
    });
  }
});

// ============================================
// RECOMPENSAS VISUALES: SINCRONIZACION SEGURA
// ============================================
// No se otorgan XP aqui: las misiones ya administran la experiencia.
// Las insignias se guardan una sola vez y no se revocan.

app.post("/api/recompensas/sincronizar", autenticar, async (req, res) => {
  try {
    const usuarioRef = db.collection("usuarios").doc(req.usuarioFirebase.uid);
    const [usuarioSnap, metasSnap, movimientosSnap, misionesSnap] = await Promise.all([
      usuarioRef.get(),
      usuarioRef.collection("metas").get(),
      usuarioRef.collection("movimientos").get(),
      usuarioRef.collection("recompensas_otorgadas").get()
    ]);

    if (!usuarioSnap.exists) {
      return res.status(404).json({ ok: false, mensaje: "No existe el perfil del estudiante." });
    }

    const nivel = Number(usuarioSnap.get("nivel")) || 1;
    const misionesCompletadas = new Set(misionesSnap.docs.map(doc => doc.id));
    const ahorroEnMetasCentavos = metasSnap.docs.reduce((total, doc) => {
      const actual = doc.get("actual");
      return total + (typeof actual === "number" && Number.isFinite(actual) && actual > 0
        ? Math.round(actual * 100) : 0);
    }, 0);
    const hayMetaCompletada = metasSnap.docs.some(doc => {
      const actual = doc.get("actual");
      const objetivo = doc.get("objetivo");
      return doc.get("completada") === true ||
        (typeof actual === "number" && typeof objetivo === "number" &&
         Number.isFinite(actual) && Number.isFinite(objetivo) && objetivo > 0 && actual >= objetivo);
    });

    // Usar fecha del movimiento, registrada en milisegundos.
    // Se cuentan fechas distintas de gastos validos.
    const diasGasto = new Set();
    for (const doc of movimientosSnap.docs) {
      const mov = doc.data();
      if (mov.tipo !== "Gasto" || typeof mov.monto !== "number" ||
          !Number.isFinite(mov.monto) || mov.monto <= 0 ||
          !Number.isSafeInteger(mov.fecha) || mov.fecha <= 0) continue;
      const fechaSV = new Date(mov.fecha - 6 * 60 * 60 * 1000);
      if (Number.isFinite(fechaSV.getTime())) diasGasto.add(fechaSV.toISOString().slice(0, 10));
    }

    const condiciones = {
      ahorrador: ahorroEnMetasCentavos >= 10000,
      constante: diasGasto.size >= 7,
      maestro: nivel >= 10,
      sombrero: misionesCompletadas.has("primer_ahorro_semanal"),
      lentes: nivel >= 3,
      corona: nivel >= 10,
      fondo_morado: misionesCompletadas.has("inversionista_novato"),
      fondo_verde: hayMetaCompletada
    };

    const desbloqueos = Object.keys(condiciones).filter(id => condiciones[id]);
    const resultado = await db.runTransaction(async tx => {
      const referencias = desbloqueos.map(id =>
        usuarioRef.collection("recompensas_desbloqueadas").doc(id));
      const existentes = await Promise.all(referencias.map(ref => tx.get(ref)));
      const nuevas = [];
      referencias.forEach((ref, index) => {
        if (!existentes[index].exists) {
          tx.create(ref, {
            desbloqueada: true,
            fechaDesbloqueo: admin.firestore.FieldValue.serverTimestamp(),
            validadaPor: "backend"
          });
          nuevas.push(ref.id);
        }
      });
      return nuevas;
    });

    return res.json({ ok: true, nuevas: resultado, condiciones, nivel,
      ahorroEnMetas: ahorroEnMetasCentavos / 100, diasGasto: diasGasto.size });
  } catch (error) {
    console.error("Error sincronizando recompensas:", error);
    return res.status(500).json({ ok: false, mensaje: "No se pudieron sincronizar las recompensas." });
  }
});

// ============================================
// INICIAR SERVIDOR
// ============================================

const PORT = process.env.PORT || 3001;

app.listen(PORT, () => {
  console.log(
    `FinanQuest backend funcionando en puerto ${PORT}`
  );
});
