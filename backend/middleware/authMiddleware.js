const jwt = require("jsonwebtoken");

const authMiddleware = (req, res, next) => {
    try {
        const authHeader = req.headers.authorization;

        if (!authHeader) {
            return res.status(401).json({
                message: "No token provided"
            });
        }

        let token;

        // ✅ Check if it starts with "Bearer " or was passed as a raw token
        if (authHeader.startsWith("Bearer ")) {
            const parts = authHeader.trim().split(/\s+/);
            if (parts.length !== 2) {
                return res.status(401).json({
                    message: "Invalid authorization format"
                });
            }
            token = parts[1];
        } else {
            // ✅ Handles direct raw token without throwing format error
            token = authHeader.trim();
        }

        const decoded = jwt.verify(
            token,
            process.env.JWT_SECRET
        );

        req.user = decoded;
        next();

    } catch (error) {
        console.error("JWT Error:", error.message);
        return res.status(401).json({
            message: "Invalid token"
        });
    }
};

module.exports = authMiddleware;