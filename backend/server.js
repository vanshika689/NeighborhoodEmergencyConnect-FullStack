const express = require("express");
const dotenv = require("dotenv");
dotenv.config();
const app = express();
const connectDB = require("./config/db");

const authRoutes = require("./routes/authRoutes");
const alertRoutes = require("./routes/alertRoutes");
const dashboardRoutes = require("./routes/dashboardRoutes")
const uploadRoutes = require("./routes/uploadRoutes");




connectDB();

app.use(express.json());
const imagekit = require("./config/imagekit")

app.use("/api/auth", authRoutes);
app.use("/api/alerts", alertRoutes);
app.use("/api/dashboard",dashboardRoutes);
app.use("/api/upload", uploadRoutes);


app.listen(5000, "0.0.0.0" , () => {
    console.log("Server is running on port 5000");
});