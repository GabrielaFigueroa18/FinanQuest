require("dotenv").config();

const admin = require("firebase-admin");
const path = require("path");

if (!admin.apps.length) {
  let credential;

  if (process.env.FIREBASE_SERVICE_ACCOUNT) {
    // Render: credenciales desde variable de entorno
    const serviceAccount = JSON.parse(
      process.env.FIREBASE_SERVICE_ACCOUNT
    );

    credential = admin.credential.cert(serviceAccount);
  } else {
    // Computadora local
    const serviceAccount = require(
      path.join(__dirname, "serviceAccountKey.json")
    );

    credential = admin.credential.cert(serviceAccount);
  }

  admin.initializeApp({ credential });
}

const db = admin.firestore();

module.exports = { admin, db };