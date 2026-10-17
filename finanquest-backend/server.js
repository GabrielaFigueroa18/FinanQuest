
require("dotenv").config();

const express = require("express");
const cors = require("cors");

const app = express();

app.use(cors());
app.use(express.json());

app.get("/", (req, res) => {
  res.json({
    mensaje: "Bienvenido al backend de FinanQuest SV"
  });
});

app.get("/health", (req, res) => {
  res.json({
    ok: true,
    servicio: "FinanQuest Backend"
  });
});

const PORT = process.env.PORT || 3001;

app.listen(PORT, () => {
  console.log(`FinanQuest backend funcionando en puerto ${PORT}`);
});
