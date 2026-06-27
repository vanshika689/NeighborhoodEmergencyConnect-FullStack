const { initializeApp, getApps, cert } = require("firebase-admin/app");

let serviceAccount;

if (process.env.FIREBASE_SERVICE_ACCOUNT) {
    // Production — Render
    serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
} else {
    // Local development
    serviceAccount = require("./serviceAccountKey.json");
}

// Initialize only if not already initialized
if (getApps().length === 0) {
    initializeApp({
        credential: cert(serviceAccount)
    });
}