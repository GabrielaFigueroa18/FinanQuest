
const express = require("express");

const router = express.Router();

// ==========================================
// API DE NOTICIAS FINANCIERAS - FINANQUEST SV
// ==========================================

router.get("/", async (req, res) => {
  try {
    const apiKey = process.env.GNEWS_API_KEY;

    if (!apiKey) {
      return res.status(500).json({
        ok: false,
        mensaje: "Falta configurar GNEWS_API_KEY"
      });
    }

    // Categorías de noticias financieras
    const temas = {
      todas: "(finanzas OR economía OR ahorro OR inversión)",
      ahorro: "(ahorro OR presupuesto)",
      economia: "(economía OR inflación)",
      inversiones: "(inversión OR bolsa)"
    };

    const tema = String(req.query.tema || "todas");
    const busqueda = String(req.query.q || "").trim();

    const consulta = busqueda
      ? busqueda.slice(0, 150)
      : (temas[tema] || temas.todas);

    // Dirección de la API GNews
    const url = new URL("https://gnews.io/api/v4/search");

    url.searchParams.set("q", consulta);
    url.searchParams.set("lang", "es");
    url.searchParams.set("max", "10");
    url.searchParams.set("sortby", "publishedAt");

    // Consultar noticias
    const respuesta = await fetch(url, {
      headers: {
        "X-Api-Key": apiKey
      },
      signal: AbortSignal.timeout(10000)
    });

    const datos = await respuesta.json();

    if (!respuesta.ok) {
      return res.status(502).json({
        ok: false,
        mensaje:
          datos.errors?.[0] ||
          "No se pudieron consultar las noticias"
      });
    }

    // Preparar noticias para Android
    const noticias = (datos.articles || []).map((articulo) => ({
      titulo: articulo.title || "",
      descripcion: articulo.description || "",
      imagen: articulo.image || "",
      url: articulo.url || "",
      fecha: articulo.publishedAt || "",
      fuente: articulo.source?.name || "Fuente desconocida"
    }));

    return res.json({
      ok: true,
      noticias
    });

  } catch (error) {
    console.error("Error consultando GNews:", error.message);

    return res.status(500).json({
      ok: false,
      mensaje: "Error de conexión con el servicio de noticias"
    });
  }
});

module.exports = router;
